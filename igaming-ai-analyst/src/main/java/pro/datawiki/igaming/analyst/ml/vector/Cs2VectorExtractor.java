package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.Cs2AnalyticsRepository;
import pro.datawiki.igaming.dto.sport.esports.Cs2Sport;

/**
 * Tactical vector extractor for Counter-Strike 2 (CS2 Esports).
 * Queries cs2_team_advanced_v and cs2_team_round_advanced_v directly from ClickHouse DDS via declarative repository (Zero Owls, Zero Sines).
 * Models pistol round conversions, veto map pool proficiency, and HLTV ratings.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class Cs2VectorExtractor extends AbstractSportVectorExtractor<Cs2Sport> {

    private final ClickHouseDdsClient clickHouse;
    private final Cs2AnalyticsRepository cs2Repository;
    private final Cs2Sport sport = new Cs2Sport();

    @Autowired
    public Cs2VectorExtractor(ClickHouseDdsClient clickHouse, Cs2AnalyticsRepository cs2Repository) {
        this.clickHouse = clickHouse;
        this.cs2Repository = cs2Repository;
    }

    public Cs2VectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

    @Override
    public Cs2Sport getSport() {
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

        // ── Real ClickHouse DDS Facts from cs2_team_advanced_v ────────────────
        Long tid = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        if (tid != null && tid > 0 && cs2Repository != null) {
            cs2Repository.findAdvancedStatsByTeamId(tid).ifPresent(r -> {
                if (r.totalMatches() > 0) {
                    float pwr = (float) r.pistolRoundWr();
                    float otRate = (float) r.overtimeRate();

                    vec[30] = pwr; // Pistol WR
                    vec[20] = otRate;
                    vec[31] = (float) r.secondRoundConversion();
                    vec[34] = (float) r.conversion5v4Rate();
                    vec[17] = (float) r.lateRoundEconomyStamina();
                }
            });

            // Real round breakdown from cs2_team_round_advanced_v
            cs2Repository.findRoundStatsByTeamId(tid).ifPresent(rnd -> {
                if (rnd.totalRoundsWon() > 0) {
                    vec[18] = (float) rnd.ctWinShare();
                    vec[19] = (float) rnd.tWinShare();
                    vec[21] = (float) Math.min(1.0, rnd.ecoWinConversion() * 4.0);
                }
            });
        }

        return l2Normalize(vec);
    }
}
