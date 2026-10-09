package pro.datawiki.igaming.source.vbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.vbet.dto.VbetEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class VbetDiscoveryService {

    private final VbetApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();

    public int discoverEvents() {
        log.info("Starting Vbet event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        // 1. Live Events
        try {
            List<VbetEvent> liveEvents = apiClient.getLiveEvents();
            if (liveEvents != null && !liveEvents.isEmpty()) {
                log.info("Vbet: found {} live events", liveEvents.size());
                for (VbetEvent event : liveEvents) {
                    if (processAndSaveEvent(event, true)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Vbet live events: {}", e.getMessage());
        }

        // 2. Prematch Events
        try {
            List<VbetEvent> prematchEvents = apiClient.getPrematchEvents();
            if (prematchEvents != null && !prematchEvents.isEmpty()) {
                log.info("Vbet: found {} prematch events", prematchEvents.size());
                for (VbetEvent event : prematchEvents) {
                    if (processAndSaveEvent(event, false)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Vbet prematch events: {}", e.getMessage());
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
        }

        log.info("Vbet discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(VbetEvent event, boolean isLive) {
        if (event == null || event.getId() == null) {
            return false;
        }

        String team1 = event.getHomeTeam();
        String team2 = event.getAwayTeam();
        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = parseTeams(event.getName());
                if (parts != null && parts.length >= 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                }
            }
        }

        if (team1 == null || team2 == null) {
            return false;
        }

        String externalId = String.valueOf(event.getId());
        String sportName = event.getEnglishSportName() != null ? event.getEnglishSportName() : (event.getSportName() != null ? event.getSportName() : "Soccer");
        String leagueName = event.getEnglishChampionshipName() != null ? event.getEnglishChampionshipName() : (event.getChampionshipName() != null ? event.getChampionshipName() : "General");

        long startMs = System.currentTimeMillis();
        if (event.getDate() != null) {
            try {
                startMs = Instant.parse(event.getDate()).toEpochMilli();
            } catch (Exception ignored) {}
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
        match.setBookmaker("vbet");
        match.setEventUrl("https://sport.vbet.com/sportsbook/event/" + externalId);

        try {
            persistenceService.saveOrUpdateMatchMetadata(match, currentFootprint);
            discoveryCache.put(externalId, currentFootprint);
            return true;
        } catch (Exception e) {
            log.debug("Error saving Vbet match metadata for {}: {}", externalId, e.getMessage());
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
