package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for CS2 team round statistics from igaming_dds.cs2_team_round_advanced_v.
 */
public record Cs2TeamRoundFactDto(
        int totalRoundsWon,
        double ctWinShare,
        double tWinShare,
        double ecoWinConversion
) {
    public static Cs2TeamRoundFactDto empty() {
        return new Cs2TeamRoundFactDto(0, 0.50, 0.50, 0.08);
    }
}
