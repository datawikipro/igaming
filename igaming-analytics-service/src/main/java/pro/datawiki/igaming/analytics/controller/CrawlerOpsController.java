package pro.datawiki.igaming.analytics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analytics.dto.crawler.*;
import pro.datawiki.igaming.analytics.service.CrawlerOpsService;

import java.util.List;

/**
 * REST API for Crawler Ingestion Pipeline Operations & Rule 8 Thresholds Monitor.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/crawler-ops")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrawlerOpsController {

    private final CrawlerOpsService crawlerOpsService;

    /**
     * Get real-time status and metrics of all 52 crawler ingestion pipelines.
     */
    @GetMapping("/pipelines")
    public ResponseEntity<List<CrawlerPipelineDto>> getPipelines() {
        return ResponseEntity.ok(crawlerOpsService.getAllPipelines());
    }

    /**
     * Get pipeline status for a specific bookmaker.
     */
    @GetMapping("/pipelines/{bookmaker}")
    public ResponseEntity<CrawlerPipelineDto> getPipeline(@PathVariable String bookmaker) {
        CrawlerPipelineDto dto = crawlerOpsService.getPipeline(bookmaker);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Summary statistics across all 52 pipelines.
     */
    @GetMapping("/pipelines/stats")
    public ResponseEntity<PipelineStatsDto> getPipelineStats() {
        return ResponseEntity.ok(crawlerOpsService.getPipelineStats());
    }

    /**
     * Get all active threshold rules (global + per-bookmaker overrides).
     */
    @GetMapping("/thresholds")
    public ResponseEntity<List<ThresholdRuleDto>> getThresholds() {
        return ResponseEntity.ok(crawlerOpsService.getAllThresholds());
    }

    /**
     * Get threshold rule for a specific bookmaker.
     */
    @GetMapping("/thresholds/{bookmaker}")
    public ResponseEntity<ThresholdRuleDto> getThreshold(@PathVariable String bookmaker) {
        return ResponseEntity.ok(crawlerOpsService.getThreshold(bookmaker));
    }

    /**
     * Create or update a threshold rule.
     */
    @PostMapping("/thresholds")
    public ResponseEntity<ThresholdRuleDto> updateThreshold(@RequestBody ThresholdRuleDto rule) {
        return ResponseEntity.ok(crawlerOpsService.updateThreshold(rule));
    }

    /**
     * Trigger on-demand sync / crawl pass for a bookmaker.
     */
    @PostMapping("/pipelines/{bookmaker}/sync")
    public ResponseEntity<TriggerSyncResponseDto> triggerSync(@PathVariable String bookmaker) {
        return ResponseEntity.ok(crawlerOpsService.triggerSync(bookmaker));
    }

    /**
     * Get active threshold violations and crawler degradation alerts.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<PipelineAlertDto>> getActiveAlerts() {
        return ResponseEntity.ok(crawlerOpsService.getActiveAlerts());
    }
}
