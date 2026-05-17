package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmResponse;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;
import pro.datawiki.igaming.llm.admin.service.LlmRoutingService;

@Slf4j
@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmGatewayController {

    private final LlmRoutingService routingService;
    private final LlmGatewayNodeService nodeService;

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
     * Public endpoint for core clients (aggregator, bots, crawlers) to generate LLM text.
     * Automatically load balances and routes over healthy nodes, handles 429 failover.
     */
    @PostMapping("/generate")
    public ResponseEntity<LlmResponse> generate(@RequestBody LlmRequest request) {
        try {
            LlmResponse response = routingService.generate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ LLM execution failed after all failovers: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
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
