package pro.datawiki.igaming.llm.provider;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.config.GcpCredentialsManager;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GeminiProvider implements LlmProvider {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    @Value("${GOOGLE_API_KEY:}")
    private String apiKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        log.info("🔐 GeminiProvider: API key updated dynamically (REST mode active: {})", apiKey != null && !apiKey.isEmpty());
    }

    private final GcpCredentialsManager credentialsManager;
    private final List<VertexAI> vertexAIInstances = new ArrayList<>();
    private final AtomicInteger rotationIndex = new AtomicInteger(0);
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiProvider(GcpCredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("✅ GeminiProvider: REST mode active using GOOGLE_API_KEY");
        } else {
            log.info("Initializing GeminiProvider with {} credentials for Vertex AI", credentialsManager.getCredentialsCount());
            refreshInstances();
        }
    }
    
    private synchronized void refreshInstances() {
        // Clear old ones
        cleanup();
        vertexAIInstances.clear();
        
        // This is a bit simplified - we assume credentials don't change at runtime 
        // without a restart or explicit refresh.
        for (int i = 0; i < credentialsManager.getCredentialsCount(); i++) {
            VertexAI v = new VertexAI.Builder()
                    .setProjectId(projectId)
                    .setLocation(location)
                    .setCredentials(credentialsManager.getNextCredentials())
                    .build();
            vertexAIInstances.add(v);
        }
    }

    @PreDestroy
    public void cleanup() {
        for (VertexAI v : vertexAIInstances) {
            try {
                v.close();
            } catch (Exception e) {
                log.error("Error closing VertexAI instance: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getName() {
        return "gemini";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        if (apiKey != null && !apiKey.isEmpty()) {
            return generateRest(request);
        }

        if (vertexAIInstances.isEmpty()) {
            throw new IllegalStateException("No VertexAI instances available");
        }

        int idx = rotationIndex.getAndIncrement() % vertexAIInstances.size();
        VertexAI vertexAI = vertexAIInstances.get(idx);

        String modelName = request.getModel() != null ? request.getModel() : "gemini-3-flash-preview";
        log.info("🚀 Gemini Request: model={}, account_index={}", modelName, idx);

        try {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            
            // Set temperature/maxTokens if provided
            if (request.getTemperature() != null || request.getMaxTokens() != null) {
                // Config can be set on the model
            }

            GenerateContentResponse response = model.generateContent(request.getPrompt());
            String text = ResponseHandler.getText(response);

            return LlmResponse.builder()
                    .text(text)
                    .model(modelName)
                    .provider(getName())
                    .tokensUsed(response.getUsageMetadata().getTotalTokenCount())
                    .finishReason(response.getCandidates(0).getFinishReason().name())
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Gemini Error (account {}): {}", idx, e.getMessage());
            // If it's a 429, the next request will automatically use the next account due to rotationIndex increment
            throw new RuntimeException("Gemini execution failed", e);
        }
    }

    private LlmResponse generateRest(LlmRequest request) {
        String modelName = request.getModel() != null ? request.getModel() : "gemini-3-flash-preview";
        String googleModel = modelName;
        
        // Map to strictly 3.x series or appropriate names for REST API
        if (googleModel.contains("flash")) {
            googleModel = "gemini-3-flash-preview";
        } else if (googleModel.contains("pro")) {
            googleModel = "gemini-3-pro-preview";
        }

        log.info("🚀 Gemini REST Request: model={}, googleModel={}", modelName, googleModel);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + googleModel + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> contentsMap = new HashMap<>();
        List<Map<String, Object>> partsList = new ArrayList<>();
        partsList.add(Map.of("text", request.getPrompt()));
        contentsMap.put("parts", partsList);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(contentsMap));

        // Add systemInstruction if present
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            Map<String, Object> systemInstructionMap = new HashMap<>();
            systemInstructionMap.put("parts", List.of(Map.of("text", request.getSystemPrompt())));
            body.put("systemInstruction", systemInstructionMap);
        }

        // Add generationConfig if present
        Map<String, Object> generationConfig = new HashMap<>();
        if (request.getTemperature() != null) {
            generationConfig.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            generationConfig.put("maxOutputTokens", request.getMaxTokens());
        }
        if (!generationConfig.isEmpty()) {
            body.put("generationConfig", generationConfig);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response == null || !response.containsKey("candidates")) {
                throw new RuntimeException("Empty response from Gemini REST API");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates returned from Gemini REST API: " + response);
            }
            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            Map<String, Object> usage = (Map<String, Object>) response.get("usageMetadata");
            Integer totalTokens = usage != null ? (Integer) usage.get("totalTokenCount") : 0;

            return LlmResponse.builder()
                    .text(text)
                    .model(modelName)
                    .provider(getName())
                    .tokensUsed(totalTokens)
                    .finishReason((String) candidate.get("finishReason"))
                    .build();

        } catch (Exception e) {
            log.error("❌ Gemini REST Error: {}", e.getMessage());
            throw new RuntimeException("Gemini REST execution failed", e);
        }
    }
}
