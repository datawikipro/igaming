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
import java.util.Locale;

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
            "/",
            "/sport/fussball/",
            "/sport/basketball/",
            "/sport/tennis/",
            "/sport/eishockey/"
    );

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9,de;q=0.8");
        headers.set("Origin", betanoConfig.getBaseUrl());
        headers.set("Referer", betanoConfig.getBaseUrl() + "/");
        return headers;
    }

    public synchronized List<BetanoEvent> getAllEvents() {
        List<BetanoEvent> allEvents = new ArrayList<>();
        if (browserService == null) {
            return fetchPrematchViaRest();
        }

        List<String> capturedPayloads = new java.util.concurrent.CopyOnWriteArrayList<>();
        com.microsoft.playwright.BrowserContext context = null;
        com.microsoft.playwright.Page page = null;
        try {
            context = browserService.getSharedContext();
            try {
                context.clearCookies();
            } catch (Exception ignored) {}

            page = context.newPage();
            page.onConsoleMessage(msg -> log.info("Betano console: {}", msg.text()));

            page.route("**/*", route -> {
                try {
                    String rt = route.request().resourceType();
                    if ("image".equals(rt) || "media".equals(rt)) {
                        route.abort();
                    } else {
                        route.resume();
                    }
                } catch (Exception ignored) {
                    route.resume();
                }
            });

            page.onResponse(response -> {
                try {
                    String respUrl = response.url();
                    if (response.status() == 200 && (respUrl.contains("danae-webapi") || respUrl.contains("top-events") || respUrl.contains("/api/sport/"))) {
                        byte[] bodyBytes = response.body();
                        if (bodyBytes != null && bodyBytes.length > 500) {
                            capturedPayloads.add(new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                } catch (Exception ignored) {}
            });

            String liveUrl = betanoConfig.getBaseUrl() + "/live/";
            page.navigate(liveUrl, new com.microsoft.playwright.Page.NavigateOptions()
                    .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.COMMIT)
                    .setTimeout(30000));
            page.waitForTimeout(4000);

            String currentUrl = page.url();
            log.info("Betano page arrived at: {}", currentUrl);
            if (currentUrl != null && (currentUrl.contains("landingpages") || currentUrl.contains("splash"))) {
                log.warn("Betano redirected to splash page: {}, retrying navigation to /live/...", currentUrl);
                page.navigate(liveUrl, new com.microsoft.playwright.Page.NavigateOptions()
                        .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.COMMIT)
                        .setTimeout(30000));
                page.waitForTimeout(4000);
            }

            String evalResult = (String) page.evaluate("""
                async () => {
                    const out = [];
                    console.log('Starting Betano in-page extraction from: ' + window.location.href);

                    // 1. Danae live overview
                    try {
                        const lr = await fetch('/danae-webapi/api/live/overview/latest?includeVirtuals=true&queryLanguageId=3&queryOperatorId=5');
                        console.log('Danae live overview status: ' + lr.status);
                        if (lr.status === 200) {
                            const text = await lr.text();
                            console.log('Danae live preview: ' + text.substring(0, 300));
                            out.push(text);
                        }
                    } catch (e) {
                        console.log('Danae live fetch error: ' + e);
                    }

                    // Test sport date filters
                    for (const ep of ['/api/sport/fussball/heute/', '/api/sport/fussball/spielplan/', '/api/sport/fussball/upcoming/', '/api/sport/fussball/morgen/']) {
                        try {
                            const tr = await fetch(ep);
                            console.log('Endpoint test ' + ep + ' status: ' + tr.status);
                            if (tr.status === 200) {
                                const txt = await tr.text();
                                console.log('Endpoint ' + ep + ' len: ' + txt.length + ' preview: ' + txt.substring(0, 150));
                                out.push(txt);
                            }
                        } catch (e) {}
                    }

                    // 2. Home top events
                    try {
                        const tr = await fetch('/api/home/top-events-v2/');
                        console.log('Home top events status: ' + tr.status);
                        if (tr.status === 200) {
                            out.push(await tr.text());
                        }
                    } catch (e) {
                        console.log('Home top events fetch error: ' + e);
                    }

                    // 3. Prematch top sports & leagues
                    const sports = [
                        'fussball', 'basketball', 'tennis', 'eishockey', 'handball',
                        'volleyball', 'tischtennis', 'baseball', 'darts', 'mma',
                        'futsal', 'badminton', 'snooker', 'esports', 'rugby-union'
                    ];

                    const sportJsons = await Promise.all(sports.map(async (sp) => {
                        try {
                            const r = await fetch('/api/sport/' + sp + '/');
                            if (r.status === 200) {
                                return await r.json();
                            }
                        } catch (e) {}
                        return null;
                    }));

                    const leagueUrls = new Set();
                    for (const sjson of sportJsons) {
                        if (!sjson || !sjson.data) continue;
                        out.push(JSON.stringify(sjson));

                        if (Array.isArray(sjson.data.topLeagues)) {
                            for (const tl of sjson.data.topLeagues) {
                                if (tl && tl.url) leagueUrls.add(tl.url);
                            }
                        }

                        if (Array.isArray(sjson.data.regionGroups)) {
                            for (const rg of sjson.data.regionGroups) {
                                if (Array.isArray(rg.regions)) {
                                    for (const reg of rg.regions) {
                                        if (Array.isArray(reg.leagues)) {
                                            for (const lg of reg.leagues) {
                                                if (lg && lg.url) leagueUrls.add(lg.url);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    const leagueList = Array.from(leagueUrls).slice(0, 30);
                    console.log('Discovered ' + leagueList.length + ' top leagues to fetch (from total ' + leagueUrls.size + ' available)');
                    const batchSize = 2;
                    for (let i = 0; i < leagueList.length; i += batchSize) {
                        const batch = leagueList.slice(i, i + batchSize);
                        await Promise.all(batch.map(async (url) => {
                            try {
                                const lr = await fetch('/api' + url);
                                if (lr.status === 200) {
                                    out.push(await lr.text());
                                }
                            } catch (err) {}
                        }));
                        await new Promise(r => setTimeout(r, 300));
                    }

                    console.log('Extraction complete, total payloads collected: ' + out.length);
                    return JSON.stringify(out);
                }
            """);

            if (evalResult != null && evalResult.startsWith("[")) {
                JsonNode arr = objectMapper.readTree(evalResult);
                if (arr.isArray()) {
                    log.info("Betano browser session evaluated {} payload items", arr.size());
                    for (JsonNode item : arr) {
                        if (item.isTextual() && !item.asText().isBlank()) {
                            capturedPayloads.add(item.asText());
                        } else if (item.isObject()) {
                            capturedPayloads.add(item.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error in Betano getAllEvents browser session: {}", e.getMessage());
        } finally {
            if (page != null) {
                try { page.close(); } catch (Exception ignored) {}
            }
        }

        int pIdx = 0;
        for (String payload : capturedPayloads) {
            List<BetanoEvent> parsed = parseEvents(payload);
            allEvents.addAll(parsed);
            if (!parsed.isEmpty() || pIdx < 5) {
                String preview = payload.length() > 200 ? payload.substring(0, 200).replace("\n", " ") : payload;
                log.info("Payload #{} yielded {} events (len: {}, preview: {})", pIdx, parsed.size(), payload.length(), preview);
            }
            pIdx++;
        }

        java.util.Map<String, BetanoEvent> deduplicated = new java.util.LinkedHashMap<>();
        for (BetanoEvent ev : allEvents) {
            if (ev != null && ev.getResolvedId() != null) {
                deduplicated.put(ev.getResolvedId(), ev);
                recentEventsById.put(ev.getResolvedId(), ev);
            }
        }
        List<BetanoEvent> result = new ArrayList<>(deduplicated.values());
        log.info("Betano single session discovery completed: {} unique events retrieved", result.size());
        return result;
    }

    public synchronized List<BetanoEvent> getPrematchEvents() {
        return getAllEvents().stream()
                .filter(ev -> !Boolean.TRUE.equals(ev.getIsLive()))
                .toList();
    }

    private List<String> fetchPrematchPayloadsViaBrowser() {
        return List.of();
    }

    public List<BetanoEvent> fetchPrematchViaRest() {
        List<BetanoEvent> events = new ArrayList<>();
        List<String> sports = List.of(
                "fussball", "basketball", "tennis", "eishockey", "handball",
                "volleyball", "tischtennis", "baseball", "darts", "mma",
                "futsal", "badminton", "snooker", "esports", "rugby-union"
        );

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        for (String sport : sports) {
            try {
                String sportUrl = betanoConfig.getBaseUrl() + "/api/sport/" + sport + "/";
                ResponseEntity<String> resp = betanoRestTemplate.exchange(sportUrl, HttpMethod.GET, entity, String.class);
                if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                    continue;
                }

                JsonNode root = objectMapper.readTree(resp.getBody());
                JsonNode dataNode = root.get("data");
                if (dataNode == null) continue;

                List<String> leagueUrls = new ArrayList<>();
                if (dataNode.has("topLeagues") && dataNode.get("topLeagues").isArray()) {
                    for (JsonNode tl : dataNode.get("topLeagues")) {
                        if (tl.hasNonNull("url")) {
                            String u = tl.get("url").asText();
                            if (!leagueUrls.contains(u)) leagueUrls.add(u);
                        }
                    }
                }
                if (dataNode.has("regionGroups") && dataNode.get("regionGroups").isArray()) {
                    for (JsonNode rg : dataNode.get("regionGroups")) {
                        if (rg.has("regions") && rg.get("regions").isArray()) {
                            for (JsonNode reg : rg.get("regions")) {
                                if (reg.has("leagues") && reg.get("leagues").isArray()) {
                                    for (JsonNode lg : reg.get("leagues")) {
                                        if (lg.hasNonNull("url")) {
                                            String u = lg.get("url").asText();
                                            if (!leagueUrls.contains(u)) leagueUrls.add(u);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                int fetched = 0;
                for (String leagueUrl : leagueUrls) {
                    if (fetched >= 10 || events.size() >= 3000) break;
                    try {
                        String fullLeagueUrl = betanoConfig.getBaseUrl() + "/api" + leagueUrl;
                        ResponseEntity<String> lResp = betanoRestTemplate.exchange(fullLeagueUrl, HttpMethod.GET, entity, String.class);
                        if (lResp.getStatusCode().is2xxSuccessful() && lResp.getBody() != null) {
                            JsonNode lRoot = objectMapper.readTree(lResp.getBody());
                            JsonNode lData = lRoot.get("data");
                            if (lData != null && lData.has("blocks") && lData.get("blocks").isArray()) {
                                for (JsonNode block : lData.get("blocks")) {
                                    if (block.has("events") && block.get("events").isArray()) {
                                        for (JsonNode evNode : block.get("events")) {
                                            try {
                                                BetanoEvent ev = objectMapper.treeToValue(evNode, BetanoEvent.class);
                                                if (ev != null && ev.getResolvedId() != null) {
                                                    if (ev.getResolvedTeams() != null && ev.getResolvedTeams().length >= 2) {
                                                        events.add(ev);
                                                        recentEventsById.put(ev.getResolvedId(), ev);
                                                    }
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }
                        }
                        fetched++;
                        try { Thread.sleep(150); } catch (InterruptedException ignored) {}
                    } catch (Exception e) {
                        log.debug("Failed fetching Betano league {}: {}", leagueUrl, e.getMessage());
                    }
                }
                log.info("Betano REST sport {} yielded {} cumulative prematch events", sport, events.size());
            } catch (Exception e) {
                log.warn("Betano REST sport {} failed: {}", sport, e.getMessage());
            }
        }
        return events;
    }

    public synchronized List<BetanoEvent> getLiveEvents() {
        return getAllEvents().stream()
                .filter(ev -> Boolean.TRUE.equals(ev.getIsLive()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<BetanoEvent> parseDanaeLiveOverview(String body) {
        List<BetanoEvent> list = new ArrayList<>();
        if (body == null || body.isBlank()) return list;
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode dataNode = (root.has("data") && root.get("data").isObject()) ? root.get("data") : root;
            if (root.has("liveOverview") && root.get("liveOverview").isObject()) {
                dataNode = root.get("liveOverview");
            }

            JsonNode eventsNode = dataNode.has("events") ? dataNode.get("events") : root.get("events");
            JsonNode marketsNode = dataNode.has("markets") ? dataNode.get("markets") : root.get("markets");
            JsonNode selectionsNode = dataNode.has("selections") ? dataNode.get("selections") : root.get("selections");
            JsonNode leaguesNode = dataNode.has("leagues") ? dataNode.get("leagues") : root.get("leagues");
            JsonNode sportsNode = dataNode.has("sports") ? dataNode.get("sports") : root.get("sports");
            JsonNode globalParticipantsNode = dataNode.has("participants") ? dataNode.get("participants") : root.get("participants");

            // Unpack Redux-style byId dictionaries
            if (eventsNode != null && eventsNode.has("byId")) eventsNode = eventsNode.get("byId");
            if (marketsNode != null && marketsNode.has("byId")) marketsNode = marketsNode.get("byId");
            if (selectionsNode != null && selectionsNode.has("byId")) selectionsNode = selectionsNode.get("byId");
            if (leaguesNode != null && leaguesNode.has("byId")) leaguesNode = leaguesNode.get("byId");
            if (sportsNode != null && sportsNode.has("byId")) sportsNode = sportsNode.get("byId");
            if (globalParticipantsNode != null && globalParticipantsNode.has("byId")) globalParticipantsNode = globalParticipantsNode.get("byId");

            List<JsonNode> evNodeList = new ArrayList<>();
            if (eventsNode != null) {
                if (eventsNode.isObject()) {
                    eventsNode.elements().forEachRemaining(evNodeList::add);
                } else if (eventsNode.isArray()) {
                    eventsNode.forEach(evNodeList::add);
                }
            }

            for (JsonNode evNode : evNodeList) {
                String eid = evNode.hasNonNull("id") ? evNode.get("id").asText() :
                            (evNode.hasNonNull("eventId") ? evNode.get("eventId").asText() : null);
                String name = evNode.hasNonNull("name") ? evNode.get("name").asText() :
                            (evNode.hasNonNull("shortName") ? evNode.get("shortName").asText() : null);
                String sportId = evNode.hasNonNull("sportId") ? evNode.get("sportId").asText() : null;
                Long startTime = evNode.hasNonNull("startTime") ? evNode.get("startTime").asLong() :
                                (evNode.hasNonNull("startTimeEpoch") ? evNode.get("startTimeEpoch").asLong() : null);
                String leagueId = evNode.hasNonNull("leagueId") ? evNode.get("leagueId").asText() : null;

                String leagueName = "General";
                if (leaguesNode != null && leagueId != null && leaguesNode.has(leagueId)) {
                    JsonNode lg = leaguesNode.get(leagueId);
                    if (lg.hasNonNull("name")) leagueName = lg.get("name").asText();
                }

                String sportName = null;
                if (sportsNode != null && sportId != null && sportsNode.has(sportId)) {
                    JsonNode sp = sportsNode.get(sportId);
                    if (sp.hasNonNull("name")) sportName = sp.get("name").asText();
                }

                BetanoEvent ev = new BetanoEvent();
                ev.setId(eid);
                ev.setName(name);
                ev.setSportId(sportId);
                if (sportName != null) {
                    ev.setSportName(sportName);
                }
                ev.setLeagueName(leagueName);
                ev.setStartTime(startTime);
                ev.setIsLive(Boolean.TRUE);

                if (evNode.has("participants") && evNode.get("participants").isArray()) {
                    List<java.util.Map<String, Object>> parts = new ArrayList<>();
                    for (JsonNode p : evNode.get("participants")) {
                        if (p.isObject()) {
                            parts.add(objectMapper.convertValue(p, java.util.Map.class));
                        } else if (p.isTextual()) {
                            parts.add(java.util.Map.of("name", p.asText()));
                        } else if (p.isNumber() && globalParticipantsNode != null) {
                            String pid = p.asText();
                            JsonNode partObj = globalParticipantsNode.get(pid);
                            if (partObj != null && partObj.hasNonNull("name")) {
                                parts.add(java.util.Map.of("name", partObj.get("name").asText()));
                            }
                        }
                    }
                    if (!parts.isEmpty()) {
                        ev.setParticipants(parts);
                    }
                }

                if (evNode.has("marketIdList") && evNode.get("marketIdList").isArray() && marketsNode != null) {
                    List<pro.datawiki.igaming.source.betano.dto.BetanoMarket> mktList = new ArrayList<>();
                    for (JsonNode midNode : evNode.get("marketIdList")) {
                        String mid = midNode.asText();
                        JsonNode mNode = marketsNode.get(mid);
                        if (mNode != null) {
                            pro.datawiki.igaming.source.betano.dto.BetanoMarket mkt = new pro.datawiki.igaming.source.betano.dto.BetanoMarket();
                            mkt.setId(mid);
                            mkt.setName(mNode.hasNonNull("name") ? mNode.get("name").asText() : "");
                            mkt.setType(mNode.hasNonNull("type") ? mNode.get("type").asText() : "");
                            mkt.setMarketType(mNode.hasNonNull("type") ? mNode.get("type").asText() : "");

                            if (mNode.has("selectionIdList") && mNode.get("selectionIdList").isArray() && selectionsNode != null) {
                                List<pro.datawiki.igaming.source.betano.dto.BetanoSelection> selList = new ArrayList<>();
                                for (JsonNode sidNode : mNode.get("selectionIdList")) {
                                    String sid = sidNode.asText();
                                    JsonNode sNode = selectionsNode.get(sid);
                                    if (sNode != null) {
                                        pro.datawiki.igaming.source.betano.dto.BetanoSelection sel = new pro.datawiki.igaming.source.betano.dto.BetanoSelection();
                                        sel.setId(sid);
                                        sel.setName(sNode.hasNonNull("name") ? sNode.get("name").asText() : "");
                                        if (sNode.hasNonNull("price")) sel.setPrice(sNode.get("price").asDouble());
                                        if (sNode.hasNonNull("handicap")) sel.setHandicap(sNode.get("handicap").asDouble());
                                        selList.add(sel);
                                    }
                                }
                                mkt.setSelections(selList);
                            }
                            mktList.add(mkt);
                        }
                    }
                    ev.setMarkets(mktList);
                }

                if (ev.getResolvedId() != null && ev.getResolvedTeams() != null && ev.getResolvedTeams().length >= 2) {
                    list.add(ev);
                    recentEventsById.put(ev.getResolvedId(), ev);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing Danae live overview: {}", e.getMessage());
        }
        return list;
    }

    public BetanoEvent getEventDetails(String eventId) {
        if (eventId != null) {
            return recentEventsById.get(eventId);
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
            JsonNode dataNode = (root.has("data") && root.get("data").isObject()) ? root.get("data") : root;

            boolean hasEvents = root.has("events") || dataNode.has("events") || root.has("liveOverview") || root.has("sports");
            boolean hasMarkets = root.has("markets") || dataNode.has("markets") || root.has("liveOverview") || root.has("sports");
            if (hasEvents && hasMarkets) {
                List<BetanoEvent> danaeEvents = parseDanaeLiveOverview(body);
                if (danaeEvents != null && !danaeEvents.isEmpty()) {
                    return danaeEvents;
                }
            }

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

        String rawId = node.hasNonNull("id") ? node.get("id").asText() : (node.hasNonNull("eventId") ? node.get("eventId").asText() : null);
        boolean hasId = rawId != null && rawId.matches("^\\d+$");
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
                    String[] teams = ev.getResolvedTeams();
                    if (teams != null && teams.length >= 2) {
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
