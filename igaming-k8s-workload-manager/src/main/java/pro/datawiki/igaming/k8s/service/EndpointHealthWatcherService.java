package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class EndpointHealthWatcherService {

    private final HealthWatcherProperties properties;
    private final KubernetesClient kubernetesClient;
    private final Map<String, TargetHealthState> targetStates = new ConcurrentHashMap<>();

    public EndpointHealthWatcherService(HealthWatcherProperties properties, KubernetesClient kubernetesClient) {
        this.properties = properties;
        this.kubernetesClient = kubernetesClient;
    }

    @Scheduled(fixedDelayString = "${health-watcher.poll-interval-ms:30000}", initialDelay = 15000L)
    public void scheduledHealthCheck() {
        if (!this.properties.isEnabled()) {
            return;
        }
        try {
            this.checkAllEndpoints();
        } catch (Exception e) {
            log.error("❌ Scheduled health check encountered unexpected error: {}", e.getMessage(), e);
        }
    }

    public synchronized List<TargetHealthState> checkAllEndpoints() {
        List<HealthWatcherProperties.TargetService> targets = resolveTargets();
        List<TargetHealthState> results = new ArrayList<>();

        for (HealthWatcherProperties.TargetService target : targets) {
            if (!target.isEnabled()) {
                this.targetStates.compute(target.getName(), (k, v) -> TargetHealthState.builder()
                        .name(target.getName())
                        .url(target.getUrl())
                        .namespace(target.getNamespace())
                        .deploymentName(target.getDeploymentName())
                        .status("DISABLED")
                        .lastCheckTime(Instant.now())
                        .build());
                continue;
            }

            TargetHealthState state = this.probeTarget(target);
            this.targetStates.put(target.getName(), state);
            results.add(state);
        }
        return results;
    }

    private List<HealthWatcherProperties.TargetService> resolveTargets() {
        if (this.properties.getTargets() != null && !this.properties.getTargets().isEmpty()) {
            return this.properties.getTargets();
        }
        return getDefaultTargets();
    }

    public static List<HealthWatcherProperties.TargetService> getDefaultTargets() {
        return List.of(
                HealthWatcherProperties.TargetService.builder()
                        .name("airflow-api")
                        .url("http://airflow-api-server.airflow.svc.cluster.local:8080/api/v2/monitor/health")
                        .namespace("airflow")
                        .deploymentName("airflow-api-server")
                        .timeoutMs(10000)
                        .failureThreshold(3)
                        .cooldownMinutes(15)
                        .enabled(true)
                        .autoRestart(true)
                        .build(),
                HealthWatcherProperties.TargetService.builder()
                        .name("llm-admin")
                        .url("http://llm-admin.llm.svc.cluster.local:80/actuator/health")
                        .namespace("llm")
                        .deploymentName("llm-admin")
                        .timeoutMs(10000)
                        .failureThreshold(3)
                        .cooldownMinutes(15)
                        .enabled(true)
                        .autoRestart(true)
                        .build(),
                HealthWatcherProperties.TargetService.builder()
                        .name("data-catalog")
                        .url("http://data-catalog-backend.data-catalog.svc.cluster.local:8080/api/sources")
                        .namespace("data-catalog")
                        .deploymentName("data-catalog-backend")
                        .timeoutMs(10000)
                        .failureThreshold(3)
                        .cooldownMinutes(15)
                        .enabled(true)
                        .autoRestart(true)
                        .build(),
                HealthWatcherProperties.TargetService.builder()
                        .name("superset")
                        .url("http://superset.bi.svc.cluster.local:8088/superset/welcome/")
                        .namespace("bi")
                        .deploymentName("superset")
                        .timeoutMs(15000)
                        .failureThreshold(3)
                        .cooldownMinutes(15)
                        .enabled(false)
                        .autoRestart(false)
                        .build()
        );
    }

    private TargetHealthState probeTarget(HealthWatcherProperties.TargetService target) {
        TargetHealthState previous = this.targetStates.get(target.getName());
        int prevFailures = previous != null ? previous.getConsecutiveFailures() : 0;
        Instant lastRestart = previous != null ? previous.getLastRestartTime() : null;
        int totalRestarts = previous != null ? previous.getTotalRestarts() : 0;

        int timeoutMs = target.getTimeoutMs() > 0 ? target.getTimeoutMs() : this.properties.getDefaultReadTimeoutMs();
        int connectTimeoutMs = this.properties.getDefaultConnectTimeoutMs();

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
        }

        int currentFailures = prevFailures + 1;
        int threshold = target.getFailureThreshold() > 0 ? target.getFailureThreshold() : this.properties.getDefaultFailureThreshold();
        int cooldownMinutes = target.getCooldownMinutes() > 0 ? target.getCooldownMinutes() : this.properties.getDefaultCooldownMinutes();

        String status = currentFailures >= threshold ? "UNHEALTHY" : "DEGRADED";
        log.warn("⚠️ Health check FAILED for '{}' ({}) [{}/{}]: {} (duration: {}ms)",
                target.getName(), target.getUrl(), currentFailures, threshold, errorMessage, responseTimeMs);

        if (target.isAutoRestart() && currentFailures >= threshold) {
            boolean inCooldown = lastRestart != null && Duration.between(lastRestart, now).toMinutes() < cooldownMinutes;
            if (!inCooldown) {
                try {
                    log.warn("🚨 RESTARTING DEPLOYMENT {}/{} due to service hang / {} consecutive failures (reason: {})",
                            target.getNamespace(), target.getDeploymentName(), currentFailures, errorMessage);
                    this.kubernetesClient.apps().deployments()
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

    public List<TargetHealthState> getHealthStates() {
        return new ArrayList<>(this.targetStates.values());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetHealthState {
        private String name;
        private String url;
        private String namespace;
        private String deploymentName;
        private String status;
        private int consecutiveFailures;
        private long lastResponseTimeMs;
        private Integer lastStatusCode;
        private String lastErrorMessage;
        private Instant lastCheckTime;
        private Instant lastRestartTime;
        private int totalRestarts;
    }
}
