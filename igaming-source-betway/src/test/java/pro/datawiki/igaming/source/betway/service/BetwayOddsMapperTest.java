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

        OddItem home = odds.stream().filter(o -> o.getOutcomeName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.85, home.getValue());
        assertTrue(home.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultOutcome.HOME_WIN, ((MatchResultBet) home.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> o.getOutcomeName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(3.60, draw.getValue());
        assertEquals(MatchResultOutcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem away = odds.stream().filter(o -> o.getOutcomeName().equals("Chelsea")).findFirst().orElseThrow();
        assertEquals(4.20, away.getValue());
        assertEquals(MatchResultOutcome.AWAY_WIN, ((MatchResultBet) away.getBetType()).outcome());
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

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getFactorId() == 1000101).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof DoubleChanceBet);
        assertEquals(DoubleChanceOutcome.HOME_OR_DRAW, ((DoubleChanceBet) dc1X.getBetType()).outcome());
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
                .filter(o -> o.getGroupName().equals("total") && o.getOutcomeName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.75, overMatch.getValue());
        TotalBet bet = (TotalBet) overMatch.getBetType();
        assertEquals(2.5, bet.total());
        assertEquals(TotalOutcome.OVER, bet.outcome());
        assertEquals(BetSubject.MATCH, bet.subject());

        OddItem overTeam1 = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total_team1") && o.getOutcomeName().contains("Over"))
                .findFirst().orElseThrow();
        assertEquals(1.65, overTeam1.getValue());
        TotalBet betT1 = (TotalBet) overTeam1.getBetType();
        assertEquals(1.5, betT1.total());
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
                .filter(o -> o.getGroupName().equals("handicap_home"))
                .findFirst().orElseThrow();
        assertEquals(2.10, h1.getValue());
        HandicapBet hBet = (HandicapBet) h1.getBetType();
        assertEquals(-1.5, hBet.handicap());
        assertEquals(BetSubject.TEAM1, hBet.subject());
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

        OddItem bttsYes = request.getOdds().stream().filter(o -> o.getGroupName().equals("btts") && o.getOutcomeName().equalsIgnoreCase("Yes")).findFirst().orElseThrow();
        assertEquals(1.80, bttsYes.getValue());
        assertTrue(bttsYes.getBetType() instanceof BinaryMarketBet);
        assertEquals(BinaryMarketOutcome.YES, ((BinaryMarketBet) bttsYes.getBetType()).outcome());

        OddItem dnb1 = request.getOdds().stream().filter(o -> o.getGroupName().equals("draw_no_bet") && o.getOutcomeName().equals("Juventus")).findFirst().orElseThrow();
        assertEquals(1.70, dnb1.getValue());
        assertEquals(1000104, dnb1.getFactorId());
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
        assertEquals(9.5, cornerBet.total());

        OddItem cardOver = request.getOdds().stream()
                .filter(o -> o.getGroupName().contains("cards"))
                .findFirst().orElseThrow();
        assertEquals(2.05, cardOver.getValue());
        TotalBet cardBet = (TotalBet) cardOver.getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardBet.statType());
        assertEquals(4.5, cardBet.total());
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
                .filter(o -> o.getGroupName().equals("esports_map1_winner") && o.getOutcomeName().equals("Natus Vincere"))
                .findFirst().orElseThrow();
        assertEquals(1.72, map1Winner.getValue());
        MatchResultBet mBet = (MatchResultBet) map1Winner.getBetType();
        assertEquals(BetScope.PERIOD_1, mBet.scope());
        assertEquals(MatchResultOutcome.HOME_WIN, mBet.outcome());

        OddItem mapHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_handicap_team1"))
                .findFirst().orElseThrow();
        assertEquals(2.60, mapHdc.getValue());
        HandicapBet hBet = (HandicapBet) mapHdc.getBetType();
        assertEquals(-1.5, hBet.handicap());
        assertEquals(StatType.MAPS, hBet.statType());
    }
}
