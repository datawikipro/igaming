package pro.datawiki.igaming.source.bcgame.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;
import pro.datawiki.igaming.source.bcgame.service.handler.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BcgameOddsMapperTest {

    @Mock
    private SportNormalizationService sportNormalizationService;

    @Mock
    private UnmappedBetService unmappedBetService;

    private BcgameOddsMapper mapper;

    @BeforeEach
    void setUp() {
        // Provide handlers in unordered sequence to verify sorting
        List<BcgameMarketHandler> handlers = List.of(
                new BcgameCorrectScoreHandler(), // @Order(70)
                new BcgameTotalMarketHandler(),   // @Order(40)
                new BcgameStatsMarketHandler(),   // @Order(10)
                new BcgameHandicapMarketHandler(),// @Order(50)
                new BcgameBttsHandler(),          // @Order(60)
                new BcgameResultMarketHandler(),  // @Order(30)
                new BcgameEsportsMarketHandler()  // @Order(20)
        );

        mapper = new BcgameOddsMapper(sportNormalizationService, unmappedBetService, handlers);
    }

    @Test
    @DisplayName("Should verify supports and handler order")
    void testSupportsAndOrder() {
        assertTrue(mapper.supports("bcgame", SportType.FOOTBALL));
        assertTrue(mapper.supports("BCGAME", null));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
        assertFalse(mapper.supports("pinnacle", null));

        List<BcgameMarketHandler> sortedHandlers = mapper.getMarketHandlers();
        assertEquals(7, sortedHandlers.size());
        assertInstanceOf(BcgameStatsMarketHandler.class, sortedHandlers.get(0));    // Order 10
        assertInstanceOf(BcgameEsportsMarketHandler.class, sortedHandlers.get(1));  // Order 20
        assertInstanceOf(BcgameResultMarketHandler.class, sortedHandlers.get(2));   // Order 30
        assertInstanceOf(BcgameTotalMarketHandler.class, sortedHandlers.get(3));    // Order 40
        assertInstanceOf(BcgameHandicapMarketHandler.class, sortedHandlers.get(4)); // Order 50
        assertInstanceOf(BcgameBttsHandler.class, sortedHandlers.get(5));           // Order 60
        assertInstanceOf(BcgameCorrectScoreHandler.class, sortedHandlers.get(6));   // Order 70
    }

    @Test
    @DisplayName("Should map complete event across all handler types")
    void testMapCompleteEvent() {
        when(sportNormalizationService.normalize("Soccer")).thenReturn(SportType.FOOTBALL);

        BcgameEventDto event = new BcgameEventDto();
        event.setId("ev-100");
        event.setName("Real Madrid vs Barcelona");
        event.setHomeTeam("Real Madrid");
        event.setAwayTeam("Barcelona");
        event.setSportName("Soccer");
        event.setTournamentName("La Liga");
        event.setIsLive(false);
        event.setStartTime(1750000000000L);

        // 1. Result Market
        BcgameMarketDto resultMarket = new BcgameMarketDto();
        resultMarket.setName("1X2");
        resultMarket.setOutcomes(List.of(
                createOutcome("1", "Real Madrid", 2.10, null),
                createOutcome("x", "Draw", 3.40, null),
                createOutcome("2", "Barcelona", 3.20, null)
        ));

        // 2. Total Market
        BcgameMarketDto totalMarket = new BcgameMarketDto();
        totalMarket.setName("Total Goals");
        totalMarket.setOutcomes(List.of(
                createOutcome("o25", "Over 2.5", 1.85, 2.5),
                createOutcome("u25", "Under 2.5", 1.95, 2.5)
        ));

        // 3. Handicap Market
        BcgameMarketDto handicapMarket = new BcgameMarketDto();
        handicapMarket.setName("Handicap");
        handicapMarket.setOutcomes(List.of(
                createOutcome("h1", "Real Madrid (-1.5)", 2.80, -1.5),
                createOutcome("h2", "Barcelona (+1.5)", 1.45, 1.5)
        ));

        // 4. BTTS Market
        BcgameMarketDto bttsMarket = new BcgameMarketDto();
        bttsMarket.setName("Both Teams To Score");
        bttsMarket.setOutcomes(List.of(
                createOutcome("btts_y", "Yes", 1.70, null),
                createOutcome("btts_n", "No", 2.10, null)
        ));

        // 5. Correct Score Market
        BcgameMarketDto csMarket = new BcgameMarketDto();
        csMarket.setName("Correct Score");
        csMarket.setOutcomes(List.of(
                createOutcome("cs_21", "2:1", 8.50, null)
        ));

        // 6. Stats Market (Corners 1X2)
        BcgameMarketDto cornersMarket = new BcgameMarketDto();
        cornersMarket.setName("Corners 1X2");
        cornersMarket.setOutcomes(List.of(
                createOutcome("c1", "Real Madrid", 1.80, null),
                createOutcome("cx", "Draw", 6.50, null),
                createOutcome("c2", "Barcelona", 2.20, null)
        ));

        event.setMarkets(List.of(resultMarket, totalMarket, handicapMarket, bttsMarket, csMarket, cornersMarket));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);

        assertNotNull(request);
        assertEquals("bcgame", request.getBookmaker());
        assertEquals("ev-100", request.getExternalEventId());
        assertEquals("Real Madrid", request.getTeam1());
        assertEquals("Barcelona", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals(1750000000000L, request.getStartTime());
        assertFalse(request.getIsLive());
        assertEquals("https://bc.game/sports/event/ev-100", request.getEventUrl());

        List<OddItem> odds = request.getOdds();
        assertNotNull(odds);
        // 3 (1x2) + 2 (total) + 2 (handicap) + 2 (btts) + 1 (cs) + 3 (corners) = 13 items
        assertEquals(13, odds.size());

        // Verify some odd items
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet
                && ((MatchResultBet) o.getBetType()).outcome() == MatchResultBet.Outcome.WIN1));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet
                && ((TotalBet) o.getBetType()).direction() == TotalBet.Direction.OVER));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet
                && ((HandicapBet) o.getBetType()).outcome() == HandicapBet.Outcome.TEAM1));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet
                && ((BinaryMarketBet) o.getBetType()).marketType() == BinaryMarketBet.MarketType.BTTS));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof CorrectScoreBet));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet
                && ((MatchResultBet) o.getBetType()).statType() == StatType.CORNERS));
    }

    @Test
    @DisplayName("Should map esports event with maps and kills")
    void testMapEsportsEvent() {
        when(sportNormalizationService.normalize("CS2")).thenReturn(SportType.CS2);

        BcgameEventDto event = new BcgameEventDto();
        event.setId("esports-200");
        event.setName("NaVi vs FaZe");
        event.setSportName("CS2");
        event.setTournamentName("Major");

        BcgameMarketDto map1Winner = new BcgameMarketDto();
        map1Winner.setName("Map 1 - Winner");
        map1Winner.setOutcomes(List.of(
                createOutcome("m1_1", "NaVi", 1.80, null),
                createOutcome("m1_2", "FaZe", 2.00, null)
        ));

        BcgameMarketDto firstBlood = new BcgameMarketDto();
        firstBlood.setName("Map 1 - First Blood");
        firstBlood.setOutcomes(List.of(
                createOutcome("fb_1", "NaVi", 1.90, null),
                createOutcome("fb_2", "FaZe", 1.90, null)
        ));

        event.setMarkets(List.of(map1Winner, firstBlood));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals("NaVi", request.getTeam1());
        assertEquals("FaZe", request.getTeam2());
        assertEquals(4, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet
                && ((MatchResultBet) o.getBetType()).scope() == BetScope.MAP_1
                && ((MatchResultBet) o.getBetType()).outcome() == MatchResultBet.Outcome.WIN1_2WAY));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet
                && ((BinaryMarketBet) o.getBetType()).marketType() == BinaryMarketBet.MarketType.FIRST_BLOOD));
    }

    @Test
    @DisplayName("Should use fallback metadata from MatchCache when event fields are missing")
    void testMatchCacheFallback() {
        when(sportNormalizationService.normalize("Football")).thenReturn(SportType.FOOTBALL);

        MatchCache cached = new MatchCache();
        cached.setExternalId("cached-300");
        cached.setTeam1("Liverpool");
        cached.setTeam2("Manchester City");
        cached.setSportName("Football");
        cached.setLeagueName("Premier League");
        cached.setStartTime(1760000000000L);
        cached.setEventUrl("https://bc.game/sports/custom/cached-300");

        BcgameEventDto event = new BcgameEventDto();
        event.setId("cached-300");
        // Teams and tournament missing in detailed event
        event.setMarkets(List.of(
                createTotalMarket("Total", 2.5, 1.90, 1.90)
        ));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cached, event);
        assertNotNull(request);
        assertEquals("cached-300", request.getExternalEventId());
        assertEquals("Liverpool", request.getTeam1());
        assertEquals("Manchester City", request.getTeam2());
        assertEquals("Premier League", request.getLeagueName());
        assertEquals(1760000000000L, request.getStartTime());
        assertEquals("https://bc.game/sports/custom/cached-300", request.getEventUrl());
        assertEquals(2, request.getOdds().size());
    }

    @Test
    @DisplayName("Should notify unmappedBetService for unknown markets")
    void testUnmappedMarketNotification() {
        when(sportNormalizationService.normalize("Soccer")).thenReturn(SportType.FOOTBALL);

        BcgameEventDto event = new BcgameEventDto();
        event.setId("ev-400");
        event.setName("Team A vs Team B");
        event.setSportName("Soccer");

        BcgameMarketDto unknownMarket = new BcgameMarketDto();
        unknownMarket.setName("Will there be a penalty in 90 mins?");
        unknownMarket.setOutcomes(List.of(
                createOutcome("p1", "Yes", 2.50, null),
                createOutcome("p2", "No", 1.50, null)
        ));

        event.setMarkets(List.of(unknownMarket));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(0, request.getOdds().size());

        verify(unmappedBetService, times(1)).saveAndNotify(
                eq("bcgame"), eq("Soccer"), eq("Will there be a penalty in 90 mins?"),
                eq("Will there be a penalty in 90 mins?"), eq("ev-400")
        );
    }

    @Test
    @DisplayName("Should delegate in legacy map() method")
    void testLegacyMapMethod() {
        BetType bet1 = mapper.map("1X2", "1", null);
        assertNotNull(bet1);
        assertInstanceOf(MatchResultBet.class, bet1);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) bet1).outcome());

        BetType betTotal = mapper.map("Total", "Over 2.5", 2.5);
        assertNotNull(betTotal);
        assertInstanceOf(TotalBet.class, betTotal);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) betTotal).direction());

        assertNull(mapper.map("Unknown Unknown", "Outcome", 1.0));
        assertNull(mapper.map(null, null, null));
    }

    @Test
    @DisplayName("Should handle null and empty events gracefully")
    void testNullAndEmptyHandling() {
        assertNull(mapper.mapToOddsUpdateRequest(null));
        assertNull(mapper.mapToOddsUpdateRequest(null, (BcgameEventDto) null));

        BcgameEventDto emptyEvent = new BcgameEventDto();
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(emptyEvent);
        assertNotNull(req);
        assertEquals("bcgame", req.getBookmaker());
        assertTrue(req.getOdds().isEmpty());
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds, Double param) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        outcome.setParam(param);
        return outcome;
    }

    private BcgameMarketDto createTotalMarket(String name, double param, double overOdds, double underOdds) {
        BcgameMarketDto market = new BcgameMarketDto();
        market.setName(name);
        market.setOutcomes(List.of(
                createOutcome("tot_o", "Over " + param, overOdds, param),
                createOutcome("tot_u", "Under " + param, underOdds, param)
        ));
        return market;
    }
}
