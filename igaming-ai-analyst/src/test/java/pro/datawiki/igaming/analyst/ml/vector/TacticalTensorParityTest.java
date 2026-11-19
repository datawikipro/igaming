package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TacticalTensorParityTest {

    @Test
    @DisplayName("Macro Dims 0..7 must strictly match Python prepare_multitask_matrix.py formula")
    void testMacroParityWithPython() {
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);

        long matchTs = 1700000000000L;
        long lastTs = matchTs - (7L * 86400000L); // 7 days rest

        List<Map<String, Object>> hist = new ArrayList<>();
        // 5 wins (2-1), 2 draws (1-1), 3 losses (0-2) -> 10 matches, 14 GF, 13 GA
        for (int i = 0; i < 5; i++) {
            hist.add(Map.of("team_score", 2, "opponent_score", 1, "result", "WIN", "start_timestamp", lastTs));
        }
        for (int i = 0; i < 2; i++) {
            hist.add(Map.of("team_score", 1, "opponent_score", 1, "result", "DRAW", "start_timestamp", lastTs));
        }
        for (int i = 0; i < 3; i++) {
            hist.add(Map.of("team_score", 0, "opponent_score", 2, "result", "LOSS", "start_timestamp", lastTs));
        }

        TacticalVectorContext ctx = TacticalVectorContext.builder()
                .teamId(101L)
                .teamName("Test FC")
                .matchTimestamp(matchTs)
                .recentMatches(hist)
                .build();

        float[] macro = extractor.computeStandardMacroDims0To7(ctx, true);

        assertThat(macro[0]).isCloseTo(0.50f, offset(1e-5f)); // wins / 10
        assertThat(macro[1]).isCloseTo(0.20f, offset(1e-5f)); // draws / 10
        assertThat(macro[2]).isCloseTo(0.30f, offset(1e-5f)); // losses / 10
        assertThat(macro[3]).isCloseTo((12.0f / 10.0f) / 10.0f, offset(1e-5f)); // (12/10)/10 = 0.12
        assertThat(macro[4]).isCloseTo((13.0f / 10.0f) / 10.0f, offset(1e-5f)); // (13/10)/10 = 0.13
        assertThat(macro[5]).isCloseTo((-1.0f / 10.0f) / 10.0f, offset(1e-5f)); // (-1/10)/10 = -0.01
        assertThat(macro[6]).isCloseTo(1.0f, offset(1e-5f)); // min(1.0, 10/10) = 1.0
        assertThat(macro[7]).isCloseTo(0.50f, offset(1e-5f)); // 7 days / 14 = 0.50
    }

    @Test
    @DisplayName("Unrated players with 0.0 rating must safely fallback to 6.8 and produce 0.40 in Micro line ratings")
    void testUnratedZeroRatingBugEliminated() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);
        when(registry.getExtractor(any(pro.datawiki.igaming.dto.sport.Sport.class))).thenReturn((SportVectorExtractor) extractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        when(aggressionEvaluator.evaluateLineAggression(any()))
                .thenReturn(new PlayerLineAggressionEvaluator.LineAggressionProfile(0.42f, 0.47f, 0.37f, 0.06f, 0.45f, 0.25f));
        when(aggressionEvaluator.evaluateMatchup(any(), any()))
                .thenReturn(new PlayerLineAggressionEvaluator.MatchupFrictionDynamics(0.22f, 0.16f, 0.16f, 0.45f, 0.0f, 0.43f));

        SquadChemistryEvaluator chemistryEvaluator = new SquadChemistryEvaluator(clickHouse);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        // Starters with literal rating 0.0 (like Sofascore unrated women/minor matches)
        List<Map<String, Object>> s1 = List.of(
                Map.of("name", "Player 1", "position", "GK", "rating", 0.0),
                Map.of("name", "Player 2", "position", "CB", "rating", 0.0),
                Map.of("name", "Player 3", "position", "FW", "rating", 0.0)
        );
        List<Map<String, Object>> s2 = List.of(
                Map.of("name", "Player A", "position", "GK", "rating", 0.0),
                Map.of("name", "Player B", "position", "CB", "rating", 0.0),
                Map.of("name", "Player C", "position", "FW", "rating", 0.0)
        );

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_name", "Team One");
        dossier.put("team2_name", "Team Two");
        dossier.put("team1_starters", s1);
        dossier.put("team2_starters", s2);

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(777L, dossier);
        float[] feats = result.getFeatures();

        // Micro block begins at index 259
        // Dims 38..41 (indices 259 + 38 = 297, 298, 299, 300)
        // With rating 0.0 safely parsed as 6.8:
        // (6.8 - 6.0) / 2.0 = 0.40f. It must NEVER be -3.0f!
        assertThat(feats[259 + 38]).isCloseTo(0.40f, offset(0.01f));
        assertThat(feats[259 + 39]).isCloseTo(0.40f, offset(0.01f));
        assertThat(feats[259 + 40]).isCloseTo(0.40f, offset(0.01f));
        assertThat(feats[259 + 41]).isCloseTo(0.40f, offset(0.01f));

        // Average ratings (index 259 + 1 and 259 + 13) must be 6.8f, not 0.0f
        assertThat(feats[259 + 1]).isCloseTo(6.8f, offset(0.05f));
        assertThat(feats[259 + 13]).isCloseTo(6.8f, offset(0.05f));
    }

    @Test
    @DisplayName("Matches without confirmed lineups must exactly return DEFAULT_LINEUP_FEATS")
    void testDefaultLineupFeatsParity() {
        SportVectorExtractorRegistry registry = Mockito.mock(SportVectorExtractorRegistry.class);
        ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
        FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);
        when(registry.getExtractor(any(pro.datawiki.igaming.dto.sport.Sport.class))).thenReturn((SportVectorExtractor) extractor);

        PlayerLineAggressionEvaluator aggressionEvaluator = Mockito.mock(PlayerLineAggressionEvaluator.class);
        SquadChemistryEvaluator chemistryEvaluator = new SquadChemistryEvaluator(clickHouse);
        TacticalTensorBuilder builder = new TacticalTensorBuilder(registry, aggressionEvaluator, chemistryEvaluator, clickHouse, null);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_name", "Real Madrid");
        dossier.put("team2_name", "Barcelona");
        // No lineups in dossier

        TacticalTensorBuilder.TensorResult result = builder.buildTensor(888L, dossier);
        float[] feats = result.getFeatures();

        // Dims 259..322 in feats must match DEFAULT_LINEUP_FEATS (except lf[31] composite card bias)
        for (int i = 0; i < 64; i++) {
            if (i == 31) continue; // Composite card bias is dynamic
            assertThat(feats[259 + i])
                    .as("Lineup feature at index " + i)
                    .isEqualTo(TacticalTensorBuilder.DEFAULT_LINEUP_FEATS[i]);
        }
    }
}
