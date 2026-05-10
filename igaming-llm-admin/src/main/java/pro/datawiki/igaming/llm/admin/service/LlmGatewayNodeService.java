package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
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

            // 2. Perform HTTP ping if node was DOWN or HEALTHY to confirm status
            if (node.isActive()) {
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
                }
            }

            if (changed) {
                nodeRepository.save(node);
            }
        }
    }
}
