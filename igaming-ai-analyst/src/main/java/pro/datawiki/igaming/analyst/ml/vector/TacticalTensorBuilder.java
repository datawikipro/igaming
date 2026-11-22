package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.TournamentStageFormat;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;
import pro.datawiki.igaming.dto.sport.racket.TennisSport;
import pro.datawiki.igaming.dto.ml.tensor.*;

import java.util.*;

/**
 * Orchestrates the full 291-dimensional multi-task tactical tensor.
 * Built with 100% Java domain models and zero Python procedural code.
 * Implements bottom-up player line aggression and matchup friction.
 * Enriched with Point-in-Time tournament motivation and leverage (Dims 55..58).
 */
@Slf4j
@Component
public class TacticalTensorBuilder {

    private final SportVectorExtractorRegistry registry;
    private final PlayerLineAggressionEvaluator playerLineAggressionEvaluator;
    private final SquadChemistryEvaluator squadChemistryEvaluator;
    private final ClickHouseDdsClient clickHouse;
    private final QdrantTacticalDnaClient qdrantClient;
    private final TournamentTierClassifier tierClassifier;

    @Autowired
    public TacticalTensorBuilder(
            SportVectorExtractorRegistry registry,
            PlayerLineAggressionEvaluator playerLineAggressionEvaluator,
            SquadChemistryEvaluator squadChemistryEvaluator,
            ClickHouseDdsClient clickHouse,
            QdrantTacticalDnaClient qdrantClient,
            TournamentTierClassifier tierClassifier
    ) {
        this.registry = registry;
        this.playerLineAggressionEvaluator = playerLineAggressionEvaluator;
        this.squadChemistryEvaluator = squadChemistryEvaluator;
        this.clickHouse = clickHouse;
        this.qdrantClient = qdrantClient;
        this.tierClassifier = tierClassifier != null ? tierClassifier : new TournamentTierClassifier();
    }

    public TacticalTensorBuilder(
            SportVectorExtractorRegistry registry,
            PlayerLineAggressionEvaluator playerLineAggressionEvaluator,
            SquadChemistryEvaluator squadChemistryEvaluator,
            ClickHouseDdsClient clickHouse,
            QdrantTacticalDnaClient qdrantClient
    ) {
        this(registry, playerLineAggressionEvaluator, squadChemistryEvaluator, clickHouse, qdrantClient, new TournamentTierClassifier());
    }

    @Getter
    @Builder
    public static class TensorResult {
        private final PolymorphicTacticalTensor polymorphicTensor;
        private final float[] features;
        private final int dimension;
        private final Sport sport;
        private final float team1WinRate;
        private final float team2WinRate;
        private final float formDifferential;
        private final boolean hasLineups;
        private final PlayerLineAggressionEvaluator.LineAggressionProfile team1LineAggression;
        private final PlayerLineAggressionEvaluator.LineAggressionProfile team2LineAggression;
        private final SquadChemistryEvaluator.SquadChemistryAndFatigue team1SquadChemistry;
        private final SquadChemistryEvaluator.SquadChemistryAndFatigue team2SquadChemistry;
        private final PlayerLineAggressionEvaluator.MatchupFrictionDynamics matchupFriction;
        private final float homeBlowoutGradient;
        private final float awayBlowoutGradient;
        private final boolean isGroupOfDeath;
        private final Float refereeAvgYellowCards;
        private final String refereeStrictnessTier;
        private final boolean isDerby;
        private final float compositeCardBias;

        public float getRedCardHazard() {
            return compositeCardBias;
        }

        public float getDerbyFrictionIndex() {
            return isDerby ? 1.0f : 0.0f;
        }
    }

