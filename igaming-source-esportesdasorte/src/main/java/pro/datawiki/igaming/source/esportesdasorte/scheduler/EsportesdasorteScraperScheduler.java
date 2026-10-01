package pro.datawiki.igaming.source.esportesdasorte.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.esportesdasorte.service.EsportesdasorteDiscoveryService;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler", matchIfMissing = false)
public class EsportesdasorteScraperScheduler {

    private final VpnManagerService vpnManagerService;
    private final EsportesdasorteDiscoveryService discoveryService;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${app.crawler.delay:60000}")
    public void scrapeLeagues() {
        if (!vpnManagerService.ensureProxyAlive()) {
            log.warn("Proxy is not alive. Skipping scrape cycle.");
            return;
        }

        log.info("Starting Esportes da Sorte scrape cycle...");
        try {
            int discovered = discoveryService.discoverEvents();
            log.info("Finished Esportes da Sorte scrape cycle. Discovered {} active events.", discovered);
        } catch (Exception e) {
            log.error("Error during Esportes da Sorte scrape cycle: {}", e.getMessage(), e);
            vpnManagerService.reportFailureAndRotate();
        }
    }
}
