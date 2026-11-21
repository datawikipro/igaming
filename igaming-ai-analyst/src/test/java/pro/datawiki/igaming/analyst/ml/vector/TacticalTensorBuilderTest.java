package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class TacticalTensorBuilderTest {

    @Test
    void testTacticalTensorBuilderWithRefereeAndDerby() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) extractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        when(aggressionEvaluator.evaluateLineAggression(any()))
                .thenReturn(new PlayerLineAggressionEvaluator.LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f, 0.0f));
        when(aggressionEvaluator.evaluateMatchup(any(), any()))
                .thenReturn(new PlayerLineAggressionEvaluator.MatchupFrictionDynamics(0.22f, 0.16f, 0.16f, 0.45f, 0.0f, 0.43f));

        SquadChemistryEvaluator chemistryEvaluator = new SquadChemistryEvaluator(clickHouse);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_name", "Helmond Sport");
        dossier.put("team2_name", "FC Eindhoven");
        dossier.put("tournament_name", "Eerste Divisie");
        dossier.put("country_name", "Netherlands");
        dossier.put("referee_name", "Stan Teuben");
        dossier.put("referee_games", 10);
        dossier.put("referee_yellow_cards", 55);
        dossier.put("referee_avg_yellow_cards", 5.5f);
        dossier.put("referee_strictness", "VERY_STRICT");
        dossier.put("is_derby", true);

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(12345L, dossier);

        assertThat(result).isNotNull();
        assertThat(result.isDerby()).isTrue();
        assertThat(result.getRefereeAvgYellowCards()).isEqualTo(5.5f);
        assertThat(result.getRefereeStrictnessTier()).isEqualTo("VERY_STRICT");
        assertThat(result.getCompositeCardBias()).isGreaterThanOrEqualTo(0.70f);

        // Feature checks in 64-dim pure physics tensor:
        // Index 57 should match compositeCardBias
        assertThat(result.getDimension()).isEqualTo(64);
        assertThat(result.getFeatures()[57]).isEqualTo(result.getCompositeCardBias());
    }

    @Test
    void testTacticalTensorBuilderWithTennisClassDisparity() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        TennisVectorExtractor tennisExtractor = new TennisVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) tennisExtractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        SquadChemistryEvaluator chemistryEvaluator = new SquadChemistryEvaluator(clickHouse);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "TENNIS");
        dossier.put("team1_id", 101L);
        dossier.put("team2_id", 102L);
        dossier.put("team1_name", "Federico Cinà");
        dossier.put("team2_name", "Dino Prižmić");
        dossier.put("tournament_name", "Bratislava Challenger");
        dossier.put("country_name", "Slovakia");

        when(clickHouse.getSportMmr(eq(102L), eq("tennis"), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(102L, "Dino Prižmić", "ALL", 2150.0, 60.0, 180, 5, 2200.0)
        ));
        when(clickHouse.getSportMmr(eq(101L), eq("tennis"), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(101L, "Federico Cinà", "ALL", 1350.0, 150.0, 30, 0, 1400.0)
        ));
        when(clickHouse.getTennisMmr(eq(102L), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(102L, "Dino Prižmić", "ALL", 2150.0, 60.0, 180, 5, 2200.0)
        ));
        when(clickHouse.getTennisMmr(eq(101L), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(101L, "Federico Cinà", "ALL", 1350.0, 150.0, 30, 0, 1400.0)
        ));

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(998877L, dossier);

        assertThat(result).isNotNull();
        assertThat(result.getDimension()).isEqualTo(64);
        assertThat(result.getFeatures()).hasSize(64);

        float[] feats = result.getFeatures();
        // Block A (0..23): P1 (Cinà)
        // Block B (24..47): P2 (Prižmić)
        // Block C (48..63): Pairwise duel differentials

        // MMR P1 vs P2:
        float p1Mmr = feats[0];
        float p2Mmr = feats[24];
        assertThat(p2Mmr).isGreaterThan(p1Mmr);

        // Index 48: diff_mmr (P1 - P2) -> should be strongly negative (favoring Prižmić)
        float diffMmr = feats[48];
        assertThat(diffMmr).isLessThan(-0.30f);

        // Index 49: Expected win prob (P1) -> should be well below 0.50
        float expectedWinProb = feats[49];
        assertThat(expectedWinProb).isLessThan(0.30f);

        // Index 63: Strict neutral positional bias anchor
        assertThat(feats[63]).isEqualTo(0.0f);
    }

    @Test
    void testDuelTensorMirrorSymmetry() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        TennisVectorExtractor tennisExtractor = new TennisVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) tennisExtractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        SquadChemistryEvaluator chemistryEvaluator = new SquadChemistryEvaluator(clickHouse);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        // Case: Two identical players (unranked / equal MMR)
        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "TENNIS");
        dossier.put("team1_id", 201L);
        dossier.put("team2_id", 202L);
        dossier.put("team1_name", "Player Alpha");
        dossier.put("team2_name", "Player Beta");
        dossier.put("tournament_name", "ITF M15 Antalya");

        when(clickHouse.getSportMmr(anyLong(), eq("tennis"), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(201L, "Player", "ALL", 1500.0, 100.0, 50, 0, 1500.0)
        ));
        when(clickHouse.getTennisMmr(anyLong(), anyString())).thenReturn(java.util.Optional.of(
                new ClickHouseDdsClient.TennisMmr(201L, "Player", "ALL", 1500.0, 100.0, 50, 0, 1500.0)
        ));

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(777L, dossier);
        assertThat(result.getDimension()).isEqualTo(64);
        float[] feats = result.getFeatures();

        // Block A (feats[0..23]) must exactly equal Block B (feats[24..47])
        for (int i = 0; i < 24; i++) {
            assertThat(feats[i]).as("Feature " + i + " must match mirror feature " + (24 + i))
                    .isEqualTo(feats[24 + i]);
        }

        // Differentials in Block C:
        assertThat(feats[48]).isEqualTo(0.0f); // MMR diff
        assertThat(feats[49]).isEqualTo(0.50f); // Expected Win Prob exactly 50%
        assertThat(feats[51]).isEqualTo(0.0f); // Peak stature diff
        assertThat(feats[52]).isEqualTo(0.0f); // Service hold diff
        assertThat(feats[53]).isEqualTo(0.0f); // Return break diff
        assertThat(feats[54]).isEqualTo(0.0f); // Dominance ratio diff
        assertThat(feats[57]).isEqualTo(0.0f); // Overall WR diff
        assertThat(feats[59]).isEqualTo(0.50f); // H2H neutral 50%
        assertThat(feats[63]).isEqualTo(0.0f); // Strict neutral positional anchor
    }

    @Test
    void testSquadChemistryAndFifaVirusModulation() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) extractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        when(aggressionEvaluator.evaluateLineAggression(any()))
                .thenReturn(new PlayerLineAggressionEvaluator.LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f, 0.0f));
        when(aggressionEvaluator.evaluateMatchup(any(), any()))
                .thenReturn(new PlayerLineAggressionEvaluator.MatchupFrictionDynamics(0.22f, 0.16f, 0.16f, 0.45f, 0.0f, 0.43f));

        SquadChemistryEvaluator chemistryEvaluator = Mockito.mock(SquadChemistryEvaluator.class);
        // Team 1: Under severe FIFA virus (3 fatigued starters, stamina discount 0.90, late leak +0.12)
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem1 = new SquadChemistryEvaluator.SquadChemistryAndFatigue(
                0.70f, 0.88f, 0.85f, 1.0f, 3, 0.90f, 0.12f
        );
        // Team 2: Well-rested cohesive squad (0 fatigued, stamina 1.0, late leak 0.0)
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem2 = new SquadChemistryEvaluator.SquadChemistryAndFatigue(
                0.80f, 0.85f, 0.82f, 0.85f, 0, 1.0f, 0.0f
        );
        when(chemistryEvaluator.evaluateSquadChemistry(Mockito.eq(101L), any(), Mockito.anyLong())).thenReturn(chem1);
        when(chemistryEvaluator.evaluateSquadChemistry(Mockito.eq(102L), any(), Mockito.anyLong())).thenReturn(chem2);

        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        Map<String, Object> starter1 = new HashMap<>();
        starter1.put("rating", 7.0);
        starter1.put("position", "CB");

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_id", 101L);
        dossier.put("team2_id", 102L);
        dossier.put("team1_name", "Arsenal");
        dossier.put("team2_name", "Chelsea");
        dossier.put("team1_starters", java.util.List.of(starter1));
        dossier.put("team2_starters", java.util.List.of(starter1));

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(555L, dossier);

        assertThat(result).isNotNull();
        assertThat(result.getTeam1SquadChemistry()).isEqualTo(chem1);
        assertThat(result.getTeam2SquadChemistry()).isEqualTo(chem2);
        assertThat(result.getTeam1SquadChemistry().nationalFatiguedCount()).isEqualTo(3);

        float[] feats = result.getFeatures();
        assertThat(result.getDimension()).isEqualTo(64);
        // Dim 37: Team 1 avg starter rating discounted by 0.90 ((7.0 * 0.90) / 10.0 = 0.63)
        float hStarterRating = feats[37];
        assertThat(hStarterRating).isEqualTo((7.0f * 0.90f) / 10.0f);

        // Dim 38: Team 2 avg starter rating not discounted ((7.0 * 1.0) / 10.0 = 0.70)
        float aStarterRating = feats[38];
        assertThat(aStarterRating).isEqualTo(7.0f / 10.0f);

        // Dims 311 (Index 259 + 52) and 312 (Index 259 + 53): In advancedBlock (indices 20 and 21), home defense fatigue leak should include +0.12 bonus over away
        float[] advFeats = result.getPolymorphicTensor().getAdvancedBlock().getValues();
        float hFatigueLeak = advFeats[20];
        float aFatigueLeak = advFeats[21];
        assertThat(hFatigueLeak).isGreaterThan(aFatigueLeak);
        assertThat(hFatigueLeak - aFatigueLeak).isCloseTo(0.12f, org.assertj.core.data.Offset.offset(0.01f));
    }
}
