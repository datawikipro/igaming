package pro.datawiki.igaming.source.bwin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainFixture;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainEventsResponse;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainSportOffer;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BwinDiscoveryService {

    private final BwinApiClient bwinApiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryTimeCache = new ConcurrentHashMap<>();

    public void discoverEvents() {
        log.debug("Starting Bwin (Entain) discovery cycle...");
        EntainEventsResponse response = bwinApiClient.getEvents();

        if (response == null || response.getSportsOffer() == null || response.getSportsOffer().isEmpty()) {
            log.warn("Bwin API returned empty event list.");
            return;
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
            discoveryTimeCache.clear();
        }

        int processed = 0;
        for (EntainSportOffer sportOffer : response.getSportsOffer()) {
            if (sportOffer.getFixtures() == null) continue;
            
            String sportName = "Unknown";
            if (sportOffer.getName() != null && sportOffer.getName().getValue() != null) {
                sportName = sportOffer.getName().getValue();
            }

            for (EntainFixture fixture : sportOffer.getFixtures()) {
                if (fixture == null || fixture.getSourceId() == null) continue;

                try {
                    saveOrUpdateEvent(fixture, sportName);
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to process Bwin event {}: {}", fixture.getSourceId(), e.getMessage());
                }
            }
        }
        log.debug("Bwin discovery completed. {} events processed.", processed);
    }

    private void saveOrUpdateEvent(EntainFixture event, String sportName) {
        String externalId = String.valueOf(event.getSourceId());

        String leagueName = "Unknown League";
        if (event.getTournamentName() != null && event.getTournamentName().getValue() != null) {
            leagueName = event.getTournamentName().getValue();
        } else if (event.getStageName() != null && event.getStageName().getValue() != null) {
            leagueName = event.getStageName().getValue();
        }

        String team1 = "Unknown Home";
        String team2 = "Unknown Away";
        if (event.getName() != null && event.getName().getValue() != null) {
            String eventName = event.getName().getValue();
            String[] parts = eventName.split(" - ");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = eventName.split(" vs ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    team1 = eventName;
                }
            }
        }

        long startMs = 0L;
        if (event.getStartDate() != null) {
            try {
                startMs = Instant.parse(event.getStartDate()).toEpochMilli();
            } catch (Exception e) {
                log.warn("Failed to parse event start time '{}' for event {}", event.getStartDate(), event.getSourceId());
            }
        }

        String currentFootprint = String.format("%s|%s|%s|%s|%s", 
                startMs, team1, team2, sportName, leagueName);

        if (isThrottled(externalId, currentFootprint)) return;

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(startMs);
        match.setIsLive(true); // Highlights are live
        match.setBookmaker("bwin");

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            
            discoveryCache.put(externalId, currentFootprint);
            discoveryTimeCache.put(externalId, System.currentTimeMillis());
        } catch (Exception e) {
            if (isOptimisticLockException(e)) {
                log.debug("Optimistic locking conflict while saving match metadata for event {}: {}", externalId, e.getMessage());
            } else {
                log.error("Failed to save match metadata for event {}: {}", externalId, e.getMessage());
            }
        }
    }

    private boolean isThrottled(String externalId, String currentFootprint) {
        long now = System.currentTimeMillis();
        Long lastUpdate = discoveryTimeCache.get(externalId);
        return currentFootprint.equals(discoveryCache.get(externalId)) 
                && lastUpdate != null 
                && (now - lastUpdate) < 3 * 60 * 1000;
    }

    private boolean isOptimisticLockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.contains("OptimisticLockingFailureException") 
                    || name.contains("OptimisticLockException") 
                    || name.contains("StaleObjectStateException") 
                    || (cause.getMessage() != null && cause.getMessage().contains("Row was updated or deleted by another transaction"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
