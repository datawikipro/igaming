package pro.datawiki.igaming.source.betano.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betano.dto.BetanoEvent;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetanoOddsMapperTest {

    private BetanoOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetanoOddsMapper();
    }

    @Test
    void testMapToOddsUpdateRequest_1X2AndTotalsAndBttsAndHandicap() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("4001");
        cache.setTeam1("Benfica");
        cache.setTeam2("Porto");
        cache.setSportName("Soccer");
        cache.setLeagueName("Primeira Liga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        BetanoEvent event = new BetanoEvent();
        event.setId("4001");
        event.setName("Benfica - Porto");

        List<BetanoMarket> markets = new ArrayList<>();

        // 1X2 market
        BetanoMarket m1X2 = new BetanoMarket();
        m1X2.setName("Resultado Final");
        m1X2.setType("1X2");
        List<BetanoSelection> out1X2 = new ArrayList<>();
        BetanoSelection o1 = new BetanoSelection();
        o1.setId("1");
        o1.setName("Benfica");
        o1.setPrice(2.10);
        BetanoSelection oX = new BetanoSelection();
        oX.setId("X");
        oX.setName("Empate");
        oX.setPrice(3.30);
        BetanoSelection o2 = new BetanoSelection();
        o2.setId("2");
        o2.setName("Porto");
        o2.setPrice(3.50);
        out1X2.add(o1);
        out1X2.add(oX);
        out1X2.add(o2);
        m1X2.setSelections(out1X2);
        markets.add(m1X2);

        // Totals market (Mais/Menos 2.5)
        BetanoMarket mTotal = new BetanoMarket();
        mTotal.setName("Total de Gols Mais/Menos");
        mTotal.setType("TOTAL_GOALS");
        List<BetanoSelection> outTotal = new ArrayList<>();
        BetanoSelection oOver = new BetanoSelection();
        oOver.setId("O25");
        oOver.setName("Mais de 2.5");
        oOver.setHandicap(2.5);
        oOver.setPrice(1.88);
        BetanoSelection oUnder = new BetanoSelection();
        oUnder.setId("U25");
        oUnder.setName("Menos de 2.5");
        oUnder.setHandicap(2.5);
        oUnder.setPrice(1.92);
        outTotal.add(oOver);
        outTotal.add(oUnder);
        mTotal.setSelections(outTotal);
        markets.add(mTotal);

        // BTTS market
        BetanoMarket mBtts = new BetanoMarket();
        mBtts.setName("Ambas as equipas marcam");
        mBtts.setType("BTTS");
        List<BetanoSelection> outBtts = new ArrayList<>();
        BetanoSelection oYes = new BetanoSelection();
        oYes.setId("Y");
        oYes.setName("Sim");
        oYes.setPrice(1.72);
        BetanoSelection oNo = new BetanoSelection();
        oNo.setId("N");
        oNo.setName("Não");
        oNo.setPrice(2.05);
        outBtts.add(oYes);
        outBtts.add(oNo);
        mBtts.setSelections(outBtts);
        markets.add(mBtts);

        // Handicap market (-0.5 / +0.5)
        BetanoMarket mHcap = new BetanoMarket();
        mHcap.setName("Handicap Asiático");
        mHcap.setType("ASIAN_HANDICAP");
        List<BetanoSelection> outHcap = new ArrayList<>();
        BetanoSelection oH1 = new BetanoSelection();
        oH1.setId("H1");
        oH1.setName("Benfica (-0.5)");
        oH1.setHandicap(-0.5);
        oH1.setPrice(2.08);
        BetanoSelection oH2 = new BetanoSelection();
        oH2.setId("H2");
        oH2.setName("Porto (+0.5)");
        oH2.setHandicap(0.5);
        oH2.setPrice(1.75);
        outHcap.add(oH1);
        outHcap.add(oH2);
        mHcap.setSelections(outHcap);
        markets.add(mHcap);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals("betano", request.getBookmaker());
        assertEquals("4001", request.getExternalEventId());
        assertEquals(9, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.30));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.50));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.88));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.92));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.72));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.05));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 2.08));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 1.75));
    }

    @Test
    void testMapPeriodScopes_1stHalfAnd2ndHalf() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("4002");
        cache.setTeam1("Sporting CP");
        cache.setTeam2("Braga");
        cache.setSportName("Football");

        BetanoEvent event = new BetanoEvent();
        event.setId("4002");

        List<BetanoMarket> markets = new ArrayList<>();

        // 1st Half 1X2
        BetanoMarket h1Market = new BetanoMarket();
        h1Market.setName("1º Tempo - Resultado Final");
        h1Market.setType("HALF_1_RESULT");
        BetanoSelection s1 = new BetanoSelection();
        s1.setId("h1_1");
        s1.setName("Sporting CP");
        s1.setPrice(2.35);
        BetanoSelection sX = new BetanoSelection();
        sX.setId("h1_X");
        sX.setName("Empate");
        sX.setPrice(2.10);
        BetanoSelection s2 = new BetanoSelection();
        s2.setId("h1_2");
        s2.setName("Braga");
        s2.setPrice(4.20);
        h1Market.setSelections(List.of(s1, sX, s2));
        markets.add(h1Market);

        // 1st Half Total
        BetanoMarket h1Total = new BetanoMarket();
        h1Total.setName("1º Tempo - Total de Gols Mais/Menos 1.5");
        h1Total.setType("HALF_1_TOTAL");
        BetanoSelection o15 = new BetanoSelection();
        o15.setId("h1_over");
        o15.setName("Mais de 1.5");
        o15.setHandicap(1.5);
        o15.setPrice(2.65);
        BetanoSelection u15 = new BetanoSelection();
        u15.setId("h1_under");
        u15.setName("Menos de 1.5");
        u15.setHandicap(1.5);
        u15.setPrice(1.45);
        h1Total.setSelections(List.of(o15, u15));
        markets.add(h1Total);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals(5, request.getOdds().size());

        // Verify scope is HALF_1
        assertTrue(request.getOdds().stream().allMatch(o -> {
            if (o.getBetType() instanceof MatchResultBet mrb) {
                return mrb.scope() == BetScope.HALF_1;
            } else if (o.getBetType() instanceof TotalBet tb) {
                return tb.scope() == BetScope.HALF_1;
            }
            return false;
        }));
    }

    @Test
    void testMapTeamTotalsAndDoubleChanceAndDnbAndHtFt() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("4003");
        cache.setTeam1("Arsenal");
        cache.setTeam2("Chelsea");
        cache.setSportName("Soccer");

        BetanoEvent event = new BetanoEvent();
        event.setId("4003");

        List<BetanoMarket> markets = new ArrayList<>();

        // Team 1 Total
        BetanoMarket t1Total = new BetanoMarket();
        t1Total.setName("Arsenal - Total de Gols");
        t1Total.setType("TEAM_TOTAL");
        BetanoSelection t1Over = new BetanoSelection();
        t1Over.setId("t1_o");
        t1Over.setName("Mais de 1.5");
        t1Over.setHandicap(1.5);
        t1Over.setPrice(1.85);
        t1Total.setSelections(List.of(t1Over));
        markets.add(t1Total);

        // Double Chance
        BetanoMarket dc = new BetanoMarket();
        dc.setName("Dupla Chance");
        dc.setType("DOUBLE_CHANCE");
        BetanoSelection dc1X = new BetanoSelection();
        dc1X.setId("dc_1x");
        dc1X.setName("1X");
        dc1X.setPrice(1.30);
        dc.setSelections(List.of(dc1X));
        markets.add(dc);

        // Draw No Bet
        BetanoMarket dnb = new BetanoMarket();
        dnb.setName("Empate Anula Aposta");
        dnb.setType("DRAW_NO_BET");
        BetanoSelection dnb1 = new BetanoSelection();
        dnb1.setId("dnb_1");
        dnb1.setName("Arsenal");
        dnb1.setPrice(1.48);
        dnb.setSelections(List.of(dnb1));
        markets.add(dnb);

        // HT/FT
        BetanoMarket htft = new BetanoMarket();
        htft.setName("Intervalo/Final do Jogo");
        htft.setType("HT_FT");
        BetanoSelection htft11 = new BetanoSelection();
        htft11.setId("htft_11");
        htft11.setName("1/1");
        htft11.setPrice(3.20);
        htft.setSelections(List.of(htft11));
        markets.add(htft);

        event.setMarkets(markets);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        // Verify Team Total
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof TotalBet tb
                        && tb.subject() == BetSubject.TEAM1
                        && tb.direction() == TotalBet.Direction.OVER
                        && tb.param() == 1.5));

        // Verify Double Chance
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof MatchResultBet mrb
                        && mrb.outcome() == MatchResultBet.Outcome.DC_1X));

        // Verify Draw No Bet (Handicap 0.0)
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof HandicapBet hb
                        && hb.outcome() == HandicapBet.Outcome.TEAM1
                        && hb.param() == 0.0));

        // Verify HT/FT
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof HalfTimeFullTimeBet htftBet
                        && htftBet.outcome() == HalfTimeFullTimeBet.Outcome.W1_W1));
    }
}
