package pro.datawiki.igaming.source.sport888.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEvent;

import java.util.List;

public interface Sport888MarketHandler {

    /**
     * Checks if this handler supports the given market criterion.
     */
    boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType);

    /**
     * Processes bet offer outcomes and populates the items list with normalized OddItem instances.
     */
    void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items);
}
