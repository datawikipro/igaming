package pro.datawiki.igaming.source.dafabet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.dafabet.config.DafabetConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DafabetApiClient {

    private final BrowserService browserService;
    private final DafabetConfig dafabetConfig;
    private final DafabetApiErrorTracker errorTracker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, List<JsonNode>> cachedSportEvents = new ConcurrentHashMap<>();
    private long lastCacheTime = 0;
    private static final long CACHE_TTL_MS = 180000; // Cache TTL is 180 seconds

    public synchronized JsonNode fetchOdds(String sportName) {
        long now = System.currentTimeMillis();
        if (cachedSportEvents.isEmpty() || (now - lastCacheTime) > CACHE_TTL_MS) {
            log.info("Cache expired or empty. Triggering fresh Dafabet WebSocket scraping...");
            try {
                scrapeAndPopulateCache();
                lastCacheTime = System.currentTimeMillis();
            } catch (Exception e) {
                log.error("Failed to scrape and populate Dafabet cache: {}", e.getMessage(), e);
                errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        String sportKey = sportName.toLowerCase().replace(" ", "");
        List<JsonNode> eventsForSport = cachedSportEvents.getOrDefault(sportKey, List.of());
        log.info("DafabetApiClient returning {} cached events for sport: {} (key: {})", eventsForSport.size(), sportName, sportKey);

        ObjectNode response = objectMapper.createObjectNode();
        ObjectNode dataNode = response.putObject("data");
        ArrayNode arrayNode = dataNode.putArray("moneyLineOddsList");
        eventsForSport.forEach(arrayNode::add);

        return response;
    }

    private void scrapeAndPopulateCache() {
        errorTracker.recordAttempt();

        String baseUrl = dafabetConfig.getApi().getBaseUrl();
        String targetUrl = baseUrl + "/en/sports";

        BrowserContext context = browserService.getContext("default");
        
        Map<String, Map<Integer, String>> namespaceSchemas = new ConcurrentHashMap<>();
        Map<Integer, String> globalSchemaRegistry = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> matchStore = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> oddsStore = new ConcurrentHashMap<>();
        Map<String, String> leagueNames = new ConcurrentHashMap<>();

        java.util.concurrent.atomic.AtomicBoolean webSocketOpened = new java.util.concurrent.atomic.AtomicBoolean(false);

        try (Page page = context.newPage()) {
            log.info("Navigating to {} and setting up WebSocket frames interceptor...", targetUrl);

            // Listen to HTTP responses to extract league names from the desktop menu
            page.onResponse(response -> {
                try {
                    String url = response.url();
                    if (url.contains("desktopMenu") || url.contains("Menu")) {
                        String body = response.text();
                        if (body != null && !body.isEmpty() && body.trim().startsWith("{")) {
                            JsonNode menuRoot = objectMapper.readTree(body);
                            populateLeagueNamesFromJson(menuRoot, leagueNames);
                            log.info("Interception update: Populated {} league names from desktopMenu", leagueNames.size());
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed parsing desktopMenu response: {}", e.getMessage());
                }
            });

            // Listen to WebSocket messages
            page.onWebSocket(ws -> {
                log.info("Dafabet WebSocket connection opened: {}", ws.url());
                webSocketOpened.set(true);
                
                ws.onFrameSent(frame -> {
                    String text = frame.text();
                    if (text != null && text.length() < 300) {
                        log.info("Dafabet WS Sent: '{}'", text);
                    }
                });

                java.util.concurrent.atomic.AtomicInteger frameCounter = new java.util.concurrent.atomic.AtomicInteger(0);
                ws.onFrameReceived(frame -> {
                    int count = frameCounter.incrementAndGet();
                    String text = frame.text();
                    byte[] binary = frame.binary();
                    
                    if (count <= 25) {
                        if (text != null) {
                            log.info("Dafabet WS Frame #{}: textLength={}, textPreview='{}'", 
                                    count, text.length(), text.substring(0, Math.min(text.length(), 200)));
                        } else if (binary != null) {
                            log.info("Dafabet WS Frame #{}: binaryLength={}", count, binary.length);
                        } else {
                            log.info("Dafabet WS Frame #{}: empty/null frame", count);
                        }
                    }

                    if (text == null || text.isEmpty()) return;

                    try {
                        // Socket.IO / Engine.IO protocol check: messages start with '42'
                        if (text.startsWith("42")) {
                            if (text.contains("\"f\"")) {
                                log.info("Dafabet WS contains schema marker 'f': {}", text.substring(0, Math.min(text.length(), 300)));
                            }
                            String jsonStr = text.substring(2);
                            JsonNode root = objectMapper.readTree(jsonStr);
                            if (root.isArray() && root.size() >= 2) {
                                String eventName = root.get(0).asText();
                                if (!"m".equals(eventName)) {
                                    log.info("Dafabet WS eventName: {} (root size: {})", eventName, root.size());
                                }
                                if ("m".equals(eventName) && root.size() >= 3) {
                                    String namespace = root.get(1).asText();
                                    JsonNode dataNode = root.get(2);
                                    if (dataNode.isArray()) {
                                        processBatch(dataNode, namespace, namespaceSchemas, globalSchemaRegistry, matchStore, oddsStore, leagueNames);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed parsing WebSocket frame content: {}", e.getMessage());
                    }
                });
            });

            page.navigate(targetUrl, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(45000));

            log.info("Waiting for WebSocket connection to open...");
            long startWait = System.currentTimeMillis();
            while (!webSocketOpened.get() && (System.currentTimeMillis() - startWait) < 60000) {
                page.waitForTimeout(500);
            }

            if (webSocketOpened.get()) {
                log.info("WebSocket established. Waiting 40 seconds for WebSocket data snapshot stream to accumulate...");
                page.waitForTimeout(40000);
            } else {
                log.warn("WebSocket connection was not established within 60 seconds of navigation.");
            }

            log.info("Interception window closed. Collected {} matches, {} odds, {} league names.", 
                    matchStore.size(), oddsStore.size(), leagueNames.size());

            buildAndCacheEvents(matchStore, oddsStore, leagueNames, globalSchemaRegistry);

        } catch (Exception e) {
            log.error("Error running Playwright crawler for Dafabet: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void processBatch(JsonNode batch,
                              String namespace,
                              Map<String, Map<Integer, String>> namespaceSchemas,
                              Map<Integer, String> globalSchemaRegistry,
                              Map<String, Map<String, Object>> matchStore,
                              Map<String, Map<String, Object>> oddsStore,
                              Map<String, String> leagueNames) {
        Map<Integer, String> nsSchema = namespaceSchemas.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());

        for (JsonNode item : batch) {
            if (!item.isArray() || item.size() < 2) continue;

            // Handle schema definition frame:
            // Case A: ["f", startIndex, [fieldNames...]] (size >= 3)
            // Case B: [namespace, "f", startIndex, [fieldNames...]] (size >= 4)
            if ("f".equals(item.get(0).asText())) {
                if (item.size() >= 3 && item.get(2).isArray()) {
                    int startIndex = item.get(1).asInt();
                    JsonNode fields = item.get(2);
                    for (int i = 0; i < fields.size(); i++) {
                        String fName = fields.get(i).asText();
                        nsSchema.put(startIndex + i, fName);
                        globalSchemaRegistry.put(startIndex + i, fName);
                    }
                    continue;
                }
            } else if (item.size() >= 4 && "f".equals(item.get(1).asText())) {
                if (item.get(3).isArray()) {
                    int startIndex = item.get(2).asInt();
                    JsonNode fields = item.get(3);
                    for (int i = 0; i < fields.size(); i++) {
                        String fName = fields.get(i).asText();
                        nsSchema.put(startIndex + i, fName);
                        globalSchemaRegistry.put(startIndex + i, fName);
                    }
                    continue;
                }
            }

            String action = item.get(1).asText();

            if ("m".equals(action) || "st".equals(action)) {
                // Match update: [namespace, action, index1, val1, index2, val2, ...]
                Map<String, Object> props = parseAlternatingFields(item, nsSchema, globalSchemaRegistry);
                String matchId = getStringProperty(props, "matchid", "1");
                if (matchId != null) {
                    Map<String, Object> existing = matchStore.computeIfAbsent(matchId, k -> new HashMap<>());
                    existing.putAll(props);
                }
            } else if ("o".equals(action)) {
                // Odds update: [namespace, "o", index1, val1, index2, val2, ...]
                Map<String, Object> props = parseAlternatingFields(item, nsSchema, globalSchemaRegistry);
                String oddsId = getStringProperty(props, "oddsid", "2");
                if (oddsId != null) {
                    Map<String, Object> existing = oddsStore.computeIfAbsent(oddsId, k -> new HashMap<>());
                    existing.putAll(props);
                }
            } else if ("t".equals(action) || "l".equals(action)) {
                // Tournament/League update: [namespace, action, index1, val1, index2, val2]
                Map<String, Object> props = parseAlternatingFields(item, nsSchema, globalSchemaRegistry);
                String leagueId = getStringProperty(props, "leagueid", "1");
                String leagueName = getStringProperty(props, "leaguenameen", "leaguenamecn", "leaguename", "2");
                if (leagueId != null && leagueName != null) {
                    leagueNames.put(leagueId, cleanHtml(leagueName));
                }
            } else if ("-o".equals(action)) {
                // Delete odds: [namespace, "-o", oddsId]
                if (item.size() >= 3) {
                    String oddsId = item.get(2).asText();
                    oddsStore.remove(oddsId);
                }
            } else if ("-st".equals(action) || "-m".equals(action)) {
                // Delete match: [namespace, "-st", matchId]
                if (item.size() >= 3) {
                    String matchId = item.get(2).asText();
                    matchStore.remove(matchId);
                }
            }
        }
    }

    private Map<String, Object> parseAlternatingFields(JsonNode item, Map<Integer, String> schema, Map<Integer, String> fallbackSchema) {
        Map<String, Object> props = new HashMap<>();
        for (int i = 2; i < item.size() - 1; i += 2) {
            int fieldIdx = item.get(i).asInt();
            JsonNode valNode = item.get(i + 1);
            String fieldName = (schema != null) ? schema.get(fieldIdx) : null;
            if (fieldName == null && fallbackSchema != null) {
                fieldName = fallbackSchema.get(fieldIdx);
            }
            
            Object value = getJsonValue(valNode);
            if (fieldName != null) {
                props.put(fieldName, value);
            }
            props.put(String.valueOf(fieldIdx), value);
        }
        return props;
    }

    private Object getJsonValue(JsonNode node) {
        if (node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isIntegralNumber()) {
            return node.asLong();
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        return node.asText();
    }

    private void populateLeagueNamesFromJson(JsonNode node, Map<String, String> leagueNames) {
        if (node.isObject()) {
            JsonNode idNode = node.path("leagueId");
            if (idNode.isMissingNode()) idNode = node.path("tournamentId");
            if (idNode.isMissingNode()) idNode = node.path("LeagueId");
            if (idNode.isMissingNode()) idNode = node.path("TournamentId");

            JsonNode nameNode = node.path("leagueName");
            if (nameNode.isMissingNode()) nameNode = node.path("tournamentName");
            if (nameNode.isMissingNode()) nameNode = node.path("LeagueName");
            if (nameNode.isMissingNode()) nameNode = node.path("TournamentName");
            if (nameNode.isMissingNode()) nameNode = node.path("name"); // fallback

            if (!idNode.isMissingNode() && !nameNode.isMissingNode()) {
                leagueNames.put(idNode.asText(), nameNode.asText());
            }
            node.fields().forEachRemaining(entry -> populateLeagueNamesFromJson(entry.getValue(), leagueNames));
        } else if (node.isArray()) {
            node.forEach(item -> populateLeagueNamesFromJson(item, leagueNames));
        }
    }

    private void buildAndCacheEvents(Map<String, Map<String, Object>> matchStore,
                                     Map<String, Map<String, Object>> oddsStore,
                                     Map<String, String> leagueNames,
                                     Map<Integer, String> schemaRegistry) {

        // Build reverse schema map: fieldName -> index (as String)
        Map<String, String> fieldToIndex = new HashMap<>();
        for (Map.Entry<Integer, String> e : schemaRegistry.entrySet()) {
            fieldToIndex.putIfAbsent(e.getValue(), String.valueOf(e.getKey()));
        }
        log.info("Schema registry ({} fields): {}", schemaRegistry.size(), schemaRegistry);
        log.info("Reverse field map: {}", fieldToIndex);

        // Resolve field indices dynamically with precise schema-verified fallbacks
        String homeNameIdx  = resolveIndex(fieldToIndex, "69", "hteamnameen", "homeName", "home_name", "homeTeamName", "home_en", "homeNameEn");
        String homeNameIdx2 = resolveIndex(fieldToIndex, "124", "hteamnamesc2", "hteamnamecn", "homeName2", "homeNameLocal");
        String awayNameIdx  = resolveIndex(fieldToIndex, "71", "ateamnameen", "awayName", "away_name", "awayTeamName", "away_en", "awayNameEn");
        String awayNameIdx2 = resolveIndex(fieldToIndex, "127", "ateamnamesc2", "ateamnamecn", "awayName2", "awayNameLocal");
        String leagueIdIdx  = resolveIndex(fieldToIndex, "45", "leagueid", "leagueId", "tournamentId", "league_id", "tournament_id");
        String kickoffIdx   = resolveIndex(fieldToIndex, "63", "kickofftime", "kickoffTime", "kickoff", "startTime", "start_time", "matchTime");
        String liveIdx      = resolveIndex(fieldToIndex, "64", "eventstatus", "isLive", "liveStatus", "matchStatus", "status");
        String sportIdIdx   = resolveIndex(fieldToIndex, "46", "sporttype", "sportId", "sport_id", "sportType");

        log.info("Resolved field indices: home={}/{}, away={}/{}, league={}, kickoff={}, live={}, sport={}",
                homeNameIdx, homeNameIdx2, awayNameIdx, awayNameIdx2, leagueIdIdx, kickoffIdx, liveIdx, sportIdIdx);

        String matchIdIdx = resolveIndex(fieldToIndex, "1", "matchid", "matchId", "eventId", "eventid");
        Map<String, List<Map<String, Object>>> oddsByMatch = new HashMap<>();
        for (Map<String, Object> oddsProps : oddsStore.values()) {
            String matchId = getStringProperty(oddsProps, matchIdIdx, "1", "matchid", "matchId", "eventId", "eventid");
            if (matchId != null) {
                oddsByMatch.computeIfAbsent(matchId, k -> new ArrayList<>()).add(oddsProps);
            }
        }

        cachedSportEvents.clear();

        int loggedMatches = 0;
        for (Map.Entry<String, Map<String, Object>> entry : matchStore.entrySet()) {
            String matchId = entry.getKey();
            Map<String, Object> matchProps = entry.getValue();

            if (loggedMatches < 5) {
                log.info("DEBUG raw matchProps for matchId {}: {}", matchId, matchProps);
                loggedMatches++;
            }

            // Schema-aware team name resolution prioritizing verified English team names
            String homeName = getTeamName(matchProps, "hteamnameen", "hteamnamesc2", "hteamnamecn", "hteamnamech", "homeName", "home_name", "homeTeamName", "home_en", "homeNameEn", "69", "66", "92", homeNameIdx, homeNameIdx2);
            String awayName = getTeamName(matchProps, "ateamnameen", "ateamnamesc2", "ateamnamecn", "ateamnamech", "awayName", "away_name", "awayTeamName", "away_en", "awayNameEn", "71", "98", "94", awayNameIdx, awayNameIdx2);

            if (homeName == null || awayName == null) {
                continue;
            }

            homeName = cleanHtml(homeName);
            awayName = cleanHtml(awayName);

            String leagueName = getStringProperty(matchProps, "leaguenameen", "leaguenamecn", "leaguenamesc2", "leaguenamech", "leagueName", "tournamentName", "172", "174");
            if (leagueName == null || leagueName.isBlank()) {
                String leagueId = getStringProperty(matchProps, "leagueid", "45", "47", leagueIdIdx);
                if (leagueId != null) {
                    leagueName = leagueNames.getOrDefault(leagueId, "League " + leagueId);
                } else {
                    leagueName = "Unknown League";
                }
            }

            Long kickOffTime = getLongProperty(matchProps, "kickofftime", "kickoffTime", "kickoff", "startTime", "start_time", "matchTime", "63", "62", "86", kickoffIdx);
            long startTimeMillis = (kickOffTime != null) ? kickOffTime * 1000 : System.currentTimeMillis() + 3600000;

            boolean isLive = false;
            String liveIndicator = getStringProperty(matchProps, "eventstatus", "isLive", "liveStatus", "matchStatus", "status", "64", "63", "87", liveIdx);
            if (liveIndicator != null && (liveIndicator.toLowerCase().contains("live") || liveIndicator.toLowerCase().contains("running"))) {
                isLive = true;
            }

            String sportKey = "soccer";
            Long sportId = getLongProperty(matchProps, "sporttype", "sportId", "sport_id", "sportType", "46", "48", "90", sportIdIdx);
            if (sportId != null) {
                if (sportId == 1) sportKey = "soccer";
                else if (sportId == 2) sportKey = "basketball";
                else if (sportId == 3) sportKey = "baseball";
                else if (sportId == 4) sportKey = "icehockey";
                else if (sportId == 5) sportKey = "tennis";
                else if (sportId == 6) sportKey = "volleyball";
                else if (sportId == 8) sportKey = "handball";
                else if (sportId == 9) sportKey = "tabletennis";
                else if (sportId == 10) sportKey = "futsal";
                else if (sportId == 12) sportKey = "badminton";
                else if (sportId == 13) sportKey = "snooker";
                else if (sportId == 14) sportKey = "darts";
                else if (sportId == 15) sportKey = "rugby";
                else if (sportId == 16) sportKey = "cricket";
                else if (sportId == 19) sportKey = "mma";
                else if (sportId == 43) sportKey = "esports";
                // Do not drop unknown sport IDs - fallback to soccer/general
            }

            ObjectNode eventNode = objectMapper.createObjectNode();
            eventNode.put("id", matchId);
            eventNode.put("home", homeName);
            eventNode.put("away", awayName);
            eventNode.put("leagueName", leagueName);
            eventNode.put("isLive", isLive);
            eventNode.put("startTime", startTimeMillis);

            ObjectNode moneylineNode = objectMapper.createObjectNode();
            ArrayNode handicapsArray = eventNode.putArray("handicaps");
            ArrayNode totalsArray = eventNode.putArray("totals");

            // Resolve odds field indices dynamically
            String betTypeIdx = resolveIndex(fieldToIndex, "0", "type", "bettype", "betType", "bet_type", "oddType");
            String odds1Idx   = resolveIndex(fieldToIndex, "20", "odds1a", "odds1", "homeOdds", "price1");
            String odds1AltIdx = resolveIndex(fieldToIndex, "37", "odds1Alt");
            String odds2Idx   = resolveIndex(fieldToIndex, "21", "odds2a", "odds2", "awayOdds", "price2");
            String odds2AltIdx = resolveIndex(fieldToIndex, "38", "odds2Alt");
            String spreadIdx  = resolveIndex(fieldToIndex, "9", "oddsspreada", "spread", "handicap", "line");
            String drawIdx    = resolveIndex(fieldToIndex, "36", "draw", "bal50", "drawOdds", "priceDraw");

            List<Map<String, Object>> matchOddsList = oddsByMatch.getOrDefault(matchId, List.of());
            for (Map<String, Object> oddsProps : matchOddsList) {
                Double betTypeVal = getDoubleProperty(oddsProps, betTypeIdx, "type", "0", "bettype", "betType", "bet_type", "oddType", "6");
                if (betTypeVal == null) continue;
                int betType = betTypeVal.intValue();

                Double odds1 = getDoubleProperty(oddsProps, odds1Idx, "odds1a", "20", "odds1", "homeOdds", "price1", "3");
                if (odds1 == null) odds1 = getDoubleProperty(oddsProps, odds1AltIdx, "odds1Alt", "37");

                Double odds2 = getDoubleProperty(oddsProps, odds2Idx, "odds2a", "21", "odds2", "awayOdds", "price2", "4");
                if (odds2 == null) odds2 = getDoubleProperty(oddsProps, odds2AltIdx, "odds2Alt", "38");

                Double spread = getDoubleProperty(oddsProps, spreadIdx, "oddsspreada", "9", "spread", "handicap", "line", "5");

                if (odds1 != null) odds1 = convertToDecimal(odds1);
                if (odds2 != null) odds2 = convertToDecimal(odds2);

                if (betType == 1) {
                    if (odds1 != null && odds2 != null && spread != null) {
                        ObjectNode hdp = objectMapper.createObjectNode();
                        hdp.put("limit", spread);
                        hdp.put("home", odds1);
                        hdp.put("away", odds2);
                        handicapsArray.add(hdp);
                    }
                } else if (betType == 2) {
                    if (odds1 != null && odds2 != null && spread != null) {
                        ObjectNode tot = objectMapper.createObjectNode();
                        tot.put("limit", spread);
                        tot.put("over", odds1);
                        tot.put("under", odds2);
                        totalsArray.add(tot);
                    }
                } else if (betType == 3) {
                    if (odds1 != null && odds2 != null) {
                        moneylineNode.put("home", odds1);
                        moneylineNode.put("away", odds2);
                        Double drawOdds = getDoubleProperty(oddsProps, drawIdx, "draw", "bal50", "drawOdds", "priceDraw", "36");
                        if (drawOdds != null) {
                            moneylineNode.put("draw", convertToDecimal(drawOdds));
                        }
                    }
                }
            }

            if (moneylineNode.size() > 0) {
                eventNode.set("moneyline", moneylineNode);
            }

            cachedSportEvents.computeIfAbsent(sportKey, k -> new ArrayList<>()).add(eventNode);
        }
    }

    /**
     * Resolves a field index: first checks schema registry for known field names,
     * falls back to the hardcoded default index.
     */
    private String resolveIndex(Map<String, String> fieldToIndex, String defaultIdx, String... fieldNames) {
        for (String name : fieldNames) {
            String idx = fieldToIndex.get(name);
            if (idx != null) return idx;
        }
        return defaultIdx;
    }

    private String cleanHtml(String text) {
        if (text == null) return null;
        return text.replaceAll("<[^>]*>", "").trim();
    }

    private double convertToDecimal(double value) {
        if (value <= 0) {
            if (value < -1.0) {
                return 1.0 - value; // Indo format
            } else if (value > -1.0 && value < 0) {
                return 1.0 - 1.0 / value; // Malay format
            }
        } else {
            if (value < 1.0) {
                return value + 1.0; // Hong Kong format
            }
        }
        return value;
    }

    private String getTeamName(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val == null) {
                continue;
            }
            if (val instanceof Boolean) {
                continue;
            }
            if (val instanceof Number) {
                continue;
            }
            String str = val.toString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
                continue;
            }
            if (isNumeric(str)) {
                continue;
            }
            return str;
        }
        return null;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getStringProperty(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val != null) {
                if (val instanceof Number) {
                    double d = ((Number) val).doubleValue();
                    if (d == Math.floor(d) && !Double.isInfinite(d)) {
                        return String.valueOf(((Number) val).longValue());
                    }
                }
                String s = val.toString();
                if (s.endsWith(".0")) {
                    try {
                        double d = Double.parseDouble(s);
                        if (d == Math.floor(d)) {
                            return String.valueOf((long) d);
                        }
                    } catch (Exception ignored) {}
                }
                return s;
            }
        }
        return null;
    }

    private Double getDoubleProperty(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val != null) {
                if (val instanceof Number) {
                    return ((Number) val).doubleValue();
                }
                try {
                    return Double.parseDouble(val.toString());
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Long getLongProperty(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val != null) {
                if (val instanceof Number) {
                    return ((Number) val).longValue();
                }
                try {
                    return Double.valueOf(val.toString().trim()).longValue();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
