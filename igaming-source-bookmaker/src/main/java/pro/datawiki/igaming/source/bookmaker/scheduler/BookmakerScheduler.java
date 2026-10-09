package pro.datawiki.igaming.source.bookmaker.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bookmaker.service.BookmakerDiscoveryService;
import pro.datawiki.igaming.source.bookmaker.service.BookmakerMatchService;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bookmaker.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BookmakerScheduler {

    private final BookmakerDiscoveryService discoveryService;
    private final BookmakerMatchService matchService;

    @Scheduled(cron = "${bookmaker.discovery.cron:0 */2 * * * *}")
    public void scheduleDiscovery() {
        log.info("Executing scheduled BookMaker catalog discovery...");
        try {
            int discovered = discoveryService.discoverAll();
            log.info("Scheduled BookMaker discovery finished: {} matches found.", discovered);
        } catch (Exception e) {
            log.error("Scheduled BookMaker discovery failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${bookmaker.loader.cron:*/20 * * * * *}")
    public void scheduleLoader() {
        log.debug("Executing scheduled BookMaker line update...");
        try {
            int pushed = matchService.scrapeAllSports();
            log.debug("Scheduled BookMaker line update completed: {} updates pushed.", pushed);
        } catch (Exception e) {
            log.error("Scheduled BookMaker line update failed: {}", e.getMessage(), e);
        }
    }
}
