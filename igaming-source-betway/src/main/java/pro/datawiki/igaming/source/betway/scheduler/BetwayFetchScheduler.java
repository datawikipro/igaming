package pro.datawiki.igaming.source.betway.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betway.service.MatchService;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "crawler", matchIfMissing = true)
public class BetwayFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${app.crawler.discovery-delay:120000}", initialDelay = 5000)
    public void runDiscovery() {
        try {
            log.info("Starting scheduled Betway discovery...");
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Betway scheduled discovery: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.crawler.odds-delay:20000}", initialDelay = 15000)
    public void runOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Error during Betway scheduled odds refresh: {}", e.getMessage());
        }
    }
}
