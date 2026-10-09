package pro.datawiki.igaming.k8s.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.k8s.dto.BookmakerIncident;
import pro.datawiki.igaming.k8s.repository.BookmakerIncidentRepository;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class BookmakerWatchdogService {

    private final BookmakerIncidentRepository incidentRepository;
    private final PlaneTaskDispatcherClient planeDispatcher;
    private final KubernetesClient kubernetesClient;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public BookmakerWatchdogService(
            BookmakerIncidentRepository incidentRepository,
            PlaneTaskDispatcherClient planeDispatcher,
            KubernetesClient kubernetesClient
    ) {
        this.incidentRepository = incidentRepository;
        this.planeDispatcher = planeDispatcher;
        this.kubernetesClient = kubernetesClient;

        this.objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE)
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(60000); // 60 seconds for heavy aggregate queries
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getMessageConverters().removeIf(m -> m instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter);
        this.restTemplate.getMessageConverters().add(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(this.objectMapper));
    }

    @Value("${aggregator.fleet.url:http://igaming-aggregator.igaming-dev.svc.cluster.local/api/bookmakers/fleet}")
    private String aggregatorFleetUrl;

    @Value("${bookmaker-watchdog.enabled:true}")
    private boolean enabled;

    @Value("${bookmaker-watchdog.stale-delay-threshold-min:15.0}")
    private double staleDelayThresholdMin;

    @Value("${bookmaker-watchdog.high-lag-threshold-min:30.0}")
    private double highLagThresholdMin;

    @Value("${bookmaker-watchdog.min-matches-threshold:500}")
    private int minMatchesThreshold;

    @Value("${bookmaker-watchdog.super-arb-threshold-percent:10.0}")
    private double superArbThresholdPercent;

    @Value("${bookmaker-watchdog.smm-stale-hours-threshold:4.0}")
    private double smmStaleHoursThreshold;

    // Cache of latest fleet overview for instant UI serving
    private volatile FleetOverviewDto cachedFleetOverview;
    private volatile Instant lastWatchdogRun;

    @Data
    @Builder
    public static class WatchdogRunSummary {
        private Instant runAt;
        private int totalBookmakers;
        private int activeStreaming;
        private int staleAlerts;
        private int superArbAlerts;
        private int lowVolumeAlerts;
        private int openIncidentsCount;
        private boolean systemHealthy;
    }

    @Scheduled(fixedDelayString = "${bookmaker-watchdog.poll-interval-ms:60000}", initialDelay = 15000)
    public void runWatchdogCycle() {
        if (!enabled) return;

        try {
            log.info("🔍 Starting Bookmaker Watchdog inspection cycle...");
            lastWatchdogRun = Instant.now();

            FleetOverviewDto fleet = fetchFleetOverview();
            if (fleet != null) {
                this.cachedFleetOverview = fleet;
                inspectFleet(fleet);
            }

            inspectClusterResources();
            inspectSocialMediaActivity();

            log.info("✅ Bookmaker Watchdog cycle completed successfully.");
        } catch (Exception e) {
            log.error("❌ Error during watchdog cycle: {}", e.getMessage(), e);
        }
    }

    public WatchdogRunSummary triggerManualCheck() {
        runWatchdogCycle();
        return getSummary();
    }

    public FleetOverviewDto getLatestFleetOverview() {
        if (cachedFleetOverview == null) {
            cachedFleetOverview = fetchFleetOverview();
        }
        return cachedFleetOverview;
    }

    public WatchdogRunSummary getSummary() {
        List<BookmakerIncident> open = incidentRepository.findActiveIncidents();
        int stale = (int) open.stream().filter(i -> "BOOKMAKER_STALE".equals(i.getCategory())).count();
        int superArbs = (int) open.stream().filter(i -> "SUPER_ARB_10PLUS".equals(i.getCategory())).count();
        int lowVol = (int) open.stream().filter(i -> "LOW_DATA_VOLUME".equals(i.getCategory())).count();

        int totalBm = cachedFleetOverview != null ? cachedFleetOverview.getTotalBookmakers() : 0;
        int active = cachedFleetOverview != null ? cachedFleetOverview.getOnlineBookmakers() : 0;

        return WatchdogRunSummary.builder()
                .runAt(lastWatchdogRun)
                .totalBookmakers(totalBm)
                .activeStreaming(active)
                .staleAlerts(stale)
                .superArbAlerts(superArbs)
                .lowVolumeAlerts(lowVol)
                .openIncidentsCount(open.size())
                .systemHealthy(open.isEmpty())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. Inspect Fleet: Data flow, Staleness, Super-arbs, Low Volume
    // ─────────────────────────────────────────────────────────────────────────────

    private void inspectFleet(FleetOverviewDto fleet) {
        if (fleet.getBookmakers() == null) return;

        for (BookmakerFleetStatsDto bm : fleet.getBookmakers()) {
            String id = bm.getId();
            String name = bm.getName();
            long matches = bm.getActiveMatchesCount() != null ? bm.getActiveMatchesCount() : 0L;
            long odds = bm.getActiveOddsCount() != null ? bm.getActiveOddsCount() : 0L;
            Double delay = bm.getDelayMinutes();
            int superArbs = bm.getSuperArbsCount() != null ? bm.getSuperArbsCount() : 0;
            Double maxProfit = bm.getSuperArbsMaxProfit();

            // Metric 1: Did bookmaker stop sending data? (Stale data)
            boolean isStale = (delay != null && delay > staleDelayThresholdMin && matches > 0);
            handleMetricIncident(
                    "BOOKMAKER_STALE",
                    id,
                    isStale,
                    "CRITICAL",
                    String.format("[STALE] Букмекер %s перестал присылать данные (лаг %.1f мин)", name, delay != null ? delay : 0.0),
                    buildStaleDescription(bm),
                    delay != null ? String.format("%.1f min", delay) : "N/A",
                    String.format("%.1f min", staleDelayThresholdMin)
            );

            // Metric 2: Did super-arbs 10%+ appear?
            boolean hasSuperArb = (superArbs > 0 && maxProfit != null && maxProfit >= superArbThresholdPercent);
            handleMetricIncident(
                    "SUPER_ARB_10PLUS",
                    id,
                    hasSuperArb,
                    "CRITICAL",
                    String.format("[SUPER-ARB] Аномальная вилка %.1f%% с участием %s", maxProfit != null ? maxProfit : 0.0, name),
                    buildSuperArbDescription(bm),
                    maxProfit != null ? String.format("%.1f%%", maxProfit) : "0%",
                    String.format("%.1f%%", superArbThresholdPercent)
            );

            // Metric 3: Low data volume (Golden Rule 8: < 500 matches for active sources)
            boolean isLowVolume = (matches > 0 && matches < minMatchesThreshold);
            handleMetricIncident(
                    "LOW_DATA_VOLUME",
                    id,
                    isLowVolume,
                    "WARNING",
                    String.format("[LOW DATA] Линия %s содержит всего %d матчей (порог: %d)", name, matches, minMatchesThreshold),
                    buildLowVolumeDescription(bm),
                    String.valueOf(matches),
                    String.valueOf(minMatchesThreshold)
            );

            // Metric 4: High lag
            boolean isHighLag = (delay != null && delay > highLagThresholdMin);
            handleMetricIncident(
                    "HIGH_INGESTION_LAG",
                    id,
                    isHighLag,
                    "WARNING",
                    String.format("[HIGH LAG] Критическое отставание линии %s (%.1f мин)", name, delay != null ? delay : 0.0),
                    String.format("Отставание линии букмекера %s составляет %.1f минут (порог: %.1f мин).", name, delay != null ? delay : 0.0, highLagThresholdMin),
                    delay != null ? String.format("%.1f min", delay) : "N/A",
                    String.format("%.1f min", highLagThresholdMin)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. Inspect Cluster Resources (CPU & RAM)
    // ─────────────────────────────────────────────────────────────────────────────

    private void inspectClusterResources() {
        try {
            List<Node> nodes = kubernetesClient.nodes().list().getItems();
            for (Node node : nodes) {
                String nodeName = node.getMetadata() != null ? node.getMetadata().getName() : "unknown-node";
                Map<String, Quantity> allocatable = node.getStatus() != null ? node.getStatus().getAllocatable() : Collections.emptyMap();

                // Check node condition Ready
                boolean isReady = node.getStatus() != null && node.getStatus().getConditions() != null &&
                        node.getStatus().getConditions().stream().anyMatch(c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus()));

                boolean isOverloaded = !isReady; // Flag if node has DiskPressure, MemoryPressure, or NotReady
                if (node.getStatus() != null && node.getStatus().getConditions() != null) {
                    for (var cond : node.getStatus().getConditions()) {
                        if (("MemoryPressure".equals(cond.getType()) || "DiskPressure".equals(cond.getType())) && "True".equals(cond.getStatus())) {
                            isOverloaded = true;
                            break;
                        }
                    }
                }

                handleMetricIncident(
                        "CPU_RAM_OVERLOAD",
                        nodeName,
                        isOverloaded,
                        "CRITICAL",
                        String.format("[NODE PRESSURE] Перегрузка ноды %s (Memory/Disk pressure)", nodeName),
                        String.format("Нода %s испытывает нехватку ресурсов памяти или диска в кластере Kubernetes.", nodeName),
                        isOverloaded ? "PRESSURE" : "NORMAL",
                        "NORMAL"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to inspect cluster nodes via K8s client: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. Inspect Social Media Activity
    // ─────────────────────────────────────────────────────────────────────────────

    private void inspectSocialMediaActivity() {
        try {
            // Check daytime window in Moscow timezone (09:00 to 23:00 MSK)
            ZonedDateTime mskTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
            int hour = mskTime.getHour();
            boolean isDaytime = (hour >= 9 && hour <= 23);

            if (!isDaytime) {
                return; // Suppress social media staleness alerts overnight
            }

            // Probe smm-bot-telegram health & events
            String smmUrl = "http://smm-bot-telegram.igaming-dev.svc.cluster.local:8080/healthz";
            boolean isSmmRunning = true;
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(smmUrl, String.class);
                isSmmRunning = resp.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                isSmmRunning = false;
            }

            handleMetricIncident(
                    "SOCIAL_MEDIA_STALE",
                    "telegram_channels",
                    !isSmmRunning,
                    "WARNING",
                    "[SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен",
                    "Контейнер smm-bot-telegram не отвечает на healthcheck в рабочее время (09:00 - 23:00 MSK). Посты в каналы могут быть приостановлены.",
                    isSmmRunning ? "ONLINE" : "OFFLINE",
                    "ONLINE"
            );
        } catch (Exception e) {
            log.warn("Failed to check social media status: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. Incident Lifecycle, Anti-Spam (Deduplication) & Plane Dispatch
    // ─────────────────────────────────────────────────────────────────────────────

    private void handleMetricIncident(
            String category,
            String target,
            boolean isViolating,
            String severity,
            String title,
            String descriptionMarkdown,
            String currentValue,
            String thresholdValue
    ) {
        Optional<BookmakerIncident> existingOpt = incidentRepository.findOpenIncident(category, target);

        if (isViolating) {
            if (existingOpt.isPresent()) {
                // Already open — update metrics and lastSeen timestamp without duplicate Plane ticket
                BookmakerIncident existing = existingOpt.get();
                existing.setCurrentValue(currentValue);
                existing.setLastSeenAt(Instant.now());
                incidentRepository.save(existing);
            } else {
                // New anomaly! Create incident and dispatch AI Agent to Plane
                log.warn("🚨 New anomaly detected: category={}, target={}, title={}", category, target, title);

                BookmakerIncident newIncident = BookmakerIncident.builder()
                        .category(category)
                        .target(target)
                        .severity(severity)
                        .title(title)
                        .description(descriptionMarkdown)
                        .currentValue(currentValue)
                        .thresholdValue(thresholdValue)
                        .status("OPEN")
                        .detectedAt(Instant.now())
                        .lastSeenAt(Instant.now())
                        .build();

                // Dispatch task in Plane
                PlaneTaskDispatcherClient.PlaneDispatchResult dispatch =
                        planeDispatcher.dispatchAiTask(title, descriptionMarkdown);

                if (dispatch.isSuccess()) {
                    newIncident.setPlaneIssueId(dispatch.getIssueId());
                    newIncident.setPlaneIssueUrl(dispatch.getPlaneUrl());
                }

                incidentRepository.save(newIncident);
            }
        } else {
            // Anomaly cleared! Auto-resolve open incident
            existingOpt.ifPresent(existing -> {
                log.info("✅ Anomaly cleared for {}/{}, resolving incident #{}", category, target, existing.getId());
                incidentRepository.resolveIncident(existing.getId());
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helpers & Markdown Formatters for Plane Tasks
    // ─────────────────────────────────────────────────────────────────────────────

    private FleetOverviewDto fetchFleetOverview() {
        try {
            ResponseEntity<FleetOverviewDto> response = restTemplate.exchange(
                    aggregatorFleetUrl,
                    HttpMethod.GET,
                    null,
                    FleetOverviewDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.warn("Failed to fetch fleet overview from aggregator ({}): {}", aggregatorFleetUrl, e.getMessage());
            return null;
        }
    }

    private String buildStaleDescription(BookmakerFleetStatsDto bm) {
        return String.format(
                "### ⚠️ Поток данных букмекера остановился\n\n" +
                "- **Букмекер**: %s (`%s`)\n" +
                "- **Движок**: %s\n" +
                "- **Маршрут прокси**: `%s`\n" +
                "- **Задержка поступления**: %.1f мин (порог: %.1f мин)\n" +
                "- **Текущие матчи в БД**: %d\n" +
                "- **Текущие котировки в БД**: %d\n\n" +
                "#### 🛠️ Инструкция для AI-разработчика:\n" +
                "1. Проверить логи подов `igaming-source-%s-crawler` и `igaming-source-%s-loader` в namespace `igaming-source` (`kubectl logs -n igaming-source deploy/igaming-source-%s-crawler --tail=100`).\n" +
                "2. Проверить проксирование на роутере `ru-proxy` (`100.83.113.50:3128`) и Shadowsocks туннель.\n" +
                "3. Убедиться, что букмекер не включил новую защиту Cloudflare/WAF или не изменил структуру API.\n" +
                "4. Проверить выполнение **Definition of Done** (порог >= 500 матчей) после восстановления.",
                bm.getName(), bm.getId(), bm.getEngine(), bm.getProxyRoute(),
                bm.getDelayMinutes() != null ? bm.getDelayMinutes() : 0.0, staleDelayThresholdMin,
                bm.getActiveMatchesCount() != null ? bm.getActiveMatchesCount() : 0L,
                bm.getActiveOddsCount() != null ? bm.getActiveOddsCount() : 0L,
                bm.getId(), bm.getId(), bm.getId()
        );
    }

    private String buildSuperArbDescription(BookmakerFleetStatsDto bm) {
        return String.format(
                "### 🚨 Обнаружена аномальная супервилка (10%%+)\n\n" +
                "- **Букмекер**: %s (`%s`)\n" +
                "- **Количество активных супервилок**: %d\n" +
                "- **Максимальный профит**: %.1f%%\n\n" +
                "#### 🛠️ Инструкция для AI-разработчика:\n" +
                "1. Вилки с профитом свыше 10-15%% почти всегда являются результатом **ошибки парсинга** (инвертированы исходы ТБ/ТМ, перепутан знак форы, либо некорректный маппинг команд).\n" +
                "2. Проверить котировки в таблице `surebet_alert` и `surebet_outcome_legs` для букмекера `%s`.\n" +
                "3. Проверить нормализатор в `aggregator-normalizer` и маппер исходов в `igaming-source-%s`.\n" +
                "4. Устранить ошибку соответствия исходов и верифицировать отсутствие ложных вилок.",
                bm.getName(), bm.getId(),
                bm.getSuperArbsCount() != null ? bm.getSuperArbsCount() : 0,
                bm.getSuperArbsMaxProfit() != null ? bm.getSuperArbsMaxProfit() : 0.0,
                bm.getId(), bm.getId()
        );
    }

    private String buildLowVolumeDescription(BookmakerFleetStatsDto bm) {
        return String.format(
                "### 📉 Нарушение Золотого правила 8: Недостаточное наполнение линии\n\n" +
                "- **Букмекер**: %s (`%s`)\n" +
                "- **Количество активных матчей**: %d (Требуемый порог: >= 500)\n" +
                "- **Количество котировок**: %d\n\n" +
                "#### 🛠️ Инструкция для AI-разработчика:\n" +
                "1. По Золотому правилу 8 задача по букмекеру считается завершенной ТОЛЬКО при наполнении линии от 500 активных матчей.\n" +
                "2. Проверить пагинацию краулера `igaming-source-%s-crawler`, охват видов спорта (футбол, хоккей, баскетбол, теннис) и фильтры по времени.\n" +
                "3. Добиться наполнения линии >= 500 матчей (`SELECT count(*) FROM match_cache >= 500`).",
                bm.getName(), bm.getId(),
                bm.getActiveMatchesCount() != null ? bm.getActiveMatchesCount() : 0L,
                bm.getActiveOddsCount() != null ? bm.getActiveOddsCount() : 0L,
                bm.getId()
        );
    }
}
