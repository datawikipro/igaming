package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StuckPodService {

    private final KubernetesClient kubernetesClient;

    /** How long a pod can stay in Pending before it is considered stuck (minutes). */
    @Value("${pending-pod.threshold-minutes:10}")
    private int pendingThresholdMinutes;

    /** Track recently restarted pods to avoid restart loops (podKey -> lastRestartEpochMs). */
    private final ConcurrentHashMap<String, Long> recentlyRestartedPods = new ConcurrentHashMap<>();

    /** Cooldown period before we restart the same pod again (minutes). */
    @Value("${pending-pod.cooldown-minutes:15}")
    private int restartCooldownMinutes;

    private boolean shouldCleanupPod(Pod pod, List<String> targetNamespaces) {
        if (pod.getMetadata() == null) {
            return false;
        }
        String namespace = pod.getMetadata().getNamespace();
        if (namespace == null || !targetNamespaces.contains(namespace.toLowerCase())) {
            return false;
        }

        if (pod.getMetadata().getDeletionTimestamp() != null) {
            return true;
        }

        if (pod.getStatus() != null) {
            String phase = pod.getStatus().getPhase();
            String reason = pod.getStatus().getReason();
            if ("Succeeded".equalsIgnoreCase(phase)) {
                return true;
            }
            if ("Evicted".equalsIgnoreCase(reason)) {
                return true;
            }
        }

        return false;
    }

    public StuckPodsReport getStuckPodsReport() {
        List<Pod> pods;
        try {
            pods = kubernetesClient.pods().inAnyNamespace().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list pods for stuck report: {}", e.getMessage());
            return new StuckPodsReport(0, List.of());
        }

        List<String> targetNamespaces = getTargetNamespaces();

        List<PodDetail> terminating = pods.stream()
                .filter(p -> shouldCleanupPod(p, targetNamespaces))
                .map(p -> {
                    String status = "Terminating";
                    if (p.getMetadata().getDeletionTimestamp() == null && p.getStatus() != null) {
                        if ("Succeeded".equalsIgnoreCase(p.getStatus().getPhase())) {
                            status = "Succeeded";
                        } else if ("Evicted".equalsIgnoreCase(p.getStatus().getReason())) {
                            status = "Evicted";
                        }
                    }
                    return new PodDetail(
                            p.getMetadata().getName(),
                            p.getMetadata().getNamespace(),
                            p.getSpec().getNodeName() != null ? p.getSpec().getNodeName() : "unknown",
                            status
                    );
                })
                .collect(Collectors.toList());

        return new StuckPodsReport(terminating.size(), terminating);
    }

    public int cleanupStuckPods() {
        List<Pod> pods;
        try {
            pods = kubernetesClient.pods().inAnyNamespace().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list pods for stuck cleanup: {}", e.getMessage());
            return 0;
        }

        List<String> targetNamespaces = getTargetNamespaces();

        int count = 0;
        for (Pod pod : pods) {
            if (shouldCleanupPod(pod, targetNamespaces)) {
                try {
                    kubernetesClient.pods()
                            .inNamespace(pod.getMetadata().getNamespace())
                            .withName(pod.getMetadata().getName())
                            .withGracePeriod(0L)
                            .delete();
                    count++;
                    log.info("Successfully cleaned up pod {}/{}", pod.getMetadata().getNamespace(), pod.getMetadata().getName());
                } catch (Exception e) {
                    log.error("Failed to force delete pod {}/{}: {}", pod.getMetadata().getNamespace(), pod.getMetadata().getName(), e.getMessage());
                }
            }
        }
        return count;
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    public void scheduledCleanupStuckPods() {
        try {
            int cleanedCount = cleanupStuckPods();
            if (cleanedCount > 0) {
                log.info("⏰ Auto-cleanup deleted {} stuck/terminated pods from targeted namespaces.", cleanedCount);
            }
        } catch (Exception e) {
            log.error("❌ Scheduled cleanup of stuck pods failed: {}", e.getMessage());
        }
    }

    public PendingPodsReport getPendingPodsReport() {
        List<Pod> allPods;
        try {
            allPods = kubernetesClient.pods().inAnyNamespace().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list pods for pending report: {}", e.getMessage());
            return new PendingPodsReport(0, 0, List.of());
        }

        List<String> targetNamespaces = getTargetNamespaces();
        List<PendingPodDetail> pendingPods = new ArrayList<>();

        for (Pod pod : allPods) {
            if (pod.getMetadata() == null || pod.getStatus() == null) continue;
            String ns = pod.getMetadata().getNamespace();
            if (ns == null || !targetNamespaces.contains(ns.toLowerCase())) continue;
            if (!"Pending".equalsIgnoreCase(pod.getStatus().getPhase())) continue;

            long pendingMinutes = getPendingDurationMinutes(pod);
            String reason = extractPendingReason(pod);
            String ownerKind = getOwnerKind(pod);

            pendingPods.add(PendingPodDetail.builder()
                    .name(pod.getMetadata().getName())
                    .namespace(ns)
                    .pendingMinutes(pendingMinutes)
                    .reason(reason)
                    .ownerKind(ownerKind)
                    .restartable(ownerKind != null && !ownerKind.isEmpty())
                    .build());
        }

        long stuckCount = pendingPods.stream()
                .filter(p -> p.getPendingMinutes() >= pendingThresholdMinutes)
                .count();

        pendingPods.sort((a, b) -> Long.compare(b.getPendingMinutes(), a.getPendingMinutes()));

        return new PendingPodsReport(pendingPods.size(), (int) stuckCount, pendingPods);
    }

    public int restartStuckPendingPods() {
        List<Pod> allPods;
        try {
            allPods = kubernetesClient.pods().inAnyNamespace().list().getItems();
        } catch (Exception e) {
            log.error("Failed to list pods for pending restart: {}", e.getMessage());
            return 0;
        }

        List<String> targetNamespaces = getTargetNamespaces();
        long now = System.currentTimeMillis();
        int restarted = 0;

        recentlyRestartedPods.entrySet().removeIf(
                entry -> now - entry.getValue() > Duration.ofMinutes(restartCooldownMinutes).toMillis()
        );

        for (Pod pod : allPods) {
            if (pod.getMetadata() == null || pod.getStatus() == null) continue;
            String ns = pod.getMetadata().getNamespace();
            if (ns == null || !targetNamespaces.contains(ns.toLowerCase())) continue;
            if (!"Pending".equalsIgnoreCase(pod.getStatus().getPhase())) continue;

            long pendingMinutes = getPendingDurationMinutes(pod);
            if (pendingMinutes < pendingThresholdMinutes) continue;

            String ownerKind = getOwnerKind(pod);
            if (ownerKind == null || ownerKind.isEmpty()) {
                continue;
            }

            String podKey = ns + "/" + pod.getMetadata().getName();
            if (recentlyRestartedPods.containsKey(podKey)) {
                continue;
            }

            try {
                log.warn("🔄 PENDING POD RESTART: Pod {}/{}", ns, pod.getMetadata().getName());

                kubernetesClient.pods()
                        .inNamespace(ns)
                        .withName(pod.getMetadata().getName())
                        .withGracePeriod(0L)
                        .delete();

                recentlyRestartedPods.put(podKey, now);
                restarted++;

            } catch (Exception e) {
                log.error("Failed to restart pending pod {}/{}: {}",
                        ns, pod.getMetadata().getName(), e.getMessage());
            }
        }

        return restarted;
    }

    @Scheduled(fixedDelay = 180000, initialDelay = 60000)
    public void scheduledRestartPendingPods() {
        try {
            int restarted = restartStuckPendingPods();
            if (restarted > 0) {
                log.info("⏰ Auto-restart: deleted {} stuck Pending pods to trigger reschedule.", restarted);
            }
        } catch (Exception e) {
            log.error("❌ Scheduled restart of Pending pods failed: {}", e.getMessage());
        }
    }

    private List<String> getTargetNamespaces() {
        String ownNamespace = kubernetesClient.getNamespace();
        if (ownNamespace == null || ownNamespace.isEmpty()) {
            ownNamespace = "igaming-master";
        }
        return List.of(ownNamespace.toLowerCase(), "igaming-source", "llm", "proxy", "service-proxy", "s3-dev");
    }

    private long getPendingDurationMinutes(Pod pod) {
        try {
            if (pod.getStatus().getConditions() != null) {
                for (PodCondition cond : pod.getStatus().getConditions()) {
                    if ("PodScheduled".equals(cond.getType()) && "False".equals(cond.getStatus())) {
                        if (cond.getLastTransitionTime() != null) {
                            ZonedDateTime transition = ZonedDateTime.parse(cond.getLastTransitionTime());
                            return Duration.between(transition, ZonedDateTime.now()).toMinutes();
                        }
                    }
                }
            }
            String created = pod.getMetadata().getCreationTimestamp();
            if (created != null) {
                ZonedDateTime creationTime = ZonedDateTime.parse(created);
                return Duration.between(creationTime, ZonedDateTime.now()).toMinutes();
            }
        } catch (Exception e) {
            log.debug("Failed to parse pending duration for pod {}: {}",
                    pod.getMetadata().getName(), e.getMessage());
        }
        return 0;
    }

    private String extractPendingReason(Pod pod) {
        if (pod.getStatus() != null && pod.getStatus().getConditions() != null) {
            for (PodCondition cond : pod.getStatus().getConditions()) {
                if ("PodScheduled".equals(cond.getType()) && "False".equals(cond.getStatus())) {
                    return cond.getMessage() != null ? cond.getMessage() : cond.getReason();
                }
            }
        }
        if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
            for (var cs : pod.getStatus().getContainerStatuses()) {
                if (cs.getState() != null && cs.getState().getWaiting() != null) {
                    return cs.getState().getWaiting().getReason() + ": " +
                            (cs.getState().getWaiting().getMessage() != null
                                    ? cs.getState().getWaiting().getMessage() : "");
                }
            }
        }
        return "Unknown";
    }

    private String getOwnerKind(Pod pod) {
        if (pod.getMetadata().getOwnerReferences() != null && !pod.getMetadata().getOwnerReferences().isEmpty()) {
            return pod.getMetadata().getOwnerReferences().get(0).getKind();
        }
        return null;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class StuckPodsReport {
        private int terminatingCount;
        private List<PodDetail> terminatingPods;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PodDetail {
        private String name;
        private String namespace;
        private String nodeName;
        private String status;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class PendingPodDetail {
        private String name;
        private String namespace;
        private long pendingMinutes;
        private String reason;
        private String ownerKind;
        private boolean restartable;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PendingPodsReport {
        private int totalPending;
        private int stuckBeyondThreshold;
        private List<PendingPodDetail> pendingPods;
    }
}
