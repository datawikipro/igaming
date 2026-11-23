package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.volleyball.VolleyballSport;

/**
 * Tactical vector extractor for Volleyball and Volleyball Women.
 * Models set dynamics (best of 5), point differentials (25-pt sets), attack efficiency, block/ace index.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class VolleyballVectorExtractor extends AbstractSportVectorExtractor<VolleyballSport> {

    private final ClickHouseDdsClient clickHouse;
    private final VolleyballSport sport = new VolleyballSport();

    @Autowired
    public VolleyballVectorExtractor(ClickHouseDdsClient clickHouse) {
        this.clickHouse = clickHouse;
    }

    @Override
    public VolleyballSport getSport() {
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

        // ── Contextual MMR Prior (Dims 16..17) ───────────────────────────────────
        Long tid = ctx.getTeamId();
        Long oppTid = ctx.getOpponentId();

        if (tid != null && tid > 0 && oppTid != null && oppTid > 0 && clickHouse != null) {
            clickHouse.getSportMmr(tid, "volleyball-team", "ALL").ifPresent(teamMmr -> {
                clickHouse.getSportMmr(oppTid, "volleyball-team", "ALL").ifPresent(oppMmr -> {
                    double mmrDiff = teamMmr.mmr() - oppMmr.mmr();
                    double phiOpp = oppMmr.rd() / 173.7178;
                    double gPhiOpp = 1.0 / Math.sqrt(1.0 + (3.0 * phiOpp * phiOpp) / (Math.PI * Math.PI));
                    double expectedWinProb = 1.0 / (1.0 + Math.exp(-gPhiOpp * (mmrDiff / 173.7178)));

                    vec[16] = (float) Math.tanh(mmrDiff / 400.0);
                    vec[17] = (float) expectedWinProb;
                });
            });
        }

        // Point-in-Time Tournament Motivation & Dynamic Stakes (Dims 52..58)
        if (ctx.getEliminationPressureIndex() > 0) {
            vec[52] = ctx.getEliminationPressureIndex();
        }
        if (ctx.getDrawViabilityFlag() > 0) {
            vec[53] = ctx.getDrawViabilityFlag();
        }
        if (ctx.getRegionalCardBias() > 0) {
            vec[54] = ctx.getRegionalCardBias();
        }
        vec[55] = ctx.getBlowoutGradient();
        vec[56] = ctx.getWinLeverage();
        vec[57] = ctx.getEliminationHazard();
        vec[58] = ctx.getPrizeWageRatio();

        return l2Normalize(vec);
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        // Set ratio & point differential priors (typical 180 total points per 4-set match)
        vec[8] = 0.50f; vec[9] = 0.50f; vec[10] = 0.50f; vec[11] = 0.50f;
        vec[12] = 0.50f; vec[13] = 0.50f; vec[14] = 0.0f; vec[15] = 0.0f;
        vec[16] = 0.50f; vec[17] = 0.50f;
        for (int i = 0; i < 12; i++) {
            vec[18 + i] = 0.50f;
        }
        // Attack efficiency & Reception quality
        vec[30] = 0.52f; // Attack kill rate
        vec[31] = 0.18f; // Attack error rate
        vec[32] = 0.55f; // Perfect reception %
        vec[33] = 0.12f; // Ace per set proxy
        vec[34] = 0.22f; // Block per set proxy
        for (int i = 35; i <= 59; i++) {
            vec[i] = 0.50f;
        }
        // Key position ratings: Setter, Outside Hitter, Middle Blocker, Libero
        vec[60] = 0.70f;
        vec[61] = 0.70f;
        vec[62] = 0.70f;
        vec[63] = 0.70f;
    }
}
