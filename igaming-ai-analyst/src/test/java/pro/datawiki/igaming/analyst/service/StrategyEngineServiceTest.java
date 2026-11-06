package pro.datawiki.igaming.analyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StrategyEngineServiceTest {

    private AiStrategyTemplateRepository templateRepo;
    private AiUserStrategySettingRepository userSettingRepo;
    private AiStrategyVoteRepository voteRepo;
    private MatchAiRecommendationRepository recRepo;
    private JdbcTemplate jdbcTemplate;
    private jakarta.persistence.EntityManager entityManager;
    private StrategyEngineService service;

    private AiStrategyTemplate stratNerves;
    private AiStrategyTemplate stratHighOdds;
    private AiStrategyTemplate stratSniper;

    @BeforeEach
    void setUp() {
        templateRepo = Mockito.mock(AiStrategyTemplateRepository.class);
        userSettingRepo = Mockito.mock(AiUserStrategySettingRepository.class);
        voteRepo = Mockito.mock(AiStrategyVoteRepository.class);
        recRepo = Mockito.mock(MatchAiRecommendationRepository.class);
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        entityManager = Mockito.mock(jakarta.persistence.EntityManager.class);

        stratNerves = AiStrategyTemplate.builder()
                .slug("nerves_shield")
                .name("Защита нервов")
                .minOdd(BigDecimal.valueOf(1.30))
                .maxOdd(BigDecimal.valueOf(1.65))
                .minProbabilityPct(BigDecimal.valueOf(88.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(false)
                .build();

        stratHighOdds = AiStrategyTemplate.builder()
                .slug("high_odds")
                .name("Высокий кэф")
                .minOdd(BigDecimal.valueOf(2.05))
                .maxOdd(BigDecimal.valueOf(2.85))
                .minProbabilityPct(BigDecimal.valueOf(55.0))
                .ladderStepRule("MAX_ODD_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        stratSniper = AiStrategyTemplate.builder()
                .slug("ultra_sniper")
                .name("Ультра-Снайпер")
                .minOdd(BigDecimal.valueOf(1.30))
                .maxOdd(BigDecimal.valueOf(1.50))
                .minProbabilityPct(BigDecimal.valueOf(95.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(false)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc())
                .thenReturn(List.of(stratNerves, stratHighOdds, stratSniper));

        service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager);
    }

    @Test
    @DisplayName("When only partial strategy votes exist, generates ONLY the missing strategy invariants")
    void testGenerateStrategyVotes_WhenPartialVotesExist_GeneratesOnlyMissingTemplates() {
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(101L)
                .matchId(777L)
                .team1Name("Arsenal")
                .team2Name("Chelsea")
                .leagueName("Premier League")
                .sportSlug("football")
                .fairP1(BigDecimal.valueOf(0.60))
                .fairPx(BigDecimal.valueOf(0.25))
                .fairP2(BigDecimal.valueOf(0.15))
                .fairOver25(BigDecimal.valueOf(0.55))
                .fairUnder25(BigDecimal.valueOf(0.45))
                .availableBookmakers("leon,fonbet")
                .recommendedOdd(BigDecimal.valueOf(1.85))
                .recommendedMarket("TOTAL_OVER_2.5")
                .convictionTier("ML_80")
                .evaluationStage("PRE_LINEUP")
                .build();

        // Existing vote for nerves_shield only
        AiStrategyVote existingVote = AiStrategyVote.builder()
                .id(1L)
                .recommendationId(101L)
                .matchId(777L)
                .strategySlug("nerves_shield")
                .strategyName("Защита нервов")
                .market("1X")
                .voteDecision("PICK")
                .build();

        when(voteRepo.findByRecommendationId(101L)).thenReturn(new ArrayList<>(List.of(existingVote)));
        when(voteRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<AiStrategyVote> result = service.generateStrategyVotes(rec, 1.85, "TOTAL_OVER_2.5", null);

        // Verify saveAll was called with exactly 2 new votes (high_odds and ultra_sniper)
        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);
        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();

        assertThat(savedVotes).hasSize(2);
        List<String> savedSlugs = savedVotes.stream().map(AiStrategyVote::getStrategySlug).toList();
        assertThat(savedSlugs).containsExactlyInAnyOrder("high_odds", "ultra_sniper");
        assertThat(savedSlugs).doesNotContain("nerves_shield"); // already existed!

        // Combined result must contain all 3
        assertThat(result).hasSize(3);
        List<String> resultSlugs = result.stream().map(AiStrategyVote::getStrategySlug).toList();
        assertThat(resultSlugs).containsExactlyInAnyOrder("nerves_shield", "high_odds", "ultra_sniper");
    }

    @Test
    @DisplayName("When all strategy votes already exist, returns existing without saving duplicates")
    void testGenerateStrategyVotes_WhenAllVotesExist_ReturnsExistingWithoutSaving() {
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(102L)
                .matchId(888L)
                .team1Name("Liverpool")
                .team2Name("Everton")
                .sportSlug("football")
                .build();

        AiStrategyVote v1 = AiStrategyVote.builder().id(1L).recommendationId(102L).strategySlug("nerves_shield").build();
        AiStrategyVote v2 = AiStrategyVote.builder().id(2L).recommendationId(102L).strategySlug("high_odds").build();
        AiStrategyVote v3 = AiStrategyVote.builder().id(3L).recommendationId(102L).strategySlug("ultra_sniper").build();

        when(voteRepo.findByRecommendationId(102L)).thenReturn(List.of(v1, v2, v3));

        List<AiStrategyVote> result = service.generateStrategyVotes(rec, 1.85, "W1", null);

        verify(voteRepo, never()).saveAll(any());
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("recalculateSpecificStrategy purges old votes for given slug and generates new ones for missing recs")
    void testRecalculateSpecificStrategy_PurgesAndRecalculatesTargetSlug() {
        when(templateRepo.findBySlug("high_odds")).thenReturn(Optional.of(stratHighOdds));
        when(jdbcTemplate.update(eq("DELETE FROM ai_strategy_vote WHERE strategy_slug = ?"), eq("high_odds")))
                .thenReturn(5);

        // 1 recommendation is missing high_odds
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), eq("high_odds")))
                .thenReturn(List.of(201L));

        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(201L)
                .matchId(999L)
                .team1Name("Real Madrid")
                .team2Name("Barcelona")
                .sportSlug("football")
                .availableBookmakers("leon")
                .recommendedOdd(BigDecimal.valueOf(2.20))
                .recommendedMarket("TOTAL_OVER_2.5")
                .convictionTier("ML_80")
                .evaluationStage("PRE_LINEUP")
                .build();

        when(recRepo.findAllById(List.of(201L))).thenReturn(List.of(rec));
        when(voteRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.recalculateSpecificStrategy("high_odds", true);

        assertThat(result.get("status")).isEqualTo("SUCCESS");
        assertThat(result.get("purgedCount")).isEqualTo(5);
        assertThat(result.get("generatedCount")).isEqualTo(1);
        assertThat(result.get("strategySlug")).isEqualTo("high_odds");

        verify(jdbcTemplate).update(eq("DELETE FROM ai_strategy_vote WHERE strategy_slug = ?"), eq("high_odds"));
        verify(voteRepo).saveAll(any());
    }
}
