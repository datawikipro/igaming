package pro.datawiki.igaming.source.fanduel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanDuelApiClient {

    private final BrowserService browserService;
    private final ObjectMapper objectMapper;
    private final FanDuelOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;

    /**
     * Local hash cache to avoid pushing unchanged odds to the aggregator.
     */
    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    /**
     * Navigates to the FanDuel sport/league page and intercepts the event-group API response.
     * <p>
     * FanDuel loads odds via XHR from their CDN:
     * {@code https://sportsbook-us-east-1.fanduel.com/cache/prd/v2_X/US/US-NJ/GBP/en/event-groups/{eventGroupId}.json}
     * We navigate to the user-facing page and intercept that response using Playwright.
     *
     * @param eventGroupId  FanDuel numeric event group ID (e.g. "14098" for NFL)
     * @param sportName     Human-readable sport name sent to the aggregator
     * @param leagueName    League/competition name sent to the aggregator
     * @param navigationUrl Full FanDuel page URL, e.g.:
     *                      {@code https://sportsbook.fanduel.com/sports/american-football/nfl-14098}
     */
    public void processLeague(String eventGroupId, String sportName, String leagueName, String navigationUrl) {
        log.info("Fetching FanDuel league: {} (groupId={}) via {}", leagueName, eventGroupId, navigationUrl);

        try {
            String json = browserService.navigateAndInterceptResponse(
                    navigationUrl,
                    // FanDuel CDN URL pattern: .../event-groups/{id}.json
                    url -> url.contains("event-groups") && url.contains(eventGroupId),
                    20000
            );

            if (json == null || json.isEmpty()) {
                log.warn("No event-groups response captured for FanDuel league: {} (groupId={})", leagueName, eventGroupId);
                return;
            }

            FanDuelEventGroupResponse response = objectMapper.readValue(json, FanDuelEventGroupResponse.class);

            if (response.getAttachments() == null || response.getAttachments().getEvents() == null) {
                log.warn("FanDuel response has no events for league: {} (groupId={})", leagueName, eventGroupId);
                return;
            }

            int pushed = 0;
            int skipped = 0;

            for (OddsUpdateRequest request : response.getAttachments().getEvents().values().stream()
                    .map(event -> oddsMapper.mapToOddsUpdateRequest(event, response, sportName, leagueName))
                    .filter(req -> req != null && req.getOdds() != null && !req.getOdds().isEmpty())
                    .collect(Collectors.toList())) {

                String eventId = request.getExternalEventId();
                String hash = String.valueOf(objectMapper.writeValueAsString(request.getOdds()).hashCode());

                if (hash.equals(localStateHashCache.get(eventId))) {
                    aggregatorClient.reportUnchangedOdds("fanduel", eventId);
                    skipped++;
                } else {
                    aggregatorClient.pushOddsUpdate(request);
                    localStateHashCache.put(eventId, hash);
                    pushed++;
                }
            }

            log.info("FanDuel {}: pushed={}, unchanged={}", leagueName, pushed, skipped);

        } catch (Exception e) {
            log.error("Error processing FanDuel league {} (groupId={}): {}", leagueName, eventGroupId, e.getMessage(), e);
        }
    }
}
