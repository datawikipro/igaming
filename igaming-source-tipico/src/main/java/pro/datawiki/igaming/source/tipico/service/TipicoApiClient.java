package pro.datawiki.igaming.source.tipico.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.tipico.config.TipicoConfig;
import pro.datawiki.igaming.source.tipico.dto.TipicoEvent;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TipicoApiClient {

    private final RestTemplate tipicoRestTemplate;
    private final TipicoConfig tipicoConfig;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private BrowserService browserService;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Origin", "https://sports.tipico.de");
        headers.set("Referer", "https://sports.tipico.de/");
        return headers;
    }

    public List<TipicoEvent> getPrematchEvents() {
        List<TipicoEvent> allEvents = new ArrayList<>();
        String url = tipicoConfig.getBaseUrl() + "/api/v1/programs/sports/1";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = tipicoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct prematch fetch failed from Tipico: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            tipicoConfig.getBaseUrl() + "/de/fussball",
                            u -> u.contains("/api/v1/") && (u.contains("program") || u.contains("events")),
                            20000
                    );
                    if (intercepted != null && !intercepted.isBlank()) {
                        allEvents.addAll(parseEvents(intercepted));
                    }
                } catch (Exception be) {
                    log.warn("Browser interception fallback also failed: {}", be.getMessage());
                }
            }
        }
        return allEvents;
    }

    public List<TipicoEvent> getLiveEvents() {
        List<TipicoEvent> allEvents = new ArrayList<>();
        String url = tipicoConfig.getBaseUrl() + "/api/v1/events/live";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = tipicoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct live fetch failed from Tipico: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            tipicoConfig.getBaseUrl() + "/de/live",
                            u -> u.contains("/api/v1/") && (u.contains("live") || u.contains("events")),
                            20000
                    );
                    if (intercepted != null && !intercepted.isBlank()) {
                        allEvents.addAll(parseEvents(intercepted));
                    }
                } catch (Exception be) {
                    log.warn("Browser live interception fallback also failed: {}", be.getMessage());
                }
            }
        }
        return allEvents;
    }

    public TipicoEvent getEventDetails(Long eventId) {
        String url = tipicoConfig.getBaseUrl() + "/api/v1/events/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = tipicoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), TipicoEvent.class);
                    } else if (data.isObject()) {
                        return objectMapper.treeToValue(data, TipicoEvent.class);
                    }
                }
                return objectMapper.readValue(body, TipicoEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Tipico: {}", eventId, e.getMessage());
        }
        return null;
    }

    public List<TipicoEvent> parseEvents(String body) {
        List<TipicoEvent> list = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return list;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode eventsNode = root.has("events") ? root.get("events") : (root.has("data") ? root.get("data") : root);
            if (eventsNode.isArray()) {
                for (JsonNode node : eventsNode) {
                    TipicoEvent event = objectMapper.treeToValue(node, TipicoEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            } else if (eventsNode.isObject() && eventsNode.has("id")) {
                TipicoEvent event = objectMapper.treeToValue(eventsNode, TipicoEvent.class);
                if (event != null && event.getResolvedId() != null) {
                    list.add(event);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Tipico events: {}", e.getMessage());
        }
        return list;
    }
}
