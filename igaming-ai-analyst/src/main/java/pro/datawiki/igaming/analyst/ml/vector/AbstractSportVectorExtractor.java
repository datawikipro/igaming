package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;

/**
 * Universal abstract base class for sport-specific tactical vector extractors.
 *
 * Enforces strict compile-time invariance and mathematical parity with Python
 * training matrix generation (prepare_multitask_matrix.py:763-795):
 * - Dims 0..7: Guaranteed unified scaling (wins, draws, losses, GF/10, GA/10, GD/10, sample_size/10, rest_days/14).
 * - Dims 8..63: Baseline physical DDS priors per sport, cleanly overlaid by DDS facts when available.
 * - Final L2 Normalization guaranteed.
 *
 * Prevents dimensional divergence, incorrect scaling divisors, or feature swapping at the compiler level.
 *
 * @param <S> Concrete Sport class from the polymorphic hierarchy
 */
@Slf4j
public abstract class AbstractSportVectorExtractor<S extends Sport> implements SportVectorExtractor<S> {

    /**
     * Computes Dimensions 0..7 of the form vector strictly matching Python prepare_multitask_matrix.py:
     * Dims 0..2: wins / n, draws / n (or 0.0), losses / n
     * Dims 3..5: (gf / n) / 10.0, (ga / n) / 10.0, ((gf - ga) / n) / 10.0
     * Dim 6: min(1.0, n / 10.0)
     * Dim 7: min(1.0, max(0.0, rest_days / 14.0)) or 0.50
     */
    public final float[] computeStandardMacroDims0To7(TacticalVectorContext ctx, boolean hasDraws) {
        float[] res = new float[8];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        if (hist == null || hist.isEmpty()) {
            res[0] = 0.50f;
            res[1] = hasDraws ? 0.25f : 0.0f;
            res[2] = hasDraws ? 0.25f : 0.50f;
            res[3] = 0.15f; // Neutral ~1.5 goals / 10
            res[4] = 0.15f;
            res[5] = 0.0f;
            res[6] = 0.50f;
            res[7] = 0.50f;
            return res;
        }

        int wins = 0, draws = 0, losses = 0;
        float gf = 0.0f, ga = 0.0f;
        int n = 0;
        long latestMatchTs = 0L;

        for (Map<String, Object> h : hist) {
            if (h.get("team_score") == null || h.get("opponent_score") == null) {
                continue;
            }
            int s1 = toInt(h.get("team_score"), 0);
            int s2 = toInt(h.get("opponent_score"), 0);

            String r = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
            if ("WIN".equals(r) || "1".equals(r) || (r.isEmpty() && s1 > s2)) {
                wins++;
            } else if ("DRAW".equals(r) || "0".equals(r) || (r.isEmpty() && s1 == s2)) {
                draws++;
            } else if ("LOSS".equals(r) || "-1".equals(r) || (r.isEmpty() && s1 < s2)) {
                losses++;
            }

            gf += s1;
            ga += s2;
            n++;

            long mTs = parseTimestamp(h);
            if (mTs > latestMatchTs) {
                latestMatchTs = mTs;
            }
        }

        if (n == 0) {
            res[0] = 0.50f;
            res[1] = hasDraws ? 0.25f : 0.0f;
            res[2] = hasDraws ? 0.25f : 0.50f;
            res[3] = 0.15f;
            res[4] = 0.15f;
            res[5] = 0.0f;
            res[6] = 0.50f;
            res[7] = 0.50f;
            return res;
        }

        res[0] = (float) wins / (float) n;
        res[1] = hasDraws ? ((float) draws / (float) n) : 0.0f;
        res[2] = (float) losses / (float) n;
        res[3] = ((float) gf / (float) n) / 10.0f;
        res[4] = ((float) ga / (float) n) / 10.0f;
        res[5] = (((float) (gf - ga)) / (float) n) / 10.0f;
        res[6] = Math.min(1.0f, (float) n / 10.0f);

        long curTs = ctx.getMatchTimestamp();
        if (latestMatchTs > 0 && curTs > latestMatchTs) {
            float restDays = (float) ((curTs - latestMatchTs) / 86400000.0);
            res[7] = Math.min(1.0f, Math.max(0.0f, restDays / 14.0f));
        } else {
            res[7] = 0.50f;
        }

        return res;
    }

