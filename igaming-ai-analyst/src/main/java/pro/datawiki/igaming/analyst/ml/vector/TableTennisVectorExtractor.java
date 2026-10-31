package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Table Tennis (Ping Pong).
 * Queries table_tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Models daily multi-match tournament grinds, same-day H2H records, and blade rubber styles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableTennisVectorExtractor implements SportVectorExtractor<TableTennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TableTennisSport sport = new TableTennisSport();

    @Override
    public TableTennisSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── Dims 0..7: Macro Win/Loss/Points Margin ───────────────────────────
        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0, clean30 = 0, deciding5th = 0;
            int totalPtsFor = 0, totalPtsAgainst = 0;

            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;

                int s1 = toInt(h.get("team_score"), 0);
                int s2 = toInt(h.get("opponent_score"), 0);
                if (s1 == 3 && s2 == 0) clean30++;
                if (s1 + s2 == 5) deciding5th++;
                totalPtsFor += s1;
                totalPtsAgainst += s2;
            }

            vec[0] = (float) wins / n;
            vec[1] = 0.0f; // Table tennis has strictly binary outcomes!
            vec[2] = (float) (n - wins) / n;
            vec[3] = (float) clean30 / n;
            vec[4] = (float) deciding5th / n;
            vec[5] = (float) (totalPtsFor - totalPtsAgainst) / (n * 5.0f);
            vec[6] = Math.min(1.0f, (float) n / 10.0f);
            vec[7] = 0.5f;
        } else {
            vec[0] = 0.50f; vec[1] = 0.0f; vec[2] = 0.50f;
            vec[3] = 0.25f; vec[4] = 0.25f; vec[5] = 0.0f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..63
        vec[8]  = 0.50f; // conceded_first_set_rate
        vec[9]  = 0.38f; // comeback_equalize_rate
        vec[10] = 0.22f; // comeback_win_rate
        vec[11] = 0.22f; // ppg_losing
        vec[12] = 0.22f; // collapsed_lead_rate
        vec[13] = 0.22f; // blown_lead_points
        vec[14] = 0.30f; // clean_sweep_rate
        vec[15] = 0.30f; // failed_to_win_set_rate
        vec[16] = 0.40f; // avg_max_deficit
        vec[17] = 0.30f; // late_game_stamina

        // Sets distribution
        vec[18] = 0.25f; vec[19] = 0.25f; vec[20] = 0.25f; vec[21] = 0.25f;
        vec[24] = 0.25f; vec[25] = 0.25f; vec[26] = 0.25f; vec[27] = 0.25f;

        // Tactical baselines
        vec[30] = 0.50f; // elo_normalized
        vec[31] = 0.50f; // winstreak_proxy
        vec[32] = 0.50f; // point_margin_avg
        vec[33] = 0.50f; // deuce_clutch_rate
        vec[34] = 0.50f; // same_day_h2h_win_rate
        vec[35] = 0.50f; // service_dominance
        vec[36] = 0.50f; // rally_stamina

        // Biological Age & Blade/Rubber Style
        vec[48] = 0.35f; // avg_age
        vec[49] = 0.30f; // young
        vec[50] = 0.15f; // veteran
        vec[51] = 0.30f; // age_std
        vec[52] = ctx.getEliminationPressureIndex();
        vec[53] = ctx.getDrawViabilityFlag();
        vec[54] = ctx.getRegionalCardBias();
        vec[60] = 0.70f; // spin_attack_rating
        vec[61] = 0.70f; // chopper_defense_rating
        vec[62] = 0.70f; // counter_rating
        vec[63] = 0.70f; // loop_rating

        // ── Real ClickHouse DDS Facts from table_tennis_match_fact ────────────
        Long pid = ctx.getTeamId();
        if (pid != null && pid > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(player1_live_elo) as avg_elo, " +
                    "avg(player1_winstreak) as streak, " +
                    "avg(points_margin) as margin, " +
                    "avg(score1 > score2) as wr, " +
                    "countIf(score1 = 3 AND score2 = 0) / greatest(1, count()) as sweep_rate, " +
                    "countIf(total_sets = 5) / greatest(1, count()) as dec_rate, " +
                    "avg(same_day_h2h_matches_today) as daily_m, " +
                    "avg(same_day_h2h_p1_wins_today) as daily_wins " +
                    "FROM table_tennis_match_fact WHERE player1_id = " + pid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("tt:" + pid, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double elo = toDouble(r.get("avg_elo"), 1500.0);
                vec[30] = (float) Math.max(0.0, Math.min(1.0, (elo - 1000.0) / 1200.0));
                vec[31] = (float) Math.max(0.0, Math.min(1.0, (toDouble(r.get("streak"), 0.0) + 5.0) / 10.0));
                vec[32] = (float) ((toDouble(r.get("margin"), 0.0) + 15.0) / 30.0);
                vec[14] = (float) toDouble(r.get("sweep_rate"), 0.30);
                vec[10] = (float) toDouble(r.get("dec_rate"), 0.22);

                double dm = toDouble(r.get("daily_m"), 0.0);
                double dw = toDouble(r.get("daily_wins"), 0.0);
                if (dm > 0) {
                    vec[34] = (float) (dw / dm);
                }
            }
        }

        return l2Normalize(vec);
    }
}
