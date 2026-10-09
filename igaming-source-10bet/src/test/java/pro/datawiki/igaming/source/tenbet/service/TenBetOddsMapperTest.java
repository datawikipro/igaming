package pro.datawiki.igaming.source.tenbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class TenBetOddsMapperTest {

    private TenBetOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TenBetOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("7001");
        cache.setTeam1("Borussia Dortmund");
        cache.setTeam2("RB Leipzig");
        cache.setSportName("Football");
        cache.setLeagueName("Bundesliga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        SBTechEvent event = new SBTechEvent();
        event.setId(7001L);
        event.setName("Borussia Dortmund - RB Leipzig");

        List<SBTechMarket> markets = new ArrayList<>();

        // 1X2 market
        SBTechMarket m1X2 = new SBTechMarket();
        m1X2.setName("Match Result");
        m1X2.setMarketType("1X2");
        List<SBTechOutcome> out1X2 = new ArrayList<>();
        SBTechOutcome o1 = new SBTechOutcome();
        o1.setCode("1");
        o1.setName("Borussia Dortmund");
        o1.setPrice(2.20);
        SBTechOutcome oX = new SBTechOutcome();
        oX.setCode("X");
        oX.setName("Draw");
        oX.setPrice(3.50);
        SBTechOutcome o2 = new SBTechOutcome();
        o2.setCode("2");
        o2.setName("RB Leipzig");
        o2.setPrice(3.10);
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
        oOver.setPrice(1.65);
        SBTechOutcome oUnder = new SBTechOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Under 2.5");
        oUnder.setPrice(2.25);
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
        oYes.setPrice(1.55);
        SBTechOutcome oNo = new SBTechOutcome();
        oNo.setCode("NO");
        oNo.setName("No");
        oNo.setPrice(2.40);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("10bet", request.getBookmaker());
        assertEquals("7001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.20));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.50));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.65));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.55));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.40));
    }
}
