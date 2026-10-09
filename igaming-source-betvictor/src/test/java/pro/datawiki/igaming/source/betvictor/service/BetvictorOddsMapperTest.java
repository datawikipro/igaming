package pro.datawiki.igaming.source.betvictor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorEvent;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorMarket;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorOutcome;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetvictorOddsMapperTest {

    private BetvictorOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetvictorOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBttsAndHandicap() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("2001");
        cache.setTeam1("Arsenal");
        cache.setTeam2("Chelsea");
        cache.setSportName("Football");
        cache.setLeagueName("Premier League");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        BetvictorEvent event = new BetvictorEvent();
        event.setId(2001L);
        event.setTitle("Arsenal vs Chelsea");

        List<BetvictorMarket> markets = new ArrayList<>();

        // 1X2 market
        BetvictorMarket m1X2 = new BetvictorMarket();
        m1X2.setName("Match Betting");
        m1X2.setMarketType("MATCH_BETTING");
        List<BetvictorOutcome> out1X2 = new ArrayList<>();
        BetvictorOutcome o1 = new BetvictorOutcome();
        o1.setId(101L);
        o1.setName("Arsenal");
        o1.setPrice(1.95);
        BetvictorOutcome oX = new BetvictorOutcome();
        oX.setId(102L);
        oX.setName("Draw");
        oX.setPrice(3.40);
        BetvictorOutcome o2 = new BetvictorOutcome();
        o2.setId(103L);
        o2.setName("Chelsea");
        o2.setPrice(4.10);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market (Over/Under 2.5)
        BetvictorMarket mTotal = new BetvictorMarket();
        mTotal.setName("Total Goals Over/Under");
        mTotal.setMarketType("TOTAL_GOALS");
        List<BetvictorOutcome> outTotal = new ArrayList<>();
        BetvictorOutcome oOver = new BetvictorOutcome();
        oOver.setId(104L);
        oOver.setName("Over 2.5");
        oOver.setHandicap(2.5);
        oOver.setPrice(1.85);
        BetvictorOutcome oUnder = new BetvictorOutcome();
        oUnder.setId(105L);
        oUnder.setName("Under 2.5");
        oUnder.setHandicap(2.5);
        oUnder.setPrice(1.95);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        BetvictorMarket mBtts = new BetvictorMarket();
        mBtts.setName("Both Teams To Score");
        mBtts.setMarketType("BTTS");
        List<BetvictorOutcome> outBtts = new ArrayList<>();
        BetvictorOutcome oYes = new BetvictorOutcome();
        oYes.setId(106L);
        oYes.setName("Yes");
        oYes.setPrice(1.70);
        BetvictorOutcome oNo = new BetvictorOutcome();
        oNo.setId(107L);
        oNo.setName("No");
        oNo.setPrice(2.05);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        // Handicap market (-1.5 / +1.5)
        BetvictorMarket mHcap = new BetvictorMarket();
        mHcap.setName("Asian Handicap");
        mHcap.setMarketType("ASIAN_HANDICAP");
        List<BetvictorOutcome> outHcap = new ArrayList<>();
        BetvictorOutcome oH1 = new BetvictorOutcome();
        oH1.setId(108L);
        oH1.setName("Arsenal (-1.5)");
        oH1.setHandicap(-1.5);
        oH1.setPrice(2.60);
        BetvictorOutcome oH2 = new BetvictorOutcome();
        oH2.setId(109L);
        oH2.setName("Chelsea (+1.5)");
        oH2.setHandicap(1.5);
        oH2.setPrice(1.48);
        outHcap.add(oH1);
        outHcap.add(oH2);
        mHcap.setOutcomes(outHcap);
        markets.add(mHcap);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("betvictor", request.getBookmaker());
        assertEquals("2001", request.getExternalEventId());
        assertEquals(9, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.40));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 4.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.85));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.70));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.05));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 2.60));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 1.48));
    }
}
