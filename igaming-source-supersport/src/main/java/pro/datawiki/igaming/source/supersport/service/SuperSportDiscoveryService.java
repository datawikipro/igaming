package pro.datawiki.igaming.source.supersport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.supersport.dto.SuperSportEventDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperSportDiscoveryService {

    private final SuperSportApiClient apiClient;
    private final MatchPersistenceService persistenceService;
    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting SuperSport (supersport.hr) event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<SuperSportEventDto> events = apiClient.fetchUpcomingEvents();
            if (events != null && !events.isEmpty()) {
                for (SuperSportEventDto event : events) {
                    if (processAndSaveEvent(event, false)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering upcoming events for SuperSport (supersport.hr): {}", e.getMessage());
        }

        try {
            List<SuperSportEventDto> liveEvents = apiClient.fetchLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                for (SuperSportEventDto event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering live events for SuperSport (supersport.hr): {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("SuperSport (supersport.hr) discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(SuperSportEventDto event, boolean isLive) {
        if (event == null || event.getId() == null) {
            return false;
        }

        String externalId = event.getId();
        String team1 = event.getHomeTeam() != null ? event.getHomeTeam().trim() : "";
        String team2 = event.getAwayTeam() != null ? event.getAwayTeam().trim() : "";
        if (team1.isEmpty() || team2.isEmpty()) {
            return false;
        }

        String sportName = event.getSport() != null ? event.getSport() : "FOOTBALL";
        String leagueName = event.getLeague() != null ? event.getLeague() : "General";
        long startMs = event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis();

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        String cachedFootprint = discoveryCache.get(externalId);
        if (cachedFootprint != null && cachedFootprint.equals(currentFootprint)) {
            return false;
        }

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setBookmaker("supersport");
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setIsLive(isLive || Boolean.TRUE.equals(event.getIsLive()));
        match.setStartTime(startMs);
        match.setEventUrl("https://www.supersport.hr/event/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error persisting SuperSport match {}: {}", externalId, e.getMessage());
            return false;
        }
    }
}
