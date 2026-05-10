package pro.datawiki.igaming.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.llm.dto.LlmLeaseRequest;
import pro.datawiki.igaming.llm.dto.LlmLeaseResponse;
import pro.datawiki.igaming.llm.provider.GeminiProvider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
public class LlmLeaseService {

    @Value("${app.llm.admin-url:http://igaming-llm-admin.igaming-llm.svc.cluster.local}")
    private String adminUrl;

    @Value("${app.llm.dynamic-lease-enabled:true}")
    private boolean leaseEnabled;

    @Value("${app.llm.pod-name:}")
    private String podName;

    @Value("${app.llm.pod-ip:127.0.0.1}")
    private String podIp;

    @Value("${app.llm.provider-type:gemini}")
    private String providerType;

    private final GeminiProvider geminiProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean leasedSuccessfully = false;

    public LlmLeaseService(GeminiProvider geminiProvider) {
        this.geminiProvider = geminiProvider;
    }

    @PostConstruct
    public void init() {
        if (!leaseEnabled) {
            log.info("ℹ️ LlmLeaseService: Dynamic leasing is disabled. Using local configurations.");
            return;
        }

        // Generate a fallback podName if empty (useful for local testing)
        if (podName == null || podName.isEmpty()) {
            podName = "local-llm-gateway-" + (System.currentTimeMillis() % 10000);
            log.info("ℹ️ LlmLeaseService: Empty podName. Generated local fallback: {}", podName);
        }

        log.info("=== Starting LLM Lease Allocation Process ===");
        log.info("Pod Name: {}", podName);
        log.info("Pod IP: {}", podIp);
        log.info("Admin URL: {}", adminUrl);
        log.info("Provider: {}", providerType);

        LlmLeaseRequest request = LlmLeaseRequest.builder()
                .podName(podName)
                .podIp(podIp)
                .providerType(providerType)
                .build();

        int maxRetries = 30;
        int delaySeconds = 5;
        String leaseEndpoint = adminUrl + "/api/v1/llm/lease";

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Requesting LLM configuration lease (Attempt {}/{})...", attempt, maxRetries);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<LlmLeaseRequest> entity = new HttpEntity<>(request, headers);

                LlmLeaseResponse response = restTemplate.postForObject(leaseEndpoint, entity, LlmLeaseResponse.class);
                if (response != null && response.getApiKey() != null && !response.getApiKey().isEmpty()) {
                    log.info("✅ Successfully leased LLM configuration: {} (Model: {})", response.getName(), response.getModelName());
                    geminiProvider.setApiKey(response.getApiKey());
                    leasedSuccessfully = true;
                    return;
                } else {
                    log.warn("⚠️ Received empty response or missing API key from lease endpoint: {}", response);
                }
            } catch (Exception e) {
                log.warn("⚠️ Attempt {}/{} failed to connect to LLM admin lease endpoint: {}", attempt, maxRetries, e.getMessage());
            }

            try {
                Thread.sleep(delaySeconds * 1000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("❌ Lease acquisition loop interrupted");
                break;
            }
        }

        log.error("❌ ERROR: Failed to lease LLM configuration after {} attempts. Startup aborted.", maxRetries);
        throw new IllegalStateException("Failed to lease LLM configuration from " + leaseEndpoint);
    }

    @PreDestroy
    public void cleanup() {
        if (!leasedSuccessfully) {
            return;
        }

        try {
            log.info("🔄 Releasing LLM lease gracefully for pod '{}'...", podName);
            String releaseEndpoint = adminUrl + "/api/v1/llm/release";

            LlmLeaseRequest request = LlmLeaseRequest.builder()
                    .podName(podName)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LlmLeaseRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForObject(releaseEndpoint, entity, Void.class);
            log.info("✅ LLM lease released successfully for pod '{}'", podName);
        } catch (Exception e) {
            log.error("❌ Failed to release LLM lease gracefully: {}", e.getMessage());
        }
    }
}
