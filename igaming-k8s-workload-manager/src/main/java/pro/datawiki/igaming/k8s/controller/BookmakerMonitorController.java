package pro.datawiki.igaming.k8s.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.k8s.dto.BookmakerIncident;
import pro.datawiki.igaming.k8s.repository.BookmakerIncidentRepository;
import pro.datawiki.igaming.k8s.service.BookmakerWatchdogService;
import pro.datawiki.igaming.k8s.service.PlaneTaskDispatcherClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/workload/bookmakers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BookmakerMonitorController {

    private final BookmakerWatchdogService watchdogService;
    private final BookmakerIncidentRepository incidentRepository;
    private final PlaneTaskDispatcherClient planeDispatcher;

    @GetMapping("/telemetry")
    public ResponseEntity<FleetOverviewDto> getBookmakersTelemetry() {
        return ResponseEntity.ok(watchdogService.getLatestFleetOverview());
    }

    @GetMapping("/summary")
    public ResponseEntity<BookmakerWatchdogService.WatchdogRunSummary> getWatchdogSummary() {
        return ResponseEntity.ok(watchdogService.getSummary());
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<BookmakerIncident>> getIncidents(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "50") int limit) {

        if ("OPEN".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(incidentRepository.findActiveIncidents());
        }
        return ResponseEntity.ok(incidentRepository.findAllIncidents(limit));
    }

    @PostMapping("/check-now")
    public ResponseEntity<BookmakerWatchdogService.WatchdogRunSummary> triggerCheckNow() {
        log.info("Manual watchdog check triggered via API");
        return ResponseEntity.ok(watchdogService.triggerManualCheck());
    }

    @PostMapping("/incidents/{id}/dispatch-ai")
    public ResponseEntity<Map<String, Object>> dispatchAiForIncident(@PathVariable Long id) {
        Optional<BookmakerIncident> incidentOpt = incidentRepository.findById(id);
        if (incidentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BookmakerIncident incident = incidentOpt.get();
        log.info("Manual AI dispatch requested for incident #{} ({})", id, incident.getTitle());

        PlaneTaskDispatcherClient.PlaneDispatchResult result =
                planeDispatcher.dispatchAiTask(incident.getTitle(), incident.getDescription());

        if (result.isSuccess()) {
            incident.setPlaneIssueId(result.getIssueId());
            incident.setPlaneIssueUrl(result.getPlaneUrl());
            incidentRepository.save(incident);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "issueId", result.getIssueId() != null ? result.getIssueId() : "",
                    "planeUrl", result.getPlaneUrl() != null ? result.getPlaneUrl() : ""
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", result.getErrorMessage() != null ? result.getErrorMessage() : "Failed to dispatch"
            ));
        }
    }

    @PostMapping("/incidents/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(@PathVariable Long id) {
        incidentRepository.resolveIncident(id);
        return ResponseEntity.ok(Map.of("status", "success", "resolvedId", id));
    }
}
