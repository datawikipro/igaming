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
 * UnknownBet Radar — радар неизвестных ставок/котировок.
 *
 * <p>Проксирует запросы к aggregator для получения списка матчей и исходов,
 * по которым не смогло произойти сопоставление (entity resolution failed),
 * и предоставляет инструменты ручного маппинга.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/v1/radar/unknown-bets        — список неразрешённых ставок</li>
 *   <li>GET  /api/v1/radar/unknown-bets/stats  — агрегированная статистика по БК</li>
 *   <li>POST /api/v1/radar/unknown-bets/{id}/map — привязать ставку к известному матчу</li>
 *   <li>POST /api/v1/radar/unknown-bets/{id}/skip — пометить как неактуальную</li>
 *   <li>GET  /api/v1/radar/bookmakers           — список активных букмекеров с кол-вом unknown</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/radar")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UnknownBetRadarController {

    private final RestTemplate restTemplate;

    @Value("${aggregator.management.url:http://igaming-aggregator/api/v1/management}")
    private String aggregatorManagementUrl;

    @Value("${aggregator.stats.url:http://igaming-aggregator/api/diagnostics/stats}")
    private String aggregatorStatsUrl;

    // ──────────────────────────────────────────────────────────────────
    // Unknown Bets Feed
    // ──────────────────────────────────────────────────────────────────

    /**
     * Список ставок с неразрешёнными сущностями (normalization_status = PENDING / FAILED).
     * Возвращает страничный список с деталями: bookmaker, raw_name, sport, context.
     */
    @GetMapping("/unknown-bets")
    public ResponseEntity<Object> getUnknownBets(
            @RequestParam(defaultValue = "") String bookmaker,
            @RequestParam(defaultValue = "PENDING,FAILED") String statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            StringBuilder url = new StringBuilder(aggregatorManagementUrl)
                    .append("/unknown-bets?page=").append(page)
                    .append("&size=").append(size);
            if (!statuses.isBlank()) url.append("&statuses=").append(statuses);
            if (!bookmaker.isBlank()) url.append("&bookmaker=").append(bookmaker);

            Object result = restTemplate.getForObject(url.toString(), Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator unknown-bets: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "content", List.of(),
                    "totalElements", 0,
                    "_note", "aggregator unavailable"
            ));
        }
    }

    /**
     * Агрегированная статистика по неизвестным ставкам:
     * сколько PENDING/FAILED у каждого букмекера.
     */
    @GetMapping("/unknown-bets/stats")
    public ResponseEntity<Object> getUnknownBetsStats() {
        try {
            Object result = restTemplate.getForObject(
                    aggregatorManagementUrl + "/unknown-bets/stats", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator unknown-bets stats: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "totalPending", 0,
                    "totalFailed", 0,
                    "byBookmaker", List.of(),
                    "_note", "aggregator unavailable"
            ));
        }
    }

    /**
     * Привязать неизвестную ставку к известному canonical match_id.
     * Body: { "matchId": 12345, "teamAlias": "Манчестер Юнайтед", "canonicalName": "Manchester United FC" }
     */
    @PostMapping("/unknown-bets/{id}/map")
    public ResponseEntity<Object> mapUnknownBet(
            @PathVariable Long id,
            @RequestBody Map<String, Object> mapping) {
        try {
            String url = aggregatorManagementUrl + "/unknown-bets/" + id + "/map";
            Object result = restTemplate.postForObject(url, mapping, Object.class);
            return ResponseEntity.ok(result != null ? result : Map.of("mapped", true, "id", id));
        } catch (Exception e) {
            log.error("Failed to map unknown bet id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Пометить ставку как неактуальную (SKIPPED) — не участвует в повторных попытках.
     */
    @PostMapping("/unknown-bets/{id}/skip")
    public ResponseEntity<Object> skipUnknownBet(@PathVariable Long id) {
        try {
            String url = aggregatorManagementUrl + "/unknown-bets/" + id + "/skip";
            restTemplate.postForObject(url, null, Void.class);
            return ResponseEntity.ok(Map.of("skipped", true, "id", id));
        } catch (Exception e) {
            log.error("Failed to skip unknown bet id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Bookmaker Summary
    // ──────────────────────────────────────────────────────────────────

    /**
     * Список всех активных букмекеров с количеством неизвестных ставок.
     * Используется для фильтрации в Radar UI.
     */
    @GetMapping("/bookmakers")
    public ResponseEntity<Object> getBookmakers() {
        try {
            Object result = restTemplate.getForObject(
                    aggregatorManagementUrl + "/bookmakers/unknown-summary", Object.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator bookmakers: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Агрегированная диагностика всей системы Entity Resolution.
     * Объединяет данные stats + unknown-bets/stats в один дашборд-объект.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Object> getDashboard() {
        Object systemStats = null;
        Object unknownStats = null;
        Object queueStats = null;

        try {
            systemStats = restTemplate.getForObject(aggregatorStatsUrl, Object.class);
        } catch (Exception e) {
            log.debug("system stats unavailable: {}", e.getMessage());
        }

        try {
            unknownStats = restTemplate.getForObject(
                    aggregatorManagementUrl + "/unknown-bets/stats", Object.class);
        } catch (Exception e) {
            log.debug("unknown bets stats unavailable: {}", e.getMessage());
        }

        try {
            queueStats = restTemplate.getForObject(
                    aggregatorManagementUrl + "/normalization-queue/stats", Object.class);
        } catch (Exception e) {
            log.debug("queue stats unavailable: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "systemStats",   systemStats   != null ? systemStats   : Map.of(),
                "unknownStats",  unknownStats  != null ? unknownStats  : Map.of(),
                "queueStats",    queueStats    != null ? queueStats    : Map.of()
        ));
    }
}
