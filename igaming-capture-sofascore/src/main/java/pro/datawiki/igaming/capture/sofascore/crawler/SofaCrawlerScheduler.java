package pro.datawiki.igaming.capture.sofascore.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled runner for SofaScore Crawler.
 * Active when app.role is 'crawler' or 'standalone'.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "crawler")
public class SofaCrawlerScheduler {

    private final SofaCrawlerService crawlerService;
    private final AtomicInteger historicalDaysBack = new AtomicInteger(1);

    /**
     * Poll live events every 45 seconds.
     */
    @Scheduled(fixedDelayString = "${sofascore.crawler.live-interval-ms:45000}", initialDelay = 5000)
    public void scheduleLiveCrawl() {
        log.info("[SofaCrawlerScheduler] Running scheduled live discovery across sports...");
        try {
            int count = crawlerService.crawlLiveEvents();
            log.info("[SofaCrawlerScheduler] Live crawl finished: {} tasks queued.", count);
        } catch (Exception e) {
            log.error("[SofaCrawlerScheduler] Error during live crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Poll prematch events for today and tomorrow every 10 minutes.
     */
    @Scheduled(fixedDelayString = "${sofascore.crawler.prematch-interval-ms:600000}", initialDelay = 15000)
    public void schedulePrematchCrawl() {
        log.info("[SofaCrawlerScheduler] Running scheduled prematch discovery for today & tomorrow...");
        try {
            LocalDate today = LocalDate.now();
            int todayCount = crawlerService.crawlScheduledEvents(today);
            int tomorrowCount = crawlerService.crawlScheduledEvents(today.plusDays(1));
            log.info("[SofaCrawlerScheduler] Prematch crawl finished: today={}, tomorrow={}", todayCount, tomorrowCount);
        } catch (Exception e) {
            log.error("[SofaCrawlerScheduler] Error during prematch crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Incrementally crawl past historical dates in the background every 5 minutes.
     */
    @Scheduled(fixedDelayString = "${sofascore.crawler.history-interval-ms:300000}", initialDelay = 30000)
    public void scheduleHistoricalCrawl() {
        int daysBack = historicalDaysBack.getAndIncrement();
        if (daysBack > 730) { // 2 years limit
            historicalDaysBack.set(1);
            daysBack = 1;
        }

        LocalDate targetDate = LocalDate.now().minusDays(daysBack);
        log.info("[SofaCrawlerScheduler] Running scheduled historical archive discovery for {} (days back: {})...",
                targetDate, daysBack);
        try {
            int count = crawlerService.crawlHistoricalDate(targetDate);
            log.info("[SofaCrawlerScheduler] Historical crawl for {} finished: {} tasks queued.", targetDate, count);
        } catch (Exception e) {
            log.error("[SofaCrawlerScheduler] Error during historical crawl for {}: {}", targetDate, e.getMessage());
        }
    }
}
