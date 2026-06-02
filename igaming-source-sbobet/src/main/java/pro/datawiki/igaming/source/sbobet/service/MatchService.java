package pro.datawiki.igaming.source.sbobet.service;

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

    private final SbobetApiClient apiClient;
    private final SbobetOddsMapper oddsMapper;
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
        log.info("Starting SBOBET line scraping for active sports...");
        for (Map.Entry<String, String> sportEntry : TARGET_SPORTS.entrySet()) {
            String sportKey = sportEntry.getKey();
            String sportName = sportEntry.getValue();
            try {
                scrapeSport(sportKey, sportName);
            } catch (Exception e) {
                log.error("Failed to scrape SBOBET sport {} (Key: {}): {}", sportName, sportKey, e.getMessage(), e);
            }
        }
        log.info("SBOBET line scraping completed.");
    }

    private void scrapeSport(String sportKey, String sportName) {
        log.debug("Scraping SBOBET sport: {} (Key: {})", sportName, sportKey);

        JsonNode responseNode = apiClient.fetchOdds(sportKey);
        if (responseNode == null || !responseNode.has("leagues")) {
            log.warn("No leagues data found in SBOBET response for sport: {}", sportName);
            return;
        }

        int pushedCount = 0;
        int unchangedCount = 0;
        SportType sportType = sportNormalizationService.normalize(sportName);

        for (JsonNode leagueNode : responseNode.path("leagues")) {
            String leagueName = leagueNode.path("name").asText("Unknown League");

            for (JsonNode eventNode : leagueNode.path("events")) {
                String eventId = eventNode.path("id").asText();
                if (eventId.isEmpty()) {
                    continue;
                }

                try {
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(eventNode, sportName, sportType, leagueName);
                    if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                        continue;
                    }

                    // Store match details locally for tracking status and persistence
                    MatchCache matchCache = new MatchCache();
                    matchCache.setBookmaker("sbobet");
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

                    // De-duplicate updates: only push to Kafka if the odds have changed
                    String cachedHash = localStateHashCache.get(eventId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(eventId, currentHash);
                        pushedCount++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("sbobet", eventId);
                        unchangedCount++;
                    }

                } catch (Exception e) {
                    log.error("Failed to process SBOBET event ID {}: {}", eventId, e.getMessage());
                }
            }
        }

        log.info("SBOBET Sport {}: Pushed {} updates, {} unchanged.", sportName, pushedCount, unchangedCount);
    }
}
