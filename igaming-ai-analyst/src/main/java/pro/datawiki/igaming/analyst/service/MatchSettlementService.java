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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSettlementService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiLearningMemoryRepository memoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final BigDecimal DEFAULT_UNIT_STAKE = BigDecimal.valueOf(1000.0);

    /**
     * Periodically settles finished matches every 5 minutes.
     */
    @Scheduled(cron = "${analyst.scheduling.settle-cron:0 */5 * * * *}")
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
    public boolean settleSingleRecommendation(MatchAiRecommendation rec) {
        if (rec.getMatchId() == null) {
            return false;
        }

        String sql = "SELECT final_score1, final_score2, match_result, is_live FROM match_record WHERE id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, rec.getMatchId());
        if (rows.isEmpty()) {
            return false;
        }

        Map<String, Object> matchData = rows.get(0);
        String s1 = (String) matchData.get("final_score1");
        String s2 = (String) matchData.get("final_score2");
        Boolean isLive = (Boolean) matchData.get("is_live");

        if (s1 == null || s2 == null || Boolean.TRUE.equals(isLive)) {
            return false; // Match not yet finished or scores not recorded
        }

        try {
            int score1 = Integer.parseInt(s1.trim());
            int score2 = Integer.parseInt(s2.trim());
            String finalScore = score1 + ":" + score2;
            rec.setFinalScore(finalScore);

            SettlementOutcome outcome = evaluateBetOutcome(rec.getRecommendedMarket(), score1, score2);

            BigDecimal odd = rec.getRecommendedOdd() != null ? rec.getRecommendedOdd() : BigDecimal.valueOf(1.90);

            if (outcome == SettlementOutcome.WON) {
                rec.setResultStatus("WON");
                BigDecimal pnl = odd.subtract(BigDecimal.ONE).multiply(DEFAULT_UNIT_STAKE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal roi = odd.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100.0)).setScale(2, RoundingMode.HALF_UP);
                rec.setActualPnl(pnl);
                rec.setRoiPct(roi);
                rec.setErrorCategory("WON_AS_PLANNED");
                rec.setLessonLearned("Базовый сценарий полностью подтвердился. Тактическая гипотеза оправдана.");
            } else if (outcome == SettlementOutcome.LOST) {
                rec.setResultStatus("LOST");
                rec.setActualPnl(DEFAULT_UNIT_STAKE.negate());
                rec.setRoiPct(BigDecimal.valueOf(-100.0));
            } else if (outcome == SettlementOutcome.VOID) {
                rec.setResultStatus("VOID");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setRoiPct(BigDecimal.ZERO);
                rec.setErrorCategory("PUSH_VOID_PROTECTION");
                rec.setLessonLearned("Азиатская линия защитила депозит возвратом при счете " + finalScore);
            }

            // Settle shadow picks if present & perform reflection
            settleShadowPicksAndReflect(rec, outcome, score1, score2, finalScore);

            recommendationRepository.save(rec);
            log.info("Settled match #{} ({}-{}): Bet='{}', Score='{}', Result={}, ErrorCategory={}, PnL={}",
                    rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(),
                    rec.getRecommendedMarket(), finalScore, rec.getResultStatus(), rec.getErrorCategory(), rec.getActualPnl());
            return true;
        } catch (NumberFormatException e) {
            log.warn("Cannot parse scores for match #{}: {} - {}", rec.getMatchId(), s1, s2);
            return false;
        }
    }

    private void settleShadowPicksAndReflect(MatchAiRecommendation rec, SettlementOutcome outcome, int s1, int s2, String finalScore) {
        if (rec.getShadowPicksJson() != null && !rec.getShadowPicksJson().isBlank()) {
            try {
                List<ShadowPickDto> shadows = objectMapper.readValue(rec.getShadowPicksJson(), new TypeReference<List<ShadowPickDto>>() {});
                boolean anyShadowWon = false;
                ShadowPickDto winningShadow = null;

                for (ShadowPickDto sp : shadows) {
                    SettlementOutcome so = evaluateBetOutcome(sp.getMarket(), s1, s2);
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
                        saveLessonToMemory(rec, "SHADOW_DEFENSE_PROVEN", lesson);
                    } else {
                        rec.setErrorCategory("TACTICAL_BLIND_SPOT");
                        String lesson = String.format("Ни один из сценариев не оправдался (счет %s). Системная ошибка оценки матча и формы команд. " +
                                "Требуется ревизия параметров защиты/нападения.", finalScore);
                        rec.setLessonLearned(lesson);
                        saveLessonToMemory(rec, "TACTICAL_BLIND_SPOT", lesson);
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

    private void saveLessonToMemory(MatchAiRecommendation rec, String category, String text) {
        try {
            AiLearningMemory mem = AiLearningMemory.builder()
                    .teamName(rec.getTeam1Name())
                    .leagueName(rec.getLeagueName())
                    .contextCategory(category)
                    .lessonText(text)
                    .sourceMatchId(rec.getMatchId())
                    .confidenceScore(85)
                    .build();
            memoryRepository.save(mem);
            log.info("Saved learning lesson into ai_learning_memory for match #{}: {}", rec.getMatchId(), text);
        } catch (Exception e) {
            log.error("Failed to save memory lesson for match #{}: {}", rec.getMatchId(), e.getMessage());
        }
    }

    public enum SettlementOutcome {
        WON, LOST, VOID, UNRESOLVED
    }

    public SettlementOutcome evaluateBetOutcome(String market, int s1, int s2) {
        if (market == null) return SettlementOutcome.UNRESOLVED;
        String m = market.toUpperCase().trim();

        int diff = s1 - s2; // positive = team1 won
        int totalGoals = s1 + s2;

        // 1. Totals
        if (m.equals("TOTAL_OVER_2.5") || m.contains("OVER 2.5") || m.contains("ТБ 2.5") || m.contains("ТОТАЛ БОЛЬШЕ 2.5")) {
            return totalGoals >= 3 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_UNDER_2.5") || m.contains("UNDER 2.5") || m.contains("ТМ 2.5") || m.contains("ТОТАЛ МЕНЬШЕ 2.5")) {
            return totalGoals <= 2 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_OVER_2.0") || m.contains("OVER 2.0") || m.contains("ТБ 2.0")) {
            if (totalGoals > 2) return SettlementOutcome.WON;
            if (totalGoals == 2) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_UNDER_2.0") || m.contains("UNDER 2.0") || m.contains("ТМ 2.0")) {
            if (totalGoals < 2) return SettlementOutcome.WON;
            if (totalGoals == 2) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_OVER_3.0") || m.contains("OVER 3.0") || m.contains("ТБ 3.0")) {
            if (totalGoals > 3) return SettlementOutcome.WON;
            if (totalGoals == 3) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_UNDER_3.0") || m.contains("UNDER 3.0") || m.contains("ТМ 3.0")) {
            if (totalGoals < 3) return SettlementOutcome.WON;
            if (totalGoals == 3) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_OVER_1.5") || m.contains("OVER 1.5") || m.contains("ТБ 1.5")) {
            return totalGoals >= 2 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_UNDER_1.5") || m.contains("UNDER 1.5") || m.contains("ТМ 1.5")) {
            return totalGoals <= 1 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_OVER_3.5") || m.contains("OVER 3.5") || m.contains("ТБ 3.5")) {
            return totalGoals >= 4 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("TOTAL_UNDER_3.5") || m.contains("UNDER 3.5") || m.contains("ТМ 3.5")) {
            return totalGoals <= 3 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // 2. Both Teams To Score (BTTS)
        if (m.equals("BTTS_YES") || m.contains("BTTS YES") || m.contains("ОБЕ ЗАБЬЮТ: ДА") || m.contains("ОЗ ДА")) {
            return (s1 > 0 && s2 > 0) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("BTTS_NO") || m.contains("BTTS NO") || m.contains("ОБЕ ЗАБЬЮТ: НЕТ") || m.contains("ОЗ НЕТ")) {
            return (s1 == 0 || s2 == 0) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // 3. Double Chance
        if (m.equals("DOUBLE_CHANCE_1X") || m.contains(" 1X") || m.startsWith("1X")) {
            return diff >= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("DOUBLE_CHANCE_X2") || m.contains(" X2") || m.startsWith("X2")) {
            return diff <= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("DOUBLE_CHANCE_12") || m.contains(" 12") || m.startsWith("12")) {
            return diff != 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // 4. Asian Handicap 0 (Draw No Bet)
        if (m.equals("AH_HOME_0") || (m.contains("(0)") && (m.contains("1") || m.contains("HOME"))) || m.contains("DNB 1") || m.contains("ФОРА 1 (0)")) {
            if (diff > 0) return SettlementOutcome.WON;
            if (diff == 0) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_0") || (m.contains("(0)") && (m.contains("2") || m.contains("AWAY"))) || m.contains("DNB 2") || m.contains("ФОРА 2 (0)")) {
            if (diff < 0) return SettlementOutcome.WON;
            if (diff == 0) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // 5. Asian Handicap +0.25 / +0.5
        if (m.equals("AH_HOME_PLUS_0.25") || (m.contains("+0.25") && (m.contains("1") || m.contains("HOME")))) {
            return diff >= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_PLUS_0.25") || (m.contains("+0.25") && (m.contains("2") || m.contains("AWAY")))) {
            return diff <= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("AH_HOME_PLUS_0.5") || (m.contains("+0.5") && (m.contains("1") || m.contains("HOME")))) {
            return diff >= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_PLUS_0.5") || (m.contains("+0.5") && (m.contains("2") || m.contains("AWAY")))) {
            return diff <= 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // 6. Asian Handicap +1.0
        if (m.equals("AH_HOME_PLUS_1.0") || (m.contains("+1.0") && (m.contains("1") || m.contains("HOME")))) {
            if (diff >= 0) return SettlementOutcome.WON;
            if (diff == -1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_PLUS_1.0") || (m.contains("+1.0") && (m.contains("2") || m.contains("AWAY") || m.contains("BREST") || m.contains("GENOA")))) {
            if (diff <= 0) return SettlementOutcome.WON;
            if (diff == 1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // 7. Asian Handicap +2.0
        if (m.equals("AH_HOME_PLUS_2.0") || (m.contains("+2.0") && (m.contains("1") || m.contains("HOME") || m.contains("TELSTAR")))) {
            if (diff >= -1) return SettlementOutcome.WON;
            if (diff == -2) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_PLUS_2.0") || (m.contains("+2.0") && (m.contains("2") || m.contains("AWAY")))) {
            if (diff <= 1) return SettlementOutcome.WON;
            if (diff == 2) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // 8. Asian Handicap +2.5
        if (m.equals("AH_AWAY_PLUS_2.5") || (m.contains("+2.5") && (m.contains("2") || m.contains("AWAY") || m.contains("LOKEREN")))) {
            return (s2 + 2.5 > s1) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("AH_HOME_PLUS_2.5") || (m.contains("+2.5") && (m.contains("1") || m.contains("HOME")))) {
            return (s1 + 2.5 > s2) ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        // 9. Asian Handicap -1.0
        if (m.equals("AH_HOME_MINUS_1.0") || (m.contains("-1.0") && (m.contains("1") || m.contains("HOME") || m.contains("PHILADELPHIA")))) {
            if (diff > 1) return SettlementOutcome.WON;
            if (diff == 1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }
        if (m.equals("AH_AWAY_MINUS_1.0") || (m.contains("-1.0") && (m.contains("2") || m.contains("AWAY")))) {
            if (diff < -1) return SettlementOutcome.WON;
            if (diff == -1) return SettlementOutcome.VOID;
            return SettlementOutcome.LOST;
        }

        // 10. 1X2 Outrights
        if (m.equals("P1") || m.equals("П1") || m.equals("HOME") || m.equals("HOME_WIN") || m.equals("WIN1") || m.contains("ПОБЕДА 1")) {
            return diff > 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("P2") || m.equals("П2") || m.equals("AWAY") || m.equals("AWAY_WIN") || m.equals("WIN2") || m.contains("ПОБЕДА 2")) {
            return diff < 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }
        if (m.equals("X") || m.equals("Х") || m.equals("DRAW") || m.contains("НИЧЬЯ")) {
            return diff == 0 ? SettlementOutcome.WON : SettlementOutcome.LOST;
        }

        return SettlementOutcome.UNRESOLVED;
    }
}
