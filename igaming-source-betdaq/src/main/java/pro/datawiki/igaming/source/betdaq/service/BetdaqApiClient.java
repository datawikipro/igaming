package pro.datawiki.igaming.source.betdaq.service;

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
import pro.datawiki.igaming.source.betdaq.config.BetdaqConfig;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqEventDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqSportDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetdaqApiClient {

    private final RestTemplate betdaqRestTemplate;
    private final BetdaqConfig config;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Origin", "https://www.betdaq.com");
        headers.set("Referer", "https://www.betdaq.com/");
        return headers;
    }

    public List<BetdaqSportDto> getSports() {
        try {
            String url = config.getBaseUrl() + "/sports";
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betdaqRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode sportsNode = root.has("sports") ? root.get("sports") : (root.isArray() ? root : null);
                if (sportsNode != null && sportsNode.isArray()) {
                    return objectMapper.convertValue(sportsNode, new TypeReference<List<BetdaqSportDto>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("Failed fetching Betdaq sports from API: {}. Using default sports list.", e.getMessage());
        }

        return List.of(
                createSportDto("100003", "Football", "SOCCER"),
                createSportDto("100004", "Horse Racing", "HORSE_RACING"),
                createSportDto("100008", "Tennis", "TENNIS"),
                createSportDto("100006", "Basketball", "BASKETBALL"),
                createSportDto("100005", "Cricket", "CRICKET"),
                createSportDto("100010", "Ice Hockey", "ICE_HOCKEY")
        );
    }

    public List<BetdaqEventDto> getEvents(String sportId) {
        try {
            String url = config.getBaseUrl() + "/events?sportId=" + (sportId != null ? sportId : "100003");
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betdaqRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode eventsNode = root.has("events") ? root.get("events") : (root.isArray() ? root : null);
                if (eventsNode != null && eventsNode.isArray()) {
                    return objectMapper.convertValue(eventsNode, new TypeReference<List<BetdaqEventDto>>() {});
                }
            }
        } catch (Exception e) {
            log.debug("Error fetching Betdaq events for sport {}: {}", sportId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public BetdaqEventDto getEventDetails(String eventId) {
        try {
            String url = config.getBaseUrl() + "/events/" + eventId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betdaqRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), BetdaqEventDto.class);
            }
        } catch (Exception e) {
            log.debug("Error fetching Betdaq event details for {}: {}", eventId, e.getMessage());
        }
        return null;
    }

    private BetdaqSportDto createSportDto(String id, String name, String code) {
        BetdaqSportDto dto = new BetdaqSportDto();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        return dto;
    }
}
