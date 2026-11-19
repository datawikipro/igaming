package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Tactical vector extractor for Tennis.
 * Queries tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Enriched with Universal Dynamic MMR (Multi-Tier Glicko-2), Latent Class, Injury Layoff Dynamics,
 * and Serve Integrity Telemetry.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py and Constitution Rule 1.1/1.2.
 */
@Slf4j
@Component
public class TennisVectorExtractor extends AbstractSportVectorExtractor<TennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TournamentTierClassifier tierClassifier;
    private final TennisSport sport = new TennisSport();

    @Autowired
    public TennisVectorExtractor(ClickHouseDdsClient clickHouse, TournamentTierClassifier tierClassifier) {
        this.clickHouse = clickHouse;
        this.tierClassifier = tierClassifier;
    }

    public TennisVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, new TournamentTierClassifier());
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
        TournamentTierClassifier.TierInfo tierInfo = tierClassifier.classify(ctx.getTournamentName());
        float slamTier = (8 - tierInfo.tierLevel()) / 7.0f; // Tier 1 -> 1.0, Tier 7 -> 0.14

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
        String surface = (ctx.getSurface() != null && !ctx.getSurface().isBlank()) ? ctx.getSurface() : "HARD";
        Optional<ClickHouseDdsClient.TennisMmr> playerMmrOpt = clickHouse.getTennisMmr(pid, surface);
        Optional<ClickHouseDdsClient.TennisMmr> oppMmrOpt = clickHouse.getTennisMmr(oppPid, surface);

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
        List<Map<String, Object>> rows = Collections.emptyList();
        String advSql = "SELECT total_matches, fs_pct, aces_per_match, dfs_per_match, win_rate, bp_saved_pct, " +
                "bp_conv_pct, hard_wr, clay_wr, hold_pct_modeled, break_pct_modeled, dominance_ratio_sigmoid, " +
                "days_since_last_match, comeback_matches, peak_rank, current_rank, quartic_stature " +
                "FROM igaming_dds.tennis_player_advanced_v WHERE player_id = ?";
        if (pid != null && pid > 0) {
            rows = clickHouse.queryJsonCached("tennis_adv:" + pid, advSql, pid);
        }

        // Fallback: If no stats found by ID, lookup by player name in in-memory tennis rankings (3,490 ranked players)
        if ((rows.isEmpty() || toInt(rows.get(0).get("total_matches"), 0) == 0) && ctx.getTeamName() != null && !ctx.getTeamName().isBlank()) {
            Optional<ClickHouseDdsClient.TennisRanking> rankOpt = clickHouse.resolveTennisPlayerRanking(ctx.getTeamName());
            if (rankOpt.isPresent()) {
                ClickHouseDdsClient.TennisRanking rr = rankOpt.get();
                if (rr.quarticStature() > 0.0) {
                    quarticStature = rr.quarticStature();
                    currentStature = quarticStature;
                } else {
                    int rk = rr.currentRank();
                    if (rk > 0 && rk <= 2000) {
                        quarticStature = Math.pow(rk, -0.25);
                        currentStature = quarticStature;
                    }
                }
                if (rr.playerId() > 0) {
                    rows = clickHouse.queryJsonCached("tennis_adv:" + rr.playerId(), advSql, rr.playerId());
                }
            }
        }

        if (!rows.isEmpty() && toInt(rows.get(0).get("total_matches"), 0) > 0) {
            Map<String, Object> r = rows.get(0);
            double fsPct = toDouble(r.get("fs_pct"), 62.0);
            double aces = toDouble(r.get("aces_per_match"), 4.0);
            double dfs = toDouble(r.get("dfs_per_match"), 2.0);
            double wr = toDouble(r.get("win_rate"), 0.50);
            double bpSaved = toDouble(r.get("bp_saved_pct"), 60.0);
            double bpConv = toDouble(r.get("bp_conv_pct"), 40.0);
            winRate = (float) wr;

            vec[30] = (float) (fsPct / 100.0);
            vec[33] = (float) (aces / 15.0);
            vec[34] = (float) (dfs / 10.0);
            vec[35] = (float) (bpSaved / 100.0);
            vec[36] = (float) (bpConv / 100.0);
            vec[39] = (float) toDouble(r.get("hard_wr"), 0.50);
            vec[40] = (float) toDouble(r.get("clay_wr"), 0.50);

            holdPct = (float) toDouble(r.get("hold_pct_modeled"), 0.80);
            breakPct = (float) toDouble(r.get("break_pct_modeled"), 0.22);
            vec[37] = holdPct;
            vec[38] = breakPct;

            drNormalized = (float) toDouble(r.get("dominance_ratio_sigmoid"), 0.50);

            // ── Post-Injury Recovery & Layoff Telemetry ───────────────────────
            double daysSinceLast = Math.max(0.0, toDouble(r.get("days_since_last_match"), 7.0));
            int comebackMatches = toInt(r.get("comeback_matches"), 5);

            layoffNorm = (float) (daysSinceLast / (daysSinceLast + 60.0));
            cadence = (float) (1.0 - Math.exp(-0.65 * comebackMatches));

            float baseRust = (daysSinceLast >= 45.0) ? (float) (0.35 * (daysSinceLast / (daysSinceLast + 45.0))) : 0.0f;
            rustPenalty = (float) (baseRust * Math.exp(-0.65 * Math.max(0, comebackMatches - 1)));

            double comebackFs = toDouble(r.get("comeback_fs_pct"), fsPct);
            serveIntegrity = (float) Math.min(1.0, Math.max(0.60, fsPct > 0 ? (comebackFs / fsPct) : 1.0));

            // ── Quartic Power-Law Stature Scaling & Injury Deflation ───────────
            int peakRank = toInt(r.get("peak_rank"), 1500);
            int currentRank = toInt(r.get("current_rank"), 1500);
            double ddsQuartic = toDouble(r.get("quartic_stature"), 0.05);

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
        vec[59] = Math.min(1.0f, Math.max(0.05f, 0.15f + rustPenalty * 0.50f));
        vec[60] = Math.min(0.98f, Math.max(0.40f, holdPct));
        vec[61] = Math.min(0.98f, Math.max(0.10f, breakPct));
        vec[62] = drNormalized;
        vec[63] = winRate;

        // Head-to-Head Lifetime against opponent
        Long oppId = ctx.getOpponentId();
        if (oppId != null && oppId > 0 && pid != null && pid > 0) {
            String h2hSql = "SELECT count() as h2h_m, " +
                    "avg(if(player1_id = ?, score1 > score2, score2 > score1)) as h2h_wr " +
                    "FROM tennis_match_fact WHERE (player1_id = ? AND player2_id = ?) " +
                    "OR (player1_id = ? AND player2_id = ?)";
            List<Map<String, Object>> h2hRows = clickHouse.queryJsonCached("tennis_h2h:" + pid + ":" + oppId, h2hSql, pid, pid, oppId, oppId, pid);
            if (!h2hRows.isEmpty() && toInt(h2hRows.get(0).get("h2h_m"), 0) > 0) {
                vec[44] = (float) toDouble(h2hRows.get(0).get("h2h_wr"), 0.50);
            }
        }

        return l2Normalize(vec);
    }
}
