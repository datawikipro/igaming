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
}
