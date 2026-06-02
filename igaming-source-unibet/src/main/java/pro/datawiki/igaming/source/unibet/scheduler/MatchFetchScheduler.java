package pro.datawiki.igaming.source.unibet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.unibet.service.MatchService;

/**
 * League-crawler role: discovers events from Unibet (Kambi) API and saves metadata to match_cache.
 * Only active when app.role=league-crawler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${unibet.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Unibet discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Unibet discovery cycle", e);
        }
    }
}
