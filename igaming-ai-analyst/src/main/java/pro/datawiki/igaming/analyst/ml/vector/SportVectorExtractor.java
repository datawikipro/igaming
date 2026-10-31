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
