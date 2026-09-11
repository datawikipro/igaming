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
    private static final Map<String, String> SPORT_URL_SEGMENTS = Map.ofEntries(
            Map.entry("football", "football"),
            Map.entry("soccer", "football"),
            Map.entry("tennis", "tennis"),
            Map.entry("basketball", "basketball"),
            Map.entry("volleyball", "volleyball"),
            Map.entry("hockey", "ice-hockey"),
            Map.entry("icehockey", "ice-hockey"),
            Map.entry("ice-hockey", "ice-hockey"),
            Map.entry("baseball", "baseball"),
            Map.entry("cricket", "cricket"),
            Map.entry("tabletennis", "table-tennis"),
            Map.entry("table_tennis", "table-tennis"),
            Map.entry("table-tennis", "table-tennis"),
            Map.entry("handball", "handball"),
            Map.entry("esports", "e-sports"),
            Map.entry("e-sports", "e-sports"),
            Map.entry("badminton", "badminton"),
            Map.entry("rugby", "rugby"),
            Map.entry("darts", "darts"),
            Map.entry("snooker", "snooker"),
            Map.entry("futsal", "futsal"),
            Map.entry("boxing", "boxing"),
            Map.entry("mma", "mixed-martial-arts"),
            Map.entry("mixed-martial-arts", "mixed-martial-arts")
    );

    private final BrowserService browserService;
    private final SbobetConfig sbobetConfig;
    private final SbobetApiErrorTracker errorTracker;
    private final VpnManagerService vpnManagerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    public JsonNode fetchOdds(String sportName) {
        String baseUrl = sbobetConfig.getApi().getBaseUrl();
        String sportSegment = SPORT_URL_SEGMENTS.getOrDefault(sportName.toLowerCase().replace(" ", ""), sportName.toLowerCase());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        com.fasterxml.jackson.databind.node.ObjectNode transformedResponse = mapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ArrayNode leaguesArray = transformedResponse.putArray("leagues");
        java.util.Map<String, com.fasterxml.jackson.databind.node.ObjectNode> leaguesMap = new java.util.HashMap<>();
        java.util.Set<String> seenEventIds = new java.util.HashSet<>();

        String[] sections = {"today", "early", "live"};
        for (String section : sections) {
            String sectionUrl = baseUrl + "/ru-RU/euro/" + sportSegment + "/" + section;
            log.info("Fetching SBOBET sport '{}' section '{}' from {}", sportName, section, sectionUrl);
            errorTracker.recordAttempt();
            try {
                String html = browserService.navigateAndGetBody(sectionUrl, INTERCEPT_TIMEOUT_MS);
                if (html != null && !html.isEmpty() && html.contains("$P.onUpdate('od',")) {
                    parseSbobetSectionHtml(html, leaguesMap, leaguesArray, seenEventIds, mapper, "live".equalsIgnoreCase(section));
                }
            } catch (Exception e) {
                log.debug("Error fetching SBOBET section {} for {}: {}", section, sportName, e.getMessage());
            }
        }

        // Fallback to base sport page if no leagues discovered
        if (leaguesArray.isEmpty()) {
            String fallbackUrl = baseUrl + "/ru-RU/euro/" + sportSegment;
            log.info("Attempting base fallback URL for {}: {}", sportName, fallbackUrl);
            try {
                String html = browserService.navigateAndGetBody(fallbackUrl, INTERCEPT_TIMEOUT_MS);
                if (html != null && !html.isEmpty() && html.contains("$P.onUpdate('od',")) {
                    parseSbobetSectionHtml(html, leaguesMap, leaguesArray, seenEventIds, mapper, false);
                }
            } catch (Exception e) {
                log.warn("Fallback fetch failed for {}: {}", sportName, e.getMessage());
            }
        }

        log.info("Successfully fetched and transformed SBOBET odds for sport: {} — found {} leagues, {} events", 
                sportName, leaguesArray.size(), seenEventIds.size());
        return transformedResponse;
    }

    private void parseSbobetSectionHtml(String html, 
                                        java.util.Map<String, com.fasterxml.jackson.databind.node.ObjectNode> leaguesMap,
                                        com.fasterxml.jackson.databind.node.ArrayNode leaguesArray,
                                        java.util.Set<String> seenEventIds,
                                        ObjectMapper mapper,
                                        boolean isLiveDefault) {
        try {
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

            // 2. Locate the $P.onUpdate('od', ...) script block
            int startIdx = html.indexOf("$P.onUpdate('od',");
            if (startIdx == -1) return;

            int openBracketIdx = html.indexOf("[", startIdx);
            if (openBracketIdx == -1) return;

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

            if (endIdx == -1) return;

            String jsonArrayStr = html.substring(openBracketIdx, endIdx + 1);
            while (jsonArrayStr.contains(",,")) {
                jsonArrayStr = jsonArrayStr.replace(",,", ",null,");
            }
            while (jsonArrayStr.contains(", ,")) {
                jsonArrayStr = jsonArrayStr.replace(", ,", ",null,");
            }

            JsonNode rootNode = mapper.readTree(jsonArrayStr);
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
                                if (eventId.isEmpty() || seenEventIds.contains(eventId)) {
                                    continue;
                                }
                                seenEventIds.add(eventId);

                                String homeTeam = eventInfo.get(1).asText();
                                String awayTeam = eventInfo.get(2).asText();
                                String startTimeStr = eventInfo.get(5).asText();
                                
                                long startTimeMillis = 0;
                                try {
                                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                            .ofPattern("MM/dd/yyyy HH:mm");
                                    java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(startTimeStr, formatter);
                                    startTimeMillis = localDateTime.atZone(java.time.ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli();
                                } catch (Exception ex) {
                                    startTimeMillis = java.time.Instant.now().toEpochMilli() + 3600000;
                                }

                                String leagueId = eg.get(1).asText();
                                String leagueName = leagueMap.getOrDefault(leagueId, "League " + leagueId);

                                com.fasterxml.jackson.databind.node.ObjectNode leagueNode = leaguesMap.get(leagueId);
                                if (leagueNode == null) {
                                    leagueNode = mapper.createObjectNode();
                                    leagueNode.put("name", leagueName);
                                    leagueNode.putArray("events");
                                    leaguesArray.add(leagueNode);
                                    leaguesMap.put(leagueId, leagueNode);
                                }
                                com.fasterxml.jackson.databind.node.ArrayNode eventsArray = (com.fasterxml.jackson.databind.node.ArrayNode) leagueNode.get("events");

                                com.fasterxml.jackson.databind.node.ObjectNode eventNode = mapper.createObjectNode();
                                eventNode.put("id", eventId);
                                eventNode.put("home", homeTeam);
                                eventNode.put("away", awayTeam);
                                eventNode.put("startTime", startTimeMillis);
                                eventNode.put("isLive", isLiveDefault);

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
                                                double rawAwayHdp = meta.get(5).asDouble();
                                                double hdp = -rawAwayHdp;
                                                double homeOdds = values.get(0).asDouble();
                                                double awayOdds = values.get(1).asDouble();
                                                
                                                com.fasterxml.jackson.databind.node.ObjectNode hdpNode = mapper.createObjectNode();
                                                hdpNode.put("hdp", hdp);
                                                hdpNode.put("home", homeOdds);
                                                hdpNode.put("away", awayOdds);
                                                handicapsArray.add(hdpNode);
                                            } else if (type == 11) {
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
        } catch (Exception e) {
            log.warn("Error parsing SBOBET HTML block: {}", e.getMessage());
        }
    }

    private void rotateAndWait(int attempt) {
        if (attempt < 3) {
            vpnManagerService.reportFailureAndRotate();
            try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}
