package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.AiAnalysisResultDto;
import pro.datawiki.igaming.dto.sport.Sport;

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

    private final Map<Integer, Long> disabledWorkersUntil = new java.util.concurrent.ConcurrentHashMap<>();

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    /**
     * Dispatches match analysis task to an available AGY worker.
     */
    public Optional<AiAnalysisResultDto> evaluateMatch(Long matchId, String dossierPrompt, String constitution) {
        return evaluateMatch(matchId, dossierPrompt, constitution, null);
    }

    public Optional<AiAnalysisResultDto> evaluateMatch(Long matchId, String dossierPrompt, String constitution, Sport sport) {
        log.info("Starting AGY AI evaluation for matchId={} (Sport={})...", matchId, sport != null ? sport.getCode() : "DEFAULT");

        String fullPrompt = buildFullAnalystPrompt(matchId, dossierPrompt, constitution, sport);

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
                        boolean isBrowserVerification = lastOutput.contains("Please verify your account in your browser")
                                || (lastOutput.contains("accounts.google.com/signin/continue") && lastOutput.contains("Verify your account"));
                        if (isBrowserVerification) {
                            log.warn("Worker plane-ai-worker-{} encountered Google account browser verification requirement. Placing on 15m cooldown.", podIndex);
                            disabledWorkersUntil.put(podIndex, System.currentTimeMillis() + 15 * 60 * 1000L);
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
        long now = System.currentTimeMillis();
        for (int i = 0; i < poolSize; i++) {
            if (exclude != null && exclude.contains(i)) {
                continue;
            }
            // Skip workers with known bad OAuth tokens or active temporary cooldown
            if (i == 2 || i == 3 || disabledWorkersUntil.getOrDefault(i, 0L) > now) {
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

    private String buildFullAnalystPrompt(Long matchId, String dossier, String constitution, Sport sport) {
        boolean hasDraw = sport == null || sport.hasDrawOutcome();
        String sportCode = sport != null ? sport.getCode() : "FOOTBALL";

        String directive4;
        String outputExample;

        if (hasDraw) {
            directive4 = """
            4. MARKET SELECTION, TOTALS & DRAW RISK SHIELD:
               - PURE ZERO-ODDS STAGE: You do NOT see bookmaker odds. You are estimating true physical probabilities.
               - TOTALS OVER/UNDER AS PRIMARY WEAPON:
                 When the 1X2 match outcome is balanced, uncertain, or coinflip (P1 ~ P2 ~ 35%), but expected goals/tempo/ML model indicate an overwhelming probability for Totals (e.g. TOTAL_OVER_2.5 >= 70%% or TOTAL_OVER_3.0), you MUST select the TOTAL market as your primary "recommended_bet" (e.g. "TOTAL_OVER_2.5")! Do NOT force a 1X2 or Double Chance pick when the real mathematical edge is in Totals!
               - ⛔ BENCH CHAOS VETO ON TOTAL UNDER:
                 If there is an explosive attacking joker or high-rated offensive substitute on the bench (marked as ⚡ Bench Joker or rating >= 7.0), you are STRICTLY FORBIDDEN from recommending TOTAL_UNDER_2.0 or TOTAL_UNDER_2.5! The introduction of fresh attacking talent against fatigued defenders in the 65-80th minute shatters low totals.
               - FORBIDDEN: Strictly FORBIDDEN to pick "AH_HOME_0" or "AH_AWAY_0" if draw probability P(Draw) >= 24%% or if expected xG <= 2.2.
               - ALWAYS PREFER DOUBLE CHANCE FOR 1X2 RISKS: In games where you do bet on the outcome and draw risk is elevated, select "DOUBLE_CHANCE_1X" or "DOUBLE_CHANCE_X2".
               - Asian Handicaps are strictly reserved for positive underdog cushions (+1.0, +1.5, +2.0) or clear negative spreads (-1.0) when P(Win) >= 65%%.
            """;

            outputExample = String.format("""
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
              "confidence_score": 85,
              "tactical_rationale": "Подробное тактическое обоснование на русском языке...",
              "key_factors": ["FACTOR_1", "FACTOR_2"],
              "steam_direction": "NEUTRAL",
              "lineup_status": "CONFIRMED",
              "shadow_picks": [
                {
                  "type": "DRAW_DEFENSE",
                  "market": "DOUBLE_CHANCE_1X",
                  "hypothesis": "Страховочный сценарий на русском языке"
                },
                {
                  "type": "CUSHION_TOTAL",
                  "market": "TOTAL_UNDER_3.5",
                  "hypothesis": "Подушечный сценарий тотала на русском языке"
                }
              ]
            }
            ```
            """, matchId);
        } else if ("BASKETBALL".equalsIgnoreCase(sportCode)) {
            directive4 = """
            4. BASKETBALL QUANTITATIVE PROTOCOL (STRICTLY NO DRAWS):
               - FORBIDDEN: In basketball, draws DO NOT EXIST! Strictly FORBIDDEN to recommend "DOUBLE_CHANCE_1X", "DOUBLE_CHANCE_X2", "1X", "X2", "PX", or "DRAW".
               - TARGET MARKETS:
                 a) Match Points Total (e.g. "TOTAL_OVER_160.5" or "TOTAL_UNDER_165.5"). If game pace is electric or both teams shoot >38%% from deep, TOTAL OVER is your highest-probability pick!
                 b) Asian Handicap / Point Spreads (e.g. "AH_HOME_MINUS_5.5", "AH_AWAY_PLUS_7.5"). Give underdog spread cushions (+5.5 to +12.5) when matchup has high 3PT volatility.
                 c) Moneyline ("P1" or "P2") for confident favorites.
            """;

            outputExample = String.format("""
            ```json
            {
              "match_id": %d,
              "conviction_tier": "CONFIDENT",
              "core_hypothesis": "Основная тактическая гипотеза по темпу и броскам на русском языке",
              "identified_risk": "Ключевой риск темпа / потерь на русском языке",
              "expected_score": "92 : 84",
              "fair_p1": 68.0,
              "fair_px": 0.0,
              "fair_p2": 32.0,
              "fair_over25": 0.0,
              "fair_under25": 0.0,
              "fair_btts_yes": 0.0,
              "fair_btts_no": 0.0,
              "recommended_bet": "AH_HOME_MINUS_5.5",
              "market_type": "ASIAN_HANDICAP",
              "confidence_score": 85,
              "tactical_rationale": "Обоснование темпа атак и реализации трехочковых на русском языке...",
              "key_factors": ["HIGH_PACE", "RIM_PROTECTION_EDGE"],
              "steam_direction": "NEUTRAL",
              "lineup_status": "CONFIRMED",
              "shadow_picks": [
                {
                  "type": "CUSHION_SPREAD",
                  "market": "AH_AWAY_PLUS_10.5",
                  "hypothesis": "Страховочная плюсовая фора на случай спада точности"
                },
                {
                  "type": "CUSHION_TOTAL",
                  "market": "TOTAL_UNDER_179.5",
                  "hypothesis": "Подушечный верхний тотал на случай вязкой защиты"
                }
              ]
            }
            ```
            """, matchId);
        } else {
            // Tennis, Table Tennis, Racket, Combat, Solo sports
            directive4 = """
            4. RACKET & SOLO SPORT PROTOCOL (STRICTLY NO DRAWS):
               - FORBIDDEN: Draws DO NOT EXIST! Strictly FORBIDDEN to recommend "DOUBLE_CHANCE_1X", "DOUBLE_CHANCE_X2", "1X", "X2", "PX", or "DRAW".
               - TARGET MARKETS:
                 a) Moneyline ("P1" or "P2") for confident favorites. In danger zone, prefer cushioned Set Handicap (+1.5 sets).
                 b) Set Handicap: "AH_HOME_PLUS_1.5" or "AH_AWAY_PLUS_1.5" (+1.5 sets cushion for underdog).
                 c) Game Total ("TOTAL_OVER_21.5" / "TOTAL_UNDER_22.5") if game pace dictates a tight 3-setter.
            """;

            outputExample = String.format("""
            ```json
            {
              "match_id": %d,
              "conviction_tier": "CONFIDENT",
              "core_hypothesis": "Основная тактическая гипотеза (подача / покрытие / форма) на русском языке",
              "identified_risk": "Ключевой риск срыва подачи / тай-брейка на русском языке",
              "expected_score": "2 : 0",
              "fair_p1": 72.0,
              "fair_px": 0.0,
              "fair_p2": 28.0,
              "fair_over25": 0.0,
              "fair_under25": 0.0,
              "fair_btts_yes": 0.0,
              "fair_btts_no": 0.0,
              "recommended_bet": "AH_HOME_PLUS_1.5",
              "market_type": "ASIAN_HANDICAP",
              "confidence_score": 86,
              "tactical_rationale": "Обоснование стабильности первой подачи и формы на данном покрытии...",
              "key_factors": ["FIRST_SERVE_HOLD", "SURFACE_SPECIALIST"],
              "steam_direction": "NEUTRAL",
              "lineup_status": "CONFIRMED",
              "shadow_picks": [
                {
                  "type": "CUSHION_SPREAD",
                  "market": "AH_HOME_PLUS_1.5",
                  "hypothesis": "Страховочная фора по сетам (+1.5) при упорной борьбе"
                },
                {
                  "type": "CUSHION_TOTAL",
                  "market": "TOTAL_OVER_20.5",
                  "hypothesis": "Страховочный тотал при тай-брейке"
                }
              ]
            }
            ```
            """, matchId);
        }

        return """
            %s

            ---------------------------------------------------------------------------------
            CRITICAL OPERATIONAL DIRECTIVES (STAGE 1: ZERO-ODDS SPORTS SIMULATION):
            1. PURE COGNITIVE & TACTICAL ANALYSIS: You are running in 100%% BLIND ZERO-ODDS MODE.
               You DO NOT see bookmaker odds. You DO NOT know which team the public or bookmaker favors.
               Your mission is to simulate the sporting event strictly from first principles:
               player ratings, starters, bench depth, injuries, match history, and tactics.
            2. ABSOLUTELY NO TOOL CALLS: DO NOT use bash commands, DO NOT search the web, DO NOT read or edit files.
               The dossier below contains all comprehensive rosters, player metrics, and rules.
            3. SINGLE-TURN EXECUTION: Perform all internal probabilistic simulations immediately and emit the final JSON block directly in your first turn.
            %s
            5. FAIR PROBABILITIES & CONVICTION TIERS (ZERO RANDOM GUESSES):
               - True Objective Probabilities: Compute fair probabilities ("fair_p1", "fair_px", "fair_p2") reflecting pure sports reality.
                 Sum of fair_p1 + fair_px + fair_p2 MUST equal 100.0%%.
               - DATA-BLINDNESS VETO VS ACTIVE QUANT ANALYSIS:
                 * Complete Data Void: If the dossier completely lacks ANY factual sporting evidence for BOTH sides (no match history for either team, no DNA profiles, no ratings, and no tactical context),
                   YOU MUST SET:
                   "conviction_tier": "PASS",
                   "recommended_bet": "PASS",
                   "core_hypothesis": "Недостаточно данных в досье (отсутствует история / составы / рейтинги). Пропуск матча во избежание дисперсионного риска."
                 * Actionable Sporting Data: If the dossier contains recent matches, team DNA profiles (e.g. HOME_FORTRESS, DEFENSIVE_WALL, CRISIS_STREAK, SCORING_MACHINE), ratings, or storylines,
                   DO NOT PASS! Actively simulate the tactical matchup, evaluate underlying form and matchup dynamic, compute fair probabilities ("fair_p1", "fair_px", "fair_p2"), and assign an active conviction tier ("CONFIDENT" or "GOOD_PICK")!
               - "CONFIDENT" («Да, я уверен»): Ironclad tactical/class dominance (P_fair >= 70%%, clear lineup superiority, minimal upset risk).
               - "GOOD_PICK" («Да, это хороший выбор»): Solid tactical edge (+EV candidate), but with game-state volatility. For this tier:
                  * In sports WITHOUT DRAWS (Tennis, Table Tennis, Basketball, Snooker, Volleyball, Combat): ALWAYS select direct "P1" or "P2" (or "TOTAL_OVER" / "TOTAL_UNDER") — do NOT pick exotic set handicaps that lack pre-match liquidity!
                  * In sports WITH DRAWS (Football, Ice Hockey): NEVER select raw P1/P2 in balanced games — ALWAYS choose a cushioned market ("DOUBLE_CHANCE_1X", "DOUBLE_CHANCE_X2", "AH_HOME_0", "AH_AWAY_0", or "TOTAL_UNDER_2.5" / "TOTAL_OVER_2.5").
               - "PASS" («Пропуск матча»): If the matchup has no distinct edge, is overly chaotic, or lacks data, set "conviction_tier": "PASS" and "recommended_bet": "PASS".
            6. DO NOT GUESS ODDS OR BOOKMAKERS:
               Do NOT output "market_odd", "best_bookmaker", "expected_value_pct", or "kelly_stake_pct".
               These are computed automatically in Stage 2 by comparing your fair probabilities with real-time live market quotes.
            7. LANGUAGE MANDATE:
               - Reason internally in English using mathematical rigor and game-state tree modeling.
               - Provide all user-facing descriptive texts ("core_hypothesis", "identified_risk", "tactical_rationale", and shadow pick "hypothesis") STRICTLY in Russian for dashboard operators.
            ---------------------------------------------------------------------------------
            DOSSIER FOR MATCH #%d:
            ---------------------------------------------------------------------------------
            %s

            ---------------------------------------------------------------------------------
            MANDATORY OUTPUT FORMAT:
            Return STRICTLY ONE valid JSON markdown codeblock in the following schema:
            %s
            """.formatted(constitution, directive4, matchId, dossier, outputExample);
    }
}
