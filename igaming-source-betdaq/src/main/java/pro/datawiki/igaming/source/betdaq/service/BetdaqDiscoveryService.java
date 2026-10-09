package pro.datawiki.igaming.source.betdaq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqEventDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqSportDto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetdaqDiscoveryService {

    private final BetdaqApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting Betdaq event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<BetdaqSportDto> sports = apiClient.getSports();
            if (sports != null && !sports.isEmpty()) {
                for (BetdaqSportDto sport : sports) {
                    try {
                        List<BetdaqEventDto> events = apiClient.getEvents(sport.getId());
                        if (events != null && !events.isEmpty()) {
                            for (BetdaqEventDto event : events) {
                                if (event.getSportName() == null) {
                                    event.setSportName(sport.getName());
                                }
                                if (processAndSaveEvent(event)) {
                                    totalDiscovered.incrementAndGet();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed fetching Betdaq events for sport {}: {}", sport.getName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Betdaq events: {}", e.getMessage());
        }

        log.info("Betdaq discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveEvent(BetdaqEventDto event) {
        if (event == null || event.getId() == null || event.getName() == null) {
            return false;
        }

        String eventName = event.getName().trim();
        String[] teams = parseTeams(eventName);
        if (teams == null || teams.length < 2) {
            return false;
        }

        String team1 = teams[0].trim();
        String team2 = teams[1].trim();

        MatchCache cache = new MatchCache();
        cache.setBookmaker("betdaq");
        cache.setExternalId(event.getId());
        cache.setTeam1(team1);
        cache.setTeam2(team2);

        String sportName = event.getSportName() != null ? event.getSportName() : "Football";
        cache.setSportName(sportName);

        String leagueName = event.getCompetitionName() != null ? event.getCompetitionName() : "General";
        cache.setLeagueName(leagueName);

        boolean isLive = Boolean.TRUE.equals(event.getIsLive()) || "in_play".equalsIgnoreCase(event.getStatus());
        cache.setIsLive(isLive);

        long startMs = event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis();
        cache.setStartTime(startMs);

        cache.setEventUrl("https://www.betdaq.com/event/" + event.getId());

        String footprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        try {
            persistenceService.saveOrUpdateMatchMetadata(cache, footprint);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving Betdaq match {}: {}", event.getId(), e.getMessage());
            return false;
        }
    }

    private String[] parseTeams(String name) {
        if (name.contains(" v ")) {
            return name.split(" v ");
        }
        if (name.contains(" vs ")) {
            return name.split(" vs ");
        }
        if (name.contains(" - ")) {
            return name.split(" - ");
        }
        if (name.contains(" @ ")) {
            String[] parts = name.split(" @ ");
            return new String[]{parts[1].trim(), parts[0].trim()};
        }
        return null;
    }
}
