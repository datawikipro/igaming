package pro.datawiki.igaming.source.betano.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.betano.config.BetanoConfig;
import pro.datawiki.igaming.source.betano.dto.BetanoEvent;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetanoApiClient {

    private final RestTemplate betanoRestTemplate;
    private final BetanoConfig betanoConfig;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private BrowserService browserService;

    private final java.util.Map<String, BetanoEvent> recentEventsById = new java.util.concurrent.ConcurrentHashMap<>();

    private static final List<String> PREMATCH_SPORT_PATHS = List.of(
            "/sport/fussball",
            "/sport/fussball/heute-spiele",
            "/sport/tennis",
            "/sport/basketball",
            "/sport/eishockey",
            "/sport/tischtennis",
            "/sport/handball",
            "/sport/volleyball",
            "/sport/esports",
            "/sport/american-football",
            "/sport/baseball",
            "/sport/mma",
            "/sport/darts",
            "/sport/snooker",
            "/sport/rugby-union",
            "/sport/futsal",
            "/sport/badminton"
    );

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9,pt-BR;q=0.8,de;q=0.7");
        headers.set("Origin", "https://www.betano.com");
        headers.set("Referer", "https://www.betano.com/");
        return headers;
    }

    public synchronized List<BetanoEvent> getPrematchEvents() {
        List<BetanoEvent> allEvents = new ArrayList<>();
        if (browserService == null) {
            return allEvents;
        }
        for (String path : PREMATCH_SPORT_PATHS) {
            try {
                List<String> payloads = navigateAndCollectJsonPayloads(betanoConfig.getBaseUrl() + path, 30000);
                for (String payload : payloads) {
                    allEvents.addAll(parseEvents(payload));
                }
                log.info("Betano prematch path {} yielded cumulative {} events", path, allEvents.size());
                if (allEvents.size() >= 2500) {
                    break;
                }
            } catch (Exception e) {
                log.warn("Error collecting Betano prematch path {}: {}", path, e.getMessage());
            }
        }
        return allEvents;
    }

    public synchronized List<BetanoEvent> getLiveEvents() {
        List<BetanoEvent> allEvents = new ArrayList<>();
        if (browserService == null) {
            return allEvents;
        }
        try {
            List<String> payloads = navigateAndCollectJsonPayloads(betanoConfig.getBaseUrl() + "/live", 30000);
            for (String payload : payloads) {
                allEvents.addAll(parseEvents(payload));
            }
            log.info("Betano /live yielded {} events", allEvents.size());
        } catch (Exception be) {
            log.warn("Browser live interception failed: {}", be.getMessage());
        }
        return allEvents;
    }

    private List<String> navigateAndCollectJsonPayloads(String pageUrl, int waitMs) {
        List<String> payloads = new java.util.concurrent.CopyOnWriteArrayList<>();
        com.microsoft.playwright.Page page = null;
        try {
            page = browserService.getSharedContext().newPage();
            final String[] actualOrigin = {null};
            java.util.concurrent.atomic.AtomicInteger respCounter = new java.util.concurrent.atomic.AtomicInteger(0);
            page.onResponse(response -> {
                try {
                    String respUrl = response.url();
                    String ct = response.headerValue("content-type");
                    int count = respCounter.incrementAndGet();
                    if (count <= 15 || respUrl.contains("api") || respUrl.contains("danae") || respUrl.contains("event")) {
                        log.info("Betano HTTP Response #{}: status={}, ct='{}', url='{}'", count, response.status(), ct, respUrl.length() > 120 ? respUrl.substring(0, 120) + "..." : respUrl);
                    }
                    if (ct != null && ct.toLowerCase().contains("json") && response.status() == 200) {
                        if (respUrl.contains("/api/") || respUrl.contains("/danae-webapi/") || respUrl.contains("/bff/") || respUrl.contains("/graphql")) {
                            if (actualOrigin[0] == null && respUrl.startsWith("https://")) {
                                int idx = respUrl.indexOf('/', 8);
                                if (idx > 0) actualOrigin[0] = respUrl.substring(0, idx);
                            }
                            String body = response.text();
                            if (body != null && !body.isBlank()) {
                                payloads.add(body.trim());
                                log.info("Betano collected JSON payload ({} chars) from {}", body.length(), respUrl);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            });

            page.navigate(pageUrl, new com.microsoft.playwright.Page.NavigateOptions()
                    .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(waitMs));
            page.waitForTimeout(4000);

            // Also extract any SSR / initial state or fetch danae-webapi overview directly inside browser context
            try {
                String inBrowserFetch = (String) page.evaluate("""
                    async () => {
                        const out = [];
                        try {
                            if (window.__INITIAL_STATE__) out.push(JSON.stringify(window.__INITIAL_STATE__));
                        } catch (e) {}
                        try {
                            const nextData = document.getElementById('__NEXT_DATA__');
                            if (nextData && nextData.textContent) out.push(nextData.textContent);
                        } catch (e) {}
                        const p = window.location.pathname;
                        const urls = [
                            '/danae-webapi/api/live/overview/latest?includeVirtuals=true&queryLanguageId=3&queryOperatorId=5',
                            '/api/home/top-events-v2/',
                            '/api' + p,
                            '/danae-webapi/api' + p
                        ];
                        for (const u of urls) {
                            try {
                                const r = await fetch(u, { credentials: 'include', headers: { 'Accept': 'application/json' } });
                                if (r.ok) out.push(await r.text());
                            } catch (e) {}
                        }
                        return JSON.stringify(out);
                    }
                """);
                if (inBrowserFetch != null && inBrowserFetch.startsWith("[")) {
                    JsonNode arr = objectMapper.readTree(inBrowserFetch);
                    if (arr.isArray()) {
                        log.info("Betano inBrowserFetch evaluated {} items", arr.size());
                        for (JsonNode item : arr) {
                            if (item.isTextual() && !item.asText().isBlank()) {
                                payloads.add(item.asText());
                            } else if (item.isObject()) {
                                payloads.add(item.toString());
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.warn("Betano page navigation {} error: {}", pageUrl, e.getMessage());
        } finally {
            if (page != null) {
                try { page.close(); } catch (Exception ignored) {}
            }
        }
        return payloads;
    }

    public BetanoEvent getEventDetails(String eventId) {
        if (eventId != null) {
            BetanoEvent cached = recentEventsById.get(eventId);
            if (cached != null) {
                return cached;
            }
        }
        String url = betanoConfig.getBaseUrl() + "/api/events/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = betanoRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                List<BetanoEvent> parsed = parseEvents(response.getBody());
                if (!parsed.isEmpty()) {
                    return parsed.get(0);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Betano: {}", eventId, e.getMessage());
        }
        return null;
    }

    public List<BetanoEvent> parseEvents(String body) {
        List<BetanoEvent> list = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return list;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            java.util.Set<String> seenIds = new java.util.HashSet<>();
            extractEventsRecursively(root, null, null, list, seenIds, 0);
            if (recentEventsById.size() > 50000) {
                recentEventsById.clear();
            }
            for (BetanoEvent ev : list) {
                if (ev.getResolvedId() != null) {
                    recentEventsById.put(ev.getResolvedId(), ev);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Betano events: {}", e.getMessage());
        }
        return list;
    }

    private void extractEventsRecursively(JsonNode node, String inheritedSport, String inheritedLeague,
                                          List<BetanoEvent> out, java.util.Set<String> seenIds, int depth) {
        if (node == null || depth > 14) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                extractEventsRecursively(child, inheritedSport, inheritedLeague, out, seenIds, depth + 1);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }

        String currentSport = inheritedSport;
        if (node.hasNonNull("sportId")) {
            currentSport = node.get("sportId").asText();
        } else if (node.hasNonNull("sportName")) {
            currentSport = node.get("sportName").asText();
        }

        String currentLeague = inheritedLeague;
        if (node.hasNonNull("leagueName")) {
            currentLeague = node.get("leagueName").asText();
        } else if (node.hasNonNull("LeagueDescription")) {
            currentLeague = node.get("LeagueDescription").asText();
        } else if (node.hasNonNull("regionName") && currentLeague == null) {
            currentLeague = node.get("regionName").asText();
        }

        boolean hasId = node.hasNonNull("id") || node.hasNonNull("eventId");
        boolean hasTeamsOrName = node.hasNonNull("shortName") || node.hasNonNull("name")
                || (node.has("participants") && node.get("participants").isArray() && node.get("participants").size() >= 2)
                || (node.has("competitors") && node.get("competitors").isArray() && node.get("competitors").size() >= 2);
        boolean hasEventIndicators = node.has("markets") || node.has("startTime") || node.has("startTimeEpoch")
                || node.has("participants") || node.has("competitors") || node.has("isLive") || node.has("live")
                || node.has("betRadarId") || node.has("url");

        if (hasId && hasTeamsOrName && hasEventIndicators) {
            try {
                BetanoEvent ev = objectMapper.treeToValue(node, BetanoEvent.class);
                if (ev != null && ev.getResolvedId() != null) {
                    String resolvedName = ev.getResolvedName();
                    if (resolvedName != null && (resolvedName.contains("-") || resolvedName.contains("vs") || resolvedName.contains("—") || resolvedName.contains(" v "))) {
                        if (ev.getSportName() == null && currentSport != null) {
                            ev.setSportName(currentSport);
                        }
                        if (ev.getLeagueName() == null && currentLeague != null) {
                            ev.setLeagueName(currentLeague);
                        }
                        if (seenIds.add(ev.getResolvedId())) {
                            out.add(ev);
                        }
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        var fields = node.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            extractEventsRecursively(entry.getValue(), currentSport, currentLeague, out, seenIds, depth + 1);
        }
    }
}
