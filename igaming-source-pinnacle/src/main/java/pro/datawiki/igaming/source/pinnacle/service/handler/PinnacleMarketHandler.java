package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;

import java.util.List;

public interface PinnacleMarketHandler {
    boolean supports(String marketType);
    void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items);
}
