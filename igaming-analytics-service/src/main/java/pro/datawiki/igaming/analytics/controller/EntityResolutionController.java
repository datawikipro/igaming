package pro.datawiki.igaming.analytics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Entity Resolution Hub — проксирует запросы к aggregator management API
 * для управления очередью нормализации (normalization_request).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/v1/mdm/normalization-queue — список PENDING-записей</li>
 *   <li>GET /api/v1/mdm/normalization-queue/stats — статистика по статусам</li>
 *   <li>POST /api/v1/mdm/normalization-queue/{id}/resolve — ручное разрешение сущности</li>
 *   <li>POST /api/v1/mdm/normalization-queue/{id}/retry — повторная отправка в LLM</li>
 *   <li>DELETE /api/v1/mdm/normalization-queue/{id} — отклонить/удалить запись</li>
 *   <li>GET /api/v1/mdm/aliases — список зарегистрированных алиасов команд</li>
 *   <li>DELETE /api/v1/mdm/aliases/{id} — удалить алиас</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mdm")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EntityResolutionController {

    private final RestTemplate restTemplate;

    @Value("${aggregator.management.url:http://igaming-aggregator/api/v1/management}")
    private String aggregatorManagementUrl;

    // ──────────────────────────────────────────────────────────────────
    // Normalization Queue
    // ──────────────────────────────────────────────────────────────────

    /** Список pending-записей на нормализацию (страница + фильтры). */
    @GetMapping("/normalization-queue")
    public ResponseEntity<Object> getNormalizationQueue(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            String url = aggregatorManagementUrl
                    + "/normalization-queue?status=" + status
                    + "&page=" + page + "&size=" + size;
            Object result = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator normalization-queue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "aggregator unavailable", "detail", e.getMessage()));
        }
    }

    /** Сводная статистика: сколько записей в каждом статусе. */
    @GetMapping("/normalization-queue/stats")
    public ResponseEntity<Object> getNormalizationStats() {
        try {
            String url = aggregatorManagementUrl + "/normalization-queue/stats";
            Object result = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator stats: {}", e.getMessage());
            // Возвращаем заглушку, чтобы UI не ломался
            return ResponseEntity.ok(Map.of(
                    "PENDING", 0, "RESOLVED", 0, "FAILED", 0, "SKIPPED", 0,
                    "_note", "aggregator unavailable"
            ));
        }
    }

    /** Ручное разрешение: назначить canonical_name + team_id / league_id. */
    @PostMapping("/normalization-queue/{id}/resolve")
    public ResponseEntity<Object> resolveNormalizationRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> resolution) {
        try {
            String url = aggregatorManagementUrl + "/normalization-queue/" + id + "/resolve";
            Object result = restTemplate.postForObject(url, resolution, Object.class);
            return ResponseEntity.ok(result != null ? result : Map.of("resolved", true));
        } catch (Exception e) {
            log.error("Failed to resolve normalization id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Повторная отправка PENDING-записи в LLM-нормализатор. */
    @PostMapping("/normalization-queue/{id}/retry")
    public ResponseEntity<Object> retryNormalization(@PathVariable Long id) {
        try {
            String url = aggregatorManagementUrl + "/normalization-queue/" + id + "/retry";
            restTemplate.postForObject(url, null, Void.class);
            return ResponseEntity.ok(Map.of("retried", true, "id", id));
        } catch (Exception e) {
            log.error("Failed to retry normalization id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Отклонить / удалить запись из очереди нормализации. */
    @DeleteMapping("/normalization-queue/{id}")
    public ResponseEntity<Object> deleteNormalizationRequest(@PathVariable Long id) {
        try {
            restTemplate.delete(aggregatorManagementUrl + "/normalization-queue/" + id);
            return ResponseEntity.ok(Map.of("deleted", true, "id", id));
        } catch (Exception e) {
            log.error("Failed to delete normalization id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Alias Management
    // ──────────────────────────────────────────────────────────────────

    /** Список зарегистрированных team_alias / league_alias. */
    @GetMapping("/aliases")
    public ResponseEntity<Object> getAliases(
            @RequestParam(defaultValue = "team") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            String url = aggregatorManagementUrl
                    + "/aliases?type=" + type + "&page=" + page + "&size=" + size;
            Object result = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator aliases: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("content", List.of(), "_note", "aggregator unavailable"));
        }
    }

    /** Удалить алиас по ID (принудительная повторная нормализация при следующем встрече). */
    @DeleteMapping("/aliases/{id}")
    public ResponseEntity<Object> deleteAlias(
            @PathVariable Long id,
            @RequestParam(defaultValue = "team") String type) {
        try {
            restTemplate.delete(aggregatorManagementUrl + "/aliases/" + type + "/" + id);
            return ResponseEntity.ok(Map.of("deleted", true, "id", id, "type", type));
        } catch (Exception e) {
            log.error("Failed to delete alias id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
