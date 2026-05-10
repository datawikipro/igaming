package pro.datawiki.igaming.llm.provider;

import com.google.cloud.dialogflow.cx.v3.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.config.GcpCredentialsManager;
import pro.datawiki.igaming.llm.dto.LlmRequest;
import pro.datawiki.igaming.llm.dto.LlmResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GoogleAgentStudioProvider implements LlmProvider {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location:global}")
    private String location;

    @Value("${google.agent-studio.agent-id}")
    private String agentId;

    private final GcpCredentialsManager credentialsManager;
    private final List<SessionsClient> sessionsClients = new ArrayList<>();
    private final AtomicInteger rotationIndex = new AtomicInteger(0);

    public GoogleAgentStudioProvider(GcpCredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @PostConstruct
    public void init() throws IOException {
        log.info("Initializing GoogleAgentStudioProvider with agent {}", agentId);
        refreshClients();
    }

    private synchronized void refreshClients() throws IOException {
        cleanup();
        sessionsClients.clear();

        for (int i = 0; i < credentialsManager.getCredentialsCount(); i++) {
            SessionsSettings settings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(() -> credentialsManager.getNextCredentials())
                    // If not global, need to set endpoint
                    .setEndpoint(location.equals("global") ? 
                        "dialogflow.googleapis.com:443" : 
                        location + "-dialogflow.googleapis.com:443")
                    .build();
            
            sessionsClients.add(SessionsClient.create(settings));
        }
    }

    @PreDestroy
    public void cleanup() {
        for (SessionsClient client : sessionsClients) {
            client.close();
        }
    }

    @Override
    public String getName() {
        return "agent-studio";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        if (sessionsClients.isEmpty()) {
            throw new IllegalStateException("No Dialogflow clients available");
        }

        int idx = rotationIndex.getAndIncrement() % sessionsClients.size();
        SessionsClient client = sessionsClients.get(idx);

        String sessionId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();
        SessionName sessionName = SessionName.ofProjectLocationAgentSessionName(projectId, location, agentId, sessionId);

        TextInput textInput = TextInput.newBuilder().setText(request.getPrompt()).build();
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).setLanguageCode("ru").build();
        DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
                .setSession(sessionName.toString())
                .setQueryInput(queryInput)
                .build();

        log.info("🚀 Agent Studio Request: agent={}, account_index={}, session={}", agentId, idx, sessionId);

        try {
            DetectIntentResponse response = client.detectIntent(detectIntentRequest);
            QueryResult queryResult = response.getQueryResult();
            
            StringBuilder textOutput = new StringBuilder();
            for (ResponseMessage msg : queryResult.getResponseMessagesList()) {
                if (msg.hasText()) {
                    textOutput.append(String.join("", msg.getText().getTextList()));
                }
            }

            return LlmResponse.builder()
                    .text(textOutput.toString())
                    .model("agent-studio")
                    .provider(getName())
                    .tokensUsed(0) // CX doesn't provide tokens in simple way
                    .finishReason("stop")
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Agent Studio Error (account {}): {}", idx, e.getMessage());
            throw new RuntimeException("Agent Studio execution failed", e);
        }
    }
}
