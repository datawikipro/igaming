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
                .thenReturn(new PlayerLineAggressionEvaluator.LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f));
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
        assertThat(result.getDimension()).isEqualTo(356);
        assertThat(result.getFeatures()).hasSize(356);

        float[] feats = result.getFeatures();
        // Micro block begins at index 259:
        // Index 259 + 1: P1 (Cinà) effective rating
        // Index 259 + 13: P2 (Prižmić) effective rating
        float p1Eff = feats[259 + 1];
        float p2Eff = feats[259 + 13];
        assertThat(p2Eff).isGreaterThan(p1Eff);

        // Index 259 + 24: diff_effective_rating (P1 - P2) -> should be negative (favoring P2 in L2-norm space)
        float diffEff = feats[259 + 24];
        assertThat(diffEff).isLessThan(-0.05f);

        // Index 259 + 25: diff_short_set_power (P1 short power - P2 short power) -> should favor P2
        float diffShortPower = feats[259 + 25];
        assertThat(diffShortPower).isLessThan(-0.03f);

        // Index 259 + 29: class_disparity_mask -> should trigger 1.0f!
        float classDisparityMask = feats[259 + 29];
        assertThat(classDisparityMask).isEqualTo(1.0f);
    }

    @Test
    void testSquadChemistryAndFifaVirusModulation() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) extractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        when(aggressionEvaluator.evaluateLineAggression(any()))
                .thenReturn(new PlayerLineAggressionEvaluator.LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f));
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
