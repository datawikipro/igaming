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
        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0, draws = 0, losses = 0, gf = 0, ga = 0, cs = 0, fts = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;
                else if ("DRAW".equals(res) || "0".equals(res)) draws++;
                else if ("LOSS".equals(res) || "-1".equals(res)) losses++;

                int score = toInt(h.get("team_score"), 0);
                int oppScore = toInt(h.get("opponent_score"), 0);
                gf += score;
                ga += oppScore;
                if (oppScore == 0) cs++;
                if (score == 0) fts++;
            }

            vec[0] = (float) wins / n;
            vec[1] = (float) draws / n;
            vec[2] = (float) losses / n;
            vec[3] = (float) gf / (n * 3.0f);
            vec[4] = (float) ga / (n * 3.0f);
            vec[5] = (float) (gf - ga) / (n * 3.0f);
            vec[6] = (float) cs / n;
            vec[7] = (float) fts / n;
        } else {
            vec[0] = 0.45f; vec[1] = 0.28f; vec[2] = 0.27f;
            vec[3] = 0.45f; vec[4] = 0.40f; vec[5] = 0.05f;
            vec[6] = 0.28f; vec[7] = 0.24f;
        }

        // Baseline Priors for Dims 8..63
        vec[8]  = 0.48f; // conceded_first_rate
        vec[9]  = 0.32f; // comeback_equalize_rate
        vec[10] = 0.16f; // comeback_win_rate
        vec[11] = 0.22f; // ppg_losing
        vec[12] = 0.18f; // collapsed_lead_rate
        vec[13] = 0.25f; // blown_lead_points
        vec[14] = 0.28f; // clean_sheet_rate
        vec[15] = 0.24f; // failed_to_score_rate
        vec[16] = 0.45f; // avg_max_deficit
        vec[17] = 0.21f; // late_goals_scored_ratio

        for (int i = 0; i < 6; i++) {
            vec[18 + i] = 0.166f;
            vec[24 + i] = 0.166f;
        }

        vec[30] = 0.34f; vec[31] = 0.34f; vec[32] = 0.50f;
        vec[35] = 0.75f; vec[36] = 0.25f; vec[37] = 0.30f;
        vec[38] = 0.50f; vec[39] = 0.50f; vec[40] = 0.80f;
        vec[41] = 0.45f; vec[42] = 0.40f; vec[43] = 0.20f;
        vec[44] = 0.50f; vec[45] = 0.44f; vec[46] = 0.33f;
        vec[47] = 0.50f;

        vec[48] = 0.38f; vec[49] = 0.25f; vec[50] = 0.15f;
        vec[51] = 0.35f;
        vec[52] = ctx.getEliminationPressureIndex() != null ? ctx.getEliminationPressureIndex() : 0.0f;
        vec[53] = ctx.getDrawViabilityFlag() != null ? ctx.getDrawViabilityFlag() : 1.0f;
        vec[54] = ctx.getRegionalCardBias() != null ? ctx.getRegionalCardBias() : 0.50f;
        vec[59] = 0.18f; vec[60] = 0.68f;
        vec[61] = 0.68f; vec[62] = 0.68f; vec[63] = 0.68f;

        // ── Real ClickHouse DDS Facts ─────────────────────────────────────────
        Long tid = ctx.getTeamId();
        String tname = ctx.getTeamName();
        String whereClause = (tid != null && tid > 0)
                ? "team_id = " + tid
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

            // 3. Tactical Metrics from football_match_fact
            if (tid != null && tid > 0) {
                String tacSql = "SELECT avg(home_xg) as xg, avg(home_ppda) as ppda, avg(home_possession_pct) as poss, " +
                        "avg(home_fouls) as fouls, avg(home_yellow_cards) as cards " +
                        "FROM football_match_fact WHERE home_team_id = " + tid + " AND home_xg > 0";
                List<Map<String, Object>> tacRows = clickHouse.queryJsonCached("tac:" + tid, tacSql);
                if (!tacRows.isEmpty() && toDouble(tacRows.get(0).get("xg"), 0) > 0) {
                    Map<String, Object> t = tacRows.get(0);
                    vec[30] = (float) (toDouble(t.get("xg"), 1.35) / 4.0);
                    vec[37] = (float) (toDouble(t.get("ppda"), 10.0) / 30.0);
                    vec[39] = (float) (toDouble(t.get("poss"), 50.0) / 100.0);
                    vec[45] = (float) (toDouble(t.get("fouls"), 11.0) / 25.0);
                    vec[46] = (float) (toDouble(t.get("cards"), 2.0) / 6.0);
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
                }
            }
        }

        return l2Normalize(vec);
    }
}
