package pro.datawiki.igaming.source.betesporte.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betesporte.service.BetesporteMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.betesporte.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BetesporteScheduler {

    private final BetesporteMatchService matchService;

    @Scheduled(fixedDelayString = "${app.betesporte.poll-rate-ms:10000}", initialDelay = 5000)
    public void scrapeLineSchedule() {
        try {
            log.debug("Triggering Betesporte line scraping cycle...");
            int pushed = matchService.scrapeAllSports();
            log.debug("Betesporte cycle finished: {} updates pushed", pushed);
        } catch (Exception e) {
            log.error("Unhandled error in Betesporte scraping schedule: {}", e.getMessage(), e);
        }
    }
}
