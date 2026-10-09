package pro.datawiki.igaming.source.polymarket.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.polymarket.config.PolymarketConfig;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketEventDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolymarketApiClient {

    private final RestTemplate polymarketRestTemplate;
    private final PolymarketConfig config;
    private final ObjectMapper objectMapper;

    private static final List<String> DISCOVERY_TAGS = List.of(
            "sports",
            "politics",
            "elections",
            "geopolitics",
            "soccer",
            "basketball",
            "nba",
            "nfl",
            "tennis",
            "mma",
            "esports",
            "crypto"
    );

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Origin", "https://polymarket.com");
        headers.set("Referer", "https://polymarket.com/");
        return headers;
    }

    public List<PolymarketEventDto> fetchAllSportsEvents() {
        return fetchAllEvents();
    }

    public List<PolymarketEventDto> fetchAllEvents() {
        List<PolymarketEventDto> allEvents = new ArrayList<>();

        for (String tag : DISCOVERY_TAGS) {
            for (int offset = 0; offset < 300; offset += 100) {
                try {
                    String url = config.getBaseUrl() + "/events?closed=false&limit=100&offset=" + offset + "&tag_slug=" + tag;
                    HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
                    ResponseEntity<String> response = polymarketRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        List<PolymarketEventDto> events = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<List<PolymarketEventDto>>() {}
                        );
                        if (events != null && !events.isEmpty()) {
                            allEvents.addAll(events);
                            if (events.size() < 100) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error fetching Polymarket events for tag {} (offset={}): {}", tag, offset, e.getMessage());
                    break;
                }
            }
        }
        return allEvents;
    }

    public PolymarketEventDto fetchEventById(String eventId) {
        try {
            String url = config.getBaseUrl() + "/events/" + eventId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = polymarketRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), PolymarketEventDto.class);
            }
        } catch (Exception e) {
            log.debug("Error fetching Polymarket event {}: {}", eventId, e.getMessage());
        }
        return null;
    }
}
