package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.BasketballAnalyticsRepository;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.BasketballAdvancedFactDto;
import pro.datawiki.igaming.dto.sport.basketball.ClassicBasketballSport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tactical vector extractor for Basketball.
 * Queries basketball_team_advanced_v via declarative ClickHouse repository (Zero Owls, Zero Sines).
 * Models quarter-by-quarter pace, clutch time Q4 execution, and rim protector impact.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class BasketballVectorExtractor extends AbstractSportVectorExtractor<ClassicBasketballSport> {

    private final ClickHouseDdsClient clickHouse;
    private final BasketballAnalyticsRepository basketballRepository;
    private final ClassicBasketballSport sport = new ClassicBasketballSport();

    @Autowired
    public BasketballVectorExtractor(ClickHouseDdsClient clickHouse, BasketballAnalyticsRepository basketballRepository) {
        this.clickHouse = clickHouse;
        this.basketballRepository = basketballRepository;
    }

    public BasketballVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

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

        // ── Scaling for 40-minute (FIBA/Euroleague) vs 48-minute (NBA) games ──────
        double durationScale = 1.0;
        String tName = ctx.getTournamentName() != null ? ctx.getTournamentName().toUpperCase() : "";
        if (!tName.contains("NBA") && !tName.contains("CBA")) {
            durationScale = 40.0 / 48.0; // scale 48m NBA baseline to 40m FIBA
        }

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

                        vec[16] = (float) Math.tanh(mmrDiff / 400.0);
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
                    vec[30] = (float) Math.max(0.0, Math.min(1.0, (pf + pa - 180.0) / 80.0)); // Pace proxy
                    vec[31] = (float) Math.max(0.0, Math.min(1.0, (pf - 80.0) / 50.0));       // Off rating
                    vec[32] = (float) Math.max(0.0, Math.min(1.0, (130.0 - pa) / 50.0));      // Def rating
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
                    vec[48] = (float) Math.max(0.0, Math.min(1.0, 0.5 + (netDiff / 30.0))); // clutch net rating
                    vec[49] = Math.abs(netDiff) >= 10.0 ? 1.0f : 0.0f;                      // blowout propensity
                    vec[50] = (float) r.otRate();                                           // overtime frequency
                    vec[51] = vec[30];                                                      // pace standardized

                    // Rim protection and interior defense (Dims 60..63)
                    vec[60] = vec[32]; // rim protection rating
                    vec[61] = vec[31]; // off rating
                    vec[62] = vec[33]; // net rating
                    vec[63] = vec[34]; // efg%
                }
            }
        }

        return l2Normalize(vec);
    }
}
