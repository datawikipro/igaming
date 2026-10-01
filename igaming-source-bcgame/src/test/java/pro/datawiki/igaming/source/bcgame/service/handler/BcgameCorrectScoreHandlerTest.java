package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameCorrectScoreHandlerTest {

    private BcgameCorrectScoreHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameCorrectScoreHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported Correct Score markets")
    void testSupports() {
        BcgameMarketContext soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Inter")
                .awayTeam("Milan")
                .build();

        // Supported
        assertTrue(handler.supports("Correct Score", soccerContext));
        assertTrue(handler.supports("Exact Score", soccerContext));
        assertTrue(handler.supports("1st Half Correct Score", soccerContext));
        assertTrue(handler.supports("2nd Half - Correct Score", soccerContext));
        assertTrue(handler.supports("Точный счет", soccerContext));
        assertTrue(handler.supports("Точный счёт", soccerContext));
        assertTrue(handler.supports("CS", soccerContext));

        // Unsupported (Stats)
        assertFalse(handler.supports("Corners Correct Score", soccerContext));

        // Unsupported (Other markets)
        assertFalse(handler.supports("Total Goals", soccerContext));
        assertFalse(handler.supports("Both Teams to Score", soccerContext));
        assertFalse(handler.supports("1X2", soccerContext));
        assertFalse(handler.supports("Asian Handicap", soccerContext));
        assertFalse(handler.supports("", soccerContext));
        assertFalse(handler.supports(null, soccerContext));
    }

    @Test
    @DisplayName("Should correctly handle full match Correct Score with exact scores and Any Other Score")
    void testHandleFullMatchCorrectScore() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_cs");
        market.setName("Correct Score");
        market.setOutcomes(List.of(
                createOutcome("cs_10", "1 - 0", 6.50),
                createOutcome("cs_21", "2:1", 8.00),
                createOutcome("cs_00", "0-0", 9.50),
                createOutcome("cs_aos", "Any Other Score", 4.20)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(4, items.size());

        OddItem item10 = items.get(0);
        assertEquals("cs_10", item10.getFactorId());
        assertEquals(6.50, item10.getValue());
        assertInstanceOf(CorrectScoreBet.class, item10.getBetType());
        CorrectScoreBet bet10 = (CorrectScoreBet) item10.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet10.scope());
        assertEquals(1, bet10.score1());
        assertEquals(0, bet10.score2());
        assertFalse(bet10.isAnyOtherScore());

        CorrectScoreBet bet21 = (CorrectScoreBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet21.scope());
        assertEquals(2, bet21.score1());
        assertEquals(1, bet21.score2());
        assertFalse(bet21.isAnyOtherScore());

        CorrectScoreBet bet00 = (CorrectScoreBet) items.get(2).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet00.scope());
        assertEquals(0, bet00.score1());
        assertEquals(0, bet00.score2());
        assertFalse(bet00.isAnyOtherScore());

        OddItem itemAos = items.get(3);
        assertEquals("cs_aos", itemAos.getFactorId());
        assertEquals(4.20, itemAos.getValue());
        CorrectScoreBet betAos = (CorrectScoreBet) itemAos.getBetType();
        assertEquals(BetScope.FULL_MATCH, betAos.scope());
        assertTrue(betAos.isAnyOtherScore());
    }

    @Test
    @DisplayName("Should correctly handle 1st Half Correct Score")
    void testHandleHalf1CorrectScore() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_h1_cs");
        market.setName("1st Half Correct Score");
        market.setOutcomes(List.of(
                createOutcome("h1_00", "0:0", 2.80),
                createOutcome("h1_10", "1:0", 3.50),
                createOutcome("h1_other", "Other", 5.00)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(3, items.size());

        CorrectScoreBet bet00 = (CorrectScoreBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, bet00.scope());
        assertEquals(0, bet00.score1());
        assertEquals(0, bet00.score2());
        assertFalse(bet00.isAnyOtherScore());

        CorrectScoreBet bet10 = (CorrectScoreBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, bet10.scope());
        assertEquals(1, bet10.score1());
        assertEquals(0, bet10.score2());

        CorrectScoreBet betOther = (CorrectScoreBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, betOther.scope());
        assertTrue(betOther.isAnyOtherScore());
    }

    @Test
    @DisplayName("Should correctly handle Russian outcome names (Точный счет / Любой другой)")
    void testHandleRussianCorrectScore() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Спартак")
                .awayTeam("ЦСКА")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_ru_cs");
        market.setName("Точный счет");
        market.setOutcomes(List.of(
                createOutcome("ru_cs_20", "2:0", 7.20),
                createOutcome("ru_cs_other", "Любой другой", 4.50)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        CorrectScoreBet bet20 = (CorrectScoreBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet20.scope());
        assertEquals(2, bet20.score1());
        assertEquals(0, bet20.score2());
        assertFalse(bet20.isAnyOtherScore());

        CorrectScoreBet betOther = (CorrectScoreBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, betOther.scope());
        assertTrue(betOther.isAnyOtherScore());
    }
}
