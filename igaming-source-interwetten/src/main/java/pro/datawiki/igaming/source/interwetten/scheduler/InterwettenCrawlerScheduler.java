package pro.datawiki.igaming.source.interwetten.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.interwetten.service.InterwettenDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class InterwettenCrawlerScheduler {

    private final InterwettenDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void runScheduler() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.warn("Interwetten crawler cycle error: {}", e.getMessage());
        }
    }
}