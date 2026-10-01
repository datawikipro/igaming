package pro.datawiki.igaming.source.bcgame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BcgameDiscoveryService {

    private final BcgameApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting BC.Game events discovery...");
        List<BcgameSportDto> sports = apiClient.getSports();
        int totalSaved = 0;

        for (BcgameSportDto sport : sports) {
            try {
                List<BcgameEventDto> events = apiClient.getEvents(sport.getId());
                log.info("Discovered {} events for sport '{}' ({})", events.size(), sport.getName(), sport.getId());
                for (BcgameEventDto event : events) {
                    if (event.getSportName() == null) {
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
        if (event == null || event.getId() == null) return false;

        String home = event.getHomeTeam();
        String away = event.getAwayTeam();
        if ((home == null || away == null || home.isBlank() || away.isBlank()) && event.getName() != null) {
            String[] parts = event.getName().split(" vs | - ");
            if (parts.length >= 2) {
                home = parts[0].trim();
                away = parts[1].trim();
            }
        }

        if (home == null || away == null || home.isBlank() || away.isBlank()) {
            return false;
        }

        try {
            MatchCache match = new MatchCache();
            match.setBookmaker("bcgame");
            match.setExternalId(event.getId());
            match.setTeam1(home);
            match.setTeam2(away);
            String sport = event.getSportName() != null ? event.getSportName() : "General";
            match.setSportName(sport);
            String league = event.getTournamentName() != null ? event.getTournamentName() : "General";
            match.setLeagueName(league);
            boolean isLive = Boolean.TRUE.equals(event.getIsLive()) || "live".equalsIgnoreCase(event.getStatus());
            match.setIsLive(isLive);
            long startTime = event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis();
            match.setStartTime(startTime);
            match.setEventUrl("https://bc.game/sports/event/" + event.getId());

            String payload = String.format("%s|%s|%s|%s|%s|%s", startTime, home, away, sport, league, isLive);
            persistenceService.saveOrUpdateMatchMetadata(match, payload);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving BC.Game match {}: {}", event.getId(), e.getMessage());
            return false;
        }
    }
}
