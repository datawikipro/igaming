package pro.datawiki.igaming.capture.sofascore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.capture.sofascore.service.SofaReconciliationService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sofascore")
@RequiredArgsConstructor
@Slf4j
public class SofaReconciliationController {

    private final SofaReconciliationService reconciliationService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Returns real-time SofaScore ground truth coverage metrics.
     */
    @GetMapping("/coverage")
    public ResponseEntity<Map<String, Object>> getCoverage() {
        Map<String, Object> resp = new HashMap<>();

        // 1. Total Raw Events
        Long rawCount = jdbcTemplate.queryForObject("SELECT count(*) FROM sofascore_raw_events", Long.class);
        resp.put("totalRawEvents", rawCount);

        // 2. Events per Sport
        List<Map<String, Object>> sportCounts = jdbcTemplate.queryForList("""
            SELECT sport, count(*) as count, 
                   to_timestamp(min(match_date)/1000)::date as min_date,
                   to_timestamp(max(match_date)/1000)::date as max_date
            FROM sofascore_raw_events 
            GROUP BY sport 
            ORDER BY count(*) DESC
        """);
        resp.put("rawEventsPerSport", sportCounts);

        // 3. Match Record Total & Mapped
        Map<String, Object> matchStats = jdbcTemplate.queryForMap("""
            SELECT 
                COUNT(*) as total_matches,
                COUNT(CASE WHEN reference_provider = 'SOFASCORE' THEN 1 END) as mapped_sofascore,
                ROUND(COUNT(CASE WHEN reference_provider = 'SOFASCORE' THEN 1 END) * 100.0 / COUNT(*), 1) as coverage_pct
            FROM match_record
        """);
        resp.put("matchRecordOverall", matchStats);

        // 4. Coverage by Time Bucket
        List<Map<String, Object>> buckets = jdbcTemplate.queryForList("""
            SELECT 
                CASE 
                    WHEN start_time >= extract(epoch from now())*1000 THEN 'Upcoming'
                    WHEN start_time >= extract(epoch from now() - interval '48 hours')*1000 THEN 'Past 48h'
                    WHEN start_time >= extract(epoch from now() - interval '7 days')*1000 THEN 'Past 2-7d'
                    ELSE 'Past 7-30d+'
                END as bucket,
                COUNT(*) as total,
                COUNT(CASE WHEN reference_provider = 'SOFASCORE' THEN 1 END) as mapped,
                ROUND(COUNT(CASE WHEN reference_provider = 'SOFASCORE' THEN 1 END) * 100.0 / COUNT(*), 1) as pct
            FROM match_record
            WHERE start_time >= extract(epoch from now() - interval '30 days')*1000
            GROUP BY 1
            ORDER BY min(start_time) DESC
        """);
        resp.put("coverageByBucket", buckets);

        return ResponseEntity.ok(resp);
    }

    /**
     * Trigger on-demand reconciliation for a specific date range.
     */
    @PostMapping("/reconcile")
    public ResponseEntity<SofaReconciliationService.ReconciliationStats> triggerReconciliation(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean forceAll) {

        LocalDate s = startDate != null ? startDate : LocalDate.now().minusDays(2);
        LocalDate e = endDate != null ? endDate : LocalDate.now().plusDays(2);

        Instant start = s.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = e.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        var stats = reconciliationService.reconcileWindow(start, end, forceAll);
        return ResponseEntity.ok(stats);
    }

    /**
     * Trigger asynchronous backfill of missing historical dates.
     */
    @PostMapping("/backfill")
    public ResponseEntity<Map<String, Object>> triggerBackfill(@RequestParam(defaultValue = "32") int pastDays) {
        CompletableFuture.runAsync(() -> reconciliationService.backfillMissingHistoricalDates(pastDays));
        return ResponseEntity.ok(Map.of(
                "status", "STARTED",
                "message", "Historical backfill started asynchronously for past " + pastDays + " days"
        ));
    }
}
