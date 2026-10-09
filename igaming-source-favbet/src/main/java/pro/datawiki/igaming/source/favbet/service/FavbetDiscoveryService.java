package pro.datawiki.igaming.source.favbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.favbet.dto.FavbetEvent;
import pro.datawiki.igaming.source.favbet.dto.FavbetSport;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavbetDiscoveryService {

    private final FavbetApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Favbet event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Fetch live events
        try {
            List<FavbetEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Favbet: found {} live events", liveEvents.size());
                for (FavbetEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Favbet live events: {}", e.getMessage());
        }

        // 2. Fetch sports and prematch events
        try {
            List<FavbetSport> sports = apiClient.getSports();
            if (sports != null && !sports.isEmpty()) {
                log.info("Favbet: found {} sports categories", sports.size());
                for (FavbetSport sport : sports) {
                    if (sport.getSportId() == null) continue;
                    try {
                        List<FavbetEvent> events = apiClient.getEventsBySport(sport.getSportId());
                        if (events != null && !events.isEmpty()) {
                            for (FavbetEvent event : events) {
                                if (event.getSportName() == null) {
                                    event.setSportName(sport.getSportName());
                                }
                                if (processAndSaveEvent(event, false)) {
                                    totalDiscovered.incrementAndGet();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed fetching Favbet events for sport {}: {}", sport.getSportName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Favbet prematch sports: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Favbet discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(FavbetEvent event, boolean isLive) {
        if (event == null || event.getEventId() == null || event.getEventName() == null) {
            return false;
        }

        String eventName = event.getEventName().trim();
        String[] teams = parseTeams(eventName);
        if (teams == null || teams.length < 2) {
            return false;
        }

        String externalId = String.valueOf(event.getEventId());
        String team1 = teams[0].trim();
        String team2 = teams[1].trim();
        String sportName = event.getSportName() != null ? event.getSportName() : "Soccer";
        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";

        long startMs = 0L;
        if (event.getEventDt() != null && event.getEventDt() > 0) {
            startMs = event.getEventDt() * 1000L;
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
        match.setBookmaker("favbet");
        match.setEventUrl("https://www.favbet.com/en/sportsbook/event/" + externalId + "/");

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Favbet match metadata for {}: {}", externalId, e.getMessage());
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
