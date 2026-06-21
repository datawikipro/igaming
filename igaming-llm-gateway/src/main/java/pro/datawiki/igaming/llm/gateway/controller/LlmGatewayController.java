package pro.datawiki.igaming.llm.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.gateway.dto.*;
import pro.datawiki.igaming.llm.gateway.service.LlmQueueService;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmGatewayController {

    private final LlmQueueService queueService;

    @PostMapping("/submit")
    public Mono<ResponseEntity<LlmSubmitResponse>> submit(@RequestBody LlmRequest request) {
        return queueService.submit(request)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<LlmResponse>> generate(@RequestBody LlmRequest request) {
        return queueService.generate(request)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e -> {
                    log.error("❌ LLM execution failed on gateway: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PostMapping("/worker/claim")
    public Mono<ResponseEntity<LlmTask>> claimTask(
            @RequestParam String providerType,
            @RequestParam String modelName,
            @RequestParam String workerId) {
        return queueService.claimTask(providerType, modelName, workerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @PostMapping("/worker/complete")
    public Mono<ResponseEntity<Void>> completeTask(@RequestBody TaskCompleteRequest request) {
        return queueService.completeTask(request)
                .thenReturn(ResponseEntity.ok().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @GetMapping("/queue/stats")
    public Mono<ResponseEntity<java.util.List<ModelQueueStats>>> getQueueStats() {
        return queueService.getQueueStats()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<Boolean>> isLlmAlive() {
        // Simple heuristic: if we have configuration cached, we are alive
        return Mono.just(ResponseEntity.ok(true));
    }
}
