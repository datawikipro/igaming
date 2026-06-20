package pro.datawiki.igaming.source.unibet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.unibet.config.UnibetConfig;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEventDetailsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnibetApiClient {

    private final UnibetConfig unibetConfig;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;

    public KambiEventsResponse getEvents() {
        log.info("Navigating to Unibet page to intercept events...");
        try {
            String mainSiteUrl = System.getenv("APP_BROWSER_MAIN_SITE_URL");
            if (mainSiteUrl == null || mainSiteUrl.isEmpty()) {
                mainSiteUrl = "https://www.unibet.com/";
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
            log.error("Scraping failed for Unibet: {}", e.getMessage());
        }
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        log.warn("Event details fetching not fully implemented for Playwright yet.");
        return null;
    }
}
