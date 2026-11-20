package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Analytical fact record for Table Tennis players from table_tennis_match_fact.
 */
public record TableTennisFactDto(
        int matches,
        double avgElo,
        double streak,
        double margin,
        double wr,
        double sweepRate,
        double decRate,
        double dailyM,
        double dailyWins
) {
    public static TableTennisFactDto empty() {
        return new TableTennisFactDto(0, 1500.0, 0.0, 0.0, 0.5, 0.30, 0.22, 0.0, 0.0);
    }
}
