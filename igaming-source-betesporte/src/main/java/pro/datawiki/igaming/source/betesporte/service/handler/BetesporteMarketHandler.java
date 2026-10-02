package pro.datawiki.igaming.source.betesporte.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;

import java.util.List;

/**
 * Strategy interface for Betesporte market handlers.
 */
public interface BetesporteMarketHandler {

    /**
     * Checks if this handler supports the given market and sport type.
     */
    boolean supports(BetesporteMarketDto market, SportType sportType);

    /**
     * Maps the outcomes in the market to standardized OddItem instances.
     */
    void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items);
}
