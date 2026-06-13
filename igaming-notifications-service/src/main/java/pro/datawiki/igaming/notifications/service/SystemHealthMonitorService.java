package pro.datawiki.igaming.notifications.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.SystemHealthDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemHealthMonitorService {

    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate;

    @Value("${workload.manager.url:http://igaming-k8s-workload-manager/api/v1/workload/loaders/stats}")
    private String workloadManagerStatsUrl;

    public SystemHealthDto checkSystemHealth() {
        List<String> issues = new ArrayList<>();
        String status = "ONLINE";
        long maxDelay = 0;
        long offlineLoaders = 0;

        try {
            // 1. Check critical pods
            List<String> criticalDeployments = List.of("igaming-aggregator-ingestion", "igaming-bot-telegram", "igaming-aggregator-surebet");
            for (String depName : criticalDeployments) {
                Deployment d = kubernetesClient.apps().deployments().inNamespace("igaming-dev").withName(depName).get();
                if (d == null) {
                    issues.add(depName + " deployment not found");
                } else {
                    int ready = d.getStatus() != null && d.getStatus().getReadyReplicas() != null ? d.getStatus().getReadyReplicas() : 0;
                    if (ready == 0) {
                        issues.add(depName + " is offline (0 ready replicas)");
                    }
                }
            }

            // 2. Fetch loaders stats from Workload Manager
            List<Map<String, Object>> stats = fetchLoadersStats();
            
            for (Map<String, Object> stat : stats) {
                String avgLagStr = stat.get("avgLag") != null ? stat.get("avgLag").toString() : null;
                if (avgLagStr != null && avgLagStr.endsWith(" min")) {
                    try {
                        long delay = Long.parseLong(avgLagStr.replace(" min", ""));
                        if (delay > maxDelay) {
                            maxDelay = delay;
                        }
                    } catch (NumberFormatException ignored) {}
                }
                
                int readyInstances = stat.get("readyInstances") != null ? (Integer) stat.get("readyInstances") : 0;
                int desiredInstances = stat.get("desiredInstances") != null ? (Integer) stat.get("desiredInstances") : 0;
                if (readyInstances == 0 && desiredInstances > 0) {
                    offlineLoaders++;
                }
            }

            if (maxDelay > 15) {
                issues.add("Odds are stale! Max delay is " + maxDelay + " minutes (> 15m threshold)");
            }

            if (offlineLoaders > 0) {
                issues.add(offlineLoaders + " loader deployments are scaled up but have 0 ready instances");
            }

        } catch (Exception e) {
            status = "DEGRADED";
            issues.add("Failed to retrieve complete system health: " + e.getMessage());
            log.error("Health check error: {}", e.getMessage());
        }

        if (!issues.isEmpty() && "ONLINE".equals(status)) {
            status = "DEGRADED";
        }

        return SystemHealthDto.builder()
                .status(status)
                .issues(issues)
                .maxDelayMinutes(maxDelay)
                .offlineLoadersCount(offlineLoaders)
                .build();
    }

    private List<Map<String, Object>> fetchLoadersStats() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    workloadManagerStatsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch loader stats from Workload Manager: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
}
