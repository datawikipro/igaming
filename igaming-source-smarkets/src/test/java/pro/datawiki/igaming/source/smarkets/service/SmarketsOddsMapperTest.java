package pro.datawiki.igaming.source.smarkets.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SmarketsOddsMapperTest {

    private SmarketsOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SmarketsOddsMapper();
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

        // Man Utd @ 3.45 -> price ~ 2898
        SmarketsContractQuotes q1 = new SmarketsContractQuotes();
        SmarketsQuoteEntry b1 = new SmarketsQuoteEntry();
        b1.setPrice(2898);
        q1.setBids(List.of(b1));
        quotesMap.put("c1", q1);

        // Draw @ 3.60 -> price ~ 2777
        SmarketsContractQuotes q2 = new SmarketsContractQuotes();
        SmarketsQuoteEntry b2 = new SmarketsQuoteEntry();
        b2.setPrice(2777);
        q2.setBids(List.of(b2));
        quotesMap.put("c2", q2);

        // Man City @ 2.05 -> price ~ 4878
        SmarketsContractQuotes q3 = new SmarketsContractQuotes();
        SmarketsQuoteEntry b3 = new SmarketsQuoteEntry();
        b3.setPrice(4878);
        q3.setBids(List.of(b3));
        quotesMap.put("c3", q3);

        // Over 2.5 @ 1.85 -> price ~ 5405
        SmarketsContractQuotes q4 = new SmarketsContractQuotes();
        SmarketsQuoteEntry b4 = new SmarketsQuoteEntry();
        b4.setPrice(5405);
        q4.setBids(List.of(b4));
        quotesMap.put("c4", q4);

        // Under 2.5 @ 2.02 -> price ~ 4950
        SmarketsContractQuotes q5 = new SmarketsContractQuotes();
        SmarketsQuoteEntry b5 = new SmarketsQuoteEntry();
        b5.setPrice(4950);
        q5.setBids(List.of(b5));
        quotesMap.put("c5", q5);

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

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN1") && Math.abs(o.getValue() - 3.45) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("DRAW") && Math.abs(o.getValue() - 3.60) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN2") && Math.abs(o.getValue() - 2.05) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("OVER") && Math.abs(o.getValue() - 1.85) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("UNDER") && Math.abs(o.getValue() - 2.02) < 0.01));
    }

    @Test
    void testOrderBookOutlierBidsIgnored() {
        SmarketsContractQuotes q = new SmarketsContractQuotes();
        SmarketsQuoteEntry realBid = new SmarketsQuoteEntry();
        realBid.setPrice(5000); // 50% -> 2.0
        SmarketsQuoteEntry lowballBid = new SmarketsQuoteEntry();
        lowballBid.setPrice(1); // 0.01% -> would be 10000.0 if improperly inverted

        q.setBids(List.of(realBid, lowballBid));
        assertEquals(2.0, q.getBestBackOdds(), 0.001);

        // When executable offers are present, best backer price is lowest offer price (highest back odds)
        SmarketsQuoteEntry bestOffer = new SmarketsQuoteEntry();
        bestOffer.setPrice(4900); // 49% -> 2.0408
        bestOffer.setQuantity(100);
        SmarketsQuoteEntry worseOffer = new SmarketsQuoteEntry();
        worseOffer.setPrice(6000); // 60% -> 1.666
        worseOffer.setQuantity(50);

        q.setOffers(List.of(bestOffer, worseOffer));
        assertEquals(2.0408, q.getBestBackOdds(), 0.001);
    }

    @Test
    void testTimeBracketAndAuxiliaryMarketsIgnored() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("45293756");
        event.setName("Sint-Truidense VV vs Iberia 1999 Tbilisi");
        event.setType("football_match");
        event.setStartDatetime("2026-10-14T18:00:00Z");

        // 1. True Full-time Result
        SmarketsMarket ftResult = new SmarketsMarket();
        ftResult.setId("m_ft");
        ftResult.setName("Full-time result");
        SmarketsMarketType mtFt = new SmarketsMarketType();
        mtFt.setName("WINNER_3_WAY");
        ftResult.setMarketType(mtFt);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c_home");
        cHome.setName("Sint-Truiden");
        cHome.setSlug("home");

        SmarketsContract cDraw = new SmarketsContract();
        cDraw.setId("c_draw");
        cDraw.setName("Draw");
        cDraw.setSlug("draw");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c_away");
        cAway.setName("Iberia 1999 Tbilisi");
        cAway.setSlug("away");

        // 2. Bogus Time-bracket Winner (1-10 min)
        SmarketsMarket bracketMarket = new SmarketsMarket();
        bracketMarket.setId("m_bracket");
        bracketMarket.setName("Winner in 1 minute to 10 minute time bracket");
        SmarketsMarketType mtBracket = new SmarketsMarketType();
        mtBracket.setName("WINNER_3_WAY_TIME_BRACKET");
        mtBracket.setParam("1-10");
        bracketMarket.setMarketType(mtBracket);

        SmarketsContract cBogusHome = new SmarketsContract();
        cBogusHome.setId("c_bhome");
        cBogusHome.setName("Sint-Truiden");
        cBogusHome.setSlug("home");

        SmarketsContract cBogusDraw = new SmarketsContract();
        cBogusDraw.setId("c_bdraw");
        cBogusDraw.setName("Draw");
        cBogusDraw.setSlug("draw");

        // 3. Corners Over/Under
        SmarketsMarket cornersMarket = new SmarketsMarket();
        cornersMarket.setId("m_corners");
        cornersMarket.setName("Over/under 9.5 corners");
        SmarketsMarketType mtCorners = new SmarketsMarketType();
        mtCorners.setName("CORNERS_OVER_UNDER");
        mtCorners.setParam("9.5");
        cornersMarket.setMarketType(mtCorners);

        SmarketsContract cCornOver = new SmarketsContract();
        cCornOver.setId("c_cover");
        cCornOver.setName("Over 9.5 corners");

        // Contracts map
        Map<String, List<SmarketsContract>> contractsMap = new HashMap<>();
        contractsMap.put("m_ft", List.of(cHome, cDraw, cAway));
        contractsMap.put("m_bracket", List.of(cBogusHome, cBogusDraw));
        contractsMap.put("m_corners", List.of(cCornOver));

        // Quotes map
        Map<String, SmarketsContractQuotes> quotesMap = new HashMap<>();

        // Real: Home = 1.30, Draw = 5.50, Away = 8.50
        SmarketsContractQuotes qHome = new SmarketsContractQuotes();
        SmarketsQuoteEntry bHome = new SmarketsQuoteEntry();
        bHome.setPrice(7692); // ~1.30
        qHome.setBids(List.of(bHome));
        quotesMap.put("c_home", qHome);

        SmarketsContractQuotes qDraw = new SmarketsContractQuotes();
        SmarketsQuoteEntry bDraw = new SmarketsQuoteEntry();
        bDraw.setPrice(1818); // ~5.50
        qDraw.setBids(List.of(bDraw));
        quotesMap.put("c_draw", qDraw);

        SmarketsContractQuotes qAway = new SmarketsContractQuotes();
        SmarketsQuoteEntry bAway = new SmarketsQuoteEntry();
        bAway.setPrice(1176); // ~8.50
        qAway.setBids(List.of(bAway));
        quotesMap.put("c_away", qAway);

        // Bogus: Home = 4.50, Draw = 1.20
        SmarketsContractQuotes qBHome = new SmarketsContractQuotes();
        SmarketsQuoteEntry bBHome = new SmarketsQuoteEntry();
        bBHome.setPrice(2222); // ~4.50
        qBHome.setBids(List.of(bBHome));
        quotesMap.put("c_bhome", qBHome);

        SmarketsContractQuotes qBDraw = new SmarketsContractQuotes();
        SmarketsQuoteEntry bBDraw = new SmarketsQuoteEntry();
        bBDraw.setPrice(8333); // ~1.20
        qBDraw.setBids(List.of(bBDraw));
        quotesMap.put("c_bdraw", qBDraw);

        // Corners: Over 9.5 @ 1.80
        SmarketsContractQuotes qCorn = new SmarketsContractQuotes();
        SmarketsQuoteEntry bCorn = new SmarketsQuoteEntry();
        bCorn.setPrice(5555); // ~1.80
        qCorn.setBids(List.of(bCorn));
        quotesMap.put("c_cover", qCorn);

        // Map markets with bogus bracket market coming AFTER ftResult
        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(ftResult, bracketMarket, cornersMarket),
                contractsMap,
                quotesMap
        );

        assertNotNull(request);
        assertEquals(3, request.getOdds().size(), "Should have exactly 3 odds: WIN1, DRAW, WIN2");

        // Verify WIN1 is 1.30 (NOT overwritten by 4.50)
        double win1 = request.getOdds().stream().filter(o -> "WIN1".equals(o.getBetType().code())).findFirst().orElseThrow().getValue();
        assertEquals(1.30, win1, 0.05);

        // Verify DRAW is 5.50 (NOT overwritten by 1.20)
        double draw = request.getOdds().stream().filter(o -> "DRAW".equals(o.getBetType().code())).findFirst().orElseThrow().getValue();
        assertEquals(5.50, draw, 0.05);

        // Verify WIN2 is mapped despite name difference (resolved by slug)
        double win2 = request.getOdds().stream().filter(o -> "WIN2".equals(o.getBetType().code())).findFirst().orElseThrow().getValue();
        assertEquals(8.50, win2, 0.05);

        // Verify NO total goals mapped from corners
        assertTrue(request.getOdds().stream().noneMatch(o -> o.getBetType().code().startsWith("TOTAL")), "Corners must NOT be mapped as total goals");
    }
}
