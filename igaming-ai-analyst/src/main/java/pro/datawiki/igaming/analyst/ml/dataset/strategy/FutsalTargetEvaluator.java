package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class FutsalTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.FUTSAL;
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
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 3: handicap_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 4: handicap_plus_1_5
                totGoals > 5.5 ? 1.0f : 0.0f,                  // 5: total_goals_over_5_5
                totGoals > 4.5 ? 1.0f : 0.0f,                  // 6: total_goals_over_4_5
                totGoals < 6.5 ? 1.0f : 0.0f,                  // 7: total_goals_under_6_5
                p1H > p1A ? 1.0f : 0.0f,                       // 8: first_half_p1
                firstHalfTot > 2.5 ? 1.0f : 0.0f,              // 9: first_half_over_2_5
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 10: btts
                (hs >= 2 && as >= 2) ? 1.0f : 0.0f,            // 11: both_score_2plus
                totGoals > 7.5 ? 1.0f : 0.0f,                  // 12: high_scoring_over_7_5
                as == 0 ? 1.0f : 0.0f                          // 13: clean_sheet_home
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "winner_px", "Match Draw (PX)"),
                target(2, "winner_p2", "Match Winner Away (P2)"),
                target(3, "handicap_minus_1_5", "Home Cover -1.5 Goals"),
                target(4, "handicap_plus_1_5", "Away Cover +1.5 Goals"),
                target(5, "total_goals_over_5_5", "Total Goals Over 5.5"),
                target(6, "total_goals_over_4_5", "Cushion Goals Over 4.5"),
                target(7, "total_goals_under_6_5", "Cushion Goals Under 6.5"),
                target(8, "first_half_p1", "1st Half Winner Home"),
                target(9, "first_half_over_2_5", "1st Half Goals Over 2.5"),
                target(10, "btts", "Both Teams to Score (BTTS)"),
                target(11, "both_score_2plus", "Both Teams Score >= 2 Goals"),
                target(12, "high_scoring_over_7_5", "High Scoring Over 7.5"),
                target(13, "clean_sheet_home", "Home Clean Sheet")
        );
    }
}
