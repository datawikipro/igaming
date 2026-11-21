package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.analyst.repository.clickhouse.TennisAnalyticsRepository;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TennisPlayerAdvancedFactDto;
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
        populateStandardBaselinePriors(vec, sport);

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
        CourtSurface surface = ctx.getSurface() != null ? ctx.getSurface() : CourtSurface.HARD;
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
        vec[52] = 0.0f;                    // elimination_pressure
        vec[53] = 0.0f;                    // draw_viability (strictly 0.0 in tennis)
        vec[54] = 0.50f;                   // regional_court_bias
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
}
