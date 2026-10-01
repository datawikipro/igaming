package pro.datawiki.igaming.source.bet7k.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;

import java.util.List;

/**
 * Strategy interface for Bet7k market handlers.
 */
public interface Bet7kMarketHandler {

    /**
     * Checks if this handler supports the given market and sport type.
     */
    boolean supports(Bet7kMarketDto market, SportType sportType);

    /**
     * Maps the outcomes in the market to standardized OddItem instances.
     */
    void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items);
}
