package pro.datawiki.igaming.capture.sofascore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.capture.sofascore.provider.MatchFixtureProvider;
import pro.datawiki.igaming.dto.ReferenceFixtureDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixtureSyncScheduler {

    private final List<MatchFixtureProvider> fixtureProviders;

    private static final List<String> SUPPORTED_SPORTS = List.of(
            "FOOTBALL", "BASKETBALL", "HOCKEY", "TENNIS", "VOLLEYBALL", "CS2", "DOTA2"
    );

    /**
     * Periodically syncs fixtures for today and the next 2 days across all supported sports.
     * Runs on startup (after 10s) and then every 2 hours.
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 7200000)
    public void syncFixtures() {
        log.info("[FixtureSyncScheduler] Starting scheduled Ground Truth fixtures synchronization...");

        LocalDate today = LocalDate.now();
        List<LocalDate> targetDates = List.of(today, today.plusDays(1), today.plusDays(2));

        int totalIngested = 0;

        for (MatchFixtureProvider provider : fixtureProviders) {
            for (String sport : SUPPORTED_SPORTS) {
                if (!provider.supportsSport(sport)) continue;

                for (LocalDate date : targetDates) {
                    try {
                        List<ReferenceFixtureDto> fixtures = provider.fetchScheduledFixtures(sport, date);
                        totalIngested += fixtures.size();
                    } catch (Exception e) {
                        log.warn("[FixtureSyncScheduler] Error fetching from {} for sport {} on {}: {}",
                                provider.getProviderName(), sport, date, e.getMessage());
                    }
                }
            }
        }

        log.info("[FixtureSyncScheduler] Completed Ground Truth fixtures synchronization. Total fixtures fetched: {}", totalIngested);
    }
}
