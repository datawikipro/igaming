package pro.datawiki.igaming.llm.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.llm.admin.domain.LlmModel;
import pro.datawiki.igaming.llm.admin.dto.ModelLookupResponse;
import pro.datawiki.igaming.llm.admin.repository.LlmModelRepository;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderRepository;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LlmModelController {

    private final LlmProviderRepository providerRepository;
    private final LlmModelRepository modelRepository;

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
