package pro.datawiki.accounts.cloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.accounts.cloud.client.AccountsServiceClient;
import pro.datawiki.accounts.domain.entity.ManagedServer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartScalingService {

    private final GeoScalingService geoScalingBalancerService;
    private final AccountsServiceClient accountsServiceClient;
    private final KubernetesNodeService kubernetesNodeService;
    
    private final AtomicInteger desiredSpotCount = new AtomicInteger(2);
    private final AtomicInteger desiredStableCount = new AtomicInteger(0);

    public void setDesiredCount(int count) {
        log.info("Setting desired SPOT worker count in memory: {}", count);
        desiredSpotCount.set(count);
    }

    public int getDesiredCount() {
        return desiredSpotCount.get();
    }

    public void setDesiredStableCount(int count) {
        log.info("Setting desired STABLE worker count in memory: {}", count);
        desiredStableCount.set(count);
    }

    public int getDesiredStableCount() {
        return desiredStableCount.get();
    }

    public void syncTargetWithCurrentCount() {
        List<ManagedServer> hardwareInstances = accountsServiceClient.getAllServers();

        long activeSpotLoaders = hardwareInstances.stream()
                .filter(m -> "RUNNING".equalsIgnoreCase(m.getStatus()))
                .filter(m -> "LOADER".equalsIgnoreCase(m.getRole()) && "SPOT".equalsIgnoreCase(m.getProvisioningModel()))
                .count();

        long activeStableLoaders = hardwareInstances.stream()
                .filter(m -> "RUNNING".equalsIgnoreCase(m.getStatus()))
                .filter(m -> "LOADER".equalsIgnoreCase(m.getRole()) && "STANDARD".equalsIgnoreCase(m.getProvisioningModel()))
                .count();

        log.info("Syncing targets: SPOT={}, STABLE={}", activeSpotLoaders, activeStableLoaders);
        setDesiredCount((int) activeSpotLoaders);
        setDesiredStableCount((int) activeStableLoaders);
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void reconcile() {
        int spotTarget = getDesiredCount();
        int stableTarget = getDesiredStableCount();

        List<ManagedServer> hardwareInstances = accountsServiceClient.getAllServers();

        java.util.function.Predicate<ManagedServer> isLoader = m -> "LOADER".equalsIgnoreCase(m.getRole());
        java.util.function.Predicate<ManagedServer> isSpot = m -> "SPOT".equalsIgnoreCase(m.getProvisioningModel());
        java.util.function.Predicate<ManagedServer> isStable = isSpot.negate();
        java.util.function.Predicate<ManagedServer> isRunning = m -> "RUNNING".equalsIgnoreCase(m.getStatus());
        java.util.function.Predicate<ManagedServer> isProvisioning = m -> 
                "PROVISIONING".equalsIgnoreCase(m.getStatus()) || "STAGING".equalsIgnoreCase(m.getStatus());
        java.util.function.Predicate<ManagedServer> isDead = m -> 
                "TERMINATED".equalsIgnoreCase(m.getStatus()) || "STOPPED".equalsIgnoreCase(m.getStatus()) || "SUSPENDED".equalsIgnoreCase(m.getStatus());

        List<String> activeSpot = hardwareInstances.stream()
                .filter(isLoader).filter(isRunning).filter(isSpot)
                .map(ManagedServer::getInstanceName).toList();
        List<String> provisioningSpot = hardwareInstances.stream()
                .filter(isLoader).filter(isProvisioning).filter(isSpot)
                .map(ManagedServer::getInstanceName).toList();

        List<String> activeStable = hardwareInstances.stream()
                .filter(isLoader).filter(isRunning).filter(isStable)
                .map(ManagedServer::getInstanceName).toList();
        List<String> provisioningStable = hardwareInstances.stream()
                .filter(isLoader).filter(isProvisioning).filter(isStable)
                .map(ManagedServer::getInstanceName).toList();

        long totalSpot = activeSpot.size() + provisioningSpot.size();
        long totalStable = activeStable.size() + provisioningStable.size();

        log.info("Reconcile — SPOT: active={}, provisioning={}, total={}, target={} | STABLE: active={}, provisioning={}, total={}, target={}",
                activeSpot.size(), provisioningSpot.size(), totalSpot, spotTarget,
                activeStable.size(), provisioningStable.size(), totalStable, stableTarget);

        hardwareInstances.stream()
                .filter(isLoader).filter(isDead)
                .map(ManagedServer::getInstanceName)
                .forEach(deadNode -> {
                    log.info("Auto-scaling cleanup: terminating dead instance {}", deadNode);
                    try { geoScalingBalancerService.terminateInstance(deadNode); }
                    catch (Exception e) { log.error("Failed to terminate dead instance {}: {}", deadNode, e.getMessage()); }
                    kubernetesNodeService.deleteNode(deadNode);
                });

        if (totalSpot < spotTarget) {
            int toCreate = spotTarget - (int) totalSpot;
            log.info("Scaling UP SPOT: creating {} new spot workers", toCreate);
            for (int i = 0; i < toCreate; i++) {
                try { geoScalingBalancerService.provisionNode("ANY", "SPOT", "LOADER", "e2-standard-4", false); }
                catch (Exception e) { log.error("Failed to provision spot worker: {}", e.getMessage()); }
            }
        } else if (totalSpot > spotTarget) {
            int toDelete = (int) totalSpot - spotTarget;
            log.info("Scaling DOWN SPOT: terminating {} spot workers", toDelete);
            
            java.util.List<String> targets = new java.util.ArrayList<>();
            int provisioningToDelete = Math.min(toDelete, provisioningSpot.size());
            for (int i = 0; i < provisioningToDelete; i++) { targets.add(provisioningSpot.get(i)); }
            
            int remainingToDelete = toDelete - provisioningToDelete;
            if (remainingToDelete > 0 && !activeSpot.isEmpty()) {
                java.util.List<String> sortedActive = sortNodesByLoad(activeSpot);
                for (int i = 0; i < Math.min(remainingToDelete, sortedActive.size()); i++) { targets.add(sortedActive.get(i)); }
            }
            
            targets.forEach(name -> {
                log.info("Auto-scaling SPOT: terminating {}", name);
                geoScalingBalancerService.terminateInstance(name);
                kubernetesNodeService.deleteNode(name);
            });
        }

        if (totalStable < stableTarget) {
            int toCreate = stableTarget - (int) totalStable;
            log.info("Scaling UP STABLE: creating {} new standard workers", toCreate);
            for (int i = 0; i < toCreate; i++) {
                try { geoScalingBalancerService.provisionNode("ANY", "STANDARD", "LOADER", "e2-standard-4", false); }
                catch (Exception e) { log.error("Failed to provision stable worker: {}", e.getMessage()); }
            }
        } else if (totalStable > stableTarget) {
            int toDelete = (int) totalStable - stableTarget;
            log.info("Scaling DOWN STABLE: terminating {} stable workers", toDelete);
            
            java.util.List<String> targets = new java.util.ArrayList<>();
            int provisioningToDelete = Math.min(toDelete, provisioningStable.size());
            for (int i = 0; i < provisioningToDelete; i++) { targets.add(provisioningStable.get(i)); }
            
            int remainingToDelete = toDelete - provisioningToDelete;
            if (remainingToDelete > 0 && !activeStable.isEmpty()) {
                java.util.List<String> sortedActive = sortNodesByLoad(activeStable);
                for (int i = 0; i < Math.min(remainingToDelete, sortedActive.size()); i++) { targets.add(sortedActive.get(i)); }
            }
            
            targets.forEach(name -> {
                log.info("Auto-scaling STABLE: terminating {}", name);
                geoScalingBalancerService.terminateInstance(name);
                kubernetesNodeService.deleteNode(name);
            });
        }
    }

    private java.util.List<String> sortNodesByLoad(java.util.List<String> nodes) {
        if (nodes == null || nodes.size() <= 1) {
            return nodes;
        }
        try {
            java.util.List<io.fabric8.kubernetes.api.model.Pod> pods = kubernetesNodeService.getKubernetesClient()
                    .pods().inAnyNamespace().list().getItems();
            
            java.util.Map<String, Long> podCounts = pods.stream()
                    .filter(pod -> pod.getSpec() != null && pod.getSpec().getNodeName() != null)
                    .filter(pod -> pod.getStatus() != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()))
                    .collect(Collectors.groupingBy(
                            pod -> pod.getSpec().getNodeName(),
                            Collectors.counting()
                    ));
            
            java.util.List<String> sorted = new java.util.ArrayList<>(nodes);
            sorted.sort(java.util.Comparator.comparingLong(name -> podCounts.getOrDefault(name, 0L)));
            return sorted;
        } catch (Exception e) {
            log.error("Failed to sort nodes by load, falling back to original list: {}", e.getMessage());
            return nodes;
        }
    }
}
