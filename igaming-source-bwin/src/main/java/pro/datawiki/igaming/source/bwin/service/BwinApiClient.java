package pro.datawiki.igaming.source.bwin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.bwin.config.BwinConfig;
import pro.datawiki.igaming.source.bwin.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.bwin.dto.kambi.KambiEventDetailsResponse;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class BwinApiClient {

    private final BwinConfig bwinConfig;
    private final ObjectMapper objectMapper;

    public KambiEventsResponse getEvents() {
        AtomicReference<KambiEventsResponse> interceptedResponse = new AtomicReference<>();
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"));
            Page page = context.newPage();

            page.onResponse(response -> {
                String url = response.url();
                if (url.contains("cds-api") && url.contains("fixtures") && url.contains(".json")) {
                    try {
                        String body = response.text();
                        if (body != null && body.length() > 500) {
                            log.info("Intercepted Bwin events JSON from: {}", url);
                            // Deserializing into our placeholder DTO (will adapt as we get real payloads)
                            interceptedResponse.set(objectMapper.readValue(body, KambiEventsResponse.class));
                        }
                    } catch (Exception e) {
                        log.trace("Could not parse intercepted JSON from {}", url, e);
                    }
                }
            });

            log.info("Navigating to Bwin sports page to intercept events...");
            page.navigate("https://sports.bwin.com/en/sports");
            
            // Wait for network requests to settle
            page.waitForTimeout(5000);
            
            browser.close();
        } catch (Exception e) {
            log.error("Playwright scraping failed: {}", e.getMessage());
        }
        
        return interceptedResponse.get();
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        // Placeholder for event-specific interception logic
        log.warn("Event details fetching not fully implemented for Playwright yet.");
        return null;
    }
}
