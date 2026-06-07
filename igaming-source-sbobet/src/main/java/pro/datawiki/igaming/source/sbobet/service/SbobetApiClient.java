package pro.datawiki.igaming.source.sbobet.service;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.sbobet.config.SbobetConfig;
 
import java.util.Map;
 
/**
 * Fetches SBOBET odds using two strategies:
 * <ol>
 *   <li><b>Primary</b>: Navigate to the sport's betting page and intercept the JSON XHR
 *       response that SBOBET's own frontend makes (pattern: {@code /web-root/delivery/odds}).
 *       This works because SBOBET blocks direct API access but allows the request when it
 *       originates from within the page context with proper session cookies.</li>
 *   <li><b>Fallback</b>: Direct URL navigation to the odds endpoint (legacy behaviour).</li>
 * </ol>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SbobetApiClient {
 
    private static final int INTERCEPT_TIMEOUT_MS = 25000;

    /** Map from sport name (lower-case) to SBOBET URL segment */
    private static final Map<String, String> SPORT_URL_SEGMENTS = Map.of(
            "football",   "football",
            "soccer",     "football",
            "tennis",     "tennis",
            "basketball", "basketball",
            "volleyball", "volleyball",
            "hockey",     "ice-hockey",
            "icehockey",  "ice-hockey",
            "baseball",   "baseball",
            "cricket",    "cricket"
    );

    private final BrowserService browserService;
    private final SbobetConfig sbobetConfig;
    private final SbobetApiErrorTracker errorTracker;
    private final VpnManagerService vpnManagerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public JsonNode fetchOdds(String sportName) {
        String baseUrl = sbobetConfig.getApi().getBaseUrl();
        String sportSegment = SPORT_URL_SEGMENTS.getOrDefault(sportName.toLowerCase(), sportName.toLowerCase());
        String bettingPageUrl = baseUrl + "/ru-RU/euro/" + sportSegment;

        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("Fetching SBOBET odds for sport: {} (attempt {}/3)", sportName, attempt);
            errorTracker.recordAttempt();
            try {
                log.info("Navigating to SBOBET betting page: {}", bettingPageUrl);
                String html = browserService.navigateAndGetBody(bettingPageUrl, INTERCEPT_TIMEOUT_MS);

                if (html == null || html.isEmpty()) {
                    log.warn("Empty response from SBOBET for sport: {} (attempt {}/3)", sportName, attempt);
                    errorTracker.recordError("Empty response");
                    rotateAndWait(attempt);
                    continue;
                }

                // 1. Extract league ID to name mapping dictionary from HTML
                java.util.Map<String, String> leagueMap = new java.util.HashMap<>();
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "id=\"bu:od:afa:to:(\\d+)\".*?<div class=\"SubHeadT\">([^<]+)</div>",
                        java.util.regex.Pattern.DOTALL
                );
                java.util.regex.Matcher matcher = pattern.matcher(html);
                while (matcher.find()) {
                    String leagueId = matcher.group(1);
                    String leagueName = matcher.group(2).trim();
                    leagueMap.put(leagueId, leagueName);
                }
                log.info("Extracted {} league mappings from SBOBET HTML markup", leagueMap.size());

                // 2. Locate the $P.onUpdate('od', ...) script block and extract JS array using bracket counting
                int startIdx = html.indexOf("$P.onUpdate('od',");
                if (startIdx == -1) {
                    log.warn("Could not find $P.onUpdate('od', in SBOBET HTML (attempt {}/3)", attempt);
                    errorTracker.recordError("Missing onUpdate script");
                    rotateAndWait(attempt);
                    continue;
                }

                int openBracketIdx = html.indexOf("[", startIdx);
                if (openBracketIdx == -1) {
                    log.warn("Could not find opening [ in SBOBET HTML (attempt {}/3)", attempt);
                    errorTracker.recordError("Missing opening bracket");
                    rotateAndWait(attempt);
                    continue;
                }

                int bracketCount = 0;
                int endIdx = -1;
                for (int i = openBracketIdx; i < html.length(); i++) {
                    char c = html.charAt(i);
                    if (c == '[') {
                        bracketCount++;
                    } else if (c == ']') {
                        bracketCount--;
                        if (bracketCount == 0) {
                            endIdx = i;
                            break;
                        }
                    }
                }

                if (endIdx == -1) {
                    log.warn("Could not find matching closing ] in SBOBET HTML (attempt {}/3)", attempt);
                    errorTracker.recordError("Unbalanced brackets");
                    rotateAndWait(attempt);
                    continue;
                }

                String jsonArrayStr = html.substring(openBracketIdx, endIdx + 1);

                // 3. Clean up sparse arrays
                while (jsonArrayStr.contains(",,")) {
                    jsonArrayStr = jsonArrayStr.replace(",,", ",null,");
                }
                while (jsonArrayStr.contains(", ,")) {
                    jsonArrayStr = jsonArrayStr.replace(", ,", ",null,");
                }

                // 4. Configure ObjectMapper and parse clean JSON
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                JsonNode rootNode = mapper.readTree(jsonArrayStr);

                // 5. Transform raw SBOBET structure to standard leagues hierarchy
                com.fasterxml.jackson.databind.node.ObjectNode transformedResponse = mapper.createObjectNode();
                com.fasterxml.jackson.databind.node.ArrayNode leaguesArray = transformedResponse.putArray("leagues");

                java.util.Map<String, com.fasterxml.jackson.databind.node.ObjectNode> leaguesMap = new java.util.HashMap<>();

                if (rootNode.isArray() && rootNode.size() > 2 && rootNode.get(2).isArray()) {
                    JsonNode dataList = rootNode.get(2);
                    for (int d = 0; d < dataList.size(); d++) {
                        JsonNode dNode = dataList.get(d);
                        if (dNode.isArray() && dNode.size() > 1 && dNode.get(1).isArray()) {
                            JsonNode eventGroups = dNode.get(1);
                            for (int e = 0; e < eventGroups.size(); e++) {
                                JsonNode eg = eventGroups.get(e);
                                if (eg.size() > 4) {
                                    JsonNode eventInfo = eg.get(2);
                                    JsonNode oddsArray = eg.get(4);

                                    String eventId = eventInfo.get(0).asText();
                                    if (eventId.isEmpty()) {
                                        continue;
                                    }

                                    String homeTeam = eventInfo.get(1).asText();
                                    String awayTeam = eventInfo.get(2).asText();
                                    String startTimeStr = eventInfo.get(5).asText();
                                    
                                    // Parse start time to epoch millis (format MM/dd/yyyy HH:mm)
                                    long startTimeMillis = 0;
                                    try {
                                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                                .ofPattern("MM/dd/yyyy HH:mm");
                                        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(startTimeStr, formatter);
                                        startTimeMillis = localDateTime.atZone(java.time.ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli();
                                    } catch (Exception ex) {
                                        startTimeMillis = java.time.Instant.now().toEpochMilli() + 3600000; // fallback to 1 hour from now
                                    }

                                    boolean isLive = false;
                                    String leagueId = eg.get(1).asText();
                                    String leagueName = leagueMap.getOrDefault(leagueId, "League " + leagueId);

                                    // Get or create league node
                                    com.fasterxml.jackson.databind.node.ObjectNode leagueNode = leaguesMap.get(leagueId);
                                    if (leagueNode == null) {
                                        leagueNode = mapper.createObjectNode();
                                        leagueNode.put("name", leagueName);
                                        leagueNode.putArray("events");
                                        leaguesArray.add(leagueNode);
                                        leaguesMap.put(leagueId, leagueNode);
                                    }
                                    com.fasterxml.jackson.databind.node.ArrayNode eventsArray = (com.fasterxml.jackson.databind.node.ArrayNode) leagueNode.get("events");

                                    // Create event node
                                    com.fasterxml.jackson.databind.node.ObjectNode eventNode = mapper.createObjectNode();
                                    eventNode.put("id", eventId);
                                    eventNode.put("home", homeTeam);
                                    eventNode.put("away", awayTeam);
                                    eventNode.put("startTime", startTimeMillis);
                                    eventNode.put("isLive", isLive);

                                    // Parse odds inside oddsArray
                                    com.fasterxml.jackson.databind.node.ObjectNode moneylineNode = mapper.createObjectNode();
                                    com.fasterxml.jackson.databind.node.ArrayNode handicapsArray = eventNode.putArray("handicaps");
                                    com.fasterxml.jackson.databind.node.ArrayNode totalsArray = eventNode.putArray("totals");

                                    for (int o = 0; o < oddsArray.size(); o++) {
                                        JsonNode oddItemNode = oddsArray.get(o);
                                        if (oddItemNode.isArray() && oddItemNode.size() > 2) {
                                            JsonNode meta = oddItemNode.get(1);
                                            JsonNode values = oddItemNode.get(2);
                                            
                                            if (meta.isArray() && meta.size() > 5 && values.isArray() && values.size() > 1) {
                                                int type = meta.get(0).asInt();
                                                
                                                if (type == 1) {
                                                    // Handicap (Asian spread)
                                                    double rawAwayHdp = meta.get(5).asDouble();
                                                    double hdp = -rawAwayHdp; // home handicap
                                                    double homeOdds = values.get(0).asDouble();
                                                    double awayOdds = values.get(1).asDouble();
                                                    
                                                    com.fasterxml.jackson.databind.node.ObjectNode hdpNode = mapper.createObjectNode();
                                                    hdpNode.put("hdp", hdp);
                                                    hdpNode.put("home", homeOdds);
                                                    hdpNode.put("away", awayOdds);
                                                    handicapsArray.add(hdpNode);
                                                } else if (type == 11) {
                                                    // Totals (Asian Over/Under)
                                                    double rawLimit = meta.get(4).asDouble();
                                                    double limit = (rawLimit >= 1000) ? (rawLimit / 10.0) : (rawLimit / 100.0);
                                                    double overOdds = values.get(0).asDouble();
                                                    double underOdds = values.get(1).asDouble();
                                                    
                                                    com.fasterxml.jackson.databind.node.ObjectNode totalNode = mapper.createObjectNode();
                                                    totalNode.put("limit", limit);
                                                    totalNode.put("over", overOdds);
                                                    totalNode.put("under", underOdds);
                                                    totalsArray.add(totalNode);
                                                } else {
                                                    // Potential Moneyline/Winner or other
                                                    if (values.size() == 2) {
                                                        moneylineNode.put("home", values.get(0).asDouble());
                                                        moneylineNode.put("away", values.get(1).asDouble());
                                                    } else if (values.size() == 3) {
                                                        moneylineNode.put("home", values.get(0).asDouble());
                                                        moneylineNode.put("draw", values.get(1).asDouble());
                                                        moneylineNode.put("away", values.get(2).asDouble());
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (moneylineNode.size() > 0) {
                                        eventNode.set("moneyline", moneylineNode);
                                    }

                                    eventsArray.add(eventNode);
                                }
                            }
                        }
                    }
                }

                log.info("Successfully fetched and transformed SBOBET odds for sport: {} — found {} leagues", 
                        sportName, leaguesArray.size());
                return transformedResponse;

            } catch (Exception e) {
                log.error("Failed to fetch SBOBET odds for sport: {} (attempt {}/3): {}", sportName, attempt, e.getMessage());
                errorTracker.recordError(e.getClass().getSimpleName() + ": " + e.getMessage());
                rotateAndWait(attempt);
            }
        }
        return null;
    }

    private void rotateAndWait(int attempt) {
        if (attempt < 3) {
            vpnManagerService.reportFailureAndRotate();
            try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}
