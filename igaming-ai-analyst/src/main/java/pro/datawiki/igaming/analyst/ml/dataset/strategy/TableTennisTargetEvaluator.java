package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class TableTennisTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.TABLE_TENNIS;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totSets = hs + as;
        if (totSets < 3) return null;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p2H = safeInt(hsNode, "period2", 0);
        int p2A = safeInt(asNode, "period2", 0);
        int p3H = safeInt(hsNode, "period3", 0);
        int p3A = safeInt(asNode, "period3", 0);
        int p4H = safeInt(hsNode, "period4", 0);
        int p4A = safeInt(asNode, "period4", 0);
        int p5H = safeInt(hsNode, "period5", 0);
        int p5A = safeInt(asNode, "period5", 0);

        int totPtsH = p1H + p2H + p3H + p4H + p5H;
        int totPtsA = p1A + p2A + p3A + p4A + p5A;
        double totPts = (totPtsH + totPtsA >= 30) ? (totPtsH + totPtsA) : (totSets * 18.5);

        double baseTotal = (baseline != null && baseline >= 50.0) ? baseline : 74.5;
        double firstSetPts = (p1H + p1A > 10) ? (p1H + p1A) : 18.0;

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 1: set_handicap_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 2: set_handicap_plus_1_5
                totSets > 3.5 ? 1.0f : 0.0f,                   // 3: total_sets_over_3_5
                totSets == 5 ? 1.0f : 0.0f,                    // 4: total_sets_over_4_5
                totPts > baseTotal ? 1.0f : 0.0f,              // 5: total_points_over_baseline
                totPts > (baseTotal - 6.0) ? 1.0f : 0.0f,      // 6: cushion_low
                totPts < (baseTotal + 6.0) ? 1.0f : 0.0f,      // 7: cushion_high
                p1H > p1A ? 1.0f : 0.0f,                       // 8: first_set_winner_p1
                firstSetPts > 18.5 ? 1.0f : 0.0f,              // 9: first_set_total_over_18_5
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 10: both_win_set
                ((hs == 3 && as == 0) || (as == 3 && hs == 0)) ? 1.0f : 0.0f, // 11: clean_sweep_3_0
                Math.abs(totPtsH - totPtsA) >= 10 ? 1.0f : 0.0f, // 12: points_margin_over_10
                Math.abs(totPtsH - totPtsA) <= 4 ? 1.0f : 0.0f  // 13: high_entropy_clutch
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "set_handicap_minus_1_5", "Home -1.5 Sets (3:0 or 3:1)"),
                target(2, "set_handicap_plus_1_5", "Away +1.5 Sets"),
                target(3, "total_sets_over_3_5", "Total Sets Over 3.5"),
                target(4, "total_sets_over_4_5", "Deciding 5th Set (3:2 match)"),
                target(5, "total_points_over_74_5", "Total Match Points Over Dynamic Baseline (74.5)"),
                target(6, "total_points_over_68_5", "Cushion Total Points Over 68.5"),
                target(7, "total_points_under_80_5", "Cushion Total Points Under 80.5"),
                target(8, "first_set_winner_p1", "1st Set Winner Home (P1)"),
                target(9, "first_set_total_over_18_5", "1st Set Points Over 18.5"),
                target(10, "both_win_set", "Both Players Win >= 1 Set"),
                target(11, "clean_sweep_3_0", "Clean Sweep 3:0"),
                target(12, "points_margin_over_10", "Total Points Margin >= 10 (Blowout)"),
                target(13, "high_entropy_clutch", "Clutch Game (Points diff <= 4)")
        );
    }
}
