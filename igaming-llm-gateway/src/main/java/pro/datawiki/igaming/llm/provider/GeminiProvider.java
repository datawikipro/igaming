package pro.datawiki.igaming.llm.provider;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.config.GcpCredentialsManager;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GeminiProvider implements LlmProvider {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    private final GcpCredentialsManager credentialsManager;
    private final List<VertexAI> vertexAIInstances = new ArrayList<>();
    private final AtomicInteger rotationIndex = new AtomicInteger(0);

    public GeminiProvider(GcpCredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing GeminiProvider with {} credentials", credentialsManager.getCredentialsCount());
        // We could initialize all instances now, or lazily. Let's do it lazily or pre-fill if we have them.
        refreshInstances();
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
        if (vertexAIInstances.isEmpty()) {
            throw new IllegalStateException("No VertexAI instances available");
        }

        int idx = rotationIndex.getAndIncrement() % vertexAIInstances.size();
        VertexAI vertexAI = vertexAIInstances.get(idx);

        String modelName = request.getModel() != null ? request.getModel() : "gemini-1.5-flash-002";
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
}
