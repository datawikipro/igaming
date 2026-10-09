package pro.datawiki.igaming.source.mozzartbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class MozzartDiscoveryService {

    private final MozzartApiClient apiClient;
    private final MatchPersistenceService persistenceService;
    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Mozzart Bet event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Fetch live events
        try {
            List<MozzartEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Mozzart Bet: found {} live events", liveEvents.size());
                for (MozzartEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Mozzart Bet live events: {}", e.getMessage());
        }

        // 2. Fetch prematch events
        try {
            List<MozzartEvent> prematchEvents = apiClient.getPrematchEvents();
            if (prematchEvents != null && !prematchEvents.isEmpty()) {
                log.info("Mozzart Bet: found {} prematch events", prematchEvents.size());
                for (MozzartEvent event : prematchEvents) {
                    if (processAndSaveEvent(event, false)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Mozzart Bet prematch events: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Mozzart Bet discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(MozzartEvent event, boolean isLive) {
        if (event == null || event.getResolvedId() == null) {
            return false;
        }

        String eventName = event.getResolvedName();
        if (eventName == null || eventName.isBlank()) {
            return false;
        }

        String[] teams = parseTeams(eventName);
        if (teams == null || teams.length < 2) {
            if (event.getHome() != null && event.getVisitor() != null) {
                teams = new String[]{event.getHome(), event.getVisitor()};
            } else if (event.getHomeTeam() != null && event.getAwayTeam() != null) {
                teams = new String[]{event.getHomeTeam(), event.getAwayTeam()};
            } else {
                return false;
            }
        }

        String externalId = String.valueOf(event.getResolvedId());
        String team1 = teams[0].trim();
        String team2 = teams[1].trim();
        String sportName = event.getSportName() != null ? event.getSportName() : "Football";
        String leagueName = event.getCompetitionName() != null ? event.getCompetitionName() : "General";

        long startMs = event.getResolvedStartTime();

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
        match.setIsLive(isLive || Boolean.TRUE.equals(event.getIsLive()) || Boolean.TRUE.equals(event.getLive()));
        match.setBookmaker("mozzartbet");
        match.setEventUrl("https://www.mozzartbet.com/sr#/match/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Mozzart Bet match metadata for {}: {}", externalId, e.getMessage());
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
