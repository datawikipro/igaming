package pro.datawiki.igaming.source.apuestatotal.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.apuestatotal.service.ApuestatotalDiscoveryService;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler", matchIfMissing = false)
public class ApuestatotalScraperScheduler {

    private final VpnManagerService vpnManagerService;
    private final ApuestatotalDiscoveryService discoveryService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${app.crawler.delay:60000}")
    public void scrapeLeagues() {
        if (!vpnManagerService.ensureProxyAlive()) {
            log.warn("Proxy is not alive. Skipping scrape cycle.");
            return;
        }

        log.info("Starting Apuesta Total scrape cycle...");
        try {
            int discovered = discoveryService.discoverEvents();
            log.info("Finished Apuesta Total scrape cycle. Discovered {} active events.", discovered);
        } catch (Exception e) {
            log.error("Error during Apuesta Total scrape cycle: {}", e.getMessage(), e);
            vpnManagerService.reportFailureAndRotate();
        }
    }
}
