package pro.datawiki.igaming.analyst.quantitative;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.dto.ml.ContinuousProbabilityMatrixDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MarketFairProbabilityResolverTest {

    private final MarketFairProbabilityResolver resolver = new MarketFairProbabilityResolver();

    @Test
    @DisplayName("Resolves match result probabilities correctly from ML outputs")
    void testMatchResultProbabilities() {
        MlPredictionDto ml = MlPredictionDto.builder()
                .probHome(BigDecimal.valueOf(0.60))
                .probDraw(BigDecimal.valueOf(0.25))
                .probAway(BigDecimal.valueOf(0.15))
                .build();

        assertEquals(60.0, resolver.resolveFairProbability(ml, "WIN1", null), 0.01);
        assertEquals(25.0, resolver.resolveFairProbability(ml, "DRAW", null), 0.01);
        assertEquals(15.0, resolver.resolveFairProbability(ml, "WIN2", null), 0.01);
        assertEquals(85.0, resolver.resolveFairProbability(ml, "DOUBLE_CHANCE_1X", null), 0.01);
        assertEquals(40.0, resolver.resolveFairProbability(ml, "DOUBLE_CHANCE_X2", null), 0.01);
    }

    @Test
    @DisplayName("Resolves continuous matrix totals and handicaps when matrix is present")
    void testContinuousMatrixResolution() {
        ContinuousProbabilityMatrixDto matrix = ContinuousProbabilityMatrixDto.builder()
                .totalOverProbabilities(Map.of(2.5, 0.58, 3.5, 0.35))
                .totalUnderProbabilities(Map.of(2.5, 0.42, 3.5, 0.65))
                .handicapHomeProbabilities(Map.of(-1.5, 0.40, 1.5, 0.88))
                .handicapAwayProbabilities(Map.of(1.5, 0.60, -1.5, 0.12))
                .bttsYesProb(0.54)
                .bttsNoProb(0.46)
                .build();

        MlPredictionDto ml = MlPredictionDto.builder()
                .probabilityMatrix(matrix)
                .probHome(BigDecimal.valueOf(0.50))
                .probAway(BigDecimal.valueOf(0.50))
                .build();

        assertEquals(58.0, resolver.resolveFairProbability(ml, "TOTAL_OVER_2.5", null), 0.01);
        assertEquals(42.0, resolver.resolveFairProbability(ml, "TOTAL_UNDER_2.5", null), 0.01);
        assertEquals(88.0, resolver.resolveFairProbability(ml, "AH_HOME_PLUS_1.5", null), 0.01);
        assertEquals(54.0, resolver.resolveFairProbability(ml, "BTTS_YES", null), 0.01);
        assertEquals(46.0, resolver.resolveFairProbability(ml, "BTTS_NO", null), 0.01);
    }

    @Test
    @DisplayName("Resolves tennis set handicap +1.5 using exact binomial set math")
    void testTennisSetHandicapPlus15() {
        Sport tennis = Sports.get("TENNIS");
        MlPredictionDto ml = MlPredictionDto.builder()
                .probHome(BigDecimal.valueOf(0.60))
                .probAway(BigDecimal.valueOf(0.40))
                .build();

        // In best-of-3, TacticalProbabilityMath converts match prob 0.60 to set prob ~0.55,
        // so P(win >= 1 set) = 1 - (1 - p_set)^2 = 79.8%
        double prob = resolver.resolveFairProbability(ml, "AH_HOME_PLUS_1.5", tennis, 3, null);
        assertEquals(79.8, prob, 0.5);
    }
}
