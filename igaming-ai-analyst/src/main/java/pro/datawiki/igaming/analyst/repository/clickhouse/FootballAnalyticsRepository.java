package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.FootballGoalTimelineFactDto;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.FootballResilienceFactDto;

import java.util.Collection;
import java.util.Optional;

public interface FootballAnalyticsRepository {

    @Query("""
        SELECT count() as matches, avg(conceded_first) as cf, avg(equalized_after_trailing) as eq,
               countIf(conceded_first = 1 AND final_result = 'WIN') / greatest(1, countIf(conceded_first = 1)) as comeback_w,
               avg(points_won_from_losing_position) as ppg_los,
               avg(collapsed_after_leading) as col, avg(blown_lead_points) as blown_pts,
               avg(abs(max_deficit)) as avg_deficit,
               avg(goals_scored_75_plus) as late_f, avg(goals_conceded_75_plus) as late_a
        FROM match_gamestate_resilience_fact WHERE team_id IN :teamIds
    """)
    Optional<FootballResilienceFactDto> findResilienceByTeamIds(@Param("teamIds") Collection<Long> teamIds);

    @Query("""
        SELECT countIf(minute <= 15) / greatest(1, count()) as b1,
               countIf(minute > 15 AND minute <= 30) / greatest(1, count()) as b2,
               countIf(minute > 30 AND minute <= 45) / greatest(1, count()) as b3,
               countIf(minute > 45 AND minute <= 60) / greatest(1, count()) as b4,
               countIf(minute > 60 AND minute <= 75) / greatest(1, count()) as b5,
               countIf(minute > 75) / greatest(1, count()) as b6
        FROM match_goal_timeline_fact WHERE team_id IN :teamIds AND incident_type = 'GOAL'
    """)
    Optional<FootballGoalTimelineFactDto> findGoalDistributionByTeamIds(@Param("teamIds") Collection<Long> teamIds);
}
