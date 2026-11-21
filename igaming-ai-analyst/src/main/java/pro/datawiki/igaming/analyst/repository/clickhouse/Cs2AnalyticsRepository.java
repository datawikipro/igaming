package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.Cs2TeamAdvancedFactDto;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.Cs2TeamRoundFactDto;

import java.util.Optional;

public interface Cs2AnalyticsRepository {

    @Query("""
        SELECT total_matches, pistol_round_wr, overtime_rate, second_round_conversion,
               conversion_5v4_rate, late_round_economy_stamina
        FROM igaming_dds.cs2_team_advanced_v
        WHERE team_id = :teamId
    """)
    Optional<Cs2TeamAdvancedFactDto> findAdvancedStatsByTeamId(@Param("teamId") Long teamId);

    @Query("""
        SELECT total_rounds_won, ct_win_share, t_win_share, eco_win_conversion
        FROM igaming_dds.cs2_team_round_advanced_v
        WHERE team_id = :teamId
    """)
    Optional<Cs2TeamRoundFactDto> findRoundStatsByTeamId(@Param("teamId") Long teamId);
}
