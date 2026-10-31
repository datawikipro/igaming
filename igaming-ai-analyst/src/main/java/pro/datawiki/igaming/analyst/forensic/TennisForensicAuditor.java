package pro.datawiki.igaming.analyst.forensic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

/**
 * Institutional forensic auditor for Tennis.
 * Investigates:
 * 1. Break point clutch conversion under pressure (30:40).
 * 2. ATP Main Tour vs Challenger / Grand Slam tier disparity.
 * 3. Accumulated fatigue from previous multi-set marathon matches.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TennisForensicAuditor implements SportForensicAuditor<TennisSport> {

    private final ObjectMapper objectMapper;
    private final TennisSport sport = new TennisSport();

    @Override
    public TennisSport getSport() {
        return sport;
    }

    @Override
    public String buildForensicPrompt(ForensicMatchContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("ВЫ — СТАРШИЙ КВАНТОВЫЙ АНАЛИТИК ИСПЫТАТЕЛЬНОЙ ЛАБОРАТОРИИ АЛГОРИТМИЧЕСКИХ СТРАТЕГИЙ (ТЕННИС).\n");
        sb.append("ВАША ЦЕЛЬ: Провести судебную квант-экспертизу теннисного матча когорты «Железный банк» (высокая уверенность >= 80%, кэф 1.20-1.60), где модель допустила проигрыш сета или форы.\n\n");

        sb.append("### 1. ПРЕ-МАТЧЕВЫЙ КВАНТОВЫЙ ПРОФИЛЬ\n");
        sb.append("- Матч: ").append(ctx.getHomeTeam()).append(" — ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Турнир: ").append(ctx.getLeagueName()).append("\n");
        sb.append("- Рекомендованная ставка: ").append(ctx.getRecommendedMarket())
          .append(" (Кэф: ").append(ctx.getRecommendedOdd())
          .append(", Модельная вероятность: ").append(ctx.getFairProbability()).append("%)\n");
        sb.append("- Пре-матчевая гипотеза модели: ").append(ctx.getCoreHypothesis() != null ? ctx.getCoreHypothesis() : "Равная борьба / удержание сета").append("\n\n");

        sb.append("### 2. СТИЛИ И КЛАСС СОПЕРНИКОВ\n");
        sb.append("- Игрок 1: ").append(ctx.getHomeTeam()).append("\n");
        sb.append("- Игрок 2: ").append(ctx.getAwayTeam()).append("\n\n");

        sb.append("### 3. ПОСЕТОВЫЙ ПРОТОКОЛ МАТЧА (GROUND TRUTH TIMELINE)\n");
        sb.append("- Итоговый счет: ").append(ctx.getFinalScore()).append("\n");
        sb.append("- Динамика сетов: ").append(ctx.getPeriodTimeline() != null ? ctx.getPeriodTimeline() : "Set 1, Set 2").append("\n");
        sb.append("- Ключевые брейки:\n");
        if (ctx.getGoalChronology() != null && !ctx.getGoalChronology().isEmpty()) {
            for (String g : ctx.getGoalChronology()) {
                sb.append("  * ").append(g).append("\n");
            }
        } else {
            sb.append("  * Счет матча: ").append(ctx.getFinalScore()).append("\n");
        }
        sb.append("\n");

        sb.append("### 4. ВОПРОСЫ СУДЕБНОЙ ЭКСПЕРТИЗЫ:\n");
        sb.append("1. Что разрушило прогноз? (Качественный разрыв в классе первой подачи? Провал конверсии брейк-поинтов? Накопленная усталость?)\n");
        sb.append("2. Какую универсальную координату мы обязаны добавить в TennisVectorExtractor (например, break_point_clutch_delta, atp_main_tour_tier_delta)?\n");
        sb.append("3. Напишите формулу признака.\n\n");

        sb.append("ВЫВЕДИТЕ ОТВЕТ СТРОГО В ВИДЕ ВАЛИДНОГО JSON-ОБЪЕКТА:\n");
        sb.append("{\n");
        sb.append("  \"match_id\": ").append(ctx.getMatchId() != null ? ctx.getMatchId() : 0).append(",\n");
        sb.append("  \"recommendation_id\": ").append(ctx.getRecommendationId() != null ? ctx.getRecommendationId() : 0).append(",\n");
        sb.append("  \"sport_code\": \"TENNIS\",\n");
        sb.append("  \"primary_failure_mechanism\": \"BREAK_POINT_CLUTCH_CHOKE | SLAM_CLASS_DISPARITY | ACCUMULATED_MARATHON_FATIGUE\",\n");
        sb.append("  \"inflection_minute\": 1,\n");
        sb.append("  \"inflection_description\": \"Описание доминирования фаворита\",\n");
        sb.append("  \"missing_feature_name\": \"имя_координаты\",\n");
        sb.append("  \"missing_feature_formula\": \"формула\",\n");
        sb.append("  \"weight_adjustment_recommendation\": \"рекомендация\",\n");
        sb.append("  \"confidence_score\": 90\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public ForensicAuditReport parseAuditReport(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) return null;
        try {
            String clean = jsonResponse.trim();
            if (clean.startsWith("```json")) clean = clean.substring(7);
            if (clean.startsWith("```")) clean = clean.substring(3);
            if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
            return objectMapper.readValue(clean.trim(), ForensicAuditReport.class);
        } catch (Exception e) {
            log.error("Failed to parse tennis forensic audit report: {}", e.getMessage());
            return null;
        }
    }
}
