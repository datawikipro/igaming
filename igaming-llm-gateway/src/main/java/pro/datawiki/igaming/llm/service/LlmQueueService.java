package pro.datawiki.igaming.llm.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.llm.client.LlmAdminClient;
import pro.datawiki.igaming.llm.domain.LlmTask;
import pro.datawiki.igaming.llm.dto.*;
import pro.datawiki.igaming.llm.repository.LlmTaskRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет очередью задач и кешем ответов.
 *
 * Поток для клиента:
 *   1. submit()    — проверяет кеш, создаёт задачу PENDING, возвращает taskId
 *   2. getResult() — возвращает статус/результат
 *   3. generate()  — sync-обёртка: submit + polling 120s (обратная совместимость)
 *
 * Поток для воркера:
 *   1. claimTask()    — SELECT FOR UPDATE SKIP LOCKED → PROCESSING
 *   2. completeTask() — пишет результат → COMPLETED / FAILED
 *
 * Разрешение model → provider выполняется через igaming-llm-admin REST API,
 * без хардкода. Результат кешируется in-process (invalidation не нужна —
 * список моделей меняется редко, перезапуск gateway сбросит кеш).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmQueueService {

    private static final int POLL_INTERVAL_MS = 500;
    private static final int POLL_TIMEOUT_MS  = 120_000;

    private final LlmTaskRepository taskRepository;
    private final LlmAdminClient    adminClient;

    /** In-process кеш: modelId → ModelLookupResponse */
    private final ConcurrentHashMap<String, ModelLookupResponse> modelCache = new ConcurrentHashMap<>();

    // ─── Client API ──────────────────────────────────────────────────────────

    @Transactional
    public LlmSubmitResponse submit(LlmRequest request) {
        ResolvedModel resolved = resolveModel(request);
        String hash = sha256(resolved.providerName() + "|" + resolved.modelName() + "|" + request.getPrompt());

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

        // 2. Новая задача
        LlmTask task = LlmTask.builder()
                .providerType(resolved.providerName())
                .modelName(resolved.modelName())
                .promptHash(hash)
                .prompt(request.getPrompt())
                .systemPrompt(request.getSystemPrompt())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .userId(request.getUserId())
                .permanent(request.isPermanent())
                .ttlHours(request.getTtlHours())
                .build();

        task = taskRepository.save(task);
        log.info("📝 Task created id={} provider={} model={}", task.getId(), resolved.providerName(), resolved.modelName());

        return LlmSubmitResponse.builder()
                .taskId(task.getId())
                .status("PENDING")
                .cached(false)
                .build();
    }

    public Optional<LlmTask> getResult(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    /** Sync generate — submit + blocking poll. Обратная совместимость. */
    public LlmResponse generate(LlmRequest request) {
        LlmSubmitResponse submitted = submit(request);

        if (submitted.isCached()) {
            return toResponse(submitted, request);
        }

        UUID taskId  = submitted.getTaskId();
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            sleep(POLL_INTERVAL_MS);

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

    // ─── Worker API ──────────────────────────────────────────────────────────

    @Transactional
    public Optional<LlmTask> claimTask(String providerType, String modelName, String workerId) {
        Optional<LlmTask> opt = taskRepository.claimNextTask(providerType, modelName);
        if (opt.isEmpty()) return Optional.empty();

        LlmTask task = opt.get();
        task.setStatus("PROCESSING");
        task.setWorkerId(workerId);
        taskRepository.save(task);
        log.info("🔒 Worker '{}' claimed task {} ({}::{})", workerId, task.getId(), providerType, modelName);
        return Optional.of(task);
    }

    @Transactional
    public void completeTask(TaskCompleteRequest req) {
        taskRepository.findById(req.getTaskId()).ifPresent(task -> {
            task.setStatus(req.getStatus());
            task.setResultText(req.getResultText());
            task.setTokensUsed(req.getTokensUsed());
            task.setFinishReason(req.getFinishReason());
            task.setErrorMessage(req.getErrorMessage());
            // FAILED задачи не кешируются
            if ("FAILED".equals(req.getStatus())) {
                task.setExpiresAt(task.getCreatedAt());
            }
            taskRepository.save(task);
            log.info("✅ Task {} → {} (worker={})", req.getTaskId(), req.getStatus(), req.getWorkerId());
        });
    }

    // ─── Model resolution (no hardcode) ──────────────────────────────────────

    /**
     * Разрешает model → (providerName, modelName) через igaming-llm-admin.
     * Если клиент явно указал оба поля — используем без lookup.
     * Результат кешируется в памяти до перезапуска.
     */
    private ResolvedModel resolveModel(LlmRequest request) {
        // Явное указание обоих полей — доверяем клиенту
        if (hasText(request.getProviderType()) && hasText(request.getModel())) {
            return new ResolvedModel(request.getProviderType(), request.getModel());
        }

        String modelId = hasText(request.getModel()) ? request.getModel() : "";

        if (!modelId.isEmpty()) {
            ModelLookupResponse info = modelCache.computeIfAbsent(modelId, id -> {
                try {
                    return adminClient.lookupModel(id);
                } catch (FeignException.NotFound e) {
                    log.warn("⚠️ Model '{}' not found in admin. Client must register it first.", id);
                    throw new IllegalArgumentException("Unknown model '" + id + "'. Register it in igaming-llm-admin.");
                } catch (Exception e) {
                    log.error("❌ Admin lookup failed for model '{}': {}", id, e.getMessage());
                    throw new RuntimeException("Cannot resolve model '" + id + "' — admin unavailable", e);
                }
            });
            return new ResolvedModel(info.getProviderName(), info.getModelName());
        }

        // Только провайдер без модели — ошибка: модель обязательна
        throw new IllegalArgumentException("Request must specify 'model' field. Available models are configured in igaming-llm-admin.");
    }

    private record ResolvedModel(String providerName, String modelName) {}

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private LlmResponse toResponse(LlmSubmitResponse s, LlmRequest req) {
        ResolvedModel r = resolveModel(req);
        return LlmResponse.builder()
                .text(s.getText())
                .model(r.modelName())
                .provider(r.providerName())
                .tokensUsed(s.getTokensUsed())
                .finishReason(s.getFinishReason())
                .build();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }
}
