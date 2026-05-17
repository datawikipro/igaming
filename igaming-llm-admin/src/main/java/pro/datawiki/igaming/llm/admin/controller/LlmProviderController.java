package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.ModelLookupResponse;
import pro.datawiki.igaming.llm.admin.dto.SupportedProviderResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmProviderController {

    private final LlmProviderRepository providerRepository;
    private final LlmModelRepository modelRepository;
    private final LlmProviderKeyRepository keyRepository;

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
        List<LlmModel> models = modelRepository.findWithProviderByModelId(modelId);
        if (models.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        LlmModel m = models.get(0);
        return ResponseEntity.ok(ModelLookupResponse.builder()
                .modelId(m.getId())
                .modelName(m.getModelId())
                .displayName(m.getDisplayName())
                .providerId(m.getProvider().getId())
                .providerName(m.getProvider().getName())
                .providerDisplay(m.getProvider().getDisplayName())
                .build());
    }
}
