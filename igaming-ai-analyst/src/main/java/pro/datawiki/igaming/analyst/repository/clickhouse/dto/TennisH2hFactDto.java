package pro.datawiki.igaming.analyst.repository.clickhouse.dto;

/**
 * Head-to-head fact record for Tennis players from tennis_match_fact.
 */
public record TennisH2hFactDto(
        int h2hM,
        double h2hWr
) {
    public static TennisH2hFactDto empty() {
        return new TennisH2hFactDto(0, 0.50);
    }
}
