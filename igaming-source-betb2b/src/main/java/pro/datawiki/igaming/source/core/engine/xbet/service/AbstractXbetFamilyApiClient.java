package pro.datawiki.igaming.source.core.engine.xbet.service;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractXbetFamilyApiClient {

    private final XbetFamilyApiErrorTracker errorTracker;
    private final BrowserService browserService;
    private final String liveUrl;
    private final String prematchUrl;
    private final String partnerId;

    protected AbstractXbetFamilyApiClient(XbetFamilyApiErrorTracker errorTracker,
                                          BrowserService browserService,
                                          String liveUrl,
                                          String prematchUrl,
                                          String partnerId) {
        this.errorTracker = errorTracker;
        this.browserService = browserService;
        this.liveUrl = liveUrl;
        this.prematchUrl = prematchUrl;
        this.partnerId = partnerId;
    }

    public String fetchLine(boolean isLive) {
        String url = isLive ? liveUrl : prematchUrl;
        errorTracker.recordAttempt();

        // 1. Try with rewritten service-api URL (legacy/standard behavior)
        String serviceApiUrl = rewriteUrlIfNeeded(url, isLive, true);
        log.info("Fetching {} from service-api URL: {}", isLive ? "LIVE" : "PREMATCH", serviceApiUrl);
        String response = doFetch(serviceApiUrl, isLive);
        if (response != null && !response.trim().startsWith("<")) {
            log.info("Successfully fetched data from service-api URL");
            return response;
        }

        // 2. If it returned HTML or failed, try direct LineFeed/Get1x2_VZip URL (without service-api)
        String directUrl = rewriteUrlIfNeeded(url, isLive, false);
        if (directUrl != null && !directUrl.equals(serviceApiUrl)) {
            log.info("Service-api URL failed/blocked. Trying direct URL: {}", directUrl);
            response = doFetch(directUrl, isLive);
            if (response != null && !response.trim().startsWith("<")) {
                log.info("Successfully fetched data from direct URL");
                return response;
            }
        }

        // 3. Fallback to the original URL if both rewrites failed
        if (url != null && !url.equals(serviceApiUrl) && !url.equals(directUrl)) {
            log.info("Both rewrites failed. Trying original configured URL: {}", url);
            response = doFetch(url, isLive);
            if (response != null && !response.trim().startsWith("<")) {
                log.info("Successfully fetched data from original URL");
                return response;
            }
        }

        log.error("All fetch attempts failed for {}", isLive ? "LIVE" : "PREMATCH");
        errorTracker.recordError("All fetch attempts failed (potential geoblock/HTML response)");
        return null;
    }

    private String doFetch(String url, boolean isLive) {
        String strippedUrl = stripCountryParameter(url);
        try {
            Map<String, String> headers = Map.of("Accept", "application/json, text/plain, */*");
            String contextName = isLive ? "xbet-live" : "xbet-prematch";
            String response = browserService.navigateAndGetBody(strippedUrl, 5000, contextName, headers);
            if (response != null && !response.isEmpty()) {
                if (response.trim().startsWith("<")) {
                    log.warn("Fetch returned HTML (preview: {})", response.substring(0, Math.min(200, response.length())));
                    return null;
                }
                if (response.contains("NotAcceptableException") || response.contains("NotAcceptable") ||
                    response.contains("Fail route") || response.contains("\"statusCode\": 404") ||
                    response.contains("\"statusCode\":404")) {
                    log.warn("Fetch returned error JSON: {}", response);
                    return null;
                }
                log.info("Fetch succeeded. Preview (first 100 chars): {}", response.substring(0, Math.min(100, response.length())));
                return response;
            }
        } catch (Exception e) {
            log.warn("Error calling browser navigate for {}: {}", strippedUrl, e.getMessage());
        }
        return null;
    }

    private String stripCountryParameter(String url) {
        if (url == null) return null;
        String stripped = url.replaceAll("(?i)(?<=&&|\\?|&)country=[^&]*&?", "");
        if (stripped.endsWith("?") || stripped.endsWith("&")) {
            stripped = stripped.substring(0, stripped.length() - 1);
        }
        return stripped;
    }

    private String rewriteUrlIfNeeded(String url, boolean isLive, boolean useServiceApi) {
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
            if (partnerId != null && !partnerId.isBlank()) {
                params.putIfAbsent("partner", partnerId);
            }
            params.putIfAbsent("virtualSports", "true");
            
            StringBuilder queryBuilder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            
            String feedPath = isLive ? "LiveFeed/Get1x2_VZip" : "LineFeed/Get1x2_VZip";
            String newUrl;
            if (useServiceApi) {
                newUrl = String.format("%s/service-api/%s?%s", baseUrl, feedPath, queryBuilder.toString());
            } else {
                newUrl = String.format("%s/%s?%s", baseUrl, feedPath, queryBuilder.toString());
            }
            log.info("Rewrote URL from {} to {} (useServiceApi={})", url, newUrl, useServiceApi);
            return newUrl;
        }
        return url;
    }
}
