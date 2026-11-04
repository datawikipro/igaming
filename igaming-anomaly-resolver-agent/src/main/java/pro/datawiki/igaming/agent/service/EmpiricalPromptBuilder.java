package pro.datawiki.igaming.agent.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

@Component
public class EmpiricalPromptBuilder {

    public String buildCollisionAuditPrompt(
            CanonicalMatchHeaderDto m1,
            CanonicalMatchHeaderDto m2,
            String conflictTeam,
            String extraDetails
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EMPIRICAL SCHEDULE COLLISION & MDM ANOMALY AUDIT ===\n\n");
        sb.append("You are the Chief Data Integrity AI Agent for the SmartBet.guru sports platform.\n");
        sb.append("A schedule collision or entity conflict was detected between two active MDM records.\n");
        sb.append("Your task is to EMPIRICALLY investigate whether this is a genuine sporting conflict, or a data-level bug.\n\n");

        sb.append("### CANDIDATE RECORD #1 (ID: ").append(m1.getMatchId()).append(")\n");
        appendMatchDetails(sb, m1);

        sb.append("\n### CANDIDATE RECORD #2 (ID: ").append(m2.getMatchId()).append(")\n");
        appendMatchDetails(sb, m2);

        if (conflictTeam != null && !conflictTeam.isBlank()) {
            sb.append("\nReported Conflict Team / Entity: '").append(conflictTeam).append("'\n");
        }
        if (extraDetails != null && !extraDetails.isBlank()) {
            sb.append("Additional Context: ").append(extraDetails).append("\n");
        }

        sb.append("\n### EMPIRICAL CLASSIFICATION CATEGORIES:\n");
        sb.append("1. DUPLICATE_MDM_MATCH: Both records represent the EXACT SAME physical sporting match from different bookmakers. ");
        sb.append("The start times match (within +/- 30 mins) and team names are phonetic aliases or translations of each other. ");
        sb.append("Action: Winner is the one with more bookmakers or SofaScore ID; loser must be consolidated.\n");

        sb.append("2. OUTRIGHT_OR_STATS_TRASH: One of the records is NOT a real head-to-head match, but a tournament futures/outright bet ");
        sb.append("(e.g. contains 'pobeditel', 'winner', 'itogi', 'chempion', 'places 1-3') or a statistical player prop mistaken for a game.\n");

        sb.append("3. UNMAPPED_TEAM_ALIAS: Teams are the same club but one was created as an unmapped raw name due to transliteration ");
        sb.append("(e.g. 'Хапоэль' vs 'Hapoel', 'Маккаби' vs 'Maccabi').\n");

        sb.append("4. SYNTHETIC_DUEL: One match is an e-sport, cyber-simulation (e.g. FIFA 8 mins, SRL, 2x2, E-League), while the other is real.\n");

        sb.append("5. REAL_SCHEDULE_CONFLICT: Both matches are genuine, distinct sporting events happening at overlapping times for the same team/athlete.\n\n");

        sb.append("### REQUIRED JSON OUTPUT FORMAT:\n");
        sb.append("CRITICAL: DO NOT execute any tools, terminal commands, or external searches. Answer immediately and STRICTLY with a single JSON code block wrapped in ```json ... ``` based on the fixture data provided above:\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"verdictType\": \"DUPLICATE_MDM_MATCH | OUTRIGHT_OR_STATS_TRASH | UNMAPPED_TEAM_ALIAS | SYNTHETIC_DUEL | REAL_SCHEDULE_CONFLICT\",\n");
        sb.append("  \"winnerMatchId\": <id of the real/canonical match to retain>,\n");
        sb.append("  \"loserMatchId\": <id of the duplicate or trash match to cancel>,\n");
        sb.append("  \"canonicalSofaId\": <sofascoreId if known or null>,\n");
        sb.append("  \"teamAliasRaw\": \"<raw team name to link, or null>\",\n");
        sb.append("  \"canonicalTeamId\": <canonical team ID to link to, or null>,\n");
        sb.append("  \"confidence\": 0.95,\n");
        sb.append("  \"rationale\": \"<detailed explanation of why this verdict was reached>\",\n");
        sb.append("  \"recommendedAction\": \"MERGE_DUPLICATE | CANCEL_TRASH | MAP_ALIAS | CONFIRM_CONFLICT\"\n");
        sb.append("}\n");
        sb.append("```\n");

        return sb.toString();
    }

    private void appendMatchDetails(StringBuilder sb, CanonicalMatchHeaderDto m) {
        sb.append(" - Sport: ").append(m.getSportSlug() != null ? m.getSportSlug() : "unknown").append("\n");
        sb.append(" - Tournament: ").append(m.getTournamentName() != null ? m.getTournamentName() : "unknown").append("\n");
        sb.append(" - Home Team: ").append(m.getHomeTeamName()).append(" (ID: ").append(m.getHomeTeamId()).append(")\n");
        sb.append(" - Away Team: ").append(m.getAwayTeamName()).append(" (ID: ").append(m.getAwayTeamId()).append(")\n");
        sb.append(" - Scheduled Start Time Epoch: ").append(m.getStartTimeEpochSec()).append("\n");
        sb.append(" - Status: ").append(m.getStatus()).append("\n");
        sb.append(" - Source Origin: ").append(m.getSourceOrigin()).append("\n");
        sb.append(" - SofaScore ID: ").append(m.getSofascoreId() != null ? m.getSofascoreId() : "NONE").append("\n");
        sb.append(" - Bookmaker URLs Count: ").append(m.getBookmakerUrls() != null ? m.getBookmakerUrls().size() : 0).append("\n");
        if (m.getBookmakerUrls() != null && !m.getBookmakerUrls().isEmpty()) {
            sb.append(" - Bookmakers: ").append(String.join(", ", m.getBookmakerUrls().keySet())).append("\n");
        }
    }
}
