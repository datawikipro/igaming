package pro.datawiki.igaming.source.betdaq.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betdaq.service.BetdaqMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.betdaq.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BetdaqFetchScheduler {

    private final BetdaqMatchService matchService;

    @Scheduled(fixedDelayString = "${app.betdaq.discovery-interval-ms:60000}", initialDelay = 5000)
    public void scheduleDiscovery() {
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Scheduled discovery error in Betdaq: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${app.betdaq.fetch-interval-ms:30000}", initialDelay = 15000)
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Scheduled odds fetch error in Betdaq: {}", e.getMessage(), e);
        }
    }
}
