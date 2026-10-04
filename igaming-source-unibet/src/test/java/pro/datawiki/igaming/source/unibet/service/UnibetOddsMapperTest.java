package pro.datawiki.igaming.source.unibet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.core.engine.kambi.dto.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class UnibetOddsMapperTest {

    private UnibetOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new UnibetOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
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
        assertEquals("unibet", request.getBookmaker());
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
    public void testMatchHandicapNotTreatedAsMoneylineAndNormalized() {
        KambiBetOffer handicapOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion = new KambiBetOffer.KambiCriterion();
        criterion.setLabel("Match Handicap");
        criterion.setEnglishLabel("Match Handicap");
        handicapOffer.setCriterion(criterion);

        List<KambiOutcome> outcomes = new ArrayList<>();
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(401L);
        o1.setType("OT_ONE");
        o1.setLabel("Real Madrid -1.5");
        o1.setLine(-1500.0); // Kambi sends -1500 for -1.5
        o1.setOdds(2050); // 2.05
        outcomes.add(o1);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(402L);
        o2.setType("OT_TWO");
        o2.setLabel("Barcelona 1.5");
        o2.setLine(1500.0); // Kambi sends 1500 for 1.5
        o2.setOdds(1800); // 1.80
        outcomes.add(o2);

        handicapOffer.setOutcomes(outcomes);

        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Real Madrid");
        mockEvent.setAwayName("Barcelona");

        BetType bt1 = oddsMapper.resolveBetType(mockEvent, handicapOffer, o1, SportType.FOOTBALL, "Match Handicap", o1.getLabel());
        BetType bt2 = oddsMapper.resolveBetType(mockEvent, handicapOffer, o2, SportType.FOOTBALL, "Match Handicap", o2.getLabel());

        assertNotNull(bt1);
        assertNotNull(bt2);

        // Crucial: Must be HandicapBet, NOT MatchResultBet (which would cause a 33.8% false super-arb against Moneyline!)
        assertTrue(bt1 instanceof HandicapBet, "Match Handicap must NOT be treated as MatchResultBet / Moneyline");
        assertTrue(bt2 instanceof HandicapBet, "Match Handicap must NOT be treated as MatchResultBet / Moneyline");

        HandicapBet hb1 = (HandicapBet) bt1;
        HandicapBet hb2 = (HandicapBet) bt2;

        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param(), 0.001);

        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param(), 0.001);
    }

    @Test
    public void testTotalsNormalizedAndSubjectResolved() {
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Real Madrid");
        mockEvent.setAwayName("Barcelona");

        // 1. Team total with 2500 line
        KambiBetOffer teamTotalOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit1 = new KambiBetOffer.KambiCriterion();
        crit1.setLabel("Real Madrid - Total Goals");
        crit1.setEnglishLabel("Real Madrid - Total Goals");
        teamTotalOffer.setCriterion(crit1);

        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(501L);
        oOver.setType("OT_OVER");
        oOver.setLabel("Over 2.5");
        oOver.setLine(2500.0);
        oOver.setOdds(1900);

        BetType btOver = oddsMapper.resolveBetType(mockEvent, teamTotalOffer, oOver, SportType.FOOTBALL, "Real Madrid - Total Goals", oOver.getLabel());
        assertNotNull(btOver);
        assertTrue(btOver instanceof TotalBet);
        TotalBet tbOver = (TotalBet) btOver;
        assertEquals(BetSubject.TEAM1, tbOver.subject());
        assertEquals(2.5, tbOver.param(), 0.001);
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(BetScope.FULL_MATCH, tbOver.scope());

        // 2. 1st Half total
        KambiBetOffer halfTotalOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit2 = new KambiBetOffer.KambiCriterion();
        crit2.setLabel("1st Half - Total Goals");
        crit2.setEnglishLabel("1st Half - Total Goals");
        halfTotalOffer.setCriterion(crit2);

        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(502L);
        oUnder.setType("OT_UNDER");
        oUnder.setLabel("Under 1.5");
        oUnder.setLine(1500.0);
        oUnder.setOdds(1650);

        BetType btUnder = oddsMapper.resolveBetType(mockEvent, halfTotalOffer, oUnder, SportType.FOOTBALL, "1st Half - Total Goals", oUnder.getLabel());
        assertNotNull(btUnder);
        assertTrue(btUnder instanceof TotalBet);
        TotalBet tbUnder = (TotalBet) btUnder;
        assertEquals(BetSubject.MATCH, tbUnder.subject());
        assertEquals(1.5, tbUnder.param(), 0.001);
        assertEquals(TotalBet.Direction.UNDER, tbUnder.direction());
        assertEquals(BetScope.HALF_1, tbUnder.scope());
    }

    @Test
    public void testDoubleChanceAndDrawNoBetAndBtts() {
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Real Madrid");
        mockEvent.setAwayName("Barcelona");

        // Double Chance
        KambiBetOffer dcOffer = new KambiBetOffer();
        KambiOutcome o1X = new KambiOutcome();
        o1X.setType("OT_ONE_DRAW");
        o1X.setLabel("Real Madrid or Draw");
        BetType btDc = oddsMapper.resolveBetType(mockEvent, dcOffer, o1X, SportType.FOOTBALL, "Match Result - Double Chance", o1X.getLabel());
        assertNotNull(btDc);
        assertTrue(btDc instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) btDc).outcome());

        // Draw No Bet
        KambiBetOffer dnbOffer = new KambiBetOffer();
        KambiOutcome oDnb = new KambiOutcome();
        oDnb.setType("OT_ONE");
        oDnb.setLabel("Real Madrid");
        BetType btDnb = oddsMapper.resolveBetType(mockEvent, dnbOffer, oDnb, SportType.FOOTBALL, "Draw No Bet", oDnb.getLabel());
        assertNotNull(btDnb);
        assertTrue(btDnb instanceof HandicapBet);
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) btDnb).outcome());
        assertEquals(0.0, ((HandicapBet) btDnb).param(), 0.001);

        // BTTS
        KambiBetOffer bttsOffer = new KambiBetOffer();
        KambiOutcome oBttsYes = new KambiOutcome();
        oBttsYes.setType("OT_YES");
        oBttsYes.setLabel("Yes");
        BetType btBtts = oddsMapper.resolveBetType(mockEvent, bttsOffer, oBttsYes, SportType.FOOTBALL, "Both Teams to Score", oBttsYes.getLabel());
        assertNotNull(btBtts);
        assertTrue(btBtts instanceof BinaryMarketBet);
        assertEquals(BinaryMarketBet.MarketType.BTTS, ((BinaryMarketBet) btBtts).marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) btBtts).outcome());
    }

    @Test
    public void testIceHockey3WayMatchResultNotTreatedAs2WayMoneyline() {
        // Reproduce Timrå vs Skellefteå (SHL ice hockey match with 3-way result where draw is "Oavgjort")
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(889900L);
        mockEvent.setHomeName("Timrå IK");
        mockEvent.setAwayName("Skellefteå AIK");

        KambiBetOffer matchOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit = new KambiBetOffer.KambiCriterion();
        crit.setLabel("Ordinarie tid");
        crit.setEnglishLabel("Regular Time");
        matchOffer.setCriterion(crit);

        List<KambiOutcome> outcomes = new ArrayList<>();
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(101L);
        o1.setType("OT_ONE");
        o1.setLabel("Timrå IK");
        o1.setOdds(4250); // 4.25
        outcomes.add(o1);

        KambiOutcome oX = new KambiOutcome();
        oX.setId(102L);
        oX.setType("OT_DRAW");
        oX.setLabel("Oavgjort"); // Swedish for Draw
        oX.setOdds(4100); // 4.10
        outcomes.add(oX);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(103L);
        o2.setType("OT_TWO");
        o2.setLabel("Skellefteå AIK");
        o2.setOdds(1700); // 1.70
        outcomes.add(o2);

        matchOffer.setOutcomes(outcomes);

        BetType bt1 = oddsMapper.resolveBetType(mockEvent, matchOffer, o1, SportType.ICE_HOCKEY, "Regular Time", o1.getLabel());
        BetType btX = oddsMapper.resolveBetType(mockEvent, matchOffer, oX, SportType.ICE_HOCKEY, "Regular Time", oX.getLabel());
        BetType bt2 = oddsMapper.resolveBetType(mockEvent, matchOffer, o2, SportType.ICE_HOCKEY, "Regular Time", o2.getLabel());

        assertNotNull(bt1);
        assertNotNull(btX);
        assertNotNull(bt2);

        // Crucial: Must be WIN1 / WIN2 / DRAW, NOT WIN1_2WAY / WIN2_2WAY (which would cause a 17.2% false super-arb!)
        assertTrue(bt1 instanceof MatchResultBet);
        assertTrue(btX instanceof MatchResultBet);
        assertTrue(bt2 instanceof MatchResultBet);

        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) bt1).outcome(), "Must be 3-way WIN1, not WIN1_2WAY");
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) btX).outcome(), "Oavgjort must be resolved as DRAW");
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) bt2).outcome(), "Must be 3-way WIN2, not WIN2_2WAY");
    }

    @Test
    public void testDoubleChanceCrossTypesAndLabels() {
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Philadelphia Flyers");
        mockEvent.setAwayName("Carolina Hurricanes");

        KambiBetOffer dcOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit = new KambiBetOffer.KambiCriterion();
        crit.setLabel("Dubbelchans");
        crit.setEnglishLabel("Double Chance");
        dcOffer.setCriterion(crit);

        // OT_ONE_CROSS (1X)
        KambiOutcome o1X = new KambiOutcome();
        o1X.setType("OT_ONE_CROSS");
        o1X.setLabel("Philadelphia Flyers eller oavgjort");
        o1X.setOdds(2850); // 2.85

        // OT_CROSS_TWO (X2)
        KambiOutcome oX2 = new KambiOutcome();
        oX2.setType("OT_CROSS_TWO");
        oX2.setLabel("Carolina Hurricanes eller oavgjort");
        oX2.setOdds(2180); // 2.18

        BetType bt1X = oddsMapper.resolveBetType(mockEvent, dcOffer, o1X, SportType.ICE_HOCKEY, "Dubbelchans", o1X.getLabel());
        BetType btX2 = oddsMapper.resolveBetType(mockEvent, dcOffer, oX2, SportType.ICE_HOCKEY, "Dubbelchans", oX2.getLabel());

        assertNotNull(bt1X);
        assertNotNull(btX2);

        assertTrue(bt1X instanceof MatchResultBet);
        assertTrue(btX2 instanceof MatchResultBet);

        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) bt1X).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) btX2).outcome());
    }

    @Test
    public void testStatsCornersAndCardsResolvedCorrectly() {
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Arsenal");
        mockEvent.setAwayName("Chelsea");

        // Corner Total
        KambiBetOffer cornerTotalOffer = new KambiBetOffer();
        KambiOutcome oCorner = new KambiOutcome();
        oCorner.setType("OT_OVER");
        oCorner.setLabel("Over 9.5");
        oCorner.setLine(9500.0);
        oCorner.setOdds(1850);

        BetType btCorner = oddsMapper.resolveBetType(mockEvent, cornerTotalOffer, oCorner, SportType.FOOTBALL, "Total Corners", oCorner.getLabel());
        assertNotNull(btCorner);
        assertTrue(btCorner instanceof TotalBet);
        TotalBet tbCorner = (TotalBet) btCorner;
        assertEquals(StatType.CORNERS, tbCorner.statType(), "Must be StatType.CORNERS, not MATCH/null");
        assertEquals(9.5, tbCorner.param(), 0.001);

        // Yellow Cards Total
        KambiBetOffer cardTotalOffer = new KambiBetOffer();
        KambiOutcome oCard = new KambiOutcome();
        oCard.setType("OT_UNDER");
        oCard.setLabel("Under 4.5");
        oCard.setLine(4500.0);
        oCard.setOdds(1750);

        BetType btCard = oddsMapper.resolveBetType(mockEvent, cardTotalOffer, oCard, SportType.FOOTBALL, "Total Yellow Cards", oCard.getLabel());
        assertNotNull(btCard);
        assertTrue(btCard instanceof TotalBet);
        TotalBet tbCard = (TotalBet) btCard;
        assertEquals(StatType.YELLOW_CARDS, tbCard.statType(), "Must be StatType.YELLOW_CARDS");
        assertEquals(4.5, tbCard.param(), 0.001);
    }

    @Test
    public void testEuropean3WayHandicapDrawResolved() {
        KambiEvent mockEvent = new KambiEvent();
        mockEvent.setId(12345L);
        mockEvent.setHomeName("Liverpool");
        mockEvent.setAwayName("Everton");

        KambiBetOffer hdpOffer = new KambiBetOffer();
        List<KambiOutcome> outcomes = new ArrayList<>();

        KambiOutcome o1 = new KambiOutcome();
        o1.setType("OT_ONE");
        o1.setLabel("Liverpool (-1)");
        o1.setLine(-1000.0);
        outcomes.add(o1);

        KambiOutcome oX = new KambiOutcome();
        oX.setType("OT_DRAW");
        oX.setLabel("Handicap Tie (-1)");
        oX.setLine(-1000.0);
        outcomes.add(oX);

        KambiOutcome o2 = new KambiOutcome();
        o2.setType("OT_TWO");
        o2.setLabel("Everton (+1)");
        o2.setLine(1000.0);
        outcomes.add(o2);

        hdpOffer.setOutcomes(outcomes);

        BetType btX = oddsMapper.resolveBetType(mockEvent, hdpOffer, oX, SportType.FOOTBALL, "3-Way Handicap", oX.getLabel());
        assertNotNull(btX);
        assertTrue(btX instanceof HandicapBet);
        HandicapBet hbX = (HandicapBet) btX;
        assertEquals(HandicapBet.Outcome.DRAW, hbX.outcome());
        assertEquals(-1.0, hbX.param(), 0.001);
    }
}
