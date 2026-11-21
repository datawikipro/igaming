package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for Basketball teams from igaming_dds.basketball_team_advanced_v.
 */
public record BasketballAdvancedFactDto(
        int totalMatches,
        double avgPtsFor,
        double avgPtsAgainst,
        double netRatingProxy,
        double efgPctProxy,
        double trueShootingPctProxy,
        double avgQ1,
        double avgQ2,
        double avgQ3,
        double avgQ4,
        double q4ScoringShare,
        double otRate
) {
    public static BasketballAdvancedFactDto empty() {
        return new BasketballAdvancedFactDto(0, 105.0, 105.0, 0.50, 0.52, 0.56, 26.0, 26.0, 26.0, 26.0, 0.25, 0.05);
    }
}
