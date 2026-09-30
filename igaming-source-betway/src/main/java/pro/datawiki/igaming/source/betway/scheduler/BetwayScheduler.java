package pro.datawiki.igaming.source.betway.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betway.service.BetwayMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.betway.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BetwayScheduler {

    private final BetwayMatchService matchService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(3000);
                log.info("Triggering initial startup Betway line scrape...");
                int pushed = matchService.scrapeAllSports();
                log.info("Startup Betway initial line scrape completed: {} updates pushed.", pushed);
            } catch (Exception e) {
                log.error("Startup Betway initial scan failed: {}", e.getMessage(), e);
            }
        }, "betway-startup-worker");
        thread.setDaemon(true);
        thread.start();
    }

    @Scheduled(cron = "${app.betway.scheduler.cron:0 */2 * * * *}")
    public void scheduleScrape() {
        log.info("Executing scheduled Betway line scraping...");
        try {
            int pushed = matchService.scrapeAllSports();
            log.info("Scheduled Betway line scraping finished: {} updates pushed.", pushed);
        } catch (Exception e) {
            log.error("Scheduled Betway line scraping failed: {}", e.getMessage(), e);
        }
    }
}
