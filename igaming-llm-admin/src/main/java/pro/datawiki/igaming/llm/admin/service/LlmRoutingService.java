package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;
import pro.datawiki.igaming.llm.admin.client.LlmGatewayClient;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.LlmRequest;
import pro.datawiki.igaming.llm.admin.dto.LlmResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmGatewayNodeRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LlmRoutingService {

    private final LlmGatewayNodeRepository nodeRepository;
    private final LlmGatewayClient gatewayClient;
    private final AtomicInteger rotationIndex = new AtomicInteger(0);

    public LlmRoutingService(LlmGatewayNodeRepository nodeRepository, LlmGatewayClient gatewayClient) {
        this.nodeRepository = nodeRepository;
        this.gatewayClient = gatewayClient;
    }

    public LlmResponse generate(LlmRequest request) {
        String providerType = determineProvider(request);
        return generateWithRetry(request, providerType, 0);
    }

    private LlmResponse generateWithRetry(LlmRequest request, String providerType, int attempt) {
        List<LlmGatewayNode> availableNodes = nodeRepository.findActiveNodesByProviderType(providerType)
                .stream()
                .filter(LlmGatewayNode::isAvailable)
                .collect(Collectors.toList());

        if (availableNodes.isEmpty()) {
            log.error("❌ No active healthy nodes available for provider: {}", providerType);
            throw new IllegalStateException("No available healthy nodes for provider " + providerType);
        }

        if (attempt >= availableNodes.size()) {
            log.error("❌ All available healthy nodes exhausted for provider: {}", providerType);
            throw new RuntimeException("All nodes exhausted or failed for provider " + providerType);
        }

        // Select node via round-robin rotation
        int idx = (rotationIndex.getAndIncrement() & Integer.MAX_VALUE) % availableNodes.size();
        LlmGatewayNode node = availableNodes.get(idx);

        log.info("🚀 Routing LLM request to node: {} ({}), attempt: {}", node.getName(), node.getEndpointUrl(), attempt);

        try {
            URI targetUri = URI.create(node.getEndpointUrl());
            LlmResponse response = gatewayClient.generate(targetUri, request);

            // Update success stats
            updateSuccessStats(node.getId(), response.getTokensUsed() != null ? response.getTokensUsed() : 0);
            return response;

        } catch (FeignException e) {
            log.warn("⚠️ FeignException from node {}: status={}, message={}", node.getName(), e.status(), e.getMessage());
            
            if (e.status() == 429 || e.getMessage().contains("Quota") || e.getMessage().contains("exhausted")) {
                // 429 Too Many Requests -> Suspend node for 1 hour
                suspendNode(node.getId(), 1);
                log.warn("❌ Node {} suspended due to 429 / Quota limit.", node.getName());
            } else {
                // Other connection or execution failures -> Log warning, do not mark down to maintain service availability if it is the only node
                log.warn("⚠️ Connection/execution failure on node {}: {}", node.getName(), e.getMessage());
            }
            
            // Failover recursively to next available node
            return generateWithRetry(request, providerType, attempt + 1);
            
        } catch (Exception e) {
            log.error("❌ Unexpected exception routing to node {}: {}", node.getName(), e.getMessage());
            
            // Failover recursively to next available node
            return generateWithRetry(request, providerType, attempt + 1);
        }
    }

    @Transactional
    protected void updateSuccessStats(Long nodeId, int tokensUsed) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            node.setSuccessCount(node.getSuccessCount() + 1);
            node.setTotalTokensUsed(node.getTotalTokensUsed() + tokensUsed);
            node.setLastRequestTime(LocalDateTime.now());
            node.setStatus("HEALTHY");
            nodeRepository.save(node);
        });
    }

    @Transactional
    protected void suspendNode(Long nodeId, int hours) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            node.setFailureCount(node.getFailureCount() + 1);
            node.setStatus("EXHAUSTED");
            node.setSuspendedUntil(LocalDateTime.now().plusHours(hours));
            node.setLastRequestTime(LocalDateTime.now());
            nodeRepository.save(node);
        });
    }

    @Transactional
    protected void markNodeDown(Long nodeId, String reason) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            node.setFailureCount(node.getFailureCount() + 1);
            node.setStatus("DOWN");
            node.setLastRequestTime(LocalDateTime.now());
            nodeRepository.save(node);
        });
    }

    private String determineProvider(LlmRequest request) {
        if (request.getModel() != null) {
            String model = request.getModel().toLowerCase();
            if (model.contains("gemini-cli") || model.contains("gemini_cli")) return "gemini-cli";
            if (model.contains("gemini")) return "gemini-cli";
            if (model.contains("deepseek")) return "deepseek";
            if (model.contains("agent-studio") || model.contains("agent_studio")) return "agent-studio";
        }
        return "gemini-cli"; // Default
    }
}
