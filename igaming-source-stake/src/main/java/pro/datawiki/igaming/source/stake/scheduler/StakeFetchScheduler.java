package pro.datawiki.igaming.source.stake.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.stake.service.StakeMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.stake.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class StakeFetchScheduler {

    private final StakeMatchService matchService;

    @Scheduled(fixedDelayString = "${app.stake.discovery-interval-ms:60000}", initialDelay = 5000)
    public void scheduleDiscovery() {
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Scheduled discovery error in Stake: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${app.stake.fetch-interval-ms:30000}", initialDelay = 15000)
    public void scheduleOddsFetch() {
        try {
            matchService.fetchAndPushOdds();
        } catch (Exception e) {
            log.error("Scheduled odds fetch error in Stake: {}", e.getMessage(), e);
        }
    }
}
