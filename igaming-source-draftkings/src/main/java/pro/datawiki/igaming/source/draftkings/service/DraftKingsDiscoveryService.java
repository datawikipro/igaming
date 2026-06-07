package pro.datawiki.igaming.source.draftkings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.draftkings.config.DraftKingsConfig;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftKingsDiscoveryService {

    private final DraftKingsApiClient apiClient;
    private final MatchPersistenceService persistenceService;
    private final DraftKingsConfig config;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryTimeCache = new ConcurrentHashMap<>();

    public void discoverEvents() {
        log.debug("Starting DraftKings discovery cycle...");
        int processedCount = 0;

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
            discoveryTimeCache.clear();
        }

        for (int eventGroupId : config.getFetch().getEventGroupIds()) {
            try {
                DraftKingsEventGroupResponse response = apiClient.getEventGroup(eventGroupId);
                if (response == null || response.getEventGroup() == null || response.getEventGroup().getEvents() == null) {
                    log.warn("DraftKings returned empty event list for event group {}", eventGroupId);
                    continue;
                }

                String sportName = resolveSportName(eventGroupId);
                String leagueName = resolveLeagueName(eventGroupId);

                log.debug("DraftKings event group {} ({}): found {} events", 
                        eventGroupId, leagueName, response.getEventGroup().getEvents().size());

                for (DraftKingsEventGroupResponse.DraftKingsEvent event : response.getEventGroup().getEvents()) {
                    if (event == null || event.getEventId() == null) continue;
                    if ("Completed".equalsIgnoreCase(event.getEventStatus())) {
                        log.debug("DraftKings: Skipping completed event {}", event.getEventId());
                        continue;
                    }

                    saveOrUpdateEvent(event, sportName, leagueName);
                    processedCount++;
                }

            } catch (Exception e) {
                log.error("Failed to process DraftKings event group {}: {}", eventGroupId, e.getMessage());
            }
        }
        log.debug("DraftKings discovery completed. {} events processed.", processedCount);
    }

    private void saveOrUpdateEvent(DraftKingsEventGroupResponse.DraftKingsEvent event, String sportName, String leagueName) {
        String externalId = event.getEventId();
        
        String team1 = event.getTeamName1();
        String team2 = event.getTeamName2();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" @ ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    parts = event.getName().split(" vs ");
                    if (parts.length == 2) {
                        team1 = parts[0].trim();
                        team2 = parts[1].trim();
                    } else {
                        parts = event.getName().split(" - ");
                        if (parts.length == 2) {
                            team1 = parts[0].trim();
                            team2 = parts[1].trim();
                        }
                    }
                }
            }
        }

        if (team1 == null) team1 = "Unknown Home";
        if (team2 == null) team2 = "Unknown Away";

        long startMs = 0L;
        if (event.getStartDate() != null) {
            try {
                startMs = Instant.parse(event.getStartDate()).toEpochMilli();
            } catch (Exception e) {
                log.warn("Failed to parse event start date '{}' for event {}", event.getStartDate(), event.getEventId());
            }
        }

        String state = event.getEventStatus() != null ? event.getEventStatus() : "";
        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s", 
                startMs, team1, team2, sportName, leagueName, state);

        if (isThrottled(externalId, currentFootprint)) return;

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(startMs);
        match.setIsLive("Started".equalsIgnoreCase(state));
        match.setBookmaker("draftkings");

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            discoveryTimeCache.put(externalId, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Failed to save match metadata for DraftKings event {}: {}", externalId, e.getMessage());
        }
    }

    private boolean isThrottled(String externalId, String currentFootprint) {
        long now = System.currentTimeMillis();
        Long lastUpdate = discoveryTimeCache.get(externalId);
        return currentFootprint.equals(discoveryCache.get(externalId)) 
                && lastUpdate != null 
                && (now - lastUpdate) < 3 * 60 * 1000;
    }

    private String resolveSportName(int eventGroupId) {
        return switch (eventGroupId) {
            case 88670846 -> "Basketball";
            case 88670847 -> "American Football";
            case 88670848 -> "Baseball";
            case 88670849 -> "Ice Hockey";
            case 88671587 -> "MMA";
            case 88671238 -> "Soccer";
            default -> "Soccer";
        };
    }

    private String resolveLeagueName(int eventGroupId) {
        return switch (eventGroupId) {
            case 88670846 -> "NBA";
            case 88670847 -> "NFL";
            case 88670848 -> "MLB";
            case 88670849 -> "NHL";
            case 88671587 -> "UFC";
            case 88671238 -> "Premier League";
            default -> "Unknown League " + eventGroupId;
        };
    }
}
