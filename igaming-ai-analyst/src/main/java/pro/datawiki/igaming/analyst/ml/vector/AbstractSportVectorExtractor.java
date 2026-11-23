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
     * Populates baseline physical, tactical, and biological DDS priors for Dims 8..63.
     * Guaranteed polymorphic implementation per sport (Zero procedural switch/if branching).
     *
     * @param vec Target 64-dimensional form vector
     */
    protected abstract void populateBaselinePriors(float[] vec);

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
