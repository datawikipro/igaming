package pro.datawiki.igaming.source.fanduel.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.domain.LeagueCache;
import pro.datawiki.igaming.source.core.repository.LeagueCacheRepository;
import pro.datawiki.igaming.source.fanduel.service.FanDuelApiClient;
import pro.datawiki.igaming.source.fanduel.service.FanDuelLeagueDiscoveryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FanDuel scraping scheduler.
 *
 * <p><b>Two-phase design:</b>
 * <ol>
 *   <li><b>Discovery</b> (every 6 hours) — navigates to FanDuel main sports page,
 *       intercepts {@code sports.json} and upserts all available event groups
 *       into {@code league_cache} (Postgres). Falls back to hardcoded defaults
 *       if the interception fails.</li>
 *   <li><b>Scraping</b> (every {@code app.odds.refresh.prematch.poll.ms}, default 10 min) —
 *       reads all FanDuel leagues from {@code league_cache}, fetches odds for each,
 *       pushes to the aggregator.</li>
 * </ol>
 *
 * This design means <b>zero hardcoded league IDs</b> — new FanDuel leagues
 * appear automatically after the next discovery run.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelScraperScheduler {

    private static final String FANDUEL_ID_PREFIX = "fanduel-";
    private static final int    MAX_LEAGUES_PER_CYCLE = 50;

    private final FanDuelLeagueDiscoveryService discoveryService;
    private final FanDuelApiClient apiClient;
    private final LeagueCacheRepository leagueCacheRepository;

    /**
     * Run discovery once on startup so we have leagues in DB immediately.
     * Uses a small delay to let the browser context warm up.
     */
    @PostConstruct
    public void initialDiscovery() {
        new Thread(() -> {
            try {
                Thread.sleep(8000); // let Spring context settle
                log.info("Running initial FanDuel league discovery...");
                int count = discoveryService.discoverLeagues();
                log.info("Initial discovery done: {} leagues in league_cache", count);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Initial FanDuel discovery failed: {}", e.getMessage(), e);
            }
        }, "fanduel-init-discovery").start();
    }

    /**
     * Re-discover leagues every 6 hours.
     * Picks up newly added FanDuel competitions automatically.
     */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000, initialDelay = 6 * 60 * 60 * 1000)
    public void rediscoverLeagues() {
        log.info("Scheduled FanDuel league re-discovery...");
        try {
            discoveryService.discoverLeagues();
        } catch (Exception e) {
            log.error("FanDuel re-discovery error: {}", e.getMessage(), e);
        }
    }

    /**
     * Main scraping loop — reads discovered leagues from Postgres and fetches odds.
     */
    @Scheduled(
        fixedDelayString   = "${app.odds.refresh.prematch.poll.ms:600000}",
        initialDelayString = "${app.match.loader.poll.delay.ms:15000}"
    )
    @org.springframework.transaction.annotation.Transactional
    public void scrape() {
        // Fetch all FanDuel leagues from league_cache
        List<LeagueCache> leagues = leagueCacheRepository
                .findAndLockPendingLeagues(PageRequest.of(0, MAX_LEAGUES_PER_CYCLE));

        // Filter to FanDuel only (prefix guard in case other bookmakers share the table)
        List<LeagueCache> fanDuelLeagues = leagues.stream()
                .filter(lc -> lc.getExternalId() != null && lc.getExternalId().startsWith(FANDUEL_ID_PREFIX))
                .toList();

        if (fanDuelLeagues.isEmpty()) {
            log.info("No FanDuel leagues in league_cache yet — skipping scrape cycle");
            return;
        }

        log.info("Starting FanDuel scraping cycle: {} leagues", fanDuelLeagues.size());

        for (LeagueCache league : fanDuelLeagues) {
            // Extract numeric groupId from externalId "fanduel-{groupId}"
            String eventGroupId = league.getExternalId().substring(FANDUEL_ID_PREFIX.length());

            try {
                apiClient.processLeague(
                        eventGroupId,
                        league.getSportName(),
                        league.getLeagueName(),
                        league.getUrl()
                );
                leagueCacheRepository.updateStatus(
                        league.getId(),
                        LeagueCache.Status.PROCESSED,
                        LocalDateTime.now()
                );
            } catch (Exception e) {
                log.error("FanDuel scrape failed for league {} ({}): {}",
                        league.getLeagueName(), eventGroupId, e.getMessage());
                leagueCacheRepository.updateStatus(
                        league.getId(),
                        LeagueCache.Status.FAILED,
                        LocalDateTime.now()
                );
            }

            // Pause between leagues — browser needs time between navigations
            try { Thread.sleep(3000); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("FanDuel scraping cycle completed.");
    }
}
