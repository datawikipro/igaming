package pro.datawiki.igaming.agent.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.agent.service.PlaneAiWorkerPoolDispatcher;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlaneWorkerPoolHealthIndicator implements HealthIndicator {

    private final PlaneAiWorkerPoolDispatcher workerPoolDispatcher;

    @Override
    public Health health() {
        try {
            Map<String, Object> summary = workerPoolDispatcher.getPoolStatusSummary();
            long online = (Long) summary.getOrDefault("onlineWorkers", 0L);
            long tokenReady = (Long) summary.getOrDefault("tokenReadyWorkers", 0L);

            if (online > 0) {
                return Health.up()
                        .withDetail("onlineWorkers", online)
                        .withDetail("tokenReadyWorkers", tokenReady)
                        .withDetail("totalConfigured", summary.get("totalConfigured"))
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "No Plane AI worker pods reachable in namespace plane")
                        .build();
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
