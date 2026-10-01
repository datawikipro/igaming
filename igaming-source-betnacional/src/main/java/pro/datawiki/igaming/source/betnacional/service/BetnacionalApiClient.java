package pro.datawiki.igaming.source.betnacional.service;

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
import pro.datawiki.igaming.source.betnacional.config.BetnacionalConfig;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalResponseDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BetnacionalApiClient {

    private final RestTemplate restTemplate;
    private final BetnacionalConfig config;
    private final ObjectMapper objectMapper;

    public BetnacionalApiClient(@Qualifier("betnacionalRestTemplate") RestTemplate restTemplate,
                                BetnacionalConfig config,
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
        headers.set("Origin", "https://betnacional.com");
        headers.set("Referer", "https://betnacional.com/");
        return headers;
    }

    public List<BetnacionalEventDto> getEventsBySport(String sportSlug) {
        String baseUrl = config.getApiUrl();
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/Events/v2/GetEventsBySport")
                .queryParam("sport", sportSlug)
                .toUriString();

        try {
            log.debug("Fetching Betnacional events for sport '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    return objectMapper.convertValue(root, new TypeReference<List<BetnacionalEventDto>>() {});
                } else if (root.isObject()) {
                    if (root.has("events") && root.get("events").isArray()) {
                        return objectMapper.convertValue(root.get("events"), new TypeReference<List<BetnacionalEventDto>>() {});
                    } else if (root.has("items") && root.get("items").isArray()) {
                        return objectMapper.convertValue(root.get("items"), new TypeReference<List<BetnacionalEventDto>>() {});
                    } else if (root.has("data") && root.get("data").isArray()) {
                        return objectMapper.convertValue(root.get("data"), new TypeReference<List<BetnacionalEventDto>>() {});
                    }
                    BetnacionalResponseDto dto = objectMapper.treeToValue(root, BetnacionalResponseDto.class);
                    if (dto != null && dto.getEvents() != null) {
                        return dto.getEvents();
                    }
                }
            } else {
                log.warn("Betnacional API returned non-2xx status {} for sport '{}'", response.getStatusCode(), sportSlug);
            }
        } catch (Exception e) {
            log.error("Error fetching Betnacional events for sport '{}': {}", sportSlug, e.getMessage());
        }
        return Collections.emptyList();
    }
}
