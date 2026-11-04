package pro.datawiki.igaming.agent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.agent.dto.*;
import pro.datawiki.igaming.agent.service.AnomalyResolutionService;
import pro.datawiki.igaming.agent.service.EmpiricalAnomalyAuditorService;
import pro.datawiki.igaming.agent.service.PlaneAiWorkerPoolDispatcher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/anomaly")
@RequiredArgsConstructor
public class AnomalyAuditController {

    private final EmpiricalAnomalyAuditorService empiricalAuditorService;
    private final PlaneAiWorkerPoolDispatcher workerPoolDispatcher;
    private final AnomalyResolutionService anomalyResolutionService;

    /**
     * Synchronously audits an anomaly/collision between two matches using the 20-worker Plane AI pool.
     */
    @PostMapping("/audit-collision")
    public ResponseEntity<AuditCollisionResponse> auditCollision(@RequestBody AuditCollisionRequest request) {
        log.info("Received REST empirical audit request for match #{} vs #{}", request.getMatchId1(), request.getMatchId2());
        Optional<EmpiricalVerdictDto> verdictOpt = empiricalAuditorService.auditCollision(
                request.getMatchId1(), request.getMatchId2(), request.getConflictTeam(), request.getDetails());

        if (verdictOpt.isPresent()) {
            EmpiricalVerdictDto v = verdictOpt.get();
            AuditCollisionResponse resp = AuditCollisionResponse.builder()
                    .success(true)
                    .matchId1(request.getMatchId1())
                    .matchId2(request.getMatchId2())
                    .verdictType(v.getVerdictType())
                    .winnerMatchId(v.getWinnerMatchId())
                    .loserMatchId(v.getLoserMatchId())
                    .rationale(v.getRationale())
                    .workerPod(v.getWorkerPod())
                    .durationSeconds(v.getDurationSeconds())
                    .remediationStatus("APPLIED")
                    .build();
            return ResponseEntity.ok(resp);
        }

        return ResponseEntity.ok(AuditCollisionResponse.builder()
                .success(false)
                .matchId1(request.getMatchId1())
                .matchId2(request.getMatchId2())
                .remediationStatus("FAILED_OR_INCONCLUSIVE")
                .build());
    }

    /**
     * Returns the live status and quota of all configured Plane AI worker accounts.
     */
    @GetMapping("/workers")
    public ResponseEntity<List<WorkerStatusDto>> getWorkers() {
        return ResponseEntity.ok(workerPoolDispatcher.pollAllWorkers());
    }

    /**
     * Returns a high-level summary of the Plane AI worker pool capacity.
     */
    @GetMapping("/workers/summary")
    public ResponseEntity<Map<String, Object>> getWorkersSummary() {
        return ResponseEntity.ok(workerPoolDispatcher.getPoolStatusSummary());
    }

    /**
     * Returns cumulative anomaly resolution and empirical audit statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<AgentStatsDto> getStats() {
        return ResponseEntity.ok(anomalyResolutionService.getStats());
    }

    /**
     * Sweeps and resolves pending schedule conflicts in AI Analyst using the Plane AI worker pool.
     */
    @PostMapping("/sweep-conflicts")
    public ResponseEntity<Map<String, Object>> sweepConflicts(
            @RequestParam(defaultValue = "15") int limit
    ) {
        log.info("Received request to sweep up to {} pending schedule conflicts", limit);
        return ResponseEntity.ok(empiricalAuditorService.sweepAndResolvePendingConflicts(limit));
    }
}
