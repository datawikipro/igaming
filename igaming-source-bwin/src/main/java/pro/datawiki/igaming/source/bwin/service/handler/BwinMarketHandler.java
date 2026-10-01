package pro.datawiki.igaming.source.bwin.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainFixture;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOptionMarket;

import java.util.List;

/**
 * Strategy interface for OOP Bwin market mapping handlers.
 */
public interface BwinMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     *
     * @param market Entain market DTO
     * @param sportType Normalized sport type
     * @return true if supported
     */
    boolean supports(EntainOptionMarket market, SportType sportType);

    /**
     * Maps market outcomes into normalized OddItem objects.
     *
     * @param fixture Entain fixture DTO
     * @param market Entain market DTO
     * @param sportType Normalized sport type
     * @param items Target list of odd items
     */
    void handle(EntainFixture fixture, EntainOptionMarket market, SportType sportType, List<OddItem> items);
}
