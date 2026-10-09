package pro.datawiki.igaming.source.bcgame.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bcgame.service.BcgameMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bcgame.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BcgameFetchScheduler {

    private final BcgameMatchService matchService;

    @Scheduled(fixedDelayString = "${app.bcgame.discovery-interval-ms:60000}", initialDelay = 5000)
    public void scheduleDiscovery() {
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Scheduled discovery error in BC.Game: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${app.bcgame.fetch-interval-ms:30000}", initialDelay = 15000)
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Scheduled odds fetch error in BC.Game: {}", e.getMessage(), e);
        }
    }
}
