package pro.datawiki.igaming.k8s.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.k8s.dto.LoaderDelayDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serves /api/v1/analytics/* and /api/v1/notifications/* endpoints
 * consumed by the igaming-admin-frontend dashboard.
 *
 * Analytics stats are derived from the igaming-aggregator diagnostics API.
 * System health is synthesised from those same delay metrics.
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class AggregatorStatsController {

    private final RestTemplate restTemplate;

    @Value("${aggregator.delay.url:http://igaming-aggregator/api/diagnostics/loader-delays}")
    private String aggregatorDelayUrl;

    @Value("${aggregator.stats.url:http://igaming-aggregator/api/diagnostics/stats}")
    private String aggregatorStatsUrl;

    /** Max acceptable delay (minutes) before we consider the system degraded. */
    private static final double MAX_OK_DELAY_MINUTES = 30.0;

    public AggregatorStatsController() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    // ------------------------------------------------------------------ //
    //  /api/v1/analytics/aggregator/stats                                 //
    // ------------------------------------------------------------------ //

    @GetMapping("/api/v1/analytics/aggregator/stats")
    public ResponseEntity<AggregatorStats> getAggregatorStats() {
        // First try the dedicated stats endpoint on the aggregator
        try {
            ResponseEntity<AggregatorStats> direct = restTemplate.exchange(
                    aggregatorStatsUrl, HttpMethod.GET, null, AggregatorStats.class);
            if (direct.getBody() != null) {
                return ResponseEntity.ok(direct.getBody());
            }
        } catch (Exception e) {
            log.debug("Aggregator stats endpoint not available ({}), falling back to delay data", e.getMessage());
        }

        // Fallback: derive stats from the loader-delays endpoint (always present)
        AggregatorStats stats = buildStatsFromDelays();
        return ResponseEntity.ok(stats);
    }

    // ------------------------------------------------------------------ //
    //  /api/v1/notifications/health/system                                //
    // ------------------------------------------------------------------ //

    @GetMapping("/api/v1/notifications/health/system")
    public ResponseEntity<SystemHealth> getSystemHealth() {
        List<LoaderDelayDto> delays = fetchDelays();

        List<String> issues = new ArrayList<>();
        double maxDelay = 0.0;

        for (LoaderDelayDto d : delays) {
            if (d.getDelayMinutes() != null && d.getDelayMinutes() > MAX_OK_DELAY_MINUTES) {
                issues.add(d.getBookmaker() + " lag " + d.getDelayMinutes().intValue() + " min");
                if (d.getDelayMinutes() > maxDelay) {
                    maxDelay = d.getDelayMinutes();
                }
            }
        }

        String status = issues.isEmpty() ? "OK" : (maxDelay > 60 ? "CRITICAL" : "DEGRADED");

        SystemHealth health = new SystemHealth(status, issues, (int) maxDelay);
        return ResponseEntity.ok(health);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private AggregatorStats buildStatsFromDelays() {
        List<LoaderDelayDto> delays = fetchDelays();

        long totalMatches = delays.stream()
                .mapToLong(d -> d.getMatchesCount() != null ? d.getMatchesCount() : 0L)
                .sum();
        long totalOdds = delays.stream()
                .mapToLong(d -> d.getOddsCount() != null ? d.getOddsCount() : 0L)
                .sum();

        return new AggregatorStats(
                totalMatches,
                0L,   // teams — not available from delay API
                totalOdds,
                0L,   // pending_normalization
                0L,   // unrecognized_odds
                0L,   // matches_with_results
                0L    // matches_pending_results
        );
    }

    private List<LoaderDelayDto> fetchDelays() {
        try {
            ResponseEntity<List<LoaderDelayDto>> response = restTemplate.exchange(
                    aggregatorDelayUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<LoaderDelayDto>>() {}
            );
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch delays from aggregator: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    // ------------------------------------------------------------------ //
    //  Response DTOs (mirror frontend types/index.ts)                     //
    // ------------------------------------------------------------------ //

    public record AggregatorStats(
            long matches,
            long teams,
            long normalization_requests,
            long pending_normalization,
            long unrecognized_odds,
            long matches_with_results,
            long matches_pending_results
    ) {}

    public record SystemHealth(
            String status,
            List<String> activeIssues,
            int maxOddsDelayMinutes
    ) {}
}
