package pro.datawiki.igaming.llm.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeepSeekProvider implements LlmProvider {

    @Value("${deepseek.api-keys:}")
    private String apiKeysConfig;

    @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    private List<String> apiKeys = new ArrayList<>();
    private final AtomicInteger rotationIndex = new AtomicInteger(0);
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        if (apiKeysConfig != null && !apiKeysConfig.isEmpty()) {
            apiKeys = Arrays.stream(apiKeysConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            log.info("✅ DeepSeekProvider initialized with {} keys", apiKeys.size());
        } else {
            log.warn("⚠️ DeepSeekProvider: No API keys configured");
        }
    }

    @Override
    public String getName() {
        return "deepseek";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("DeepSeek API keys not configured");
        }

        int idx = rotationIndex.getAndIncrement() % apiKeys.size();
        String apiKey = apiKeys.get(idx);

        String modelName = request.getModel() != null ? request.getModel() : "deepseek-chat";
        log.info("🚀 DeepSeek Request: model={}, account_index={}", modelName, idx);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("messages", List.of(
            Map.of("role", "system", "content", request.getSystemPrompt() != null ? request.getSystemPrompt() : "You are a helpful assistant"),
            Map.of("role", "user", "content", request.getPrompt())
        ));
        
        if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
        if (request.getMaxTokens() != null) body.put("max_tokens", request.getMaxTokens());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(baseUrl + "/chat/completions", entity, Map.class);
            
            if (response == null || !response.containsKey("choices")) {
                throw new RuntimeException("Empty response from DeepSeek");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String text = (String) message.get("content");
            
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            Integer totalTokens = usage != null ? (Integer) usage.get("total_tokens") : 0;

            return LlmResponse.builder()
                    .text(text)
                    .model(modelName)
                    .provider(getName())
                    .tokensUsed(totalTokens)
                    .finishReason((String) choice.get("finish_reason"))
                    .build();

        } catch (Exception e) {
            log.error("❌ DeepSeek Error (account {}): {}", idx, e.getMessage());
            throw new RuntimeException("DeepSeek execution failed", e);
        }
    }
}
