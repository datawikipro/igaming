package pro.datawiki.igaming.source.betb2b.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.AbstractEventDiscoverer;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;
import pro.datawiki.igaming.source.core.validation.JsonValidationService;
import pro.datawiki.igaming.source.betb2b.dto.Betb2bGame;
import pro.datawiki.igaming.source.betb2b.dto.Betb2bResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class Betb2bEventDiscoverer extends AbstractEventDiscoverer<Betb2bResponse> {

    public Betb2bEventDiscoverer(ObjectMapper objectMapper, JsonValidationService jsonValidationService) {
        super(objectMapper, jsonValidationService);
    }

    public int discover(String json, String bookmakerName, String region, boolean isLive,
                        AbstractApiErrorTracker errorTracker,
                        BiConsumer<String, Betb2bGame> eventProcessor) {
        if (json == null || json.isEmpty()) {
            return 0;
        }

        Betb2bResponse response;
        try {
            response = deserialize(json, Betb2bResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize Betb2b Family response", e);
            errorTracker.recordError("Deserialization error");
            return 0;
        }

        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            log.warn("[{}] Betb2b response parsed but Value is null/empty (success={}, error={})",
                    bookmakerName, response != null ? response.getSuccess() : "null",
                    response != null ? response.getError() : "null");
            return 0;
        }

        int discoveredCount = 0;
        for (Betb2bGame game : response.getValue()) {
            if (game.getId() == null) continue;

            String externalId = String.valueOf(game.getId());
            try {
                eventProcessor.accept(externalId, game);
                discoveredCount++;
            } catch (Exception e) {
                log.error("Error processing Betb2b Family event {}: {}", externalId, e.getMessage());
            }
        }

        log.info("[{}/{}] Discovered {} {} events", bookmakerName, region, discoveredCount, isLive ? "live" : "prematch");
        return discoveredCount;
    }

    public Map<Integer, String> extractSportNames(String json) {
        Map<Integer, String> sportNames = new HashMap<>();
        try {
            Betb2bResponse response = deserialize(json, Betb2bResponse.class);
            if (response != null && response.getValue() != null) {
                for (Betb2bGame g : response.getValue()) {
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
