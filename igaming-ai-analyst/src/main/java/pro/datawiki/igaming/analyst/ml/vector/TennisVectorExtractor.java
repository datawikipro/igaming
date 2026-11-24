package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.analyst.repository.clickhouse.TennisAnalyticsRepository;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TennisPlayerAdvancedFactDto;
import pro.datawiki.igaming.dto.ml.tensor.PurePhysicsFeatureBlock;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

import java.util.Optional;

/**
 * Tactical vector extractor for Tennis.
 * Queries tennis_match_fact directly from ClickHouse DDS via declarative repository (Zero Owls, Zero Sines).
 * Enriched with Universal Dynamic MMR (Multi-Tier Glicko-2), Latent Class, Injury Layoff Dynamics,
 * and Serve Integrity Telemetry.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py and Constitution Rule 1.1/1.2.
 */
@Slf4j
@Component
public class TennisVectorExtractor extends AbstractSportVectorExtractor<TennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TournamentTierClassifier tierClassifier;
    private final TennisAnalyticsRepository tennisRepository;
    private final TennisSport sport = new TennisSport();

    @Autowired
    public TennisVectorExtractor(ClickHouseDdsClient clickHouse,
                                 TournamentTierClassifier tierClassifier,
                                 TennisAnalyticsRepository tennisRepository) {
        this.clickHouse = clickHouse;
        this.tierClassifier = tierClassifier;
        this.tennisRepository = tennisRepository;
    }

    public TennisVectorExtractor(ClickHouseDdsClient clickHouse, TournamentTierClassifier tierClassifier) {
        this(clickHouse, tierClassifier, null);
    }

    public TennisVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, new TournamentTierClassifier(), null);
    }

    @Override
    public TennisSport getSport() {
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

        // ── Detect Tournament Tier dynamically from Classifier (Constitution Rule 1.1) ────────
        TournamentTierClassifier.TierInfo tierInfo = tierClassifier != null
                ? tierClassifier.classify(ctx.getTournamentId(), ctx.getTournamentName(), "tennis")
                : TournamentTierClassifier.UNKNOWN;
        float slamTier = tierInfo.tierLevel() > 0 ? (8 - tierInfo.tierLevel()) / 7.0f : 0.50f;

        // Default priors if player is unranked or new
        double quarticStature = 0.05;
        double currentStature = 0.05;
        double deflationSpread = 0.0;
        float layoffNorm = 0.10f;
        float cadence = 0.85f;
        float rustPenalty = 0.00f;
        float serveIntegrity = 0.95f;
        float drNormalized = 0.50f;
        float holdPct = 0.65f;
        float breakPct = 0.20f;
        float winRate = 0.50f;

        // ── Real ClickHouse DDS Facts from tennis_player_advanced_v ───────────
        Long pid = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        Long oppPid = (ctx.getOpponentSofascoreId() != null && ctx.getOpponentSofascoreId() > 0) ? ctx.getOpponentSofascoreId() : ctx.getOpponentId();

        // ── Dynamic Universal Sports MMR (Multi-Tier Glicko-2) Resolution (Surface-Aware) ─────
        CourtSurface surface = (ctx.getSurface() != null && ctx.getSurface() != CourtSurface.UNKNOWN)
                ? ctx.getSurface()
                : CourtSurface.ALL;
        Optional<ClickHouseDdsClient.TennisMmr> playerMmrOpt = clickHouse.getTennisMmr(pid, surface.name());
        Optional<ClickHouseDdsClient.TennisMmr> oppMmrOpt = clickHouse.getTennisMmr(oppPid, surface.name());

        if (playerMmrOpt.isPresent()) {
            ClickHouseDdsClient.TennisMmr pMmr = playerMmrOpt.get();
            double mmr = pMmr.mmr();
            double peakMmr = pMmr.peakMmr();

            // Continuous Quartic Stature Mapping from Peak MMR (Vector-First Physics)
            // Sinner (peak 2606) -> ~0.95; Top 10 (peak 2300) -> ~0.76; Challenger (peak 1700) -> ~0.42; Amateur (peak 750) -> ~0.15
            double effectivePeakRank = Math.max(1.0, Math.exp((2600.0 - peakMmr) / 250.0));
            double effectiveCurRank = Math.max(1.0, Math.exp((2600.0 - mmr) / 250.0));
            quarticStature = Math.pow(effectivePeakRank, -0.25);
            currentStature = Math.pow(effectiveCurRank, -0.25);
            deflationSpread = Math.max(0.0, quarticStature - currentStature);

            if (oppMmrOpt.isPresent()) {
                ClickHouseDdsClient.TennisMmr oMmr = oppMmrOpt.get();
                double oppMmr = oMmr.mmr();
                double mmrDiff = mmr - oppMmr;
                double phiOpp = oMmr.rd() / 173.7178;
                double gPhiOpp = 1.0 / Math.sqrt(1.0 + (3.0 * phiOpp * phiOpp) / (Math.PI * Math.PI));
                double expectedWinProb = 1.0 / (1.0 + Math.exp(-gPhiOpp * (mmrDiff / 173.7178)));
                double pairConfidence = 1.0 / (1.0 + (pMmr.rd() + oMmr.rd()) / 300.0);

                vec[16] = (float) Math.tanh(mmrDiff / 400.0);
                vec[17] = (float) expectedWinProb;
                vec[18] = (float) pairConfidence;
                vec[19] = (float) ((mmr - 1500.0) / 500.0);
            }
        }

        if (pid != null && pid > 0 && tennisRepository != null) {
            Optional<TennisPlayerAdvancedFactDto> advOpt = tennisRepository.findPlayerAdvancedStats(pid);
            if (advOpt.isPresent()) {
                TennisPlayerAdvancedFactDto r = advOpt.get();
                if (r.totalMatches() > 0) {
                    double fsPct = r.fsPct();
                    double aces = r.acesPerMatch();
                    double dfs = r.dfsPerMatch();
                    double wr = r.winRate();
                    double bpSaved = r.bpSavedPct();
                    double bpConv = r.bpConvPct();
                    winRate = (float) wr;

                    vec[30] = (float) (fsPct / 100.0);
                    vec[33] = (float) (aces / 15.0);
                    vec[34] = (float) (dfs / 10.0);
                    vec[35] = (float) (bpSaved / 100.0);
                    vec[36] = (float) (bpConv / 100.0);
                    vec[39] = (float) r.hardWr();
                    vec[40] = (float) r.clayWr();

                    holdPct = (float) r.holdPctModeled();
                    breakPct = (float) r.breakPctModeled();
                    vec[37] = holdPct;
                    vec[38] = breakPct;

                    drNormalized = (float) r.dominanceRatioSigmoid();

                    // ── Post-Injury Recovery & Layoff Telemetry ───────────────────────
                    double daysSinceLast = Math.max(0.0, r.daysSinceLastMatch());
                    int comebackMatches = r.comebackMatches();

                    layoffNorm = (float) (daysSinceLast / (daysSinceLast + 60.0));
                    cadence = (float) (1.0 - Math.exp(-0.65 * comebackMatches));

                    float baseRust = (daysSinceLast >= 45.0) ? (float) (0.35 * (daysSinceLast / (daysSinceLast + 45.0))) : 0.0f;
                    rustPenalty = (float) (baseRust * Math.exp(-0.65 * Math.max(0, comebackMatches - 1)));

                    double comebackFs = fsPct;
                    serveIntegrity = (float) Math.min(1.0, Math.max(0.60, fsPct > 0 ? (comebackFs / fsPct) : 1.0));

                    // ── Quartic Power-Law Stature Scaling & Injury Deflation ───────────
                    int peakRank = r.peakRank();
                    int currentRank = r.currentRank();
                    double ddsQuartic = r.quarticStature();

                    if (peakRank > 0 && peakRank <= 2000) {
                        quarticStature = Math.max(ddsQuartic, Math.pow(peakRank, -0.25));
                    }

                    if (currentRank > 0 && currentRank <= 2000) {
                        currentStature = Math.pow(currentRank, -0.25);
                    } else {
                        currentStature = quarticStature;
                    }

                    deflationSpread = Math.max(0.0, quarticStature - currentStature);
                }
            }
        }

        // Dims 45..51: Quartic Stature, Injury Deflation and Physical Talent Dimensions
        vec[45] = (float) quarticStature;  // Peak Intrinsic Class / Talent (Quartic Power-Law 1 / peak_rank^0.25)
        vec[46] = (float) currentStature;  // Current Stature (1 / current_rank^0.25)
        vec[47] = (float) deflationSpread; // Rank Deflation Spread (drop from injury/layoff)
        vec[48] = layoffNorm;              // Layoff duration factor
        vec[49] = cadence;                 // Comeback matches adaptation cadence
        vec[50] = drNormalized;            // Dominance ratio sigmoid
        vec[51] = holdPct;                 // Modeled service hold %

        // Dims 52..58: Point-in-Time Dynamics
        vec[52] = ctx.getEliminationPressureIndex();       // elimination_pressure
        vec[53] = 0.0f;                                    // draw_viability (strictly 0.0 in tennis - no draws)
        vec[54] = ctx.getRegionalCardBias();               // regional_court_bias
        vec[55] = layoffNorm;
        vec[56] = cadence;
        vec[57] = rustPenalty;
        vec[58] = serveIntegrity;

        // Dims 59..63: Solidity & Clutch Ratings
        vec[59] = Math.min(1.0f, Math.max(0.0f, rustPenalty));
        vec[60] = Math.min(0.98f, Math.max(0.40f, holdPct));
        vec[61] = Math.min(0.98f, Math.max(0.10f, breakPct));
        vec[62] = drNormalized;
        vec[63] = winRate;

        // Head-to-Head Lifetime against opponent
        Long oppId = ctx.getOpponentId();
        if (oppId != null && oppId > 0 && pid != null && pid > 0 && tennisRepository != null) {
            tennisRepository.findH2hStats(pid, oppId).ifPresent(h2h -> {
                if (h2h.h2hM() > 0) {
                    vec[44] = (float) h2h.h2hWr();
                }
            });
        }

        return l2Normalize(vec);
    }

    @Override
    public float[] extractMacroContext(float[] v1, float[] v2) {
        // Neutral ATP/ITF tour: Home advantage does NOT exist.
        // Dimension is completely removed (Clean-Cut Policy / Rule 1.4).
        return new float[]{
                v1.length > 0 && v2.length > 0 ? (v1[0] - v2[0]) : 0.0f,
                v1.length > 5 && v2.length > 5 ? (v1[5] - v2[5]) : 0.0f
        };
    }

    @Override
    public float[] extractMicroFeatures(TacticalMatchContext matchCtx) {
        return extractTennisPhysicalAndRecoveryFeatures(
                matchCtx.ctx1(), matchCtx.ctx2(), matchCtx.v1(), matchCtx.v2()
        );
    }

    @Override
    public float[] extractPurePhysicsVector(TacticalMatchContext matchCtx, float[] lineup) {
        return buildTennisDuelPhysicsVector(matchCtx);
    }

    protected float[] extractTennisPhysicalAndRecoveryFeatures(TacticalVectorContext ctx1,
                                                               TacticalVectorContext ctx2,
                                                               float[] v1,
                                                               float[] v2) {
        float[] lf = new float[64];

        // ── Player 1 Micro Physical & Recovery Profile (Dims 0..11) ─────────────
        lf[0] = 1.0f;                                              // is_confirmed_entry
        lf[1] = v1.length > 45 ? v1[45] : 0.05f;                   // pure peak latent class stature
        lf[2] = v1.length > 17 ? v1[17] : 0.50f;                   // baseline stamina rating
        lf[3] = v1.length > 45 ? v1[45] : 0.05f;                   // short set closing power (latent class)
        lf[4] = v1.length > 57 ? v1[57] : 0.0f;                    // total injury rust
        lf[5] = v1.length > 56 ? v1[56] : 0.85f;                   // comeback match cadence
        lf[6] = v1.length > 58 ? v1[58] : 0.95f;                   // serve health ratio
        lf[7] = v1.length > 34 ? v1[34] : 0.20f;                   // double fault hazard
        lf[8] = v1.length > 35 ? v1[35] : 0.60f;                   // break point clutch
        lf[9] = v1.length > 57 ? v1[57] : 0.0f;                    // unforced error / rust indicator
        lf[10] = v1.length > 55 ? v1[55] : 0.10f;                  // layoff days
        lf[11] = (v1.length > 46 && v1[46] > 0.25f) ? 1.0f : 0.0f; // protected ranking deflated flag

        // ── Player 2 Micro Physical & Recovery Profile (Dims 12..23) ────────────
        lf[12] = 1.0f;                                             // is_confirmed_entry
        lf[13] = v2.length > 45 ? v2[45] : 0.05f;                  // pure peak latent class stature
        lf[14] = v2.length > 17 ? v2[17] : 0.50f;                  // baseline stamina rating
        lf[15] = v2.length > 45 ? v2[45] : 0.05f;                  // short set closing power (latent class)
        lf[16] = v2.length > 57 ? v2[57] : 0.0f;                   // total injury rust
        lf[17] = v2.length > 56 ? v2[56] : 0.85f;                  // comeback match cadence
        lf[18] = v2.length > 58 ? v2[58] : 0.95f;                  // serve health ratio
        lf[19] = v2.length > 34 ? v2[34] : 0.20f;                  // double fault hazard
        lf[20] = v2.length > 35 ? v2[35] : 0.60f;                  // break point clutch
        lf[21] = v2.length > 57 ? v2[57] : 0.0f;                   // unforced error / rust indicator
        lf[22] = v2.length > 55 ? v2[55] : 0.10f;                  // layoff days
        lf[23] = (v2.length > 46 && v2[46] > 0.25f) ? 1.0f : 0.0f; // protected ranking deflated flag

        // ── Head-to-Head & Asymmetry Dynamics (Dims 24..31) ──────────────────────
        lf[24] = lf[1] - lf[13];                                   // diff_effective_rating (P1_eff - P2_eff)
        lf[25] = lf[3] - lf[15];                                   // diff_short_set_power
        lf[26] = lf[4] - lf[16];                                   // diff_injury_rust
        lf[27] = lf[6] - lf[18];                                   // diff_serve_integrity
        lf[28] = lf[2] - lf[14];                                   // diff_deciding_set_stamina

        float p1Stature = v1.length > 45 ? v1[45] : 0.05f;
        float p2Stature = v2.length > 45 ? v2[45] : 0.05f;
        float p1Wr = v1.length > 0 ? v1[0] : 0.50f;
        float p2Wr = v2.length > 0 ? v2[0] : 0.50f;
        boolean classDisparity = (Math.abs(p1Stature - p2Stature) >= 0.05f && Math.abs(p1Wr - p2Wr) <= 0.15f);
        lf[29] = classDisparity ? 1.0f : 0.0f;                     // class disparity mask
        lf[30] = v1.length > 38 ? v1[38] : 0.65f;                  // player 1 hold rate
        lf[31] = v2.length > 38 ? v2[38] : 0.65f;                  // player 2 hold rate

        // ── Advanced Tennis Micro Dynamics (Dims 32..63) ────────────────────────
        float sHold1 = v1.length > 30 ? v1[30] : 0.62f;            // 1st serve win rate
        float sHold2 = v2.length > 30 ? v2[30] : 0.62f;
        lf[32] = p1Wr;                                             // career win rate on surface proxy
        lf[33] = p2Wr;
        lf[34] = p1Wr - p2Wr;
        lf[35] = sHold1;
        lf[36] = sHold2;
        lf[37] = sHold1 - sHold2;

        float ret1 = v1.length > 31 ? v1[31] : 0.35f;
        float ret2 = v2.length > 31 ? v2[31] : 0.35f;
        lf[38] = sHold1;
        lf[39] = sHold2;
        lf[40] = ret1;
        lf[41] = ret2;
        lf[42] = sHold1 - ret2;
        lf[43] = sHold2 - ret1;

        lf[44] = v1.length > 34 ? v1[34] : 0.20f;
        lf[45] = v2.length > 34 ? v2[34] : 0.20f;

        lf[46] = v1.length > 56 ? v1[56] : 0.85f;
        lf[47] = v2.length > 56 ? v2[56] : 0.85f;
        lf[48] = lf[46] - lf[47];
        lf[49] = v1.length > 57 ? v1[57] : 0.0f;
        lf[50] = v2.length > 57 ? v2[57] : 0.0f;

        lf[51] = (v1.length > 48 && v1[48] > 0.5f) ? 1.0f : 0.0f;
        lf[52] = (v2.length > 48 && v2[48] > 0.5f) ? 1.0f : 0.0f;
        lf[53] = Math.abs(lf[51] - lf[52]);

        lf[54] = v1.length > 35 ? v1[35] : 0.60f;
        lf[55] = v2.length > 35 ? v2[35] : 0.60f;
        lf[56] = lf[54] - lf[55];
        lf[57] = (lf[54] + lf[55]) * 0.5f;

        lf[58] = v1.length > 17 ? v1[17] : 0.50f;
        lf[59] = v2.length > 17 ? v2[17] : 0.50f;
        lf[60] = lf[58] - lf[59];
        lf[61] = lf[3];
        lf[62] = lf[15];
        lf[63] = lf[3] - lf[15];

        return lf;
    }

    protected float[] buildTennisDuelPhysicsVector(TacticalMatchContext matchCtx) {
        float[] p64 = new float[PurePhysicsFeatureBlock.EXPECTED_DIMENSION];
        float[] v1 = matchCtx.v1();
        float[] v2 = matchCtx.v2();
        TacticalVectorContext ctx1 = matchCtx.ctx1();

        // ── Block A (0..23): Player 1 Physical & Skill Dynamics ──
        p64[0]  = v1.length > 19 ? v1[19] : 0.0f;
        p64[1]  = v1.length > 18 ? v1[18] : 0.50f;
        p64[2]  = v1.length > 45 ? v1[45] : 0.05f;
        p64[3]  = v1.length > 46 ? v1[46] : 0.05f;
        p64[4]  = v1.length > 47 ? v1[47] : 0.0f;
        p64[5]  = v1.length > 63 ? v1[63] : 0.50f;
        p64[6]  = v1.length > 37 ? v1[37] : 0.65f;
        p64[7]  = v1.length > 38 ? v1[38] : 0.20f;
        p64[8]  = v1.length > 50 ? v1[50] : 0.50f;
        p64[9]  = v1.length > 30 ? v1[30] : 0.62f;
        p64[10] = v1.length > 33 ? v1[33] : 0.25f;
        p64[11] = v1.length > 34 ? v1[34] : 0.20f;
        p64[12] = v1.length > 35 ? v1[35] : 0.60f;
        p64[13] = v1.length > 36 ? v1[36] : 0.40f;
        p64[14] = v1.length > 39 ? v1[39] : 0.50f;
        p64[15] = v1.length > 40 ? v1[40] : 0.50f;
        p64[16] = v1.length > 48 ? v1[48] : 0.10f;
        p64[17] = v1.length > 49 ? v1[49] : 0.85f;
        p64[18] = v1.length > 57 ? v1[57] : 0.0f;
        p64[19] = v1.length > 58 ? v1[58] : 0.95f;
        p64[20] = v1.length > 59 ? v1[59] : 0.50f;
        p64[21] = v1.length > 0 ? v1[0] : 0.50f;
        p64[22] = v1.length > 1 ? v1[1] : 0.0f;
        p64[23] = v1.length > 2 ? v1[2] : 0.50f;

        // ── Block B (24..47): Player 2 Physical & Skill Dynamics (Strict Mirror) ──
        p64[24] = v2.length > 19 ? v2[19] : 0.0f;
        p64[25] = v2.length > 18 ? v2[18] : 0.50f;
        p64[26] = v2.length > 45 ? v2[45] : 0.05f;
        p64[27] = v2.length > 46 ? v2[46] : 0.05f;
        p64[28] = v2.length > 47 ? v2[47] : 0.0f;
        p64[29] = v2.length > 63 ? v2[63] : 0.50f;
        p64[30] = v2.length > 37 ? v2[37] : 0.65f;
        p64[31] = v2.length > 38 ? v2[38] : 0.20f;
        p64[32] = v2.length > 50 ? v2[50] : 0.50f;
        p64[33] = v2.length > 30 ? v2[30] : 0.62f;
        p64[34] = v2.length > 33 ? v2[33] : 0.25f;
        p64[35] = v2.length > 34 ? v2[34] : 0.20f;
        p64[36] = v2.length > 35 ? v2[35] : 0.60f;
        p64[37] = v2.length > 36 ? v2[36] : 0.40f;
        p64[38] = v2.length > 39 ? v2[39] : 0.50f;
        p64[39] = v2.length > 40 ? v2[40] : 0.50f;
        p64[40] = v2.length > 48 ? v2[48] : 0.10f;
        p64[41] = v2.length > 49 ? v2[49] : 0.85f;
        p64[42] = v2.length > 57 ? v2[57] : 0.0f;
        p64[43] = v2.length > 58 ? v2[58] : 0.95f;
        p64[44] = v2.length > 59 ? v2[59] : 0.50f;
        p64[45] = v2.length > 0 ? v2[0] : 0.50f;
        p64[46] = v2.length > 1 ? v2[1] : 0.0f;
        p64[47] = v2.length > 2 ? v2[2] : 0.50f;

        // ── Block C (48..63): Pairwise Duel Physics & Symmetric Differentials ──
        p64[48] = p64[0] - p64[24];
        p64[49] = 1.0f / (1.0f + (float) Math.exp(-4.0 * (p64[0] - p64[24])));
        p64[50] = (p64[1] + p64[25]) * 0.5f;
        p64[51] = p64[2] - p64[26];
        p64[52] = p64[6] - p64[30];
        p64[53] = p64[7] - p64[31];
        p64[54] = p64[8] - p64[32];
        p64[55] = p64[10] - p64[34];
        p64[56] = p64[11] - p64[35];
        p64[57] = p64[5] - p64[29];
        p64[58] = (p64[17] - p64[18]) - (p64[41] - p64[42]);
        p64[59] = v1.length > 44 ? v1[44] : 0.50f; // Real H2H Win Rate

        float tierNorm = 0.50f;
        if (matchCtx.tierClassifier() != null) {
            TournamentTierClassifier.TierInfo ti = matchCtx.tierClassifier().classify(ctx1.getTournamentId(), ctx1.getTournamentName(), matchCtx.sport().getCode());
            tierNorm = ti.tierLevel() > 0 ? (8 - ti.tierLevel()) / 7.0f : 0.50f;
        }
        p64[60] = tierNorm;

        float surfCode = 0.0f;
        if (ctx1.getSurface() != null) {
            switch (ctx1.getSurface()) {
                case CLAY -> surfCode = 0.33f;
                case GRASS -> surfCode = 0.66f;
                case INDOORS -> surfCode = 1.0f;
                default -> surfCode = 0.0f;
            }
        }
        p64[61] = surfCode;
        p64[62] = (ctx1.getPaceMultiplier() != null) ? ctx1.getPaceMultiplier().floatValue() : 1.0f;
        p64[63] = (float) (matchCtx.t1Leverage() - matchCtx.t2Leverage());

        return p64;
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        // Game State & Resilience Priors
        vec[8] = 0.50f; vec[9] = 0.35f; vec[10] = 0.20f; vec[11] = 0.20f;
        vec[12] = 0.20f; vec[13] = 0.20f; vec[14] = 0.35f; vec[15] = 0.35f;
        vec[16] = 0.0f; vec[17] = 0.50f;
        float[] bins = {0.33f, 0.33f, 0.34f, 0.0f, 0.0f, 0.0f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        // Baseline Serving & Racket Priors (Zero Football Goalkeeper/Defender Artifacts)
        vec[30] = 0.62f; vec[31] = 0.50f; vec[32] = 0.50f; vec[33] = 0.25f;
        vec[34] = 0.20f; vec[35] = 0.60f; vec[36] = 0.40f; vec[37] = 0.65f;
        vec[38] = 0.20f; vec[39] = 0.50f; vec[40] = 0.50f; vec[41] = 0.50f;
        vec[42] = 0.50f; vec[43] = 0.50f; vec[44] = 0.50f; vec[45] = 0.05f;
        vec[46] = 0.05f; vec[47] = 0.00f;
        vec[48] = 0.10f; vec[49] = 0.85f; vec[50] = 0.50f; vec[51] = 0.65f;
        vec[52] = 0.00f; vec[53] = 0.00f; vec[54] = 0.50f; vec[55] = 0.10f;
        vec[56] = 0.85f; vec[57] = 0.00f; vec[58] = 0.95f; vec[59] = 0.00f;
        vec[60] = 0.65f; vec[61] = 0.20f; vec[62] = 0.50f; vec[63] = 0.50f;
    }
}
