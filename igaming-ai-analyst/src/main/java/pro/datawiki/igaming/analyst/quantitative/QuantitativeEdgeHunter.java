package pro.datawiki.igaming.analyst.quantitative;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.dto.ConvictionTier;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BetTypeRegistry;
import pro.datawiki.igaming.dto.Language;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.ml.MarketEdgeCandidateDto;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stage 2 Quantitative Valuation & Market Edge (+EV) Overlay Engine.
 * Formulates objective mathematical fair odds, edge hunting (+EV), Kelly staking,
 * and multi-scenario cushion hierarchies without odds-anchoring bias.
 * <p>
 * Modularized into single-responsibility collaborators:
 * - {@link ContinuousMatrixEdgeScanner} for +EV matrix discovery
 * - {@link MarketFairProbabilityResolver} for typed mathematical probability resolution
 * - {@link DossierOddsFallbackResolver} for metadata consensus fallback
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuantitativeEdgeHunter {

    public static final double MIN_RECOMMENDED_ODD = ContinuousMatrixEdgeScanner.MIN_RECOMMENDED_ODD;
    public static final double MAX_SANITY_EV_PCT = ContinuousMatrixEdgeScanner.MAX_SANITY_EV_PCT;

    private final RedisOddsConsensusService redisOddsConsensusService;
    private final pro.datawiki.igaming.analyst.service.odds.MarketOddsResolver marketOddsResolver;
    private final InstitutionalRiskPolicyManager policyManager;
    private final ContinuousMatrixEdgeScanner matrixEdgeScanner;
    private final MarketFairProbabilityResolver probabilityResolver;
    private final DossierOddsFallbackResolver fallbackOddsResolver;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository recommendationRepository;

    @Getter
    @Builder
    public static class ValuationResult {
        private final String convictionTier;
        private final String rawTier;
        private final String recommendedMarket;
        private final BigDecimal recommendedOdd;
        private final String bestBookmaker;
        private final BigDecimal expectedValuePct;
        private final BigDecimal kellyStakePct;
        private final int confidenceScore;
        private final String coreHypothesis;
        private final String identifiedRisk;
        private final String tacticalRationale;
        private final String steamDirection;
        private final BigDecimal fairP1;
        private final BigDecimal fairPx;
        private final BigDecimal fairP2;
        private final BigDecimal fairOver25;
        private final BigDecimal fairUnder25;
        private final BigDecimal fairBttsYes;
        private final BigDecimal fairBttsNo;
        private final String expectedXg;
        private final InstitutionalRiskPolicyManager.PolicyCapResult appliedPolicyCap;
    }

    /**
     * Evaluates Pre-Lineup Fast Radar signal (Stage 1 screening).
     */
    public ValuationResult evaluatePreLineupRadar(
            Long matchId,
            MlPredictionDto ml,
            Map<String, Object> dossierMeta,
            Sport sport
    ) {
        if (ml == null || ml.getProbHome() == null || ml.getProbAway() == null) {
            log.warn("⚠️ [QuantitativeEdgeHunter] Missing ML probabilities for match #{}. Aborting valuation.", matchId);
            return null;
        }

        double p1 = ml.getProbHome().doubleValue();
        double px = ml.getProbDraw() != null ? ml.getProbDraw().doubleValue() : 0.0;
        double p2 = ml.getProbAway() != null ? ml.getProbAway().doubleValue() : 0.5;
        double pOver = ml.getProbOver() != null ? ml.getProbOver().doubleValue() : 0.5;
        double pUnder = ml.getProbUnder() != null ? ml.getProbUnder().doubleValue() : 0.5;
        Double expTotal = (dossierMeta != null && dossierMeta.get("expected_total_baseline") instanceof Number n)
                ? n.doubleValue() : null;

        int matchBestOf = resolveBestOf(dossierMeta);

        pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate recCandidate = (sport != null)
                ? sport.resolveRecommendedMarket(p1, px, p2, pOver, pUnder, expTotal, matchBestOf)
                : new pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate("WIN1", p1, "DEFAULT");

        Optional<MarketEdgeCandidateDto> matrixEdgeOpt = findBestMatrixEdge(matchId, ml, sport, matchBestOf, dossierMeta);
        String targetMarket = matrixEdgeOpt.map(MarketEdgeCandidateDto::getMarket).orElse(recCandidate.market());

        if (sport != null && !sport.isMarketSupported(targetMarket, matchBestOf)) {
            targetMarket = "WIN1";
        }

        BetType targetBet = BetTypeRegistry.fromCode(targetMarket);
        boolean isDrawRadar = (targetBet instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW);
        double fairProb = probabilityResolver.resolveFairProbability(ml, targetBet, sport, matchBestOf, expTotal);
        String tier = ml.getConfidenceTier();

        if (tier == null || tier.isBlank() || "PASS".equalsIgnoreCase(tier)) {
            tier = ConvictionTier.evaluateFromProb(fairProb, isDrawRadar).getCode();
        }

        QuoteInfo quoteInfo = resolveBestQuote(matchId, targetMarket, dossierMeta);
        if (quoteInfo == null && matchId != null && recommendationRepository != null) {
            List<pro.datawiki.igaming.analyst.entity.MatchAiRecommendation> prevRecs = recommendationRepository.findAllByMatchId(matchId);
            for (pro.datawiki.igaming.analyst.entity.MatchAiRecommendation prev : prevRecs) {
                if (prev.getRecommendedOdd() != null && prev.getRecommendedOdd().doubleValue() > 1.01
                        && prev.getRecommendedMarket() != null && !"PASS".equalsIgnoreCase(prev.getRecommendedMarket())) {
                    targetMarket = prev.getRecommendedMarket();
                    targetBet = BetTypeRegistry.fromCode(targetMarket);
                    fairProb = probabilityResolver.resolveFairProbability(ml, targetBet, sport, matchBestOf, expTotal);
                    isDrawRadar = (targetBet instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW);
                    tier = ConvictionTier.evaluateFromProb(fairProb, isDrawRadar).getCode();
                    String bk = prev.getBestBookmaker() != null ? prev.getBestBookmaker() : ("Historical (" + prev.getModelVersion() + ")");
                    quoteInfo = new QuoteInfo(prev.getRecommendedOdd().doubleValue(), bk);
                    log.info("Match #{}: recovered historical quote for market '{}' @ {} from {}",
                            matchId, targetMarket, quoteInfo.odd(), bk);
                    break;
                }
            }
        }
        if (quoteInfo == null) {
            return buildMissingQuotePassResult(fairProb, targetMarket, ml,
                    String.format("🔮 ПРЕДВАРИТЕЛЬНЫЙ РАДАР: Котировки букмекеров отсутствуют (%s | P=%.1f%%)", targetMarket, fairProb),
                    "В сканере отсутствуют подтвержденные котировки букмекеров на данный исход. Ставка невозможна.");
        }

        double marketOddVal = quoteInfo.odd;
        double fairOdd = 100.0 / Math.max(1.0, fairProb);

        // Institutional Tier Cap based on Odds and Sport Volatility
        InstitutionalRiskPolicyManager.PolicyCapResult capResult = policyManager.evaluatePrudenceCaps(sport, targetMarket, marketOddVal, tier);
        tier = capResult.getAppliedTier();

        double evPct = ((marketOddVal * (fairProb / 100.0)) - 1.0) * 100.0;
        double stakePct = ConvictionTier.fromCode(tier).getRadarStakePct();
        if (marketOddVal < MIN_RECOMMENDED_ODD || evPct < 0.0) {
            stakePct = 1.0;
        }

        String reasonCode = ml.getDecisionReasonCode() != null ? ml.getDecisionReasonCode() : "PRE_LINEUP_RADAR";
        String coreHypothesis = String.format("🔮 ПРЕДВАРИТЕЛЬНЫЙ РАДАР ПОТЕНЦИАЛА: %s (%s | P=%.1f%%, Fair Odds: %.2f)",
                reasonCode, targetMarket, fairProb, fairOdd);

        int window = sport != null ? sport.getLineupConfirmationWindowMinutes() : 45;
        String risk = "Стартовые составы ещё не опубликованы. Окончательная верификация за " + window + " минут до матча.";
        if (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty()) {
            risk = "⚠️ Риск ротации ключевых игроков: " + String.join("; ", ml.getKeyPlayerDependencies());
        }

        return ValuationResult.builder()
                .convictionTier(tier)
                .rawTier(capResult.getRawTier())
                .recommendedMarket(targetMarket)
                .recommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP))
                .bestBookmaker(quoteInfo.bookmaker != null ? quoteInfo.bookmaker : "NONE")
                .expectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0))
                .kellyStakePct(BigDecimal.valueOf(stakePct).setScale(2, RoundingMode.HALF_UP))
                .confidenceScore((int) Math.round(fairProb))
                .coreHypothesis(coreHypothesis)
                .identifiedRisk(risk)
                .tacticalRationale(null)
                .fairP1(ml.getProbHome())
                .fairPx(ml.getProbDraw())
                .fairP2(ml.getProbAway())
                .fairOver25(ml.getProbOver())
                .fairUnder25(ml.getProbUnder())
                .appliedPolicyCap(capResult.isCapped() ? capResult : null)
                .build();
    }

    /**
     * Evaluates Stage 2 (Lineup Confirmed) Quantitative Institutional Valuation.
     */
    public ValuationResult evaluateLineupConfirmed(
            Long matchId,
            MlPredictionDto ml,
            Map<String, Object> dossierMeta,
            Sport sport,
            boolean isLineupConfirmed,
            long minutesToKickoff
    ) {
        if (ml == null || ml.getProbHome() == null || ml.getProbAway() == null) {
            log.warn("⚠️ [QuantitativeEdgeHunter] Missing ML probabilities for match #{}. Aborting valuation.", matchId);
            return null;
        }
        boolean isTeam = sport == null || sport.isTeamSport();
        double p1 = ml.getProbHome().doubleValue();
        double px = ml.getProbDraw() != null ? ml.getProbDraw().doubleValue() : 0.0;
        double p2 = ml.getProbAway() != null ? ml.getProbAway().doubleValue() : 0.5;
        double pOver = ml.getProbOver() != null ? ml.getProbOver().doubleValue() : 0.5;
        double pUnder = ml.getProbUnder() != null ? ml.getProbUnder().doubleValue() : 0.5;
        Double expTotal = (dossierMeta != null && dossierMeta.get("expected_total_baseline") instanceof Number n)
                ? n.doubleValue() : null;

        int matchBestOf = resolveBestOf(dossierMeta);

        pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate recCandidate = (sport != null)
                ? sport.resolveRecommendedMarket(p1, px, p2, pOver, pUnder, expTotal, matchBestOf)
                : new pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate("WIN1", p1, "DEFAULT");

        Optional<MarketEdgeCandidateDto> matrixEdgeOpt = findBestMatrixEdge(matchId, ml, sport, matchBestOf, dossierMeta);
        String targetMarket = matrixEdgeOpt.map(MarketEdgeCandidateDto::getMarket).orElse(recCandidate.market());

        if (sport != null && !sport.isMarketSupported(targetMarket, matchBestOf)) {
            targetMarket = (sport.hasDrawOutcome()) ? "DOUBLE_CHANCE_1X" : "WIN1";
        }

        int confScore = ml.getRecommendedProb() != null ? (int) Math.round(ml.getRecommendedProb().doubleValue() * 100.0) : 65;

        // Polymorphic protected market fallback (Double Chance 1X/X2, Set Handicap +1.5)
        // Strictly fixes cross-market probability pollution: evaluates outcome-specific probability rather than global max_prob
        BetType initialBet = BetTypeRegistry.fromCode(targetMarket);
        if (initialBet instanceof MatchResultBet mrb) {
            boolean isRawW1 = (mrb.outcome() == MatchResultBet.Outcome.WIN1 || mrb.outcome() == MatchResultBet.Outcome.WIN1_2WAY);
            boolean isRawW2 = (mrb.outcome() == MatchResultBet.Outcome.WIN2 || mrb.outcome() == MatchResultBet.Outcome.WIN2_2WAY);

            if ((isRawW1 || isRawW2) && sport != null) {
                int outcomeConfidence = isRawW1 ? (int) Math.round(p1 * 100.0) : (int) Math.round(p2 * 100.0);
                if (outcomeConfidence < 75) {
                    targetMarket = sport.resolveProtectedMarket(targetMarket, p1, px, p2, matchBestOf);
                    confScore = isRawW1 ? (int) Math.round(Math.min(95.0, (p1 + px) * 100.0)) : (int) Math.round(Math.min(95.0, (p2 + px) * 100.0));
                } else {
                    confScore = outcomeConfidence;
                }
            }
        }

        String reasonCode = ml.getDecisionReasonCode() != null ? ml.getDecisionReasonCode() : "QUANTITATIVE_SIGNAL";
        String riskText = buildRiskText(sport, isLineupConfirmed, minutesToKickoff, isTeam, ml);
        String hypothesisText = buildHypothesisText(sport, isLineupConfirmed, minutesToKickoff, isTeam, reasonCode, targetMarket, confScore);

        String tier = "PASS".equalsIgnoreCase(targetMarket) ? "PASS" : ml.getConfidenceTier();
        if (tier == null || tier.isBlank() || "PASS".equals(tier) || "NO_BET".equals(tier) || "ABSTAIN".equals(tier)) {
            tier = "ML_60";
        } else {
            tier = tier.trim().toUpperCase();
        }

        BetType evaluatedBet = BetTypeRegistry.fromCode(targetMarket);
        if (evaluatedBet instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW) {
            if (sport != null && !sport.hasDrawOutcome()) {
                targetMarket = (p1 >= p2) ? "WIN1" : "WIN2";
                log.info("Sport '{}' has no draw outcome. Re-routed to {} for match #{}", sport.getCode(), targetMarket, matchId);
                evaluatedBet = BetTypeRegistry.fromCode(targetMarket);
            }
        }

        if (sport != null && !sport.isMarketSupported(targetMarket)) {
            targetMarket = sport.getFallbackSupportedMarket(targetMarket);
            evaluatedBet = BetTypeRegistry.fromCode(targetMarket);
        }

        // Compute fair probability mathematically via typed resolver
        double fairProb = probabilityResolver.resolveFairProbability(ml, evaluatedBet, sport, matchBestOf, expTotal);
        boolean isDrawMarket = (evaluatedBet instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW);

        tier = assignConfidenceTier(fairProb, isDrawMarket);

        QuoteInfo quoteInfo = resolveBestQuote(matchId, targetMarket, dossierMeta);
        if (quoteInfo == null && matchId != null && recommendationRepository != null) {
            List<pro.datawiki.igaming.analyst.entity.MatchAiRecommendation> prevRecs = recommendationRepository.findAllByMatchId(matchId);
            for (pro.datawiki.igaming.analyst.entity.MatchAiRecommendation prev : prevRecs) {
                if (prev.getRecommendedOdd() != null && prev.getRecommendedOdd().doubleValue() > 1.01
                        && prev.getRecommendedMarket() != null && !"PASS".equalsIgnoreCase(prev.getRecommendedMarket())) {
                    targetMarket = prev.getRecommendedMarket();
                    evaluatedBet = BetTypeRegistry.fromCode(targetMarket);
                    fairProb = probabilityResolver.resolveFairProbability(ml, evaluatedBet, sport, matchBestOf, expTotal);
                    isDrawMarket = (evaluatedBet instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW);
                    tier = assignConfidenceTier(fairProb, isDrawMarket);
                    String bk = prev.getBestBookmaker() != null ? prev.getBestBookmaker() : ("Historical (" + prev.getModelVersion() + ")");
                    quoteInfo = new QuoteInfo(prev.getRecommendedOdd().doubleValue(), bk);
                    log.info("Match #{}: recovered historical quote for market '{}' @ {} from {}",
                            matchId, targetMarket, quoteInfo.odd(), bk);
                    break;
                }
            }
        }
        if (quoteInfo == null) {
            return buildMissingQuotePassResult(fairProb, targetMarket, ml, hypothesisText,
                    "В сканере отсутствуют подтвержденные котировки букмекеров на данный исход. Ставка невозможна.");
        }

        double marketOddVal = quoteInfo.odd;
        String bestBookmaker = quoteInfo.bookmaker;

        double evPct = ((fairProb / 100.0) * marketOddVal - 1.0) * 100.0;

        // Apply Institutional Risk Prudence Caps
        InstitutionalRiskPolicyManager.PolicyCapResult capResult = policyManager.evaluatePrudenceCaps(sport, targetMarket, marketOddVal, tier);
        tier = capResult.getAppliedTier();

        // Kelly Criterion Stake Sizing
        double kellyPct = policyManager.calculateKellyStakePct(marketOddVal, fairProb, tier);
        BigDecimal kellyStake = BigDecimal.valueOf(kellyPct).setScale(2, RoundingMode.HALF_UP);

        int confidence = Math.max(confScore, (int) Math.round(fairProb));

        // Solo sport upset variance defense: halve Kelly stake if odd > 1.58 and confidence < 86
        if (policyManager != null) {
            BigDecimal disciplined = policyManager.applySoloSportUpsetDiscipline(sport, marketOddVal, confidence, kellyStake);
            if (disciplined != null) {
                kellyStake = disciplined;
            }
        }

        String rationale = null;
        boolean isSoloRacket = (sport != null && !sport.isTeamSport());
        BetType finalBet = BetTypeRegistry.fromCode(targetMarket);
        if (isSoloRacket && (finalBet instanceof HandicapBet hb && hb.param() != null && Math.abs(hb.param() - 1.5) < 0.05)) {
            rationale = "🛡️ Дисциплина одиночных ракеточных видов: ставка перекрыта подушечной форой по сетам (+1.5) — взятие хотя бы одного сета гарантирует проход.";
        }

        return ValuationResult.builder()
                .convictionTier(tier)
                .rawTier(capResult.getRawTier())
                .recommendedMarket(targetMarket)
                .recommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP))
                .bestBookmaker(bestBookmaker != null ? bestBookmaker : "NONE")
                .expectedValuePct(BigDecimal.valueOf(evPct).setScale(2, RoundingMode.HALF_UP))
                .kellyStakePct(kellyStake)
                .confidenceScore(confidence)
                .coreHypothesis(hypothesisText)
                .identifiedRisk(riskText)
                .tacticalRationale(rationale)
                .steamDirection("NEUTRAL")
                .fairP1(ml.getProbHome())
                .fairPx(ml.getProbDraw())
                .fairP2(ml.getProbAway())
                .fairOver25(ml.getProbOver())
                .fairUnder25(ml.getProbUnder())
                .fairBttsYes(null)
                .fairBttsNo(null)
                .expectedXg(null)
                .appliedPolicyCap(capResult.isCapped() ? capResult : null)
                .build();
    }

    public double resolveFairProbability(MlPredictionDto ml, String market, Sport sport) {
        return probabilityResolver.resolveFairProbability(ml, market, sport);
    }

    public double resolveFairProbability(MlPredictionDto ml, String market, Sport sport, int bestOf, Double expTotal) {
        return probabilityResolver.resolveFairProbability(ml, market, sport, bestOf, expTotal);
    }

    public Double fallbackOddFromDossier(Map<String, Object> dossierMeta, String targetMarket) {
        return fallbackOddsResolver.fallbackOddFromDossier(dossierMeta, targetMarket);
    }

    public Optional<MarketEdgeCandidateDto> findBestMatrixEdge(
            Long matchId,
            MlPredictionDto ml,
            Sport sport,
            int bestOf,
            Map<String, Object> dossierMeta
    ) {
        return matrixEdgeScanner.findBestMatrixEdge(matchId, ml, sport, bestOf, dossierMeta);
    }

    private record QuoteInfo(double odd, String bookmaker) {}

    private QuoteInfo resolveBestQuote(Long matchId, String market, Map<String, Object> dossierMeta) {
        RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, market);
        if (quote != null && quote.value() > 1.01) {
            return new QuoteInfo(quote.value(), quote.bookmaker());
        }
        Double dossierOdd = fallbackOddsResolver.fallbackOddFromDossier(dossierMeta, market);
        if (dossierOdd != null && dossierOdd > 1.01) {
            return new QuoteInfo(dossierOdd, "Dossier Consensus");
        }
        if (matchId != null && recommendationRepository != null) {
            List<pro.datawiki.igaming.analyst.entity.MatchAiRecommendation> prevRecs = recommendationRepository.findAllByMatchId(matchId);
            for (pro.datawiki.igaming.analyst.entity.MatchAiRecommendation prev : prevRecs) {
                if (prev.getRecommendedOdd() != null && prev.getRecommendedOdd().doubleValue() > 1.01) {
                    if (market != null && market.equalsIgnoreCase(prev.getRecommendedMarket())) {
                        String bk = prev.getBestBookmaker() != null ? prev.getBestBookmaker() : ("Historical (" + prev.getModelVersion() + ")");
                        return new QuoteInfo(prev.getRecommendedOdd().doubleValue(), bk);
                    }
                }
            }
        }
        return null;
    }

    private int resolveBestOf(Map<String, Object> dossierMeta) {
        if (dossierMeta == null) return 3;
        return pro.datawiki.igaming.dto.CanonicalMatchHeaderDto.parseBestOfFromStrings(
                (String) dossierMeta.get("tournament_name"),
                (String) dossierMeta.get("league_name"),
                (String) dossierMeta.get("stage"),
                (String) dossierMeta.get("round")
        );
    }

    private String assignConfidenceTier(double fairProb, boolean isDraw) {
        if (isDraw) {
            if (fairProb >= 45.0) return "ML_90";
            if (fairProb >= 39.0) return "ML_80";
            if (fairProb >= 34.0) return "ML_70";
            return "ML_60";
        } else {
            if (fairProb >= 90.0) return "ML_90";
            if (fairProb >= 80.0) return "ML_80";
            if (fairProb >= 70.0) return "ML_70";
            return "ML_60";
        }
    }

    private ValuationResult buildMissingQuotePassResult(
            double fairProb, String market, MlPredictionDto ml, String hypothesis, String risk
    ) {
        return ValuationResult.builder()
                .convictionTier("PASS")
                .rawTier("PASS")
                .recommendedMarket("PASS")
                .recommendedOdd(BigDecimal.ONE)
                .bestBookmaker("NONE")
                .expectedValuePct(BigDecimal.ZERO)
                .kellyStakePct(BigDecimal.ZERO)
                .confidenceScore((int) Math.round(fairProb))
                .coreHypothesis(hypothesis)
                .identifiedRisk(risk)
                .tacticalRationale(null)
                .steamDirection("NEUTRAL")
                .fairP1(ml != null ? ml.getProbHome() : null)
                .fairPx(ml != null ? ml.getProbDraw() : null)
                .fairP2(ml != null ? ml.getProbAway() : null)
                .fairOver25(ml != null ? ml.getProbOver() : null)
                .fairUnder25(ml != null ? ml.getProbUnder() : null)
                .appliedPolicyCap(null)
                .build();
    }

    private String buildRiskText(Sport sport, boolean isLineupConfirmed, long minutesToKickoff, boolean isTeam, MlPredictionDto ml) {
        if (minutesToKickoff < -2) {
            return "Отчет зафиксирован к началу матча на основе официальных протоколов команд (не поминутный лайв).";
        }
        if (minutesToKickoff <= 1) {
            String venueSurface = sport != null ? sport.getVenueSurface(Language.RU) : "поле";
            return isLineupConfirmed
                    ? "⚡ Стартовый расчет: Состав на " + venueSurface + " верифицирован на 1-й минуте матча. Ротация учтена."
                    : "⚡ Стартовый расчет: Игра началась. Ротация и предматчевые параметры зафиксированы.";
        }
        if (isLineupConfirmed) {
            return "Сценарный анализ вероятных составов. " +
                    (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty()
                            ? String.join("; ", ml.getKeyPlayerDependencies())
                            : "Ротационная устойчивость подтверждена.");
        }
        if (!isTeam) {
            return "Индивидуальный матч: стартовые участники фиксированы, форма спортсменов и покрытие верифицированы перед матчем.";
        }
        int window = sport != null ? sport.getLineupConfirmationWindowMinutes() : 45;
        return String.format("Предматчевая верификация завершена: официальный протокол для лиги не публикуется. Расчет зафиксирован по предматчевой модели и консенсусу линий за %d минут до старта.", window);
    }

    private String buildHypothesisText(
            Sport sport, boolean isLineupConfirmed, long minutesToKickoff,
            boolean isTeam, String reasonCode, String targetMarket, int confScore
    ) {
        if (minutesToKickoff < -2) {
            return String.format("🟢 СТАРТОВЫЙ РАСЧЕТ (По свистку и протоколам): %s (%s | P=%d%%)", reasonCode, targetMarket, confScore);
        }
        if (minutesToKickoff <= 1) {
            String venueSurface = sport != null ? sport.getVenueSurface(Language.RU) : "поле";
            String lineupLabel = sport != null ? sport.getStartingLineupLabel(Language.RU) : "состав";
            return isLineupConfirmed
                    ? String.format("⚡ ВЕРИФИКАЦИЯ (1-я минута, реальный %s на %s): %s (%s | P=%d%%)", lineupLabel, venueSurface, reasonCode, targetMarket, confScore)
                    : String.format("⚡ ВЕРИФИКАЦИЯ (Старт игры): %s (%s | P=%d%%)", reasonCode, targetMarket, confScore);
        }
        if (isLineupConfirmed) {
            String lineupBadge = sport != null ? sport.getLineupConfirmedBadge(Language.RU) : "✅ Составы";
            return String.format("📋 АНАЛИЗ ЗАЯВЛЕННЫХ СОСТАВОВ (%s): %s (%s | P=%d%%)", lineupBadge.replace("✅ ", ""), reasonCode, targetMarket, confScore);
        }
        if (!isTeam) {
            return String.format("📊 ПРЕДМАТЧЕВЫЙ АНАЛИЗ ФОРМЫ СПОРТСМЕНОВ: %s (%s | P=%d%%)", reasonCode, targetMarket, confScore);
        }
        return String.format("⚡ ФИНАЛЬНЫЙ РАСЧЕТ ПЕРЕД СТАРТОМ: %s (%s | P=%d%%)", reasonCode, targetMarket, confScore);
    }
}
