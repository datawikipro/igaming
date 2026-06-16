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
            String pageUrl = "https://sportsbook.betsson.com/";
            String json = browserService.navigateAndInterceptResponse(
                    pageUrl, 
                    url -> url.contains("listView") && url.contains(".json"), 
                    15000
            );
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, KambiEventsResponse.class);
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
