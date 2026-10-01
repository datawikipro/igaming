package pro.datawiki.igaming.source.smarkets.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SmarketsOddsMapperTest {

    private SmarketsOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SmarketsOddsMapper();
    }

    private SmarketsContractQuotes createQuote(double odds) {
        SmarketsContractQuotes quotes = new SmarketsContractQuotes();
        SmarketsQuoteEntry entry = new SmarketsQuoteEntry();
        entry.setPrice((int) Math.round(10000.0 / odds));
        quotes.setBids(List.of(entry));
        return quotes;
    }

    @Test
    void testPriceConversionToOdds() {
        SmarketsQuoteEntry quote = new SmarketsQuoteEntry();
        quote.setPrice(5000); // 50%
        assertEquals(2.0, quote.getDecimalOdds(), 0.001);

        quote.setPrice(2500); // 25%
        assertEquals(4.0, quote.getDecimalOdds(), 0.001);

        quote.setPrice(5236); // 52.36%
        assertEquals(1.9098, quote.getDecimalOdds(), 0.001);
    }

    @Test
    void testMapFootballMatchWinnerAndTotal() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("45291587");
        event.setName("Man Utd vs Man City");
        event.setType("football_match");
        event.setStartDatetime("2026-09-13T15:30:00Z");
        event.setState("upcoming");
        event.setSlug("premier-league");

        // Winner Market
        SmarketsMarket winnerMarket = new SmarketsMarket();
        winnerMarket.setId("m1");
        winnerMarket.setName("Full-time result");
        SmarketsMarketType mt1 = new SmarketsMarketType();
        mt1.setName("WINNER_3_WAY");
        winnerMarket.setMarketType(mt1);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c1");
        cHome.setName("Man Utd");

        SmarketsContract cDraw = new SmarketsContract();
        cDraw.setId("c2");
        cDraw.setName("Draw");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c3");
        cAway.setName("Man City");

        // Over/Under Market
        SmarketsMarket totalMarket = new SmarketsMarket();
        totalMarket.setId("m2");
        totalMarket.setName("Over/under 2.5 goals");
        SmarketsMarketType mt2 = new SmarketsMarketType();
        mt2.setName("OVER_UNDER");
        mt2.setParam("2.5");
        totalMarket.setMarketType(mt2);

        SmarketsContract cOver = new SmarketsContract();
        cOver.setId("c4");
        cOver.setName("Over 2.5");

        SmarketsContract cUnder = new SmarketsContract();
        cUnder.setId("c5");
        cUnder.setName("Under 2.5");

        Map<String, List<SmarketsContract>> contractsMap = new HashMap<>();
        contractsMap.put("m1", List.of(cHome, cDraw, cAway));
        contractsMap.put("m2", List.of(cOver, cUnder));

        // Quotes
        Map<String, SmarketsContractQuotes> quotesMap = new HashMap<>();
        quotesMap.put("c1", createQuote(3.45));
        quotesMap.put("c2", createQuote(3.60));
        quotesMap.put("c3", createQuote(2.05));
        quotesMap.put("c4", createQuote(1.85));
        quotesMap.put("c5", createQuote(2.02));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(winnerMarket, totalMarket),
                contractsMap,
                quotesMap
        );

        assertNotNull(request);
        assertEquals("smarkets", request.getBookmaker());
        assertEquals("Man Utd", request.getTeam1());
        assertEquals("Man City", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertFalse(request.getIsLive());
        assertEquals(5, request.getOdds().size());

        OddItem itemHome = request.getOdds().stream().filter(o -> "c1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHome);
        assertTrue(itemHome.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) itemHome.getBetType()).outcome());
        assertEquals(3.45, itemHome.getValue(), 0.05);

        OddItem itemDraw = request.getOdds().stream().filter(o -> "c2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDraw);
        assertTrue(itemDraw.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) itemDraw.getBetType()).outcome());
        assertEquals(3.60, itemDraw.getValue(), 0.05);

        OddItem itemAway = request.getOdds().stream().filter(o -> "c3".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemAway);
        assertTrue(itemAway.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) itemAway.getBetType()).outcome());
        assertEquals(2.05, itemAway.getValue(), 0.05);

        OddItem itemOver = request.getOdds().stream().filter(o -> "c4".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemOver);
        assertTrue(itemOver.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemOver.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemOver.getBetType()).param());

        OddItem itemUnder = request.getOdds().stream().filter(o -> "c5".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemUnder);
        assertTrue(itemUnder.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) itemUnder.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemUnder.getBetType()).param());
    }

    @Test
    void testDoubleChanceMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-dc");
        event.setName("Arsenal vs Chelsea");
        event.setType("football_match");

        SmarketsMarket dcMarket = new SmarketsMarket();
        dcMarket.setId("m-dc");
        dcMarket.setName("Double chance");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("DOUBLE_CHANCE");
        dcMarket.setMarketType(mt);

        SmarketsContract c1X = new SmarketsContract();
        c1X.setId("c-1x");
        c1X.setName("Arsenal or Draw");

        SmarketsContract c12 = new SmarketsContract();
        c12.setId("c-12");
        c12.setName("Arsenal or Chelsea");

        SmarketsContract cX2 = new SmarketsContract();
        cX2.setId("c-x2");
        cX2.setName("Draw or Chelsea");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-dc", List.of(c1X, c12, cX2));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-1x", createQuote(1.35),
                "c-12", createQuote(1.28),
                "c-x2", createQuote(1.95)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(dcMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem item1X = request.getOdds().stream().filter(o -> "c-1x".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1X);
        assertTrue(item1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) item1X.getBetType()).outcome());

        OddItem item12 = request.getOdds().stream().filter(o -> "c-12".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item12);
        assertTrue(item12.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) item12.getBetType()).outcome());

        OddItem itemX2 = request.getOdds().stream().filter(o -> "c-x2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemX2);
        assertTrue(itemX2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) itemX2.getBetType()).outcome());
    }

    @Test
    void testDrawNoBetMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-dnb");
        event.setName("Liverpool vs Real Madrid");
        event.setType("football_match");

        SmarketsMarket dnbMarket = new SmarketsMarket();
        dnbMarket.setId("m-dnb");
        dnbMarket.setName("Draw no bet");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("DRAW_NO_BET");
        dnbMarket.setMarketType(mt);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c-dnb-1");
        cHome.setName("Liverpool");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c-dnb-2");
        cAway.setName("Real Madrid");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-dnb", List.of(cHome, cAway));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-dnb-1", createQuote(1.65),
                "c-dnb-2", createQuote(2.25)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(dnbMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem itemDnb1 = request.getOdds().stream().filter(o -> "c-dnb-1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb1);
        assertTrue(itemDnb1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) itemDnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(0.0, hb1.param());

        OddItem itemDnb2 = request.getOdds().stream().filter(o -> "c-dnb-2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb2);
        assertTrue(itemDnb2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) itemDnb2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.0, hb2.param());
    }

    @Test
    void testHandicapMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-hcap");
        event.setName("Bayern Munich vs Dortmund");
        event.setType("football_match");

        SmarketsMarket hcapMarket = new SmarketsMarket();
        hcapMarket.setId("m-hcap");
        hcapMarket.setName("Asian handicap -1.5");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("HANDICAP");
        mt.setParam("-1.5");
        hcapMarket.setMarketType(mt);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c-h1");
        cHome.setName("Bayern Munich -1.5");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c-h2");
        cAway.setName("Dortmund +1.5");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-hcap", List.of(cHome, cAway));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-h1", createQuote(2.10),
                "c-h2", createQuote(1.78)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(hcapMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem itemH1 = request.getOdds().stream().filter(o -> "c-h1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemH1);
        assertTrue(itemH1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) itemH1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param());

        OddItem itemH2 = request.getOdds().stream().filter(o -> "c-h2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemH2);
        assertTrue(itemH2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) itemH2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param());
    }

    @Test
    void testBttsAndCorrectScoreMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-btts-cs");
        event.setName("Barcelona vs Juventus");
        event.setType("football_match");

        // BTTS Market
        SmarketsMarket bttsMarket = new SmarketsMarket();
        bttsMarket.setId("m-btts");
        bttsMarket.setName("Both teams to score");

        SmarketsContract cYes = new SmarketsContract();
        cYes.setId("c-yes");
        cYes.setName("Yes");

        SmarketsContract cNo = new SmarketsContract();
        cNo.setId("c-no");
        cNo.setName("No");

        // Correct Score Market
        SmarketsMarket csMarket = new SmarketsMarket();
        csMarket.setId("m-cs");
        csMarket.setName("Correct score");

        SmarketsContract cs21 = new SmarketsContract();
        cs21.setId("c-21");
        cs21.setName("2 - 1");

        SmarketsContract cs11 = new SmarketsContract();
        cs11.setId("c-11");
        cs11.setName("1 - 1");

        Map<String, List<SmarketsContract>> contractsMap = Map.of(
                "m-btts", List.of(cYes, cNo),
                "m-cs", List.of(cs21, cs11)
        );

        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-yes", createQuote(1.72),
                "c-no", createQuote(2.15),
                "c-21", createQuote(8.50),
                "c-11", createQuote(6.20)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(bttsMarket, csMarket),
                contractsMap,
                quotesMap
        );

        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem itemBttsYes = request.getOdds().stream().filter(o -> "c-yes".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsYes);
        assertTrue(itemBttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbYes = (BinaryMarketBet) itemBttsYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbYes.outcome());

        OddItem itemBttsNo = request.getOdds().stream().filter(o -> "c-no".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsNo);
        assertTrue(itemBttsNo.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbNo = (BinaryMarketBet) itemBttsNo.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbNo.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, bmbNo.outcome());

        OddItem itemCs21 = request.getOdds().stream().filter(o -> "c-21".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs21);
        assertTrue(itemCs21.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs21Bet = (CorrectScoreBet) itemCs21.getBetType();
        assertEquals(2, cs21Bet.score1());
        assertEquals(1, cs21Bet.score2());
        assertFalse(cs21Bet.isAnyOtherScore());

        OddItem itemCs11 = request.getOdds().stream().filter(o -> "c-11".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs11);
        assertTrue(itemCs11.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs11Bet = (CorrectScoreBet) itemCs11.getBetType();
        assertEquals(1, cs11Bet.score1());
        assertEquals(1, cs11Bet.score2());
        assertFalse(cs11Bet.isAnyOtherScore());
    }
}
