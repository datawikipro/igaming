package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameHandicapMarketHandlerTest {

    private BcgameHandicapMarketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameHandicapMarketHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    private BcgameOutcomeDto createOutcomeWithHandicap(String id, String name, Double odds, Double handicap) {
        BcgameOutcomeDto outcome = createOutcome(id, name, odds);
        outcome.setHandicap(handicap);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported handicap markets")
    void testSupports() {
        BcgameMarketContext soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Liverpool")
                .awayTeam("Chelsea")
                .build();

        // Supported handicaps
        assertTrue(handler.supports("Handicap", soccerContext));
        assertTrue(handler.supports("Asian Handicap", soccerContext));
        assertTrue(handler.supports("European Handicap", soccerContext));
        assertTrue(handler.supports("3-Way Handicap", soccerContext));
        assertTrue(handler.supports("Spread", soccerContext));
        assertTrue(handler.supports("Point Spread", soccerContext));
        assertTrue(handler.supports("Puck Line", soccerContext));
        assertTrue(handler.supports("Run Line", soccerContext));
        assertTrue(handler.supports("1st Half Asian Handicap", soccerContext));
        assertTrue(handler.supports("2nd Half - Handicap", soccerContext));
        assertTrue(handler.supports("1st Quarter Spread", soccerContext));
        assertTrue(handler.supports("2nd Period Handicap", soccerContext));
        assertTrue(handler.supports("Фора", soccerContext));
        assertTrue(handler.supports("Азиатская фора", soccerContext));
        assertTrue(handler.supports("Европейская фора", soccerContext));
        assertTrue(handler.supports("Фора 1-й тайм", soccerContext));

        // Unsupported: Stats (handled by BcgameStatsMarketHandler)
        assertFalse(handler.supports("Corners Handicap", soccerContext));
        assertFalse(handler.supports("Yellow Cards Handicap", soccerContext));
        assertFalse(handler.supports("Fouls Handicap", soccerContext));

        // Unsupported: Esports sub-markets (handled by BcgameEsportsMarketHandler)
        BcgameMarketContext esportsContext = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();
        assertFalse(handler.supports("Handicap Maps", esportsContext));
        assertFalse(handler.supports("Handicap Rounds", esportsContext));
        assertFalse(handler.supports("Handicap Kills", esportsContext));
        assertFalse(handler.supports("Map 1 Handicap", esportsContext));

        // Unsupported: Totals, Results, BTTS, Correct Score
        assertFalse(handler.supports("Total Goals", soccerContext));
        assertFalse(handler.supports("Over/Under", soccerContext));
        assertFalse(handler.supports("Match Result", soccerContext));
        assertFalse(handler.supports("1X2", soccerContext));
        assertFalse(handler.supports("Double Chance", soccerContext));
        assertFalse(handler.supports("Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Correct Score", soccerContext));
    }

    @Test
    @DisplayName("Should handle 2-Way Asian Handicap full match market")
    void testHandleAsianHandicap() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Liverpool")
                .awayTeam("Chelsea")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_ah");
        market.setName("Asian Handicap");
        market.setOutcomes(List.of(
                createOutcome("o1", "Liverpool (-1.5)", 1.90),
                createOutcome("o2", "Chelsea (+1.5)", 1.95)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("o1", item1.getFactorId());
        assertEquals(1.90, item1.getValue());
        assertInstanceOf(HandicapBet.class, item1.getBetType());
        HandicapBet bet1 = (HandicapBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-1.5, bet1.param());
        assertTrue(bet1.isAsian());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem item2 = items.get(1);
        assertEquals("o2", item2.getFactorId());
        assertEquals(1.95, item2.getValue());
        HandicapBet bet2 = (HandicapBet) item2.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(1.5, bet2.param());
        assertTrue(bet2.isAsian());
    }

    @Test
    @DisplayName("Should handle Quarter Asian Handicap lines (e.g. -0.25 / +0.25)")
    void testHandleQuarterAsianHandicap() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Tottenham")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Handicap");
        market.setOutcomes(List.of(
                createOutcome("o_q1", "1 (-0.25)", 1.85),
                createOutcome("o_q2", "2 (+0.25)", 1.95)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-0.25, bet1.param());
        assertTrue(bet1.isAsian(), "Quarter line (-0.25) must be Asian");

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(0.25, bet2.param());
        assertTrue(bet2.isAsian(), "Quarter line (+0.25) must be Asian");
    }

    @Test
    @DisplayName("Should handle 3-Way European Handicap with Draw outcome")
    void testHandleEuropeanHandicap3Way() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("European Handicap (-1)");
        market.setOutcomes(List.of(
                createOutcome("o_eh1", "Bayern (-1)", 2.40),
                createOutcome("o_ehx", "Tie (-1)", 3.50),
                createOutcome("o_eh2", "Dortmund (+1)", 2.50)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(3, items.size());

        HandicapBet betHome = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betHome.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betHome.outcome());
        assertEquals(-1.0, betHome.param());
        assertFalse(betHome.isAsian(), "3-Way European Handicap must not be Asian");

        HandicapBet betDraw = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, betDraw.scope());
        assertEquals(HandicapBet.Outcome.DRAW, betDraw.outcome());
        assertEquals(-1.0, betDraw.param());
        assertFalse(betDraw.isAsian());

        HandicapBet betAway = (HandicapBet) items.get(2).getBetType();
        assertEquals(BetScope.FULL_MATCH, betAway.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, betAway.outcome());
        assertEquals(1.0, betAway.param());
        assertFalse(betAway.isAsian());
    }

    @Test
    @DisplayName("Should handle Half and Period handicap markets")
    void testHandleHalfAndPeriodHandicaps() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .build();

        // 1st Half Asian Handicap
        BcgameMarketDto marketH1 = new BcgameMarketDto();
        marketH1.setName("1st Half Asian Handicap");
        marketH1.setOutcomes(List.of(
                createOutcome("oh1_1", "Real Madrid (-0.5)", 2.05),
                createOutcome("oh1_2", "Barcelona (+0.5)", 1.75)
        ));

        List<OddItem> itemsH1 = new ArrayList<>();
        handler.handle(marketH1, context, itemsH1);

        assertEquals(2, itemsH1.size());
        HandicapBet betH1_1 = (HandicapBet) itemsH1.get(0).getBetType();
        assertEquals(BetScope.HALF_1, betH1_1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betH1_1.outcome());
        assertEquals(-0.5, betH1_1.param());

        HandicapBet betH1_2 = (HandicapBet) itemsH1.get(1).getBetType();
        assertEquals(BetScope.HALF_1, betH1_2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, betH1_2.outcome());
        assertEquals(0.5, betH1_2.param());

        // 2nd Half Spread
        BcgameMarketDto marketH2 = new BcgameMarketDto();
        marketH2.setName("2nd Half Spread");
        marketH2.setOutcomes(List.of(
                createOutcome("oh2_1", "1 (-1.0)", 2.10),
                createOutcome("oh2_2", "2 (+1.0)", 1.70)
        ));

        List<OddItem> itemsH2 = new ArrayList<>();
        handler.handle(marketH2, context, itemsH2);

        assertEquals(2, itemsH2.size());
        HandicapBet betH2_1 = (HandicapBet) itemsH2.get(0).getBetType();
        assertEquals(BetScope.HALF_2, betH2_1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betH2_1.outcome());
        assertEquals(-1.0, betH2_1.param());

        // Basketball 1st Quarter Spread
        BcgameMarketContext bballContext = BcgameMarketContext.builder()
                .sportType(SportType.BASKETBALL)
                .homeTeam("Lakers")
                .awayTeam("Warriors")
                .build();

        BcgameMarketDto marketQ1 = new BcgameMarketDto();
        marketQ1.setName("1st Quarter Spread");
        marketQ1.setOutcomes(List.of(
                createOutcome("oq1_1", "Lakers -2.5", 1.90),
                createOutcome("oq1_2", "Warriors +2.5", 1.90)
        ));

        List<OddItem> itemsQ1 = new ArrayList<>();
        handler.handle(marketQ1, bballContext, itemsQ1);

        assertEquals(2, itemsQ1.size());
        HandicapBet betQ1 = (HandicapBet) itemsQ1.get(0).getBetType();
        assertEquals(BetScope.PERIOD_1, betQ1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betQ1.outcome());
        assertEquals(-2.5, betQ1.param());
    }

    @Test
    @DisplayName("Should handle Russian market names and outcome formats")
    void testHandleRussianFormats() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Спартак")
                .awayTeam("Зенит")
                .build();

        // Russian 1st half handicap
        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Фора 1-й тайм");
        market.setOutcomes(List.of(
                createOutcome("o_ru1", "Ф1 (-0.5)", 1.80),
                createOutcome("o_ru2", "Ф2 (+0.5)", 2.00)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, bet1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-0.5, bet1.param());

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, bet2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(0.5, bet2.param());
    }

    @Test
    @DisplayName("Should handle outcomes with handicap field in DTO")
    void testHandleDtoHandicapField() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.BASKETBALL)
                .homeTeam("Lakers")
                .awayTeam("Warriors")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Point Spread");
        market.setOutcomes(List.of(
                createOutcomeWithHandicap("o_dto1", "Lakers", 1.90, -5.5),
                createOutcomeWithHandicap("o_dto2", "Warriors", 1.90, 5.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-5.5, bet1.param());

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(5.5, bet2.param());
    }

    @Test
    @DisplayName("Should fallback to market name handicap parameter when outcomes lack param")
    void testHandleMarketLevelParamFallback() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Manchester City")
                .awayTeam("Fulham")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Handicap (-1.5)");
        market.setOutcomes(List.of(
                createOutcome("o_fb1", "1", 1.90),
                createOutcome("o_fb2", "2", 1.90)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-1.5, bet1.param());

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(1.5, bet2.param());
    }
}
