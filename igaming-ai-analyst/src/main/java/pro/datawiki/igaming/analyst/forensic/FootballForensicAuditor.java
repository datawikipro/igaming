package pro.datawiki.igaming.analyst.forensic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.football.SoccerSport;

import java.util.List;

/**
 * Institutional forensic auditor for Football (Soccer).
 * Investigates:
 * 1. Pace Mismatch (defense age vs attack age).
 * 2. Attacking hierarchy: Alpha Talisman vs Beta Wingman isolation.
 * 3. International window bio-congestion and jet-lag travel friction.
 * 4. Early shock goal (0-15') gamestate destruction and late discipline surges.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FootballForensicAuditor implements SportForensicAuditor<SoccerSport> {

    private final ObjectMapper objectMapper;
    private final SoccerSport sport = new SoccerSport();

    @Override
    public SoccerSport getSport() {
        return sport;
    }

    @Override
    public String buildForensicPrompt(ForensicMatchContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("ВЫ — СТАРШИЙ КВАНТОВЫЙ АНАЛИТИК ИСПЫТАТЕЛЬНОЙ ЛАБОРАТОРИИ АЛГОРИТМИЧЕСКИХ СТРАТЕГИЙ (ФУТБОЛ).\n");
        sb.append("ВАША ЦЕЛЬ: Провести глубокую судебную квант-экспертизу матча из когорты «Железный банк» (высокая уверенность >= 80%, кэф 1.20-1.60), где модель допустила проигрыш.\n\n");

        sb.append("### 1. ПРЕ-МАТЧЕВЫЙ КВАНТОВЫЙ ПРОФИЛЬ\n");
        sb.append("- Матч: ").append(ctx.getHomeTeam()).append(" — ").append(ctx.getAwayTeam()).append("\n");
        sb.append("- Турнир: ").append(ctx.getLeagueName()).append("\n");
        sb.append("- Рекомендованная ставка: ").append(ctx.getRecommendedMarket())
          .append(" (Кэф: ").append(ctx.getRecommendedOdd())
          .append(", Модельная вероятность: ").append(ctx.getFairProbability()).append("%)\n");
        sb.append("- Пре-матчевая гипотеза модели: ").append(ctx.getCoreHypothesis() != null ? ctx.getCoreHypothesis() : "Доминирование фаворита по xG").append("\n\n");

        sb.append("### 2. СТРУКТУРА СОСТАВОВ И БИОЛОГИЧЕСКИЕ КООРДИНАТЫ\n");
        sb.append("- Хозяева: Защита ср. возраст: ").append(formatAge(ctx.getHomeDefenseAvgAge()))
          .append(" лет | Атака ср. возраст: ").append(formatAge(ctx.getHomeAttackAvgAge())).append(" лет\n");
        sb.append("  * Альфа-Талисман: ").append(ctx.getHomeTalismanName() != null ? ctx.getHomeTalismanName() : "Коллективный билдап")
          .append(" (Доля голов: ").append(formatPct(ctx.getHomeTalismanGoalPct()))
          .append(", Статус: ").append(ctx.getHomeTalismanStatus() != null ? ctx.getHomeTalismanStatus() : "PLAYING").append(")\n");
        sb.append("  * Ведомый Бета: ").append(ctx.getHomeBetaName() != null ? ctx.getHomeBetaName() : "Ротация")
          .append(" (Статус: ").append(ctx.getHomeBetaStatus() != null ? ctx.getHomeBetaStatus() : "PLAYING").append(")\n");
        sb.append("  * Игроки сборных с усталостью (< 72ч отдыха / перелет): ").append(ctx.getHomeNationalTeamFatiguedCount() != null ? ctx.getHomeNationalTeamFatiguedCount() : 0).append("\n");
        sb.append("  * Лазарет/Баны: ").append(formatList(ctx.getHomeMissingList())).append("\n");

        sb.append("- Гости: Защита ср. возраст: ").append(formatAge(ctx.getAwayDefenseAvgAge()))
          .append(" лет | Атака ср. возраст: ").append(formatAge(ctx.getAwayAttackAvgAge())).append(" лет\n");
        sb.append("  * Альфа-Талисман: ").append(ctx.getAwayTalismanName() != null ? ctx.getAwayTalismanName() : "Коллективный билдап")
          .append(" (Доля голов: ").append(formatPct(ctx.getAwayTalismanGoalPct()))
          .append(", Статус: ").append(ctx.getAwayTalismanStatus() != null ? ctx.getAwayTalismanStatus() : "PLAYING").append(")\n");
        sb.append("  * Ведомый Бета: ").append(ctx.getAwayBetaName() != null ? ctx.getAwayBetaName() : "Ротация")
          .append(" (Статус: ").append(ctx.getAwayBetaStatus() != null ? ctx.getAwayBetaStatus() : "PLAYING").append(")\n");
        sb.append("  * Игроки сборных с усталостью (< 72ч отдыха / перелет): ").append(ctx.getAwayNationalTeamFatiguedCount() != null ? ctx.getAwayNationalTeamFatiguedCount() : 0).append("\n");
        sb.append("  * Лазарет/Баны: ").append(formatList(ctx.getAwayMissingList())).append("\n");

        sb.append("- Разрыв скоростей (Pace Mismatch Delta): ")
          .append(ctx.getPaceMismatchDelta() != null ? String.format("%.1f", ctx.getPaceMismatchDelta()) : "0.0")
          .append(" лет (Положительное значение = молодые форварды взрывают темп против возрастной защиты)\n\n");

        sb.append("### 3. ФАКТИЧЕСКИЙ ПРОТОКОЛ МАТЧА (GROUND TRUTH TIMELINE)\n");
        sb.append("- Итоговый счет: ").append(ctx.getFinalScore()).append("\n");
        sb.append("- Динамика таймов: ").append(ctx.getPeriodTimeline() != null ? ctx.getPeriodTimeline() : "Нет данных").append("\n");
        sb.append("- Хронология голов и смена состояний счета:\n");
        if (ctx.getGoalChronology() != null && !ctx.getGoalChronology().isEmpty()) {
            for (String g : ctx.getGoalChronology()) {
                sb.append("  * ").append(g).append("\n");
            }
        } else {
            sb.append("  * Счет матча: ").append(ctx.getFinalScore()).append("\n");
        }

        sb.append("- Дисциплинарная карта и фолы:\n");
        if (ctx.getCardsChronology() != null && !ctx.getCardsChronology().isEmpty()) {
            for (String c : ctx.getCardsChronology()) {
                sb.append("  * ").append(c).append("\n");
            }
        } else {
            sb.append("  * Без аномалий в протоколе\n");
        }

        sb.append("- Замены и тактические сдвиги во 2-м тайме:\n");
        if (ctx.getSubsChronology() != null && !ctx.getSubsChronology().isEmpty()) {
            for (String s : ctx.getSubsChronology()) {
                sb.append("  * ").append(s).append("\n");
            }
        } else {
            sb.append("  * Стандартная ротация 60-75 мин\n");
        }
        sb.append("\n");

        sb.append("### 4. ВОПРОСЫ СУДЕБНОЙ ЭКСПЕРТИЗЫ ДЛЯ САМООБУЧЕНИЯ МОДЕЛИ:\n");
        sb.append("1. Какой переломный фактор сломал пре-матчевую вероятность 80%+? (Ранний гол 0-15'? Усталость сборников? Изоляция Ведомого форварда без Альфы? Взрыв эмоций в дерби во 2-м тайме?)\n");
        sb.append("2. Какую универсальную координату мы обязаны добавить в Java-вектор FootballVectorExtractor, чтобы модель в следующий раз скорректировала вероятность?\n");
        sb.append("3. Напишите математическую формулу этого признака.\n\n");

        sb.append("ВЫВЕДИТЕ ОТВЕТ СТРОГО В ВИДЕ ВАЛИДНОГО JSON-ОБЪЕКТА (без преамбул и markdown-тегов вокруг, только чистый JSON):\n");
        sb.append("{\n");
        sb.append("  \"match_id\": ").append(ctx.getMatchId() != null ? ctx.getMatchId() : 0).append(",\n");
        sb.append("  \"recommendation_id\": ").append(ctx.getRecommendationId() != null ? ctx.getRecommendationId() : 0).append(",\n");
        sb.append("  \"sport_code\": \"FOOTBALL\",\n");
        sb.append("  \"primary_failure_mechanism\": \"EARLY_GOAL_SHOCK | ISOLATED_BETA_COLLAPSE | BIO_CONGESTION_FATIGUE | PACE_MISMATCH_TIRED_DEFENSE | DERBY_EMOTIONAL_SURGE | LOW_BLOCK_FRUSTRATION\",\n");
        sb.append("  \"inflection_minute\": 58,\n");
        sb.append("  \"inflection_description\": \"Точное описание момента сбоя\",\n");
        sb.append("  \"missing_feature_name\": \"имя_координаты_для_вектора\",\n");
        sb.append("  \"missing_feature_formula\": \"математическая формула признака\",\n");
        sb.append("  \"weight_adjustment_recommendation\": \"рекомендация по штрафу вероятности\",\n");
        sb.append("  \"confidence_score\": 90\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public ForensicAuditReport parseAuditReport(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) return null;
        try {
            String clean = jsonResponse.trim();
            if (clean.startsWith("```json")) {
                clean = clean.substring(7);
            }
            if (clean.startsWith("```")) {
                clean = clean.substring(3);
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3);
            }
            return objectMapper.readValue(clean.trim(), ForensicAuditReport.class);
        } catch (Exception e) {
            log.error("Failed to parse football forensic audit report: {}", e.getMessage());
            return null;
        }
    }

    private String formatAge(Double age) {
        return age != null && age > 0 ? String.format("%.1f", age) : "25.0";
    }

    private String formatPct(Double pct) {
        return pct != null && pct > 0 ? String.format("%.0f%%", pct) : "35%";
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) return "Нет критических потерь";
        return String.join(", ", list);
    }
}
