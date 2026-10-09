package pro.datawiki.igaming.source.tipico.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.tipico.dto.TipicoEvent;
import pro.datawiki.igaming.source.tipico.dto.TipicoMarket;
import pro.datawiki.igaming.source.tipico.dto.TipicoOutcome;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TipicoOddsMapperTest {

    private TipicoOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TipicoOddsMapper(null);
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBttsAndHandicap() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("3001");
        cache.setTeam1("Bayern München");
        cache.setTeam2("Borussia Dortmund");
        cache.setSportName("Soccer");
        cache.setLeagueName("Bundesliga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        TipicoEvent event = new TipicoEvent();
        event.setId(3001L);
        event.setName("Bayern München vs Borussia Dortmund");

        List<TipicoMarket> markets = new ArrayList<>();

        // 1X2 market
        TipicoMarket m1X2 = new TipicoMarket();
        m1X2.setName("Dreiweg");
        m1X2.setMarketType("1X2");
        List<TipicoOutcome> out1X2 = new ArrayList<>();
        TipicoOutcome o1 = new TipicoOutcome();
        o1.setId(201L);
        o1.setName("Bayern München");
        o1.setPrice(1.65);
        TipicoOutcome oX = new TipicoOutcome();
        oX.setId(202L);
        oX.setName("Unentschieden");
        oX.setPrice(4.20);
        TipicoOutcome o2 = new TipicoOutcome();
        o2.setId(203L);
        o2.setName("Borussia Dortmund");
        o2.setPrice(4.80);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setOutcomes(out1X2);
        markets.add(m1X2);

        // Totals market (Over/Under 3.5)
        TipicoMarket mTotal = new TipicoMarket();
        mTotal.setName("Über/Unter Tore 3.5");
        mTotal.setMarketType("TOTAL_GOALS");
        List<TipicoOutcome> outTotal = new ArrayList<>();
        TipicoOutcome oOver = new TipicoOutcome();
        oOver.setId(204L);
        oOver.setName("Über 3.5");
        oOver.setHandicap(3.5);
        oOver.setPrice(2.10);
        TipicoOutcome oUnder = new TipicoOutcome();
        oUnder.setId(205L);
        oUnder.setName("Unter 3.5");
        oUnder.setHandicap(3.5);
        oUnder.setPrice(1.72);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setOutcomes(outTotal);
        markets.add(mTotal);

        // BTTS market
        TipicoMarket mBtts = new TipicoMarket();
        mBtts.setName("Beide Teams treffen");
        mBtts.setMarketType("BTTS");
        List<TipicoOutcome> outBtts = new ArrayList<>();
        TipicoOutcome oYes = new TipicoOutcome();
        oYes.setId(206L);
        oYes.setName("Ja");
        oYes.setPrice(1.55);
        TipicoOutcome oNo = new TipicoOutcome();
        oNo.setId(207L);
        oNo.setName("Nein");
        oNo.setPrice(2.35);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setOutcomes(outBtts);
        markets.add(mBtts);

        // Handicap market (-1.0 / +1.0)
        TipicoMarket mHcap = new TipicoMarket();
        mHcap.setName("Handicap");
        mHcap.setMarketType("HANDICAP");
        List<TipicoOutcome> outHcap = new ArrayList<>();
        TipicoOutcome oH1 = new TipicoOutcome();
        oH1.setId(208L);
        oH1.setName("Bayern München (-1)");
        oH1.setHandicap(-1.0);
        oH1.setPrice(2.45);
        TipicoOutcome oH2 = new TipicoOutcome();
        oH2.setId(209L);
        oH2.setName("Borussia Dortmund (+1)");
        oH2.setHandicap(1.0);
        oH2.setPrice(1.55);
        outHcap.add(oH1);
        outHcap.add(oH2);
        mHcap.setOutcomes(outHcap);
        markets.add(mHcap);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("tipico", request.getBookmaker());
        assertEquals("3001", request.getExternalEventId());
        assertEquals(9, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.65));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 4.20));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 4.80));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 2.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.72));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.55));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.35));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 2.45));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 1.55));
    }
}
