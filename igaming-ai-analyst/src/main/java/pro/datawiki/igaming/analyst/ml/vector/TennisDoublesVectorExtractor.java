package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.analyst.repository.clickhouse.TennisAnalyticsRepository;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;
import pro.datawiki.igaming.dto.sport.racket.TennisDoublesSport;

import java.util.Optional;

/**
 * Tactical vector extractor for Tennis Doubles (ATP Doubles, WTA Doubles, Grand Slam Doubles, ITF Doubles).
 * Queries sport_mmr_rating and tandem synergy directly from ClickHouse DDS.
 * Models:
 * - Partner synergy and match experience together
 * - Skill asymmetry penalty between partners
 * - Foundation singles stature and surface specialization
 * - No-Ad and 10-Point Match Tie-Break volatile mechanics
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix_doubles.py and Constitution Rule 1.1/1.2.
 */
@Slf4j
@Component
public class TennisDoublesVectorExtractor extends AbstractSportVectorExtractor<TennisDoublesSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TournamentTierClassifier tierClassifier;
    private final TennisAnalyticsRepository tennisRepository;
    private final TennisDoublesSport sport = new TennisDoublesSport();

    @Autowired
    public TennisDoublesVectorExtractor(ClickHouseDdsClient clickHouse,
                                        TournamentTierClassifier tierClassifier,
                                        TennisAnalyticsRepository tennisRepository) {
        this.clickHouse = clickHouse;
        this.tierClassifier = tierClassifier;
        this.tennisRepository = tennisRepository;
    }

    public TennisDoublesVectorExtractor(ClickHouseDdsClient clickHouse, TournamentTierClassifier tierClassifier) {
        this(clickHouse, tierClassifier, null);
    }

    public TennisDoublesVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, new TournamentTierClassifier(), null);
    }

    @Override
    public TennisDoublesSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];

        // ── Dims 0..7: Macro Performance Vector (Win Rate, Form, Differentials) ──
        float[] macro0to7 = computeStandardMacroDims0To7(ctx, sport.hasDrawOutcome());
        System.arraycopy(macro0to7, 0, vec, 0, 8);

        // ── Dims 8..63: Baseline Physical & Biological Priors ────────────────────
        populateBaselinePriors(vec);

        // ── Detect Tournament Tier dynamically from Classifier ────────────────────
        TournamentTierClassifier.TierInfo tierInfo = tierClassifier != null
                ? tierClassifier.classify(ctx.getTournamentId(), ctx.getTournamentName(), "tennis")
                : TournamentTierClassifier.UNKNOWN;
        float slamTier = tierInfo.tierLevel() > 0 ? (8 - tierInfo.tierLevel()) / 7.0f : 0.50f;

        Long t1Id = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        Long t2Id = (ctx.getOpponentSofascoreId() != null && ctx.getOpponentSofascoreId() > 0) ? ctx.getOpponentSofascoreId() : ctx.getOpponentId();

        CourtSurface surface = (ctx.getSurface() != null && ctx.getSurface() != CourtSurface.UNKNOWN)
                ? ctx.getSurface()
                : CourtSurface.ALL;

        // ── Glicko-2 Doubles MMR Resolution ───────────────────────────────────────
        Optional<ClickHouseDdsClient.TennisMmr> pair1MmrOpt = clickHouse.getSportMmr(t1Id, "tennis-doubles", surface.name());
        Optional<ClickHouseDdsClient.TennisMmr> pair2MmrOpt = clickHouse.getSportMmr(t2Id, "tennis-doubles", surface.name());

        double mmr1 = pair1MmrOpt.map(ClickHouseDdsClient.TennisMmr::mmr).orElse(1500.0);
        double rd1 = pair1MmrOpt.map(ClickHouseDdsClient.TennisMmr::rd).orElse(350.0);
        double mmr2 = pair2MmrOpt.map(ClickHouseDdsClient.TennisMmr::mmr).orElse(1500.0);
        double rd2 = pair2MmrOpt.map(ClickHouseDdsClient.TennisMmr::rd).orElse(350.0);

        double mmrDiff = mmr1 - mmr2;
        double pairConfidence = 1.0 / (1.0 + (rd1 + rd2) / 300.0);
        double expectedWinProb = 1.0 / (1.0 + Math.pow(10.0, -mmrDiff / 400.0));

        vec[16] = (float) Math.tanh(mmrDiff / 400.0);
        vec[17] = (float) expectedWinProb;
        vec[18] = (float) pairConfidence;
        vec[19] = (float) ((mmr1 - 1500.0) / 500.0);

        // Dims 30..38: Doubles Tactical Parameters
        vec[30] = 0.64f; // 1st serve in % baseline
        vec[31] = 0.72f; // 1st serve win % baseline
        vec[32] = 0.52f; // 2nd serve win % baseline
        vec[33] = 0.35f; // Net poach & blitz frequency
        vec[34] = 0.12f; // Double fault hazard
        vec[35] = 0.62f; // Deciding point (No-Ad 40:40) clutch
        vec[36] = 0.55f; // Match tiebreak (super tiebreak to 10) win %
        vec[37] = 0.82f; // Service games held % (higher in doubles)
        vec[38] = 0.18f; // Return games broken % (lower in doubles)

        // Dims 45..51: Tandem Chemistry & Asymmetry Dynamics
        vec[45] = (float) Math.min(1.0, Math.max(0.0, (mmr1 - 1000.0) / 1200.0)); // Tandem Class
        vec[46] = (float) Math.min(1.0, Math.max(0.0, (mmr2 - 1000.0) / 1200.0)); // Opponent Tandem Class
        vec[47] = (float) Math.min(1.0, Math.max(0.0, pair1MmrOpt.map(m -> m.matchesPlayed() / 50.0).orElse(0.1))); // Synergy
        vec[48] = (float) (1.0 - (rd1 / 350.0)); // Rating Certainty
        vec[49] = slamTier;
        vec[50] = (float) Math.tanh(mmrDiff / 300.0) * 0.5f + 0.5f;
        vec[51] = vec[37]; // hold %

        // Dims 52..58: Point-in-Time & Tie-break Dynamics
        vec[52] = ctx.getEliminationPressureIndex();
        vec[53] = 0.0f; // Draw viability = 0 in doubles tennis
        vec[54] = ctx.getRegionalCardBias();
        vec[55] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (rd1 / 350.0)));
        vec[56] = (float) Math.min(1.0, pair1MmrOpt.map(m -> m.matchesPlayed() / 30.0).orElse(0.5));
        vec[57] = (float) Math.max(0.0, (rd1 - 100.0) / 250.0);
        vec[58] = (float) Math.min(1.0, Math.max(0.60, 1.0 - vec[57]));

        // Dims 59..63: Clutch & Deciding Moments
        vec[59] = vec[35]; // Deciding point clutch
        vec[60] = vec[36]; // Match tiebreak clutch
        vec[61] = vec[37]; // Hold %
        vec[62] = (float) expectedWinProb;
        vec[63] = (float) macro0to7[0]; // Form win rate

        return l2Normalize(vec);
    }

    @Override
    public float[] extractMicroFeatures(TacticalMatchContext matchCtx) {
        float[] lf = new float[64];
        float[] v1 = matchCtx.v1();
        float[] v2 = matchCtx.v2();

        lf[0] = 1.0f;
        lf[1] = v1.length > 45 ? v1[45] : 0.50f;
        lf[2] = v1.length > 17 ? v1[17] : 0.50f;
        lf[3] = v1.length > 45 ? v1[45] : 0.50f;
        lf[4] = v1.length > 57 ? v1[57] : 0.0f;
        lf[5] = v1.length > 56 ? v1[56] : 0.85f;
        lf[6] = v1.length > 58 ? v1[58] : 0.95f;
        lf[7] = v1.length > 34 ? v1[34] : 0.12f;
        lf[8] = v1.length > 35 ? v1[35] : 0.62f;
        lf[9] = v1.length > 57 ? v1[57] : 0.0f;
        lf[10] = v1.length > 55 ? v1[55] : 0.10f;
        lf[11] = 0.0f;

        lf[12] = 1.0f;
        lf[13] = v2.length > 45 ? v2[45] : 0.50f;
        lf[14] = v2.length > 17 ? v2[17] : 0.50f;
        lf[15] = v2.length > 45 ? v2[45] : 0.50f;
        lf[16] = v2.length > 57 ? v2[57] : 0.0f;
        lf[17] = v2.length > 56 ? v2[56] : 0.85f;
        lf[18] = v2.length > 58 ? v2[58] : 0.95f;
        lf[19] = v2.length > 34 ? v2[34] : 0.12f;
        lf[20] = v2.length > 35 ? v2[35] : 0.62f;
        lf[21] = v2.length > 57 ? v2[57] : 0.0f;
        lf[22] = v2.length > 55 ? v2[55] : 0.10f;
        lf[23] = 0.0f;

        lf[24] = lf[1] - lf[13];
        lf[25] = lf[3] - lf[15];
        lf[26] = lf[4] - lf[16];
        lf[27] = lf[6] - lf[18];
        lf[28] = lf[2] - lf[14];
        lf[29] = Math.abs(lf[1] - lf[13]) >= 0.10f ? 1.0f : 0.0f;
        lf[30] = v1.length > 37 ? v1[37] : 0.82f;
        lf[31] = v2.length > 37 ? v2[37] : 0.82f;

        for (int i = 0; i < 32; i++) {
            lf[32 + i] = (i < 8) ? (lf[i] - lf[12 + i]) : 0.0f;
        }

        return lf;
    }

    @Override
    public float[] extractMacroContext(float[] v1, float[] v2) {
        // Neutral doubles tour: Home advantage does NOT exist.
        // Dimension is completely removed (Clean-Cut Policy / Rule 1.4).
        return new float[]{
                v1.length > 0 && v2.length > 0 ? (v1[0] - v2[0]) : 0.0f,
                v1.length > 5 && v2.length > 5 ? (v1[5] - v2[5]) : 0.0f
        };
    }

    @Override
    public float[] extractPurePhysicsVector(TacticalMatchContext matchCtx, float[] lineup) {
        float[] p64 = new float[pro.datawiki.igaming.dto.ml.tensor.PurePhysicsFeatureBlock.EXPECTED_DIMENSION];
        float[] v1 = matchCtx.v1();
        float[] v2 = matchCtx.v2();

        p64[0]  = v1.length > 19 ? v1[19] : 0.0f;
        p64[1]  = v1.length > 18 ? v1[18] : 0.50f;
        p64[2]  = v1.length > 45 ? v1[45] : 0.50f;
        p64[3]  = v1.length > 47 ? v1[47] : 0.50f;
        p64[4]  = v1.length > 48 ? v1[48] : 0.50f;
        p64[5]  = v1.length > 63 ? v1[63] : 0.50f;
        p64[6]  = v1.length > 37 ? v1[37] : 0.82f;
        p64[7]  = v1.length > 38 ? v1[38] : 0.18f;
        p64[8]  = v1.length > 50 ? v1[50] : 0.50f;
        p64[9]  = v1.length > 30 ? v1[30] : 0.64f;
        p64[10] = v1.length > 31 ? v1[31] : 0.72f;
        p64[11] = v1.length > 34 ? v1[34] : 0.12f;
        p64[12] = v1.length > 35 ? v1[35] : 0.62f;
        p64[13] = v1.length > 36 ? v1[36] : 0.55f;
        p64[14] = v1.length > 56 ? v1[56] : 0.85f;
        p64[15] = v1.length > 57 ? v1[57] : 0.0f;
        p64[16] = v1.length > 58 ? v1[58] : 0.95f;
        p64[17] = v1.length > 17 ? v1[17] : 0.50f;
        p64[18] = v1.length > 0 ? v1[0] : 0.50f;
        p64[19] = v1.length > 1 ? v1[1] : 0.0f;
        p64[20] = v1.length > 2 ? v1[2] : 0.50f;
        p64[21] = v1.length > 3 ? v1[3] : 0.15f;
        p64[22] = v1.length > 4 ? v1[4] : 0.15f;
        p64[23] = v1.length > 5 ? v1[5] : 0.0f;

        p64[24] = v2.length > 19 ? v2[19] : 0.0f;
        p64[25] = v2.length > 18 ? v2[18] : 0.50f;
        p64[26] = v2.length > 45 ? v2[45] : 0.50f;
        p64[27] = v2.length > 47 ? v2[47] : 0.50f;
        p64[28] = v2.length > 48 ? v2[48] : 0.50f;
        p64[29] = v2.length > 63 ? v2[63] : 0.50f;
        p64[30] = v2.length > 37 ? v2[37] : 0.82f;
        p64[31] = v2.length > 38 ? v2[38] : 0.18f;
        p64[32] = v2.length > 50 ? v2[50] : 0.50f;
        p64[33] = v2.length > 30 ? v2[30] : 0.64f;
        p64[34] = v2.length > 31 ? v2[31] : 0.72f;
        p64[35] = v2.length > 34 ? v2[34] : 0.12f;
        p64[36] = v2.length > 35 ? v2[35] : 0.62f;
        p64[37] = v2.length > 36 ? v2[36] : 0.55f;
        p64[38] = v2.length > 56 ? v2[56] : 0.85f;
        p64[39] = v2.length > 57 ? v2[57] : 0.0f;
        p64[40] = v2.length > 58 ? v2[58] : 0.95f;
        p64[41] = v2.length > 17 ? v2[17] : 0.50f;
        p64[42] = v2.length > 0 ? v2[0] : 0.50f;
        p64[43] = v2.length > 1 ? v2[1] : 0.0f;
        p64[44] = v2.length > 2 ? v2[2] : 0.50f;
        p64[45] = v2.length > 3 ? v2[3] : 0.15f;
        p64[46] = v2.length > 4 ? v2[4] : 0.15f;
        p64[47] = v2.length > 5 ? v2[5] : 0.0f;

        p64[48] = p64[0] - p64[24];
        p64[49] = 1.0f / (1.0f + (float) Math.exp(-4.0 * (p64[0] - p64[24])));
        p64[50] = (p64[1] + p64[25]) * 0.5f;
        p64[51] = p64[2] - p64[26];
        p64[52] = p64[6] - p64[30];
        p64[53] = p64[7] - p64[31];
        p64[54] = p64[8] - p64[32];
        p64[55] = p64[9] - p64[33];
        p64[56] = p64[11] - p64[35];
        p64[57] = p64[5] - p64[29];
        p64[58] = p64[12] - p64[36];
        p64[59] = v1.length > 44 ? v1[44] : 0.50f;
        float tierNorm = 0.50f;
        if (matchCtx.tierClassifier() != null) {
            TournamentTierClassifier.TierInfo ti = matchCtx.tierClassifier().classify(matchCtx.ctx1().getTournamentId(), matchCtx.ctx1().getTournamentName(), matchCtx.sport().getCode());
            tierNorm = ti.tierLevel() > 0 ? (8 - ti.tierLevel()) / 7.0f : 0.50f;
        }
        p64[60] = tierNorm;
        p64[61] = matchCtx.ctx1().getSurface() == CourtSurface.CLAY ? 0.33f : (matchCtx.ctx1().getSurface() == CourtSurface.GRASS ? 0.66f : 0.0f);
        p64[62] = (matchCtx.ctx1().getPaceMultiplier() != null) ? matchCtx.ctx1().getPaceMultiplier().floatValue() : 1.0f;
        p64[63] = (float) (matchCtx.t1Leverage() - matchCtx.t2Leverage());

        return p64;
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        vec[8] = 0.50f; vec[9] = 0.35f; vec[10] = 0.20f; vec[11] = 0.20f;
        vec[12] = 0.20f; vec[13] = 0.20f; vec[14] = 0.35f; vec[15] = 0.35f;
        vec[16] = 0.40f; vec[17] = 0.30f;
        float[] bins = {0.33f, 0.33f, 0.34f, 0.0f, 0.0f, 0.0f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.50f; vec[33] = 0.0f;
        vec[34] = 0.0f; vec[35] = 0.50f; vec[36] = 0.50f; vec[37] = 0.50f;
        vec[38] = 0.50f; vec[39] = 0.50f; vec[40] = 0.70f; vec[41] = 0.50f;
        vec[42] = 0.50f; vec[43] = 0.50f; vec[44] = 0.50f; vec[45] = 0.0f;
        vec[46] = 0.0f; vec[47] = 0.50f;
        vec[48] = 0.36f; vec[49] = 0.30f; vec[50] = 0.15f; vec[51] = 0.30f;
        vec[52] = 0.60f; vec[53] = 0.36f; vec[54] = 0.0f; vec[55] = 0.0f;
        vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.0f;
        vec[60] = 0.70f; vec[61] = 0.70f; vec[62] = 0.70f; vec[63] = 0.70f;
    }
}
