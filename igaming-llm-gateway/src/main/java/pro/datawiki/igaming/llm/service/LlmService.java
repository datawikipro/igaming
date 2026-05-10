package pro.datawiki.igaming.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;
import pro.datawiki.igaming.llm.provider.LlmProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LlmService {

    private final Map<String, LlmProvider> providers;

    public LlmService(List<LlmProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(LlmProvider::getName, p -> p));
        log.info("Registered providers: {}", providers.keySet());
    }

    @Cacheable(value = "llm-responses", key = "{#request.model, #request.prompt, #request.systemPrompt}", unless = "#result == null")
    public LlmResponse generate(LlmRequest request) {
        String providerName = determineProvider(request);
        LlmProvider provider = providers.get(providerName);

        if (provider == null) {
            throw new IllegalArgumentException("Provider not found: " + providerName);
        }

        // Add retry logic if needed (handled internally by rotation in providers for now)
        return provider.generate(request);
    }

    private String determineProvider(LlmRequest request) {
        if (request.getModel() != null) {
            String model = request.getModel().toLowerCase();
            if (model.contains("gemini-cli") || model.contains("gemini_cli")) return "gemini-cli";
            if (model.contains("gemini")) return "gemini";
            if (model.contains("deepseek")) return "deepseek";
            if (model.contains("agent-studio") || model.contains("agent_studio")) return "agent-studio";
        }
        return "gemini"; // Default
    }
}
