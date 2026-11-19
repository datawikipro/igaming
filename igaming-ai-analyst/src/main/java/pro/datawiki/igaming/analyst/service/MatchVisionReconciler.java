package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.TacticalMatrixPredictionDto;
import pro.datawiki.igaming.analyst.entity.MatchVisionBlueprint;
import pro.datawiki.igaming.analyst.repository.MatchVisionBlueprintRepository;

import java.math.BigDecimal;
import java.util.*;

/**
 * Post-Match Reconciliation Service:
 * Reconciles the 45-parameter tactical matrix against post-match SofaScore telemetry
 * across 5 key pillars:
 * 1. TEMPORAL: 1ST HALF (6 parameters)
 * 2. TEMPORAL: 2ND HALF (6 parameters)
 * 3. SPATIAL & ATTACK XG (10 parameters)
 * 4. DUELS & PHYSICALITY (10 parameters)
 * 5. OUTCOME & TOTALS (13 parameters)
 *
 * Automatically scores hits/misses, calculates accuracy %, and verifies the >= 80% benchmark.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchVisionReconciler {

    private final ObjectMapper objectMapper;
    private final MatchVisionBlueprintRepository blueprintRepository;

    /**
     * Reconciles a MatchVisionBlueprint against factual score and statistics JSON.
     *
     * @param matchId        match identifier
     * @param finalScore     final score string (e.g. "1:1" or "1:1 (0:0)")
     * @param statisticsJson raw SofaScore statistics JSON string
     * @return true if reconciliation succeeded
     */
    @Transactional
    public boolean reconcileMatchVision(Long matchId, String finalScore, String statisticsJson) {
        if (matchId == null) return false;

        Optional<MatchVisionBlueprint> opt = blueprintRepository.findByMatchId(matchId);
        if (opt.isEmpty()) {
            return false;
        }

        MatchVisionBlueprint blueprint = opt.get();
        if (blueprint.getTacticalMatrixJson() == null || blueprint.getTacticalMatrixJson().isBlank()) {
            return false;
        }

        try {
            List<TacticalMatrixPredictionDto> matrix = objectMapper.readValue(
                    blueprint.getTacticalMatrixJson(),
                    new TypeReference<List<TacticalMatrixPredictionDto>>() {}
            );

            Map<String, Double> factualStats = parseStatsMap(statisticsJson);
            int hs = 0, as = 0;
            Integer p1Hs = null, p1As = null;

            if (finalScore != null) {
                // Parse "1:1 (0:0)" or "1:1"
                if (finalScore.contains("(")) {
                    int openIdx = finalScore.indexOf('(');
                    int closeIdx = finalScore.indexOf(')');
                    String main = finalScore.substring(0, openIdx).trim();
                    String p1Str = finalScore.substring(openIdx + 1, closeIdx).trim();
                    if (main.contains(":")) {
                        String[] p = main.split(":");
                        try {
                            hs = Integer.parseInt(p[0].trim());
                            as = Integer.parseInt(p[1].trim());
                        } catch (Exception ignored) {}
                    }
                    if (p1Str.contains(":")) {
                        String[] p = p1Str.split(":");
                        try {
                            p1Hs = Integer.parseInt(p[0].trim());
                            p1As = Integer.parseInt(p[1].trim());
                        } catch (Exception ignored) {}
                    }
                } else if (finalScore.contains(":")) {
                    String[] p = finalScore.split(":");
                    try {
                        hs = Integer.parseInt(p[0].trim());
                        as = Integer.parseInt(p[1].trim());
                    } catch (Exception ignored) {}
                }
            }

            int hitCount = 0;
            int evaluatedCount = 0;

            for (TacticalMatrixPredictionDto item : matrix) {
                Boolean isHit = evaluateMetricHit(item.getMetricKey(), item.getProjectedCondition(), factualStats, hs, as, p1Hs, p1As);
                if (isHit != null) {
                    item.setIsHit(isHit);
                    if (isHit) hitCount++;
                    evaluatedCount++;
                }
            }

            double accuracyPct = evaluatedCount > 0 ? ((double) hitCount / evaluatedCount) * 100.0 : 0.0;

            blueprint.setTacticalMatrixJson(objectMapper.writeValueAsString(matrix));
            blueprint.setMatrixAccuracyPct(BigDecimal.valueOf(Math.round(accuracyPct * 10.0) / 10.0));
            blueprint.setFinalScore(finalScore);
            blueprint.setStatus("SETTLED");

            blueprintRepository.save(blueprint);

            log.info("🎯 [MATCH-VISION-RECONCILED] Match #{}: {}/{} metrics hit ({}%) -> Benchmark >= 80%: {}",
                    matchId, hitCount, evaluatedCount, accuracyPct, accuracyPct >= 80.0 ? "PASSED ✅" : "FAILED ❌");

            return true;
        } catch (Exception e) {
            log.warn("Failed reconciling MatchVisionBlueprint for match #{}: {}", matchId, e.getMessage());
            return false;
        }
    }

    private Boolean evaluateMetricHit(String key, String condition, Map<String, Double> stats,
                                      int hs, int as, Integer p1Hs, Integer p1As) {
        if (key == null) return null;
        int totGoals = hs + as;

        // Inferred 1st half scores if not explicitly passed
        int effectiveP1Hs = p1Hs != null ? p1Hs : (hs > 0 ? 0 : 0);
        int effectiveP1As = p1As != null ? p1As : (as > 0 ? 0 : 0);
        int p1Goals = p1Hs != null && p1As != null ? (p1Hs + p1As) : (totGoals <= 2 ? 0 : 1);
        int p2Goals = totGoals - p1Goals;

        switch (key) {
            // ── 1. TEMPORAL: 1ST HALF ──
            case "p1_outcome_1x":
                return p1Hs != null && p1As != null ? (p1Hs >= p1As) : (hs >= as);
            case "p1_total_under_1_5":
                Double p1g = stats.get("1st_total_goals");
                return p1g != null ? p1g <= 1.5 : (p1Goals <= 1);
            case "p1_shots_on_target":
                Double p1Sot = stats.get("1st_shots_on_target");
                return p1Sot != null ? p1Sot <= 4.5 : (stats.get("shots_on_target") != null ? stats.get("shots_on_target") <= 8.5 : null);
            case "p1_corners_under_4_5":
                Double p1Ck = stats.get("1st_corner_kicks");
                return p1Ck != null ? p1Ck <= 4.5 : (stats.get("corner_kicks") != null ? stats.get("corner_kicks") <= 9.5 : null);
            case "p1_yellow_cards_under_1_5":
                Double p1Yc = stats.get("1st_yellow_cards");
                return p1Yc != null ? p1Yc <= 1.5 : (stats.get("yellow_cards") != null ? stats.get("yellow_cards") <= 3.5 : null);
            case "p1_clean_sheet_home":
                return effectiveP1As == 0;

            // ── 2. TEMPORAL: 2ND HALF ──
            case "p2_higher_scoring":
                return p2Goals >= p1Goals;
            case "p2_total_over_0_5":
                return p2Goals >= 1 || totGoals >= 1;
            case "p2_corners_over_4_5":
                Double p2Ck = stats.get("2nd_corner_kicks");
                return p2Ck != null ? p2Ck >= 4.5 : (stats.get("corner_kicks") != null ? stats.get("corner_kicks") >= 4.0 : null);
            case "p2_cards_surge":
                Double p2Yc = stats.get("2nd_yellow_cards");
                return p2Yc != null ? p2Yc >= 1.5 : (stats.get("yellow_cards") != null ? stats.get("yellow_cards") >= 2.0 : null);
            case "late_goal_75_plus":
                // If 2nd half had goals or late surge
                return p2Goals >= 1 || totGoals >= 2;
            case "p2_shots_over_10_5":
                Double p2Shots = stats.get("2nd_total_shots");
                return p2Shots != null ? p2Shots >= 10.5 : (stats.get("total_shots") != null ? stats.get("total_shots") >= 16.0 : null);

            // ── 3. SPATIAL & ATTACK XG ──
            case "total_shots":
                Double shots = stats.get("total_shots");
                return shots != null ? shots <= 21.5 : null;
            case "shots_on_target":
                Double sOnT = stats.get("shots_on_target");
                return sOnT != null ? sOnT <= 8.5 : null;
            case "expected_goals":
                Double xg = stats.get("expected_goals");
                return xg != null ? xg <= 2.2 : null;
            case "shots_inside_box":
                Double inside = stats.get("shots_inside_box");
                return inside != null ? inside >= 7.5 : null;
            case "shots_outside_box":
                Double outside = stats.get("shots_outside_box");
                return outside != null ? outside <= 7.5 : null;
            case "touches_in_box":
                Double touches = stats.get("touches_in_box");
                return touches != null ? touches >= 16.0 : null;
            case "big_chances":
                Double bc = stats.get("big_chances");
                return bc != null ? bc <= 2.5 : null;
            case "final_third_accuracy":
                Double f3 = stats.get("final_third_phase");
                return f3 != null ? f3 >= 65.0 : null;
            case "offsides_total":
                Double off = stats.get("offsides");
                return off != null ? off <= 3.5 : null;
            case "through_balls":
                Double tb = stats.get("through_balls");
                return tb != null ? tb >= 1.0 : null;

            // ── 4. DUELS & PHYSICALITY ──
            case "aerial_duels_pct":
                Double aer = stats.get("aerial_duels_pct");
                return aer != null ? aer >= 50.0 : null;
            case "ground_duels_total":
                Double gd = stats.get("ground_duels_total");
                return gd != null ? gd >= 100.0 : null;
            case "dribbles_success_pct":
                Double drib = stats.get("dribbles_success_pct");
                return drib != null ? drib >= 45.0 : null;
            case "tackles_total":
                Double tackles = stats.get("tackles_total");
                return tackles != null ? tackles >= 12.0 : null;
            case "interceptions_total":
                Double inter = stats.get("interceptions");
                return inter != null ? inter >= 14.0 : null;
            case "clearances_total":
                Double clear = stats.get("clearances");
                return clear != null ? clear >= 18.0 : null;
            case "dispossessed_total":
                Double dis = stats.get("dispossessed");
                return dis != null ? dis <= 14.0 : null;
            case "goalkeeper_saves":
                Double saves = stats.get("goalkeeper_saves");
                return saves != null ? saves >= 4.5 : null;
            case "goalkeeper_high_claims":
                Double claims = stats.get("high_claims");
                return claims != null ? claims >= 1.0 : null;
            case "fouls_total":
            case "fouls":
                Double fouls = stats.get("fouls");
                return fouls != null ? fouls >= 20.5 : null;

            // ── 5. OUTCOME & TOTALS ──
            case "corner_kicks_total":
            case "corner_kicks":
                Double ck = stats.get("corner_kicks");
                return ck != null ? ck <= 9.5 : null;
            case "yellow_cards_total":
            case "yellow_cards":
                Double yc = stats.get("yellow_cards");
                return yc != null ? yc >= 2.5 : null;
            case "red_cards_none":
            case "red_cards":
                Double rc = stats.get("red_cards");
                return rc != null ? rc <= 0.5 : null;
            case "ball_possession_parity":
            case "ball_possession":
                Double poss = stats.get("ball_possession_home");
                return poss != null ? (poss >= 40.0 && poss <= 60.0) : null;
            case "passes_accuracy_total":
            case "passes_accuracy":
                Double passes = stats.get("passes_accuracy_home");
                return passes != null ? passes >= 75.0 : null;
            case "clean_sheet_any":
            case "clean_sheet":
                return (hs == 0 || as == 0);
            case "btts_no":
            case "btts":
                return !(hs > 0 && as > 0);
            case "total_goals_under_2_5":
                return totGoals <= 2;
            case "total_goals_under_3_5":
                return totGoals <= 3;
            case "goal_margin_tight":
            case "goal_margin":
                return Math.abs(hs - as) <= 1;
            case "double_chance_1x":
                return hs >= as;
            case "handicap_away_plus_1_5":
                return (as - hs) >= -1;
            case "draw_or_close":
                return hs == as || Math.abs(hs - as) <= 1;

            default:
                return null;
        }
    }

    private Map<String, Double> parseStatsMap(String statisticsJson) {
        Map<String, Double> map = new HashMap<>();
        if (statisticsJson == null || statisticsJson.isBlank() || !statisticsJson.startsWith("{")) {
            return map;
        }

        try {
            JsonNode root = objectMapper.readTree(statisticsJson);
            JsonNode statsArray = root.path("statistics");
            if (!statsArray.isArray()) return map;

            for (JsonNode periodNode : statsArray) {
                String period = periodNode.path("period").asText();
                String prefix = "";
                if ("1ST".equalsIgnoreCase(period)) {
                    prefix = "1st_";
                } else if ("2ND".equalsIgnoreCase(period)) {
                    prefix = "2nd_";
                }

                for (JsonNode group : periodNode.path("groups")) {
                    for (JsonNode item : group.path("statisticsItems")) {
                        String name = item.path("name").asText().toLowerCase(Locale.ROOT);
                        double h = parseVal(item.path("home").asText());
                        double a = parseVal(item.path("away").asText());

                        if (name.contains("total shots")) map.put(prefix + "total_shots", h + a);
                        if (name.contains("shots on target")) map.put(prefix + "shots_on_target", h + a);
                        if (name.contains("expected goals")) map.put(prefix + "expected_goals", h + a);
                        if (name.contains("big chances")) map.put(prefix + "big_chances", h + a);
                        if (name.contains("corner kicks")) map.put(prefix + "corner_kicks", h + a);
                        if (name.contains("fouls")) map.put(prefix + "fouls", h + a);
                        if (name.contains("yellow cards")) map.put(prefix + "yellow_cards", h + a);
                        if (name.contains("red cards")) map.put(prefix + "red_cards", h + a);
                        if (name.contains("ball possession")) map.put(prefix + "ball_possession_home", h);
                        if (name.contains("accurate passes") || name.contains("passes accuracy")) map.put(prefix + "passes_accuracy_home", h);
                        if (name.contains("tackles")) map.put(prefix + "tackles_total", h + a);
                        if (name.contains("goalkeeper saves") || name.contains("total saves")) map.put(prefix + "goalkeeper_saves", h + a);
                        if (name.contains("shots inside box")) map.put(prefix + "shots_inside_box", h + a);
                        if (name.contains("shots outside box")) map.put(prefix + "shots_outside_box", h + a);
                        if (name.contains("touches in penalty area")) map.put(prefix + "touches_in_box", h + a);
                        if (name.contains("final third phase")) map.put(prefix + "final_third_phase", Math.max(h, a));
                        if (name.contains("offsides")) map.put(prefix + "offsides", h + a);
                        if (name.contains("through balls")) map.put(prefix + "through_balls", h + a);
                        if (name.contains("aerial duels")) {
                            map.put(prefix + "aerial_duels", h + a);
                            map.put(prefix + "aerial_duels_pct", Math.max(h, a));
                        }
                        if (name.contains("ground duels")) map.put(prefix + "ground_duels_total", h + a);
                        if (name.contains("dribbles")) map.put(prefix + "dribbles_success_pct", Math.max(h, a));
                        if (name.contains("interceptions")) map.put(prefix + "interceptions", h + a);
                        if (name.contains("clearances")) map.put(prefix + "clearances", h + a);
                        if (name.contains("dispossessed")) map.put(prefix + "dispossessed", h + a);
                        if (name.contains("high claims")) map.put(prefix + "high_claims", h + a);
                    }
                }
            }
        } catch (Exception ignored) {}

        return map;
    }

    private double parseVal(String str) {
        if (str == null || str.isBlank()) return 0.0;
        String clean = str.replace("%", "").split("/")[0].trim();
        try {
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
