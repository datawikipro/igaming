package pro.datawiki.igaming.analyst.ml.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Institutional Multi-Task Target Evaluator for all 7 supported sports.
 * Computes 14-16 continuous ground truth binary labels per match directly from
 * official SofaScore raw events and ClickHouse dynamic scoring baselines.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MlSportTargetsEvaluator {

    private final ObjectMapper objectMapper;

    public record TargetEvaluationResult(float[] targets, int numTargets) {}

    /**
     * Evaluates Ground Truth target vector for a finished match.
     *
     * @param sportSlug Standard sport slug (e.g. "basketball", "ice-hockey", "football")
     * @param eventJson Raw SofaScore event JSON
     * @param homeScore Final home score (current / display)
     * @param awayScore Final away score (current / display)
     * @param expectedTotalBaseline Baseline total from ClickHouse DDS (or null for default)
     * @return TargetEvaluationResult or null if match is invalid/incomplete
     */
    public TargetEvaluationResult evaluateTargets(String sportSlug, String eventJson,
                                                  Integer homeScore, Integer awayScore,
                                                  Double expectedTotalBaseline) {
        if (homeScore == null || awayScore == null || homeScore < 0 || awayScore < 0) {
            return null;
        }

        JsonNode homeScoreNode = null;
        JsonNode awayScoreNode = null;

        if (eventJson != null && !eventJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(eventJson);
                JsonNode eventNode = root.has("event") ? root.get("event") : root;
                if (eventNode.has("homeScore")) homeScoreNode = eventNode.get("homeScore");
                if (eventNode.has("awayScore")) awayScoreNode = eventNode.get("awayScore");
            } catch (Exception e) {
                log.debug("Failed to parse event_json: {}", e.getMessage());
            }
        }

        String sport = normalizeSport(sportSlug);

        return switch (sport) {
            case "basketball" -> evaluateBasketball(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
            case "ice-hockey" -> evaluateIceHockey(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
            case "football" -> evaluateFootball(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
            case "tennis" -> evaluateTennis(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
            case "table-tennis" -> evaluateTableTennis(homeScore, awayScore, homeScoreNode, awayScoreNode, expectedTotalBaseline);
            case "cs2" -> evaluateCs2(homeScore, awayScore, homeScoreNode, awayScoreNode);
            case "dota2" -> evaluateDota2(homeScore, awayScore, homeScoreNode, awayScoreNode);
            default -> null;
        };
    }

    private TargetEvaluationResult evaluateBasketball(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totPts = hs + as;
        // Strict domain validation: Basketball matches must have realistic total points
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

    private TargetEvaluationResult evaluateIceHockey(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
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

    private TargetEvaluationResult evaluateFootball(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
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

    private TargetEvaluationResult evaluateTennis(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
        int totSets = hs + as;
        if (totSets < 2) return null;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);
        int p2H = safeInt(hsNode, "period2", 0);
        int p2A = safeInt(asNode, "period2", 0);
        int p3H = safeInt(hsNode, "period3", 0);
        int p3A = safeInt(asNode, "period3", 0);

        int totGamesH = p1H + p2H + p3H;
        int totGamesA = p1A + p2A + p3A;
        double totGames = (totGamesH + totGamesA >= 12) ? (totGamesH + totGamesA) : (totSets * 9.5);

        double baseTotal = (baseline != null && baseline >= 16.0) ? baseline : 21.5;

        double firstSetGames = (p1H + p1A >= 6) ? (p1H + p1A) : 9.0;
        boolean tbFlag = (p1H == 7 && p1A == 6) || (p1A == 7 && p1H == 6)
                || (hsNode != null && hsNode.has("period1TieBreak"));

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs == 2 && as == 0) ? 1.0f : 0.0f,            // 1: set_handicap_minus_1_5
                as >= 1 ? 1.0f : 0.0f,                         // 2: set_handicap_plus_1_5
                totSets > 2.5 ? 1.0f : 0.0f,                   // 3: total_sets_over_2_5
                totGames > baseTotal ? 1.0f : 0.0f,            // 4: total_games_over_baseline
                totGames > (baseTotal - 2.0) ? 1.0f : 0.0f,    // 5: cushion_low
                totGames < (baseTotal + 2.0) ? 1.0f : 0.0f,    // 6: cushion_high
                p1H > p1A ? 1.0f : 0.0f,                       // 7: first_set_winner_p1
                firstSetGames > 9.5 ? 1.0f : 0.0f,             // 8: first_set_over_9_5
                tbFlag ? 1.0f : 0.0f,                          // 9: first_set_tiebreak
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 10: both_win_set
                ((hs == 2 && as == 0) || (as == 2 && hs == 0)) ? 1.0f : 0.0f, // 11: clean_sweep_2_0
                Math.abs(totGamesH - totGamesA) >= 6 ? 1.0f : 0.0f, // 12: games_margin_over_5_5
                (totSets == 3 && Math.abs(totGamesH - totGamesA) <= 2) ? 1.0f : 0.0f // 13: deciding_set_clutch
        };

        return new TargetEvaluationResult(targets, 14);
    }

    private TargetEvaluationResult evaluateTableTennis(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
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

    private TargetEvaluationResult evaluateCs2(int hs, int as, JsonNode hsNode, JsonNode asNode) {
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

    private TargetEvaluationResult evaluateDota2(int hs, int as, JsonNode hsNode, JsonNode asNode) {
        int totMaps = hs + as;
        if (totMaps < 1) return null;

        int p1H = safeInt(hsNode, "period1", 0);
        int p1A = safeInt(asNode, "period1", 0);

        float[] targets = new float[]{
                hs > as ? 1.0f : 0.0f,                         // 0: winner_p1
                (hs - as) >= 2 ? 1.0f : 0.0f,                  // 1: map_handicap_minus_1_5
                (as - hs) >= -1 ? 1.0f : 0.0f,                 // 2: map_handicap_plus_1_5
                totMaps > 2.5 ? 1.0f : 0.0f,                   // 3: total_maps_over_2_5
                p1H > p1A ? 1.0f : 0.0f,                       // 4: map1_winner_p1
                (hs >= 1 && as >= 1) ? 1.0f : 0.0f,            // 5: both_win_map
                ((hs == 2 && as == 0) || (as == 2 && hs == 0)) ? 1.0f : 0.0f, // 6: clean_sweep_2_0
                totMaps == 3 ? 1.0f : 0.0f,                    // 7: decider_map_played
                hs >= 1 ? 1.0f : 0.0f,                         // 8: home_wins_at_least_1_map
                as >= 1 ? 1.0f : 0.0f,                         // 9: away_wins_at_least_1_map
                (hs == 2 && as == 1) ? 1.0f : 0.0f,            // 10: clutch_2_1_home_win
                (hs == 1 && as == 2) ? 1.0f : 0.0f,            // 11: clutch_1_2_away_win
                (hs == 2 && as == 0) ? 1.0f : 0.0f,            // 12: blowout_2_0_home
                (hs == 0 && as == 2) ? 1.0f : 0.0f             // 13: blowout_0_2_away
        };

        return new TargetEvaluationResult(targets, 14);
    }

    private int safeInt(JsonNode node, String field, int defaultVal) {
        if (node == null || !node.has(field)) return defaultVal;
        JsonNode f = node.get(field);
        if (f.isInt() || f.isLong()) return f.intValue();
        try {
            return Integer.parseInt(f.asText().trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private String normalizeSport(String sport) {
        if (sport == null) return "football";
        String s = sport.trim().toLowerCase().replace('_', '-');
        if ("soccer".equals(s)) return "football";
        if ("hockey".equals(s)) return "ice-hockey";
        return s;
    }
}
