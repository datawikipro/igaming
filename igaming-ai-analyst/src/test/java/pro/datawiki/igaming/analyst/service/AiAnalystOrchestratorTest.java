package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.guard.MatchPreFlightGatekeeper;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.analyst.quantitative.QuantitativeEdgeHunter;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.scenario.LineupScenarioEnricher;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAnalystOrchestratorTest {

    @Mock
    private MatchDossierService dossierService;

    @Mock
    private MlInferenceClient mlInferenceClient;

    @Mock
    private MatchAiRecommendationRepository recommendationRepository;

    @Mock
    private StrategyEngineService strategyEngineService;

    @Mock
    private AnalystStatisticsService statisticsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MatchPreFlightGatekeeper preFlightGatekeeper;

    @Mock
    private QuantitativeEdgeHunter edgeHunter;

    @Mock
    private InstitutionalRiskPolicyManager policyManager;

    @Mock
    private LineupScenarioEnricher lineupScenarioEnricher;

    @Mock
    private AnalystAnomalyReporter anomalyReporter;

    @InjectMocks
    private AiAnalystOrchestrator orchestrator;

    private Long matchId = 55555L;

    @BeforeEach
    void setUp() {
        when(recommendationRepository.findByMatchIdAndModelVersion(eq(matchId), anyString()))
                .thenReturn(Optional.empty());
        when(recommendationRepository.save(any(MatchAiRecommendation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should detect team1 with 0 historical matches, reject bet (PASS), log error and publish to Kafka")
    void testTeam1ZeroHistoryAnomaly() {
        when(dossierService.getFormattedPrompt(matchId)).thenReturn("VALID PROMPT");

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("team1_name", "Unmapped City");
        dossier.put("team2_name", "Arsenal");
        dossier.put("league_name", "Premier League");
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_recent_matches", Collections.emptyList());
        dossier.put("team2_recent_matches", List.of(Map.of("result", "WIN")));

        when(dossierService.getMatchDossier(matchId)).thenReturn(dossier);

        MatchPreFlightGatekeeper.PreFlightDecision proceed = MatchPreFlightGatekeeper.PreFlightDecision.builder()
                .decisionType(MatchPreFlightGatekeeper.DecisionType.PROCEED)
                .recommendation(new MatchAiRecommendation())
                .build();
        when(preFlightGatekeeper.checkPreFlightGuards(eq(matchId), eq(dossier), any(), any(), eq(false)))
                .thenReturn(proceed);

        MlPredictionDto mlReject = MlPredictionDto.builder()
                .hasMlEvaluation(false)
                .decisionReasonCode("ZERO_HISTORY_UNMAPPED_TEAM")
                .reason("BETTING_PROHIBITED_ZERO_HISTORY")
                .build();
        when(mlInferenceClient.predict(eq(matchId), eq(dossier))).thenReturn(mlReject);

        Optional<MatchAiRecommendation> opt = orchestrator.evaluateMatch(matchId);

        assertTrue(opt.isPresent());
        MatchAiRecommendation rec = opt.get();
        assertEquals("PASS", rec.getConvictionTier());
        assertEquals("PASS", rec.getRecommendedMarket());
        assertEquals(BigDecimal.ONE, rec.getRecommendedOdd());
        assertEquals("UNMAPPED_TEAM_PASS", rec.getEvaluationStage());
        assertEquals("UNMAPPED_TEAM_ZERO_HISTORY", rec.getErrorCategory());

        // Verify that anomalyReporter was called for Unmapped City
        verify(anomalyReporter, times(1)).reportUnmappedTeam(
                eq(matchId), eq("Unmapped City"), eq("Premier League"), eq("FOOTBALL"), eq("football")
        );
        verify(anomalyReporter, never()).reportUnmappedTeam(
                eq(matchId), eq("Arsenal"), anyString(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("Should detect team2 with 0 historical matches, reject bet (PASS), log error and publish to Kafka")
    void testTeam2ZeroHistoryAnomaly() {
        when(dossierService.getFormattedPrompt(matchId)).thenReturn("VALID PROMPT");

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("team1_name", "Chelsea");
        dossier.put("team2_name", "Ghost FC");
        dossier.put("league_name", "Premier League");
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_recent_matches", List.of(Map.of("result", "WIN")));
        dossier.put("team2_recent_matches", Collections.emptyList());

        when(dossierService.getMatchDossier(matchId)).thenReturn(dossier);

        MatchPreFlightGatekeeper.PreFlightDecision proceed = MatchPreFlightGatekeeper.PreFlightDecision.builder()
                .decisionType(MatchPreFlightGatekeeper.DecisionType.PROCEED)
                .recommendation(new MatchAiRecommendation())
                .build();
        when(preFlightGatekeeper.checkPreFlightGuards(eq(matchId), eq(dossier), any(), any(), eq(false)))
                .thenReturn(proceed);

        MlPredictionDto mlReject = MlPredictionDto.builder()
                .hasMlEvaluation(false)
                .decisionReasonCode("ZERO_HISTORY_UNMAPPED_TEAM")
                .reason("BETTING_PROHIBITED_ZERO_HISTORY")
                .build();
        when(mlInferenceClient.predict(eq(matchId), eq(dossier))).thenReturn(mlReject);

        Optional<MatchAiRecommendation> opt = orchestrator.evaluateMatch(matchId);

        assertTrue(opt.isPresent());
        MatchAiRecommendation rec = opt.get();
        assertEquals("PASS", rec.getConvictionTier());
        assertEquals("PASS", rec.getRecommendedMarket());
        assertEquals("UNMAPPED_TEAM_PASS", rec.getEvaluationStage());

        // Verify that anomalyReporter was called for Ghost FC
        verify(anomalyReporter, times(1)).reportUnmappedTeam(
                eq(matchId), eq("Ghost FC"), eq("Premier League"), eq("FOOTBALL"), eq("football")
        );
    }

    @Test
    @DisplayName("Should detect both teams with 0 historical matches and report both")
    void testBothTeamsZeroHistoryAnomaly() {
        when(dossierService.getFormattedPrompt(matchId)).thenReturn("VALID PROMPT");

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("team1_name", "Ghost A");
        dossier.put("team2_name", "Ghost B");
        dossier.put("league_name", "La Liga");
        dossier.put("sport_name", "FOOTBALL");
        dossier.put("team1_recent_matches", Collections.emptyList());
        dossier.put("team2_recent_matches", Collections.emptyList());

        when(dossierService.getMatchDossier(matchId)).thenReturn(dossier);

        MatchPreFlightGatekeeper.PreFlightDecision proceed = MatchPreFlightGatekeeper.PreFlightDecision.builder()
                .decisionType(MatchPreFlightGatekeeper.DecisionType.PROCEED)
                .recommendation(new MatchAiRecommendation())
                .build();
        when(preFlightGatekeeper.checkPreFlightGuards(eq(matchId), eq(dossier), any(), any(), eq(false)))
                .thenReturn(proceed);

        MlPredictionDto mlReject = MlPredictionDto.builder()
                .hasMlEvaluation(false)
                .decisionReasonCode("ZERO_HISTORY_UNMAPPED_TEAM")
                .build();
        when(mlInferenceClient.predict(eq(matchId), eq(dossier))).thenReturn(mlReject);

        Optional<MatchAiRecommendation> opt = orchestrator.evaluateMatch(matchId);

        assertTrue(opt.isPresent());
        MatchAiRecommendation rec = opt.get();
        assertEquals("PASS", rec.getConvictionTier());
        assertEquals("UNMAPPED_TEAM_PASS", rec.getEvaluationStage());

        // Verify that anomalyReporter was called for both teams
        verify(anomalyReporter, times(1)).reportUnmappedTeam(
                eq(matchId), eq("Ghost A"), eq("La Liga"), eq("FOOTBALL"), eq("football")
        );
        verify(anomalyReporter, times(1)).reportUnmappedTeam(
                eq(matchId), eq("Ghost B"), eq("La Liga"), eq("FOOTBALL"), eq("football")
        );
    }
}
