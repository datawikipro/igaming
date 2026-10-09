package pro.datawiki.igaming.source.stoiximan.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.stoiximan.service.MatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${stoiximan.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Stoiximan discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Stoiximan discovery cycle", e);
        }
    }

    @Scheduled(fixedDelayString = "${stoiximan.odds.delay.ms:20000}", initialDelay = 10000)
    public void scheduleScrape() {
        try {
            log.info("Starting scheduled Stoiximan odds scraping cycle...");
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during Stoiximan odds scraping cycle", e);
        }
    }
}
