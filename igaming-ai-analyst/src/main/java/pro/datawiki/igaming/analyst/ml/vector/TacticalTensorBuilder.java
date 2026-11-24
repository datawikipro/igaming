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
 * Orchestrates the polymorphic multi-task tactical tensor (64-dim Pure Physics and composable feature blocks).
 * Built with 100% Java domain models and zero procedural code.
 * Implements bottom-up player line aggression and matchup friction.
 * Enriched with Point-in-Time tournament motivation and leverage.
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
     * Builds the complete neural input tensor from match dossier metadata.
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
        String stageStr = (stage != null && !stage.isBlank() && !"null".equalsIgnoreCase(stage.trim())) ? stage.trim() : "";
        String fullStageInput = stageStr.isEmpty() ? tournName : (stageStr + " " + tournName);
        TournamentStageFormat stageFormat = TournamentStageFormat.resolve(fullStageInput, isCup);
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

        // 2. Macro Tensor (v1, v2, diff, prod, ctx) - 100% Polymorphic via Extractor
        float[] ctx = extractor.extractMacroContext(v1, v2);
        float[] macro = new float[256 + ctx.length];
        System.arraycopy(v1, 0, macro, 0, 64);
        System.arraycopy(v2, 0, macro, 64, 64);

        for (int i = 0; i < 64; i++) {
            macro[128 + i] = v1[i] - v2[i];        // diff (includes blowout and leverage asymmetry!)
            macro[192 + i] = v1[i] * v2[i];        // prod (friction & clash interaction!)
        }
        System.arraycopy(ctx, 0, macro, 256, ctx.length);

        // 3. DNA Feature Block (Qdrant Point-in-Time embeddings)
        TacticalDnaFeatureBlock dnaBlock = qdrantClient != null
                ? qdrantClient.fetchTacticalDna(sport, t1Id, t1Name, t2Id, t2Name)
                : TacticalDnaFeatureBlock.neutral(sport);

        // 4. 64-dim Micro Lineup & Tactical Friction Tensor (100% Polymorphic via Extractor)
        TacticalMatchContext matchCtx = new TacticalMatchContext(
                sport, ctx1, ctx2, v1, v2, s1, s2, b1, b2, p1Agg, p2Agg, friction,
                chem1, chem2, dnaBlock, compositeCardBias, isDerby, t1Leverage, t2Leverage, tierClassifier
        );
        float[] lineup = extractor.extractMicroFeatures(matchCtx);

        // 5. Build Strongly-Typed Composable Feature Blocks
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

        // 6. Assemble Self-Validating Polymorphic Tactical Tensor
        PolymorphicTacticalTensor.Builder tensorBuilder = PolymorphicTacticalTensor.builder()
                .sport(sport)
                .macroBlock(macroBlock)
                .lineupBlock(lineupBlock)
                .advancedBlock(advancedBlock)
                .dnaBlock(dnaBlock);

        if (sport.getExpectedModelDimension() == PurePhysicsFeatureBlock.EXPECTED_DIMENSION) {
            float[] pure64 = extractor.extractPurePhysicsVector(matchCtx, lineup);
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
