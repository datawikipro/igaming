package pro.datawiki.igaming.source.mozzartbet.service;

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
import pro.datawiki.igaming.source.mozzartbet.config.MozzartConfig;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartEvent;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MozzartApiClient {

    private final RestTemplate mozzartRestTemplate;
    private final MozzartConfig mozzartConfig;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "sr-RS,sr;q=0.9,en;q=0.8");
        headers.set("Origin", "https://www.mozzartbet.com");
        headers.set("Referer", "https://www.mozzartbet.com/");
        return headers;
    }

    public List<MozzartEvent> getPrematchEvents() {
        List<MozzartEvent> allEvents = new ArrayList<>();
        String url = mozzartConfig.getBaseUrl() + "/events/active";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = mozzartRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch prematch events from Mozzart Bet: {}", e.getMessage());
        }
        return allEvents;
    }

    public List<MozzartEvent> getLiveEvents() {
        List<MozzartEvent> allEvents = new ArrayList<>();
        String url = mozzartConfig.getBaseUrl() + "/events/live";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = mozzartRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Mozzart Bet: {}", e.getMessage());
        }
        return allEvents;
    }

    public MozzartEvent getEventDetails(Long eventId) {
        String url = mozzartConfig.getBaseUrl() + "/events/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = mozzartRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), MozzartEvent.class);
                    } else if (data.isObject()) {
                        return objectMapper.treeToValue(data, MozzartEvent.class);
                    }
                }
                return objectMapper.readValue(body, MozzartEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Mozzart Bet: {}", eventId, e.getMessage());
        }
        return null;
    }

    private List<MozzartEvent> parseEvents(String body) {
        List<MozzartEvent> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    MozzartEvent event = objectMapper.treeToValue(node, MozzartEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed parsing Mozzart Bet event list: {}", e.getMessage());
        }
        return list;
    }
}
