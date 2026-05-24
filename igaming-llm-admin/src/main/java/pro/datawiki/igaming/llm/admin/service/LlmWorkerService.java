package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;

import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmGatewayNodeRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service

public class LlmWorkerService {

    private final LlmProviderRepository providerRepository;
    private final LlmProviderKeyRepository keyRepository;
    private final LlmModelRepository modelRepository;
    private final LlmGatewayNodeRepository nodeRepository;
    private final LlmGatewayNodeService nodeService;

    // Keep track of active workers in memory
    private final Map<String, WorkerInfo> activeWorkers = new ConcurrentHashMap<>();

    // Keep track of the last distributed key ID for each provider (for round-robin distribution)
    private final Map<Long, Long> lastUsedKeyIdByProvider = new ConcurrentHashMap<>();

    public LlmWorkerService(LlmProviderRepository providerRepository, 
                            LlmProviderKeyRepository keyRepository, 
                            LlmModelRepository modelRepository, 
                            LlmGatewayNodeRepository nodeRepository,
                            LlmGatewayNodeService nodeService) {
        this.providerRepository = providerRepository;
        this.keyRepository = keyRepository;
        this.modelRepository = modelRepository;
        this.nodeRepository = nodeRepository;
        this.nodeService = nodeService;
    }

    @Transactional
    public void suspendFailedKey(String providerType, String failedApiKey) {
        if (failedApiKey == null || failedApiKey.isBlank()) {
            return;
        }

        String searchType = providerType.toLowerCase();
        List<LlmProvider> providers = providerRepository.findAll();
        LlmProvider matchedProvider = null;
        for (LlmProvider p : providers) {
            if (p.isActive()) {
                String name = p.getName().toLowerCase();
                if (name.contains(searchType) || searchType.contains(name)) {
                    matchedProvider = p;
                    break;
                }
            }
        }

        if (matchedProvider == null) {
            log.warn("⚠️ No active provider found matching type '{}' for key suspension", providerType);
            return;
        }

        Long providerId = matchedProvider.getId();
        List<LlmProviderKey> activeKeys = keyRepository.findByProviderIdAndActiveTrue(providerId);

        for (LlmProviderKey key : activeKeys) {
            if (key.getApiKey().equals(failedApiKey)) {
                key.setSuspendedUntil(LocalDateTime.now().plusHours(8));
                keyRepository.saveAndFlush(key);
                log.info("🚫 Key ID {} (label: '{}') for provider '{}' suspended until {} due to quota exhaustion.",
                        key.getId(), key.getLabel(), matchedProvider.getName(), key.getSuspendedUntil());
                break;
            }
        }
    }

