package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tactical vector extractor for Tennis.
 * Queries tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Enriched with Latent Class, Injury Layoff Dynamics, and Serve Integrity Telemetry.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TennisVectorExtractor extends AbstractSportVectorExtractor<TennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TennisSport sport = new TennisSport();

    private static final Set<String> GRAND_SLAM_OR_ATP_ELITE = Set.of(
            "prizmic", "djokovic", "alcaraz", "sinner", "medvedev", "zverev", "rublev",
            "tsitsipas", "rune", "hurkacz", "fritz", "ruud", "dimitrov", "de minaur", "shelton",
            "tiafoe", "musetti", "auger", "khachanov", "baez", "cerundolo", "jarry", "bublik",
            "arnaldi", "griekspoor", "thompson", "korda", "monfils", "berrettini", "shapovalov",
            "nishioka", "goffin", "carreno", "cilic", "wawrinka", "thiem", "nishikori", "kyrgios",
            "murray", "nadal", "bautista", "fognini", "gasquet", "struff", "mannarino", "lehecka",
            "draper", "fils", "mensik", "fonseca", "van de zandschulp", "moutet", "altmaier",
            "vukic", "safiullin", "kotov", "shevchenko", "marozsan", "seyboth wild", "diaz acosta"
    );

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

        // ── Detect Elite Grand Slam / ATP Pedigree from Name & Context ────────
        String teamNameClean = ctx.getTeamName() != null ? ctx.getTeamName().toLowerCase(Locale.ROOT) : "";
        boolean isSlamPedigree = isEliteSlamPlayer(teamNameClean);
        float slamTier = isSlamPedigree ? 0.95f : 0.30f;
        String tournLower = (ctx.getTournamentName() != null ? ctx.getTournamentName() : "").toLowerCase(Locale.ROOT);
        if (tournLower.contains("slam") || tournLower.contains("open") || tournLower.contains("atp") || tournLower.contains("masters")) {
            slamTier = Math.max(slamTier, 0.75f);
        }

        // Default priors if player is unranked or new
        double quarticStature = isSlamPedigree ? 0.85 : 0.05;
        double currentStature = isSlamPedigree ? 0.80 : 0.05;
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
        if (pid != null && pid > 0) {
            String sql = "SELECT * FROM igaming_dds.tennis_player_advanced_v WHERE player_id = " + pid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("tennis_adv:" + pid, sql);
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

                if (isSlamPedigree) {
                    peakRank = Math.min(peakRank, 10);
                    currentRank = Math.min(currentRank, 25);
                }

                if (peakRank > 0 && peakRank <= 2000) {
                    quarticStature = Math.max(ddsQuartic, Math.pow(peakRank, -0.25));
                } else if (isSlamPedigree) {
                    quarticStature = 0.85;
                }

                if (currentRank > 0 && currentRank <= 2000) {
                    currentStature = Math.pow(currentRank, -0.25);
                } else {
                    currentStature = quarticStature;
                }

                deflationSpread = Math.max(0.0, quarticStature - currentStature);
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
        vec[59] = Math.min(1.0f, Math.max(0.05f, 0.15f + rustPenalty * 0.50f));
        vec[60] = Math.min(0.98f, Math.max(0.40f, holdPct));
        vec[61] = Math.min(0.98f, Math.max(0.10f, breakPct));
        vec[62] = drNormalized;
        vec[63] = winRate;

            // Head-to-Head Lifetime against opponent
            Long oppId = ctx.getOpponentId();
            if (oppId != null && oppId > 0) {
                String h2hSql = "SELECT count() as h2h_m, " +
                        "avg(if(player1_id = " + pid + ", score1 > score2, score2 > score1)) as h2h_wr " +
                        "FROM tennis_match_fact WHERE (player1_id = " + pid + " AND player2_id = " + oppId + ") " +
                        "OR (player1_id = " + oppId + " AND player2_id = " + pid + ")";
                List<Map<String, Object>> h2hRows = clickHouse.queryJsonCached("tennis_h2h:" + pid + ":" + oppId, h2hSql);
                if (!h2hRows.isEmpty() && toInt(h2hRows.get(0).get("h2h_m"), 0) > 0) {
                    vec[44] = (float) toDouble(h2hRows.get(0).get("h2h_wr"), 0.50);
                }
            }

        return l2Normalize(vec);
    }

    private boolean isEliteSlamPlayer(String name) {
        if (name == null || name.isBlank()) return false;
        String clean = java.text.Normalizer.normalize(name.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        for (String star : GRAND_SLAM_OR_ATP_ELITE) {
            if (clean.contains(star)) return true;
        }
        return false;
    }
}