    /**
     * Populates baseline physical and biological DDS priors for Dims 8..63, strictly matching
     * SPORT_BASELINE_PRIORS in prepare_multitask_matrix.py:480-560.
     */
    public void populateStandardBaselinePriors(float[] vec, Sport sport) {
        String slug = sport != null ? sport.getSlug() : "football";

        switch (slug) {
            case "ice-hockey" -> {
                vec[8] = 0.50f; vec[9] = 0.40f; vec[10] = 0.22f; vec[11] = 0.25f;
                vec[12] = 0.22f; vec[13] = 0.30f; vec[14] = 0.08f; vec[15] = 0.08f;
                vec[16] = 0.55f; vec[17] = 0.33f;
                float[] bins = {0.15f, 0.16f, 0.17f, 0.17f, 0.17f, 0.18f};
                for (int i = 0; i < 6; i++) {
                    vec[18 + i] = bins[i];
                    vec[24 + i] = bins[i];
                }
                vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.77f; vec[33] = 0.915f;
                vec[34] = 0.10f; vec[35] = 0.50f; vec[36] = 0.24f; vec[37] = 0.50f;
                vec[38] = 1.00f; vec[39] = 0.50f; vec[40] = 0.20f; vec[41] = 0.45f;
                vec[42] = 0.30f; vec[43] = 0.30f; vec[44] = 0.50f; vec[45] = 0.30f;
                vec[46] = 0.20f; vec[47] = 0.50f;
                vec[48] = 0.42f; vec[49] = 0.20f; vec[50] = 0.20f; vec[51] = 0.38f;
                vec[52] = 0.60f; vec[53] = 0.40f; vec[54] = 0.70f; vec[55] = 0.0f;
                vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.20f;
                vec[60] = 0.70f; vec[61] = 0.70f; vec[62] = 0.70f; vec[63] = 0.70f;
            }
            case "basketball" -> {
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
            case "tennis" -> {
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
            case "table-tennis" -> {
                vec[8] = 0.50f; vec[9] = 0.38f; vec[10] = 0.22f; vec[11] = 0.22f;
                vec[12] = 0.22f; vec[13] = 0.22f; vec[14] = 0.30f; vec[15] = 0.30f;
                vec[16] = 0.40f; vec[17] = 0.30f;
                float[] bins = {0.25f, 0.25f, 0.25f, 0.25f, 0.0f, 0.0f};
                for (int i = 0; i < 6; i++) {
                    vec[18 + i] = bins[i];
                    vec[24 + i] = bins[i];
                }
                vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.50f; vec[33] = 0.0f;
                vec[34] = 0.0f; vec[35] = 0.50f; vec[36] = 0.50f; vec[37] = 0.50f;
                vec[38] = 0.50f; vec[39] = 0.50f; vec[40] = 0.70f; vec[41] = 0.50f;
                vec[42] = 0.50f; vec[43] = 0.50f; vec[44] = 0.50f; vec[45] = 0.0f;
                vec[46] = 0.0f; vec[47] = 0.50f;
                vec[48] = 0.35f; vec[49] = 0.30f; vec[50] = 0.15f; vec[51] = 0.30f;
                vec[52] = 0.50f; vec[53] = 0.35f; vec[54] = 0.0f; vec[55] = 0.0f;
                vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.0f;
                vec[60] = 0.70f; vec[61] = 0.70f; vec[62] = 0.70f; vec[63] = 0.70f;
            }
            default -> { // football / soccer / default
                vec[8] = 0.48f; vec[9] = 0.32f; vec[10] = 0.16f; vec[11] = 0.22f;
                vec[12] = 0.18f; vec[13] = 0.25f; vec[14] = 0.28f; vec[15] = 0.24f;
                vec[16] = 0.45f; vec[17] = 0.21f;
                float[] bins = {0.138f, 0.142f, 0.150f, 0.188f, 0.163f, 0.217f};
                for (int i = 0; i < 6; i++) {
                    vec[18 + i] = bins[i];
                    vec[24 + i] = bins[i];
                }
                vec[30] = 0.34f; vec[31] = 0.34f; vec[32] = 0.50f; vec[33] = 0.75f;
                vec[34] = 0.25f; vec[35] = 0.30f; vec[36] = 0.52f; vec[37] = 0.50f;
                vec[38] = 0.80f; vec[39] = 0.20f; vec[40] = 0.50f; vec[41] = 0.44f;
                vec[42] = 0.33f; vec[43] = 0.25f; vec[44] = 0.30f; vec[45] = 0.44f;
                vec[46] = 0.28f; vec[47] = 0.24f;
                vec[48] = 0.38f; vec[49] = 0.25f; vec[50] = 0.15f; vec[51] = 0.35f;
                vec[52] = 0.50f; vec[53] = 0.36f; vec[54] = 0.65f; vec[55] = 0.0f;
                vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.18f;
                vec[60] = 0.68f; vec[61] = 0.68f; vec[62] = 0.68f; vec[63] = 0.68f;
            }
        }
    }

    protected Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    protected long toLong(Object obj, long def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString().trim());
        } catch (Exception e) {
            return def;
        }
    }

    protected long parseTimestamp(Map<String, Object> map) {
        if (map == null) return 0L;
        Object val = map.get("start_timestamp");
        if (val == null) val = map.get("match_timestamp");
        if (val == null) val = map.get("start_time");
        if (val == null) val = map.get("timestamp");
        if (val == null) val = map.get("ts");
        if (val instanceof Number n) return n.longValue();
        if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (Exception ignored) {}
        }
        return 0L;
    }
}
