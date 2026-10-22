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

    private final Set<Integer> disabledWorkers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    /**
     * Dispatches match analysis task to an available AGY worker.
     */
    public Optional<AiAnalysisResultDto> evaluateMatch(Long matchId, String dossierPrompt, String constitution) {
        log.info("Starting AGY AI evaluation for matchId={}...", matchId);

        String fullPrompt = buildFullAnalystPrompt(matchId, dossierPrompt, constitution);

        Set<Integer> triedWorkers = new HashSet<>();
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Optional<Integer> targetWorkerOpt = selectAvailableWorker(triedWorkers);
            if (targetWorkerOpt.isEmpty()) {
                log.warn("No available eligible AGY workers in pool for matchId={}, attempt {}/{}", matchId, attempt, maxAttempts);
                break;
            }

            int targetWorker = targetWorkerOpt.get();
            triedWorkers.add(targetWorker);
            String workerUrl = String.format(workerUrlTemplate, targetWorker);
            log.info("Selected worker plane-ai-worker-{} at {} (attempt {}/{})", targetWorker, workerUrl, attempt, maxAttempts);

            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("issueId", "analyst-match-" + matchId);
                payload.put("taskTitle", "AI Quantitative Sports Analysis for Match #" + matchId);
                payload.put("taskDescription", fullPrompt);
                payload.put("model", defaultModel);
                payload.put("agentMd", constitution);
                payload.put("taskType", "ANALYST");
                payload.put("gitRepo", "https://github.com/datawikipro/igaming");

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

                    Optional<AiAnalysisResultDto> result = pollWorkerResult(targetWorker, matchId);
                    if (result.isPresent()) {
                        return result;
                    }
                    log.warn("Worker plane-ai-worker-{} produced empty or invalid result for matchId={}, failing over to next worker...",
                            targetWorker, matchId);
                } else {
                    log.warn("Worker plane-ai-worker-{} rejected task with status {}: {}",
                            targetWorker, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.error("Error communicating with AGY worker plane-ai-worker-{} for matchId={}: {}", targetWorker, matchId, e.getMessage());
            }
        }

        log.error("All {} AGY worker attempts failed for matchId={}", maxAttempts, matchId);
        return Optional.empty();
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
                        if (lastOutput.contains("Eligibility check failed") || lastOutput.contains("Verify your account") || lastOutput.contains("Please verify your account")) {
                            log.warn("Worker plane-ai-worker-{} encountered Google account eligibility verification requirement. Disabling worker.", podIndex);
                            disabledWorkers.add(podIndex);
                            return Optional.empty();
                        }
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
     * Finds eligible IDLE workers from the pool, excluding any failed or busy ones,
     * and randomizes selection across eligible workers for load balancing.
     */
    private Optional<Integer> selectAvailableWorker(Set<Integer> exclude) {
        List<Integer> eligible = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            if (exclude != null && exclude.contains(i)) {
                continue;
            }
            // Skip workers with known bad OAuth tokens or verification blocks
            if (i == 2 || i == 3 || disabledWorkers.contains(i)) {
                continue;
            }
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
                        eligible.add(i);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (eligible.isEmpty()) {
            return Optional.empty();
        }
        Collections.shuffle(eligible);
        return Optional.of(eligible.get(0));
    }

    private String buildFullAnalystPrompt(Long matchId, String dossier, String constitution) {
        return """
            %s

            ---------------------------------------------------------------------------------
            CRITICAL OPERATIONAL DIRECTIVES:
            1. PURE COGNITIVE ANALYSIS MODE: You are a pure quantitative and tactical reasoning engine.
            2. ABSOLUTELY NO TOOL CALLS: DO NOT use bash commands, DO NOT search the web, DO NOT read or edit files.
               The dossier below contains all comprehensive live odds, lineups, player ratings, H2H, and tactical rules.
            3. SINGLE-TURN EXECUTION: Perform all internal probabilistic simulations immediately and emit the final JSON block directly in your first turn.
            4. DOUBLE CHANCE IMPERATIVE (DRAW RISK SHIELD):
               - FORBIDDEN: Strictly FORBIDDEN to pick "AH_HOME_0" or "AH_AWAY_0" if draw probability P(Draw) >= 24%% or if expected xG <= 2.2.
               - ALWAYS PREFER DOUBLE CHANCE: In such games, you MUST select "DOUBLE_CHANCE_1X" or "DOUBLE_CHANCE_X2".
                 Empirical backtests prove Double Chance achieves an 87.5%% winrate and turns draws into direct profit rather than freezing bankroll.
               - Asian Handicaps are strictly reserved for positive underdog cushions (+1.0, +1.5, +2.0) or clear negative spreads (-1.0) when P(Win) >= 65%%.
            5. CONVICTION TIERS AND THE RIGHT TO PASS (NO FORCED PICKS):
               You are NEVER forced to pick an artificial bet if a match is volatile or fifty-fifty.
               You MUST classify your decision into one of 3 conviction tiers:
               - "CONFIDENT" («Да, я уверен»): Ironclad tactical/class dominance (P(Win) >= 70%%, clear lineup superiority, minimal upset risk). Full Kelly allocation.
               - "GOOD_PICK" («Да, это хороший выбор»): Solid +EV value proposition, but with identified game-state volatility. For this tier, NEVER select raw P1/P2 at odds >= 1.45 — ALWAYS choose a cushioned market (e.g. "AH_HOME_PLUS_1.5" in racket/hockey, or "DOUBLE_CHANCE_1X" in football).
               - "PASS" («Пропуск матча»): If the matchup has no distinct edge, is overly chaotic, or risk outweighs expected reward, set "conviction_tier": "PASS" and "recommended_bet": "PASS".
            6. LANGUAGE MANDATE:
               - Reason internally in English using mathematical rigor and game-state tree modeling.
               - Provide all user-facing descriptive texts ("core_hypothesis", "identified_risk", "tactical_rationale", and shadow pick "hypothesis") STRICTLY in Russian for dashboard operators.
            ---------------------------------------------------------------------------------
            DOSSIER FOR MATCH #%d:
            ---------------------------------------------------------------------------------
            %s

            ---------------------------------------------------------------------------------
            MANDATORY OUTPUT FORMAT:
            Return STRICTLY ONE valid JSON markdown codeblock in the following schema:
            ```json
            {
              "match_id": %d,
              "conviction_tier": "CONFIDENT",
              "core_hypothesis": "Основная тактическая гипотеза на русском языке",
              "identified_risk": "Идентифицированный ключевой риск сценария на русском языке",
              "expected_xg": "1.35 : 0.95",
              "fair_p1": 46.5,
              "fair_px": 28.5,
              "fair_p2": 25.0,
              "fair_over25": 42.0,
              "fair_under25": 58.0,
              "fair_btts_yes": 45.0,
              "fair_btts_no": 55.0,
              "recommended_bet": "DOUBLE_CHANCE_1X",
              "market_type": "DOUBLE_CHANCE",
              "market_odd": 1.42,
              "best_bookmaker": "baltbet",
              "expected_value_pct": 8.5,
              "kelly_stake_pct": 2.5,
              "confidence_score": 85,
              "tactical_rationale": "Подробное тактическое обоснование на русском языке...",
              "key_factors": ["FACTOR_1", "FACTOR_2"],
              "steam_direction": "NEUTRAL",
              "lineup_status": "CONFIRMED",
              "shadow_picks": [
                {
                  "type": "DRAW_DEFENSE",
                  "market": "DOUBLE_CHANCE_1X",
                  "odd": 1.38,
                  "hypothesis": "Страховочный сценарий на русском языке"
                },
                {
                  "type": "CUSHION_TOTAL",
                  "market": "TOTAL_UNDER_3.5",
                  "odd": 1.45,
                  "hypothesis": "Подушечный сценарий тотала на русском языке"
                }
              ]
            }
            ```
            """.formatted(constitution, matchId, dossier, matchId);
    }
}
