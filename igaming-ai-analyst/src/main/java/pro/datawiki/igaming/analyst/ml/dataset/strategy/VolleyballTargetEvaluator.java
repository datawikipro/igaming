package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class VolleyballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.VOLLEYBALL;
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
        double totPts = (totPtsH + totPtsA >= 75) ? (totPtsH + totPtsA) : (totSets * 45.0);
        double firstSetPts = (p1H + p1A >= 25) ? (p1H + p1A) : 45.0;

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 1: set_handicap_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 2: set_handicap_plus_1_5
                totSets > 3.5 ? 1.0f : 0.0f,                   // 3: total_sets_over_3_5
                totSets == 5 ? 1.0f : 0.0f,                    // 4: total_sets_over_4_5
                p1H > p1A ? 1.0f : 0.0f,                       // 5: first_set_winner_p1
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 6: both_win_set
                ((hs == 3 && as == 0) || (as == 3 && hs == 0)) ? 1.0f : 0.0f, // 7: clean_sweep_3_0
                totPts > 180.5 ? 1.0f : 0.0f,                  // 8: total_points_over_180_5
                totPts > 160.5 ? 1.0f : 0.0f,                  // 9: total_points_over_160_5
                totPts < 185.5 ? 1.0f : 0.0f,                  // 10: total_points_under_185_5
                firstSetPts > 45.5 ? 1.0f : 0.0f,              // 11: first_set_points_over_45_5
                totSets == 5 ? 1.0f : 0.0f,                    // 12: clutch_5th_set
                totSets > 3.5 ? 1.0f : 0.0f                    // 13: decider_set_played
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "set_handicap_minus_1_5", "Home Cover -1.5 Sets (3:0 or 3:1)"),
                target(2, "set_handicap_plus_1_5", "Away Cover +1.5 Sets"),
                target(3, "total_sets_over_3_5", "Total Sets Over 3.5 (4 or 5 sets)"),
                target(4, "total_sets_over_4_5", "Deciding 5th Set Played (3:2)"),
                target(5, "first_set_winner_p1", "1st Set Winner Home"),
                target(6, "both_win_set", "Both Teams Win >= 1 Set"),
                target(7, "clean_sweep_3_0", "Clean Sweep 3:0 (Home or Away)"),
                target(8, "total_points_over_180_5", "Total Points Over 180.5"),
                target(9, "total_points_over_160_5", "Cushion Points Over 160.5"),
                target(10, "total_points_under_185_5", "Cushion Points Under 185.5"),
                target(11, "first_set_points_over_45_5", "1st Set Points Over 45.5"),
                target(12, "clutch_5th_set", "Clutch 5th Set Decider"),
                target(13, "decider_set_played", "Decider Set Played")
        );
    }
}
