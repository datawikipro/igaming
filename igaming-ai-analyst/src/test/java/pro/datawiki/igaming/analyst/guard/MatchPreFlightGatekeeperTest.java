package pro.datawiki.igaming.analyst.guard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.AnalystAnomalyReporter;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchPreFlightGatekeeperTest {

    @Mock
    private MatchAiRecommendationRepository recommendationRepository;

    @Mock
    private RedisOddsConsensusService redisOddsConsensusService;

    @Mock
    private AnalystAnomalyReporter anomalyReporter;

    @InjectMocks
    private MatchPreFlightGatekeeper gatekeeper;

    @Test
    @DisplayName("Should stop and quarantine placeholder league ('League 9661')")
    void testPlaceholderLeagueQuarantine() {
        Sport football = Sports.get("FOOTBALL");
        MatchAiRecommendation rec = new MatchAiRecommendation();
        rec.setMatchId(101L);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("league_name", "League 9661");
        dossier.put("team1_name", "Alpha");
        dossier.put("team2_name", "Beta");

        MatchPreFlightGatekeeper.PreFlightDecision decision = gatekeeper.checkPreFlightGuards(
                101L, dossier, football, rec, false
        );

        assertFalse(decision.shouldProceed());
        assertEquals("SKIPPED_UNMAPPED_LEAGUE", decision.getStage());
        assertEquals("PASS", rec.getConvictionTier());
        assertEquals("SKIPPED_UNMAPPED_LEAGUE", rec.getEvaluationStage());
    }

    @Test
    @DisplayName("Should detect solo sport unverified player entity and force PASS (Zero-Garbage Invariant)")
    void testSoloSportUnresolvedEntity() {
        Sport tennis = Sports.get("TENNIS");
        MatchAiRecommendation rec = new MatchAiRecommendation();
        rec.setMatchId(202L);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("league_name", "ITF Men");
        dossier.put("team1_name", "Novak Djokovic");
        dossier.put("team2_name", "Unknown Player");
        dossier.put("team1_recent_matches", List.of(Map.of("match", 1)));
        dossier.put("team2_recent_matches", Collections.emptyList()); // 0 matches & no sofa id

        MatchPreFlightGatekeeper.PreFlightDecision decision = gatekeeper.checkPreFlightGuards(
                202L, dossier, tennis, rec, false
        );

        assertFalse(decision.shouldProceed());
        assertEquals("UNRESOLVED_PLAYER_PASS", decision.getStage());
        assertEquals("PASS", rec.getConvictionTier());
        assertEquals("UNRESOLVED_PLAYER_ENTITY", rec.getErrorCategory());
    }

    @Test
    @DisplayName("Should proceed when all guards pass successfully")
    void testGuardsProceed() {
        Sport football = Sports.get("FOOTBALL");
        MatchAiRecommendation rec = new MatchAiRecommendation();
        rec.setMatchId(303L);

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("league_name", "Premier League");
        dossier.put("team1_name", "Arsenal");
        dossier.put("team2_name", "Chelsea");
        dossier.put("start_time", System.currentTimeMillis() + 3600000L);

        when(recommendationRepository.findByEvaluationStageNotIn(any())).thenReturn(Collections.emptyList());

        MatchPreFlightGatekeeper.PreFlightDecision decision = gatekeeper.checkPreFlightGuards(
                303L, dossier, football, rec, false
        );

        assertTrue(decision.shouldProceed());
        assertEquals("Arsenal", rec.getTeam1Name());
        assertEquals("Chelsea", rec.getTeam2Name());
    }
}
