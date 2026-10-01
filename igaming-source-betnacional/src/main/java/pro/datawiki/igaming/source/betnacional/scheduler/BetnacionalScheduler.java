package pro.datawiki.igaming.source.betnacional.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betnacional.service.BetnacionalMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.betnacional.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BetnacionalScheduler {

    private final BetnacionalMatchService matchService;

    @Scheduled(fixedDelayString = "${app.betnacional.poll-rate-ms:10000}", initialDelay = 5000)
    public void scrapeLineSchedule() {
        try {
            log.debug("Triggering Betnacional line scraping cycle...");
            int pushed = matchService.scrapeAllSports();
            log.debug("Betnacional cycle finished: {} updates pushed", pushed);
        } catch (Exception e) {
            log.error("Unhandled error in Betnacional scraping schedule: {}", e.getMessage(), e);
        }
    }
}
