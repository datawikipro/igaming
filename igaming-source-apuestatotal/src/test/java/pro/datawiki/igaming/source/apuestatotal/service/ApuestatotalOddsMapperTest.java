package pro.datawiki.igaming.source.apuestatotal.service;

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
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalMatchOddsData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalBttsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalDoubleChanceHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalEsportsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalHandicapHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalMarketHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalMatchResultHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalStatsCardsHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalStatsCornersHandler;
import pro.datawiki.igaming.source.apuestatotal.service.handler.ApuestatotalTotalHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class ApuestatotalOddsMapperTest {

    private SportNormalizationService sportNormalizationService;
    private ApuestatotalOddsMapper mapper;
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
            if (lower.contains("league") || lower.contains("lol")) return SportType.LEAGUE_OF_LEGENDS;
            if (lower.contains("valoran")) return SportType.VALORANT;
            if (lower.contains("basket")) return SportType.BASKETBALL;
            return SportType.FOOTBALL;
        });

        // Pass handlers in reverse/unsorted order to verify @Order sorting in constructor
        List<ApuestatotalMarketHandler> unsortedHandlers = Arrays.asList(
                new ApuestatotalMatchResultHandler(),
                new ApuestatotalHandicapHandler(),
                new ApuestatotalTotalHandler(),
                new ApuestatotalDoubleChanceHandler(),
                new ApuestatotalBttsHandler(),
                new ApuestatotalStatsCardsHandler(),
                new ApuestatotalStatsCornersHandler(),
                new ApuestatotalEsportsHandler()
        );

        mapper = new ApuestatotalOddsMapper(sportNormalizationService, unsortedHandlers);

        footballMatch = new MatchCache();
        footballMatch.setId(1001L);
        footballMatch.setExternalId("1001");
        footballMatch.setSportName("Football");
        footballMatch.setLeagueName("Liga 1 Peru");
        footballMatch.setTeam1("Alianza Lima");
        footballMatch.setTeam2("Universitario");
        footballMatch.setIsLive(false);
        footballMatch.setStartTime(1720000000000L);

        cs2Match = new MatchCache();
        cs2Match.setId(2002L);
        cs2Match.setExternalId("2002");
        cs2Match.setSportName("Counter-Strike 2");
        cs2Match.setLeagueName("ESL Pro League");
        cs2Match.setTeam1("Natus Vincere");
        cs2Match.setTeam2("FaZe Clan");
        cs2Match.setIsLive(false);
        cs2Match.setStartTime(1720000000000L);
    }

    // ==========================================
    // Архитектура: Метаданные, @Order, supports
    // ==========================================

    @Test
    @DisplayName("Verify ApuestatotalOddsMapper metadata, bookmaker support and handler sorting by @Order")
    void testMapperMetadataAndHandlerOrdering() {
        assertTrue(mapper.supports("apuestatotal", SportType.FOOTBALL));
        assertTrue(mapper.supports("APUESTATOTAL", SportType.CS2));
        assertFalse(mapper.supports("pinnacle", SportType.FOOTBALL));
        assertFalse(mapper.supports("winline", SportType.FOOTBALL));
        assertNull(mapper.map("1", "1", null));

        List<ApuestatotalMarketHandler> handlers = mapper.getHandlers();
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
        assertInstanceOf(ApuestatotalEsportsHandler.class, handlers.get(0));
        assertInstanceOf(ApuestatotalStatsCornersHandler.class, handlers.get(1));
        assertInstanceOf(ApuestatotalStatsCardsHandler.class, handlers.get(2));
        assertInstanceOf(ApuestatotalBttsHandler.class, handlers.get(3));
        assertInstanceOf(ApuestatotalDoubleChanceHandler.class, handlers.get(4));
        assertInstanceOf(ApuestatotalTotalHandler.class, handlers.get(5));
        assertInstanceOf(ApuestatotalHandicapHandler.class, handlers.get(6));
        assertInstanceOf(ApuestatotalMatchResultHandler.class, handlers.get(7));
    }

    @Test
    @DisplayName("Verify default constructor with null handlers creates and sorts default handlers")
    void testDefaultConstructorCreatesHandlers() {
        ApuestatotalOddsMapper defaultMapper = new ApuestatotalOddsMapper(sportNormalizationService);
        List<ApuestatotalMarketHandler> handlers = defaultMapper.getHandlers();
        assertNotNull(handlers);
        assertEquals(8, handlers.size());
        assertInstanceOf(ApuestatotalEsportsHandler.class, handlers.get(0));
        assertInstanceOf(ApuestatotalMatchResultHandler.class, handlers.get(7));
    }

    @Test
    @DisplayName("Verify OddsUpdateRequest header metadata mapping")
    void testOddsUpdateRequestHeaderMetadata() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(11L).nameEn("1").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(12L).nameEn("X").factor(3.40).build(),
                        ApuestatotalStakeData.builder().id(13L).nameEn("2").factor(3.20).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);

        assertNotNull(request);
        assertEquals("apuestatotal", request.getBookmaker());
        assertEquals(List.of(BookmakerRegion.PE), request.getRegions());
        assertEquals("1001", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("Liga 1 Peru", request.getLeagueName());
        assertEquals("Alianza Lima", request.getTeam1());
        assertEquals("Universitario", request.getTeam2());
        assertFalse(request.getIsLive());
        assertEquals(1720000000000L, request.getStartTime());
        assertEquals("https://apuestatotal.com/deportes/evento/1001", request.getEventUrl());
        assertEquals(3, request.getOdds().size());
    }

    // ==========================================
    // 2.5.1 Тесты основных рынков (1X2, Moneyline, таймы, тоталы, форы)
    // ==========================================

    @Test
    @DisplayName("2.5.1 1X2 Full Match: W1, Draw, W2 (Group ID 1)")
    void testMatchResult1X2FullMatch() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .nameRu("Исход матча")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(101L).nameEn("1").factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(102L).nameEn("X").factor(3.60).build(),
                        ApuestatotalStakeData.builder().id(103L).nameEn("2").factor(4.20).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.1 1X2 Spanish Terms: Local, Empate, Visitante")
    void testMatchResultSpanishTerms() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Resultado final")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(104L).nameEn("Local").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(105L).nameEn("Empate").factor(3.20).build(),
                        ApuestatotalStakeData.builder().id(106L).nameEn("Visitante").factor(3.40).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) items.get(2).getBetType()).outcome());
    }

    @Test
    @DisplayName("2.5.1 Moneyline 2-Way: WIN1_2WAY, WIN2_2WAY (Group ID 702)")
    void testMoneyline2Way() {
        MatchCache basketMatch = new MatchCache();
        basketMatch.setId(1002L);
        basketMatch.setExternalId("1002");
        basketMatch.setSportName("Basketball");
        basketMatch.setTeam1("Lakers");
        basketMatch.setTeam2("Celtics");

        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(702L)
                .nameEn("Winner")
                .nameRu("Победитель")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(201L).nameEn("Lakers").factor(1.50).build(),
                        ApuestatotalStakeData.builder().id(202L).nameEn("Celtics").factor(2.60).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1002L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(basketMatch, oddsData);
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
    @DisplayName("2.5.1 Halves 1X2: 1st Half (Group 4) and 2nd Half (Group 7)")
    void testHalves1X2() {
        ApuestatotalStakeGroupData half1Group = ApuestatotalStakeGroupData.builder()
                .id(4L)
                .nameEn("1st Half - 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(401L).nameEn("1").factor(2.40).build(),
                        ApuestatotalStakeData.builder().id(402L).nameEn("X").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(403L).nameEn("2").factor(3.80).build()
                ))
                .build();

        ApuestatotalStakeGroupData half2Group = ApuestatotalStakeGroupData.builder()
                .id(7L)
                .nameEn("2nd Half - 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(701L).nameEn("1").factor(2.60).build(),
                        ApuestatotalStakeData.builder().id(702L).nameEn("X").factor(2.20).build(),
                        ApuestatotalStakeData.builder().id(703L).nameEn("2").factor(3.50).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.1 Totals: Match Total (Group 3), Halves (Groups 6 & 9) and Individual Totals")
    void testTotals() {
        ApuestatotalStakeGroupData matchTotalGroup = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Total Goals")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(301L).nameEn("Over").argument(2.5).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(302L).nameEn("Under").argument(2.5).factor(1.95).build()
                ))
                .build();

        ApuestatotalStakeGroupData half1TotalGroup = ApuestatotalStakeGroupData.builder()
                .id(6L)
                .nameEn("1st Half Total Goals")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(601L).nameEn("Over").argument(1.0).factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(602L).nameEn("Under").argument(1.0).factor(2.00).build()
                ))
                .build();

        ApuestatotalStakeGroupData half2TotalGroup = ApuestatotalStakeGroupData.builder()
                .id(9L)
                .nameEn("2nd Half Total Goals")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(901L).nameEn("Over").argument(1.5).factor(2.05).build(),
                        ApuestatotalStakeData.builder().id(902L).nameEn("Under").argument(1.5).factor(1.75).build()
                ))
                .build();

        ApuestatotalStakeGroupData indTotalGroup = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Team 1 Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(311L).nameEn("Over").argument(1.5).factor(1.75).build(),
                        ApuestatotalStakeData.builder().id(312L).nameEn("Under").argument(1.5).factor(2.15).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(matchTotalGroup, half1TotalGroup, half2TotalGroup, indTotalGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(8, items.size());

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

        // 2nd Half Total Over 1.5
        TotalBet h2Over = (TotalBet) items.get(4).getBetType();
        assertEquals(BetScope.HALF_2, h2Over.scope());
        assertEquals(1.5, h2Over.param());

        // Team 1 Total Over 1.5
        TotalBet t1Over = (TotalBet) items.get(6).getBetType();
        assertEquals(BetScope.FULL_MATCH, t1Over.scope());
        assertEquals(BetSubject.TEAM1, t1Over.subject());
        assertEquals(1.5, t1Over.param());
    }

    @Test
    @DisplayName("2.5.1 Handicaps: Match Handicap (Group 2) and Halves (Groups 5 & 8) including Asian lines")
    void testHandicaps() {
        ApuestatotalStakeGroupData matchHandicapGroup = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(201L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.30).build(),
                        ApuestatotalStakeData.builder().id(202L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.65).build()
                ))
                .build();

        ApuestatotalStakeGroupData half1HandicapGroup = ApuestatotalStakeGroupData.builder()
                .id(5L)
                .nameEn("1st Half - Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(501L).nameEn("1 (-0.5)").argument(-0.5).factor(2.50).build(),
                        ApuestatotalStakeData.builder().id(502L).nameEn("2 (+0.5)").argument(0.5).factor(1.55).build()
                ))
                .build();

        ApuestatotalStakeGroupData half2HandicapGroup = ApuestatotalStakeGroupData.builder()
                .id(8L)
                .nameEn("2nd Half - Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(801L).nameEn("1 (-0.5)").argument(-0.5).factor(2.40).build(),
                        ApuestatotalStakeData.builder().id(802L).nameEn("2 (+0.5)").argument(0.5).factor(1.60).build()
                ))
                .build();

        ApuestatotalStakeGroupData asianHandicapGroup = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Asian Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(211L).nameEn("H1 (-1.25)").argument(-1.25).factor(2.20).build(),
                        ApuestatotalStakeData.builder().id(212L).nameEn("H2 (+1.25)").argument(1.25).factor(1.70).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(matchHandicapGroup, half1HandicapGroup, half2HandicapGroup, asianHandicapGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(8, items.size());

        // Match Handicap
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.MATCH, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());

        // 1st Half Handicap
        HandicapBet half1H1 = (HandicapBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, half1H1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, half1H1.outcome());
        assertEquals(-0.5, half1H1.param());

        // 2nd Half Handicap
        HandicapBet half2H1 = (HandicapBet) items.get(4).getBetType();
        assertEquals(BetScope.HALF_2, half2H1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, half2H1.outcome());
        assertEquals(-0.5, half2H1.param());

        // Asian Handicap
        HandicapBet asianH1 = (HandicapBet) items.get(6).getBetType();
        assertTrue(asianH1.isAsian());
        assertEquals(-1.25, asianH1.param());
    }

    // ==========================================
    // 2.5.2 Тесты росписи и статистики (Double Chance, BTTS, Corners, Yellow Cards)
    // ==========================================

    @Test
    @DisplayName("2.5.2 Double Chance: Full Match (Group 992) and 1st Half (Group 993) including Spanish names")
    void testDoubleChance() {
        ApuestatotalStakeGroupData dcMatch = ApuestatotalStakeGroupData.builder()
                .id(992L)
                .nameEn("Double Chance")
                .nameRu("Двойной шанс")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(9921L).nameEn("1X").factor(1.25).build(),
                        ApuestatotalStakeData.builder().id(9922L).nameEn("12").factor(1.30).build(),
                        ApuestatotalStakeData.builder().id(9923L).nameEn("X2").factor(1.85).build()
                ))
                .build();

        ApuestatotalStakeGroupData dcHalf = ApuestatotalStakeGroupData.builder()
                .id(993L)
                .nameEn("1st Half - Double Chance")
                .nameRu("1-й тайм - Двойной шанс")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(9931L).nameEn("1X").factor(1.15).build(),
                        ApuestatotalStakeData.builder().id(9932L).nameEn("12").factor(1.45).build(),
                        ApuestatotalStakeData.builder().id(9933L).nameEn("X2").factor(1.80).build()
                ))
                .build();

        ApuestatotalStakeGroupData dcSpanish = ApuestatotalStakeGroupData.builder()
                .nameEn("Doble oportunidad")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(9941L).nameEn("Local o Empate").factor(1.22).build(),
                        ApuestatotalStakeData.builder().id(9942L).nameEn("Local o Visitante").factor(1.32).build(),
                        ApuestatotalStakeData.builder().id(9943L).nameEn("Empate o Visitante").factor(1.90).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(dcMatch, dcHalf, dcSpanish))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(9, items.size());

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

        MatchResultBet spanishDc1x = (MatchResultBet) items.get(6).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, spanishDc1x.outcome());
    }

    @Test
    @DisplayName("2.5.2 BTTS (Both Teams To Score): Full Match (Group 46) and 1st Half (Group 10)")
    void testBothTeamsToScore() {
        ApuestatotalStakeGroupData bttsMatch = ApuestatotalStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .nameRu("Обе команды забьют")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(461L).nameEn("Yes").factor(1.72).build(),
                        ApuestatotalStakeData.builder().id(462L).nameEn("No").factor(2.10).build()
                ))
                .build();

        ApuestatotalStakeGroupData bttsHalf = ApuestatotalStakeGroupData.builder()
                .id(10L)
                .nameEn("1st Half - Both Teams To Score")
                .nameRu("1-й тайм - Обе забьют")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(101L).nameRu("Да").factor(4.50).build(),
                        ApuestatotalStakeData.builder().id(102L).nameRu("Нет").factor(1.20).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.2 Corners: 1X2 (Group 168), Total (Group 166), Handicap (Group 167)")
    void testCornersMarkets() {
        ApuestatotalStakeGroupData corners1X2 = ApuestatotalStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1681L).nameEn("1").factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(1682L).nameEn("X").factor(7.50).build(),
                        ApuestatotalStakeData.builder().id(1683L).nameEn("2").factor(2.30).build()
                ))
                .build();

        ApuestatotalStakeGroupData cornersTotal = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1661L).nameEn("Over").argument(9.5).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(1662L).nameEn("Under").argument(9.5).factor(1.95).build()
                ))
                .build();

        ApuestatotalStakeGroupData cornersHandicap = ApuestatotalStakeGroupData.builder()
                .id(167L)
                .nameEn("Corners Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1671L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.05).build(),
                        ApuestatotalStakeData.builder().id(1672L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.75).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.2 Yellow Cards: 1X2 (Group 187), Total (Group 188), Handicap (Group 189)")
    void testYellowCardsMarkets() {
        ApuestatotalStakeGroupData cards1X2 = ApuestatotalStakeGroupData.builder()
                .id(187L)
                .nameEn("Yellow Cards 1X2")
                .nameRu("Желтые карточки 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1871L).nameEn("1").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(1872L).nameEn("X").factor(4.50).build(),
                        ApuestatotalStakeData.builder().id(1873L).nameEn("2").factor(2.40).build()
                ))
                .build();

        ApuestatotalStakeGroupData cardsTotal = ApuestatotalStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1881L).nameEn("Over").argument(3.5).factor(1.70).build(),
                        ApuestatotalStakeData.builder().id(1882L).nameEn("Under").argument(3.5).factor(2.15).build()
                ))
                .build();

        ApuestatotalStakeGroupData cardsHandicap = ApuestatotalStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1891L).nameEn("Handicap 1 (0)").argument(0.0).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(1892L).nameEn("Handicap 2 (0)").argument(0.0).factor(1.95).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    // 2.5.3 Тесты киберспорта с SportType (CS2, Dota2, LoL, Valorant)
    // ==========================================

    @Test
    @DisplayName("2.5.3 Map Winners: Map 1 (Group 703), Map 2 (Group 704), Map 3 (Group 705) with BetScope.MAP_1..3")
    void testEsportsMapWinners() {
        ApuestatotalStakeGroupData map1 = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7031L).nameEn("1").factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(7032L).nameEn("2").factor(1.95).build()
                ))
                .build();

        ApuestatotalStakeGroupData map2 = ApuestatotalStakeGroupData.builder()
                .id(704L)
                .nameEn("Map 2 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7041L).nameEn("1").factor(1.70).build(),
                        ApuestatotalStakeData.builder().id(7042L).nameEn("2").factor(2.15).build()
                ))
                .build();

        ApuestatotalStakeGroupData map3 = ApuestatotalStakeGroupData.builder()
                .id(705L)
                .nameEn("Map 3 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7051L).nameEn("1").factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(7052L).nameEn("2").factor(1.90).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
        assertEquals(StatType.MATCH, m1.statType());

        MatchResultBet m2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.MAP_2, m2.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m2.outcome());

        MatchResultBet m3 = (MatchResultBet) items.get(4).getBetType();
        assertEquals(BetScope.MAP_3, m3.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m3.outcome());
    }

    @Test
    @DisplayName("2.5.3 Map Handicap (Groups 740/741) and Map Total (Group 742) with StatType.MAPS")
    void testEsportsMapHandicapAndTotal() {
        ApuestatotalStakeGroupData mapHandicap = ApuestatotalStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7401L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.60).build(),
                        ApuestatotalStakeData.builder().id(7402L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.50).build()
                ))
                .build();

        ApuestatotalStakeGroupData mapTotal = ApuestatotalStakeGroupData.builder()
                .id(742L)
                .nameEn("Map Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7421L).nameEn("Over").argument(2.5).factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(7422L).nameEn("Under").argument(2.5).factor(1.70).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.3 Rounds Total and Handicap: StatType.ROUNDS on Map 1 and Map 2")
    void testEsportsRoundsTotalAndHandicap() {
        ApuestatotalStakeGroupData roundsTotal = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 1 - Total Rounds")
                .nameRu("Карта 1 тотал раундов")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(8101L).nameEn("Over").argument(21.5).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(8102L).nameEn("Under").argument(21.5).factor(1.95).build()
                ))
                .build();

        ApuestatotalStakeGroupData roundsHandicap = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 2 - Round Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(8103L).nameEn("H1 (-3.5)").argument(-3.5).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(8104L).nameEn("H2 (+3.5)").argument(3.5).factor(1.90).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(2002L)
                .groups(List.of(roundsTotal, roundsHandicap))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);
        assertNotNull(request);
        List<OddItem> items = request.getOdds();
        assertEquals(4, items.size());

        TotalBet rOver = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, rOver.scope());
        assertEquals(StatType.ROUNDS, rOver.statType());
        assertEquals(TotalBet.Direction.OVER, rOver.direction());
        assertEquals(21.5, rOver.param());

        HandicapBet rH1 = (HandicapBet) items.get(2).getBetType();
        assertEquals(BetScope.MAP_2, rH1.scope());
        assertEquals(StatType.ROUNDS, rH1.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, rH1.outcome());
        assertEquals(-3.5, rH1.param());
    }

    @Test
    @DisplayName("2.5.3 Support for multiple esports disciplines: Dota2, LoL, Valorant")
    void testEsportsMultipleDisciplines() {
        MatchCache dotaMatch = new MatchCache();
        dotaMatch.setId(3001L);
        dotaMatch.setExternalId("3001");
        dotaMatch.setSportName("Dota 2");
        dotaMatch.setTeam1("Team Spirit");
        dotaMatch.setTeam2("OG");

        MatchCache lolMatch = new MatchCache();
        lolMatch.setId(3002L);
        lolMatch.setExternalId("3002");
        lolMatch.setSportName("League of Legends");
        lolMatch.setTeam1("T1");
        lolMatch.setTeam2("Gen.G");

        MatchCache valMatch = new MatchCache();
        valMatch.setId(3003L);
        valMatch.setExternalId("3003");
        valMatch.setSportName("Valorant");
        valMatch.setTeam1("Fnatic");
        valMatch.setTeam2("Sentinels");

        ApuestatotalStakeGroupData map1Winner = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(901L).nameEn("1").factor(1.65).build(),
                        ApuestatotalStakeData.builder().id(902L).nameEn("2").factor(2.20).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(3001L)
                .groups(List.of(map1Winner))
                .build();

        OddsUpdateRequest dotaReq = mapper.mapToOddsUpdateRequest(dotaMatch, oddsData);
        assertNotNull(dotaReq);
        assertEquals(SportType.DOTA2, dotaReq.getSportType());
        assertEquals(2, dotaReq.getOdds().size());

        oddsData.setMatchId(3002L);
        OddsUpdateRequest lolReq = mapper.mapToOddsUpdateRequest(lolMatch, oddsData);
        assertNotNull(lolReq);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, lolReq.getSportType());
        assertEquals(2, lolReq.getOdds().size());

        oddsData.setMatchId(3003L);
        OddsUpdateRequest valReq = mapper.mapToOddsUpdateRequest(valMatch, oddsData);
        assertNotNull(valReq);
        assertEquals(SportType.VALORANT, valReq.getSportType());
        assertEquals(2, valReq.getOdds().size());
    }

    @Test
    @DisplayName("2.5.3 Non-esports match rejects map markets in EsportsHandler")
    void testEsportsNonEsportsRejection() {
        ApuestatotalStakeGroupData mapGroup = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(ApuestatotalStakeData.builder().id(701L).nameEn("1").factor(1.8).build()))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(mapGroup))
                .build();

        // Football match should NOT map Map 1 Winner (it is not esports and none of the other handlers accept it)
        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(footballMatch, oddsData);
        assertNull(request);
    }

    // ==========================================
    // 2.5.4 Тесты граничных случаев (null/empty, unsupported, invalid factors)
    // ==========================================

    @Test
    @DisplayName("2.5.4 Null and empty inputs return null request")
    void testNullAndEmptyInputs() {
        ApuestatotalMatchOddsData validData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(ApuestatotalStakeGroupData.builder()
                        .id(1L)
                        .nameEn("1X2")
                        .stakes(List.of(ApuestatotalStakeData.builder().nameEn("1").factor(1.5).build()))
                        .build()))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(null, validData));
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, null));

        ApuestatotalMatchOddsData emptyGroupsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(new ArrayList<>())
                .build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, emptyGroupsData));

        ApuestatotalMatchOddsData nullGroupsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(null)
                .build();
        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, nullGroupsData));
    }

    @Test
    @DisplayName("2.5.4 Fallback externalEventId and default SportType when optional fields are null")
    void testFallbackExternalIdAndSportType() {
        MatchCache minimalMatch = new MatchCache();
        minimalMatch.setExternalId("ext-999");
        minimalMatch.setSportName(null);
        minimalMatch.setTeam1("Team A");
        minimalMatch.setTeam2("Team B");

        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("1").factor(1.5).build()))
                .build();

        // matchId in oddsData is null -> fallback to cached.getExternalId()
        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(null)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(minimalMatch, oddsData);
        assertNotNull(request);
        assertEquals("ext-999", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("https://apuestatotal.com/deportes/evento/ext-999", request.getEventUrl());
    }

    @Test
    @DisplayName("2.5.4 Unsupported or unknown market ID and names produce no odds and return null request")
    void testUnsupportedMarketsIgnored() {
        ApuestatotalStakeGroupData unknownGroup = ApuestatotalStakeGroupData.builder()
                .id(999999L)
                .nameEn("Who will be the first player to touch the ball")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(9991L).nameEn("Player A").factor(5.5).build(),
                        ApuestatotalStakeData.builder().id(9992L).nameEn("Player B").factor(6.0).build()
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(unknownGroup))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.5.4 Invalid odds factors (<= 1.0, null, negative) are filtered out")
    void testInvalidOddsFactorsFiltered() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(11L).nameEn("1").factor(1.0).build(),   // factor == 1.0 invalid
                        ApuestatotalStakeData.builder().id(12L).nameEn("X").factor(null).build(),  // null factor invalid
                        ApuestatotalStakeData.builder().id(13L).nameEn("2").factor(-1.5).build(),  // negative factor invalid
                        ApuestatotalStakeData.builder().id(14L).nameEn("1").factor(0.85).build()   // factor < 1.0 invalid
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(group))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.5.4 Mixed valid and invalid stakes only retain valid odds")
    void testMixedValidAndInvalidStakes() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(11L).nameEn("1").factor(1.95).build(), // valid
                        ApuestatotalStakeData.builder().id(12L).nameEn("X").factor(null).build(), // invalid
                        ApuestatotalStakeData.builder().id(13L).nameEn("2").factor(3.80).build()  // valid
                ))
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
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
    @DisplayName("2.5.4 Gracefully handles null or empty stakes list inside groups")
    void testNullOrEmptyStakesInGroup() {
        ApuestatotalStakeGroupData emptyStakesGroup = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(new ArrayList<>())
                .build();

        ApuestatotalStakeGroupData nullStakesGroup = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(null)
                .build();

        ApuestatotalMatchOddsData oddsData = ApuestatotalMatchOddsData.builder()
                .matchId(1001L)
                .groups(List.of(emptyStakesGroup, nullStakesGroup))
                .build();

        assertNull(mapper.mapToOddsUpdateRequest(footballMatch, oddsData));
    }

    @Test
    @DisplayName("2.5.4 mapStakeGroup null safety")
    void testMapStakeGroupNullSafety() {
        List<OddItem> items = new ArrayList<>();
        assertDoesNotThrow(() -> mapper.mapStakeGroup(null, items, footballMatch, SportType.FOOTBALL));
        assertDoesNotThrow(() -> mapper.mapStakeGroup(ApuestatotalStakeGroupData.builder().build(), null, footballMatch, SportType.FOOTBALL));
        assertTrue(items.isEmpty());
    }
}
