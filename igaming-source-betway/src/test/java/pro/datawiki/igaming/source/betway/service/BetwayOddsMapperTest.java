package pro.datawiki.igaming.source.betway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetwayOddsMapperTest {

    private BetwayOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetwayOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betway", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETWAY", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-100")
                .sportName("Football")
                .leagueName("Premier League")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .isLive(false)
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Match Winner 1X2")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Arsenal").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("Draw").decimal(3.60).build(),
                                        BetwayOutcomeDto.builder().name("Chelsea").decimal(4.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betway", request.getBookmaker());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertTrue(request.getRegions().contains(BookmakerRegion.EU));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem home = odds.stream().filter(o -> o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.85, home.getValue());
        assertTrue(home.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) home.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> o.getName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(3.60, draw.getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem away = odds.stream().filter(o -> o.getName().equals("Chelsea")).findFirst().orElseThrow();
        assertEquals(4.20, away.getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) away.getBetType()).outcome());
    }

    @Test
    void testDoubleChance() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-101")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Liverpool or Draw").decimal(1.40).build(),
                                        BetwayOutcomeDto.builder().name("Liverpool or Man City").decimal(1.30).build(),
                                        BetwayOutcomeDto.builder().name("Draw or Man City").decimal(1.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().equals("Liverpool or Draw")).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-102")
                .sportName("Football")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Total Goals Over/Under 2.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(1.75).build(),
                                        BetwayOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(2.10).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Real Madrid Total Goals Over/Under 1.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(1.65).build(),
                                        BetwayOutcomeDto.builder().name("Under 1.5").handicap(1.5).decimal(2.25).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem overMatch = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total") && o.getName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.75, overMatch.getValue());
        TotalBet bet = (TotalBet) overMatch.getBetType();
        assertEquals(2.5, bet.param());
        assertEquals(TotalBet.Direction.OVER, bet.direction());
        assertEquals(BetSubject.MATCH, bet.subject());

        OddItem overTeam1 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total_team1") && o.getName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.65, overTeam1.getValue());
        TotalBet betT1 = (TotalBet) overTeam1.getBetType();
        assertEquals(1.5, betT1.param());
        assertEquals(BetSubject.TEAM1, betT1.subject());
    }

    @Test
    void testHandicapMarket() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-103")
                .sportName("Football")
                .homeTeam("Bayern Munich")
                .awayTeam("Dortmund")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Handicap -1.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Bayern Munich (-1.5)").handicap(-1.5).decimal(2.10).build(),
                                        BetwayOutcomeDto.builder().name("Dortmund (+1.5)").handicap(1.5).decimal(1.72).build()
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
    }

    @Test
    void testBothTeamsToScoreAndDrawNoBet() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-104")
                .sportName("Football")
                .homeTeam("Juventus")
                .awayTeam("Inter Milan")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Both Teams To Score")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(1.80).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(1.95).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Draw No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Juventus").decimal(1.70).build(),
                                        BetwayOutcomeDto.builder().name("Inter Milan").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem bttsYes = request.getOdds().stream().filter(o -> o.getGroupName().equals("btts") && o.getName().equalsIgnoreCase("Yes")).findFirst().orElseThrow();
        assertEquals(1.80, bttsYes.getValue());
        assertTrue(bttsYes.getBetType() instanceof BinaryMarketBet);
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) bttsYes.getBetType()).outcome());

        OddItem dnb1 = request.getOdds().stream().filter(o -> o.getGroupName().equals("draw_no_bet") && o.getName().equals("Juventus")).findFirst().orElseThrow();
        assertEquals(1.70, dnb1.getValue());
        assertTrue(dnb1.getBetType() instanceof HandicapBet);
        HandicapBet dnbBet = (HandicapBet) dnb1.getBetType();
        assertEquals(0.0, dnbBet.param());
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());
    }

    @Test
    void testCornersAndCardsStatistics() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-105")
                .sportName("Football")
                .homeTeam("PSG")
                .awayTeam("Marseille")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Total Corners Over/Under 9.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 9.5").handicap(9.5).decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("Under 9.5").handicap(9.5).decimal(1.85).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Total Cards Over/Under 4.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 4.5").handicap(4.5).decimal(2.05).build(),
                                        BetwayOutcomeDto.builder().name("Under 4.5").handicap(4.5).decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem cornerOver = request.getOdds().stream()
                .filter(o -> o.getGroupName().contains("corners"))
                .findFirst().orElseThrow();
        assertEquals(1.90, cornerOver.getValue());
        TotalBet cornerBet = (TotalBet) cornerOver.getBetType();
        assertEquals(StatType.CORNERS, cornerBet.statType());
        assertEquals(9.5, cornerBet.param());

        OddItem cardOver = request.getOdds().stream()
                .filter(o -> o.getGroupName().contains("cards"))
                .findFirst().orElseThrow();
        assertEquals(2.05, cardOver.getValue());
        TotalBet cardBet = (TotalBet) cardOver.getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardBet.statType());
        assertEquals(4.5, cardBet.param());
    }

    @Test
    void testCornersComprehensiveStatistics() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-corners-all")
                .sportName("Football")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Total Corners Over/Under 10.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 10.5").decimal(1.95).build(),
                                        BetwayOutcomeDto.builder().name("Under 10.5").decimal(1.80).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Total Corners 8.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over").decimal(1.40).build(),
                                        BetwayOutcomeDto.builder().name("Under").decimal(2.70).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Real Madrid Total Corners Over/Under 5.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 5.5").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("Under 5.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Total Corners Over/Under 4.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 4.5").decimal(1.75).build(),
                                        BetwayOutcomeDto.builder().name("Under 4.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Most Corners")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid").decimal(1.65).build(),
                                        BetwayOutcomeDto.builder().name("Draw").decimal(7.50).build(),
                                        BetwayOutcomeDto.builder().name("Barcelona").decimal(2.60).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Corners Handicap -1.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid (-1.5)").handicap(-1.5).decimal(2.10).build(),
                                        BetwayOutcomeDto.builder().name("Barcelona (+1.5)").handicap(1.5).decimal(1.68).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Corners Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid or Draw").decimal(1.30).build(),
                                        BetwayOutcomeDto.builder().name("Real Madrid or Barcelona").decimal(1.15).build(),
                                        BetwayOutcomeDto.builder().name("Draw or Barcelona").decimal(1.95).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Corners Draw No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid").decimal(1.45).build(),
                                        BetwayOutcomeDto.builder().name("Barcelona").decimal(2.55).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Total Corners Odd/Even")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Odd").decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("Even").decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("First Corner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid").decimal(1.70).build(),
                                        BetwayOutcomeDto.builder().name("Barcelona").decimal(2.10).build(),
                                        BetwayOutcomeDto.builder().name("No Corner").decimal(50.0).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Last Corner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Real Madrid").decimal(1.75).build(),
                                        BetwayOutcomeDto.builder().name("Barcelona").decimal(2.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        OddItem over10 = odds.stream().filter(o -> o.getGroupName().equals("corners_total") && o.getName().contains("10.5")).findFirst().orElseThrow();
        assertEquals(1.95, over10.getValue());
        TotalBet tb10 = (TotalBet) over10.getBetType();
        assertEquals(10.5, tb10.param());
        assertEquals(TotalBet.Direction.OVER, tb10.direction());
        assertEquals(StatType.CORNERS, tb10.statType());

        OddItem over8 = odds.stream().filter(o -> o.getGroupName().equals("corners_total") && o.getName().equals("Over")).findFirst().orElseThrow();
        assertEquals(1.40, over8.getValue());
        TotalBet tb8 = (TotalBet) over8.getBetType();
        assertEquals(8.5, tb8.param());

        OddItem overT1 = odds.stream().filter(o -> o.getGroupName().equals("corners_total_team1")).findFirst().orElseThrow();
        assertEquals(1.85, overT1.getValue());
        TotalBet tbT1 = (TotalBet) overT1.getBetType();
        assertEquals(5.5, tbT1.param());
        assertEquals(BetSubject.TEAM1, tbT1.subject());

        OddItem overH1 = odds.stream().filter(o -> o.getGroupName().equals("corners_total_half_1")).findFirst().orElseThrow();
        assertEquals(1.75, overH1.getValue());
        TotalBet tbH1 = (TotalBet) overH1.getBetType();
        assertEquals(BetScope.HALF_1, tbH1.scope());
        assertEquals(4.5, tbH1.param());

        OddItem w1 = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(1.65, w1.getValue());
        MatchResultBet mb1 = (MatchResultBet) w1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mb1.outcome());
        assertEquals(StatType.CORNERS, mb1.statType());

        OddItem draw = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2") && o.getName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(7.50, draw.getValue());
        MatchResultBet mbDraw = (MatchResultBet) draw.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, mbDraw.outcome());

        OddItem hdc = odds.stream().filter(o -> o.getGroupName().equals("corners_handicap") && o.getName().contains("Real Madrid")).findFirst().orElseThrow();
        assertEquals(2.10, hdc.getValue());
        HandicapBet hb = (HandicapBet) hdc.getBetType();
        assertEquals(-1.5, hb.param());
        assertEquals(StatType.CORNERS, hb.statType());

        OddItem dc1X = odds.stream().filter(o -> o.getGroupName().equals("corners_double_chance") && o.getName().contains("Draw")).findFirst().orElseThrow();
        assertEquals(1.30, dc1X.getValue());
        MatchResultBet mbDc = (MatchResultBet) dc1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, mbDc.outcome());
        assertEquals(StatType.CORNERS, mbDc.statType());

        OddItem dnb = odds.stream().filter(o -> o.getGroupName().equals("corners_draw_no_bet") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(1.45, dnb.getValue());
        HandicapBet dnbBet = (HandicapBet) dnb.getBetType();
        assertEquals(0.0, dnbBet.param());
        assertEquals(StatType.CORNERS, dnbBet.statType());

        OddItem odd = odds.stream().filter(o -> o.getGroupName().equals("corners_odd_even") && o.getName().equals("Odd")).findFirst().orElseThrow();
        assertEquals(1.90, odd.getValue());
        BinaryMarketBet bbOdd = (BinaryMarketBet) odd.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, bbOdd.marketType());
        assertEquals(BinaryMarketBet.Outcome.ODD, bbOdd.outcome());
        assertEquals(StatType.CORNERS, bbOdd.statType());

        OddItem first = odds.stream().filter(o -> o.getGroupName().equals("corners_first") && o.getName().equals("Real Madrid")).findFirst().orElseThrow();
        assertEquals(1.70, first.getValue());
        BinaryMarketBet bbFirst = (BinaryMarketBet) first.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bbFirst.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bbFirst.outcome());
        assertEquals(StatType.CORNERS, bbFirst.statType());

        OddItem last = odds.stream().filter(o -> o.getGroupName().equals("corners_last") && o.getName().equals("Barcelona")).findFirst().orElseThrow();
        assertEquals(2.05, last.getValue());
        BinaryMarketBet bbLast = (BinaryMarketBet) last.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CORNER, bbLast.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bbLast.outcome());
        assertEquals(StatType.CORNERS, bbLast.statType());
    }

    @Test
    void testCardsComprehensiveStatistics() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-cards-all")
                .sportName("Football")
                .homeTeam("Inter Milan")
                .awayTeam("AC Milan")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Total Cards Over/Under 4.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 4.5").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("Under 4.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Total Bookings 3.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over").decimal(1.50).build(),
                                        BetwayOutcomeDto.builder().name("Under").decimal(2.40).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("AC Milan Total Bookings Over/Under 2.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 2.5").decimal(2.00).build(),
                                        BetwayOutcomeDto.builder().name("Under 2.5").decimal(1.72).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Cards Over/Under 1.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 1.5").decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("Under 1.5").decimal(1.80).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Most Bookings")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan").decimal(2.20).build(),
                                        BetwayOutcomeDto.builder().name("Tie").decimal(4.50).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan").decimal(2.30).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Cards Handicap -0.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan (-0.5)").handicap(-0.5).decimal(2.15).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan (+0.5)").handicap(0.5).decimal(1.65).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Cards Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1X").decimal(1.40).build(),
                                        BetwayOutcomeDto.builder().name("12").decimal(1.25).build(),
                                        BetwayOutcomeDto.builder().name("X2").decimal(1.45).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Bookings Draw No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan").decimal(1.85).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Total Bookings Odd/Even")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Odd").decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("Even").decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Red Card - Yes/No")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(4.50).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(1.18).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("First Card")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan").decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan").decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Last Card")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan").decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        OddItem over4 = odds.stream().filter(o -> o.getGroupName().equals("cards_total") && o.getName().contains("4.5")).findFirst().orElseThrow();
        assertEquals(1.85, over4.getValue());
        TotalBet tb4 = (TotalBet) over4.getBetType();
        assertEquals(4.5, tb4.param());
        assertEquals(StatType.YELLOW_CARDS, tb4.statType());

        OddItem over3 = odds.stream().filter(o -> o.getGroupName().equals("cards_total") && o.getName().equals("Over")).findFirst().orElseThrow();
        assertEquals(1.50, over3.getValue());
        TotalBet tb3 = (TotalBet) over3.getBetType();
        assertEquals(3.5, tb3.param());
        assertEquals(StatType.YELLOW_CARDS, tb3.statType());

        OddItem overT2 = odds.stream().filter(o -> o.getGroupName().equals("cards_total_team2")).findFirst().orElseThrow();
        assertEquals(2.00, overT2.getValue());
        TotalBet tbT2 = (TotalBet) overT2.getBetType();
        assertEquals(2.5, tbT2.param());
        assertEquals(BetSubject.TEAM2, tbT2.subject());

        OddItem overH1 = odds.stream().filter(o -> o.getGroupName().equals("cards_total_half_1")).findFirst().orElseThrow();
        assertEquals(1.90, overH1.getValue());
        TotalBet tbH1 = (TotalBet) overH1.getBetType();
        assertEquals(BetScope.HALF_1, tbH1.scope());
        assertEquals(1.5, tbH1.param());

        OddItem w1 = odds.stream().filter(o -> o.getGroupName().equals("cards_1x2") && o.getName().equals("Inter Milan")).findFirst().orElseThrow();
        assertEquals(2.20, w1.getValue());
        MatchResultBet mb1 = (MatchResultBet) w1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mb1.outcome());
        assertEquals(StatType.YELLOW_CARDS, mb1.statType());

        OddItem tie = odds.stream().filter(o -> o.getGroupName().equals("cards_1x2") && o.getName().equals("Tie")).findFirst().orElseThrow();
        assertEquals(4.50, tie.getValue());
        MatchResultBet mbTie = (MatchResultBet) tie.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, mbTie.outcome());

        OddItem hdc = odds.stream().filter(o -> o.getGroupName().equals("cards_handicap") && o.getName().contains("Inter Milan")).findFirst().orElseThrow();
        assertEquals(2.15, hdc.getValue());
        HandicapBet hb = (HandicapBet) hdc.getBetType();
        assertEquals(-0.5, hb.param());
        assertEquals(StatType.YELLOW_CARDS, hb.statType());

        OddItem dc1X = odds.stream().filter(o -> o.getGroupName().equals("cards_double_chance") && o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        MatchResultBet mbDc = (MatchResultBet) dc1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, mbDc.outcome());
        assertEquals(StatType.YELLOW_CARDS, mbDc.statType());

        OddItem dnb = odds.stream().filter(o -> o.getGroupName().equals("cards_draw_no_bet") && o.getName().equals("Inter Milan")).findFirst().orElseThrow();
        assertEquals(1.85, dnb.getValue());
        HandicapBet dnbBet = (HandicapBet) dnb.getBetType();
        assertEquals(0.0, dnbBet.param());
        assertEquals(StatType.YELLOW_CARDS, dnbBet.statType());

        OddItem odd = odds.stream().filter(o -> o.getGroupName().equals("cards_odd_even") && o.getName().equals("Odd")).findFirst().orElseThrow();
        assertEquals(1.90, odd.getValue());
        BinaryMarketBet bbOdd = (BinaryMarketBet) odd.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, bbOdd.marketType());
        assertEquals(BinaryMarketBet.Outcome.ODD, bbOdd.outcome());
        assertEquals(StatType.YELLOW_CARDS, bbOdd.statType());

        OddItem redYes = odds.stream().filter(o -> o.getGroupName().equals("cards_red_card") && o.getName().equals("Yes")).findFirst().orElseThrow();
        assertEquals(4.50, redYes.getValue());
        BinaryMarketBet bbRed = (BinaryMarketBet) redYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, bbRed.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bbRed.outcome());
        assertEquals(StatType.YELLOW_CARDS, bbRed.statType());

        OddItem first = odds.stream().filter(o -> o.getGroupName().equals("cards_first") && o.getName().equals("Inter Milan")).findFirst().orElseThrow();
        assertEquals(1.90, first.getValue());
        BinaryMarketBet bbFirst = (BinaryMarketBet) first.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CARD, bbFirst.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bbFirst.outcome());
        assertEquals(StatType.YELLOW_CARDS, bbFirst.statType());

        OddItem last = odds.stream().filter(o -> o.getGroupName().equals("cards_last") && o.getName().equals("AC Milan")).findFirst().orElseThrow();
        assertEquals(1.90, last.getValue());
        BinaryMarketBet bbLast = (BinaryMarketBet) last.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CARD, bbLast.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bbLast.outcome());
        assertEquals(StatType.YELLOW_CARDS, bbLast.statType());
    }

    @Test
    void testEsportsCS2Markets() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-106")
                .sportName("CS2")
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Natus Vincere").decimal(1.65).build(),
                                        BetwayOutcomeDto.builder().name("FaZe Clan").decimal(2.25).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 1 Winner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Natus Vincere").decimal(1.72).build(),
                                        BetwayOutcomeDto.builder().name("FaZe Clan").decimal(2.05).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map Handicap -1.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Natus Vincere (-1.5)").handicap(-1.5).decimal(2.60).build(),
                                        BetwayOutcomeDto.builder().name("FaZe Clan (+1.5)").handicap(1.5).decimal(1.45).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        OddItem map1Winner = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_winner") && o.getName().equals("Natus Vincere"))
                .findFirst().orElseThrow();
        assertEquals(1.72, map1Winner.getValue());
        MatchResultBet mBet = (MatchResultBet) map1Winner.getBetType();
        assertEquals(BetScope.MAP_1, mBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());

        OddItem mapHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_handicap") && o.getName().contains("Natus"))
                .findFirst().orElseThrow();
        assertEquals(2.60, mapHdc.getValue());
        HandicapBet hBet = (HandicapBet) mapHdc.getBetType();
        assertEquals(-1.5, hBet.param());
        assertEquals(StatType.MAPS, hBet.statType());
    }

    @Test
    void testEsportsCS2RoundsAndHandicap() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-cs2-rounds")
                .sportName("CS2")
                .homeTeam("G2 Esports")
                .awayTeam("Vitality")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Map 1 Total Rounds Over/Under 21.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 21.5").handicap(21.5).decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("Under 21.5").handicap(21.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 1 Round Handicap -2.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("G2 Esports (-2.5)").handicap(-2.5).decimal(2.10).build(),
                                        BetwayOutcomeDto.builder().name("Vitality (+2.5)").handicap(2.5).decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals(4, request.getOdds().size());

        OddItem totalRounds = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_total_rounds") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.85, totalRounds.getValue());
        TotalBet rBet = (TotalBet) totalRounds.getBetType();
        assertEquals(BetScope.MAP_1, rBet.scope());
        assertEquals(21.5, rBet.param());
        assertEquals(StatType.ROUNDS, rBet.statType());

        OddItem roundHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_round_handicap") && o.getName().contains("G2"))
                .findFirst().orElseThrow();
        assertEquals(2.10, roundHdc.getValue());
        HandicapBet hBet = (HandicapBet) roundHdc.getBetType();
        assertEquals(BetScope.MAP_1, hBet.scope());
        assertEquals(-2.5, hBet.param());
        assertEquals(StatType.ROUNDS, hBet.statType());
    }

    @Test
    void testEsportsDota2Markets() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-dota2-1")
                .sportName("Dota 2")
                .leagueName("The International")
                .homeTeam("Team Spirit")
                .awayTeam("Team Liquid")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Game 1 Winner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Team Spirit").decimal(1.60).build(),
                                        BetwayOutcomeDto.builder().name("Team Liquid").decimal(2.30).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 1 Total Kills Over/Under 48.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 48.5").handicap(48.5).decimal(1.90).build(),
                                        BetwayOutcomeDto.builder().name("Under 48.5").handicap(48.5).decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Game 1 First Blood")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Team Spirit").decimal(1.80).build(),
                                        BetwayOutcomeDto.builder().name("Team Liquid").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem g1Winner = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_winner") && o.getName().equals("Team Spirit"))
                .findFirst().orElseThrow();
        assertEquals(1.60, g1Winner.getValue());
        MatchResultBet mBet = (MatchResultBet) g1Winner.getBetType();
        assertEquals(BetScope.MAP_1, mBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());

        OddItem killsOver = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_total_kills") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.90, killsOver.getValue());
        TotalBet kBet = (TotalBet) killsOver.getBetType();
        assertEquals(BetScope.MAP_1, kBet.scope());
        assertEquals(48.5, kBet.param());
        assertEquals(StatType.KILLS, kBet.statType());

        OddItem fb = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_first_blood") && o.getName().equals("Team Spirit"))
                .findFirst().orElseThrow();
        assertEquals(1.80, fb.getValue());
        BinaryMarketBet fbBet = (BinaryMarketBet) fb.getBetType();
        assertEquals(BetScope.MAP_1, fbBet.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, fbBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, fbBet.outcome());
    }

    @Test
    void testEsportsLoLMarkets() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-lol-1")
                .sportName("Esports")
                .leagueName("League of Legends LCK")
                .homeTeam("T1")
                .awayTeam("Gen.G")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Total Games Over/Under 2.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(2.00).build(),
                                        BetwayOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(1.75).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 1 First Blood")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, request.getSportType());
        assertEquals(4, request.getOdds().size());

        OddItem totalGames = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_total_maps") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(2.00, totalGames.getValue());
        TotalBet gBet = (TotalBet) totalGames.getBetType();
        assertEquals(StatType.MAPS, gBet.statType());
        assertEquals(2.5, gBet.param());

        OddItem fbYes = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_first_blood") && o.getName().equals("Yes"))
                .findFirst().orElseThrow();
        assertEquals(1.85, fbYes.getValue());
        BinaryMarketBet fbBet = (BinaryMarketBet) fbYes.getBetType();
        assertEquals(BinaryMarketBet.Outcome.YES, fbBet.outcome());
    }

    @Test
    void testEsportsValorantMarkets() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-val-1")
                .sportName("Valorant")
                .leagueName("VCT Masters")
                .homeTeam("Sentinels")
                .awayTeam("Paper Rex")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Sentinels").decimal(1.75).build(),
                                        BetwayOutcomeDto.builder().name("Paper Rex").decimal(2.10).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 1 Total Rounds Over/Under 22.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 22.5").handicap(22.5).decimal(1.80).build(),
                                        BetwayOutcomeDto.builder().name("Under 22.5").handicap(22.5).decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.VALORANT, request.getSportType());
        assertEquals(4, request.getOdds().size());

        OddItem mWin = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_match_winner") && o.getName().equals("Sentinels"))
                .findFirst().orElseThrow();
        assertEquals(1.75, mWin.getValue());
        MatchResultBet mBet = (MatchResultBet) mWin.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());
    }

    @Test
    void testComprehensiveDrawNoBet() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-dnb-all")
                .sportName("Football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Draw No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Arsenal").decimal(1.65).build(),
                                        BetwayOutcomeDto.builder().name("Chelsea").decimal(2.25).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Draw No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Arsenal").decimal(1.70).build(),
                                        BetwayOutcomeDto.builder().name("Chelsea").decimal(2.15).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Tie No Bet")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1").decimal(1.68).build(),
                                        BetwayOutcomeDto.builder().name("2").decimal(2.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        OddItem dnbMatchHome = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("draw_no_bet") && o.getName().equals("Arsenal"))
                .findFirst().orElseThrow();
        assertEquals(1.65, dnbMatchHome.getValue());
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

        OddItem tnbAway = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("draw_no_bet") && o.getName().equals("2"))
                .findFirst().orElseThrow();
        assertEquals(2.20, tnbAway.getValue());
        HandicapBet hTnb = (HandicapBet) tnbAway.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hTnb.outcome());
    }

    @Test
    void testComprehensiveBothTeamsToScore() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-btts-all")
                .sportName("Football")
                .homeTeam("Barcelona")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Both Teams To Score")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(1.75).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(2.05).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Both Teams To Score")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(4.00).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(1.22).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Both Teams to Score in Both Halves")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Yes").decimal(11.00).build(),
                                        BetwayOutcomeDto.builder().name("No").decimal(1.04).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Goal / Goal")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("GG").decimal(1.72).build(),
                                        BetwayOutcomeDto.builder().name("NG").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(8, request.getOdds().size());

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

        OddItem gg = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("btts") && o.getName().equals("GG"))
                .findFirst().orElseThrow();
        assertEquals(1.72, gg.getValue());
        BinaryMarketBet bGg = (BinaryMarketBet) gg.getBetType();
        assertEquals(BinaryMarketBet.Outcome.YES, bGg.outcome());
    }

    @Test
    void testComprehensiveCorrectScore() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-cs-all")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Correct Score")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1 - 0").decimal(8.50).build(),
                                        BetwayOutcomeDto.builder().name("2 - 1").decimal(9.00).build(),
                                        BetwayOutcomeDto.builder().name("0 - 0").decimal(11.00).build(),
                                        BetwayOutcomeDto.builder().name("1 - 2").decimal(9.50).build(),
                                        BetwayOutcomeDto.builder().name("Any Other Score").decimal(5.50).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Correct Score")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("0 - 0").decimal(3.00).build(),
                                        BetwayOutcomeDto.builder().name("1 - 0").decimal(4.50).build(),
                                        BetwayOutcomeDto.builder().name("Man City 0 - 1").decimal(4.20).build(),
                                        BetwayOutcomeDto.builder().name("Other").decimal(15.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(9, request.getOdds().size());

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

        OddItem cs1hOther = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("correct_score_half_1") && o.getName().equals("Other"))
                .findFirst().orElseThrow();
        assertTrue(((CorrectScoreBet) cs1hOther.getBetType()).isAnyOtherScore());
        assertEquals(BetScope.HALF_1, ((CorrectScoreBet) cs1hOther.getBetType()).scope());
    }

    @Test
    void testComprehensiveHalfTimeFullTime() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-htft-all")
                .sportName("Football")
                .homeTeam("Inter Milan")
                .awayTeam("AC Milan")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Half Time / Full Time")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1/1").decimal(3.20).build(),
                                        BetwayOutcomeDto.builder().name("1/X").decimal(15.00).build(),
                                        BetwayOutcomeDto.builder().name("1/2").decimal(29.00).build(),
                                        BetwayOutcomeDto.builder().name("X/1").decimal(5.25).build(),
                                        BetwayOutcomeDto.builder().name("X/X").decimal(5.00).build(),
                                        BetwayOutcomeDto.builder().name("X/2").decimal(6.50).build(),
                                        BetwayOutcomeDto.builder().name("2/1").decimal(26.00).build(),
                                        BetwayOutcomeDto.builder().name("2/X").decimal(14.00).build(),
                                        BetwayOutcomeDto.builder().name("2/2").decimal(4.50).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Double Result")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Inter Milan / Inter Milan").decimal(3.25).build(),
                                        BetwayOutcomeDto.builder().name("Inter Milan / Draw").decimal(14.50).build(),
                                        BetwayOutcomeDto.builder().name("Draw / AC Milan").decimal(6.20).build(),
                                        BetwayOutcomeDto.builder().name("AC Milan - Inter Milan").decimal(25.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(13, request.getOdds().size());

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

        OddItem namedAwayHome = odds.stream().filter(o -> o.getName().equals("AC Milan - Inter Milan")).findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W1, ((HalfTimeFullTimeBet) namedAwayHome.getBetType()).outcome());
    }

    @Test
    void testComprehensiveDoubleChance() {
        BetwayEventDto event = BetwayEventDto.builder()
                .id("ev-dc-all")
                .sportName("Football")
                .homeTeam("Bayern Munich")
                .awayTeam("Borussia Dortmund")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Bayern Munich or Draw").decimal(1.22).build(),
                                        BetwayOutcomeDto.builder().name("Bayern Munich or Borussia Dortmund").decimal(1.18).build(),
                                        BetwayOutcomeDto.builder().name("Draw or Borussia Dortmund").decimal(2.15).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("1st Half Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1X").decimal(1.15).build(),
                                        BetwayOutcomeDto.builder().name("12").decimal(1.45).build(),
                                        BetwayOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("1 or X").decimal(1.20).build(),
                                        BetwayOutcomeDto.builder().name("1 or 2").decimal(1.17).build(),
                                        BetwayOutcomeDto.builder().name("X or 2").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(9, request.getOdds().size());

        OddItem dcMatch1X = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("double_chance") && o.getName().equals("Bayern Munich or Draw"))
                .findFirst().orElseThrow();
        assertEquals(1.22, dcMatch1X.getValue());
        MatchResultBet m1X = (MatchResultBet) dcMatch1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, m1X.outcome());
        assertEquals(BetScope.FULL_MATCH, m1X.scope());

        OddItem dcMatch12 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("double_chance") && o.getName().contains("Borussia Dortmund") && o.getName().contains("Bayern"))
                .findFirst().orElseThrow();
        assertEquals(1.18, dcMatch12.getValue());
        MatchResultBet m12 = (MatchResultBet) dcMatch12.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, m12.outcome());

        OddItem dc1hX2 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("double_chance_half_1") && o.getName().equals("X2"))
                .findFirst().orElseThrow();
        assertEquals(1.65, dc1hX2.getValue());
        MatchResultBet m1hX2 = (MatchResultBet) dc1hX2.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, m1hX2.outcome());
        assertEquals(BetScope.HALF_1, m1hX2.scope());

        OddItem dc1orX = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("double_chance") && o.getName().equals("1 or X"))
                .findFirst().orElseThrow();
        assertEquals(1.20, dc1orX.getValue());
        MatchResultBet m1orX = (MatchResultBet) dc1orX.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, m1orX.outcome());
    }

    @Test
    void testEsportsExtendedScopesAndSecondHalfStatistics() {
        BetwayEventDto csEvent = BetwayEventDto.builder()
                .id("ev-cs-map2")
                .sportName("CS2")
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("Map 2 Winner")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("NaVi").decimal(1.70).build(),
                                        BetwayOutcomeDto.builder().name("FaZe").decimal(2.10).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("Map 3 Total Rounds Over/Under 21.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 21.5").decimal(1.85).build(),
                                        BetwayOutcomeDto.builder().name("Under 21.5").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest csRequest = mapper.mapToOddsUpdateRequest(csEvent);
        assertNotNull(csRequest);
        assertEquals(4, csRequest.getOdds().size());

        OddItem m2Winner = csRequest.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_2_winner") && o.getName().equals("NaVi"))
                .findFirst().orElseThrow();
        assertEquals(1.70, m2Winner.getValue());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) m2Winner.getBetType()).scope());

        OddItem m3Rounds = csRequest.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_3_total_rounds") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.85, m3Rounds.getValue());
        assertEquals(BetScope.MAP_3, ((TotalBet) m3Rounds.getBetType()).scope());

        BetwayEventDto footballEvent = BetwayEventDto.builder()
                .id("ev-fb-h2")
                .sportName("Football")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetwayMarketDto.builder()
                                .name("2nd Half Total Corners Over/Under 5.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 5.5").decimal(1.80).build(),
                                        BetwayOutcomeDto.builder().name("Under 5.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetwayMarketDto.builder()
                                .name("2nd Half Cards Over/Under 2.5")
                                .outcomes(List.of(
                                        BetwayOutcomeDto.builder().name("Over 2.5").decimal(2.05).build(),
                                        BetwayOutcomeDto.builder().name("Under 2.5").decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest fbRequest = mapper.mapToOddsUpdateRequest(footballEvent);
        assertNotNull(fbRequest);
        assertEquals(4, fbRequest.getOdds().size());

        OddItem c2h = fbRequest.getOdds().stream()
                .filter(o -> o.getGroupName().equals("corners_total_half_2") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.80, c2h.getValue());
        assertEquals(BetScope.HALF_2, ((TotalBet) c2h.getBetType()).scope());
        assertEquals(StatType.CORNERS, ((TotalBet) c2h.getBetType()).statType());

        OddItem card2h = fbRequest.getOdds().stream()
                .filter(o -> o.getGroupName().equals("cards_total_half_2") && o.getName().startsWith("Over"))
                .findFirst().orElseThrow();
        assertEquals(2.05, card2h.getValue());
        assertEquals(BetScope.HALF_2, ((TotalBet) card2h.getBetType()).scope());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) card2h.getBetType()).statType());
    }
}
