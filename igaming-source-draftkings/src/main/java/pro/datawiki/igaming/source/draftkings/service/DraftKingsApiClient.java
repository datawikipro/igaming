package pro.datawiki.igaming.source.draftkings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.draftkings.config.DraftKingsConfig;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftKingsApiClient {

    private final BrowserService browserService;
    private final DraftKingsConfig config;

    private final java.util.Map<Integer, DraftKingsEventGroupResponse> cache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<Integer, Long> cacheTime = new java.util.concurrent.ConcurrentHashMap<>();

    public DraftKingsEventGroupResponse getEventGroup(int eventGroupId) {
        long now = System.currentTimeMillis();
        Long lastFetch = cacheTime.get(eventGroupId);
        if (lastFetch != null && (now - lastFetch) < 10000) {
            log.trace("Serving DraftKings eventgroup {} from cache", eventGroupId);
            return cache.get(eventGroupId);
        }

        String url = config.getApi().getBaseUrl() + "/sites/" + config.getApi().getSiteId() + "/api/v5/eventgroups/" + eventGroupId + "?format=json";
        try {
            log.debug("Fetching DraftKings eventgroup {} from {}", eventGroupId, url);
            // Query using Playwright BrowserService to bypass Cloudflare/Akamai bot detection
            DraftKingsEventGroupResponse response = browserService.apiGet(url, DraftKingsEventGroupResponse.class, 25000, "draftkings");
            if (response != null) {
                cache.put(eventGroupId, response);
                cacheTime.put(eventGroupId, now);
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch DraftKings eventgroup {} via BrowserService: {}", eventGroupId, e.getMessage());
            return null;
        }
    }
}
