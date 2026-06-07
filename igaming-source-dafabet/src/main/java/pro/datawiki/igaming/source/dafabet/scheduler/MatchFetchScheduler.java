package pro.datawiki.igaming.source.dafabet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.dafabet.service.MatchService;

/**
 * Crawler role for Dafabet: crawls fixtures and odds
 * and pushes updates directly to the aggregator.
 * Only active when app.role=league-crawler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${dafabet.fetch.delay.ms:60000}")
    public void scheduleFetch() {
        log.info("League-crawler: Starting Dafabet line scraping cycle...");
        try {
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during Dafabet scraping cycle", e);
        }
    }
}
