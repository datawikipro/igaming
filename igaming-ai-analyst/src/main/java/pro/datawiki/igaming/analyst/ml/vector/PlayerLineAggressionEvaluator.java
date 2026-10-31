package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bottom-Up Player & Line Aggression Evaluator.
 * Implements the user's core tactical principle ("Ирландец в Осло"):
 * Aggression and card propensities are calculated from the individual players on the pitch,
 * categorized into tactical lines (Defense, Midfield, Attack), rather than coarse league stereotypes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerLineAggressionEvaluator {

    private final ClickHouseDdsClient clickHouse;

    // Fast in-memory cache for player discipline profiles: sofa_id -> PlayerDiscipline
    private final Map<Long, PlayerDiscipline> playerCache = new ConcurrentHashMap<>();

    public record PlayerDiscipline(
            long sofaPlayerId,
            String position,
            float aggressionIndex,
            float foulsDrawnIndex
    ) {}

    public record LineAggressionProfile(
            float defAggression,
            float midAggression,
            float attAggression,
            float gkAggression,
            float teamTotalAggression,
            float attFoulsDrawn
    ) {}

    public record MatchupFrictionDynamics(
            float midfieldClash,
            float attack1VsDefense2,
            float attack2VsDefense1,
            float totalMatchAggression,
            float aggressionDifferential,
            float dynamicCardBias
    ) {}

    /**
     * Evaluates the line-by-line aggression for a list of starting players.
     */
    public LineAggressionProfile evaluateLineAggression(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) {
            return new LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f);
        }

        // Collect sofa IDs to prefetch
        Set<Long> idsToFetch = new HashSet<>();
        for (Map<String, Object> p : starters) {
            Long sofaId = extractPlayerId(p);
            if (sofaId != null && sofaId > 0 && !playerCache.containsKey(sofaId)) {
                idsToFetch.add(sofaId);
            }
        }

        if (!idsToFetch.isEmpty()) {
            prefetchPlayers(idsToFetch);
        }

        List<Float> defAggList = new ArrayList<>();
        List<Float> midAggList = new ArrayList<>();
        List<Float> attAggList = new ArrayList<>();
        List<Float> gkAggList = new ArrayList<>();
        List<Float> attFoulsDrawnList = new ArrayList<>();
        List<Float> allAggList = new ArrayList<>();

        for (Map<String, Object> p : starters) {
            String rawPos = String.valueOf(p.getOrDefault("position", "")).trim().toUpperCase();
            Long sofaId = extractPlayerId(p);
            PlayerDiscipline disc = sofaId != null ? playerCache.get(sofaId) : null;

            String line = classifyLine(rawPos);
            float agg;
            float foulsDrawn;

            if (disc != null) {
                agg = disc.aggressionIndex();
                foulsDrawn = disc.foulsDrawnIndex();
            } else {
                // Positional prior fallback
                agg = switch (line) {
                    case "D" -> 0.42f;
                    case "M" -> 0.47f;
                    case "F" -> 0.37f;
                    default -> 0.06f;
                };
                foulsDrawn = switch (line) {
                    case "F" -> 0.28f;
                    case "M" -> 0.25f;
                    case "D" -> 0.18f;
                    default -> 0.04f;
                };
            }

            allAggList.add(agg);
            switch (line) {
                case "D" -> defAggList.add(agg);
                case "M" -> midAggList.add(agg);
                case "F" -> {
                    attAggList.add(agg);
                    attFoulsDrawnList.add(foulsDrawn);
                }
                default -> gkAggList.add(agg);
            }
        }

        float defAgg = average(defAggList, 0.42f);
        float midAgg = average(midAggList, 0.47f);
        float attAgg = average(attAggList, 0.37f);
        float gkAgg = average(gkAggList, 0.06f);
        float totalAgg = average(allAggList, 0.45f);
        float attDrawn = average(attFoulsDrawnList, 0.25f);

        return new LineAggressionProfile(defAgg, midAgg, attAgg, gkAgg, totalAgg, attDrawn);
    }

    /**
     * Evaluates mutual friction and clash dynamics between two opposing teams.
     */
    public MatchupFrictionDynamics evaluateMatchup(LineAggressionProfile p1, LineAggressionProfile p2) {
        float midfieldClash = p1.midAggression() * p2.midAggression();
        float att1Def2 = p1.attAggression() * p2.defAggression();
        float att2Def1 = p2.attAggression() * p1.defAggression();
        float totalMatchAgg = (p1.teamTotalAggression() + p2.teamTotalAggression()) / 2.0f;
        float diff = p1.teamTotalAggression() - p2.teamTotalAggression();

        // Dynamic Bottom-up Card Bias:
        // Scaled to 0.15 .. 0.95 range based on actual players on pitch + midfield clash friction
        float rawCardBias = (totalMatchAgg * 0.70f) + (midfieldClash * 0.50f);
        float dynamicCardBias = Math.max(0.15f, Math.min(0.95f, rawCardBias));

        return new MatchupFrictionDynamics(
                midfieldClash,
                att1Def2,
                att2Def1,
                totalMatchAgg,
                diff,
                dynamicCardBias
        );
    }

    private void prefetchPlayers(Set<Long> sofaIds) {
        if (sofaIds.isEmpty()) return;
        try {
            String idsStr = sofaIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            String sql = "SELECT player_sofascore_id, position, aggression_index, fouls_drawn_index " +
                    "FROM igaming_dds.player_line_aggression_v " +
                    "WHERE player_sofascore_id IN (" + idsStr + ")";
            List<Map<String, Object>> rows = clickHouse.queryJsonCached("batch_pl_agg:" + idsStr.hashCode(), sql);
            for (Map<String, Object> r : rows) {
                long sid = parseLong(r.get("player_sofascore_id"), 0L);
                if (sid > 0) {
                    String pos = String.valueOf(r.getOrDefault("position", ""));
                    float agg = parseFloat(r.get("aggression_index"), 0.45f);
                    float fd = parseFloat(r.get("fouls_drawn_index"), 0.20f);
                    playerCache.put(sid, new PlayerDiscipline(sid, pos, agg, fd));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to prefetch player aggression from ClickHouse: {}", e.getMessage());
        }
    }

    private String classifyLine(String pos) {
        if (pos == null || pos.isBlank()) return "M";
        if (pos.startsWith("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF") || pos.contains("ЗАЩ")) {
            return "D";
        }
        if (pos.startsWith("F") || pos.contains("CF") || pos.contains("ST") || pos.contains("RW") || pos.contains("LW") || pos.contains("ATT") || pos.contains("НАП")) {
            return "F";
        }
        if (pos.startsWith("G") || pos.contains("GK") || pos.contains("GOAL") || pos.contains("ВР")) {
            return "G";
        }
        return "M";
    }

    private Long extractPlayerId(Map<String, Object> p) {
        Object sofaIdObj = p.get("sofascore_id");
        if (sofaIdObj == null) sofaIdObj = p.get("sofa_id");
        if (sofaIdObj == null) sofaIdObj = p.get("id");
        return parseLong(sofaIdObj, null);
    }

    private float average(List<Float> list, float fallback) {
        if (list == null || list.isEmpty()) return fallback;
        float sum = 0.0f;
        for (float v : list) sum += v;
        return sum / list.size();
    }

    private Long parseLong(Object obj, Long fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

    private float parseFloat(Object obj, float fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.floatValue();
        try {
            return Float.parseFloat(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