    @Transactional
    public WorkerRegistrationResponse register(WorkerRegistrationRequest request) {
        log.info("➕ Registering worker '{}' (provider: {}, model: {}, IP: {})",
                request.getWorkerName(), request.getProviderType(), request.getModelName(), request.getPodIp());

        String apiKey = acquireApiKey(request.getProviderType());


        // Resolve model dynamically from the active gateway node configuration in the admin database
        List<LlmGatewayNode> activeNodes = nodeRepository.findActiveNodesByProviderType(request.getProviderType());
        String activeModelName = activeNodes.stream()
                .filter(node -> node.getModel() != null)
                .map(node -> node.getModel().getModelId())
                .findFirst()
                .orElseGet(() -> modelRepository.findFirstByProviderName(request.getProviderType())
                        .map(LlmModel::getModelId)
                        .orElse(request.getModelName())
                );

        log.info("🎯 Dynamically resolved model name for worker '{}': '{}' (requested: '{}')",
                request.getWorkerName(), activeModelName, request.getModelName());

        activeWorkers.put(request.getWorkerName(), WorkerInfo.builder()
                .workerName(request.getWorkerName())
                .providerType(request.getProviderType())
                .modelName(activeModelName)
                .podIp(request.getPodIp())
                .lastHeartbeat(LocalDateTime.now())
                .build());

        // Auto-allocate gateway node lease to this worker pod
        try {
            LlmLeaseRequest leaseReq = LlmLeaseRequest.builder()
                    .podName(request.getWorkerName())
                    .providerType(request.getProviderType())
                    .podIp(request.getPodIp())
                    .build();
            LlmGatewayNode leasedNode = nodeService.acquireLease(leaseReq);
            if (leasedNode != null) {
                log.info("🔒 Successfully auto-allocated lease node '{}' to worker pod '{}'", 
                        leasedNode.getName(), request.getWorkerName());
            } else {
                log.warn("⚠️ No available idle configurations for provider: {} to lease to pod '{}'", 
                        request.getProviderType(), request.getWorkerName());
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not auto-allocate node lease for worker '{}': {}", request.getWorkerName(), e.getMessage());
        }

        return WorkerRegistrationResponse.builder()
                .workerName(request.getWorkerName())
                .apiKey(apiKey)
                .modelName(activeModelName)
                .build();
    }

    public void heartbeat(String workerName) {
        WorkerInfo info = activeWorkers.get(workerName);
        if (info != null) {
            info.setLastHeartbeat(LocalDateTime.now());
        } else {
            log.warn("⚠️ Heartbeat received for unregistered worker '{}'", workerName);
        }
    }

    public void deregister(String workerName) {
        log.info("➖ Deregistering worker '{}'", workerName);
        activeWorkers.remove(workerName);
        
        // Auto-release gateway node lease
        try {
            nodeService.releaseLease(workerName);
            log.info("🔓 Successfully auto-released lease for worker pod '{}'", workerName);
        } catch (Exception e) {
            log.warn("⚠️ Could not auto-release lease for worker '{}': {}", workerName, e.getMessage());
        }
    }

    public Map<String, WorkerInfo> getActiveWorkers() {
        return activeWorkers;
    }

    private String acquireApiKey(String providerType) {
        if (providerType == null || providerType.isEmpty()) {
            return "mock-key";
        }
        String searchType = providerType.toLowerCase();

        List<LlmProvider> providers = providerRepository.findAll();
        LlmProvider matchedProvider = null;
        for (LlmProvider p : providers) {
            if (p.isActive()) {
                String name = p.getName().toLowerCase();
                if (name.contains(searchType) || searchType.contains(name)) {
                    matchedProvider = p;
                    break;
                }
            }
        }

        if (matchedProvider == null) {
            log.warn("⚠️ No active provider found matching type '{}'", providerType);
            return "mock-key-please-configure-in-admin";
        }

        Long providerId = matchedProvider.getId();
        List<LlmProviderKey> activeKeys = keyRepository.findByProviderIdAndActiveTrue(providerId);

        if (activeKeys.isEmpty()) {
            log.warn("⚠️ No active key found for matched provider '{}'", matchedProvider.getName());
            return "mock-key-please-configure-in-admin";
        }

        // 2. Filter out keys that are currently suspended
        LocalDateTime now = LocalDateTime.now();
        List<LlmProviderKey> nonSuspendedKeys = activeKeys.stream()
                .filter(k -> k.getSuspendedUntil() == null || k.getSuspendedUntil().isBefore(now))
                .sorted(java.util.Comparator.comparing(LlmProviderKey::getId))
                .toList();

        if (nonSuspendedKeys.isEmpty()) {
            log.warn("❌ All active keys for provider '{}' are suspended due to quota exhaustion!", matchedProvider.getName());
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "All keys for provider " + matchedProvider.getName() + " are exhausted"
            );
        }

        // Get last distributed key ID or fallback to 0
        Long lastKeyId = lastUsedKeyIdByProvider.getOrDefault(providerId, 0L);

        // Find the next available active key
        LlmProviderKey nextKey = null;
        for (LlmProviderKey key : nonSuspendedKeys) {
            if (key.getId() > lastKeyId) {
                nextKey = key;
                break;
            }
        }

        // Wrap around to the first key if we reached the end
        if (nextKey == null) {
            nextKey = nonSuspendedKeys.get(0);
        }

        // Save last distributed key ID in memory
        lastUsedKeyIdByProvider.put(providerId, nextKey.getId());

        log.info("🔑 Round-robin key distribution: distributed key ID {} (label: '{}') to worker", 
                nextKey.getId(), nextKey.getLabel());
        return nextKey.getApiKey();
    }

    @lombok.Data
    @lombok.Builder
    public static class WorkerInfo {
        private String workerName;
        private String providerType;
        private String modelName;
        private String podIp;
        private LocalDateTime lastHeartbeat;
    }
}
