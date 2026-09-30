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

    @Test
    void testCornersMarkets() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-109")
                .sportName("Football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total Corners Over/Under 9.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 9.5").handicap(9.5).decimal(1.85).build(),
                                        BetanoOutcomeDto.builder().name("Under 9.5").handicap(9.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Corners 1X2")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.65).build(),
                                        BetanoOutcomeDto.builder().name("Draw").decimal(7.50).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.80).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Corners Handicap -1.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal (-1.5)").handicap(-1.5).decimal(1.90).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea (+1.5)").handicap(1.5).decimal(1.85).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Corners Double Chance")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("1X").decimal(1.20).build(),
                                        BetanoOutcomeDto.builder().name("12").decimal(1.15).build(),
                                        BetanoOutcomeDto.builder().name("X2").decimal(1.80).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Corners Draw No Bet")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.35).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.90).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Corners Odd/Even")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Odd").decimal(1.90).build(),
                                        BetanoOutcomeDto.builder().name("Even").decimal(1.85).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("First Corner")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.70).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.10).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Last Corner")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Arsenal").decimal(1.75).build(),
                                        BetanoOutcomeDto.builder().name("Chelsea").decimal(2.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // Total Corners
        OddItem overCorners = odds.stream().filter(o -> o.getGroupName().equals("corners_total") && o.getName().contains("Over")).findFirst().orElseThrow();
        assertEquals(1.85, overCorners.getValue());
        TotalBet tBet = (TotalBet) overCorners.getBetType();
        assertEquals(9.5, tBet.param());
        assertEquals(TotalBet.Direction.OVER, tBet.direction());
        assertEquals(StatType.CORNERS, tBet.statType());
        assertEquals(BetSubject.MATCH, tBet.subject());

        // Corners 1X2
        OddItem homeCorners = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2") && o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.65, homeCorners.getValue());
        MatchResultBet m1 = (MatchResultBet) homeCorners.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, m1.outcome());
        assertEquals(StatType.CORNERS, m1.statType());

        OddItem drawCorners = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2") && o.getName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(7.50, drawCorners.getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) drawCorners.getBetType()).outcome());

        // Corners Handicap
        OddItem hdpHome = odds.stream().filter(o -> o.getGroupName().equals("corners_handicap") && o.getName().contains("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.90, hdpHome.getValue());
        HandicapBet hBet = (HandicapBet) hdpHome.getBetType();
        assertEquals(-1.5, hBet.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hBet.outcome());
        assertEquals(StatType.CORNERS, hBet.statType());

        // Corners Double Chance
        OddItem dc1X = odds.stream().filter(o -> o.getGroupName().equals("corners_double_chance") && o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.20, dc1X.getValue());
        MatchResultBet dcBet = (MatchResultBet) dc1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, dcBet.outcome());
        assertEquals(StatType.CORNERS, dcBet.statType());

        // Corners Draw No Bet
        OddItem dnbHome = odds.stream().filter(o -> o.getGroupName().equals("corners_draw_no_bet") && o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.35, dnbHome.getValue());
        HandicapBet dnbBet = (HandicapBet) dnbHome.getBetType();
        assertEquals(0.0, dnbBet.param());
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());
        assertEquals(StatType.CORNERS, dnbBet.statType());

        // Corners Odd/Even
        OddItem odd = odds.stream().filter(o -> o.getGroupName().equals("corners_odd_even") && o.getName().equals("Odd")).findFirst().orElseThrow();
        assertEquals(1.90, odd.getValue());
        BinaryMarketBet bOdd = (BinaryMarketBet) odd.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, bOdd.marketType());
        assertEquals(StatType.CORNERS, bOdd.statType());

        // First / Last Corner
        OddItem first = odds.stream().filter(o -> o.getGroupName().equals("corners_first") && o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.70, first.getValue());
        BinaryMarketBet bFirst = (BinaryMarketBet) first.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bFirst.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bFirst.outcome());
        assertEquals(StatType.CORNERS, bFirst.statType());

        OddItem last = odds.stream().filter(o -> o.getGroupName().equals("corners_last") && o.getName().equals("Chelsea")).findFirst().orElseThrow();
        assertEquals(2.05, last.getValue());
        BinaryMarketBet bLast = (BinaryMarketBet) last.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CORNER, bLast.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bLast.outcome());
        assertEquals(StatType.CORNERS, bLast.statType());
    }

    @Test
    void testCornersMultilingualAndHalves() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-110")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total de Escanteios - 1º Tempo")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Mais de 4,5").decimal(1.80).build(),
                                        BetanoOutcomeDto.builder().name("Menos de 4,5").decimal(1.90).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Escanteios 1º Tempo - Vencedor")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Flamengo").decimal(1.75).build(),
                                        BetanoOutcomeDto.builder().name("Empate").decimal(4.50).build(),
                                        BetanoOutcomeDto.builder().name("Palmeiras").decimal(2.50).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Escanteios Handicap Asiático")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Flamengo (-0.5)").decimal(1.85).build(),
                                        BetanoOutcomeDto.builder().name("Palmeiras (+0.5)").decimal(1.95).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Flamengo - Total de Escanteios")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Mais de 5.5").decimal(1.70).build(),
                                        BetanoOutcomeDto.builder().name("Menos de 5.5").decimal(2.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // 1st Half Corners Total with comma parsing 4,5 -> 4.5
        OddItem overHalf = odds.stream().filter(o -> o.getGroupName().equals("corners_total_half_1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.80, overHalf.getValue());
        TotalBet tHalf = (TotalBet) overHalf.getBetType();
        assertEquals(4.5, tHalf.param());
        assertEquals(BetScope.HALF_1, tHalf.scope());
        assertEquals(StatType.CORNERS, tHalf.statType());

        // 1st Half Corners 1X2
        OddItem winHalf = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2_half_1") && o.getName().equals("Flamengo")).findFirst().orElseThrow();
        assertEquals(1.75, winHalf.getValue());
        MatchResultBet mHalf = (MatchResultBet) winHalf.getBetType();
        assertEquals(BetScope.HALF_1, mHalf.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mHalf.outcome());
        assertEquals(StatType.CORNERS, mHalf.statType());

        // Asian Handicap
        OddItem hdpAsian = odds.stream().filter(o -> o.getGroupName().equals("corners_handicap") && o.getName().contains("Flamengo")).findFirst().orElseThrow();
        assertEquals(1.85, hdpAsian.getValue());
        HandicapBet hBet = (HandicapBet) hdpAsian.getBetType();
        assertEquals(-0.5, hBet.param());
        assertTrue(hBet.isAsian());
        assertEquals(StatType.CORNERS, hBet.statType());

        // Team Total Corners
        OddItem t1Total = odds.stream().filter(o -> o.getGroupName().equals("corners_total_team1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.70, t1Total.getValue());
        TotalBet t1Bet = (TotalBet) t1Total.getBetType();
        assertEquals(5.5, t1Bet.param());
        assertEquals(BetSubject.TEAM1, t1Bet.subject());
        assertEquals(StatType.CORNERS, t1Bet.statType());
    }

    @Test
    void testYellowCardsMarkets() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-111")
                .sportName("Football")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total Yellow Cards Over/Under 4.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 4.5").handicap(4.5).decimal(1.80).build(),
                                        BetanoOutcomeDto.builder().name("Under 4.5").handicap(4.5).decimal(2.00).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Yellow Cards 1X2")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Real Madrid").decimal(2.20).build(),
                                        BetanoOutcomeDto.builder().name("Draw").decimal(4.20).build(),
                                        BetanoOutcomeDto.builder().name("Barcelona").decimal(2.50).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Yellow Cards Handicap -0.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Real Madrid (-0.5)").handicap(-0.5).decimal(2.10).build(),
                                        BetanoOutcomeDto.builder().name("Barcelona (+0.5)").handicap(0.5).decimal(1.70).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Yellow Cards Double Chance")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("1X").decimal(1.40).build(),
                                        BetanoOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        BetanoOutcomeDto.builder().name("X2").decimal(1.50).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Yellow Cards Draw No Bet")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Real Madrid").decimal(1.80).build(),
                                        BetanoOutcomeDto.builder().name("Barcelona").decimal(1.95).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Yellow Cards Odd/Even")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Odd").decimal(1.90).build(),
                                        BetanoOutcomeDto.builder().name("Even").decimal(1.85).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("First Yellow Card")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Real Madrid").decimal(1.90).build(),
                                        BetanoOutcomeDto.builder().name("Barcelona").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // Yellow Cards Total
        OddItem overYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_total") && o.getName().contains("Over")).findFirst().orElseThrow();
        assertEquals(1.80, overYellow.getValue());
        TotalBet yTot = (TotalBet) overYellow.getBetType();
        assertEquals(4.5, yTot.param());
        assertEquals(StatType.YELLOW_CARDS, yTot.statType());

        // Yellow Cards 1X2
        OddItem winYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_1x2") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(2.20, winYellow.getValue());
        MatchResultBet y1x2 = (MatchResultBet) winYellow.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, y1x2.outcome());
        assertEquals(StatType.YELLOW_CARDS, y1x2.statType());

        // Yellow Cards Handicap
        OddItem hdpYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_handicap") && o.getName().contains("Real Madrid")).findFirst().orElseThrow();
        assertEquals(2.10, hdpYellow.getValue());
        HandicapBet yHdp = (HandicapBet) hdpYellow.getBetType();
        assertEquals(-0.5, yHdp.param());
        assertEquals(StatType.YELLOW_CARDS, yHdp.statType());

        // Yellow Cards Double Chance
        OddItem dcYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_double_chance") && o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.40, dcYellow.getValue());
        MatchResultBet yDc = (MatchResultBet) dcYellow.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, yDc.outcome());
        assertEquals(StatType.YELLOW_CARDS, yDc.statType());

        // Yellow Cards Draw No Bet
        OddItem dnbYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_draw_no_bet") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(1.80, dnbYellow.getValue());
        HandicapBet yDnb = (HandicapBet) dnbYellow.getBetType();
        assertEquals(0.0, yDnb.param());
        assertEquals(StatType.YELLOW_CARDS, yDnb.statType());

        // Yellow Cards Odd/Even
        OddItem oddYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_odd_even") && o.getName().equals("Odd")).findFirst().orElseThrow();
        assertEquals(1.90, oddYellow.getValue());
        BinaryMarketBet yOdd = (BinaryMarketBet) oddYellow.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, yOdd.marketType());
        assertEquals(StatType.YELLOW_CARDS, yOdd.statType());

        // First Yellow Card
        OddItem firstYellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_first") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(1.90, firstYellow.getValue());
        BinaryMarketBet yFirst = (BinaryMarketBet) firstYellow.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CARD, yFirst.marketType());
        assertEquals(StatType.YELLOW_CARDS, yFirst.statType());
    }

    @Test
    void testTotalCardsAndRedCardMarkets() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-112")
                .sportName("Football")
                .homeTeam("Inter Milan")
                .awayTeam("AC Milan")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total Cards Over/Under 5.5")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Over 5.5").handicap(5.5).decimal(1.85).build(),
                                        BetanoOutcomeDto.builder().name("Under 5.5").handicap(5.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Cards 1X2")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Inter Milan").decimal(2.10).build(),
                                        BetanoOutcomeDto.builder().name("Draw").decimal(4.50).build(),
                                        BetanoOutcomeDto.builder().name("AC Milan").decimal(2.60).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Cards Handicap")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Inter Milan (-1.0)").handicap(-1.0).decimal(2.40).build(),
                                        BetanoOutcomeDto.builder().name("AC Milan (+1.0)").handicap(1.0).decimal(1.55).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Red Card in Match")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Yes").decimal(3.80).build(),
                                        BetanoOutcomeDto.builder().name("No").decimal(1.25).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Cartão Vermelho - Sim/Não")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Sim").decimal(4.00).build(),
                                        BetanoOutcomeDto.builder().name("Não").decimal(1.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // General Cards Total (StatType.CARDS)
        OddItem overCards = odds.stream().filter(o -> o.getGroupName().equals("cards_total") && o.getName().contains("Over")).findFirst().orElseThrow();
        assertEquals(1.85, overCards.getValue());
        TotalBet cTot = (TotalBet) overCards.getBetType();
        assertEquals(5.5, cTot.param());
        assertEquals(StatType.CARDS, cTot.statType());

        // General Cards 1X2 (StatType.CARDS)
        OddItem winCards = odds.stream().filter(o -> o.getGroupName().equals("cards_1x2") && o.getName().equals("Inter Milan")).findFirst().orElseThrow();
        assertEquals(2.10, winCards.getValue());
        MatchResultBet c1x2 = (MatchResultBet) winCards.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, c1x2.outcome());
        assertEquals(StatType.CARDS, c1x2.statType());

        // General Cards Handicap (StatType.CARDS)
        OddItem hdpCards = odds.stream().filter(o -> o.getGroupName().equals("cards_handicap") && o.getName().contains("Inter Milan")).findFirst().orElseThrow();
        assertEquals(2.40, hdpCards.getValue());
        HandicapBet cHdp = (HandicapBet) hdpCards.getBetType();
        assertEquals(-1.0, cHdp.param());
        assertEquals(StatType.CARDS, cHdp.statType());

        // Red Card
        OddItem redYes = odds.stream().filter(o -> o.getGroupName().equals("red_card") && o.getName().equals("Yes")).findFirst().orElseThrow();
        assertEquals(3.80, redYes.getValue());
        BinaryMarketBet redBet = (BinaryMarketBet) redYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, redBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, redBet.outcome());
        assertEquals(StatType.CARDS, redBet.statType());

        // Red Card in Portuguese
        OddItem redSim = odds.stream().filter(o -> o.getGroupName().equals("red_card") && o.getName().equals("Sim")).findFirst().orElseThrow();
        assertEquals(4.00, redSim.getValue());
        BinaryMarketBet redSimBet = (BinaryMarketBet) redSim.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, redSimBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, redSimBet.outcome());
    }

    @Test
    void testCardsMultilingualAndHalves() {
        BetanoEventDto event = BetanoEventDto.builder()
                .id("ev-113")
                .sportName("Futebol")
                .homeTeam("Porto")
                .awayTeam("Benfica")
                .markets(List.of(
                        BetanoMarketDto.builder()
                                .name("Total de Cartões Amarelos - 1º Tempo")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Mais de 1,5").decimal(1.75).build(),
                                        BetanoOutcomeDto.builder().name("Menos de 1,5").decimal(2.00).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Cartões Amarelos 1º Tempo - Vencedor")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Porto").decimal(2.30).build(),
                                        BetanoOutcomeDto.builder().name("Empate").decimal(2.60).build(),
                                        BetanoOutcomeDto.builder().name("Benfica").decimal(2.80).build()
                                ))
                                .build(),
                        BetanoMarketDto.builder()
                                .name("Porto - Total de Cartões Amarelos")
                                .outcomes(List.of(
                                        BetanoOutcomeDto.builder().name("Mais de 2.5").decimal(1.80).build(),
                                        BetanoOutcomeDto.builder().name("Menos de 2.5").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // 1st Half Yellow Cards Total with comma parsing
        OddItem overHalf = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_total_half_1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.75, overHalf.getValue());
        TotalBet yHalf = (TotalBet) overHalf.getBetType();
        assertEquals(1.5, yHalf.param());
        assertEquals(BetScope.HALF_1, yHalf.scope());
        assertEquals(StatType.YELLOW_CARDS, yHalf.statType());

        // 1st Half Yellow Cards 1X2
        OddItem winHalf = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_1x2_half_1") && o.getName().equals("Porto")).findFirst().orElseThrow();
        assertEquals(2.30, winHalf.getValue());
        MatchResultBet mHalf = (MatchResultBet) winHalf.getBetType();
        assertEquals(BetScope.HALF_1, mHalf.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mHalf.outcome());
        assertEquals(StatType.YELLOW_CARDS, mHalf.statType());

        // Team Total Yellow Cards
        OddItem t1Yellow = odds.stream().filter(o -> o.getGroupName().equals("yellow_cards_total_team1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.80, t1Yellow.getValue());
        TotalBet t1Bet = (TotalBet) t1Yellow.getBetType();
        assertEquals(2.5, t1Bet.param());
        assertEquals(BetSubject.TEAM1, t1Bet.subject());
        assertEquals(StatType.YELLOW_CARDS, t1Bet.statType());
    }
}

