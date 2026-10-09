package pro.datawiki.igaming.source.cloudbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetFixtureDto;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetSportDto;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudbetDiscoveryService {

    private final CloudbetApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting Cloudbet event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<CloudbetSportDto> sports = apiClient.getSports();
            if (sports != null && !sports.isEmpty()) {
                for (CloudbetSportDto sport : sports) {
                    try {
                        List<CloudbetFixtureDto> fixtures = apiClient.getFixtures(sport.getKey());
                        if (fixtures != null && !fixtures.isEmpty()) {
                            for (CloudbetFixtureDto fixture : fixtures) {
                                if (processAndSaveFixture(fixture, sport.getName())) {
                                    totalDiscovered.incrementAndGet();
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Failed fetching Cloudbet fixtures for sport {}: {}", sport.getKey(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Cloudbet events: {}", e.getMessage());
        }

        log.info("Cloudbet discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveFixture(CloudbetFixtureDto fixture, String defaultSport) {
        if (fixture == null || fixture.getId() == null) {
            return false;
        }

        String team1 = fixture.getHome() != null ? fixture.getHome().getName() : null;
        String team2 = fixture.getAway() != null ? fixture.getAway().getName() : null;

        if (team1 == null || team2 == null) {
            if (fixture.getName() != null) {
                String[] parts = fixture.getName().split(" vs | - | @ ");
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
        cache.setBookmaker("cloudbet");
        cache.setExternalId(fixture.getId());
        cache.setTeam1(team1);
        cache.setTeam2(team2);

        String sportName = fixture.getSport() != null && fixture.getSport().getName() != null
                ? fixture.getSport().getName() : defaultSport;
        cache.setSportName(sportName != null ? sportName : "General");

        String leagueName = fixture.getCompetition() != null && fixture.getCompetition().getName() != null
                ? fixture.getCompetition().getName() : "General";
        cache.setLeagueName(leagueName);

        boolean isLive = "trading_live".equalsIgnoreCase(fixture.getStatus()) || "live".equalsIgnoreCase(fixture.getStatus());
        cache.setIsLive(isLive);

        long startMs = System.currentTimeMillis();
        if (fixture.getCutoffTime() != null) {
            try {
                startMs = Instant.parse(fixture.getCutoffTime()).toEpochMilli();
            } catch (Exception ignored) {}
        }
        cache.setStartTime(startMs);

        cache.setEventUrl("https://www.cloudbet.com/en/sports/event/" + fixture.getId());

        String footprint = String.format("%s|%s|%s|%s|%s|%s",
                startMs, team1, team2, sportName, leagueName, isLive);

        try {
            persistenceService.saveOrUpdateMatchMetadata(cache, footprint);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving Cloudbet match {}: {}", fixture.getId(), e.getMessage());
            return false;
        }
    }
}
