package pro.datawiki.igaming.source.wplay.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.wplay.service.WplayDiscoveryService;
import pro.datawiki.igaming.source.wplay.service.WplayMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "crawler", matchIfMissing = true)
public class WplayScheduler {

    private final WplayDiscoveryService discoveryService;
    private final WplayMatchService matchService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${app.crawler.discovery-delay:120000}")
    public void scheduleDiscovery() {
        try {
            discoveryService.discoverEvents();
        } catch (Exception e) {
            log.error("Exception during Wplay discovery: {}", e.getMessage(), e);
        }
    }

    @Scheduled(initialDelay = 25000, fixedDelayString = "${app.crawler.odds-delay:30000}")
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Exception during Wplay odds fetch: {}", e.getMessage(), e);
        }
    }
}
