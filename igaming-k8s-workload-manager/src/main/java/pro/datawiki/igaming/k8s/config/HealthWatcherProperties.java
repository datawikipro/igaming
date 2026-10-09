package pro.datawiki.igaming.k8s.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "health-watcher")
public class HealthWatcherProperties {

    /** Master toggle for synthetic endpoint health monitoring */
    private boolean enabled = true;

    /** Fixed poll interval in milliseconds (default: 30000 = 30s) */
    private long pollIntervalMs = 30000;

    /** Global default connect timeout in ms */
    private int defaultConnectTimeoutMs = 5000;

    /** Global default read timeout in ms */
    private int defaultReadTimeoutMs = 10000;

    /** Default failure threshold before rolling restart */
    private int defaultFailureThreshold = 3;

    /** Default cooldown in minutes before repeating restart for same deployment */
    private int defaultCooldownMinutes = 15;

    /** List of target services to probe */
    private List<TargetService> targets = new ArrayList<>();

    @Data
    public static class TargetService {
        private String name;
        private String url;
        private String namespace;
        private String deploymentName;
        private int timeoutMs = 10000;
        private int failureThreshold = 3;
        private int cooldownMinutes = 15;
        private boolean enabled = true;
        private boolean autoRestart = true;
    }
}