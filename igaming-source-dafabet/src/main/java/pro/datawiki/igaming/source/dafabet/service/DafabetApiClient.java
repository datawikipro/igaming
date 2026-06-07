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
    private static final long CACHE_TTL_MS = 60000; // Cache TTL is 60 seconds

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
        cachedSportEvents.clear();
        errorTracker.recordAttempt();

        String baseUrl = dafabetConfig.getApi().getBaseUrl();
        String targetUrl = baseUrl + "/en/sports";

        BrowserContext context = browserService.getContext("default");
        
        Map<Integer, String> schemaRegistry = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> matchStore = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> oddsStore = new ConcurrentHashMap<>();
        Map<String, String> leagueNames = new ConcurrentHashMap<>();

        try (Page page = context.newPage()) {
            log.info("Navigating to {} and setting up WebSocket frames interceptor...", targetUrl);

            page.onWebSocket(ws -> {
                log.info("Dafabet WebSocket connection opened: {}", ws.url());
                ws.onFrameReceived(frame -> {
                    String text = frame.text();
                    if (text == null || text.isEmpty()) return;

                    try {
                        // Socket.IO / Engine.IO protocol check: messages usually start with '42'
                        if (text.startsWith("42")) {
                            String jsonStr = text.substring(2);
                            JsonNode root = objectMapper.readTree(jsonStr);
                            if (root.isArray() && root.size() >= 3) {
                                String eventName = root.get(0).asText();
                                if ("m".equals(eventName)) {
                                    JsonNode dataNode = root.get(2);
                                    if (dataNode.isArray()) {
                                        processBatch(dataNode, schemaRegistry, matchStore, oddsStore, leagueNames);
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

            log.info("Waiting 25 seconds for WebSocket data snapshot stream to accumulate...");
            page.waitForTimeout(25000);

            log.info("Interception window closed. Collected {} matches, {} odds, {} league names.", 
                    matchStore.size(), oddsStore.size(), leagueNames.size());

            buildAndCacheEvents(matchStore, oddsStore, leagueNames, schemaRegistry);

        } catch (Exception e) {
            log.error("Error running Playwright crawler for Dafabet: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void processBatch(JsonNode batch, 
                              Map<Integer, String> schemaRegistry,
                              Map<String, Map<String, Object>> matchStore,
                              Map<String, Map<String, Object>> oddsStore,
                              Map<String, String> leagueNames) {
        for (JsonNode item : batch) {
            if (!item.isArray() || item.size() < 2) continue;

            String action = item.get(1).asText();

            if ("f".equals(action)) {
                // Schema registry definition: [namespace, "f", startIndex, [fieldNames...]]
                if (item.size() >= 4 && item.get(3).isArray()) {
                    int startIndex = item.get(2).asInt();
                    JsonNode fields = item.get(3);
                    for (int i = 0; i < fields.size(); i++) {
                        schemaRegistry.put(startIndex + i, fields.get(i).asText());
                    }
                }
            } else if ("m".equals(action)) {
                // Match update: [namespace, "m", index1, val1, index2, val2, ...]
                Map<String, Object> props = parseAlternatingFields(item, schemaRegistry);
                String matchId = getStringProperty(props, "matchid", "MatchId", "1");
                if (matchId != null) {
                    Map<String, Object> existing = matchStore.computeIfAbsent(matchId, k -> new HashMap<>());
                    existing.putAll(props);
                }
            } else if ("o".equals(action)) {
                // Odds update: [namespace, "o", index1, val1, index2, val2, ...]
                Map<String, Object> props = parseAlternatingFields(item, schemaRegistry);
                String oddsId = getStringProperty(props, "oddsid", "OddsId", "2");
                if (oddsId != null) {
                    Map<String, Object> existing = oddsStore.computeIfAbsent(oddsId, k -> new HashMap<>());
                    existing.putAll(props);
                }
            } else if ("t".equals(action) || "l".equals(action)) {
                // League/Tournament update
                Map<String, Object> props = parseAlternatingFields(item, schemaRegistry);
                String leagueId = getStringProperty(props, "tournamentid", "TournamentId", "leagueid", "LeagueId", "1");
                String leagueName = getStringProperty(props, "tournamentname", "TournamentName", "leaguename", "LeagueName", "2");
                if (leagueId != null && leagueName != null) {
                    leagueNames.put(leagueId, leagueName);
                }
            } else if ("-o".equals(action)) {
                // Delete odds: [namespace, "-o", oddsId]
                if (item.size() >= 3) {
                    String oddsId = item.get(2).asText();
                    oddsStore.remove(oddsId);
                }
            } else if ("-st".equals(action)) {
                // Delete match: [namespace, "-st", matchId]
                if (item.size() >= 3) {
                    String matchId = item.get(2).asText();
                    matchStore.remove(matchId);
                }
            }
        }
    }

    private Map<String, Object> parseAlternatingFields(JsonNode item, Map<Integer, String> schemaRegistry) {
        Map<String, Object> props = new HashMap<>();
        for (int i = 2; i < item.size() - 1; i += 2) {
            int fieldIdx = item.get(i).asInt();
            JsonNode valNode = item.get(i + 1);
            String fieldName = schemaRegistry.get(fieldIdx);
            
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
        if (node.isNumber()) {
            return node.asDouble();
        }
        return node.asText();
    }

    private void buildAndCacheEvents(Map<String, Map<String, Object>> matchStore,
                                     Map<String, Map<String, Object>> oddsStore,
                                     Map<String, String> leagueNames,
                                     Map<Integer, String> schemaRegistry) {
        
        Map<String, List<Map<String, Object>>> oddsByMatch = new HashMap<>();
        for (Map<String, Object> oddsProps : oddsStore.values()) {
            String matchId = getStringProperty(oddsProps, "matchid", "MatchId", "1");
            if (matchId != null) {
                oddsByMatch.computeIfAbsent(matchId, k -> new ArrayList<>()).add(oddsProps);
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : matchStore.entrySet()) {
            String matchId = entry.getKey();
            Map<String, Object> matchProps = entry.getValue();

            String homeName = getStringProperty(matchProps, "home", "homeName", "HomeName", "Team1", "Home", "93", "149", "163");
            String awayName = getStringProperty(matchProps, "away", "awayName", "AwayName", "Team2", "Away", "128", "167");

            if (homeName == null || awayName == null) {
                continue;
            }

            homeName = cleanHtml(homeName);
            awayName = cleanHtml(awayName);

            String leagueId = getStringProperty(matchProps, "tournamentid", "TournamentId", "leagueid", "LeagueId", "89");
            String leagueName = "Unknown League";
            if (leagueId != null) {
                leagueName = leagueNames.getOrDefault(leagueId, "League " + leagueId);
            }

            Long kickOffTime = getLongProperty(matchProps, "kickOffTime", "kickoffTime", "MatchTime", "88");
            long startTimeMillis = (kickOffTime != null) ? kickOffTime * 1000 : System.currentTimeMillis() + 3600000;

            boolean isLive = false;
            String liveIndicator = getStringProperty(matchProps, "liveindicator", "LiveIndicator", "90", "81");
            if (liveIndicator != null && (liveIndicator.toLowerCase().contains("live") || liveIndicator.toLowerCase().contains("running"))) {
                isLive = true;
            }

            String sportKey = "soccer";
            Long sportId = getLongProperty(matchProps, "sportid", "sportId", "SportId", "sport", "121");
            if (sportId != null) {
                if (sportId == 1) sportKey = "soccer";
                else if (sportId == 2) sportKey = "basketball";
                else if (sportId == 4) sportKey = "icehockey";
                else if (sportId == 5) sportKey = "tennis";
                else if (sportId == 6) sportKey = "volleyball";
                else {
                    continue; // Skip unsupported sports
                }
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

            List<Map<String, Object>> matchOddsList = oddsByMatch.getOrDefault(matchId, List.of());
            for (Map<String, Object> oddsProps : matchOddsList) {
                Double betTypeVal = getDoubleProperty(oddsProps, "bettype", "BetType", "6");
                if (betTypeVal == null) continue;
                int betType = betTypeVal.intValue();

                Double odds1 = getDoubleProperty(oddsProps, "odds1a", "odds1", "Odds1", "3");
                Double odds2 = getDoubleProperty(oddsProps, "odds2a", "odds2", "Odds2", "4");
                Double spread = getDoubleProperty(oddsProps, "oddsspreada", "oddsspread", "OddsSpread", "5");

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
                        Double drawOdds = getDoubleProperty(oddsProps, "bal50", "draw", "drawOdds", "36");
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

    private String getStringProperty(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val != null) {
                return val.toString();
            }
        }
        return null;
    }

    private Double getDoubleProperty(Map<String, Object> props, String... keys) {
        for (String key : keys) {
            Object val = props.get(key);
            if (val != null) {
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
                try {
                    return Double.valueOf(val.toString()).longValue();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
