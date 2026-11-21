package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FootballVectorExtractorTest {

    private final ClickHouseDdsClient clickHouse = Mockito.mock(ClickHouseDdsClient.class);
    private final FootballVectorExtractor extractor = new FootballVectorExtractor(clickHouse);

    @Test
    @DisplayName("Team DNA: Missing Alpha Attacker activates deficit signal in dim 25")
    void testMissingAlphaAttackerDeficit() {
        // Lineup where Erling Haaland (rating 8.5) is on the BENCH (not in starters)
        Map<String, Object> haalandBench = Map.of(
                "sofascore_id", 839213L,
                "name", "Erling Haaland",
                "position", "FW",
                "rating", 8.5
        );

        // Starters without Haaland (reserve striker Julian Alvarez rating 7.1)
        List<Map<String, Object>> starters = List.of(
                Map.of("sofascore_id", 1L, "position", "GK", "rating", 7.2),
                Map.of("sofascore_id", 2L, "position", "CB", "rating", 7.4),
                Map.of("sofascore_id", 3L, "position", "CB", "rating", 7.1),
                Map.of("sofascore_id", 4L, "position", "CM", "rating", 7.5),
                Map.of("sofascore_id", 5L, "position", "CM", "rating", 7.3),
                Map.of("sofascore_id", 6L, "position", "FW", "rating", 7.1) // Alvarez
        );

        TacticalVectorContext ctxWithAbsence = TacticalVectorContext.builder()
                .teamId(17L)
                .teamName("Manchester City")
                .starters(starters)
                .bench(List.of(haalandBench))
                .build();

        float[] vecMissing = extractor.extractFormVector(ctxWithAbsence);

        // Dim 25 must reflect the missing alpha attacker deficit
        assertTrue(vecMissing[25] > 0.0f, "Missing alpha attacker deficit (dim 25) must be positive when Haaland is absent!");
        // Dim 29 must reflect bench firepower reserve
        assertTrue(vecMissing[29] > 0.0f, "Bench firepower (dim 29) must be positive with Haaland on bench!");

        // Now contrast with Haaland STARTING
        Map<String, Object> haalandStarter = Map.of(
                "sofascore_id", 839213L,
                "name", "Erling Haaland",
                "position", "FW",
                "rating", 8.5
        );
        List<Map<String, Object>> startersFull = List.of(
                Map.of("sofascore_id", 1L, "position", "GK", "rating", 7.2),
                Map.of("sofascore_id", 2L, "position", "CB", "rating", 7.4),
                Map.of("sofascore_id", 3L, "position", "CB", "rating", 7.1),
                Map.of("sofascore_id", 4L, "position", "CM", "rating", 7.5),
                Map.of("sofascore_id", 5L, "position", "CM", "rating", 7.3),
                haalandStarter
        );

        TacticalVectorContext ctxFull = TacticalVectorContext.builder()
                .teamId(17L)
                .teamName("Manchester City")
                .starters(startersFull)
                .bench(List.of())
                .build();

        float[] vecFull = extractor.extractFormVector(ctxFull);

        assertEquals(0.0f, vecFull[25], 1e-4, "Missing alpha attacker deficit (dim 25) must be 0 when top striker starts!");
        assertTrue(vecFull[24] > vecMissing[24], "Starter attacking potency (dim 24) must be higher with Haaland starting!");
    }
}
