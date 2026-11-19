package pro.datawiki.igaming.analyst.service.odds;

import java.util.Optional;

/**
 * Interface contract for resolving market odds for matches.
 * Decouples Institutional Quantitative Consensus resolution from Arbitrage / Surebet max price resolution.
 */
public interface MarketOddsResolver {

    /**
     * Resolves an executable quote for the specified match and market.
     *
     * @param matchId canonical or vendor match identifier
     * @param targetMarket market code (e.g., "WIN1", "TOTAL_OVER_2.5", "DOUBLE_CHANCE_1X", etc.)
     * @return Optional containing MarketQuoteDto if valid quote meets resolution criteria, or empty
     */
    Optional<MarketQuoteDto> resolveQuote(Long matchId, String targetMarket);
}
