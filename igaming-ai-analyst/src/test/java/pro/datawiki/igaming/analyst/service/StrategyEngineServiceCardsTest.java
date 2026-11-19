package pro.datawiki.igaming.analyst.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.datawiki.igaming.analyst.entity.AiStrategyTemplate;
import pro.datawiki.igaming.analyst.entity.AiStrategyVote;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiStrategyTemplateRepository;
import pro.datawiki.igaming.analyst.repository.AiStrategyVoteRepository;
import pro.datawiki.igaming.analyst.repository.AiUserStrategySettingRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StrategyEngineServiceCardsTest {

    @Test
    void testCardsUnderIsVetoedInDerbyAndStrictReferee() {
        AiStrategyTemplateRepository templateRepo = Mockito.mock(AiStrategyTemplateRepository.class);
        AiUserStrategySettingRepository userSettingRepo = Mockito.mock(AiUserStrategySettingRepository.class);
        AiStrategyVoteRepository voteRepo = Mockito.mock(AiStrategyVoteRepository.class);
        MatchAiRecommendationRepository recRepo = Mockito.mock(MatchAiRecommendationRepository.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        jakarta.persistence.EntityManager entityManager = Mockito.mock(jakarta.persistence.EntityManager.class);

        AiStrategyTemplate safeStrat = AiStrategyTemplate.builder()
                .slug("safest-pick")
                .name("Safest Pick")
                .minOdd(BigDecimal.valueOf(1.30))
                .minProbabilityPct(BigDecimal.valueOf(60.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(safeStrat));

        StrategyEngineService service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager);

        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(100L)
                .matchId(5555L)
                .team1Name("Helmond Sport")
                .team2Name("FC Eindhoven")
                .leagueName("Eerste Divisie")
                .sportSlug("football")
                .fairP1(BigDecimal.valueOf(0.35))
                .fairPx(BigDecimal.valueOf(0.30))
                .fairP2(BigDecimal.valueOf(0.35))
                .fairUnder25(BigDecimal.valueOf(0.55))
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("is_derby", true);
        dossier.put("referee_name", "Stan Teuben");
        dossier.put("referee_avg_yellow_cards", 5.2f);
        dossier.put("referee_strictness", "VERY_STRICT");
        dossier.put("country_name", "Netherlands");

        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);

        service.generateStrategyVotes(rec, 1.80, null, dossier);

        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();
        assertThat(savedVotes).isNotEmpty();

        // Must NOT select TOTAL_YELLOW_CARDS_UNDER_4.5 because it's a Derby with a Strict Referee!
        for (AiStrategyVote vote : savedVotes) {
            assertThat(vote.getMarket()).isNotEqualTo("TOTAL_YELLOW_CARDS_UNDER_4.5");
        }
    }

    @Test
    void testCardsUnderIsAllowedInCalmMatchWithCalibratedProb() {
        AiStrategyTemplateRepository templateRepo = Mockito.mock(AiStrategyTemplateRepository.class);
        AiUserStrategySettingRepository userSettingRepo = Mockito.mock(AiUserStrategySettingRepository.class);
        AiStrategyVoteRepository voteRepo = Mockito.mock(AiStrategyVoteRepository.class);
        MatchAiRecommendationRepository recRepo = Mockito.mock(MatchAiRecommendationRepository.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        jakarta.persistence.EntityManager entityManager = Mockito.mock(jakarta.persistence.EntityManager.class);

        AiStrategyTemplate safeStrat = AiStrategyTemplate.builder()
                .slug("safest-pick")
                .name("Safest Pick")
                .minOdd(BigDecimal.valueOf(1.30))
                .minProbabilityPct(BigDecimal.valueOf(60.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(safeStrat));

        StrategyEngineService service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager);

        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(101L)
                .matchId(5556L)
                .team1Name("Bodø/Glimt")
                .team2Name("Sandefjord")
                .leagueName("Eliteserien")
                .sportSlug("football")
                .fairP1(BigDecimal.valueOf(0.50))
                .fairPx(BigDecimal.valueOf(0.28))
                .fairP2(BigDecimal.valueOf(0.22))
                .fairUnder25(BigDecimal.valueOf(0.55))
                .build();

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("is_derby", false);
        dossier.put("referee_name", "Calm Ref");
        dossier.put("referee_avg_yellow_cards", 2.8f);
        dossier.put("referee_strictness", "LENIENT");
        dossier.put("country_name", "Norway");

        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);

        service.generateStrategyVotes(rec, 1.80, null, dossier);

        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();
        assertThat(savedVotes).isNotEmpty();

        // In a calm match, if TOTAL_YELLOW_CARDS_UNDER_4.5 is eligible, its prob is calibrated <= 0.76 (not hardcoded 0.84)
        for (AiStrategyVote vote : savedVotes) {
            if ("TOTAL_YELLOW_CARDS_UNDER_4.5".equals(vote.getMarket())) {
                assertThat(vote.getFairProbability().doubleValue()).isLessThanOrEqualTo(0.76);
            }
        }
    }

    @Test
    void testWhenConvictionTierIsPassAllStrategiesVotePass() {
        AiStrategyTemplateRepository templateRepo = Mockito.mock(AiStrategyTemplateRepository.class);
        AiUserStrategySettingRepository userSettingRepo = Mockito.mock(AiUserStrategySettingRepository.class);
        AiStrategyVoteRepository voteRepo = Mockito.mock(AiStrategyVoteRepository.class);
        MatchAiRecommendationRepository recRepo = Mockito.mock(MatchAiRecommendationRepository.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        jakarta.persistence.EntityManager entityManager = Mockito.mock(jakarta.persistence.EntityManager.class);

        AiStrategyTemplate safeStrat = AiStrategyTemplate.builder()
                .slug("ultra_sniper")
                .name("Ультра-Снайпер")
                .minOdd(BigDecimal.valueOf(1.30))
                .minProbabilityPct(BigDecimal.valueOf(95.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        AiStrategyTemplate balStrat = AiStrategyTemplate.builder()
                .slug("balanced_growth")
                .name("Сбалансированный")
                .minOdd(BigDecimal.valueOf(1.50))
                .minProbabilityPct(BigDecimal.valueOf(72.0))
                .ladderStepRule("BALANCED")
                .pureWinAllowed(true)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(safeStrat, balStrat));

        StrategyEngineService service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager);

        // Even if probabilities exist (e.g. Over 2.5 was 0.85), if convictionTier == "PASS":
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(200L)
                .matchId(7777L)
                .team1Name("Low League A")
                .team2Name("Low League B")
                .leagueName("Amateur Regional")
                .sportSlug("football")
                .convictionTier("PASS")
                .evaluationStage("GATEKEEPER_REJECT")
                .fairP1(BigDecimal.valueOf(0.50))
                .fairPx(BigDecimal.valueOf(0.25))
                .fairP2(BigDecimal.valueOf(0.25))
                .fairOver25(BigDecimal.valueOf(0.85))
                .build();

        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);

        service.generateStrategyVotes(rec, 1.50, null, null);

        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();
        assertThat(savedVotes).hasSize(2);

        // Both strategies MUST vote PASS and resultStatus must be SKIPPED
        for (AiStrategyVote vote : savedVotes) {
            assertThat(vote.getVoteDecision()).isEqualTo("PASS");
            assertThat(vote.getMarket()).isEqualTo("PASS");
            assertThat(vote.getResultStatus()).isEqualTo("SKIPPED");
            assertThat(vote.getStakeRub()).isEqualTo(BigDecimal.ZERO);
        }
    }
    @Test
    void testExpectedValuePctCalculationAndEnrichment() {
        AiStrategyTemplateRepository templateRepo = Mockito.mock(AiStrategyTemplateRepository.class);
        AiUserStrategySettingRepository userSettingRepo = Mockito.mock(AiUserStrategySettingRepository.class);
        AiStrategyVoteRepository voteRepo = Mockito.mock(AiStrategyVoteRepository.class);
        MatchAiRecommendationRepository recRepo = Mockito.mock(MatchAiRecommendationRepository.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        jakarta.persistence.EntityManager entityManager = Mockito.mock(jakarta.persistence.EntityManager.class);

        AiStrategyTemplate ironBank = AiStrategyTemplate.builder()
                .slug("iron_bank")
                .name("Железный банк")
                .minOdd(BigDecimal.valueOf(1.30))
                .minProbabilityPct(BigDecimal.valueOf(70.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(ironBank));
        when(templateRepo.findBySlug("iron_bank")).thenReturn(java.util.Optional.of(ironBank));

        StrategyEngineService service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager);

        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(300L)
                .matchId(8888L)
                .team1Name("Team A")
                .team2Name("Team B")
                .leagueName("Premier League")
                .sportSlug("football")
                .convictionTier("ML_80")
                .evaluationStage("PRE_LINEUP")
                .fairP1(BigDecimal.valueOf(0.10))
                .fairPx(BigDecimal.valueOf(0.20))
                .fairP2(BigDecimal.valueOf(0.70))
                .recommendedMarket("WIN2")
                .recommendedOdd(BigDecimal.valueOf(1.80))
                .availableBookmakers("pinnacle")
                .build();

        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);
        service.generateStrategyVotes(rec, 1.80, "WIN2", null);

        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();
        assertThat(savedVotes).hasSize(1);
        AiStrategyVote vote = savedVotes.get(0);
        assertThat(vote.getVoteDecision()).isEqualTo("PICK");
        assertThat(vote.getExpectedValuePct()).isNotNull();
        assertThat(vote.getExpectedValuePct().doubleValue()).isNotZero();

        // Test enrichment backfill when expectedValuePct is zero
        AiStrategyVote zeroVote = AiStrategyVote.builder()
                .id(1L)
                .recommendationId(rec.getId())
                .matchId(rec.getMatchId())
                .strategySlug("iron_bank")
                .voteDecision("PICK")
                .recommendedOdd(BigDecimal.valueOf(1.50))
                .fairProbability(BigDecimal.valueOf(80.0))
                .expectedValuePct(BigDecimal.ZERO)
                .build();

        when(voteRepo.findByRecommendationIdIn(List.of(rec.getId()))).thenReturn(List.of(zeroVote));
        service.enrichRecommendationsWithVotes(List.of(rec), "iron_bank");

        assertThat(rec.getActiveStrategyVote()).isNotNull();
        assertThat(rec.getActiveStrategyVote().getExpectedValuePct().doubleValue()).isEqualTo(20.0);
    }
}
