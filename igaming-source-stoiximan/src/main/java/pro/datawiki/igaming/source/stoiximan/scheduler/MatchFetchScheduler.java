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
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${stoiximan.odds.delay.ms:45000}", initialDelay = 2000)
    public void scheduleScrape() {
        try {
            log.info("Starting scheduled Stoiximan/Kambi unified discovery & odds scraping cycle...");
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during Stoiximan/Kambi odds scraping cycle", e);
        }
    }
}
