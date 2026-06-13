package pro.datawiki.igaming.notifications.service;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadsTokenRefreshService {

    private final KubernetesClient kubernetesClient;
    private final ObjectMapper objectMapper;

    /**
     * Scheduled task to refresh the Threads access token.
     * Runs once every 30 days.
     */
    @Scheduled(cron = "0 0 3 */30 * *")
    public void scheduledTokenRefresh() {
        log.info("Starting scheduled Threads access token refresh task...");
        try {
            refreshThreadsToken();
        } catch (Exception e) {
            log.error("Failed to execute scheduled Threads access token refresh: {}", e.getMessage(), e);
        }
    }

    public String refreshThreadsToken() {
        log.info("Initializing Threads access token refresh...");
        
        // 1. Retrieve current token from igaming-bot-threads deployment
        String currentToken = getThreadsTokenFromDeployment("igaming-bot-threads");
        if (currentToken == null || currentToken.isEmpty()) {
            // Fallback to igaming-bot-telegram
            currentToken = getThreadsTokenFromDeployment("igaming-bot-telegram");
        }

        if (currentToken == null || currentToken.isEmpty()) {
            throw new IllegalStateException("Could not retrieve current THREADS_ACCESS_TOKEN from any deployment");
        }

        // 2. Call Threads Graph API to refresh the token
        String newToken = callThreadsRefreshApi(currentToken);
        
        // 3. Update deployments
        updateDeploymentToken("igaming-bot-threads", newToken);
        updateDeploymentToken("igaming-bot-telegram", newToken);
        
        log.info("Threads access token successfully refreshed and applied to deployments.");
        return newToken;
    }

    private String getThreadsTokenFromDeployment(String depName) {
        try {
            Deployment d = kubernetesClient.apps().deployments().inNamespace("igaming-dev").withName(depName).get();
            if (d != null && d.getSpec() != null && d.getSpec().getTemplate() != null && d.getSpec().getTemplate().getSpec() != null) {
                var containers = d.getSpec().getTemplate().getSpec().getContainers();
                if (containers != null && !containers.isEmpty()) {
                    var container = containers.get(0);
                    var envVars = container.getEnv();
                    if (envVars != null) {
                        for (EnvVar env : envVars) {
                            if ("THREADS_ACCESS_TOKEN".equals(env.getName())) {
                                return env.getValue();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve Threads token from deployment {}: {}", depName, e.getMessage());
        }
        return null;
    }

    private String callThreadsRefreshApi(String currentToken) {
        try {
            String url = "https://graph.threads.net/refresh_access_token?grant_type=th_refresh_token&access_token=" + currentToken;
            log.info("Sending token refresh request to Threads Graph API...");
            
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Threads API returned non-200 status code: " + response.statusCode() + ", body: " + response.body());
            }
            
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode accessTokenNode = root.get("access_token");
            if (accessTokenNode == null) {
                throw new RuntimeException("access_token not found in response payload: " + response.body());
            }
            
            return accessTokenNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Threads Graph API for token refresh: " + e.getMessage(), e);
        }
    }

    private void updateDeploymentToken(String depName, String newToken) {
        try {
            Deployment d = kubernetesClient.apps().deployments().inNamespace("igaming-dev").withName(depName).get();
            if (d == null) {
                log.warn("Deployment {} not found, skipping update", depName);
                return;
            }
            
            if (d.getSpec() != null && d.getSpec().getTemplate() != null && d.getSpec().getTemplate().getSpec() != null) {
                var containers = d.getSpec().getTemplate().getSpec().getContainers();
                if (containers != null && !containers.isEmpty()) {
                    var container = containers.get(0);
                    var envVars = container.getEnv();
                    if (envVars != null) {
                        boolean updated = false;
                        for (EnvVar env : envVars) {
                            if ("THREADS_ACCESS_TOKEN".equals(env.getName())) {
                                env.setValue(newToken);
                                updated = true;
                                break;
                            }
                        }
                        
                        if (updated) {
                            kubernetesClient.apps().deployments().inNamespace("igaming-dev").resource(d).update();
                            log.info("Updated THREADS_ACCESS_TOKEN env var for deployment {}", depName);
                        } else {
                            log.warn("THREADS_ACCESS_TOKEN env var not found in deployment {}, skipping update", depName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update deployment {} with new token: {}", depName, e.getMessage(), e);
            throw new RuntimeException("Failed to update deployment " + depName, e);
        }
    }
}
