package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationSpecification;
import pro.datawiki.igaming.analyst.service.AiAnalystOrchestrator;
import pro.datawiki.igaming.analyst.service.MatchDossierService;
import pro.datawiki.igaming.analyst.service.MatchSettlementService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai-analyst")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiAnalystController {

    private final AiAnalystOrchestrator orchestrator;
    private final MatchDossierService dossierService;
    private final MatchSettlementService settlementService;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final pro.datawiki.igaming.analyst.repository.AiLearningMemoryRepository memoryRepository;
    private final pro.datawiki.igaming.analyst.service.MlInferenceClient mlInferenceClient;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final pro.datawiki.igaming.analyst.repository.AiPolicyInterventionRepository policyInterventionRepository;
    private final pro.datawiki.igaming.analyst.service.StrategyEngineService strategyEngineService;

    /**
     * Trigger on-demand AI evaluation of a match.
     */
    @PostMapping("/evaluate/{matchId}")
    public ResponseEntity<?> evaluateMatch(@PathVariable Long matchId) {
        log.info("Received REST request to evaluate match #{}", matchId);
        Optional<MatchAiRecommendation> result = orchestrator.evaluateMatch(matchId);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.badRequest().body(Map.of(
                "error", "EVALUATION_FAILED",
                "message", "Could not produce recommendation for match #" + matchId
        ));
    }

    /**
     * Inspect raw pre-assembled dossier prompt for a match.
     */
    @GetMapping("/dossier/{matchId}")
    public ResponseEntity<?> getMatchDossier(@PathVariable Long matchId) {
        String prompt = dossierService.getFormattedPrompt(matchId);
        Map<String, Object> meta = dossierService.getMatchDossier(matchId);
        if (prompt == null || prompt.startsWith("MATCH_NOT_FOUND")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "matchId", matchId,
                "metadata", meta,
                "dossierPrompt", prompt
        ));
    }

    /**
     * List recommendations with database-level filtering and pagination.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> listRecommendations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String oddRange,
            @RequestParam(required = false) String bookmaker,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String strategy,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size,
            @RequestParam(required = false) String sort) {

        // If pagination or advanced filters are requested, execute DB-level specification query
        if (page != null || tier != null || status != null || sport != null || stage != null || timeRange != null || oddRange != null || (bookmaker != null && !bookmaker.isBlank()) || (query != null && !query.isBlank()) || (strategy != null && !strategy.isBlank()) || (version != null && !version.isBlank()) || "paged".equalsIgnoreCase(sort)) {
            Specification<MatchAiRecommendation> spec = MatchAiRecommendationSpecification.filter(
                    status, tier, sport, stage, timeRange, oddRange, bookmaker, query, strategy, version
            );
            Sort sortOrder = Sort.by(Sort.Direction.DESC, "id");
            if ("time_asc".equalsIgnoreCase(sort) || "matchStartTime,asc".equalsIgnoreCase(sort)) {
                sortOrder = Sort.by(Sort.Direction.ASC, "matchStartTime").and(Sort.by(Sort.Direction.DESC, "id"));
            }
            Pageable pageable = PageRequest.of(
                    page != null ? Math.max(0, page) : 0,
                    size != null ? Math.min(100, Math.max(1, size)) : 50,
                    sortOrder
            );
            Page<MatchAiRecommendation> paged = recommendationRepository.findAll(spec, pageable);
            strategyEngineService.enrichRecommendationsWithVotes(paged.getContent(), strategy);
            return ResponseEntity.ok(paged);
        }

        // Backward compatibility mode for unpaginated simple calls
        List<MatchAiRecommendation> list;
        if (status != null && !status.isBlank()) {
            if ("SETTLED".equalsIgnoreCase(status)) {
                list = recommendationRepository.findByResultStatusInOrderByIdDesc(List.of("WON", "LOST", "VOID"));
            } else {
                list = recommendationRepository.findByResultStatus(status.toUpperCase());
            }
        } else if ("WAIT".equalsIgnoreCase(tier)) {
            list = recommendationRepository.findByResultStatus("WAITING");
        } else if ("PASS".equalsIgnoreCase(tier)) {
            list = recommendationRepository.findTop150ByResultStatusOrderByIdDesc("SKIPPED");
        } else if ("ACTIVE".equalsIgnoreCase(tier)) {
            list = recommendationRepository.findByResultStatus("PENDING");
        } else if ("NOT_PASS".equalsIgnoreCase(tier) || "NO_PASS".equalsIgnoreCase(tier)) {
            list = recommendationRepository.findAllActiveAndSettled();
        } else {
            List<MatchAiRecommendation> activeAndSettled = recommendationRepository.findAllActiveAndSettled();
            List<MatchAiRecommendation> recentPass = recommendationRepository.findTop150ByResultStatusOrderByIdDesc("SKIPPED");
            list = new java.util.ArrayList<>(activeAndSettled);
            list.addAll(recentPass);
            list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));
        }
        strategyEngineService.enrichRecommendationsWithVotes(list, strategy);
        return ResponseEntity.ok(list);
    }

    /**
     * Get single recommendation.
     */
    @GetMapping("/recommendations/{id}")
    public ResponseEntity<MatchAiRecommendation> getRecommendation(@PathVariable Long id) {
        return recommendationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Performance stats (winrate, ROI, PnL).
     */
    @GetMapping("/stats")
    public ResponseEntity<AnalystStatsDto> getPerformanceStats(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String oddRange,
            @RequestParam(required = false) String bookmaker,
            @RequestParam(required = false) String marketCategory,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String version) {
        return ResponseEntity.ok(orchestrator.getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, version));
    }

    /**
     * List all registered AI model versions with release notes and live aggregated performance metrics.
     */
    @GetMapping("/versions")
    public ResponseEntity<List<pro.datawiki.igaming.analyst.dto.ModelVersionDto>> getModelVersions() {
        return ResponseEntity.ok(orchestrator.getModelVersions());
    }

    /**
     * List available bookmakers with active match counts and metadata.
     */
    @GetMapping("/bookmakers")
    public ResponseEntity<?> getAvailableBookmakers() {
        return ResponseEntity.ok(orchestrator.getAvailableBookmakersStats());
    }

    /**
     * Manually trigger scan and parallel evaluation of upcoming matches.
     */
    @PostMapping("/scan")
    public ResponseEntity<?> triggerScan() {
        new Thread(orchestrator::scanUpcomingMatches, "manual-scan-trigger").start();
        return ResponseEntity.ok(Map.of("status", "SCAN_TRIGGERED", "message", "Parallel scan initiated"));
    }

    /**
     * Manually trigger settlement of finished matches across all pending bets.
     */
    @PostMapping("/settle")
    public ResponseEntity<?> triggerSettlement() {
        settlementService.settleFinishedMatches();
        return ResponseEntity.ok(Map.of("status", "SETTLEMENT_COMPLETED"));
    }

    /**
     * Settle a specific recommendation with an explicit score or trigger on-demand settlement.
     */
    @PostMapping("/recommendations/{id}/settle")
    public ResponseEntity<?> settleRecommendation(
            @PathVariable Long id,
            @RequestParam(value = "finalScore", required = false) String finalScoreParam,
            @RequestBody(required = false) Map<String, String> body) {
        String score = finalScoreParam != null && !finalScoreParam.isBlank() ? finalScoreParam : (body != null ? body.get("finalScore") : null);
        if (score == null || score.isBlank()) {
            Optional<MatchAiRecommendation> recOpt = recommendationRepository.findById(id);
            if (recOpt.isPresent()) {
                boolean settled = settlementService.settleSingleRecommendation(recOpt.get());
                if (settled) {
                    return ResponseEntity.ok(recommendationRepository.findById(id).orElse(recOpt.get()));
                }
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "MISSING_SCORE",
                    "message", "Не указан 'finalScore' в теле запроса (например: {\"finalScore\": \"6:2, 6:4\"} или {\"finalScore\": \"2:1\"})"
            ));
        }

        Optional<MatchAiRecommendation> result = settlementService.settleRecommendationManually(id, score);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.badRequest().body(Map.of(
                "error", "SETTLEMENT_FAILED",
                "message", "Не удалось рассчитать рекомендацию #" + id + " по счету '" + score + "'. Проверьте корректность счета или правил дисциплины."
        ));
    }

    /**
     * Resolves a schedule conflict or quarantine on a recommendation.
     * Called by Server-Side Anomaly Resolver Robot.
     */
    @PostMapping("/recommendations/{matchId}/resolve-conflict")
    public ResponseEntity<?> resolveConflict(
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "PROMOTE") String action,
            @RequestParam(defaultValue = "RESOLVED_BY_ROBOT") String reason,
            @RequestParam(required = false) Long winnerMatchId
    ) {
        log.info("🤖 [API] Resolving conflict for match #{}: action={}, reason={}, winnerMatchId={}",
                matchId, action, reason, winnerMatchId);
        Optional<MatchAiRecommendation> recOpt = recommendationRepository.findByMatchId(matchId);
        if (recOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MatchAiRecommendation rec = recOpt.get();
        if ("CANCEL".equalsIgnoreCase(action)) {
            rec.setResultStatus("CANCELLED");
            rec.setEvaluationStage("CANCELLED_SYNTHETIC_COLLISION");
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis(String.format("ОТМЕНЕНО РОБОТОМ: синтетический фентези дубликат (причина: %s). Реальный матч: #%s",
                    reason, winnerMatchId != null ? winnerMatchId : "подтвержден в SofaScore"));
            rec.setIdentifiedRisk("Синтетическая кросс-дуэль лиги аннулирована.");
            recommendationRepository.save(rec);
            return ResponseEntity.ok(Map.of("status", "CANCELLED", "matchId", matchId));
        } else if ("PROMOTE".equalsIgnoreCase(action)) {
            rec.setResultStatus("PENDING");
            rec.setEvaluationStage("EVALUATED");
            recommendationRepository.save(rec);
            // Re-evaluate with ML model
            Optional<MatchAiRecommendation> evaluated = orchestrator.forceEvaluateMatch(matchId);
            return ResponseEntity.ok(evaluated.orElse(rec));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Unknown action: " + action));
    }

    /**
     * Trigger manual reconciliation across all recent recommendations (including VOID recovery).
     */
    @PostMapping("/reconcile")
    public ResponseEntity<?> triggerReconciliation() {
        log.info("Triggering on-demand settlement reconciliation...");
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                settlementService.reconcileRecentlySettledMatches();
            } catch (Exception e) {
                log.error("Async reconciliation failed: {}", e.getMessage(), e);
            }
        });
        return ResponseEntity.ok(Map.of("status", "RECONCILIATION_TRIGGERED"));
    }

    /**
     * List recent persistent AI learning lessons.
     */
    @GetMapping("/memories")
    public ResponseEntity<List<pro.datawiki.igaming.analyst.entity.AiLearningMemory>> listMemories() {
        return ResponseEntity.ok(memoryRepository.findTop20ByOrderByCreatedAtDesc());
    }

    /**
     * Retrieve active AI recommendation for a specific match ID.
     */
    @GetMapping("/recommendations/match/{matchId}")
    public ResponseEntity<?> getRecommendationByMatchId(@PathVariable Long matchId) {
        return recommendationRepository.findByMatchId(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve detailed 9-scenario cross simulation & projected lineups for a recommendation.
     */
    @GetMapping("/recommendations/{id}/scenarios")
    public ResponseEntity<?> getRecommendationScenarios(@PathVariable Long id) {
        return recommendationRepository.findById(id).map(originalRec -> {
            MatchAiRecommendation rec = originalRec;
            if (rec.getProjectedLineupJson() == null || rec.getProjectedLineupJson().isBlank()) {
                rec = orchestrator.enrichAndPersistProjectedScenarios(rec).orElse(rec);
            }

            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("id", rec.getId());
            resp.put("matchId", rec.getMatchId());
            resp.put("team1Name", rec.getTeam1Name());
            resp.put("team2Name", rec.getTeam2Name());
            resp.put("sportName", rec.getSportName());
            resp.put("leagueName", rec.getLeagueName());
            resp.put("lineupStatus", rec.getLineupStatus());
            resp.put("lineupResiliencePct", rec.getLineupResiliencePct());
            resp.put("keyPlayerDependencies", rec.getKeyPlayerDependencies());
            resp.put("recommendedMarket", rec.getRecommendedMarket());
            resp.put("recommendedOdd", rec.getRecommendedOdd());
            resp.put("fairP1", rec.getFairP1());
            resp.put("fairPx", rec.getFairPx());
            resp.put("fairP2", rec.getFairP2());
            resp.put("fairOver25", rec.getFairOver25());
            resp.put("fairUnder25", rec.getFairUnder25());

            if (rec.getProjectedLineupJson() != null && !rec.getProjectedLineupJson().isBlank()) {
                try {
                    Map<String, Object> lineupData = objectMapper.readValue(rec.getProjectedLineupJson(), Map.class);
                    resp.put("lineupData", lineupData);
                } catch (Exception e) {
                    resp.put("lineupDataRaw", rec.getProjectedLineupJson());
                }
            }
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Interactive What-If Scenario Lab:
     * Simulate custom lineups (player absences / promotions) and get real-time updated ML tactical probabilities.
     */
    @PostMapping("/simulate-custom-lineup")
    public ResponseEntity<?> simulateCustomLineup(@RequestBody Map<String, Object> req) {
        log.info("Received request to simulate custom lineup for {} vs {}", req.get("team1_name"), req.get("team2_name"));
        Map<String, Object> result = mlInferenceClient.simulateCustomLineup(req);
        return ResponseEntity.ok(result);
    }

    /**
     * Get team lineup profile with historical roster breakdown and ratings.
     */
    @GetMapping("/team-profile")
    public ResponseEntity<?> getTeamProfile(
            @RequestParam String team,
            @RequestParam(defaultValue = "football") String sport) {
        return ResponseEntity.ok(mlInferenceClient.getTeamProfile(team, sport));
    }

    /**
     * Institutional Decision Rule Audit Log:
     * Returns history of risk interventions (tier caps, zero-draw transforms, volatility guards).
     */
    @GetMapping("/policy-audit")
    public ResponseEntity<?> listPolicyInterventions(
            @RequestParam(required = false) String ruleCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (ruleCode != null && !ruleCode.isBlank()) {
            return ResponseEntity.ok(policyInterventionRepository.findByRuleCodeOrderByCreatedAtDesc(ruleCode.trim(), pageable));
        }
        return ResponseEntity.ok(policyInterventionRepository.findByOrderByCreatedAtDesc(pageable));
    }

    /**
     * Institutional Decision Rule Summary:
     * Returns aggregate statistics on interventions and net PnL impact (money saved on losses vs opportunity cost).
     */
    @GetMapping("/policy-audit/summary")
    public ResponseEntity<?> getPolicyInterventionSummary() {
        return ResponseEntity.ok(policyInterventionRepository.getPolicyInterventionSummary());
    }

}
