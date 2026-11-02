package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.dataset.MlSportTargetsEvaluator.TargetEvaluationResult;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.List;
import java.util.Map;

@Component
public class Dota2TargetEvaluator extends AbstractSportTargetEvaluator {

    @Override
    public Sport getSupportedSport() {
        return Sports.DOTA2;
    }

    @Override
    public TargetEvaluationResult evaluate(int hs, int as, JsonNode hsNode, JsonNode asNode, Double baseline) {
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

    @Override
    public List<Map<String, Object>> getTargetSchemas() {
        return List.of(
                target(0, "winner_p1", "Match Winner Home (P1)"),
                target(1, "map_handicap_minus_1_5", "Home -1.5 Maps (Clean Sweep)"),
                target(2, "map_handicap_plus_1_5", "Away Wins >= 1 Map (+1.5)"),
                target(3, "total_maps_over_2_5", "Total Maps Over 2.5 (3-Map Decider)"),
                target(4, "map1_winner_p1", "Map 1 Winner Home"),
                target(5, "both_win_map", "Both Teams Win >= 1 Map"),
                target(6, "clean_sweep_2_0", "Clean Sweep 2:0"),
                target(7, "decider_map_played", "Decider Map 3 Played"),
                target(8, "home_wins_at_least_1_map", "Home Takes >= 1 Map"),
                target(9, "away_wins_at_least_1_map", "Away Takes >= 1 Map"),
                target(10, "clutch_2_1_home_win", "2:1 Series Win Home"),
                target(11, "clutch_1_2_away_win", "1:2 Series Win Away"),
                target(12, "blowout_2_0_home", "2:0 Blowout Home"),
                target(13, "blowout_0_2_away", "0:2 Blowout Away")
        );
    }
}
