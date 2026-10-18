package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.AiAnalysisResultDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orchestrates communication with Kubernetes AGY Worker Pods (plane-ai-worker-0..19).
 * Selects an IDLE worker with healthy quota, dispatches analytical tasks,
 * and deserializes the structured quantitative recommendation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgyWorkerClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${analyst.worker.pool-size:20}")
    private int poolSize;

    @Value("${analyst.worker.url-template:http://plane-ai-worker-%d.plane-ai-worker.plane.svc.cluster.local:8000}")
    private String workerUrlTemplate;

    @Value("${analyst.worker.model:gemini-3.8-flash-high}")
    private String defaultModel;

    @Value("${analyst.worker.timeout-seconds:180}")
    private int timeoutSeconds;

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    /**
     * Dispatches match analysis task to an available AGY worker.
     */
    public Optional<AiAnalysisResultDto> evaluateMatch(Long matchId, String dossierPrompt, String constitution) {
        log.info("Starting AGY AI evaluation for matchId={}...", matchId);

        String fullPrompt = buildFullAnalystPrompt(matchId, dossierPrompt, constitution);

        // Find available worker pod index
        int targetWorker = selectAvailableWorker();
        String workerUrl = String.format(workerUrlTemplate, targetWorker);
        log.info("Selected worker plane-ai-worker-{} at {}", targetWorker, workerUrl);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("issueId", "analyst-match-" + matchId);
            payload.put("taskTitle", "AI Quantitative Sports Analysis for Match #" + matchId);
            payload.put("taskDescription", fullPrompt);
            payload.put("model", defaultModel);
            payload.put("agentMd", constitution);
            payload.put("taskType", "ANALYST");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(workerUrl + "/execute"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 202) {
                log.info("Worker plane-ai-worker-{} accepted task for matchId={}. Response: {}",
                        targetWorker, matchId, response.body());

                // Poll or extract result
                return pollWorkerResult(targetWorker, matchId);
            } else {
                log.warn("Worker plane-ai-worker-{} rejected task with status {}: {}",
                        targetWorker, response.statusCode(), response.body());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error communicating with AGY worker for matchId={}: {}", matchId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Polls worker status until the task finishes or timeout expires.
     */
    private Optional<AiAnalysisResultDto> pollWorkerResult(int podIndex, Long matchId) {
        String statusUrl = String.format(workerUrlTemplate, podIndex) + "/status";
        long startTs = System.currentTimeMillis();
        long maxWaitMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTs < maxWaitMillis) {
            try {
                Thread.sleep(3000);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(statusUrl))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode node = objectMapper.readTree(response.body());
                    String status = node.path("status").asText();

                    boolean isBusy = "BUSY".equalsIgnoreCase(status);
                    String lastOutput = node.path("lastOutput").asText("");

                    if ("IDLE".equalsIgnoreCase(status) && !lastOutput.isBlank()) {
                        log.info("Worker plane-ai-worker-{} completed task for matchId={}", podIndex, matchId);
                        return parseAnalystOutput(matchId, lastOutput);
                    }

                    if (!isBusy && (System.currentTimeMillis() - startTs > 20000) && lastOutput.isBlank()) {
                        // After 20 seconds, if still idle with no output, abort
                        log.warn("Worker plane-ai-worker-{} stayed idle without producing output for matchId={}", podIndex, matchId);
                        break;
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.debug("Polling worker-{} status error: {}", podIndex, e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts and parses JSON from agent response.
     */
    public Optional<AiAnalysisResultDto> parseAnalystOutput(Long matchId, String rawText) {
        try {
            String jsonStr = extractJsonString(rawText);
            AiAnalysisResultDto dto = objectMapper.readValue(jsonStr, AiAnalysisResultDto.class);
            if (dto.getMatchId() == null) {
                dto.setMatchId(matchId);
            }
            return Optional.of(dto);
        } catch (Exception e) {
            log.error("Failed to parse AI output into AiAnalysisResultDto: {}. Raw: {}", e.getMessage(), rawText);
            return Optional.empty();
        }
    }

    private String extractJsonString(String text) {
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text;
    }

    /**
     * Round-robin / quota check to pick the best IDLE worker.
     */
    private int selectAvailableWorker() {
        for (int i = 0; i < poolSize; i++) {
            String url = String.format(workerUrlTemplate, i) + "/can-accept";
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode node = objectMapper.readTree(response.body());
                    if (node.path("eligible").asBoolean(false)) {
                        return i;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return 0; // Default fallback to worker 0
    }

    private String buildFullAnalystPrompt(Long matchId, String dossier, String constitution) {
        return """
            %s
            
            ---------------------------------------------------------------------------------
            ВЫПОЛНИТЕ КВАНТОВЫЙ АНАЛИЗ МАТЧА ПО СЛЕДУЮЩЕМУ ДОСЬЕ:
            ---------------------------------------------------------------------------------
            %s
            
            ---------------------------------------------------------------------------------
            ОБЯЗАТЕЛЬНЫЙ ФОРМАТ ВЫВОДА:
            Верните СТРОГО один валидный JSON-блок в формате:
            ```json
            {
              "match_id": %d,
              "core_hypothesis": "Команда 1 победит 1:0 за счет плотной обороны и контроля центра поля",
              "identified_risk": "Опасение шального гола гостей со стандарта, который уведет счет в 1:1 или заставит раскрываться до 2:1",
              "expected_xg": "1.35 : 0.95",
              "fair_p1": 46.5,
              "fair_px": 28.5,
              "fair_p2": 25.0,
              "fair_over25": 42.0,
              "fair_under25": 58.0,
              "fair_btts_yes": 45.0,
              "fair_btts_no": 55.0,
              "recommended_bet": "AH_HOME_0",
              "market_type": "HANDICAP",
              "market_odd": 1.58,
              "best_bookmaker": "baltbet",
              "expected_value_pct": 8.5,
              "kelly_stake_pct": 2.5,
              "confidence_score": 85,
              "tactical_rationale": "Подробное тактическое обоснование...",
              "key_factors": ["FACTOR_1", "FACTOR_2"],
              "steam_direction": "NEUTRAL",
              "lineup_status": "CONFIRMED",
              "shadow_picks": [
                {
                  "type": "DRAW_DEFENSE",
                  "market": "DOUBLE_CHANCE_1X",
                  "odd": 1.38,
                  "hypothesis": "Выживает, если аутсайдер забьет шальной гол и матч закончится 1:1"
                },
                {
                  "type": "CUSHION_TOTAL",
                  "market": "TOTAL_UNDER_3.5",
                  "odd": 1.45,
                  "hypothesis": "Выживает, если после пропущенного гола хозяевам придется забивать 2 мяча и будет 2:1"
                }
              ]
            }
            ```
            """.formatted(constitution, dossier, matchId);
    }
}
