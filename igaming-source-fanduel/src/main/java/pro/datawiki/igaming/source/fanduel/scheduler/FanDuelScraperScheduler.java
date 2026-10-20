package pro.datawiki.igaming.source.fanduel.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.fanduel.config.FanDuelConfig;
import pro.datawiki.igaming.source.fanduel.service.FanDuelApiClient;
import pro.datawiki.igaming.source.fanduel.service.FanDuelLeagueDiscoveryService;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelScraperScheduler {

    private final FanDuelApiClient apiClient;
    private final FanDuelConfig config;
    private final FanDuelLeagueDiscoveryService discoveryService;

    private static final Map<Long, String> SPORT_NAMES = Map.ofEntries(
        Map.entry(6423L, "American Football"),
        Map.entry(1L, "Soccer"),
        Map.entry(7522L, "Basketball"),
        Map.entry(7524L, "Ice Hockey"),
        Map.entry(7511L, "Baseball"),
        Map.entry(2L, "Tennis"),
        Map.entry(26420387L, "MMA"),
        Map.entry(6L, "Boxing"),
        Map.entry(3L, "Golf"),
        Map.entry(8L, "Motor Sport"),
        Map.entry(4L, "Cricket"),
        Map.entry(5L, "Rugby"),
        Map.entry(7L, "Handball"),
        Map.entry(9L, "Australian Rules"),
        Map.entry(10L, "Volleyball"),
        Map.entry(11L, "Darts"),
        Map.entry(12L, "Snooker"),
        Map.entry(2593174L, "Table Tennis")
    );

    private static final Map<String, String> CUSTOM_PAGE_SPORTS = Map.of(
        "nfl", "American Football",
        "nba", "Basketball",
        "nhl", "Ice Hockey",
        "mlb", "Baseball",
        "ncaaf", "American Football",
        "ncaab", "Basketball",
        "wnba", "Basketball"
    );

    /**
     * Run discovery once on startup so we have leagues in DB immediately,
     * followed by the initial scraping cycle.
     */
    @PostConstruct
    public void initialScrape() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // let Spring settle
                log.info("Starting initial FanDuel league discovery (Zero-Filter Policy)...");
                discoveryService.discoverLeagues();
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
     * Periodic league discovery cycle — discovers all leagues, event groups, and competitions.
     */
    @Scheduled(
        fixedDelayString   = "${app.league.discovery.poll.ms:1800000}",
        initialDelayString = "${app.league.discovery.initial.delay.ms:15000}"
    )
    public void scheduledDiscovery() {
        try {
            log.info("Running periodic FanDuel league discovery...");
            int count = discoveryService.discoverLeagues();
            log.info("Periodic FanDuel league discovery completed: {} operations", count);
        } catch (Exception e) {
            log.error("Periodic FanDuel league discovery failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Main scraping cycle — runs every 5 minutes (300,000 ms) by default.
     */
    @Scheduled(
        fixedDelayString   = "${app.odds.refresh.prematch.poll.ms:300000}",
        initialDelayString = "${app.match.loader.poll.delay.ms:30000}"
    )
    public void scrape() {
        log.info("=== Starting FanDuel REST scrape cycle ===");
        int totalSaved = 0;

        // 1. Fetch featured Homepage (Live & highlighted matches across all sports)
        try {
            int saved = apiClient.fetchHomepage();
            totalSaved += saved;
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) {
            log.error("FanDuel fetch homepage failed: {}", e.getMessage());
        }

        // 2. Fetch custom curated sport pages (featured NFL, NBA, NHL, MLB)
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

        // 3. Fetch all configured sport categories by eventTypeId
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
