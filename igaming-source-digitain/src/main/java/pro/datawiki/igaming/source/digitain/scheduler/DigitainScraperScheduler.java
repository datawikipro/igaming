package pro.datawiki.igaming.source.digitain.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.service.VpnManagerService;
import pro.datawiki.igaming.source.digitain.service.DigitainApiClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Profile("league-crawler")
public class DigitainScraperScheduler {

    private static final Logger log = LoggerFactory.getLogger(DigitainScraperScheduler.class);

    private final VpnManagerService vpnManagerService;
    private final DigitainApiClient apiClient;

    public DigitainScraperScheduler(VpnManagerService vpnManagerService, DigitainApiClient apiClient) {
        this.vpnManagerService = vpnManagerService;
        this.apiClient = apiClient;
    }

    @Scheduled(fixedDelayString = "${app.crawler.delay:60000}")
    public void scrapeLeagues() {
        if (!vpnManagerService.ensureProxyAlive()) {
            log.warn("Proxy is not alive. Skipping scrape cycle.");
            return;
        }

        log.info("Starting Digitain scrape cycle...");
        try {
            // TODO: Fetch sports, leagues, matches using apiClient
            apiClient.fetchSports();
            
            log.info("Finished Digitain scrape cycle.");
        } catch (Exception e) {
            log.error("Error during Digitain scrape cycle", e);
            vpnManagerService.reportFailureAndRotate();
        }
    }
}
