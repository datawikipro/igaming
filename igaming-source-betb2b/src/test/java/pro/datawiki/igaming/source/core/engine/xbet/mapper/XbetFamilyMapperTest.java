package pro.datawiki.igaming.source.core.engine.xbet.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;

import static org.junit.jupiter.api.Assertions.*;

class XbetFamilyMapperTest {

    private XbetFamilyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new XbetFamilyMapper(java.util.List.of(
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetMainResultStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetHandicapStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetTotalStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetHalvesStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetHalfTimeFullTimeStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetTeamToScoreStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetBinaryMarketStrategy(),
                new pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetStatsStrategy()
        ));
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("1xbet", SportType.FOOTBALL));
        assertTrue(mapper.supports("1xbit", SportType.FOOTBALL));
        assertTrue(mapper.supports("betandyou", SportType.FOOTBALL));
        assertTrue(mapper.supports("melbet", SportType.BASKETBALL));
        assertTrue(mapper.supports("melbet-com", SportType.FOOTBALL));
        assertTrue(mapper.supports("melbet.ru", SportType.HOCKEY));
        assertTrue(mapper.supports("22bet", SportType.FOOTBALL));
        assertTrue(mapper.supports("fansport", SportType.TENNIS));
        assertFalse(mapper.supports("pari", SportType.FOOTBALL));
        assertFalse(mapper.supports(null, SportType.FOOTBALL));
    }

    @Test
    void testMatchResultsAndDoubleChance() {
        BetType w1 = mapper.map("W1", "1", null);
        assertInstanceOf(MatchResultBet.class, w1);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1).outcome());

        BetType x = mapper.map("X", "2", null);
        assertInstanceOf(MatchResultBet.class, x);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) x).outcome());

        BetType w2 = mapper.map("W2", "3", null);
        assertInstanceOf(MatchResultBet.class, w2);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) w2).outcome());

        BetType dc1x = mapper.map("1X", "4", null);
        assertInstanceOf(MatchResultBet.class, dc1x);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1x).outcome());

        BetType dc12 = mapper.map("12", "5", null);
        assertInstanceOf(MatchResultBet.class, dc12);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12).outcome());

        BetType dcx2 = mapper.map("X2", "6", null);
        assertInstanceOf(MatchResultBet.class, dcx2);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcx2).outcome());
    }

    @Test
    void testHandicaps() {
        // T=7 is Handicap 1
        BetType h1 = mapper.map("H1(-1.5)", "7", -1.5);
        assertInstanceOf(HandicapBet.class, h1);
        HandicapBet handicap1 = (HandicapBet) h1;
        assertEquals(HandicapBet.Outcome.TEAM1, handicap1.outcome());
        assertEquals(-1.5, handicap1.param());

        // T=8 is Handicap 2
        BetType h2 = mapper.map("H2(1.5)", "8", 1.5);
        assertInstanceOf(HandicapBet.class, h2);
        HandicapBet handicap2 = (HandicapBet) h2;
        assertEquals(HandicapBet.Outcome.TEAM2, handicap2.outcome());
        assertEquals(1.5, handicap2.param());

        // T=7 with null param defaults to 0.0
        BetType h0 = mapper.map("H1(0)", "7", null);
        assertInstanceOf(HandicapBet.class, h0);
        assertEquals(0.0, ((HandicapBet) h0).param());
    }

    @Test
    void testMatchTotals() {
        // T=9 is Match Total Over
        BetType to = mapper.map("TO(2.5)", "9", 2.5);
        assertInstanceOf(TotalBet.class, to);
        TotalBet totalOver = (TotalBet) to;
        assertEquals(BetSubject.MATCH, totalOver.subject());
        assertEquals(TotalBet.Direction.OVER, totalOver.direction());
        assertEquals(2.5, totalOver.param());

        // T=10 is Match Total Under
        BetType tu = mapper.map("TU(2.5)", "10", 2.5);
        assertInstanceOf(TotalBet.class, tu);
        TotalBet totalUnder = (TotalBet) tu;
        assertEquals(BetSubject.MATCH, totalUnder.subject());
        assertEquals(TotalBet.Direction.UNDER, totalUnder.direction());
        assertEquals(2.5, totalUnder.param());
    }

    @Test
    void testIndividualTotals() {
        // T=11 is Individual Total 1 Over (NOT Handicap 1!)
        BetType it1o = mapper.map("IT1_O(1.5)", "11", 1.5);
        assertInstanceOf(TotalBet.class, it1o);
        TotalBet ind1Over = (TotalBet) it1o;
        assertEquals(BetSubject.TEAM1, ind1Over.subject());
        assertEquals(TotalBet.Direction.OVER, ind1Over.direction());
        assertEquals(1.5, ind1Over.param());

        // T=12 is Individual Total 1 Under (NOT Handicap 2!)
        BetType it1u = mapper.map("IT1_U(1.5)", "12", 1.5);
        assertInstanceOf(TotalBet.class, it1u);
        TotalBet ind1Under = (TotalBet) it1u;
        assertEquals(BetSubject.TEAM1, ind1Under.subject());
        assertEquals(TotalBet.Direction.UNDER, ind1Under.direction());
        assertEquals(1.5, ind1Under.param());

        // T=13 is Individual Total 2 Over
        BetType it2o = mapper.map("IT2_O(0.5)", "13", 0.5);
        assertInstanceOf(TotalBet.class, it2o);
        TotalBet ind2Over = (TotalBet) it2o;
        assertEquals(BetSubject.TEAM2, ind2Over.subject());
        assertEquals(TotalBet.Direction.OVER, ind2Over.direction());
        assertEquals(0.5, ind2Over.param());

        // T=14 is Individual Total 2 Under
        BetType it2u = mapper.map("IT2_U(0.5)", "14", 0.5);
        assertInstanceOf(TotalBet.class, it2u);
        TotalBet ind2Under = (TotalBet) it2u;
        assertEquals(BetSubject.TEAM2, ind2Under.subject());
        assertEquals(TotalBet.Direction.UNDER, ind2Under.direction());
        assertEquals(0.5, ind2Under.param());
    }

    @Test
    void testOtMoneylineAndBtts() {
        BetType ot1 = mapper.map("W1_OT", "401", null);
        assertInstanceOf(MatchResultBet.class, ot1);
        assertEquals(BetScope.FULL_MATCH_INCLUDING_OT, ((MatchResultBet) ot1).scope());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) ot1).outcome());

        BetType ot2 = mapper.map("W2_OT", "402", null);
        assertInstanceOf(MatchResultBet.class, ot2);
        assertEquals(BetScope.FULL_MATCH_INCLUDING_OT, ((MatchResultBet) ot2).scope());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) ot2).outcome());

        BetType bttsY = mapper.map("BTTS_YES", "180", null);
        assertInstanceOf(BinaryMarketBet.class, bttsY);
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) bttsY).outcome());

        BetType bttsN = mapper.map("BTTS_NO", "181", null);
        assertInstanceOf(BinaryMarketBet.class, bttsN);
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) bttsN).outcome());
    }

    @Test
    void testHalfTimeMarkets() {
        // 1H 1X2
        BetType h1w1 = mapper.map("1H_W1", "15", null);
        assertInstanceOf(MatchResultBet.class, h1w1);
        assertEquals(BetScope.HALF_1, ((MatchResultBet) h1w1).scope());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) h1w1).outcome());

        // 1H Total Over
        BetType h1to = mapper.map("1H_TO(1.5)", "45", 1.5);
        assertInstanceOf(TotalBet.class, h1to);
        assertEquals(BetScope.HALF_1, ((TotalBet) h1to).scope());
        assertEquals(1.5, ((TotalBet) h1to).param());

        // 2H 1X2
        BetType h2w2 = mapper.map("2H_W2", "63", null);
        assertInstanceOf(MatchResultBet.class, h2w2);
        assertEquals(BetScope.HALF_2, ((MatchResultBet) h2w2).scope());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) h2w2).outcome());
    }

    @Test
    void testHtFtAndSpecialMarkets() {
        // HT/FT 1/1
        BetType htft11 = mapper.map("HTFT_1_1", "21", null);
        assertInstanceOf(HalfTimeFullTimeBet.class, htft11);
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, ((HalfTimeFullTimeBet) htft11).outcome());

        // HT/FT X/2
        BetType htftx2 = mapper.map("HTFT_X_2", "26", null);
        assertInstanceOf(HalfTimeFullTimeBet.class, htftx2);
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W2, ((HalfTimeFullTimeBet) htftx2).outcome());

        // Team 1 to score YES
        BetType t1Yes = mapper.map("T1_TO_SCORE_YES", "178", null);
        assertInstanceOf(TeamToScoreBet.class, t1Yes);
        assertEquals(BetSubject.TEAM1, ((TeamToScoreBet) t1Yes).subject());
        assertEquals(TeamToScoreBet.Outcome.YES, ((TeamToScoreBet) t1Yes).outcome());

        // Total Even
        BetType even = mapper.map("TOTAL_EVEN", "186", null);
        assertInstanceOf(BinaryMarketBet.class, even);
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, ((BinaryMarketBet) even).marketType());
        assertEquals(BinaryMarketBet.Outcome.EVEN, ((BinaryMarketBet) even).outcome());

        // Corners Total Over
        BetType cornerTo = mapper.map("CORNERS_TO(9.5)", "1711", 9.5);
        assertInstanceOf(TotalBet.class, cornerTo);
        assertEquals(StatType.CORNERS, ((TotalBet) cornerTo).statType());
        assertEquals(9.5, ((TotalBet) cornerTo).param());
    }
}

