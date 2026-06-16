package pro.datawiki.igaming.source.sport888.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.sport888.config.Sport888Config;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEventDetailsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class Sport888ApiClient {

    private final Sport888Config sport888Config;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;

    public KambiEventsResponse getEvents() {
        log.info("Navigating to 888sport page to intercept events...");
        try {
            String url = sport888Config.getApi().getBaseUrl() + "/" + sport888Config.getApi().getBrand() + "/listView/all/all/all/all.json"
                    + "?lang=" + sport888Config.getApi().getLocale()
                    + "&market=" + sport888Config.getApi().getMarket();
            log.info("Fetching 888sport events directly from Kambi API: {}", url);
            String json = browserService.navigateAndGetBody(url, 15000);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, KambiEventsResponse.class);
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
