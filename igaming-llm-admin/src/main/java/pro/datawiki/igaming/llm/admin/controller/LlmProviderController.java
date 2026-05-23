package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmProvider;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.dto.SupportedProviderResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmProviderController {

    private final LlmProviderRepository providerRepository;
    private final LlmProviderKeyRepository keyRepository;
    private final LlmModelRepository modelRepository;

    // ─── Providers ───────────────────────────────────────────────────────────

    @GetMapping("/providers")
    public List<LlmProvider> listProviders() {
        return providerRepository.findAll();
    }

    @GetMapping("/providers/supported")
    public List<SupportedProviderResponse> getSupportedProviders() {
        return providerRepository.findAll().stream()
                .filter(LlmProvider::isActive)
                .map(provider -> SupportedProviderResponse.builder()
                        .name(provider.getName())
                        .displayName(provider.getDisplayName())
                        .models(
                                modelRepository.findByProviderIdAndActiveTrue(provider.getId())
                                        .stream()
                                        .map(m -> m.getModelId())
                                        .collect(Collectors.toList())
                        )
                        .build()
                )
                .collect(Collectors.toList());
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

}
