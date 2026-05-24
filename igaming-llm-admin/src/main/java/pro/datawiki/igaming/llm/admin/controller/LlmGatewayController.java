package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.domain.LlmTask;
import pro.datawiki.igaming.llm.admin.dto.*;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;
import pro.datawiki.igaming.llm.admin.service.LlmQueueService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmGatewayController {

    private final LlmGatewayNodeService nodeService;
    private final LlmQueueService queueService;

    /**
     * Submit a task asynchronously - returns taskId immediately.
     */
    @PostMapping("/submit")
    public ResponseEntity<LlmSubmitResponse> submit(@RequestBody LlmRequest request) {
        try {
            return ResponseEntity.ok(queueService.submit(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Poll result of a task.
     */
    @GetMapping("/result/{taskId}")
    public ResponseEntity<LlmTask> getResult(@PathVariable UUID taskId) {
        return queueService.getResult(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Sync generate - submit + blocking poll until done (up to 120s).
     */
    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<LlmResponse>> generate(@RequestBody LlmRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ResponseEntity.ok(queueService.generate(request));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            } catch (Exception e) {
                log.error("❌ LLM execution failed on queue: {}", e.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        });
    }

    /**
     * Worker/Gateway claims the next task.
     */
    @PostMapping("/worker/claim")
    public ResponseEntity<LlmTask> claimTask(
            @RequestParam String providerType,
            @RequestParam String modelName,
            @RequestParam String workerId) {
        return queueService.claimTask(providerType, modelName, workerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 = no tasks
    }

    /**
     * Worker/Gateway complete task and save results/errors.
     */
    @PostMapping("/worker/complete")
    public ResponseEntity<Void> completeTask(@RequestBody TaskCompleteRequest request) {
        queueService.completeTask(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for LLM Gateway pods to dynamically lease and lock a configuration/key.
     */
    @PostMapping("/lease")
    public ResponseEntity<LlmGatewayNode> leaseNode(@RequestBody LlmLeaseRequest request) {
        try {
            LlmGatewayNode node = nodeService.acquireLease(request);
            return ResponseEntity.ok(node);
        } catch (Exception e) {
            log.error("❌ Failed to lease LLM gateway node: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint for LLM Gateway pods to gracefully release their lease on shutdown.
     */
    @PostMapping("/release")
    public ResponseEntity<Void> releaseNode(@RequestBody LlmLeaseRequest request) {
        try {
            nodeService.releaseLease(request.getPodName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Failed to release LLM gateway node lease: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to check if there is at least one active, healthy LLM gateway alive right now.
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> isLlmAlive() {
        boolean alive = nodeService.getAllNodes().stream()
                .anyMatch(LlmGatewayNode::isAvailable);
        return ResponseEntity.ok(alive);
    }
}
