package pro.datawiki.igaming.source.fortuna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.fortuna.dto.FortunaEvent;
import pro.datawiki.igaming.source.fortuna.dto.FortunaMarket;
import pro.datawiki.igaming.source.fortuna.dto.FortunaOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FortunaOddsMapperTest {

    private FortunaOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FortunaOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("3001");
        cache.setTeam1("Sparta Praha");
        cache.setTeam2("Slavia Praha");
        cache.setSportName("Football");
        cache.setLeagueName("1. Liga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        FortunaEvent event = new FortunaEvent();
        event.setId(3001L);
        event.setName("Sparta Praha - Slavia Praha");

        List<FortunaMarket> markets = new ArrayList<>();

        // 1X2 market
        FortunaMarket m1X2 = new FortunaMarket();
        m1X2.setName("Výsledek zápasu");
        m1X2.setMarketType("1X2");
        List<FortunaOutcome> out1X2 = new ArrayList<>();
        FortunaOutcome o1 = new FortunaOutcome();
        o1.setCode("1");
        o1.setName("Sparta Praha");
        o1.setOdds(2.40);
        FortunaOutcome oX = new FortunaOutcome();
        oX.setCode("0");
        oX.setName("Remíza");
        oX.setOdds(3.10);
        FortunaOutcome o2 = new FortunaOutcome();
        o2.setCode("2");
        o2.setName("Slavia Praha");
        o2.setOdds(2.80);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market
        FortunaMarket mTotal = new FortunaMarket();
        mTotal.setName("Počet gólů 2.5");
        mTotal.setMarketType("TOTAL");
        mTotal.setParam(2.5);
        List<FortunaOutcome> outTotal = new ArrayList<>();
        FortunaOutcome oOver = new FortunaOutcome();
        oOver.setCode("OVER");
        oOver.setName("Více než 2.5");
        oOver.setOdds(1.95);
        FortunaOutcome oUnder = new FortunaOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Méně než 2.5");
        oUnder.setOdds(1.85);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        FortunaMarket mBtts = new FortunaMarket();
        mBtts.setName("Oba týmy dají gól");
        mBtts.setMarketType("BTTS");
        List<FortunaOutcome> outBtts = new ArrayList<>();
        FortunaOutcome oYes = new FortunaOutcome();
        oYes.setCode("YES");
        oYes.setName("Ano");
        oYes.setOdds(1.70);
        FortunaOutcome oNo = new FortunaOutcome();
        oNo.setCode("NO");
        oNo.setName("Ne");
        oNo.setOdds(2.05);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("fortuna", request.getBookmaker());
        assertEquals("3001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.40));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.70));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.05));
    }
}
