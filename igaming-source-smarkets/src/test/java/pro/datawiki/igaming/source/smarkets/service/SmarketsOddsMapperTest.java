package pro.datawiki.igaming.source.smarkets.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SmarketsOddsMapperTest {

    private SmarketsOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SmarketsOddsMapper();
    }

    private SmarketsContractQuotes createQuote(double odds) {
        SmarketsContractQuotes quotes = new SmarketsContractQuotes();
        SmarketsQuoteEntry entry = new SmarketsQuoteEntry();
        entry.setPrice((int) Math.round(10000.0 / odds));
        quotes.setBids(List.of(entry));
        return quotes;
    }

    @Test
    void testPriceConversionToOdds() {
        SmarketsQuoteEntry quote = new SmarketsQuoteEntry();
        quote.setPrice(5000); // 50%
        assertEquals(2.0, quote.getDecimalOdds(), 0.001);

        quote.setPrice(2500); // 25%
        assertEquals(4.0, quote.getDecimalOdds(), 0.001);

        quote.setPrice(5236); // 52.36%
        assertEquals(1.9098, quote.getDecimalOdds(), 0.001);
    }

    @Test
    void testMapFootballMatchWinnerAndTotal() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("45291587");
        event.setName("Man Utd vs Man City");
        event.setType("football_match");
        event.setStartDatetime("2026-09-13T15:30:00Z");
        event.setState("upcoming");
        event.setSlug("premier-league");

        // Winner Market
        SmarketsMarket winnerMarket = new SmarketsMarket();
        winnerMarket.setId("m1");
        winnerMarket.setName("Full-time result");
        SmarketsMarketType mt1 = new SmarketsMarketType();
        mt1.setName("WINNER_3_WAY");
        winnerMarket.setMarketType(mt1);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c1");
        cHome.setName("Man Utd");

        SmarketsContract cDraw = new SmarketsContract();
        cDraw.setId("c2");
        cDraw.setName("Draw");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c3");
        cAway.setName("Man City");

        // Over/Under Market
        SmarketsMarket totalMarket = new SmarketsMarket();
        totalMarket.setId("m2");
        totalMarket.setName("Over/under 2.5 goals");
        SmarketsMarketType mt2 = new SmarketsMarketType();
        mt2.setName("OVER_UNDER");
        mt2.setParam("2.5");
        totalMarket.setMarketType(mt2);

        SmarketsContract cOver = new SmarketsContract();
        cOver.setId("c4");
        cOver.setName("Over 2.5");

        SmarketsContract cUnder = new SmarketsContract();
        cUnder.setId("c5");
        cUnder.setName("Under 2.5");

        Map<String, List<SmarketsContract>> contractsMap = new HashMap<>();
        contractsMap.put("m1", List.of(cHome, cDraw, cAway));
        contractsMap.put("m2", List.of(cOver, cUnder));

        // Quotes
        Map<String, SmarketsContractQuotes> quotesMap = new HashMap<>();
        quotesMap.put("c1", createQuote(3.45));
        quotesMap.put("c2", createQuote(3.60));
        quotesMap.put("c3", createQuote(2.05));
        quotesMap.put("c4", createQuote(1.85));
        quotesMap.put("c5", createQuote(2.02));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(winnerMarket, totalMarket),
                contractsMap,
                quotesMap
        );

        assertNotNull(request);
        assertEquals("smarkets", request.getBookmaker());
        assertEquals("Man Utd", request.getTeam1());
        assertEquals("Man City", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertFalse(request.getIsLive());
        assertEquals(5, request.getOdds().size());

        OddItem itemHome = request.getOdds().stream().filter(o -> "c1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHome);
        assertTrue(itemHome.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) itemHome.getBetType()).outcome());
        assertEquals(3.45, itemHome.getValue(), 0.05);

        OddItem itemDraw = request.getOdds().stream().filter(o -> "c2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDraw);
        assertTrue(itemDraw.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) itemDraw.getBetType()).outcome());
        assertEquals(3.60, itemDraw.getValue(), 0.05);

        OddItem itemAway = request.getOdds().stream().filter(o -> "c3".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemAway);
        assertTrue(itemAway.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) itemAway.getBetType()).outcome());
        assertEquals(2.05, itemAway.getValue(), 0.05);

        OddItem itemOver = request.getOdds().stream().filter(o -> "c4".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemOver);
        assertTrue(itemOver.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemOver.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemOver.getBetType()).param());

        OddItem itemUnder = request.getOdds().stream().filter(o -> "c5".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemUnder);
        assertTrue(itemUnder.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) itemUnder.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemUnder.getBetType()).param());
    }

    @Test
    void testDoubleChanceMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-dc");
        event.setName("Arsenal vs Chelsea");
        event.setType("football_match");

        SmarketsMarket dcMarket = new SmarketsMarket();
        dcMarket.setId("m-dc");
        dcMarket.setName("Double chance");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("DOUBLE_CHANCE");
        dcMarket.setMarketType(mt);

        SmarketsContract c1X = new SmarketsContract();
        c1X.setId("c-1x");
        c1X.setName("Arsenal or Draw");

        SmarketsContract c12 = new SmarketsContract();
        c12.setId("c-12");
        c12.setName("Arsenal or Chelsea");

        SmarketsContract cX2 = new SmarketsContract();
        cX2.setId("c-x2");
        cX2.setName("Draw or Chelsea");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-dc", List.of(c1X, c12, cX2));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-1x", createQuote(1.35),
                "c-12", createQuote(1.28),
                "c-x2", createQuote(1.95)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(dcMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem item1X = request.getOdds().stream().filter(o -> "c-1x".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1X);
        assertTrue(item1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) item1X.getBetType()).outcome());

        OddItem item12 = request.getOdds().stream().filter(o -> "c-12".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item12);
        assertTrue(item12.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) item12.getBetType()).outcome());

        OddItem itemX2 = request.getOdds().stream().filter(o -> "c-x2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemX2);
        assertTrue(itemX2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) itemX2.getBetType()).outcome());
    }

    @Test
    void testDrawNoBetMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-dnb");
        event.setName("Liverpool vs Real Madrid");
        event.setType("football_match");

        SmarketsMarket dnbMarket = new SmarketsMarket();
        dnbMarket.setId("m-dnb");
        dnbMarket.setName("Draw no bet");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("DRAW_NO_BET");
        dnbMarket.setMarketType(mt);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c-dnb-1");
        cHome.setName("Liverpool");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c-dnb-2");
        cAway.setName("Real Madrid");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-dnb", List.of(cHome, cAway));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-dnb-1", createQuote(1.65),
                "c-dnb-2", createQuote(2.25)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(dnbMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem itemDnb1 = request.getOdds().stream().filter(o -> "c-dnb-1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb1);
        assertTrue(itemDnb1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) itemDnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(0.0, hb1.param());

        OddItem itemDnb2 = request.getOdds().stream().filter(o -> "c-dnb-2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb2);
        assertTrue(itemDnb2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) itemDnb2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.0, hb2.param());
    }

    @Test
    void testHandicapMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-hcap");
        event.setName("Bayern Munich vs Dortmund");
        event.setType("football_match");

        SmarketsMarket hcapMarket = new SmarketsMarket();
        hcapMarket.setId("m-hcap");
        hcapMarket.setName("Asian handicap -1.5");
        SmarketsMarketType mt = new SmarketsMarketType();
        mt.setName("HANDICAP");
        mt.setParam("-1.5");
        hcapMarket.setMarketType(mt);

        SmarketsContract cHome = new SmarketsContract();
        cHome.setId("c-h1");
        cHome.setName("Bayern Munich -1.5");

        SmarketsContract cAway = new SmarketsContract();
        cAway.setId("c-h2");
        cAway.setName("Dortmund +1.5");

        Map<String, List<SmarketsContract>> contractsMap = Map.of("m-hcap", List.of(cHome, cAway));
        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-h1", createQuote(2.10),
                "c-h2", createQuote(1.78)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event, List.of(hcapMarket), contractsMap, quotesMap);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem itemH1 = request.getOdds().stream().filter(o -> "c-h1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemH1);
        assertTrue(itemH1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) itemH1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param());

        OddItem itemH2 = request.getOdds().stream().filter(o -> "c-h2".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemH2);
        assertTrue(itemH2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) itemH2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param());
    }

    @Test
    void testBttsAndCorrectScoreMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-btts-cs");
        event.setName("Barcelona vs Juventus");
        event.setType("football_match");

        // BTTS Market
        SmarketsMarket bttsMarket = new SmarketsMarket();
        bttsMarket.setId("m-btts");
        bttsMarket.setName("Both teams to score");

        SmarketsContract cYes = new SmarketsContract();
        cYes.setId("c-yes");
        cYes.setName("Yes");

        SmarketsContract cNo = new SmarketsContract();
        cNo.setId("c-no");
        cNo.setName("No");

        // Correct Score Market
        SmarketsMarket csMarket = new SmarketsMarket();
        csMarket.setId("m-cs");
        csMarket.setName("Correct score");

        SmarketsContract cs21 = new SmarketsContract();
        cs21.setId("c-21");
        cs21.setName("2 - 1");

        SmarketsContract cs11 = new SmarketsContract();
        cs11.setId("c-11");
        cs11.setName("1 - 1");

        Map<String, List<SmarketsContract>> contractsMap = Map.of(
                "m-btts", List.of(cYes, cNo),
                "m-cs", List.of(cs21, cs11)
        );

        Map<String, SmarketsContractQuotes> quotesMap = Map.of(
                "c-yes", createQuote(1.72),
                "c-no", createQuote(2.15),
                "c-21", createQuote(8.50),
                "c-11", createQuote(6.20)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(bttsMarket, csMarket),
                contractsMap,
                quotesMap
        );

        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem itemBttsYes = request.getOdds().stream().filter(o -> "c-yes".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsYes);
        assertTrue(itemBttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbYes = (BinaryMarketBet) itemBttsYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbYes.outcome());

        OddItem itemBttsNo = request.getOdds().stream().filter(o -> "c-no".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsNo);
        assertTrue(itemBttsNo.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbNo = (BinaryMarketBet) itemBttsNo.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbNo.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, bmbNo.outcome());

        OddItem itemCs21 = request.getOdds().stream().filter(o -> "c-21".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs21);
        assertTrue(itemCs21.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs21Bet = (CorrectScoreBet) itemCs21.getBetType();
        assertEquals(2, cs21Bet.score1());
        assertEquals(1, cs21Bet.score2());
        assertFalse(cs21Bet.isAnyOtherScore());

        OddItem itemCs11 = request.getOdds().stream().filter(o -> "c-11".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs11);
        assertTrue(itemCs11.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs11Bet = (CorrectScoreBet) itemCs11.getBetType();
        assertEquals(1, cs11Bet.score1());
        assertEquals(1, cs11Bet.score2());
        assertFalse(cs11Bet.isAnyOtherScore());
    }

    @Test
    void testEsportsMapWinnerAndTotalMaps() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-cs2-1");
        event.setName("Natus Vincere vs FaZe Clan");
        event.setType("cs2_match");

        // Market 1: Map 1 Winner
        SmarketsMarket map1Winner = new SmarketsMarket();
        map1Winner.setId("m-m1-win");
        map1Winner.setName("Map 1 winner");
        SmarketsMarketType mt1 = new SmarketsMarketType();
        mt1.setName("WINNER_2_WAY");
        map1Winner.setMarketType(mt1);

        SmarketsContract cNaviM1 = new SmarketsContract();
        cNaviM1.setId("c-navi-m1");
        cNaviM1.setName("Natus Vincere");

        SmarketsContract cFazeM1 = new SmarketsContract();
        cFazeM1.setId("c-faze-m1");
        cFazeM1.setName("FaZe Clan");

        // Market 2: Total Maps
        SmarketsMarket totalMaps = new SmarketsMarket();
        totalMaps.setId("m-tot-maps");
        totalMaps.setName("Total maps played");
        SmarketsMarketType mt2 = new SmarketsMarketType();
        mt2.setName("OVER_UNDER");
        mt2.setParam("2.5");
        totalMaps.setMarketType(mt2);

        SmarketsContract cOverMaps = new SmarketsContract();
        cOverMaps.setId("c-ov-maps");
        cOverMaps.setName("Over 2.5");

        SmarketsContract cUnderMaps = new SmarketsContract();
        cUnderMaps.setId("c-un-maps");
        cUnderMaps.setName("Under 2.5");

        // Market 3: Map Handicap
        SmarketsMarket mapHandicap = new SmarketsMarket();
        mapHandicap.setId("m-hcap-maps");
        mapHandicap.setName("Map handicap");
        SmarketsMarketType mt3 = new SmarketsMarketType();
        mt3.setName("HANDICAP");
        mapHandicap.setMarketType(mt3);

        SmarketsContract cNaviHcap = new SmarketsContract();
        cNaviHcap.setId("c-navi-hcap");
        cNaviHcap.setName("Natus Vincere -1.5");

        SmarketsContract cFazeHcap = new SmarketsContract();
        cFazeHcap.setId("c-faze-hcap");
        cFazeHcap.setName("FaZe Clan +1.5");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-m1-win", List.of(cNaviM1, cFazeM1),
                "m-tot-maps", List.of(cOverMaps, cUnderMaps),
                "m-hcap-maps", List.of(cNaviHcap, cFazeHcap)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-navi-m1", createQuote(1.75),
                "c-faze-m1", createQuote(2.10),
                "c-ov-maps", createQuote(1.90),
                "c-un-maps", createQuote(1.90),
                "c-navi-hcap", createQuote(2.80),
                "c-faze-hcap", createQuote(1.45)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(map1Winner, totalMaps, mapHandicap),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem itemM1 = request.getOdds().stream().filter(o -> "c-navi-m1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemM1);
        assertTrue(itemM1.getBetType() instanceof MatchResultBet);
        MatchResultBet m1Bet = (MatchResultBet) itemM1.getBetType();
        assertEquals(BetScope.MAP_1, m1Bet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m1Bet.outcome());
        assertEquals(StatType.MATCH, m1Bet.statType());

        OddItem itemTotMaps = request.getOdds().stream().filter(o -> "c-ov-maps".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemTotMaps);
        assertTrue(itemTotMaps.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) itemTotMaps.getBetType();
        assertEquals(BetScope.FULL_MATCH, tb.scope());
        assertEquals(TotalBet.Direction.OVER, tb.direction());
        assertEquals(2.5, tb.param());
        assertEquals(StatType.MAPS, tb.statType());

        OddItem itemHcapMaps = request.getOdds().stream().filter(o -> "c-navi-hcap".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHcapMaps);
        assertTrue(itemHcapMaps.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) itemHcapMaps.getBetType();
        assertEquals(BetScope.FULL_MATCH, hb.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-1.5, hb.param());
        assertEquals(StatType.MAPS, hb.statType());
    }

    @Test
    void testEsportsRoundsAndKillsAndFirstBlood() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-dota-1");
        event.setName("Team Spirit vs Team Liquid");
        event.setType("dota_2_match");

        // Market 1: Map 1 Total rounds
        SmarketsMarket mRounds = new SmarketsMarket();
        mRounds.setId("m-r-tot");
        mRounds.setName("Map 1 - Total rounds");
        mRounds.setMarketType(new SmarketsMarketType());

        SmarketsContract cOverR = new SmarketsContract();
        cOverR.setId("c-ov-r");
        cOverR.setName("Over 21.5");

        // Market 2: Map 1 Round handicap
        SmarketsMarket mRoundsHcap = new SmarketsMarket();
        mRoundsHcap.setId("m-r-hcap");
        mRoundsHcap.setName("Map 1 - Round handicap");
        mRoundsHcap.setMarketType(new SmarketsMarketType());

        SmarketsContract cSpiritHcap = new SmarketsContract();
        cSpiritHcap.setId("c-sp-hcap");
        cSpiritHcap.setName("Team Spirit -2.5");

        // Market 3: Map 1 Total kills
        SmarketsMarket mKills = new SmarketsMarket();
        mKills.setId("m-kills");
        mKills.setName("Map 1 - Total kills");
        mKills.setMarketType(new SmarketsMarketType());

        SmarketsContract cOverK = new SmarketsContract();
        cOverK.setId("c-ov-k");
        cOverK.setName("Over 48.5");

        // Market 4: First blood
        SmarketsMarket mFb = new SmarketsMarket();
        mFb.setId("m-fb");
        mFb.setName("Map 1 - First blood");
        mFb.setMarketType(new SmarketsMarketType());

        SmarketsContract cSpiritFb = new SmarketsContract();
        cSpiritFb.setId("c-sp-fb");
        cSpiritFb.setName("Team Spirit");

        SmarketsContract cLiquidFb = new SmarketsContract();
        cLiquidFb.setId("c-lq-fb");
        cLiquidFb.setName("Team Liquid");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-r-tot", List.of(cOverR),
                "m-r-hcap", List.of(cSpiritHcap),
                "m-kills", List.of(cOverK),
                "m-fb", List.of(cSpiritFb, cLiquidFb)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-ov-r", createQuote(1.88),
                "c-sp-hcap", createQuote(1.95),
                "c-ov-k", createQuote(1.85),
                "c-sp-fb", createQuote(1.72),
                "c-lq-fb", createQuote(2.05)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(mRounds, mRoundsHcap, mKills, mFb),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());
        assertEquals(5, request.getOdds().size());

        OddItem itemRounds = request.getOdds().stream().filter(o -> "c-ov-r".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemRounds);
        assertTrue(itemRounds.getBetType() instanceof TotalBet);
        TotalBet tbR = (TotalBet) itemRounds.getBetType();
        assertEquals(BetScope.MAP_1, tbR.scope());
        assertEquals(StatType.ROUNDS, tbR.statType());
        assertEquals(21.5, tbR.param());

        OddItem itemHcapR = request.getOdds().stream().filter(o -> "c-sp-hcap".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHcapR);
        assertTrue(itemHcapR.getBetType() instanceof HandicapBet);
        HandicapBet hbR = (HandicapBet) itemHcapR.getBetType();
        assertEquals(BetScope.MAP_1, hbR.scope());
        assertEquals(StatType.ROUNDS, hbR.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hbR.outcome());
        assertEquals(-2.5, hbR.param());

        OddItem itemKills = request.getOdds().stream().filter(o -> "c-ov-k".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemKills);
        assertTrue(itemKills.getBetType() instanceof TotalBet);
        TotalBet tbK = (TotalBet) itemKills.getBetType();
        assertEquals(BetScope.MAP_1, tbK.scope());
        assertEquals(StatType.KILLS, tbK.statType());
        assertEquals(48.5, tbK.param());

        OddItem itemFb1 = request.getOdds().stream().filter(o -> "c-sp-fb".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemFb1);
        assertTrue(itemFb1.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) itemFb1.getBetType();
        assertEquals(BetScope.MAP_1, bmb.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
        assertEquals(StatType.FIRST_BLOOD, bmb.statType());
    }

    @Test
    void testCornersMarkets() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-corn-1");
        event.setName("Liverpool vs Man City");
        event.setType("football_match");

        // Total corners
        SmarketsMarket mTotCorners = new SmarketsMarket();
        mTotCorners.setId("m-corn-tot");
        mTotCorners.setName("Total corners over/under 9.5");

        SmarketsContract cOver = new SmarketsContract();
        cOver.setId("c-c-ov");
        cOver.setName("Over 9.5");

        SmarketsContract cUnder = new SmarketsContract();
        cUnder.setId("c-c-un");
        cUnder.setName("Under 9.5");

        // Liverpool Team corners
        SmarketsMarket mHomeCorners = new SmarketsMarket();
        mHomeCorners.setId("m-corn-home");
        mHomeCorners.setName("Liverpool total corners");

        SmarketsContract cHomeOver = new SmarketsContract();
        cHomeOver.setId("c-c-h-ov");
        cHomeOver.setName("Over 4.5");

        // Corner Handicap
        SmarketsMarket mCornHcap = new SmarketsMarket();
        mCornHcap.setId("m-corn-hcap");
        mCornHcap.setName("Corners handicap");

        SmarketsContract cCornHcap1 = new SmarketsContract();
        cCornHcap1.setId("c-c-h1");
        cCornHcap1.setName("Liverpool -1.5");

        SmarketsContract cCornHcap2 = new SmarketsContract();
        cCornHcap2.setId("c-c-h2");
        cCornHcap2.setName("Man City +1.5");

        // Corners 1X2
        SmarketsMarket mCorn1X2 = new SmarketsMarket();
        mCorn1X2.setId("m-corn-1x2");
        mCorn1X2.setName("Most corners");

        SmarketsContract cCornW1 = new SmarketsContract();
        cCornW1.setId("c-c-w1");
        cCornW1.setName("Liverpool");

        SmarketsContract cCornDraw = new SmarketsContract();
        cCornDraw.setId("c-c-x");
        cCornDraw.setName("Draw");

        SmarketsContract cCornW2 = new SmarketsContract();
        cCornW2.setId("c-c-w2");
        cCornW2.setName("Man City");

        // First corner
        SmarketsMarket mFirstCorn = new SmarketsMarket();
        mFirstCorn.setId("m-corn-first");
        mFirstCorn.setName("First corner");

        SmarketsContract cFirst1 = new SmarketsContract();
        cFirst1.setId("c-fc-1");
        cFirst1.setName("Liverpool");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-corn-tot", List.of(cOver, cUnder),
                "m-corn-home", List.of(cHomeOver),
                "m-corn-hcap", List.of(cCornHcap1, cCornHcap2),
                "m-corn-1x2", List.of(cCornW1, cCornDraw, cCornW2),
                "m-corn-first", List.of(cFirst1)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-c-ov", createQuote(1.90),
                "c-c-un", createQuote(1.90),
                "c-c-h-ov", createQuote(1.80),
                "c-c-h1", createQuote(2.05),
                "c-c-h2", createQuote(1.75),
                "c-c-w1", createQuote(2.10),
                "c-c-x", createQuote(6.50),
                "c-c-w2", createQuote(1.95),
                "c-fc-1", createQuote(1.85)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(mTotCorners, mHomeCorners, mCornHcap, mCorn1X2, mFirstCorn),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(9, request.getOdds().size());

        // Total
        OddItem itemTot = request.getOdds().stream().filter(o -> "c-c-ov".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemTot);
        assertTrue(itemTot.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) itemTot.getBetType();
        assertEquals(StatType.CORNERS, tb.statType());
        assertEquals(BetSubject.MATCH, tb.subject());
        assertEquals(9.5, tb.param());

        // Home Total
        OddItem itemHomeTot = request.getOdds().stream().filter(o -> "c-c-h-ov".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHomeTot);
        assertTrue(itemHomeTot.getBetType() instanceof TotalBet);
        TotalBet tbHome = (TotalBet) itemHomeTot.getBetType();
        assertEquals(StatType.CORNERS, tbHome.statType());
        assertEquals(BetSubject.TEAM1, tbHome.subject());
        assertEquals(4.5, tbHome.param());

        // Handicap
        OddItem itemHcap1 = request.getOdds().stream().filter(o -> "c-c-h1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemHcap1);
        assertTrue(itemHcap1.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) itemHcap1.getBetType();
        assertEquals(StatType.CORNERS, hb.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-1.5, hb.param());

        // 1X2
        OddItem item1X2Draw = request.getOdds().stream().filter(o -> "c-c-x".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1X2Draw);
        assertTrue(item1X2Draw.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) item1X2Draw.getBetType();
        assertEquals(StatType.CORNERS, mrb.statType());
        assertEquals(MatchResultBet.Outcome.DRAW, mrb.outcome());

        // First corner
        OddItem itemFc = request.getOdds().stream().filter(o -> "c-fc-1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemFc);
        assertTrue(itemFc.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) itemFc.getBetType();
        assertEquals(StatType.CORNERS, bmb.statType());
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
    }

    @Test
    void testCardsMarkets() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-card-1");
        event.setName("Real Madrid vs Barcelona");
        event.setType("football_match");

        // Yellow cards total
        SmarketsMarket mYcTot = new SmarketsMarket();
        mYcTot.setId("m-yc-tot");
        mYcTot.setName("Yellow cards total over/under 4.5");

        SmarketsContract cYcOver = new SmarketsContract();
        cYcOver.setId("c-yc-ov");
        cYcOver.setName("Over 4.5");

        // Yellow card handicap
        SmarketsMarket mYcHcap = new SmarketsMarket();
        mYcHcap.setId("m-yc-hcap");
        mYcHcap.setName("Yellow cards handicap");

        SmarketsContract cYcHcap1 = new SmarketsContract();
        cYcHcap1.setId("c-yc-h1");
        cYcHcap1.setName("Real Madrid -0.5");

        // Red Card
        SmarketsMarket mRed = new SmarketsMarket();
        mRed.setId("m-rc");
        mRed.setName("Red card in match");

        SmarketsContract cRedYes = new SmarketsContract();
        cRedYes.setId("c-rc-y");
        cRedYes.setName("Yes");

        SmarketsContract cRedNo = new SmarketsContract();
        cRedNo.setId("c-rc-n");
        cRedNo.setName("No");

        // First Card
        SmarketsMarket mFirstCard = new SmarketsMarket();
        mFirstCard.setId("m-fc");
        mFirstCard.setName("First card");

        SmarketsContract cFcHome = new SmarketsContract();
        cFcHome.setId("c-fc-1");
        cFcHome.setName("Real Madrid");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-yc-tot", List.of(cYcOver),
                "m-yc-hcap", List.of(cYcHcap1),
                "m-rc", List.of(cRedYes, cRedNo),
                "m-fc", List.of(cFcHome)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-yc-ov", createQuote(1.95),
                "c-yc-h1", createQuote(2.00),
                "c-rc-y", createQuote(3.80),
                "c-rc-n", createQuote(1.22),
                "c-fc-1", createQuote(1.90)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(mYcTot, mYcHcap, mRed, mFirstCard),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(5, request.getOdds().size());

        // Yellow cards total
        OddItem itemYcTot = request.getOdds().stream().filter(o -> "c-yc-ov".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemYcTot);
        assertTrue(itemYcTot.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) itemYcTot.getBetType();
        assertEquals(StatType.YELLOW_CARDS, tb.statType());
        assertEquals(4.5, tb.param());

        // Yellow cards handicap
        OddItem itemYcHcap = request.getOdds().stream().filter(o -> "c-yc-h1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemYcHcap);
        assertTrue(itemYcHcap.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) itemYcHcap.getBetType();
        assertEquals(StatType.YELLOW_CARDS, hb.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-0.5, hb.param());

        // Red Card
        OddItem itemRcYes = request.getOdds().stream().filter(o -> "c-rc-y".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemRcYes);
        assertTrue(itemRcYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbRc = (BinaryMarketBet) itemRcYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, bmbRc.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbRc.outcome());

        // First Card
        OddItem itemFc = request.getOdds().stream().filter(o -> "c-fc-1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemFc);
        assertTrue(itemFc.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbFc = (BinaryMarketBet) itemFc.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CARD, bmbFc.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmbFc.outcome());
    }

    @Test
    void testPeriodMarkets() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-half-1");
        event.setName("Arsenal vs Chelsea");
        event.setType("football_match");

        // 1st Half Result
        SmarketsMarket m1hResult = new SmarketsMarket();
        m1hResult.setId("m-1h-res");
        m1hResult.setName("1st half result");

        SmarketsContract c1hHome = new SmarketsContract();
        c1hHome.setId("c-1h-1");
        c1hHome.setName("Arsenal");

        SmarketsContract c1hDraw = new SmarketsContract();
        c1hDraw.setId("c-1h-x");
        c1hDraw.setName("Draw");

        // 1st Half Total
        SmarketsMarket m1hTot = new SmarketsMarket();
        m1hTot.setId("m-1h-tot");
        m1hTot.setName("1st half over/under 1.5 goals");

        SmarketsContract c1hOver = new SmarketsContract();
        c1hOver.setId("c-1h-ov");
        c1hOver.setName("Over 1.5");

        // 1st Half Handicap
        SmarketsMarket m1hHcap = new SmarketsMarket();
        m1hHcap.setId("m-1h-hcap");
        m1hHcap.setName("1st half handicap");

        SmarketsContract c1hHcap1 = new SmarketsContract();
        c1hHcap1.setId("c-1h-h1");
        c1hHcap1.setName("Arsenal -0.5");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-1h-res", List.of(c1hHome, c1hDraw),
                "m-1h-tot", List.of(c1hOver),
                "m-1h-hcap", List.of(c1hHcap1)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-1h-1", createQuote(2.40),
                "c-1h-x", createQuote(2.20),
                "c-1h-ov", createQuote(2.65),
                "c-1h-h1", createQuote(2.35)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(m1hResult, m1hTot, m1hHcap),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        // 1st Half 1X2
        OddItem item1hHome = request.getOdds().stream().filter(o -> "c-1h-1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1hHome);
        assertTrue(item1hHome.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) item1hHome.getBetType();
        assertEquals(BetScope.HALF_1, mrb.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());

        // 1st Half Total
        OddItem item1hTot = request.getOdds().stream().filter(o -> "c-1h-ov".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1hTot);
        assertTrue(item1hTot.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) item1hTot.getBetType();
        assertEquals(BetScope.HALF_1, tb.scope());
        assertEquals(TotalBet.Direction.OVER, tb.direction());
        assertEquals(1.5, tb.param());

        // 1st Half Handicap
        OddItem item1hHcap = request.getOdds().stream().filter(o -> "c-1h-h1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1hHcap);
        assertTrue(item1hHcap.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) item1hHcap.getBetType();
        assertEquals(BetScope.HALF_1, hb.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-0.5, hb.param());
    }

    @Test
    void testHalfTimeFullTimeMarket() {
        SmarketsEvent event = new SmarketsEvent();
        event.setId("ev-htft-1");
        event.setName("Bayern Munich vs PSG");
        event.setType("football_match");

        SmarketsMarket mHtFt = new SmarketsMarket();
        mHtFt.setId("m-htft");
        mHtFt.setName("Half-time / Full-time");

        SmarketsContract c11 = new SmarketsContract();
        c11.setId("c-htft-11");
        c11.setName("Bayern Munich / Bayern Munich");

        SmarketsContract cX1 = new SmarketsContract();
        cX1.setId("c-htft-x1");
        cX1.setName("Draw / Bayern Munich");

        SmarketsContract cXX = new SmarketsContract();
        cXX.setId("c-htft-xx");
        cXX.setName("Draw / Draw");

        SmarketsContract c22 = new SmarketsContract();
        c22.setId("c-htft-22");
        c22.setName("PSG / PSG");

        Map<String, List<SmarketsContract>> contracts = Map.of(
                "m-htft", List.of(c11, cX1, cXX, c22)
        );

        Map<String, SmarketsContractQuotes> quotes = Map.of(
                "c-htft-11", createQuote(2.80),
                "c-htft-x1", createQuote(5.00),
                "c-htft-xx", createQuote(6.50),
                "c-htft-22", createQuote(5.50)
        );

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(
                event,
                List.of(mHtFt),
                contracts,
                quotes
        );

        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem item11 = request.getOdds().stream().filter(o -> "c-htft-11".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item11);
        assertTrue(item11.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, ((HalfTimeFullTimeBet) item11.getBetType()).outcome());

        OddItem itemX1 = request.getOdds().stream().filter(o -> "c-htft-x1".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemX1);
        assertTrue(itemX1.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W1, ((HalfTimeFullTimeBet) itemX1.getBetType()).outcome());

        OddItem itemXX = request.getOdds().stream().filter(o -> "c-htft-xx".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemXX);
        assertTrue(itemXX.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.X_X, ((HalfTimeFullTimeBet) itemXX.getBetType()).outcome());

        OddItem item22 = request.getOdds().stream().filter(o -> "c-htft-22".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item22);
        assertTrue(item22.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W2, ((HalfTimeFullTimeBet) item22.getBetType()).outcome());
    }
}
