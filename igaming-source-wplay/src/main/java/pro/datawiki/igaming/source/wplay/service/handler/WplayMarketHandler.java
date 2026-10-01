package pro.datawiki.igaming.source.wplay.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;

import java.util.List;

/**
 * Strategy interface for OOP Wplay market mapping handlers.
 */
public interface WplayMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     *
     * @param market Wplay market DTO
     * @param sportType Normalized sport type
     * @return true if supported
     */
    boolean supports(WplayMarketDto market, SportType sportType);

    /**
     * Maps market outcomes into normalized OddItem objects.
     *
     * @param market Wplay market DTO
     * @param event Wplay event DTO
     * @param sportType Normalized sport type
     * @param items Target list of odd items
     */
    void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items);
}
