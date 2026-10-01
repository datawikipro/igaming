package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameResultMarketHandlerTest {

    private BcgameResultMarketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameResultMarketHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported markets")
    void testSupports() {
        BcgameMarketContext soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();

        // Supported
        assertTrue(handler.supports("1X2", soccerContext));
        assertTrue(handler.supports("Match Result", soccerContext));
        assertTrue(handler.supports("Match Winner", soccerContext));
        assertTrue(handler.supports("1st Half - 1X2", soccerContext));
        assertTrue(handler.supports("Double Chance", soccerContext));
        assertTrue(handler.supports("1st Half - Double Chance", soccerContext));
        assertTrue(handler.supports("Draw No Bet", soccerContext));
        assertTrue(handler.supports("Moneyline", soccerContext));
        assertTrue(handler.supports("Head to Head", soccerContext));
        assertTrue(handler.supports("2-Way", soccerContext));
        assertTrue(handler.supports("3-Way", soccerContext));

        // Unsupported (Stats)
        assertFalse(handler.supports("Corners 1X2", soccerContext));
        assertFalse(handler.supports("Yellow Cards 1X2", soccerContext));
        assertFalse(handler.supports("Fouls 1X2", soccerContext));

        // Unsupported (Totals, Handicaps, BTTS, Correct Score)
        assertFalse(handler.supports("Total Goals", soccerContext));
        assertFalse(handler.supports("Asian Handicap", soccerContext));
        assertFalse(handler.supports("Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Correct Score", soccerContext));

        // Esports sub-markets unsupported (handled by esports handler)
        BcgameMarketContext esportsContext = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();
        assertFalse(handler.supports("Map 1 Winner", esportsContext));
        assertFalse(handler.supports("Round 1 Winner", esportsContext));
        assertFalse(handler.supports("First Blood", esportsContext));
    }

    @Test
    @DisplayName("Should correctly handle 1X2 3-Way market")
    void testHandle1X2ThreeWay() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m1");
        market.setName("Match Result");
        market.setOutcomes(List.of(
                createOutcome("o1", "Real Madrid", 2.10),
                createOutcome("o2", "Draw", 3.40),
                createOutcome("o3", "Barcelona", 3.20)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(3, items.size());

        OddItem item1 = items.get(0);
        assertEquals("o1", item1.getFactorId());
        assertEquals("Match Result", item1.getGroupName());
        assertEquals(2.10, item1.getValue());
        assertInstanceOf(MatchResultBet.class, item1.getBetType());
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem itemX = items.get(1);
        MatchResultBet betX = (MatchResultBet) itemX.getBetType();
        assertEquals(BetScope.FULL_MATCH, betX.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());

        OddItem item2 = items.get(2);
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle 1st Half 1X2 market")
    void testHandleHalf1X2() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Liverpool")
                .awayTeam("Chelsea")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_h1");
        market.setName("1st Half - 1X2");
        market.setOutcomes(List.of(
                createOutcome("o1", "1", 2.50),
                createOutcome("o2", "X", 2.10),
                createOutcome("o3", "2", 3.80)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(3, items.size());
        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());

        MatchResultBet betX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, betX.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Double Chance market")
    void testHandleDoubleChance() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Tottenham")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_dc");
        market.setName("Double Chance");
        market.setOutcomes(List.of(
                createOutcome("o_1x", "1X", 1.25),
                createOutcome("o_12", "12", 1.30),
                createOutcome("o_x2", "X2", 1.85)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(3, items.size());

        MatchResultBet bet1X = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1X.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, bet1X.outcome());

        MatchResultBet bet12 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet12.scope());
        assertEquals(MatchResultBet.Outcome.DC_12, bet12.outcome());

        MatchResultBet betX2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.FULL_MATCH, betX2.scope());
        assertEquals(MatchResultBet.Outcome.DC_X2, betX2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Draw No Bet (DNB) market")
    void testHandleDrawNoBet() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Manchester City")
                .awayTeam("Liverpool")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_dnb");
        market.setName("Draw No Bet");
        market.setOutcomes(List.of(
                createOutcome("o_dnb1", "Manchester City", 1.55),
                createOutcome("o_dnb2", "Liverpool", 2.45)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle 2-Way Moneyline market in Tennis")
    void testHandleMoneylineTennis() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.TENNIS)
                .homeTeam("Novak Djokovic")
                .awayTeam("Carlos Alcaraz")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_ml");
        market.setName("Winner");
        market.setOutcomes(List.of(
                createOutcome("o_w1", "Novak Djokovic", 1.80),
                createOutcome("o_w2", "Carlos Alcaraz", 2.05)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
    }
}
