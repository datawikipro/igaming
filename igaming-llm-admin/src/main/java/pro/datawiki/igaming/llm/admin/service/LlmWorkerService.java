package pro.datawiki.igaming.llm.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationRequest;
import pro.datawiki.igaming.llm.admin.dto.WorkerRegistrationResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;

import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;

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

    // Keep track of active workers in memory
    private final Map<String, WorkerInfo> activeWorkers = new ConcurrentHashMap<>();

    public LlmWorkerService(LlmProviderRepository providerRepository, LlmProviderKeyRepository keyRepository, LlmModelRepository modelRepository) {
        this.providerRepository = providerRepository;
        this.keyRepository = keyRepository;
        this.modelRepository = modelRepository;
    }

    public WorkerRegistrationResponse register(WorkerRegistrationRequest request) {
        log.info("➕ Registering worker '{}' (provider: {}, model: {}, IP: {})",
                request.getWorkerName(), request.getProviderType(), request.getModelName(), request.getPodIp());

        String apiKey = acquireApiKey(request.getProviderType());

        // Resolve model dynamically from the admin database if available
        String activeModelName = modelRepository.findFirstByProviderName(request.getProviderType())
                .map(LlmModel::getModelId)
                .orElse(request.getModelName());

        log.info("🎯 Dynamically resolved model name for worker '{}': '{}' (requested: '{}')",
                request.getWorkerName(), activeModelName, request.getModelName());

        activeWorkers.put(request.getWorkerName(), WorkerInfo.builder()
                .workerName(request.getWorkerName())
                .providerType(request.getProviderType())
                .modelName(activeModelName)
                .podIp(request.getPodIp())
                .lastHeartbeat(LocalDateTime.now())
                .build());

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

        Optional<LlmProviderKey> keyOpt = keyRepository.findFirstActiveKey(matchedProvider.getId());
        if (keyOpt.isPresent()) {
            return keyOpt.get().getApiKey();
        }

        log.warn("⚠️ No active key found for matched provider '{}'", matchedProvider.getName());
        return "mock-key-please-configure-in-admin";
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
