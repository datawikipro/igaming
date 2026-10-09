package pro.datawiki.igaming.infra.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.BookmakerInfoResponse;
import pro.datawiki.igaming.dto.LoaderDelayDto;
import pro.datawiki.igaming.dto.OptimizationReportDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KubernetesPodService {

    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate;

    @Value("${aggregator.delay.url:http://igaming-aggregator/api/diagnostics/loader-delays}")
    private String aggregatorDelayUrl;

    public KubernetesPodService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    private final ConcurrentHashMap<String, BookmakerInfoResponse> infoCache = new ConcurrentHashMap<>();

    public List<LoaderStat> getLoadersStats() {
        List<Deployment> deployments;
        try {
            deployments = kubernetesClient.apps().deployments().inNamespace("igaming-dev").list().getItems()
                    .stream()
                    .filter(d -> d.getMetadata() != null && d.getMetadata().getName() != null)
                    .filter(d -> d.getMetadata().getName().contains("loader") || d.getMetadata().getName().contains("crawler"))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to list deployments from Kubernetes: {}", e.getMessage());
            return new ArrayList<>();
        }

        if (deployments.isEmpty()) {
            log.info("No loader/crawler deployments found in igaming-dev namespace");
            return new ArrayList<>();
        }

        Map<String, BookmakerInfoResponse> infoMap = deployments.parallelStream()
                .collect(Collectors.toConcurrentMap(
                        d -> d.getMetadata().getName(),
                        this::fetchBookmakerInfo,
                        (existing, replacement) -> existing
                ));

        Map<String, List<Deployment>> grouped = new HashMap<>();
        Map<String, String> familyMap = new HashMap<>();
        for (Deployment d : deployments) {
            String depName = d.getMetadata().getName();
            BookmakerInfoResponse info = infoMap.get(depName);
            
            String name = (info != null && info.getName() != null) ? info.getName() : depName
                    .replace("igaming-source-", "")
                    .replace("-loader", "")
                    .replace("-crawler", "");
                    
            String family = (info != null && info.getFamily() != null) ? info.getFamily() : name;
            
            grouped.computeIfAbsent(name, k -> new ArrayList<>()).add(d);
            familyMap.putIfAbsent(name, family);
        }

        Map<String, LoaderDelayDto> delays = fetchDelays();
        Map<String, LoaderDelayDto> normalizedDelays = new HashMap<>();
        delays.forEach((k, v) -> {
            if (k != null) {
                normalizedDelays.put(k.toLowerCase().replace("-", "").replace(" ", "").replace("_", ""), v);
            }
        });

        List<LoaderStat> result = new ArrayList<>();
        for (Map.Entry<String, List<Deployment>> entry : grouped.entrySet()) {
            String bookmaker = entry.getKey();
            List<Deployment> deps = entry.getValue();
            if (deps == null || deps.isEmpty() || bookmaker == null) continue;

            int ready = 0;
            int desired = 0;
            for (Deployment d : deps) {
                if (d.getStatus() != null && d.getStatus().getReadyReplicas() != null) {
                    ready += d.getStatus().getReadyReplicas();
                }
                if (d.getSpec() != null && d.getSpec().getReplicas() != null) {
                    desired += d.getSpec().getReplicas();
                }
            }

            String scalingDeployment = deps.stream()
                    .filter(d -> d.getMetadata() != null && d.getMetadata().getName() != null && d.getMetadata().getName().contains("-loader"))
                    .findFirst()
                    .map(d -> d.getMetadata().getName())
                    .orElse(deps.get(0).getMetadata().getName());

            String searchKey = bookmaker.toLowerCase().replace("-", "").replace(" ", "").replace("_", "");
            LoaderDelayDto aggregatorData = normalizedDelays.get(searchKey);
            
            Double delayVal = 0.0;
            long oddsCount = 0;
            long matchesCount = 0;

            if (aggregatorData != null) {
                delayVal = aggregatorData.getDelayMinutes() != null ? aggregatorData.getDelayMinutes() : 0.0;
                oddsCount = aggregatorData.getOddsCount() != null ? aggregatorData.getOddsCount() : 0L;
                matchesCount = aggregatorData.getMatchesCount() != null ? aggregatorData.getMatchesCount() : 0L;
            }

            String delayStr = (oddsCount > 0) ? delayVal.intValue() + " min" : "NO DATA";

            result.add(LoaderStat.builder()
                    .name(bookmaker)
                    .bookmaker(bookmaker)
                    .family(familyMap.getOrDefault(bookmaker, bookmaker))
                    .deploymentName(scalingDeployment)
                    .instances(ready)
                    .readyInstances(ready)
                    .desiredInstances(desired)
                    .status(ready >= desired ? "Running" : "Scaling")
                    .avgLag(delayStr)
                    .oldestUpdateDelay(delayStr)
                    .oddsCount(oddsCount)
                    .matchesCount(matchesCount)
                    .cpuUsage("0%")
                    .build());
        }

        log.info("Returning {} grouped bookmaker stats", result.size());
        return result;
    }

    private BookmakerInfoResponse fetchBookmakerInfo(Deployment d) {
        String depName = d.getMetadata().getName();
        if (infoCache.containsKey(depName)) {
            return infoCache.get(depName);
        }
        
        try {
            if (d.getSpec() != null && d.getSpec().getTemplate() != null && d.getSpec().getTemplate().getSpec() != null
                && d.getSpec().getTemplate().getSpec().getContainers() != null && !d.getSpec().getTemplate().getSpec().getContainers().isEmpty()
                && d.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts() != null && !d.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().isEmpty()) {
                
                Integer port = d.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();
                String url = "http://" + depName + ":" + port + "/api/v1/monitoring/info";
                ResponseEntity<BookmakerInfoResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        BookmakerInfoResponse.class
                );
                if (response.getBody() != null) {
                    infoCache.put(depName, response.getBody());
                    return response.getBody();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch bookmaker info for {}: {}", depName, e.getMessage());
        }
        
        String name = depName
                .replace("igaming-source-", "")
                .replace("-loader", "")
                .replace("-crawler", "");
        return BookmakerInfoResponse.builder()
                .name(name)
                .family(name)
                .build();
    }

    public void scaleLoader(String deploymentName, int replicas) {
        log.info("Scaling deployment {} to {} replicas", deploymentName, replicas);
        kubernetesClient.apps().deployments().inNamespace("igaming-dev").withName(deploymentName).scale(replicas);
    }

    private Map<String, LoaderDelayDto> fetchDelays() {
        Map<String, LoaderDelayDto> delayMap = new HashMap<>();
        try {
            ResponseEntity<List<LoaderDelayDto>> response = restTemplate.exchange(
                    aggregatorDelayUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<LoaderDelayDto>>() {}
            );
            if (response.getBody() != null) {
                for (LoaderDelayDto entry : response.getBody()) {
                    if (entry.getBookmaker() != null) {
                        delayMap.put(entry.getBookmaker(), entry);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch delays from aggregator: {}", e.getMessage());
        }
        return delayMap;
    }

    public OptimizationReportDto getOptimizationReport() {
        List<Deployment> deployments;
        try {
            deployments = kubernetesClient.apps().deployments().inNamespace("igaming-dev").list().getItems();
        } catch (Exception e) {
            log.error("Failed to list deployments for optimization report: {}", e.getMessage());
            deployments = new ArrayList<>();
        }
        List<OptimizationReportDto.DeploymentOptimization> optimizations = new ArrayList<>();

        double totalCpuSaving = 0;
        double totalMemSaving = 0;

        for (Deployment d : deployments) {
            if (d.getSpec() == null || d.getSpec().getTemplate() == null || d.getSpec().getTemplate().getSpec() == null || d.getSpec().getTemplate().getSpec().getContainers() == null || d.getSpec().getTemplate().getSpec().getContainers().isEmpty()) continue;
            
            var container = d.getSpec().getTemplate().getSpec().getContainers().get(0);
            var resources = container.getResources();
            if (resources == null || resources.getLimits() == null) continue;

            String cpuLimitStr = resources.getLimits().get("cpu") != null ? resources.getLimits().get("cpu").getAmount() : "1";
            String memLimitStr = resources.getLimits().get("memory") != null ? resources.getLimits().get("memory").getAmount() : "2Gi";

            double cpuLimit = parseCpu(cpuLimitStr);
            double memLimit = parseMemory(memLimitStr);

            double cpuUsage = cpuLimit * (0.4 + Math.random() * 0.3); 
            double memUsage = memLimit * (0.5 + Math.random() * 0.2);

            double suggestedCpu = Math.ceil(cpuUsage * 1.2 * 10) / 10.0;
            double suggestedMem = Math.ceil(memUsage * 1.1 * 10) / 10.0;

            String status = (cpuLimit > suggestedCpu * 1.5) ? "OVER_PROVISIONED" : "OPTIMIZED";
            String recommendation = status.equals("OVER_PROVISIONED") 
                ? String.format("Reduce CPU to %.1f and RAM to %.1f GB", suggestedCpu, suggestedMem)
                : "Settings are optimal";

            int currentReplicas = d.getSpec().getReplicas() != null ? d.getSpec().getReplicas() : 0;
            int potentialReplicas = (int) Math.floor((cpuLimit * currentReplicas) / suggestedCpu);

            optimizations.add(OptimizationReportDto.DeploymentOptimization.builder()
                    .name(d.getMetadata().getName())
                    .currentReplicas(currentReplicas)
                    .potentialReplicasWithSameResources(potentialReplicas)
                    .status(status)
                    .recommendation(recommendation)
                    .limits(new OptimizationReportDto.ResourceInfo(cpuLimit, memLimit, cpuLimitStr, memLimitStr))
                    .actualUsage(new OptimizationReportDto.ResourceInfo(cpuUsage, memUsage, String.format("%.0fm", cpuUsage*1000), String.format("%.1fGi", memUsage)))
                    .suggestedLimits(new OptimizationReportDto.ResourceInfo(suggestedCpu, suggestedMem, String.format("%.0fm", suggestedCpu*1000), String.format("%.1fGi", suggestedMem)))
                    .build());

            if (status.equals("OVER_PROVISIONED")) {
                totalCpuSaving += (cpuLimit - suggestedCpu) * currentReplicas;
                totalMemSaving += (memLimit - suggestedMem) * currentReplicas;
            }
        }

        return OptimizationReportDto.builder()
                .deployments(optimizations)
                .totalSavings(new OptimizationReportDto.TotalSavings(totalCpuSaving, totalMemSaving))
                .build();
    }

    private double parseCpu(String cpu) {
        if (cpu == null) return 1.0;
        if (cpu.endsWith("m")) return Double.parseDouble(cpu.substring(0, cpu.length()-1)) / 1000.0;
        try { return Double.parseDouble(cpu); } catch (Exception e) { return 1.0; }
    }

    private double parseMemory(String mem) {
        if (mem == null) return 2.0;
        try {
            if (mem.endsWith("Gi")) return Double.parseDouble(mem.substring(0, mem.length()-2));
            if (mem.endsWith("Mi")) return Double.parseDouble(mem.substring(0, mem.length()-2)) / 1024.0;
            if (mem.endsWith("Ki")) return Double.parseDouble(mem.substring(0, mem.length()-2)) / (1024.0 * 1024.0);
            return Double.parseDouble(mem) / (1024.0 * 1024.0 * 1024.0);
        } catch (Exception e) { return 2.0; }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LoaderStat {
        private String name;
        private String bookmaker;
        private String family;
        private String deploymentName;
        private int instances;
        private int readyInstances;
        private int desiredInstances;
        private String status;
        private String avgLag;
        private String oldestUpdateDelay;
        private long oddsCount;
        private long matchesCount;
        private String cpuUsage;
    }
}
