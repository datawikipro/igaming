package pro.datawiki.igaming.source.paf.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.paf.service.MatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler", matchIfMissing = true)
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${paf.fetch.delay.ms:30000}", initialDelay = 5000)
    public void scheduleFetch() {
        log.info("Starting PAF (Kambi) discovery and odds scrape cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during PAF discovery cycle: {}", e.getMessage());
        }
        try {
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during PAF odds scraping cycle: {}", e.getMessage());
        }
    }
}
