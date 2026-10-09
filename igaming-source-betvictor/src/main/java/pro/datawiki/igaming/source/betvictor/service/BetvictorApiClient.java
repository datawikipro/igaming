package pro.datawiki.igaming.source.betvictor.service;

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
import pro.datawiki.igaming.source.betvictor.config.BetvictorConfig;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorEvent;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetvictorApiClient {

    private final RestTemplate betvictorRestTemplate;
    private final BetvictorConfig betvictorConfig;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private BrowserService browserService;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-GB,en;q=0.9");
        headers.set("Origin", "https://www.betvictor.com");
        headers.set("Referer", "https://www.betvictor.com/");
        return headers;
    }

    public List<BetvictorEvent> getPrematchEvents() {
        List<BetvictorEvent> allEvents = new ArrayList<>();
        String url = betvictorConfig.getBaseUrl() + "/api/v2/sports/1/events";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betvictorRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct prematch fetch failed from Betvictor: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            betvictorConfig.getBaseUrl() + "/en-gb/sports/football",
                            u -> u.contains("/api/v2/") && (u.contains("events") || u.contains("football")),
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

    public List<BetvictorEvent> getLiveEvents() {
        List<BetvictorEvent> allEvents = new ArrayList<>();
        String url = betvictorConfig.getBaseUrl() + "/api/v2/in-play/events";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betvictorRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.debug("Direct live fetch failed from Betvictor: {}. Attempting browser fallback...", e.getMessage());
            if (browserService != null) {
                try {
                    String intercepted = browserService.navigateAndInterceptResponse(
                            betvictorConfig.getBaseUrl() + "/en-gb/in-play",
                            u -> u.contains("/api/v2/") && (u.contains("in-play") || u.contains("events")),
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

    public BetvictorEvent getEventDetails(Long eventId) {
        String url = betvictorConfig.getBaseUrl() + "/api/v2/events/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betvictorRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), BetvictorEvent.class);
                    } else if (data.isObject()) {
                        return objectMapper.treeToValue(data, BetvictorEvent.class);
                    }
                }
                return objectMapper.readValue(body, BetvictorEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Betvictor: {}", eventId, e.getMessage());
        }
        return null;
    }

    public List<BetvictorEvent> parseEvents(String body) {
        List<BetvictorEvent> list = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return list;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode eventsNode = root.has("events") ? root.get("events") : (root.has("data") ? root.get("data") : root);
            if (eventsNode.isArray()) {
                for (JsonNode node : eventsNode) {
                    BetvictorEvent event = objectMapper.treeToValue(node, BetvictorEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            } else if (eventsNode.isObject() && eventsNode.has("id")) {
                BetvictorEvent event = objectMapper.treeToValue(eventsNode, BetvictorEvent.class);
                if (event != null && event.getResolvedId() != null) {
                    list.add(event);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Betvictor events: {}", e.getMessage());
        }
        return list;
    }
}
