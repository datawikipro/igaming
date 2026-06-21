package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.ModelQueueStats;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmNodeController {

    private final LlmGatewayNodeService nodeService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.llm-gateway.url:http://llm-gateway:3040}")
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
    public ResponseEntity<ModelQueueStats[]> getGatewayStats() {
        try {
            String url = gatewayUrl + "/api/v1/llm/queue/stats";
            ResponseEntity<ModelQueueStats[]> response = restTemplate.getForEntity(url, ModelQueueStats[].class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("❌ Failed to fetch queue stats from gateway: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
