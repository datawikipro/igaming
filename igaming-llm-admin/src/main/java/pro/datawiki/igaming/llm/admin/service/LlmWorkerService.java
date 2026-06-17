package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.domain.LlmKeyModelSuspension;
import pro.datawiki.igaming.llm.admin.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmKeyModelSuspensionRepository;

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
import java.util.Set;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LlmWorkerService {

    private final LlmProviderRepository providerRepository;
    private final LlmProviderKeyRepository keyRepository;
    private final LlmModelRepository modelRepository;
    private final LlmGatewayNodeRepository nodeRepository;
    private final LlmGatewayNodeService nodeService;
    private final LlmKeyModelSuspensionRepository suspensionRepository;

    // Keep track of active workers in memory
    private final Map<String, WorkerInfo> activeWorkers = new ConcurrentHashMap<>();

    // Keep track of the last distributed key ID for each provider (for round-robin distribution)
    private final Map<Long, Long> lastUsedKeyIdByProvider = new ConcurrentHashMap<>();

    public LlmWorkerService(LlmProviderRepository providerRepository, 
                            LlmProviderKeyRepository keyRepository, 
                            LlmModelRepository modelRepository, 
                            LlmGatewayNodeRepository nodeRepository,
                            LlmGatewayNodeService nodeService,
                            LlmKeyModelSuspensionRepository suspensionRepository) {
        this.providerRepository = providerRepository;
        this.keyRepository = keyRepository;
        this.modelRepository = modelRepository;
        this.nodeRepository = nodeRepository;
        this.nodeService = nodeService;
        this.suspensionRepository = suspensionRepository;
    }

    @Transactional
    public void suspendFailedKey(String providerType, String failedApiKey) {
        suspendFailedKey(providerType, null, failedApiKey, null);
    }

    @Transactional
    public void suspendFailedKey(String providerType, String modelName, String failedApiKey, String failureReason) {
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

        boolean isPermanent = false;
        if (failureReason != null) {
            String lowerReason = failureReason.toLowerCase();
            if (lowerReason.contains("ineligibletiererror") || lowerReason.contains("restricted_age") || 
                lowerReason.contains("under 18") || lowerReason.contains("must be 18 years old or older") ||
                lowerReason.contains("not eligible for gemini code assist")) {
                isPermanent = true;
            }
        }

        for (LlmProviderKey key : activeKeys) {
            if (key.getApiKey().equals(failedApiKey)) {
                if (isPermanent) {
                    key.setActive(false);
                    keyRepository.saveAndFlush(key);
                    log.error("🚫 Key ID {} (label: '{}') for provider '{}' DEACTIVATED PERMANENTLY due to age/eligibility restriction: {}",
                            key.getId(), key.getLabel(), matchedProvider.getName(), failureReason);
                } else {
                    LocalDateTime suspendedUntil = LocalDateTime.now().plusMinutes(15);
                    if (modelName == null || modelName.isBlank()) {
                        List<LlmModel> models = matchedProvider.getModels();
                        if (models != null && !models.isEmpty()) {
                            log.warn("⚠️ modelName is empty for key suspension. Suspending key for all active models of provider '{}'", matchedProvider.getName());
                            for (LlmModel m : models) {
                                suspendKeyForModel(key, m.getModelId(), suspendedUntil, matchedProvider.getName(), failureReason);
                            }
                        } else {
                            suspendKeyForModel(key, "default", suspendedUntil, matchedProvider.getName(), failureReason);
                        }
                    } else {
                        suspendKeyForModel(key, modelName, suspendedUntil, matchedProvider.getName(), failureReason);
                    }
                }
                break;
            }
        }
    }

    private void suspendKeyForModel(LlmProviderKey key, String modelName, LocalDateTime suspendedUntil, String providerName, String failureReason) {
        LlmKeyModelSuspension suspension = suspensionRepository.findByKeyIdAndModelName(key.getId(), modelName)
                .orElseGet(() -> LlmKeyModelSuspension.builder()
                        .key(key)
                        .modelName(modelName)
                        .build());
        suspension.setSuspendedUntil(suspendedUntil);
        suspensionRepository.saveAndFlush(suspension);
        log.info("🚫 Key ID {} (label: '{}') for provider '{}' suspended for model '{}' until {} due to quota exhaustion. Reason: {}",
                key.getId(), key.getLabel(), providerName, modelName, suspendedUntil, failureReason);
    }

    @Transactional
    public WorkerRegistrationResponse register(WorkerRegistrationRequest request) {
        if (request.getProviderType() != null && request.getProviderType().startsWith("gemini")) {
            request.setProviderType("gemini");
        }

        log.info("➕ Registering worker '{}' (provider: {}, model: {}, IP: {})",
                request.getWorkerName(), request.getProviderType(), request.getModelName(), request.getPodIp());

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

        String apiKey = acquireApiKey(request.getProviderType(), activeModelName);

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

    private String acquireApiKey(String providerType, String modelName) {
        if (providerType == null || providerType.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider type is missing or empty");
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
            log.error("⚠️ No active provider found matching type '{}'", providerType);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active provider found matching type '" + providerType + "'");
        }

        Long providerId = matchedProvider.getId();
        List<LlmProviderKey> activeKeys = keyRepository.findByProviderIdAndActiveTrue(providerId);

        if (activeKeys.isEmpty()) {
            log.error("⚠️ No active key found for matched provider '{}'", matchedProvider.getName());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active key found for matched provider '" + matchedProvider.getName() + "'");
        }

        // 2. Filter out keys that are currently suspended for this modelName
        LocalDateTime now = LocalDateTime.now();
        List<Long> activeKeyIds = activeKeys.stream().map(LlmProviderKey::getId).toList();
        
        List<LlmKeyModelSuspension> activeSuspensions = suspensionRepository.findActiveSuspensions(activeKeyIds, modelName, now);
        Set<Long> suspendedKeyIds = activeSuspensions.stream()
                .map(s -> s.getKey().getId())
                .collect(Collectors.toSet());

        List<LlmProviderKey> nonSuspendedKeys = activeKeys.stream()
                .filter(k -> !suspendedKeyIds.contains(k.getId()))
                .sorted(Comparator.comparing(LlmProviderKey::getId))
                .toList();

        if (nonSuspendedKeys.isEmpty()) {
            log.warn("⚠️ All active keys for provider '{}' are suspended for model '{}'! Falling back to the earliest suspended key to prevent service disruption.", 
                    matchedProvider.getName(), modelName);
            Map<Long, LocalDateTime> suspensionTimes = activeSuspensions.stream()
                    .collect(Collectors.toMap(s -> s.getKey().getId(), LlmKeyModelSuspension::getSuspendedUntil, (a, b) -> a));

            nonSuspendedKeys = activeKeys.stream()
                    .sorted(Comparator.comparing(k -> suspensionTimes.getOrDefault(k.getId(), LocalDateTime.MIN)))
                    .toList();
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

        log.info("🔑 Round-robin key distribution: distributed key ID {} (label: '{}') to worker for model '{}'", 
                nextKey.getId(), nextKey.getLabel(), modelName);
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
