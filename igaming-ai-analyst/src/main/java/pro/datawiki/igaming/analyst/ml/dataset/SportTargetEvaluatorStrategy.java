package pro.datawiki.igaming.analyst.ml.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;

/**
 * Polymorphic strategy for evaluating multi-task ground truth targets
 * and metadata schemas per Sport discipline, strictly obeying Rule 9.
 */
public interface SportTargetEvaluatorStrategy {

    /**
     * Primary Sport supported by this evaluator strategy.
     */
    Sport getSupportedSport();

    /**
     * Evaluates Ground Truth target vector for a finished match of this sport.
     */
    MlSportTargetsEvaluator.TargetEvaluationResult evaluate(
            int homeScore, int awayScore, JsonNode homeScoreNode, JsonNode awayScoreNode, Double baseline);

    /**
     * Returns rich metadata schema for all targets produced by this sport.
     */
    List<Map<String, Object>> getTargetSchemas();
}
