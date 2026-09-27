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
}
