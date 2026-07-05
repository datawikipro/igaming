package pro.datawiki.igaming.infra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.infra.repository.ManagedInstanceRepository;
import pro.datawiki.igaming.infra.repository.ScalingConfigRepository;
import pro.datawiki.igaming.infra.entity.ManagedInstance;
import pro.datawiki.igaming.infra.entity.ScalingConfig;
import pro.datawiki.igaming.infra.service.provider.CloudNodeInfo;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartScalingService {

    private final GeoScalingBalancerService geoScalingBalancerService;
    private final CloudHardwareService hardwareService;
    private final ScalingConfigRepository configRepository;
    private final ManagedInstanceRepository instanceRepository;
    private final KubernetesNodeService kubernetesNodeService;
    
    private static final String TARGET_SPOT_COUNT_KEY = "target_spot_count";
    private static final String TARGET_STABLE_COUNT_KEY = "target_stable_count";
    // Legacy key — kept for backward-compat with existing DB rows
    private static final String LEGACY_TARGET_KEY = "target_worker_count";

    @Value("${SMART_SCALING_TARGET:2}")
    private int fallbackTarget;

    // ─── Spot target ────────────────────────────────────────────────────────────

    @Transactional
    public void setDesiredCount(int count) {
        log.info("Saving desired SPOT worker count to DB: {}", count);
        ScalingConfig config = configRepository.findById(TARGET_SPOT_COUNT_KEY)
                .orElseGet(() -> new ScalingConfig(TARGET_SPOT_COUNT_KEY, count));
        config.setConfigValue(count);
        configRepository.save(config);
    }

    public int getDesiredCount() {
        return configRepository.findById(TARGET_SPOT_COUNT_KEY)
                .or(() -> configRepository.findById(LEGACY_TARGET_KEY))
                .map(ScalingConfig::getConfigValue)
                .orElse(fallbackTarget);
    }

    // ─── Stable target ───────────────────────────────────────────────────────────

    @Transactional
    public void setDesiredStableCount(int count) {
        log.info("Saving desired STABLE worker count to DB: {}", count);
        ScalingConfig config = configRepository.findById(TARGET_STABLE_COUNT_KEY)
                .orElse(new ScalingConfig(TARGET_STABLE_COUNT_KEY, 0));
        config.setConfigValue(count);
        configRepository.save(config);
    }

    public int getDesiredStableCount() {
        return configRepository.findById(TARGET_STABLE_COUNT_KEY)
                .map(ScalingConfig::getConfigValue)
                .orElse(0);
    }

    // ─── Sync ────────────────────────────────────────────────────────────────────

    @Transactional
    public void syncTargetWithCurrentCount() {
        Map<String, CloudNodeInfo> hardwareInstances = hardwareService.getInstancesInfo();
        java.util.List<ManagedInstance> allManaged = instanceRepository.findAll();
        java.util.Map<String, ManagedInstance> managedMap = allManaged.stream()
                .collect(java.util.stream.Collectors.toMap(ManagedInstance::getInstanceName, m -> m));

        long activeSpotLoaders = hardwareInstances.entrySet().stream()
                .filter(e -> "RUNNING".equalsIgnoreCase(e.getValue().status()))
                .filter(e -> {
                    ManagedInstance m = managedMap.get(e.getKey());
                    return m != null && "LOADER".equalsIgnoreCase(m.getRole()) && "SPOT".equalsIgnoreCase(m.getProvisioningModel());
                })
                .count();

        long activeStableLoaders = hardwareInstances.entrySet().stream()
                .filter(e -> "RUNNING".equalsIgnoreCase(e.getValue().status()))
                .filter(e -> {
                    ManagedInstance m = managedMap.get(e.getKey());
                    return m != null && "LOADER".equalsIgnoreCase(m.getRole()) && "STANDARD".equalsIgnoreCase(m.getProvisioningModel());
                })
                .count();

        log.info("Syncing targets: SPOT={}, STABLE={}", activeSpotLoaders, activeStableLoaders);
        setDesiredCount((int) activeSpotLoaders);
        setDesiredStableCount((int) activeStableLoaders);
    }


    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void reconcile() {
        int spotTarget   = getDesiredCount();
        int stableTarget = getDesiredStableCount();

        // 1. Get real-time hardware info
        Map<String, CloudNodeInfo> hardwareInstances = hardwareService.getInstancesInfo();

        // ── Helper filters ────────────────────────────────────────────────────────
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isLoader = e -> {
            Map<String, String> labels = e.getValue().labels();
            return labels != null && "loader".equalsIgnoreCase(labels.get("role"));
        };
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isSpot = e -> {
            Map<String, String> labels = e.getValue().labels();
            return labels != null && "spot".equalsIgnoreCase(labels.get("model"));
        };
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isStable = isSpot.negate();
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isRunning = e ->
                "RUNNING".equalsIgnoreCase(e.getValue().status());
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isProvisioning = e -> {
            String s = e.getValue().status();
            return "PROVISIONING".equalsIgnoreCase(s) || "STAGING".equalsIgnoreCase(s);
        };
        java.util.function.Predicate<Map.Entry<String, CloudNodeInfo>> isDead = e -> {
            String s = e.getValue().status();
            return "TERMINATED".equalsIgnoreCase(s) || "STOPPED".equalsIgnoreCase(s) || "SUSPENDED".equalsIgnoreCase(s);
        };

        // ── 2. Active / provisioning counts by type ───────────────────────────────
        java.util.List<String> activeSpot = hardwareInstances.entrySet().stream()
                .filter(isLoader).filter(isRunning).filter(isSpot)
                .map(Map.Entry::getKey).toList();
        java.util.List<String> provisioningSpot = hardwareInstances.entrySet().stream()
                .filter(isLoader).filter(isProvisioning).filter(isSpot)
                .map(Map.Entry::getKey).toList();

        java.util.List<String> activeStable = hardwareInstances.entrySet().stream()
                .filter(isLoader).filter(isRunning).filter(isStable)
                .map(Map.Entry::getKey).toList();
        java.util.List<String> provisioningStable = hardwareInstances.entrySet().stream()
                .filter(isLoader).filter(isProvisioning).filter(isStable)
                .map(Map.Entry::getKey).toList();

        long totalSpot   = activeSpot.size()   + provisioningSpot.size();
        long totalStable = activeStable.size() + provisioningStable.size();

        log.info("Reconcile — SPOT: active={}, provisioning={}, total={}, target={} | STABLE: active={}, provisioning={}, total={}, target={}",
                activeSpot.size(), provisioningSpot.size(), totalSpot, spotTarget,
                activeStable.size(), provisioningStable.size(), totalStable, stableTarget);

        // ── 3. Cleanup dead instances (both types) ────────────────────────────────
        hardwareInstances.entrySet().stream()
                .filter(isLoader).filter(isDead)
                .map(Map.Entry::getKey)
                .forEach(deadNode -> {
                    log.info("Auto-scaling cleanup: terminating dead instance {}", deadNode);
                    try { geoScalingBalancerService.terminateInstance(deadNode); }
                    catch (Exception e) { log.error("Failed to terminate dead instance {} in GCP: {}", deadNode, e.getMessage()); }
                    kubernetesNodeService.deleteNode(deadNode);
                });

        // ── 4. Reconcile SPOT ─────────────────────────────────────────────────────
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
            
            // 1. Prioritize provisioning nodes (they are not running pods yet)
            int provisioningToDelete = Math.min(toDelete, provisioningSpot.size());
            for (int i = 0; i < provisioningToDelete; i++) {
                targets.add(provisioningSpot.get(i));
            }
            
            int remainingToDelete = toDelete - provisioningToDelete;
            if (remainingToDelete > 0 && !activeSpot.isEmpty()) {
                // 2. Select the least loaded active spot nodes
                java.util.List<String> sortedActive = sortNodesByLoad(activeSpot);
                for (int i = 0; i < Math.min(remainingToDelete, sortedActive.size()); i++) {
                    targets.add(sortedActive.get(i));
                }
            }
            
            targets.forEach(name -> {
                log.info("Auto-scaling SPOT: terminating {}", name);
                geoScalingBalancerService.terminateInstance(name);
                kubernetesNodeService.deleteNode(name);
            });
        }

        // ── 5. Reconcile STABLE ───────────────────────────────────────────────────
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
            
            // 1. Prioritize provisioning nodes (they are not running pods yet)
            int provisioningToDelete = Math.min(toDelete, provisioningStable.size());
            for (int i = 0; i < provisioningToDelete; i++) {
                targets.add(provisioningStable.get(i));
            }
            
            int remainingToDelete = toDelete - provisioningToDelete;
            if (remainingToDelete > 0 && !activeStable.isEmpty()) {
                // 2. Select the least loaded active stable nodes
                java.util.List<String> sortedActive = sortNodesByLoad(activeStable);
                for (int i = 0; i < Math.min(remainingToDelete, sortedActive.size()); i++) {
                    targets.add(sortedActive.get(i));
                }
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
                    .collect(java.util.stream.Collectors.groupingBy(
                            pod -> pod.getSpec().getNodeName(),
                            java.util.stream.Collectors.counting()
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
