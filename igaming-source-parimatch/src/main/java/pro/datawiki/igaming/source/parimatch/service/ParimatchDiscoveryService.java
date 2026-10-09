package pro.datawiki.igaming.source.parimatch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchEvent;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchSport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParimatchDiscoveryService {

    private final ParimatchApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Parimatch event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Live Events
        try {
            List<ParimatchEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Parimatch: found {} live events", liveEvents.size());
                for (ParimatchEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Parimatch live events: {}", e.getMessage());
        }

        // 2. Prematch Events
        try {
            List<ParimatchSport> sports = apiClient.getSports();
            if (sports != null && !sports.isEmpty()) {
                log.info("Parimatch: found {} sports categories", sports.size());
                for (ParimatchSport sport : sports) {
                    if (sport.getId() == null) continue;
                    try {
                        List<ParimatchEvent> events = apiClient.getPrematchEvents(sport.getId());
                        if (events != null && !events.isEmpty()) {
                            for (ParimatchEvent event : events) {
                                if (event.getSportName() == null) {
                                    event.setSportName(sport.getName());
                                }
                                if (processAndSaveEvent(event, false)) {
                                    totalDiscovered.incrementAndGet();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed fetching Parimatch events for sport {}: {}", sport.getName(), e.getMessage());
                    }
                }
            } else {
                // Fallback to general prematch events fetch
                List<ParimatchEvent> events = apiClient.getPrematchEvents(null);
                if (events != null && !events.isEmpty()) {
                    for (ParimatchEvent event : events) {
                        if (processAndSaveEvent(event, false)) {
                            totalDiscovered.incrementAndGet();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Parimatch prematch sports: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Parimatch discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(ParimatchEvent event, boolean isLive) {
        if (event == null || event.getId() == null || event.getName() == null) {
            return false;
        }

        String eventName = event.getName().trim();
        String[] teams = parseTeams(eventName);
        if (teams == null || teams.length < 2) {
            return false;
        }

        String externalId = event.getId();
        String team1 = teams[0].trim();
        String team2 = teams[1].trim();
        String sportName = event.getSportName() != null ? event.getSportName() : "Soccer";
        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";

        long startMs = 0L;
        if (event.getStartDate() != null && event.getStartDate() > 0) {
            startMs = event.getStartDate();
        } else {
            startMs = System.currentTimeMillis();
        }

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        String cachedFootprint = discoveryCache.get(externalId);
        if (cachedFootprint != null && cachedFootprint.equals(currentFootprint)) {
            return false;
        }

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(startMs);
        match.setIsLive(isLive || Boolean.TRUE.equals(event.getIsLive()));
        match.setBookmaker("parimatch");
        match.setEventUrl("https://parimatch.com/en/events/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Parimatch match metadata for {}: {}", externalId, e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String eventName) {
        if (eventName.contains(" - ")) {
            return eventName.split(" - ", 2);
        } else if (eventName.contains(" vs. ")) {
            return eventName.split(" vs\\. ", 2);
        } else if (eventName.contains(" vs ")) {
            return eventName.split(" vs ", 2);
        } else if (eventName.contains(" v ")) {
            return eventName.split(" v ", 2);
        }
        return null;
    }
}
