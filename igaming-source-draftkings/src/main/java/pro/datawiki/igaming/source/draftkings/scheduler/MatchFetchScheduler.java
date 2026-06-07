package pro.datawiki.igaming.source.draftkings.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.draftkings.service.MatchService;

/**
 * League-crawler role: discovers events from DraftKings Sportsbook and saves metadata to match_cache.
 * Only active when app.role=league-crawler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${draftkings.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting DraftKings discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during DraftKings discovery cycle", e);
        }
    }
}
