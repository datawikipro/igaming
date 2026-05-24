package pro.datawiki.igaming.llm.admin.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.repository.LlmGatewayNodeRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Manages gateway node lifecycle with a strict state machine:
 *
 *   IDLE      — free slot, no pod assigned, waiting for a worker
 *   STARTED   — lease acquired by a pod, waiting for pod to reach Running phase
 *   HEALTHY   — pod is Running and confirmed working
 *   EXHAUSTED — quota exceeded, node is suspended until suspendedUntil
 *   DOWN      — HTTP health-check failed for non-k8s node
 *
 * Transitions:
 *   IDLE      → STARTED   : acquireLease()
 *   STARTED   → HEALTHY   : checkNodesHealth() confirms pod phase = Running
 *   STARTED   → IDLE      : checkNodesHealth() pod not found or not Running after timeout
 *   HEALTHY   → EXHAUSTED : external quota-exceeded signal (via node update)
 *   HEALTHY   → DOWN      : HTTP health-check fails
 *   HEALTHY   → IDLE      : releaseLease() or pod disappears
 *   EXHAUSTED → IDLE      : suspension period expired (checkNodesHealth)
 *   DOWN      → HEALTHY   : HTTP health-check recovers
 *   DOWN      → IDLE      : pod disappears during DOWN state
 *   any       → IDLE      : resetSuspension() manual override
 */
@Slf4j
@Service
public class LlmGatewayNodeService {

