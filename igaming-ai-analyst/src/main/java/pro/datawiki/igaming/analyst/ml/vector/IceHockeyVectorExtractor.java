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
 * Models period-by-period scoring, overtime propensity, event pace, defensive trap dynamics,
 * slot suppression, and goaltender impact.
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

        // ── Dims 0..7: Macro Win/OT/Loss/Goals & Event Density ────────────────
        int histMatches = 0;
        int trapLowMatches = 0;   // <= 3 goals (NCAA/Ettan trap cluster)
        int under45Matches = 0;   // <= 4 goals
        int highEventMatches = 0; // >= 6 goals (shootouts/open ice)
        int shutouts = 0;
        int failedToScore = 0;

        float priorW = 0.45f;
        float priorD = 0.22f;
        float priorL = 0.33f;
        float priorGf = 2.50f;
        float priorGa = 2.50f;
        float priorSo = 0.08f;
        float priorFts = 0.08f;
        float cWeight = 4.0f;

        if (hist != null && !hist.isEmpty()) {
            histMatches = hist.size();
            int wins = 0, draws = 0, losses = 0, gf = 0, ga = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;
                else if ("DRAW".equals(res) || "0".equals(res)) draws++;
                else if ("LOSS".equals(res) || "-1".equals(res)) losses++;

                int teamScore = toInt(h.get("team_score"), 0);
                int oppScore = toInt(h.get("opponent_score"), 0);
                gf += teamScore;
                ga += oppScore;
                int matchTot = teamScore + oppScore;

                if (matchTot <= 3) trapLowMatches++;
                if (matchTot <= 4) under45Matches++;
                if (matchTot >= 6) highEventMatches++;
                if (oppScore == 0) shutouts++;
                if (teamScore == 0) failedToScore++;
            }

            // Bayesian Dirichlet-Multinomial shrinkage prevents small-sample saturation
            float effN = histMatches + cWeight;
            vec[0] = (wins + cWeight * priorW) / effN;
            vec[1] = (draws + cWeight * priorD) / effN; // Regulation draws exist in hockey!
            vec[2] = (losses + cWeight * priorL) / effN;
            vec[3] = (gf + cWeight * priorGf) / (effN * 5.0f);
            vec[4] = (ga + cWeight * priorGa) / (effN * 5.0f);
            vec[5] = ((gf - ga) + cWeight * (priorGf - priorGa)) / (effN * 5.0f);
            vec[6] = Math.min(1.0f, (float) histMatches / 10.0f);
            vec[7] = 0.5f;
        } else {
            vec[0] = priorW; vec[1] = priorD; vec[2] = priorL;
            vec[3] = priorGf / 5.0f; vec[4] = priorGa / 5.0f; vec[5] = 0.0f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..17
        vec[8]  = 0.50f; // conceded_first_rate
        vec[9]  = 0.40f; // comeback_equalize_rate
        vec[10] = 0.22f; // comeback_win_rate
        vec[11] = 0.25f; // ppg_losing
        vec[12] = 0.22f; // collapsed_lead_rate
        vec[13] = 0.30f; // blown_lead_points
        vec[14] = histMatches > 0 ? (float) shutouts / histMatches : 0.08f; // shutout_rate
        vec[15] = histMatches > 0 ? (float) failedToScore / histMatches : 0.08f; // failed_to_score_rate
        vec[16] = 0.55f; // avg_max_deficit
        vec[17] = 0.33f; // late_3rd_period_scoring_ratio (empty-net volatility)

        // Periods distribution (P1, P2, P3, OT)
        vec[18] = 0.28f; vec[19] = 0.33f; vec[20] = 0.34f; vec[21] = 0.05f;
        vec[24] = 0.28f; vec[25] = 0.33f; vec[26] = 0.34f; vec[27] = 0.05f;

        // Tactical baselines
        vec[30] = 0.50f; // shots_on_goal_for
        vec[31] = 0.50f; // shots_on_goal_against
        vec[32] = 0.50f; // pace_index (event density)
        vec[33] = 0.915f; // save_pct_baseline
        vec[34] = 0.19f; // power_play_pct
        vec[35] = 0.81f; // penalty_kill_pct
        vec[36] = 0.24f; // overtime_propensity
        vec[37] = 0.70f; // shot_suppression_defensive_wall
        vec[38] = 0.10f; // shooting_conversion_efficiency
        vec[39] = 0.50f; // puck_possession_pct
        vec[40] = 0.35f; // penalty_minutes_density
        vec[41] = histMatches > 0 ? (float) trapLowMatches / histMatches : 0.20f; // neutral_zone_trap_factor (<= 3 goals)
        vec[42] = 0.35f; // period_variance
        vec[43] = histMatches > 0 ? (float) highEventMatches / histMatches : 0.30f; // high_event_ratio (>= 6 goals)
        vec[44] = histMatches > 0 ? (float) under45Matches / histMatches : 0.45f; // low_event_ratio (<= 4 goals)
        vec[45] = 0.28f; // empty_net_volatility
        vec[46] = 0.20f; // b2b_fatigue_penalty
        vec[47] = 0.55f; // home_ice_delta

        // Goaltender & Squad Age
        vec[48] = 0.42f; // avg_age
        vec[49] = 0.20f; // young (<22)
        vec[50] = 0.20f; // veteran (>32)
        vec[51] = 0.38f; // age_std

        // Tournament Motivation from Context (Dims 52..58)
        vec[52] = ctx.getEliminationPressureIndex() != null ? ctx.getEliminationPressureIndex() : 0.0f;
        vec[53] = ctx.getDrawViabilityFlag() != null ? ctx.getDrawViabilityFlag() : 0.0f;
        vec[54] = ctx.getRegionalCardBias() != null ? ctx.getRegionalCardBias() : 0.50f;
        vec[55] = ctx.getBlowoutGradient() != null ? ctx.getBlowoutGradient() : 0.0f;
        vec[56] = ctx.getWinLeverage() != null ? ctx.getWinLeverage() : 0.50f;
        vec[57] = ctx.getEliminationHazard() != null ? ctx.getEliminationHazard() : 0.50f;
        vec[58] = ctx.getPrizeWageRatio() != null ? ctx.getPrizeWageRatio() : 0.05f;

        // Lineup evaluations from ctx (Starters / Netminder)
        evaluateStarters(ctx, vec);

        // ── Real ClickHouse DDS Facts from ice_hockey_match_fact ──────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;

        if (effectiveSofaId != null && effectiveSofaId > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_score, away_score)) as gf, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_score, home_score)) as ga, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_shots, away_shots)) as shots_for, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_shots, home_shots)) as shots_against, " +
                    "avg(is_overtime) as ot_rate, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_score > away_score, away_score > home_score)) as wr, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_p1, away_p1)) as p1_g, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_p2, away_p2)) as p2_g, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_p3, away_p3)) as p3_g, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_p1, home_p1)) as p1_ga, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_p2, home_p2)) as p2_ga, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_p3, home_p3)) as p3_ga, " +
                    "avg(home_score + away_score <= 3) as trap_low_rate, " +
                    "avg(home_score + away_score <= 4) as under_4_5_rate, " +
                    "avg(home_score + away_score >= 6) as high_event_rate, " +
                    "avg(home_score + away_score) as avg_total_goals " +
                    "FROM ice_hockey_match_fact WHERE (home_team_id = " + effectiveSofaId + " OR away_team_id = " + effectiveSofaId + ")";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("hockey_full:" + effectiveSofaId, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double sf = toDouble(r.get("shots_for"), 30.0);
                double sa = toDouble(r.get("shots_against"), 30.0);
                double gfAvg = toDouble(r.get("gf"), 2.7);
                double gaAvg = toDouble(r.get("ga"), 2.7);
                double avgTotal = toDouble(r.get("avg_total_goals"), 5.4);
                double trapRate = toDouble(r.get("trap_low_rate"), 0.20);
                double under45Rate = toDouble(r.get("under_4_5_rate"), 0.45);
                double highEvRate = toDouble(r.get("high_event_rate"), 0.30);

                if (sf > 0) vec[30] = (float) Math.min(1.0, sf / 60.0);
                if (sa > 0) vec[31] = (float) Math.min(1.0, sa / 60.0);

                // Dims 32: Dynamic Pace Index from event density
                vec[32] = (float) Math.max(0.1, Math.min(1.0, avgTotal / 7.0));

                // Dims 33: Goaltender Save Percentage
                if (sa > 0 && gaAvg > 0) {
                    double estSvPct = Math.max(0.85, 1.0 - (gaAvg / Math.max(15.0, sa)));
                    vec[33] = (float) estSvPct;
                } else if (gaAvg > 0) {
                    // When gaAvg is exceptionally low (e.g. 1.8 in NCAA/HockeyEttan), save pct is high
                    vec[33] = gaAvg <= 2.2 ? 0.940f : (gaAvg <= 2.8 ? 0.920f : 0.895f);
                }

                vec[36] = (float) toDouble(r.get("ot_rate"), 0.24);

                // Dims 41: Neutral zone trap factor (<= 3 total goals rate)
                vec[41] = (float) Math.max(vec[41], trapRate);

                // Dims 43 & 44: High Event vs Low Event Rates
                vec[43] = (float) Math.max(vec[43], highEvRate);
                vec[44] = (float) Math.max(vec[44], under45Rate);

                // Shot suppression and shooting conversion
                vec[37] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (gaAvg / Math.max(1.0, sa))));
                vec[38] = (float) Math.max(0.0, Math.min(1.0, gfAvg / Math.max(1.0, sf)));

                // Period scoring distribution
                double p1 = toDouble(r.get("p1_g"), 0.8);
                double p2 = toDouble(r.get("p2_g"), 1.0);
                double p3 = toDouble(r.get("p3_g"), 1.1);
                double totP = Math.max(0.1, p1 + p2 + p3);
                vec[18] = (float) (p1 / totP);
                vec[19] = (float) (p2 / totP);
                vec[20] = (float) (p3 / totP);

                double p1a = toDouble(r.get("p1_ga"), 0.8);
                double p2a = toDouble(r.get("p2_ga"), 1.0);
                double p3a = toDouble(r.get("p3_ga"), 1.1);
                double totPa = Math.max(0.1, p1a + p2a + p3a);
                vec[24] = (float) (p1a / totPa);
                vec[25] = (float) (p2a / totPa);
                vec[26] = (float) (p3a / totPa);
            }
        }

        return l2Normalize(vec);
    }

    private void evaluateStarters(TacticalVectorContext ctx, float[] vec) {
        List<Map<String, Object>> starters = ctx.getStarters();
        if (starters == null || starters.isEmpty()) {
            vec[59] = 0.0f;  // no backup alert
            vec[60] = 0.72f; // netminder rating baseline
            vec[61] = 0.68f; // defense blueline
            vec[62] = 0.68f; // center depth
            vec[63] = 0.70f; // winger forecheck
            return;
        }

        double gkRating = 0.0;
        int gkCount = 0;
        double defRating = 0.0;
        int defCount = 0;
        double fwdRating = 0.0;
        int fwdCount = 0;
        double totalRating = 0.0;
        int totalPlayers = 0;

        for (Map<String, Object> p : starters) {
            double r = toDouble(p.get("rating"), toDouble(p.get("match_rating"), 6.8));
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();

            if (r > 0) {
                totalRating += r;
                totalPlayers++;
            }

            if ("G".equals(pos) || "GK".equals(pos) || "GOALIE".equals(pos)) {
                gkRating += r;
                gkCount++;
            } else if ("D".equals(pos) || "LD".equals(pos) || "RD".equals(pos) || "DEFENSE".equals(pos)) {
                defRating += r;
                defCount++;
            } else {
                fwdRating += r;
                fwdCount++;
            }
        }

        if (gkCount > 0) {
            double avgGk = gkRating / gkCount;
            vec[60] = (float) Math.min(1.0, avgGk / 10.0);
            vec[59] = avgGk < 6.4 ? 1.0f : 0.0f; // Leaky / Backup Netminder Alert
        } else {
            vec[59] = 0.0f;
            vec[60] = 0.72f;
        }

        vec[61] = defCount > 0 ? (float) Math.min(1.0, (defRating / defCount) / 10.0) : 0.68f;
        vec[62] = fwdCount > 0 ? (float) Math.min(1.0, (fwdRating / fwdCount) / 10.0) : 0.68f;
        vec[63] = totalPlayers > 0 ? (float) Math.min(1.0, (totalRating / totalPlayers) / 10.0) : 0.70f;
    }
}
