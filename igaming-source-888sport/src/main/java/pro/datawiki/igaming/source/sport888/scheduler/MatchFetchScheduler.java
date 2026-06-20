package pro.datawiki.igaming.source.sport888.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.sport888.service.MatchService;

/**
 * League-crawler role: discovers events from Sport888 (Kambi) API and saves metadata to match_cache.
 * Only active when app.role=league-crawler.
 */
@Component
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchFetchScheduler.class);

    private final MatchService matchService;

    public MatchFetchScheduler(MatchService matchService) {
        this.matchService = matchService;
    }

    @Scheduled(fixedDelayString = "${888sport.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Sport888 discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Sport888 discovery cycle", e);
        }
    }
}
