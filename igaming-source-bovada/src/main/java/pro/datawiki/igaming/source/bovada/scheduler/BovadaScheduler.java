package pro.datawiki.igaming.source.bovada.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bovada.service.BovadaDiscoveryService;
import pro.datawiki.igaming.source.bovada.service.BovadaMatchService;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bovada.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BovadaScheduler {

    private final BovadaDiscoveryService discoveryService;
    private final BovadaMatchService matchService;

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onStartup() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(3000);
                log.info("Triggering initial startup Bovada catalog discovery...");
                int discovered = discoveryService.discoverAll();
                log.info("Startup Bovada discovery completed: {} matches found. Now triggering initial line scrape...", discovered);
                int pushed = matchService.scrapeAllSports();
                log.info("Startup Bovada initial line scrape completed: {} updates pushed.", pushed);
            } catch (Exception e) {
                log.error("Startup Bovada initial scan failed: {}", e.getMessage(), e);
            }
        }, "bovada-startup-worker");
        thread.setDaemon(true);
        thread.start();
    }

    @Scheduled(cron = "${bovada.discovery.cron:0 */2 * * * *}")
    public void scheduleDiscovery() {
        log.info("Executing scheduled Bovada catalog discovery...");
        try {
            int discovered = discoveryService.discoverAll();
            log.info("Scheduled Bovada discovery finished: {} matches found.", discovered);
        } catch (Exception e) {
            log.error("Scheduled Bovada discovery failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${bovada.loader.cron:*/15 * * * * *}")
    public void scheduleLoader() {
        log.debug("Executing scheduled Bovada line update...");
        try {
            int pushed = matchService.scrapeAllSports();
            log.debug("Scheduled Bovada line update completed: {} updates pushed.", pushed);
        } catch (Exception e) {
            log.error("Scheduled Bovada line update failed: {}", e.getMessage(), e);
        }
    }
}
