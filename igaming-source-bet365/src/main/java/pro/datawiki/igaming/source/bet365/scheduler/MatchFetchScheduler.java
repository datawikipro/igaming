package pro.datawiki.igaming.source.bet365.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet365.service.MatchService;

/**
 * League-crawler role: discovers events from Bet365 API and saves metadata to match_cache.
 * Only active when app.role=league-crawler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${bet365.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Bet365 discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Bet365 discovery cycle", e);
            
            // If DOM parsing failed, crash the JVM so K8s restarts the pod and flags it
            Throwable cause = e;
            boolean isDomError = false;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("DOM parsing failed")) {
                    isDomError = true;
                    break;
                }
                cause = cause.getCause();
            }
            
            if (isDomError) {
                log.error("CRITICAL: DOM layout has changed. Crashing the pod so Kubernetes restarts it and triggers alerts.");
                System.exit(1);
            }
        }
    }
}
