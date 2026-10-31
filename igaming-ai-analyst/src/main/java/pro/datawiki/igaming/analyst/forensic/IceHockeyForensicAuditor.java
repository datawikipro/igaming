package pro.datawiki.igaming.analyst.forensic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.hockey.IceHockeySport;

/**
 * Institutional forensic auditor for Ice Hockey.
 * Investigates:
 * 1. Regulation Tie Equilibrium Drag (intentional tempo freeze at 1:1 in 3rd period for overtime point).
 * 2. Starting Goalie vs Backup Netminder disparity.
 * 3. Special teams fatigue (excessive penalty kill strain).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IceHockeyForensicAuditor implements SportForensicAuditor<IceHockeySport> {

    private final ObjectMapper objectMapper;
    private final IceHockeySport sport = new IceHockeySport();

    @Override
    public IceHockeySport getSport() {
        return sport;
    }

    @Override
    public String buildForensicPrompt(ForensicMatchContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("ВЫ — СТАРШИЙ КВАНТОВЫЙ АНАЛИТИК ИСПЫТАТЕЛЬНОЙ ЛАБОРАТОРИИ АЛГОРИТМИЧЕСКИХ СТРАТЕГИЙ (ХОККЕЙ).\n");
        sb.append("ВАША ЦЕЛЬ: Провести судебную квант-экспертизу хоккейного матча когорты «Железный банк» (высокая уверенность >= 80%, кэф 1.20-1.60), где модель допустила проигрыш тотала или победы.\n\n");

        sb.append("### 1. ПРЕ-МАТЧЕВЫЙ КВАНТОВЫЙ ПРОФИЛЬ\n");
        sb.append("- Матч: ").append(ctx.getHomeTeam()).append(" — ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Турнир: ").append(ctx.getLeagueName()).append("\n");
        sb.append("- Рекомендованная ставка: ").append(ctx.getRecommendedMarket())
          .append(" (Кэф: ").append(ctx.getRecommendedOdd())
          .append(", Модельная вероятность: ").append(ctx.getFairProbability()).append("%)\n");
        sb.append("- Пре-матчевая гипотеза модели: ").append(ctx.getCoreHypothesis() != null ? ctx.getCoreHypothesis() : "Верховой темп матча").append("\n\n");

        sb.append("### 2. СТРУКТУРА КОМАНД И ФАКТОР ВРАТАРЕЙ\n");
        sb.append("- Хозяева: ").append(ctx.getHomeTeam()).append("\n");
        sb.append("- Гости: ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Регламентная система турнира: 3-очковая европейская система за победу в основное время (1 очко гарантировано при овертайме)\n\n");

        sb.append("### 3. ПОПЕРИОДНЫЙ ПРОТОКОЛ МАТЧА (GROUND TRUTH TIMELINE)\n");
        sb.append("- Итоговый счет (основное время): ").append(ctx.getFinalScore()).append("\n");
        sb.append("- Периоды: ").append(ctx.getPeriodTimeline() != null ? ctx.getPeriodTimeline() : "P1, P2, P3").append("\n");
        sb.append("- Хронология голов:\n");
        if (ctx.getGoalChronology() != null && !ctx.getGoalChronology().isEmpty()) {
            for (String g : ctx.getGoalChronology()) {
                sb.append("  * ").append(g).append("\n");
            }
        } else {
            sb.append("  * Счет: ").append(ctx.getFinalScore()).append("\n");
        }
        sb.append("\n");

        sb.append("### 4. ВОПРОСЫ СУДЕБНОЙ ЭКСПЕРТИЗЫ:\n");
        sb.append("1. Что разрушило ставку на Тотал Больше? (Заморозка игры при счете 1:1 в 3-м периоде ради гарантированного 1 очка? Выдающаяся игра вратаря? Пассивный откат в средней зоне?)\n");
        sb.append("2. Какую универсальную координату мы обязаны добавить в IceHockeyVectorExtractor (например, regulation_tie_equilibrium_drag, netminder_gsaa_delta)?\n");
        sb.append("3. Напишите формулу признака.\n\n");

        sb.append("ВЫВЕДИТЕ ОТВЕТ СТРОГО В ВИДЕ ВАЛИДНОГО JSON-ОБЪЕКТА:\n");
        sb.append("{\n");
        sb.append("  \"match_id\": ").append(ctx.getMatchId() != null ? ctx.getMatchId() : 0).append(",\n");
        sb.append("  \"recommendation_id\": ").append(ctx.getRecommendationId() != null ? ctx.getRecommendationId() : 0).append(",\n");
        sb.append("  \"sport_code\": \"ICE_HOCKEY\",\n");
        sb.append("  \"primary_failure_mechanism\": \"REGULATION_TIE_EQUILIBRIUM_FREEZE | NETMINDER_GOD_MODE | NEUTRAL_ZONE_TRAP_SUFFOCATION\",\n");
        sb.append("  \"inflection_minute\": 45,\n");
        sb.append("  \"inflection_description\": \"Описание засухи шайб в 3-м периоде\",\n");
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
            log.error("Failed to parse ice hockey forensic audit report: {}", e.getMessage());
            return null;
        }
    }
}
