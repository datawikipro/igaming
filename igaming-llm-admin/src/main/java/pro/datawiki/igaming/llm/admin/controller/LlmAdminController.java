package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.client.LlmGatewayClient;
import pro.datawiki.igaming.llm.admin.domain.LlmGatewayNode;
import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.*;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;
import pro.datawiki.igaming.llm.admin.service.LlmGatewayNodeService;
import pro.datawiki.igaming.llm.admin.service.LlmWorkerService;

import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmAdminController {

    private final LlmProviderRepository providerRepository;
    private final LlmModelRepository modelRepository;
    private final LlmProviderKeyRepository keyRepository;
    private final LlmGatewayNodeService nodeService;
    private final LlmGatewayClient gatewayClient;
    private final LlmWorkerService workerService;

    @Value("${app.llm.gateway-url:http://llm-gateway}")
    private String gatewayUrl;

    // ─── Providers ───────────────────────────────────────────────────────────

    @GetMapping("/providers")
    public List<LlmProvider> listProviders() {
        return providerRepository.findAll();
    }

    @GetMapping("/providers/supported")
    public List<SupportedProviderResponse> getSupportedProviders() {
        return List.of(
            SupportedProviderResponse.builder()
                .name("gemini")
                .displayName("Google Gemini")
                .models(List.of("gemini-3.1-pro", "gemini-3-flash", "gemini-3.1-flash-lite", "gemini-3.1-flash-live"))
                .build(),
            SupportedProviderResponse.builder()
                .name("gemini-cli")
                .displayName("Gemini CLI")
                .models(List.of("gemini-3.1-pro", "gemini-3-flash", "gemini-3.1-flash-lite"))
                .build(),
            SupportedProviderResponse.builder()
                .name("deepseek")
                .displayName("DeepSeek API")
                .models(List.of("deepseek-chat", "deepseek-reasoner", "deepseek-coder"))
                .build(),
            SupportedProviderResponse.builder()
                .name("openai")
                .displayName("OpenAI")
                .models(List.of("gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo"))
                .build(),
            SupportedProviderResponse.builder()
                .name("anthropic")
                .displayName("Anthropic Claude")
                .models(List.of("claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"))
                .build()
        );
    }

    @PostMapping("/providers")
    public LlmProvider createProvider(@RequestBody LlmProvider provider) {
        return providerRepository.save(provider);
    }

    @PutMapping("/providers/{id}")
    public ResponseEntity<LlmProvider> updateProvider(@PathVariable Long id, @RequestBody LlmProvider body) {
        return providerRepository.findById(id).map(p -> {
            p.setDisplayName(body.getDisplayName());
            p.setActive(body.isActive());
            return ResponseEntity.ok(providerRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/providers/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ─── Provider Keys ───────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}/keys")
    public List<LlmProviderKey> listKeys(@PathVariable Long providerId) {
        return keyRepository.findByProviderIdAndActiveTrue(providerId);
    }

    @PostMapping("/providers/{providerId}/keys")
    public ResponseEntity<LlmProviderKey> addKey(@PathVariable Long providerId,
                                                  @RequestBody LlmProviderKey key) {
        return providerRepository.findById(providerId).map(provider -> {
            key.setProvider(provider);
            return ResponseEntity.ok(keyRepository.save(key));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/providers/keys/{keyId}")
    public ResponseEntity<Void> deleteKey(@PathVariable Long keyId) {
        keyRepository.deleteById(keyId);
        return ResponseEntity.ok().build();
    }

    // ─── Models ──────────────────────────────────────────────────────────────

    @GetMapping("/models")
    public List<LlmModel> listModels(@RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return modelRepository.findByProviderIdAndActiveTrue(providerId);
        }
        return modelRepository.findAll();
    }

    @PostMapping("/models")
    public ResponseEntity<LlmModel> createModel(@RequestBody LlmModel model,
                                                 @RequestParam Long providerId) {
        return providerRepository.findById(providerId).map(provider -> {
            model.setProvider(provider);
            return ResponseEntity.ok(modelRepository.save(model));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/models/{id}")
    public ResponseEntity<LlmModel> updateModel(@PathVariable Long id, @RequestBody LlmModel body) {
        return modelRepository.findById(id).map(m -> {
            m.setDisplayName(body.getDisplayName());
            m.setActive(body.isActive());
            return ResponseEntity.ok(modelRepository.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/models/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        modelRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ─── Lookup (used by gateway) ─────────────────────────────────────────────

    @GetMapping("/models/lookup")
    public ResponseEntity<ModelLookupResponse> lookupModel(@RequestParam String modelId) {
        return modelRepository.findWithProviderByModelId(modelId)
                .map(m -> ResponseEntity.ok(ModelLookupResponse.builder()
                        .modelId(m.getId())
                        .modelName(m.getModelId())
                        .displayName(m.getDisplayName())
                        .providerId(m.getProvider().getId())
                        .providerName(m.getProvider().getName())
                        .providerDisplay(m.getProvider().getDisplayName())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Gateway Nodes Management ─────────────────────────────────────────────

    @GetMapping("/nodes")
    public ResponseEntity<List<LlmGatewayNode>> getNodes() {
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

    @PostMapping("/nodes")
    public ResponseEntity<LlmGatewayNode> createOrUpdateNode(@RequestBody LlmGatewayNode node) {
        return ResponseEntity.ok(nodeService.saveNode(node));
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id) {
        nodeService.deleteNode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/nodes/{id}/reset")
    public ResponseEntity<Void> resetSuspension(@PathVariable Long id) {
        nodeService.resetSuspension(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/gateway/stats")
    public ResponseEntity<List<ModelQueueStats>> getGatewayStats() {
        try {
            URI targetUri = URI.create(gatewayUrl);
            List<ModelQueueStats> stats = gatewayClient.getQueueStats(targetUri);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Failed to fetch queue stats from gateway: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Worker Registration & Heartbeats ──────────────────────────────────────

    @PostMapping("/workers/register")
    public ResponseEntity<WorkerRegistrationResponse> registerWorker(@RequestBody WorkerRegistrationRequest request) {
        try {
            WorkerRegistrationResponse response = workerService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to register worker '{}': {}", request.getWorkerName(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/workers/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestParam("workerName") String workerName) {
        try {
            workerService.heartbeat(workerName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Failed heartbeat for worker '{}': {}", workerName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/workers/deregister")
    public ResponseEntity<Void> deregisterWorker(@RequestParam("workerName") String workerName) {
        try {
            workerService.deregister(workerName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Failed to deregister worker '{}': {}", workerName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/workers")
    public ResponseEntity<java.util.Collection<LlmWorkerService.WorkerInfo>> getActiveWorkers() {
        return ResponseEntity.ok(workerService.getActiveWorkers().values());
    }
}
