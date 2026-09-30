package pro.datawiki.igaming.source.betano.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetanoOddsMapperTest {

    private BetanoOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetanoOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betano", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETANO", SportType.CS2));
        assertFalse(mapper.supports("pinnacle", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-100")
                .sportName("Football")
                .leagueName("Premier League")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .isLive(false)
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Match Result")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.85).outcomeType("OT_ONE").build(),
                                        BetanoOutcomeDto.builder().name("Draw").decimal(3.60).outcomeType("OT_DRAW").build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(4.20).outcomeType("OT_TWO").build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betano", request.getBookmaker());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertTrue(request.getRegions().contains(BookmakerRegion.GLOBAL));
        assertTrue(request.getRegions().contains(BookmakerRegion.LATAM));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem home = odds.stream().filter(o -> o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.85, home.getValue());
        assertTrue(home.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) home.getBetType()).outcome());
        assertEquals(BetScope.FULL_MATCH, ((MatchResultBet) home.getBetType()).scope());

        OddItem draw = odds.stream().filter(o -> o.getName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(3.60, draw.getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem away = odds.stream().filter(o -> o.getName().equals("Chelsea")).findFirst().orElseThrow();
        assertEquals(4.20, away.getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) away.getBetType()).outcome());
    }

    @Test
    void testDoubleChance() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-101")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Liverpool or Draw").decimal(1.40).build(),
                                        BetanoOutcomeDto.builder().name("Liverpool or Man City").decimal(1.30).build(),
                                        BetanoOutcomeDto.builder().name("Draw or Man City").decimal(1.50).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("1st Half Double Chance")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("1X").decimal(1.22).build(),
                                        BetanoOutcomeDto.builder().name("12").decimal(1.45).build(),
                                        BetanoOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().equals("Liverpool or Draw")).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());
        assertEquals(BetScope.FULL_MATCH, ((MatchResultBet) dc1X.getBetType()).scope());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().equals("Liverpool or Man City")).findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().equals("Draw or Man City")).findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());

        OddItem dcHalf1X = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("double_chance_half_1") && o.getName().equals("1X"))
                .findFirst().orElseThrow();
        assertEquals(1.22, dcHalf1X.getValue());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) dcHalf1X.getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dcHalf1X.getBetType()).outcome());
    }

    @Test
    void testDrawNoBet() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-102")
                .sportName("Football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Draw No Bet")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.65).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.25).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("1st Half Draw No Bet")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.70).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.15).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem dnbMatchHome = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("draw_no_bet") && o.getName().equals("Arsenal"))
                .findFirst().orElseThrow();
        assertEquals(1.65, dnbMatchHome.getValue());
        assertTrue(dnbMatchHome.getBetType() instanceof HandicapBet);
        HandicapBet hMatch = (HandicapBet) dnbMatchHome.getBetType();
        assertEquals(0.0, hMatch.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hMatch.outcome());
        assertEquals(BetScope.FULL_MATCH, hMatch.scope());

        OddItem dnbHalfHome = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("draw_no_bet_half_1") && o.getName().equals("Arsenal"))
                .findFirst().orElseThrow();
        assertEquals(1.70, dnbHalfHome.getValue());
        HandicapBet hHalf = (HandicapBet) dnbHalfHome.getBetType();
        assertEquals(0.0, hHalf.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hHalf.outcome());
        assertEquals(BetScope.HALF_1, hHalf.scope());
    }

    @Test
    void testBothTeamsToScore() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-103")
                .sportName("Football")
                .homeTeam("Barcelona")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Both Teams To Score")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Yes").decimal(1.75).build(),
                                        BetanoOutcomeDto.builder().name("No").decimal(2.05).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("1st Half Both Teams To Score")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Yes").decimal(4.00).build(),
                                        BetanoOutcomeDto.builder().name("No").decimal(1.22).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Both Teams to Score in Both Halves")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Yes").decimal(11.00).build(),
                                        BetanoOutcomeDto.builder().name("No").decimal(1.04).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        OddItem bttsYes = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("btts") && o.getName().equals("Yes"))
                .findFirst().orElseThrow();
        assertEquals(1.75, bttsYes.getValue());
        BinaryMarketBet b1 = (BinaryMarketBet) bttsYes.getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, b1.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, b1.outcome());

        OddItem btts1hYes = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("btts_half_1") && o.getName().equals("Yes"))
                .findFirst().orElseThrow();
        assertEquals(4.00, btts1hYes.getValue());
        BinaryMarketBet b1h = (BinaryMarketBet) btts1hYes.getBetType();
        assertEquals(BetScope.HALF_1, b1h.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, b1h.marketType());

        OddItem bttsBhYes = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("btts_both_halves") && o.getName().equals("Yes"))
                .findFirst().orElseThrow();
        assertEquals(11.00, bttsBhYes.getValue());
        BinaryMarketBet bBh = (BinaryMarketBet) bttsBhYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BOTH_HALVES_BTTS, bBh.marketType());
    }

    @Test
    void testTotalOverUnder() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-104")
                .sportName("Football")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total Goals Over/Under 2.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(1.75).build(),
                                        BetanoOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(2.10).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Real Madrid Total Goals Over/Under 1.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(1.65).build(),
                                        BetanoOutcomeDto.builder().name("Under 1.5").handicap(1.5).decimal(2.25).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("1st Half Total Over/Under 1.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(2.30).build(),
                                        BetanoOutcomeDto.builder().name("Under 1.5").handicap(1.5).decimal(1.60).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        OddItem overMatch = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total") && o.getName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.75, overMatch.getValue());
        TotalBet bet = (TotalBet) overMatch.getBetType();
        assertEquals(2.5, bet.param());
        assertEquals(TotalBet.Direction.OVER, bet.direction());
        assertEquals(BetSubject.MATCH, bet.subject());
        assertEquals(BetScope.FULL_MATCH, bet.scope());

        OddItem overTeam1 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total_team1") && o.getName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.65, overTeam1.getValue());
        TotalBet betT1 = (TotalBet) overTeam1.getBetType();
        assertEquals(1.5, betT1.param());
        assertEquals(BetSubject.TEAM1, betT1.subject());

        OddItem overHalf = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total_half_1") && o.getName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(2.30, overHalf.getValue());
        TotalBet betHalf = (TotalBet) overHalf.getBetType();
        assertEquals(1.5, betHalf.param());
        assertEquals(BetScope.HALF_1, betHalf.scope());
    }

    @Test
    void testHandicapMarket() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-105")
                .sportName("Football")
                .homeTeam("Bayern Munich")
                .awayTeam("Dortmund")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Handicap -1.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Bayern Munich (-1.5)").handicap(-1.5).decimal(2.10).build(),
                                        BetanoOutcomeDto.builder().name("Dortmund (+1.5)").handicap(1.5).decimal(1.72).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("handicap") && o.getName().contains("Bayern"))
                .findFirst().orElseThrow();
        assertEquals(2.10, h1.getValue());
        HandicapBet hBet = (HandicapBet) h1.getBetType();
        assertEquals(-1.5, hBet.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hBet.outcome());
        assertEquals(BetScope.FULL_MATCH, hBet.scope());

        OddItem h2 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("handicap") && o.getName().contains("Dortmund"))
                .findFirst().orElseThrow();
        assertEquals(1.72, h2.getValue());
        HandicapBet hBet2 = (HandicapBet) h2.getBetType();
        assertEquals(1.5, hBet2.param());
        assertEquals(HandicapBet.Outcome.TEAM2, hBet2.outcome());
    }

    @Test
    void testCorrectScoreMarket() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-106")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Correct Score")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("1 - 0").decimal(8.50).build(),
                                        BetanoOutcomeDto.builder().name("2 - 1").decimal(9.00).build(),
                                        BetanoOutcomeDto.builder().name("0 - 0").decimal(11.00).build(),
                                        BetanoOutcomeDto.builder().name("Any Other Score").decimal(5.50).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("1st Half Correct Score")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("0 - 0").decimal(3.00).build(),
                                        BetanoOutcomeDto.builder().name("Man City 0 - 1").decimal(4.20).build(),
                                        BetanoOutcomeDto.builder().name("Other").decimal(15.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(7, request.getOdds().size());

        OddItem cs21 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("correct_score") && o.getName().equals("2 - 1"))
                .findFirst().orElseThrow();
        assertEquals(9.00, cs21.getValue());
        CorrectScoreBet csBet = (CorrectScoreBet) cs21.getBetType();
        assertEquals(2, csBet.score1());
        assertEquals(1, csBet.score2());
        assertFalse(csBet.isAnyOtherScore());
        assertEquals(BetScope.FULL_MATCH, csBet.scope());

        OddItem aos = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("correct_score") && o.getName().equals("Any Other Score"))
                .findFirst().orElseThrow();
        assertEquals(5.50, aos.getValue());
        CorrectScoreBet aosBet = (CorrectScoreBet) aos.getBetType();
        assertTrue(aosBet.isAnyOtherScore());

        OddItem cs1h00 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("correct_score_half_1") && o.getName().equals("0 - 0"))
                .findFirst().orElseThrow();
        assertEquals(3.00, cs1h00.getValue());
        CorrectScoreBet cs1hBet = (CorrectScoreBet) cs1h00.getBetType();
        assertEquals(0, cs1hBet.score1());
        assertEquals(0, cs1hBet.score2());
        assertEquals(BetScope.HALF_1, cs1hBet.scope());
    }

    @Test
    void testHalfTimeFullTimeMarket() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-107")
                .sportName("Football")
                .homeTeam("Inter Milan")
                .awayTeam("AC Milan")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Half Time / Full Time")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("1/1").decimal(3.20).build(),
                                        BetanoOutcomeDto.builder().name("1/X").decimal(15.00).build(),
                                        BetanoOutcomeDto.builder().name("1/2").decimal(29.00).build(),
                                        BetanoOutcomeDto.builder().name("X/1").decimal(5.25).build(),
                                        BetanoOutcomeDto.builder().name("X/X").decimal(5.00).build(),
                                        BetanoOutcomeDto.builder().name("X/2").decimal(6.50).build(),
                                        BetanoOutcomeDto.builder().name("2/1").decimal(26.00).build(),
                                        BetanoOutcomeDto.builder().name("2/X").decimal(14.00).build(),
                                        BetanoOutcomeDto.builder().name("2/2").decimal(4.50).build(),
                                        BetanoOutcomeDto.builder().name("Inter Milan / Inter Milan").decimal(3.25).build(),
                                        BetanoOutcomeDto.builder().name("Draw / AC Milan").decimal(6.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(11, request.getOdds().size());

        List<OddItem> odds = request.getOdds();

        OddItem htft11 = odds.stream().filter(o -> o.getName().equals("1/1")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, ((HalfTimeFullTimeBet) htft11.getBetType()).outcome());

        OddItem htft1X = odds.stream().filter(o -> o.getName().equals("1/X")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_X, ((HalfTimeFullTimeBet) htft1X.getBetType()).outcome());

        OddItem htft12 = odds.stream().filter(o -> o.getName().equals("1/2")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W2, ((HalfTimeFullTimeBet) htft12.getBetType()).outcome());

        OddItem htftX1 = odds.stream().filter(o -> o.getName().equals("X/1")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W1, ((HalfTimeFullTimeBet) htftX1.getBetType()).outcome());

        OddItem htftXX = odds.stream().filter(o -> o.getName().equals("X/X")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_X, ((HalfTimeFullTimeBet) htftXX.getBetType()).outcome());

        OddItem htftX2 = odds.stream().filter(o -> o.getName().equals("X/2")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W2, ((HalfTimeFullTimeBet) htftX2.getBetType()).outcome());

        OddItem htft21 = odds.stream().filter(o -> o.getName().equals("2/1")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W1, ((HalfTimeFullTimeBet) htft21.getBetType()).outcome());

        OddItem htft2X = odds.stream().filter(o -> o.getName().equals("2/X")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_X, ((HalfTimeFullTimeBet) htft2X.getBetType()).outcome());

        OddItem htft22 = odds.stream().filter(o -> o.getName().equals("2/2")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W2, ((HalfTimeFullTimeBet) htft22.getBetType()).outcome());

        OddItem namedHomeHome = odds.stream().filter(o -> o.getName().equals("Inter Milan / Inter Milan")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, ((HalfTimeFullTimeBet) namedHomeHome.getBetType()).outcome());

        OddItem namedDrawAway = odds.stream().filter(o -> o.getName().equals("Draw / AC Milan")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W2, ((HalfTimeFullTimeBet) namedDrawAway.getBetType()).outcome());
    }

    @Test
    void testPeriodMarket() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-108")
                .sportName("Basketball")
                .homeTeam("Lakers")
                .awayTeam("Celtics")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Quarter 1 Winner")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Lakers").decimal(1.90).build(),
                                        BetanoOutcomeDto.builder().name("Celtics").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem q1 = request.getOdds().stream().filter(o -> o.getName().equals("Lakers")).findFirst().orElseThrow();
        assertEquals(1.90, q1.getValue());
        MatchResultBet mBet = (MatchResultBet) q1.getBetType();
        assertEquals(BetScope.QUARTER_1, mBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());
    }
}
