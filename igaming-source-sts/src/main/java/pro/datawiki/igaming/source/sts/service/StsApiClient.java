package pro.datawiki.igaming.source.sts.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import pro.datawiki.igaming.source.sts.config.StsConfig;
import pro.datawiki.igaming.source.sts.dto.StsEvent;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StsApiClient {

    private final RestTemplate stsRestTemplate;
    private final StsConfig stsConfig;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Origin", "https://sts.pl");
        headers.set("Referer", "https://sts.pl/");
        return headers;
    }

    public List<StsEvent> getPrematchEvents() {
        List<StsEvent> allEvents = new ArrayList<>();
        String url = stsConfig.getBaseUrl() + "/prematch/matches";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = stsRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch prematch events from STS: {}", e.getMessage());
        }
        return allEvents;
    }

    public List<StsEvent> getLiveEvents() {
        List<StsEvent> allEvents = new ArrayList<>();
        String url = stsConfig.getBaseUrl() + "/live/matches";

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = stsRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                allEvents.addAll(parseEvents(response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live events from STS: {}", e.getMessage());
        }
        return allEvents;
    }

    public StsEvent getEventDetails(Long eventId) {
        String url = stsConfig.getBaseUrl() + "/matches/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = stsRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                JsonNode root = objectMapper.readTree(body);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    if (data.isArray() && !data.isEmpty()) {
                        return objectMapper.treeToValue(data.get(0), StsEvent.class);
                    } else if (data.isObject()) {
                        return objectMapper.treeToValue(data, StsEvent.class);
                    }
                }
                return objectMapper.readValue(body, StsEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from STS: {}", eventId, e.getMessage());
        }
        return null;
    }

    private List<StsEvent> parseEvents(String body) {
        List<StsEvent> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    StsEvent event = objectMapper.treeToValue(node, StsEvent.class);
                    if (event != null && event.getResolvedId() != null) {
                        list.add(event);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed parsing STS event list: {}", e.getMessage());
        }
        return list;
    }
}
