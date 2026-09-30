package pro.datawiki.igaming.source.sport888.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.sport888.dto.kambi.*;
import pro.datawiki.igaming.source.sport888.service.handler.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class Sport888OddsMapperTest {

    private Sport888OddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenAnswer(inv -> {
            String sport = inv.getArgument(0, String.class);
            if ("Esports".equalsIgnoreCase(sport) || "CS2".equalsIgnoreCase(sport)) {
                return SportType.CS2;
            }
            return SportType.FOOTBALL;
        });

        List<Sport888MarketHandler> handlers = List.of(
                new Sport888StatsHandler(),
                new Sport888EsportsHandler(),
                new Sport888DoubleChanceHandler(),
                new Sport888BttsHandler(),
                new Sport888DrawNoBetHandler(),
                new Sport888CorrectScoreHandler(),
                new Sport888TotalHandler(),
                new Sport888HandicapHandler(),
                new Sport888MatchResultHandler()
        );

        oddsMapper = new Sport888OddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver, handlers);
    }

    @Test
    public void testMapToOddsUpdateRequest() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        List<KambiEvent> events = new ArrayList<>();
        KambiEvent event = new KambiEvent();
        event.setId(99223344L);
        event.setName("Real Madrid - Barcelona");
        event.setHomeName("Real Madrid");
        event.setAwayName("Barcelona");
        event.setStart("2026-06-02T18:00:00Z");
        event.setState("NOT_STARTED");

        List<KambiEvent.KambiPath> path = new ArrayList<>();
        KambiEvent.KambiPath p1 = new KambiEvent.KambiPath();
        p1.setName("Football");
        path.add(p1);
        event.setPath(path);
        events.add(event);
        response.setEvents(events);

        List<KambiBetOffer> betoffers = new ArrayList<>();

        // 1. Match Winner (1X2)
        KambiBetOffer matchResultOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion1 = new KambiBetOffer.KambiCriterion();
        criterion1.setLabel("Match Result");
        criterion1.setEnglishLabel("Match Result");
        matchResultOffer.setCriterion(criterion1);

        List<KambiOutcome> outcomes1 = new ArrayList<>();

        KambiOutcome o1 = new KambiOutcome();
        o1.setId(101L);
        o1.setOdds(1950); // 1.95
        o1.setType("OT_ONE");
        o1.setLabel("Real Madrid");
        outcomes1.add(o1);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(102L);
        o2.setOdds(3400); // 3.40
        o2.setType("OT_DRAW");
        o2.setLabel("Draw");
        outcomes1.add(o2);

        KambiOutcome o3 = new KambiOutcome();
        o3.setId(103L);
        o3.setOdds(3600); // 3.60
        o3.setType("OT_TWO");
        o3.setLabel("Barcelona");
        outcomes1.add(o3);

        matchResultOffer.setOutcomes(outcomes1);
        betoffers.add(matchResultOffer);

        // 2. Total Goals
        KambiBetOffer totalOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion2 = new KambiBetOffer.KambiCriterion();
        criterion2.setLabel("Total Goals");
        criterion2.setEnglishLabel("Total Goals");
        totalOffer.setCriterion(criterion2);

        List<KambiOutcome> outcomes2 = new ArrayList<>();

        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(201L);
        oOver.setOdds(1900); // 1.90
        oOver.setType("OT_OVER");
        oOver.setLine(2.5);
        oOver.setLabel("Over 2.5");
        outcomes2.add(oOver);

        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(202L);
        oUnder.setOdds(1920); // 1.92
        oUnder.setType("OT_UNDER");
        oUnder.setLine(2.5);
        oUnder.setLabel("Under 2.5");
        outcomes2.add(oUnder);

        totalOffer.setOutcomes(outcomes2);
        betoffers.add(totalOffer);

        // 3. Handicap
        KambiBetOffer handicapOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion3 = new KambiBetOffer.KambiCriterion();
        criterion3.setLabel("Handicap");
        criterion3.setEnglishLabel("Handicap");
        handicapOffer.setCriterion(criterion3);

        List<KambiOutcome> outcomes3 = new ArrayList<>();

        KambiOutcome oH1 = new KambiOutcome();
        oH1.setId(301L);
        oH1.setOdds(2050); // 2.05
        oH1.setType("OT_ONE");
        oH1.setLine(-0.5);
        oH1.setLabel("Real Madrid -0.5");
        outcomes3.add(oH1);

        KambiOutcome oH2 = new KambiOutcome();
        oH2.setId(302L);
        oH2.setOdds(1800); // 1.80
        oH2.setType("OT_TWO");
        oH2.setLine(0.5);
        oH2.setLabel("Barcelona 0.5");
        outcomes3.add(oH2);

        handicapOffer.setOutcomes(outcomes3);
        betoffers.add(handicapOffer);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "La Liga");

        assertNotNull(request);
        assertEquals("888sport", request.getBookmaker());
        assertEquals("99223344", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("La Liga", request.getLeagueName());
        assertEquals("Real Madrid", request.getTeam1());
        assertEquals("Barcelona", request.getTeam2());
        assertFalse(request.getIsLive());
        assertEquals(1780423200000L, request.getStartTime()); // 2026-06-02T18:00:00Z in epoch millis

        List<OddItem> odds = request.getOdds();
        assertNotNull(odds);
        assertEquals(7, odds.size());

        // Assert WIN1
        OddItem w1 = odds.stream().filter(o -> "101".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(w1);
        assertEquals(1.95, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        // Assert Over 2.5
        OddItem over = odds.stream().filter(o -> "201".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(over);
        assertEquals(1.90, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) over.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) over.getBetType()).param());

        // Assert Handicap 1
        OddItem h1 = odds.stream().filter(o -> "301".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(h1);
        assertEquals(2.05, h1.getValue());
        assertTrue(h1.getBetType() instanceof HandicapBet);
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) h1.getBetType()).outcome());
        assertEquals(-0.5, ((HandicapBet) h1.getBetType()).param());
    }

    @Test
    public void testDoubleChanceAndDnbAndBttsAndCorrectScore() {
        KambiEventDetailsResponse response = createBaseFootballResponse();
        List<KambiBetOffer> betoffers = new ArrayList<>();

        // Double Chance
        KambiBetOffer dcOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cDc = new KambiBetOffer.KambiCriterion();
        cDc.setLabel("Double Chance");
        dcOffer.setCriterion(cDc);
        List<KambiOutcome> dcOutcomes = new ArrayList<>();

        KambiOutcome o1x = new KambiOutcome();
        o1x.setId(401L);
        o1x.setOdds(1300);
        o1x.setType("OT_ONE_X");
        dcOutcomes.add(o1x);

        KambiOutcome o12 = new KambiOutcome();
        o12.setId(402L);
        o12.setOdds(1350);
        o12.setType("OT_ONE_TWO");
        dcOutcomes.add(o12);

        KambiOutcome ox2 = new KambiOutcome();
        ox2.setId(403L);
        ox2.setOdds(1700);
        ox2.setType("OT_X_TWO");
        dcOutcomes.add(ox2);

        dcOffer.setOutcomes(dcOutcomes);
        betoffers.add(dcOffer);

        // Draw No Bet
        KambiBetOffer dnbOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cDnb = new KambiBetOffer.KambiCriterion();
        cDnb.setLabel("Draw No Bet");
        dnbOffer.setCriterion(cDnb);
        List<KambiOutcome> dnbOutcomes = new ArrayList<>();

        KambiOutcome dnb1 = new KambiOutcome();
        dnb1.setId(501L);
        dnb1.setOdds(1450);
        dnb1.setType("OT_ONE");
        dnbOutcomes.add(dnb1);

        KambiOutcome dnb2 = new KambiOutcome();
        dnb2.setId(502L);
        dnb2.setOdds(2600);
        dnb2.setType("OT_TWO");
        dnbOutcomes.add(dnb2);

        dnbOffer.setOutcomes(dnbOutcomes);
        betoffers.add(dnbOffer);

        // Both Teams to Score
        KambiBetOffer bttsOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cBtts = new KambiBetOffer.KambiCriterion();
        cBtts.setLabel("Both Teams To Score");
        bttsOffer.setCriterion(cBtts);
        List<KambiOutcome> bttsOutcomes = new ArrayList<>();

        KambiOutcome bttsYes = new KambiOutcome();
        bttsYes.setId(601L);
        bttsYes.setOdds(1800);
        bttsYes.setType("OT_YES");
        bttsOutcomes.add(bttsYes);

        KambiOutcome bttsNo = new KambiOutcome();
        bttsNo.setId(602L);
        bttsNo.setOdds(2000);
        bttsNo.setType("OT_NO");
        bttsOutcomes.add(bttsNo);

        bttsOffer.setOutcomes(bttsOutcomes);
        betoffers.add(bttsOffer);

        // Correct Score
        KambiBetOffer csOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCs = new KambiBetOffer.KambiCriterion();
        cCs.setLabel("Correct Score");
        csOffer.setCriterion(cCs);
        List<KambiOutcome> csOutcomes = new ArrayList<>();

        KambiOutcome cs21 = new KambiOutcome();
        cs21.setId(701L);
        cs21.setOdds(8500);
        cs21.setLabel("2-1");
        csOutcomes.add(cs21);

        KambiOutcome csOther = new KambiOutcome();
        csOther.setId(702L);
        csOther.setOdds(12000);
        csOther.setLabel("Any Other Score");
        csOutcomes.add(csOther);

        csOffer.setOutcomes(csOutcomes);
        betoffers.add(csOffer);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // Check Double Chance
        OddItem item1x = odds.stream().filter(o -> "401".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item1x);
        assertTrue(item1x.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) item1x.getBetType()).outcome());

        OddItem item12 = odds.stream().filter(o -> "402".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(item12);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) item12.getBetType()).outcome());

        OddItem itemx2 = odds.stream().filter(o -> "403".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemx2);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) itemx2.getBetType()).outcome());

        // Check Draw No Bet
        OddItem itemDnb1 = odds.stream().filter(o -> "501".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemDnb1);
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) itemDnb1.getBetType()).outcome());

        // Check BTTS
        OddItem itemBttsYes = odds.stream().filter(o -> "601".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsYes);
        assertTrue(itemBttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bttsBet = (BinaryMarketBet) itemBttsYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bttsBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bttsBet.outcome());

        // Check Correct Score
        OddItem itemCs21 = odds.stream().filter(o -> "701".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCs21);
        assertTrue(itemCs21.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet scoreBet = (CorrectScoreBet) itemCs21.getBetType();
        assertEquals(2, scoreBet.score1());
        assertEquals(1, scoreBet.score2());
        assertFalse(scoreBet.isAnyOtherScore());

        OddItem itemCsOther = odds.stream().filter(o -> "702".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemCsOther);
        assertTrue(((CorrectScoreBet) itemCsOther.getBetType()).isAnyOtherScore());
    }

    @Test
    public void testCornersAndCardsStatistics() {
        KambiEventDetailsResponse response = createBaseFootballResponse();
        List<KambiBetOffer> betoffers = new ArrayList<>();

        // Corners Total
        KambiBetOffer cornersOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCorners = new KambiBetOffer.KambiCriterion();
        cCorners.setLabel("Total Corners");
        cornersOffer.setCriterion(cCorners);
        List<KambiOutcome> cornersOutcomes = new ArrayList<>();

        KambiOutcome cOver = new KambiOutcome();
        cOver.setId(801L);
        cOver.setOdds(1850);
        cOver.setType("OT_OVER");
        cOver.setLine(9.5);
        cornersOutcomes.add(cOver);

        KambiOutcome cUnder = new KambiOutcome();
        cUnder.setId(802L);
        cUnder.setOdds(1950);
        cUnder.setType("OT_UNDER");
        cUnder.setLine(9.5);
        cornersOutcomes.add(cUnder);

        cornersOffer.setOutcomes(cornersOutcomes);
        betoffers.add(cornersOffer);

        // Yellow Cards Total
        KambiBetOffer cardsOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCards = new KambiBetOffer.KambiCriterion();
        cCards.setLabel("Total Yellow Cards");
        cardsOffer.setCriterion(cCards);
        List<KambiOutcome> cardsOutcomes = new ArrayList<>();

        KambiOutcome yOver = new KambiOutcome();
        yOver.setId(803L);
        yOver.setOdds(1750);
        yOver.setType("OT_OVER");
        yOver.setLine(3.5);
        cardsOutcomes.add(yOver);

        cardsOffer.setOutcomes(cardsOutcomes);
        betoffers.add(cardsOffer);

        // Corners 1X2
        KambiBetOffer corners1x2Offer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cCorners1x2 = new KambiBetOffer.KambiCriterion();
        cCorners1x2.setLabel("Most Corners");
        corners1x2Offer.setCriterion(cCorners1x2);
        List<KambiOutcome> corners1x2Outcomes = new ArrayList<>();

        KambiOutcome c1 = new KambiOutcome();
        c1.setId(804L);
        c1.setOdds(1600);
        c1.setType("OT_ONE");
        corners1x2Outcomes.add(c1);

        KambiOutcome cDraw = new KambiOutcome();
        cDraw.setId(805L);
        cDraw.setOdds(7000);
        cDraw.setType("OT_DRAW");
        corners1x2Outcomes.add(cDraw);

        corners1x2Offer.setOutcomes(corners1x2Outcomes);
        betoffers.add(corners1x2Offer);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();

        // Check Corners Total
        OddItem cornerOverItem = odds.stream().filter(o -> "801".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(cornerOverItem);
        assertTrue(cornerOverItem.getBetType() instanceof TotalBet);
        TotalBet cTotalBet = (TotalBet) cornerOverItem.getBetType();
        assertEquals(StatType.CORNERS, cTotalBet.statType());
        assertEquals(9.5, cTotalBet.param());
        assertEquals(TotalBet.Direction.OVER, cTotalBet.direction());

        // Check Yellow Cards Total
        OddItem cardOverItem = odds.stream().filter(o -> "803".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(cardOverItem);
        assertTrue(cardOverItem.getBetType() instanceof TotalBet);
        TotalBet cardTotalBet = (TotalBet) cardOverItem.getBetType();
        assertEquals(StatType.YELLOW_CARDS, cardTotalBet.statType());
        assertEquals(3.5, cardTotalBet.param());

        // Check Corners 1X2
        OddItem corner1Item = odds.stream().filter(o -> "804".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(corner1Item);
        assertTrue(corner1Item.getBetType() instanceof MatchResultBet);
        MatchResultBet cornerResult = (MatchResultBet) corner1Item.getBetType();
        assertEquals(StatType.CORNERS, cornerResult.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, cornerResult.outcome());
    }

    @Test
    public void testEsportsMarkets() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        List<KambiEvent> events = new ArrayList<>();
        KambiEvent event = new KambiEvent();
        event.setId(88776655L);
        event.setName("NaVi - FaZe");
        event.setHomeName("NaVi");
        event.setAwayName("FaZe");
        event.setState("NOT_STARTED");

        List<KambiEvent.KambiPath> path = new ArrayList<>();
        KambiEvent.KambiPath p1 = new KambiEvent.KambiPath();
        p1.setName("CS2");
        path.add(p1);
        event.setPath(path);
        events.add(event);
        response.setEvents(events);

        List<KambiBetOffer> betoffers = new ArrayList<>();

        // 1. Total Maps
        KambiBetOffer mapsOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cMaps = new KambiBetOffer.KambiCriterion();
        cMaps.setLabel("Total Maps");
        mapsOffer.setCriterion(cMaps);
        List<KambiOutcome> mapsOutcomes = new ArrayList<>();

        KambiOutcome mOver = new KambiOutcome();
        mOver.setId(901L);
        mOver.setOdds(1900);
        mOver.setType("OT_OVER");
        mOver.setLine(2.5);
        mapsOutcomes.add(mOver);

        mapsOffer.setOutcomes(mapsOutcomes);
        betoffers.add(mapsOffer);

        // 2. Map 1 Winner
        KambiBetOffer map1Offer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cMap1 = new KambiBetOffer.KambiCriterion();
        cMap1.setLabel("Map 1 Winner");
        map1Offer.setCriterion(cMap1);
        List<KambiOutcome> map1Outcomes = new ArrayList<>();

        KambiOutcome map1Home = new KambiOutcome();
        map1Home.setId(902L);
        map1Home.setOdds(1700);
        map1Home.setType("OT_ONE");
        map1Home.setLabel("NaVi");
        map1Outcomes.add(map1Home);

        map1Offer.setOutcomes(map1Outcomes);
        betoffers.add(map1Offer);

        // 3. Map 1 Total Rounds
        KambiBetOffer roundsOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cRounds = new KambiBetOffer.KambiCriterion();
        cRounds.setLabel("Map 1 Total Rounds");
        roundsOffer.setCriterion(cRounds);
        List<KambiOutcome> roundsOutcomes = new ArrayList<>();

        KambiOutcome rOver = new KambiOutcome();
        rOver.setId(903L);
        rOver.setOdds(1850);
        rOver.setType("OT_OVER");
        rOver.setLine(20.5);
        roundsOutcomes.add(rOver);

        roundsOffer.setOutcomes(roundsOutcomes);
        betoffers.add(roundsOffer);

        // 4. First Blood
        KambiBetOffer fbOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cFb = new KambiBetOffer.KambiCriterion();
        cFb.setLabel("Map 1 First Blood");
        fbOffer.setCriterion(cFb);
        List<KambiOutcome> fbOutcomes = new ArrayList<>();

        KambiOutcome fbHome = new KambiOutcome();
        fbHome.setId(904L);
        fbHome.setOdds(1900);
        fbHome.setType("OT_ONE");
        fbOutcomes.add(fbHome);

        fbOffer.setOutcomes(fbOutcomes);
        betoffers.add(fbOffer);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "CS2", "ESL Pro League");
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        List<OddItem> odds = request.getOdds();

        // Check Total Maps
        OddItem totalMapsItem = odds.stream().filter(o -> "901".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(totalMapsItem);
        assertTrue(totalMapsItem.getBetType() instanceof TotalBet);
        TotalBet mapsBet = (TotalBet) totalMapsItem.getBetType();
        assertEquals(StatType.MAPS, mapsBet.statType());
        assertEquals(2.5, mapsBet.param());

        // Check Map 1 Winner
        OddItem map1WinnerItem = odds.stream().filter(o -> "902".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(map1WinnerItem);
        assertTrue(map1WinnerItem.getBetType() instanceof MatchResultBet);
        MatchResultBet map1WinnerBet = (MatchResultBet) map1WinnerItem.getBetType();
        assertEquals(BetScope.MAP_1, map1WinnerBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, map1WinnerBet.outcome());

        // Check Map 1 Total Rounds
        OddItem roundsItem = odds.stream().filter(o -> "903".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(roundsItem);
        assertTrue(roundsItem.getBetType() instanceof TotalBet);
        TotalBet roundsBet = (TotalBet) roundsItem.getBetType();
        assertEquals(BetScope.MAP_1, roundsBet.scope());
        assertEquals(StatType.ROUNDS, roundsBet.statType());
        assertEquals(20.5, roundsBet.param());

        // Check Map 1 First Blood
        OddItem fbItem = odds.stream().filter(o -> "904".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(fbItem);
        assertTrue(fbItem.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet fbBet = (BinaryMarketBet) fbItem.getBetType();
        assertEquals(BetScope.MAP_1, fbBet.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, fbBet.marketType());
    }

    private KambiEventDetailsResponse createBaseFootballResponse() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        List<KambiEvent> events = new ArrayList<>();
        KambiEvent event = new KambiEvent();
        event.setId(11223344L);
        event.setName("Arsenal - Chelsea");
        event.setHomeName("Arsenal");
        event.setAwayName("Chelsea");
        event.setStart("2026-06-02T18:00:00Z");
        event.setState("NOT_STARTED");

        List<KambiEvent.KambiPath> path = new ArrayList<>();
        KambiEvent.KambiPath p1 = new KambiEvent.KambiPath();
        p1.setName("Football");
        path.add(p1);
        event.setPath(path);
        events.add(event);
        response.setEvents(events);
        return response;
    }
}
