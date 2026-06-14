package pro.datawiki.igaming.source.betsson.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.betsson.dto.kambi.KambiEventsResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetssonDiscoveryService {

    private final BetssonApiClient betssonApiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryTimeCache = new ConcurrentHashMap<>();

    public void discoverEvents() {
        log.debug("Starting Betsson (Kambi) discovery cycle...");
        KambiEventsResponse response = betssonApiClient.getEvents();

        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
            log.warn("Betsson API returned empty event list.");
            return;
        }

        log.debug("Betsson (Kambi): found {} events in list view", response.getEvents().size());

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
            discoveryTimeCache.clear();
        }

        int processed = 0;
        for (KambiEventsResponse.KambiEventWrapper wrapper : response.getEvents()) {
            KambiEvent event = wrapper.getEvent();
            if (event == null || event.getId() == null) continue;

            try {
                saveOrUpdateEvent(event);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process Betsson event {}: {}", event.getId(), e.getMessage());
            }
        }
        log.debug("Betsson discovery completed. {} events processed.", processed);
    }

    private void saveOrUpdateEvent(KambiEvent event) {
        String externalId = String.valueOf(event.getId());

        String sportName = "Unknown";
        if (event.getPath() != null && !event.getPath().isEmpty()) {
            sportName = event.getPath().get(0).getName();
        }

        String leagueName = event.getGroup();
        if (leagueName == null && event.getPath() != null && event.getPath().size() > 1) {
            leagueName = event.getPath().get(event.getPath().size() - 1).getName();
        }
        if (leagueName == null) {
            leagueName = "Unknown League";
        }

        String team1 = event.getHomeName();
        String team2 = event.getAwayName();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" - ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    parts = event.getName().split(" vs ");
                    if (parts.length == 2) {
                        team1 = parts[0].trim();
                        team2 = parts[1].trim();
                    }
                }
            }
        }

        if (team1 == null) team1 = "Unknown Home";
        if (team2 == null) team2 = "Unknown Away";

        long startMs = 0L;
        if (event.getStart() != null) {
            try {
                startMs = Instant.parse(event.getStart()).toEpochMilli();
            } catch (Exception e) {
                log.warn("Failed to parse event start time '{}' for event {}", event.getStart(), event.getId());
            }
        }

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s", 
                startMs, team1, team2, sportName, leagueName, event.getState() != null ? event.getState() : "");

        if (isThrottled(externalId, currentFootprint)) return;

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(startMs);
        match.setIsLive("STARTED".equalsIgnoreCase(event.getState()));
        match.setBookmaker("betsson");

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
