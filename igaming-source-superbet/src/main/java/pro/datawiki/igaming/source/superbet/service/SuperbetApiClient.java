package pro.datawiki.igaming.source.superbet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.superbet.config.SuperbetConfig;
import pro.datawiki.igaming.source.superbet.dto.SuperbetEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperbetApiClient {

    private final RestTemplate superbetRestTemplate;
    private final SuperbetConfig superbetConfig;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Origin", "https://superbet.pl");
        headers.set("Referer", "https://superbet.pl/");
        return headers;
    }

    public List<SuperbetEvent> getPrematchEvents() {
        List<SuperbetEvent> allEvents = new ArrayList<>();
        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        String startRfc = now.toString();
        String endRfc = now.plus(7, ChronoUnit.DAYS).toString();
        String url = superbetConfig.getBaseUrl() + "/events?startDate=" + startRfc + "&endDate=" + endRfc + "&index=active-prematch";

        try {
            log.info("Fetching Superbet prematch events from: {}", url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = superbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch prematch events from Superbet: {}", e.getMessage());
        }
        return allEvents;
    }

    public List<SuperbetEvent> getLiveEvents() {
        List<SuperbetEvent> allEvents = new ArrayList<>();
        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        String startRfc = now.toString();
        String endRfc = now.plus(1, ChronoUnit.DAYS).toString();
        String url = superbetConfig.getBaseUrl() + "/events?startDate=" + startRfc + "&endDate=" + endRfc + "&index=active-live";

        try {
            log.info("Fetching Superbet live events from: {}", url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = superbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Superbet: {}", e.getMessage());
        }
        return allEvents;
    }

    public SuperbetEvent getEventDetails(Long eventId) {
        String url = superbetConfig.getBaseUrl() + "/events?events=" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = superbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("events")) {
                    JsonNode eventsNode = root.get("events");
                    if (eventsNode.isArray() && !eventsNode.isEmpty()) {
                        return objectMapper.treeToValue(eventsNode.get(0), SuperbetEvent.class);
                    }
                } else if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), SuperbetEvent.class);
                    } else if (data.isObject()) {
                        return objectMapper.treeToValue(data, SuperbetEvent.class);
                    }
                }
                return objectMapper.readValue(body, SuperbetEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Superbet: {}", eventId, e.getMessage());
        }
        return null;
    }

    private List<SuperbetEvent> parseEvents(String body) {
        List<SuperbetEvent> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode dataNode = root.has("events") ? root.get("events") : (root.has("data") ? root.get("data") : root);
            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    SuperbetEvent event = objectMapper.treeToValue(node, SuperbetEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed parsing Superbet event list: {}", e.getMessage());
        }
        return list;
    }
}
