package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.k8s.config.HealthWatcherProperties;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointHealthWatcherService {

    private final HealthWatcherProperties properties;
    private final KubernetesClient kubernetesClient;

    private final Map<String, TargetHealthState> targetStates = new ConcurrentHashMap<>();

    @Data
    @Builder
    public static class TargetHealthState {
        private String name;
        private String url;
        private String namespace;
        private String deploymentName;
        private String status; // HEALTHY, DEGRADED, RESTARTING, UNHEALTHY, DISABLED
        private int consecutiveFailures;
        private long lastResponseTimeMs;
        private Integer lastStatusCode;
        private String lastErrorMessage;
        private Instant lastCheckTime;
        private Instant lastRestartTime;
        private int totalRestarts;
    }

    @Scheduled(fixedDelayString = "${health-watcher.poll-interval-ms:30000}", initialDelay = 15000)
    public void scheduledHealthCheck() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            checkAllEndpoints();
        } catch (Exception e) {
            log.error("❌ Scheduled health check encountered unexpected error: {}", e.getMessage(), e);
        }
    }

    public synchronized List<TargetHealthState> checkAllEndpoints() {
        List<HealthWatcherProperties.TargetService> targets = properties.getTargets();
        List<TargetHealthState> results = new ArrayList<>();

        for (HealthWatcherProperties.TargetService target : targets) {
            if (!target.isEnabled()) {
                targetStates.compute(target.getName(), (k, v) -> TargetHealthState.builder()
                        .name(target.getName())
                        .url(target.getUrl())
                        .namespace(target.getNamespace())
                        .deploymentName(target.getDeploymentName())
                        .status("DISABLED")
                        .lastCheckTime(Instant.now())
                        .build());
                continue;
            }

            TargetHealthState state = probeTarget(target);
            targetStates.put(target.getName(), state);
            results.add(state);
        }

        return results;
    }

    private TargetHealthState probeTarget(HealthWatcherProperties.TargetService target) {
        TargetHealthState previous = targetStates.get(target.getName());
        int prevFailures = previous != null ? previous.getConsecutiveFailures() : 0;
        Instant lastRestart = previous != null ? previous.getLastRestartTime() : null;
        int totalRestarts = previous != null ? previous.getTotalRestarts() : 0;

        int timeoutMs = target.getTimeoutMs() > 0 ? target.getTimeoutMs() : properties.getDefaultReadTimeoutMs();
        int connectTimeoutMs = properties.getDefaultConnectTimeoutMs();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        long start = System.currentTimeMillis();
        boolean success = false;
        Integer statusCode = null;
        String errorMessage = null;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(URI.create(target.getUrl()), String.class);
            statusCode = response.getStatusCode().value();
            if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) {
                success = true;
            } else {
                errorMessage = "Non-success status code: " + statusCode;
            }
        } catch (ResourceAccessException e) {
            errorMessage = "Connection / Read Timeout (" + timeoutMs + "ms): " + e.getMessage();
        } catch (Exception e) {
            errorMessage = "HTTP Request Error: " + e.getMessage();
        }

        long responseTimeMs = System.currentTimeMillis() - start;
        Instant now = Instant.now();

        if (success) {
            if (prevFailures > 0) {
                log.info("💚 Service '{}' ({}) recovered after {} previous failure(s). Response time: {}ms",
                        target.getName(), target.getUrl(), prevFailures, responseTimeMs);
            }
            return TargetHealthState.builder()
                    .name(target.getName())
                    .url(target.getUrl())
                    .namespace(target.getNamespace())
                    .deploymentName(target.getDeploymentName())
                    .status("HEALTHY")
                    .consecutiveFailures(0)
                    .lastResponseTimeMs(responseTimeMs)
                    .lastStatusCode(statusCode)
                    .lastErrorMessage(null)
                    .lastCheckTime(now)
                    .lastRestartTime(lastRestart)
                    .totalRestarts(totalRestarts)
                    .build();
        } else {
            int currentFailures = prevFailures + 1;
            int threshold = target.getFailureThreshold() > 0 ? target.getFailureThreshold() : properties.getDefaultFailureThreshold();
            int cooldownMinutes = target.getCooldownMinutes() > 0 ? target.getCooldownMinutes() : properties.getDefaultCooldownMinutes();

            String status = currentFailures >= threshold ? "UNHEALTHY" : "DEGRADED";

            log.warn("⚠️ Health check FAILED for '{}' ({}) [{}/{}]: {} (duration: {}ms)",
                    target.getName(), target.getUrl(), currentFailures, threshold, errorMessage, responseTimeMs);

            if (target.isAutoRestart() && currentFailures >= threshold) {
                boolean inCooldown = lastRestart != null && Duration.between(lastRestart, now).toMinutes() < cooldownMinutes;
                if (!inCooldown) {
                    try {
                        log.warn("🚨 RESTARTING DEPLOYMENT {}/{} due to service hang / {} consecutive failures (reason: {})",
                                target.getNamespace(), target.getDeploymentName(), currentFailures, errorMessage);

                        kubernetesClient.apps()
                                .deployments()
                                .inNamespace(target.getNamespace())
                                .withName(target.getDeploymentName())
                                .rolling()
                                .restart();

                        lastRestart = now;
                        totalRestarts++;
                        status = "RESTARTING";
                        currentFailures = 0;
                    } catch (Exception e) {
                        log.error("❌ Failed to trigger rolling restart for deployment {}/{}: {}",
                                target.getNamespace(), target.getDeploymentName(), e.getMessage(), e);
                    }
                } else {
                    long remainingCooldown = cooldownMinutes - Duration.between(lastRestart, now).toMinutes();
                    log.info("⏳ Restart cooldown active for deployment {}/{} ({} min remaining)",
                            target.getNamespace(), target.getDeploymentName(), remainingCooldown);
                }
            }

            return TargetHealthState.builder()
                    .name(target.getName())
                    .url(target.getUrl())
                    .namespace(target.getNamespace())
                    .deploymentName(target.getDeploymentName())
                    .status(status)
                    .consecutiveFailures(currentFailures)
                    .lastResponseTimeMs(responseTimeMs)
                    .lastStatusCode(statusCode)
                    .lastErrorMessage(errorMessage)
                    .lastCheckTime(now)
                    .lastRestartTime(lastRestart)
                    .totalRestarts(totalRestarts)
                    .build();
        }
    }

    public List<TargetHealthState> getHealthStates() {
        return new ArrayList<>(targetStates.values());
    }
}