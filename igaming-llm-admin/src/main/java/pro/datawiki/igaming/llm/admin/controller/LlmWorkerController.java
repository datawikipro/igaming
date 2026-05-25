package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationResponse;
import pro.datawiki.igaming.llm.admin.service.LlmWorkerService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmWorkerController {

    private final LlmWorkerService workerService;

    // ─── Worker Registration & Heartbeats ──────────────────────────────────────

    @PostMapping("/workers/register")
    public ResponseEntity<WorkerRegistrationResponse> registerWorker(@RequestBody WorkerRegistrationRequest request) {
        try {
            if (request.getFailedApiKey() != null && !request.getFailedApiKey().isBlank()) {
                try {
                    workerService.suspendFailedKey(request.getProviderType(), request.getFailedApiKey(), request.getFailureReason());
                } catch (Exception e) {
                    log.error("⚠️ Failed to suspend key: {}", e.getMessage());
                }
            }
            WorkerRegistrationResponse response = workerService.register(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("❌ Failed to register worker '{}' (Status: {}): {}", request.getWorkerName(), e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            log.error("❌ Failed to register worker '{}': {}", request.getWorkerName(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/workers/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestParam("workerName") String workerName) {
        try {
            workerService.heartbeat(workerName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Failed heartbeat for worker '{}': {}", workerName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/workers/deregister")
    public ResponseEntity<Void> deregisterWorker(@RequestParam("workerName") String workerName) {
        try {
            workerService.deregister(workerName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Failed to deregister worker '{}': {}", workerName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/workers")
    public ResponseEntity<Collection<LlmWorkerService.WorkerInfo>> getActiveWorkers() {
        return ResponseEntity.ok(workerService.getActiveWorkers().values());
    }
}
