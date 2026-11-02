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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TennisVectorExtractor implements SportVectorExtractor<TennisSport> {

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
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        // ── Dims 0..7: Macro Win/Loss/Sets/Rest ───────────────────────────────
        if (hist != null && !hist.isEmpty()) {
            int n = hist.size();
            int wins = 0, cleanWins = 0, over25 = 0, totalSets = 0;
            for (Map<String, Object> h : hist) {
                String res = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
                if ("WIN".equals(res) || "1".equals(res)) wins++;

                int s1 = toInt(h.get("team_score"), 0);
                int s2 = toInt(h.get("opponent_score"), 0);
                totalSets += (s1 + s2);
                if (s1 == 2 && s2 == 0) cleanWins++;
                if ((s1 + s2) >= 3) over25++;
            }

            float effN = n + 4.0f;
            vec[0] = (wins + 4.0f * 0.50f) / effN;
            vec[1] = 0.0f; // No draws in tennis!
            vec[2] = ((n - wins) + 4.0f * 0.50f) / effN;
            vec[3] = (cleanWins + 4.0f * 0.35f) / effN;
            vec[4] = (over25 + 4.0f * 0.35f) / effN;
            vec[5] = (totalSets + 4.0f * 2.2f) / (effN * 3.0f);
            vec[6] = Math.min(1.0f, (float) n / 10.0f);
            vec[7] = 0.5f; // rest days
        } else {
            vec[0] = 0.50f; vec[1] = 0.0f; vec[2] = 0.50f;
            vec[3] = 0.35f; vec[4] = 0.35f; vec[5] = 0.50f;
            vec[6] = 0.50f; vec[7] = 0.50f;
        }

        // Calibrated Priors for Dims 8..63
        vec[8]  = 0.50f; // conceded_first_set_rate
        vec[9]  = 0.35f; // comeback_win_rate
        vec[10] = 0.20f; // deciding_set_win_rate
        vec[11] = 0.50f; // tiebreak_clutch
        vec[12] = 0.20f; // collapsed_lead_rate
        vec[13] = 0.20f; // blown_set_points
        vec[14] = 0.35f; // straight_set_win_rate
        vec[15] = 0.35f; // straight_set_loss_rate
        vec[16] = 0.40f; // avg_max_deficit
        vec[17] = 0.30f; // late_game_stamina

        // Games distribution
        vec[18] = 0.33f; vec[19] = 0.33f; vec[20] = 0.34f;
        vec[24] = 0.33f; vec[25] = 0.33f; vec[26] = 0.34f;

        // Tactical & Service baselines
        vec[30] = 0.62f; // first_serve_in_pct (62%)
        vec[31] = 0.72f; // first_serve_won_pct (72%)
        vec[32] = 0.52f; // second_serve_won_pct (52%)
        vec[33] = 0.25f; // aces_per_game
        vec[34] = 0.15f; // double_faults_per_game
        vec[35] = 0.60f; // break_points_saved_pct (60%)
        vec[36] = 0.40f; // break_points_converted_pct (40%)
        vec[37] = 0.80f; // service_hold_pct
        vec[38] = 0.22f; // return_break_pct
        vec[39] = 0.50f; // hard_win_pct
        vec[40] = 0.50f; // clay_win_pct
        vec[41] = 0.50f; // grass_win_pct
        vec[42] = 0.50f; // tiebreak_win_pct
        vec[43] = 0.50f; // deciding_set_win_pct
        vec[44] = 0.50f; // h2h_dominance

        // Default Latent Class & Injury Recovery coordinates
        vec[45] = 0.50f; // latent_class_norm
        vec[46] = 0.00f; // rank_deflation_index
        vec[47] = 0.30f; // slam_pedigree_tier

        // Player Biological Age & Archetype (Tennis)
        vec[48] = 0.36f; // avg_age (normalized ~26 years)
        vec[49] = 0.30f; // young (<22)
        vec[50] = 0.15f; // veteran (>32)
        vec[51] = 0.30f; // age_std
        vec[52] = ctx.getEliminationPressureIndex();
        vec[53] = ctx.getDrawViabilityFlag();
        vec[54] = ctx.getRegionalCardBias();

        // Default Recovery & Layoff Coordinates
        vec[55] = 0.10f; // injury_layoff_duration
        vec[56] = 0.85f; // comeback_match_cadence
        vec[57] = 0.00f; // net_injury_rust_decay
        vec[58] = 0.95f; // serve_integrity_ratio

        vec[60] = 0.70f; // serve_rating
        vec[61] = 0.70f; // return_rating
        vec[62] = 0.70f; // baseline_rating
        vec[63] = 0.70f; // net_clutch_rating

        // ── Detect Elite Grand Slam / ATP Pedigree from Name & Context ────────
        String teamNameClean = ctx.getTeamName() != null ? ctx.getTeamName().toLowerCase(Locale.ROOT) : "";
        boolean isSlamPedigree = isEliteSlamPlayer(teamNameClean);
        float slamTier = isSlamPedigree ? 0.95f : 0.30f;
        String tournLower = (ctx.getTournamentName() != null ? ctx.getTournamentName() : "").toLowerCase(Locale.ROOT);
        if (tournLower.contains("slam") || tournLower.contains("open") || tournLower.contains("atp") || tournLower.contains("masters")) {
            slamTier = Math.max(slamTier, 0.75f);
        }
        vec[47] = slamTier;

        // If player has elite Slam / ATP tour pedigree, populate latent class and recovery priors
        if (isSlamPedigree) {
            vec[45] = 0.82f; // latent_class_norm
            vec[46] = 0.65f; // rank_deflation_index
            vec[55] = 0.65f; // injury_layoff_duration
            vec[56] = 0.73f; // comeback_match_cadence
            vec[57] = 0.08f; // net_injury_rust_decay
            vec[58] = 0.96f; // serve_integrity_ratio
        }

        // ── Real ClickHouse DDS Facts from tennis_match_fact ──────────────────
        Long pid = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        if (pid != null && pid > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(if(player1_id = " + pid + ", p1_first_serve_pct, p2_first_serve_pct)) as fs_pct, " +
                    "avg(if(player1_id = " + pid + ", p1_aces, p2_aces)) as aces, " +
                    "avg(if(player1_id = " + pid + ", p1_double_faults, p2_double_faults)) as dfs, " +
                    "avg(if(player1_id = " + pid + ", p1_break_points_saved_pct, p2_break_points_saved_pct)) as bp_saved, " +
                    "avg(if(player1_id = " + pid + ", p1_break_points_converted_pct, p2_break_points_converted_pct)) as bp_conv, " +
                    "avg(if(player1_id = " + pid + ", score1 > score2, score2 > score1)) as wr, " +
                    "countIf(surface = 'HARD' AND if(player1_id = " + pid + ", score1 > score2, score2 > score1)) / greatest(1, countIf(surface = 'HARD')) as hard_wr, " +
                    "countIf(surface = 'CLAY' AND if(player1_id = " + pid + ", score1 > score2, score2 > score1)) / greatest(1, countIf(surface = 'CLAY')) as clay_wr, " +
                    "dateDiff('day', max(match_time), now()) as days_since_last_match, " +
                    "countIf(match_time >= now() - INTERVAL 45 DAY) as comeback_matches, " +
                    "avgIf(if(player1_id = " + pid + ", p1_first_serve_pct, p2_first_serve_pct), match_time >= now() - INTERVAL 45 DAY) as comeback_fs_pct " +
                    "FROM tennis_match_fact WHERE player1_id = " + pid + " OR player2_id = " + pid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("tennis:" + pid, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double fsPct = toDouble(r.get("fs_pct"), 62.0);
                double aces = toDouble(r.get("aces"), 4.0);
                double dfs = toDouble(r.get("dfs"), 2.0);
                double wr = toDouble(r.get("wr"), 0.50);

                vec[30] = (float) (fsPct / 100.0);
                vec[33] = (float) (aces / 15.0);
                vec[34] = (float) (dfs / 10.0);
                vec[35] = (float) (toDouble(r.get("bp_saved"), 60.0) / 100.0);
                vec[36] = (float) (toDouble(r.get("bp_conv"), 40.0) / 100.0);
                vec[39] = (float) toDouble(r.get("hard_wr"), 0.50);
                vec[40] = (float) toDouble(r.get("clay_wr"), 0.50);

                // ── Post-Injury Recovery & Layoff Telemetry ───────────────────────
                double daysSinceLast = Math.max(0.0, toDouble(r.get("days_since_last_match"), 7.0));
                int comebackMatches = toInt(r.get("comeback_matches"), 5);

                // 1. Injury layoff duration (logistic saturation: 180 days -> ~0.75)
                float layoffNorm = (float) (daysSinceLast / (daysSinceLast + 60.0));
                vec[55] = layoffNorm;

                // 2. Comeback match cadence: exponential recovery by matches
                float cadence = (float) (1.0 - Math.exp(-0.65 * comebackMatches));
                vec[56] = cadence;

                // 3. Net injury rust decay (starts if days >= 45 days, wears off as M increases)
                float baseRust = (daysSinceLast >= 45.0) ? (float) (0.35 * (daysSinceLast / (daysSinceLast + 45.0))) : 0.0f;
                float rustPenalty = (float) (baseRust * Math.exp(-0.65 * Math.max(0, comebackMatches - 1)));
                vec[57] = rustPenalty;

                // 4. Serve integrity ratio: comeback 1st serve vs career baseline
                double comebackFs = toDouble(r.get("comeback_fs_pct"), fsPct);
                float serveIntegrity = (float) Math.min(1.0, Math.max(0.60, fsPct > 0 ? (comebackFs / fsPct) : 1.0));
                vec[58] = serveIntegrity;

                // 5. Latent class vs nominal deflated ranking
                if (wr >= 0.65 || aces >= 6.0 || isSlamPedigree) {
                    slamTier = Math.max(slamTier, 0.85f);
                    vec[47] = slamTier;
                }
                float latentClass = isSlamPedigree ? 0.82f : (float) Math.max(0.40, wr);
                vec[45] = latentClass;

                // Rank deflation index (Sleeping Giant signal)
                if (slamTier >= 0.70f && daysSinceLast >= 60.0) {
                    vec[46] = Math.min(1.0f, layoffNorm * slamTier);
                } else {
                    vec[46] = 0.0f;
                }
            } else if (isSlamPedigree) {
                // Known elite player without local ClickHouse DDS history yet
                vec[45] = 0.82f;
                vec[46] = 0.65f;
                vec[47] = 0.95f;
                vec[55] = 0.65f;
                vec[56] = 0.73f; // assume M=2 tune-up
                vec[57] = 0.08f;
                vec[58] = 0.96f;
            }

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
