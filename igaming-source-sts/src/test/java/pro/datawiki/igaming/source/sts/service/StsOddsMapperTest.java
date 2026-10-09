package pro.datawiki.igaming.source.sts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.sts.dto.StsEvent;
import pro.datawiki.igaming.source.sts.dto.StsMarket;
import pro.datawiki.igaming.source.sts.dto.StsOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StsOddsMapperTest {

    private StsOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StsOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("2001");
        cache.setTeam1("Legia Warszawa");
        cache.setTeam2("Lech Poznan");
        cache.setSportName("Football");
        cache.setLeagueName("Ekstraklasa");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        StsEvent event = new StsEvent();
        event.setId(2001L);
        event.setName("Legia Warszawa - Lech Poznan");

        List<StsMarket> markets = new ArrayList<>();

        // 1X2 market
        StsMarket m1X2 = new StsMarket();
        m1X2.setName("Wynik meczu");
        m1X2.setType("1X2");
        List<StsOutcome> out1X2 = new ArrayList<>();
        StsOutcome o1 = new StsOutcome();
        o1.setCode("1");
        o1.setName("Legia Warszawa");
        o1.setRate(2.10);
        StsOutcome oX = new StsOutcome();
        oX.setCode("X");
        oX.setName("Remis");
        oX.setRate(3.30);
        StsOutcome o2 = new StsOutcome();
        o2.setCode("2");
        o2.setName("Lech Poznan");
        o2.setRate(3.25);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market
        StsMarket mTotal = new StsMarket();
        mTotal.setName("Liczba goli 2.5");
        mTotal.setType("TOTAL");
        mTotal.setParam(2.5);
        List<StsOutcome> outTotal = new ArrayList<>();
        StsOutcome oOver = new StsOutcome();
        oOver.setCode("OVER");
        oOver.setName("Powyżej 2.5");
        oOver.setRate(1.90);
        StsOutcome oUnder = new StsOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Poniżej 2.5");
        oUnder.setRate(1.90);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        StsMarket mBtts = new StsMarket();
        mBtts.setName("Obie druzyny strzela gola");
        mBtts.setType("BTTS");
        List<StsOutcome> outBtts = new ArrayList<>();
        StsOutcome oYes = new StsOutcome();
        oYes.setCode("YES");
        oYes.setName("Tak");
        oYes.setRate(1.75);
        StsOutcome oNo = new StsOutcome();
        oNo.setCode("NO");
        oNo.setName("Nie");
        oNo.setRate(1.95);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("sts", request.getBookmaker());
        assertEquals("2001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.30));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.25));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.90));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.75));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.95));
    }
}
