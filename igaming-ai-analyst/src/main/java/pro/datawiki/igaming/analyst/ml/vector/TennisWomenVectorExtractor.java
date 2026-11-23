package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.analyst.repository.clickhouse.TennisAnalyticsRepository;
import pro.datawiki.igaming.analyst.repository.clickhouse.dto.TennisPlayerAdvancedFactDto;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;
import pro.datawiki.igaming.dto.sport.racket.TennisWomenSport;

import java.util.Optional;

/**
 * Tactical vector extractor for Women's Tennis (WTA Tour, ITF Women, Grand Slams Women).
 * Calibrated specifically for WTA Tour dynamics:
 * - Best of 3 sets maximum format across all tournaments (including Grand Slams)
 * - Calibrated service hold prior (66.5% vs ATP 81.5%)
 * - Higher break-point return rate and break-back volatility (32.0% vs ATP 18.0%)
 * - WTA peak MMR benchmark centered at 2400 (Swiatek/Sabalenka) vs ATP 2600
 */
@Slf4j
@Component
public class TennisWomenVectorExtractor extends AbstractSportVectorExtractor<TennisWomenSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TournamentTierClassifier tierClassifier;
    private final TennisAnalyticsRepository tennisRepository;
    private final TennisWomenSport sport = new TennisWomenSport();

    @Autowired
    public TennisWomenVectorExtractor(ClickHouseDdsClient clickHouse,
                                      TournamentTierClassifier tierClassifier,
                                      TennisAnalyticsRepository tennisRepository) {
        this.clickHouse = clickHouse;
        this.tierClassifier = tierClassifier;
        this.tennisRepository = tennisRepository;
    }

    public TennisWomenVectorExtractor(ClickHouseDdsClient clickHouse, TournamentTierClassifier tierClassifier) {
        this(clickHouse, tierClassifier, null);
    }

    public TennisWomenVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, new TournamentTierClassifier(), null);
    }

    @Override
    public TennisWomenSport getSport() {
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

        TournamentTierClassifier.TierInfo tierInfo = tierClassifier != null
                ? tierClassifier.classify(ctx.getTournamentId(), ctx.getTournamentName(), "tennis")
                : TournamentTierClassifier.UNKNOWN;
        float slamTier = tierInfo.tierLevel() > 0 ? (8 - tierInfo.tierLevel()) / 7.0f : 0.50f;

        // WTA-specific default priors (Hold ~66.5%, Return ~33.5%)
        double quarticStature = 0.05;
        double currentStature = 0.05;
        double deflationSpread = 0.0;
        float holdPct = 0.665f; // WTA average hold rate
        float breakPct = 0.335f; // WTA average break rate
        float winRate = 0.50f;

        Long pid = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        Long oppPid = (ctx.getOpponentSofascoreId() != null && ctx.getOpponentSofascoreId() > 0) ? ctx.getOpponentSofascoreId() : ctx.getOpponentId();

        CourtSurface surface = (ctx.getSurface() != null && ctx.getSurface() != CourtSurface.UNKNOWN)
                ? ctx.getSurface()
                : CourtSurface.ALL;
        Optional<ClickHouseDdsClient.TennisMmr> playerMmrOpt = clickHouse.getTennisMmr(pid, surface.name());
        Optional<ClickHouseDdsClient.TennisMmr> oppMmrOpt = clickHouse.getTennisMmr(oppPid, surface.name());

        if (playerMmrOpt.isPresent()) {
            ClickHouseDdsClient.TennisMmr pMmr = playerMmrOpt.get();
            double mmr = pMmr.mmr();
            double peakMmr = pMmr.peakMmr();

            // Calibrated for WTA Peak (2400 vs ATP 2600)
            double effectivePeakRank = Math.max(1.0, Math.exp((2400.0 - peakMmr) / 250.0));
            double effectiveCurRank = Math.max(1.0, Math.exp((2400.0 - mmr) / 250.0));
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

                vec[16] = (float) Math.tanh(mmrDiff / 350.0);
                vec[17] = (float) expectedWinProb;
                vec[18] = (float) pairConfidence;
                vec[19] = (float) ((mmr - 1400.0) / 450.0);
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
                    vec[33] = (float) (aces / 8.0); // Calibrated for WTA ace rates (~2.8 vs ATP 8.5)
                    vec[34] = (float) (dfs / 8.0);
                    vec[35] = (float) (bpSaved / 100.0);
                    vec[36] = (float) (bpConv / 100.0);
                    vec[39] = (float) r.hardWr();
                }
            }
        }

        // Dims 20..25: WTA Dynamic priors
        vec[20] = (float) quarticStature;
        vec[21] = (float) currentStature;
        vec[22] = (float) deflationSpread;
        vec[23] = winRate;
        vec[24] = slamTier;
        vec[25] = holdPct;

        return l2Normalize(vec);
    }
}
