package pro.datawiki.igaming.source.parimatch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.parimatch.config.ParimatchConfig;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchEvent;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchEventsResponse;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchSport;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParimatchApiClient {

    private final RestTemplate parimatchRestTemplate;
    private final ParimatchConfig parimatchConfig;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9,ru;q=0.8");
        headers.set("Referer", "https://parimatch.com/");
        headers.set("Origin", "https://parimatch.com");
        return headers;
    }

    public List<ParimatchSport> getSports() {
        String url = parimatchConfig.getBaseUrl() + "/sports";
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = parimatchRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<ParimatchSport>>() {});
                } else if (body.startsWith("{")) {
                    ParimatchEventsResponse resp = objectMapper.readValue(body, ParimatchEventsResponse.class);
                    return resp.getSports() != null ? resp.getSports() : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch sports from Parimatch: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ParimatchEvent> getPrematchEvents(String sportId) {
        String url = parimatchConfig.getBaseUrl() + "/prematch/events" + (sportId != null ? "?sportId=" + sportId : "");
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = parimatchRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<ParimatchEvent>>() {});
                } else if (body.startsWith("{")) {
                    ParimatchEventsResponse resp = objectMapper.readValue(body, ParimatchEventsResponse.class);
                    return resp.getEvents() != null ? resp.getEvents() : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch prematch events from Parimatch: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ParimatchEvent> getLiveEvents() {
        String url = parimatchConfig.getBaseUrl() + "/live/events";
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = parimatchRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<ParimatchEvent>>() {});
                } else if (body.startsWith("{")) {
                    ParimatchEventsResponse resp = objectMapper.readValue(body, ParimatchEventsResponse.class);
                    return resp.getEvents() != null ? resp.getEvents() : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Parimatch: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public ParimatchEvent getEventDetails(String eventId) {
        String url = parimatchConfig.getBaseUrl() + "/event/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = parimatchRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                if (body.startsWith("{")) {
                    return objectMapper.readValue(body, ParimatchEvent.class);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Parimatch: {}", eventId, e.getMessage());
        }
        return null;
    }
}
