package pro.datawiki.igaming.source.paf.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.paf.service.handler.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

public class PafOddsMapperTest {

    private PafOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);

        Mockito.when(sportNormalizationService.normalize("Football")).thenReturn(SportType.FOOTBALL);
        Mockito.when(sportNormalizationService.normalize("Jalkapallo")).thenReturn(SportType.FOOTBALL);
        Mockito.when(sportNormalizationService.normalize("CS2")).thenReturn(SportType.CS2);
        Mockito.when(sportNormalizationService.normalize("Dota 2")).thenReturn(SportType.DOTA2);
        Mockito.when(sportNormalizationService.normalize(anyString())).thenAnswer(inv -> {
            String sport = inv.getArgument(0);
            if (sport != null && (sport.toLowerCase().contains("cs") || sport.toLowerCase().contains("counter"))) {
                return SportType.CS2;
            }
            if (sport != null && sport.toLowerCase().contains("dota")) {
                return SportType.DOTA2;
            }
            return SportType.FOOTBALL;
        });

        List<PafMarketHandler> handlers = List.of(
                new PafEsportsHandler(),
                new PafStatsHandler(),
                new PafCorrectScoreHandler(),
                new PafBttsHandler(),
                new PafDrawNoBetHandler(),
                new PafDoubleChanceHandler(),
                new PafHandicapHandler(),
                new PafTotalHandler(),
                new PafMatchResultHandler()
        );

        oddsMapper = new PafOddsMapper(unmappedBetService, sportNormalizationService, handlers);
    }

    private MatchCache createFootballMatch() {
        MatchCache match = new MatchCache();
        match.setExternalId("100998877");
        match.setSportName("Football");
        match.setLeagueName("Champions League");
        match.setTeam1("Real Madrid");
        match.setTeam2("Manchester City");
        match.setIsLive(false);
        return match;
    }

    private MatchCache createEsportsMatch() {
        MatchCache match = new MatchCache();
        match.setExternalId("200998877");
        match.setSportName("CS2");
        match.setLeagueName("ESL Pro League");
        match.setTeam1("Natus Vincere");
        match.setTeam2("FaZe Clan");
        match.setIsLive(false);
        return match;
    }

    @Test
    public void testMatchResultAndDoubleChanceAndDrawNoBet() {
        MatchCache match = createFootballMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // 1. 1X2 Match
        KambiBetOffer bo1x2 = new KambiBetOffer();
        KambiBetOffer.KambiCriterion c1x2 = new KambiBetOffer.KambiCriterion();
        c1x2.setLabel("Match");
        c1x2.setEnglishLabel("Match");
        bo1x2.setCriterion(c1x2);

        KambiOutcome o1 = new KambiOutcome();
        o1.setId(101L);
        o1.setType("OT_ONE");
        o1.setLabel("Real Madrid");
        o1.setOdds(2400);

        KambiOutcome ox = new KambiOutcome();
        ox.setId(102L);
        ox.setType("OT_DRAW");
        ox.setLabel("Draw");
        ox.setOdds(3500);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(103L);
        o2.setType("OT_TWO");
        o2.setLabel("Manchester City");
        o2.setOdds(2800);

        bo1x2.setOutcomes(List.of(o1, ox, o2));
        offers.add(bo1x2);

        // 2. Double Chance
        KambiBetOffer boDc = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cDc = new KambiBetOffer.KambiCriterion();
        cDc.setLabel("Double Chance");
        cDc.setEnglishLabel("Double Chance");
        boDc.setCriterion(cDc);

        KambiOutcome dc1x = new KambiOutcome();
        dc1x.setId(201L);
        dc1x.setType("OT_ONE_OR_DRAW");
        dc1x.setLabel("1X");
        dc1x.setOdds(1450);

        KambiOutcome dc12 = new KambiOutcome();
        dc12.setId(202L);
        dc12.setType("OT_ONE_OR_TWO");
        dc12.setLabel("12");
        dc12.setOdds(1300);

        KambiOutcome dcX2 = new KambiOutcome();
        dcX2.setId(203L);
        dcX2.setType("OT_DRAW_OR_TWO");
        dcX2.setLabel("X2");
        dcX2.setOdds(1550);

        boDc.setOutcomes(List.of(dc1x, dc12, dcX2));
        offers.add(boDc);

        // 3. Draw No Bet
        KambiBetOffer boDnb = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cDnb = new KambiBetOffer.KambiCriterion();
        cDnb.setLabel("Draw No Bet");
        cDnb.setEnglishLabel("Draw No Bet");
        boDnb.setCriterion(cDnb);

        KambiOutcome dnb1 = new KambiOutcome();
        dnb1.setId(301L);
        dnb1.setType("OT_ONE");
        dnb1.setLabel("Real Madrid");
        dnb1.setOdds(1750);

        KambiOutcome dnb2 = new KambiOutcome();
        dnb2.setId(302L);
        dnb2.setType("OT_TWO");
        dnb2.setLabel("Manchester City");
        dnb2.setOdds(2050);

        boDnb.setOutcomes(List.of(dnb1, dnb2));
        offers.add(boDnb);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals("paf", req.getBookmaker());
        assertEquals("100998877", req.getExternalEventId());
        assertEquals(8, req.getOdds().size());

        // Validate 1X2
        OddItem w1 = req.getOdds().stream().filter(o -> "101".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(w1);
        assertEquals(2.40, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        // Validate Double Chance 1X
        OddItem item1x = req.getOdds().stream().filter(o -> "201".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1x);
        assertEquals(1.45, item1x.getValue());
        assertTrue(item1x.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) item1x.getBetType()).outcome());

        // Validate Draw No Bet
        OddItem itemDnb1 = req.getOdds().stream().filter(o -> "301".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb1);
        assertEquals(1.75, itemDnb1.getValue());
        assertTrue(itemDnb1.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) itemDnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(0.0, hb.param());
    }

    @Test
    public void testTotalsAndHandicap() {
        MatchCache match = createFootballMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // Total Goals Over/Under 2.5 with line in millicount (2500)
        KambiBetOffer boTotal = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cTotal = new KambiBetOffer.KambiCriterion();
        cTotal.setLabel("Total Goals");
        cTotal.setEnglishLabel("Total Goals");
        boTotal.setCriterion(cTotal);

        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(401L);
        oOver.setType("OT_OVER");
        oOver.setLine(2500.0);
        oOver.setLabel("Over 2.5");
        oOver.setOdds(1850);

        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(402L);
        oUnder.setType("OT_UNDER");
        oUnder.setLine(2500.0);
        oUnder.setLabel("Under 2.5");
        oUnder.setOdds(1950);

        boTotal.setOutcomes(List.of(oOver, oUnder));
        offers.add(boTotal);

        // Handicap -1.5 / +1.5
        KambiBetOffer boH = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cH = new KambiBetOffer.KambiCriterion();
        cH.setLabel("Handicap");
        cH.setEnglishLabel("Handicap");
        boH.setCriterion(cH);

        KambiOutcome h1 = new KambiOutcome();
        h1.setId(501L);
        h1.setType("OT_ONE");
        h1.setLine(-1.5);
        h1.setLabel("Real Madrid -1.5");
        h1.setOdds(4200);

        KambiOutcome h2 = new KambiOutcome();
        h2.setId(502L);
        h2.setType("OT_TWO");
        h2.setLine(1.5);
        h2.setLabel("Manchester City 1.5");
        h2.setOdds(1220);

        boH.setOutcomes(List.of(h1, h2));
        offers.add(boH);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals(4, req.getOdds().size());

        OddItem itemOver = req.getOdds().stream().filter(o -> "401".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemOver);
        assertEquals(1.85, itemOver.getValue());
        assertTrue(itemOver.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemOver.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemOver.getBetType()).param());

        OddItem itemH1 = req.getOdds().stream().filter(o -> "501".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemH1);
        assertEquals(4.20, itemH1.getValue());
        assertTrue(itemH1.getBetType() instanceof HandicapBet);
        assertEquals(-1.5, ((HandicapBet) itemH1.getBetType()).param());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) itemH1.getBetType()).outcome());
    }

    @Test
    public void testBttsAndCorrectScore() {
        MatchCache match = createFootballMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // BTTS
        KambiBetOffer boBtts = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cBtts = new KambiBetOffer.KambiCriterion();
        cBtts.setLabel("Both Teams to Score");
        cBtts.setEnglishLabel("Both Teams to Score");
        boBtts.setCriterion(cBtts);

        KambiOutcome bttsY = new KambiOutcome();
        bttsY.setId(601L);
        bttsY.setType("OT_YES");
        bttsY.setLabel("Yes");
        bttsY.setOdds(1650);

        KambiOutcome bttsN = new KambiOutcome();
        bttsN.setId(602L);
        bttsN.setType("OT_NO");
        bttsN.setLabel("No");
        bttsN.setOdds(2200);

        boBtts.setOutcomes(List.of(bttsY, bttsN));
        offers.add(boBtts);

        // Correct Score
        KambiBetOffer boCs = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCs = new KambiBetOffer.KambiCriterion();
        cCs.setLabel("Correct Score");
        cCs.setEnglishLabel("Correct Score");
        boCs.setCriterion(cCs);

        KambiOutcome cs21 = new KambiOutcome();
        cs21.setId(701L);
        cs21.setLabel("2 - 1");
        cs21.setOdds(8500);

        boCs.setOutcomes(List.of(cs21));
        offers.add(boCs);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals(3, req.getOdds().size());

        OddItem itemBtts = req.getOdds().stream().filter(o -> "601".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBtts);
        assertTrue(itemBtts.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) itemBtts.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmb.outcome());

        OddItem itemCs = req.getOdds().stream().filter(o -> "701".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs);
        assertEquals(8.5, itemCs.getValue());
        assertTrue(itemCs.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs = (CorrectScoreBet) itemCs.getBetType();
        assertEquals(2, cs.score1());
        assertEquals(1, cs.score2());
        assertFalse(cs.isAnyOtherScore());
    }

    @Test
    public void testCornersAndCardsStats() {
        MatchCache match = createFootballMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // Total Corners Over/Under 9.5
        KambiBetOffer boCorners = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCorners = new KambiBetOffer.KambiCriterion();
        cCorners.setLabel("Total Corners");
        cCorners.setEnglishLabel("Total Corners");
        boCorners.setCriterion(cCorners);

        KambiOutcome oCoOver = new KambiOutcome();
        oCoOver.setId(801L);
        oCoOver.setType("OT_OVER");
        oCoOver.setLine(9.5);
        oCoOver.setLabel("Over 9.5");
        oCoOver.setOdds(1900);

        KambiOutcome oCoUnder = new KambiOutcome();
        oCoUnder.setId(802L);
        oCoUnder.setType("OT_UNDER");
        oCoUnder.setLine(9.5);
        oCoUnder.setLabel("Under 9.5");
        oCoUnder.setOdds(1800);

        boCorners.setOutcomes(List.of(oCoOver, oCoUnder));
        offers.add(boCorners);

        // Yellow Cards 1X2
        KambiBetOffer boCards1x2 = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCards = new KambiBetOffer.KambiCriterion();
        cCards.setLabel("Most Yellow Cards");
        cCards.setEnglishLabel("Most Yellow Cards");
        boCards1x2.setCriterion(cCards);

        KambiOutcome cd1 = new KambiOutcome();
        cd1.setId(901L);
        cd1.setType("OT_ONE");
        cd1.setLabel("Real Madrid");
        cd1.setOdds(2200);

        KambiOutcome cdx = new KambiOutcome();
        cdx.setId(902L);
        cdx.setType("OT_DRAW");
        cdx.setLabel("Draw");
        cdx.setOdds(3900);

        KambiOutcome cd2 = new KambiOutcome();
        cd2.setId(903L);
        cd2.setType("OT_TWO");
        cd2.setLabel("Manchester City");
        cd2.setOdds(2600);

        boCards1x2.setOutcomes(List.of(cd1, cdx, cd2));
        offers.add(boCards1x2);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals(5, req.getOdds().size());

        // Validate Corners Total
        OddItem itemCorners = req.getOdds().stream().filter(o -> "801".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCorners);
        assertTrue(itemCorners.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) itemCorners.getBetType();
        assertEquals(StatType.CORNERS, tb.statType());
        assertEquals(9.5, tb.param());
        assertEquals(TotalBet.Direction.OVER, tb.direction());

        // Validate Yellow Cards 1X2
        OddItem itemCardWin = req.getOdds().stream().filter(o -> "901".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCardWin);
        assertTrue(itemCardWin.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) itemCardWin.getBetType();
        assertEquals(StatType.YELLOW_CARDS, mrb.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());
    }

    @Test
    public void testEsportsCS2Mapping() {
        MatchCache match = createEsportsMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // 1. Total Maps Over/Under 2.5
        KambiBetOffer boMapsTotal = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cMapsTotal = new KambiBetOffer.KambiCriterion();
        cMapsTotal.setLabel("Total Maps");
        cMapsTotal.setEnglishLabel("Total Maps");
        boMapsTotal.setCriterion(cMapsTotal);

        KambiOutcome moOver = new KambiOutcome();
        moOver.setId(1101L);
        moOver.setType("OT_OVER");
        moOver.setLine(2.5);
        moOver.setLabel("Over 2.5");
        moOver.setOdds(2000);

        KambiOutcome moUnder = new KambiOutcome();
        moUnder.setId(1102L);
        moUnder.setType("OT_UNDER");
        moUnder.setLine(2.5);
        moUnder.setLabel("Under 2.5");
        moUnder.setOdds(1720);

        boMapsTotal.setOutcomes(List.of(moOver, moUnder));
        offers.add(boMapsTotal);

        // 2. Map 1 Winner
        KambiBetOffer boMap1Winner = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cMap1Winner = new KambiBetOffer.KambiCriterion();
        cMap1Winner.setLabel("Map 1 Winner");
        cMap1Winner.setEnglishLabel("Map 1 Winner");
        boMap1Winner.setCriterion(cMap1Winner);

        KambiOutcome m1w1 = new KambiOutcome();
        m1w1.setId(1201L);
        m1w1.setType("OT_ONE");
        m1w1.setLabel("Natus Vincere");
        m1w1.setOdds(1800);

        KambiOutcome m1w2 = new KambiOutcome();
        m1w2.setId(1202L);
        m1w2.setType("OT_TWO");
        m1w2.setLabel("FaZe Clan");
        m1w2.setOdds(1950);

        boMap1Winner.setOutcomes(List.of(m1w1, m1w2));
        offers.add(boMap1Winner);

        // 3. Map 1 Total Rounds Over/Under 21.5
        KambiBetOffer boRounds = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cRounds = new KambiBetOffer.KambiCriterion();
        cRounds.setLabel("Map 1 Total Rounds");
        cRounds.setEnglishLabel("Map 1 Total Rounds");
        boRounds.setCriterion(cRounds);

        KambiOutcome rOver = new KambiOutcome();
        rOver.setId(1301L);
        rOver.setType("OT_OVER");
        rOver.setLine(21.5);
        rOver.setLabel("Over 21.5");
        rOver.setOdds(1880);

        KambiOutcome rUnder = new KambiOutcome();
        rUnder.setId(1302L);
        rUnder.setType("OT_UNDER");
        rUnder.setLine(21.5);
        rUnder.setLabel("Under 21.5");
        rUnder.setOdds(1880);

        boRounds.setOutcomes(List.of(rOver, rUnder));
        offers.add(boRounds);

        // 4. Map 1 First Blood
        KambiBetOffer boFb = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cFb = new KambiBetOffer.KambiCriterion();
        cFb.setLabel("Map 1 First Blood");
        cFb.setEnglishLabel("Map 1 First Blood");
        boFb.setCriterion(cFb);

        KambiOutcome fb1 = new KambiOutcome();
        fb1.setId(1401L);
        fb1.setType("OT_ONE");
        fb1.setLabel("Natus Vincere");
        fb1.setOdds(1850);

        KambiOutcome fb2 = new KambiOutcome();
        fb2.setId(1402L);
        fb2.setType("OT_TWO");
        fb2.setLabel("FaZe Clan");
        fb2.setOdds(1850);

        boFb.setOutcomes(List.of(fb1, fb2));
        offers.add(boFb);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals(SportType.CS2, req.getSportType());
        assertEquals(8, req.getOdds().size());

        // Validate Total Maps
        OddItem itemMaps = req.getOdds().stream().filter(o -> "1101".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemMaps);
        assertTrue(itemMaps.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) itemMaps.getBetType();
        assertEquals(StatType.MAPS, tb.statType());
        assertEquals(2.5, tb.param());
        assertEquals(BetScope.FULL_MATCH, tb.scope());

        // Validate Map 1 Winner
        OddItem itemM1W1 = req.getOdds().stream().filter(o -> "1201".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemM1W1);
        assertTrue(itemM1W1.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) itemM1W1.getBetType();
        assertEquals(BetScope.MAP_1, mrb.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, mrb.outcome());

        // Validate Map 1 Total Rounds
        OddItem itemRounds = req.getOdds().stream().filter(o -> "1301".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemRounds);
        assertTrue(itemRounds.getBetType() instanceof TotalBet);
        TotalBet rb = (TotalBet) itemRounds.getBetType();
        assertEquals(StatType.ROUNDS, rb.statType());
        assertEquals(BetScope.MAP_1, rb.scope());
        assertEquals(21.5, rb.param());

        // Validate First Blood
        OddItem itemFb = req.getOdds().stream().filter(o -> "1401".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemFb);
        assertTrue(itemFb.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet fbb = (BinaryMarketBet) itemFb.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, fbb.marketType());
        assertEquals(BetScope.MAP_1, fbb.scope());
    }

    @Test
    public void testFinnishLabelsMapping() {
        MatchCache match = createFootballMatch();
        List<KambiBetOffer> offers = new ArrayList<>();

        // 1. Finnish 1X2 "Varsinainen peliaika"
        KambiBetOffer bo1x2 = new KambiBetOffer();
        KambiBetOffer.KambiCriterion c1x2 = new KambiBetOffer.KambiCriterion();
        c1x2.setLabel("Varsinainen peliaika");
        c1x2.setEnglishLabel("Full Time");
        bo1x2.setCriterion(c1x2);

        KambiOutcome o1 = new KambiOutcome();
        o1.setId(2101L);
        o1.setType("OT_ONE");
        o1.setLabel("Real Madrid");
        o1.setOdds(2200);

        KambiOutcome ox = new KambiOutcome();
        ox.setId(2102L);
        ox.setType("OT_DRAW");
        ox.setLabel("Tasapeli");
        ox.setOdds(3400);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(2103L);
        o2.setType("OT_TWO");
        o2.setLabel("Manchester City");
        o2.setOdds(3100);

        bo1x2.setOutcomes(List.of(o1, ox, o2));
        offers.add(bo1x2);

        // 2. Finnish BTTS "Molemmat joukkueet tekevät maalin"
        KambiBetOffer boBtts = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cBtts = new KambiBetOffer.KambiCriterion();
        cBtts.setLabel("Molemmat joukkueet tekevät maalin");
        cBtts.setEnglishLabel("Both Teams to Score");
        boBtts.setCriterion(cBtts);

        KambiOutcome bttsY = new KambiOutcome();
        bttsY.setId(2201L);
        bttsY.setType("OT_YES");
        bttsY.setLabel("Kyllä");
        bttsY.setOdds(1700);

        KambiOutcome bttsN = new KambiOutcome();
        bttsN.setId(2202L);
        bttsN.setType("OT_NO");
        bttsN.setLabel("Ei");
        bttsN.setOdds(2100);

        boBtts.setOutcomes(List.of(bttsY, bttsN));
        offers.add(boBtts);

        // 3. Finnish Total "Yli/Alle 2.5 maalia"
        KambiBetOffer boTotal = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cTotal = new KambiBetOffer.KambiCriterion();
        cTotal.setLabel("Maalit yhteensä");
        cTotal.setEnglishLabel("Total Goals");
        boTotal.setCriterion(cTotal);

        KambiOutcome tOver = new KambiOutcome();
        tOver.setId(2301L);
        tOver.setType("OT_OVER");
        tOver.setLine(2.5);
        tOver.setLabel("Yli 2.5");
        tOver.setOdds(1800);

        KambiOutcome tUnder = new KambiOutcome();
        tUnder.setId(2302L);
        tUnder.setType("OT_UNDER");
        tUnder.setLine(2.5);
        tUnder.setLabel("Alle 2.5");
        tUnder.setOdds(2000);

        boTotal.setOutcomes(List.of(tOver, tUnder));
        offers.add(boTotal);

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(match, offers);
        assertNotNull(req);
        assertEquals(7, req.getOdds().size());

        OddItem item1x2 = req.getOdds().stream().filter(o -> "2101".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1x2);
        assertTrue(item1x2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) item1x2.getBetType()).outcome());

        OddItem itemBtts = req.getOdds().stream().filter(o -> "2201".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBtts);
        assertTrue(itemBtts.getBetType() instanceof BinaryMarketBet);
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) itemBtts.getBetType()).outcome());

        OddItem itemTotal = req.getOdds().stream().filter(o -> "2301".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemTotal);
        assertTrue(itemTotal.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemTotal.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemTotal.getBetType()).param());
    }

    @Test
    public void testEventDetailsResponseMapping() {
        KambiEventDetailsResponse details = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId(998877L);
        event.setName("HJK Helsinki vs KuPS");
        event.setHomeName("HJK Helsinki");
        event.setAwayName("KuPS");
        event.setStart("2026-10-02T19:00:00Z");
        event.setState("NOT_STARTED");

        KambiEvent.KambiPath pathSport = new KambiEvent.KambiPath();
        pathSport.setName("Jalkapallo");
        KambiEvent.KambiPath pathLeague = new KambiEvent.KambiPath();
        pathLeague.setName("Veikkausliiga");
        event.setPath(List.of(pathSport, pathLeague));

        details.setEvents(List.of(event));

        KambiBetOffer bo = new KambiBetOffer();
        KambiBetOffer.KambiCriterion c = new KambiBetOffer.KambiCriterion();
        c.setLabel("Varsinainen peliaika");
        c.setEnglishLabel("Match");
        bo.setCriterion(c);

        KambiOutcome o1 = new KambiOutcome();
        o1.setId(5551L);
        o1.setType("OT_ONE");
        o1.setLabel("HJK Helsinki");
        o1.setOdds(1950);

        KambiOutcome ox = new KambiOutcome();
        ox.setId(5552L);
        ox.setType("OT_DRAW");
        ox.setLabel("Tasapeli");
        ox.setOdds(3400);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(5553L);
        o2.setType("OT_TWO");
        o2.setLabel("KuPS");
        o2.setOdds(3900);

        bo.setOutcomes(List.of(o1, ox, o2));
        details.setBetoffers(List.of(bo));

        OddsUpdateRequest req = oddsMapper.mapToOddsUpdateRequest(details, "Jalkapallo", "Veikkausliiga");
        assertNotNull(req);
        assertEquals("paf", req.getBookmaker());
        assertEquals("998877", req.getExternalEventId());
        assertEquals("HJK Helsinki", req.getTeam1());
        assertEquals("KuPS", req.getTeam2());
        assertEquals("Jalkapallo", req.getSportName());
        assertEquals("Veikkausliiga", req.getLeagueName());
        assertEquals(3, req.getOdds().size());
    }
}
