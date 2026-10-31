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
     * Optionally filtered by sportSlug (e.g. football, basketball, hockey, tennis).
     * Contains won/lost counts, real PnL in RUB, winrate %, and average odds.
     */
    @GetMapping("/performance")
    public ResponseEntity<List<StrategyPerformanceDto>> getPerformance(
            @RequestParam(value = "sport", required = false) String sport
    ) {
        return ResponseEntity.ok(strategyEngineService.getMultiStrategyPerformance(sport));
    }

    /**
     * Returns comparative performance breakdown for all 7 strategies partitioned by sport.
     */
    @GetMapping("/performance/by-sport")
    public ResponseEntity<Map<String, List<StrategyPerformanceDto>>> getPerformanceBySport() {
        return ResponseEntity.ok(strategyEngineService.getMultiStrategyPerformanceBySport());
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
     * Complete recalculation of strategy votes across all recommendations under current risk rules.
     * When forceAll is true, deletes all previous votes and regenerates them fresh.
     */
    @PostMapping("/recalculate")
    public ResponseEntity<Map<String, Object>> recalculate(
            @RequestParam(value = "forceAll", defaultValue = "false") boolean forceAll
    ) {
        Map<String, Object> result = strategyEngineService.recalculateAllStrategyVotes(forceAll);
        return ResponseEntity.ok(result);
    }

    /**
     * Backfills missing strategy votes for all existing recommendations.
     */
    @PostMapping("/backfill")
    public ResponseEntity<Map<String, Object>> backfill() {
        Map<String, Object> result = strategyEngineService.recalculateAllStrategyVotes(false);
        return ResponseEntity.ok(result);
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
