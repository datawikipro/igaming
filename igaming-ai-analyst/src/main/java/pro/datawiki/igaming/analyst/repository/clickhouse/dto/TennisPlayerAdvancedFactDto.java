package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for Tennis players from igaming_dds.tennis_player_advanced_v.
 */
public record TennisPlayerAdvancedFactDto(
        int totalMatches,
        double fsPct,
        double acesPerMatch,
        double dfsPerMatch,
        double winRate,
        double bpSavedPct,
        double bpConvPct,
        double hardWr,
        double clayWr,
        double holdPctModeled,
        double breakPctModeled,
        double dominanceRatioSigmoid,
        double daysSinceLastMatch,
        int comebackMatches,
        double comebackFsPct,
        int peakRank,
        int currentRank,
        double quarticStature
) {
    public static TennisPlayerAdvancedFactDto empty() {
        return new TennisPlayerAdvancedFactDto(
                0, 62.0, 4.0, 2.0, 0.50, 60.0, 40.0, 0.50, 0.50, 0.80, 0.22, 0.50,
                7.0, 5, 62.0, 1500, 1500, 0.05
        );
    }
}
