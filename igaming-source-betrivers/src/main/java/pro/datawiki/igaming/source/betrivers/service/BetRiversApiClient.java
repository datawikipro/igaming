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
import pro.datawiki.igaming.source.betrivers.config.BetRiversConfig;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventsResponse;

/**
 * HTTP client for BetRivers Kambi API.
 * BetRivers brand: rsiusny, market: US-NY
 * Kambi US endpoint: https://us-offering-api.kambicdn.com/offering/v2018
 */
@Service
@Slf4j
public class BetRiversApiClient {

    private final RestTemplate restTemplate;
    private final BetRiversConfig config;
    private final ObjectMapper objectMapper;

    public BetRiversApiClient(@Qualifier("betRiversRestTemplate") RestTemplate restTemplate,
                              BetRiversConfig config,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch all live and prematch events for a given sport slug.
     * Kambi URL pattern: /offering/v2018/{brand}/listView/all/all/all/all.json?market={market}&lang={locale}
     */
    public KambiEventsResponse getSportEvents(String sportSlug) {
        String brand = config.getApi().getBrand();
        String market = config.getApi().getMarket();
        String locale = config.getApi().getLocale();
        String baseUrl = config.getApi().getBaseUrl();

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/" + brand + "/listView/" + sportSlug + "/all/all/all.json")
                .queryParam("market", market)
                .queryParam("lang", locale)
                .queryParam("includeParticipants", "true")
                .toUriString();

        try {
            log.debug("BetRivers: fetching sport events [{}] from {}", sportSlug, url);
            HttpHeaders headers = buildHeaders();
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (rawResponse.getBody() == null) {
                log.warn("BetRivers: null body for sport {}", sportSlug);
                return null;
            }
            return objectMapper.readValue(rawResponse.getBody(), KambiEventsResponse.class);
        } catch (Exception e) {
            log.error("BetRivers: failed to fetch sport events [{}]: {}", sportSlug, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch detailed bet offers for a specific event ID.
     * Kambi URL pattern: /offering/v2018/{brand}/betoffer/event/{eventId}.json
     */
    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        String brand = config.getApi().getBrand();
        String market = config.getApi().getMarket();
        String locale = config.getApi().getLocale();
        String baseUrl = config.getApi().getBaseUrl();

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/" + brand + "/betoffer/event/" + eventId + ".json")
                .queryParam("market", market)
                .queryParam("lang", locale)
                .toUriString();

        try {
            log.debug("BetRivers: fetching event details for event {}", eventId);
            HttpHeaders headers = buildHeaders();
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (rawResponse.getBody() == null) {
                log.warn("BetRivers: null body for event {}", eventId);
                return null;
            }
            return objectMapper.readValue(rawResponse.getBody(), KambiEventDetailsResponse.class);
        } catch (Exception e) {
            log.error("BetRivers: failed to fetch event details [{}]: {}", eventId, e.getMessage());
            return null;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Referer", "https://www.betrivers.com/");
        headers.set("Origin", "https://www.betrivers.com");
        return headers;
    }
}
