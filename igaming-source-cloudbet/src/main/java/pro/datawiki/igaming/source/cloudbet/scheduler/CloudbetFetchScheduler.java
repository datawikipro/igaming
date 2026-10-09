package pro.datawiki.igaming.source.cloudbet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.cloudbet.service.CloudbetMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cloudbet.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class CloudbetFetchScheduler {

    private final CloudbetMatchService matchService;

    @Scheduled(fixedDelayString = "${app.cloudbet.discovery-interval-ms:60000}", initialDelay = 5000)
    public void scheduleDiscovery() {
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Scheduled discovery error in Cloudbet: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${app.cloudbet.fetch-interval-ms:30000}", initialDelay = 15000)
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Scheduled odds fetch error in Cloudbet: {}", e.getMessage(), e);
        }
    }
}
