package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.BasketballAnalyticsRepository;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.BasketballAdvancedFactDto;
import pro.datawiki.igaming.dto.sport.basketball.BasketballWomenSport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tactical vector extractor for Women's Basketball (WNBA, EuroLeague Women, NCAA Women).
 * Calibrated specifically for Women's Basketball physical dynamics:
 * - 40 minutes regulation time, Size 6 ball (28.5")
 * - Centered pace proxy around 140.0 pts (vs 180.0 in men's 48m NBA)
 * - Calibrated offensive rating center (65.0 pts) and defensive rating center (85.0 pts)
 * - Higher blowout propensity and clutch dispersion
 */
@Slf4j
@Component
public class BasketballWomenVectorExtractor extends AbstractSportVectorExtractor<BasketballWomenSport> {

    private final ClickHouseDdsClient clickHouse;
    private final BasketballAnalyticsRepository basketballRepository;
    private final BasketballWomenSport sport = new BasketballWomenSport();

    @Autowired
    public BasketballWomenVectorExtractor(ClickHouseDdsClient clickHouse, BasketballAnalyticsRepository basketballRepository) {
        this.clickHouse = clickHouse;
        this.basketballRepository = basketballRepository;
    }

    public BasketballWomenVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

    @Override
    public BasketballWomenSport getSport() {
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

        // ── Canonical Duration & Pace Scaling (40 minutes regulation) ───────────
        int regMinutes = ctx.getRegulationMinutes() != null && ctx.getRegulationMinutes() > 0
                ? ctx.getRegulationMinutes()
                : sport.getDefaultRegulationMinutes();
        double durationScale = regMinutes == 48 ? (40.0 / 48.0) : 1.0;

        // ── Team-Level Contextual MMR Prior (Dims 16..17) ─────────────────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long oppTid = ctx.getOpponentId();

        if (tid != null && tid > 0) {
            clickHouse.getSportMmr(tid, "basketball-team", "ALL").ifPresent(teamMmr -> {
                if (oppTid != null && oppTid > 0) {
                    clickHouse.getSportMmr(oppTid, "basketball-team", "ALL").ifPresent(oppMmr -> {
                        double mmrDiff = teamMmr.mmr() - oppMmr.mmr();
                        double phiOpp = oppMmr.rd() / 173.7178;
                        double gPhiOpp = 1.0 / Math.sqrt(1.0 + (3.0 * phiOpp * phiOpp) / (Math.PI * Math.PI));
                        double expectedWinProb = 1.0 / (1.0 + Math.exp(-gPhiOpp * (mmrDiff / 173.7178)));

                        vec[16] = (float) Math.tanh(mmrDiff / 350.0);
                        vec[17] = (float) expectedWinProb;
                    });
                }
            });
        }

        List<Long> targetTeamIds = new ArrayList<>();
        if (tid != null && tid > 0) targetTeamIds.add(tid);
        if (sofaId != null && sofaId > 0 && !sofaId.equals(tid)) targetTeamIds.add(sofaId);

        if (!targetTeamIds.isEmpty() && basketballRepository != null) {
            Optional<BasketballAdvancedFactDto> opt = basketballRepository.findAdvancedStatsByTeamIds(targetTeamIds);
            if (opt.isPresent()) {
                BasketballAdvancedFactDto r = opt.get();
                if (r.totalMatches() > 0) {
                    double pf = r.avgPtsFor() * durationScale;
                    double pa = r.avgPtsAgainst() * durationScale;

                    // Calibrated for Women's Basketball (Mean Total ~140.0 pts)
                    vec[30] = (float) Math.max(0.0, Math.min(1.0, (pf + pa - 140.0) / 60.0)); // Pace proxy centered at 140.0
                    vec[31] = (float) Math.max(0.0, Math.min(1.0, (pf - 65.0) / 40.0));       // Off rating centered at 65.0
                    vec[32] = (float) Math.max(0.0, Math.min(1.0, (95.0 - pa) / 40.0));       // Def rating centered at 75.0
                    vec[33] = (float) r.netRatingProxy();

                    // Shot efficiency proxies (eFG% & True Shooting %)
                    vec[34] = (float) r.efgPctProxy();
                    vec[35] = (float) r.trueShootingPctProxy();

                    double q1 = r.avgQ1();
                    double q2 = r.avgQ2();
                    double q3 = r.avgQ3();
                    double q4 = r.avgQ4();
                    double tot = Math.max(1.0, q1 + q2 + q3 + q4);
                    vec[18] = (float) (q1 / tot);
                    vec[19] = (float) (q2 / tot);
                    vec[20] = (float) (q3 / tot);
                    vec[21] = (float) (q4 / tot);

                    // Q4 execution relative to average quarter
                    vec[17] = (float) r.q4ScoringShare();

                    // Clutch Net Rating and Spread Dispersion (Dims 48..51)
                    double netDiff = pf - pa;
                    vec[48] = (float) Math.max(0.0, Math.min(1.0, 0.5 + (netDiff / 25.0)));
                    vec[49] = Math.abs(netDiff) >= 12.0 ? 1.0f : 0.0f; // blowout propensity (higher in women's basketball)
                    vec[50] = (float) r.otRate();
                    vec[51] = vec[30];

                    // Rim protection and interior defense (Dims 60..63)
                    vec[60] = vec[32];
                    vec[61] = vec[31];
                    vec[62] = vec[33];
                    vec[63] = vec[34];
                }
            }
        }

        return l2Normalize(vec);
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        vec[8] = 0.50f; vec[9] = 0.45f; vec[10] = 0.28f; vec[11] = 0.30f;
        vec[12] = 0.25f; vec[13] = 0.35f; vec[14] = 0.0f; vec[15] = 0.0f;
        vec[16] = 0.60f; vec[17] = 0.28f;
        float[] bins = {0.25f, 0.25f, 0.25f, 0.25f, 0.0f, 0.0f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.50f; vec[33] = 0.0f;
        vec[34] = 0.0f; vec[35] = 0.70f; vec[36] = 0.30f; vec[37] = 0.50f;
        vec[38] = 0.50f; vec[39] = 0.50f; vec[40] = 0.85f; vec[41] = 0.60f;
        vec[42] = 0.60f; vec[43] = 0.40f; vec[44] = 0.50f; vec[45] = 0.40f;
        vec[46] = 0.0f; vec[47] = 0.50f;
        vec[48] = 0.39f; vec[49] = 0.22f; vec[50] = 0.18f; vec[51] = 0.36f;
        vec[52] = 0.85f; vec[53] = 0.37f; vec[54] = 0.72f; vec[55] = 0.0f;
        vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.25f;
        vec[60] = 0.70f; vec[61] = 0.70f; vec[62] = 0.70f; vec[63] = 0.70f;
    }
}
