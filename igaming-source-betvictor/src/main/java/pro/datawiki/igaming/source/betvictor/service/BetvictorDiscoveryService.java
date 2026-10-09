package pro.datawiki.igaming.source.betvictor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorEvent;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetvictorDiscoveryService {

    private final BetvictorApiClient apiClient;
    private final MatchPersistenceService persistenceService;
    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Betvictor event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Fetch live events
        try {
            List<BetvictorEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Betvictor: found {} live events", liveEvents.size());
                for (BetvictorEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Betvictor live events: {}", e.getMessage());
        }

        // 2. Fetch prematch events
        try {
            List<BetvictorEvent> prematchEvents = apiClient.getPrematchEvents();
            if (prematchEvents != null && !prematchEvents.isEmpty()) {
                log.info("Betvictor: found {} prematch events", prematchEvents.size());
                for (BetvictorEvent event : prematchEvents) {
                    if (processAndSaveEvent(event, false)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Betvictor prematch events: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Betvictor discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(BetvictorEvent event, boolean isLive) {
        if (event == null || event.getResolvedId() == null) {
            return false;
        }

        String eventName = event.getResolvedName();
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
        String sportName = event.getSportName() != null ? event.getSportName() : "Soccer";
        String leagueName = event.getResolvedLeague();

        long startMs = System.currentTimeMillis();
        if (event.getStartTimeEpoch() != null && event.getStartTimeEpoch() > 0) {
            startMs = event.getStartTimeEpoch();
        } else if (event.getStartTime() != null) {
            try {
                startMs = Instant.parse(event.getStartTime()).toEpochMilli();
            } catch (Exception ignored) {
            }
        }

        String currentFootprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        String cachedFootprint = discoveryCache.get(externalId);
        if (cachedFootprint != null && cachedFootprint.equals(currentFootprint)) {
            return false;
        }

        MatchCache match = new MatchCache();
        match.setExternalId(externalId);
        match.setBookmaker("betvictor");
        match.setSportName(sportName);
        match.setLeagueName(leagueName);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setIsLive(isLive);
        match.setStartTime(startMs);
        match.setEventUrl("https://www.betvictor.com/en-gb/sports/events/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error persisting Betvictor match {}: {}", externalId, e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String name) {
        if (name == null || name.isBlank()) return null;
        String[] parts = name.split("(?i)\\s+(?:vs\\.?|v\\.?|—|-)\\s+");
        if (parts.length >= 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return null;
    }
}
