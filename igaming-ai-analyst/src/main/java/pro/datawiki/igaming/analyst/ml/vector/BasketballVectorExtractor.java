package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.basketball.ClassicBasketballSport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Basketball.
 * Queries basketball_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Models quarter-by-quarter pace, clutch time Q4 execution, and rim protector impact.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BasketballVectorExtractor implements SportVectorExtractor<ClassicBasketballSport> {

    private final ClickHouseDdsClient clickHouse;
    private final ClassicBasketballSport sport = new ClassicBasketballSport();

    @Override
    public ClassicBasketballSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── League Regulation Duration & Pace Normalization (from DB context or sport default) ──
        int regMinutes = (ctx.getRegulationMinutes() != null && ctx.getRegulationMinutes() > 0)
                ? ctx.getRegulationMinutes()
                : sport.getDefaultRegulationMinutes();
        double paceMult = (ctx.getPaceMultiplier() != null && ctx.getPaceMultiplier() > 0.0)
                ? ctx.getPaceMultiplier()
                : 1.0;
        float durationScale = (40.0f / (float) regMinutes) * (float) (1.0 / paceMult);

        // ── Dims 0..7: Macro Win/Loss/Points ──────────────────────────────────
        float priorW = 0.50f;
        float priorPts = 105.0f;
        float cWeight = 4.0f;

        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0;
            float totalPtsFor = 0.0f, totalPtsAgainst = 0.0f;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;

                totalPtsFor += (float) toInt(h.get("team_score"), 0) * durationScale;
                totalPtsAgainst += (float) toInt(h.get("opponent_score"), 0) * durationScale;
            }

            float effN = n + cWeight;
            vec[0] = (wins + cWeight * priorW) / effN;
            vec[1] = 0.0f; // No draws in basketball!
            vec[2] = ((n - wins) + cWeight * priorW) / effN;
            vec[3] = (totalPtsFor + cWeight * priorPts) / (effN * 120.0f);
            vec[4] = (totalPtsAgainst + cWeight * priorPts) / (effN * 120.0f);
            vec[5] = (totalPtsFor - totalPtsAgainst) / (effN * 30.0f);
            vec[6] = Math.min(1.0f, (float) n / 10.0f);
            vec[7] = 0.5f;
        } else {
            vec[0] = 0.50f; vec[1] = 0.0f; vec[2] = 0.50f;
            vec[3] = 0.50f; vec[4] = 0.50f; vec[5] = 0.0f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..63
        vec[8]  = 0.50f; // conceded_first_rate
        vec[9]  = 0.45f; // comeback_equalize_rate
        vec[10] = 0.28f; // comeback_win_rate
        vec[11] = 0.30f; // ppg_losing
        vec[12] = 0.25f; // collapsed_lead_rate
        vec[13] = 0.35f; // blown_lead_points
        vec[14] = 0.0f;  // clean sheet non-existent
        vec[15] = 0.0f;  // failed to score non-existent
        vec[16] = 0.60f; // avg_max_deficit
        vec[17] = 0.28f; // q4_clutch_scoring_ratio

        // Quarters distribution (Q1, Q2, Q3, Q4)
        vec[18] = 0.25f; vec[19] = 0.25f; vec[20] = 0.25f; vec[21] = 0.25f;
        vec[24] = 0.25f; vec[25] = 0.25f; vec[26] = 0.25f; vec[27] = 0.25f;

        // Tactical baselines
        vec[30] = 0.50f; // pace_proxy
        vec[31] = 0.50f; // offensive_rating
        vec[32] = 0.50f; // defensive_rating
        vec[33] = 0.50f; // net_rating
        vec[34] = 0.52f; // efg_pct (52%)
        vec[35] = 0.56f; // true_shooting_pct (56%)
        vec[36] = 0.13f; // turnover_pct (13%)
        vec[37] = 0.28f; // offensive_rebound_pct
        vec[38] = 0.76f; // free_throw_pct (76%)
        vec[39] = 0.38f; // three_point_rate (38%)
        vec[40] = 0.45f; // paint_points_share

        // Roster & Rim Protector Dynamics
        vec[48] = 0.39f; // avg_starter_age (~26.5 yrs)
        vec[49] = 0.22f; // young
        vec[50] = 0.18f; // veteran
        vec[51] = 0.36f; // age_std
        vec[52] = ctx.getEliminationPressureIndex();
        vec[53] = ctx.getDrawViabilityFlag();
        vec[54] = ctx.getRegionalCardBias();
        vec[60] = 0.72f; // rim_protector_rating (center)
        vec[61] = 0.70f; // perimeter_defender_rating
        vec[62] = 0.72f; // playmaker_rating
        vec[63] = 0.72f; // bucket_getter_rating

        // ── Real ClickHouse DDS Facts from basketball_match_fact ──────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;

        if (effectiveSofaId != null && effectiveSofaId > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_score, away_score)) as pts_for, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", away_score, home_score)) as pts_against, " +
                    "avg(is_overtime) as ot_rate, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_score > away_score, away_score > home_score)) as wr, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_q1, away_q1)) as q1, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_q2, away_q2)) as q2, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_q3, away_q3)) as q3, " +
                    "avg(if(home_team_id = " + effectiveSofaId + ", home_q4, away_q4)) as q4 " +
                    "FROM basketball_match_fact WHERE (home_team_id = " + effectiveSofaId + " OR away_team_id = " + effectiveSofaId + ")";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("bball:" + effectiveSofaId, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double pf = toDouble(r.get("pts_for"), 105.0) * durationScale;
                double pa = toDouble(r.get("pts_against"), 105.0) * durationScale;
                vec[30] = (float) Math.max(0.0, Math.min(1.0, (pf + pa - 180.0) / 80.0)); // Pace proxy
                vec[31] = (float) Math.max(0.0, Math.min(1.0, (pf - 80.0) / 50.0));       // Off rating
                vec[32] = (float) Math.max(0.0, Math.min(1.0, (130.0 - pa) / 50.0));      // Def rating

                double q1 = toDouble(r.get("q1"), 26.0);
                double q2 = toDouble(r.get("q2"), 26.0);
                double q3 = toDouble(r.get("q3"), 26.0);
                double q4 = toDouble(r.get("q4"), 26.0);
                double tot = Math.max(1.0, q1 + q2 + q3 + q4);
                vec[18] = (float) (q1 / tot);
                vec[19] = (float) (q2 / tot);
                vec[20] = (float) (q3 / tot);
                vec[21] = (float) (q4 / tot);
            }
        }

        return l2Normalize(vec);
    }
}
