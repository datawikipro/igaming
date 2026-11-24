package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class AmericanFootballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.AMERICAN_FOOTBALL;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totPoints = hs + as;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p2H = safeInt(hsNode, "period2", 0);
        int p2A = safeInt(asNode, "period2", 0);
        int fHalfH = p1H + p2H;
        int fHalfA = p1A + p2A;

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 4 ? 1.0f : 0.0f,                  // 1: spread_minus_3_5
                (as - hs) >= -3 ? 1.0f : 0.0f,                 // 2: spread_plus_3_5
                (hs - as) >= 8 ? 1.0f : 0.0f,                  // 3: spread_minus_7_5
                totPoints > 44.5 ? 1.0f : 0.0f,                // 4: total_points_over_44_5
                totPoints > 40.5 ? 1.0f : 0.0f,                // 5: total_points_over_40_5
                totPoints < 48.5 ? 1.0f : 0.0f,                // 6: total_points_under_48_5
                fHalfH > fHalfA ? 1.0f : 0.0f,                 // 7: first_half_winner_p1
                (fHalfH + fHalfA) > 21.5 ? 1.0f : 0.0f,        // 8: first_half_over_21_5
                Math.abs(hs - as) <= 8 ? 1.0f : 0.0f,          // 9: one_score_game
                Math.abs(hs - as) >= 14 ? 1.0f : 0.0f,         // 10: blowout_margin_over_14
                p1H > p1A ? 1.0f : 0.0f,                       // 11: first_quarter_winner_p1
                (p1H + p1A) > 9.5 ? 1.0f : 0.0f,               // 12: first_quarter_over_9_5
                safeInt(hsNode, "overtime", 0) > 0 || safeInt(asNode, "overtime", 0) > 0 ? 1.0f : 0.0f // 13: overtime_flag
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Moneyline Winner Home (P1)"),
                target(1, "spread_minus_3_5", "Home Cover -3.5 Points"),
                target(2, "spread_plus_3_5", "Away Cover +3.5 Points"),
                target(3, "spread_minus_7_5", "Home Cover -7.5 Points"),
                target(4, "total_points_over_44_5", "Total Points Over 44.5"),
                target(5, "total_points_over_40_5", "Cushion Points Over 40.5"),
                target(6, "total_points_under_48_5", "Cushion Points Under 48.5"),
                target(7, "first_half_winner_p1", "1st Half Winner Home"),
                target(8, "first_half_over_21_5", "1st Half Points Over 21.5"),
                target(9, "one_score_game", "One-Score Margin (Diff <= 8)"),
                target(10, "blowout_margin_over_14", "Blowout Game (Diff >= 14)"),
                target(11, "first_quarter_winner_p1", "1st Quarter Winner Home"),
                target(12, "first_quarter_over_9_5", "1st Quarter Points Over 9.5"),
                target(13, "overtime_flag", "Overtime Played")
        );
    }
}
