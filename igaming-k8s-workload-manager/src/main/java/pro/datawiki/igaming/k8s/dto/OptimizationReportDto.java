package pro.datawiki.igaming.k8s.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationReportDto {
    private List<DeploymentOptimization> deployments;
    private TotalSavings totalSavings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeploymentOptimization {
        private String name;
        private ResourceInfo limits;
        private ResourceInfo actualUsage;
        private ResourceInfo suggestedLimits;
        private String status; // e.g. "OPTIMIZED", "OVER_PROVISIONED", "UNDER_PROVISIONED"
        private String recommendation;
        private Integer currentReplicas;
        private Integer potentialReplicasWithSameResources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceInfo {
        private Double cpuCores;
        private Double memoryGb;
        private String cpuDisplay;
        private String memoryDisplay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalSavings {
        private Double cpuCores;
        private Double memoryGb;
    }
}
