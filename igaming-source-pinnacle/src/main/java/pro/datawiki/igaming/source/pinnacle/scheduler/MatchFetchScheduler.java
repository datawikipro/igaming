package pro.datawiki.igaming.source.pinnacle.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.pinnacle.service.MatchService;

/**
 * League-crawler role for Pinnacle: crawls both fixtures and odds for active sports
 * and pushes updates directly to the aggregator.
 * Only active when app.role=league-crawler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${pinnacle.fetch.delay.ms:60000}")
    public void scheduleFetch() {
        log.info("League-crawler: Starting Pinnacle line scraping cycle...");
        try {
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during Pinnacle scraping cycle", e);
        }
    }
}
