package pro.datawiki.igaming.llm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.llm.domain.*;
import pro.datawiki.igaming.llm.dto.*;
import pro.datawiki.igaming.llm.repository.*;

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

    @Transactional
    public LlmSubmitResponse submit(LlmRequest request) {
        // Resolve model using modelId
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw new IllegalArgumentException("Request must specify 'model' field.");
        }

        List<LlmModel> models = modelRepository.findWithProviderByModelId(request.getModel());
        if (models.isEmpty()) {
            log.warn("⚠️ Model '{}' not found.", request.getModel());
            throw new IllegalArgumentException("Unknown model '" + request.getModel() + "'.");
        }
        LlmModel resolvedModel = models.get(0);
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
                .build();

        task = taskRepository.save(task);
        log.info("📝 Task created id={} provider={} model={}", task.getId(), providerName, modelName);

        return LlmSubmitResponse.builder()
                .taskId(task.getId())
                .status("PENDING")
                .cached(false)
                .build();
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
                    return LlmResponse.builder()
                            .text(t.getResultText())
                            .model(t.getModelName())
                            .provider(t.getProviderType())
                            .tokensUsed(t.getTokensUsed())
                            .finishReason(t.getFinishReason())
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
        // 1. Try to claim from primary queue first
        Optional<LlmTask> opt = taskRepository.claimNextTask(providerType, modelName);
        if (opt.isPresent()) {
            LlmTask task = opt.get();
            task.setStatus("PROCESSING");
            task.setWorkerId(workerId);
            taskRepository.save(task);
            log.info("🔒 Worker '{}' claimed task {} ({}::{})", workerId, task.getId(), providerType, modelName);
            return Optional.of(task);
        }

        // 2. Queue redirection
        List<LlmQueueLink> links = queueLinkRepository.findByActiveTrue();
        for (LlmQueueLink link : links) {
            String sourceProv = link.getSourceProvider();
            String sourceModel = link.getSourceModel();
            String targetProv = link.getTargetProvider();
            String targetModel = link.getTargetModel();

            if (targetProv.equalsIgnoreCase(providerType) && targetModel.equalsIgnoreCase(modelName)) {
                Optional<LlmTask> linkedOpt = taskRepository.claimNextTask(sourceProv, sourceModel);
                if (linkedOpt.isPresent()) {
                    LlmTask task = linkedOpt.get();
                    log.info("🔗 Queue Redirection: Target worker helper '{}' ({}::{}) claimed task {} originally for ({}::{})", 
                        workerId, providerType, modelName, task.getId(), task.getProviderType(), task.getModelName());
                    
                    task.setProviderType(providerType);
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

            // Closed-loop Auto-suspension on quota limits
            if ("FAILED".equals(request.getStatus()) && request.getErrorMessage() != null) {
                String error = request.getErrorMessage().toLowerCase();
                if (error.contains("quota") || error.contains("exhausted") || error.contains("limit") || error.contains("429")) {
                    String workerId = task.getWorkerId();
                    if (workerId != null && !workerId.isBlank()) {
                        nodeRepository.findByLeasedByPod(workerId).ifPresent(node -> {
                            log.warn("🧹 Closed-Loop: Worker '{}' hit 429 quota exhaustion. Auto-suspending leased node '{}' for 8 hours.", workerId, node.getName());
                            node.setStatus("EXHAUSTED");
                            node.setSuspendedUntil(LocalDateTime.now().plusHours(8));
                            node.setFailureCount(node.getFailureCount() + 1);
                            node.setLastRequestTime(LocalDateTime.now());
                            nodeRepository.save(node);
                        });
                    }
                }
            }
        });
    }

    public List<ModelQueueStats> getQueueStats() {
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
