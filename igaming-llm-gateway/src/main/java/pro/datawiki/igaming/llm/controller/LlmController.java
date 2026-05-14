package pro.datawiki.igaming.llm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.domain.LlmTask;
import pro.datawiki.igaming.llm.dto.*;
import pro.datawiki.igaming.llm.service.LlmQueueService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmController {

    private final LlmQueueService queueService;

    /**
     * Sync generate — обратная совместимость со старыми клиентами.
     * Внутри: submit + blocking poll до 120s.
     */
    @PostMapping("/generate")
    public ResponseEntity<LlmResponse> generate(@RequestBody LlmRequest request) {
        try {
            return ResponseEntity.ok(queueService.generate(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Async submit — возвращает taskId немедленно.
     * Если кеш hit — возвращает COMPLETED с текстом сразу.
     */
    @PostMapping("/submit")
    public ResponseEntity<LlmSubmitResponse> submit(@RequestBody LlmRequest request) {
        try {
            return ResponseEntity.ok(queueService.submit(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Polling результата задачи.
     */
    @GetMapping("/result/{taskId}")
    public ResponseEntity<LlmTask> getResult(@PathVariable UUID taskId) {
        return queueService.getResult(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Worker endpoints ─────────────────────────────────────────────────────

    /**
     * Воркер запрашивает следующую задачу.
     * POST /api/v1/llm/worker/claim?providerType=deepseek&modelName=deepseek-chat&workerId=pod-xyz
     */
    @PostMapping("/worker/claim")
    public ResponseEntity<LlmTask> claimTask(
            @RequestParam String providerType,
            @RequestParam String modelName,
            @RequestParam String workerId) {
        return queueService.claimTask(providerType, modelName, workerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 = нет задач
    }

    /**
     * Воркер записывает результат.
     */
    @PostMapping("/worker/complete")
    public ResponseEntity<Void> completeTask(@RequestBody TaskCompleteRequest request) {
        queueService.completeTask(request);
        return ResponseEntity.ok().build();
    }
}
