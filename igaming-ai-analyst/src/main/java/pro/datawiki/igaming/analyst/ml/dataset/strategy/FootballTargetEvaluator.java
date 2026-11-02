package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class FootballTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.FOOTBALL;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int normH = safeInt(hsNode, "normaltime", hs);
        int normA = safeInt(asNode, "normaltime", as);
        int totGoals = normH + normA;

        double baseTotal = (baseline != null && baseline >= 1.5) ? baseline : 2.5;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p1Tot = p1H + p1A;
        float btts = (normH > 0 && normA > 0) ? 1.0f : 0.0f;

        float[] targets = new float[]{
                normH > normA ? 1.0f : 0.0f,                   // 0: winner_1x2_p1
                normH == normA ? 1.0f : 0.0f,                  // 1: winner_1x2_px
                normH < normA ? 1.0f : 0.0f,                   // 2: winner_1x2_p2
                normH >= normA ? 1.0f : 0.0f,                  // 3: double_chance_1x
                normA >= normH ? 1.0f : 0.0f,                  // 4: double_chance_x2
                (normH - normA) >= 2 ? 1.0f : 0.0f,            // 5: handicap_minus_1
                (normA - normH) >= 0 ? 1.0f : 0.0f,            // 6: handicap_plus_1
                totGoals > baseTotal ? 1.0f : 0.0f,            // 7: total_goals_over_baseline
                totGoals > (baseTotal - 1.0) ? 1.0f : 0.0f,    // 8: cushion_low
                totGoals < (baseTotal + 1.0) ? 1.0f : 0.0f,    // 9: cushion_high
                btts,                                          // 10: btts
                (btts > 0 && totGoals > 2.5) ? 1.0f : 0.0f,    // 11: btts_and_over_2_5
                p1H > p1A ? 1.0f : 0.0f,                       // 12: first_half_p1
                p1Tot > 0.5 ? 1.0f : 0.0f,                     // 13: first_half_over_0_5
                p1Tot > 1.5 ? 1.0f : 0.0f,                     // 14: first_half_over_1_5
                normA == 0 ? 1.0f : 0.0f                       // 15: clean_sheet_home
        };

        return new TargetEvaluationResult(targets, 16);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_1x2_p1", "1X2 Win Home (P1)"),
                target(1, "winner_1x2_px", "1X2 Draw (PX)"),
                target(2, "winner_1x2_p2", "1X2 Win Away (P2)"),
                target(3, "double_chance_1x", "Double Chance 1X"),
                target(4, "double_chance_x2", "Double Chance X2"),
                target(5, "handicap_minus_1", "Handicap Home -1.0 (2+ Goals)"),
                target(6, "handicap_plus_1", "Handicap Away +1.0"),
                target(7, "total_goals_over_2_5", "Total Goals Over Dynamic Baseline (2.5)"),
                target(8, "total_goals_over_1_5", "Cushion Goals Over 1.5"),
                target(9, "total_goals_under_3_5", "Cushion Goals Under 3.5"),
                target(10, "btts", "Both Teams to Score (BTTS Yes)"),
                target(11, "btts_and_over_2_5", "Combo: BTTS Yes + Over 2.5"),
                target(12, "first_half_p1", "1st Half Winner Home"),
                target(13, "first_half_over_0_5", "1st Half Goals Over 0.5"),
                target(14, "first_half_over_1_5", "1st Half Goals Over 1.5"),
                target(15, "clean_sheet_home", "Home Clean Sheet (0 goals conceded)")
        );
    }
}
