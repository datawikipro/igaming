package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;

import java.util.List;

public interface SbobetMarketHandler {
    boolean supports(String marketKey);

    default boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    default boolean supports(String marketKey, SbobetMarketContext context) {
        return supports(marketKey, context != null ? context.getSportType() : null);
    }

    void handle(JsonNode marketNode, List<OddItem> items);

    default void handle(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handle(marketNode, items);
    }

    default void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handle(marketNode, sportType, items);
    }

    default void handle(String marketKey, JsonNode marketNode, SbobetMarketContext context, List<OddItem> items) {
        handle(marketKey, marketNode, context != null ? context.getSportType() : null, items);
    }
}
