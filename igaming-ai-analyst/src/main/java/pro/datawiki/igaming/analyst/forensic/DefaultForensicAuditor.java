package pro.datawiki.igaming.analyst.forensic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

/**
 * Universal fallback forensic auditor for generic sports (Table Tennis, Esports, etc.).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultForensicAuditor implements SportForensicAuditor<TableTennisSport> {

    private final ObjectMapper objectMapper;
    private final TableTennisSport sport = new TableTennisSport();

    @Override
    public TableTennisSport getSport() {
        return sport;
    }

    @Override
    public String buildForensicPrompt(ForensicMatchContext ctx) {
        StringBuilder sb = new StringBuilder();
        String sportName = ctx.getSport() != null ? ctx.getSport().getCode() : "SPORT";
        sb.append("ВЫ — СТАРШИЙ КВАНТОВЫЙ АНАЛИТИК ИСПЫТАТЕЛЬНОЙ ЛАБОРАТОРИИ АЛГОРИТМИЧЕСКИХ СТРАТЕГИЙ (").append(sportName).append(").\n");
        sb.append("ВАША ЦЕЛЬ: Провести судебную квант-экспертизу матча когорты «Железный банк» (высокая уверенность >= 80%, кэф 1.20-1.60), где модель допустила сбой прогноза.\n\n");

        sb.append("### 1. ПРЕ-МАТЧЕВЫЙ КВАНТОВЫЙ ПРОФИЛЬ\n");
        sb.append("- Матч: ").append(ctx.getHomeTeam()).append(" — ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Турнир: ").append(ctx.getLeagueName()).append("\n");
        sb.append("- Дата проведения: ").append(ctx.getMatchStartTime() != null ? ctx.getMatchStartTime() : "30 октября 2026 г. (ВЧЕРА)").append("\n");
        sb.append("⚠️ ВНИМАНИЕ: Матч сыгран ВЧЕРА (30 октября 2026 г.)! Анализировать строго вчерашний протокол!\n");
        sb.append("- Рекомендованная ставка: ").append(ctx.getRecommendedMarket())
          .append(" (Кэф: ").append(ctx.getRecommendedOdd())
          .append(", Модельная вероятность: ").append(ctx.getFairProbability()).append("%)\n");
        sb.append("- Пре-матчевая гипотеза модели: ").append(ctx.getCoreHypothesis() != null ? ctx.getCoreHypothesis() : "Преимущество фаворита").append("\n\n");

        sb.append("### 2. ФАКТИЧЕСКИЙ ПРОТОКОЛ МАТЧА\n");
        sb.append("- Итоговый счет: ").append(ctx.getFinalScore()).append("\n");
        sb.append("- Периоды/Сеты: ").append(ctx.getPeriodTimeline() != null ? ctx.getPeriodTimeline() : "Нет данных").append("\n\n");

        sb.append("### 3. ВОПРОСЫ СУДЕБНОЙ ЭКСПЕРТИЗЫ:\n");
        sb.append("1. Что разрушило ставку высокой уверенности?\n");
        sb.append("2. Какую универсальную координату мы обязаны добавить в вектор признаков?\n");
        sb.append("3. Напишите формулу признака.\n\n");

        sb.append("ВЫВЕДИТЕ ОТВЕТ СТРОГО В ВИДЕ ВАЛИДНОГО JSON-ОБЪЕКТА:\n");
        sb.append("{\n");
        sb.append("  \"match_id\": ").append(ctx.getMatchId() != null ? ctx.getMatchId() : 0).append(",\n");
        sb.append("  \"recommendation_id\": ").append(ctx.getRecommendationId() != null ? ctx.getRecommendationId() : 0).append(",\n");
        sb.append("  \"sport_code\": \"").append(sportName).append("\",\n");
        sb.append("  \"primary_failure_mechanism\": \"TACTICAL_COLLAPSE\",\n");
        sb.append("  \"inflection_minute\": 0,\n");
        sb.append("  \"inflection_description\": \"Описание момента сбоя\",\n");
        sb.append("  \"missing_feature_name\": \"имя_координаты\",\n");
        sb.append("  \"missing_feature_formula\": \"формула\",\n");
        sb.append("  \"weight_adjustment_recommendation\": \"штраф\",\n");
        sb.append("  \"confidence_score\": 85\n");
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
            log.error("Failed to parse generic forensic audit report: {}", e.getMessage());
            return null;
        }
    }
}
