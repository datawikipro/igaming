package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис мониторинга health-check эндпоинтов критических сервисов.
 *
 * <p>Исправленные URL (относительно устаревшей версии):
 * <ul>
 *   <li>Airflow API Server — Airflow 3 сменил путь с /health на /api/v2/monitor/health</li>
 *   <li>LLM Admin — Spring Actuator доступен по /actuator/health, а не в корне /</li>
 *   <li>Data Catalog Backend — Spring Actuator по /actuator/health</li>
 * </ul>
 *
 * <p>Все URL конфигурируются через application.properties,
 * что позволяет переопределять их без перекомпиляции.
 */
@Slf4j
@Service
public class EndpointHealthWatcherService {

    private final KubernetesClient kubernetesClient;
    private final RestTemplate restTemplate;

    // -----------------------------------------------------------------------
    // Конфигурация URL (переопределяется в application.properties)
    // -----------------------------------------------------------------------

    /**
     * Health-check URL для Apache Airflow 3 API Server.
     * Airflow 3 изменил путь с /health на /api/v2/monitor/health.
     */
    @Value("${health.watcher.airflow-api.url:http://airflow-api-server.accounts/api/v2/monitor/health}")
    private String airflowHealthUrl;

    /**
     * Health-check URL для LLM Admin (Spring Boot Actuator).
     * Spring Actuator недоступен в корне /; правильный путь — /actuator/health.
     */
    @Value("${health.watcher.llm-admin.url:http://llm-admin.accounts/actuator/health}")
    private String llmAdminHealthUrl;

    /**
     * Health-check URL для Data Catalog Backend (Spring Boot Actuator).
     */
    @Value("${health.watcher.data-catalog.url:http://data-catalog-backend.accounts/actuator/health}")
    private String dataCatalogHealthUrl;

    /**
     * Количество последовательных неудач до принудительного перезапуска деплоймента.
     * По умолчанию 30 — чтобы не перезапускать сервисы при кратковременных сбоях сети.
     */
    @Value("${health.watcher.failures-before-restart:30}")
    private int failuresBeforeRestart;

    /**
     * Кулдаун между автоматическими перезапусками одного деплоймента (секунды).
     */
    @Value("${health.watcher.restart-cooldown-seconds:600}")
    private int restartCooldownSeconds;

    /**
     * Неймспейс, в котором находятся мониторируемые сервисы.
     */
    @Value("${health.watcher.namespace:accounts}")
    private String watcherNamespace;

    /**
     * Флаг включения/выключения автоматического перезапуска (по умолчанию выключен —
     * наблюдение без деструктивных действий).
     */
    @Value("${health.watcher.auto-restart-enabled:false}")
    private boolean autoRestartEnabled;

    // -----------------------------------------------------------------------
    // Внутреннее состояние
    // -----------------------------------------------------------------------

    /** Счётчики последовательных неудач по имени эндпоинта. */
    private final ConcurrentHashMap<String, AtomicInteger> failureCounters = new ConcurrentHashMap<>();

    /** Метка времени последнего автоматического перезапуска (epochMs) по имени деплоймента. */
    private final ConcurrentHashMap<String, Long> lastRestartTimestamps = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Конфигурация мониторируемых эндпоинтов
    // -----------------------------------------------------------------------

    private record WatchedEndpoint(String name, String deploymentName, String healthUrl) {}

    private List<WatchedEndpoint> watchedEndpoints;

    public EndpointHealthWatcherService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    public void init() {
        // Инициализация списка выполняется после внедрения @Value-полей
        watchedEndpoints = List.of(
                new WatchedEndpoint("airflow-api-server", "airflow-api-server", airflowHealthUrl),
                new WatchedEndpoint("llm-admin",          "llm-admin",          llmAdminHealthUrl),
                new WatchedEndpoint("data-catalog-backend", "data-catalog-backend", dataCatalogHealthUrl)
        );
        log.info("EndpointHealthWatcherService initialized. Monitored endpoints:");
        watchedEndpoints.forEach(ep ->
                log.info("  - {} -> {}", ep.name(), ep.healthUrl()));
        log.info("Auto-restart: {}, failures threshold: {}, cooldown: {}s",
                autoRestartEnabled, failuresBeforeRestart, restartCooldownSeconds);
    }

    // -----------------------------------------------------------------------
    // Расписание проверок (каждые 60 секунд)
    // -----------------------------------------------------------------------

    @Scheduled(fixedDelayString = "${health.watcher.check-interval-ms:60000}",
               initialDelay = 30000)
    public void checkAllEndpoints() {
        if (watchedEndpoints == null) {
            return;
        }
        for (WatchedEndpoint ep : watchedEndpoints) {
            checkEndpoint(ep);
        }
    }

