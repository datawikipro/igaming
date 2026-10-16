package pro.datawiki.igaming.capture.sofascore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillProgressDto;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillResult;
import pro.datawiki.igaming.capture.sofascore.service.SofaHistoryBackfillService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for controlling and monitoring SofaScore historical backfills.
 */
@RestController
@RequestMapping("/api/v1/sofascore/backfill")
@RequiredArgsConstructor
@Slf4j
public class SofaHistoryBackfillController {

    private final SofaHistoryBackfillService backfillService;

    /**
     * Ingests matches for a specific date and sport.
     */
    @PostMapping("/date")
    public ResponseEntity<Map<String, Object>> backfillDate(
            @RequestParam(defaultValue = "football") String sport,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("[SofaBackfill API] Received request to backfill sport '{}' on {}", sport, date);
        int ingested = backfillService.backfillDate(sport, date);
        return ResponseEntity.ok(Map.of(
                "sport", sport,
                "date", date.toString(),
                "eventsIngested", ingested,
                "status", "SUCCESS"
        ));
    }

    /**
     * Ingests matches for a single sport across a date range.
     * Default start date is 2 years ago (2024-10-16).
     */
    @PostMapping("/range")
    public ResponseEntity<?> backfillRange(
            @RequestParam(defaultValue = "football") String sport,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "250") long delayMs,
            @RequestParam(defaultValue = "false") boolean async) {

        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.of(2024, 10, 16);
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();

        log.info("[SofaBackfill API] Range request: sport='{}', start={}, end={}, delay={}ms, async={}",
                sport, effectiveStart, effectiveEnd, delayMs, async);

        if (async) {
            backfillService.backfillRangeAsync(Collections.singletonList(sport), effectiveStart, effectiveEnd, delayMs);
            return ResponseEntity.accepted().body(Map.of(
                    "status", "STARTED",
                    "sport", sport,
                    "startDate", effectiveStart.toString(),
                    "endDate", effectiveEnd.toString(),
                    "message", "Backfill job started asynchronously in background"
            ));
        }

        BackfillResult result = backfillService.backfillRange(sport, effectiveStart, effectiveEnd, delayMs);
        return ResponseEntity.ok(result);
    }

    /**
     * Ingests matches across all supported sports for a date range.
     */
    @PostMapping("/all")
    public ResponseEntity<?> backfillAllSports(
            @RequestParam(required = false) List<String> sports,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "250") long delayMs,
            @RequestParam(defaultValue = "true") boolean async) {

        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.of(2024, 10, 16);
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();
        List<String> targetSports = (sports != null && !sports.isEmpty()) ? sports : SofaHistoryBackfillService.DEFAULT_SPORTS;

        log.info("[SofaBackfill API] All sports request: sports={}, start={}, end={}, delay={}ms, async={}",
                targetSports, effectiveStart, effectiveEnd, delayMs, async);

        if (async) {
            backfillService.backfillRangeAsync(targetSports, effectiveStart, effectiveEnd, delayMs);
            return ResponseEntity.accepted().body(Map.of(
                    "status", "STARTED",
                    "sports", targetSports,
                    "startDate", effectiveStart.toString(),
                    "endDate", effectiveEnd.toString(),
                    "message", "Multi-sport backfill job started asynchronously in background"
            ));
        }

        BackfillResult result = backfillService.backfillRangeAllSports(targetSports, effectiveStart, effectiveEnd, delayMs);
        return ResponseEntity.ok(result);
    }

    /**
     * Checks current progress and status of running or completed backfills.
     */
    @GetMapping("/status")
    public ResponseEntity<BackfillProgressDto> getStatus() {
        return ResponseEntity.ok(backfillService.getCurrentProgress());
    }
}
