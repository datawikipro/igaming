package pro.datawiki.igaming.capture.sofascore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandRequest;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandResponse;

import java.util.Map;

/**
 * REST Controller exposing synchronous and asynchronous on-demand match refresh endpoints.
 */
@RestController
@RequestMapping("/api/v1/sofascore/on-demand")
@RequiredArgsConstructor
@Slf4j
public class SofaOnDemandController {

    private final SofaOnDemandService onDemandService;
    private final SofaQueueService queueService;

    /**
     * Refresh match data on-demand.
     * When request.async is false (default): synchronous execution returning updated match data and lineups immediately.
     * When request.async is true: asynchronous queuing returning 202 Accepted with taskId.
     */
    @PostMapping("/refresh")
    public ResponseEntity<SofaOnDemandResponse> refreshMatch(@RequestBody SofaOnDemandRequest request) {
        log.info("[OnDemandController] Refresh requested: matchId={}, eventId={}, sport={}, async={}",
                request.getMatchId(), request.getEventId(), request.getSport(), request.isAsync());

        if (request.isAsync()) {
            SofaOnDemandResponse queuedResponse = onDemandService.submitMatchRefreshAsync(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(queuedResponse);
        } else {
            SofaOnDemandResponse syncResponse = onDemandService.refreshMatchSync(request);
            return ResponseEntity.ok(syncResponse);
        }
    }

    /**
     * Poll task execution status or retrieve completed result.
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<SofaOnDemandResponse> getStatus(@PathVariable String taskId) {
        SofaOnDemandResponse response = onDemandService.getTaskStatus(taskId);
        if ("NOT_FOUND".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Synchronous inspection by direct SofaScore event ID.
     */
    @GetMapping("/match/{eventId}")
    public ResponseEntity<SofaOnDemandResponse> getByEventId(@PathVariable Long eventId) {
        SofaOnDemandRequest req = SofaOnDemandRequest.builder()
                .eventId(eventId)
                .async(false)
                .build();
        return ResponseEntity.ok(onDemandService.refreshMatchSync(req));
    }

    /**
     * Returns real-time queue metrics across all priority tiers.
     */
    @GetMapping("/queue-stats")
    public ResponseEntity<Map<String, Object>> getQueueStats() {
        return ResponseEntity.ok(Map.of(
                "on_demand_queue_size", queueService.getOnDemandQueueSize(),
                "live_queue_size", queueService.getLiveQueueSize(),
                "prematch_queue_size", queueService.getPrematchQueueSize(),
                "history_queue_size", queueService.getHistoryQueueSize(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}
