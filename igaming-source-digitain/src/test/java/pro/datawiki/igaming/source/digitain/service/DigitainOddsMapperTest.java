package pro.datawiki.igaming.source.digitain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.digitain.dto.DigitainMatchOddsData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainBttsHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainDoubleChanceHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainEsportsHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainHandicapHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainMarketHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainMatchResultHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainStatsCardsHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainStatsCornersHandler;
import pro.datawiki.igaming.source.digitain.service.handler.DigitainTotalHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class DigitainOddsMapperTest {

    private SportNormalizationService sportNormalizationService;
    private DigitainOddsMapper mapper;
    private MatchCache footballMatch;
    private MatchCache cs2Match;

    @BeforeEach
    void setUp() {
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        Mockito.when(sportNormalizationService.normalize(anyString())).thenAnswer(invocation -> {
            String sport = invocation.getArgument(0);
            if (sport == null) return SportType.FOOTBALL;
            String lower = sport.toLowerCase();
            if (lower.contains("cs") || lower.contains("counter")) return SportType.CS2;
            if (lower.contains("dota")) return SportType.DOTA2;
            if (lower.contains("basket")) return SportType.BASKETBALL;
            return SportType.FOOTBALL;
        });

        // Pass handlers in reverse/unsorted order to verify @Order sorting in constructor
        List<DigitainMarketHandler> unsortedHandlers = Arrays.asList(
                new DigitainMatchResultHandler(),
                new DigitainHandicapHandler(),
                new DigitainTotalHandler(),
                new DigitainDoubleChanceHandler(),
                new DigitainBttsHandler(),
                new DigitainStatsCardsHandler(),
                new DigitainStatsCornersHandler(),
                new DigitainEsportsHandler()
        );

        mapper = new DigitainOddsMapper(sportNormalizationService, unsortedHandlers);

        footballMatch = new MatchCache();
        footballMatch.setId(1001L);
        footballMatch.setSportName("Football");
        footballMatch.setLeagueName("English Premier League");
        footballMatch.setTeam1("Arsenal");
        footballMatch.setTeam2("Chelsea");
        footballMatch.setIsLive(false);
        footballMatch.setStartTime(1720000000000L);

        cs2Match = new MatchCache();
        cs2Match.setId(2002L);
        cs2Match.setSportName("Counter-Strike 2");
        cs2Match.setLeagueName("ESL Pro League");
        cs2Match.setTeam1("Natus Vincere");
        cs2Match.setTeam2("FaZe Clan");
        cs2Match.setIsLive(false);
        cs2Match.setStartTime(1720000000000L);
    }

    // ==========================================
    // Общие проверки: Метаданные, @Order, supports
    // ==========================================

    @Test
    @DisplayName("Verify DigitainOddsMapper metadata, bookmaker support and handler sorting by @Order")
    void testMapperMetadataAndHandlerOrdering() {
        assertTrue(mapper.supports("digitain", SportType.FOOTBALL));
        assertTrue(mapper.supports("DIGITAIN", SportType.CS2));
        assertFalse(mapper.supports("pinnacle", SportType.FOOTBALL));
        assertFalse(mapper.supports("winline", SportType.FOOTBALL));
        assertNull(mapper.map("1", "1", null));

        List<DigitainMarketHandler> handlers = mapper.getHandlers();
        assertNotNull(handlers);
        assertEquals(8, handlers.size());

        // Expected order:
        // 10: Esports
        // 20: Corners
        // 30: Cards
        // 40: BTTS
        // 50: DoubleChance
        // 60: Total
        // 70: Handicap
        // 80: MatchResult
        assertInstanceOf(DigitainEsportsHandler.class, handlers.get(0));
        assertInstanceOf(DigitainStatsCornersHandler.class, handlers.get(1));
        assertInstanceOf(DigitainStatsCardsHandler.class, handlers.get(2));
        assertInstanceOf(DigitainBttsHandler.class, handlers.get(3));
        assertInstanceOf(DigitainDoubleChanceHandler.class, handlers.get(4));
        assertInstanceOf(DigitainTotalHandler.class, handlers.get(5));
        assertInstanceOf(DigitainHandicapHandler.class, handlers.get(6));
        assertInstanceOf(DigitainMatchResultHandler.class, handlers.get(7));
    }

    @Test
    @DisplayName("Verify OddsUpdateRequest header metadata mapping")
    void testOddsUpdateRequestHeaderMetadata() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        DigitainStakeData.builder().id(11L).nameEn("1").factor(2.10).build(),
                        DigitainStakeData.builder().id(12L).nameEn("X").factor(3.40).build(),
                        DigitainStakeData.builder().id(13L).nameEn("2").factor(3.20).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);

        assertNotNull(request);
        assertEquals("digitain", request.getBookmaker());
        assertEquals(List.of(BookmakerRegion.RU), request.getRegions());
        assertEquals("1001", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("English Premier League", request.getLeagueName());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertFalse(request.getIsLive());
        assertEquals(1720000000000L, request.getStartTime());
        assertEquals("https://melbet.ru/line/sport/event/1001", request.getEventUrl());
        assertEquals(3, request.getOdds().size());
    }

    // ==========================================
    // 2.7.1 Тесты основных рынков
    // ==========================================

    @Test
    @DisplayName("2.7.1 1X2 Full Match: W1, Draw, W2 (Group ID 1)")
    void testMatchResult1X2FullMatch() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .nameRu("Исход матча")
                .stakes(List.of(
                        DigitainStakeData.builder().id(101L).nameEn("1").factor(1.85).build(),
                        DigitainStakeData.builder().id(102L).nameEn("X").factor(3.60).build(),
                        DigitainStakeData.builder().id(103L).nameEn("2").factor(4.20).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(3, items.size());

        OddItem item1 = items.get(0);
        assertEquals("101", item1.getFactorId());
        assertEquals(1.85, item1.getValue());
        assertInstanceOf(MatchResultBet.class, item1.getBetType());
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        MatchResultBet betX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
    }

    @Test
    @DisplayName("2.7.1 Moneyline 2-Way: WIN1_2WAY, WIN2_2WAY (Group ID 702)")
    void testMoneyline2Way() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(702L)
                .nameEn("Winner")
                .nameRu("Победитель")
                .stakes(List.of(
                        DigitainStakeData.builder().id(201L).nameEn("Arsenal").factor(1.50).build(),
                        DigitainStakeData.builder().id(202L).nameEn("Chelsea").factor(2.60).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(2, items.size());

        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, b1.outcome());

        MatchResultBet b2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, b2.outcome());
    }

    @Test
    @DisplayName("2.7.1 Halves 1X2: 1st Half (Group 4) and 2nd Half (Group 7)")
    void testHalves1X2() {
        DigitainStakeGroupData half1Group = DigitainStakeGroupData.builder()
                .id(4L)
                .nameEn("1st Half - 1X2")
                .stakes(List.of(
                        DigitainStakeData.builder().id(401L).nameEn("1").factor(2.40).build(),
                        DigitainStakeData.builder().id(402L).nameEn("X").factor(2.10).build(),
                        DigitainStakeData.builder().id(403L).nameEn("2").factor(3.80).build()
                ))
                .build();

        DigitainStakeGroupData half2Group = DigitainStakeGroupData.builder()
                .id(7L)
                .nameEn("2nd Half - 1X2")
                .stakes(List.of(
                            DigitainStakeData.builder().id(701L).nameEn("1").factor(2.60).build(),
                            DigitainStakeData.builder().id(702L).nameEn("X").factor(2.20).build(),
                            DigitainStakeData.builder().id(703L).nameEn("2").factor(3.50).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(half1Group, half2Group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(6, items.size());

        MatchResultBet h1Win1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, h1Win1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, h1Win1.outcome());

        MatchResultBet h2Win2 = (MatchResultBet) items.get(5).getBetType();
        assertEquals(BetScope.HALF_2, h2Win2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, h2Win2.outcome());
    }

    @Test
    @DisplayName("2.7.1 Totals: Match Total (Group 3), Halves (Groups 6 & 9) and Individual Totals")
    void testTotals() {
        DigitainStakeGroupData matchTotalGroup = DigitainStakeGroupData.builder()
                .id(3L)
                .nameEn("Total Goals")
                .stakes(List.of(
                        DigitainStakeData.builder().id(301L).nameEn("Over").argument(2.5).factor(1.90).build(),
                        DigitainStakeData.builder().id(302L).nameEn("Under").argument(2.5).factor(1.95).build()
                ))
                .build();

        DigitainStakeGroupData half1TotalGroup = DigitainStakeGroupData.builder()
                .id(6L)
                .nameEn("1st Half Total Goals")
                .stakes(List.of(
                        DigitainStakeData.builder().id(601L).nameEn("Over").argument(1.0).factor(1.80).build(),
                        DigitainStakeData.builder().id(602L).nameEn("Under").argument(1.0).factor(2.00).build()
                ))
                .build();

        DigitainStakeGroupData indTotalGroup = DigitainStakeGroupData.builder()
                .id(3L)
                .nameEn("Team 1 Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(311L).nameEn("Over").argument(1.5).factor(1.75).build(),
                        DigitainStakeData.builder().id(312L).nameEn("Under").argument(1.5).factor(2.15).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(matchTotalGroup, half1TotalGroup, indTotalGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(6, items.size());

        // Match Total Over 2.5
        TotalBet mOver = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, mOver.scope());
        assertEquals(BetSubject.MATCH, mOver.subject());
        assertEquals(TotalBet.Direction.OVER, mOver.direction());
        assertEquals(2.5, mOver.param());
        assertEquals(StatType.MATCH, mOver.statType());

        // 1st Half Total Over 1.0
        TotalBet h1Over = (TotalBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, h1Over.scope());
        assertEquals(1.0, h1Over.param());

        // Team 1 Total Over 1.5
        TotalBet t1Over = (TotalBet) items.get(4).getBetType();
        assertEquals(BetScope.FULL_MATCH, t1Over.scope());
        assertEquals(BetSubject.TEAM1, t1Over.subject());
        assertEquals(1.5, t1Over.param());
    }

    @Test
    @DisplayName("2.7.1 Handicaps: Match Handicap (Group 2) and Halves (Groups 5 & 8)")
    void testHandicaps() {
        DigitainStakeGroupData matchHandicapGroup = DigitainStakeGroupData.builder()
                .id(2L)
                .nameEn("Handicap")
                .stakes(List.of(
                        DigitainStakeData.builder().id(201L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.30).build(),
                        DigitainStakeData.builder().id(202L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.65).build()
                ))
                .build();

        DigitainStakeGroupData half1HandicapGroup = DigitainStakeGroupData.builder()
                .id(5L)
                .nameEn("1st Half - Handicap")
                .stakes(List.of(
                        DigitainStakeData.builder().id(501L).nameEn("1 (-0.5)").argument(-0.5).factor(2.50).build(),
                        DigitainStakeData.builder().id(502L).nameEn("2 (+0.5)").argument(0.5).factor(1.55).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(matchHandicapGroup, half1HandicapGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(4, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertEquals(StatType.MATCH, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());

        HandicapBet halfH1 = (HandicapBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, halfH1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, halfH1.outcome());
        assertEquals(-0.5, halfH1.param());
    }

    @Test
    @DisplayName("2.7.1 BTTS (Both Teams To Score): Full Match (Group 46/10) and Half 1")
    void testBothTeamsToScore() {
        DigitainStakeGroupData bttsMatch = DigitainStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .nameRu("Обе команды забьют")
                .stakes(List.of(
                        DigitainStakeData.builder().id(461L).nameEn("Yes").factor(1.72).build(),
                        DigitainStakeData.builder().id(462L).nameEn("No").factor(2.10).build()
                ))
                .build();

        DigitainStakeGroupData bttsHalf = DigitainStakeGroupData.builder()
                .id(10L)
                .nameEn("1st Half - Both Teams To Score")
                .nameRu("1-й тайм - Обе забьют")
                .stakes(List.of(
                        DigitainStakeData.builder().id(101L).nameRu("Да").factor(4.50).build(),
                        DigitainStakeData.builder().id(102L).nameRu("Нет").factor(1.20).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(bttsMatch, bttsHalf))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(4, items.size());

        // Match BTTS Yes
        BinaryMarketBet yesMatch = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, yesMatch.scope());
        assertEquals(BetSubject.MATCH, yesMatch.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, yesMatch.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, yesMatch.outcome());
        assertEquals(StatType.MATCH, yesMatch.statType());

        // Match BTTS No
        BinaryMarketBet noMatch = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, noMatch.outcome());

        // Half 1 BTTS Yes
        BinaryMarketBet yesHalf = (BinaryMarketBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, yesHalf.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, yesHalf.outcome());
    }

    @Test
    @DisplayName("2.7.1 Double Chance: Full Match (Group 992) and 1st Half (Group 993)")
    void testDoubleChance() {
        DigitainStakeGroupData dcMatch = DigitainStakeGroupData.builder()
                .id(992L)
                .nameEn("Double Chance")
                .nameRu("Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(9921L).nameEn("1X").factor(1.25).build(),
                        DigitainStakeData.builder().id(9922L).nameEn("12").factor(1.30).build(),
                        DigitainStakeData.builder().id(9923L).nameEn("X2").factor(1.85).build()
                ))
                .build();

        DigitainStakeGroupData dcHalf = DigitainStakeGroupData.builder()
                .id(993L)
                .nameEn("1st Half - Double Chance")
                .nameRu("1-й тайм - Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(9931L).nameEn("1X").factor(1.15).build(),
                        DigitainStakeData.builder().id(9932L).nameEn("12").factor(1.45).build(),
                        DigitainStakeData.builder().id(9933L).nameEn("X2").factor(1.80).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(dcMatch, dcHalf))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(6, items.size());

        MatchResultBet dc1x = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, dc1x.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, dc1x.outcome());
        assertEquals(StatType.MATCH, dc1x.statType());

        MatchResultBet dc12 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, dc12.outcome());

        MatchResultBet dcX2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, dcX2.outcome());

        MatchResultBet halfDc1x = (MatchResultBet) items.get(3).getBetType();
        assertEquals(BetScope.HALF_1, halfDc1x.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, halfDc1x.outcome());
    }

    // ==========================================
    // 2.7.2 Тесты статистических рынков
    // ==========================================

    @Test
    @DisplayName("2.7.2 Corners: 1X2 (Group 168), Total (Group 166), Handicap (Group 167)")
    void testCornersMarkets() {
        DigitainStakeGroupData corners1X2 = DigitainStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners 1X2")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1681L).nameEn("1").factor(1.80).build(),
                        DigitainStakeData.builder().id(1682L).nameEn("X").factor(7.50).build(),
                        DigitainStakeData.builder().id(1683L).nameEn("2").factor(2.30).build()
                ))
                .build();

        DigitainStakeGroupData cornersTotal = DigitainStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1661L).nameEn("Over").argument(9.5).factor(1.85).build(),
                        DigitainStakeData.builder().id(1662L).nameEn("Under").argument(9.5).factor(1.95).build()
                ))
                .build();

        DigitainStakeGroupData cornersHandicap = DigitainStakeGroupData.builder()
                .id(167L)
                .nameEn("Corners Handicap")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1671L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.05).build(),
                        DigitainStakeData.builder().id(1672L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.75).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(corners1X2, cornersTotal, cornersHandicap))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(7, items.size());

        // Corners 1X2
        MatchResultBet c1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(StatType.CORNERS, c1.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, c1.outcome());

        MatchResultBet cX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(StatType.CORNERS, cX.statType());
        assertEquals(MatchResultBet.Outcome.DRAW, cX.outcome());

        // Corners Total
        TotalBet cTot = (TotalBet) items.get(3).getBetType();
        assertEquals(StatType.CORNERS, cTot.statType());
        assertEquals(TotalBet.Direction.OVER, cTot.direction());
        assertEquals(9.5, cTot.param());

        // Corners Handicap
        HandicapBet cHandicap = (HandicapBet) items.get(5).getBetType();
        assertEquals(StatType.CORNERS, cHandicap.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, cHandicap.outcome());
        assertEquals(-1.5, cHandicap.param());
    }

    @Test
    @DisplayName("2.7.2 Yellow Cards: 1X2 (Group 187), Total (Group 188), Handicap (Group 189)")
    void testYellowCardsMarkets() {
        DigitainStakeGroupData cards1X2 = DigitainStakeGroupData.builder()
                .id(187L)
                .nameEn("Yellow Cards 1X2")
                .nameRu("Желтые карточки 1X2")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1871L).nameEn("1").factor(2.10).build(),
                        DigitainStakeData.builder().id(1872L).nameEn("X").factor(4.50).build(),
                        DigitainStakeData.builder().id(1873L).nameEn("2").factor(2.40).build()
                ))
                .build();

        DigitainStakeGroupData cardsTotal = DigitainStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1881L).nameEn("Over").argument(3.5).factor(1.70).build(),
                        DigitainStakeData.builder().id(1882L).nameEn("Under").argument(3.5).factor(2.15).build()
                ))
                .build();

        DigitainStakeGroupData cardsHandicap = DigitainStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1891L).nameEn("Handicap 1 (0)").argument(0.0).factor(1.85).build(),
                        DigitainStakeData.builder().id(1892L).nameEn("Handicap 2 (0)").argument(0.0).factor(1.95).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(cards1X2, cardsTotal, cardsHandicap))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(7, items.size());

        // Cards 1X2
        MatchResultBet cardBet = (MatchResultBet) items.get(0).getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardBet.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, cardBet.outcome());

        // Cards Total
        TotalBet cardTot = (TotalBet) items.get(3).getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardTot.statType());
        assertEquals(TotalBet.Direction.OVER, cardTot.direction());
        assertEquals(3.5, cardTot.param());

        // Cards Handicap
        HandicapBet cardHcp = (HandicapBet) items.get(5).getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardHcp.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, cardHcp.outcome());
        assertEquals(0.0, cardHcp.param());
    }

    // ==========================================
    // 2.7.3 Тесты киберспортивных рынков
    // ==========================================

    @Test
    @DisplayName("2.7.3 Map Winners: Map 1 (Group 703), Map 2 (Group 704), Map 3 (Group 705)")
    void testMapWinners() {
        DigitainStakeGroupData map1 = DigitainStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7031L).nameEn("1").factor(1.85).build(),
                        DigitainStakeData.builder().id(7032L).nameEn("2").factor(1.95).build()
                ))
                .build();

        DigitainStakeGroupData map2 = DigitainStakeGroupData.builder()
                .id(704L)
                .nameEn("Map 2 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7041L).nameEn("1").factor(1.70).build(),
                        DigitainStakeData.builder().id(7042L).nameEn("2").factor(2.15).build()
                ))
                .build();

        DigitainStakeGroupData map3 = DigitainStakeGroupData.builder()
                .id(705L)
                .nameEn("Map 3 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7051L).nameEn("1").factor(1.90).build(),
                        DigitainStakeData.builder().id(7052L).nameEn("2").factor(1.90).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(2002L)
                .groups(List.of(map1, map2, map3))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(6, items.size());

        MatchResultBet m1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, m1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m1.outcome());

        MatchResultBet m2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.MAP_2, m2.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m2.outcome());

        MatchResultBet m3 = (MatchResultBet) items.get(4).getBetType();
        assertEquals(BetScope.MAP_3, m3.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m3.outcome());
    }

    @Test
    @DisplayName("2.7.3 Map Handicap (Groups 740/741) and Map Total (Group 742) with StatType.MAPS")
    void testMapHandicapAndTotal() {
        DigitainStakeGroupData mapHandicap = DigitainStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7401L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.60).build(),
                        DigitainStakeData.builder().id(7402L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.50).build()
                ))
                .build();

        DigitainStakeGroupData mapTotal = DigitainStakeGroupData.builder()
                .id(742L)
                .nameEn("Map Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7421L).nameEn("Over").argument(2.5).factor(2.10).build(),
                        DigitainStakeData.builder().id(7422L).nameEn("Under").argument(2.5).factor(1.70).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(2002L)
                .groups(List.of(mapHandicap, mapTotal))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(4, items.size());

        HandicapBet hcp = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, hcp.scope());
        assertEquals(StatType.MAPS, hcp.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hcp.outcome());
        assertEquals(-1.5, hcp.param());

        TotalBet tot = (TotalBet) items.get(2).getBetType();
        assertEquals(BetScope.FULL_MATCH, tot.scope());
        assertEquals(StatType.MAPS, tot.statType());
        assertEquals(TotalBet.Direction.OVER, tot.direction());
        assertEquals(2.5, tot.param());
    }

    @Test
    @DisplayName("2.7.3 Rounds Total: StatType.ROUNDS on Map 1")
    void testRoundsTotal() {
        DigitainStakeGroupData roundsTotal = DigitainStakeGroupData.builder()
                .id(810L)
                .nameEn("Map 1 Total Rounds")
                .nameRu("Тотал раундов 1 карта")
                .stakes(List.of(
                        DigitainStakeData.builder().id(8101L).nameEn("Over").argument(21.5).factor(1.85).build(),
                        DigitainStakeData.builder().id(8102L).nameEn("Under").argument(21.5).factor(1.95).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(2002L)
                .groups(List.of(roundsTotal))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(2, items.size());

        TotalBet rOver = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, rOver.scope());
        assertEquals(StatType.ROUNDS, rOver.statType());
        assertEquals(TotalBet.Direction.OVER, rOver.direction());
        assertEquals(21.5, rOver.param());
    }

    // ==========================================
    // 2.7.4 Тесты граничных случаев
    // ==========================================

    @Test
    @DisplayName("2.7.4 Null and empty inputs return null request")
    void testNullAndEmptyInputs() {
        DigitainMatchOddsData validData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(DigitainStakeGroupData.builder()
                        .id(1L)
                        .nameEn("1X2")
                        .stakes(List.of(DigitainStakeData.builder().nameEn("1").factor(1.5).build()))
                        .build()))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(null, validData));
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, null));

        DigitainMatchOddsData emptyGroupsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(new ArrayList<>())
                .build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, emptyGroupsData));

        DigitainMatchOddsData nullGroupsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(null)
                .build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, nullGroupsData));
    }

    @Test
    @DisplayName("2.7.4 Unsupported or unknown market ID and names produce no odds and return null request")
    void testUnsupportedMarketsIgnored() {
        DigitainStakeGroupData unknownGroup = DigitainStakeGroupData.builder()
                .id(999999L)
                .nameEn("Who will be the first player to touch the ball")
                .stakes(List.of(
                        DigitainStakeData.builder().id(9991L).nameEn("Player A").factor(5.5).build(),
                        DigitainStakeData.builder().id(9992L).nameEn("Player B").factor(6.0).build()
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(unknownGroup))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.7.4 Invalid odds factors (<= 1.0, null, negative) are filtered out")
    void testInvalidOddsFactorsFiltered() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        DigitainStakeData.builder().id(11L).nameEn("1").factor(1.0).build(), // factor == 1.0 invalid
                        DigitainStakeData.builder().id(12L).nameEn("X").factor(null).build(), // null factor invalid
                        DigitainStakeData.builder().id(13L).nameEn("2").factor(-1.5).build(), // negative factor invalid
                        DigitainStakeData.builder().id(14L).nameEn("1").factor(0.85).build() // factor < 1.0 invalid
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.7.4 Mixed valid and invalid stakes only retain valid odds")
    void testMixedValidAndInvalidStakes() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        DigitainStakeData.builder().id(11L).nameEn("1").factor(1.95).build(), // valid
                        DigitainStakeData.builder().id(12L).nameEn("X").factor(null).build(), // invalid
                        DigitainStakeData.builder().id(13L).nameEn("2").factor(3.80).build()  // valid
                ))
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());
        assertEquals("11", request.getOdds().get(0).getFactorId());
        assertEquals("13", request.getOdds().get(1).getFactorId());
    }

    @Test
    @DisplayName("2.7.4 Gracefully handles null or empty stakes list inside groups")
    void testNullOrEmptyStakesInGroup() {
        DigitainStakeGroupData emptyStakesGroup = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(new ArrayList<>())
                .build();

        DigitainStakeGroupData nullStakesGroup = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(null)
                .build();

        DigitainMatchOddsData oddsData = DigitainMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(emptyStakesGroup, nullStakesGroup))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.7.4 mapStakeGroup null safety")
    void testMapStakeGroupNullSafety() {
        List<OddItem> items = new ArrayList<>();
        assertDoesNotThrow(() -> mapper.mapStakeGroup(null, items, footballMatch, SportType.FOOTBALL));
        assertTrue(items.isEmpty());
    }
}
