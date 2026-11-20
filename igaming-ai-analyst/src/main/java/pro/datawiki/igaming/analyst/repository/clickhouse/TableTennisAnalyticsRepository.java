package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TableTennisFactDto;

import java.util.Optional;

public interface TableTennisAnalyticsRepository {

    @Query("""
        SELECT count() as matches,
               avg(if(player1_id = :pid, player1_live_elo, player2_live_elo)) as avg_elo,
               avg(if(player1_id = :pid, player1_winstreak, player2_winstreak)) as streak,
               avg(if(player1_id = :pid, points_margin, -points_margin)) as margin,
               avg(if(player1_id = :pid, score1 > score2, score2 > score1)) as wr,
               countIf((player1_id = :pid AND score1 = 3 AND score2 = 0) OR (player2_id = :pid AND score2 = 3 AND score1 = 0)) / greatest(1, count()) as sweep_rate,
               countIf(total_sets = 5) / greatest(1, count()) as dec_rate,
               avg(same_day_h2h_matches_today) as daily_m,
               avg(if(player1_id = :pid, same_day_h2h_p1_wins_today, same_day_h2h_matches_today - same_day_h2h_p1_wins_today)) as daily_wins
        FROM table_tennis_match_fact
        WHERE player1_id = :pid OR player2_id = :pid
    """)
    Optional<TableTennisFactDto> findStatsByPlayerId(@Param("pid") Long pid);
}
