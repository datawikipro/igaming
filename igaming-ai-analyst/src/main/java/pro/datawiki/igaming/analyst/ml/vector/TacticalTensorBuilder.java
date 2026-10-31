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
 * Implements bottom-up player line aggression and matchup friction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TacticalTensorBuilder {

    private final SportVectorExtractorRegistry registry;
    private final PlayerLineAggressionEvaluator playerLineAggressionEvaluator;

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
        private final PlayerLineAggressionEvaluator.LineAggressionProfile team1LineAggression;
        private final PlayerLineAggressionEvaluator.LineAggressionProfile team2LineAggression;
        private final PlayerLineAggressionEvaluator.MatchupFrictionDynamics matchupFriction;
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

        String stage = String.valueOf(dossierMeta.getOrDefault("stage", ""));
        String tournName = String.valueOf(dossierMeta.getOrDefault("tournament_name", ""));
        String countryName = String.valueOf(dossierMeta.getOrDefault("country_name", ""));

        float elimPressure = 0.0f;
        float drawViability = 1.0f;
        String stageLower = (stage + " " + tournName).toLowerCase();
        if (stageLower.contains("playoff") || stageLower.contains("final") || stageLower.contains("semi") ||
            stageLower.contains("quarter") || stageLower.contains("1/8") || stageLower.contains("1/16") ||
            stageLower.contains("knockout") || stageLower.contains("вылет") || stageLower.contains("плей-офф") ||
            stageLower.contains("кубок") || stageLower.contains("cup")) {
            elimPressure = 1.0f;
            drawViability = 0.0f;
        } else if (stageLower.contains("group") || stageLower.contains("групп")) {
            elimPressure = 0.5f;
            drawViability = 1.0f;
        }

        boolean hasLineups = (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty());

        // ── Bottom-Up Line Aggression & Matchup Friction (Principle: "Ирландец в Осло") ──
        PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg = playerLineAggressionEvaluator.evaluateLineAggression(s1);
        PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg = playerLineAggressionEvaluator.evaluateLineAggression(s2);
        PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction = playerLineAggressionEvaluator.evaluateMatchup(p1Agg, p2Agg);

        float regionalCardBias = 0.50f;
        if (hasLineups) {
            // Calculated directly from the 22 players on the pitch!
            regionalCardBias = friction.dynamicCardBias();
        } else {
            // Fallback Macro heuristic when lineups are not yet announced
            String cLower = (countryName + " " + tournName).toLowerCase();
            if (cLower.contains("paraguay") || cLower.contains("парагвай") ||
                cLower.contains("costa rica") || cLower.contains("коста-рика") ||
                cLower.contains("mexico") || cLower.contains("мексика") ||
                cLower.contains("colombia") || cLower.contains("колумбия") ||
                cLower.contains("argentina") || cLower.contains("аргентина") ||
                cLower.contains("chile") || cLower.contains("чили") ||
                cLower.contains("greece") || cLower.contains("греция") ||
                cLower.contains("turkey") || cLower.contains("турция") ||
                cLower.contains("serbia") || cLower.contains("сербия") ||
                cLower.contains("azerbaijan") || cLower.contains("азербайджан")) {
                regionalCardBias = 0.85f;
            } else if (cLower.contains("norway") || cLower.contains("sweden") || cLower.contains("denmark") ||
                       cLower.contains("finland") || cLower.contains("japan")) {
                regionalCardBias = 0.30f;
            }
        }

        TacticalVectorContext ctx1 = TacticalVectorContext.builder()
                .teamId(t1Id)
                .teamName(t1Name)
                .opponentId(t2Id)
                .opponentName(t2Name)
                .matchTimestamp(matchTs)
                .recentMatches(t1Hist)
                .starters(s1)
                .bench(b1)
                .stage(stage)
                .tournamentName(tournName)
                .countryName(countryName)
                .eliminationPressureIndex(elimPressure)
                .drawViabilityFlag(drawViability)
                .regionalCardBias(regionalCardBias)
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
                .stage(stage)
                .tournamentName(tournName)
                .countryName(countryName)
                .eliminationPressureIndex(elimPressure)
                .drawViabilityFlag(drawViability)
                .regionalCardBias(regionalCardBias)
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

        // 3. 32-dim Lineup & Positional Line Aggression
        float[] lineup = extractLineupFeatures(s1, s2, b1, b2, p1Agg, p2Agg, friction);

        // 4. Combined 291-dim Tensor
        float[] fullTensor = new float[291];
        System.arraycopy(macro, 0, fullTensor, 0, 259);
        System.arraycopy(lineup, 0, fullTensor, 259, 32);

        log.info("[Tactical Tensor Engine] Built 291-dim tensor for match {} ({} vs {} | {}): V1_WR={}, V2_WR={}, Lineups={}, DefAgg=[{}/{}], MidAgg=[{}/{}], AttAgg=[{}/{}], MidClash={}, CardBias={}",
                matchId, t1Name, t2Name, sport.getCode(),
                String.format(Locale.ROOT, "%.2f", v1[0]),
                String.format(Locale.ROOT, "%.2f", v2[0]),
                hasLineups,
                String.format(Locale.ROOT, "%.2f", p1Agg.defAggression()),
                String.format(Locale.ROOT, "%.2f", p2Agg.defAggression()),
                String.format(Locale.ROOT, "%.2f", p1Agg.midAggression()),
                String.format(Locale.ROOT, "%.2f", p2Agg.midAggression()),
                String.format(Locale.ROOT, "%.2f", p1Agg.attAggression()),
                String.format(Locale.ROOT, "%.2f", p2Agg.attAggression()),
                String.format(Locale.ROOT, "%.2f", friction.midfieldClash()),
                String.format(Locale.ROOT, "%.2f", regionalCardBias));

        return TensorResult.builder()
                .features(fullTensor)
                .dimension(291)
                .sport(sport)
                .team1WinRate(v1[0])
                .team2WinRate(v2[0])
                .formDifferential(v1[0] - v2[0])
                .hasLineups(hasLineups)
                .team1LineAggression(p1Agg)
                .team2LineAggression(p2Agg)
                .matchupFriction(friction)
                .build();
    }

    private float[] extractLineupFeatures(List<Map<String, Object>> s1, List<Map<String, Object>> s2,
                                          List<Map<String, Object>> b1, List<Map<String, Object>> b2,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg,
                                          PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction) {
        float[] lf = new float[32];
        // Default neutral ratings
        lf[0] = 0.0f; lf[1] = 6.8f; lf[2] = 6.2f; lf[3] = 7.5f; lf[6] = 6.8f;
        lf[12] = 0.0f; lf[13] = 6.8f; lf[14] = 6.2f; lf[15] = 7.5f; lf[18] = 6.8f;

        if (s1 != null && !s1.isEmpty()) {
            lf[0] = 1.0f;
            lf[1] = calculateAvgRating(s1);
            lf[6] = calculateGkRating(s1);
        }
        // Team 1 Positional Line Aggression
        lf[7] = p1Agg.defAggression();
        lf[8] = p1Agg.midAggression();
        lf[9] = p1Agg.attAggression();
        lf[10] = p1Agg.teamTotalAggression();
        lf[11] = p1Agg.attFoulsDrawn();

        if (s2 != null && !s2.isEmpty()) {
            lf[12] = 1.0f;
            lf[13] = calculateAvgRating(s2);
            lf[18] = calculateGkRating(s2);
        }
        // Team 2 Positional Line Aggression
        lf[19] = p2Agg.defAggression();
        lf[20] = p2Agg.midAggression();
        lf[21] = p2Agg.attAggression();
        lf[22] = p2Agg.teamTotalAggression();
        lf[23] = p2Agg.attFoulsDrawn();

        if (b1 != null && !b1.isEmpty()) {
            lf[24] = calculateBenchDepth(b1);
        }
        if (b2 != null && !b2.isEmpty()) {
            lf[25] = calculateBenchDepth(b2);
        }

        // Mutual Matchup Friction Dynamics (Clashes & Friction)
        lf[26] = friction.midfieldClash();
        lf[27] = friction.attack1VsDefense2();
        lf[28] = friction.attack2VsDefense1();
        lf[29] = friction.totalMatchAggression();
        lf[30] = friction.aggressionDifferential();
        lf[31] = friction.dynamicCardBias();

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
