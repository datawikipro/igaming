package pro.datawiki.igaming.k8s.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PlaneTaskDispatcherClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${plane.executor.url:http://plane-ai-executor.plane.svc.cluster.local:8080/api/v1/voice/create-task}")
    private String planeExecutorUrl;

    @Value("${plane.base-url:https://plane.smartbet.guru}")
    private String planeBaseUrl;

    @Value("${plane.workspace-slug:dataplatform}")
    private String workspaceSlug;

    @Value("${plane.project-identifier:IGAMING}")
    private String projectIdentifier;

    public PlaneTaskDispatcherClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    @Data
    @Builder
    public static class PlaneDispatchResult {
        private boolean success;
        private String issueId;
        private String planeUrl;
        private String errorMessage;
    }

    public PlaneDispatchResult dispatchAiTask(String title, String descriptionMarkdown) {
        log.info("🚀 Dispatching incident task to Plane AI Worker: title='{}'", title);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("workspaceSlug", workspaceSlug);
            payload.put("projectIdentifier", projectIdentifier);
            payload.put("title", title);
            payload.put("descriptionMarkdown", descriptionMarkdown);
            payload.put("stateName", "Backlog");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(planeExecutorUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String issueId = root.path("issueId").asText(null);
                String planeUrl = root.path("planeUrl").asText(null);

                if (planeUrl == null && issueId != null) {
                    planeUrl = String.format("%s/%s/projects/%s/issues/%s", planeBaseUrl, workspaceSlug, projectIdentifier, issueId);
                }

                log.info("✅ Plane task created successfully! issueId={}, url={}", issueId, planeUrl);

                return PlaneDispatchResult.builder()
                        .success(true)
                        .issueId(issueId)
                        .planeUrl(planeUrl)
                        .build();
            } else {
                String msg = "Non-2xx response from plane executor: " + response.getStatusCode();
                log.warn(msg);
                return PlaneDispatchResult.builder().success(false).errorMessage(msg).build();
            }
        } catch (Exception e) {
            log.error("❌ Failed to create task in Plane: {}", e.getMessage());
            return PlaneDispatchResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
