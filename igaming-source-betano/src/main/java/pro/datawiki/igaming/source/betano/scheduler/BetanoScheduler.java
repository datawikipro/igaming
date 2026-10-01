package pro.datawiki.igaming.source.betano.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betano.service.BetanoMatchService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetanoScheduler {

    private final BetanoMatchService matchService;

    @Scheduled(fixedDelayString = "${betano.discovery.delay.ms:30000}", initialDelay = 5000)
    public void scheduleDiscovery() {
        log.info("Starting Betano discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Betano discovery cycle: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${betano.scrape.delay.ms:15000}", initialDelay = 10000)
    public void scheduleScrape() {
        log.info("Starting Betano line scraping cycle...");
        try {
            matchService.scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during Betano line scraping cycle: {}", e.getMessage(), e);
        }
    }
}
