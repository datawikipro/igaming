package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.sport.ParsedScore;
import pro.datawiki.igaming.dto.sport.SettlementOutcome;
import pro.datawiki.igaming.dto.sport.SettlementResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSettlementService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final MatchDossierService dossierService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final pro.datawiki.igaming.analyst.repository.AiPolicyInterventionRepository policyInterventionRepository;
    private final StrategyEngineService strategyEngineService;
    private final pro.datawiki.igaming.analyst.rating.SportMmrService sportMmrService;

    private static final BigDecimal DEFAULT_UNIT_STAKE = BigDecimal.valueOf(1000.0);

    /**
     * Staking Plan based on ML Probability Tier:
     * 90% ML -> 1000.0 ₽
     * 80% ML -> 500.0 ₽
     * 70% ML -> 250.0 ₽
     * 60% ML -> 100.0 ₽
     */
    public static BigDecimal resolveStakeAmount(String tier) {
        if ("ML_90".equalsIgnoreCase(tier)) {
            return BigDecimal.valueOf(1000.0);
        } else if ("ML_80".equalsIgnoreCase(tier)) {
            return BigDecimal.valueOf(500.0);
        } else if ("ML_70".equalsIgnoreCase(tier)) {
            return BigDecimal.valueOf(250.0);
        } else if ("ML_60".equalsIgnoreCase(tier)) {
            return BigDecimal.valueOf(100.0);
        } else {
            return BigDecimal.valueOf(100.0);
        }
    }

    public static boolean isLiveStatus(String status) {
        if (status == null || status.isBlank()) return false;
        String s = status.toUpperCase().trim();
        return s.equals("LIVE") || s.equals("IN_PLAY") || s.equals("INPLAY")
                || s.contains("HALF") || s.equals("HT") || s.equals("HALFTIME")
                || s.contains("SET") || s.contains("PERIOD") || s.contains("QUARTER")
                || s.equals("EXTRA_TIME") || s.equals("ET") || s.equals("OVERTIME") || s.equals("OT")
                || s.equals("PENALTIES") || s.equals("PEN") || s.equals("PAUSED") || s.equals("BREAK")
                || s.equals("INTERRUPTED");
    }

    public static boolean isFinishedStatus(String status) {
        if (status == null || status.isBlank()) return false;
        String s = status.toUpperCase().trim();
        return s.equals("FINISHED") || s.equals("ENDED") || s.equals("CLOSED")
                || s.equals("FT") || s.equals("FINAL") || s.equals("AET") || s.equals("AP");
    }

    public static long getSportSettlementTimeoutSec(String sportName) {
        if (sportName == null) return 14400L;
        return Sports.get(sportName).getSettlementTimeoutSeconds();
    }

    /**
     * Periodically settles finished matches every 2 minutes.
     * Non-transactional batch loop to ensure one failed match does not abort the entire queue.
     */
    @Scheduled(cron = "${analyst.scheduling.settle-cron:0 */2 * * * *}")
    public void settleFinishedMatches() {
        // Check all pending recommendations where match has already kicked off and scores may be ready in MDM Ground Truth
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<MatchAiRecommendation> pending = recommendationRepository.findPendingForSettlement(now);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Checking settlement for {} pending AI recommendations...", pending.size());
        for (MatchAiRecommendation rec : pending) {
            try {
                settleSingleRecommendation(rec);
            } catch (Exception ex) {
                log.warn("Failed to settle recommendation #{} (match #{}): {}", rec.getId(), rec.getMatchId(), ex.getMessage());
            }
        }
    }

    /**
     * Periodic reconciliation job: re-checks recently settled matches (settled within last 6 hours)
     * to correct scores if late goals, VAR decisions, or crawler delay previously recorded an incomplete score.
     */
    @Scheduled(cron = "${analyst.scheduling.reconcile-cron:0 */10 * * * *}")
    public void reconcileRecentlySettledMatches() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(48);
        List<MatchAiRecommendation> recent;
        try {
            recent = recommendationRepository.findByEvaluationStageAndUpdatedAtAfter(
                    "SETTLED", cutoff
            );
        } catch (Exception ex) {
            log.debug("Reconciliation query skipped: {}", ex.getMessage());
            return;
        }

        if (recent.isEmpty()) {
            return;
        }

        for (MatchAiRecommendation rec : recent) {
            try {
                reconcileSingleRecommendation(rec);
            } catch (Exception ex) {
                log.debug("Reconciliation error for rec #{}: {}", rec.getId(), ex.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reconcileSingleRecommendation(MatchAiRecommendation rec) {
        if (rec == null || rec.getMatchId() == null) return false;
        String currentScore = rec.getFinalScore();

        String confirmedScore = resolveConfirmedScore(rec);
        if (confirmedScore != null && !confirmedScore.isBlank() && !confirmedScore.equals(currentScore)) {
            if ("-:-".equals(confirmedScore)) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setFinalScore("-:-");
                rec.setErrorCategory("MATCH_POSTPONED_OR_ABANDONED");
                rec.setLessonLearned("Матч отменен или перенесен по данным сверки SofaScore. Оформлен возврат (VOID).");
                recommendationRepository.save(rec);
                String spName = rec.getSportName();
                Sport sp = spName != null ? Sports.get(spName) : null;
                if (sp != null) {
                    strategyEngineService.settleVotesForRecommendation(rec.getId(), "-:-", 0, 0, sp);
                }
                log.info("🔄 [Settlement Reconciliation] Match #{} re-settled as VOID (-:-)", rec.getMatchId());
                return true;
            }
            log.info("🔄 [Settlement Reconciliation] Score discrepancy detected for match #{} ({} vs {}): recorded='{}', confirmed='{}'. Re-settling...",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), currentScore, confirmedScore);
            SettlementResult res = settleRecommendationWithScore(rec, confirmedScore);
            boolean updated = res.isTerminal();
            if (updated) {
                recommendationRepository.save(rec);
                log.info("✅ [Settlement Reconciliation] Match #{} re-settled successfully: newScore='{}'",
                        rec.getMatchId(), rec.getFinalScore());
                return true;
            }
        } else if ("-:-".equals(currentScore) && "SETTLED".equalsIgnoreCase(rec.getEvaluationStage())) {
            // Check if this match was prematurely marked VOID when it actually hasn't started or is in-play
            CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(rec.getMatchId());
            long nowSec = System.currentTimeMillis() / 1000L;
            Long startSec = (header != null && header.getStartTimeEpochSec() != null) ? header.getStartTimeEpochSec() : 
                (rec.getMatchStartTime() != null ? rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() : null);
            if (startSec != null && startSec > nowSec) {
                LocalDateTime canonicalStart = LocalDateTime.ofInstant(Instant.ofEpochSecond(startSec), ZoneOffset.UTC);
                log.info("⏪ [Settlement Reconciliation] Reviving prematurely VOIDED match #{} ({}-{}): start time is in future ({})",
                        rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), canonicalStart);
                rec.setEvaluationStage("PRE_LINEUP");
                rec.setResultStatus("PENDING");
                rec.setFinalScore(null);
                rec.setErrorCategory(null);
                rec.setLessonLearned(null);
                rec.setMatchStartTime(canonicalStart);
                recommendationRepository.save(rec);
                try {
                    jdbcTemplate.update("""
                        UPDATE ai_strategy_vote 
                        SET result_status = 'PENDING', final_score = NULL, settled_at = NULL, actual_pnl = 0, roi_pct = 0
                        WHERE recommendation_id = ?
                    """, rec.getId());
                } catch (Exception ignored) {}
                return true;
            }

            // Reconcile pending strategy votes if recommendation was previously marked VOID with -:-
            String spName = rec.getSportName();
            Sport sp = spName != null ? Sports.get(spName) : null;
            if (sp != null) {
                strategyEngineService.settleVotesForRecommendation(rec.getId(), "-:-", 0, 0, sp);
            }
        }
        return false;
    }

    public String resolveConfirmedScore(MatchAiRecommendation rec) {
        if (rec == null || rec.getMatchId() == null) return null;
        CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(rec.getMatchId());
        Long sofaFixtureId = header != null ? header.getSofascoreId() : null;
        String sportName = rec.getSportName() != null ? rec.getSportName() : (header != null ? header.getSportSlug() : null);
        if (sportName == null) return null;
        Sport sport = Sports.get(sportName);
        long requiredTimeoutSec = sport.getSettlementTimeoutSeconds();
        long nowSec = System.currentTimeMillis() / 1000L;
        Long startSec = (header != null && header.getStartTimeEpochSec() != null) ? header.getStartTimeEpochSec() : 
                (rec.getMatchStartTime() != null ? rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() : null);
        boolean pastNormalDuration = startSec != null && (nowSec - startSec > requiredTimeoutSec);

        // 1. Exact SofaScore fixture lookup by sofaFixtureId or mdm_match_id
        if (sofaFixtureId == null && rec.getMatchId() != null) {
            try {
                List<Map<String, Object>> mRows = jdbcTemplate.queryForList(
                    "SELECT event_id FROM sofascore_raw_events WHERE mdm_match_id = ? ORDER BY captured_at DESC LIMIT 1",
                    rec.getMatchId()
                );
                if (!mRows.isEmpty() && mRows.get(0).get("event_id") != null) {
                    sofaFixtureId = ((Number) mRows.get(0).get("event_id")).longValue();
                }
            } catch (Exception ignored) {}
        }
        if (sofaFixtureId != null) {
            try {
                String sofaSql = "SELECT home_score, away_score, status, event_json FROM sofascore_raw_events WHERE event_id = ?";
                List<Map<String, Object>> sRows = jdbcTemplate.queryForList(sofaSql, sofaFixtureId);
                if (!sRows.isEmpty()) {
                    String st = (String) sRows.get(0).get("status");
                    if ("postponed".equalsIgnoreCase(st) || "cancelled".equalsIgnoreCase(st) || "abandoned".equalsIgnoreCase(st)) {
                        return "-:-";
                    }
                    if (isFinishedStatus(st) || pastNormalDuration) {
                        String evJson = (String) sRows.get(0).get("event_json");
                        if (evJson != null && !evJson.isBlank()) {
                            try {
                                com.fasterxml.jackson.databind.JsonNode evNode = objectMapper.readTree(evJson);
                                if (evNode.has("event") && evNode.path("event").isObject()) {
                                    evNode = evNode.path("event");
                                }
                                com.fasterxml.jackson.databind.JsonNode hsNode = evNode.path("homeScore");
                                com.fasterxml.jackson.databind.JsonNode asNode = evNode.path("awayScore");
                                if (hsNode != null && !hsNode.isMissingNode() && asNode != null && !asNode.isMissingNode()) {
                                    String desc = evNode.path("status").path("description").asText(null);
                                    int[] reg = sport.resolveRegulationScoreFromSofa(hsNode, asNode, desc);
                                    if (reg != null) {
                                        return reg[0] + ":" + reg[1];
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        Object hs = sRows.get(0).get("home_score");
                        Object as = sRows.get(0).get("away_score");
                        if (hs != null && as != null && !String.valueOf(hs).isBlank() && !String.valueOf(as).isBlank()) {
                            return hs + ":" + as;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Authoritative Canonical MDM Header if finished
        if (header != null && (isFinishedStatus(header.getStatus()) || pastNormalDuration)) {
            if (header.getRegularScore1() != null && header.getRegularScore2() != null &&
                    !header.getRegularScore1().isBlank() && !header.getRegularScore2().isBlank()) {
                if (isValidSportScore(sport, header.getRegularScore1(), header.getRegularScore2())) {
                    return header.getRegularScore1() + ":" + header.getRegularScore2();
                }
            }
            if (header.getScore1() != null && header.getScore2() != null &&
                    !header.getScore1().isBlank() && !header.getScore2().isBlank()) {
                if (isValidSportScore(sport, header.getScore1(), header.getScore2())) {
                    return header.getScore1() + ":" + header.getScore2();
                }
            }
        }

        // 3. MDM Live Score Consensus Service (backed by Redis Live Score Cache-Aside)
        try {
            pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto liveScore = dossierService.fetchMdmLiveScore(rec.getMatchId());
            if (liveScore != null && liveScore.getScore1() != null && liveScore.getScore2() != null) {
                if (liveScore.isFinished() || (liveScore.getStatus() != null && liveScore.getStatus().isTerminal()) || pastNormalDuration) {
                    String sc1 = String.valueOf(liveScore.getScore1());
                    String sc2 = String.valueOf(liveScore.getScore2());
                    if (isValidSportScore(sport, sc1, sc2)) {
                        return sc1 + ":" + sc2;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 4. Esports raw events fallback (HLTV for CS2, OpenDota for Dota2)
        if (sport != null && sport.isEsports()) {
            String esportsScore = resolveEsportsScore(rec);
            if (esportsScore != null && !esportsScore.isBlank()) {
                return esportsScore;
            }
        }

        return null;
    }

    /**
     * Resolves esports scores directly from esports_raw_events (HLTV for CS2, OpenDota for Dota2)
     * as a fast fallback before consensus flusher runs.
     */
    private String resolveEsportsScore(MatchAiRecommendation rec) {
        if (rec == null || rec.getMatchId() == null) return null;
        try {
            // 1. Direct lookup by mdm_match_id
            String sqlMatch = "SELECT game, status, raw_payload_json, team1_raw, team2_raw FROM esports_raw_events " +
                    "WHERE mdm_match_id = ? AND (status = 'FINISHED' OR status = 'ENDED') ORDER BY updated_at DESC LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlMatch, rec.getMatchId());
            if (!rows.isEmpty()) {
                String score = extractEsportsScoreFromRow(rows.get(0), rec.getTeam1Name(), rec.getTeam2Name());
                if (score != null) return score;
            }

            // 2. Team lookup bounded by match start time window (+-24 hours)
            if (rec.getTeam1Name() != null && rec.getTeam2Name() != null) {
                Long startSec = rec.getMatchStartTime() != null
                        ? rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond()
                        : null;
                if (startSec != null && startSec > 0) {
                    long startMs = startSec * 1000L;
                    long windowMs = 24L * 3600L * 1000L;
                    String teamSql = "SELECT event_id, game, status, raw_payload_json, team1_raw, team2_raw FROM esports_raw_events " +
                            "WHERE (status = 'FINISHED' OR status = 'ENDED') " +
                            "AND match_date BETWEEN ? AND ? " +
                            "ORDER BY match_date ASC";
                    List<Map<String, Object>> tRows = jdbcTemplate.queryForList(teamSql, startMs - windowMs, startMs + windowMs);

                    int winsHome = 0;
                    int winsAway = 0;
                    boolean foundAny = false;
                    String singleSeriesScore = null;

                    for (Map<String, Object> r : tRows) {
                        String t1Raw = (String) r.get("team1_raw");
                        String t2Raw = (String) r.get("team2_raw");
                        if (t1Raw == null || t2Raw == null) continue;

                        boolean direct = SimilarityUtils.areTeamsSimilar(rec.getTeam1Name(), t1Raw)
                                && SimilarityUtils.areTeamsSimilar(rec.getTeam2Name(), t2Raw);
                        boolean reversed = SimilarityUtils.areTeamsSimilar(rec.getTeam1Name(), t2Raw)
                                && SimilarityUtils.areTeamsSimilar(rec.getTeam2Name(), t1Raw);

                        if (!direct && !reversed) continue;

                        foundAny = true;
                        String eventId = (String) r.get("event_id");
                        safeLinkEsportsMatchId(rec.getMatchId(), eventId);

                        String json = (String) r.get("raw_payload_json");
                        if (json == null || json.isBlank()) continue;

                        try {
                            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
                            // HLTV / CS2 format carries total series score (team1Score: 2, team2Score: 1)
                            if (root.has("team1Score") && root.has("team2Score")) {
                                int s1 = root.path("team1Score").asInt(-1);
                                int s2 = root.path("team2Score").asInt(-1);
                                if (s1 >= 0 && s2 >= 0) {
                                    singleSeriesScore = direct ? (s1 + ":" + s2) : (s2 + ":" + s1);
                                }
                            }
                            // OpenDota / Dota2 format: individual match with radiantWin boolean
                            if (root.has("radiantWin")) {
                                boolean radiantWin = root.path("radiantWin").asBoolean();
                                if (direct) {
                                    if (radiantWin) winsHome++; else winsAway++;
                                } else {
                                    if (radiantWin) winsAway++; else winsHome++;
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    if (singleSeriesScore != null) {
                        return singleSeriesScore;
                    }

                    if (foundAny && (winsHome > 0 || winsAway > 0)) {
                        return winsHome + ":" + winsAway;
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Esports fallback score check failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
        }
        return null;
    }

    private void safeLinkEsportsMatchId(Long mdmMatchId, String eventId) {
        if (mdmMatchId == null || eventId == null) return;
        try {
            jdbcTemplate.update("UPDATE esports_raw_events SET mdm_match_id = ? WHERE event_id = ? AND mdm_match_id IS NULL",
                    mdmMatchId, eventId);
        } catch (Exception ignored) {}
    }

    private String extractEsportsScoreFromRow(Map<String, Object> row, String expTeam1, String expTeam2) {
        String json = (String) row.get("raw_payload_json");
        if (json == null || json.isBlank()) return null;
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            if (root.has("team1Score") && root.has("team2Score")) {
                int s1 = root.path("team1Score").asInt(-1);
                int s2 = root.path("team2Score").asInt(-1);
                if (s1 >= 0 && s2 >= 0) {
                    String t1Raw = (String) row.get("team1_raw");
                    boolean direct = SimilarityUtils.areTeamsSimilar(expTeam1, t1Raw);
                    return direct ? (s1 + ":" + s2) : (s2 + ":" + s1);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public java.util.Optional<MatchAiRecommendation> settleRecommendationManually(Long recommendationId, String explicitScore) {
        java.util.Optional<MatchAiRecommendation> opt = recommendationRepository.findById(recommendationId);
        if (opt.isEmpty()) {
            return java.util.Optional.empty();
        }
        MatchAiRecommendation rec = opt.get();
        SettlementResult res = settleRecommendationWithScore(rec, explicitScore);
        if (res.isTerminal()) {
            return java.util.Optional.of(recommendationRepository.save(rec));
        }
        return java.util.Optional.empty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SettlementResult settleSingleRecommendation(MatchAiRecommendation rec) {
        if (rec.getMatchId() == null) {
            return SettlementResult.REJECTED_MISSING_MARKET;
        }

        CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(rec.getMatchId());
        Long sofaFixtureId = header != null ? header.getSofascoreId() : null;
        String sportName = rec.getSportName();
        if (sportName == null && header != null && header.getSportSlug() != null) {
            sportName = header.getSportSlug().toUpperCase();
        }

        if (rec.getLeagueName() != null) {
            String lLower = rec.getLeagueName().toLowerCase();
            if (lLower.contains("tt elite") || lLower.contains("setka cup") || lLower.contains("настольный теннис") || lLower.contains("table tennis")) {
                sportName = "TABLE_TENNIS";
            } else if (lLower.contains("cricket") || lLower.contains("крикет") || lLower.contains("t20")) {
                sportName = "CRICKET";
            } else if (lLower.contains("volleyball") || lLower.contains("волейбол") || lLower.contains("воллей лига")) {
                sportName = "VOLLEYBALL";
            }
        }

        long nowSec = System.currentTimeMillis() / 1000L;
        Long startSec = null;
        if (header != null && header.getStartTimeEpochSec() != null) {
            startSec = header.getStartTimeEpochSec();
            if (rec.getMatchStartTime() == null || Math.abs(rec.getMatchStartTime().toEpochSecond(ZoneOffset.UTC) - startSec) > 300) {
                rec.setMatchStartTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(startSec), ZoneOffset.UTC));
            }
        } else if (rec.getMatchStartTime() != null) {
            startSec = rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
        }

        // 1. Guard: Match has not started yet
        if (startSec != null && nowSec < startSec) {
            return SettlementResult.DEFERRED_MISSING_SCORE;
        }

        long requiredTimeoutSec = getSportSettlementTimeoutSec(sportName);
        boolean pastNormalDuration = startSec != null && (nowSec - startSec > requiredTimeoutSec);

        // 2. Guard: Active Live Status -> NEVER settle an in-progress match!
        if (header != null && isLiveStatus(header.getStatus())) {
            // Guard against stale Redis live status: if match is past normal duration, proceed to check final scores
            if (pastNormalDuration) {
                log.info("Match #{} has status {} in Redis/MDM but started {}s ago (> {}s). Proceeding to resolve confirmed final score...",
                        rec.getMatchId(), header.getStatus(), nowSec - startSec, requiredTimeoutSec);
            } else {
                log.debug("Settlement skipped for match #{}: match is actively live in MDM (status={})", rec.getMatchId(), header.getStatus());
                return SettlementResult.DEFERRED_MATCH_IN_PLAY;
            }
        }

        String s1 = null;
        String s2 = null;

        // Priority 1: Verified SofaScore ground-truth by exact fixture ID or mdm_match_id
        if (sofaFixtureId == null && rec.getMatchId() != null) {
            try {
                List<Map<String, Object>> mRows = jdbcTemplate.queryForList(
                    "SELECT event_id FROM sofascore_raw_events WHERE mdm_match_id = ? ORDER BY captured_at DESC LIMIT 1",
                    rec.getMatchId()
                );
                if (!mRows.isEmpty() && mRows.get(0).get("event_id") != null) {
                    sofaFixtureId = ((Number) mRows.get(0).get("event_id")).longValue();
                }
            } catch (Exception ignored) {}
        }
        if (sofaFixtureId != null) {
            try {
                String sofaSql = "SELECT home_score, away_score, status, event_json FROM sofascore_raw_events WHERE event_id = ?";
                List<Map<String, Object>> sRows = jdbcTemplate.queryForList(sofaSql, sofaFixtureId);
                if (!sRows.isEmpty()) {
                    String st = (String) sRows.get(0).get("status");
                    if ("postponed".equalsIgnoreCase(st) || "cancelled".equalsIgnoreCase(st) || "abandoned".equalsIgnoreCase(st)) {
                        rec.setEvaluationStage("SETTLED");
                        rec.setResultStatus("VOID");
                        rec.setActualPnl(BigDecimal.ZERO);
                        rec.setRoiPct(BigDecimal.ZERO);
                        rec.setFinalScore("-:-");
                        rec.setErrorCategory("MATCH_POSTPONED_OR_ABANDONED");
                        rec.setLessonLearned("Матч отменен или перенесен по данным SofaScore. Оформлен возврат (VOID).");
                        recommendationRepository.save(rec);
                        Sport spVoid = sportName != null ? Sports.get(sportName) : null;
                        if (spVoid != null) {
                            strategyEngineService.settleVotesForRecommendation(rec.getId(), "-:-", 0, 0, spVoid);
                        }
                        log.info("Settled match #{} ({}-{}) as VOID: SofaScore status='{}'", rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), st);
                        return SettlementResult.VOID;
                    }
                    if (isLiveStatus(st)) {
                        if (!pastNormalDuration) {
                            log.debug("Settlement skipped for match #{}: SofaScore reports event is actively live (status={})", rec.getMatchId(), st);
                            return SettlementResult.DEFERRED_MATCH_IN_PLAY;
                        }
                    }
                    if (isFinishedStatus(st) || pastNormalDuration) {
                        String evJson = (String) sRows.get(0).get("event_json");
                        if (evJson != null && !evJson.isBlank()) {
                            try {
                                com.fasterxml.jackson.databind.JsonNode evNode = objectMapper.readTree(evJson);
                                if (evNode.has("event") && evNode.path("event").isObject()) {
                                    evNode = evNode.path("event");
                                }
                                com.fasterxml.jackson.databind.JsonNode hsNode = evNode.path("homeScore");
                                com.fasterxml.jackson.databind.JsonNode asNode = evNode.path("awayScore");
                                if (hsNode != null && !hsNode.isMissingNode() && asNode != null && !asNode.isMissingNode()) {
                                    String desc = evNode.path("status").path("description").asText(null);
                                    Sport sp = sportName != null ? Sports.get(sportName) : null;
                                    if (sp == null) {
                                        log.warn("Settlement: Sport is unknown for match #{}, cannot resolve regulation score", rec.getMatchId());
                                        return SettlementResult.REJECTED_UNSUPPORTED_SPORT;
                                    }
                                    int[] reg = sp.resolveRegulationScoreFromSofa(hsNode, asNode, desc);
                                    if (reg != null) {
                                        s1 = String.valueOf(reg[0]);
                                        s2 = String.valueOf(reg[1]);
                                        log.info("Settlement: Resolved regulation final scores {}:{} for match #{} from sofascore event_json (desc={})", s1, s2, rec.getMatchId(), desc);
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        if (s1 == null || s2 == null) {
                            Object hs = sRows.get(0).get("home_score");
                            Object as = sRows.get(0).get("away_score");
                            if (hs != null && as != null && !String.valueOf(hs).isBlank() && !String.valueOf(as).isBlank()) {
                                s1 = String.valueOf(hs);
                                s2 = String.valueOf(as);
                                log.info("Settlement: Resolved final scores {}:{} for match #{} from sofascore_raw_events fixture #{}", s1, s2, rec.getMatchId(), sofaFixtureId);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("Fixture lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 2: Canonical MDM Match Header (backed by Redis Live Score Cache-Aside)
        if (s1 == null || s2 == null) {
            if (header != null) {
                String mdmStatus = header.getStatus();
                if (isLiveStatus(mdmStatus)) {
                    if (!pastNormalDuration) {
                        log.debug("Settlement skipped for match #{}: MDM header status is live ({})", rec.getMatchId(), mdmStatus);
                        return SettlementResult.DEFERRED_MATCH_IN_PLAY;
                    }
                }
                boolean isExplicitlyFinished = isFinishedStatus(mdmStatus);
                if (isExplicitlyFinished || pastNormalDuration) {
                    if (header.getRegularScore1() != null && header.getRegularScore2() != null &&
                            !header.getRegularScore1().isBlank() && !header.getRegularScore2().isBlank()) {
                        s1 = header.getRegularScore1();
                        s2 = header.getRegularScore2();
                        log.info("Settlement: Resolved regular scores {}:{} for match #{} from MDM Header",
                                s1, s2, rec.getMatchId());
                    } else if (header.getScore1() != null && header.getScore2() != null &&
                            !header.getScore1().isBlank() && !header.getScore2().isBlank()) {
                        s1 = header.getScore1();
                        s2 = header.getScore2();
                        log.info("Settlement: Resolved final scores {}:{} for match #{} (status={}, pastDuration={}) directly from MDM Header / Redis Live Score",
                                s1, s2, rec.getMatchId(), mdmStatus, pastNormalDuration);
                    }
                }
            }
        }

        // Priority 3: MDM Live Score Consensus Service (backed by Redis Live Score Cache-Aside)
        if (s1 == null || s2 == null) {
            try {
                pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto liveScore = dossierService.fetchMdmLiveScore(rec.getMatchId());
                if (liveScore != null) {
                    if (liveScore.getStatus() != null && isLiveStatus(liveScore.getStatus().name())) {
                        if (!pastNormalDuration) {
                            log.debug("Settlement skipped for match #{}: MDM Live Score reports match is in-play ({})", rec.getMatchId(), liveScore.getStatus());
                            return SettlementResult.DEFERRED_MATCH_IN_PLAY;
                        }
                    }
                    if (liveScore.getScore1() != null && liveScore.getScore2() != null) {
                        boolean isFinished = liveScore.isFinished() || (liveScore.getStatus() != null && liveScore.getStatus().isTerminal());
                        if (isFinished || pastNormalDuration) {
                            s1 = String.valueOf(liveScore.getScore1());
                            s2 = String.valueOf(liveScore.getScore2());
                            log.info("Settlement: Resolved scores {}:{} for match #{} from MDM Live Score (status={}, pastDuration={})",
                                    s1, s2, rec.getMatchId(), liveScore.getStatus(), pastNormalDuration);
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("MDM Live Score lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 4.5: Esports raw events fallback (HLTV for CS2, OpenDota for Dota2)
        Sport targetSport = sportName != null ? Sports.get(sportName) : null;
        if ((s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) && targetSport != null && targetSport.isEsports()) {
            String esportsScore = resolveEsportsScore(rec);
            if (esportsScore != null && esportsScore.contains(":")) {
                String[] parts = esportsScore.split(":");
                s1 = parts[0].trim();
                s2 = parts[1].trim();
                log.info("Settlement: Resolved esports scores {}:{} for match #{} from esports_raw_events", s1, s2, rec.getMatchId());
            }
        }

        // Priority 5: Match postponed / cancelled / abandoned (> sport settlement timeout + 1h without result)
        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            Long startTimeEpochMs = (startSec != null) ? startSec * 1000L : null;
            long timeoutMs = (requiredTimeoutSec + 3600L) * 1000L;
            if (startTimeEpochMs != null && startTimeEpochMs < System.currentTimeMillis() - timeoutMs) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setFinalScore("-:-");
                rec.setErrorCategory("MATCH_POSTPONED_OR_ABANDONED");
                rec.setLessonLearned("Матч отменен, перенесен или результат не зафиксирован в течение допустимого времени. Оформлен возврат (VOID).");
                recommendationRepository.save(rec);
                if (targetSport != null) {
                    strategyEngineService.settleVotesForRecommendation(rec.getId(), "-:-", 0, 0, targetSport);
                }
                log.info("Settled match #{} ({}-{}) as VOID: match expired >{}h ago without scores",
                        rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), timeoutMs / 3600000L);
                return SettlementResult.VOID;
            }
            return SettlementResult.DEFERRED_MISSING_SCORE;
        }

        return settleRecommendationWithScore(rec, s1 + ":" + s2);
    }

    public SettlementResult settleRecommendationWithScore(MatchAiRecommendation rec, String explicitScore) {
        if (rec == null || explicitScore == null || explicitScore.isBlank() || "-:-".equals(explicitScore.trim())) {
            return SettlementResult.DEFERRED_MISSING_SCORE;
        }

        String sportName = rec.getSportName();
        if (rec.getLeagueName() != null) {
            String lLower = rec.getLeagueName().toLowerCase();
            if (lLower.contains("tt elite") || lLower.contains("setka cup") || lLower.contains("настольный теннис") || lLower.contains("table tennis")) {
                sportName = "TABLE_TENNIS";
            } else if (lLower.contains("cricket") || lLower.contains("крикет") || lLower.contains("t20")) {
                sportName = "CRICKET";
            } else if (lLower.contains("volleyball") || lLower.contains("волейбол") || lLower.contains("воллей лига")) {
                sportName = "VOLLEYBALL";
            }
        }

        if (sportName == null || sportName.isBlank()) {
            log.warn("Cannot settle recommendation #{} for match #{}: sportName is missing/null", rec.getId(), rec.getMatchId());
            return SettlementResult.REJECTED_UNSUPPORTED_SPORT;
        }

        try {
            Sport sport = Sports.get(sportName);
            java.util.Optional<ParsedScore> parsedOpt = ParsedScore.parse(explicitScore);
            if (parsedOpt.isEmpty()) {
                log.warn("Cannot parse explicit score '{}' for match #{}", explicitScore, rec.getMatchId());
                return SettlementResult.REJECTED_UNPARSEABLE_SCORE;
            }
            ParsedScore parsed = parsedOpt.get();

            rec.setFinalScore(parsed.displayScore());

            if ("PASS".equalsIgnoreCase(rec.getRecommendedMarket()) || "PASS".equalsIgnoreCase(rec.getConvictionTier())) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("PASS");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("MARKET_PASS");
                rec.setLessonLearned("Матч пропущен моделью (PASS). Ставка не размещалась.");
                strategyEngineService.settleVotesForRecommendation(rec.getId(), parsed.displayScore(), parsed.score1(), parsed.score2(), sport);
                recommendationRepository.save(rec);
                return SettlementResult.PASS;
            }

            if ("WAIT".equalsIgnoreCase(rec.getRecommendedMarket()) || "WAIT".equalsIgnoreCase(rec.getConvictionTier())) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("PASS");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("MARKET_PASS");
                rec.setLessonLearned("Матч завершился в статусе ожидания составов (WAIT). Ставка не размещалась.");
                strategyEngineService.settleVotesForRecommendation(rec.getId(), parsed.displayScore(), parsed.score1(), parsed.score2(), sport);
                recommendationRepository.save(rec);
                return SettlementResult.PASS;
            }

            if (sport.isStatisticalProxy() || sport.isFantasyDuel() || sport.isSpecials() || !sport.isRealSport()) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("SKIPPED");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("CANCELLED_SYNTHETIC_COLLISION");
                rec.setLessonLearned("Событие не является реальным спортивным матчем (прокси/фэнтези/спецпари). Исключено из аналитики.");
                strategyEngineService.settleVotesForRecommendation(rec.getId(), parsed.displayScore(), parsed.score1(), parsed.score2(), sport);
                recommendationRepository.save(rec);
                return SettlementResult.VOID;
            }

            String market = rec.getRecommendedMarket();
            if (market != null) {
                String mUpper = market.toUpperCase().trim();
                if ("WIN1".equals(mUpper) || "P1".equals(mUpper) || "1".equals(mUpper) || "W1".equals(mUpper)) market = "P1";
                else if ("WIN2".equals(mUpper) || "P2".equals(mUpper) || "2".equals(mUpper) || "W2".equals(mUpper)) market = "P2";
                else if ("DRAW".equals(mUpper) || "PX".equals(mUpper) || "X".equals(mUpper)) market = "PX";
            }

            SettlementOutcome outcome = sport.evaluateSettlement(market, parsed.score1(), parsed.score2(), parsed.displayScore());
            if (outcome == SettlementOutcome.UNRESOLVED) {
                long timeoutSec = getSportSettlementTimeoutSec(sportName);
                Long startSec = rec.getMatchStartTime() != null ? rec.getMatchStartTime().toEpochSecond(ZoneOffset.UTC) : null;
                long nowSec = System.currentTimeMillis() / 1000L;
                if (startSec != null && (nowSec - startSec > timeoutSec)) {
                    rec.setEvaluationStage("SETTLED");
                    rec.setResultStatus("VOID");
                    rec.setActualPnl(BigDecimal.ZERO);
                    rec.setRoiPct(BigDecimal.ZERO);
                    rec.setErrorCategory("UNRESOLVED_SCORE_TIMEOUT");
                    rec.setLessonLearned("Счет матча (" + parsed.displayScore() + ") не позволил однозначно рассчитать исход рынка по спортивным правилам после завершения времени матча. Оформлен возврат (VOID).");
                    strategyEngineService.settleVotesForRecommendation(rec.getId(), parsed.displayScore(), parsed.score1(), parsed.score2(), sport);
                    recommendationRepository.save(rec);
                    log.info("Settled match #{} as VOID due to UNRESOLVED outcome timeout (past timeout {}s, score={})",
                            rec.getMatchId(), timeoutSec, parsed.displayScore());
                    return SettlementResult.VOID;
                }
                log.info("Outcome unresolved for match #{}: Sport={}, Market='{}', Score='{}'",
                        rec.getMatchId(), sport.getCode(), rec.getRecommendedMarket(), parsed.displayScore());
                return SettlementResult.DEFERRED_MISSING_SCORE;
            }

            BigDecimal unitStake = resolveStakeAmount(rec.getConvictionTier());
            BigDecimal odd = rec.getRecommendedOdd();

            if ("PASS".equalsIgnoreCase(rec.getRecommendedMarket())) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("PASS");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("MARKET_PASS");
                rec.setLessonLearned("Матч пропущен моделью (PASS). Ставка не размещалась.");
            } else if (odd == null || odd.compareTo(BigDecimal.valueOf(1.01)) <= 0) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("NO_MARKET_ODDS");
                rec.setLessonLearned("Матч рассчитан как возврат (VOID): отсутствовала подтвержденная котировка букмекера.");
            } else if (outcome == SettlementOutcome.WON) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("WON");
                BigDecimal pnl = odd.subtract(BigDecimal.ONE).multiply(unitStake).setScale(2, RoundingMode.HALF_UP);
                BigDecimal roi = odd.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100.0)).setScale(2, RoundingMode.HALF_UP);
                rec.setActualPnl(pnl);
                rec.setRoiPct(roi);
                rec.setErrorCategory("WON_AS_PLANNED");
                rec.setLessonLearned("Базовый сценарий полностью подтвердился. Тактическая гипотеза оправдана.");
            } else if (outcome == SettlementOutcome.LOST) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("LOST");
                rec.setActualPnl(unitStake.negate());
                rec.setRoiPct(BigDecimal.valueOf(-100.0));
                rec.setErrorCategory("TACTICAL_BLIND_SPOT");
                rec.setLessonLearned(String.format("Матч завершился поражением (счет %s). Основной сценарий '%s' не подтвердился.", parsed.displayScore(), rec.getRecommendedMarket()));
            } else if (outcome == SettlementOutcome.VOID) {
                rec.setEvaluationStage("SETTLED");
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("PUSH_VOID_PROTECTION");
                rec.setLessonLearned("Азиатская линия / правила отмены дисциплины защитили депозит возвратом при счете " + parsed.displayScore());
            }

            // Perform post-mortem loss reflection if needed
            categorizeLossPostMortem(rec, outcome, parsed.score1(), parsed.score2(), parsed.displayScore(), sport);
            strategyEngineService.settleVotesForRecommendation(rec.getId(), parsed.displayScore(), parsed.score1(), parsed.score2(), sport);

            recommendationRepository.save(rec);
            updatePolicyInterventionsOnSettlement(rec, outcome);

            // Dynamic Universal MMR update for racket sports
            if (rec.getMatchId() != null) {
                String sp = rec.getSportSlug() != null ? rec.getSportSlug() : rec.getSportName();
                if (sp != null) {
                    String sLower = sp.toLowerCase(java.util.Locale.ROOT).replace('_', '-');
                    if ("tennis".equals(sLower) || "table-tennis".equals(sLower)) {
                        try {
                            CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(rec.getMatchId());
                            if (header != null && header.getHomeTeamId() != null && header.getAwayTeamId() != null) {
                                sportMmrService.processFinishedMdmMatch(
                                        rec.getMatchId(),
                                        sLower,
                                        header.getTournamentName() != null ? header.getTournamentName() : rec.getLeagueName(),
                                        header.getHomeTeamId(),
                                        header.getAwayTeamId(),
                                        parsed.score1(),
                                        parsed.score2(),
                                        header.getStartTimeEpochSec() != null ? java.time.Instant.ofEpochSecond(header.getStartTimeEpochSec()) : java.time.Instant.now(),
                                        header.getHomeTeamName() != null ? header.getHomeTeamName() : rec.getTeam1Name(),
                                        header.getAwayTeamName() != null ? header.getAwayTeamName() : rec.getTeam2Name(),
                                        header.getHomeCountryName(),
                                        header.getAwayCountryName(),
                                        null
                                );
                            }
                        } catch (Exception ex) {
                            log.debug("MMR update on recommendation settlement skipped for match #{}: {}", rec.getMatchId(), ex.getMessage());
                        }
                    }
                }
            }
            log.info("Settled match #{} ({}-{}): Sport={}, Bet='{}', Score='{}', Result={}, ErrorCategory={}",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), sport.getCode(),
                    rec.getRecommendedMarket(), parsed.displayScore(), outcome, rec.getErrorCategory());
            return SettlementResult.fromOutcome(outcome);
        } catch (Exception e) {
            log.warn("Cannot settle recommendation for match #{}: {}", rec.getMatchId(), e.getMessage());
            return SettlementResult.REJECTED_UNPARSEABLE_SCORE;
        }
    }



    private void categorizeLossPostMortem(MatchAiRecommendation rec, SettlementOutcome outcome, int s1, int s2, String finalScore, Sport sport) {
        if (outcome != SettlementOutcome.LOST) return;

        String market = rec.getRecommendedMarket() != null ? rec.getRecommendedMarket().toUpperCase() : "";

        // 1. Sport-specific post-mortem loss categorization (Polymorphic Domain Hierarchy)
        if (sport != null) {
            java.util.Optional<Sport.LossCategoryVerdict> verdict = sport.categorizeLossPostMortem(
                    s1, s2, finalScore, market, rec.getLeagueName(), rec.getEvaluationStage(), rec.getExpectedXg());
            if (verdict.isPresent()) {
                rec.setErrorCategory(verdict.get().errorCategory());
                rec.setLessonLearned(verdict.get().lessonLearned());
                return;
            }
        }

        // 2. Default: TACTICAL_BLIND_SPOT
        rec.setErrorCategory("TACTICAL_BLIND_SPOT");
        String lesson = String.format("Системная ошибка оценки матча и формы команд (счет %s). " +
                "Требуется ревизия параметров защиты/нападения.", finalScore);
        rec.setLessonLearned(lesson);
    }



    private int[] extractScoreFromSofaRow(Map<String, Object> row, String sportName) {
        Object hs = row.get("home_score");
        Object as = row.get("away_score");
        if (hs != null && as != null && !String.valueOf(hs).isBlank() && !String.valueOf(as).isBlank()) {
            try {
                return new int[]{Integer.parseInt(String.valueOf(hs).trim()), Integer.parseInt(String.valueOf(as).trim())};
            } catch (Exception ignored) {}
        }
        String ej = (String) row.get("event_json");
        if (ej != null && !ej.isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode evNode = objectMapper.readTree(ej);
                if (evNode.has("event") && evNode.path("event").isObject()) {
                    evNode = evNode.path("event");
                }
                com.fasterxml.jackson.databind.JsonNode hsNode = evNode.path("homeScore");
                com.fasterxml.jackson.databind.JsonNode asNode = evNode.path("awayScore");
                if (hsNode != null && !hsNode.isMissingNode() && asNode != null && !asNode.isMissingNode()) {
                    String desc = evNode.path("status").path("description").asText(null);
                    Sport sp = sportName != null ? Sports.get(sportName) : null;
                    if (sp != null) {
                        int[] reg = sp.resolveRegulationScoreFromSofa(hsNode, asNode, desc);
                        if (reg != null) return reg;
                    }
                    int h = hsNode.has("current") ? hsNode.path("current").asInt(-1) : (hsNode.has("display") ? hsNode.path("display").asInt(-1) : -1);
                    int a = asNode.has("current") ? asNode.path("current").asInt(-1) : (asNode.has("display") ? asNode.path("display").asInt(-1) : -1);
                    if (h >= 0 && a >= 0) {
                        return new int[]{h, a};
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isValidSportScore(Sport sport, String s1Str, String s2Str) {
        if (s1Str == null || s2Str == null) return false;
        try {
            int s1 = Integer.parseInt(s1Str.trim());
            int s2 = Integer.parseInt(s2Str.trim());
            return sport != null ? sport.isValidScore(s1, s2) : (s1 >= 0 && s2 >= 0);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void updatePolicyInterventionsOnSettlement(MatchAiRecommendation rec, SettlementOutcome outcome) {
        if (policyInterventionRepository == null || rec == null || outcome == null) return;
        try {
            java.util.List<pro.datawiki.igaming.analyst.entity.AiPolicyIntervention> list = 
                    policyInterventionRepository.findByRecommendationId(rec.getId());
            if ((list == null || list.isEmpty()) && rec.getMatchId() != null) {
                list = policyInterventionRepository.findByMatchId(rec.getMatchId());
            }
            if (list == null || list.isEmpty()) return;

            for (pro.datawiki.igaming.analyst.entity.AiPolicyIntervention p : list) {
                p.setResultStatus(outcome.name());
                p.setFinalScore(rec.getFinalScore());
                BigDecimal odd = p.getRecommendedOdd() != null ? p.getRecommendedOdd() : BigDecimal.ONE;
                BigDecimal rawStake = p.getRawStakeRub() != null ? p.getRawStakeRub() : BigDecimal.ZERO;
                BigDecimal appliedStake = p.getAppliedStakeRub() != null ? p.getAppliedStakeRub() : BigDecimal.ZERO;

                if (outcome == SettlementOutcome.LOST) {
                    // Policy saved money by capping the stake
                    p.setPolicyImpactPnl(rawStake.subtract(appliedStake));
                } else if (outcome == SettlementOutcome.WON) {
                    // Opportunity cost
                    BigDecimal netOdd = odd.subtract(BigDecimal.ONE);
                    p.setPolicyImpactPnl(appliedStake.subtract(rawStake).multiply(netOdd).setScale(2, java.math.RoundingMode.HALF_UP));
                } else {
                    p.setPolicyImpactPnl(BigDecimal.ZERO);
                }
                policyInterventionRepository.save(p);
            }
        } catch (Exception ex) {
            log.warn("Failed updating policy intervention settlement for rec #{}: {}", rec.getId(), ex.getMessage());
        }
    }

}
