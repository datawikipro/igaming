package pro.datawiki.igaming.source.betrivers.service;

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
import pro.datawiki.igaming.source.betrivers.config.BetriversConfig;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiBetOfferResponse;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiGroupResponse;

@Service
@Slf4j
public class BetriversApiClient {

    private final RestTemplate restTemplate;
    private final BetriversConfig config;
    private final ObjectMapper objectMapper;

    public BetriversApiClient(@Qualifier("kambiRestTemplate") RestTemplate restTemplate,
                              BetriversConfig config,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Origin", "https://ny.betrivers.com");
        headers.set("Referer", "https://ny.betrivers.com/");
        return headers;
    }

    public pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewResponse getListView(String sportSlug) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "listView", sportSlug + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Kambi listView for '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Kambi listView for '{}': {}", sportSlug, e.getMessage());
        }
        return null;
    }

    public KambiGroupResponse getGroups() {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "group.json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Kambi groups from {}", url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiGroupResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Kambi groups: {}", e.getMessage());
        }
        return null;
    }

    public KambiEventsResponse getEventsByGroup(Long groupId) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "event", "group", String.valueOf(groupId) + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Kambi events for group {} from {}", groupId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiEventsResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Kambi events for group {}: {}", groupId, e.getMessage());
        }
        return null;
    }

    public KambiBetOfferResponse getBetOffers(Long eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(config.getApi().getBrand(), "betoffer", "event", String.valueOf(eventId) + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Kambi betOffers for event {} from {}", eventId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiBetOfferResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Kambi betOffers for event {}: {}", eventId, e.getMessage());
        }
        return null;
    }
}
