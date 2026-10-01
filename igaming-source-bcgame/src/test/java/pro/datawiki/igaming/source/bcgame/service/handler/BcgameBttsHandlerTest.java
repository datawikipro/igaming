package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameBttsHandlerTest {

    private BcgameBttsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameBttsHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported BTTS markets")
    void testSupports() {
        BcgameMarketContext soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .build();

        // Supported
        assertTrue(handler.supports("Both Teams to Score", soccerContext));
        assertTrue(handler.supports("Both Teams To Score", soccerContext));
        assertTrue(handler.supports("BTTS", soccerContext));
        assertTrue(handler.supports("1st Half - Both Teams to Score", soccerContext));
        assertTrue(handler.supports("2nd Half - Both Teams to Score", soccerContext));
        assertTrue(handler.supports("Both to Score", soccerContext));
        assertTrue(handler.supports("Обе забьют", soccerContext));
        assertTrue(handler.supports("Обе команды забьют", soccerContext));

        // Unsupported (Stats)
        assertFalse(handler.supports("Corners - Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Cards - Both Teams to Score", soccerContext));

        // Unsupported (Combos)
        assertFalse(handler.supports("1X2 & Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Total Goals & BTTS", soccerContext));
        assertFalse(handler.supports("Result and Both Teams to Score", soccerContext));

        // Unsupported (Other markets)
        assertFalse(handler.supports("Total Goals", soccerContext));
        assertFalse(handler.supports("1X2", soccerContext));
        assertFalse(handler.supports("Asian Handicap", soccerContext));
        assertFalse(handler.supports("Correct Score", soccerContext));
        assertFalse(handler.supports("", soccerContext));
        assertFalse(handler.supports(null, soccerContext));
    }

    @Test
    @DisplayName("Should correctly handle full match BTTS market")
    void testHandleFullMatchBtts() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_btts");
        market.setName("Both Teams to Score");
        market.setOutcomes(List.of(
                createOutcome("o_yes", "Yes", 1.75),
                createOutcome("o_no", "No", 2.05)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        OddItem itemYes = items.get(0);
        assertEquals("o_yes", itemYes.getFactorId());
        assertEquals("Both Teams to Score", itemYes.getGroupName());
        assertEquals(1.75, itemYes.getValue());
        assertInstanceOf(BinaryMarketBet.class, itemYes.getBetType());
        BinaryMarketBet betYes = (BinaryMarketBet) itemYes.getBetType();
        assertEquals(BetScope.FULL_MATCH, betYes.scope());
        assertEquals(BetSubject.MATCH, betYes.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, betYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betYes.outcome());
        assertEquals(StatType.MATCH, betYes.statType());

        OddItem itemNo = items.get(1);
        assertEquals("o_no", itemNo.getFactorId());
        assertEquals(2.05, itemNo.getValue());
        assertInstanceOf(BinaryMarketBet.class, itemNo.getBetType());
        BinaryMarketBet betNo = (BinaryMarketBet) itemNo.getBetType();
        assertEquals(BetScope.FULL_MATCH, betNo.scope());
        assertEquals(BetSubject.MATCH, betNo.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, betNo.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, betNo.outcome());
        assertEquals(StatType.MATCH, betNo.statType());
    }

    @Test
    @DisplayName("Should correctly handle 1st Half BTTS market")
    void testHandleHalf1Btts() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_btts_h1");
        market.setName("1st Half - Both Teams to Score");
        market.setOutcomes(List.of(
                createOutcome("o_h1_yes", "Yes", 4.20),
                createOutcome("o_h1_no", "No", 1.22)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        BinaryMarketBet betYes = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, betYes.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, betYes.outcome());

        BinaryMarketBet betNo = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, betNo.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, betNo.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Russian BTTS labels (Да / Нет)")
    void testHandleRussianBtts() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Спартак")
                .awayTeam("Зенит")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_ru_btts");
        market.setName("Обе команды забьют");
        market.setOutcomes(List.of(
                createOutcome("o_ru_yes", "Да", 1.85),
                createOutcome("o_ru_no", "Нет", 1.95)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        BinaryMarketBet betYes = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betYes.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, betYes.outcome());

        BinaryMarketBet betNo = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, betNo.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, betNo.outcome());
    }
}
