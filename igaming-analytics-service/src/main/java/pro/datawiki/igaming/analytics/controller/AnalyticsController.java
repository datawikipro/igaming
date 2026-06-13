package pro.datawiki.igaming.analytics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.DiagnosticsStatsDto;
import pro.datawiki.igaming.dto.MatchAnalyticsDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RestTemplate restTemplate;

    @Value("${aggregator.stats.url:http://igaming-aggregator/api/diagnostics/stats}")
    private String aggregatorStatsUrl;

    @Value("${aggregator.analytics.url:http://igaming-aggregator/api/analytics/matches}")
    private String aggregatorAnalyticsUrl;

    @Value("${aggregator.management.url:http://igaming-aggregator/api/v1/management}")
    private String aggregatorManagementUrl;

    @GetMapping("/aggregator/stats")
    public DiagnosticsStatsDto getAggregatorStats() {
        try {
            return restTemplate.getForObject(aggregatorStatsUrl, DiagnosticsStatsDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch stats from aggregator", e);
            return null;
        }
    }

    @GetMapping("/matches")
    public MatchAnalyticsDto getMatchAnalytics(
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end) {
        try {
            String url = aggregatorAnalyticsUrl;
            List<String> params = new ArrayList<>();
            if (sport != null) params.add("sport=" + sport);
            if (start != null) params.add("start=" + start);
            if (end != null) params.add("end=" + end);
            if (!params.isEmpty()) {
                url += "?" + String.join("&", params);
            }
            return restTemplate.getForObject(url, MatchAnalyticsDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch analytics from aggregator", e);
            return null;
        }
    }

    @GetMapping("/management/{type}")
    public Object getManagementData(@PathVariable String type) {
        try {
            return restTemplate.getForObject(aggregatorManagementUrl + "/" + type, Object.class);
        } catch (Exception e) {
            log.error("Failed to fetch management data type {}", type, e);
            return null;
        }
    }

    @PostMapping("/management/normalization-queue/{id}/retry")
    public void retryNormalization(@PathVariable Long id) {
        try {
            restTemplate.postForObject(aggregatorManagementUrl + "/normalization-queue/" + id + "/retry", null, Void.class);
        } catch (Exception e) {
            log.error("Failed to retry normalization id {}", id, e);
        }
    }

    @DeleteMapping("/management/normalization-queue/{id}")
    public void deleteNormalization(@PathVariable Long id) {
        try {
            restTemplate.delete(aggregatorManagementUrl + "/normalization-queue/" + id);
        } catch (Exception e) {
            log.error("Failed to delete normalization id {}", id, e);
        }
    }
}
