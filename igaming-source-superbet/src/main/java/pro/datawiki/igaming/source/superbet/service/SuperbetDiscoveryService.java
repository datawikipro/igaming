package pro.datawiki.igaming.source.superbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.superbet.dto.SuperbetEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperbetDiscoveryService {

    private final SuperbetApiClient apiClient;
    private final MatchPersistenceService persistenceService;
    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Superbet event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Fetch live events
        try {
            List<SuperbetEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Superbet: found {} live events", liveEvents.size());
                for (SuperbetEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Superbet live events: {}", e.getMessage());
        }

        // 2. Fetch prematch events
        try {
            List<SuperbetEvent> prematchEvents = apiClient.getPrematchEvents();
            if (prematchEvents != null && !prematchEvents.isEmpty()) {
                log.info("Superbet: found {} prematch events", prematchEvents.size());
                for (SuperbetEvent event : prematchEvents) {
                    if (processAndSaveEvent(event, false)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Superbet prematch events: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Superbet discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(SuperbetEvent event, boolean isLiveHint) {
        if (event == null || event.getResolvedId() == null) {
            return false;
        }

        String eventName = event.getResolvedName();
        if (eventName == null || eventName.isBlank()) {
            return false;
        }

        String[] teams = parseTeams(eventName);
        if (teams == null || teams.length < 2) {
            if (event.getHomeTeam() != null && event.getAwayTeam() != null) {
                teams = new String[]{event.getHomeTeam(), event.getAwayTeam()};
            } else {
                return false;
            }
        }

        String externalId = String.valueOf(event.getResolvedId());
        String team1 = teams[0].trim();
        String team2 = teams[1].trim();
        String sportName = event.getResolvedSportName();
        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";

        long startMs = System.currentTimeMillis();
        Long resolvedDate = event.getResolvedMatchDate();
        if (resolvedDate != null && resolvedDate > 0) {
            startMs = resolvedDate;
        } else if (event.getStartDate() != null) {
            try {
                startMs = Instant.parse(event.getStartDate()).toEpochMilli();
            } catch (Exception ignored) {
            }
        }

        boolean isLive = isLiveHint || event.isMatchLive();

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
        match.setIsLive(isLive);
        match.setBookmaker("superbet");
        match.setEventUrl("https://superbet.pl/zaklady-bukmacherskie/eveniment/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Superbet match metadata for {}: {}", externalId, e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String eventName) {
        if (eventName.contains("·")) {
            return eventName.split("·", 2);
        } else if (eventName.contains("\u00b7")) {
            return eventName.split("\u00b7", 2);
        } else if (eventName.contains(" - ")) {
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
