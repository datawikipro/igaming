package pro.datawiki.igaming.analyst.forensic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.basketball.ClassicBasketballSport;

/**
 * Institutional forensic auditor for Basketball.
 * Investigates:
 * 1. 4th Quarter clutch intentional foul handicap inflation (garbage time foul cascades).
 * 2. Back-to-Back (B2B) / 3-in-4 nights fatigue schedule strain.
 * 3. Starting 5 vs Bench scoring collapse (foul trouble of key rim protector / bucket getter).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BasketballForensicAuditor implements SportForensicAuditor<ClassicBasketballSport> {

    private final ObjectMapper objectMapper;
    private final ClassicBasketballSport sport = new ClassicBasketballSport();

    @Override
    public ClassicBasketballSport getSport() {
        return sport;
    }

    @Override
    public String buildForensicPrompt(ForensicMatchContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("ВЫ — СТАРШИЙ КВАНТОВЫЙ АНАЛИТИК ИСПЫТАТЕЛЬНОЙ ЛАБОРАТОРИИ АЛГОРИТМИЧЕСКИХ СТРАТЕГИЙ (БАСКЕТБОЛ).\n");
        sb.append("ВАША ЦЕЛЬ: Провести судебную квант-экспертизу баскетбольного матча когорты «Железный банк» (высокая уверенность >= 80%, кэф 1.20-1.60), где модель допустила проигрыш форы или тотала.\n\n");

        sb.append("### 1. ПРЕ-МАТЧЕВЫЙ КВАНТОВЫЙ ПРОФИЛЬ\n");
        sb.append("- Матч: ").append(ctx.getHomeTeam()).append(" — ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Турнир: ").append(ctx.getLeagueName()).append("\n");
        sb.append("- Рекомендованная ставка: ").append(ctx.getRecommendedMarket())
          .append(" (Кэф: ").append(ctx.getRecommendedOdd())
          .append(", Модельная вероятность: ").append(ctx.getFairProbability()).append("%)\n");
        sb.append("- Пре-матчевая гипотеза модели: ").append(ctx.getCoreHypothesis() != null ? ctx.getCoreHypothesis() : "Удержание форы андердогом").append("\n\n");

        sb.append("### 2. СТРУКТУРА РОСТЕРА И ГРАФИК ОТДЫХА\n");
        sb.append("- Хозяева: ").append(ctx.getHomeTeam()).append("\n");
        sb.append("  * Ключевой скорер (Primary Bucket Getter): ").append(ctx.getHomeTalismanName() != null ? ctx.getHomeTalismanName() : "Лидер ротации").append("\n");
        sb.append("  * Потери/Травмы: ").append(ctx.getHomeMissingList() != null ? String.join(", ", ctx.getHomeMissingList()) : "Нет").append("\n");

        sb.append("- Гости: ").append(ctx.getAwayTeam()).append("\n");
        sb.append("  * Ключевой скорер: ").append(ctx.getAwayTalismanName() != null ? ctx.getAwayTalismanName() : "Лидер ротации").append("\n");
        sb.append("  * Потери/Травмы: ").append(ctx.getAwayMissingList() != null ? String.join(", ", ctx.getAwayMissingList()) : "Нет").append("\n\n");

        sb.append("### 3. ПОЧЕТВЕРТНОЙ ПРОТОКОЛ МАТЧА (GROUND TRUTH TIMELINE)\n");
        sb.append("- Итоговый счет: ").append(ctx.getFinalScore()).append("\n");
        sb.append("- Почетвертная динамика: ").append(ctx.getPeriodTimeline() != null ? ctx.getPeriodTimeline() : "Q1, Q2, Q3, Q4").append("\n");
        sb.append("- Хронология клатча и 4-й четверти:\n");
        if (ctx.getGoalChronology() != null && !ctx.getGoalChronology().isEmpty()) {
            for (String g : ctx.getGoalChronology()) {
                sb.append("  * ").append(g).append("\n");
            }
        } else {
            sb.append("  * Финальный счет: ").append(ctx.getFinalScore()).append("\n");
        }
        sb.append("\n");

        sb.append("### 4. ВОПРОСЫ СУДЕБНОЙ ЭКСПЕРТИЗЫ:\n");
        sb.append("1. Что разрушило фору в 4-й четверти? (Серия умышленных тактических фолов за 45 секунд до конца? Провал ротации скамейки? B2B усталость?)\n");
        sb.append("2. Какую универсальную координату мы обязаны добавить в BasketballVectorExtractor (например, clutch_intentional_foul_volatility, b2b_fatigue_drain)?\n");
        sb.append("3. Напишите формулу признака.\n\n");

        sb.append("ВЫВЕДИТЕ ОТВЕТ СТРОГО В ВИДЕ ВАЛИДНОГО JSON-ОБЪЕКТА:\n");
        sb.append("{\n");
        sb.append("  \"match_id\": ").append(ctx.getMatchId() != null ? ctx.getMatchId() : 0).append(",\n");
        sb.append("  \"recommendation_id\": ").append(ctx.getRecommendationId() != null ? ctx.getRecommendationId() : 0).append(",\n");
        sb.append("  \"sport_code\": \"BASKETBALL\",\n");
        sb.append("  \"primary_failure_mechanism\": \"GARBAGE_TIME_INTENTIONAL_FOULS | B2B_REST_DEFICIT | BENCH_COLLAPSE | RIM_PROTECTOR_FOUL_TROUBLE\",\n");
        sb.append("  \"inflection_minute\": 47,\n");
        sb.append("  \"inflection_description\": \"Описание развала счета в концовке\",\n");
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
            log.error("Failed to parse basketball forensic audit report: {}", e.getMessage());
            return null;
        }
    }
}
