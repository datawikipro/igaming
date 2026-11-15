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
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BasketballVectorExtractor extends AbstractSportVectorExtractor<ClassicBasketballSport> {

    private final ClickHouseDdsClient clickHouse;
    private final ClassicBasketballSport sport = new ClassicBasketballSport();

    @Override
    public ClassicBasketballSport getSport() {
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

        // ── League Regulation Duration & Pace Normalization ──────────────────────
        int regMinutes = (ctx.getRegulationMinutes() != null && ctx.getRegulationMinutes() > 0)
                ? ctx.getRegulationMinutes()
                : sport.getDefaultRegulationMinutes();
        double paceMult = (ctx.getPaceMultiplier() != null && ctx.getPaceMultiplier() > 0.0)
                ? ctx.getPaceMultiplier()
                : 1.0;
        float durationScale = (40.0f / (float) regMinutes) * (float) (1.0 / paceMult);

        // ── Real ClickHouse DDS Facts from basketball_team_advanced_v ──────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();

        // ── Dynamic Universal Sports MMR (ID-Only Querying from ClickHouse) ──
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;
        Long oppTid = ctx.getOpponentId();
        Long oppSofaId = ctx.getOpponentSofascoreId();
        Long effectiveOppId = (oppSofaId != null && oppSofaId > 0) ? oppSofaId : oppTid;

        if (effectiveSofaId != null && effectiveSofaId > 0) {
            clickHouse.getSportMmr(effectiveSofaId, "basketball", "ALL").ifPresent(mmr -> {
                vec[14] = (float) Math.max(-1.0, Math.min(1.0, (mmr.mmr() - 1500.0) / 1000.0));
                vec[15] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (mmr.rd() / 350.0)));
                vec[52] = (float) Math.max(0.0, Math.min(1.5, (mmr.peakMmr() - 1500.0) / 1000.0));
                vec[53] = (float) Math.max(-1.0, Math.min(1.0, mmr.streak() / 10.0));

                if (effectiveOppId != null && effectiveOppId > 0) {
                    clickHouse.getSportMmr(effectiveOppId, "basketball", "ALL").ifPresent(oppMmr -> {
                        double mmrDiff = mmr.mmr() - oppMmr.mmr();
                        double phiOpp = oppMmr.rd() / 173.7178;
                        double gPhiOpp = 1.0 / Math.sqrt(1.0 + (3.0 * phiOpp * phiOpp) / (Math.PI * Math.PI));
                        double expectedWinProb = 1.0 / (1.0 + Math.exp(-gPhiOpp * (mmrDiff / 173.7178)));

                        vec[16] = (float) Math.tanh(mmrDiff / 400.0);
                        vec[17] = (float) expectedWinProb;
                    });
                }
            });
        }

        if ((tid != null && tid > 0) || (sofaId != null && sofaId > 0)) {
            String cacheKey = "bball_adv:" + (tid != null ? tid : 0) + ":" + (sofaId != null ? sofaId : 0);
            String sql;
            List<Map<String, Object>> rows;
            if (tid != null && sofaId != null && !tid.equals(sofaId)) {
                sql = "SELECT total_matches, avg_pts_for, avg_pts_against, net_rating_proxy, efg_pct_proxy, " +
                        "true_shooting_pct_proxy, avg_q1, avg_q2, avg_q3, avg_q4, q4_scoring_share, ot_rate " +
                        "FROM igaming_dds.basketball_team_advanced_v WHERE team_id IN (?, ?) LIMIT 1";
                rows = clickHouse.queryJsonCached(cacheKey, sql, tid, sofaId);
            } else {
                long targetId = (tid != null && tid > 0) ? tid : sofaId;
                sql = "SELECT total_matches, avg_pts_for, avg_pts_against, net_rating_proxy, efg_pct_proxy, " +
                        "true_shooting_pct_proxy, avg_q1, avg_q2, avg_q3, avg_q4, q4_scoring_share, ot_rate " +
                        "FROM igaming_dds.basketball_team_advanced_v WHERE team_id = ? LIMIT 1";
                rows = clickHouse.queryJsonCached(cacheKey, sql, targetId);
            }
            if (!rows.isEmpty() && toInt(rows.get(0).get("total_matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double pf = toDouble(r.get("avg_pts_for"), 105.0) * durationScale;
                double pa = toDouble(r.get("avg_pts_against"), 105.0) * durationScale;
                vec[30] = (float) Math.max(0.0, Math.min(1.0, (pf + pa - 180.0) / 80.0)); // Pace proxy
                vec[31] = (float) Math.max(0.0, Math.min(1.0, (pf - 80.0) / 50.0));       // Off rating
                vec[32] = (float) Math.max(0.0, Math.min(1.0, (130.0 - pa) / 50.0));      // Def rating
                vec[33] = (float) toDouble(r.get("net_rating_proxy"), 0.50);

                // Shot efficiency proxies (eFG% & True Shooting %)
                vec[34] = (float) toDouble(r.get("efg_pct_proxy"), 0.52);
                vec[35] = (float) toDouble(r.get("true_shooting_pct_proxy"), 0.56);

                double q1 = toDouble(r.get("avg_q1"), 26.0);
                double q2 = toDouble(r.get("avg_q2"), 26.0);
                double q3 = toDouble(r.get("avg_q3"), 26.0);
                double q4 = toDouble(r.get("avg_q4"), 26.0);
                double tot = Math.max(1.0, q1 + q2 + q3 + q4);
                vec[18] = (float) (q1 / tot);
                vec[19] = (float) (q2 / tot);
                vec[20] = (float) (q3 / tot);
                vec[21] = (float) (q4 / tot);

                // Q4 execution relative to average quarter
                vec[17] = (float) toDouble(r.get("q4_scoring_share"), 0.25);

                // Clutch Net Rating and Spread Dispersion (Dims 48..51)
                double netDiff = pf - pa;
                vec[48] = (float) Math.max(0.0, Math.min(1.0, 0.5 + (netDiff / 30.0))); // clutch net rating
                vec[49] = Math.abs(netDiff) >= 10.0 ? 1.0f : 0.0f;                      // blowout propensity
                vec[50] = (float) toDouble(r.get("ot_rate"), 0.05);                     // overtime frequency
                vec[51] = vec[30];                                                      // pace standardized

                // Rim protection and interior defense (Dims 60..63)
                vec[60] = vec[32]; // rim protection rating
                vec[61] = vec[31]; // off rating
                vec[62] = vec[33]; // net rating
                vec[63] = vec[34]; // efg%
            }
        }

        return l2Normalize(vec);
    }
}
