package pro.datawiki.igaming.source.bcgame.service;

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
import pro.datawiki.igaming.source.bcgame.config.BcgameConfig;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BcgameApiClient {

    private final RestTemplate bcgameRestTemplate;
    private final BcgameConfig config;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        return headers;
    }

    public List<BcgameSportDto> getSports() {
        String url = config.getBaseUrl() + "/v1/sports";
        try {
            log.info("Fetching BC.Game sports from {}", url);
            ResponseEntity<String> response = bcgameRestTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;

                List<BcgameSportDto> sports = new ArrayList<>();
                if (dataNode.isArray()) {
                    for (JsonNode n : dataNode) {
                        BcgameSportDto s = objectMapper.treeToValue(n, BcgameSportDto.class);
                        sports.add(s);
                    }
                }
                if (!sports.isEmpty()) return sports;
            }
        } catch (Exception e) {
            log.warn("Direct sports fetch failed ({}), using standard fallback sports catalog", e.getMessage());
        }

        // Standard BC.Game sports fallback
        return List.of(
                createSportRef("1", "Soccer"),
                createSportRef("2", "Basketball"),
                createSportRef("3", "Tennis"),
                createSportRef("4", "Ice Hockey"),
                createSportRef("5", "CS2"),
                createSportRef("6", "Dota 2"),
                createSportRef("7", "League of Legends"),
                createSportRef("8", "Valorant")
        );
    }

    public List<BcgameEventDto> getEvents(String sportId) {
        String url = config.getBaseUrl() + "/v1/events?sportId=" + sportId;
        try {
            log.info("Fetching BC.Game events for sport {} from {}", sportId, url);
            ResponseEntity<String> response = bcgameRestTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;

                List<BcgameEventDto> events = new ArrayList<>();
                if (dataNode.isArray()) {
                    for (JsonNode n : dataNode) {
                        BcgameEventDto e = objectMapper.treeToValue(n, BcgameEventDto.class);
                        events.add(e);
                    }
                }
                return events;
            }
        } catch (Exception e) {
            log.error("Failed to fetch BC.Game events for sport {}: {}", sportId, e.getMessage());
        }
        return List.of();
    }

    public BcgameEventDto getEventDetails(String eventId) {
        String url = config.getBaseUrl() + "/v1/events/" + eventId;
        try {
            ResponseEntity<String> response = bcgameRestTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                return objectMapper.treeToValue(dataNode, BcgameEventDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch BC.Game event details for event {}: {}", eventId, e.getMessage());
        }
        return null;
    }

    private BcgameSportDto createSportRef(String id, String name) {
        BcgameSportDto dto = new BcgameSportDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
