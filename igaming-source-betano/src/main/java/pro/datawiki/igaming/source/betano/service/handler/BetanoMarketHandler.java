package pro.datawiki.igaming.source.betano.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;

import java.util.List;

/**
 * Strategy interface for OOP Betano market mapping handlers.
 */
public interface BetanoMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     *
     * @param market Betano market DTO
     * @param sportType Normalized sport type
     * @return true if supported
     */
    boolean supports(BetanoMarketDto market, SportType sportType);

    /**
     * Maps market outcomes into normalized OddItem objects.
     *
     * @param market Betano market DTO
     * @param event Betano event DTO
     * @param sportType Normalized sport type
     * @param items Target list of odd items
     */
    void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items);
}