    /**
     * Builds the complete 291-dim neural input tensor from match dossier metadata.
     */
    public TensorResult buildTensor(Long matchId, Map<String, Object> dossierMeta) {
        Object sportMeta = dossierMeta.get("sport_name");
        if (sportMeta == null) {
            sportMeta = dossierMeta.get("sport");
        }
        if (sportMeta == null) {
            throw new IllegalArgumentException("Cannot build tactical tensor without sport_name for match #" + matchId);
        }
        String sportName = String.valueOf(sportMeta);
        Sport sport = Sports.get(sportName);
        if (sport == null) {
            throw new IllegalArgumentException("Unsupported sport for tactical tensor: " + sportName + " (match #" + matchId + ")");
        }
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

        Long tournId = (dossierMeta.get("tournament_id") instanceof Number n) ? n.longValue() : null;
        String stage = String.valueOf(dossierMeta.getOrDefault("stage", ""));
        String tournName = String.valueOf(dossierMeta.getOrDefault("tournament_name", ""));
        String countryName = String.valueOf(dossierMeta.getOrDefault("country_name", ""));

        Object rawSurface = dossierMeta.getOrDefault("surface", dossierMeta.get("ground_type"));
        CourtSurface surface;
        if (rawSurface instanceof CourtSurface cs) {
            surface = cs;
        } else if (rawSurface != null && !rawSurface.toString().isBlank() && !"null".equalsIgnoreCase(rawSurface.toString().trim())) {
            surface = CourtSurface.resolveSurface(rawSurface.toString(), tournName);
        } else {
            surface = CourtSurface.resolveSurface(null, tournName);
        }

        Boolean isCup = (dossierMeta.get("is_cup") instanceof Boolean b) ? b : null;
        TournamentStageFormat stageFormat = TournamentStageFormat.resolve(stage + " " + tournName, isCup);
        float elimPressure = stageFormat.getEliminationPressure();
        float drawViability = stageFormat.getDrawViability();

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

        // ── Bottom-Up Squad Chemistry & International Fatigue ("Вирус сборных") ──
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem1 = squadChemistryEvaluator.evaluateSquadChemistry(t1Id, s1, matchTs);
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem2 = squadChemistryEvaluator.evaluateSquadChemistry(t2Id, s2, matchTs);

        // ── 1. Regional / Macro Baseline Card Propensity ──
        float macroRegionalBias = (float) parseDouble(tournSnap.get("regional_card_bias"), 0.50);

        // ── 2. Rivalry & High-Friction Encounter (Strict ID / Dossier Flag Only) ──
        boolean isDerby = Boolean.TRUE.equals(dossierMeta.get("is_derby"));
        float derbyFriction = isDerby ? 1.0f : 0.0f;

        // ── 3. Referee Strictness Index (Normalized Card Rate) ──
        Float refAvgYc = (dossierMeta.get("referee_avg_yellow_cards") instanceof Number n) ? n.floatValue() : null;
        String refName = (String) dossierMeta.get("referee_name");
        if ((refAvgYc == null || refAvgYc <= 0) && refName != null && !refName.isBlank()) {
            Map<String, Object> refStats = clickHouse.getRefereeDiscipline(sport.getCode(), refName);
            if (!refStats.isEmpty() && refStats.get("avg_yellow_cards") instanceof Number n) {
                refAvgYc = n.floatValue();
                dossierMeta.put("referee_avg_yellow_cards", refAvgYc);
            }
        }
        String refStrictness = (String) dossierMeta.get("referee_strictness");
        float refereeStrictnessIndex = refAvgYc != null && refAvgYc > 0
                ? Math.min(1.0f, Math.max(0.0f, refAvgYc / 6.0f))
                : macroRegionalBias;

        // Pure observable card bias (referee or regional prior; ML learns non-linear interactions directly)
        float compositeCardBias = refereeStrictnessIndex;

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
                .tournamentId(tournId)
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
                .surface(surface)
                .squadChemistry(chem1)
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
                .tournamentId(tournId)
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
                .surface(surface)
                .squadChemistry(chem2)
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
        macro[258] = sport.isTeamSport() ? 1.0f : 0.0f; // home advantage bias (strictly 0.0f for duel sports!)

        // 3. 64-dim Micro Lineup & Tactical Friction Tensor (Polymorphic per Sport)
        float[] lineup;
        if (!sport.isTeamSport()) {
            lineup = extractTennisPhysicalAndRecoveryFeatures(ctx1, ctx2, v1, v2);
        } else if ("BASKETBALL".equalsIgnoreCase(sport.getCode()) || "BASKETBALL_48M".equalsIgnoreCase(sport.getCode())) {
            lineup = extractBasketballTacticalFeatures(ctx1, ctx2, s1, s2, b1, b2, v1, v2);
        } else {
            lineup = extractLineupFeatures(s1, s2, b1, b2, p1Agg, p2Agg, friction, compositeCardBias, chem1, chem2, v1, v2);
        }

        // 4. Build Strongly-Typed Composable Feature Blocks
        MacroFeatureBlock macroBlock = new MacroFeatureBlock(macro);

        float[] micro32 = new float[MicroLineupFeatureBlock.EXPECTED_DIMENSION];
        System.arraycopy(lineup, 0, micro32, 0, Math.min(lineup.length, 32));
        MicroLineupFeatureBlock lineupBlock = new MicroLineupFeatureBlock(micro32);

        float[] adv32 = new float[AdvancedTacticalFeatureBlock.EXPECTED_DIMENSION];
        if (lineup.length >= 64) {
            System.arraycopy(lineup, 32, adv32, 0, 32);
        } else {
            adv32 = AdvancedTacticalFeatureBlock.neutral().getValues();
        }
        AdvancedTacticalFeatureBlock advancedBlock = new AdvancedTacticalFeatureBlock(adv32);

        TacticalDnaFeatureBlock dnaBlock = qdrantClient != null
                ? qdrantClient.fetchTacticalDna(sport, t1Id, t1Name, t2Id, t2Name)
                : TacticalDnaFeatureBlock.neutral(sport);

        // 5. Assemble Self-Validating Polymorphic Tactical Tensor
        PolymorphicTacticalTensor.Builder tensorBuilder = PolymorphicTacticalTensor.builder()
                .sport(sport)
                .macroBlock(macroBlock)
                .lineupBlock(lineupBlock)
                .advancedBlock(advancedBlock)
                .dnaBlock(dnaBlock);

        if (sport.getExpectedModelDimension() == PurePhysicsFeatureBlock.EXPECTED_DIMENSION) {
            float[] pure64;
            if (!sport.isTeamSport()) {
                pure64 = buildDuelPhysicsVector(ctx1, ctx2, v1, v2, sport);
            } else {
                pure64 = buildTeamPurePhysicsVector(v1, v2, lineup, dnaBlock, t1Leverage, t2Leverage, compositeCardBias, isDerby);
            }
            tensorBuilder.purePhysicsBlock(new PurePhysicsFeatureBlock(pure64));
        }

        PolymorphicTacticalTensor polymorphicTensor = tensorBuilder.build();

        float[] fullTensor = polymorphicTensor.getDenseVector();
        int totalDim = polymorphicTensor.getTotalDimension();

        log.debug("[Tactical Tensor Engine] Built {}-dim validated polymorphic tensor for match {} ({} vs {} | {}): V1_WR={}, V2_WR={}, ∇GD=[{}/{}], Lev=[{}/{}], GroupOfDeath={}, Lineups={}, RefCards={}, Derby={}, CompositeCardBias={}, Chem1Tenure={}, Chem2Tenure={}, FatiguedH={}, FatiguedA={}",
                totalDim, matchId, t1Name, t2Name, sport.getCode(),
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
                String.format(Locale.ROOT, "%.2f", compositeCardBias),
                chem1 != null ? String.format(Locale.ROOT, "%.2f", chem1.squadTenureScore()) : "N/A",
                chem2 != null ? String.format(Locale.ROOT, "%.2f", chem2.squadTenureScore()) : "N/A",
                chem1 != null ? chem1.nationalFatiguedCount() : 0,
                chem2 != null ? chem2.nationalFatiguedCount() : 0);

        return TensorResult.builder()
                .polymorphicTensor(polymorphicTensor)
                .features(fullTensor)
                .dimension(totalDim)
                .sport(sport)
                .team1WinRate(v1[0])
                .team2WinRate(v2[0])
                .formDifferential(v1[0] - v2[0])
                .hasLineups(hasLineups)
                .team1LineAggression(p1Agg)
                .team2LineAggression(p2Agg)
                .team1SquadChemistry(chem1)
                .team2SquadChemistry(chem2)
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

    private float[] buildDuelPhysicsVector(TacticalVectorContext ctx1, TacticalVectorContext ctx2,
                                           float[] v1, float[] v2, Sport sport) {
        float[] p64 = new float[PurePhysicsFeatureBlock.EXPECTED_DIMENSION];

        // ── Block A (0..23): Player 1 Physical & Skill Dynamics ──
        p64[0]  = v1.length > 19 ? v1[19] : 0.0f;               // Normalized MMR: (mmr - 1500) / 500
        p64[1]  = v1.length > 18 ? v1[18] : 0.50f;              // Reliability / RD proxy
        p64[2]  = v1.length > 45 ? v1[45] : 0.05f;              // Peak Intrinsic Stature
        p64[3]  = v1.length > 46 ? v1[46] : 0.05f;              // Current Stature
        p64[4]  = v1.length > 47 ? v1[47] : 0.0f;               // Injury Deflation Spread
        p64[5]  = v1.length > 63 ? v1[63] : 0.50f;              // Overall Form Win Rate
        p64[6]  = v1.length > 37 ? v1[37] : 0.65f;              // Modeled Service Hold %
        p64[7]  = v1.length > 38 ? v1[38] : 0.20f;              // Modeled Return Break %
        p64[8]  = v1.length > 50 ? v1[50] : 0.50f;              // Dominance Ratio Sigmoid
        p64[9]  = v1.length > 30 ? v1[30] : 0.62f;              // First Serve In %
        p64[10] = v1.length > 33 ? v1[33] : 0.25f;              // Aces Per Match (norm)
        p64[11] = v1.length > 34 ? v1[34] : 0.20f;              // Double Faults Per Match (norm)
        p64[12] = v1.length > 35 ? v1[35] : 0.60f;              // Break Points Saved %
        p64[13] = v1.length > 36 ? v1[36] : 0.40f;              // Break Points Converted %
        p64[14] = v1.length > 39 ? v1[39] : 0.50f;              // Hard Court Win Rate
        p64[15] = v1.length > 40 ? v1[40] : 0.50f;              // Clay Court Win Rate
        p64[16] = v1.length > 48 ? v1[48] : 0.10f;              // Layoff Days Factor
        p64[17] = v1.length > 49 ? v1[49] : 0.85f;              // Comeback Cadence
        p64[18] = v1.length > 57 ? v1[57] : 0.0f;               // Rust Penalty
        p64[19] = v1.length > 58 ? v1[58] : 0.95f;              // Serve Integrity
        p64[20] = v1.length > 59 ? v1[59] : 0.50f;              // Clutch / Tiebreak Index
        p64[21] = v1.length > 0 ? v1[0] : 0.50f;                // Short-Term Momentum (W/L)
        p64[22] = v1.length > 1 ? v1[1] : 0.0f;                 // Goal/Set Margin Drift
        p64[23] = v1.length > 2 ? v1[2] : 0.50f;                // Opponent Quality Average

        // ── Block B (24..47): Player 2 Physical & Skill Dynamics (Strict Mirror) ──
        p64[24] = v2.length > 19 ? v2[19] : 0.0f;
        p64[25] = v2.length > 18 ? v2[18] : 0.50f;
        p64[26] = v2.length > 45 ? v2[45] : 0.05f;
        p64[27] = v2.length > 46 ? v2[46] : 0.05f;
        p64[28] = v2.length > 47 ? v2[47] : 0.0f;
        p64[29] = v2.length > 63 ? v2[63] : 0.50f;
        p64[30] = v2.length > 37 ? v2[37] : 0.65f;
        p64[31] = v2.length > 38 ? v2[38] : 0.20f;
        p64[32] = v2.length > 50 ? v2[50] : 0.50f;
        p64[33] = v2.length > 30 ? v2[30] : 0.62f;
        p64[34] = v2.length > 33 ? v2[33] : 0.25f;
        p64[35] = v2.length > 34 ? v2[34] : 0.20f;
        p64[36] = v2.length > 35 ? v2[35] : 0.60f;
        p64[37] = v2.length > 36 ? v2[36] : 0.40f;
        p64[38] = v2.length > 39 ? v2[39] : 0.50f;
        p64[39] = v2.length > 40 ? v2[40] : 0.50f;
        p64[40] = v2.length > 48 ? v2[48] : 0.10f;
        p64[41] = v2.length > 49 ? v2[49] : 0.85f;
        p64[42] = v2.length > 57 ? v2[57] : 0.0f;
        p64[43] = v2.length > 58 ? v2[58] : 0.95f;
        p64[44] = v2.length > 59 ? v2[59] : 0.50f;
        p64[45] = v2.length > 0 ? v2[0] : 0.50f;
        p64[46] = v2.length > 1 ? v2[1] : 0.0f;
        p64[47] = v2.length > 2 ? v2[2] : 0.50f;

        // ── Block C (48..63): Pairwise Duel Physics & Symmetric Differentials ──
        p64[48] = p64[0] - p64[24];                              // Normalized MMR Diff
        p64[49] = 1.0f / (1.0f + (float) Math.exp(-4.0 * (p64[0] - p64[24]))); // Physical Symmetric Expected Win Prob (P1)
        p64[50] = (p64[1] + p64[25]) * 0.5f;                    // Pair Confidence (Symmetric RD mean)
        p64[51] = p64[2] - p64[26];                              // Peak Stature Diff
        p64[52] = p64[6] - p64[30];                              // Service Hold % Diff
        p64[53] = p64[7] - p64[31];                              // Return Break % Diff
        p64[54] = p64[8] - p64[32];                              // Dominance Ratio Diff
        p64[55] = p64[10] - p64[34];                             // Aces Per Match Diff
        p64[56] = p64[11] - p64[35];                             // Double Faults Diff
        p64[57] = p64[5] - p64[29];                              // Overall Win Rate Diff
        p64[58] = (p64[17] - p64[18]) - (p64[41] - p64[42]);     // Net Recovery / Rust Advantage
        p64[59] = 0.50f;                                         // H2H Win Rate neutral anchor (0.50)

        // Tournament Tier (0.14..1.0, neutral anchor 0.50 for UNKNOWN)
        float tierNorm = 0.50f;
        if (tierClassifier != null) {
            TournamentTierClassifier.TierInfo ti = tierClassifier.classify(ctx1.getTournamentId(), ctx1.getTournamentName(), sport.getCode());
            tierNorm = ti.tierLevel() > 0 ? (8 - ti.tierLevel()) / 7.0f : 0.50f;
        }
        p64[60] = tierNorm;

        // Surface Encoding (Hard: 0.0, Clay: 0.33, Grass: 0.66, Indoors: 1.0)
        float surfCode = 0.0f;
        if (ctx1.getSurface() != null) {
            switch (ctx1.getSurface()) {
                case CLAY -> surfCode = 0.33f;
                case GRASS -> surfCode = 0.66f;
                case INDOORS -> surfCode = 1.0f;
                default -> surfCode = 0.0f;
            }
        }
        p64[61] = surfCode;

        // Total Sets / Pace Context
        p64[62] = (ctx1.getPaceMultiplier() != null) ? ctx1.getPaceMultiplier().floatValue() : 1.0f;

        // STRICT CONSTITUTIONAL ANCHOR: Zero Neutral Bias (Positional Symmetry Anchor)
        p64[63] = 0.0f;

        return p64;
    }

    private float[] buildTeamPurePhysicsVector(
            float[] v1, float[] v2, float[] lineup, TacticalDnaFeatureBlock dnaBlock,
            double t1Leverage, double t2Leverage, float compositeCardBias, boolean isDerby) {

        float[] pure64 = new float[PurePhysicsFeatureBlock.EXPECTED_DIMENSION];

        // Block 1 (0..7): Glicko MMR & Class
        pure64[0] = v1.length > 14 ? v1[14] : 0.0f;
        pure64[1] = v2.length > 14 ? v2[14] : 0.0f;
        pure64[2] = pure64[0] - pure64[1];
        pure64[3] = v1.length > 15 ? v1[15] : 0.0f;
        pure64[4] = v2.length > 15 ? v2[15] : 0.0f;
        pure64[5] = v1.length > 52 ? v1[52] : 0.5f;
        pure64[6] = v2.length > 52 ? v2[52] : 0.5f;
        pure64[7] = (v1.length > 28 && v2.length > 28) ? (v1[28] - v2[28]) : 0.0f; // Net Lineup Quality Retention Advantage

        // Block 2 (8..31): Form & Scoring Physics
        pure64[8]  = v1.length > 0 ? v1[0] : 0.0f;
        pure64[9]  = v1.length > 1 ? v1[1] : 0.0f;
        pure64[10] = v1.length > 2 ? v1[2] : 0.0f;
        pure64[11] = v1.length > 3 ? v1[3] : 0.0f;
        pure64[12] = v1.length > 4 ? v1[4] : 0.0f;
        pure64[13] = v1.length > 5 ? v1[5] : 0.0f;
        pure64[14] = v1.length > 30 ? v1[30] : 0.34f;
        pure64[15] = v1.length > 31 ? v1[31] : 0.34f;
        pure64[16] = v1.length > 8 ? v1[8] : 0.28f;
        pure64[17] = v1.length > 10 ? v1[10] : 0.24f;
        pure64[18] = v1.length > 35 ? v1[35] : 0.75f;
        pure64[19] = v1.length > 47 ? v1[47] : 0.50f;

        pure64[20] = v2.length > 0 ? v2[0] : 0.0f;
        pure64[21] = v2.length > 1 ? v2[1] : 0.0f;
        pure64[22] = v2.length > 2 ? v2[2] : 0.0f;
        pure64[23] = v2.length > 3 ? v2[3] : 0.0f;
        pure64[24] = v2.length > 4 ? v2[4] : 0.0f;
        pure64[25] = v2.length > 5 ? v2[5] : 0.0f;
        pure64[26] = v2.length > 30 ? v2[30] : 0.34f;
        pure64[27] = v2.length > 31 ? v2[31] : 0.34f;
        pure64[28] = v2.length > 8 ? v2[8] : 0.28f;
        pure64[29] = v2.length > 10 ? v2[10] : 0.24f;
        pure64[30] = v2.length > 35 ? v2[35] : 0.75f;
        pure64[31] = v2.length > 47 ? v2[47] : 0.50f;

        // Block 3 (32..47): Rest, Fatigue, Chemistry & Lineups
        pure64[32] = v1.length > 7 ? v1[7] : 0.50f;
        pure64[33] = v2.length > 7 ? v2[7] : 0.50f;
        pure64[34] = pure64[32] - pure64[33];
        pure64[35] = v1.length > 59 ? v1[59] : 0.15f;
        pure64[36] = v2.length > 59 ? v2[59] : 0.15f;
        pure64[37] = lineup != null && lineup.length > 1 ? lineup[1] / 10.0f : 0.68f;
        pure64[38] = lineup != null && lineup.length > 13 ? lineup[13] / 10.0f : 0.68f;
        pure64[39] = pure64[37] - pure64[38];
        pure64[40] = lineup != null && lineup.length > 4 ? lineup[4] : 0.0f;
        pure64[41] = lineup != null && lineup.length > 16 ? lineup[16] : 0.0f;
        pure64[42] = v1.length > 60 ? v1[60] : 0.68f;
        pure64[43] = v2.length > 60 ? v2[60] : 0.68f;
        pure64[44] = v1.length > 61 ? v1[61] : 0.68f;
        pure64[45] = v2.length > 61 ? v2[61] : 0.68f;
        pure64[46] = v1.length > 54 ? v1[54] : 0.50f;
        pure64[47] = v2.length > 54 ? v2[54] : 0.50f;

        // Block 4 (48..63): Style Clash, Leverage, Substitutions & Friction
        // Synced indices with FootballVectorExtractor:
        // vec[36] = avg_def_line, vec[37] = avg_possession, vec[35] = pressing, vec[40] = avg_aerial_won
        pure64[48] = v1.length > 36 ? v1[36] : 0.52f; // Defensive line height (Home)
        pure64[49] = v2.length > 36 ? v2[36] : 0.52f; // Defensive line height (Away)
        pure64[50] = v1.length > 37 ? v1[37] : 0.50f; // Possession % (Home)
        pure64[51] = v2.length > 37 ? v2[37] : 0.50f; // Possession % (Away)
        pure64[52] = v1.length > 35 ? v1[35] : 0.30f; // Pressing intensity (Home)
        pure64[53] = v2.length > 35 ? v2[35] : 0.30f; // Pressing intensity (Away)
        pure64[54] = v1.length > 40 ? v1[40] : 0.50f; // Aerial duels won % (Home)
        pure64[55] = v2.length > 40 ? v2[40] : 0.50f; // Aerial duels won % (Away)
        pure64[56] = pure64[50] - pure64[51];          // Net possession control differential
        pure64[57] = compositeCardBias;
        pure64[58] = isDerby ? 1.0f : 0.0f;
        pure64[59] = (v1.length > 29 && v2.length > 29) ? (v1[29] - v2[29]) : 0.0f; // Net Bench Firepower Advantage
        pure64[60] = dnaBlock != null ? dnaBlock.getCosineDistance() : 1.0f;
        pure64[61] = (v1.length > 25 && v2.length > 25) ? (v1[25] - v2[25]) : 0.0f; // Net Missing Alpha Attacker Deficit
        pure64[62] = (float) (t1Leverage - t2Leverage);
        pure64[63] = 0.0f;                             // Constitutional Neutral Positional Anchor (Rule 1.1)

        return pure64;
    }

    private float[] createNeutralLineupFeatures(float[] v1, float[] v2, float compositeCardBias) {
        float[] lf = new float[64];
        // Lineup confirmed flags default to 0.0f
        lf[0] = 0.0f;
        lf[1] = 6.8f;
        lf[6] = 6.8f;
        lf[12] = 0.0f;
        lf[13] = 6.8f;
        lf[18] = 6.8f;

        lf[31] = compositeCardBias;

        if (v1 != null && v2 != null) {
            float hAge = v1.length > 48 ? v1[48] : 0.50f;
            float aAge = v2.length > 48 ? v2[48] : 0.50f;
            lf[32] = hAge;
            lf[33] = aAge;
            lf[34] = hAge;
            lf[35] = aAge;
            lf[38] = 0.40f; // (6.8 - 6.0) / 2.0
            lf[39] = 0.40f;
            lf[40] = 0.40f;
            lf[41] = 0.40f;
            lf[42] = 0.80f; // 4/5
            lf[43] = 0.80f;
            lf[44] = v1.length > 14 ? v1[14] : 0.28f;
            lf[45] = v2.length > 14 ? v2[14] : 0.28f;
            lf[46] = v1.length > 26 ? (v1[24] + v1[25] + v1[26]) * 0.5f : 0.25f;
            lf[47] = v2.length > 26 ? (v2[24] + v2[25] + v2[26]) * 0.5f : 0.25f;
            lf[48] = v1.length > 60 ? v1[60] : 0.68f;
            lf[49] = v2.length > 60 ? v2[60] : 0.68f;
            lf[50] = 1.0f;
            lf[51] = 1.0f;
            lf[52] = v1.length > 23 ? v1[23] : 0.21f;
            lf[53] = v2.length > 23 ? v2[23] : 0.21f;
            lf[57] = v1.length > 44 ? v1[44] : 0.50f;
            lf[58] = v2.length > 44 ? v2[44] : 0.50f;
        }
        return lf;
    }

    private float[] extractLineupFeatures(List<Map<String, Object>> s1, List<Map<String, Object>> s2,
                                          List<Map<String, Object>> b1, List<Map<String, Object>> b2,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg,
                                          PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg,
                                          PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction,
                                          float compositeCardBias,
                                          SquadChemistryEvaluator.SquadChemistryAndFatigue chem1,
                                          SquadChemistryEvaluator.SquadChemistryAndFatigue chem2,
                                          float[] v1, float[] v2) {
        float[] lf = createNeutralLineupFeatures(v1, v2, compositeCardBias);

        if (s1 == null || s1.isEmpty() || s2 == null || s2.isEmpty()) {
            return lf;
        }

        lf[0] = 1.0f;
        lf[1] = calculateAvgRating(s1) * (chem1 != null ? chem1.staminaDiscount() : 1.0f);
        lf[2] = calculateMinRating(s1);
        lf[3] = calculateMaxRating(s1);
        lf[4] = p1Agg != null ? p1Agg.totalInjuryRustDecay() : 0.0f;
        lf[5] = 0.0f;
        lf[6] = calculateGkRating(s1);

        // Team 1 Positional Line Aggression
        if (p1Agg != null) {
            lf[7] = p1Agg.defAggression();
            lf[8] = p1Agg.midAggression();
            lf[9] = p1Agg.attAggression();
            lf[10] = p1Agg.teamTotalAggression();
            lf[11] = p1Agg.attFoulsDrawn();
        }

        lf[12] = 1.0f;
        lf[13] = calculateAvgRating(s2) * (chem2 != null ? chem2.staminaDiscount() : 1.0f);
        lf[14] = calculateMinRating(s2);
        lf[15] = calculateMaxRating(s2);
        lf[16] = p2Agg != null ? p2Agg.totalInjuryRustDecay() : 0.0f;
        lf[17] = 0.0f;
        lf[18] = calculateGkRating(s2);

        // Team 2 Positional Line Aggression
        if (p2Agg != null) {
            lf[19] = p2Agg.defAggression();
            lf[20] = p2Agg.midAggression();
            lf[21] = p2Agg.attAggression();
            lf[22] = p2Agg.teamTotalAggression();
            lf[23] = p2Agg.attFoulsDrawn();
        }

        if (b1 != null && !b1.isEmpty()) {
            lf[24] = calculateBenchDepth(b1);
        }
        if (b2 != null && !b2.isEmpty()) {
            lf[25] = calculateBenchDepth(b2);
        }

        // Mutual Matchup Friction Dynamics (Clashes & Friction)
        if (friction != null) {
            lf[26] = friction.midfieldClash();
            lf[27] = friction.attack1VsDefense2();
            lf[28] = friction.attack2VsDefense1();
            lf[29] = friction.totalMatchAggression();
            lf[30] = friction.aggressionDifferential();
        }
        lf[31] = compositeCardBias;

        // ════════════════════════════════════════════════════════════════════════════
        // 32 ADVANCED MICRO TACTICAL EXPANSIONS (Dims 32..63)
        // ════════════════════════════════════════════════════════════════════════════
        // 1. Positional Age & Speed Asymmetry (Dims 32..37)
        float hDefAge = calculatePositionalAge(s1, "DEF", v1 != null ? v1[48] : 0.38f);
        float aDefAge = calculatePositionalAge(s2, "DEF", v2 != null ? v2[48] : 0.38f);
        float hAttAge = calculatePositionalAge(s1, "ATT", v1 != null ? v1[48] : 0.38f);
        float aAttAge = calculatePositionalAge(s2, "ATT", v2 != null ? v2[48] : 0.38f);
        lf[32] = hDefAge;
        lf[33] = aDefAge;
        lf[34] = hAttAge;
        lf[35] = aAttAge;
        lf[36] = hAttAge - aDefAge; // Pace advantage of Home Attack vs Away Defense
        lf[37] = aAttAge - hDefAge; // Pace advantage of Away Attack vs Home Defense

        // 2. Isolated Positional Line Ratings & Density (Dims 38..43)
        float hDefR = calculatePositionalRating(s1, "DEF", 6.8f);
        float aDefR = calculatePositionalRating(s2, "DEF", 6.8f);
        float hAttR = calculatePositionalRating(s1, "ATT", 6.8f);
        float aAttR = calculatePositionalRating(s2, "ATT", 6.8f);
        lf[38] = (hDefR - 6.0f) / 2.0f;
        lf[39] = (aDefR - 6.0f) / 2.0f;
        lf[40] = (hAttR - 6.0f) / 2.0f;
        lf[41] = (aAttR - 6.0f) / 2.0f;
        lf[42] = countDefenders(s1) / 5.0f; // 3 CBs vs 4 vs 5 backline density
        lf[43] = countDefenders(s2) / 5.0f;

        // 3. Goalkeeper Shot-Stopping & 1st Half Low-Block Resilience (Dims 44..49)
        lf[44] = v1 != null ? v1[14] : 0.28f; // 1st half clean sheet propensity
        lf[45] = v2 != null ? v2[14] : 0.28f;
        lf[46] = v1 != null ? (v1[24] + v1[25] + v1[26]) * 0.5f : 0.25f; // 1st half goals conceded proxy
        lf[47] = v2 != null ? (v2[24] + v2[25] + v2[26]) * 0.5f : 0.25f;
        lf[48] = v1 != null ? v1[60] : 0.68f; // GK shot-stopping rating
        lf[49] = v2 != null ? v2[60] : 0.68f;

        // 4. Fatigue & 2nd Half Scoring Propensity (Dims 50..53)
        lf[50] = v1 != null ? (v1[21] + v1[22] + v1[23]) : 0.50f; // Home 2nd half scoring rate
        lf[51] = v2 != null ? (v2[21] + v2[22] + v2[23]) : 0.50f; // Away 2nd half scoring rate
        lf[52] = Math.min(1.0f, (v1 != null ? v1[23] : 0.21f) + (chem1 != null ? chem1.lateDefenseLeakBonus() : 0.0f)); // Home late defense leak (75-90 min conceded + fatigue bonus)
        lf[53] = Math.min(1.0f, (v2 != null ? v2[23] : 0.21f) + (chem2 != null ? chem2.lateDefenseLeakBonus() : 0.0f)); // Away late defense leak (75-90 min conceded + fatigue bonus)

        // 5. Bench Attack Depth & Substitution Impact (Dims 54..56)
        lf[54] = lf[24]; // home bench depth
        lf[55] = lf[25]; // away bench depth
        lf[56] = lf[24] - lf[25]; // bench impact advantage

        // 6. Aerial Dominance & Set-Piece Mismatch (Dims 57..59)
        float hAer = v1 != null ? v1[44] : 0.50f;
        float aAer = v2 != null ? v2[44] : 0.50f;
        lf[57] = hAer;
        lf[58] = aAer;
        lf[59] = hAer - aAer; // Set-piece aerial superiority

        // 7. Tactical Clash Dynamics (Dims 60..63)
        lf[60] = lf[40] - lf[39]; // Home Attack vs Away Defense
        lf[61] = lf[41] - lf[38]; // Away Attack vs Home Defense
        lf[62] = lf[38] - lf[41]; // Home Defense vs Away Attack
        lf[63] = lf[39] - lf[40]; // Away Defense vs Home Attack

        return lf;
    }

    /**
     * Constructs the 64-dimensional micro physical integrity and recovery tensor for Tennis.
     * Incorporates surface affinity splits, serve/return clash, court fatigue, and clutch dynamics.
     */
    private float[] extractTennisPhysicalAndRecoveryFeatures(TacticalVectorContext ctx1,
                                                            TacticalVectorContext ctx2,
                                                            float[] v1,
                                                            float[] v2) {
        float[] lf = new float[64];

        // ── Player 1 Micro Physical & Recovery Profile (Dims 0..11) ─────────────
        lf[0] = 1.0f;                                              // is_confirmed_entry
        lf[1] = v1[45];                                            // pure peak latent class stature
        lf[2] = v1[17];                                            // baseline stamina rating
        lf[3] = v1[45];                                            // short set closing power (latent class)
        lf[4] = v1[57];                                            // total injury rust
        lf[5] = v1[56];                                            // comeback match cadence
        lf[6] = v1[58];                                            // serve health ratio
        lf[7] = v1[34];                                            // double fault hazard
        lf[8] = v1[35];                                            // break point clutch
        lf[9] = v1[57];                                            // unforced error / rust indicator
        lf[10] = v1[55];                                           // layoff days
        lf[11] = v1[46] > 0.25f ? 1.0f : 0.0f;                     // protected ranking deflated flag

        // ── Player 2 Micro Physical & Recovery Profile (Dims 12..23) ────────────
        lf[12] = 1.0f;                                             // is_confirmed_entry
        lf[13] = v2[45];                                           // pure peak latent class stature
        lf[14] = v2[17];                                           // baseline stamina rating
        lf[15] = v2[45];                                           // short set closing power (latent class)
        lf[16] = v2[57];                                           // total injury rust
        lf[17] = v2[56];                                           // comeback match cadence
        lf[18] = v2[58];                                           // serve health ratio
        lf[19] = v2[34];                                           // double fault hazard
        lf[20] = v2[35];                                           // break point clutch
        lf[21] = v2[57];                                           // unforced error / rust indicator
        lf[22] = v2[55];                                           // layoff days
        lf[23] = v2[46] > 0.25f ? 1.0f : 0.0f;                     // protected ranking deflated flag

        // ── Head-to-Head & Asymmetry Dynamics (Dims 24..31) ──────────────────────
        lf[24] = lf[1] - lf[13];                                   // diff_effective_rating (P1_eff - P2_eff)
        lf[25] = lf[3] - lf[15];                                   // diff_short_set_power
        lf[26] = lf[4] - lf[16];                                   // diff_injury_rust
        lf[27] = lf[6] - lf[18];                                   // diff_serve_integrity
        lf[28] = lf[2] - lf[14];                                   // diff_deciding_set_stamina

        boolean classDisparity = (Math.abs(v1[45] - v2[45]) >= 0.05f && Math.abs(v1[0] - v2[0]) <= 0.15f);
        lf[29] = classDisparity ? 1.0f : 0.0f;                     // class disparity mask
        lf[30] = v1[38];                                           // player 1 hold rate
        lf[31] = v2[38];                                           // player 2 hold rate

        // ── Advanced Tennis Micro Dynamics (Dims 32..63) ────────────────────────
        // Surface Affinity Splits (Dims 32..37)
        float sHold1 = v1[30]; // 1st serve win rate
        float sHold2 = v2[30];
        lf[32] = v1[0];        // career win rate on surface proxy
        lf[33] = v2[0];
        lf[34] = v1[0] - v2[0];
        lf[35] = sHold1;
        lf[36] = sHold2;
        lf[37] = sHold1 - sHold2;

        // Serve vs Return Clashing (Dims 38..43)
        float ret1 = v1[31];   // return points won proxy
        float ret2 = v2[31];
        lf[38] = sHold1;
        lf[39] = sHold2;
        lf[40] = ret1;
        lf[41] = ret2;
        lf[42] = sHold1 - ret2; // P1 Serve Hold vs P2 Return
        lf[43] = sHold2 - ret1; // P2 Serve Hold vs P1 Return

        // Ace-to-Double Fault & Service Dominance (Dims 44..45)
        lf[44] = v1[34]; // double fault hazard
        lf[45] = v2[34];

        // Court Hours & Cumulative Fatigue (Dims 46..50)
        lf[46] = v1[56]; // match cadence / court load
        lf[47] = v2[56];
        lf[48] = v1[56] - v2[56]; // court fatigue asymmetry
        lf[49] = v1[57]; // days rest proxy
        lf[50] = v2[57];

        // Tactical Grip & Lefty Mismatch (Dims 51..53)
        lf[51] = v1[48] > 0.5f ? 1.0f : 0.0f; // lefty proxy
        lf[52] = v2[48] > 0.5f ? 1.0f : 0.0f;
        lf[53] = Math.abs(lf[51] - lf[52]);   // style disparity

        // Break Point Clutch & Conversion (Dims 54..57)
        lf[54] = v1[35]; // BP clutch
        lf[55] = v2[35];
        lf[56] = v1[35] - v2[35];
        lf[57] = (v1[35] + v2[35]) * 0.5f;

        // Tie-Break Clutch & Deciding Set Endurance (Dims 58..63)
        lf[58] = v1[17]; // stamina in deciding sets
        lf[59] = v2[17];
        lf[60] = v1[17] - v2[17];
        lf[61] = lf[3];  // short set closing power
        lf[62] = lf[15];
        lf[63] = lf[3] - lf[15];

        return lf;
    }

    /**
     * Constructs the 64-dimensional micro tactical tensor for Basketball.
     * Incorporates Back-to-Back fatigue, pace clashing, rim protection, and 3P variance.
     */
    private float[] extractBasketballTacticalFeatures(TacticalVectorContext ctx1,
                                                      TacticalVectorContext ctx2,
                                                      List<Map<String, Object>> s1,
                                                      List<Map<String, Object>> s2,
                                                      List<Map<String, Object>> b1,
                                                      List<Map<String, Object>> b2,
                                                      float[] v1,
                                                      float[] v2) {
        float[] lf = new float[64];
        // Dims 0..31: Basketball base quarter execution & ratings
        lf[0] = v1 != null ? v1[0] : 0.50f;
        lf[1] = calculateAvgRating(s1);
        lf[2] = v1 != null ? v1[31] : 0.50f; // offensive rating
        lf[3] = v1 != null ? v1[32] : 0.50f; // defensive rating
        lf[6] = v1 != null ? v1[60] : 0.72f; // rim protector

        lf[12] = v2 != null ? v2[0] : 0.50f;
        lf[13] = calculateAvgRating(s2);
        lf[14] = v2 != null ? v2[31] : 0.50f;
        lf[15] = v2 != null ? v2[32] : 0.50f;
        lf[18] = v2 != null ? v2[60] : 0.72f;

        lf[24] = calculateBenchDepth(b1);
        lf[25] = calculateBenchDepth(b2);

        // Quarter ratios
        lf[26] = v1 != null ? v1[18] : 0.25f; // Q1
        lf[27] = v1 != null ? v1[21] : 0.25f; // Q4
        lf[28] = v2 != null ? v2[18] : 0.25f;
        lf[29] = v2 != null ? v2[21] : 0.25f;
        lf[30] = lf[2] - lf[15];              // Net offensive/defensive mismatch
        lf[31] = lf[14] - lf[3];

        // Dims 32..63: Advanced Basketball Analytics
        // Pace & Transition (Dims 32..35)
        float pace1 = v1 != null ? v1[30] : 0.50f;
        float pace2 = v2 != null ? v2[30] : 0.50f;
        lf[32] = pace1;
        lf[33] = pace2;
        lf[34] = pace1 - pace2;
        lf[35] = (pace1 + pace2) * 0.5f;

        // Rim Protection & Rebounds (Dims 36..41)
        float rim1 = v1 != null ? v1[60] : 0.72f;
        float rim2 = v2 != null ? v2[60] : 0.72f;
        float oReb1 = v1 != null ? v1[37] : 0.28f;
        float oReb2 = v2 != null ? v2[37] : 0.28f;
        lf[36] = rim1;
        lf[37] = rim2;
        lf[38] = oReb1;
        lf[39] = oReb2;
        lf[40] = oReb1 - oReb2;
        lf[41] = rim1 - rim2;

        // 3-Point Volatility & Paint Scoring (Dims 42..47)
        float three1 = v1 != null ? v1[39] : 0.38f;
        float three2 = v2 != null ? v2[39] : 0.38f;
        float paint1 = v1 != null ? v1[40] : 0.45f;
        float paint2 = v2 != null ? v2[40] : 0.45f;
        lf[42] = three1;
        lf[43] = three2;
        lf[44] = Math.abs(three1 - three2);
        lf[45] = paint1;
        lf[46] = paint2;
        lf[47] = paint1 - paint2;

        // Bench Unit & Depth Impact (Dims 48..51)
        lf[48] = lf[24];
        lf[49] = lf[25];
        lf[50] = lf[24] - lf[25];
        lf[51] = (lf[24] + lf[25]) * 0.5f;

        // Clutch Execution & Free Throw Precision (Dims 52..55)
        float ft1 = v1 != null ? v1[38] : 0.76f;
        float ft2 = v2 != null ? v2[38] : 0.76f;
        float clutch1 = v1 != null ? v1[17] : 0.28f;
        float clutch2 = v2 != null ? v2[17] : 0.28f;
        lf[52] = ft1;
        lf[53] = ft2;
        lf[54] = clutch1;
        lf[55] = clutch2;

        // Age & Physical Stamina (Dims 56..59)
        lf[56] = v1 != null ? v1[48] : 0.38f;
        lf[57] = v2 != null ? v2[48] : 0.38f;
        lf[58] = lf[56] - lf[57];
        lf[59] = (lf[27] - lf[29]); // Q4 scoring disparity

        // Cross-Line Matchups (Dims 60..63)
        lf[60] = paint1 - rim2; // Team 1 Paint Scoring vs Team 2 Rim Protector
        lf[61] = paint2 - rim1; // Team 2 Paint Scoring vs Team 1 Rim Protector
        lf[62] = lf[2] - lf[15]; // Team 1 Offense vs Team 2 Defense
        lf[63] = lf[14] - lf[3]; // Team 2 Offense vs Team 1 Defense

        return lf;
    }

    private float calculatePositionalAge(List<Map<String, Object>> starters, String posGroup, float fallback) {
        if (starters == null || starters.isEmpty()) return fallback;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            boolean match = switch (posGroup) {
                case "DEF" -> pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF");
                case "ATT" -> pos.contains("F") || pos.contains("FW") || pos.contains("ST") || pos.contains("RW") || pos.contains("LW") || pos.contains("ATT");
                default -> false;
            };
            if (match) {
                double age = parseDouble(p.get("age"), 26.0);
                sum += age;
                count++;
            }
        }
        if (count == 0) return fallback;
        double avgAge = sum / count;
        return (float) Math.max(0.0, Math.min(1.0, (avgAge - 18.0) / 22.0));
    }

