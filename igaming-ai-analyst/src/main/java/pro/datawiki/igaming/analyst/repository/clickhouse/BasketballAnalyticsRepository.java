package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.BasketballAdvancedFactDto;

import java.util.Collection;
import java.util.Optional;

public interface BasketballAnalyticsRepository {

    @Query("""
        SELECT total_matches, avg_pts_for, avg_pts_against, net_rating_proxy, efg_pct_proxy,
               true_shooting_pct_proxy, avg_q1, avg_q2, avg_q3, avg_q4, q4_scoring_share, ot_rate
        FROM igaming_dds.basketball_team_advanced_v
        WHERE team_id IN :teamIds LIMIT 1
    """)
    Optional<BasketballAdvancedFactDto> findAdvancedStatsByTeamIds(@Param("teamIds") Collection<Long> teamIds);
}
