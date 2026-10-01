package pro.datawiki.igaming.source.tenbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TenBetOddsMapperTest {

    private TenBetOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TenBetOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("10bet", SportType.FOOTBALL));
        assertTrue(mapper.supports("10BET", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-100")
                .sportName("Football")
                .leagueName("Premier League")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .isLive(false)
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner 1X2")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Arsenal").decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("Draw").decimal(3.60).build(),
                                        TenBetOutcomeDto.builder().name("Chelsea").decimal(4.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("10bet", request.getBookmaker());
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
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-101")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool or Draw").decimal(1.40).build(),
                                        TenBetOutcomeDto.builder().name("Liverpool or Man City").decimal(1.30).build(),
                                        TenBetOutcomeDto.builder().name("Draw or Man City").decimal(1.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().contains("Draw") && o.getName().contains("Liverpool")).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().contains("Liverpool or Man City")).findFirst().orElseThrow();
        assertEquals(1.30, dc12.getValue());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().contains("Draw or Man City")).findFirst().orElseThrow();
        assertEquals(1.50, dcX2.getValue());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-102")
                .sportName("Football")
                .homeTeam("Barcelona")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Total Goals Over / Under")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(1.75).build(),
                                        TenBetOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.75, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());
        assertEquals(BetSubject.MATCH, overBet.subject());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().startsWith("Under")).findFirst().orElseThrow();
        assertEquals(2.10, under.getValue());
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testTeamTotals() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-103")
                .sportName("Football")
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Bayern Total Goals")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(1.60).build(),
                                        TenBetOutcomeDto.builder().name("Under 1.5").handicap(1.5).decimal(2.30).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().get(0);
        TotalBet tb = (TotalBet) over.getBetType();
        assertEquals(BetSubject.TEAM1, tb.subject());
        assertEquals(1.5, tb.param());
    }

    @Test
    void testHandicapSpread() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-104")
                .sportName("Basketball")
                .homeTeam("Lakers")
                .awayTeam("Celtics")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Point Spread")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Lakers -4.5").handicap(-4.5).decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Celtics +4.5").handicap(4.5).decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Lakers")).findFirst().orElseThrow();
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-4.5, hb1.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Celtics")).findFirst().orElseThrow();
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(4.5, hb2.param());
    }

    @Test
    void testFirstHalfScope() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-105")
                .sportName("Football")
                .homeTeam("PSG")
                .awayTeam("Marseille")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("1st Half Total Goals")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(2.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(1, request.getOdds().size());
        TotalBet tb = (TotalBet) request.getOdds().get(0).getBetType();
        assertEquals(BetScope.HALF_1, tb.scope());
    }

    @Test
    void testExtractTeamsFromTitle() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-106")
                .name("Inter vs Milan")
                .sportName("Soccer")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("1X2")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Inter").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("Inter", request.getTeam1());
        assertEquals("Milan", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
    }

    @Test
    void testSportResolution() {
        assertEquals(SportType.FOOTBALL, mapper.resolveSportType("Soccer"));
        assertEquals(SportType.BASKETBALL, mapper.resolveSportType("Basketball"));
        assertEquals(SportType.TENNIS, mapper.resolveSportType("Tennis"));
        assertEquals(SportType.CS2, mapper.resolveSportType("CS2"));
        assertEquals(SportType.DOTA2, mapper.resolveSportType("Dota 2"));
        assertEquals(SportType.LEAGUE_OF_LEGENDS, mapper.resolveSportType("League of Legends"));
        assertEquals(SportType.VALORANT, mapper.resolveSportType("Valorant"));
        assertEquals(SportType.UNKNOWN, mapper.resolveSportType(""));
    }

    @Test
    void testNullAndEmptyHandling() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        TenBetEventDto emptyEvent = TenBetEventDto.builder().id("ev-null").build();
        assertNull(mapper.mapToOddsUpdateRequest(emptyEvent));

        TenBetEventDto noMarketsEvent = TenBetEventDto.builder()
                .id("ev-empty")
                .homeTeam("Team A")
                .awayTeam("Team B")
                .build();
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(noMarketsEvent);
        assertNotNull(req);
        assertTrue(req.getOdds().isEmpty());
    }

    @Test
    void testCS2EsportsMarkets() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("cs2-1")
                .sportName("CS2")
                .leagueName("ESL Pro League")
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Natus Vincere").decimal(1.72).build(),
                                        TenBetOutcomeDto.builder().name("FaZe Clan").decimal(2.10).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Natus Vincere").decimal(1.80).build(),
                                        TenBetOutcomeDto.builder().name("FaZe Clan").decimal(2.00).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Total Maps")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(1.95).build(),
                                        TenBetOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(1.85).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map Handicap")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Natus Vincere -1.5").handicap(-1.5).decimal(3.10).build(),
                                        TenBetOutcomeDto.builder().name("FaZe Clan +1.5").handicap(1.5).decimal(1.35).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 - Total Rounds")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 21.5").handicap(21.5).decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Under 21.5").handicap(21.5).decimal(1.90).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 - Round Handicap")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Natus Vincere -2.5").handicap(-2.5).decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("FaZe Clan +2.5").handicap(2.5).decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals("Natus Vincere", request.getTeam1());
        assertEquals("FaZe Clan", request.getTeam2());

        List<OddItem> odds = request.getOdds();
        assertEquals(12, odds.size());

        // Match Winner
        OddItem mw1 = odds.stream().filter(o -> o.getGroupName().equals("esports_match_winner") && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertEquals(1.72, mw1.getValue());
        assertTrue(mw1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) mw1.getBetType()).outcome());
        assertEquals(BetScope.FULL_MATCH, ((MatchResultBet) mw1.getBetType()).scope());

        // Map 1 Winner
        OddItem m1w = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_winner") && o.getName().contains("FaZe")).findFirst().orElseThrow();
        assertEquals(2.00, m1w.getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) m1w.getBetType()).outcome());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) m1w.getBetType()).scope());

        // Total Maps
        OddItem tmOver = odds.stream().filter(o -> o.getGroupName().equals("esports_total_maps") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.95, tmOver.getValue());
        assertTrue(tmOver.getBetType() instanceof TotalBet);
        TotalBet tbMaps = (TotalBet) tmOver.getBetType();
        assertEquals(TotalBet.Direction.OVER, tbMaps.direction());
        assertEquals(2.5, tbMaps.param());
        assertEquals(StatType.MAPS, tbMaps.statType());

        // Map Handicap
        OddItem mh1 = odds.stream().filter(o -> o.getGroupName().equals("esports_map_handicap") && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertEquals(3.10, mh1.getValue());
        assertTrue(mh1.getBetType() instanceof HandicapBet);
        HandicapBet hbMaps = (HandicapBet) mh1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hbMaps.outcome());
        assertEquals(-1.5, hbMaps.param());
        assertEquals(StatType.MAPS, hbMaps.statType());

        // Map 1 Total Rounds
        OddItem trOver = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_total_rounds") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.90, trOver.getValue());
        TotalBet tbRounds = (TotalBet) trOver.getBetType();
        assertEquals(BetScope.MAP_1, tbRounds.scope());
        assertEquals(21.5, tbRounds.param());
        assertEquals(StatType.ROUNDS, tbRounds.statType());

        // Map 1 Round Handicap
        OddItem rh1 = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_round_handicap") && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertEquals(1.85, rh1.getValue());
        HandicapBet hbRounds = (HandicapBet) rh1.getBetType();
        assertEquals(BetScope.MAP_1, hbRounds.scope());
        assertEquals(-2.5, hbRounds.param());
        assertEquals(StatType.ROUNDS, hbRounds.statType());
    }

    @Test
    void testDota2EsportsMarkets() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("dota-1")
                .sportName("Dota 2")
                .leagueName("The International")
                .homeTeam("Team Spirit")
                .awayTeam("Team Liquid")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Team Spirit").decimal(1.65).build(),
                                        TenBetOutcomeDto.builder().name("Team Liquid").decimal(2.25).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 - First Blood")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Team Spirit").decimal(1.83).build(),
                                        TenBetOutcomeDto.builder().name("Team Liquid").decimal(1.92).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 Total Kills")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 48.5").handicap(48.5).decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("Under 48.5").handicap(48.5).decimal(1.95).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 Kill Handicap")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Team Spirit -5.5").handicap(-5.5).decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Team Liquid +5.5").handicap(5.5).decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(8, odds.size());

        // First Blood
        OddItem fb = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_first_blood") && o.getName().contains("Spirit")).findFirst().orElseThrow();
        assertEquals(1.83, fb.getValue());
        assertTrue(fb.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) fb.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
        assertEquals(BetScope.MAP_1, bmb.scope());

        // Map 1 Total Kills
        OddItem killsOver = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_total_kills") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.85, killsOver.getValue());
        TotalBet tbKills = (TotalBet) killsOver.getBetType();
        assertEquals(48.5, tbKills.param());
        assertEquals(StatType.KILLS, tbKills.statType());

        // Map 1 Kill Handicap
        OddItem killsHdp = odds.stream().filter(o -> o.getGroupName().equals("esports_map_1_kill_handicap") && o.getName().contains("Liquid")).findFirst().orElseThrow();
        assertEquals(1.90, killsHdp.getValue());
        HandicapBet hbKills = (HandicapBet) killsHdp.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hbKills.outcome());
        assertEquals(5.5, hbKills.param());
        assertEquals(StatType.KILLS, hbKills.statType());
    }

    @Test
    void testLoLEsportsMarkets() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("lol-1")
                .sportName("League of Legends")
                .leagueName("LCK")
                .homeTeam("T1")
                .awayTeam("Gen.G")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("T1").decimal(2.10).build(),
                                        TenBetOutcomeDto.builder().name("Gen.G").decimal(1.70).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("First Blood")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("T1").decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("Gen.G").decimal(1.85).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 1 Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("T1").decimal(2.05).build(),
                                        TenBetOutcomeDto.builder().name("Gen.G").decimal(1.75).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem fb = request.getOdds().stream().filter(o -> o.getGroupName().equals("esports_first_blood") && o.getName().equals("T1")).findFirst().orElseThrow();
        BinaryMarketBet bmb = (BinaryMarketBet) fb.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
        assertEquals(BetScope.FULL_MATCH, bmb.scope());
    }

    @Test
    void testValorantEsportsMarkets() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("val-1")
                .sportName("Valorant")
                .leagueName("VCT Champions")
                .homeTeam("Sentinels")
                .awayTeam("Fnatic")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Sentinels").decimal(1.95).build(),
                                        TenBetOutcomeDto.builder().name("Fnatic").decimal(1.85).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Total Maps Over/Under")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(2.05).build(),
                                        TenBetOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(1.75).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Map 2 Total Rounds")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 21.5").handicap(21.5).decimal(1.88).build(),
                                        TenBetOutcomeDto.builder().name("Under 21.5").handicap(21.5).decimal(1.92).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.VALORANT, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem m2Rounds = request.getOdds().stream().filter(o -> o.getGroupName().equals("esports_map_2_total_rounds") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        TotalBet tb = (TotalBet) m2Rounds.getBetType();
        assertEquals(BetScope.MAP_2, tb.scope());
        assertEquals(StatType.ROUNDS, tb.statType());
        assertEquals(21.5, tb.param());
    }

    @Test
    void testCornersMarkets() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-corners-1")
                .sportName("Football")
                .leagueName("Premier League")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Total Corners Over / Under")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 10.5").handicap(10.5).decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("Under 10.5").handicap(10.5).decimal(1.95).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Liverpool Total Corners")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 5.5").handicap(5.5).decimal(1.70).build(),
                                        TenBetOutcomeDto.builder().name("Under 5.5").handicap(5.5).decimal(2.10).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("1st Half Total Corners")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 4.5").handicap(4.5).decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Under 4.5").handicap(4.5).decimal(1.90).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Corners 1X2")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool").decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Draw").decimal(7.50).build(),
                                        TenBetOutcomeDto.builder().name("Man City").decimal(2.20).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Corners Handicap")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool -1.5").handicap(-1.5).decimal(2.05).build(),
                                        TenBetOutcomeDto.builder().name("Man City +1.5").handicap(1.5).decimal(1.75).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("First Corner")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool").decimal(1.80).build(),
                                        TenBetOutcomeDto.builder().name("Man City").decimal(2.00).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Corners Double Chance")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("1X").decimal(1.30).build(),
                                        TenBetOutcomeDto.builder().name("12").decimal(1.15).build(),
                                        TenBetOutcomeDto.builder().name("X2").decimal(1.50).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Corners Draw No Bet")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool").decimal(1.65).build(),
                                        TenBetOutcomeDto.builder().name("Man City").decimal(2.15).build()
                                ))
                                .build(),
                        TenBetMarketDto.builder()
                                .name("Corners Odd/Even")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Odd").decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Even").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.FOOTBALL, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(20, odds.size());

        // Total Corners
        OddItem cTotal = odds.stream().filter(o -> o.getGroupName().equals("corners_total") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.85, cTotal.getValue());
        TotalBet tb = (TotalBet) cTotal.getBetType();
        assertEquals(StatType.CORNERS, tb.statType());
        assertEquals(10.5, tb.param());
        assertEquals(BetSubject.MATCH, tb.subject());

        // Team Total Corners
        OddItem cTeamTotal = odds.stream().filter(o -> o.getGroupName().equals("corners_total_team1") && o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.70, cTeamTotal.getValue());
        TotalBet tbTeam = (TotalBet) cTeamTotal.getBetType();
        assertEquals(StatType.CORNERS, tbTeam.statType());
        assertEquals(BetSubject.TEAM1, tbTeam.subject());
        assertEquals(5.5, tbTeam.param());

        // 1st Half Corners
        OddItem c1h = odds.stream().filter(o -> o.getGroupName().equals("corners_total_half_1")).findFirst().orElseThrow();
        TotalBet tb1h = (TotalBet) c1h.getBetType();
        assertEquals(BetScope.HALF_1, tb1h.scope());
        assertEquals(StatType.CORNERS, tb1h.statType());

        // Corners 1X2
        OddItem c1x2 = odds.stream().filter(o -> o.getGroupName().equals("corners_1x2") && o.getName().equals("Liverpool")).findFirst().orElseThrow();
        assertEquals(1.90, c1x2.getValue());
        MatchResultBet mrb = (MatchResultBet) c1x2.getBetType();
        assertEquals(StatType.CORNERS, mrb.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());

        // Corners Handicap
        OddItem cHdp = odds.stream().filter(o -> o.getGroupName().equals("corners_handicap") && o.getName().contains("Liverpool")).findFirst().orElseThrow();
        assertEquals(2.05, cHdp.getValue());
        HandicapBet hb = (HandicapBet) cHdp.getBetType();
        assertEquals(StatType.CORNERS, hb.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-1.5, hb.param());

        // First Corner
        OddItem fc = odds.stream().filter(o -> o.getGroupName().equals("corners_first") && o.getName().equals("Liverpool")).findFirst().orElseThrow();
        assertEquals(1.80, fc.getValue());
        BinaryMarketBet bmb = (BinaryMarketBet) fc.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
        assertEquals(StatType.CORNERS, bmb.statType());

        // Corners Double Chance
        OddItem cDc = odds.stream().filter(o -> o.getGroupName().equals("corners_double_chance") && o.getName().equals("1X")).findFirst().orElseThrow();
        MatchResultBet dcBet = (MatchResultBet) cDc.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, dcBet.outcome());
        assertEquals(StatType.CORNERS, dcBet.statType());

        // Corners DNB
        OddItem cDnb = odds.stream().filter(o -> o.getGroupName().equals("corners_draw_no_bet") && o.getName().equals("Liverpool")).findFirst().orElseThrow();
        HandicapBet dnbBet = (HandicapBet) cDnb.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());
        assertEquals(0.0, dnbBet.param());
        assertEquals(StatType.CORNERS, dnbBet.statType());
    }
}
