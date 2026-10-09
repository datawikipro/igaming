package pro.datawiki.igaming.source.stake.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.stake.dto.StakeFixtureDto;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class StakeDiscoveryService {

    private final StakeApiClient apiClient;
    private final MatchPersistenceService persistenceService;

    public int discoverEvents() {
        log.info("Starting Stake event discovery cycle...");
        AtomicInteger totalDiscovered = new AtomicInteger(0);

        try {
            List<StakeFixtureDto> fixtures = apiClient.fetchAllFixtures();
            if (fixtures != null && !fixtures.isEmpty()) {
                log.info("Stake: fetched {} fixtures across sports", fixtures.size());
                for (StakeFixtureDto fixture : fixtures) {
                    if (processAndSaveFixture(fixture)) {
                        totalDiscovered.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error discovering Stake fixtures: {}", e.getMessage());
        }

        log.info("Stake discovery cycle completed. Total active matches processed: {}", totalDiscovered.get());
        return totalDiscovered.get();
    }

    public boolean processAndSaveFixture(StakeFixtureDto fixture) {
        if (fixture == null || fixture.getId() == null) {
            return false;
        }

        String team1 = "Home";
        String team2 = "Away";

        if (fixture.getCompetitors() != null && fixture.getCompetitors().size() >= 2) {
            for (StakeFixtureDto.CompetitorRef comp : fixture.getCompetitors()) {
                if ("home".equalsIgnoreCase(comp.getQualifier())) {
                    team1 = comp.getName();
                } else if ("away".equalsIgnoreCase(comp.getQualifier())) {
                    team2 = comp.getName();
                }
            }
            if ("Home".equals(team1) && "Away".equals(team2)) {
                team1 = fixture.getCompetitors().get(0).getName();
                team2 = fixture.getCompetitors().get(1).getName();
            }
        } else if (fixture.getName() != null) {
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

        MatchCache cache = new MatchCache();
        cache.setBookmaker("stake");
        cache.setExternalId(fixture.getId());
        cache.setTeam1(team1);
        cache.setTeam2(team2);

        String sportName = fixture.getSport() != null && fixture.getSport().getName() != null
                ? fixture.getSport().getName() : "General";
        cache.setSportName(sportName);

        String leagueName = fixture.getTournament() != null && fixture.getTournament().getName() != null
                ? fixture.getTournament().getName() : "Global";
        cache.setLeagueName(leagueName);

        boolean isLive = "live".equalsIgnoreCase(fixture.getStatus()) || "active".equalsIgnoreCase(fixture.getStatus());
        cache.setIsLive(isLive);

        if (fixture.getStartTime() instanceof Number num) {
            cache.setStartTime(num.longValue());
        } else if (fixture.getStartTime() instanceof String str) {
            try {
                cache.setStartTime(Instant.parse(str).toEpochMilli());
            } catch (Exception e) {
                cache.setStartTime(System.currentTimeMillis());
            }
        } else {
            cache.setStartTime(System.currentTimeMillis());
        }

        cache.setEventUrl("https://stake.com/sports/" + (fixture.getSlug() != null ? fixture.getSlug() : fixture.getId()));

        String footprint = String.format("%s|%s|%s|%s|%s|%s",
                cache.getStartTime(), team1, team2, sportName, leagueName, isLive);

        try {
            persistenceService.saveOrUpdateMatchMetadata(cache, footprint);
            return true;
        } catch (Exception e) {
            log.debug("Failed saving Stake match {}: {}", fixture.getId(), e.getMessage());
            return false;
        }
    }
}
