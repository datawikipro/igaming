package pro.datawiki.igaming.source.dafabet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final DafabetApiClient apiClient;
    private final DafabetOddsMapper oddsMapper;
    private final MatchPersistenceService persistenceService;
    private final AggregatorClient aggregatorClient;
    private final SportNormalizationService sportNormalizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    private static final Map<String, String> TARGET_SPORTS = Map.of(
            "Soccer", "Football",
            "Basketball", "Basketball",
            "Tennis", "Tennis",
            "IceHockey", "Ice Hockey",
            "Volleyball", "Volleyball"
    );

    public void scrapeAllSports() {
        log.info("Starting Dafabet line scraping for active sports...");
        for (Map.Entry<String, String> sportEntry : TARGET_SPORTS.entrySet()) {
            String sportKey = sportEntry.getKey();
            String sportName = sportEntry.getValue();
            try {
                scrapeSport(sportKey, sportName);
            } catch (Exception e) {
                log.error("Failed to scrape Dafabet sport {} (Key: {}): {}", sportName, sportKey, e.getMessage(), e);
            }
        }
        log.info("Dafabet line scraping completed.");
    }

    private void scrapeSport(String sportKey, String sportName) {
        log.debug("Scraping Dafabet sport: {} (Key: {})", sportName, sportKey);

        JsonNode responseNode = apiClient.fetchOdds(sportKey);
        if (responseNode == null) {
            log.warn("No response data found for Dafabet sport: {}", sportName);
            return;
        }

        // Support various JSON nesting formats (flat lists of events, or group by leagues)
        JsonNode eventsNode = responseNode.path("events");
        JsonNode leaguesNode = responseNode.path("leagues");

        int pushedCount = 0;
        int unchangedCount = 0;
        SportType sportType = sportNormalizationService.normalize(sportName);

        if (leaguesNode.isArray()) {
            for (JsonNode leagueNode : leaguesNode) {
                String leagueName = leagueNode.path("name").asText("Unknown League");
                JsonNode leagueEvents = leagueNode.path("events");
                if (leagueEvents.isArray()) {
                    for (JsonNode eventNode : leagueEvents) {
                        if (processEvent(eventNode, sportName, sportType, leagueName)) {
                            pushedCount++;
                        } else {
                            unchangedCount++;
                        }
                    }
                }
            }
        } else if (eventsNode.isArray()) {
            for (JsonNode eventNode : eventsNode) {
                String leagueName = eventNode.path("leagueName").asText("Unknown League");
                if (processEvent(eventNode, sportName, sportType, leagueName)) {
                    pushedCount++;
                } else {
                    unchangedCount++;
                }
            }
        } else {
            // Check if root itself is a list of events/leagues
            if (responseNode.isArray()) {
                for (JsonNode node : responseNode) {
                    String leagueName = node.path("leagueName").asText("Unknown League");
                    if (node.has("events") && node.get("events").isArray()) {
                        for (JsonNode eventNode : node.get("events")) {
                            if (processEvent(eventNode, sportName, sportType, leagueName)) {
                                pushedCount++;
                            } else {
                                unchangedCount++;
                            }
                        }
                    } else {
                        if (processEvent(node, sportName, sportType, leagueName)) {
                            pushedCount++;
                        } else {
                            unchangedCount++;
                        }
                    }
                }
            } else {
                log.warn("Unknown JSON payload structure from Dafabet for sport: {}. Keys: {}", sportName, getRootKeys(responseNode));
            }
        }

        log.info("Dafabet Sport {}: Pushed {} updates, {} unchanged.", sportName, pushedCount, unchangedCount);
    }

    private String getRootKeys(JsonNode node) {
        if (node == null) return "null";
        if (node.isObject()) {
            java.util.List<String> keys = new java.util.ArrayList<>();
            node.fieldNames().forEachRemaining(keys::add);
            return keys.toString();
        }
        return node.getNodeType().toString();
    }

    private boolean processEvent(JsonNode eventNode, String sportName, SportType sportType, String leagueName) {
        String eventId = eventNode.path("id").asText("");
        if (eventId.isEmpty()) {
            eventId = eventNode.path("eventId").asText("");
        }
        if (eventId.isEmpty()) {
            return false;
        }

        try {
            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(eventNode, sportName, sportType, leagueName);
            if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                return false;
            }

            // Build match cache metadata
            MatchCache matchCache = new MatchCache();
            matchCache.setBookmaker("dafabet");
            matchCache.setExternalId(eventId);
            matchCache.setSportName(sportName);
            matchCache.setLeagueName(leagueName);
            matchCache.setTeam1(request.getTeam1());
            matchCache.setTeam2(request.getTeam2());
            matchCache.setIsLive(request.getIsLive());
            matchCache.setStartTime(request.getStartTime());
            matchCache.setEventUrl(request.getEventUrl());

            String serializedPayload = objectMapper.writeValueAsString(request);
            String currentHash = persistenceService.computeHash(serializedPayload);

            persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);

            // Deduplicate odds pushes
            String cachedHash = localStateHashCache.get(eventId);
            if (cachedHash == null || !cachedHash.equals(currentHash)) {
                aggregatorClient.pushOddsUpdate(request);
                localStateHashCache.put(eventId, currentHash);
                return true;
            } else {
                aggregatorClient.reportUnchangedOdds("dafabet", eventId);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to process Dafabet event ID {}: {}", eventId, e.getMessage());
            return false;
        }
    }
}
