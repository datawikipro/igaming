package pro.datawiki.igaming.source.bet7k.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet7k.service.Bet7kDiscoveryService;
import pro.datawiki.igaming.source.bet7k.service.MatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "crawler", matchIfMissing = true)
public class Bet7kFetchScheduler {

    private final Bet7kDiscoveryService discoveryService;
    private final MatchService matchService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${app.crawler.discovery-delay:120000}")
    public void scheduleDiscovery() {
        try {
            discoveryService.discoverEvents();
        } catch (Exception e) {
            log.error("Exception during bet7k discovery: {}", e.getMessage(), e);
        }
    }

    @Scheduled(initialDelay = 15000, fixedDelayString = "${app.crawler.odds-delay:20000}")
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Exception during bet7k odds fetch: {}", e.getMessage(), e);
        }
    }
}