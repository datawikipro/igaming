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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // Standard sport IDs in Pinnacle Arcadia
    private static final Map<Integer, String> TARGET_SPORTS = Map.of(
            29, "Soccer",
            19, "Hockey",
            33, "Tennis",
            4, "Basketball",
            34, "Volleyball",
            18, "Handball",
            3, "Baseball",
            15, "Football"
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

        // 1. Fetch matchups
        JsonNode matchupsResponse = apiClient.getMatchups(sportId);
        if (matchupsResponse == null || !matchupsResponse.isArray() || matchupsResponse.isEmpty()) {
            log.warn("No matchups found for Pinnacle sport {} (ID: {})", sportName, sportId);
            return;
        }

        // 2. Fetch straight markets
        JsonNode marketsResponse = apiClient.getStraightMarkets(sportId);
        Map<String, List<JsonNode>> marketsByMatchup = new HashMap<>();
        if (marketsResponse != null && marketsResponse.isArray()) {
            for (JsonNode m : marketsResponse) {
                String matchupId = m.path("matchupId").asText();
                if (!matchupId.isEmpty()) {
                    marketsByMatchup.computeIfAbsent(matchupId, k -> new ArrayList<>()).add(m);
                }
            }
        }

        SportType sportType = sportNormalizationService.normalize(sportName);
        int pushedCount = 0;
        int unchangedCount = 0;
        int totalSaved = 0;

        for (JsonNode matchup : matchupsResponse) {
            String type = matchup.path("type").asText();
            // Accept main matchups and exclude sub-specials without parentId
            if (!"matchup".equalsIgnoreCase(type) && !matchup.path("parentId").isNull() && !matchup.path("parentId").isMissingNode()) {
                continue;
            }

            String eventId = matchup.path("id").asText();
            if (eventId.isEmpty()) continue;

            List<JsonNode> markets = marketsByMatchup.get(eventId);
            OddsUpdateRequest request = oddsMapper.mapArcadiaToOddsUpdateRequest(matchup, markets, sportName, sportType);
            if (request == null || request.getTeam1() == null || request.getTeam2() == null) {
                continue;
            }

            try {
                // Save or update match metadata locally
                MatchCache matchCache = new MatchCache();
                matchCache.setBookmaker("pinnacle");
                matchCache.setExternalId(eventId);
                matchCache.setSportName(sportName);
                matchCache.setLeagueName(request.getLeagueName());
                matchCache.setTeam1(request.getTeam1());
                matchCache.setTeam2(request.getTeam2());
                matchCache.setIsLive(request.getIsLive());
                matchCache.setStartTime(request.getStartTime());
                matchCache.setEventUrl(request.getEventUrl());

                String serializedPayload = objectMapper.writeValueAsString(request);
                String currentHash = persistenceService.computeHash(serializedPayload);

                persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);
                totalSaved++;

                if (request.getOdds() != null && !request.getOdds().isEmpty()) {
                    String cachedHash = localStateHashCache.get(eventId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(eventId, currentHash);
                        pushedCount++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("pinnacle", eventId);
                        unchangedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process Pinnacle event ID: {}: {}", eventId, e.getMessage());
            }
        }

        log.info("Pinnacle Sport {}: Saved {} matches, pushed {} odds updates, {} unchanged.",
                sportName, totalSaved, pushedCount, unchangedCount);
    }
}
