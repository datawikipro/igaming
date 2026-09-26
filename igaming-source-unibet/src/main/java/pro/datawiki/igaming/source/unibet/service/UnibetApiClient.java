package pro.datawiki.igaming.source.unibet.service;

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
import pro.datawiki.igaming.source.unibet.config.UnibetConfig;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventsResponse;

@Service
@Slf4j
public class UnibetApiClient {

    private final RestTemplate restTemplate;
    private final UnibetConfig config;
    private final ObjectMapper objectMapper;

    public UnibetApiClient(@Qualifier("unibetRestTemplate") RestTemplate restTemplate,
                           UnibetConfig config,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Encoding", "gzip, deflate");
        headers.set("Origin", "https://www.unibet.com");
        headers.set("Referer", "https://www.unibet.com/");
        return headers;
    }

    public KambiEventsResponse getSportEvents(String sportSlug) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "listView", sportSlug + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Unibet events for sport '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiEventsResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Unibet events for sport '{}': {}", sportSlug, e.getMessage());
        }
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "betoffer", "event", String.valueOf(eventId) + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Unibet betOffers for event {} from {}", eventId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiEventDetailsResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Unibet betOffers for event {}: {}", eventId, e.getMessage());
        }
        return null;
    }
}
