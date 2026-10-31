package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Tennis.
 * Queries tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TennisVectorExtractor implements SportVectorExtractor<TennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TennisSport sport = new TennisSport();

    @Override
    public TennisSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── Dims 0..7: Macro Win/Loss/Sets/Rest ───────────────────────────────
        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0, cleanWins = 0, over25 = 0, totalSets = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;

                int s1 = toInt(h.get("team_score"), 0);
                int s2 = toInt(h.get("opponent_score"), 0);
                totalSets += (s1 + s2);
                if (s1 == 2 && s2 == 0) cleanWins++;
                if ((s1 + s2) >= 3) over25++;
            }

            vec[0] = (float) wins / n;
            vec[1] = 0.0f; // No draws in tennis!
            vec[2] = (float) (n - wins) / n;
            vec[3] = (float) cleanWins / n;
            vec[4] = (float) over25 / n;
            vec[5] = (float) totalSets / (n * 3.0f);
            vec[6] = Math.min(1.0f, (float) n / 10.0f);
            vec[7] = 0.5f; // rest days
        } else {
            vec[0] = 0.50f; vec[1] = 0.0f; vec[2] = 0.50f;
            vec[3] = 0.35f; vec[4] = 0.35f; vec[5] = 0.50f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..63
        vec[8]  = 0.50f; // conceded_first_set_rate
        vec[9]  = 0.35f; // comeback_win_rate
        vec[10] = 0.20f; // deciding_set_win_rate
        vec[11] = 0.50f; // tiebreak_clutch
        vec[12] = 0.20f; // collapsed_lead_rate
        vec[13] = 0.20f; // blown_set_points
        vec[14] = 0.35f; // straight_set_win_rate
        vec[15] = 0.35f; // straight_set_loss_rate
        vec[16] = 0.40f; // avg_max_deficit
        vec[17] = 0.30f; // late_game_stamina

        // Games distribution
        vec[18] = 0.33f; vec[19] = 0.33f; vec[20] = 0.34f;
        vec[24] = 0.33f; vec[25] = 0.33f; vec[26] = 0.34f;

        // Tactical & Service baselines
        vec[30] = 0.62f; // first_serve_in_pct (62%)
        vec[31] = 0.72f; // first_serve_won_pct (72%)
        vec[32] = 0.52f; // second_serve_won_pct (52%)
        vec[33] = 0.25f; // aces_per_game
        vec[34] = 0.15f; // double_faults_per_game
        vec[35] = 0.60f; // break_points_saved_pct (60%)
        vec[36] = 0.40f; // break_points_converted_pct (40%)
        vec[37] = 0.80f; // service_hold_pct
        vec[38] = 0.22f; // return_break_pct
        vec[39] = 0.50f; // hard_win_pct
        vec[40] = 0.50f; // clay_win_pct
        vec[41] = 0.50f; // grass_win_pct
        vec[42] = 0.50f; // tiebreak_win_pct
        vec[43] = 0.50f; // deciding_set_win_pct
        vec[44] = 0.50f; // h2h_dominance

        // Player Biological Age & Archetype (Tennis)
        vec[48] = 0.36f; // avg_age (normalized ~26 years)
        vec[49] = 0.30f; // young (<22)
        vec[50] = 0.15f; // veteran (>32)
        vec[51] = 0.30f; // age_std
        vec[52] = 0.60f; // height_proxy
        vec[60] = 0.70f; // serve_rating
        vec[61] = 0.70f; // return_rating
        vec[62] = 0.70f; // baseline_rating
        vec[63] = 0.70f; // net_clutch_rating

        // ── Real ClickHouse DDS Facts from tennis_match_fact ──────────────────
        Long pid = ctx.getTeamId();
        if (pid != null && pid > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(p1_first_serve_pct) as fs_pct, " +
                    "avg(p1_aces) as aces, " +
                    "avg(p1_double_faults) as dfs, " +
                    "avg(p1_break_points_saved_pct) as bp_saved, " +
                    "avg(p1_break_points_converted_pct) as bp_conv, " +
                    "avg(score1 > score2) as wr, " +
                    "countIf(surface = 'HARD' AND score1 > score2) / greatest(1, countIf(surface = 'HARD')) as hard_wr, " +
                    "countIf(surface = 'CLAY' AND score1 > score2) / greatest(1, countIf(surface = 'CLAY')) as clay_wr " +
                    "FROM tennis_match_fact WHERE player1_id = " + pid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("tennis:" + pid, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                vec[30] = (float) (toDouble(r.get("fs_pct"), 62.0) / 100.0);
                vec[33] = (float) (toDouble(r.get("aces"), 4.0) / 15.0);
                vec[34] = (float) (toDouble(r.get("dfs"), 2.0) / 10.0);
                vec[35] = (float) (toDouble(r.get("bp_saved"), 60.0) / 100.0);
                vec[36] = (float) (toDouble(r.get("bp_conv"), 40.0) / 100.0);
                vec[39] = (float) toDouble(r.get("hard_wr"), 0.50);
                vec[40] = (float) toDouble(r.get("clay_wr"), 0.50);
            }

            // Head-to-Head Lifetime against opponent
            Long oppId = ctx.getOpponentId();
            if (oppId != null && oppId > 0) {
                String h2hSql = "SELECT count() as h2h_m, avg(score1 > score2) as h2h_wr " +
                        "FROM tennis_match_fact WHERE player1_id = " + pid + " AND player2_id = " + oppId;
                List<Map<String, Object>> h2hRows = clickHouse.queryJsonCached("tennis_h2h:" + pid + ":" + oppId, h2hSql);
                if (!h2hRows.isEmpty() && toInt(h2hRows.get(0).get("h2h_m"), 0) > 0) {
                    vec[44] = (float) toDouble(h2hRows.get(0).get("h2h_wr"), 0.50);
                }
            }
        }

        return l2Normalize(vec);
    }
}
