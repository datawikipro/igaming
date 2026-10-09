package pro.datawiki.igaming.source.bet9ja.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet9ja.service.Bet9jaDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class Bet9jaCrawlerScheduler {

    private final Bet9jaDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void runScheduler() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.warn("Bet9ja crawler cycle error: {}", e.getMessage());
        }
    }
}