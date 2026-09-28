package pro.datawiki.igaming.source.fanduel.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.fanduel.config.FanDuelConfig;
import pro.datawiki.igaming.source.fanduel.service.FanDuelApiClient;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelScraperScheduler {

    private final FanDuelApiClient apiClient;
    private final FanDuelConfig config;

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
     * Run discovery once on startup so we have leagues in DB immediately.
     * Uses a small delay to let the browser context warm up.
     */
    @PostConstruct
    public void initialScrape() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // let Spring settle
                log.info("Starting initial FanDuel REST scraping cycle...");
                scrape();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Initial FanDuel scrape failed: {}", e.getMessage(), e);
            }
        }, "fanduel-init-scrape").start();
    }

    /**
     * Main scraping cycle — runs every 5 minutes (300,000 ms) by default.
     */
    @Scheduled(
        fixedDelayString   = "${app.odds.refresh.prematch.poll.ms:300000}",
        initialDelayString = "${app.match.loader.poll.delay.ms:15000}"
    )
    public void scrape() {
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
    }
}

