package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.dto.StrategyPerformanceDto;
import pro.datawiki.igaming.analyst.entity.AiStrategyTemplate;
import pro.datawiki.igaming.analyst.entity.AiStrategyVote;
import pro.datawiki.igaming.analyst.repository.AiStrategyVoteRepository;
import pro.datawiki.igaming.analyst.service.StrategyEngineService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-analyst/strategies")
@RequiredArgsConstructor
@Slf4j
public class AiStrategyController {

    private final StrategyEngineService strategyEngineService;
    private final AiStrategyVoteRepository strategyVoteRepository;

    /**
     * Lists all 7 default ladder strategies and any user custom templates.
     */
    @GetMapping
    public ResponseEntity<List<AiStrategyTemplate>> listStrategies() {
        return ResponseEntity.ok(strategyEngineService.getAllStrategies());
    }

    /**
     * Returns comparative live performance summary across all 7 strategies.
     * Contains won/lost counts, real PnL in RUB, winrate %, and average odds.
     */
    @GetMapping("/performance")
    public ResponseEntity<List<StrategyPerformanceDto>> getPerformance() {
        return ResponseEntity.ok(strategyEngineService.getMultiStrategyPerformance());
    }

    /**
     * Returns votes made by each strategy for a specific match recommendation.
     */
    @GetMapping("/votes")
    public ResponseEntity<List<AiStrategyVote>> getVotesForRecommendation(
            @RequestParam("recommendationId") Long recommendationId
    ) {
        return ResponseEntity.ok(strategyVoteRepository.findByRecommendationId(recommendationId));
    }

    /**
     * Set active strategy for user.
     */
    @PostMapping("/user-active")
    public ResponseEntity<Map<String, Object>> setUserActiveStrategy(
            @RequestParam("userId") Long userId,
            @RequestParam("strategySlug") String strategySlug
    ) {
        strategyEngineService.setUserActiveStrategy(userId, strategySlug);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "userId", userId, "activeStrategy", strategySlug));
    }

    /**
     * Backfills missing strategy votes for all existing recommendations.
     */
    @PostMapping("/backfill")
    public ResponseEntity<Map<String, Object>> backfill() {
        int count = strategyEngineService.backfillAllMissingVotes();
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "backfilledCount", count));
    }

    /**
     * Create custom strategy template.
     */
    @PostMapping("/custom")
    public ResponseEntity<AiStrategyTemplate> createCustomStrategy(@RequestBody AiStrategyTemplate template) {
        AiStrategyTemplate saved = strategyEngineService.createCustomStrategy(template);
        return ResponseEntity.ok(saved);
    }
}
