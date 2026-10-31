package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.*;

/**
 * Orchestrates the full 291-dimensional multi-task tactical tensor.
 * Built with 100% Java domain models and zero Python procedural code.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TacticalTensorBuilder {

    private final SportVectorExtractorRegistry registry;

    @Getter
    @Builder
    public static class TensorResult {
        private final float[] features;
        private final int dimension;
        private final Sport sport;
        private final float team1WinRate;
        private final float team2WinRate;
        private final float formDifferential;
        private final boolean hasLineups;
    }

    /**
     * Builds the complete 291-dim neural input tensor from match dossier metadata.
     */
    public TensorResult buildTensor(Long matchId, Map<String, Object> dossierMeta) {
        String sportName = String.valueOf(dossierMeta.getOrDefault("sport_name", "FOOTBALL"));
        Sport sport = Sports.get(sportName);
        SportVectorExtractor<?> extractor = registry.getExtractor(sport);

        Long t1Id = parseLong(dossierMeta.get("team1_id"));
        Long t2Id = parseLong(dossierMeta.get("team2_id"));
        String t1Name = String.valueOf(dossierMeta.getOrDefault("team1_name", "Team 1"));
        String t2Name = String.valueOf(dossierMeta.getOrDefault("team2_name", "Team 2"));
        long matchTs = parseLong(dossierMeta.get("start_time"), System.currentTimeMillis());

        List<Map<String, Object>> t1Hist = extractList(dossierMeta.get("team1_recent_matches"));
        List<Map<String, Object>> t2Hist = extractList(dossierMeta.get("team2_recent_matches"));
        List<Map<String, Object>> s1 = extractList(dossierMeta.get("team1_starters"));
        List<Map<String, Object>> s2 = extractList(dossierMeta.get("team2_starters"));
        List<Map<String, Object>> b1 = extractList(dossierMeta.get("team1_bench"));
        List<Map<String, Object>> b2 = extractList(dossierMeta.get("team2_bench"));

        TacticalVectorContext ctx1 = TacticalVectorContext.builder()
                .teamId(t1Id)
                .teamName(t1Name)
                .opponentId(t2Id)
                .opponentName(t2Name)
                .matchTimestamp(matchTs)
                .recentMatches(t1Hist)
                .starters(s1)
                .bench(b1)
                .build();

        TacticalVectorContext ctx2 = TacticalVectorContext.builder()
                .teamId(t2Id)
                .teamName(t2Name)
                .opponentId(t1Id)
                .opponentName(t1Name)
                .matchTimestamp(matchTs)
                .recentMatches(t2Hist)
                .starters(s2)
                .bench(b2)
                .build();

        // 1. 64-dim Macro Tactical Vectors
        float[] v1 = extractor.extractFormVector(ctx1);
        float[] v2 = extractor.extractFormVector(ctx2);

        // 2. 259-dim Macro Tensor (v1, v2, diff, prod, ctx)
        float[] macro = new float[259];
        System.arraycopy(v1, 0, macro, 0, 64);
        System.arraycopy(v2, 0, macro, 64, 64);

        for (int i = 0; i < 64; i++) {
            macro[128 + i] = v1[i] - v2[i];        // diff
            macro[192 + i] = v1[i] * v2[i];        // prod
        }
        macro[256] = v1[0] - v2[0];                // win rate diff
        macro[257] = v1[5] - v2[5];                // goal diff diff
        macro[258] = 1.0f;                         // home advantage bias

        // 3. 32-dim Lineup & Biological Injury Decay
        float[] lineup = extractLineupFeatures(s1, s2, b1, b2);

        // 4. Combined 291-dim Tensor
        float[] fullTensor = new float[291];
        System.arraycopy(macro, 0, fullTensor, 0, 259);
        System.arraycopy(lineup, 0, fullTensor, 259, 32);

        boolean hasLineups = (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty());

        log.info("[Tactical Tensor Engine] Built 291-dim tensor for match {} ({} vs {} | {}): V1_WR={}, V2_WR={}, Lineups={}",
                matchId, t1Name, t2Name, sport.getCode(), String.format(Locale.ROOT, "%.2f", v1[0]), String.format(Locale.ROOT, "%.2f", v2[0]), hasLineups);

        return TensorResult.builder()
                .features(fullTensor)
                .dimension(291)
                .sport(sport)
                .team1WinRate(v1[0])
                .team2WinRate(v2[0])
                .formDifferential(v1[0] - v2[0])
                .hasLineups(hasLineups)
                .build();
    }

    private float[] extractLineupFeatures(List<Map<String, Object>> s1, List<Map<String, Object>> s2,
                                          List<Map<String, Object>> b1, List<Map<String, Object>> b2) {
        float[] lf = new float[32];
        // Default neutral ratings
        lf[0] = 0.0f; lf[1] = 6.8f; lf[2] = 6.2f; lf[3] = 7.5f; lf[6] = 6.8f;
        lf[12] = 0.0f; lf[13] = 6.8f; lf[14] = 6.2f; lf[15] = 7.5f; lf[18] = 6.8f;

        if (s1 != null && !s1.isEmpty()) {
            lf[0] = 1.0f;
            lf[1] = calculateAvgRating(s1);
            lf[6] = calculateGkRating(s1);
        }
        if (s2 != null && !s2.isEmpty()) {
            lf[12] = 1.0f;
            lf[13] = calculateAvgRating(s2);
            lf[18] = calculateGkRating(s2);
        }
        if (b1 != null && !b1.isEmpty()) {
            lf[24] = calculateBenchDepth(b1);
        }
        if (b2 != null && !b2.isEmpty()) {
            lf[25] = calculateBenchDepth(b2);
        }
        return lf;
    }

    private float calculateAvgRating(List<Map<String, Object>> starters) {
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            double r = parseDouble(p.get("rating"), 6.8);
            sum += r;
            count++;
        }
        return count > 0 ? (float) (sum / count) : 6.8f;
    }

    private float calculateGkRating(List<Map<String, Object>> starters) {
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();
            if (pos.contains("GK") || pos.contains("GOAL") || pos.contains("ВР")) {
                return (float) parseDouble(p.get("rating"), 6.8);
            }
        }
        return 6.8f;
    }

    private float calculateBenchDepth(List<Map<String, Object>> bench) {
        int jokers = 0;
        for (Map<String, Object> p : bench) {
            double r = parseDouble(p.get("rating"), 6.5);
            if (r >= 7.0) jokers++;
        }
        return Math.min(1.0f, jokers * 0.33f);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Object obj) {
        if (obj instanceof List<?> l) {
            return (List<Map<String, Object>>) l;
        }
        return Collections.emptyList();
    }

    private Long parseLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private long parseLong(Object obj, long fallback) {
        Long val = parseLong(obj);
        return val != null ? val : fallback;
    }

    private double parseDouble(Object obj, double fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
