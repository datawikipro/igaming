package pro.datawiki.igaming.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.plane.workers")
public class PlaneAiProperties {

    /**
     * Total number of Plane AI dedicated worker pods in the pool.
     */
    private int count = 18;

    /**
     * K8s internal DNS URL pattern for Plane AI workers.
     */
    private String baseUrlPattern = "http://plane-ai-worker-%d.plane-ai-worker.plane.svc.cluster.local:8000";

    /**
     * Timeout for synchronous execution in seconds.
     */
    private int timeoutSeconds = 180;

    /**
     * Default model to dispatch to Plane AI workers.
     */
    private String model = "gemini-3.8-flash-high";

    /**
     * Maximum number of worker retry attempts on failure/busy.
     */
    private int maxAttempts = 10;
}
