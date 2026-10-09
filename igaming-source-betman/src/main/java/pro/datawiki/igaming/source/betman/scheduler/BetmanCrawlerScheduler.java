package pro.datawiki.igaming.source.betman.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betman.service.BetmanDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetmanCrawlerScheduler {

    private final BetmanDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void runScheduler() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.warn("Betman crawler cycle error: {}", e.getMessage());
        }
    }
}