package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * 15-minute goal distribution facts for Football teams from match_goal_timeline_fact.
 */
public record FootballGoalTimelineFactDto(
        double b1,
        double b2,
        double b3,
        double b4,
        double b5,
        double b6
) {
    public static FootballGoalTimelineFactDto empty() {
        return new FootballGoalTimelineFactDto(0.166, 0.166, 0.166, 0.166, 0.166, 0.166);
    }
}
