package pro.datawiki.igaming.source.bet7k.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.bet7k.config.Bet7kConfig;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kResponseDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class Bet7kApiClient {

    private final RestTemplate restTemplate;
    private final Bet7kConfig config;
    private final ObjectMapper objectMapper;

    public Bet7kApiClient(@Qualifier("bet7kRestTemplate") RestTemplate restTemplate,
                          Bet7kConfig config,
                          ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Origin", "https://bet7k.com");
        headers.set("Referer", "https://bet7k.com/");
        return headers;
    }

    public List<Bet7kEventDto> getEventsBySport(String sportSlug) {
        String baseUrl = config.getApiUrl();
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/Events/v2/GetEventsBySport")
                .queryParam("sport", sportSlug)
                .toUriString();

        try {
            log.debug("Fetching Bet7k events for sport '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    return objectMapper.convertValue(root, new TypeReference<List<Bet7kEventDto>>() {});
                } else if (root.isObject()) {
                    if (root.has("events") && root.get("events").isArray()) {
                        return objectMapper.convertValue(root.get("events"), new TypeReference<List<Bet7kEventDto>>() {});
                    } else if (root.has("items") && root.get("items").isArray()) {
                        return objectMapper.convertValue(root.get("items"), new TypeReference<List<Bet7kEventDto>>() {});
                    } else if (root.has("data") && root.get("data").isArray()) {
                        return objectMapper.convertValue(root.get("data"), new TypeReference<List<Bet7kEventDto>>() {});
                    }
                    Bet7kResponseDto dto = objectMapper.treeToValue(root, Bet7kResponseDto.class);
                    if (dto != null && dto.getEvents() != null) {
                        return dto.getEvents();
                    }
                }
            } else {
                log.warn("Bet7k API returned non-2xx status {} for sport '{}'", response.getStatusCode(), sportSlug);
            }
        } catch (Exception e) {
            log.error("Error fetching Bet7k events for sport '{}': {}", sportSlug, e.getMessage());
        }
        return Collections.emptyList();
    }
}
