package pro.datawiki.igaming.source.core.engine.xbet.service;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractBetB2BFamilyApiClient {

    private final XbetFamilyApiErrorTracker errorTracker;
    private final BrowserService browserService;
    private final String liveUrl;
    private final String prematchUrl;
    private final String partnerId;

    protected AbstractBetB2BFamilyApiClient(XbetFamilyApiErrorTracker errorTracker,
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

    public List<String> fetchLines(boolean isLive) {
        if (isLive) {
            String line = fetchLine(true);
            return line != null ? List.of(line) : List.of();
        }

        List<String> lines = new java.util.ArrayList<>();
        String mainLine = fetchLine(false);
        if (mainLine != null) {
            lines.add(mainLine);
        }

        // Fetch all major sports to get complete 3,000+ match coverage
        int feedIndex = (prematchUrl != null && prematchUrl.contains("/LineFeed/")) ? prematchUrl.indexOf("/LineFeed/") : -1;
        String baseUrl = (feedIndex > 0 && prematchUrl != null) ? prematchUrl.substring(0, feedIndex) : (prematchUrl != null ? prematchUrl.split("(?<=://[^/]+)")[0] : null);
        List<Integer> sportsToFetch = (baseUrl != null) ? fetchActiveSportIds(baseUrl, false, true) : List.of();
        if (sportsToFetch.isEmpty()) {
            sportsToFetch = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 32, 37, 38, 40, 41, 44, 48, 66, 74, 84);
        }

        for (int sportId : sportsToFetch) {
            try {
                String sportUrl = (prematchUrl != null && prematchUrl.contains("?"))
                        ? prematchUrl + "&sports=" + sportId
                        : (prematchUrl != null ? prematchUrl + "?sports=" + sportId : null);
                if (sportUrl == null) continue;
                String serviceApiUrl = rewriteUrlIfNeeded(sportUrl, false, true);
                String resp = doFetch(serviceApiUrl, false);
                if (resp != null && !resp.trim().startsWith("<") && !resp.contains("NotAcceptable")) {
                    lines.add(resp);
                }
            } catch (Exception ignored) {}
        }
        return lines;
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
                } else if (response.contains("NotAcceptableException") || response.contains("NotAcceptable") ||
                    response.contains("Fail route") || response.contains("\"statusCode\": 404") ||
                    response.contains("\"statusCode\":404")) {
                    log.warn("Fetch returned error JSON: {}", response);
                } else {
                    log.info("Browser fetch succeeded. Preview (first 100 chars): {}", response.substring(0, Math.min(100, response.length())));
                    return response;
                }
            }
        } catch (Exception e) {
            log.debug("Browser navigate failed for {}, falling back to direct HTTP: {}", strippedUrl, e.getMessage());
        }

        // Direct HTTP GET fallback with proxy
        try {
            log.debug("Attempting direct HTTP fetch via RestTemplate for: {}", strippedUrl);
            org.springframework.web.client.RestTemplate restTemplate = createProxiedRestTemplate();
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            httpHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            httpHeaders.set("Accept", "application/json, text/plain, */*");
            httpHeaders.set("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(httpHeaders);

            org.springframework.http.ResponseEntity<String> responseEntity = restTemplate.exchange(
                    strippedUrl, org.springframework.http.HttpMethod.GET, entity, String.class);

            String body = responseEntity.getBody();
            if (body != null && !body.isEmpty()) {
                if (body.trim().startsWith("<")) {
                    log.warn("Direct HTTP fetch returned HTML (preview: {})", body.substring(0, Math.min(200, body.length())));
                    return null;
                }
                log.info("Direct HTTP fetch succeeded for {}. Length: {} bytes", strippedUrl, body.length());
                return body;
            }
        } catch (Exception ex) {
            log.warn("Direct HTTP fetch failed for {}: {}", strippedUrl, ex.getMessage());
        }
        return null;
    }

    private org.springframework.web.client.RestTemplate createProxiedRestTemplate() {
        String proxyUri = browserService != null && browserService.getProxyManager() != null 
                ? browserService.getProxyManager().getCurrentProxyUri() : null;
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(7000);
        requestFactory.setReadTimeout(15000);
        if (proxyUri != null && !proxyUri.isBlank()) {
            try {
                java.net.URI uri = java.net.URI.create(proxyUri);
                java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 3128));
                requestFactory.setProxy(proxy);
                log.debug("Using proxy {} for direct HTTP RestTemplate fetch", proxyUri);
            } catch (Exception e) {
                log.warn("Failed to set proxy {} for RestTemplate: {}", proxyUri, e.getMessage());
            }
        }
        return new org.springframework.web.client.RestTemplate(requestFactory);
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
        if (url.contains("/LineFeed/Get1xMatchByLeague") || url.contains("/LiveFeed/Get1xMatchByLeague") ||
            url.contains("Get1x2_VZip") || url.contains("Get1x2_Zip")) {
            int feedIndex = url.contains("/LineFeed/") ? url.indexOf("/LineFeed/") : (url.contains("/LiveFeed/") ? url.indexOf("/LiveFeed/") : url.indexOf("/service-api/"));
            String baseUrl = feedIndex > 0 ? url.substring(0, feedIndex) : url.split("(?<=://[^/]+)")[0];
            
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
            
            params.putIfAbsent("lng", "ru");
            if (partnerId != null && !partnerId.isBlank()) {
                params.putIfAbsent("partner", partnerId);
            }
            params.putIfAbsent("virtualSports", "true");
            params.put("count", "10000");
            params.putIfAbsent("mode", "4");
            
            // Explicitly request all major sports to bypass 1xBet / BetB2B default 50-item daily highlight truncation
            if (!params.containsKey("sports") && !params.containsKey("champs")) {
                List<Integer> activeSports = fetchActiveSportIds(baseUrl, isLive, useServiceApi);
                if (activeSports != null && !activeSports.isEmpty()) {
                    String sportsParam = activeSports.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
                    params.put("sports", sportsParam);
                } else {
                    params.put("sports", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100");
                }
            }
            
            StringBuilder queryBuilder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            
            String feedPath = isLive ? "LiveFeed/Get1x2_Zip" : "LineFeed/Get1x2_Zip";
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

    private java.util.List<Integer> fetchActiveSportIds(String baseUrl, boolean isLive, boolean useServiceApi) {
        try {
            String feedPath = isLive ? "LiveFeed/GetSports_Zip" : "LineFeed/GetSports_Zip";
            String sportsUrl = useServiceApi 
                    ? String.format("%s/service-api/%s?lng=ru", baseUrl, feedPath)
                    : String.format("%s/%s?lng=ru", baseUrl, feedPath);
            if (partnerId != null && !partnerId.isBlank()) {
                sportsUrl += "&partner=" + partnerId;
            }
            String response = doFetch(sportsUrl, isLive);
            if (response != null && !response.isEmpty() && !response.trim().startsWith("<")) {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
                com.fasterxml.jackson.databind.JsonNode valueNode = root.get("Value");
                if (valueNode != null && valueNode.isArray()) {
                    java.util.List<Integer> sportIds = new java.util.ArrayList<>();
                    for (com.fasterxml.jackson.databind.JsonNode sNode : valueNode) {
                        if (sNode.has("I")) {
                            sportIds.add(sNode.get("I").asInt());
                        }
                    }
                    if (!sportIds.isEmpty()) {
                        log.info("Discovered {} active sport IDs from {}", sportIds.size(), sportsUrl);
                        return sportIds;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch active sport IDs: {}", e.getMessage());
        }
        return java.util.Collections.emptyList();
    }
}

