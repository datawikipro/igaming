package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.IceHockeyAnalyticsRepository;
import pro.datawiki.igaming.dto.sport.hockey.IceHockeySport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Ice Hockey.
 * Queries ice_hockey_team_advanced_v directly from ClickHouse DDS via declarative repository (Zero Owls, Zero Sines).
 * Models period-by-period scoring, overtime propensity, event pace, defensive trap dynamics,
 * slot suppression, and goaltender impact.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class IceHockeyVectorExtractor extends AbstractSportVectorExtractor<IceHockeySport> {

    private final ClickHouseDdsClient clickHouse;
    private final IceHockeyAnalyticsRepository hockeyRepository;
    private final IceHockeySport sport = new IceHockeySport();

    @Autowired
    public IceHockeyVectorExtractor(ClickHouseDdsClient clickHouse, IceHockeyAnalyticsRepository hockeyRepository) {
        this.clickHouse = clickHouse;
        this.hockeyRepository = hockeyRepository;
    }

    public IceHockeyVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

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
        populateBaselinePriors(vec);

        // Evaluate Starters from Context if present (Dims 60..63)
        evaluateStarters(ctx, vec);

        // ── Team-Level Contextual MMR Prior (Dims 16..17) ─────────────────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;
        Long oppTid = ctx.getOpponentId();

        if (tid != null && tid > 0) {
            clickHouse.getSportMmr(tid, "ice-hockey-team", "ALL").ifPresent(teamMmr -> {
                if (oppTid != null && oppTid > 0) {
                    clickHouse.getSportMmr(oppTid, "ice-hockey-team", "ALL").ifPresent(oppMmr -> {
                        double mmrDiff = teamMmr.mmr() - oppMmr.mmr();
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

        if (effectiveSofaId != null && effectiveSofaId > 0 && hockeyRepository != null) {
            hockeyRepository.findAdvancedStatsByTeamId(effectiveSofaId).ifPresent(r -> {
                if (r.totalMatches() > 0) {
                    double sf = r.avgSf();
                    double sa = r.avgSa();
                    double gaAvg = r.avgGa();
                    double avgTotal = r.avgTotalGoals();
                    double trapRate = r.trapLowRate();
                    double under45Rate = r.under45Rate();
                    double highEvRate = r.highEventRate();

                    if (sf > 0) vec[30] = (float) Math.min(1.0, sf / 60.0);
                    if (sa > 0) vec[31] = (float) Math.min(1.0, sa / 60.0);

                    // Dims 32: Dynamic Pace Index from event density
                    vec[32] = (float) Math.max(0.1, Math.min(1.0, avgTotal / 7.0));

                    // Dims 33..36: Goaltender, Conversion & Possession Physics
                    vec[33] = (float) r.savePct();
                    vec[34] = (float) r.shootingPct();
                    vec[35] = (float) r.corsiForPct();
                    vec[36] = (float) r.otRate();

                    // Dims 37: Shot suppression
                    vec[37] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (gaAvg / Math.max(1.0, sa))));

                    // Dims 38..39: PDO Index and Deviation (Pure Observable Physics)
                    double pdoIndex = r.pdoIndex();
                    double pdoDev = r.pdoLuckDeviation();
                    vec[38] = (float) Math.max(0.0, Math.min(1.5, pdoIndex / 1000.0));
                    vec[39] = (float) Math.max(0.0, Math.min(1.0, (pdoDev / 100.0) + 0.5));

                    // Dims 40..42: Neutral zone trap & Event Rates
                    vec[40] = (float) Math.max(vec[40], trapRate);
                    vec[41] = (float) Math.max(vec[41], under45Rate);
                    vec[42] = (float) Math.max(vec[42], highEvRate);

                    // Period scoring distribution
                    double p1 = r.p1Gf();
                    double p2 = r.p2Gf();
                    double p3 = r.p3Gf();
                    double totGf = Math.max(0.1, p1 + p2 + p3);
                    vec[18] = (float) (p1 / totGf);
                    vec[19] = (float) (p2 / totGf);
                    vec[20] = (float) (p3 / totGf);
                    vec[21] = (float) Math.min(0.15, 0.1 / totGf);
                }
            });
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
            Long pid = toLong(p.get("sofascore_id"));
            if (pid == null) pid = toLong(p.get("id"));
            if (pid != null && pid > 0) {
                var pMmrOpt = clickHouse.getSportMmr(pid, "ice-hockey-player", "ALL");
                if (pMmrOpt.isPresent()) {
                    double pMmr = pMmrOpt.get().mmr();
                    double mmrWeight = Math.max(0.70, Math.min(1.40, 1.0 + (pMmr - 1500.0) / 2800.0));
                    r = r * mmrWeight;
                }
            }
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();

            if ("G".equals(pos) || "GK".equals(pos)) {
                goalieRating += r;
                goalieCount++;
            } else if ("D".equals(pos) || "LD".equals(pos) || "RD".equals(pos)) {
                defRating += r;
                defCount++;
            } else {
                fwdRating += r;
                fwdCount++;
            }
        }

        if (goalieCount > 0) vec[60] = (float) Math.min(1.0, (goalieRating / goalieCount) / 10.0);
        if (defCount > 0) vec[61] = (float) Math.min(1.0, (defRating / defCount) / 10.0);
        if (fwdCount > 0) vec[62] = (float) Math.min(1.0, (fwdRating / fwdCount) / 10.0);
        vec[63] = (float) Math.min(1.0, ((goalieRating + defRating + fwdRating) / starters.size()) / 10.0);
    }

    private double parsePlayerRating(Map<String, Object> p) {
        if (p == null) return 6.8;
        Object rObj = p.get("effective_rating");
        if (rObj == null) rObj = p.get("rating");
        if (rObj == null) rObj = p.get("match_rating");
        double r = toDouble(rObj, 6.8);
        return (r >= 2.0 && r <= 10.0) ? r : 6.8;
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        vec[8] = 0.50f; vec[9] = 0.40f; vec[10] = 0.22f; vec[11] = 0.25f;
        vec[12] = 0.22f; vec[13] = 0.30f; vec[14] = 0.08f; vec[15] = 0.08f;
        vec[16] = 0.55f; vec[17] = 0.33f;
        float[] bins = {0.15f, 0.16f, 0.17f, 0.17f, 0.17f, 0.18f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.77f; vec[33] = 0.915f;
        vec[34] = 0.10f; vec[35] = 0.50f; vec[36] = 0.24f; vec[37] = 0.50f;
        vec[38] = 1.00f; vec[39] = 0.50f; vec[40] = 0.20f; vec[41] = 0.45f;
        vec[42] = 0.30f; vec[43] = 0.30f; vec[44] = 0.50f; vec[45] = 0.30f;
        vec[46] = 0.20f; vec[47] = 0.50f;
        vec[48] = 0.42f; vec[49] = 0.20f; vec[50] = 0.20f; vec[51] = 0.38f;
        vec[52] = 0.60f; vec[53] = 0.40f; vec[54] = 0.70f; vec[55] = 0.0f;
        vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.20f;
        vec[60] = 0.70f; vec[61] = 0.70f; vec[62] = 0.70f; vec[63] = 0.70f;
    }
}
