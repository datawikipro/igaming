package pro.datawiki.igaming.source.betb2b.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** Optional partner ID for the BetB2B API. Each white-label has its own ID.
     *  Leave empty to omit the parameter from the URL. */
    @Value("${app.betb2b.partner-id:}")
    private String partnerId;

    public String fetchLine(boolean isLive) {
        String url = isLive ? liveUrl : prematchUrl;
        url = rewriteUrlIfNeeded(url, isLive);
        log.info("Fetching {} from {}", isLive ? "LIVE" : "PREMATCH", url);
        errorTracker.recordAttempt();

        try {
            Map<String, String> headers = Map.of("Accept", "application/json, text/plain, */*");
            String response = browserService.navigateAndGetBody(url, 5000, "default", headers);
            if (response != null && !response.isEmpty()) {
                if (response.trim().startsWith("<")) {
                    log.warn("Failed to fetch data, received HTML response instead of JSON (preview: {})",
                            response.substring(0, Math.min(300, response.length())));
                    errorTracker.recordError("HTML response received");
                    return null;
                }
                log.info("API response preview (first 500 chars): {}", response.substring(0, Math.min(500, response.length())));
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

    private String rewriteUrlIfNeeded(String url, boolean isLive) {
        if (url == null) return null;
        if (url.contains("/LineFeed/Get1xMatchByLeague") || url.contains("/LiveFeed/Get1xMatchByLeague")) {
            int feedIndex = url.contains("/LineFeed/") ? url.indexOf("/LineFeed/") : url.indexOf("/LiveFeed/");
            String baseUrl = url.substring(0, feedIndex);
            
            Map<String, String> params = new HashMap<>();
            int queryIndex = url.indexOf("?");
            if (queryIndex != -1) {
                String query = url.substring(queryIndex + 1);
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length >= 2) {
                        params.put(pair[0], pair[1]);
                    } else if (pair.length == 1) {
                        params.put(pair[0], "");
                    }
                }
            }
            
            params.putIfAbsent("lng", "en");
            params.putIfAbsent("mode", "4");
            params.putIfAbsent("country", "168");
            if (partnerId != null && !partnerId.isBlank()) {
                params.putIfAbsent("partner", partnerId);
            }
            params.putIfAbsent("virtualSports", "true");
            params.putIfAbsent("count", "1000");
            
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            String feedType = isLive ? "LiveFeed" : "LineFeed";
            String newUrl = String.format("%s/service-api/%s/Get1x2_VZip?%s", baseUrl, feedType, queryBuilder.toString());
            log.info("Rewrote URL from {} to {}", url, newUrl);
            return newUrl;
        }
        return url;
    }
}
