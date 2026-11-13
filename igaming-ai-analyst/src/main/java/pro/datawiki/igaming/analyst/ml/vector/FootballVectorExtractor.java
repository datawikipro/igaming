package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.football.SoccerSport;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Football (Soccer).
 * Queries football_match_fact, football_lineup_player_fact,
 * match_gamestate_resilience_fact, and match_goal_timeline_fact.
 * Captures transition chaos, compactness, conversion fragility, and blunder rates.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FootballVectorExtractor extends AbstractSportVectorExtractor<SoccerSport> {

    private final ClickHouseDdsClient clickHouse;
    private final SoccerSport sport = new SoccerSport();

    @Override
    public SoccerSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];

        // ── Dims 0..7: Guaranteed Macro Performance Vector (Parity with Python) ──
        float[] macro0to7 = computeStandardMacroDims0To7(ctx, sport.hasDrawOutcome());
        System.arraycopy(macro0to7, 0, vec, 0, 8);

        // ── Dims 8..63: Baseline Physical & Biological Priors ────────────────────
        populateStandardBaselinePriors(vec, sport);

        // Evaluate Starters from Context if present (Dims 60..63)
        evaluateFootballStarters(ctx, vec);

        // ── Real ClickHouse DDS Facts ─────────────────────────────────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;
        String tname = ctx.getTeamName();

        String idClause = null;
        if (tid != null && tid > 0 && sofaId != null && sofaId > 0 && !sofaId.equals(tid)) {
            idClause = "(team_id = " + tid + " OR team_id = " + sofaId + ")";
        } else if (tid != null && tid > 0) {
            idClause = "team_id = " + tid;
        } else if (sofaId != null && sofaId > 0) {
            idClause = "team_id = " + sofaId;
        }

        String whereClause;
        if (idClause != null) {
            whereClause = idClause;
        } else if (tname != null && !tname.isBlank()) {
            String tnameLatin = SimilarityUtils.hasCyrillic(tname) ? SimilarityUtils.transliterateCyrillic(tname).trim() : null;
            if (tnameLatin != null && !tnameLatin.equalsIgnoreCase(tname)) {
                whereClause = "(LOWER(team_name) = LOWER('" + tname.replace("'", "\\'") + "') OR LOWER(team_name) = LOWER('" + tnameLatin.replace("'", "\\'") + "'))";
            } else {
                whereClause = "LOWER(team_name) = LOWER('" + tname.replace("'", "\\'") + "')";
            }
        } else {
            whereClause = null;
        }

        if (whereClause != null) {
            // 1. Resilience (Dims 8..17)
            String resSql = "SELECT count() as matches, avg(conceded_first) as cf, avg(equalized_after_trailing) as eq, " +
                    "countIf(conceded_first = 1 AND final_result = 'WIN') / greatest(1, countIf(conceded_first = 1)) as comeback_w, " +
                    "avg(points_won_from_losing_position) as ppg_los, " +
                    "avg(collapsed_after_leading) as col, avg(blown_lead_points) as blown_pts, " +
                    "avg(abs(max_deficit)) as avg_deficit, " +
                    "avg(goals_scored_75_plus) as late_f, avg(goals_conceded_75_plus) as late_a " +
                    "FROM match_gamestate_resilience_fact WHERE " + whereClause;
            List<Map<String, Object>> resRows = clickHouse.queryJsonCached("res:" + whereClause, resSql);
            if (!resRows.isEmpty() && toInt(resRows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = resRows.get(0);
                vec[8]  = (float) toDouble(r.get("cf"), 0.48);
                vec[9]  = (float) toDouble(r.get("eq"), 0.32);
                vec[10] = (float) toDouble(r.get("comeback_w"), 0.16);
                vec[11] = (float) Math.min(1.0, toDouble(r.get("ppg_los"), 0.66) / 3.0);
                vec[12] = (float) toDouble(r.get("col"), 0.18);
                vec[13] = (float) Math.min(1.0, toDouble(r.get("blown_pts"), 0.75) / 3.0);
                vec[16] = (float) Math.min(1.0, toDouble(r.get("avg_deficit"), 1.35) / 3.0);
                double lf = toDouble(r.get("late_f"), 0.34);
                double la = toDouble(r.get("late_a"), 0.34);
                vec[17] = (float) (lf / Math.max(0.1, lf + la));
            }

            // 2. 15-minute Goal Distribution (Dims 18..29)
            String gtSql = "SELECT countIf(minute <= 15) / greatest(1, count()) as b1, " +
                    "countIf(minute > 15 AND minute <= 30) / greatest(1, count()) as b2, " +
                    "countIf(minute > 30 AND minute <= 45) / greatest(1, count()) as b3, " +
                    "countIf(minute > 45 AND minute <= 60) / greatest(1, count()) as b4, " +
                    "countIf(minute > 60 AND minute <= 75) / greatest(1, count()) as b5, " +
                    "countIf(minute > 75) / greatest(1, count()) as b6 " +
                    "FROM match_goal_timeline_fact WHERE " + whereClause + " AND incident_type = 'GOAL'";
            List<Map<String, Object>> gtRows = clickHouse.queryJsonCached("gt:" + whereClause, gtSql);
            if (!gtRows.isEmpty()) {
                Map<String, Object> g = gtRows.get(0);
                for (int i = 1; i <= 6; i++) {
                    vec[17 + i] = (float) toDouble(g.get("b" + i), 0.166);
                }
            }

            // 3. Tactical & Transition Metrics from football_team_advanced_v (Dims 30..47)
            if (effectiveSofaId != null && effectiveSofaId > 0) {
                String tacSql = "SELECT * FROM igaming_dds.football_team_advanced_v WHERE team_id = " + effectiveSofaId;

                List<Map<String, Object>> tacRows = clickHouse.queryJsonCached("tac_adv:" + effectiveSofaId, tacSql);
                if (!tacRows.isEmpty() && toInt(tacRows.get(0).get("total_matches"), 0) > 0) {
                    Map<String, Object> t = tacRows.get(0);
                    double xg = toDouble(t.get("avg_xg"), 1.35);
                    double xga = toDouble(t.get("avg_xga"), 1.35);
                    double xgOpen = toDouble(t.get("avg_xg_open"), 0.90);
                    double xgSetPiece = toDouble(t.get("avg_xg_set_piece"), 0.35);
                    double passAcc = toDouble(t.get("avg_pass_acc"), 80.0);
                    double highEvRate = toDouble(t.get("high_event_rate"), 0.25);
                    double lowEvRate = toDouble(t.get("low_event_rate"), 0.30);
                    double fouls = toDouble(t.get("avg_fouls"), 11.0);
                    double cards = toDouble(t.get("avg_cards"), 2.0);

                    vec[30] = (float) Math.min(1.0, xg / 3.5);
                    vec[31] = (float) Math.min(1.0, xga / 3.5);
                    vec[32] = (float) Math.min(1.0, (xg + xga) / 3.5);

                    // Conversion Fragility: high in semi-pro/amateur
                    vec[33] = (float) Math.min(1.0, (xga * (100.0 - passAcc)) / 100.0);

                    // Shot Blocking Rate
                    vec[34] = (float) Math.max(0.05, Math.min(0.50, passAcc / 250.0));

                    // Dims 35: Set Piece Danger Ratio
                    vec[35] = (float) Math.min(1.0, xgSetPiece / Math.max(0.1, xg));

                    // Dims 36: Transition Chaos Index
                    double chaosIndex = (xgOpen / Math.max(0.1, xg)) * ((100.0 - passAcc) / 40.0) * (0.5 + highEvRate);
                    vec[36] = (float) Math.max(0.1, Math.min(1.0, chaosIndex));

                    // PPDA
                    vec[37] = (float) toDouble(t.get("pressing_intensity_sigmoid"), 0.30);
                    vec[38] = (float) (toDouble(t.get("avg_def_line"), 52.0) / 100.0);
                    vec[39] = (float) (toDouble(t.get("avg_possession"), 50.0) / 100.0);
                    vec[40] = (float) Math.min(1.0, passAcc / 100.0);

                    // Rest-Defense Solidity
                    vec[41] = (float) Math.max(0.1, Math.min(1.0, 1.0 - vec[36]));

                    // Openness gradient
                    vec[42] = (float) Math.max(vec[42], highEvRate);
                    vec[43] = (float) Math.max(vec[43], lowEvRate);

                    vec[44] = (float) (toDouble(t.get("avg_aerial_won"), 50.0) / 100.0);
                    vec[45] = (float) (fouls / 25.0);
                    vec[46] = (float) (cards / 6.0);
                    vec[47] = (float) Math.min(1.0, (fouls + cards * 2.0) / 30.0);

                    // Blunder Frequency Index (Dim 59)
                    float passError = Math.max(0.0f, 1.0f - vec[40]);
                    vec[59] = (float) Math.min(1.0, Math.max(0.05, 0.15 + (vec[49] * 0.45f) + (passError * 0.40f) + (vec[49] * passError * 0.80f)));
                }

                // 4. Squad Biological Age from football_lineup_player_fact (Dims 48..51)
                String ageSql = "SELECT avg(age_at_match) as avg_age, " +
                        "countIf(age_at_match <= 22) / greatest(1, count()) as young_pct, " +
                        "countIf(age_at_match >= 32) / greatest(1, count()) as veteran_pct, " +
                        "stddevPop(age_at_match) as age_std " +
                        "FROM football_lineup_player_fact WHERE mdm_team_id = " + tid + " AND age_at_match > 0";
                List<Map<String, Object>> ageRows = clickHouse.queryJsonCached("age:" + tid, ageSql);
                if (!ageRows.isEmpty() && toDouble(ageRows.get(0).get("avg_age"), 0) > 0) {
                    Map<String, Object> a = ageRows.get(0);
                    double avgAge = toDouble(a.get("avg_age"), 26.5);
                    vec[48] = (float) Math.exp(-Math.pow(avgAge - 26.8, 2) / (2.0 * 12.25));
                    vec[49] = (float) toDouble(a.get("young_pct"), 0.25);
                    vec[50] = (float) toDouble(a.get("veteran_pct"), 0.15);
                    vec[51] = (float) (toDouble(a.get("age_std"), 3.5) / 10.0);

                    float passErrorReal = Math.max(0.0f, 1.0f - vec[40]);
                    vec[59] = (float) Math.min(1.0, Math.max(0.05, 0.15 + (vec[49] * 0.45f) + (passErrorReal * 0.40f) + (vec[49] * passErrorReal * 0.80f)));
                }
            }
        }

        // Apply Bottom-Up Squad Chemistry & International Fatigue ("Вирус сборных")
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem = ctx.getSquadChemistry();
        if (chem != null) {
            float blunderFatiguePenalty = chem.nationalFatiguedCount() >= 2 ? 0.08f : (chem.nationalFatiguedCount() == 1 ? 0.03f : 0.0f);
            vec[59] = (float) Math.min(1.0, Math.max(0.05, (vec[59] * chem.blunderRiskMultiplier()) + blunderFatiguePenalty));
            vec[61] = (float) Math.min(1.0, Math.max(0.20, (vec[61] * 0.40f) + (chem.cbPairingSolidity() * 0.60f)));
            vec[62] = (float) Math.min(1.0, Math.max(0.20, (vec[62] * 0.40f) + (chem.midfieldScreenSolidity() * 0.60f)));
        }

        return l2Normalize(vec);
    }

    private void evaluateFootballStarters(TacticalVectorContext ctx, float[] vec) {
        List<Map<String, Object>> starters = ctx.getStarters();
        if (starters == null || starters.isEmpty()) {
            return;
        }

        double gkRating = 0.0;
        int gkCount = 0;
        double cbRating = 0.0;
        int cbCount = 0;
        double midRating = 0.0;
        int midCount = 0;
        double attRating = 0.0;
        int attCount = 0;

        for (Map<String, Object> p : starters) {
            double r = parsePlayerRating(p);
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();

            if ("G".equals(pos) || "GK".equals(pos)) {
                gkRating += r;
                gkCount++;
            } else if ("D".equals(pos) || "CB".equals(pos) || "LB".equals(pos) || "RB".equals(pos)) {
                cbRating += r;
                cbCount++;
            } else if ("M".equals(pos) || "CM".equals(pos) || "DM".equals(pos) || "AM".equals(pos)) {
                midRating += r;
                midCount++;
            } else {
                attRating += r;
                attCount++;
            }
        }

        if (gkCount > 0) vec[60] = (float) Math.min(1.0, (gkRating / gkCount) / 10.0);
        if (cbCount > 0) vec[61] = (float) Math.min(1.0, (cbRating / cbCount) / 10.0);
        if (midCount > 0) vec[62] = (float) Math.min(1.0, (midRating / midCount) / 10.0);
        if (attCount > 0) vec[63] = (float) Math.min(1.0, (attRating / attCount) / 10.0);
    }

    private double parsePlayerRating(Map<String, Object> p) {
        if (p == null) return 6.8;
        Object rObj = p.get("effective_rating");
        if (rObj == null) rObj = p.get("rating");
        if (rObj == null) rObj = p.get("match_rating");
        double r = toDouble(rObj, 6.8);
        return (r >= 2.0 && r <= 10.0) ? r : 6.8;
    }
}
