package pro.datawiki.igaming.source.vaidebet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetMatchOddsData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class VaidebetOddsMapperTest {

    private VaidebetOddsMapper mapper;
    private SportNormalizationService sportNormalizationService;
    private MatchCache footballMatch;
    private MatchCache cs2Match;

    @BeforeEach
    void setUp() {
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        when(sportNormalizationService.normalize(anyString())).thenAnswer(invocation -> {
            String sport = invocation.getArgument(0);
            if (sport == null) return SportType.FOOTBALL;
            String lower = sport.toLowerCase();
            if (lower.contains("cs") || lower.contains("counter-strike")) return SportType.CS2;
            if (lower.contains("dota")) return SportType.DOTA2;
            if (lower.contains("lol") || lower.contains("league of legends")) return SportType.LEAGUE_OF_LEGENDS;
            if (lower.contains("basketball")) return SportType.BASKETBALL;
            if (lower.contains("tennis")) return SportType.TENNIS;
            return SportType.FOOTBALL;
        });

        // Fallback default handlers constructor test
        mapper = new VaidebetOddsMapper(sportNormalizationService);

        footballMatch = new MatchCache();
        footballMatch.setId(1001L);
        footballMatch.setSportName("Football");
        footballMatch.setLeagueName("Brasileirão Serie A");
        footballMatch.setTeam1("Flamengo");
        footballMatch.setTeam2("Palmeiras");
        footballMatch.setIsLive(false);
        footballMatch.setStartTime(System.currentTimeMillis());

        cs2Match = new MatchCache();
        cs2Match.setId(2001L);
        cs2Match.setSportName("Counter-Strike");
        cs2Match.setLeagueName("ESL Pro League");
        cs2Match.setTeam1("Natus Vincere");
        cs2Match.setTeam2("FaZe Clan");
        cs2Match.setIsLive(true);
        cs2Match.setStartTime(System.currentTimeMillis());
    }

    // ==========================================
    // 2.5.1 Основные рынки
    // ==========================================

    @Test
    void testMainMarkets1X2AndTotalAndHandicap() {
        VaidebetMatchOddsData oddsData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        // 1X2 Match Result
                        VaidebetStakeGroupData.builder()
                                .id(1L)
                                .nameEn("Match Result")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(11L).nameEn("1").factor(2.10).build(),
                                        VaidebetStakeData.builder().id(12L).nameEn("X").factor(3.30).build(),
                                        VaidebetStakeData.builder().id(13L).nameEn("2").factor(3.20).build()
                                )).build(),
                        // Total Over/Under 2.5
                        VaidebetStakeGroupData.builder()
                                .id(3L)
                                .nameEn("Total")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(31L).nameEn("Over").argument(2.5).factor(1.85).build(),
                                        VaidebetStakeData.builder().id(32L).nameEn("Under").argument(2.5).factor(1.95).build()
                                )).build(),
                        // Handicap -0.5 / +0.5
                        VaidebetStakeGroupData.builder()
                                .id(2L)
                                .nameEn("Handicap")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(21L).nameEn("Handicap 1 (-0.5)").argument(0.5).factor(2.05).build(),
                                        VaidebetStakeData.builder().id(22L).nameEn("Handicap 2 (+0.5)").argument(0.5).factor(1.80).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);

        assertNotNull(request);
        assertEquals("vaidebet", request.getBookmaker());
        assertEquals("1001", request.getExternalEventId());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("https://vaidebet.com/line/sport/event/1001", request.getEventUrl());
        assertTrue(request.getRegions().contains(BookmakerRegion.BR));
        assertTrue(request.getRegions().contains(BookmakerRegion.LATAM));

        List<OddItem> odds = request.getOdds();
        assertEquals(7, odds.size());

        // Check 1X2
        OddItem w1 = odds.get(0);
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        // Check Total
        OddItem totOver = odds.get(3);
        assertTrue(totOver.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) totOver.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) totOver.getBetType()).param());

        // Check Handicap
        OddItem h1 = odds.get(5);
        assertTrue(h1.getBetType() instanceof HandicapBet);
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) h1.getBetType()).outcome());
        assertEquals(-0.5, ((HandicapBet) h1.getBetType()).param());
    }

    @Test
    void testHalvesAndPeriodsMarkets() {
        VaidebetMatchOddsData oddsData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        // 1st Half 1X2
                        VaidebetStakeGroupData.builder()
                                .id(4L)
                                .nameEn("1st Half Result")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(41L).nameEn("1").factor(2.60).build(),
                                        VaidebetStakeData.builder().id(42L).nameEn("X").factor(2.10).build(),
                                        VaidebetStakeData.builder().id(43L).nameEn("2").factor(3.80).build()
                                )).build(),
                        // 1st Half Total
                        VaidebetStakeGroupData.builder()
                                .id(6L)
                                .nameEn("1st Half Total")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(61L).nameEn("Over").argument(1.5).factor(2.20).build(),
                                        VaidebetStakeData.builder().id(62L).nameEn("Under").argument(1.5).factor(1.60).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(5, odds.size());

        MatchResultBet h1Bet = (MatchResultBet) odds.get(0).getBetType();
        assertEquals(BetScope.HALF_1, h1Bet.scope());

        TotalBet h1Tot = (TotalBet) odds.get(3).getBetType();
        assertEquals(BetScope.HALF_1, h1Tot.scope());
    }

    // ==========================================
    // 2.5.2 Роспись и статистика
    // ==========================================

    @Test
    void testDoubleChanceAndBtts() {
        VaidebetMatchOddsData oddsData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        // Double Chance
                        VaidebetStakeGroupData.builder()
                                .id(992L)
                                .nameEn("Double Chance")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(9921L).nameEn("1X").factor(1.28).build(),
                                        VaidebetStakeData.builder().id(9922L).nameEn("12").factor(1.30).build(),
                                        VaidebetStakeData.builder().id(9923L).nameEn("X2").factor(1.65).build()
                                )).build(),
                        // BTTS
                        VaidebetStakeGroupData.builder()
                                .id(10L)
                                .nameEn("Both Teams To Score")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(101L).nameEn("Yes").factor(1.75).build(),
                                        VaidebetStakeData.builder().id(102L).nameEn("No").factor(2.05).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(5, odds.size());

        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) odds.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) odds.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) odds.get(2).getBetType()).outcome());

        assertTrue(odds.get(3).getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bttsYes = (BinaryMarketBet) odds.get(3).getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bttsYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bttsYes.outcome());

        BinaryMarketBet bttsNo = (BinaryMarketBet) odds.get(4).getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bttsNo.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, bttsNo.outcome());
    }

    @Test
    void testStatsCornersAndCards() {
        VaidebetMatchOddsData oddsData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        // Corners Total
                        VaidebetStakeGroupData.builder()
                                .id(166L)
                                .nameEn("Corners Total")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(1661L).nameEn("Over").argument(9.5).factor(1.85).build(),
                                        VaidebetStakeData.builder().id(1662L).nameEn("Under").argument(9.5).factor(1.95).build()
                                )).build(),
                        // Yellow Cards Total
                        VaidebetStakeGroupData.builder()
                                .id(188L)
                                .nameEn("Yellow Cards Total")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(1881L).nameEn("Over").argument(4.5).factor(1.78).build(),
                                        VaidebetStakeData.builder().id(1882L).nameEn("Under").argument(4.5).factor(2.02).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(4, odds.size());

        TotalBet cornerBet = (TotalBet) odds.get(0).getBetType();
        assertEquals(StatType.CORNERS, cornerBet.statType());
        assertEquals(9.5, cornerBet.param());

        TotalBet cardBet = (TotalBet) odds.get(2).getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardBet.statType());
        assertEquals(4.5, cardBet.param());
    }

    // ==========================================
    // 2.5.3 Киберспорт с SportType
    // ==========================================

    @Test
    void testEsportsMapsAndRoundsWithSportTypeCS2() {
        VaidebetMatchOddsData oddsData = VaidebetMatchOddsData.builder()
                .matchId(2001L)
                .groups(List.of(
                        // Map 1 Winner
                        VaidebetStakeGroupData.builder()
                                .id(703L)
                                .nameEn("Map 1 Winner")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(7031L).nameEn("1").factor(1.70).build(),
                                        VaidebetStakeData.builder().id(7032L).nameEn("2").factor(2.15).build()
                                )).build(),
                        // Map Total
                        VaidebetStakeGroupData.builder()
                                .id(742L)
                                .nameEn("Map Total")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(7421L).nameEn("Over").argument(2.5).factor(2.05).build(),
                                        VaidebetStakeData.builder().id(7422L).nameEn("Under").argument(2.5).factor(1.75).build()
                                )).build(),
                        // Map 1 Rounds Total
                        VaidebetStakeGroupData.builder()
                                .nameEn("Map 1 Total Rounds")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(801L).nameEn("Over").argument(21.5).factor(1.85).build(),
                                        VaidebetStakeData.builder().id(802L).nameEn("Under").argument(21.5).factor(1.95).build()
                                )).build(),
                        // Map 1 Rounds Handicap
                        VaidebetStakeGroupData.builder()
                                .nameEn("Map 1 Round Handicap")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(811L).nameEn("Handicap 1 (-3.5)").argument(3.5).factor(1.90).build(),
                                        VaidebetStakeData.builder().id(812L).nameEn("Handicap 2 (+3.5)").argument(3.5).factor(1.90).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(8, odds.size());

        // Map 1 Winner
        MatchResultBet m1Bet = (MatchResultBet) odds.get(0).getBetType();
        assertEquals(BetScope.MAP_1, m1Bet.scope());
        assertEquals(StatType.MATCH, m1Bet.statType());

        // Map Total
        TotalBet mapTot = (TotalBet) odds.get(2).getBetType();
        assertEquals(BetScope.FULL_MATCH, mapTot.scope());
        assertEquals(StatType.MAPS, mapTot.statType());

        // Map 1 Rounds Total
        TotalBet rTot = (TotalBet) odds.get(4).getBetType();
        assertEquals(BetScope.MAP_1, rTot.scope());
        assertEquals(StatType.ROUNDS, rTot.statType());

        // Map 1 Rounds Handicap
        HandicapBet rHandicap = (HandicapBet) odds.get(6).getBetType();
        assertEquals(BetScope.MAP_1, rHandicap.scope());
        assertEquals(StatType.ROUNDS, rHandicap.statType());
    }

    // ==========================================
    // 2.5.4 Граничные случаи
    // ==========================================

    @Test
    void testEdgeCasesNullAndEmpty() {
        assertNull(mapper.mapToOddsUpdateRequest(null, new VaidebetMatchOddsData()));
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, null));
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, VaidebetMatchOddsData.builder().groups(List.of()).build()));

        // Group with empty stakes
        VaidebetMatchOddsData emptyStakesData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        VaidebetStakeGroupData.builder().id(1L).stakes(List.of()).build()
                )).build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, emptyStakesData));

        // Group with all invalid odds
        VaidebetMatchOddsData invalidOddsData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        VaidebetStakeGroupData.builder().id(1L).nameEn("Match Result").stakes(List.of(
                                VaidebetStakeData.builder().id(1L).nameEn("1").factor(1.0).build(),
                                VaidebetStakeData.builder().id(2L).nameEn("X").factor(null).build(),
                                VaidebetStakeData.builder().id(3L).nameEn("2").factor(0.8).build()
                        )).build()
                )).build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, invalidOddsData));
    }

    @Test
    void testUnsupportedMarketsAndFallbackConstructors() {
        // Fallback default constructor
        VaidebetOddsMapper defaultMapper = new VaidebetOddsMapper();
        assertNotNull(defaultMapper.getHandlers());
        assertEquals(8, defaultMapper.getHandlers().size());
        assertTrue(defaultMapper.supports("vaidebet", SportType.FOOTBALL));
        assertFalse(defaultMapper.supports("fonbet", SportType.FOOTBALL));

        // Unsupported market group (e.g. unknown custom market)
        VaidebetMatchOddsData unsupportedData = VaidebetMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(
                        VaidebetStakeGroupData.builder()
                                .id(99999L)
                                .nameEn("Will it rain tomorrow in stadium?")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(1L).nameEn("Yes").factor(2.5).build(),
                                        VaidebetStakeData.builder().id(2L).nameEn("No").factor(1.5).build()
                                )).build()
                )).build();

        assertNull(defaultMapper.mapToOddsUpdateRequest(footballMatch, unsupportedData));
    }

    @Test
    void testFallbackSportTypeResolution() {
        MatchCache dotaMatch = new MatchCache();
        dotaMatch.setId(3001L);
        dotaMatch.setSportName("Dota 2");
        dotaMatch.setTeam1("Team Spirit");
        dotaMatch.setTeam2("OG");

        VaidebetOddsMapper noServiceMapper = new VaidebetOddsMapper();
        VaidebetMatchOddsData data = VaidebetMatchOddsData.builder()
                .matchId(3001L)
                .groups(List.of(
                        VaidebetStakeGroupData.builder()
                                .id(703L)
                                .nameEn("Map 1 Winner")
                                .stakes(List.of(
                                        VaidebetStakeData.builder().id(7031L).nameEn("1").factor(1.8).build(),
                                        VaidebetStakeData.builder().id(7032L).nameEn("2").factor(2.0).build()
                                )).build()
                )).build();

        OddsUpdateRequest request = noServiceMapper.mapToOddsUpdateRequest(dotaMatch, data);
        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());
    }
}
