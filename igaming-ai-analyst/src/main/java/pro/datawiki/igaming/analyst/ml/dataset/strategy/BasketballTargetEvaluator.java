package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class BasketballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.BASKETBALL;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totPts = hs + as;
        if (totPts < 70) {
            return null;
        }

        double baseTotal = (baseline != null && baseline >= 70.0) ? baseline : 160.5;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p2H = safeInt(hsNode, "period2", 0);
        int p2A = safeInt(asNode, "period2", 0);

        int halfH = p1H + p2H;
        int halfA = p1A + p2A;
        double halfTot = (halfH + halfA > 30) ? (halfH + halfA) : (totPts / 2.0);
        double q1Tot = (p1H + p1A > 15) ? (p1H + p1A) : (totPts / 4.0);

        int diff = hs - as;
        boolean hasOt = (hsNode != null && (hsNode.has("period5") || hsNode.has("overtime")))
                || (asNode != null && (asNode.has("period5") || asNode.has("overtime")));

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                diff >= 6 ? 1.0f : 0.0f,                       // 1: spread_minus_5_5
                diff >= -5 ? 1.0f : 0.0f,                      // 2: spread_plus_5_5
                diff >= 10 ? 1.0f : 0.0f,                      // 3: spread_minus_9_5
                totPts > baseTotal ? 1.0f : 0.0f,              // 4: total_points_over_baseline
                totPts > (baseTotal - 10.0) ? 1.0f : 0.0f,     // 5: total_points_cushion_low
                totPts < (baseTotal + 10.0) ? 1.0f : 0.0f,     // 6: total_points_cushion_high
                halfH > halfA ? 1.0f : 0.0f,                   // 7: first_half_winner_p1
                halfTot > (baseTotal / 2.0 - 1.75) ? 1.0f : 0.0f, // 8: first_half_over
                p1H > p1A ? 1.0f : 0.0f,                       // 9: first_quarter_winner_p1
                q1Tot > (baseTotal / 4.0 - 1.625) ? 1.0f : 0.0f,  // 10: first_quarter_over
                Math.abs(diff) <= 5 ? 1.0f : 0.0f,             // 11: clutch_margin_under_6
                Math.abs(diff) >= 15 ? 1.0f : 0.0f,            // 12: blowout_margin_over_15
                hasOt ? 1.0f : 0.0f                            // 13: overtime_flag
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Moneyline Winner Home (with OT)"),
                target(1, "spread_minus_5_5", "Home Cover -5.5 Points"),
                target(2, "spread_plus_5_5", "Away Cover +5.5 Points"),
                target(3, "spread_minus_9_5", "Home Cover -9.5 Points (Strong Favorite)"),
                target(4, "total_points_over_baseline", "Total Match Points Over Dynamic Baseline"),
                target(5, "total_points_cushion_low", "Cushion Points Over Baseline - 10"),
                target(6, "total_points_cushion_high", "Cushion Points Under Baseline + 10"),
                target(7, "first_half_winner_p1", "1st Half Winner Home"),
                target(8, "first_half_over", "1st Half Total Points Over Half Baseline"),
                target(9, "first_quarter_winner_p1", "1st Quarter Winner Home"),
                target(10, "first_quarter_over", "1st Quarter Points Over Qtr Baseline"),
                target(11, "clutch_margin_under_6", "Clutch Game (Final Margin <= 5)"),
                target(12, "blowout_margin_over_15", "Blowout Game (Final Margin >= 15)"),
                target(13, "overtime_flag", "Game Went to Overtime (Draw in 4Q)")
        );
    }
}
