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
    private AiStrategyTemplate stratYouth;

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
                .targetTierScope("PRO_SENIOR")
                .minOdd(BigDecimal.valueOf(1.30))
                .maxOdd(BigDecimal.valueOf(1.65))
                .minProbabilityPct(BigDecimal.valueOf(88.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(false)
                .build();

        stratHighOdds = AiStrategyTemplate.builder()
                .slug("high_odds")
                .name("Высокий кэф")
                .targetTierScope("PRO_SENIOR")
                .minOdd(BigDecimal.valueOf(2.05))
                .maxOdd(BigDecimal.valueOf(2.85))
                .minProbabilityPct(BigDecimal.valueOf(55.0))
                .ladderStepRule("MAX_ODD_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        stratSniper = AiStrategyTemplate.builder()
                .slug("ultra_sniper")
                .name("Ультра-Снайпер")
                .targetTierScope("PRO_SENIOR")
                .minOdd(BigDecimal.valueOf(1.30))
                .maxOdd(BigDecimal.valueOf(1.50))
                .minProbabilityPct(BigDecimal.valueOf(95.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(false)
                .build();

        stratYouth = AiStrategyTemplate.builder()
                .slug("youth_and_amateur")
                .name("Молодёжь & Любители (Sandbox)")
                .targetTierScope("YOUTH_AMATEUR")
                .minOdd(BigDecimal.valueOf(1.50))
                .maxOdd(BigDecimal.valueOf(2.50))
                .minProbabilityPct(BigDecimal.valueOf(50.0))
                .ladderStepRule("SAFEST_ELIGIBLE")
                .pureWinAllowed(true)
                .build();

        when(templateRepo.findAllByOrderBySortOrderAsc())
                .thenReturn(List.of(stratNerves, stratHighOdds, stratSniper, stratYouth));

        service = new StrategyEngineService(templateRepo, userSettingRepo, voteRepo, recRepo, jdbcTemplate, entityManager, new pro.datawiki.igaming.analyst.service.tier.TournamentScopeClassifier());
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

        // Existing vote for nerves_shield and youth_and_amateur
        AiStrategyVote existingVote = AiStrategyVote.builder()
                .id(1L)
                .recommendationId(101L)
                .matchId(777L)
                .strategySlug("nerves_shield")
                .strategyName("Защита нервов")
                .market("1X")
                .voteDecision("PICK")
                .build();
        AiStrategyVote existingYouthVote = AiStrategyVote.builder()
                .id(2L)
                .recommendationId(101L)
                .matchId(777L)
                .strategySlug("youth_and_amateur")
                .strategyName("Молодёжь & Любители (Sandbox)")
                .market("PASS")
                .voteDecision("PASS")
                .build();

        when(voteRepo.findByRecommendationId(101L)).thenReturn(new ArrayList<>(List.of(existingVote, existingYouthVote)));
        when(voteRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<AiStrategyVote> result = service.generateStrategyVotes(rec, 1.85, "TOTAL_OVER_2.5", null);

        // Verify saveAll was called with exactly 2 new votes (high_odds and ultra_sniper)
        ArgumentCaptor<List<AiStrategyVote>> captor = ArgumentCaptor.forClass(List.class);
        verify(voteRepo).saveAll(captor.capture());
        List<AiStrategyVote> savedVotes = captor.getValue();

        assertThat(savedVotes).hasSize(2);
        List<String> savedSlugs = savedVotes.stream().map(AiStrategyVote::getStrategySlug).toList();
        assertThat(savedSlugs).containsExactlyInAnyOrder("high_odds", "ultra_sniper");
        assertThat(savedSlugs).doesNotContain("nerves_shield", "youth_and_amateur"); // already existed!

        // Combined result must contain all 4
        assertThat(result).hasSize(4);
        List<String> resultSlugs = result.stream().map(AiStrategyVote::getStrategySlug).toList();
        assertThat(resultSlugs).containsExactlyInAnyOrder("nerves_shield", "high_odds", "ultra_sniper", "youth_and_amateur");
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
        AiStrategyVote v4 = AiStrategyVote.builder().id(4L).recommendationId(102L).strategySlug("youth_and_amateur").build();

        when(voteRepo.findByRecommendationId(102L)).thenReturn(List.of(v1, v2, v3, v4));

        List<AiStrategyVote> result = service.generateStrategyVotes(rec, 1.85, "W1", null);

        verify(voteRepo, never()).saveAll(any());
        assertThat(result).hasSize(4);
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

    @Test
    @DisplayName("When match is youth (Tier 6), production strategies emit PASS with Tier scope reason and youth sandbox picks")
    void testScopeIsolation_WhenYouthFixture_ProductionStrategiesPassAndYouthStrategyPicks() {
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(301L)
                .matchId(1111L)
                .leagueName("Spain Youth League U19")
                .team1Name("Atletico Madrid U19")
                .team2Name("Real Madrid U19")
                .sportSlug("football")
                .availableBookmakers("leon")
                .recommendedOdd(BigDecimal.valueOf(1.95))
                .recommendedMarket("TOTAL_OVER_2.5")
                .convictionTier("ML_80")
                .evaluationStage("PRE_LINEUP")
                .expectedValuePct(BigDecimal.valueOf(8.5))
                .fairOver25(BigDecimal.valueOf(0.60))
                .build();

        List<AiStrategyVote> votes = service.buildStrategyVotes(rec, 1.95, "TOTAL_OVER_2.5", List.of(stratNerves, stratSniper, stratYouth), null);

        assertThat(votes).hasSize(3);

        AiStrategyVote nervesVote = votes.stream().filter(v -> "nerves_shield".equals(v.getStrategySlug())).findFirst().orElseThrow();
        assertThat(nervesVote.getVoteDecision()).isEqualTo("PASS");
        assertThat(nervesVote.getResultStatus()).isEqualTo("SKIPPED");
        assertThat(nervesVote.getMarketLabel()).contains("Tier 6-7: молодёжь/любители");

        AiStrategyVote sniperVote = votes.stream().filter(v -> "ultra_sniper".equals(v.getStrategySlug())).findFirst().orElseThrow();
        assertThat(sniperVote.getVoteDecision()).isEqualTo("PASS");
        assertThat(sniperVote.getResultStatus()).isEqualTo("SKIPPED");
        assertThat(sniperVote.getMarketLabel()).contains("Tier 6-7: молодёжь/любители");

        AiStrategyVote youthVote = votes.stream().filter(v -> "youth_and_amateur".equals(v.getStrategySlug())).findFirst().orElseThrow();
        assertThat(youthVote.getVoteDecision()).isEqualTo("PICK");
        assertThat(youthVote.getResultStatus()).isEqualTo("PENDING");
        assertThat(youthVote.getMarket()).isEqualTo("TOTAL_OVER_2.5");
    }

    @Test
    @DisplayName("When match is adult senior (Tier 1-5), youth sandbox emits PASS and production strategies evaluate")
    void testScopeIsolation_WhenAdultSeniorFixture_YouthStrategyPassesAndProductionStrategiesEvaluate() {
        MatchAiRecommendation rec = MatchAiRecommendation.builder()
                .id(302L)
                .matchId(2222L)
                .leagueName("Premier League")
                .team1Name("Liverpool")
                .team2Name("Arsenal")
                .sportSlug("football")
                .availableBookmakers("leon")
                .recommendedOdd(BigDecimal.valueOf(1.40))
                .recommendedMarket("1X")
                .convictionTier("ML_80")
                .evaluationStage("PRE_LINEUP")
                .expectedValuePct(BigDecimal.valueOf(26.0))
                .fairP1(BigDecimal.valueOf(0.65))
                .fairPx(BigDecimal.valueOf(0.25))
                .fairP2(BigDecimal.valueOf(0.10))
                .build();

        List<AiStrategyVote> votes = service.buildStrategyVotes(rec, 1.40, "1X", List.of(stratNerves, stratYouth), null);

        assertThat(votes).hasSize(2);

        AiStrategyVote youthVote = votes.stream().filter(v -> "youth_and_amateur".equals(v.getStrategySlug())).findFirst().orElseThrow();
        assertThat(youthVote.getVoteDecision()).isEqualTo("PASS");
        assertThat(youthVote.getResultStatus()).isEqualTo("SKIPPED");
        assertThat(youthVote.getMarketLabel()).contains("только для молодёжи/любителей");

        AiStrategyVote nervesVote = votes.stream().filter(v -> "nerves_shield".equals(v.getStrategySlug())).findFirst().orElseThrow();
        assertThat(nervesVote.getVoteDecision()).isEqualTo("PICK");
        assertThat(nervesVote.getResultStatus()).isEqualTo("PENDING");
    }
}
