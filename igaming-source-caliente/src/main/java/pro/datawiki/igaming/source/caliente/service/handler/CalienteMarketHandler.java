package pro.datawiki.igaming.source.caliente.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteMarketDto;

import java.util.List;

/**
 * Strategy interface for Caliente market handlers.
 */
public interface CalienteMarketHandler {

    /**
     * Checks if this handler supports the given market and sport type.
     */
    boolean supports(CalienteMarketDto market, SportType sportType);

    /**
     * Maps the outcomes in the market to standardized OddItem instances.
     */
    void handle(CalienteMarketDto market, CalienteEventDto event, SportType sportType, List<OddItem> items);
}
