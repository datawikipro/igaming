package pro.datawiki.igaming.source.betano.service;

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
import pro.datawiki.igaming.source.betano.config.BetanoConfig;
import pro.datawiki.igaming.source.betano.dto.BetanoEvent;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetanoApiClient {

    private final RestTemplate betanoRestTemplate;
    private final BetanoConfig betanoConfig;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private BrowserService browserService;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9,pt-BR;q=0.8,de;q=0.7");
        headers.set("Origin", "https://www.betano.com");
        headers.set("Referer", "https://www.betano.com/");
        return headers;
    }

    public List<BetanoEvent> getPrematchEvents() {
        List<BetanoEvent> allEvents = new ArrayList<>();
        String url = betanoConfig.getBaseUrl() + "/api/sport/football/matches/";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betanoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct prematch fetch failed from Betano: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            betanoConfig.getBaseUrl() + "/sport/futebol",
                            u -> u.contains("/api/") && (u.contains("matches") || u.contains("football") || u.contains("events")),
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

    public List<BetanoEvent> getLiveEvents() {
        List<BetanoEvent> allEvents = new ArrayList<>();
        String url = betanoConfig.getBaseUrl() + "/api/live/";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betanoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct live fetch failed from Betano: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            betanoConfig.getBaseUrl() + "/live",
                            u -> u.contains("/api/") && (u.contains("live") || u.contains("events")),
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

    public BetanoEvent getEventDetails(String eventId) {
        String url = betanoConfig.getBaseUrl() + "/api/events/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betanoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), BetanoEvent.class);
                    } else if (data.isObject()) {
                        if (data.has("event")) {
                            return objectMapper.treeToValue(data.get("event"), BetanoEvent.class);
                        }
                        return objectMapper.treeToValue(data, BetanoEvent.class);
                    }
                }
                return objectMapper.readValue(body, BetanoEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Betano: {}", eventId, e.getMessage());
        }
        return null;
    }

    public List<BetanoEvent> parseEvents(String body) {
        List<BetanoEvent> list = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return list;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode targetNode = root;
            if (root.has("data")) {
                targetNode = root.get("data");
                if (targetNode.has("events")) {
                    targetNode = targetNode.get("events");
                }
            } else if (root.has("events")) {
                targetNode = root.get("events");
            }

            if (targetNode.isArray()) {
                for (JsonNode node : targetNode) {
                    BetanoEvent event = objectMapper.treeToValue(node, BetanoEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            } else if (targetNode.isObject() && targetNode.has("id")) {
                BetanoEvent event = objectMapper.treeToValue(targetNode, BetanoEvent.class);
                if (event != null && event.getResolvedId() != null) {
                    list.add(event);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Betano events: {}", e.getMessage());
        }
        return list;
    }
}
