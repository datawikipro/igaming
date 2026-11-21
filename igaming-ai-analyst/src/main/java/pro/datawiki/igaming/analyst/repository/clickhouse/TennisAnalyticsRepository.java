package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TennisH2hFactDto;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TennisPlayerAdvancedFactDto;

import java.util.Optional;

public interface TennisAnalyticsRepository {

    @Query("""
        SELECT total_matches, fs_pct, aces_per_match, dfs_per_match, win_rate, bp_saved_pct,
               bp_conv_pct, hard_wr, clay_wr, hold_pct_modeled, break_pct_modeled, dominance_ratio_sigmoid,
               days_since_last_match, comeback_matches, comeback_fs_pct, peak_rank, current_rank, quartic_stature
        FROM igaming_dds.tennis_player_advanced_v
        WHERE player_id = :playerId
    """)
    Optional<TennisPlayerAdvancedFactDto> findPlayerAdvancedStats(@Param("playerId") Long playerId);

    @Query("""
        SELECT count() as h2h_m,
               avg(if(player1_id = :p1, score1 > score2, score2 > score1)) as h2h_wr
        FROM tennis_match_fact
        WHERE (player1_id = :p1 AND player2_id = :p2)
           OR (player1_id = :p2 AND player2_id = :p1)
    """)
    Optional<TennisH2hFactDto> findH2hStats(@Param("p1") Long p1, @Param("p2") Long p2);
}
