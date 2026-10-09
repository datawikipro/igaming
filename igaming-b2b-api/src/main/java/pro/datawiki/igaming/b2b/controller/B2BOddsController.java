package pro.datawiki.igaming.b2b.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.b2b.service.B2bQuotaService;
import pro.datawiki.igaming.dto.SurebetAlertDto;
import pro.datawiki.igaming.dto.ValueBetAlertDto;

import java.util.*;

@RestController
@RequestMapping("/api/v1/odds")
@RequiredArgsConstructor
@Slf4j
public class B2BOddsController {

    private final B2bQuotaService quotaService;
    private final RestTemplate restTemplate;

    @Value("${app.aggregator.url:http://igaming-aggregator.igaming-dev.svc.cluster.local:8080/api}")
    private String aggregatorUrl;

    @GetMapping("/surebets")
    public ResponseEntity<?> getSurebets(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam(value = "min_profit", defaultValue = "0.0") double minProfit,
            @RequestParam(value = "bookmakers", required = false) String bookmakers,
            @RequestParam(value = "is_live", required = false) Boolean isLive,
            @RequestParam(value = "format", defaultValue = "decimal") String format) {

        ResponseEntity<?> authCheck = checkQuota(apiKey);
        if (authCheck != null) return authCheck;

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(aggregatorUrl + "/surebets/active");
            ResponseEntity<List<SurebetAlertDto>> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<SurebetAlertDto>>() {}
            );

            List<SurebetAlertDto> results = response.getBody() != null ? response.getBody() : Collections.emptyList();

            // Filter by minProfit
            if (minProfit > 0.0) {
                results = results.stream()
                        .filter(s -> s.getProfitPercent() != null && s.getProfitPercent() >= minProfit)
                        .toList();
            }

            // Filter by isLive
            if (isLive != null) {
                results = results.stream()
                        .filter(s -> Objects.equals(s.getIsLive(), isLive))
                        .toList();
            }

            // Filter by specific bookmakers if requested
            if (bookmakers != null && !bookmakers.isBlank()) {
                Set<String> bkSet = new HashSet<>(Arrays.asList(bookmakers.toLowerCase().split(",")));
                results = results.stream()
                        .filter(s -> s.getOutcomes() != null && s.getOutcomes().stream()
                                .anyMatch(o -> o.getBookmaker() != null && bkSet.contains(o.getBookmaker().toLowerCase())))
                        .toList();
            }

            HttpHeaders headers = buildRateLimitHeaders(apiKey);
            return new ResponseEntity<>(results, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("B2B API: Failed to fetch surebets from aggregator: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Aggregator feed unavailable", "message", e.getMessage()));
        }
    }

    @GetMapping("/valuebets")
    public ResponseEntity<?> getValuebets(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam(value = "min_ev", defaultValue = "0.0") double minEv,
            @RequestParam(value = "bookmakers", required = false) String bookmakers,
            @RequestParam(value = "is_live", required = false) Boolean isLive,
            @RequestParam(value = "format", defaultValue = "decimal") String format) {

        ResponseEntity<?> authCheck = checkQuota(apiKey);
        if (authCheck != null) return authCheck;

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(aggregatorUrl + "/valuebets");
            ResponseEntity<List<ValueBetAlertDto>> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ValueBetAlertDto>>() {}
            );

            List<ValueBetAlertDto> results = response.getBody() != null ? response.getBody() : Collections.emptyList();

            if (minEv > 0.0) {
                results = results.stream()
                        .filter(v -> v.getEv() != null && v.getEv() >= minEv)
                        .toList();
            }

            if (isLive != null) {
                results = results.stream()
                        .filter(v -> Objects.equals(v.getIsLive(), isLive))
                        .toList();
            }

            if (bookmakers != null && !bookmakers.isBlank()) {
                Set<String> bkSet = new HashSet<>(Arrays.asList(bookmakers.toLowerCase().split(",")));
                results = results.stream()
                        .filter(v -> v.getBookmaker() != null && bkSet.contains(v.getBookmaker().toLowerCase()))
                        .toList();
            }

            HttpHeaders headers = buildRateLimitHeaders(apiKey);
            return new ResponseEntity<>(results, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("B2B API: Failed to fetch valuebets from aggregator: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Aggregator feed unavailable", "message", e.getMessage()));
        }
    }

    private ResponseEntity<?> checkQuota(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing X-API-Key authentication header"));
        }

        if (!quotaService.validateApiKey(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid or inactive B2B Commercial API Key"));
        }

        int limit = quotaService.getLimitForApiKey(apiKey);
        boolean allowed = quotaService.tryConsume(apiKey, limit);
        if (!allowed) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Retry-After", "3600");
            headers.set("X-RateLimit-Limit", String.valueOf(limit));
            headers.set("X-RateLimit-Remaining", "0");
            return new ResponseEntity<>(
                    Map.of("error", "Daily API quota exceeded", "limit", limit),
                    headers,
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        return null;
    }

    private HttpHeaders buildRateLimitHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        int limit = quotaService.getLimitForApiKey(apiKey);
        int remaining = quotaService.getRemaining(apiKey, limit);
        headers.set("X-RateLimit-Limit", String.valueOf(limit));
        headers.set("X-RateLimit-Remaining", String.valueOf(remaining));
        return headers;
    }
}
