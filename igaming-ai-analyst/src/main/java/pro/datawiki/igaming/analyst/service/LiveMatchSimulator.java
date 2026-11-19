package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.dto.ml.ContinuousProbabilityMatrixDto;
import pro.datawiki.igaming.dto.ml.LiveInPlayPredictionDto;
import pro.datawiki.igaming.dto.ml.LiveMatchSnapshotDto;
import pro.datawiki.igaming.dto.ml.MarketEdgeCandidateDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Fast in-memory In-Play Simulator for live matches ("Bar Scenario").
 * Evaluates remaining-time probabilities, scenarios, and live +EV entry points.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveMatchSimulator {

    private final MatchDossierService dossierService;
    private final MlInferenceClient mlInferenceClient;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final InstitutionalRiskPolicyManager policyManager;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Executes In-Play simulation and live edge discovery for a match.
     */
    public Optional<LiveInPlayPredictionDto> simulateLiveMatch(Long matchId, LiveMatchSnapshotDto providedSnapshot) {
        if (matchId == null) return Optional.empty();

        Map<String, Object> dossierMeta = dossierService.getMatchDossier(matchId);
        String t1 = dossierMeta != null && dossierMeta.get("team1_name") != null ? (String) dossierMeta.get("team1_name") : "Хозяева";
        String t2 = dossierMeta != null && dossierMeta.get("team2_name") != null ? (String) dossierMeta.get("team2_name") : "Гости";
        String sportSlug = dossierMeta != null && dossierMeta.get("sport_name") != null ? (String) dossierMeta.get("sport_name") : "football";
        Sport sport = Sports.get(sportSlug);

        LiveMatchSnapshotDto snapshot = providedSnapshot;
        if (snapshot == null) {
            snapshot = buildFallbackSnapshot(matchId, dossierMeta);
        }

        log.info("Running In-Play Live Simulation for match #{} ({} vs {}): minute={}, score={}:{}",
                matchId, t1, t2, snapshot.getMinute(), snapshot.getScoreHome(), snapshot.getScoreAway());

        LiveInPlayPredictionDto prediction = mlInferenceClient.livePredict(snapshot, sportSlug, t1, t2);
        if (prediction == null) {
            return Optional.empty();
        }

        // Overlay live market edges from Redis consensus
        List<MarketEdgeCandidateDto> liveEdges = scanLiveMarketEdges(matchId, snapshot, prediction.getRemainingMatrix(), sport);
        prediction.setTopLiveEdges(liveEdges);

        return Optional.of(prediction);
    }

    private List<MarketEdgeCandidateDto> scanLiveMarketEdges(
            Long matchId, LiveMatchSnapshotDto snapshot, ContinuousProbabilityMatrixDto remainingMatrix, Sport sport
    ) {
        if (remainingMatrix == null) return Collections.emptyList();
        List<MarketEdgeCandidateDto> edges = new ArrayList<>();
        int curGoals = snapshot.getScoreHome() + snapshot.getScoreAway();

        // 1. Scan remaining totals
        if (remainingMatrix.getTotalOverProbabilities() != null) {
            for (Map.Entry<Double, Double> entry : remainingMatrix.getTotalOverProbabilities().entrySet()) {
                double remLine = entry.getKey();
                double fullLine = curGoals + remLine;
                double pOver = entry.getValue() * 100.0;
                double pUnder = remainingMatrix.getUnderProbability(remLine) * 100.0;

                evaluateLiveCandidate(matchId, "TOTAL_OVER_" + fullLine, "Тотал больше " + fullLine, "TOTAL", fullLine, pOver, edges);
                evaluateLiveCandidate(matchId, "TOTAL_UNDER_" + fullLine, "Тотал меньше " + fullLine, "TOTAL", fullLine, pUnder, edges);
            }
        }

        // 2. Scan BTTS if a clean sheet is still ongoing
        if (snapshot.getScoreHome() == 0 || snapshot.getScoreAway() == 0) {
            double pBtts = remainingMatrix.getBttsYesProb() * 100.0;
            if (pBtts >= 40.0) {
                evaluateLiveCandidate(matchId, "BTTS_YES", "Обе забьют: Да", "BTTS", null, pBtts, edges);
            }
        }

        // 3. Scan Rest-of-Match Handicap
        int scoreDiff = snapshot.getScoreHome() - snapshot.getScoreAway();
        if (scoreDiff != 0) {
            String cushionMkt = scoreDiff > 0 ? "AH_AWAY_+1.5" : "AH_HOME_+1.5";
            double pCush = 65.0;
            evaluateLiveCandidate(matchId, cushionMkt, "Фора (+1.5)", "HANDICAP", 1.5, pCush, edges);
        }

        edges.sort((a, b) -> b.getExpectedValuePct().compareTo(a.getExpectedValuePct()));
        return edges.stream().limit(5).toList();
    }

    private void evaluateLiveCandidate(
            Long matchId, String market, String label, String category, Double line, double fairProb, List<MarketEdgeCandidateDto> edges
    ) {
        if (fairProb < 45.0) return;
        RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, market);
        if (quote == null || quote.value() < 1.30) return;

        double odd = quote.value();
        double ev = ((fairProb / 100.0) * odd - 1.0) * 100.0;
        if (ev < 2.0) return; // Only positive EV

        pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(market);
        boolean isSafe = (bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb &&
                (mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DC_1X
                        || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DC_X2))
                || (bet instanceof pro.datawiki.igaming.dto.market.HandicapBet hb && hb.param() != null && Math.abs(hb.param() - 1.5) < 0.05);

        edges.add(MarketEdgeCandidateDto.builder()
                .market(market)
                .marketLabel(label)
                .category(category)
                .line(line)
                .fairProbabilityPct(Math.round(fairProb * 10.0) / 10.0)
                .fairOdd(BigDecimal.valueOf(100.0 / Math.max(1.0, fairProb)).setScale(2, RoundingMode.HALF_UP))
                .marketOdd(BigDecimal.valueOf(odd).setScale(3, RoundingMode.HALF_UP))
                .bestBookmaker(quote.bookmaker())
                .expectedValuePct(BigDecimal.valueOf(ev).setScale(2, RoundingMode.HALF_UP))
                .convictionTier("LIVE")
                .isSafeCushion(isSafe)
                .build());
    }

    private LiveMatchSnapshotDto buildFallbackSnapshot(Long matchId, Map<String, Object> dossierMeta) {
        Long sofaEventId = dossierMeta != null && dossierMeta.get("sofascore_id") instanceof Number n ? n.longValue() : null;
        int scoreH = 0;
        int scoreA = 0;
        int minute = 1;
        int redH = 0;
        int redA = 0;
        double xgH = 0.0;
        double xgA = 0.0;
        String period = "1ST_HALF";

        if (sofaEventId != null && sofaEventId > 0 && jdbcTemplate != null) {
            try {
                String sql = "SELECT event_json FROM sofascore_raw_events WHERE event_id = ? ORDER BY captured_at DESC LIMIT 1";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sofaEventId);
                if (!rows.isEmpty()) {
                    String eventJson = (String) rows.get(0).get("event_json");
                    if (eventJson != null && !eventJson.isBlank()) {
                        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(eventJson);
                        com.fasterxml.jackson.databind.JsonNode event = root.has("event") ? root.path("event") : root;
                        scoreH = event.path("homeScore").path("current").asInt(0);
                        scoreA = event.path("awayScore").path("current").asInt(0);
                        period = event.path("status").path("description").asText("1ST_HALF");
                        long startSec = event.path("time").path("currentPeriodStartTimestamp").asLong(0L);
                        if (startSec > 0) {
                            long nowSec = System.currentTimeMillis() / 1000L;
                            long elapsed = Math.max(0, nowSec - startSec) / 60L;
                            minute = "2ND_HALF".equalsIgnoreCase(period) ? (int) Math.min(95, 45 + elapsed) : (int) Math.min(48, elapsed);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to extract live snapshot from sofascore_raw_events for #{}: {}", matchId, e.getMessage());
            }
        }

        if (minute <= 1) {
            Long startMs = dossierMeta != null && dossierMeta.get("start_time") instanceof Number n ? n.longValue() : System.currentTimeMillis();
            long elapsedMs = Math.max(0, System.currentTimeMillis() - startMs);
            minute = Math.max(1, Math.min(90, (int) (elapsedMs / (60L * 1000L))));
            if (minute > 45) period = "2ND_HALF";
        }

        return LiveMatchSnapshotDto.builder()
                .matchId(matchId)
                .sofaEventId(sofaEventId)
                .minute(minute)
                .scoreHome(scoreH)
                .scoreAway(scoreA)
                .redCardsHome(redH)
                .redCardsAway(redA)
                .liveXgHome(xgH)
                .liveXgAway(xgA)
                .matchPeriod(period)
                .isLive(true)
                .build();
    }
}
