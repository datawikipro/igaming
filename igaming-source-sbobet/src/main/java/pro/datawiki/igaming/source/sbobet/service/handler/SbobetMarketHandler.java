package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.OddItem;

import java.util.List;

public interface SbobetMarketHandler {
    boolean supports(String marketKey);
    void handle(JsonNode marketNode, List<OddItem> items);
}
