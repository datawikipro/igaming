package pro.datawiki.igaming.source.tab.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.tab.service.TabDiscoveryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class TabCrawlerScheduler {

    private final TabDiscoveryService discoveryService;

    @Scheduled(fixedDelayString = "${app.crawler.discovery.interval-ms:30000}")
    public void runDiscovery() {
        try {
            discoveryService.discoverAndSync();
        } catch (Exception e) {
            log.error("Error in Tab crawler schedule: {}", e.getMessage());
        }
    }
}