    // -----------------------------------------------------------------------
    // Логика проверки одного эндпоинта
    // -----------------------------------------------------------------------

    private void checkEndpoint(WatchedEndpoint ep) {
        boolean healthy = performHealthCheck(ep.name(), ep.healthUrl());

        AtomicInteger counter = failureCounters.computeIfAbsent(ep.name(), k -> new AtomicInteger(0));

        if (healthy) {
            int prev = counter.getAndSet(0);
            if (prev > 0) {
                log.info("✅ [HealthWatcher] {} recovered after {} consecutive failures", ep.name(), prev);
            }
        } else {
            int failures = counter.incrementAndGet();
            log.warn("⚠️  [HealthWatcher] {} health check FAILED ({}/{}): {}",
                    ep.name(), failures, failuresBeforeRestart, ep.healthUrl());

            if (autoRestartEnabled && failures >= failuresBeforeRestart) {
                triggerRestart(ep, failures);
            }
        }
    }

    /**
     * Выполняет HTTP GET запрос к health-check URL.
     *
     * @param name    имя сервиса (для логов)
     * @param url     полный URL health-check эндпоинта
     * @return true — сервис здоров (HTTP 2xx), false — недоступен или вернул ошибку
     */
    boolean performHealthCheck(String name, String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            boolean ok = response.getStatusCode().is2xxSuccessful();
            if (!ok) {
                log.debug("[HealthWatcher] {} returned non-2xx: {}", name, response.getStatusCode());
            }
            return ok;
        } catch (Exception ex) {
            log.debug("[HealthWatcher] {} check failed: {}", name, ex.getMessage());
            return false;
        }
    }

    /**
     * Инициирует rolling-restart деплоймента через аннотацию kubectl rollout restart.
     * Выполняется только если прошёл кулдаун с момента прошлого перезапуска.
     */
    private void triggerRestart(WatchedEndpoint ep, int failures) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) restartCooldownSeconds * 1000;
        Long lastRestart = lastRestartTimestamps.get(ep.deploymentName());

        if (lastRestart != null && (now - lastRestart) < cooldownMs) {
            long remainingSec = (cooldownMs - (now - lastRestart)) / 1000;
            log.warn("[HealthWatcher] {} restart blocked by cooldown ({}s remaining)", ep.name(), remainingSec);
            return;
        }

        try {
            log.error("🔄 [HealthWatcher] {} RESTART triggered after {} consecutive failures. Deployment: {}",
                    ep.name(), failures, ep.deploymentName());

            // Rolling restart через обновление аннотации деплоймента
            kubernetesClient.apps().deployments()
                    .inNamespace(watcherNamespace)
                    .withName(ep.deploymentName())
                    .edit(deployment -> {
                        Map<String, String> annotations = deployment.getSpec()
                                .getTemplate().getMetadata().getAnnotations();
                        if (annotations == null) {
                            annotations = new java.util.HashMap<>();
                            deployment.getSpec().getTemplate().getMetadata().setAnnotations(annotations);
                        }
                        annotations.put("kubectl.kubernetes.io/restartedAt",
                                java.time.Instant.now().toString());
                        return deployment;
                    });

            lastRestartTimestamps.put(ep.deploymentName(), now);
            failureCounters.get(ep.name()).set(0);

            log.info("✅ [HealthWatcher] Rolling restart initiated for {}", ep.deploymentName());
        } catch (Exception ex) {
            log.error("❌ [HealthWatcher] Failed to restart {}: {}", ep.deploymentName(), ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Публичное API для контроллера
    // -----------------------------------------------------------------------

    /**
     * Возвращает текущий snapshot состояния всех мониторируемых эндпоинтов.
     */
    public List<EndpointStatus> getEndpointStatuses() {
        if (watchedEndpoints == null) {
            return List.of();
        }
        return watchedEndpoints.stream()
                .map(ep -> {
                    int failures = failureCounters.containsKey(ep.name())
                            ? failureCounters.get(ep.name()).get()
                            : 0;
                    Long lastRestart = lastRestartTimestamps.get(ep.deploymentName());
                    return new EndpointStatus(
                            ep.name(),
                            ep.healthUrl(),
                            failures == 0 ? "HEALTHY" : "DEGRADED",
                            failures,
                            failuresBeforeRestart,
                            lastRestart
                    );
                })
                .toList();
    }

    // -----------------------------------------------------------------------
    // DTO
    // -----------------------------------------------------------------------

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class EndpointStatus {
        private String name;
        private String healthUrl;
        private String status;
        private int consecutiveFailures;
        private int failuresBeforeRestart;
        private Long lastRestartTimestamp;
    }
}
