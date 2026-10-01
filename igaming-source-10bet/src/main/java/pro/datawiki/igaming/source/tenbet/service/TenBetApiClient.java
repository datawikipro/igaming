package pro.datawiki.igaming.source.tenbet.service;

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
import pro.datawiki.igaming.source.tenbet.config.TenBetConfig;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetResponseDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TenBetApiClient {

    private final RestTemplate restTemplate;
    private final TenBetConfig config;
    private final ObjectMapper objectMapper;

    public TenBetApiClient(@Qualifier("tenBetRestTemplate") RestTemplate restTemplate,
                           TenBetConfig config,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Origin", "https://www.10bet.com");
        headers.set("Referer", "https://www.10bet.com/sports");
        return headers;
    }

    public List<TenBetEventDto> getEventsBySport(String sportSlug) {
        String baseUrl = config.getApiUrl();
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/Events/v2/GetEventsBySport")
                .queryParam("sport", sportSlug)
                .toUriString();

        try {
            log.debug("Fetching 10bet events for sport '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody().trim();
                if (body.startsWith("<")) {
                    log.warn("10bet API returned HTML instead of JSON for sport '{}' (geo-restricted or Cloudflare challenge)", sportSlug);
                    return Collections.emptyList();
                }
                JsonNode root = objectMapper.readTree(body);
                if (root.isArray()) {
                    return objectMapper.convertValue(root, new TypeReference<List<TenBetEventDto>>() {});
                } else if (root.isObject()) {
                    if (root.has("events") && root.get("events").isArray()) {
                        return objectMapper.convertValue(root.get("events"), new TypeReference<List<TenBetEventDto>>() {});
                    } else if (root.has("items") && root.get("items").isArray()) {
                        return objectMapper.convertValue(root.get("items"), new TypeReference<List<TenBetEventDto>>() {});
                    } else if (root.has("data") && root.get("data").isArray()) {
                        return objectMapper.convertValue(root.get("data"), new TypeReference<List<TenBetEventDto>>() {});
                    }
                    TenBetResponseDto dto = objectMapper.treeToValue(root, TenBetResponseDto.class);
                    if (dto != null && dto.getEvents() != null) {
                        return dto.getEvents();
                    }
                }
            } else {
                log.warn("10bet API returned non-2xx status {} for sport '{}'", response.getStatusCode(), sportSlug);
            }
        } catch (Exception e) {
            log.warn("Notice while fetching 10bet events for sport '{}': {}", sportSlug, e.getMessage());
        }
        return Collections.emptyList();
    }
}
