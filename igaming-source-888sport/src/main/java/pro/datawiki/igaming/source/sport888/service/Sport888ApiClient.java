package pro.datawiki.igaming.source.sport888.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.sport888.config.Sport888Config;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEventDetailsResponse;

@Service
public class Sport888ApiClient {

    private static final Logger log = LoggerFactory.getLogger(Sport888ApiClient.class);

    private final Sport888Config sport888Config;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;

    public Sport888ApiClient(Sport888Config sport888Config, ObjectMapper objectMapper, BrowserService browserService) {
        this.sport888Config = sport888Config;
        this.objectMapper = objectMapper;
        this.browserService = browserService;
    }

    public KambiEventsResponse getEvents() {
        log.info("Navigating to 888sport page to intercept events...");
        try {
            String apiUrl = sport888Config.getApi().getBaseUrl() + "/" + sport888Config.getApi().getBrand() + "/listView/all/all/all/all.json"
                    + "?lang=" + sport888Config.getApi().getLocale()
                    + "&market=" + sport888Config.getApi().getMarket();

            String mainSiteUrl = System.getenv("SPORT888_MAIN_SITE_URL");
            if (mainSiteUrl == null || mainSiteUrl.isEmpty()) {
                String bookmaker = System.getenv().getOrDefault("APP_BOOKMAKER_NAME", "888sport");
                if (bookmaker.equals("rushbet")) {
                    mainSiteUrl = "https://www.rushbet.co/";
                } else {
                    mainSiteUrl = "https://www." + bookmaker + ".com/";
                }
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
            log.error("Scraping failed for 888sport: {}", e.getMessage());
        }
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        log.warn("Event details fetching not fully implemented for Playwright yet.");
        return null;
    }
}
