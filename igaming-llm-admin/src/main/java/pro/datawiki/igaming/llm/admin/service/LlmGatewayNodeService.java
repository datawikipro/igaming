package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.repository.LlmGatewayNodeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LlmGatewayNodeService {

    private final LlmGatewayNodeRepository nodeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public LlmGatewayNodeService(LlmGatewayNodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public List<LlmGatewayNode> getAllNodes() {
        return nodeRepository.findAll();
    }

    public Optional<LlmGatewayNode> getNodeById(Long id) {
        return nodeRepository.findById(id);
    }

    @Transactional
    public LlmGatewayNode saveNode(LlmGatewayNode node) {
        return nodeRepository.save(node);
    }

    @Transactional
    public void deleteNode(Long id) {
        nodeRepository.deleteById(id);
    }

    @Transactional
    public void resetSuspension(Long id) {
        nodeRepository.findById(id).ifPresent(node -> {
            node.setSuspendedUntil(null);
            node.setStatus("HEALTHY");
            nodeRepository.save(node);
            log.info("✅ Manually reset suspension for node: {}", node.getName());
        });
    }

    @Transactional
    public synchronized LlmGatewayNode acquireLease(LlmLeaseRequest req) {
        if (req.getPodName() == null || req.getPodName().isEmpty()) {
            throw new IllegalArgumentException("podName is required");
        }

        // 1. Check if this pod already holds a lease
        Optional<LlmGatewayNode> existing = nodeRepository.findByLeasedByPod(req.getPodName());
        if (existing.isPresent()) {
            LlmGatewayNode node = existing.get();
            log.info("ℹ️ Pod '{}' already has an active lease for node '{}'", req.getPodName(), node.getName());
            return node;
        }

        // 2. Find available nodes of the requested provider type
        String provider = req.getProviderType() != null ? req.getProviderType() : "gemini";
        List<LlmGatewayNode> available = nodeRepository.findAvailableNodes(provider);
        if (available.isEmpty()) {
            log.error("❌ No available healthy configurations for provider '{}' to lease to pod '{}'", provider, req.getPodName());
            throw new IllegalStateException("No available healthy configurations for provider: " + provider);
        }

        // 3. Lease the first available configuration
        LlmGatewayNode node = available.get(0);
        node.setLeasedByPod(req.getPodName());
        node.setLeasedAt(LocalDateTime.now());

        // Dynamic endpoint URL registration (port 3040 for the gateway microservice)
        String ip = req.getPodIp() != null ? req.getPodIp() : "127.0.0.1";
        node.setEndpointUrl("http://" + ip + ":3040");
        node.setStatus("HEALTHY"); // Mark healthy when newly leased

        LlmGatewayNode saved = nodeRepository.save(node);
        log.info("✅ Successfully leased node '{}' (provider: {}) to pod '{}' with endpoint '{}'",
                saved.getName(), provider, req.getPodName(), saved.getEndpointUrl());
        return saved;
    }

    @Transactional
    public void releaseLease(String podName) {
        nodeRepository.findByLeasedByPod(podName).ifPresent(node -> {
            log.info("🔄 Releasing lease for pod '{}' from node '{}'", podName, node.getName());
            node.setLeasedByPod(null);
            node.setLeasedAt(null);
            node.setEndpointUrl(""); // Clear endpoint URL
            nodeRepository.save(node);
            log.info("✅ Released lease successfully for pod '{}'", podName);
        });
    }

    /**
     * Periodic health check of all nodes every 30 seconds.
     * If a suspended node has passed its suspension period, we reset it.
     * We also check if DOWN nodes are back online.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkNodesHealth() {
        List<LlmGatewayNode> nodes = nodeRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (LlmGatewayNode node : nodes) {
            boolean changed = false;

            // 1. Check if suspension has expired
            if ("EXHAUSTED".equals(node.getStatus()) && node.getSuspendedUntil() != null && now.isAfter(node.getSuspendedUntil())) {
                node.setStatus("HEALTHY");
                node.setSuspendedUntil(null);
                changed = true;
                log.info("🔄 Node {} suspension period expired. Resetting status to HEALTHY.", node.getName());
            }

            // 2. Perform HTTP ping only if the node is leased (has a running pod assigned)
            if (node.isActive() && node.getLeasedByPod() != null && !node.getLeasedByPod().isEmpty() && node.getEndpointUrl() != null && !node.getEndpointUrl().isEmpty()) {
                try {
                    // Check actuator health of the gateway pod
                    String healthUrl = node.getEndpointUrl() + "/actuator/health";
                    String response = restTemplate.getForObject(healthUrl, String.class);
                    if (response != null && response.contains("UP")) {
                        if ("DOWN".equals(node.getStatus())) {
                            node.setStatus("HEALTHY");
                            changed = true;
                            log.info("🟢 Node {} is back online! Restoring to HEALTHY.", node.getName());
                        }
                    }
                } catch (Exception e) {
                    if (!"DOWN".equals(node.getStatus()) && !"EXHAUSTED".equals(node.getStatus())) {
                        node.setStatus("DOWN");
                        changed = true;
                        log.warn("🔴 Node {} health check failed: {}. Marked as DOWN.", node.getName(), e.getMessage());
                    }

                    // Auto-release lease if pod died/is unreachable
                    if (node.getLeasedByPod() != null) {
                        log.warn("🧹 Self-healing: releasing lease for dead pod '{}' from node '{}'", node.getLeasedByPod(), node.getName());
                        node.setLeasedByPod(null);
                        node.setLeasedAt(null);
                        node.setEndpointUrl("");
                        node.setStatus("HEALTHY"); // Make available again
                        changed = true;
                    }
                }
            }

            if (changed) {
                nodeRepository.save(node);
            }
        }
    }
}
