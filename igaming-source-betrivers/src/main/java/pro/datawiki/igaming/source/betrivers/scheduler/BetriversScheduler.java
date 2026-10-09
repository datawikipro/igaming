package pro.datawiki.igaming.source.betrivers.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betrivers.service.BetriversDiscoveryService;
import pro.datawiki.igaming.source.betrivers.service.BetriversMatchService;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "betrivers.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BetriversScheduler {

    private final BetriversDiscoveryService discoveryService;
    private final BetriversMatchService matchService;

    @Scheduled(cron = "${betrivers.discovery.cron:0 */2 * * * *}")
    public void scheduleDiscovery() {
        log.info("Executing scheduled BetRivers catalog discovery...");
        try {
            int discovered = discoveryService.discoverAll();
            log.info("Scheduled BetRivers discovery finished: {} matches found.", discovered);
        } catch (Exception e) {
            log.error("Scheduled BetRivers discovery failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${betrivers.loader.cron:*/15 * * * * *}")
    public void scheduleLoader() {
        log.debug("Executing scheduled BetRivers line update...");
        try {
            int pushed = matchService.scrapeAllSports();
            log.debug("Scheduled BetRivers line update completed: {} updates pushed.", pushed);
        } catch (Exception e) {
            log.error("Scheduled BetRivers line update failed: {}", e.getMessage(), e);
        }
    }
}
