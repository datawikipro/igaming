package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;

import java.util.*;

/**
 * Orchestrates the full 291-dimensional multi-task tactical tensor.
 * Built with 100% Java domain models and zero Python procedural code.
 * Implements bottom-up player line aggression and matchup friction.
 * Enriched with Point-in-Time tournament motivation and leverage (Dims 55..58).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TacticalTensorBuilder {

    private final SportVectorExtractorRegistry registry;
    private final PlayerLineAggressionEvaluator playerLineAggressionEvaluator;
    private final ClickHouseDdsClient clickHouse;

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
        private final float homeBlowoutGradient;
        private final float awayBlowoutGradient;
        private final boolean isGroupOfDeath;
        private final Float refereeAvgYellowCards;
        private final String refereeStrictnessTier;
        private final boolean isDerby;
        private final float compositeCardBias;
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
        Long t1SofaId = parseLong(dossierMeta.get("team1_sofascore_id"));
        Long t2SofaId = parseLong(dossierMeta.get("team2_sofascore_id"));
        String t1Name = String.valueOf(dossierMeta.getOrDefault("team1_name", "Team 1"));
        String t2Name = String.valueOf(dossierMeta.getOrDefault("team2_name", "Team 2"));
        long matchTs = parseLong(dossierMeta.get("start_time"), System.currentTimeMillis());

        List<Map<String, Object>> t1Hist = extractList(dossierMeta.get("team1_recent_matches"));
        List<Map<String, Object>> t2Hist = extractList(dossierMeta.get("team2_recent_matches"));
        List<Map<String, Object>> s1 = extractList(dossierMeta.get("team1_starters"));
        List<Map<String, Object>> s2 = extractList(dossierMeta.get("team2_starters"));
        List<Map<String, Object>> b1 = extractList(dossierMeta.get("team1_bench"));
        List<Map<String, Object>> b2 = extractList(dossierMeta.get("team2_bench"));

        Integer regMinutes = (dossierMeta.get("regulation_minutes") instanceof Number n) ? n.intValue() : null;
        Double paceMult = (dossierMeta.get("pace_multiplier") instanceof Number n) ? n.doubleValue() : null;
        Double expTotal = (dossierMeta.get("expected_total_baseline") instanceof Number n) ? n.doubleValue() : null;

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

        // ── Point-in-Time Tournament Motivation & Standings (Zero Lookahead Bias) ──
        Map<String, Object> tournSnap = (matchId != null && matchId > 0)
                ? clickHouse.getTournamentPointInTime(matchId)
                : Collections.emptyMap();

        float t1Blowout = (float) parseDouble(tournSnap.get("home_blowout_gradient"), 0.0);
        float t2Blowout = (float) parseDouble(tournSnap.get("away_blowout_gradient"), 0.0);
        float t1Leverage = (float) parseDouble(tournSnap.get("home_win_leverage"), elimPressure > 0.8f ? 1.0 : 0.50);
        float t2Leverage = (float) parseDouble(tournSnap.get("away_win_leverage"), elimPressure > 0.8f ? 1.0 : 0.50);
        float t1Hazard = (float) parseDouble(tournSnap.get("home_elimination_hazard"), elimPressure > 0.8f ? 1.0 : 0.50);
        float t2Hazard = (float) parseDouble(tournSnap.get("away_elimination_hazard"), elimPressure > 0.8f ? 1.0 : 0.50);
        float t1Prize = (float) parseDouble(tournSnap.get("home_prize_wage_ratio"), 0.05);
        float t2Prize = (float) parseDouble(tournSnap.get("away_prize_wage_ratio"), elimPressure > 0.8f ? 0.35 : 0.05);
        float groupEntropy = (float) parseDouble(tournSnap.get("group_entropy"), 0.50);
        boolean isGroupOfDeath = parseLong(tournSnap.get("is_group_of_death"), 0L) == 1L;

        boolean hasLineups = (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty());

        // ── Bottom-Up Line Aggression & Matchup Friction ─────────────────────────
        PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg = playerLineAggressionEvaluator.evaluateLineAggression(s1);
        PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg = playerLineAggressionEvaluator.evaluateLineAggression(s2);
        PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction = playerLineAggressionEvaluator.evaluateMatchup(p1Agg, p2Agg);

        // ── 1. Regional / Macro Baseline Card Propensity ──
        float macroRegionalBias = 0.50f;
        String cLower = (countryName + " " + tournName).toLowerCase();
        if (cLower.contains("paraguay") || cLower.contains("парагвай") ||
            cLower.contains("costa rica") || cLower.contains("коста-рика") ||
            cLower.contains("mexico") || cLower.contains("мексика") ||
            cLower.contains("colombia") || cLower.contains("колумбия") ||
            cLower.contains("argentina") || cLower.contains("аргентина") ||
            cLower.contains("chile") || cLower.contains("чили") ||
            cLower.contains("brazil") || cLower.contains("бразилия") ||
            cLower.contains("uruguay") || cLower.contains("уругвай") ||
            cLower.contains("peru") || cLower.contains("перу") ||
            cLower.contains("ecuador") || cLower.contains("эквадор") ||
            cLower.contains("bolivia") || cLower.contains("боливия") ||
            cLower.contains("greece") || cLower.contains("греция") ||
            cLower.contains("turkey") || cLower.contains("турция") ||
            cLower.contains("serbia") || cLower.contains("сербия") ||
            cLower.contains("spain") || cLower.contains("испания") || cLower.contains("segunda") ||
            cLower.contains("azerbaijan") || cLower.contains("азербайджан")) {
            macroRegionalBias = 0.85f;
        } else if (cLower.contains("norway") || cLower.contains("sweden") || cLower.contains("denmark") ||
                   cLower.contains("finland") || cLower.contains("japan")) {
            macroRegionalBias = 0.30f;
        }

        // ── 2. Local Derby & Rivalry Detection (Derby Friction Index) ──
        boolean isDerby = Boolean.TRUE.equals(dossierMeta.get("is_derby"))
                || DerbyRivalryDetector.isDerbyMatch(t1Name, t2Name, tournName, countryName);
        float derbyFriction = isDerby ? 0.88f : 0.25f;

        // ── 3. Referee Strictness Index (Conversion Rate of Fouls to Cards) ──
        Float refAvgYc = (dossierMeta.get("referee_avg_yellow_cards") instanceof Number n) ? n.floatValue() : null;
        String refStrictness = (String) dossierMeta.get("referee_strictness");
        float refereeStrictnessIndex;
        if (refAvgYc != null && refAvgYc > 0) {
            // Normalizing against baseline of 4.0 YC/match (scaled to 6.0 ceiling)
            refereeStrictnessIndex = Math.min(1.0f, Math.max(0.15f, refAvgYc / 6.0f));
        } else if (refStrictness != null) {
            refereeStrictnessIndex = switch (refStrictness.toUpperCase()) {
                case "VERY_STRICT" -> 0.90f;
                case "STRICT" -> 0.78f;
                case "LENIENT" -> 0.35f;
                default -> 0.55f;
            };
        } else {
            // Fallback to regional referee standard
            refereeStrictnessIndex = macroRegionalBias;
        }

        // ── 4. Player & Line Aggression Friction ──
        float playerFriction = hasLineups ? friction.dynamicCardBias() : macroRegionalBias;

        // ── 5. Superposition of 3 Forces: Referee (40%), Players (35%), Derby/Regional Context (25%) ──
        float compositeCardBias = (0.40f * refereeStrictnessIndex)
                + (0.35f * playerFriction)
                + (0.25f * Math.max(macroRegionalBias, derbyFriction));
        compositeCardBias = Math.min(0.98f, Math.max(0.12f, compositeCardBias));

        TacticalVectorContext ctx1 = TacticalVectorContext.builder()
                .teamId(t1Id)
                .teamName(t1Name)
                .teamSofascoreId(t1SofaId)
                .opponentId(t2Id)
                .opponentName(t2Name)
                .opponentSofascoreId(t2SofaId)
                .matchTimestamp(matchTs)
                .recentMatches(t1Hist)
                .starters(s1)
                .bench(b1)
                .stage(stage)
                .tournamentName(tournName)
                .countryName(countryName)
                .regulationMinutes(regMinutes)
                .paceMultiplier(paceMult)
                .expectedTotalBaseline(expTotal)
                .eliminationPressureIndex(elimPressure)
                .drawViabilityFlag(drawViability)
                .regionalCardBias(compositeCardBias)
                .refereeCardStrictness(refereeStrictnessIndex)
                .refereeAvgYellowCards(refAvgYc)
                .refereeStrictnessTier(refStrictness)
                .isDerby(isDerby)
                .derbyFrictionIndex(derbyFriction)
                .blowoutGradient(t1Blowout)
                .winLeverage(t1Leverage)
                .eliminationHazard(t1Hazard)
                .prizeWageRatio(t1Prize)
                .groupEntropy(groupEntropy)
                .isGroupOfDeath(isGroupOfDeath)
                .build();

        TacticalVectorContext ctx2 = TacticalVectorContext.builder()
                .teamId(t2Id)
                .teamName(t2Name)
                .teamSofascoreId(t2SofaId)
                .opponentId(t1Id)
                .opponentName(t1Name)
                .opponentSofascoreId(t1SofaId)
                .matchTimestamp(matchTs)
                .recentMatches(t2Hist)
                .starters(s2)
                .bench(b2)
                .stage(stage)
                .tournamentName(tournName)
                .countryName(countryName)
                .regulationMinutes(regMinutes)
                .paceMultiplier(paceMult)
                .expectedTotalBaseline(expTotal)
                .eliminationPressureIndex(elimPressure)
                .drawViabilityFlag(drawViability)
                .regionalCardBias(compositeCardBias)
                .refereeCardStrictness(refereeStrictnessIndex)
                .refereeAvgYellowCards(refAvgYc)
                .refereeStrictnessTier(refStrictness)
                .isDerby(isDerby)
                .derbyFrictionIndex(derbyFriction)
                .blowoutGradient(t2Blowout)
                .winLeverage(t2Leverage)
                .eliminationHazard(t2Hazard)
                .prizeWageRatio(t2Prize)
                .groupEntropy(groupEntropy)
                .isGroupOfDeath(isGroupOfDeath)
                .build();

        // 1. 64-dim Macro Tactical Vectors (Dims 55..58 populated with Tournament Motivation)
        float[] v1 = extractor.extractFormVector(ctx1);
        float[] v2 = extractor.extractFormVector(ctx2);

        // 2. 259-dim Macro Tensor (v1, v2, diff, prod, ctx)
        float[] macro = new float[259];
        System.arraycopy(v1, 0, macro, 0, 64);
        System.arraycopy(v2, 0, macro, 64, 64);

        for (int i = 0; i < 64; i++) {
            macro[128 + i] = v1[i] - v2[i];        // diff (includes blowout and leverage asymmetry!)
            macro[192 + i] = v1[i] * v2[i];        // prod (friction & clash interaction!)
        }
        macro[256] = v1[0] - v2[0];                // win rate diff
        macro[257] = v1[5] - v2[5];                // goal diff diff
        macro[258] = 1.0f;                         // home advantage bias

        // 3. 32-dim Lineup & Positional Line Aggression (Polymorphic for Team vs Individual Tennis)
        float[] lineup;
        if (sport instanceof TennisSport || (!sport.isTeamSport() && "TENNIS".equalsIgnoreCase(sport.getCode()))) {
            lineup = extractTennisPhysicalAndRecoveryFeatures(ctx1, ctx2, v1, v2);
        } else {
            lineup = extractLineupFeatures(s1, s2, b1, b2, p1Agg, p2Agg, friction, compositeCardBias);
        }

        // 4. Combined 291-dim Tensor
        float[] fullTensor = new float[291];
        System.arraycopy(macro, 0, fullTensor, 0, 259);
        System.arraycopy(lineup, 0, fullTensor, 259, 32);

        log.info("[Tactical Tensor Engine] Built 291-dim tensor for match {} ({} vs {} | {}): V1_WR={}, V2_WR={}, ∇GD=[{}/{}], Lev=[{}/{}], GroupOfDeath={}, Lineups={}, RefCards={}, Derby={}, CompositeCardBias={}",
                matchId, t1Name, t2Name, sport.getCode(),
                String.format(Locale.ROOT, "%.2f", v1[0]),
                String.format(Locale.ROOT, "%.2f", v2[0]),
                String.format(Locale.ROOT, "%.2f", t1Blowout),
                String.format(Locale.ROOT, "%.2f", t2Blowout),
                String.format(Locale.ROOT, "%.2f", t1Leverage),
                String.format(Locale.ROOT, "%.2f", t2Leverage),
                isGroupOfDeath,
                hasLineups,
                refAvgYc != null ? String.format(Locale.ROOT, "%.1f", refAvgYc) : (refStrictness != null ? refStrictness : "N/A"),
                isDerby,
                String.format(Locale.ROOT, "%.2f", compositeCardBias));

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
                .homeBlowoutGradient(t1Blowout)
                .awayBlowoutGradient(t2Blowout)
                .isGroupOfDeath(isGroupOfDeath)
                .refereeAvgYellowCards(refAvgYc)
                .refereeStrictnessTier(refStrictness)
                .isDerby(isDerby)
                .compositeCardBias(compositeCardBias)
                .build();
    }

    private float[] extractLineupFeatures(List<Map<String, Object>> s1, List<Map<String, Object>> s2,
                                          List<Map<String, Object>> b1, List<Map<String, Object>> b2,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg,
                                          PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction,
                                          float compositeCardBias) {
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
        lf[31] = compositeCardBias;

        return lf;
    }

    /**
     * Constructs the 32-dimensional micro physical integrity and recovery tensor for Tennis (Cluster 6 Fix).
     * Replaces football positions (GK/DEF/MID/ATT) with individual player latent class, injury rust,
     * match cadence, serve integrity, and head-to-head class disparity dynamics.
     */
    private float[] extractTennisPhysicalAndRecoveryFeatures(TacticalVectorContext ctx1,
                                                            TacticalVectorContext ctx2,
                                                            float[] v1,
                                                            float[] v2) {
        float[] lf = new float[32];

        // ── Player 1 Micro Physical & Recovery Profile (Dims 0..11) ─────────────
        lf[0] = 1.0f;                                              // is_confirmed_entry
        lf[1] = v1[45] * (1.0f - v1[57]);                          // effective_player_rating (latent * (1 - rust))
        lf[2] = v1[17];                                            // baseline_stamina_rating
        lf[3] = (v1[45] * 0.60f + v1[30] * 0.40f) * (1.0f - 0.25f * v1[57]); // short_set_power (latent class + serve sets 1-2)
        lf[4] = v1[57];                                            // total_injury_rust
        lf[5] = v1[56];                                            // comeback_match_cadence
        lf[6] = v1[58];                                            // serve_health_ratio
        lf[7] = v1[34];                                            // double_fault_hazard
        lf[8] = v1[35];                                            // break_point_clutch
        lf[9] = Math.min(1.0f, v1[57] * 1.5f);                     // unforced_error_volatility
        lf[10] = (v1[55] > 0.5f && v1[56] < 0.3f) ? 0.40f : 0.05f;// early_retirement_hazard
        lf[11] = v1[46] > 0.25f ? 1.0f : 0.0f;                     // protected_ranking_deflated_flag

        // ── Player 2 Micro Physical & Recovery Profile (Dims 12..23) ────────────
        lf[12] = 1.0f;                                             // is_confirmed_entry
        lf[13] = v2[45] * (1.0f - v2[57]);                         // effective_player_rating
        lf[14] = v2[17];                                           // baseline_stamina_rating
        lf[15] = (v2[45] * 0.60f + v2[30] * 0.40f) * (1.0f - 0.25f * v2[57]); // short_set_power
        lf[16] = v2[57];                                           // total_injury_rust
        lf[17] = v2[56];                                           // comeback_match_cadence
        lf[18] = v2[58];                                           // serve_health_ratio
        lf[19] = v2[34];                                           // double_fault_hazard
        lf[20] = v2[35];                                           // break_point_clutch
        lf[21] = Math.min(1.0f, v2[57] * 1.5f);                    // unforced_error_volatility
        lf[22] = (v2[55] > 0.5f && v2[56] < 0.3f) ? 0.40f : 0.05f;// early_retirement_hazard
        lf[23] = v2[46] > 0.25f ? 1.0f : 0.0f;                     // protected_ranking_deflated_flag

        // ── Head-to-Head & Asymmetry Dynamics (Dims 24..31) ──────────────────────
        lf[24] = lf[1] - lf[13];                                   // diff_effective_rating (P1_eff - P2_eff)
        lf[25] = lf[3] - lf[15];                                   // diff_short_set_power
        lf[26] = lf[4] - lf[16];                                   // diff_injury_rust
        lf[27] = lf[6] - lf[18];                                   // diff_serve_integrity
        lf[28] = lf[2] - lf[14];                                   // diff_deciding_set_stamina

        // Class disparity mask: latent class delta >= 0.05f in L2-normalized space when win-rate delta is artificially compressed (<= 0.15f)
        boolean classDisparity = (Math.abs(v1[45] - v2[45]) >= 0.05f && Math.abs(v1[0] - v2[0]) <= 0.15f);
        lf[29] = classDisparity ? 1.0f : 0.0f;                     // class_disparity_mask
        lf[30] = (float) (lf[3] * (1.0 - Math.min(1.0, v2[38])));  // short_match_blowout_potential
        lf[31] = (1.0f - lf[4]) * (1.0f - lf[16]);                // both_players_at_peak

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
