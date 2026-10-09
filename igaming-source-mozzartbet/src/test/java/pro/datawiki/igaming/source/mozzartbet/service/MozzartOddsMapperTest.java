package pro.datawiki.igaming.source.mozzartbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartEvent;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartMarket;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MozzartOddsMapperTest {

    private MozzartOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MozzartOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("5001");
        cache.setTeam1("Crvena Zvezda");
        cache.setTeam2("Partizan");
        cache.setSportName("Football");
        cache.setLeagueName("SuperLiga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        MozzartEvent event = new MozzartEvent();
        event.setId(5001L);
        event.setName("Crvena Zvezda - Partizan");

        List<MozzartMarket> markets = new ArrayList<>();

        // 1X2 market
        MozzartMarket m1X2 = new MozzartMarket();
        m1X2.setName("Konačan ishod");
        m1X2.setCode("1X2");
        List<MozzartOutcome> out1X2 = new ArrayList<>();
        MozzartOutcome o1 = new MozzartOutcome();
        o1.setCode("1");
        o1.setName("Crvena Zvezda");
        o1.setValue(1.90);
        MozzartOutcome oX = new MozzartOutcome();
        oX.setCode("X");
        oX.setName("Nerešeno");
        oX.setValue(3.40);
        MozzartOutcome o2 = new MozzartOutcome();
        o2.setCode("2");
        o2.setName("Partizan");
        o2.setValue(3.80);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market
        MozzartMarket mTotal = new MozzartMarket();
        mTotal.setName("Ukupno golova 2.5");
        mTotal.setCode("TOTAL");
        mTotal.setParam(2.5);
        List<MozzartOutcome> outTotal = new ArrayList<>();
        MozzartOutcome oOver = new MozzartOutcome();
        oOver.setCode("OVER");
        oOver.setName("Više 2.5");
        oOver.setValue(1.85);
        MozzartOutcome oUnder = new MozzartOutcome();
        oUnder.setCode("UNDER");
        oUnder.setName("Manje 2.5");
        oUnder.setValue(1.95);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        MozzartMarket mBtts = new MozzartMarket();
        mBtts.setName("Oba tima daju gol");
        mBtts.setCode("BTTS");
        List<MozzartOutcome> outBtts = new ArrayList<>();
        MozzartOutcome oYes = new MozzartOutcome();
        oYes.setCode("GG");
        oYes.setName("Da");
        oYes.setValue(1.72);
        MozzartOutcome oNo = new MozzartOutcome();
        oNo.setCode("NG");
        oNo.setName("Ne");
        oNo.setValue(2.02);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("mozzartbet", request.getBookmaker());
        assertEquals("5001", request.getExternalEventId());
        assertEquals(7, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.90));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.40));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.85));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.72));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.02));
    }
}