    private float calculatePositionalRating(List<Map<String, Object>> starters, String posGroup, float fallback) {
        if (starters == null || starters.isEmpty()) return fallback;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            boolean match = switch (posGroup) {
                case "DEF" -> pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF");
                case "ATT" -> pos.contains("F") || pos.contains("FW") || pos.contains("ST") || pos.contains("RW") || pos.contains("LW") || pos.contains("ATT");
                default -> false;
            };
            if (match) {
                sum += parseRating(p.get("rating"), fallback);
                count++;
            }
        }
        return count > 0 ? (float) (sum / count) : fallback;
    }

    private int countDefenders(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 4;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            if (pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF")) {
                count++;
            }
        }
        return count > 0 ? count : 4;
    }

    private float calculateAvgRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 6.8f;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            sum += r;
            count++;
        }
        return count > 0 ? (float) (sum / count) : 6.8f;
    }

    private float calculateMinRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 6.2f;
        double min = 10.0;
        boolean found = false;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            if (r < min) {
                min = r;
                found = true;
            }
        }
        return found ? (float) min : 6.2f;
    }

    private float calculateMaxRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 7.5f;
        double max = 0.0;
        boolean found = false;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            if (r > max) {
                max = r;
                found = true;
            }
        }
        return found ? (float) max : 7.5f;
    }

    private float calculateGkRating(List<Map<String, Object>> starters) {
        if (starters == null) return 6.8f;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();
            if (pos.contains("GK") || pos.contains("GOAL") || pos.contains("ВР") || "G".equals(pos)) {
                return (float) parsePlayerRatingRobust(p, 6.8);
            }
        }
        return 6.8f;
    }

    private float calculateBenchDepth(List<Map<String, Object>> bench) {
        if (bench == null || bench.isEmpty()) return 0.0f;
        int jokers = 0;
        double sumQuality = 0.0;
        for (Map<String, Object> p : bench) {
            double r = parsePlayerRatingRobust(p, 6.5);
            Long pid = parseLong(p.get("sofascore_id"));
            if (pid == null) pid = parseLong(p.get("id"));
            if (pid != null && pid > 0 && clickHouse != null) {
                var pMmr = clickHouse.getSportMmr(pid, "football-player", "ALL");
                if (pMmr.isPresent()) {
                    double mmrWeight = Math.max(0.70, Math.min(1.40, 1.0 + (pMmr.get().mmr() - 1500.0) / 2800.0));
                    r = r * mmrWeight;
                }
            }
            if (r >= 7.0) jokers++;
            sumQuality += r;
        }
        float jokerBonus = Math.min(0.50f, jokers * 0.17f);
        float avgBenchRatio = (float) Math.min(0.50f, ((sumQuality / bench.size()) - 6.0) / 3.0);
        return Math.min(1.0f, Math.max(0.0f, jokerBonus + avgBenchRatio));
    }

    private double parsePlayerRatingRobust(Map<String, Object> p, double fallback) {
        if (p == null) return fallback;
        Object r = p.get("effective_rating");
        if (r == null) r = p.get("rating");
        if (r == null) r = p.get("match_rating");
        return parseRating(r, fallback);
    }

    private double parseRating(Object obj, double fallback) {
        double val = parseDouble(obj, fallback);
        return (val >= 2.0 && val <= 10.0) ? val : fallback;
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
