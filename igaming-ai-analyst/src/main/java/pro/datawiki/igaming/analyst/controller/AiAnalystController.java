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
     * List recent recommendations.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<MatchAiRecommendation>> listRecommendations(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(recommendationRepository.findByResultStatus(status.toUpperCase()));
        }
        return ResponseEntity.ok(recommendationRepository.findTop50ByOrderByCreatedAtDesc());
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
     * Manually trigger settlement of finished matches.
     */
    @PostMapping("/settle")
    public ResponseEntity<?> triggerSettlement() {
        settlementService.settleFinishedMatches();
        return ResponseEntity.ok(Map.of("status", "SETTLEMENT_COMPLETED"));
    }
}
