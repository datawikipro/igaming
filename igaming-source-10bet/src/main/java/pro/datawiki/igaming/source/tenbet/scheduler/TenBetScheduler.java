package pro.datawiki.igaming.source.tenbet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.tenbet.service.TenBetMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.10bet.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class TenBetScheduler {

    private final TenBetMatchService matchService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(3000);
                log.info("Triggering initial startup 10bet line scrape...");
                int pushed = matchService.scrapeAllSports();
                log.info("Startup 10bet initial line scrape completed: {} updates pushed.", pushed);
            } catch (Exception e) {
                log.error("Startup 10bet initial scan failed: {}", e.getMessage(), e);
            }
        }, "tenbet-startup-worker");
        thread.setDaemon(true);
        thread.start();
    }

    @Scheduled(cron = "${app.10bet.scheduler.cron:0 */2 * * * *}")
    public void scheduleScrape() {
        log.info("Executing scheduled 10bet line scraping...");
        try {
            int pushed = matchService.scrapeAllSports();
            log.info("Scheduled 10bet line scraping finished: {} updates pushed.", pushed);
        } catch (Exception e) {
            log.error("Scheduled 10bet line scraping failed: {}", e.getMessage(), e);
        }
    }
}
