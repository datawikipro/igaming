package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for CS2 teams from igaming_dds.cs2_team_advanced_v.
 */
public record Cs2TeamAdvancedFactDto(
        int totalMatches,
        double pistolRoundWr,
        double overtimeRate,
        double secondRoundConversion,
        double conversion5v4Rate,
        double lateRoundEconomyStamina
) {
    public static Cs2TeamAdvancedFactDto empty() {
        return new Cs2TeamAdvancedFactDto(0, 0.50, 0.15, 0.50, 0.50, 0.30);
    }
}
