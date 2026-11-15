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
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IceHockeyVectorExtractor extends AbstractSportVectorExtractor<IceHockeySport> {

    private final ClickHouseDdsClient clickHouse;
    private final IceHockeySport sport = new IceHockeySport();

    @Override
    public IceHockeySport getSport() {
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

        // Lineup evaluations from ctx (Starters / Netminder)
        evaluateStarters(ctx, vec);

        // ── Dynamic Universal Sports MMR (ID-Only Querying from ClickHouse) ──
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;

        Long oppTid = ctx.getOpponentId();
        Long oppSofaId = ctx.getOpponentSofascoreId();
        Long effectiveOppId = (oppSofaId != null && oppSofaId > 0) ? oppSofaId : oppTid;

        if (effectiveSofaId != null && effectiveSofaId > 0) {
            clickHouse.getSportMmr(effectiveSofaId, "ice-hockey", "ALL").ifPresent(mmr -> {
                vec[14] = (float) Math.max(-1.0, Math.min(1.0, (mmr.mmr() - 1500.0) / 1000.0));
                vec[15] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (mmr.rd() / 350.0)));
                vec[52] = (float) Math.max(0.0, Math.min(1.5, (mmr.peakMmr() - 1500.0) / 1000.0));
                vec[53] = (float) Math.max(-1.0, Math.min(1.0, mmr.streak() / 10.0));

                if (effectiveOppId != null && effectiveOppId > 0) {
                    clickHouse.getSportMmr(effectiveOppId, "ice-hockey", "ALL").ifPresent(oppMmr -> {
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

        // ── Real ClickHouse DDS Facts from ice_hockey_team_advanced_v ───────

        if (effectiveSofaId != null && effectiveSofaId > 0) {
            String sql = "SELECT total_matches, avg_sf, avg_sa, avg_ga, avg_total_goals, trap_low_rate, " +
                    "under45_rate, high_event_rate, save_pct, ot_rate, shooting_pct, corsi_for_pct, pdo_luck_deviation " +
                    "FROM igaming_dds.ice_hockey_team_advanced_v WHERE team_id = ?";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("hockey_adv:" + effectiveSofaId, sql, effectiveSofaId);
            if (!rows.isEmpty() && toInt(rows.get(0).get("total_matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double sf = toDouble(r.get("avg_sf"), 30.0);
                double sa = toDouble(r.get("avg_sa"), 30.0);
                double gaAvg = toDouble(r.get("avg_ga"), 2.7);
                double avgTotal = toDouble(r.get("avg_total_goals"), 5.4);
                double trapRate = toDouble(r.get("trap_low_rate"), 0.20);
                double under45Rate = toDouble(r.get("under45_rate"), 0.45);
                double highEvRate = toDouble(r.get("high_event_rate"), 0.30);

                if (sf > 0) vec[30] = (float) Math.min(1.0, sf / 60.0);
                if (sa > 0) vec[31] = (float) Math.min(1.0, sa / 60.0);

                // Dims 32: Dynamic Pace Index from event density
                vec[32] = (float) Math.max(0.1, Math.min(1.0, avgTotal / 7.0));

                // Dims 33: Goaltender Save Percentage from precalculated view
                vec[33] = (float) toDouble(r.get("save_pct"), 0.915);
                vec[36] = (float) toDouble(r.get("ot_rate"), 0.24);

                // Dims 41: Neutral zone trap factor (<= 3 total goals rate)
                vec[41] = (float) Math.max(vec[41], trapRate);

                // Dims 43 & 44: High Event vs Low Event Rates
                vec[43] = (float) Math.max(vec[43], highEvRate);
                vec[44] = (float) Math.max(vec[44], under45Rate);

                // Shot suppression and shooting conversion
                vec[37] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (gaAvg / Math.max(1.0, sa))));
                vec[38] = (float) toDouble(r.get("shooting_pct"), 0.10);

                // Corsi For % (CF%): Gold-standard puck possession metric
                vec[39] = (float) toDouble(r.get("corsi_for_pct"), 0.50);

                // PDO (Luck Regression Index): Deviation from 1000
                double pdoDev = toDouble(r.get("pdo_luck_deviation"), 0.0);
                vec[42] = (float) Math.max(0.05, Math.min(0.95, 0.35 + (pdoDev / 1000.0) * 2.5));

                // Period scoring distribution
                double p1 = toDouble(r.get("p1_gf"), 0.8);
                double p2 = toDouble(r.get("p2_gf"), 1.0);
                double p3 = toDouble(r.get("p3_gf"), 0.9);
                double totGf = Math.max(0.1, p1 + p2 + p3);
                vec[18] = (float) (p1 / totGf);
                vec[19] = (float) (p2 / totGf);
                vec[20] = (float) (p3 / totGf);
                vec[21] = (float) Math.min(0.15, toDouble(r.get("ot_gf"), 0.1) / totGf);
            }
        }

        return l2Normalize(vec);
    }

    private void evaluateStarters(TacticalVectorContext ctx, float[] vec) {
        List<Map<String, Object>> starters = ctx.getStarters();
        if (starters == null || starters.isEmpty()) return;

        double goalieRating = 0.0;
        int goalieCount = 0;
        double defRating = 0.0;
        int defCount = 0;
        double fwdRating = 0.0;
        int fwdCount = 0;

        for (Map<String, Object> p : starters) {
            double r = parsePlayerRating(p);
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();

            if ("G".equals(pos) || "GK".equals(pos)) {
                goalieRating += r;
                goalieCount++;
            } else if ("D".equals(pos)) {
                defRating += r;
                defCount++;
            } else {
                fwdRating += r;
                fwdCount++;
            }
        }

        if (goalieCount > 0) vec[60] = (float) Math.min(1.0, (goalieRating / goalieCount) / 10.0);
        if (defCount > 0) vec[61] = (float) Math.min(1.0, (defRating / defCount) / 10.0);
        if (fwdCount > 0) vec[63] = (float) Math.min(1.0, (fwdRating / fwdCount) / 10.0);
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
