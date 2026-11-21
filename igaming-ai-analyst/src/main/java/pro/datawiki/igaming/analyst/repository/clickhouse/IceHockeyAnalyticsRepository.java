package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.IceHockeyAdvancedFactDto;

import java.util.Optional;

public interface IceHockeyAnalyticsRepository {

    @Query("""
        SELECT total_matches, avg_sf, avg_sa, avg_ga, avg_total_goals, trap_low_rate,
               under45_rate, high_event_rate, save_pct, ot_rate, shooting_pct, corsi_for_pct, pdo_luck_deviation,
               pdo_index, p1_gf, p2_gf, p3_gf
        FROM igaming_dds.ice_hockey_team_advanced_v
        WHERE team_id = :teamId
    """)
    Optional<IceHockeyAdvancedFactDto> findAdvancedStatsByTeamId(@Param("teamId") Long teamId);
}
