package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, clickHouse);

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

        // Feature checks in 291-dim tensor:
        // lf[31] (index 259 + 31 = 290) should match compositeCardBias
        assertThat(result.getFeatures()[290]).isEqualTo(result.getCompositeCardBias());
    }

    @Test
    void testTacticalTensorBuilderWithTennisClassDisparity() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        TennisVectorExtractor tennisExtractor = new TennisVectorExtractor(clickHouse);
        when(registry.getExtractor(any(Sport.class))).thenReturn((SportVectorExtractor) tennisExtractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, clickHouse);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "TENNIS");
        dossier.put("team1_name", "Federico Cinà");
        dossier.put("team2_name", "Dino Prižmić");
        dossier.put("tournament_name", "Bratislava Challenger");
        dossier.put("country_name", "Slovakia");

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(998877L, dossier);

        assertThat(result).isNotNull();
        assertThat(result.getDimension()).isEqualTo(291);
        assertThat(result.getFeatures()).hasSize(291);

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
}
