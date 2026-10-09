package pro.datawiki.igaming.source.iddaa.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.iddaa.service.IddaaDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class IddaaCrawlerScheduler {

    private final IddaaDiscoveryService discoveryService;

    @Scheduled(fixedDelayString = "${app.crawler.discovery.interval-ms:30000}")
    public void runDiscovery() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.error("Error in Iddaa crawler schedule: {}", e.getMessage());
        }
    }
}
