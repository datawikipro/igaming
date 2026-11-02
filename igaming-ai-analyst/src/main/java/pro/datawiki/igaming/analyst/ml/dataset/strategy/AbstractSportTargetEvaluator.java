package pro.datawiki.igaming.analyst.ml.dataset.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.analyst.ml.dataset.SportTargetEvaluatorStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base helper for sport target evaluator strategies.
 */
public abstract class AbstractSportTargetEvaluator implements SportTargetEvaluatorStrategy {

    protected Map<String, Object> target(int id, String name, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("type", "binary");
        m.put("desc", desc);
        return m;
    }

    protected int safeInt(JsonNode node, String field, int defaultVal) {
        if (node == null || !node.has(field)) return defaultVal;
        JsonNode f = node.get(field);
        if (f.isInt() || f.isLong()) return f.intValue();
        try {
            return Integer.parseInt(f.asText().trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
