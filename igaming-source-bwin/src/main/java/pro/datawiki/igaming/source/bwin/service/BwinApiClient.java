package pro.datawiki.igaming.source.bwin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.bwin.config.BwinConfig;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainEventsResponse;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainEventDetailsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class BwinApiClient {

    private final BwinConfig bwinConfig;
    private final ObjectMapper objectMapper;
    private final BrowserService browserService;

    public EntainEventsResponse getEvents() {
        log.info("Navigating to Bwin sports page to intercept events...");
        try {
            String pageUrl = "https://sports.bwin.com/en/sports";
            String json = browserService.navigateAndInterceptResponse(
                    pageUrl, 
                    url -> url.contains("api/asyncdsl/list"), 
                    15000
            );
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, EntainEventsResponse.class);
            }
        } catch (Exception e) {
            log.error("Scraping failed for Bwin: {}", e.getMessage());
        }
        return null;
    }

    public EntainEventDetailsResponse getEventDetails(Long eventId) {
        log.warn("Event details fetching not fully implemented for Playwright yet.");
        return null;
    }
}
