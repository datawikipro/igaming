package pro.datawiki.igaming.llm.admin.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.repository.LlmGatewayNodeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmReconciliationService {

    private final KubernetesClient kubernetesClient;
    private final LlmGatewayNodeRepository nodeRepository;

    @Value("${K8S_NAMESPACE:llm}")
    private String namespace;

    private static final List<String> PROVIDERS = List.of("gemini", "deepseek");

    /**
     * Periodically reconciles the number of worker replicas with the number of active node configurations.
     * Runs every 15 seconds.
     */
    @Scheduled(fixedDelay = 15000, initialDelay = 5000)
    @Transactional(readOnly = true)
    public void reconcileWorkers() {
        try {
            List<LlmGatewayNode> activeNodes = nodeRepository.findByActiveTrue();
            
            int geminiCount = 0;
            int deepseekCount = 0;

            for (LlmGatewayNode node : activeNodes) {
                if (node.getModel() != null && node.getModel().getProvider() != null) {
                    String providerName = node.getModel().getProvider().getName().toLowerCase();
                    if (providerName.contains("gemini")) {
                        geminiCount++;
                    } else if (providerName.contains("deepseek")) {
                        deepseekCount++;
                    }
                }
            }

            scaleDeployment("llm-worker-gemini", geminiCount);
            scaleDeployment("llm-worker-deepseek", deepseekCount);
        } catch (Exception e) {
            log.error("Failed to reconcile Kubernetes workers: {}", e.getMessage());
        }
    }

    private void scaleDeployment(String name, int targetReplicas) {
        try {
            Deployment deployment = kubernetesClient.apps().deployments()
                    .inNamespace(namespace)
                    .withName(name)
                    .get();

            if (deployment == null) {
                log.debug("Deployment '{}' not found in namespace '{}'. Skipping autoscaling.", name, namespace);
                return;
            }

            Integer currentReplicas = deployment.getSpec().getReplicas();
            if (currentReplicas == null) {
                currentReplicas = 0;
            }

            if (currentReplicas != targetReplicas) {
                log.info("Scaling deployment '{}' from {} to {} replicas", name, currentReplicas, targetReplicas);
                
                kubernetesClient.apps().deployments()
                        .inNamespace(namespace)
                        .withName(name)
                        .scale(targetReplicas);
            }
        } catch (Exception e) {
            log.error("Failed to scale deployment '{}': {}", name, e.getMessage());
        }
    }
}
