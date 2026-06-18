package pro.datawiki.igaming.llm.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.llm.admin.domain.*;
import pro.datawiki.igaming.llm.admin.dto.*;
import pro.datawiki.igaming.llm.admin.repository.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmQueueService {

    private static final int POLL_INTERVAL_MS = 500;
    private static final int POLL_TIMEOUT_MS  = 120_000; // 120s timeout

    private final LlmTaskRepository taskRepository;
    private final LlmModelRepository modelRepository;
    private final LlmQueueLinkRepository queueLinkRepository;
    private final LlmGatewayNodeRepository nodeRepository;
    private final LlmRoutingRuleRepository routingRuleRepository;
    private final LlmQueueSubscriptionRepository queueSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public LlmSubmitResponse submit(LlmRequest request) {
        LlmModel resolvedModel = resolveTargetModel(request);
        String providerName = resolvedModel.getProvider().getName();
        String modelName = resolvedModel.getModelId();

        String hash = sha256(providerName + "|" + modelName + "|" + request.getPrompt());

        // 1. Cache hit
        Optional<LlmTask> cached = taskRepository.findCachedByHash(hash);
        if (cached.isPresent()) {
            LlmTask task = cached.get();
            log.info("✅ Cache hit hash={} taskId={}", hash, task.getId());
            return LlmSubmitResponse.builder()
                    .taskId(task.getId())
                    .status("COMPLETED")
                    .cached(true)
                    .text(task.getResultText())
                    .tokensUsed(task.getTokensUsed())
                    .finishReason(task.getFinishReason())
                    .build();
        }

        String metadataJson = null;
        if (request.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getMetadata());
            } catch (Exception e) {
                log.error("Failed to serialize request metadata: {}", e.getMessage());
            }
        }

        // 2. New task
        LlmTask task = LlmTask.builder()
                .providerType(providerName)
                .modelId(resolvedModel.getId())
                .modelName(modelName)
                .promptHash(hash)
                .prompt(request.getPrompt())
                .systemPrompt(request.getSystemPrompt())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .userId(request.getUserId())
                .permanent(request.isPermanent())
                .ttlHours(request.getTtlHours() > 0 ? request.getTtlHours() : 24)
                .logicalType(request.getLogicalType())
                .googleRequired(request.getUseSearch())
                .urgency(request.getUrgency())
                .metadata(metadataJson)
                .build();

        task = taskRepository.save(task);
        log.info("📝 Task created id={} provider={} model={}", task.getId(), providerName, modelName);

        return LlmSubmitResponse.builder()
                .taskId(task.getId())
                .status("PENDING")
                .cached(false)
                .build();
    }

    private LlmModel resolveTargetModel(LlmRequest request) {
        if (request.getLogicalType() != null && !request.getLogicalType().isBlank()) {
            List<LlmRoutingRule> rules = routingRuleRepository.findByActiveTrue();
            
            // Try matching logicalType + googleRequired + urgency (most specific)
            Optional<LlmRoutingRule> ruleOpt = rules.stream()
                .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType())
                        && r.getGoogleRequired() != null && r.getGoogleRequired().equals(request.getUseSearch())
                        && r.getUrgency() != null && r.getUrgency().equalsIgnoreCase(request.getUrgency()))
                .findFirst();
                
            if (ruleOpt.isEmpty()) {
                // Try matching logicalType + googleRequired
                ruleOpt = rules.stream()
                    .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType())
                            && r.getGoogleRequired() != null && r.getGoogleRequired().equals(request.getUseSearch()))
                    .findFirst();
            }
            
            if (ruleOpt.isEmpty()) {
                // Try matching logicalType only
                ruleOpt = rules.stream()
                    .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType()))
                    .findFirst();
            }
            
            if (ruleOpt.isPresent()) {
                LlmModel model = ruleOpt.get().getTargetModel();
                log.info("🎯 Routed request type='{}' useSearch={} urgency='{}' -> Model '{}'", 
                    request.getLogicalType(), request.getUseSearch(), request.getUrgency(), model.getModelId());
                return model;
            }
        }
        
        // Fallback 1: Explicit request.getModel()
        if (request.getModel() != null && !request.getModel().isBlank()) {
            List<LlmModel> models = modelRepository.findWithProviderByModelId(request.getModel());
            if (!models.isEmpty()) {
                return models.get(0);
            }
            log.warn("⚠️ Explicitly requested model '{}' not found in database.", request.getModel());
        }
        
        // Fallback 2: Global default - first active model
        List<LlmModel> allActive = modelRepository.findAll().stream().filter(LlmModel::isActive).toList();
        if (!allActive.isEmpty()) {
            log.warn("⚠️ No routing rule or explicit model matched. Falling back to default model '{}'.", 
                allActive.get(0).getModelId());
            return allActive.get(0);
        }
        
        throw new IllegalArgumentException("No routing rules configured, no explicit model found, and no active fallback model exists.");
    }

    public Optional<LlmTask> getResult(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    public LlmResponse generate(LlmRequest request) {
        LlmSubmitResponse submitted = submit(request);

        if (submitted.isCached()) {
            return LlmResponse.builder()
                    .text(submitted.getText())
                    .model(request.getModel())
                    .provider(submitted.getStatus())
                    .tokensUsed(submitted.getTokensUsed())
                    .finishReason(submitted.getFinishReason())
                    .metadata(request.getMetadata())
                    .build();
        }

        UUID taskId  = submitted.getTaskId();
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task polling interrupted", e);
            }

            Optional<LlmTask> opt = taskRepository.findById(taskId);
            if (opt.isEmpty()) continue;

            LlmTask t = opt.get();
            switch (t.getStatus()) {
                case "COMPLETED" -> {
                    LlmMetadataDto metadataDto = null;
                    if (t.getMetadata() != null && !t.getMetadata().isBlank()) {
                        try {
                            metadataDto = objectMapper.readValue(t.getMetadata(), LlmMetadataDto.class);
                        } catch (Exception e) {
                            log.error("Failed to deserialize task metadata: {}", e.getMessage());
                        }
                    }
                    return LlmResponse.builder()
                            .text(t.getResultText())
                            .model(t.getModelName())
                            .provider(t.getProviderType())
                            .tokensUsed(t.getTokensUsed())
                            .finishReason(t.getFinishReason())
                            .metadata(metadataDto)
                            .build();
                }
                case "FAILED" -> throw new RuntimeException("LLM task " + taskId + " failed: " + t.getErrorMessage());
                default        -> log.debug("⏳ Task {} status={}", taskId, t.getStatus());
            }
        }

        throw new RuntimeException("LLM task " + taskId + " timed out after " + POLL_TIMEOUT_MS + "ms");
    }

    @Transactional
    public Optional<LlmTask> claimTask(String providerType, String modelName, String workerId) {
        String resolvedProviderType = providerType;
        if (providerType != null && providerType.startsWith("gemini")) {
            resolvedProviderType = "gemini";
        }

        // Check if the worker's leased node is active and available
        Optional<LlmGatewayNode> nodeOpt = nodeRepository.findByLeasedByPod(workerId);
        if (nodeOpt.isPresent()) {
            LlmGatewayNode node = nodeOpt.get();
            if (!node.isAvailable()) {
                log.warn("⚠️ Worker '{}' attempted to claim task, but its leased node '{}' is currently suspended/exhausted (status={}, suspendedUntil={}).", 
                        workerId, node.getName(), node.getStatus(), node.getSuspendedUntil());
                return Optional.empty();
            }
        }

        // 1. Try to claim from primary queue first
        Optional<LlmTask> opt = taskRepository.claimNextTask(resolvedProviderType, modelName);
        if (opt.isPresent()) {
            LlmTask task = opt.get();
            task.setStatus("PROCESSING");
            task.setWorkerId(workerId);
            taskRepository.save(task);
            log.info("🔒 Worker '{}' claimed task {} ({}::{})", workerId, task.getId(), resolvedProviderType, modelName);
            return Optional.of(task);
        }

        // 2. Claim from subscribed logical queues
        List<String> subscribedQueues = queueSubscriptionRepository.findActiveQueueNamesByModelName(modelName);
        if (!subscribedQueues.isEmpty()) {
            Optional<LlmTask> optSub = taskRepository.claimNextTaskByLogicalTypes(subscribedQueues);
            if (optSub.isPresent()) {
                LlmTask task = optSub.get();
                log.info("🔒 Worker '{}' claimed logical task {} for queue(s) {} (redirected to {}::{})", 
                    workerId, task.getId(), task.getLogicalType(), resolvedProviderType, modelName);
                
                task.setProviderType(resolvedProviderType);
                task.setModelName(modelName);
                task.setStatus("PROCESSING");
                task.setWorkerId(workerId);
                taskRepository.save(task);
                return Optional.of(task);
            }
        }

        // 3. Queue redirection (legacy link fallback)
        List<LlmQueueLink> links = queueLinkRepository.findByActiveTrue();
        for (LlmQueueLink link : links) {
            String sourceProv = link.getSourceProvider();
            String sourceModel = link.getSourceModel();
            String targetProv = link.getTargetProvider();
            String targetModel = link.getTargetModel();

            if (targetProv.equalsIgnoreCase(resolvedProviderType) && targetModel.equalsIgnoreCase(modelName)) {
                Optional<LlmTask> linkedOpt = taskRepository.claimNextTask(sourceProv, sourceModel);
                if (linkedOpt.isPresent()) {
                    LlmTask task = linkedOpt.get();
                    log.info("🔗 Queue Redirection: Target worker helper '{}' ({}::{}) claimed task {} originally for ({}::{})", 
                        workerId, resolvedProviderType, modelName, task.getId(), task.getProviderType(), task.getModelName());
                    
                    task.setProviderType(resolvedProviderType);
                    task.setModelName(modelName);
                    task.setStatus("PROCESSING");
                    task.setWorkerId(workerId);
                    taskRepository.save(task);
                    return Optional.of(task);
                }
            }
        }

        return Optional.empty();
    }

    @Transactional
    public void completeTask(TaskCompleteRequest request) {
        taskRepository.findById(request.getTaskId()).ifPresent(task -> {
            task.setStatus(request.getStatus());
            task.setResultText(request.getResultText());
            task.setTokensUsed(request.getTokensUsed());
            task.setFinishReason(request.getFinishReason());
            task.setErrorMessage(request.getErrorMessage());
            taskRepository.save(task);
            log.info("🔓 Task {} completed with status: {}", task.getId(), request.getStatus());

            String workerId = task.getWorkerId();
            if (workerId != null && !workerId.isBlank()) {
                if ("COMPLETED".equals(request.getStatus())) {
                    nodeRepository.findByLeasedByPod(workerId).ifPresent(node -> {
                        int tokensUsed = request.getTokensUsed() != null ? request.getTokensUsed() : 0;
                        node.setSuccessCount(node.getSuccessCount() + 1);
                        node.setTotalTokensUsed(node.getTotalTokensUsed() + tokensUsed);
                        node.setLastRequestTime(LocalDateTime.now());
                        if (!"HEALTHY".equals(node.getStatus()) && !"EXHAUSTED".equals(node.getStatus())) {
                            node.setStatus("HEALTHY");
                        }
                        nodeRepository.save(node);
                    });
                } else if ("FAILED".equals(request.getStatus())) {
                    nodeRepository.findByLeasedByPod(workerId).ifPresent(node -> {
                        node.setLastRequestTime(LocalDateTime.now());
                        
                        String error = request.getErrorMessage() != null ? request.getErrorMessage().toLowerCase() : "";
                        if (error.contains("quota") || error.contains("exhausted") || error.contains("limit") || error.contains("429")
                                || error.contains("ineligible") || error.contains("restricted") || error.contains("auth") || error.contains("credential")
                                || error.contains("invalid") || error.contains("denied") || error.contains("401") || error.contains("403")) {
                            log.warn("🧹 Closed-Loop: Worker '{}' hit API key/quota error. Auto-suspending leased node '{}' for 15 minutes. Resetting task {} to PENDING.", workerId, node.getName(), task.getId());

                            node.setStatus("EXHAUSTED");
                            node.setSuspendedUntil(LocalDateTime.now().plusMinutes(15));
                            
                            task.setStatus("PENDING");
                            task.setErrorMessage(null);
                            task.setWorkerId(null);
                            taskRepository.save(task);
                        } else {
                            node.setFailureCount(node.getFailureCount() + 1);
                        }
                        nodeRepository.save(node);
                    });
                }
            }
        });
    }


    public List<ModelQueueStats> getLocalQueueStats() {
        List<TaskGroupCount> counts = taskRepository.getGroupCounts();
        List<TaskGroupCount> hourly = taskRepository.getCompletedLastHour(LocalDateTime.now().minusHours(1));

        Map<String, ModelQueueStats> statsMap = new HashMap<>();

        for (TaskGroupCount c : counts) {
            String key = c.getProviderType() + "|" + c.getModelName();
            ModelQueueStats stats = statsMap.computeIfAbsent(key, k -> ModelQueueStats.builder()
                    .providerType(c.getProviderType())
                    .modelName(c.getModelName())
                    .build());

            switch (c.getStatus()) {
                case "PENDING" -> stats.setPendingCount(c.getCount());
                case "PROCESSING" -> stats.setProcessingCount(c.getCount());
                case "COMPLETED" -> stats.setCompletedCount(c.getCount());
                case "FAILED" -> stats.setFailedCount(c.getCount());
            }
        }

        for (TaskGroupCount h : hourly) {
            String key = h.getProviderType() + "|" + h.getModelName();
            ModelQueueStats stats = statsMap.get(key);
            if (stats != null) {
                stats.setProcessedPerHour(h.getCount());
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    private String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}
