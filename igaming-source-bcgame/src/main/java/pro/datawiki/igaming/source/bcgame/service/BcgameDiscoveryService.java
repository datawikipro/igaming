package pro.datawiki.igaming.source.bcgame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BcgameDiscoveryService {

    private final BcgameApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    private final Map<String, String> discoveryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryTimeCache = new ConcurrentHashMap<>();

    private static final long THROTTLE_DURATION_MS = 3 * 60 * 1000; // 3 minutes

    public int discoverEvents() {
        log.info("Starting BC.Game events discovery...");
        List<BcgameSportDto> sports = apiClient.getSports();
        if (sports == null || sports.isEmpty()) {
            log.warn("BC.Game sports catalog is empty.");
            return 0;
        }

        if (discoveryCache.size() > 50000) {
            discoveryCache.clear();
            discoveryTimeCache.clear();
        }

        int totalSaved = 0;
        for (BcgameSportDto sport : sports) {
            if (sport == null || sport.getId() == null) continue;
            try {
                List<BcgameEventDto> events = apiClient.getEvents(sport.getId());
                if (events == null || events.isEmpty()) {
                    continue;
                }
                log.info("Discovered {} events for sport '{}' ({})", events.size(), sport.getName(), sport.getId());
                for (BcgameEventDto event : events) {
                    if (event == null) continue;
                    if (event.getSportName() == null && sport.getName() != null) {
                        event.setSportName(sport.getName());
                    }
                    if (processAndSaveEvent(event)) {
                        totalSaved++;
                    }
                }
            } catch (Exception e) {
                log.error("Error discovering BC.Game events for sport {}: {}", sport.getName(), e.getMessage());
            }
        }

        log.info("BC.Game discovery completed. Total matches processed/saved: {}", totalSaved);
        return totalSaved;
    }

    public boolean processAndSaveEvent(BcgameEventDto event) {
        if (event == null || event.getId() == null || event.getId().isBlank()) {
            return false;
        }

        String externalId = event.getId().trim();
        String home = event.getHomeTeam();
        String away = event.getAwayTeam();

        if ((home == null || away == null || home.isBlank() || away.isBlank()) && event.getName() != null) {
            String name = event.getName();
            if (name.contains(" vs ")) {
                String[] parts = name.split(" vs ", 2);
                home = parts[0].trim();
                away = parts[1].trim();
            } else if (name.contains(" - ")) {
                String[] parts = name.split(" - ", 2);
                home = parts[0].trim();
                away = parts[1].trim();
            } else if (name.contains(" v ")) {
                String[] parts = name.split(" v ", 2);
                home = parts[0].trim();
                away = parts[1].trim();
            } else if (name.contains(" @ ")) {
                String[] parts = name.split(" @ ", 2);
                away = parts[0].trim();
                home = parts[1].trim();
            }
        }

        if (home == null || away == null || home.isBlank() || away.isBlank()) {
            log.debug("Skipping BC.Game event {} due to missing team names", externalId);
            return false;
        }

        String sport = (event.getSportName() != null && !event.getSportName().isBlank())
                ? event.getSportName() : "General";
        String league = (event.getTournamentName() != null && !event.getTournamentName().isBlank())
                ? event.getTournamentName() : "General";
        boolean isLive = Boolean.TRUE.equals(event.getIsLive()) || "live".equalsIgnoreCase(event.getStatus());
        long startTime = event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis();

        String payload = String.format("%s|%s|%s|%s|%s|%s", startTime, home, away, sport, league, isLive);

        if (isThrottled(externalId, payload)) {
            return true;
        }

        try {
            MatchCache match = new MatchCache();
            match.setBookmaker("bcgame");
            match.setExternalId(externalId);
            match.setTeam1(home);
            match.setTeam2(away);
            match.setSportName(sport);
            match.setLeagueName(league);
            match.setIsLive(isLive);
            match.setStartTime(startTime);
            match.setEventUrl("https://bc.game/sports/event/" + externalId);

            persistenceService.saveOrUpdateMatchMetadata(match, payload);

            discoveryCache.put(externalId, payload);
            discoveryTimeCache.put(externalId, System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            if (isOptimisticLockException(e)) {
                log.debug("Optimistic locking conflict while saving BC.Game match {}: {}", externalId, e.getMessage());
                return true;
            } else {
                log.error("Failed saving BC.Game match {}: {}", externalId, e.getMessage());
                return false;
            }
        }
    }

    public void clearCache() {
        discoveryCache.clear();
        discoveryTimeCache.clear();
    }

    private boolean isThrottled(String externalId, String currentFootprint) {
        long now = System.currentTimeMillis();
        Long lastUpdate = discoveryTimeCache.get(externalId);
        return currentFootprint.equals(discoveryCache.get(externalId))
                && lastUpdate != null
                && (now - lastUpdate) < THROTTLE_DURATION_MS;
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
