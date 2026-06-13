package pro.datawiki.igaming.k8s.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.OptimizationReportDto;
import pro.datawiki.igaming.k8s.service.KubernetesPodService;
import pro.datawiki.igaming.k8s.service.StuckPodService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workload")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkloadController {

    private final KubernetesPodService podService;
    private final StuckPodService stuckPodService;

    @GetMapping({"/loaders", "/loaders/stats"})
    public List<KubernetesPodService.LoaderStat> getLoadersStats() {
        return podService.getLoadersStats();
    }

    @GetMapping("/optimization/report")
    public OptimizationReportDto getOptimizationReport() {
        return podService.getOptimizationReport();
    }

    @PostMapping("/loaders/{deploymentName}/scale")
    public void scaleLoader(@PathVariable String deploymentName, @RequestParam int replicas) {
        podService.scaleLoader(deploymentName, replicas);
    }

    @GetMapping("/infrastructure/stuck-pods")
    public StuckPodService.StuckPodsReport getStuckPodsReport() {
        return stuckPodService.getStuckPodsReport();
    }

    @PostMapping("/infrastructure/cleanup-pods")
    public ResponseEntity<Map<String, Object>> cleanupPods() {
        int cleaned = stuckPodService.cleanupStuckPods();
        return ResponseEntity.ok(Map.of("cleanedCount", cleaned, "status", "success"));
    }

    @GetMapping("/infrastructure/pending-pods")
    public StuckPodService.PendingPodsReport getPendingPodsReport() {
        return stuckPodService.getPendingPodsReport();
    }

    @PostMapping("/infrastructure/restart-pending")
    public ResponseEntity<Map<String, Object>> restartPendingPods() {
        int restarted = stuckPodService.restartStuckPendingPods();
        return ResponseEntity.ok(Map.of(
                "restartedCount", restarted,
                "status", "success",
                "message", restarted > 0
                        ? restarted + " stuck Pending pods deleted and will be rescheduled"
                        : "No stuck Pending pods found beyond threshold"
        ));
    }
}
