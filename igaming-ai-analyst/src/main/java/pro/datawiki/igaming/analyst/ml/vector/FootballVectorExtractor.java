package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.football.SoccerSport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Football (Soccer).
 * Queries football_match_fact, football_lineup_player_fact,
 * match_gamestate_resilience_fact, and match_goal_timeline_fact.
 * Captures transition chaos, compactness, conversion fragility, and blunder rates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FootballVectorExtractor implements SportVectorExtractor<SoccerSport> {

    private final ClickHouseDdsClient clickHouse;
    private final SoccerSport sport = new SoccerSport();

    @Override
    public SoccerSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── Dims 0..7: Macro Win/Draw/Loss/GF/GA/Rest ────────────────────────
        int histMatches = 0;
        int highEventMatches = 0; // >= 4 goals (TRANSITION FIREWORKS)
        int lowEventMatches = 0;  // <= 2 goals
        int cleanSheets = 0;
        int failedToScore = 0;

        float priorW = 0.45f;
        float priorD = 0.28f;
        float priorL = 0.27f;
        float priorGf = 1.35f;
        float priorGa = 1.20f;
        float priorCs = 0.28f;
        float priorFts = 0.24f;
        float cWeight = 4.0f;

        if (hist != null && !hist.isEmpty()) {
            histMatches = hist.size();
            int wins = 0, draws = 0, losses = 0, gf = 0, ga = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;
                else if ("DRAW".equals(res) || "0".equals(res)) draws++;
                else if ("LOSS".equals(res) || "-1".equals(res)) losses++;

                int score = toInt(h.get("team_score"), 0);
                int oppScore = toInt(h.get("opponent_score"), 0);
                gf += score;
                ga += oppScore;
                int totalGoals = score + oppScore;

                if (totalGoals >= 4) highEventMatches++;
                if (totalGoals <= 2) lowEventMatches++;
                if (oppScore == 0) cleanSheets++;
                if (score == 0) failedToScore++;
            }

            // Bayesian Dirichlet-Multinomial shrinkage prevents small-sample saturation
            float effN = histMatches + cWeight;
            vec[0] = (wins + cWeight * priorW) / effN;
            vec[1] = (draws + cWeight * priorD) / effN;
            vec[2] = (losses + cWeight * priorL) / effN;
            vec[3] = (gf + cWeight * priorGf) / (effN * 3.0f);
            vec[4] = (ga + cWeight * priorGa) / (effN * 3.0f);
            vec[5] = ((gf - ga) + cWeight * (priorGf - priorGa)) / (effN * 3.0f);
            vec[6] = (cleanSheets + cWeight * priorCs) / effN;
            vec[7] = (failedToScore + cWeight * priorFts) / effN;
        } else {
            vec[0] = priorW; vec[1] = priorD; vec[2] = priorL;
            vec[3] = priorGf / 3.0f; vec[4] = priorGa / 3.0f; vec[5] = (priorGf - priorGa) / 3.0f;
            vec[6] = priorCs; vec[7] = priorFts;
        }

        // Baseline Priors for Dims 8..17
        vec[8]  = 0.48f; // conceded_first_rate
        vec[9]  = 0.32f; // comeback_equalize_rate
        vec[10] = 0.16f; // comeback_win_rate
        vec[11] = 0.22f; // ppg_losing
        vec[12] = 0.18f; // collapsed_lead_rate
        vec[13] = 0.25f; // blown_lead_points
        vec[14] = histMatches > 0 ? (float) cleanSheets / histMatches : 0.28f; // clean_sheet_rate
        vec[15] = histMatches > 0 ? (float) failedToScore / histMatches : 0.24f; // failed_to_score_rate
        vec[16] = 0.45f; // avg_max_deficit
        vec[17] = 0.21f; // late_goals_scored_ratio

        for (int i = 0; i < 6; i++) {
            vec[18 + i] = 0.166f;
            vec[24 + i] = 0.166f;
        }

        // Tactical baselines
        vec[30] = 0.35f; // xg
        vec[31] = 0.35f; // xga
        vec[32] = 0.50f; // shot_generation_pace
        vec[33] = 0.30f; // conversion_fragility
        vec[34] = 0.25f; // shot_blocking_rate
        vec[35] = 0.75f; // clean_sheet_defense_solidity
        vec[36] = histMatches > 0 ? (float) highEventMatches / histMatches : 0.25f; // transition_chaos_index
        vec[37] = 0.30f; // ppda
        vec[38] = 0.50f; // defensive_line_height
        vec[39] = 0.50f; // possession
        vec[40] = 0.80f; // pass_accuracy_pct
        vec[41] = 0.45f; // rest_defense_solidity
        vec[42] = histMatches > 0 ? (float) highEventMatches / histMatches : 0.25f; // openness_gradient (>= 4 goals)
        vec[43] = histMatches > 0 ? (float) lowEventMatches / histMatches : 0.30f;  // under_rate (<= 2 goals)
        vec[44] = 0.50f; // aerial_won_pct
        vec[45] = 0.44f; // fouls
        vec[46] = 0.33f; // yellow_cards
        vec[47] = 0.40f; // foul_to_shot_chaos

        vec[48] = 0.38f; // avg_age
        vec[49] = 0.25f; // young (<22)
        vec[50] = 0.15f; // veteran (>32)
        vec[51] = 0.35f; // age_std

        // ── Dims 52..58: Point-in-Time Tournament Motivation & Stakes ──────────
        vec[52] = ctx.getEliminationPressureIndex() != null ? ctx.getEliminationPressureIndex() : 0.0f;
        vec[53] = ctx.getDrawViabilityFlag() != null ? ctx.getDrawViabilityFlag() : 1.0f;
        vec[54] = ctx.getRegionalCardBias() != null ? ctx.getRegionalCardBias() : 0.50f;
        vec[55] = ctx.getBlowoutGradient() != null ? ctx.getBlowoutGradient() : 0.0f;
        vec[56] = ctx.getWinLeverage() != null ? ctx.getWinLeverage() : 0.50f;
        vec[57] = ctx.getEliminationHazard() != null ? ctx.getEliminationHazard() : 0.50f;
        vec[58] = ctx.getPrizeWageRatio() != null ? ctx.getPrizeWageRatio() : 0.05f;

        vec[59] = 0.20f; // blunder_frequency_index
        vec[60] = 0.68f; // goalkeeper_quality
        vec[61] = 0.68f; // cb_pairing_solidity
        vec[62] = 0.68f; // midfield_screen
        vec[63] = 0.68f; // attacking_transition_pace

        // Evaluate Starters from Context
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

        String whereClause = (idClause != null)
                ? idClause
                : (tname != null && !tname.isBlank()) ? "LOWER(team_name) = LOWER('" + tname.replace("'", "\\'") + "')" : null;

        if (whereClause != null) {
            // 1. Resilience
            String resSql = "SELECT count() as matches, avg(conceded_first) as cf, avg(equalized_after_trailing) as eq, " +
                    "avg(collapsed_after_leading) as col, avg(goals_scored_75_plus) as late_f, avg(goals_conceded_75_plus) as late_a " +
                    "FROM match_gamestate_resilience_fact WHERE " + whereClause;
            List<Map<String, Object>> resRows = clickHouse.queryJsonCached("res:" + whereClause, resSql);
            if (!resRows.isEmpty() && toInt(resRows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = resRows.get(0);
                vec[8]  = (float) toDouble(r.get("cf"), 0.48);
                vec[9]  = (float) toDouble(r.get("eq"), 0.32);
                vec[12] = (float) toDouble(r.get("col"), 0.18);
                double lf = toDouble(r.get("late_f"), 0.34);
                double la = toDouble(r.get("late_a"), 0.34);
                vec[17] = (float) (lf / Math.max(0.1, lf + la));
            }

            // 2. 15-minute Goal Distribution
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

            // 3. Tactical & Transition Metrics from football_match_fact (both home and away, queried by SofaScore ID!)
            if (effectiveSofaId != null && effectiveSofaId > 0) {
                String tacSql = "SELECT count() as matches, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_xg, away_xg)) as xg, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", away_xg, home_xg)) as xga, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_xg_open_play, away_xg_open_play)) as xg_open, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_ppda, away_ppda)) as ppda, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_possession_pct, away_possession_pct)) as poss, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_pass_accuracy_pct, away_pass_accuracy_pct)) as pass_acc, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_defensive_line_height, away_defensive_line_height)) as def_line, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_aerial_won_pct, away_aerial_won_pct)) as aerial, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_fouls, away_fouls)) as fouls, " +
                        "avg(if(home_team_id = " + effectiveSofaId + ", home_yellow_cards, away_yellow_cards)) as cards, " +
                        "avg(home_score + away_score >= 4) as high_event_rate, " +
                        "avg(home_score + away_score <= 2) as low_event_rate, " +
                        "avg(home_score + away_score) as avg_total_goals " +
                        "FROM football_match_fact WHERE (home_team_id = " + effectiveSofaId + " OR away_team_id = " + effectiveSofaId + ") AND home_xg > 0";

                List<Map<String, Object>> tacRows = clickHouse.queryJsonCached("tac_full:" + effectiveSofaId, tacSql);
                if (!tacRows.isEmpty() && toInt(tacRows.get(0).get("matches"), 0) > 0) {
                    Map<String, Object> t = tacRows.get(0);
                    double xg = toDouble(t.get("xg"), 1.35);
                    double xga = toDouble(t.get("xga"), 1.35);
                    double xgOpen = toDouble(t.get("xg_open"), 0.90);
                    double passAcc = toDouble(t.get("pass_acc"), 80.0);
                    double highEvRate = toDouble(t.get("high_event_rate"), 0.25);
                    double lowEvRate = toDouble(t.get("low_event_rate"), 0.30);
                    double fouls = toDouble(t.get("fouls"), 11.0);
                    double cards = toDouble(t.get("cards"), 2.0);

                    vec[30] = (float) Math.min(1.0, xg / 3.5);
                    vec[31] = (float) Math.min(1.0, xga / 3.5);
                    vec[32] = (float) Math.min(1.0, (xg + xga) / 3.5);

                    // Conversion Fragility: high in semi-pro/amateur (poor goalkeeping and defensive blunders)
                    vec[33] = (float) Math.min(1.0, (xga * (100.0 - passAcc)) / 100.0);

                    // Shot Blocking Rate: drops drastically in low-possession, chaotic transition leagues
                    vec[34] = (float) Math.max(0.05, Math.min(0.50, passAcc / 250.0));

                    // Dims 36: Transition Chaos Index (Open-play dominance + Low technical precision)
                    double chaosIndex = (xgOpen / Math.max(0.1, xg)) * ((100.0 - passAcc) / 40.0) * (0.5 + highEvRate);
                    vec[36] = (float) Math.max(0.1, Math.min(1.0, chaosIndex));

                    vec[37] = (float) (toDouble(t.get("ppda"), 10.0) / 30.0);
                    vec[38] = (float) (toDouble(t.get("def_line"), 52.0) / 100.0);
                    vec[39] = (float) (toDouble(t.get("poss"), 50.0) / 100.0);
                    vec[40] = (float) Math.min(1.0, passAcc / 100.0);

                    // Rest-Defense Solidity (inverse of transition chaos)
                    vec[41] = (float) Math.max(0.1, Math.min(1.0, 1.0 - vec[36]));

                    // Openness gradient (TRANSITION FIREWORKS marker)
                    vec[42] = (float) Math.max(vec[42], highEvRate);
                    vec[43] = (float) Math.max(vec[43], lowEvRate);

                    vec[44] = (float) (toDouble(t.get("aerial"), 50.0) / 100.0);
                    vec[45] = (float) (fouls / 25.0);
                    vec[46] = (float) (cards / 6.0);
                    vec[47] = (float) Math.min(1.0, (fouls + cards * 2.0) / 30.0);

                    // Blunder Frequency Index: high young pct + low pass accuracy
                    vec[59] = (float) Math.min(1.0, (vec[49] * 0.5 + (1.0 - vec[40]) * 0.5));
                }

                // 4. Squad Biological Age from football_lineup_player_fact
                String ageSql = "SELECT avg(age_at_match) as avg_age, " +
                        "countIf(age_at_match <= 22) / greatest(1, count()) as young_pct, " +
                        "countIf(age_at_match >= 32) / greatest(1, count()) as veteran_pct, " +
                        "stddevPop(age_at_match) as age_std " +
                        "FROM football_lineup_player_fact WHERE mdm_team_id = " + tid + " AND age_at_match > 0";
                List<Map<String, Object>> ageRows = clickHouse.queryJsonCached("age:" + tid, ageSql);
                if (!ageRows.isEmpty() && toDouble(ageRows.get(0).get("avg_age"), 0) > 0) {
                    Map<String, Object> a = ageRows.get(0);
                    double avgAge = toDouble(a.get("avg_age"), 25.0);
                    vec[48] = (float) Math.max(0.0, Math.min(1.0, (avgAge - 18.0) / 22.0));
                    vec[49] = (float) toDouble(a.get("young_pct"), 0.25);
                    vec[50] = (float) toDouble(a.get("veteran_pct"), 0.15);
                    vec[51] = (float) (toDouble(a.get("age_std"), 3.5) / 10.0);
                    // Recalculate blunder index with real squad youth %
                    vec[59] = (float) Math.min(1.0, (vec[49] * 0.6 + (1.0 - vec[40]) * 0.4));
                }
            }
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
            double r = toDouble(p.get("rating"), toDouble(p.get("match_rating"), 6.8));
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
}
