package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class HandballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.HANDBALL;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totGoals = hs + as;
        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int firstHalfTot = p1H + p1A;

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                hs == as ? 1.0f : 0.0f,                        // 1: winner_px
                as > hs ? 1.0f : 0.0f,                         // 2: winner_p2
                (hs - as) >= 3 ? 1.0f : 0.0f,                  // 3: handicap_minus_2_5
                (as - hs) >= -2 ? 1.0f : 0.0f,                 // 4: handicap_plus_2_5
                totGoals > 53.5 ? 1.0f : 0.0f,                 // 5: total_goals_over_53_5
                totGoals > 49.5 ? 1.0f : 0.0f,                 // 6: total_goals_over_49_5
                totGoals < 57.5 ? 1.0f : 0.0f,                 // 7: total_goals_under_57_5
                p1H > p1A ? 1.0f : 0.0f,                       // 8: first_half_p1
                firstHalfTot > 25.5 ? 1.0f : 0.0f,             // 9: first_half_over_25_5
                (hs >= 25 && as >= 25) ? 1.0f : 0.0f,          // 10: both_score_25plus
                totGoals > 60.5 ? 1.0f : 0.0f,                 // 11: high_scoring_over_60_5
                Math.abs(hs - as) <= 2 ? 1.0f : 0.0f,          // 12: clutch_margin_under_3
                Math.abs(hs - as) >= 6 ? 1.0f : 0.0f           // 13: blowout_margin_over_6
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "winner_px", "Match Draw (PX)"),
                target(2, "winner_p2", "Match Winner Away (P2)"),
                target(3, "handicap_minus_2_5", "Home Cover -2.5 Goals"),
                target(4, "handicap_plus_2_5", "Away Cover +2.5 Goals"),
                target(5, "total_goals_over_53_5", "Total Goals Over 53.5"),
                target(6, "total_goals_over_49_5", "Cushion Goals Over 49.5"),
                target(7, "total_goals_under_57_5", "Cushion Goals Under 57.5"),
                target(8, "first_half_p1", "1st Half Winner Home"),
                target(9, "first_half_over_25_5", "1st Half Goals Over 25.5"),
                target(10, "both_score_25plus", "Both Teams Score >= 25 Goals"),
                target(11, "high_scoring_over_60_5", "High Scoring Festival Over 60.5"),
                target(12, "clutch_margin_under_3", "Clutch Game (Goal Diff <= 2)"),
                target(13, "blowout_margin_over_6", "Blowout Game (Goal Diff >= 6)")
        );
    }
}
