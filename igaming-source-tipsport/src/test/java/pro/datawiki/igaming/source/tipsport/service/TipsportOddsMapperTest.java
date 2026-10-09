package pro.datawiki.igaming.source.tipsport.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.tipsport.dto.TipsportEvent;
import pro.datawiki.igaming.source.tipsport.dto.TipsportMarket;
import pro.datawiki.igaming.source.tipsport.dto.TipsportOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TipsportOddsMapperTest {

    private TipsportOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TipsportOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("4001");
        cache.setTeam1("Viktoria Plzen");
        cache.setTeam2("Banik Ostrava");
        cache.setSportName("Football");
        cache.setLeagueName("1. Liga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        TipsportEvent event = new TipsportEvent();
        event.setId(4001L);
        event.setName("Viktoria Plzen - Banik Ostrava");

        List<TipsportMarket> markets = new ArrayList<>();

        // 1X2 market
        TipsportMarket m1X2 = new TipsportMarket();
        m1X2.setName("Vítěz zápasu");
        m1X2.setMarketType("1X2");
        List<TipsportOutcome> out1X2 = new ArrayList<>();
        TipsportOutcome o1 = new TipsportOutcome();
        o1.setCode("1");
        o1.setName("Viktoria Plzen");
        o1.setRate(1.85);
        TipsportOutcome oX = new TipsportOutcome();
        oX.setCode("0");
        oX.setName("Remíza");
        oX.setRate(3.60);
        TipsportOutcome o2 = new TipsportOutcome();
        o2.setCode("2");
        o2.setName("Banik Ostrava");
        o2.setRate(4.10);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market
        TipsportMarket mTotal = new TipsportMarket();
        mTotal.setName("Počet gólů 2.5");
        mTotal.setMarketType("TOTAL");
        mTotal.setParam(2.5);
        List<TipsportOutcome> outTotal = new ArrayList<>();
        TipsportOutcome oOver = new TipsportOutcome();
        oOver.setCode("OVER");
        oOver.setName("Více než 2.5");
        oOver.setRate(1.92);
        TipsportOutcome oUnder = new TipsportOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Méně než 2.5");
        oUnder.setRate(1.88);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        TipsportMarket mBtts = new TipsportMarket();
        mBtts.setName("Oba týmy dají gól");
        mBtts.setMarketType("BTTS");
        List<TipsportOutcome> outBtts = new ArrayList<>();
        TipsportOutcome oYes = new TipsportOutcome();
        oYes.setCode("YES");
        oYes.setName("Ano");
        oYes.setRate(1.78);
        TipsportOutcome oNo = new TipsportOutcome();
        oNo.setCode("NO");
        oNo.setName("Ne");
        oNo.setRate(1.98);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("tipsport", request.getBookmaker());
        assertEquals("4001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.85));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.60));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 4.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.92));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.78));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.98));
    }
}
