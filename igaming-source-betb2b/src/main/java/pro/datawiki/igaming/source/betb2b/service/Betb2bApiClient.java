package pro.datawiki.igaming.source.betb2b.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class Betb2bApiClient {

    private final Betb2bApiErrorTracker errorTracker;
    private final BrowserService browserService;

    @Value("${app.betb2b.live-url:https://1xbet.com/LiveFeed/Get1xMatchByLeague?sports=1}")
    private String liveUrl;

    @Value("${app.betb2b.prematch-url:https://1xbet.com/LineFeed/Get1xMatchByLeague?sports=1}")
    private String prematchUrl;

    public String fetchLine(boolean isLive) {
        String url = isLive ? liveUrl : prematchUrl;
        log.info("Fetching {} from {}", isLive ? "LIVE" : "PREMATCH", url);
        errorTracker.recordAttempt();

        try {
            String response = browserService.navigateAndGetBody(url, 5000);
            if (response != null && !response.isEmpty()) {
                if (response.trim().startsWith("<")) {
                    log.warn("Failed to fetch data, received HTML response instead of JSON");
                    errorTracker.recordError("HTML response received");
                    return null;
                }
                return response;
            } else {
                log.warn("Failed to fetch data, empty response");
                errorTracker.recordError("Empty response from API");
            }
        } catch (Exception e) {
            log.error("Error fetching line info", e);
            errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return null;
    }
}
