package pro.datawiki.igaming.source.vaidebet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.vaidebet.service.VaidebetDiscoveryService;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler", matchIfMissing = false)
public class VaidebetScraperScheduler {

    private final VpnManagerService vpnManagerService;
    private final VaidebetDiscoveryService discoveryService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${app.crawler.delay:60000}")
    public void scrapeLeagues() {
        if (!vpnManagerService.ensureProxyAlive()) {
            log.warn("Proxy is not alive. Skipping scrape cycle.");
            return;
        }

        log.info("Starting Vaidebet scrape cycle...");
        try {
            int discovered = discoveryService.discoverEvents();
            log.info("Finished Vaidebet scrape cycle. Discovered {} active events.", discovered);
        } catch (Exception e) {
            log.error("Error during Vaidebet scrape cycle: {}", e.getMessage(), e);
            vpnManagerService.reportFailureAndRotate();
        }
    }
}
