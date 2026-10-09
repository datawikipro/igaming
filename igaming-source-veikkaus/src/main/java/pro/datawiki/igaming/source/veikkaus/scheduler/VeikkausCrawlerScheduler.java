package pro.datawiki.igaming.source.veikkaus.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.veikkaus.service.VeikkausDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class VeikkausCrawlerScheduler {

    private final VeikkausDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void runScheduler() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.warn("Veikkaus crawler cycle error: {}", e.getMessage());
        }
    }
}