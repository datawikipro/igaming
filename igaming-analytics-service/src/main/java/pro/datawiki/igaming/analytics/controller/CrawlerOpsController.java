package pro.datawiki.igaming.analytics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.analytics.service.CrawlerOpsService;
import pro.datawiki.igaming.dto.FleetOverviewDto;

import java.util.List;

/**
 * Controller for Crawler Operations and Ingestion Pipeline Monitoring.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/v1/crawler-ops/pipeline/stats   — Aggregated ingestion pipeline metrics & Golden Rule #8 health</li>
 *   <li>GET  /api/v1/crawler-ops/thresholds        — Bookmaker thresholds evaluation table (>= 500 active matches)</li>
 *   <li>GET  /api/v1/crawler-ops/fleet             — Direct fleet overview proxy from aggregator</li>
 *   <li>POST /api/v1/crawler-ops/fleet/refresh     — Force-refresh cached crawler fleet metrics</li>
 *   <li>POST /api/v1/crawler-ops/crawlers/{id}/probe — Instant diagnostic probe for a crawler</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/crawler-ops")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrawlerOpsController {

    private final CrawlerOpsService crawlerOpsService;

    /**
     * GET /api/v1/crawler-ops/pipeline/stats
     * Aggregated end-to-end ingestion pipeline health, Kafka message throughput,
     * active odds and matches counts, pending normalizations, and Golden Rule #8 summary.
     */
    @GetMapping("/pipeline/stats")
    public ResponseEntity<PipelineStatsDto> getPipelineStats() {
        return ResponseEntity.ok(crawlerOpsService.getPipelineStats());
    }

    /**
     * GET /api/v1/crawler-ops/thresholds
     * Per-bookmaker evaluation list against Golden Rule #8 (>= 500 matches threshold),
     * progress percentages, delay freshness, and sports breakdowns.
     */
    @GetMapping("/thresholds")
    public ResponseEntity<List<BookmakerThresholdDto>> getThresholds() {
        return ResponseEntity.ok(crawlerOpsService.getThresholds());
    }

    /**
     * GET /api/v1/crawler-ops/fleet
     * Real-time fleet overview of all 52+ scrapers, active matches, and health statuses.
     */
    @GetMapping("/fleet")
    public ResponseEntity<FleetOverviewDto> getFleet() {
        return ResponseEntity.ok(crawlerOpsService.getFleetOverview());
    }

    /**
     * POST /api/v1/crawler-ops/fleet/refresh
     * Force invalidation of cached projections and recalculate fleet status immediately.
     */
    @PostMapping("/fleet/refresh")
    public ResponseEntity<FleetOverviewDto> refreshFleet() {
        return ResponseEntity.ok(crawlerOpsService.refreshFleetOverview());
    }

    /**
     * POST /api/v1/crawler-ops/crawlers/{id}/probe
     * Immediate diagnostic probe dispatch for targeted bookmaker scraper.
     */
    @PostMapping("/crawlers/{id}/probe")
    public ResponseEntity<CrawlerProbeResultDto> probeCrawler(@PathVariable String id) {
        return ResponseEntity.ok(crawlerOpsService.probeCrawler(id));
    }
}
