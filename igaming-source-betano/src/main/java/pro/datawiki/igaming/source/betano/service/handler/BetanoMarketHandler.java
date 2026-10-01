package pro.datawiki.igaming.source.betano.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;

import java.util.List;

public interface BetanoMarketHandler {

    /**
     * Checks if this handler supports the given market criterion.
     */
    boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType);

    /**
     * Processes bet offer outcomes and populates the items list with normalized OddItem instances.
     */
    void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items);
}
