package pro.datawiki.igaming.k8s.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "health-watcher")
public class HealthWatcherProperties {
    private boolean enabled = true;
    private long pollIntervalMs = 30000;
    private int defaultConnectTimeoutMs = 5000;
    private int defaultReadTimeoutMs = 10000;
    private int defaultFailureThreshold = 3;
    private int defaultCooldownMinutes = 15;
    private List<TargetService> targets = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetService {
        private String name;
        private String url;
        private String namespace;
        private String deploymentName;
        @Builder.Default
        private int timeoutMs = 10000;
        @Builder.Default
        private int failureThreshold = 3;
        @Builder.Default
        private int cooldownMinutes = 15;
        @Builder.Default
        private boolean enabled = true;
        @Builder.Default
        private boolean autoRestart = true;
    }
}
