package pro.datawiki.igaming.capture.esports.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.capture.esports.crawler.HltvCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.OpenDotaCrawlerService;
import pro.datawiki.igaming.capture.esports.crawler.VlrCrawlerService;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;
import pro.datawiki.igaming.capture.esports.repository.EsportsRawEventRepository;
import pro.datawiki.igaming.capture.esports.service.EsportsParquetExporterService;
import pro.datawiki.igaming.capture.esports.service.EsportsRawEventService;
import pro.datawiki.igaming.capture.esports.service.EsportsRetroactiveBackfillService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Management and On-Demand Controller for Esports Capture Service.
 */
@RestController
@RequestMapping("/api/v1/esports")
@RequiredArgsConstructor
@Slf4j
public class EsportsCaptureController {

    private final HltvCrawlerService hltvCrawlerService;
    private final OpenDotaCrawlerService openDotaCrawlerService;
    private final VlrCrawlerService vlrCrawlerService;
    private final EsportsRawEventService esportsRawEventService;
    private final EsportsRetroactiveBackfillService backfillService;
    private final EsportsParquetExporterService parquetExporterService;
    private final EsportsRawEventRepository repository;

    @PostMapping("/crawl/cs2")
    public ResponseEntity<Map<String, Object>> triggerCs2Crawl() {
        log.info("API trigger: CS2 HLTV crawl");
        List<HltvCrawlerService.ParsedCs2Match> matches = hltvCrawlerService.crawlMatches();
        List<String> ingestedIds = new ArrayList<>();
        for (HltvCrawlerService.ParsedCs2Match m : matches) {
            EsportsRawEventEntity entity = esportsRawEventService.ingestHltvMatch(m);
            ingestedIds.add(entity.getEventId());
        }
        return ResponseEntity.ok(Map.of(
                "game", "CS2",
                "crawledCount", matches.size(),
                "ingestedEventIds", ingestedIds
        ));
    }

    @PostMapping("/crawl/dota2")
    public ResponseEntity<Map<String, Object>> triggerDota2Crawl() {
        log.info("API trigger: Dota 2 OpenDota crawl");
        List<OpenDotaCrawlerService.ParsedDota2Match> matches = openDotaCrawlerService.crawlProMatches();
        List<String> ingestedIds = new ArrayList<>();
        for (OpenDotaCrawlerService.ParsedDota2Match m : matches) {
            EsportsRawEventEntity entity = esportsRawEventService.ingestOpenDotaMatch(m);
            ingestedIds.add(entity.getEventId());
        }
        return ResponseEntity.ok(Map.of(
                "game", "DOTA2",
                "crawledCount", matches.size(),
                "ingestedEventIds", ingestedIds
        ));
    }

    @PostMapping("/crawl/valorant")
    public ResponseEntity<Map<String, Object>> triggerValorantCrawl() {
        log.info("API trigger: Valorant crawl");
        List<VlrCrawlerService.ParsedValorantMatch> matches = vlrCrawlerService.crawlMatches();
        List<String> ingestedIds = new ArrayList<>();
        for (VlrCrawlerService.ParsedValorantMatch m : matches) {
            EsportsRawEventEntity entity = esportsRawEventService.ingestValorantMatch(m);
            ingestedIds.add(entity.getEventId());
        }
        return ResponseEntity.ok(Map.of(
                "game", "VALORANT",
                "crawledCount", matches.size(),
                "ingestedEventIds", ingestedIds
        ));
    }

    @PostMapping("/crawl/all")
    public ResponseEntity<Map<String, Object>> triggerAllCrawlers() {
        triggerCs2Crawl();
        triggerDota2Crawl();
        triggerValorantCrawl();
        return ResponseEntity.ok(Map.of("status", "TRIGGERED_ALL"));
    }

    @PostMapping("/backfill")
    public ResponseEntity<EsportsRetroactiveBackfillService.BackfillReport> triggerBackfill() {
        log.info("API trigger: Retroactive Backfill");
        EsportsRetroactiveBackfillService.BackfillReport report = backfillService.executeBackfill();
        return ResponseEntity.ok(report);
    }

    @PostMapping("/export/parquet")
    public ResponseEntity<EsportsParquetExporterService.ExportResult> triggerExport(
            @RequestParam(required = false) String game) {
        log.info("API trigger: Parquet Export (game: {})", game);
        EsportsParquetExporterService.ExportResult result = parquetExporterService.exportFlatDataset(game);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long mappedCount = repository.countByMdmStatus("MAPPED");
        long unmappedCount = repository.countByMdmStatus("UNMAPPED");
        List<Object[]> breakdown = repository.countSummaryByGameAndMdmStatus();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", repository.count());
        stats.put("mappedEvents", mappedCount);
        stats.put("unmappedEvents", unmappedCount);
        stats.put("breakdown", breakdown);

        return ResponseEntity.ok(stats);
    }
}
