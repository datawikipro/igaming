package pro.datawiki.igaming.llm.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.LlmRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmResponse;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;
import pro.datawiki.igaming.llm.admin.service.LlmRoutingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Allow easy integration with WebStorm frontend
public class LlmAdminController {

    private final LlmRoutingService routingService;
    private final LlmGatewayNodeService nodeService;

    public LlmAdminController(LlmRoutingService routingService, LlmGatewayNodeService nodeService) {
        this.routingService = routingService;
        this.nodeService = nodeService;
    }

    /**
     * Endpoint for LLM Gateway pods to dynamically lease and lock a configuration/key.
     */
    @PostMapping("/llm/lease")
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
    @PostMapping("/llm/release")
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
    @PostMapping("/llm/generate")
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
    @GetMapping("/llm/status")
    public ResponseEntity<Boolean> isLlmAlive() {
        boolean alive = nodeService.getAllNodes().stream()
                .anyMatch(LlmGatewayNode::isAvailable);
        return ResponseEntity.ok(alive);
    }

    // --- Admin Dashboard REST Endpoints ---

    @GetMapping("/admin/nodes")
    public ResponseEntity<List<LlmGatewayNode>> getNodes() {
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

    @PostMapping("/admin/nodes")
    public ResponseEntity<LlmGatewayNode> createOrUpdateNode(@RequestBody LlmGatewayNode node) {
        return ResponseEntity.ok(nodeService.saveNode(node));
    }

    @DeleteMapping("/admin/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/nodes/{id}/reset")
    public ResponseEntity<Void> resetSuspension(@PathVariable Long id) {
        nodeService.resetSuspension(id);
        return ResponseEntity.ok().build();
    }
}
