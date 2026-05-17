package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.client.LlmGatewayClient;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.ModelQueueStats;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmNodeController {

    private final LlmGatewayNodeService nodeService;
    private final LlmGatewayClient gatewayClient;

    @Value("${app.llm.gateway-url:http://llm-gateway}")
    private String gatewayUrl;

    // ─── Gateway Nodes Management ─────────────────────────────────────────────

    @GetMapping("/nodes")
    public ResponseEntity<List<LlmGatewayNode>> getNodes() {
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

    @PostMapping("/nodes")
    public ResponseEntity<LlmGatewayNode> createOrUpdateNode(@RequestBody LlmGatewayNode node) {
        return ResponseEntity.ok(nodeService.saveNode(node));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/nodes/{id}/reset")
    public ResponseEntity<Void> resetSuspension(@PathVariable Long id) {
        nodeService.resetSuspension(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/gateway/stats")
    public ResponseEntity<List<ModelQueueStats>> getGatewayStats() {
        try {
            URI targetUri = URI.create(gatewayUrl);
            List<ModelQueueStats> stats = gatewayClient.getQueueStats(targetUri);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Failed to fetch queue stats from gateway: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
