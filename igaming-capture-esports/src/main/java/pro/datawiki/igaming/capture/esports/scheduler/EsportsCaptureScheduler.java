package pro.datawiki.igaming.capture.esports.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.capture.esports.crawler.HltvCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.OpenDotaCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.VlrCrawlerService;
import pro.datawiki.igaming.capture.esports.service.EsportsRawEventService;
import pro.datawiki.igaming.capture.esports.service.EsportsRetroactiveBackfillService;

import java.util.List;

/**
 * Scheduled background coordination for esports crawlers and retroactive backfills.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EsportsCaptureScheduler {

    private final HltvCrawlerService hltvCrawlerService;
    private final OpenDotaCrawlerService openDotaCrawlerService;
    private final VlrCrawlerService vlrCrawlerService;
    private final EsportsRawEventService esportsRawEventService;
    private final EsportsRetroactiveBackfillService backfillService;

    @Value("${app.capture.esports.hltv.enabled:true}")
    private boolean hltvEnabled;

    @Value("${app.capture.esports.opendota.enabled:true}")
    private boolean openDotaEnabled;

    @Value("${app.capture.esports.vlr.enabled:true}")
    private boolean vlrEnabled;

    /**
     * Periodic CS2 match crawl (Tier-1 to Tier-4).
     */
    @Scheduled(fixedDelayString = "${app.capture.esports.crawl-interval-ms:300000}", initialDelay = 10000)
    public void runHltvCrawl() {
        if (!hltvEnabled) return;
        try {
            log.info("⏰ [SCHEDULER-CS2] Executing scheduled HLTV match crawl...");
            List<HltvCrawlerService.ParsedCs2Match> matches = hltvCrawlerService.crawlMatches();
            for (HltvCrawlerService.ParsedCs2Match m : matches) {
                esportsRawEventService.ingestHltvMatch(m);
            }
        } catch (Exception e) {
            log.error("Error during scheduled HLTV crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Periodic Dota 2 pro match crawl.
     */
    @Scheduled(fixedDelayString = "${app.capture.esports.crawl-interval-ms:300000}", initialDelay = 20000)
    public void runOpenDotaCrawl() {
        if (!openDotaEnabled) return;
        try {
            log.info("⏰ [SCHEDULER-DOTA2] Executing scheduled OpenDota pro match crawl...");
            List<OpenDotaCrawlerService.ParsedDota2Match> matches = openDotaCrawlerService.crawlProMatches();
            for (OpenDotaCrawlerService.ParsedDota2Match m : matches) {
                esportsRawEventService.ingestOpenDotaMatch(m);
            }
        } catch (Exception e) {
            log.error("Error during scheduled OpenDota crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Periodic Valorant match crawl.
     */
    @Scheduled(fixedDelayString = "${app.capture.esports.crawl-interval-ms:300000}", initialDelay = 30000)
    public void runValorantCrawl() {
        if (!vlrEnabled) return;
        try {
            log.info("⏰ [SCHEDULER-VALORANT] Executing scheduled Valorant match crawl...");
            List<VlrCrawlerService.ParsedValorantMatch> matches = vlrCrawlerService.crawlMatches();
            for (VlrCrawlerService.ParsedValorantMatch m : matches) {
                esportsRawEventService.ingestValorantMatch(m);
            }
        } catch (Exception e) {
            log.error("Error during scheduled Valorant crawl: {}", e.getMessage(), e);
        }
    }

    /**
     * Periodic Retroactive Backfill for unmapped matches upon Blueprint refresh.
     */
    @Scheduled(fixedDelayString = "${app.capture.esports.backfill-interval-ms:600000}", initialDelay = 60000)
    public void runRetroactiveBackfill() {
        try {
            log.info("⏰ [SCHEDULER-BACKFILL] Triggering periodic Retroactive Backfill...");
            backfillService.executeBackfill();
        } catch (Exception e) {
            log.error("Error during scheduled retroactive backfill: {}", e.getMessage(), e);
        }
    }
}
