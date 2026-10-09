package pro.datawiki.igaming.source.bcgame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import pro.datawiki.igaming.source.bcgame.config.BcgameConfig;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.ArrayList;
import java.util.Collections;
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
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Origin", "https://bc.game");
        headers.set("Referer", "https://bc.game/sports");
        return headers;
    }

    public List<BcgameSportDto> getSports() {
        try {
            String url = config.getBaseUrl() + "/sports";
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = bcgameRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                if (dataNode.isArray()) {
                    return objectMapper.convertValue(dataNode, new TypeReference<List<BcgameSportDto>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("Failed fetching BC.Game sports from API: {}. Using default sport list.", e.getMessage());
        }

        // Fallback default top sports
        return List.of(
                createSportRef("1", "Soccer"),
                createSportRef("2", "Basketball"),
                createSportRef("3", "Tennis"),
                createSportRef("4", "Ice Hockey"),
                createSportRef("5", "Esports"),
                createSportRef("6", "Baseball"),
                createSportRef("7", "American Football")
        );
    }

    public List<BcgameEventDto> getEvents(String sportId) {
        try {
            String url = config.getBaseUrl() + "/events?sportId=" + (sportId != null ? sportId : "1");
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = bcgameRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                if (dataNode.isArray()) {
                    return objectMapper.convertValue(dataNode, new TypeReference<List<BcgameEventDto>>() {});
                }
            }
        } catch (Exception e) {
            log.debug("Error fetching BC.Game events for sport {}: {}", sportId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public BcgameEventDto getEventDetails(String eventId) {
        try {
            String url = config.getBaseUrl() + "/events/" + eventId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = bcgameRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                return objectMapper.convertValue(dataNode, BcgameEventDto.class);
            }
        } catch (Exception e) {
            log.debug("Error fetching BC.Game event details for {}: {}", eventId, e.getMessage());
        }
        return null;
    }

    private BcgameSportDto createSportRef(String id, String name) {
        BcgameSportDto dto = new BcgameSportDto();
        dto.setId(id);
        dto.setName(name);
        dto.setSlug(name.toLowerCase().replace(" ", "-"));
        return dto;
    }
}
