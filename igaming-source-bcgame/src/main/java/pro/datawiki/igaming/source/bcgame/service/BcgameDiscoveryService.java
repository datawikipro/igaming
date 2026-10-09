package pro.datawiki.igaming.source.bcgame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BcgameDiscoveryService {

    private final BcgameApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting BC.Game event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<BcgameSportDto> sports = apiClient.getSports();
            if (sports != null && !sports.isEmpty()) {
                for (BcgameSportDto sport : sports) {
                    try {
                        List<BcgameEventDto> events = apiClient.getEvents(sport.getId());
                        if (events != null && !events.isEmpty()) {
                            for (BcgameEventDto event : events) {
                                if (event.getSportName() == null) {
                                    event.setSportName(sport.getName());
                                }
                                if (processAndSaveEvent(event)) {
                                    totalDiscovered.incrementAndGet();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed fetching BC.Game events for sport {}: {}", sport.getName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering BC.Game events: {}", e.getMessage());
        }

        log.info("BC.Game discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(BcgameEventDto event) {
        if (event == null || event.getId() == null) {
            return false;
        }

        String team1 = event.getHomeTeam();
        String team2 = event.getAwayTeam();

        if (team1 == null || team2 == null) {
            if (event.getName() != null) {
                String[] parts = event.getName().split(" vs | - | @ ");
                if (parts.length >= 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        MatchCache cache = new MatchCache();
        cache.setBookmaker("bcgame");
        cache.setExternalId(event.getId());
        cache.setTeam1(team1);
        cache.setTeam2(team2);

        String sportName = event.getSportName() != null ? event.getSportName() : "General";
        cache.setSportName(sportName);

        String leagueName = event.getTournamentName() != null ? event.getTournamentName() : "General";
        cache.setLeagueName(leagueName);

        boolean isLive = Boolean.TRUE.equals(event.getIsLive()) || "live".equalsIgnoreCase(event.getStatus());
        cache.setIsLive(isLive);

        long startMs = event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis();
        cache.setStartTime(startMs);

        cache.setEventUrl("https://bc.game/sports/" + event.getId());

        String footprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        try {
            persistenceService.saveOrUpdateMatchMetadata(cache, footprint);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving BC.Game match {}: {}", event.getId(), e.getMessage());
            return false;
        }
    }
}
