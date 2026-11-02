package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class IceHockeyTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.ICE_HOCKEY;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int normH = safeInt(hsNode, "normaltime", hs);
        int normA = safeInt(asNode, "normaltime", as);
        int diffNorm = normH - normA;
        int totGoalsReg = normH + normA;
        int totGoalsAll = hs + as;

        double baseTotal = (baseline != null && baseline >= 3.5) ? baseline : 5.5;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p1Tot = p1H + p1A;

        float[] targets = new float[]{
                diffNorm > 0 ? 1.0f : 0.0f,                    // 0: winner_reg_p1
                diffNorm == 0 ? 1.0f : 0.0f,                   // 1: winner_reg_px
                diffNorm < 0 ? 1.0f : 0.0f,                    // 2: winner_reg_p2
                hs > as ? 1.0f : 0.0f,                         // 3: moneyline_ot_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 4: puckline_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 5: puckline_plus_1_5
                totGoalsAll > baseTotal ? 1.0f : 0.0f,         // 6: total_goals_over_baseline
                totGoalsAll > (baseTotal - 1.0) ? 1.0f : 0.0f, // 7: cushion_low
                totGoalsAll < (baseTotal + 1.0) ? 1.0f : 0.0f, // 8: cushion_high
                p1H > p1A ? 1.0f : 0.0f,                       // 9: first_period_p1
                p1Tot > 1.5 ? 1.0f : 0.0f,                     // 10: first_period_over_1_5
                (hs >= 2 && as >= 2) ? 1.0f : 0.0f,            // 11: both_teams_score_2plus
                (hs == 0 || as == 0) ? 1.0f : 0.0f,            // 12: shutout_flag
                totGoalsAll > 7.5 ? 1.0f : 0.0f                // 13: high_scoring_over_7_5
        };

        return new TargetEvaluationResult(targets, 14);
    }

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_reg_p1", "Regulation Time Win Home (P1)"),
                target(1, "winner_reg_px", "Regulation Draw (PX -> Overtime)"),
                target(2, "winner_reg_p2", "Regulation Time Win Away (P2)"),
                target(3, "moneyline_ot_p1", "Moneyline Winner Home (incl. OT/SO)"),
                target(4, "puckline_minus_1_5", "Puckline Home -1.5 (2+ Goal Win)"),
                target(5, "puckline_plus_1_5", "Puckline Away +1.5"),
                target(6, "total_goals_over_5_5", "Total Goals Over Dynamic Baseline (5.5)"),
                target(7, "total_goals_over_4_5", "Cushion Goals Over Baseline - 1.0"),
                target(8, "total_goals_under_6_5", "Cushion Goals Under Baseline + 1.0"),
                target(9, "first_period_p1", "1st Period Winner Home"),
                target(10, "first_period_over_1_5", "1st Period Goals Over 1.5"),
                target(11, "both_teams_score_2plus", "Both Teams Score >= 2 Goals"),
                target(12, "shutout_flag", "Shutout (Zero goals by either team)"),
                target(13, "high_scoring_over_7_5", "High Scoring Festival Over 7.5")
        );
    }
}
