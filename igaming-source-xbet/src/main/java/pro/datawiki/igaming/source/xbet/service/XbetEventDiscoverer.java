package pro.datawiki.igaming.source.xbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.AbstractEventDiscoverer;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;
import pro.datawiki.igaming.source.core.validation.JsonValidationService;
import pro.datawiki.igaming.source.xbet.dto.XbetGame;
import pro.datawiki.igaming.source.xbet.dto.XbetResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class XbetEventDiscoverer extends AbstractEventDiscoverer<XbetResponse> {

    public XbetEventDiscoverer(ObjectMapper objectMapper, JsonValidationService jsonValidationService) {
        super(objectMapper, jsonValidationService);
    }

    public int discover(String json, String bookmakerName, String region, boolean isLive,
                        AbstractApiErrorTracker errorTracker,
                        BiConsumer<String, XbetGame> eventProcessor) {
        if (json == null || json.isEmpty()) {
            return 0;
        }

        XbetResponse response;
        try {
            response = deserialize(json, XbetResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize 1xBet Family response", e);
            errorTracker.recordError("Deserialization error");
            return 0;
        }

        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            return 0;
        }

        int discoveredCount = 0;
        for (XbetGame game : response.getValue()) {
            if (game.getId() == null) continue;

            String externalId = String.valueOf(game.getId());
            try {
                eventProcessor.accept(externalId, game);
                discoveredCount++;
            } catch (Exception e) {
                log.error("Error processing 1xBet Family event {}: {}", externalId, e.getMessage());
            }
        }

        log.info("[{}/{}] Discovered {} {} events", bookmakerName, region, discoveredCount, isLive ? "live" : "prematch");
        return discoveredCount;
    }

    public Map<Integer, String> extractSportNames(String json) {
        Map<Integer, String> sportNames = new HashMap<>();
        try {
            XbetResponse response = deserialize(json, XbetResponse.class);
            if (response != null && response.getValue() != null) {
                for (XbetGame g : response.getValue()) {
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
