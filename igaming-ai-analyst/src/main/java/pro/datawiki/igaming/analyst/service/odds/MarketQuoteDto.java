package pro.datawiki.igaming.analyst.service.odds;

import java.io.Serializable;

/**
 * Immutable DTO representing a market quote resolved by a MarketOddsResolver.
 * Contains transparency metrics (consensus value, divergence, quorum, sharp anchor status)
 * to prevent arbitrage poisoning from corrupting institutional value calculations.
 */
public record MarketQuoteDto(
        double value,
        String bookmaker,
        double consensusValue,
        double divergencePct,
        int quorumCount,
        boolean isSharpAnchored,
        boolean isOutlierFiltered,
        String resolutionMode
) implements Serializable {

    public static MarketQuoteDto of(
            double value,
            String bookmaker,
            double consensusValue,
            double divergencePct,
            int quorumCount,
            boolean isSharpAnchored,
            boolean isOutlierFiltered,
            String resolutionMode
    ) {
        return new MarketQuoteDto(
                value,
                bookmaker,
                consensusValue,
                divergencePct,
                quorumCount,
                isSharpAnchored,
                isOutlierFiltered,
                resolutionMode
        );
    }
}
