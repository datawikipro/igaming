package pro.datawiki.igaming.source.smarkets.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import pro.datawiki.igaming.source.smarkets.config.SmarketsConfig;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.util.*;

@Service
@Slf4j
public class SmarketsApiClient {

    private final RestTemplate restTemplate;
    private final SmarketsConfig config;
    private final ObjectMapper objectMapper;

    public SmarketsApiClient(@Qualifier("smarketsRestTemplate") RestTemplate restTemplate,
                             SmarketsConfig config,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Origin", "https://smarkets.com");
        headers.set("Referer", "https://smarkets.com/");
        return headers;
    }

    /**
     * Fetch upcoming events for a given sport type.
     */
    public List<SmarketsEvent> getUpcomingEvents(String sportType, int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .path("/events/")
                    .queryParam("state", "upcoming")
                    .queryParam("type", sportType)
                    .queryParam("limit", limit)
                    .toUriString();

            ResponseEntity<SmarketsEventsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    SmarketsEventsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getEvents();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events for sport {}: {}", sportType, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Fetch live/in-play events for a given sport type.
     */
    public List<SmarketsEvent> getLiveEvents(String sportType, int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .path("/events/")
                    .queryParam("state", "live")
                    .queryParam("type", sportType)
                    .queryParam("limit", limit)
                    .toUriString();

            ResponseEntity<SmarketsEventsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    SmarketsEventsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getEvents();
            }
        } catch (Exception e) {
            log.debug("No live events or error for sport {}: {}", sportType, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Fetch popular event IDs.
     */
    public List<String> getPopularEventIds() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .path("/popular/event_ids/")
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object ids = response.getBody().get("popular_event_ids");
                if (ids instanceof List<?> list) {
                    return list.stream().map(Object::toString).toList();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch popular event IDs: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Fetch markets for a single event.
     */
    public List<SmarketsMarket> getMarketsForEvent(String eventId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .pathSegment("events", eventId, "markets", "")
                    .toUriString();

            ResponseEntity<SmarketsMarketsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    SmarketsMarketsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getMarkets();
            }
        } catch (Exception e) {
            log.debug("Failed to fetch markets for event {}: {}", eventId, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Fetch contracts for a market.
     */
    public List<SmarketsContract> getContractsForMarket(String marketId) {
        return getContractsForMarkets(List.of(marketId));
    }

    /**
     * Fetch contracts for multiple markets in a single request.
     * Endpoint: /markets/{marketIds}/contracts/
     */
    public List<SmarketsContract> getContractsForMarkets(List<String> marketIds) {
        if (marketIds == null || marketIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String joined = String.join(",", marketIds);
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .pathSegment("markets", joined, "contracts", "")
                    .toUriString();

            ResponseEntity<SmarketsContractsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    SmarketsContractsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getContracts();
            }
        } catch (Exception e) {
            log.debug("Failed to fetch contracts for markets {}: {}", marketIds, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Fetch quotes (bids & offers) for multiple markets.
     * Endpoint: /markets/{marketIds}/quotes/
     */
    public Map<String, SmarketsContractQuotes> getQuotesForMarkets(List<String> marketIds) {
        if (marketIds == null || marketIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String joinedMarketIds = String.join(",", marketIds);
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment("markets", joinedMarketIds, "quotes", "")
                .toUriString();

        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(createHeaders()),
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<Map<String, SmarketsContractQuotes>>() {}
                    );
                }
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                log.warn("Smarkets rate limit 429 hit for quotes (attempt {}), backing off 2500ms...", attempt + 1);
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                log.warn("Failed to fetch quotes for markets: {}", e.getMessage());
                break;
            }
        }
        return Collections.emptyMap();
    }
}
