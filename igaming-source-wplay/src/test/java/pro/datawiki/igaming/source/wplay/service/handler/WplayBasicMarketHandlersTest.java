package pro.datawiki.igaming.source.wplay.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WplayBasicMarketHandlersTest {

    private WplayEventDto event;

    @BeforeEach
    void setUp() {
        event = WplayEventDto.builder()
                .id("ev-33035765")
                .homeTeam("Atlético Nacional")
                .awayTeam("Millonarios")
                .sportName("Fútbol")
                .leagueName("Liga BetPlay Dimayor")
                .build();
    }

    @Test
    void testMatchResultMarketHandler() {
        MatchResultMarketHandler handler = new MatchResultMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Resultado del partido")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Atlético Nacional").decimal(1.95).build(),
                        WplayOutcomeDto.builder().name("Empate").decimal(3.20).build(),
                        WplayOutcomeDto.builder().name("Millonarios").decimal(4.10).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));
        assertFalse(handler.supports(market, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(1.95, items.get(0).getValue());
        assertInstanceOf(MatchResultBet.class, items.get(0).getBetType());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) items.get(0).getBetType()).outcome());

        assertEquals(3.20, items.get(1).getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) items.get(1).getBetType()).outcome());

        assertEquals(4.10, items.get(2).getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) items.get(2).getBetType()).outcome());
    }

    @Test
    void testDoubleChanceMarketHandler() {
        DoubleChanceMarketHandler handler = new DoubleChanceMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Doble oportunidad")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("1X").decimal(1.22).build(),
                        WplayOutcomeDto.builder().name("12").decimal(1.30).build(),
                        WplayOutcomeDto.builder().name("X2").decimal(1.75).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals("1X", items.get(0).getBetType().code());
        assertEquals("12", items.get(1).getBetType().code());
        assertEquals("X2", items.get(2).getBetType().code());
    }

    @Test
    void testDrawNoBetMarketHandler() {
        DrawNoBetMarketHandler handler = new DrawNoBetMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Apuesta sin empate")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Atlético Nacional").decimal(1.45).build(),
                        WplayOutcomeDto.builder().name("Millonarios").decimal(2.60).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertInstanceOf(HandicapBet.class, items.get(0).getBetType());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(0.0, h1.points());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(0.0, h2.points());
    }

    @Test
    void testTotalMarketHandler() {
        TotalMarketHandler handler = new TotalMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Total de goles")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Más de 2.5").decimal(1.85).build(),
                        WplayOutcomeDto.builder().name("Menos de 2.5").decimal(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertInstanceOf(TotalBet.class, items.get(0).getBetType());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(TotalBet.TotalType.OVER, over.type());
        assertEquals(2.5, over.points());
        assertEquals(BetSubject.MATCH, over.subject());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(TotalBet.TotalType.UNDER, under.type());
        assertEquals(2.5, under.points());
    }

    @Test
    void testTotalMarketHandlerTeamTotal() {
        TotalMarketHandler handler = new TotalMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Total Local")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Más de 1.5").decimal(2.10).build(),
                        WplayOutcomeDto.builder().name("Menos de 1.5").decimal(1.68).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, over.subject());
        assertEquals(1.5, over.points());
    }

    @Test
    void testHandicapMarketHandler() {
        HandicapMarketHandler handler = new HandicapMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Hándicap")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Atlético Nacional (-1.5)").handicap(-1.5).decimal(2.70).build(),
                        WplayOutcomeDto.builder().name("Millonarios (+1.5)").handicap(1.5).decimal(1.42).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertInstanceOf(HandicapBet.class, items.get(0).getBetType());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.points());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.points());
    }

    @Test
    void testBothTeamsToScoreMarketHandler() {
        BothTeamsToScoreMarketHandler handler = new BothTeamsToScoreMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Ambos equipos marcarán")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("Sí").decimal(1.78).build(),
                        WplayOutcomeDto.builder().name("No").decimal(2.02).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertInstanceOf(BinaryMarketBet.class, items.get(0).getBetType());
        BinaryMarketBet bttsYes = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BinaryMarketBet.Outcome.YES, bttsYes.outcome());
        assertEquals(BinaryMarketBet.MarketType.BTTS, bttsYes.marketType());

        BinaryMarketBet bttsNo = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, bttsNo.outcome());
    }

    @Test
    void testCorrectScoreMarketHandler() {
        CorrectScoreMarketHandler handler = new CorrectScoreMarketHandler();

        WplayMarketDto market = WplayMarketDto.builder()
                .name("Marcador exacto")
                .outcomes(List.of(
                        WplayOutcomeDto.builder().name("2 - 1").decimal(8.50).build(),
                        WplayOutcomeDto.builder().name("0:0").decimal(7.00).build(),
                        WplayOutcomeDto.builder().name("Cualquier otro").decimal(15.0).build()
                ))
                .build();

        assertTrue(handler.supports(market, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, event, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertInstanceOf(CorrectScoreBet.class, items.get(0).getBetType());
        CorrectScoreBet score1 = (CorrectScoreBet) items.get(0).getBetType();
        assertEquals(2, score1.score1());
        assertEquals(1, score1.score2());
        assertFalse(score1.isOther());

        CorrectScoreBet score2 = (CorrectScoreBet) items.get(1).getBetType();
        assertEquals(0, score2.score1());
        assertEquals(0, score2.score2());
        assertFalse(score2.isOther());

        CorrectScoreBet scoreOther = (CorrectScoreBet) items.get(2).getBetType();
        assertTrue(scoreOther.isOther());
    }
}
