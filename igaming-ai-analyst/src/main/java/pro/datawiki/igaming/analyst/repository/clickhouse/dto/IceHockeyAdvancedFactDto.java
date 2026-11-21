package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for Ice Hockey teams from igaming_dds.ice_hockey_team_advanced_v.
 */
public record IceHockeyAdvancedFactDto(
        int totalMatches,
        double avgSf,
        double avgSa,
        double avgGa,
        double avgTotalGoals,
        double trapLowRate,
        double under45Rate,
        double highEventRate,
        double savePct,
        double otRate,
        double shootingPct,
        double corsiForPct,
        double pdoLuckDeviation,
        double pdoIndex,
        double p1Gf,
        double p2Gf,
        double p3Gf
) {
    public static IceHockeyAdvancedFactDto empty() {
        return new IceHockeyAdvancedFactDto(
                0, 30.0, 30.0, 2.7, 5.4, 0.20, 0.45, 0.30, 0.915, 0.24, 0.10, 0.50,
                0.0, 1000.0, 0.8, 1.0, 0.9
        );
    }
}
