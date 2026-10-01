package pro.datawiki.igaming.source.bet7k.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet7k.service.Bet7kMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bet7k.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class Bet7kScheduler {

    private final Bet7kMatchService matchService;

    @Scheduled(fixedDelayString = "${app.bet7k.poll-rate-ms:10000}", initialDelay = 5000)
    public void scrapeLineSchedule() {
        try {
            log.debug("Triggering Bet7k line scraping cycle...");
            int pushed = matchService.scrapeAllSports();
            log.debug("Bet7k cycle finished: {} updates pushed", pushed);
        } catch (Exception e) {
            log.error("Unhandled error in Bet7k scraping schedule: {}", e.getMessage(), e);
        }
    }
}
