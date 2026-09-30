package pro.datawiki.igaming.source.betway.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;

import java.util.List;

/**
 * Strategy interface for OOP Betway market mapping handlers.
 */
public interface BetwayMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     *
     * @param market Betway market DTO
     * @param sportType Normalized sport type
     * @return true if supported
     */
    boolean supports(BetwayMarketDto market, SportType sportType);

    /**
     * Maps market outcomes into normalized OddItem objects.
     *
     * @param market Betway market DTO
     * @param event Betway event DTO
     * @param sportType Normalized sport type
     * @param items Target list of odd items
     */
    void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items);
}
