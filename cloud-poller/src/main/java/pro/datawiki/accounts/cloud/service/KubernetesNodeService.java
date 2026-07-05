package pro.datawiki.accounts.cloud.service;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.cloud.client.AccountsServiceClient;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.entity.ManagedServer;
import pro.datawiki.accounts.domain.provider.CloudProvisioningProvider;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KubernetesNodeService {

    private final KubernetesClient kubernetesClient;
    private final AccountsServiceClient accountsServiceClient;
    private final GeoScalingService geoScalingService;

    @Value("${GCP_ZONE:not defined}")
    private String configuredZone;

    public KubernetesClient getKubernetesClient() {
        return this.kubernetesClient;
    }

    public NodeStats getNodesStats() {
        List<Node> nodes;
        try {
            nodes = kubernetesClient.nodes().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list nodes from Kubernetes: {}", e.getMessage());
            return new NodeStats(0, 0, 0, 0);
        }
        
        long totalNodes = nodes.size();
        long stableNodes = 0;
        long spotNodes = 0;
        long homeNodes = 0;
        
        for (Node node : nodes) {
            String nodeName = node.getMetadata().getName();
            Map<String, String> labels = node.getMetadata().getLabels();
            if (labels != null) {
                String nodeType = labels.getOrDefault("node-type", "");
                if ("spot".equals(nodeType)) {
                    spotNodes++;
                } else if ("home".equals(nodeType) || nodeName.contains("home")) {
                    homeNodes++;
                } else if ("stable".equals(nodeType) || "standard".equals(nodeType)) {
                    stableNodes++;
                } else if ("master".equals(nodeType) || nodeName.startsWith("master-vm")) {
                    // Exclude master
                } else {
                    stableNodes++;
                }
            } else {
                if (!nodeName.startsWith("master-vm")) {
                    stableNodes++;
                }
            }
        }
        
        return new NodeStats(totalNodes, stableNodes, spotNodes, homeNodes);
    }

    public List<NodeDetail> getDetailedNodes() {
        List<ManagedServer> servers = accountsServiceClient.getAllServers();
        Map<String, ManagedServer> serverMap = servers.stream()
                .collect(Collectors.toMap(ManagedServer::getInstanceName, s -> s, (s1, s2) -> s1));
        
        List<Node> nodes;
        try {
            nodes = kubernetesClient.nodes().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list nodes for detailed view: {}", e.getMessage());
            return List.of();
        }

        return nodes.stream()
                .map(node -> {
                    String nodeName = node.getMetadata().getName();
                    Map<String, String> labels = node.getMetadata().getLabels();
                    String type = labels != null ? labels.getOrDefault("node-type", "main") : "main";
                    
                    ManagedServer hw = serverMap.get(nodeName);
                    String machineType = hw != null && hw.getMachineType() != null ? hw.getMachineType() 
                            : (labels != null ? labels.getOrDefault("node.kubernetes.io/instance-type", "not defined") : "not defined");
                    String zone = labels != null ? labels.getOrDefault("topology.kubernetes.io/zone", 
                                  labels.getOrDefault("failure-domain.beta.kubernetes.io/zone", configuredZone)) : configuredZone;
                    
                    String cpuPlatform = "not defined"; // Extracted if available in ServerInfo (omitted in this simplified version)
                    String scheduling = hw != null && hw.getProvisioningModel() != null ? hw.getProvisioningModel() : "Unknown";
                    
                    return new NodeDetail(
                            nodeName,
                            type,
                            node.getStatus().getCapacity().get("cpu").getAmount(),
                            formatRam(node.getStatus().getCapacity().get("memory").getAmount()),
                            machineType,
                            zone,
                            node.getStatus().getConditions().stream()
                                    .filter(c -> "Ready".equals(c.getType()))
                                    .map(c -> "True".equals(c.getStatus()) ? "Ready" : "NotReady")
                                    .findFirst().orElse("Unknown"),
                            cpuPlatform,
                            scheduling
                    );
                })
                .collect(Collectors.toList());
    }

    public void deleteNode(String nodeName) {
        try {
            kubernetesClient.nodes().withName(nodeName).delete();
            log.info("Deleted node {} from Kubernetes", nodeName);
        } catch (Exception e) {
            log.error("Failed to delete node {} from Kubernetes: {}", nodeName, e.getMessage());
        }
    }

    public record NodeStats(long total, long stable, long spot, long home) {}
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class NodeDetail {
        private String name;
        private String type;
        private String cpu;
        private String memory;
        private String machineType;
        private String zone;
        private String status;
        private String cpuPlatform;
        private String scheduling;
    }

    private String formatRam(String kiAmount) {
        try {
            long ki = Long.parseLong(kiAmount);
            double gb = ki / (1024.0 * 1024.0);
            return String.format("%.1f GB", gb);
        } catch (Exception e) {
            return kiAmount + " RAM";
        }
    }

    public int cleanupNotReadyNodes() {
        List<Node> nodes;
        try {
            nodes = kubernetesClient.nodes().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list nodes for cleanup: {}", e.getMessage());
            return 0;
        }

        int totalNodesCount = nodes.size();
        long notReadyCount = nodes.stream().filter(node -> {
            var readyCond = node.getStatus().getConditions().stream()
                    .filter(c -> "Ready".equals(c.getType()))
                    .findFirst()
                    .orElse(null);
            return readyCond != null && !"True".equals(readyCond.getStatus());
        }).count();

        if (totalNodesCount > 3 && ((double) notReadyCount / totalNodesCount) > 0.15) {
            log.error("CRITICAL: More than 15% of nodes are NotReady ({} out of {}). Aborting cleanup.", notReadyCount, totalNodesCount);
            return 0;
        }

        List<ManagedServer> servers = accountsServiceClient.getAllServers();
        List<CloudCredential> credentials = accountsServiceClient.getActiveCloudCredentials();

        int count = 0;
        for (Node node : nodes) {
            String nodeName = node.getMetadata().getName();
            if (nodeName.startsWith("master-vm")) {
                continue;
            }

            var readyCond = node.getStatus().getConditions().stream()
                    .filter(c -> "Ready".equals(c.getType()))
                    .findFirst()
                    .orElse(null);

            if (readyCond != null && !"True".equals(readyCond.getStatus())) {
                String lastTransition = readyCond.getLastTransitionTime();
                if (lastTransition != null) {
                    try {
                        ZonedDateTime transitionTime = ZonedDateTime.parse(lastTransition);
                        long minutesNotReady = Duration.between(transitionTime, ZonedDateTime.now()).toMinutes();
                        if (minutesNotReady < 10) {
                            log.info("Node {} is NotReady but only for {} minutes. Skipping reset.", nodeName, minutesNotReady);
                            continue;
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse lastTransitionTime for node {}", nodeName);
                    }
                }

                log.warn("⚠️ Node {} has been NotReady beyond threshold! Initiating reset...", nodeName);
                
                boolean resetDone = false;
                ManagedServer server = servers.stream().filter(s -> s.getInstanceName().equals(nodeName)).findFirst().orElse(null);
                if (server != null && server.getCloudCredentialId() != null) {
                    CloudCredential cred = credentials.stream().filter(c -> c.getId().equals(server.getCloudCredentialId())).findFirst().orElse(null);
                    if (cred != null) {
                        try {
                            CloudProvisioningProvider provider = geoScalingService.getProvider(cred.getProvider());
                            resetDone = provider.resetInstance(cred, nodeName);
                        } catch (Exception e) {
                            log.error("Failed to reset instance {} via provider", nodeName, e);
                        }
                    }
                }

                if (resetDone) {
                    log.info("✅ Natively reset instance: {}", nodeName);
                } else {
                    log.warn("Node {} could not be reset natively. Pruning Kubernetes node resource only.", nodeName);
                }
                
                deleteNode(nodeName);
                count++;
            }
        }
        return count;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void scheduledCleanupNotReadyNodes() {
        try {
            int cleanedCount = cleanupNotReadyNodes();
            if (cleanedCount > 0) {
                log.info("⏰ Auto-cleanup deleted and reset {} NotReady/dead nodes from the cluster.", cleanedCount);
            }
        } catch (Exception e) {
            log.error("❌ Scheduled cleanup of NotReady nodes failed: {}", e.getMessage());
        }
    }
}
