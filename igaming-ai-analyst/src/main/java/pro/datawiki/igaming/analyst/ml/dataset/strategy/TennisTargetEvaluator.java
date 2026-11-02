package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class TennisTargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.TENNIS;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
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

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "set_handicap_minus_1_5", "Home 2:0 in Sets (Clean Win)"),
                target(2, "set_handicap_plus_1_5", "Away Wins >= 1 Set (+1.5)"),
                target(3, "total_sets_over_2_5", "Total Sets Over 2.5 (3-Set Match)"),
                target(4, "total_games_over_21_5", "Total Match Games Over Dynamic Baseline (21.5)"),
                target(5, "total_games_over_19_5", "Cushion Games Over 19.5"),
                target(6, "total_games_under_23_5", "Cushion Games Under 23.5"),
                target(7, "first_set_winner_p1", "1st Set Winner Home"),
                target(8, "first_set_over_9_5", "1st Set Games Over 9.5"),
                target(9, "first_set_tiebreak", "1st Set Tiebreak (7:6)"),
                target(10, "both_win_set", "Both Win >= 1 Set"),
                target(11, "clean_sweep_2_0", "Clean Sweep 2:0"),
                target(12, "games_margin_over_5_5", "Game Spread >= 6 Games (Blowout)"),
                target(13, "deciding_set_clutch", "High Volatility Clutch Match")
        );
    }
}
