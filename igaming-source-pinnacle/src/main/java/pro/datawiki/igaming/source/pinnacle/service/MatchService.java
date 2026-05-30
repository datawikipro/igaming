package pro.datawiki.igaming.source.pinnacle.service;

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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final PinnacleApiClient apiClient;
    private final PinnacleOddsMapper oddsMapper;
    private final MatchPersistenceService persistenceService;
    private final AggregatorClient aggregatorClient;
    private final SportNormalizationService sportNormalizationService;
    private final ObjectMapper objectMapper;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    // Standard sport IDs in Pinnacle
    private static final Map<Integer, String> TARGET_SPORTS = Map.of(
            29, "Soccer",
            4, "Basketball",
            33, "Tennis",
            19, "Ice Hockey",
            34, "Volleyball"
    );

    public void scrapeAllSports() {
        log.info("Starting Pinnacle line scraping for active sports...");
        for (Map.Entry<Integer, String> sportEntry : TARGET_SPORTS.entrySet()) {
            int sportId = sportEntry.getKey();
            String sportName = sportEntry.getValue();
            try {
                scrapeSport(sportId, sportName);
            } catch (Exception e) {
                log.error("Failed to scrape Pinnacle sport {} (ID: {}): {}", sportName, sportId, e.getMessage(), e);
            }
        }
        log.info("Pinnacle line scraping completed.");
    }

    private void scrapeSport(int sportId, String sportName) {
        log.debug("Scraping Pinnacle sport: {} (ID: {})", sportName, sportId);

        // 1. Fetch fixtures
        JsonNode fixturesResponse = apiClient.getFixtures(sportId);
        if (fixturesResponse == null || !fixturesResponse.has("league")) {
            log.warn("No fixtures found for Pinnacle sport {} (ID: {})", sportName, sportId);
            return;
        }

        // 2. Fetch odds
        JsonNode oddsResponse = apiClient.getOdds(sportId);
        if (oddsResponse == null || !oddsResponse.has("leagues")) {
            log.warn("No odds found for Pinnacle sport {} (ID: {})", sportName, sportId);
            return;
        }

        // 3. Build a map of league IDs to league names
        Map<String, String> leagueNames = new HashMap<>();
        for (JsonNode leagueNode : fixturesResponse.path("league")) {
            String id = leagueNode.path("id").asText();
            String name = leagueNode.path("name").asText();
            leagueNames.put(id, name);
        }

        // 4. Map active fixtures in memory
        Map<String, JsonNode> fixtureEvents = new HashMap<>();
        for (JsonNode leagueNode : fixturesResponse.path("league")) {
            String leagueId = leagueNode.path("id").asText();
            for (JsonNode eventNode : leagueNode.path("events")) {
                String eventId = eventNode.path("id").asText();
                fixtureEvents.put(eventId, eventNode);
            }
        }

        // 5. Parse odds and join with fixtures
        int pushedCount = 0;
        int unchangedCount = 0;

        SportType sportType = sportNormalizationService.normalize(sportName);

        for (JsonNode leagueNode : oddsResponse.path("leagues")) {
            String leagueId = leagueNode.path("id").asText();
            String leagueName = leagueNames.getOrDefault(leagueId, "Unknown League");

            for (JsonNode eventOdds : leagueNode.path("events")) {
                String eventId = eventOdds.path("id").asText();
                JsonNode fixture = fixtureEvents.get(eventId);

                if (fixture == null) {
                    continue; // Skip if no fixture metadata exists for this event
                }

                try {
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(fixture, eventOdds, sportName, sportType, leagueName);
                    if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                        continue;
                    }

                    // Save or update match metadata locally
                    MatchCache matchCache = new MatchCache();
                    matchCache.setBookmaker("pinnacle");
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

                    // Check if odds have actually changed before pushing to Kafka
                    String cachedHash = localStateHashCache.get(eventId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(eventId, currentHash);
                        pushedCount++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("pinnacle", eventId);
                        unchangedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to process Pinnacle event ID: {}: {}", eventId, e.getMessage());
                }
            }
        }

        log.info("Pinnacle Sport {}: Pushed {} updates, {} unchanged.", sportName, pushedCount, unchangedCount);
    }
}
