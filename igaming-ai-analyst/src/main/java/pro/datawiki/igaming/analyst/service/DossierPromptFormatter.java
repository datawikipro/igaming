package pro.datawiki.igaming.analyst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.dossier.DossierPlayerDto;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
import pro.datawiki.igaming.dto.dossier.MatchDossierDto;

import java.util.List;

/**
 * Builds structured match analyst prompt in pure Java directly from MDM Ground Truth dossier.
 * Strictly enforces the Zero-Odds Protocol (no odds in prompts).
 */
@Component
@Slf4j
public class DossierPromptFormatter {

    public String formatPrompt(MatchDossierDto dossier) {
        if (dossier == null) {
            return "MATCH_NOT_FOUND";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("## ⚽ МАТЧ: %s vs %s\n", dossier.getTeam1Name(), dossier.getTeam2Name()));
        sb.append(String.format("- **Вид спорта**: %s\n", dossier.getSportName()));
        sb.append(String.format("- **Турнир / Лига**: %s\n", dossier.getLeagueName()));
        sb.append(String.format("- **Время начала (UTC)**: %s\n", dossier.getStartTimeUtc()));
        sb.append(String.format("- **Match ID**: %s\n", dossier.getMatchId()));
        if (dossier.getSofascoreId() != null) {
            sb.append(String.format("- **Ground Truth SofaScore ID**: %s\n", dossier.getSofascoreId()));
        }

        // Lineup context if available
        if (Boolean.TRUE.equals(dossier.getIsLineupConfirmed())) {
            sb.append("\n### 👥 ПОДТВЕРЖДЁННЫЕ СОСТАВЫ:\n");
            sb.append("- Стартовые составы и скамейка официально подтверждены.\n");
        }

        // Formatted recent match history
        appendRecentMatchesSection(sb, dossier.getTeam1Name(), dossier.getTeam1RecentMatches());
        appendRecentMatchesSection(sb, dossier.getTeam2Name(), dossier.getTeam2RecentMatches());

        // Formatted Lineups, Bench and Injuries
        appendLineupsSection(sb, dossier.getTeam1Name(), dossier.getTeam1Starters(), dossier.getTeam1Bench(), dossier.getTeam1Missing());
        appendLineupsSection(sb, dossier.getTeam2Name(), dossier.getTeam2Starters(), dossier.getTeam2Bench(), dossier.getTeam2Missing());

        // Storyline narrative if present
        if (dossier.getStorylineJson() != null) {
            sb.append(String.format("\n### 📖 КОНТЕКСТ МАТЧА:\n%s\n", dossier.getStorylineJson()));
        }

        return sb.toString();
    }

    public void appendRecentMatchesSection(StringBuilder sb, String teamName, List<DossierRecentMatchDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append(String.format("\n### 📊 ПОСЛЕДНИЕ МАТЧИ: %s (Всего: %d):\n", teamName, list.size()));
        int over25Count = 0;
        int totalGoals = 0;
        int cleanSheets = 0;

        for (DossierRecentMatchDto m : list) {
            String res = m.getResult() != null ? m.getResult() : "WIN";
            int tScore = m.getTeamScore();
            int oScore = m.getOpponentScore();
            boolean cs = m.isCleanSheet();
            int sumGoals = m.getTotalGoals();
            if (m.isOver25()) over25Count++;
            if (cs) cleanSheets++;
            totalGoals += sumGoals;
            sb.append(String.format("  * %s %d:%d (Тотал: %d, Сухой матч: %s)\n", res, tScore, oScore, sumGoals, cs ? "Да" : "Нет"));
        }
        double avgGoals = (double) totalGoals / list.size();
        sb.append(String.format("  -> Итог по тоталам: Средний тотал = %.2f гола/матч, Матчей с ТБ 2.5 = %d/%d (%.0f%%), Сухих матчей = %d/%d\n",
                avgGoals, over25Count, list.size(), (over25Count * 100.0) / list.size(), cleanSheets, list.size()));
    }

    public void appendLineupsSection(StringBuilder sb, String teamName, List<DossierPlayerDto> starters, List<DossierPlayerDto> bench, List<String> missing) {
        if (starters != null && !starters.isEmpty()) {
            sb.append(String.format("\n### 🟢 СТАРТОВЫЙ СОСТАВ: %s (%d игроков):\n", teamName, starters.size()));
            for (DossierPlayerDto p : starters) {
                sb.append(String.format("  * %s (%s, рейтинг: %s)\n",
                        p.getName(), p.getPosition(), p.getRating() != null ? String.format("%.1f", p.getRating()) : "0.0"));
            }
        }
        if (bench != null && !bench.isEmpty()) {
            sb.append(String.format("\n### 🟡 СКАМЕЙКА ЗАПАСНЫХ: %s (%d игроков):\n", teamName, bench.size()));
            for (DossierPlayerDto p : bench) {
                String pos = p.getPosition() != null ? p.getPosition().toUpperCase() : "";
                double rating = p.getRating() != null ? p.getRating() : 0.0;
                sb.append(String.format("  * %s (%s, рейтинг: %.1f)%s\n",
                        p.getName(), pos, rating, p.isImpactSub() ? " ⚡ [Опция усиления атаки / ротации]" : ""));
            }
        }
        if (missing != null && !missing.isEmpty()) {
            sb.append(String.format("\n### 🚨 ТРАВМЫ И ПОТЕРИ: %s (%d игроков):\n", teamName, missing.size()));
            for (String m : missing) {
                sb.append(String.format("  * %s\n", m));
            }
        }
    }
}
