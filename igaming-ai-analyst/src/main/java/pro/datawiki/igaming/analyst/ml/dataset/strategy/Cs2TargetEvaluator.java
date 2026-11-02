package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class Cs2TargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.CS2;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totMaps = hs + as;
        if (totMaps < 1) return null;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p1Tot = p1H + p1A;
        boolean isOt = (p1H >= 13 && p1A >= 12) || (p1A >= 13 && p1H >= 12) || p1Tot >= 25;

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 1: map_handicap_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 2: map_handicap_plus_1_5
                totMaps > 2.5 ? 1.0f : 0.0f,                   // 3: total_maps_over_2_5
                p1H > p1A ? 1.0f : 0.0f,                       // 4: map1_winner_p1
                p1Tot > 21.5 ? 1.0f : 0.0f,                    // 5: map1_rounds_over_21_5
                p1Tot > 19.5 ? 1.0f : 0.0f,                    // 6: map1_rounds_over_19_5
                p1Tot < 23.5 ? 1.0f : 0.0f,                    // 7: map1_rounds_under_23_5
                isOt ? 1.0f : 0.0f,                            // 8: map1_overtime_flag
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 9: both_win_map
                (p1H - p1A) >= 4 ? 1.0f : 0.0f,                // 10: map1_handicap_minus_3_5
                ((hs == 2 && as == 0) || (as == 2 && hs == 0)) ? 1.0f : 0.0f, // 11: clean_sweep_2_0
                totMaps == 3 ? 1.0f : 0.0f,                    // 12: decider_map_played
                Math.abs(p1H - p1A) <= 3 ? 1.0f : 0.0f         // 13: high_entropy_clutch
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "map_handicap_minus_1_5", "Home -1.5 Maps (Clean Sweep)"),
                target(2, "map_handicap_plus_1_5", "Away Wins >= 1 Map (+1.5)"),
                target(3, "total_maps_over_2_5", "Total Maps Over 2.5 (3-Map Decider)"),
                target(4, "map1_winner_p1", "Map 1 Winner Home"),
                target(5, "map1_rounds_over_21_5", "Map 1 Total Rounds Over 21.5"),
                target(6, "map1_rounds_over_19_5", "Cushion Map 1 Rounds Over 19.5"),
                target(7, "map1_rounds_under_23_5", "Cushion Map 1 Rounds Under 23.5"),
                target(8, "map1_overtime_flag", "Map 1 Went to Overtime (12-12)"),
                target(9, "both_win_map", "Both Teams Win >= 1 Map"),
                target(10, "map1_handicap_minus_3_5", "Map 1 Rounds P1 -3.5"),
                target(11, "clean_sweep_2_0", "Clean Sweep 2:0"),
                target(12, "decider_map_played", "Decider Map 3 Played"),
                target(13, "high_entropy_clutch", "Clutch Map 1 (Rounds Diff <= 3)")
        );
    }
}
