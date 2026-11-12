package pro.datawiki.igaming.analyst.quantitative;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.dto.ConvictionTier;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.dto.ml.ContinuousProbabilityMatrixDto;
import pro.datawiki.igaming.dto.ml.MarketEdgeCandidateDto;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scans continuous probability matrices against live Redis bookmaker consensus
 * using strongly-typed {@link BetType} domain models to discover the highest +EV edge.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContinuousMatrixEdgeScanner {

    public static final double MIN_RECOMMENDED_ODD = 1.30;
    public static final double MAX_SANITY_EV_PCT = 22.0;

    private final RedisOddsConsensusService redisOddsConsensusService;
    private final InstitutionalRiskPolicyManager policyManager;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository recommendationRepository;

    public Optional<MarketEdgeCandidateDto> findBestMatrixEdge(
            Long matchId,
            MlPredictionDto ml,
            Sport sport,
            int bestOf,
            Map<String, Object> dossierMeta
    ) {
        if (ml == null || ml.getProbabilityMatrix() == null) {
            return Optional.empty();
        }

        ContinuousProbabilityMatrixDto matrix = ml.getProbabilityMatrix();
        List<MarketEdgeCandidateDto> candidates = new ArrayList<>();

        // 1. Scan Totals (Over / Under) via TotalBet domain records
        if (matrix.getTotalOverProbabilities() != null) {
            for (Map.Entry<Double, Double> entry : matrix.getTotalOverProbabilities().entrySet()) {
                double line = entry.getKey();
                double pOver = entry.getValue() * 100.0;
                double pUnder = matrix.getUnderProbability(line) * 100.0;

                TotalBet overBet = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null);
                TotalBet underBet = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null);

                checkAndAddCandidate(matchId, overBet, "Тотал больше " + line, "TOTAL", line, pOver, candidates, sport, bestOf);
                checkAndAddCandidate(matchId, underBet, "Тотал меньше " + line, "TOTAL", line, pUnder, candidates, sport, bestOf);
            }
        }

        // 2. Scan Asian Handicaps via HandicapBet domain records
        if (matrix.getHandicapHomeProbabilities() != null) {
            for (Map.Entry<Double, Double> entry : matrix.getHandicapHomeProbabilities().entrySet()) {
                double line = entry.getKey();
                double pHome = entry.getValue() * 100.0;
                double pAway = matrix.getHandicapAwayProbability(-line) * 100.0;

                HandicapBet homeBet = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, true, null);
                HandicapBet awayBet = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, -line, true, null);

                String homeLabel = "Фора 1 (" + (line > 0 ? "+" + line : line) + ")";
                String awayLabel = "Фора 2 (" + (-line > 0 ? "+" + (-line) : (-line)) + ")";

                checkAndAddCandidate(matchId, homeBet, homeLabel, "HANDICAP", line, pHome, candidates, sport, bestOf);
                checkAndAddCandidate(matchId, awayBet, awayLabel, "HANDICAP", -line, pAway, candidates, sport, bestOf);
            }
        }

        // 3. Scan Match Result via MatchResultBet domain records
        double p1 = toPct(ml.getProbHome(), 50.0);
        double px = toPct(ml.getProbDraw(), 25.0);
        double p2 = toPct(ml.getProbAway(), 50.0);

        MatchResultBet win1 = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null);
        MatchResultBet win2 = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null);
        checkAndAddCandidate(matchId, win1, "Победа 1", "1X2", null, p1, candidates, sport, bestOf);
        checkAndAddCandidate(matchId, win2, "Победа 2", "1X2", null, p2, candidates, sport, bestOf);

        if (sport == null || sport.hasDrawOutcome()) {
            MatchResultBet dc1x = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            MatchResultBet dcx2 = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            checkAndAddCandidate(matchId, dc1x, "1X (Победа 1 или ничья)", "1X2", null, Math.min(95.0, p1 + px), candidates, sport, bestOf);
            checkAndAddCandidate(matchId, dcx2, "X2 (Ничья или победа 2)", "1X2", null, Math.min(95.0, p2 + px), candidates, sport, bestOf);
        }

        // 4. Scan Both Teams to Score via BinaryMarketBet domain records
        if (matrix.getBttsYesProb() > 0) {
            BinaryMarketBet bttsYes = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            BinaryMarketBet bttsNo = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            checkAndAddCandidate(matchId, bttsYes, "Обе забьют: Да", "BTTS", null, matrix.getBttsYesProb() * 100.0, candidates, sport, bestOf);
            checkAndAddCandidate(matchId, bttsNo, "Обе забьют: Нет", "BTTS", null, matrix.getBttsNoProb() * 100.0, candidates, sport, bestOf);
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // Sort by Expected Value (+EV) descending
        candidates.sort((a, b) -> b.getExpectedValuePct().compareTo(a.getExpectedValuePct()));

        // Return candidate with highest +EV that meets min odds (1.30), min fair probability (52.0%), and EV sanity cap (<= 22.0%)
        return candidates.stream()
                .filter(c -> c.getMarketOdd().doubleValue() >= MIN_RECOMMENDED_ODD
                        && c.getFairProbabilityPct() >= 52.0
                        && c.getExpectedValuePct().doubleValue() >= 4.0
                        && c.getExpectedValuePct().doubleValue() <= MAX_SANITY_EV_PCT)
                .findFirst();
    }

    private void checkAndAddCandidate(
            Long matchId, BetType betType, String label, String category, Double line,
            double fairProb, List<MarketEdgeCandidateDto> candidates, Sport sport, int bestOf
    ) {
        String marketCode = betType.code();
        if (sport != null && !sport.isMarketSupported(marketCode, bestOf)) return;
        if (fairProb < 45.0) return;

        RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, marketCode);
        if ((quote == null || quote.value() <= 1.01) && recommendationRepository != null && matchId != null) {
            List<pro.datawiki.igaming.analyst.entity.MatchAiRecommendation> prevRecs = recommendationRepository.findAllByMatchId(matchId);
            for (pro.datawiki.igaming.analyst.entity.MatchAiRecommendation prev : prevRecs) {
                if (prev.getRecommendedOdd() != null && prev.getRecommendedOdd().doubleValue() > 1.01) {
                    if (marketCode.equalsIgnoreCase(prev.getRecommendedMarket())) {
                        quote = new RedisOddsConsensusService.MarketQuote(
                                prev.getRecommendedOdd().doubleValue(),
                                prev.getBestBookmaker() != null ? prev.getBestBookmaker() : "Historical"
                        );
                        break;
                    }
                }
            }
        }
        if (quote == null || quote.value() <= 1.01 || quote.value() < MIN_RECOMMENDED_ODD) return;

        double odd = quote.value();
        double ev = ((fairProb / 100.0) * odd - 1.0) * 100.0;
        if (ev <= 0.0) return; // Discard negative EV
        if (ev > MAX_SANITY_EV_PCT) return; // Discard palpable outlier fake EV (> 22.0%)

        boolean isDraw = (betType instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW);
        String tier = ConvictionTier.evaluateFromProb(fairProb, isDraw).getCode();
        double kelly = policyManager.calculateKellyStakePct(odd, fairProb, tier);

        boolean isSafeCushion = (betType instanceof MatchResultBet mrb && (mrb.outcome() == MatchResultBet.Outcome.DC_1X || mrb.outcome() == MatchResultBet.Outcome.DC_X2))
                || (betType instanceof HandicapBet hb && hb.param() != null && (Math.abs(hb.param() - 1.5) < 0.05 || Math.abs(hb.param() - 2.5) < 0.05));

        candidates.add(MarketEdgeCandidateDto.builder()
                .market(marketCode)
                .marketLabel(label)
                .category(category)
                .line(line)
                .fairProbabilityPct(Math.round(fairProb * 10.0) / 10.0)
                .fairOdd(BigDecimal.valueOf(100.0 / Math.max(1.0, fairProb)).setScale(2, RoundingMode.HALF_UP))
                .marketOdd(BigDecimal.valueOf(odd).setScale(3, RoundingMode.HALF_UP))
                .bestBookmaker(quote.bookmaker())
                .expectedValuePct(BigDecimal.valueOf(ev).setScale(2, RoundingMode.HALF_UP))
                .kellyStakePct(BigDecimal.valueOf(kelly).setScale(2, RoundingMode.HALF_UP))
                .convictionTier(tier)
                .isSafeCushion(isSafeCushion)
                .build());
    }

    private double toPct(BigDecimal val, double fallback) {
        if (val == null) return fallback;
        double d = val.doubleValue();
        return (d > 0.0 && d <= 1.0) ? d * 100.0 : d;
    }
}
