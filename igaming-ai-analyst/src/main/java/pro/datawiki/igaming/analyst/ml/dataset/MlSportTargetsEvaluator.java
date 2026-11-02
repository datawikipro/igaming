package pro.datawiki.igaming.analyst.ml.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.strategy.*;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Institutional Multi-Task Target Evaluator for all supported sports.
 * Computes 14-16 continuous ground truth binary labels per match directly from
 * official SofaScore raw events and ClickHouse dynamic scoring baselines.
 * Strictly adheres to Rule 9 via polymorphic SportTargetEvaluatorStrategy dispatch.
 */
@Slf4j
@Component
public class MlSportTargetsEvaluator {

    private final ObjectMapper objectMapper;
    private final Map<String, SportTargetEvaluatorStrategy> strategyMap = new ConcurrentHashMap<>();

    public record TargetEvaluationResult(float[] targets, int numTargets) {}

    @Autowired
    public MlSportTargetsEvaluator(ObjectMapper objectMapper, List<SportTargetEvaluatorStrategy> strategies) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        if (strategies != null) {
            for (SportTargetEvaluatorStrategy s : strategies) {
                registerStrategy(s);
            }
        }
    }

    /**
     * Fallback constructor for isolated tests and offline evaluation.
     */
    public MlSportTargetsEvaluator(ObjectMapper objectMapper) {
        this(objectMapper, List.of(
                new BasketballTargetEvaluator(),
                new IceHockeyTargetEvaluator(),
                new FootballTargetEvaluator(),
                new TennisTargetEvaluator(),
                new TableTennisTargetEvaluator(),
                new Cs2TargetEvaluator(),
                new Dota2TargetEvaluator()
        ));
    }

    private void registerStrategy(SportTargetEvaluatorStrategy strategy) {
        Sport sport = strategy.getSupportedSport();
        if (sport != null) {
            if (sport.getCode() != null) strategyMap.put(sport.getCode().toUpperCase(Locale.ROOT), strategy);
            if (sport.getSlug() != null) strategyMap.put(sport.getSlug().toLowerCase(Locale.ROOT), strategy);
            if (sport.getParquetSportSlug() != null) strategyMap.put(sport.getParquetSportSlug().toLowerCase(Locale.ROOT), strategy);
        }
    }

    private SportTargetEvaluatorStrategy resolveStrategy(Sport sport) {
        if (sport == null) return null;
        SportTargetEvaluatorStrategy s = strategyMap.get(sport.getCode().toUpperCase(Locale.ROOT));
        if (s != null) return s;
        s = strategyMap.get(sport.getSlug().toLowerCase(Locale.ROOT));
        if (s != null) return s;
        return strategyMap.get(sport.getParquetSportSlug().toLowerCase(Locale.ROOT));
    }

    /**
     * Evaluates Ground Truth target vector for a finished match by Sport slug/code.
     */
    public TargetEvaluationResult evaluateTargets(String sportSlug, String eventJson,
                                                  Integer homeScore, Integer awayScore,
                                                  Double expectedTotalBaseline) {
        Sport sport = Sports.get(sportSlug);
        return evaluateTargets(sport, eventJson, homeScore, awayScore, expectedTotalBaseline);
    }

    /**
     * Evaluates Ground Truth target vector for a finished match by polymorphic Sport instance.
     */
    public TargetEvaluationResult evaluateTargets(Sport sport, String eventJson,
                                                  Integer homeScore, Integer awayScore,
                                                  Double expectedTotalBaseline) {
        if (homeScore == null || awayScore == null || homeScore < 0 || awayScore < 0) {
            return null;
        }

        SportTargetEvaluatorStrategy strategy = resolveStrategy(sport);
        if (strategy == null) {
            log.debug("No TargetEvaluationStrategy registered for sport [{}]", sport != null ? sport.getCode() : "null");
            return null;
        }

        JsonNode homeScoreNode = null;
        JsonNode awayScoreNode = null;

        if (eventJson != null && !eventJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(eventJson);
                JsonNode eventNode = root.has("event") ? root.get("event") : root;
                if (eventNode.has("homeScore")) homeScoreNode = eventNode.get("homeScore");
                if (eventNode.has("awayScore")) awayScoreNode = eventNode.get("awayScore");
            } catch (Exception e) {
                log.debug("Failed to parse event_json: {}", e.getMessage());
            }
        }

        return strategy.evaluate(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
    }

    /**
     * Returns rich metadata schema for all targets of a sport.
     */
    public List<Map<String, Object>> getTargetSchemas(Sport sport) {
        SportTargetEvaluatorStrategy strategy = resolveStrategy(sport);
        return strategy != null ? strategy.getTargetSchemas() : Collections.emptyList();
    }

    /**
     * Returns rich metadata schema for all targets of a sport slug/code.
     */
    public List<Map<String, Object>> getTargetSchemas(String sportSlug) {
        return getTargetSchemas(Sports.get(sportSlug));
    }
}
