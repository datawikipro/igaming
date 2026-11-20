package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical resilience fact record for Football teams from match_gamestate_resilience_fact.
 */
public record FootballResilienceFactDto(
        int matches,
        double cf,
        double eq,
        double comebackW,
        double ppgLos,
        double col,
        double blownPts,
        double avgDeficit,
        double lateF,
        double lateA
) {
    public static FootballResilienceFactDto empty() {
        return new FootballResilienceFactDto(0, 0.48, 0.32, 0.16, 0.66, 0.18, 0.75, 1.35, 0.34, 0.34);
    }
}
