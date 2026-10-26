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

    /**
     * Periodically settles finished matches every 2 minutes.
     */
    @Scheduled(cron = "${analyst.scheduling.settle-cron:0 */2 * * * *}")
    @Transactional
    public void settleFinishedMatches() {
        // Check all pending recommendations where scores are ready in match_record
        List<MatchAiRecommendation> pending = recommendationRepository.findByResultStatus("PENDING");

        if (pending.isEmpty()) {
            return;
        }

        log.info("Checking settlement for {} pending AI recommendations...", pending.size());
        for (MatchAiRecommendation rec : pending) {
            settleSingleRecommendation(rec);
        }
    }

    @Transactional
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

        String s1 = null;
        String s2 = null;

        // Priority 1: Check Canonical MDM Match Header (backed by Redis Live Score Cache-Aside)
        if (header != null && header.getStatus() != null) {
            String mdmStatus = header.getStatus().toUpperCase();
            if ("FINISHED".equals(mdmStatus) || "ENDED".equals(mdmStatus) || "CLOSED".equals(mdmStatus)) {
                if (header.getScore1() != null && header.getScore2() != null &&
                        !header.getScore1().isBlank() && !header.getScore2().isBlank()) {
                    s1 = header.getScore1();
                    s2 = header.getScore2();
                    log.info("Settlement: Resolved final scores {}:{} for match #{} directly from MDM Header / Redis Live Score",
                            s1, s2, rec.getMatchId());
                }
            }
        }

        // Priority 2: Check mdm_match database table
        if (s1 == null || s2 == null) {
            try {
                String mdmSql = "SELECT score1, score2, status FROM mdm_match WHERE id = ?";
                List<Map<String, Object>> mdmRows = jdbcTemplate.queryForList(mdmSql, rec.getMatchId());
                if (!mdmRows.isEmpty()) {
                    Map<String, Object> r = mdmRows.get(0);
                    String st = (String) r.get("status");
                    Object sc1 = r.get("score1");
                    Object sc2 = r.get("score2");
                    if (sc1 != null && sc2 != null && ("FINISHED".equalsIgnoreCase(st) || "ENDED".equalsIgnoreCase(st))) {
                        s1 = String.valueOf(sc1);
                        s2 = String.valueOf(sc2);
                        log.info("Settlement: Resolved final scores {}:{} for match #{} from mdm_match database", s1, s2, rec.getMatchId());
                    }
                }
            } catch (Exception ex) {
                log.debug("mdm_match lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 2.5: Check legacy match_record table fallback while migration stabilizes
        if (s1 == null || s2 == null) {
            try {
                String legSql = "SELECT final_score1, final_score2 FROM match_record WHERE id = ? AND final_score1 IS NOT NULL AND final_score2 IS NOT NULL";
                List<Map<String, Object>> legRows = jdbcTemplate.queryForList(legSql, rec.getMatchId());
                if (!legRows.isEmpty()) {
                    Object sc1 = legRows.get(0).get("final_score1");
                    Object sc2 = legRows.get(0).get("final_score2");
                    if (sc1 != null && sc2 != null && !String.valueOf(sc1).isBlank() && !String.valueOf(sc2).isBlank()) {
                        s1 = String.valueOf(sc1);
                        s2 = String.valueOf(sc2);
                        log.info("Settlement: Resolved final scores {}:{} for match #{} from match_record table", s1, s2, rec.getMatchId());
                    }
                }
            } catch (Exception ex) {
                log.debug("match_record lookup fallback ignored: {}", ex.getMessage());
            }
        }

        // Priority 3: lookup by sofascore event_id
        if ((s1 == null || s2 == null) && sofaFixtureId != null) {
            try {
                String sofaSql = "SELECT home_score, away_score, status FROM sofascore_raw_events WHERE event_id = ?";
                List<Map<String, Object>> sRows = jdbcTemplate.queryForList(sofaSql, sofaFixtureId);
                if (!sRows.isEmpty()) {
                    Object hs = sRows.get(0).get("home_score");
                    Object as = sRows.get(0).get("away_score");
                    String st = (String) sRows.get(0).get("status");
                    if (hs != null && as != null && ("finished".equalsIgnoreCase(st) || "ended".equalsIgnoreCase(st) || "closed".equalsIgnoreCase(st))) {
                        s1 = String.valueOf(hs);
                        s2 = String.valueOf(as);
                        log.info("Settlement: Resolved final scores {}:{} for match #{} from sofascore_raw_events fixture #{}", s1, s2, rec.getMatchId(), sofaFixtureId);
                    }
                }
            } catch (Exception ex) {
                log.debug("Fixture lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 2: lookup by team names in sofascore_raw_events bounded by match start time
        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            try {
                String t1 = rec.getTeam1Name() != null ? "%" + rec.getTeam1Name().trim() + "%" : "";
                String t2 = rec.getTeam2Name() != null ? "%" + rec.getTeam2Name().trim() + "%" : "";
                if (!t1.isBlank() && !t2.isBlank()) {
                    Long startTimeEpochMs = null;
                    if (header != null && header.getStartTimeEpochSec() != null) {
                        startTimeEpochMs = header.getStartTimeEpochSec() * 1000L;
                    } else if (rec.getMatchStartTime() != null) {
                        startTimeEpochMs = rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
                    }

                    List<Map<String, Object>> tRows;
                    if (startTimeEpochMs != null && startTimeEpochMs > 0) {
                        long windowMs = 24L * 3600L * 1000L;
                        String teamSearchSql = "SELECT home_score, away_score, status FROM sofascore_raw_events " +
                                "WHERE (status = 'finished' OR status = 'ended' OR status = 'closed') " +
                                "AND match_date BETWEEN ? AND ? AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                                "AND ((home_team ILIKE ? AND away_team ILIKE ?) OR (home_team ILIKE ? AND away_team ILIKE ?)) " +
                                "ORDER BY captured_at DESC LIMIT 1";
                        tRows = jdbcTemplate.queryForList(teamSearchSql, startTimeEpochMs - windowMs, startTimeEpochMs + windowMs, t1, t2, t2, t1);
                    } else {
                        String teamSearchSql = "SELECT home_score, away_score, status FROM sofascore_raw_events " +
                                "WHERE (status = 'finished' OR status = 'ended' OR status = 'closed') " +
                                "AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                                "AND ((home_team ILIKE ? AND away_team ILIKE ?) OR (home_team ILIKE ? AND away_team ILIKE ?)) " +
                                "ORDER BY captured_at DESC LIMIT 1";
                        tRows = jdbcTemplate.queryForList(teamSearchSql, t1, t2, t2, t1);
                    }
                    if (!tRows.isEmpty()) {
                        Object hs = tRows.get(0).get("home_score");
                        Object as = tRows.get(0).get("away_score");
                        if (hs != null && as != null) {
                            s1 = String.valueOf(hs);
                            s2 = String.valueOf(as);
                            log.info("Settlement: Resolved final scores {}:{} for match #{} via team search in sofascore_raw_events", s1, s2, rec.getMatchId());
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("Team name lookup failed for match #{}: {}", rec.getMatchId(), ex.getMessage());
            }
        }

        // Priority 3: Match postponed / cancelled / abandoned (>16 hours without result)
        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            Long startTimeEpochMs = null;
            if (header != null && header.getStartTimeEpochSec() != null) {
                startTimeEpochMs = header.getStartTimeEpochSec() * 1000L;
            } else if (rec.getMatchStartTime() != null) {
                startTimeEpochMs = rec.getMatchStartTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            }

            if (startTimeEpochMs != null && startTimeEpochMs < System.currentTimeMillis() - (16L * 3600L * 1000L)) {
                rec.setResultStatus("VOID");
                rec.setFinalScore("-:-");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("MATCH_POSTPONED_OR_ABANDONED");
                rec.setLessonLearned("Матч отменен, перенесен или результат не зафиксирован в течение 16 часов. Оформлен возврат (VOID).");
                recommendationRepository.save(rec);
                log.info("Settled match #{} ({}-{}) as VOID: match expired >16h ago without scores",
                        rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name());
                return true;
            }
            return false; // Match not yet finished or scores not recorded
        }



        return settleRecommendationWithScore(rec, s1 + ":" + s2);
    }

    @Transactional
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

            SettlementOutcome outcome = sport.evaluateSettlement(rec.getRecommendedMarket(), parsed.score1, parsed.score2, parsed.displayScore);
            if (outcome == SettlementOutcome.UNRESOLVED) {
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
