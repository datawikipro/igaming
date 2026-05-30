package pro.datawiki.igaming.source.xbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class XbetApiClient {

    private final XbetApiErrorTracker errorTracker;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.xbet.live-url:https://1xbet.com/LiveFeed/Get1xMatchByLeague?sports=1}")
    private String liveUrl;

    @Value("${app.xbet.prematch-url:https://1xbet.com/LineFeed/Get1xMatchByLeague?sports=1}")
    private String prematchUrl;

    public String fetchLine(boolean isLive) {
        String url = isLive ? liveUrl : prematchUrl;
        log.info("Fetching {} from {}", isLive ? "LIVE" : "PREMATCH", url);
        errorTracker.recordAttempt();

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response != null && !response.isEmpty()) {
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
