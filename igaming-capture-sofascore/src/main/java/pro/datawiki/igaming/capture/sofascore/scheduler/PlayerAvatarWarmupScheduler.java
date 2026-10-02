package pro.datawiki.igaming.capture.sofascore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.capture.sofascore.provider.MatchFixtureProvider;
import pro.datawiki.igaming.capture.sofascore.service.PlayerAvatarFetchService;
import pro.datawiki.igaming.capture.sofascore.util.IndividualSportDetector;
import pro.datawiki.igaming.dto.ReferenceFixtureDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Scheduled warm-up job that pre-fetches player face avatar metadata for upcoming
 * individual-sport matches (tennis, MMA, boxing, badminton, squash, padel, etc.).
 *
 * <p>Runs after {@link FixtureSyncScheduler} completes its initial pass (80 s initial delay)
 * and then every 4 hours. Iterates over the next 2 days of fixtures for all individual
 * sports and calls {@link PlayerAvatarFetchService#fetchPlayerFace} for each unique
 * player ID, populating the in-memory cache so that downstream queries are instant.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerAvatarWarmupScheduler {

    private final List<MatchFixtureProvider> fixtureProviders;
    private final PlayerAvatarFetchService playerAvatarFetchService;

    private static final List<String> INDIVIDUAL_SPORTS = List.of(
            "TENNIS", "TABLE_TENNIS", "BADMINTON", "SQUASH", "PADEL",
            "BOXING", "MMA", "SNOOKER", "DARTS"
    );

    /**
     * Pre-fetches player avatar metadata for upcoming individual-sport fixtures.
     * Runs 80 s after startup (after FixtureSyncScheduler) and then every 4 hours.
     */
    @Scheduled(initialDelay = 80_000, fixedDelay = 14_400_000)
    public void warmUpPlayerAvatarCache() {
        log.info("[PlayerAvatarWarmup] Starting player face avatar cache warm-up for individual sports...");

        LocalDate today = LocalDate.now();
        List<LocalDate> targetDates = List.of(today, today.plusDays(1), today.plusDays(2));

        int uniquePlayers = 0;
        int fetchedProfiles = 0;

        for (MatchFixtureProvider provider : fixtureProviders) {
            for (String sport : INDIVIDUAL_SPORTS) {
                if (!provider.supportsSport(sport)) continue;

                for (LocalDate date : targetDates) {
                    try {
                        List<ReferenceFixtureDto> fixtures = provider.fetchScheduledFixtures(sport, date);
                        for (ReferenceFixtureDto fixture : fixtures) {
                            Map<String, Object> meta = fixture.getMetadata();
                            if (meta == null) continue;

                            // Warm up player 1 face
                            Object p1Id = meta.get("player1_sofa_id");
                            if (p1Id != null) {
                                uniquePlayers++;
                                boolean fetched = playerAvatarFetchService
                                        .fetchPlayerFace(p1Id.toString(), sport)
                                        .isPresent();
                                if (fetched) fetchedProfiles++;
                            }

                            // Warm up player 2 face
                            Object p2Id = meta.get("player2_sofa_id");
                            if (p2Id != null) {
                                uniquePlayers++;
                                boolean fetched = playerAvatarFetchService
                                        .fetchPlayerFace(p2Id.toString(), sport)
                                        .isPresent();
                                if (fetched) fetchedProfiles++;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[PlayerAvatarWarmup] Error fetching fixtures for sport {} on {}: {}",
                                sport, date, e.getMessage());
                    }
                }
            }
        }

        log.info("[PlayerAvatarWarmup] Warm-up complete. Encountered {} player slots, fetched {} profiles. Cache size: {}",
                uniquePlayers, fetchedProfiles, playerAvatarFetchService.cacheSize());
    }
}
