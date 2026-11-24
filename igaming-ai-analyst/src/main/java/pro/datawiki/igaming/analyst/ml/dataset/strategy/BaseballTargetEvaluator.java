package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class BaseballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.BASEBALL;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totRuns = hs + as;

        int f5H = safeInt(hsNode, "period1", 0) + safeInt(hsNode, "period2", 0) + safeInt(hsNode, "period3", 0) + safeInt(hsNode, "period4", 0) + safeInt(hsNode, "period5", 0);
        int f5A = safeInt(asNode, "period1", 0) + safeInt(asNode, "period2", 0) + safeInt(asNode, "period3", 0) + safeInt(asNode, "period4", 0) + safeInt(asNode, "period5", 0);

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 1: runline_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 2: runline_plus_1_5
                totRuns > 8.5 ? 1.0f : 0.0f,                   // 3: total_runs_over_8_5
                totRuns > 7.5 ? 1.0f : 0.0f,                   // 4: total_runs_over_7_5
                totRuns < 9.5 ? 1.0f : 0.0f,                   // 5: total_runs_under_9_5
                f5H > f5A ? 1.0f : 0.0f,                       // 6: first_5_innings_winner_p1
                (f5H + f5A) > 4.5 ? 1.0f : 0.0f,               // 7: first_5_innings_over_4_5
                (hs >= 3 && as >= 3) ? 1.0f : 0.0f,            // 8: both_score_3plus
                Math.abs(hs - as) == 1 ? 1.0f : 0.0f,          // 9: one_run_game
                Math.abs(hs - as) >= 4 ? 1.0f : 0.0f,          // 10: blowout_margin_over_4
                safeInt(hsNode, "extraInnings", 0) > 0 || safeInt(asNode, "extraInnings", 0) > 0 ? 1.0f : 0.0f, // 11: extra_innings_flag
                (hs == 0 || as == 0) ? 1.0f : 0.0f,            // 12: shutout_flag
                totRuns > 11.5 ? 1.0f : 0.0f                   // 13: high_scoring_over_11_5
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Moneyline Winner Home (P1)"),
                target(1, "runline_minus_1_5", "Home Cover -1.5 Runs"),
                target(2, "runline_plus_1_5", "Away Cover +1.5 Runs"),
                target(3, "total_runs_over_8_5", "Total Runs Over 8.5"),
                target(4, "total_runs_over_7_5", "Cushion Runs Over 7.5"),
                target(5, "total_runs_under_9_5", "Cushion Runs Under 9.5"),
                target(6, "first_5_innings_winner_p1", "First 5 Innings Winner Home"),
                target(7, "first_5_innings_over_4_5", "First 5 Innings Runs Over 4.5"),
                target(8, "both_score_3plus", "Both Teams Score >= 3 Runs"),
                target(9, "one_run_game", "1-Run Decider Game"),
                target(10, "blowout_margin_over_4", "Blowout Game (Run Diff >= 4)"),
                target(11, "extra_innings_flag", "Extra Innings Played"),
                target(12, "shutout_flag", "Shutout Game"),
                target(13, "high_scoring_over_11_5", "High Scoring Over 11.5 Runs")
        );
    }
}
