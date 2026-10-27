package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.AiAnalystOrchestrator;
import pro.datawiki.igaming.analyst.service.MatchDossierService;
import pro.datawiki.igaming.analyst.service.MatchSettlementService;

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
     * List recommendations.
     * Default includes all active bets in play (PENDING) and settled bets (WON/LOST/VOID),
     * plus the top 150 recent PASS audit entries, guaranteeing zero missing bets in the dashboard.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<MatchAiRecommendation>> listRecommendations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tier) {
        if (status != null && !status.isBlank()) {
            if ("SETTLED".equalsIgnoreCase(status)) {
                return ResponseEntity.ok(recommendationRepository.findByResultStatusInOrderByIdDesc(List.of("WON", "LOST", "VOID")));
            }
            return ResponseEntity.ok(recommendationRepository.findByResultStatus(status.toUpperCase()));
        }
        if ("PASS".equalsIgnoreCase(tier)) {
            return ResponseEntity.ok(recommendationRepository.findTop150ByResultStatusOrderByIdDesc("SKIPPED"));
        }
        if ("ACTIVE".equalsIgnoreCase(tier)) {
            return ResponseEntity.ok(recommendationRepository.findByResultStatus("PENDING"));
        }
        List<MatchAiRecommendation> activeAndSettled = recommendationRepository.findAllActiveAndSettled();
        List<MatchAiRecommendation> recentPass = recommendationRepository.findTop150ByResultStatusOrderByIdDesc("SKIPPED");
        List<MatchAiRecommendation> combined = new java.util.ArrayList<>(activeAndSettled);
        combined.addAll(recentPass);
        combined.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));
        return ResponseEntity.ok(combined);
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
    public ResponseEntity<AnalystStatsDto> getPerformanceStats() {
        return ResponseEntity.ok(orchestrator.getPerformanceStats());
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
            @RequestBody(required = false) Map<String, String> body) {
        String score = body != null ? body.get("finalScore") : null;
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
     * List recent persistent AI learning lessons.
     */
    @GetMapping("/memories")
    public ResponseEntity<List<pro.datawiki.igaming.analyst.entity.AiLearningMemory>> listMemories() {
        return ResponseEntity.ok(memoryRepository.findTop20ByOrderByCreatedAtDesc());
    }
}
