package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameStatsMarketHandlerTest {

    private BcgameStatsMarketHandler handler;
    private BcgameMarketContext soccerContext;

    @BeforeEach
    void setUp() {
        handler = new BcgameStatsMarketHandler();
        soccerContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        return outcome;
    }

    private BcgameOutcomeDto createOutcomeWithParam(String id, String name, Double odds, Double param) {
        BcgameOutcomeDto outcome = createOutcome(id, name, odds);
        outcome.setParam(param);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported statistics markets")
    void testSupports() {
        // Supported Corners
        assertTrue(handler.supports("Corners", soccerContext));
        assertTrue(handler.supports("Total Corners", soccerContext));
        assertTrue(handler.supports("Corner Handicap", soccerContext));
        assertTrue(handler.supports("1st Half - Total Corners", soccerContext));
        assertTrue(handler.supports("Corners 1X2", soccerContext));
        assertTrue(handler.supports("Most Corners", soccerContext));
        assertTrue(handler.supports("Тотал угловых", soccerContext));

        // Supported Yellow Cards and Cards
        assertTrue(handler.supports("Yellow Cards", soccerContext));
        assertTrue(handler.supports("Total Yellow Cards", soccerContext));
        assertTrue(handler.supports("Yellow Card Handicap", soccerContext));
        assertTrue(handler.supports("Cards Total", soccerContext));
        assertTrue(handler.supports("Booking Points", soccerContext));
        assertTrue(handler.supports("Желтые карточки", soccerContext));
        assertTrue(handler.supports("Тотал ЖК", soccerContext));

        // Supported Fouls
        assertTrue(handler.supports("Fouls", soccerContext));
        assertTrue(handler.supports("Total Fouls", soccerContext));
        assertTrue(handler.supports("Fouls Handicap", soccerContext));
        assertTrue(handler.supports("Most Fouls", soccerContext));
        assertTrue(handler.supports("Фолы", soccerContext));

        // Supported Offsides
        assertTrue(handler.supports("Offsides", soccerContext));
        assertTrue(handler.supports("Total Offsides", soccerContext));
        assertTrue(handler.supports("Offside Handicap", soccerContext));
        assertTrue(handler.supports("Most Offsides", soccerContext));
        assertTrue(handler.supports("Офсайды", soccerContext));

        // Supported Shots on Target
        assertTrue(handler.supports("Shots on Target", soccerContext));
        assertTrue(handler.supports("Total Shots on Target", soccerContext));
        assertTrue(handler.supports("Shots on Target Handicap", soccerContext));
        assertTrue(handler.supports("Most Shots on Target", soccerContext));
        assertTrue(handler.supports("Удары в створ", soccerContext));

        // Unsupported general markets (handled by other handlers)
        assertFalse(handler.supports("Total Goals", soccerContext));
        assertFalse(handler.supports("Asian Handicap", soccerContext));
        assertFalse(handler.supports("Match Result", soccerContext));
        assertFalse(handler.supports("Both Teams to Score", soccerContext));
        assertFalse(handler.supports("Correct Score", soccerContext));

        // Unsupported esports markets with keywords like map, kill, tower, roshan
        assertFalse(handler.supports("Map 1 Total Kills", soccerContext));
        assertFalse(handler.supports("Map 2 Roshan", soccerContext));
    }

    @Test
    @DisplayName("Should resolve correct StatType for various market names")
    void testResolveStatType() {
        assertEquals(StatType.CORNERS, handler.resolveStatType("Total Corners"));
        assertEquals(StatType.CORNERS, handler.resolveStatType("Corner Handicap"));
        assertEquals(StatType.CORNERS, handler.resolveStatType("Тотал угловых"));

        assertEquals(StatType.YELLOW_CARDS, handler.resolveStatType("Total Yellow Cards"));
        assertEquals(StatType.YELLOW_CARDS, handler.resolveStatType("Yellow Card Handicap"));
        assertEquals(StatType.YELLOW_CARDS, handler.resolveStatType("Желтые карточки фора"));
        assertEquals(StatType.YELLOW_CARDS, handler.resolveStatType("Тотал ЖК"));

        assertEquals(StatType.CARDS, handler.resolveStatType("Total Cards"));
        assertEquals(StatType.CARDS, handler.resolveStatType("Booking Points"));
        assertEquals(StatType.CARDS, handler.resolveStatType("Карточки тотал"));

        assertEquals(StatType.FOULS, handler.resolveStatType("Total Fouls"));
        assertEquals(StatType.FOULS, handler.resolveStatType("Фолы 1X2"));

        assertEquals(StatType.OFFSIDES, handler.resolveStatType("Total Offsides"));
        assertEquals(StatType.OFFSIDES, handler.resolveStatType("Офсайды"));

        assertEquals(StatType.SHOTS_ON_TARGET, handler.resolveStatType("Total Shots on Target"));
        assertEquals(StatType.SHOTS_ON_TARGET, handler.resolveStatType("Shots on Target 1X2"));
        assertEquals(StatType.SHOTS_ON_TARGET, handler.resolveStatType("Удары в створ"));
    }

    @Test
    @DisplayName("Should handle Corners Over/Under Total markets")
    void testHandleCornersTotal() {
        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Total Corners (9.5)");
        market.setOutcomes(List.of(
                createOutcome("c_ov", "Over 9.5", 1.80),
                createOutcome("c_un", "Under 9.5", 2.00)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, soccerContext, items);

        assertEquals(2, items.size());

        OddItem itemOver = items.get(0);
        assertEquals("c_ov", itemOver.getFactorId());
        assertEquals(1.80, itemOver.getValue());
        assertInstanceOf(TotalBet.class, itemOver.getBetType());
        TotalBet betOver = (TotalBet) itemOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, betOver.scope());
        assertEquals(BetSubject.MATCH, betOver.subject());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());
        assertEquals(9.5, betOver.param());
        assertFalse(betOver.isAsian());
        assertEquals(StatType.CORNERS, betOver.statType());

        OddItem itemUnder = items.get(1);
        TotalBet betUnder = (TotalBet) itemUnder.getBetType();
        assertEquals(BetScope.FULL_MATCH, betUnder.scope());
        assertEquals(BetSubject.MATCH, betUnder.subject());
        assertEquals(TotalBet.Direction.UNDER, betUnder.direction());
        assertEquals(9.5, betUnder.param());
        assertEquals(StatType.CORNERS, betUnder.statType());
    }

    @Test
    @DisplayName("Should handle 1st Half Corners and Individual Team Corners")
    void testHandleHalfAndIndividualCorners() {
        // 1st Half Corners
        BcgameMarketDto marketH1 = new BcgameMarketDto();
        marketH1.setName("1st Half Total Corners");
        marketH1.setOutcomes(List.of(
                createOutcome("h1_ov", "Over 4.5", 1.90),
                createOutcome("h1_un", "Under 4.5", 1.90)
        ));

        List<OddItem> itemsH1 = new ArrayList<>();
        handler.handle(marketH1, soccerContext, itemsH1);

        assertEquals(2, itemsH1.size());
        TotalBet betH1 = (TotalBet) itemsH1.get(0).getBetType();
        assertEquals(BetScope.HALF_1, betH1.scope());
        assertEquals(StatType.CORNERS, betH1.statType());

        // Individual Team Corners (Arsenal = Home/Team1)
        BcgameMarketDto marketT1 = new BcgameMarketDto();
        marketT1.setName("Arsenal Total Corners");
        marketT1.setOutcomes(List.of(
                createOutcome("t1_ov", "Over 5.5", 1.75),
                createOutcome("t1_un", "Under 5.5", 2.05)
        ));

        List<OddItem> itemsT1 = new ArrayList<>();
        handler.handle(marketT1, soccerContext, itemsT1);

        assertEquals(2, itemsT1.size());
        TotalBet betT1 = (TotalBet) itemsT1.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, betT1.subject());
        assertEquals(5.5, betT1.param());
        assertEquals(StatType.CORNERS, betT1.statType());
    }

    @Test
    @DisplayName("Should handle Corner Handicap markets")
    void testHandleCornerHandicap() {
        BcgameMarketDto market = new BcgameMarketDto();
        market.setName("Corner Handicap");
        market.setOutcomes(List.of(
                createOutcome("h_home", "Arsenal (-1.5)", 1.95),
                createOutcome("h_away", "Chelsea (+1.5)", 1.85)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, soccerContext, items);

        assertEquals(2, items.size());

        OddItem itemHome = items.get(0);
        assertInstanceOf(HandicapBet.class, itemHome.getBetType());
        HandicapBet betHome = (HandicapBet) itemHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betHome.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betHome.outcome());
        assertEquals(-1.5, betHome.param());
        assertFalse(betHome.isAsian());
        assertEquals(StatType.CORNERS, betHome.statType());

        OddItem itemAway = items.get(1);
        HandicapBet betAway = (HandicapBet) itemAway.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, betAway.outcome());
        assertEquals(1.5, betAway.param());
        assertEquals(StatType.CORNERS, betAway.statType());
    }

    @Test
    @DisplayName("Should handle Corners 1X2 and Double Chance")
    void testHandleCornersResultAndDoubleChance() {
        // Corners 1X2 (3-Way)
        BcgameMarketDto market1X2 = new BcgameMarketDto();
        market1X2.setName("Most Corners");
        market1X2.setOutcomes(List.of(
                createOutcome("m1", "Arsenal", 1.60),
                createOutcome("mx", "Draw", 7.50),
                createOutcome("m2", "Chelsea", 3.20)
        ));

        List<OddItem> items1X2 = new ArrayList<>();
        handler.handle(market1X2, soccerContext, items1X2);

        assertEquals(3, items1X2.size());
        MatchResultBet bet1 = (MatchResultBet) items1X2.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.CORNERS, bet1.statType());

        MatchResultBet betX = (MatchResultBet) items1X2.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());
        assertEquals(StatType.CORNERS, betX.statType());

        MatchResultBet bet2 = (MatchResultBet) items1X2.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
        assertEquals(StatType.CORNERS, bet2.statType());

        // Corners Double Chance
        BcgameMarketDto marketDC = new BcgameMarketDto();
        marketDC.setName("Corners Double Chance");
        marketDC.setOutcomes(List.of(
                createOutcome("dc_1x", "1X", 1.25),
                createOutcome("dc_12", "12", 1.15),
                createOutcome("dc_x2", "X2", 2.10)
        ));

        List<OddItem> itemsDC = new ArrayList<>();
        handler.handle(marketDC, soccerContext, itemsDC);

        assertEquals(3, itemsDC.size());
        MatchResultBet bet1X = (MatchResultBet) itemsDC.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, bet1X.outcome());
        assertEquals(StatType.CORNERS, bet1X.statType());

        MatchResultBet bet12 = (MatchResultBet) itemsDC.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, bet12.outcome());

        MatchResultBet betX2 = (MatchResultBet) itemsDC.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, betX2.outcome());
    }

    @Test
    @DisplayName("Should handle Yellow Cards and Cards markets")
    void testHandleYellowCardsAndCards() {
        // Yellow Cards Total
        BcgameMarketDto marketYTot = new BcgameMarketDto();
        marketYTot.setName("Total Yellow Cards");
        marketYTot.setOutcomes(List.of(
                createOutcome("yc_ov", "Over 3.5", 1.70),
                createOutcome("yc_un", "Under 3.5", 2.10)
        ));

        List<OddItem> itemsYTot = new ArrayList<>();
        handler.handle(marketYTot, soccerContext, itemsYTot);

        assertEquals(2, itemsYTot.size());
        TotalBet betYTot = (TotalBet) itemsYTot.get(0).getBetType();
        assertEquals(StatType.YELLOW_CARDS, betYTot.statType());
        assertEquals(3.5, betYTot.param());

        // Yellow Cards Handicap
        BcgameMarketDto marketYHdp = new BcgameMarketDto();
        marketYHdp.setName("Yellow Card Handicap");
        marketYHdp.setOutcomes(List.of(
                createOutcome("yh_1", "1 (0)", 1.90),
                createOutcome("yh_2", "2 (0)", 1.90)
        ));

        List<OddItem> itemsYHdp = new ArrayList<>();
        handler.handle(marketYHdp, soccerContext, itemsYHdp);

        assertEquals(2, itemsYHdp.size());
        HandicapBet betYHdp = (HandicapBet) itemsYHdp.get(0).getBetType();
        assertEquals(StatType.YELLOW_CARDS, betYHdp.statType());
        assertEquals(0.0, betYHdp.param());

        // Most Yellow Cards (1X2)
        BcgameMarketDto marketY1X2 = new BcgameMarketDto();
        marketY1X2.setName("Most Yellow Cards");
        marketY1X2.setOutcomes(List.of(
                createOutcome("my1", "1", 2.20),
                createOutcome("myx", "X", 3.80),
                createOutcome("my2", "2", 2.50)
        ));

        List<OddItem> itemsY1X2 = new ArrayList<>();
        handler.handle(marketY1X2, soccerContext, itemsY1X2);

        assertEquals(3, itemsY1X2.size());
        assertEquals(StatType.YELLOW_CARDS, ((MatchResultBet) itemsY1X2.get(0).getBetType()).statType());

        // Booking Points / Cards Total
        BcgameMarketDto marketCards = new BcgameMarketDto();
        marketCards.setName("Total Cards");
        marketCards.setOutcomes(List.of(
                createOutcome("c_ov", "Over 4.5", 1.85),
                createOutcome("c_un", "Under 4.5", 1.95)
        ));

        List<OddItem> itemsCards = new ArrayList<>();
        handler.handle(marketCards, soccerContext, itemsCards);

        assertEquals(2, itemsCards.size());
        TotalBet betCards = (TotalBet) itemsCards.get(0).getBetType();
        assertEquals(StatType.CARDS, betCards.statType());
    }

    @Test
    @DisplayName("Should handle Fouls markets (Total, Handicap, 1X2)")
    void testHandleFouls() {
        // Total Fouls
        BcgameMarketDto marketTot = new BcgameMarketDto();
        marketTot.setName("Total Fouls");
        marketTot.setOutcomes(List.of(
                createOutcome("f_ov", "Over 22.5", 1.85),
                createOutcome("f_un", "Under 22.5", 1.95)
        ));

        List<OddItem> itemsTot = new ArrayList<>();
        handler.handle(marketTot, soccerContext, itemsTot);

        assertEquals(2, itemsTot.size());
        TotalBet betTot = (TotalBet) itemsTot.get(0).getBetType();
        assertEquals(StatType.FOULS, betTot.statType());
        assertEquals(22.5, betTot.param());

        // Fouls Handicap
        BcgameMarketDto marketHdp = new BcgameMarketDto();
        marketHdp.setName("Fouls Handicap");
        marketHdp.setOutcomes(List.of(
                createOutcome("fh_1", "Arsenal (-2.5)", 1.90),
                createOutcome("fh_2", "Chelsea (+2.5)", 1.90)
        ));

        List<OddItem> itemsHdp = new ArrayList<>();
        handler.handle(marketHdp, soccerContext, itemsHdp);

        assertEquals(2, itemsHdp.size());
        HandicapBet betHdp = (HandicapBet) itemsHdp.get(0).getBetType();
        assertEquals(StatType.FOULS, betHdp.statType());
        assertEquals(-2.5, betHdp.param());

        // Most Fouls (1X2)
        BcgameMarketDto market1X2 = new BcgameMarketDto();
        market1X2.setName("Most Fouls");
        market1X2.setOutcomes(List.of(
                createOutcome("mf1", "Arsenal", 1.80),
                createOutcome("mfx", "Draw", 9.00),
                createOutcome("mf2", "Chelsea", 2.20)
        ));

        List<OddItem> items1X2 = new ArrayList<>();
        handler.handle(market1X2, soccerContext, items1X2);

        assertEquals(3, items1X2.size());
        assertEquals(StatType.FOULS, ((MatchResultBet) items1X2.get(0).getBetType()).statType());
    }

    @Test
    @DisplayName("Should handle Offsides markets (Total, Handicap, 1X2)")
    void testHandleOffsides() {
        // Total Offsides
        BcgameMarketDto marketTot = new BcgameMarketDto();
        marketTot.setName("Total Offsides");
        marketTot.setOutcomes(List.of(
                createOutcome("off_ov", "Over 3.5", 1.75),
                createOutcome("off_un", "Under 3.5", 2.05)
        ));

        List<OddItem> itemsTot = new ArrayList<>();
        handler.handle(marketTot, soccerContext, itemsTot);

        assertEquals(2, itemsTot.size());
        TotalBet betTot = (TotalBet) itemsTot.get(0).getBetType();
        assertEquals(StatType.OFFSIDES, betTot.statType());
        assertEquals(3.5, betTot.param());

        // Offside Handicap
        BcgameMarketDto marketHdp = new BcgameMarketDto();
        marketHdp.setName("Offside Handicap");
        marketHdp.setOutcomes(List.of(
                createOutcome("oh_1", "1 (-1)", 2.10),
                createOutcome("oh_2", "2 (+1)", 1.70)
        ));

        List<OddItem> itemsHdp = new ArrayList<>();
        handler.handle(marketHdp, soccerContext, itemsHdp);

        assertEquals(2, itemsHdp.size());
        HandicapBet betHdp = (HandicapBet) itemsHdp.get(0).getBetType();
        assertEquals(StatType.OFFSIDES, betHdp.statType());
        assertEquals(-1.0, betHdp.param());

        // Most Offsides (1X2)
        BcgameMarketDto market1X2 = new BcgameMarketDto();
        market1X2.setName("Most Offsides");
        market1X2.setOutcomes(List.of(
                createOutcome("mo1", "Home", 2.00),
                createOutcome("mox", "Draw", 4.50),
                createOutcome("mo2", "Away", 2.30)
        ));

        List<OddItem> items1X2 = new ArrayList<>();
        handler.handle(market1X2, soccerContext, items1X2);

        assertEquals(3, items1X2.size());
        assertEquals(StatType.OFFSIDES, ((MatchResultBet) items1X2.get(0).getBetType()).statType());
    }

    @Test
    @DisplayName("Should handle Shots on Target markets (Total, Handicap, 1X2)")
    void testHandleShotsOnTarget() {
        // Total Shots on Target
        BcgameMarketDto marketTot = new BcgameMarketDto();
        marketTot.setName("Total Shots on Target");
        marketTot.setOutcomes(List.of(
                createOutcome("sot_ov", "Over 8.5", 1.85),
                createOutcome("sot_un", "Under 8.5", 1.95)
        ));

        List<OddItem> itemsTot = new ArrayList<>();
        handler.handle(marketTot, soccerContext, itemsTot);

        assertEquals(2, itemsTot.size());
        TotalBet betTot = (TotalBet) itemsTot.get(0).getBetType();
        assertEquals(StatType.SHOTS_ON_TARGET, betTot.statType());
        assertEquals(8.5, betTot.param());

        // Shots on Target Handicap
        BcgameMarketDto marketHdp = new BcgameMarketDto();
        marketHdp.setName("Shots on Target Handicap");
        marketHdp.setOutcomes(List.of(
                createOutcome("soth_1", "Arsenal (-1.5)", 1.95),
                createOutcome("soth_2", "Chelsea (+1.5)", 1.85)
        ));

        List<OddItem> itemsHdp = new ArrayList<>();
        handler.handle(marketHdp, soccerContext, itemsHdp);

        assertEquals(2, itemsHdp.size());
        HandicapBet betHdp = (HandicapBet) itemsHdp.get(0).getBetType();
        assertEquals(StatType.SHOTS_ON_TARGET, betHdp.statType());
        assertEquals(-1.5, betHdp.param());

        // Most Shots on Target (1X2)
        BcgameMarketDto market1X2 = new BcgameMarketDto();
        market1X2.setName("Most Shots on Target");
        market1X2.setOutcomes(List.of(
                createOutcome("msot1", "1", 1.70),
                createOutcome("msotx", "X", 8.00),
                createOutcome("msot2", "2", 2.60)
        ));

        List<OddItem> items1X2 = new ArrayList<>();
        handler.handle(market1X2, soccerContext, items1X2);

        assertEquals(3, items1X2.size());
        assertEquals(StatType.SHOTS_ON_TARGET, ((MatchResultBet) items1X2.get(0).getBetType()).statType());
    }

    @Test
    @DisplayName("Should handle Russian market names and Quarter Asian lines")
    void testRussianAndAsianStats() {
        // Russian corners total
        BcgameMarketDto marketRu = new BcgameMarketDto();
        marketRu.setName("Тотал угловых");
        marketRu.setOutcomes(List.of(
                createOutcome("ru_tb", "ТБ (9.75)", 1.90),
                createOutcome("ru_tm", "ТМ (9.75)", 1.90)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(marketRu, soccerContext, items);

        assertEquals(2, items.size());
        TotalBet betRu = (TotalBet) items.get(0).getBetType();
        assertEquals(StatType.CORNERS, betRu.statType());
        assertEquals(9.75, betRu.param());
        assertTrue(betRu.isAsian(), "Quarter Asian 9.75 should have isAsian=true");

        // Russian yellow cards handicap
        BcgameMarketDto marketRuHdp = new BcgameMarketDto();
        marketRuHdp.setName("Фора по желтым карточкам");
        marketRuHdp.setOutcomes(List.of(
                createOutcome("ru_f1", "Ф1 (-0.25)", 1.85),
                createOutcome("ru_f2", "Ф2 (+0.25)", 1.95)
        ));

        List<OddItem> itemsHdp = new ArrayList<>();
        handler.handle(marketRuHdp, soccerContext, itemsHdp);

        assertEquals(2, itemsHdp.size());
        HandicapBet betHdp = (HandicapBet) itemsHdp.get(0).getBetType();
        assertEquals(StatType.YELLOW_CARDS, betHdp.statType());
        assertEquals(-0.25, betHdp.param());
        assertTrue(betHdp.isAsian(), "Quarter Asian -0.25 should have isAsian=true");
    }
}
