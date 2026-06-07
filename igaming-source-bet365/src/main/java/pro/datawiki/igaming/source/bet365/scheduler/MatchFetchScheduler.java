package pro.datawiki.igaming.source.bet365.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet365.service.MatchService;
import pro.datawiki.igaming.source.core.service.VpnManagerService;

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
    private final VpnManagerService vpnManagerService;
    private int consecutiveDomFailures = 0;

    @Scheduled(fixedDelayString = "${bet365.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Bet365 discovery cycle...");
        try {
            matchService.discoverEvents();
            consecutiveDomFailures = 0; // reset on success
        } catch (Exception e) {
            log.error("Error during Bet365 discovery cycle", e);
            
            // If DOM parsing failed, crash the JVM after 3 attempts with proxy rotation
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
                consecutiveDomFailures++;
                log.warn("DOM parsing failed (attempt {}/3). Rotating proxy and retrying on next cycle.", consecutiveDomFailures);
                try {
                    vpnManagerService.reportFailureAndRotate();
                } catch (Exception ex) {
                    log.error("Failed to rotate proxy: {}", ex.getMessage());
                }
                
                if (consecutiveDomFailures >= 3) {
                    log.error("CRITICAL: DOM layout has changed. 3 consecutive failures. Crashing the pod so Kubernetes restarts it and triggers alerts.");
                    System.exit(1);
                }
            }
        }
    }
}
