package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.hockey.IceHockeySport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Ice Hockey.
 * Queries ice_hockey_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Models period-by-period scoring, overtime propensity, shots on goal, and netminder impact.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IceHockeyVectorExtractor implements SportVectorExtractor<IceHockeySport> {

    private final ClickHouseDdsClient clickHouse;
    private final IceHockeySport sport = new IceHockeySport();

    @Override
    public IceHockeySport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── Dims 0..7: Macro Win/OT/Loss/Goals ────────────────────────────────
        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0, draws = 0, losses = 0, gf = 0, ga = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;
                else if ("DRAW".equals(res) || "0".equals(res)) draws++;
                else if ("LOSS".equals(res) || "-1".equals(res)) losses++;

                gf += toInt(h.get("team_score"), 0);
                ga += toInt(h.get("opponent_score"), 0);
            }

            vec[0] = (float) wins / n;
            vec[1] = (float) draws / n; // Regulation draws exist in hockey!
            vec[2] = (float) losses / n;
            vec[3] = (float) gf / (n * 5.0f);
            vec[4] = (float) ga / (n * 5.0f);
            vec[5] = (float) (gf - ga) / (n * 5.0f);
            vec[6] = Math.min(1.0f, (float) n / 10.0f);
            vec[7] = 0.5f;
        } else {
            vec[0] = 0.45f; vec[1] = 0.22f; vec[2] = 0.33f;
            vec[3] = 0.50f; vec[4] = 0.50f; vec[5] = 0.0f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..63
        vec[8]  = 0.50f; // conceded_first_rate
        vec[9]  = 0.40f; // comeback_equalize_rate
        vec[10] = 0.22f; // comeback_win_rate
        vec[11] = 0.25f; // ppg_losing
        vec[12] = 0.22f; // collapsed_lead_rate
        vec[13] = 0.30f; // blown_lead_points
        vec[14] = 0.08f; // shutout_rate (clean sheet rare in hockey)
        vec[15] = 0.08f; // failed_to_score_rate
        vec[16] = 0.55f; // avg_max_deficit
        vec[17] = 0.33f; // late_3rd_period_scoring_ratio (empty-net volatility)

        // Periods distribution (P1, P2, P3, OT)
        vec[18] = 0.28f; vec[19] = 0.33f; vec[20] = 0.34f; vec[21] = 0.05f;
        vec[24] = 0.28f; vec[25] = 0.33f; vec[26] = 0.34f; vec[27] = 0.05f;

        // Tactical baselines
        vec[30] = 0.50f; // shots_on_goal_for
        vec[31] = 0.50f; // shots_on_goal_against
        vec[32] = 0.50f; // shooting_efficiency
        vec[33] = 0.91f; // save_pct_baseline (91%)
        vec[34] = 0.20f; // power_play_pct (20%)
        vec[35] = 0.80f; // penalty_kill_pct (80%)
        vec[36] = 0.25f; // overtime_propensity

        // Goaltender & Squad Age
        vec[48] = 0.42f; // avg_age
        vec[49] = 0.20f; // young (<22)
        vec[50] = 0.20f; // veteran (>32)
        vec[51] = 0.38f; // age_std
        vec[52] = 0.60f; // height_proxy
        vec[54] = 0.70f; // bottom_6_attack_depth
        vec[60] = 0.72f; // netminder_rating (drives 65% of defense!)
        vec[61] = 0.68f; // defense_blueline_rating
        vec[62] = 0.68f; // center_depth_rating
        vec[63] = 0.70f; // winger_forecheck_rating

        // ── Real ClickHouse DDS Facts from ice_hockey_match_fact ──────────────
        Long tid = ctx.getTeamId();
        if (tid != null && tid > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(home_shots) as shots_for, " +
                    "avg(away_shots) as shots_against, " +
                    "avg(is_overtime) as ot_rate, " +
                    "avg(home_score > away_score) as wr, " +
                    "avg(home_p1) as p1_g, avg(home_p2) as p2_g, avg(home_p3) as p3_g " +
                    "FROM ice_hockey_match_fact WHERE home_team_id = " + tid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("hockey:" + tid, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                vec[30] = (float) (toDouble(r.get("shots_for"), 30.0) / 60.0);
                vec[31] = (float) (toDouble(r.get("shots_against"), 30.0) / 60.0);
                vec[36] = (float) toDouble(r.get("ot_rate"), 0.25);

                double p1 = toDouble(r.get("p1_g"), 0.8);
                double p2 = toDouble(r.get("p2_g"), 1.0);
                double p3 = toDouble(r.get("p3_g"), 1.1);
                double tot = Math.max(0.1, p1 + p2 + p3);
                vec[18] = (float) (p1 / tot);
                vec[19] = (float) (p2 / tot);
                vec[20] = (float) (p3 / tot);
            }
        }

        return l2Normalize(vec);
    }
}
