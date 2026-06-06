package pro.datawiki.igaming.source.core.engine.xbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.AbstractEventDiscoverer;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;
import pro.datawiki.igaming.source.core.validation.JsonValidationService;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyGame;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class XbetFamilyEventDiscoverer extends AbstractEventDiscoverer<XbetFamilyResponse> {

    public XbetFamilyEventDiscoverer(ObjectMapper objectMapper, JsonValidationService jsonValidationService) {
        super(objectMapper, jsonValidationService);
    }

    public int discover(String json, String bookmakerName, String region, boolean isLive,
                        AbstractApiErrorTracker errorTracker,
                        BiConsumer<String, XbetFamilyGame> eventProcessor) {
        if (json == null || json.isEmpty()) {
            return 0;
        }

        XbetFamilyResponse response;
        try {
            response = deserialize(json, XbetFamilyResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize Xbet Family response", e);
            errorTracker.recordError("Deserialization error");
            return 0;
        }

        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            log.warn("[{}] Xbet response parsed but Value is null/empty (success={}, error={})",
                    bookmakerName, response != null ? response.getSuccess() : "null",
                    response != null ? response.getError() : "null");
            return 0;
        }

        int discoveredCount = 0;
        for (XbetFamilyGame game : response.getValue()) {
            if (game.getId() == null) continue;

            String externalId = String.valueOf(game.getId());
            try {
                eventProcessor.accept(externalId, game);
                discoveredCount++;
            } catch (Exception e) {
                log.error("Error processing Xbet Family event {}: {}", externalId, e.getMessage());
            }
        }

        log.info("[{}/{}] Discovered {} {} events", bookmakerName, region, discoveredCount, isLive ? "live" : "prematch");
        return discoveredCount;
    }

    public Map<Integer, String> extractSportNames(String json) {
        Map<Integer, String> sportNames = new HashMap<>();
        try {
            XbetFamilyResponse response = deserialize(json, XbetFamilyResponse.class);
            if (response != null && response.getValue() != null) {
                for (XbetFamilyGame g : response.getValue()) {
                    if (g.getSportId() != null && g.getSportName() != null) {
                        sportNames.put(g.getSportId(), g.getSportName());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return sportNames;
    }
}
