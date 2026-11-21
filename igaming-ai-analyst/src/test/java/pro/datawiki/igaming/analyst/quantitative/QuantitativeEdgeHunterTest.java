package pro.datawiki.igaming.analyst.quantitative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuantitativeEdgeHunterTest {

    @Mock
    private RedisOddsConsensusService redisOddsConsensusService;

    @Mock
    private pro.datawiki.igaming.analyst.service.odds.MarketOddsResolver marketOddsResolver;

    @Mock
    private InstitutionalRiskPolicyManager policyManager;

    @Mock
    private ContinuousMatrixEdgeScanner matrixEdgeScanner;

    private MarketFairProbabilityResolver probabilityResolver;
    private DossierOddsFallbackResolver fallbackOddsResolver;
    private QuantitativeEdgeHunter edgeHunter;

    @BeforeEach
    void setUp() {
        probabilityResolver = new MarketFairProbabilityResolver();
        fallbackOddsResolver = new DossierOddsFallbackResolver();
        edgeHunter = new QuantitativeEdgeHunter(
                redisOddsConsensusService,
                marketOddsResolver,
                policyManager,
                matrixEdgeScanner,
                probabilityResolver,
                fallbackOddsResolver
        );
    }

    @Test
    @DisplayName("Pre-lineup radar resolves market and calculates expected value")
    void testPreLineupRadar() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.65))
                .probDraw(BigDecimal.valueOf(0.20))
                .probAway(BigDecimal.valueOf(0.15))
                .probOver(BigDecimal.valueOf(0.55))
                .probUnder(BigDecimal.valueOf(0.45))
                .recommendedProb(BigDecimal.valueOf(0.65))
                .confidenceTier("ML_70")
                .decisionReasonCode("TACTICAL_EDGE")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("expected_total_baseline", 2.5);

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(new RedisOddsConsensusService.MarketQuote(1.80, "Pinnacle"));

        when(policyManager.evaluatePrudenceCaps(any(), anyString(), anyDouble(), anyString()))
                .thenReturn(InstitutionalRiskPolicyManager.PolicyCapResult.builder()
                        .rawTier("ML_70")
                        .appliedTier("ML_70")
                        .build());

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluatePreLineupRadar(12345L, ml, dossier, football);

        assertNotNull(res);
        assertEquals("ML_70", res.getConvictionTier());
        assertEquals("Pinnacle", res.getBestBookmaker());
        assertEquals(BigDecimal.valueOf(1.80).setScale(3), res.getRecommendedOdd());
        // EV = (1.80 * 0.65 - 1) * 100 = 17.0%
        assertTrue(res.getExpectedValuePct().doubleValue() > 0);
    }

    @Test
    @DisplayName("Stage 2 valuation correctly evaluates market edge and Kelly stake")
    void testStage2Valuation() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.45))
                .probDraw(BigDecimal.valueOf(0.30))
                .probAway(BigDecimal.valueOf(0.25))
                .probOver(BigDecimal.valueOf(0.40))
                .probUnder(BigDecimal.valueOf(0.60))
                .recommendedMarket("DOUBLE_CHANCE_1X")
                .confidenceTier("ML_70")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(new RedisOddsConsensusService.MarketQuote(1.45, "Pinnacle"));

        when(policyManager.evaluatePrudenceCaps(any(), anyString(), anyDouble(), anyString()))
                .thenReturn(InstitutionalRiskPolicyManager.PolicyCapResult.builder()
                        .rawTier("ML_70")
                        .appliedTier("ML_70")
                        .build());

        when(policyManager.calculateKellyStakePct(anyDouble(), anyDouble(), anyString()))
                .thenReturn(3.5);

        when(policyManager.applySoloSportUpsetDiscipline(any(), anyDouble(), anyInt(), any()))
                .thenAnswer(inv -> inv.getArgument(3));

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluateLineupConfirmed(
                12345L, ml, dossier, football, true, 30L
        );

        assertNotNull(res);
        assertEquals("ML_70", res.getConvictionTier());
        assertEquals("Pinnacle", res.getBestBookmaker());
        assertEquals(BigDecimal.valueOf(1.45).setScale(3), res.getRecommendedOdd());
        assertEquals(BigDecimal.valueOf(3.5).setScale(2), res.getKellyStakePct());
    }

    @Test
    @DisplayName("Pre-lineup radar returns PASS veto when bookmaker quote is missing")
    void testPreLineupRadarMissingQuoteReturnsPass() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.65))
                .probDraw(BigDecimal.valueOf(0.20))
                .probAway(BigDecimal.valueOf(0.15))
                .recommendedProb(BigDecimal.valueOf(0.65))
                .confidenceTier("ML_70")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(null);

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluatePreLineupRadar(12345L, ml, dossier, football);

        assertNotNull(res);
        assertEquals("PASS", res.getConvictionTier());
        assertEquals("PASS", res.getRecommendedMarket());
        assertEquals("NONE", res.getBestBookmaker());
        assertEquals(BigDecimal.ONE, res.getRecommendedOdd());
        assertEquals(BigDecimal.ZERO, res.getExpectedValuePct());
        assertEquals(BigDecimal.ZERO, res.getKellyStakePct());
    }

    @Test
    @DisplayName("Stage 2 valuation returns PASS veto when bookmaker quote is missing")
    void testStage2ValuationMissingQuoteReturnsPass() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.45))
                .probDraw(BigDecimal.valueOf(0.30))
                .probAway(BigDecimal.valueOf(0.25))
                .recommendedMarket("DOUBLE_CHANCE_1X")
                .confidenceTier("ML_70")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(null);

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluateLineupConfirmed(
                12345L, ml, dossier, football, true, 30L
        );

        assertNotNull(res);
        assertEquals("PASS", res.getConvictionTier());
        assertEquals("PASS", res.getRecommendedMarket());
        assertEquals("NONE", res.getBestBookmaker());
        assertEquals(BigDecimal.ONE, res.getRecommendedOdd());
        assertEquals(BigDecimal.ZERO, res.getExpectedValuePct());
        assertEquals(BigDecimal.ZERO, res.getKellyStakePct());
    }

    @Test
    @DisplayName("Pre-lineup radar returns PASS veto when expected value is negative")
    void testPreLineupRadarNegativeEvReturnsPass() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.40))
                .probDraw(BigDecimal.valueOf(0.10))
                .probAway(BigDecimal.valueOf(0.50))
                .recommendedProb(BigDecimal.valueOf(0.50))
                .confidenceTier("ML_60")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        // Odd is 1.40, fair prob for WIN1 (40%) gives EV = (1.40 * 0.40 - 1) = -44.0%
        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(new RedisOddsConsensusService.MarketQuote(1.40, "Pinnacle"));

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluatePreLineupRadar(12345L, ml, dossier, football);

        assertNotNull(res);
        assertEquals("PASS", res.getConvictionTier());
        assertEquals("PASS", res.getRecommendedMarket());
        assertEquals("NONE", res.getBestBookmaker());
        assertEquals(BigDecimal.ZERO, res.getExpectedValuePct());
        assertEquals(BigDecimal.ZERO, res.getKellyStakePct());
    }

    @Test
    @DisplayName("Stage 2 valuation returns PASS veto when expected value is negative")
    void testStage2ValuationNegativeEvReturnsPass() {
        Sport football = Sports.get("FOOTBALL");

        MlPredictionDto ml = MlPredictionDto.builder()
                .hasMlEvaluation(true)
                .probHome(BigDecimal.valueOf(0.52))
                .probDraw(BigDecimal.valueOf(0.24))
                .probAway(BigDecimal.valueOf(0.24))
                .recommendedMarket("DOUBLE_CHANCE_1X")
                .confidenceTier("ML_60")
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("sport_name", "FOOTBALL");

        when(matrixEdgeScanner.findBestMatrixEdge(anyLong(), any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        // Odd 1.20 (below MIN_RECOMMENDED_ODD 1.35 and negative EV)
        when(redisOddsConsensusService.findBestMarketQuote(anyLong(), anyString()))
                .thenReturn(new RedisOddsConsensusService.MarketQuote(1.20, "Pinnacle"));

        QuantitativeEdgeHunter.ValuationResult res = edgeHunter.evaluateLineupConfirmed(
                12345L, ml, dossier, football, true, 30L
        );

        assertNotNull(res);
        assertEquals("PASS", res.getConvictionTier());
        assertEquals("PASS", res.getRecommendedMarket());
        assertEquals("NONE", res.getBestBookmaker());
        assertEquals(BigDecimal.ZERO, res.getExpectedValuePct());
        assertEquals(BigDecimal.ZERO, res.getKellyStakePct());
    }
}
