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
}
