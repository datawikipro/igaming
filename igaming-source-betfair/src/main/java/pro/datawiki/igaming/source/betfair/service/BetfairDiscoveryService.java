package pro.datawiki.igaming.source.betfair.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairEvent;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairEventsResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetfairDiscoveryService {

    private final BetfairApiClient betfairApiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryTimeCache = new ConcurrentHashMap<>();

    public void discoverEvents() {
        log.debug("Starting Betfair event discovery cycle...");
        BetfairEventsResponse response = betfairApiClient.getEvents();

        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
            log.warn("Betfair API returned empty event list.");
            return;
        }

        log.debug("Betfair: found {} events", response.getEvents().size());

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
            discoveryTimeCache.clear();
        }

        int processed = 0;
        for (BetfairEvent event : response.getEvents()) {
            if (event == null || event.getId() == null) continue;

            try {
                saveOrUpdateEvent(event);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process Betfair event {}: {}", event.getId(), e.getMessage());
            }
        }
        log.debug("Betfair discovery completed. {} events processed.", processed);
    }

    private void saveOrUpdateEvent(BetfairEvent event) {
        String externalId = event.getId();
        String sportName = event.getSport() != null ? event.getSport() : "Soccer";
        String leagueName = event.getLeague() != null ? event.getLeague() : "Unknown League";
        String team1 = event.getHomeTeam() != null ? event.getHomeTeam() : "Unknown Home";
        String team2 = event.getAwayTeam() != null ? event.getAwayTeam() : "Unknown Away";
        long startMs = event.getStartTime();

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s", 
                startMs, team1, team2, sportName, leagueName, event.isLive());

        if (isThrottled(externalId, currentFootprint)) return;

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(startMs);
        match.setIsLive(event.isLive());
        match.setBookmaker("betfair");

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
