package pro.datawiki.igaming.source.betway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.sbtech.dto.SBTechEvent;
import pro.datawiki.igaming.source.core.engine.sbtech.dto.SBTechMarket;
import pro.datawiki.igaming.source.core.engine.sbtech.dto.SBTechOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetwayOddsMapperTest {

    private BetwayOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetwayOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("6001");
        cache.setTeam1("Arsenal");
        cache.setTeam2("Chelsea");
        cache.setSportName("Football");
        cache.setLeagueName("Premier League");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        SBTechEvent event = new SBTechEvent();
        event.setId(6001L);
        event.setName("Arsenal - Chelsea");

        List<SBTechMarket> markets = new ArrayList<>();

        // 1X2 market
        SBTechMarket m1X2 = new SBTechMarket();
        m1X2.setName("Match Result");
        m1X2.setMarketType("1X2");
        List<SBTechOutcome> out1X2 = new ArrayList<>();
        SBTechOutcome o1 = new SBTechOutcome();
        o1.setCode("1");
        o1.setName("Arsenal");
        o1.setPrice(1.95);
        SBTechOutcome oX = new SBTechOutcome();
        oX.setCode("X");
        oX.setName("Draw");
        oX.setPrice(3.60);
        SBTechOutcome o2 = new SBTechOutcome();
        o2.setCode("2");
        o2.setName("Chelsea");
        o2.setPrice(3.80);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market
        SBTechMarket mTotal = new SBTechMarket();
        mTotal.setName("Total Goals 2.5");
        mTotal.setMarketType("TOTAL");
        mTotal.setParam(2.5);
        List<SBTechOutcome> outTotal = new ArrayList<>();
        SBTechOutcome oOver = new SBTechOutcome();
        oOver.setCode("OVER");
        oOver.setName("Over 2.5");
        oOver.setPrice(1.80);
        SBTechOutcome oUnder = new SBTechOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Under 2.5");
        oUnder.setPrice(2.00);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        SBTechMarket mBtts = new SBTechMarket();
        mBtts.setName("Both Teams To Score");
        mBtts.setMarketType("BTTS");
        List<SBTechOutcome> outBtts = new ArrayList<>();
        SBTechOutcome oYes = new SBTechOutcome();
        oYes.setCode("YES");
        oYes.setName("Yes");
        oYes.setPrice(1.70);
        SBTechOutcome oNo = new SBTechOutcome();
        oNo.setCode("NO");
        oNo.setName("No");
        oNo.setPrice(2.10);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("betway", request.getBookmaker());
        assertEquals("6001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.60));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.70));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.10));
    }
}
