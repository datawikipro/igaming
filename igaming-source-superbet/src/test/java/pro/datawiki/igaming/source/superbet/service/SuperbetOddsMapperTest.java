package pro.datawiki.igaming.source.superbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.superbet.dto.SuperbetEvent;
import pro.datawiki.igaming.source.superbet.dto.SuperbetMarket;
import pro.datawiki.igaming.source.superbet.dto.SuperbetOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuperbetOddsMapperTest {

    private SuperbetOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SuperbetOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("1001");
        cache.setTeam1("FCSB");
        cache.setTeam2("CFR Cluj");
        cache.setSportName("Soccer");
        cache.setLeagueName("Liga 1 Romania");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        SuperbetEvent event = new SuperbetEvent();
        event.setId(1001L);
        event.setMatchName("FCSB - CFR Cluj");

        List<SuperbetMarket> markets = new ArrayList<>();

        // 1X2 market
        SuperbetMarket m1X2 = new SuperbetMarket();
        m1X2.setName("Rezultat final");
        m1X2.setCode("1X2");
        List<SuperbetOutcome> out1X2 = new ArrayList<>();
        SuperbetOutcome o1 = new SuperbetOutcome();
        o1.setCode("1");
        o1.setName("FCSB");
        o1.setPrice(2.15);
        SuperbetOutcome oX = new SuperbetOutcome();
        oX.setCode("X");
        oX.setName("Egal");
        oX.setPrice(3.20);
        SuperbetOutcome o2 = new SuperbetOutcome();
        o2.setCode("2");
        o2.setName("CFR Cluj");
        o2.setPrice(3.40);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market (Over/Under 2.5)
        SuperbetMarket mTotal = new SuperbetMarket();
        mTotal.setName("Total goluri 2.5");
        mTotal.setCode("TOTAL");
        mTotal.setSpecifiers("total=2.5");
        List<SuperbetOutcome> outTotal = new ArrayList<>();
        SuperbetOutcome oOver = new SuperbetOutcome();
        oOver.setCode("OVER");
        oOver.setName("Peste 2.5");
        oOver.setPrice(1.95);
        SuperbetOutcome oUnder = new SuperbetOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Sub 2.5");
        oUnder.setPrice(1.85);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        SuperbetMarket mBtts = new SuperbetMarket();
        mBtts.setName("Ambele echipe inscriu");
        mBtts.setCode("GG");
        List<SuperbetOutcome> outBtts = new ArrayList<>();
        SuperbetOutcome oYes = new SuperbetOutcome();
        oYes.setCode("GG");
        oYes.setName("Da");
        oYes.setPrice(1.80);
        SuperbetOutcome oNo = new SuperbetOutcome();
        oNo.setCode("NG");
        oNo.setName("Nu");
        oNo.setPrice(1.90);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("superbet", request.getBookmaker());
        assertEquals("1001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.15));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.20));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.40));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.85));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.90));
    }
}
