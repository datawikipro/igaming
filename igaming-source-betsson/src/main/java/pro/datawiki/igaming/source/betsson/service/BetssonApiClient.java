package pro.datawiki.igaming.source.betsson.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.betsson.config.BetssonConfig;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEventDetailsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetssonApiClient {

    private final BetssonConfig betssonConfig;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;

    public KambiEventsResponse getEvents() {
        log.info("Navigating to Betsson page to intercept events...");
        try {
            String apiUrl = betssonConfig.getApi().getBaseUrl() + "/" + betssonConfig.getApi().getBrand() + "/listView/all/all/all/all.json"
                    + "?lang=" + betssonConfig.getApi().getLocale()
                    + "&market=" + betssonConfig.getApi().getMarket();

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
            log.error("Scraping failed for Betsson: {}", e.getMessage());
        }
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        log.warn("Event details fetching not fully implemented for Playwright yet.");
        return null;
    }
}
