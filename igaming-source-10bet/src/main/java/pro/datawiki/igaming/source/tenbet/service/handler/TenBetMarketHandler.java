package pro.datawiki.igaming.source.tenbet.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;

import java.util.List;

/**
 * Strategy interface for OOP 10bet market mapping handlers.
 */
public interface TenBetMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     *
     * @param market 10bet market DTO
     * @param sportType Normalized sport type
     * @return true if supported
     */
    boolean supports(TenBetMarketDto market, SportType sportType);

    /**
     * Maps market outcomes into normalized OddItem objects.
     *
     * @param market 10bet market DTO
     * @param event 10bet event DTO
     * @param sportType Normalized sport type
     * @param items Target list of odd items
     */
    void handle(TenBetMarketDto market, TenBetEventDto event, SportType sportType, List<OddItem> items);
}
