package pro.datawiki.igaming.source.fanduel.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.fanduel.config.FanDuelConfig;
import pro.datawiki.igaming.source.fanduel.service.FanDuelApiClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelScraperScheduler {

    private final FanDuelApiClient apiClient;
    private final FanDuelConfig config;
    private final AtomicBoolean isScraping = new AtomicBoolean(false);

    private static final Map<Long, String> SPORT_NAMES = Map.of(
        6423L, "American Football",
        1L, "Soccer",
        7522L, "Basketball",
        7524L, "Ice Hockey",
        7511L, "Baseball",
        2L, "Tennis",
        26420387L, "MMA",
        6L, "Boxing",
        3L, "Golf",
        8L, "Motor Sport"
    );

    private static final Map<String, String> CUSTOM_PAGE_SPORTS = Map.of(
        "nfl", "American Football",
        "nba", "Basketball",
        "nhl", "Ice Hockey",
        "mlb", "Baseball"
    );

    /**
     * Main scraping cycle — runs every 5 minutes (300,000 ms) by default,
     * starting 5 seconds after application initialization.
     */
    @Scheduled(
        fixedDelayString   = "${app.odds.refresh.prematch.poll.ms:300000}",
        initialDelayString = "${app.match.loader.poll.delay.ms:5000}"
    )
    public void scrape() {
        if (!isScraping.compareAndSet(false, true)) {
            log.info("FanDuel scrape cycle already in progress, skipping overlapping execution");
            return;
        }
        try {
            log.info("=== Starting FanDuel REST scrape cycle ===");
            int totalSaved = 0;

        // 1. Fetch custom curated sport pages (featured NFL, NBA, NHL, MLB)
        for (String page : config.getFetch().getCustomPages()) {
            String sport = CUSTOM_PAGE_SPORTS.getOrDefault(page, "Other");
            try {
                int saved = apiClient.fetchCustomPage(page, sport);
                totalSaved += saved;
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("FanDuel fetch custom page '{}' failed: {}", page, e.getMessage());
            }
        }

        // 2. Fetch all configured sport categories by eventTypeId
        for (Long eventTypeId : config.getFetch().getSportEventTypeIds()) {
            String sport = SPORT_NAMES.getOrDefault(eventTypeId, "Other");
            try {
                int saved = apiClient.fetchSport(eventTypeId, sport);
                totalSaved += saved;
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("FanDuel fetch sport eventTypeId={} ({}) failed: {}", eventTypeId, sport, e.getMessage());
            }
        }

            log.info("=== FanDuel REST scrape cycle completed: total {} matches saved/updated ===", totalSaved);
        } finally {
            isScraping.set(false);
        }
    }
}

