package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.ShadowPickDto;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiLearningMemoryRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.sport.SettlementOutcome;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSettlementService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiLearningMemoryRepository memoryRepository;
    private final MatchDossierService dossierService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

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
        } else if ("ML_70".equalsIgnoreCase(tier) || "CONFIDENT".equalsIgnoreCase(tier)) {
            return BigDecimal.valueOf(250.0);
        } else if ("ML_60".equalsIgnoreCase(tier) || "GOOD_PICK".equalsIgnoreCase(tier)) {
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
     */
    @Scheduled(cron = "${analyst.scheduling.settle-cron:0 */2 * * * *}")
    @Transactional
    public void settleFinishedMatches() {
        // Check all pending recommendations where scores are ready in MDM Ground Truth
        List<MatchAiRecommendation> pending = recommendationRepository.findByResultStatus("PENDING");

        if (pending.isEmpty()) {
            return;
        }

        log.info("Checking settlement for {} pending AI recommendations...", pending.size());
        for (MatchAiRecommendation rec : pending) {
            settleSingleRecommendation(rec);
        }
    }

    /**
     * Periodic reconciliation job: re-checks recently settled matches (settled within last 6 hours)
     * to correct scores if late goals, VAR decisions, or crawler delay previously recorded an incomplete score.
     */
    @Scheduled(cron = "${analyst.scheduling.reconcile-cron:0 */10 * * * *}")
    @Transactional
    public void reconcileRecentlySettledMatches() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(6);
        List<MatchAiRecommendation> recent;
        try {
            recent = recommendationRepository.findByResultStatusInAndUpdatedAtAfter(
                    List.of("WON", "LOST", "VOID"), cutoff
            );
        } catch (Exception ex) {
            log.debug("Reconciliation query skipped: {}", ex.getMessage());
            return;
        }

        if (recent.isEmpty()) {
            return;
        }

        for (MatchAiRecommendation rec : recent) {
            reconcileSingleRecommendation(rec);
        }
    }

    @Transactional
    public boolean reconcileSingleRecommendation(MatchAiRecommendation rec) {
        if (rec == null || rec.getMatchId() == null) return false;
        String currentScore = rec.getFinalScore();
        if (currentScore == null || "-:-".equals(currentScore)) return false;

        String confirmedScore = resolveConfirmedScore(rec);
        if (confirmedScore != null && !confirmedScore.isBlank() && !confirmedScore.equals(currentScore)) {
            log.warn("🔄 [Settlement Reconciliation] Score discrepancy detected for match #{} ({} vs {}): recorded='{}', confirmed='{}'. Re-settling...",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), currentScore, confirmedScore);
            boolean updated = settleRecommendationWithScore(rec, confirmedScore);
            if (updated) {
                recommendationRepository.save(rec);
                log.info("✅ [Settlement Reconciliation] Match #{} re-settled successfully: newScore='{}', newStatus={}, newPnL={}",
                        rec.getMatchId(), rec.getFinalScore(), rec.getResultStatus(), rec.getActualPnl());
                return true;
            }
        }
        return false;
    }

    public String resolveConfirmedScore(MatchAiRecommendation rec) {
        if (rec == null || rec.getMatchId() == null) return null;
        CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(rec.getMatchId());
        Long sofaFixtureId = header != null ? header.getSofascoreId() : null;

        // 1. Exact SofaScore fixture lookup by sofaFixtureId
        if (sofaFixtureId != null) {
            try {
                String sofaSql = "SELECT home_score, away_score, status FROM sofascore_raw_events WHERE event_id = ?";
                List<Map<String, Object>> sRows = jdbcTemplate.queryForList(sofaSql, sofaFixtureId);
                if (!sRows.isEmpty()) {
                    String st = (String) sRows.get(0).get("status");
                    if (isFinishedStatus(st)) {
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
        if (header != null && isFinishedStatus(header.getStatus())) {
            if (header.getScore1() != null && header.getScore2() != null &&
                    !header.getScore1().isBlank() && !header.getScore2().isBlank()) {
                return header.getScore1() + ":" + header.getScore2();
            }
        }

        // 3. Direct mdm_match database table if finished
        try {
            String mdmSql = "SELECT score1, score2, status FROM mdm_match WHERE id = ?";
            List<Map<String, Object>> mdmRows = jdbcTemplate.queryForList(mdmSql, rec.getMatchId());
            if (!mdmRows.isEmpty()) {
                Map<String, Object> r = mdmRows.get(0);
                String st = (String) r.get("status");
                if (isFinishedStatus(st)) {
                    Object sc1 = r.get("score1");
                    Object sc2 = r.get("score2");
                    if (sc1 != null && sc2 != null && !String.valueOf(sc1).isBlank() && !String.valueOf(sc2).isBlank()) {
                        return sc1 + ":" + sc2;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 4. SofaScore team lookup (strictly bounded by match start time window +-24h and youth/gender isolation)
        if (rec.getTeam1Name() != null && rec.getTeam2Name() != null) {
            Long startSec = null;
            if (rec.getMatchStartTime() != null) {
                startSec = rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
            } else if (header != null && header.getStartTimeEpochSec() != null) {
                startSec = header.getStartTimeEpochSec();
            }

            if (startSec != null && startSec > 0) {
                try {
                    long startMs = startSec * 1000L;
                    long windowMs = 24L * 3600L * 1000L;
                    String t1 = "%" + rec.getTeam1Name().trim() + "%";
                    String t2 = "%" + rec.getTeam2Name().trim() + "%";
                    String teamSearchSql = "SELECT home_team, away_team, home_score, away_score, status FROM sofascore_raw_events " +
                            "WHERE (status = 'finished' OR status = 'ended' OR status = 'closed') " +
                            "AND match_date BETWEEN ? AND ? " +
                            "AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                            "AND home_team NOT ILIKE '%U17%' AND home_team NOT ILIKE '%U18%' AND home_team NOT ILIKE '%U19%' AND home_team NOT ILIKE '%U20%' AND home_team NOT ILIKE '%U21%' AND home_team NOT ILIKE '%U23%' " +
                            "AND away_team NOT ILIKE '%U17%' AND away_team NOT ILIKE '%U18%' AND away_team NOT ILIKE '%U19%' AND away_team NOT ILIKE '%U20%' AND away_team NOT ILIKE '%U21%' AND away_team NOT ILIKE '%U23%' " +
                            "AND ((home_team ILIKE ? AND away_team ILIKE ?) OR (home_team ILIKE ? AND away_team ILIKE ?)) " +
                            "ORDER BY captured_at DESC LIMIT 1";
                    List<Map<String, Object>> tRows = jdbcTemplate.queryForList(teamSearchSql, startMs - windowMs, startMs + windowMs, t1, t2, t2, t1);
                    if (!tRows.isEmpty()) {
                        Map<String, Object> row = tRows.get(0);
                        String ht = (String) row.get("home_team");
                        Object hs = row.get("home_score");
                        Object as = row.get("away_score");
                        if (hs != null && as != null) {
                            boolean directOrder = ht != null && ht.toLowerCase().contains(rec.getTeam1Name().toLowerCase());
                            return directOrder ? (hs + ":" + as) : (as + ":" + hs);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        return null;
    }

    public java.util.Optional<MatchAiRecommendation> settleRecommendationManually(Long recommendationId, String explicitScore) {
        java.util.Optional<MatchAiRecommendation> opt = recommendationRepository.findById(recommendationId);
        if (opt.isEmpty()) {
            return java.util.Optional.empty();
        }
        MatchAiRecommendation rec = opt.get();
        boolean settled = settleRecommendationWithScore(rec, explicitScore);
        if (settled) {
            return java.util.Optional.of(recommendationRepository.save(rec));
        }
        return java.util.Optional.empty();
    }

    @Transactional
    public boolean settleSingleRecommendation(MatchAiRecommendation rec) {
        if (rec.getMatchId() == null) {
            return false;
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
        if (rec.getMatchStartTime() != null) {
            startSec = rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
        } else if (header != null && header.getStartTimeEpochSec() != null) {
            startSec = header.getStartTimeEpochSec();
        }

        // 1. Guard: Match has not started yet
        if (startSec != null && nowSec < startSec) {
            return false;
        }

        // 2. Guard: Active Live Status -> NEVER settle an in-progress match!
        if (header != null && isLiveStatus(header.getStatus())) {
            log.debug("Settlement skipped for match #{}: match is actively live in MDM (status={})", rec.getMatchId(), header.getStatus());
            return false;
        }

        long requiredTimeoutSec = getSportSettlementTimeoutSec(sportName);
        boolean pastNormalDuration = startSec != null && (nowSec - startSec > requiredTimeoutSec);

        String s1 = null;
        String s2 = null;

        // Priority 1: Verified SofaScore ground-truth by exact fixture ID
        if (sofaFixtureId != null) {
            try {
                String sofaSql = "SELECT home_score, away_score, status FROM sofascore_raw_events WHERE event_id = ?";
                List<Map<String, Object>> sRows = jdbcTemplate.queryForList(sofaSql, sofaFixtureId);
                if (!sRows.isEmpty()) {
                    String st = (String) sRows.get(0).get("status");
                    if (isLiveStatus(st)) {
                        log.debug("Settlement skipped for match #{}: SofaScore reports event is actively live (status={})", rec.getMatchId(), st);
                        return false;
                    }
                    if (isFinishedStatus(st)) {
                        Object hs = sRows.get(0).get("home_score");
                        Object as = sRows.get(0).get("away_score");
                        if (hs != null && as != null && !String.valueOf(hs).isBlank() && !String.valueOf(as).isBlank()) {
                            s1 = String.valueOf(hs);
                            s2 = String.valueOf(as);
                            log.info("Settlement: Resolved final scores {}:{} for match #{} from sofascore_raw_events fixture #{}", s1, s2, rec.getMatchId(), sofaFixtureId);
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
                    log.debug("Settlement skipped for match #{}: MDM header status is live ({})", rec.getMatchId(), mdmStatus);
                    return false;
                }
                boolean isExplicitlyFinished = isFinishedStatus(mdmStatus);
                if (isExplicitlyFinished || pastNormalDuration) {
                    if (header.getScore1() != null && header.getScore2() != null &&
                            !header.getScore1().isBlank() && !header.getScore2().isBlank()) {
                        s1 = header.getScore1();
                        s2 = header.getScore2();
                        log.info("Settlement: Resolved final scores {}:{} for match #{} (status={}, pastDuration={}) directly from MDM Header / Redis Live Score",
                                s1, s2, rec.getMatchId(), mdmStatus, pastNormalDuration);
                    }
                }
            }
        }

        // Priority 3: Direct mdm_match database table
        if (s1 == null || s2 == null) {
            try {
                String mdmSql = "SELECT score1, score2, status FROM mdm_match WHERE id = ?";
                List<Map<String, Object>> mdmRows = jdbcTemplate.queryForList(mdmSql, rec.getMatchId());
                if (!mdmRows.isEmpty()) {
                    Map<String, Object> r = mdmRows.get(0);
                    String st = (String) r.get("status");
                    if (isLiveStatus(st)) {
                        log.debug("Settlement skipped for match #{}: mdm_match status is live ({})", rec.getMatchId(), st);
                        return false;
                    }
                    Object sc1 = r.get("score1");
                    Object sc2 = r.get("score2");
                    boolean isFinished = isFinishedStatus(st);
                    if (sc1 != null && sc2 != null && !String.valueOf(sc1).isBlank() && !String.valueOf(sc2).isBlank() && (isFinished || pastNormalDuration)) {
                        s1 = String.valueOf(sc1);
                        s2 = String.valueOf(sc2);
                        log.info("Settlement: Resolved final scores {}:{} for match #{} from mdm_match database (status={}, pastDuration={})",
                                s1, s2, rec.getMatchId(), st, pastNormalDuration);
                    }
                }
            } catch (Exception ex) {
                log.debug("mdm_match lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 4: Verified SofaScore ground-truth by team names bounded by start time and youth/gender isolation
        if ((s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) && rec.getTeam1Name() != null && rec.getTeam2Name() != null) {
            Long startTimeEpochMs = (startSec != null) ? startSec * 1000L : null;
            if (startTimeEpochMs != null && startTimeEpochMs > 0) {
                try {
                    String t1 = "%" + rec.getTeam1Name().trim() + "%";
                    String t2 = "%" + rec.getTeam2Name().trim() + "%";
                    long windowMs = 24L * 3600L * 1000L;
                    String teamSearchSql = "SELECT home_team, away_team, home_score, away_score, status FROM sofascore_raw_events " +
                            "WHERE (status = 'finished' OR status = 'ended' OR status = 'closed') " +
                            "AND match_date BETWEEN ? AND ? AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                            "AND home_team NOT ILIKE '%U17%' AND home_team NOT ILIKE '%U18%' AND home_team NOT ILIKE '%U19%' AND home_team NOT ILIKE '%U20%' AND home_team NOT ILIKE '%U21%' AND home_team NOT ILIKE '%U23%' " +
                            "AND away_team NOT ILIKE '%U17%' AND away_team NOT ILIKE '%U18%' AND away_team NOT ILIKE '%U19%' AND away_team NOT ILIKE '%U20%' AND away_team NOT ILIKE '%U21%' AND away_team NOT ILIKE '%U23%' " +
                            "AND ((home_team ILIKE ? AND away_team ILIKE ?) OR (home_team ILIKE ? AND away_team ILIKE ?)) " +
                            "ORDER BY captured_at DESC LIMIT 1";
                    List<Map<String, Object>> tRows = jdbcTemplate.queryForList(teamSearchSql, startTimeEpochMs - windowMs, startTimeEpochMs + windowMs, t1, t2, t2, t1);
                    if (!tRows.isEmpty()) {
                        Map<String, Object> row = tRows.get(0);
                        String ht = (String) row.get("home_team");
                        Object hs = row.get("home_score");
                        Object as = row.get("away_score");
                        if (hs != null && as != null && !String.valueOf(hs).isBlank() && !String.valueOf(as).isBlank()) {
                            boolean directOrder = ht != null && ht.toLowerCase().contains(rec.getTeam1Name().toLowerCase());
                            s1 = directOrder ? String.valueOf(hs) : String.valueOf(as);
                            s2 = directOrder ? String.valueOf(as) : String.valueOf(hs);
                            log.info("Settlement: Resolved final scores {}:{} for match #{} via team search in sofascore_raw_events", s1, s2, rec.getMatchId());
                        }
                    }
                } catch (Exception ex) {
                    log.debug("Team name lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
                }
            }
        }


        // Priority 5: Match postponed / cancelled / abandoned (> sport settlement timeout + 1h without result)
        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            Long startTimeEpochMs = (startSec != null) ? startSec * 1000L : null;
            long timeoutMs = (requiredTimeoutSec + 3600L) * 1000L;
            if (startTimeEpochMs != null && startTimeEpochMs < System.currentTimeMillis() - timeoutMs) {
                rec.setResultStatus("VOID");
                rec.setFinalScore("-:-");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("MATCH_POSTPONED_OR_ABANDONED");
                rec.setLessonLearned("Матч отменен, перенесен или результат не зафиксирован в течение допустимого времени. Оформлен возврат (VOID).");
                recommendationRepository.save(rec);
                log.info("Settled match #{} ({}-{}) as VOID: match expired >{}h ago without scores",
                        rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), timeoutMs / 3600000L);
                return true;
            }
            return false; // Match not yet finished or scores not recorded
        }

        return settleRecommendationWithScore(rec, s1 + ":" + s2);
    }

    public boolean settleRecommendationWithScore(MatchAiRecommendation rec, String explicitScore) {
        if (rec == null || explicitScore == null || explicitScore.isBlank()) {
            return false;
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

        try {
            Sport sport = Sports.get(sportName != null ? sportName : "FOOTBALL");
            ParsedScore parsed = parseFlexibleScore(explicitScore, sport);
            if (parsed == null) {
                log.warn("Cannot parse explicit score '{}' for match #{}", explicitScore, rec.getMatchId());
                return false;
            }

            rec.setFinalScore(parsed.displayScore);

            String market = rec.getRecommendedMarket();
            if (market != null) {
                String mUpper = market.toUpperCase().trim();
                if ("WIN1".equals(mUpper) || "P1".equals(mUpper) || "1".equals(mUpper) || "W1".equals(mUpper)) market = "P1";
                else if ("WIN2".equals(mUpper) || "P2".equals(mUpper) || "2".equals(mUpper) || "W2".equals(mUpper)) market = "P2";
                else if ("DRAW".equals(mUpper) || "PX".equals(mUpper) || "X".equals(mUpper)) market = "PX";
            }

            SettlementOutcome outcome = sport.evaluateSettlement(market, parsed.score1, parsed.score2, parsed.displayScore);
            if (outcome == SettlementOutcome.UNRESOLVED) {
                long timeoutSec = getSportSettlementTimeoutSec(sportName);
                Long startSec = rec.getMatchStartTime() != null ? rec.getMatchStartTime().toEpochSecond(ZoneOffset.UTC) : null;
                long nowSec = System.currentTimeMillis() / 1000L;
                if (startSec != null && (nowSec - startSec > timeoutSec)) {
                    rec.setResultStatus("VOID");
                    rec.setActualPnl(BigDecimal.ZERO);
                    rec.setRoiPct(BigDecimal.ZERO);
                    rec.setErrorCategory("UNRESOLVED_SCORE_TIMEOUT");
                    rec.setLessonLearned("Счет матча (" + parsed.displayScore + ") не позволил однозначно рассчитать исход рынка по спортивным правилам после завершения времени матча. Оформлен возврат (VOID).");
                    recommendationRepository.save(rec);
                    log.info("Settled match #{} as VOID due to UNRESOLVED outcome timeout (past timeout {}s, score={})",
                            rec.getMatchId(), timeoutSec, parsed.displayScore);
                    return true;
                }
                log.info("Outcome unresolved for match #{}: Sport={}, Market='{}', Score='{}'",
                        rec.getMatchId(), sport.getCode(), rec.getRecommendedMarket(), parsed.displayScore);
                return false;
            }

            BigDecimal unitStake = resolveStakeAmount(rec.getConvictionTier());
            BigDecimal odd = rec.getRecommendedOdd() != null ? rec.getRecommendedOdd() : BigDecimal.valueOf(1.90);

            if (outcome == SettlementOutcome.WON) {
                rec.setResultStatus("WON");
                BigDecimal pnl = odd.subtract(BigDecimal.ONE).multiply(unitStake).setScale(2, RoundingMode.HALF_UP);
                BigDecimal roi = odd.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100.0)).setScale(2, RoundingMode.HALF_UP);
                rec.setActualPnl(pnl);
                rec.setRoiPct(roi);
                rec.setErrorCategory("WON_AS_PLANNED");
                rec.setLessonLearned("Базовый сценарий полностью подтвердился. Тактическая гипотеза оправдана.");
            } else if (outcome == SettlementOutcome.LOST) {
                rec.setResultStatus("LOST");
                rec.setActualPnl(unitStake.negate());
                rec.setRoiPct(BigDecimal.valueOf(-100.0));
            } else if (outcome == SettlementOutcome.VOID) {
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("PUSH_VOID_PROTECTION");
                rec.setLessonLearned("Азиатская линия / правила отмены дисциплины защитили депозит возвратом при счете " + parsed.displayScore);
            }

            // Settle shadow picks if present & perform reflection
            settleShadowPicksAndReflect(rec, outcome, parsed.score1, parsed.score2, parsed.displayScore, sport);

            recommendationRepository.save(rec);
            log.info("Settled match #{} ({}-{}): Sport={}, Bet='{}', Score='{}', Stake={}, Result={}, ErrorCategory={}, PnL={}",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), sport.getCode(),
                    rec.getRecommendedMarket(), parsed.displayScore, unitStake, rec.getResultStatus(), rec.getErrorCategory(), rec.getActualPnl());
            return true;
        } catch (Exception e) {
            log.warn("Cannot settle recommendation for match #{}: {}", rec.getMatchId(), e.getMessage());
            return false;
        }
    }

    private static class ParsedScore {
        int score1;
        int score2;
        String displayScore;
    }

    private ParsedScore parseFlexibleScore(String scoreStr, Sport sport) {
        if (scoreStr == null || scoreStr.isBlank()) return null;
        String s = scoreStr.trim();
        ParsedScore res = new ParsedScore();
        res.displayScore = s;
        if (s.contains(",")) {
            String[] sets = s.split(",");
            int setsWon1 = 0, setsWon2 = 0;
            for (String set : sets) {
                String[] p = set.trim().split("[:\\-]");
                if (p.length == 2) {
                    try {
                        int p1 = Integer.parseInt(p[0].trim());
                        int p2 = Integer.parseInt(p[1].trim());
                        if (p1 > p2) setsWon1++;
                        else if (p2 > p1) setsWon2++;
                    } catch (Exception ignored) {}
                }
            }
            res.score1 = setsWon1;
            res.score2 = setsWon2;
            return res;
        }

        String[] parts = s.split("[:\\-]");
        if (parts.length >= 2) {
            try {
                res.score1 = Integer.parseInt(parts[0].trim());
                res.score2 = Integer.parseInt(parts[1].trim());
                return res;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void settleShadowPicksAndReflect(MatchAiRecommendation rec, SettlementOutcome outcome, int s1, int s2, String finalScore, Sport sport) {
        if (rec.getShadowPicksJson() != null && !rec.getShadowPicksJson().isBlank()) {
            try {
                List<ShadowPickDto> shadows = objectMapper.readValue(rec.getShadowPicksJson(), new TypeReference<List<ShadowPickDto>>() {});
                boolean anyShadowWon = false;
                ShadowPickDto winningShadow = null;

                for (ShadowPickDto sp : shadows) {
                    SettlementOutcome so = sport.evaluateSettlement(sp.getMarket(), s1, s2, finalScore);
                    sp.setResultStatus(so.name());
                    if (so == SettlementOutcome.WON && !anyShadowWon) {
                        anyShadowWon = true;
                        winningShadow = sp;
                    }
                }

                rec.setShadowPicksJson(objectMapper.writeValueAsString(shadows));

                if (outcome == SettlementOutcome.LOST) {
                    if (anyShadowWon && winningShadow != null) {
                        rec.setErrorCategory("SHADOW_DEFENSE_PROVEN");
                        String lesson = String.format("Опасение шального гола/сценария подтвердилось (счет %s): защитная ветка [%s (%s @ %s)] выжила и выиграла! " +
                                "Основная ставка проявила избыточную агрессивность. В аналогичных профилях рекомендовано отдавать приоритет защитной ветке.",
                                finalScore, winningShadow.getMarket(), winningShadow.getType(), winningShadow.getOdd());
                        rec.setLessonLearned(lesson);
                        saveLessonToMemory(rec, "SHADOW_DEFENSE_PROVEN", lesson, sport);
                    } else {
                        rec.setErrorCategory("TACTICAL_BLIND_SPOT");
                        String lesson = String.format("Ни один из сценариев не оправдался (счет %s). Системная ошибка оценки матча и формы команд. " +
                                "Требуется ревизия параметров защиты/нападения.", finalScore);
                        rec.setLessonLearned(lesson);
                        saveLessonToMemory(rec, "TACTICAL_BLIND_SPOT", lesson, sport);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not parse/settle shadow picks for match #{}: {}", rec.getMatchId(), e.getMessage());
            }
        } else {
            if (outcome == SettlementOutcome.LOST && rec.getErrorCategory() == null) {
                rec.setErrorCategory("TACTICAL_BLIND_SPOT");
            }
        }
    }

    private void saveLessonToMemory(MatchAiRecommendation rec, String category, String text, Sport sport) {
        try {
            String scope = (sport != null && sport.getMemoryScopes() != null && !sport.getMemoryScopes().isEmpty())
                    ? sport.getMemoryScopes().get(sport.getMemoryScopes().size() - 1)
                    : "GLOBAL";

            AiLearningMemory mem = AiLearningMemory.builder()
                    .teamName(rec.getTeam1Name())
                    .leagueName(rec.getLeagueName())
                    .sportScope(scope)
                    .contextCategory(category)
                    .lessonText(text)
                    .sourceMatchId(rec.getMatchId())
                    .confidenceScore(85)
                    .build();
            memoryRepository.save(mem);
            log.info("Saved learning lesson into ai_learning_memory [scope={}] for match #{}: {}", scope, rec.getMatchId(), text);
        } catch (Exception e) {
            log.error("Failed to save memory lesson for match #{}: {}", rec.getMatchId(), e.getMessage());
        }
    }
}