    private final LlmGatewayNodeRepository nodeRepository;
    private final LlmProviderKeyRepository keyRepository;
    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.llm.worker-namespace:llm}")
    private String workerNamespace;

    public LlmGatewayNodeService(LlmGatewayNodeRepository nodeRepository, 
                                 LlmProviderKeyRepository keyRepository, 
                                 KubernetesClient kubernetesClient) {
        this.nodeRepository = nodeRepository;
        this.keyRepository = keyRepository;
        this.kubernetesClient = kubernetesClient;
    }

    public List<LlmGatewayNode> getAllNodes() {
        return nodeRepository.findAll();
    }

    public Optional<LlmGatewayNode> getNodeById(Long id) {
        return nodeRepository.findById(id);
    }

    @Transactional
    public LlmGatewayNode saveNode(LlmGatewayNode node) {
        LlmGatewayNode saved = nodeRepository.save(node);
        if (node.getApiKey() != null && !node.getApiKey().isBlank()) {
            nodeRepository.findById(saved.getId()).ifPresent(savedNode -> {
                if (savedNode.getModel() != null && savedNode.getModel().getProvider() != null) {
                    Long providerId = savedNode.getModel().getProvider().getId();
                    List<LlmProviderKey> existingKeys = keyRepository.findByProviderIdAndActiveTrue(providerId);
                    boolean exists = existingKeys.stream().anyMatch(k -> k.getApiKey().equals(node.getApiKey()));
                    if (!exists) {
                        LlmProviderKey newKey = LlmProviderKey.builder()
                                .provider(savedNode.getModel().getProvider())
                                .label("Key for " + savedNode.getName())
                                .apiKey(node.getApiKey())
                                .active(true)
                                .createdAt(LocalDateTime.now())
                                .build();
                        keyRepository.save(newKey);
                        log.info("🔑 Automatically saved API key from node registration as provider key ID {} (label: '{}')", 
                                newKey.getId(), newKey.getLabel());
                    }
                }
            });
        }
        return saved;
    }

    @Transactional
    public void deleteNode(Long id) {
        nodeRepository.deleteById(id);
    }

    public List<LlmGatewayNode> getNodesByModelId(Long modelId) {
        return nodeRepository.findByModelId(modelId);
    }

    @Transactional
    public void deleteNodeWithPod(Long nodeId) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            String podName = node.getLeasedByPod();
            nodeRepository.deleteById(nodeId);
            log.info("🗑️ Deleted node '{}' (id={})", node.getName(), nodeId);

            if (podName != null && !podName.isBlank()) {
                try {
                    boolean deleted = kubernetesClient.pods()
                            .inNamespace(workerNamespace)
                            .withName(podName)
                            .delete()
                            .stream()
                            .findFirst()
                            .isPresent();
                    if (deleted) {
                        log.info("🔪 Terminated pod '{}' in namespace '{}'", podName, workerNamespace);
                    } else {
                        log.warn("⚠️ Pod '{}' not found in namespace '{}', may have already exited", podName, workerNamespace);
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to terminate pod '{}': {}", podName, e.getMessage());
                }
            }
        });
    }

    /**
     * Manual suspension reset — returns node to IDLE (slot is free, no pod).
     */
    @Transactional
    public void resetSuspension(Long id) {
        nodeRepository.findById(id).ifPresent(node -> {
            node.setSuspendedUntil(null);
            node.setLeasedByPod(null);
            node.setLeasedAt(null);
            node.setEndpointUrl("");
            node.setStatus("IDLE");
            nodeRepository.save(node);
            log.info("✅ Manually reset node '{}' → IDLE", node.getName());
        });
    }

    /**
     * Acquire a lease for a worker pod.
     * Uses DB-level pessimistic write lock (SELECT FOR UPDATE) to prevent
     * the race condition where multiple pods start simultaneously and all
     * read the same IDLE node before any transaction commits.
     *
     * Transitions: IDLE → STARTED
     */
    @Transactional
    public LlmGatewayNode acquireLease(LlmLeaseRequest req) {
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

        // 2. Find and lock an IDLE node at DB level (SELECT FOR UPDATE)
        //    The lock prevents concurrent transactions from picking the same node.
        String provider = req.getProviderType() != null ? req.getProviderType() : "gemini";
        List<LlmGatewayNode> available = nodeRepository.findAvailableNodesForUpdate(provider);
        if (available.isEmpty()) {
            log.error("❌ No IDLE slots for provider '{}' to lease to pod '{}'", provider, req.getPodName());
            throw new IllegalStateException("No available idle configurations for provider: " + provider);
        }

        // 3. Take the first locked slot — set STARTED, not HEALTHY yet
        LlmGatewayNode node = available.get(0);
        node.setLeasedByPod(req.getPodName());
        node.setLeasedAt(LocalDateTime.now());

        String ip = req.getPodIp() != null ? req.getPodIp() : "127.0.0.1";
        node.setEndpointUrl("http://" + ip + ":3040");
        node.setStatus("STARTED"); // Pod assigned but not yet confirmed running

        LlmGatewayNode saved = nodeRepository.save(node);
        log.info("🚀 Lease issued: node '{}' → STARTED (provider: {}, pod: '{}', endpoint: '{}')",
                saved.getName(), provider, req.getPodName(), saved.getEndpointUrl());
        return saved;
    }

    /**
     * Release a lease when a pod shuts down gracefully.
     * Transitions: any → IDLE
     */
    @Transactional
    public void releaseLease(String podName) {
        nodeRepository.findByLeasedByPod(podName).ifPresent(node -> {
            log.info("🔄 Releasing lease for pod '{}' from node '{}'", podName, node.getName());
            node.setLeasedByPod(null);
            node.setLeasedAt(null);
            node.setEndpointUrl("");
            node.setStatus("IDLE");
            nodeRepository.save(node);
            log.info("✅ Node '{}' → IDLE (pod '{}' released)", node.getName(), podName);
        });
    }

    /**
     * Periodic health-check every 30 seconds.
     *
     * State machine transitions performed here:
     *   EXHAUSTED + suspension expired          → IDLE  (+ release dead lease)
     *   HEALTHY/STARTED + leasedByPod != null   → check K8s pod phase
     *     pod Running                           → HEALTHY
     *     pod not Running / not found           → IDLE  (release lease)
     *   HEALTHY/STARTED + leasedByPod == null   → IDLE  (data correction)
     *   non-k8s node HTTP ping OK               → HEALTHY
     *   non-k8s node HTTP ping fail             → DOWN + IDLE
     */
    @Scheduled(fixedRate = 30000)
    public void checkNodesHealth() {
        List<LlmGatewayNode> nodes = nodeRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (LlmGatewayNode node : nodes) {
            boolean changed = false;

            // ── 1. Suspension expired ────────────────────────────────────────────
            if ("EXHAUSTED".equals(node.getStatus())
                    && node.getSuspendedUntil() != null
                    && now.isAfter(node.getSuspendedUntil())) {

                node.setSuspendedUntil(null);
                changed = true;
                log.info("⏰ Node '{}' suspension expired.", node.getName());

                // Check whether the leased pod is still alive
                boolean podAlive = isPodRunning(node.getLeasedByPod());
                if (podAlive) {
                    node.setStatus("HEALTHY");
                    log.info("✅ Node '{}' → HEALTHY (pod still running after suspension)", node.getName());
                } else {
                    if (node.getLeasedByPod() != null) {
                        log.info("🧹 Post-suspension: pod '{}' gone — releasing lease from node '{}'",
                                node.getLeasedByPod(), node.getName());
                    }
                    node.setLeasedByPod(null);
                    node.setLeasedAt(null);
                    node.setEndpointUrl("");
                    node.setStatus("IDLE");
                    log.info("💤 Node '{}' → IDLE", node.getName());
                }
            }

            // ── 2. Data-consistency guard: HEALTHY/STARTED with no lease → IDLE ─
            if (("HEALTHY".equals(node.getStatus()) || "STARTED".equals(node.getStatus()))
                    && (node.getLeasedByPod() == null || node.getLeasedByPod().isBlank())) {
                log.warn("⚠️ Node '{}' is {} but has no leasedByPod — correcting to IDLE", node.getName(), node.getStatus());
                node.setStatus("IDLE");
                node.setEndpointUrl("");
                changed = true;
            }

            // ── 3. Health-check for leased pods ─────────────────────────────────
            if (node.isActive()
                    && node.getLeasedByPod() != null
                    && !node.getLeasedByPod().isBlank()
                    && !"EXHAUSTED".equals(node.getStatus())) {

                if (node.getLeasedByPod().startsWith("llm-worker")) {
                    // K8s-native check
                    changed |= checkKubernetesPod(node);
                } else if (node.getEndpointUrl() != null && !node.getEndpointUrl().isBlank()) {
                    // HTTP ping for non-k8s nodes
                    changed |= checkHttpNode(node);
                }
            }

            if (changed) {
                nodeRepository.save(node);
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Returns true if the pod exists in K8s and its phase is Running.
     */
    private boolean isPodRunning(String podName) {
        if (podName == null || podName.isBlank()) return false;
        try {
            var pod = kubernetesClient.pods()
                    .inNamespace(workerNamespace)
                    .withName(podName)
                    .get();
            if (pod == null) return false;
            String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : null;
            return "Running".equals(phase);
        } catch (Exception e) {
            log.error("❌ Failed to check pod '{}': {}", podName, e.getMessage());
            return false;
        }
    }

    /**
     * Checks a K8s worker pod and updates node status accordingly.
     * Returns true if node was modified.
     */
    private boolean checkKubernetesPod(LlmGatewayNode node) {
        try {
            var pod = kubernetesClient.pods()
                    .inNamespace(workerNamespace)
                    .withName(node.getLeasedByPod())
                    .get();

            if (pod == null) {
                log.warn("🧹 Pod '{}' not found — releasing lease from node '{}'",
                        node.getLeasedByPod(), node.getName());
                releaseNodeLease(node, "IDLE");
                return true;
            }

            String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : null;
            if ("Running".equals(phase)) {
                if (!"HEALTHY".equals(node.getStatus())) {
                    log.info("✅ Pod '{}' is Running — node '{}' → HEALTHY", node.getLeasedByPod(), node.getName());
                    node.setStatus("HEALTHY");
                    return true;
                }
                // Already HEALTHY and Running — nothing to change
                return false;
            } else {
                // Pending, CrashLoopBackOff, Terminating, etc.
                log.warn("⚠️ Pod '{}' phase='{}' — releasing lease from node '{}'",
                        node.getLeasedByPod(), phase, node.getName());
                releaseNodeLease(node, "IDLE");
                return true;
            }
        } catch (Exception e) {
            log.error("❌ Failed to verify pod '{}': {}", node.getLeasedByPod(), e.getMessage());
            return false;
        }
    }

    /**
     * HTTP ping for non-k8s (external) nodes.
     * Returns true if node was modified.
     */
    private boolean checkHttpNode(LlmGatewayNode node) {
        try {
            String healthUrl = node.getEndpointUrl() + "/actuator/health";
            String response = restTemplate.getForObject(healthUrl, String.class);
            if (response != null && response.contains("UP")) {
                if (!"HEALTHY".equals(node.getStatus())) {
                    node.setStatus("HEALTHY");
                    log.info("🟢 Node '{}' → HEALTHY (HTTP ping OK)", node.getName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            if (!"DOWN".equals(node.getStatus()) && !"EXHAUSTED".equals(node.getStatus())) {
                node.setStatus("DOWN");
                log.warn("🔴 Node '{}' → DOWN (HTTP ping failed: {})", node.getName(), e.getMessage());
            }
            log.warn("🧹 Releasing lease from DOWN node '{}' (pod: '{}')", node.getName(), node.getLeasedByPod());
            releaseNodeLease(node, "IDLE");
            return true;
        }
    }

    /**
     * Clears lease fields and sets the given status.
     */
    private void releaseNodeLease(LlmGatewayNode node, String newStatus) {
        node.setLeasedByPod(null);
        node.setLeasedAt(null);
        node.setEndpointUrl("");
        node.setStatus(newStatus);
    }
}
