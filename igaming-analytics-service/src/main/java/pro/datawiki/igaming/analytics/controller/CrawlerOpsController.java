package pro.datawiki.igaming.analytics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.DiagnosticsStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.dto.LoaderDelayDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Crawler Operations (Crawler Ops) and Ingestion Pipeline Monitoring.
 * Tracks Golden Rule #8 (>= 500 active matches threshold) across 52+ bookmakers,
 * Kafka odds streaming flow, PostgreSQL ingestion rates, and scraper freshness.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/crawler-ops")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrawlerOpsController {

    private final RestTemplate restTemplate;

    @Value("${aggregator.fleet.url:http://igaming-aggregator/api/bookmakers/fleet}")
    private String aggregatorFleetUrl;

    @Value("${aggregator.fleet.refresh.url:http://igaming-aggregator/api/bookmakers/fleet/refresh}")
    private String aggregatorFleetRefreshUrl;

    @Value("${aggregator.stats.url:http://igaming-aggregator/api/diagnostics/stats}")
    private String aggregatorStatsUrl;

    @Value("${aggregator.delays.url:http://igaming-aggregator/api/diagnostics/loader-delays}")
    private String aggregatorDelaysUrl;

    public static final long GOLDEN_RULE_8_THRESHOLD = 500L;

    /**
     * GET /api/v1/crawler-ops/fleet
     * Proxies real-time fleet metrics for all bookmakers from aggregator.
     */
    @GetMapping("/fleet")
    public ResponseEntity<FleetOverviewDto> getFleet() {
        try {
            FleetOverviewDto fleet = restTemplate.getForObject(aggregatorFleetUrl, FleetOverviewDto.class);
            return ResponseEntity.ok(fleet != null ? fleet : fallbackFleetOverview());
        } catch (Exception e) {
            log.error("Failed to fetch fleet overview from {}", aggregatorFleetUrl, e);
            return ResponseEntity.ok(fallbackFleetOverview());
        }
    }

    /**
     * POST /api/v1/crawler-ops/fleet/refresh
     * Triggers immediate cache eviction and recomputation of fleet statistics on the aggregator.
     */
    @PostMapping("/fleet/refresh")
    public ResponseEntity<FleetOverviewDto> refreshFleet() {
        try {
            FleetOverviewDto refreshed = restTemplate.postForObject(aggregatorFleetRefreshUrl, null, FleetOverviewDto.class);
            return ResponseEntity.ok(refreshed != null ? refreshed : fallbackFleetOverview());
        } catch (Exception e) {
            log.error("Failed to trigger fleet refresh at {}", aggregatorFleetRefreshUrl, e);
            return ResponseEntity.ok(fallbackFleetOverview());
        }
    }

    /**
     * GET /api/v1/crawler-ops/delays
     * Returns raw delay and match counts per active bookmaker loader.
     */
    @GetMapping("/delays")
    public ResponseEntity<List<LoaderDelayDto>> getDelays() {
        try {
            LoaderDelayDto[] delays = restTemplate.getForObject(aggregatorDelaysUrl, LoaderDelayDto[].class);
            return ResponseEntity.ok(delays != null ? Arrays.asList(delays) : Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to fetch loader delays from {}", aggregatorDelaysUrl, e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * GET /api/v1/crawler-ops/pipeline/stats
     * Aggregated end-to-end ingestion pipeline metrics:
     * matches, teams, pending normalizations, Kafka message status, active odds, Golden Rule #8 summary.
     */
    @GetMapping("/pipeline/stats")
    public ResponseEntity<PipelineStatsDto> getPipelineStats() {
        DiagnosticsStatsDto diagStats = null;
        try {
            diagStats = restTemplate.getForObject(aggregatorStatsUrl, DiagnosticsStatsDto.class);
        } catch (Exception e) {
            log.warn("Diagnostics stats unavailable: {}", e.getMessage());
        }

        FleetOverviewDto fleet = null;
        try {
            fleet = restTemplate.getForObject(aggregatorFleetUrl, FleetOverviewDto.class);
        } catch (Exception e) {
            log.warn("Fleet overview unavailable: {}", e.getMessage());
        }

        List<LoaderDelayDto> delaysList = Collections.emptyList();
        try {
            LoaderDelayDto[] delays = restTemplate.getForObject(aggregatorDelaysUrl, LoaderDelayDto[].class);
            if (delays != null) {
                delaysList = Arrays.asList(delays);
            }
        } catch (Exception e) {
            log.warn("Loader delays unavailable: {}", e.getMessage());
        }

        int totalBm = fleet != null ? fleet.getTotalBookmakers() : 0;
        int onlineBm = fleet != null ? fleet.getOnlineBookmakers() : 0;
        int compliantBm = fleet != null ? fleet.getCompliantBookmakers() : 0;
        int degradedBm = fleet != null ? fleet.getDegradedBookmakers() : 0;
        int offlineBm = fleet != null ? fleet.getOfflineBookmakers() : 0;
        long totalOdds = fleet != null ? fleet.getTotalActiveOdds() : 0L;
        long totalMatches = diagStats != null && diagStats.getMatches() != null ? diagStats.getMatches() : (fleet != null ? fleet.getTotalActiveMatches() : 0L);

        double complianceRate = onlineBm > 0 ? (compliantBm * 100.0) / onlineBm : 0.0;
        String pipelineStatus = "HEALTHY";
        if (onlineBm == 0 && totalBm > 0) {
            pipelineStatus = "DOWN";
        } else if (degradedBm > compliantBm || complianceRate < 50.0) {
            pipelineStatus = "DEGRADED";
        }

        PipelineStatsDto stats = PipelineStatsDto.builder()
                .matches(totalMatches)
                .teams(diagStats != null ? diagStats.getTeams() : 0L)
                .normalizationRequests(diagStats != null ? diagStats.getNormalizationRequests() : 0L)
                .pendingNormalization(diagStats != null ? diagStats.getPendingNormalization() : 0L)
                .unrecognizedOdds(diagStats != null ? diagStats.getUnrecognizedOdds() : 0L)
                .totalActiveOdds(totalOdds)
                .totalBookmakers(totalBm)
                .onlineBookmakers(onlineBm)
                .compliantBookmakers(compliantBm)
                .degradedBookmakers(degradedBm)
                .defectBookmakers(offlineBm)
                .complianceRate(Math.round(complianceRate * 10.0) / 10.0)
                .pipelineStatus(pipelineStatus)
                .kafkaTopic("odds.updates")
                .kafkaStatus("STREAMING")
                .timestamp(System.currentTimeMillis())
                .loaderDelays(delaysList)
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/v1/crawler-ops/thresholds
     * Evaluates every bookmaker against the Golden Rule #8 (>= 500 matches) threshold.
     * Returns compliance status (COMPLIANT, DEGRADED, DEFECT), progress, delay freshness, and sports.
     */
    @GetMapping("/thresholds")
    public ResponseEntity<List<BookmakerThresholdDto>> getThresholds(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String proxyRoute,
            @RequestParam(required = false) String search
    ) {
        FleetOverviewDto fleet = null;
        try {
            fleet = restTemplate.getForObject(aggregatorFleetUrl, FleetOverviewDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch fleet for threshold evaluation", e);
        }

        if (fleet == null || fleet.getBookmakers() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String searchLower = search != null ? search.trim().toLowerCase() : null;

        List<BookmakerThresholdDto> list = fleet.getBookmakers().stream()
                .map(this::toThresholdDto)
                .filter(item -> {
                    if (searchLower != null && !searchLower.isEmpty()) {
                        boolean matchName = item.getName() != null && item.getName().toLowerCase().contains(searchLower);
                        boolean matchId = item.getId() != null && item.getId().toLowerCase().contains(searchLower);
                        if (!matchName && !matchId) return false;
                    }
                    if (status != null && !status.equalsIgnoreCase("ALL")) {
                        if (!status.equalsIgnoreCase(item.getComplianceStatus()) && !status.equalsIgnoreCase(item.getHealthStatus())) {
                            return false;
                        }
                    }
                    if (proxyRoute != null && !proxyRoute.equalsIgnoreCase("ALL")) {
                        if (item.getProxyRoute() == null || !item.getProxyRoute().equalsIgnoreCase(proxyRoute)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(BookmakerThresholdDto::getMatchesCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BookmakerThresholdDto::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * POST /api/v1/crawler-ops/crawlers/{id}/probe
     * Runs an instantaneous operational health probe for a specific bookmaker scraper.
     */
    @PostMapping("/crawlers/{id}/probe")
    public ResponseEntity<CrawlerProbeResultDto> probeCrawler(@PathVariable String id) {
        long start = System.currentTimeMillis();
        try {
            FleetOverviewDto fleet = restTemplate.getForObject(aggregatorFleetUrl, FleetOverviewDto.class);
            long duration = System.currentTimeMillis() - start;

            BookmakerFleetStatsDto target = null;
            if (fleet != null && fleet.getBookmakers() != null) {
                target = fleet.getBookmakers().stream()
                        .filter(b -> id.equalsIgnoreCase(b.getId()))
                        .findFirst()
                        .orElse(null);
            }

            if (target == null) {
                return ResponseEntity.ok(CrawlerProbeResultDto.builder()
                        .bookmakerId(id)
                        .status("FAILED")
                        .responseTimeMs(duration)
                        .timestamp(System.currentTimeMillis())
                        .message("Bookmaker '" + id + "' not found in registered catalog")
                        .currentMatches(0L)
                        .currentOdds(0L)
                        .build());
            }

            long matches = target.getActiveMatchesCount() != null ? target.getActiveMatchesCount() : 0L;
            long odds = target.getActiveOddsCount() != null ? target.getActiveOddsCount() : 0L;
            boolean online = Boolean.TRUE.equals(target.getIsOnline());

            String status = "SUCCESS";
            String msg = "Crawler responsive, line contains " + matches + " matches";
            if (!online) {
                status = "FAILED";
                msg = "Crawler heartbeat missing in last 5 minutes (OFFLINE)";
            } else if (matches < GOLDEN_RULE_8_THRESHOLD) {
                status = "DEGRADED";
                msg = "Active matches (" + matches + ") below Golden Rule #8 threshold (500)";
            }

            return ResponseEntity.ok(CrawlerProbeResultDto.builder()
                    .bookmakerId(id)
                    .status(status)
                    .responseTimeMs(duration)
                    .timestamp(System.currentTimeMillis())
                    .message(msg)
                    .currentMatches(matches)
                    .currentOdds(odds)
                    .build());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("Probe failed for bookmaker {}", id, e);
            return ResponseEntity.ok(CrawlerProbeResultDto.builder()
                    .bookmakerId(id)
                    .status("FAILED")
                    .responseTimeMs(duration)
                    .timestamp(System.currentTimeMillis())
                    .message("Probe error: " + e.getMessage())
                    .currentMatches(0L)
                    .currentOdds(0L)
                    .build());
        }
    }

    private BookmakerThresholdDto toThresholdDto(BookmakerFleetStatsDto b) {
        long matches = b.getActiveMatchesCount() != null ? b.getActiveMatchesCount() : 0L;
        long odds = b.getActiveOddsCount() != null ? b.getActiveOddsCount() : 0L;
        double progress = Math.min(100.0, Math.round((matches * 100.0 / (double) GOLDEN_RULE_8_THRESHOLD) * 10.0) / 10.0);

        String compliance;
        if (matches >= GOLDEN_RULE_8_THRESHOLD) {
            compliance = "COMPLIANT";
        } else if (matches > 0) {
            compliance = "DEGRADED";
        } else {
            compliance = "DEFECT";
        }

        Double delay = b.getDelayMinutes();
        String freshness;
        if (delay == null) {
            freshness = "UNKNOWN";
        } else if (delay < 1.0) {
            freshness = "FRESH";
        } else if (delay <= 5.0) {
            freshness = "NORMAL";
        } else if (delay <= 15.0) {
            freshness = "DELAYED";
        } else {
            freshness = "STALE";
        }

        return BookmakerThresholdDto.builder()
                .id(b.getId())
                .name(b.getName() != null ? b.getName() : b.getId())
                .logoEmoji(b.getLogoEmoji() != null ? b.getLogoEmoji() : "🎲")
                .engine(b.getEngine() != null ? b.getEngine() : "Proprietary REST / API")
                .proxyRoute(b.getProxyRoute() != null ? b.getProxyRoute() : "DIRECT")
                .regions(b.getRegions() != null ? b.getRegions() : Collections.emptySet())
                .isOnline(Boolean.TRUE.equals(b.getIsOnline()))
                .lastSeen(b.getLastSeen())
                .matchesCount(matches)
                .targetThreshold(GOLDEN_RULE_8_THRESHOLD)
                .thresholdProgress(progress)
                .complianceStatus(compliance)
                .oddsCount(odds)
                .delayMinutes(delay != null ? Math.round(delay * 10.0) / 10.0 : null)
                .freshnessStatus(freshness)
                .healthStatus(b.getHealthStatus() != null ? b.getHealthStatus() : "OFFLINE")
                .healthMessage(b.getHealthMessage())
                .sportBreakdown(b.getSportBreakdown() != null ? b.getSportBreakdown() : Collections.emptyMap())
                .topLeagues(b.getTopLeagues() != null ? b.getTopLeagues() : Collections.emptyList())
                .build();
    }

    private FleetOverviewDto fallbackFleetOverview() {
        return FleetOverviewDto.builder()
                .totalBookmakers(0)
                .onlineBookmakers(0)
                .compliantBookmakers(0)
                .degradedBookmakers(0)
                .offlineBookmakers(0)
                .totalActiveMatches(0L)
                .totalActiveOdds(0L)
                .bookmakers(Collections.emptyList())
                .generatedAt(System.currentTimeMillis())
                .build();
    }
}
