package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameTotalMarketHandlerTest {

    private BcgameTotalMarketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameTotalMarketHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    private BcgameOutcomeDto createOutcomeWithTotal(String id, String name, Double odds, Double total) {
        BcgameOutcomeDto outcome = createOutcome(id, name, odds);
        outcome.setTotal(total);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported total markets")
    void testSupports() {
        BcgameMarketContext soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();

        // Supported general totals
        assertTrue(handler.supports("Total", soccerContext));
        assertTrue(handler.supports("Totals", soccerContext));
        assertTrue(handler.supports("Total Goals", soccerContext));
        assertTrue(handler.supports("Total Points", soccerContext));
        assertTrue(handler.supports("Over/Under", soccerContext));
        assertTrue(handler.supports("O/U", soccerContext));
        assertTrue(handler.supports("Over / Under", soccerContext));
        assertTrue(handler.supports("1st Half - Total", soccerContext));
        assertTrue(handler.supports("2nd Half - Total Goals", soccerContext));
        assertTrue(handler.supports("1st Quarter - Total Points", soccerContext));
        assertTrue(handler.supports("Тотал голов", soccerContext));
        assertTrue(handler.supports("Больше/Меньше", soccerContext));

        // Supported individual totals
        assertTrue(handler.supports("Team 1 Total", soccerContext));
        assertTrue(handler.supports("Team 2 Total Goals", soccerContext));
        assertTrue(handler.supports("Home Team Total", soccerContext));
        assertTrue(handler.supports("Away Team Total", soccerContext));
        assertTrue(handler.supports("Индивидуальный тотал 1-й команды", soccerContext));
        assertTrue(handler.supports("ИТ1", soccerContext));
        assertTrue(handler.supports("ИТ2", soccerContext));

        // Unsupported: Stats (handled by BcgameStatsMarketHandler)
        assertFalse(handler.supports("Corners Total", soccerContext));
        assertFalse(handler.supports("Yellow Cards Total", soccerContext));
        assertFalse(handler.supports("Fouls Total", soccerContext));
        assertFalse(handler.supports("Shots on Target Total", soccerContext));

        // Unsupported: Esports sub-markets (handled by BcgameEsportsMarketHandler)
        BcgameMarketContext esportsContext = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();
        assertFalse(handler.supports("Total Maps", esportsContext));
        assertFalse(handler.supports("Total Rounds", esportsContext));
        assertFalse(handler.supports("Total Kills", esportsContext));
        assertFalse(handler.supports("Map 1 Total Rounds", esportsContext));

        // Unsupported: Handicaps, Results, BTTS, Correct Score
        assertFalse(handler.supports("Asian Handicap", soccerContext));
        assertFalse(handler.supports("Match Result", soccerContext));
        assertFalse(handler.supports("1X2", soccerContext));
        assertFalse(handler.supports("Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Correct Score", soccerContext));
    }

    @Test
    @DisplayName("Should handle full match Over/Under total market")
    void testHandleFullMatchTotal() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_tot");
        market.setName("Total Goals");
        market.setOutcomes(List.of(
                createOutcome("o_ov", "Over 2.5", 1.85),
                createOutcome("o_un", "Under 2.5", 1.95)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        OddItem itemOver = items.get(0);
        assertEquals("o_ov", itemOver.getFactorId());
        assertEquals(1.85, itemOver.getValue());
        assertInstanceOf(TotalBet.class, itemOver.getBetType());
        TotalBet betOver = (TotalBet) itemOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, betOver.scope());
        assertEquals(BetSubject.MATCH, betOver.subject());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());
        assertEquals(2.5, betOver.param());
        assertFalse(betOver.isAsian());
        assertEquals(StatType.MATCH, betOver.statType());

        OddItem itemUnder = items.get(1);
        assertEquals("o_un", itemUnder.getFactorId());
        assertEquals(1.95, itemUnder.getValue());
        TotalBet betUnder = (TotalBet) itemUnder.getBetType();
        assertEquals(BetScope.FULL_MATCH, betUnder.scope());
        assertEquals(BetSubject.MATCH, betUnder.subject());
        assertEquals(TotalBet.Direction.UNDER, betUnder.direction());
        assertEquals(2.5, betUnder.param());
        assertFalse(betUnder.isAsian());
    }

    @Test
    @DisplayName("Should handle 1st Half and 2nd Half total markets")
    void testHandleHalfTotals() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .build();

        // 1st Half Total
        BcgameMarketDto marketH1 = new BcgameMarketDto();
        marketH1.setName("1st Half - Total Goals (1.5)");
        marketH1.setOutcomes(List.of(
                createOutcome("o1", "Over", 2.10),
                createOutcome("o2", "Under", 1.70)
        ));

        List<OddItem> itemsH1 = new ArrayList<>();
        handler.handle(marketH1, context, itemsH1);

        assertEquals(2, itemsH1.size());
        TotalBet betH1Over = (TotalBet) itemsH1.get(0).getBetType();
        assertEquals(BetScope.HALF_1, betH1Over.scope());
        assertEquals(BetSubject.MATCH, betH1Over.subject());
        assertEquals(TotalBet.Direction.OVER, betH1Over.direction());
        assertEquals(1.5, betH1Over.param());

        // 2nd Half Total
        BcgameMarketDto marketH2 = new BcgameMarketDto();
        marketH2.setName("2nd Half Total");
        marketH2.setOutcomes(List.of(
                createOutcome("o3", "Over 1.5", 1.90),
                createOutcome("o4", "Under 1.5", 1.90)
        ));

        List<OddItem> itemsH2 = new ArrayList<>();
        handler.handle(marketH2, context, itemsH2);

        assertEquals(2, itemsH2.size());
        TotalBet betH2Under = (TotalBet) itemsH2.get(1).getBetType();
        assertEquals(BetScope.HALF_2, betH2Under.scope());
        assertEquals(TotalBet.Direction.UNDER, betH2Under.direction());
        assertEquals(1.5, betH2Under.param());
    }

    @Test
    @DisplayName("Should handle Individual Team Totals (Team 1 and Team 2)")
    void testHandleIndividualTeamTotals() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .build();

        // Team 1 Total via keyword
        BcgameMarketDto marketT1 = new BcgameMarketDto();
        marketT1.setName("Team 1 - Total Goals");
        marketT1.setOutcomes(List.of(
                createOutcome("ot1_ov", "Over 1.5", 1.65),
                createOutcome("ot1_un", "Under 1.5", 2.20)
        ));

        List<OddItem> itemsT1 = new ArrayList<>();
        handler.handle(marketT1, context, itemsT1);

        assertEquals(2, itemsT1.size());
        TotalBet betT1 = (TotalBet) itemsT1.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betT1.scope());
        assertEquals(BetSubject.TEAM1, betT1.subject());
        assertEquals(TotalBet.Direction.OVER, betT1.direction());
        assertEquals(1.5, betT1.param());

        // Team 2 Total via Team Name in market
        BcgameMarketDto marketT2 = new BcgameMarketDto();
        marketT2.setName("Barcelona Total Goals");
        marketT2.setOutcomes(List.of(
                createOutcome("ot2_ov", "Over 1.5", 2.15),
                createOutcome("ot2_un", "Under 1.5", 1.68)
        ));

        List<OddItem> itemsT2 = new ArrayList<>();
        handler.handle(marketT2, context, itemsT2);

        assertEquals(2, itemsT2.size());
        TotalBet betT2 = (TotalBet) itemsT2.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, betT2.scope());
        assertEquals(BetSubject.TEAM2, betT2.subject());
        assertEquals(TotalBet.Direction.UNDER, betT2.direction());
        assertEquals(1.5, betT2.param());
    }

    @Test
    @DisplayName("Should handle Quarter Asian Totals and European Asian markets")
    void testHandleAsianTotals() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Inter")
                .awayTeam("Milan")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Asian Total Goals");
        market.setOutcomes(List.of(
                createOutcome("o_225", "Over 2.25", 1.95),
                createOutcome("o_275", "Under 2.75", 1.85)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        TotalBet bet1 = (TotalBet) items.get(0).getBetType();
        assertEquals(2.25, bet1.param());
        assertTrue(bet1.isAsian(), "Quarter Asian 2.25 should have isAsian=true");

        TotalBet bet2 = (TotalBet) items.get(1).getBetType();
        assertEquals(2.75, bet2.param());
        assertTrue(bet2.isAsian(), "Quarter Asian 2.75 should have isAsian=true");
    }

    @Test
    @DisplayName("Should handle Exact Total outcome")
    void testHandleExactTotal() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Chelsea")
                .awayTeam("Arsenal")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Total Goals");
        market.setOutcomes(List.of(
                createOutcome("o_ex2", "Exact 2", 4.20)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(1, items.size());
        TotalBet betExact = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betExact.scope());
        assertEquals(BetSubject.MATCH, betExact.subject());
        assertEquals(TotalBet.Direction.EXACT, betExact.direction());
        assertEquals(2.0, betExact.param());
    }

    @Test
    @DisplayName("Should handle outcomes with param/total field in DTO")
    void testHandleDtoParamField() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.BASKETBALL)
                .homeTeam("Lakers")
                .awayTeam("Warriors")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Total Points");
        market.setOutcomes(List.of(
                createOutcomeWithTotal("o_b1", "Over", 1.90, 224.5),
                createOutcomeWithTotal("o_b2", "Under", 1.90, 224.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        TotalBet betOver = (TotalBet) items.get(0).getBetType();
        assertEquals(224.5, betOver.param());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());

        TotalBet betUnder = (TotalBet) items.get(1).getBetType();
        assertEquals(224.5, betUnder.param());
        assertEquals(TotalBet.Direction.UNDER, betUnder.direction());
    }

    @Test
    @DisplayName("Should handle Russian market names and outcome formats")
    void testHandleRussianFormats() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Спартак")
                .awayTeam("Зенит")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Индивидуальный тотал 1-й команды");
        market.setOutcomes(List.of(
                createOutcome("o_tb", "ТБ (1.5)", 1.75),
                createOutcome("o_tm", "ТМ (1.5)", 2.05)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        TotalBet betTB = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, betTB.subject());
        assertEquals(TotalBet.Direction.OVER, betTB.direction());
        assertEquals(1.5, betTB.param());

        TotalBet betTM = (TotalBet) items.get(1).getBetType();
        assertEquals(BetSubject.TEAM1, betTM.subject());
        assertEquals(TotalBet.Direction.UNDER, betTM.direction());
        assertEquals(1.5, betTM.param());
    }
}
