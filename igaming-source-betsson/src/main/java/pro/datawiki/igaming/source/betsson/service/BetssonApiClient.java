package pro.datawiki.igaming.source.betsson.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.betsson.config.BetssonConfig;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEventDetailsResponse;

@Service
@Slf4j
public class BetssonApiClient {

    private final BetssonConfig betssonConfig;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;
    private final RestTemplate restTemplate;

    public BetssonApiClient(BetssonConfig betssonConfig,
                            ObjectMapper objectMapper,
                            @Autowired(required = false) BrowserService browserService,
                            @Qualifier("betssonRestTemplate") RestTemplate restTemplate) {
        this.betssonConfig = betssonConfig;
        this.objectMapper = objectMapper;
        this.browserService = browserService;
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Encoding", "gzip, deflate");
        headers.set("Origin", "https://www.betsson.com");
        headers.set("Referer", "https://www.betsson.com/");
        return headers;
    }

    public KambiEventsResponse getEvents() {
        log.info("Fetching Betsson events via Kambi API...");
        try {
            String url = UriComponentsBuilder.fromHttpUrl(betssonConfig.getApi().getBaseUrl())
                    .pathSegment(betssonConfig.getApi().getBrand(), "listView", "all", "all", "all", "all.json")
                    .queryParam("lang", betssonConfig.getApi().getLocale())
                    .queryParam("market", betssonConfig.getApi().getMarket())
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                KambiEventsResponse eventsResponse = objectMapper.readValue(response.getBody(), KambiEventsResponse.class);
                if (eventsResponse != null && eventsResponse.getEvents() != null && !eventsResponse.getEvents().isEmpty()) {
                    log.info("Successfully fetched {} events from Kambi API ({})", eventsResponse.getEvents().size(), url);
                    return eventsResponse;
                }
            }
        } catch (Exception e) {
            log.warn("Direct Kambi API fetch failed for Betsson: {}. Attempting browser interception fallback...", e.getMessage());
        }

        if (browserService != null) {
            try {
                String mainSiteUrl = System.getenv("APP_BROWSER_MAIN_SITE_URL");
                if (mainSiteUrl == null || mainSiteUrl.isEmpty()) {
                    mainSiteUrl = "https://www.betsson.com/";
                }

                log.info("Fetching events by navigating to main site: {} and intercepting: {}", mainSiteUrl, "/listView/");

                String json = browserService.navigateAndInterceptResponse(
                    mainSiteUrl,
                    url -> url.contains("/listView/"),
                    30000
                );

                if (json != null && !json.isEmpty()) {
                    log.info("Successfully intercepted JSON from {}", mainSiteUrl);
                    return objectMapper.readValue(json, KambiEventsResponse.class);
                } else {
                    log.warn("Failed to intercept JSON from {} within 30s", mainSiteUrl);
                }
            } catch (Exception e) {
                log.error("Scraping fallback failed for Betsson: {}", e.getMessage());
            }
        }
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(betssonConfig.getApi().getBaseUrl())
                .pathSegment(betssonConfig.getApi().getBrand(), "betoffer", "event", String.valueOf(eventId) + ".json")
                .queryParam("lang", betssonConfig.getApi().getLocale())
                .queryParam("market", betssonConfig.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Betsson betOffers for event {} from {}", eventId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiEventDetailsResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Betsson betOffers for event {}: {}", eventId, e.getMessage());
        }
        return null;
    }
}
