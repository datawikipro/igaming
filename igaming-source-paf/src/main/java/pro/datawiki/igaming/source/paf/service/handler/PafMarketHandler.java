package pro.datawiki.igaming.source.paf.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

public interface PafMarketHandler {

    /**
     * Determines whether this handler supports the given Kambi bet offer, market name, and sport type.
     */
    boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType);

    /**
     * Handles the entire bet offer. By default, iterates through outcomes and calls {@link #handle}.
     */
    default void handleOffer(KambiEvent event, KambiBetOffer betOffer, String marketName,
                             SportType sportType, List<OddItem> items) {
        if (betOffer != null && betOffer.getOutcomes() != null) {
            for (KambiOutcome outcome : betOffer.getOutcomes()) {
                handle(event, betOffer, outcome, marketName, sportType, items);
            }
        }
    }

    /**
     * Handles a single outcome from a bet offer.
     */
    void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                String marketName, SportType sportType, List<OddItem> items);
}
