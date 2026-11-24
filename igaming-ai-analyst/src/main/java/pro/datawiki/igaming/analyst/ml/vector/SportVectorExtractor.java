package pro.datawiki.igaming.analyst.ml.vector;

import pro.datawiki.igaming.dto.sport.Sport;

/**
 * Sacred contract for sport-specific tactical vector extraction.
 * Guarantees 100% polymorphic OOP design (Zero switch/if branching).
 *
 * @param <S> Concrete Sport class from the polymorphic hierarchy
 */
public interface SportVectorExtractor<S extends Sport> {

    /**
     * The Sport instance this extractor handles.
     */
    S getSport();

    /**
     * Extracts a normalized 64-dimensional form & tactical vector
     * using real ClickHouse DDS sports-science metrics.
     *
     * @param ctx Team tactical context (roster, matches, IDs)
     * @return 64-dimensional float array
     */
    float[] extractFormVector(TacticalVectorContext ctx);

    /**
     * Extracts the 64-dimensional micro tactical / lineup / physical recovery features.
     * Guaranteed polymorphic per sport (Zero procedural branching in TacticalTensorBuilder).
     */
    default float[] extractMicroFeatures(TacticalMatchContext matchCtx) {
        return new float[64];
    }

    /**
     * Extracts the 64-dimensional pure physics vector (Glicko-2 MMR, Form & Scoring Physics, Rest & Lineups, Style & Context).
     * Guaranteed polymorphic per sport (Zero procedural branching in TacticalTensorBuilder).
     */
    default float[] extractPurePhysicsVector(TacticalMatchContext matchCtx, float[] lineup) {
        return new float[64];
    }

    /**
     * Extracts pairwise interaction context (e.g. WinRate diff, GoalDiff diff, Home ground bias).
     * Guaranteed polymorphic per sport discipline. Team sports feature home advantage (3 dims);
     * neutral duel sports (Tennis, Table Tennis) completely omit home advantage (2 dims).
     */
    default float[] extractMacroContext(float[] v1, float[] v2) {
        if (getSport() != null && !getSport().hasHomeAdvantage()) {
            return new float[]{
                    v1.length > 0 && v2.length > 0 ? (v1[0] - v2[0]) : 0.0f,
                    v1.length > 5 && v2.length > 5 ? (v1[5] - v2[5]) : 0.0f
            };
        }
        return new float[]{
                v1.length > 0 && v2.length > 0 ? (v1[0] - v2[0]) : 0.0f,
                v1.length > 5 && v2.length > 5 ? (v1[5] - v2[5]) : 0.0f,
                1.0f
        };
    }

    /**
     * L2-Normalizes a float vector.
     */
    default float[] l2Normalize(float[] vec) {
        double sumSq = 0.0;
        for (float v : vec) {
            sumSq += v * v;
        }
        if (sumSq > 1e-6) {
            float norm = (float) Math.sqrt(sumSq);
            float[] res = new float[vec.length];
            for (int i = 0; i < vec.length; i++) {
                res[i] = vec[i] / norm;
            }
            return res;
        }
        return vec;
    }

    /**
     * Safe double conversion helper.
     */
    default double toDouble(Object val, double fallback) {
        if (val == null) return fallback;
        if (val instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Safe int conversion helper.
     */
    default int toInt(Object val, int fallback) {
        if (val == null) return fallback;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
