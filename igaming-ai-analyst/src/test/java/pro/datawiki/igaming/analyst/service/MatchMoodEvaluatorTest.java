package pro.datawiki.igaming.analyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.mood.MatchMood;
import pro.datawiki.igaming.dto.mood.MatchMoodCardDto;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MatchMoodEvaluatorTest {

    private MatchMoodEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MatchMoodEvaluator();
    }

    @Test
    @DisplayName("Should detect GOAL_FIESTA when expected total xG is high")
    void testGoalFiestaMatch() {
        MatchMoodEvaluator.MatchContext ctx = MatchMoodEvaluator.MatchContext.builder()
                .matchId(101L)
                .matchSlug("hoffenheim-vs-rb-leipzig")
                .team1Name("Hoffenheim")
                .team2Name("RB Leipzig")
                .expectedTotalXg(3.45)
                .highEventRate(0.50)
                .oddsOver25(1.55)
                .hasBroadcast(true)
                .matchStartTime(LocalDateTime.now().plusHours(2))
                .build();

        Optional<MatchMoodCardDto> card = evaluator.evaluateForMood(ctx, MatchMood.GOAL_FIESTA);

        assertThat(card).isPresent();
        assertThat(card.get().getMatchedMood()).isEqualTo(MatchMood.GOAL_FIESTA);
        assertThat(card.get().getExcitementScore()).isGreaterThanOrEqualTo(8.0);
        assertThat(card.get().getViewingHook()).contains("суммарный xG 3.45");
        assertThat(card.get().getMoodBadgeLabel()).contains("Град голов");
    }

    @Test
    @DisplayName("Should detect GRUDGE_MATCH for derby with high friction and card strictness")
    void testGrudgeMatch() {
        MatchMoodEvaluator.MatchContext ctx = MatchMoodEvaluator.MatchContext.builder()
                .matchId(102L)
                .matchSlug("crvena-zvezda-vs-partizan")
                .team1Name("Crvena Zvezda")
                .team2Name("Partizan")
                .isDerby(true)
                .derbyFrictionIndex(0.92)
                .compositeCardBias(0.85)
                .refereeAvgYellowCards(5.8)
                .hasBroadcast(true)
                .matchStartTime(LocalDateTime.now().plusHours(1))
                .build();

        Optional<MatchMoodCardDto> card = evaluator.evaluateForMood(ctx, MatchMood.GRUDGE_MATCH);

        assertThat(card).isPresent();
        assertThat(card.get().getMatchedMood()).isEqualTo(MatchMood.GRUDGE_MATCH);
        assertThat(card.get().getExcitementScore()).isGreaterThanOrEqualTo(8.5);
        assertThat(card.get().getViewingHook()).contains("дерби");
    }

    @Test
    @DisplayName("Should detect UPSET_WATCH when underdog has huge motivation edge over favorite")
    void testUpsetWatchMatch() {
        MatchMoodEvaluator.MatchContext ctx = MatchMoodEvaluator.MatchContext.builder()
                .matchId(103L)
                .matchSlug("manchester-city-vs-luton-town")
                .team1Name("Manchester City")
                .team2Name("Luton Town")
                .oddsP1(1.22)
                .oddsP2(12.0)
                .winLeverageHome(0.20) // Man City rotated before UCL semi-final
                .winLeverageAway(0.95) // Luton fighting for Premier League survival
                .hasBroadcast(true)
                .matchStartTime(LocalDateTime.now().plusHours(3))
                .build();

        Optional<MatchMoodCardDto> card = evaluator.evaluateForMood(ctx, MatchMood.UPSET_WATCH);

        assertThat(card).isPresent();
        assertThat(card.get().getMatchedMood()).isEqualTo(MatchMood.UPSET_WATCH);
        assertThat(card.get().getExcitementScore()).isGreaterThanOrEqualTo(8.0);
        assertThat(card.get().getViewingHook()).contains("ловушка для фаворита");
    }

    @Test
    @DisplayName("Should detect LATE_CLUTCH when teams feature high late goal share and bench depth")
    void testLateClutchMatch() {
        MatchMoodEvaluator.MatchContext ctx = MatchMoodEvaluator.MatchContext.builder()
                .matchId(104L)
                .matchSlug("bayer-leverkusen-vs-stuttgart")
                .team1Name("Bayer Leverkusen")
                .team2Name("VfB Stuttgart")
                .lateGoalsRatio(0.38)
                .benchAttackDepth(0.82)
                .hasBroadcast(false)
                .matchStartTime(LocalDateTime.now().plusHours(4))
                .build();

        Optional<MatchMoodCardDto> card = evaluator.evaluateForMood(ctx, MatchMood.LATE_CLUTCH);

        assertThat(card).isPresent();
        assertThat(card.get().getMatchedMood()).isEqualTo(MatchMood.LATE_CLUTCH);
        assertThat(card.get().getExcitementScore()).isGreaterThanOrEqualTo(7.5);
        assertThat(card.get().getViewingHook()).contains("поздней драмы");
    }
}
