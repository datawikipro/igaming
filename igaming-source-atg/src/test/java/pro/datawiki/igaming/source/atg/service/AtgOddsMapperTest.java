package pro.datawiki.igaming.source.atg.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.atg.service.handler.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.core.engine.kambi.dto.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class AtgOddsMapperTest {

    private AtgOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new AtgOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    public void testMapToOddsUpdateRequest() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        List<KambiEvent> events = new ArrayList<>();
        KambiEvent event = new KambiEvent();
        event.setId(12345678L);
        event.setName("Home vs Away");
        event.setHomeName("Home Team");
        event.setAwayName("Away Team");
        event.setStart("2026-09-14T20:00:00Z");
        event.setState("NOT_STARTED");
        
        List<KambiEvent.KambiPath> path = new ArrayList<>();
        KambiEvent.KambiPath p1 = new KambiEvent.KambiPath();
        p1.setName("Football");
        path.add(p1);
        event.setPath(path);
        events.add(event);
        response.setEvents(events);

        List<KambiBetOffer> betoffers = new ArrayList<>();
        KambiBetOffer matchResultOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion1 = new KambiBetOffer.KambiCriterion();
        criterion1.setLabel("Full Time Result");
        criterion1.setEnglishLabel("Full Time Result");
        matchResultOffer.setCriterion(criterion1);

        List<KambiOutcome> outcomes1 = new ArrayList<>();
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(301L);
        o1.setLabel("Home Team");
        o1.setType("OT_ONE");
        o1.setOdds(1900);
        outcomes1.add(o1);

        matchResultOffer.setOutcomes(outcomes1);
        betoffers.add(matchResultOffer);
        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Test League");

        assertNotNull(request);
        assertEquals("atg", request.getBookmaker());
        assertEquals("12345678", request.getExternalEventId());
        assertEquals(1, request.getOdds().size());
        assertEquals(1.90, request.getOdds().get(0).getValue(), 0.001);
    }

    @Test
    public void testDoubleChanceHandler_KambiTypesAndSwedishHalf() {
        AtgDoubleChanceHandler handler = new AtgDoubleChanceHandler();
        KambiEvent event = new KambiEvent();
        event.setId(1001L);
        event.setHomeName("Arsenal");
        event.setAwayName("Chelsea");

        // 1. Full Match English Double Chance with Kambi types
        KambiBetOffer offer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion = new KambiBetOffer.KambiCriterion();
        criterion.setLabel("Double Chance");
        criterion.setEnglishLabel("Double Chance");
        offer.setCriterion(criterion);

        List<KambiOutcome> outcomes = new ArrayList<>();
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(11L);
        o1.setType("OT_ONE_CROSS");
        o1.setOdds(1350);
        o1.setLabel("1X");
        outcomes.add(o1);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(12L);
        o2.setType("OT_ONE_TWO");
        o2.setOdds(1250);
        o2.setLabel("12");
        outcomes.add(o2);

        KambiOutcome o3 = new KambiOutcome();
        o3.setId(13L);
        o3.setType("OT_CROSS_TWO");
        o3.setOdds(1550);
        o3.setLabel("X2");
        outcomes.add(o3);
        offer.setOutcomes(outcomes);

        assertTrue(handler.supports(offer, "Double Chance", SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handleOffer(event, offer, "Double Chance", SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        for (OddItem item : items) {
            assertTrue(item.getBetType() instanceof MatchResultBet);
            MatchResultBet mrb = (MatchResultBet) item.getBetType();
            assertEquals(BetScope.FULL_MATCH, mrb.scope());
            assertEquals(StatType.MATCH, mrb.statType());
        }
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(1.35, items.get(0).getValue(), 0.001);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(1.25, items.get(1).getValue(), 0.001);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(1.55, items.get(2).getValue(), 0.001);

        // 2. Swedish 1st Half Dubbelchans with team names
        KambiBetOffer seOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion seCrit = new KambiBetOffer.KambiCriterion();
        seCrit.setLabel("Dubbelchans - 1:a halvlek");
        seOffer.setCriterion(seCrit);

        List<KambiOutcome> seOutcomes = new ArrayList<>();
        KambiOutcome so1 = new KambiOutcome();
        so1.setId(21L);
        so1.setOdds(1400);
        so1.setLabel("Arsenal eller Oavgjort");
        seOutcomes.add(so1);

        KambiOutcome so2 = new KambiOutcome();
        so2.setId(22L);
        so2.setOdds(1600);
        so2.setLabel("Arsenal eller Chelsea");
        seOutcomes.add(so2);

        KambiOutcome so3 = new KambiOutcome();
        so3.setId(23L);
        so3.setOdds(1500);
        so3.setLabel("Oavgjort eller Chelsea");
        seOutcomes.add(so3);
        seOffer.setOutcomes(seOutcomes);

        assertTrue(handler.supports(seOffer, "Dubbelchans - 1:a halvlek", SportType.FOOTBALL));

        List<OddItem> seItems = new ArrayList<>();
        handler.handleOffer(event, seOffer, "Dubbelchans - 1:a halvlek", SportType.FOOTBALL, seItems);

        assertEquals(3, seItems.size());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) seItems.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) seItems.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) seItems.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) seItems.get(2).getBetType()).outcome());

        // Negative check: Corners Double Chance should not be supported
        assertFalse(handler.supports(offer, "Corners - Double Chance", SportType.FOOTBALL));
        assertFalse(handler.supports(offer, "Hörnor - Dubbelchans", SportType.FOOTBALL));
    }

    @Test
    public void testBttsHandler_EnglishAndSwedish() {
        AtgBttsHandler handler = new AtgBttsHandler();
        KambiEvent event = new KambiEvent();
        event.setId(2001L);

        // 1. English Full Match Both Teams To Score
        KambiBetOffer offer = new KambiBetOffer();
        List<KambiOutcome> outcomes = new ArrayList<>();
        KambiOutcome oYes = new KambiOutcome();
        oYes.setId(31L);
        oYes.setType("OT_YES");
        oYes.setLabel("Yes");
        oYes.setOdds(1800);
        outcomes.add(oYes);

        KambiOutcome oNo = new KambiOutcome();
        oNo.setId(32L);
        oNo.setType("OT_NO");
        oNo.setLabel("No");
        oNo.setOdds(2050);
        outcomes.add(oNo);
        offer.setOutcomes(outcomes);

        assertTrue(handler.supports(offer, "Both Teams To Score", SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handleOffer(event, offer, "Both Teams To Score", SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        BinaryMarketBet b1 = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(BetSubject.MATCH, b1.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, b1.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, b1.outcome());
        assertEquals(1.80, items.get(0).getValue(), 0.001);

        BinaryMarketBet b2 = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, b2.outcome());
        assertEquals(2.05, items.get(1).getValue(), 0.001);

        // 2. Swedish Half 1: Båda lagen gör mål - 1:a halvlek
        KambiBetOffer seOffer = new KambiBetOffer();
        List<KambiOutcome> seOutcomes = new ArrayList<>();
        KambiOutcome soYes = new KambiOutcome();
        soYes.setId(41L);
        soYes.setLabel("Ja");
        soYes.setOdds(3500);
        seOutcomes.add(soYes);

        KambiOutcome soNo = new KambiOutcome();
        soNo.setId(42L);
        soNo.setLabel("Nej");
        soNo.setOdds(1280);
        seOutcomes.add(soNo);
        seOffer.setOutcomes(seOutcomes);

        assertTrue(handler.supports(seOffer, "Båda lagen gör mål - 1:a halvlek", SportType.FOOTBALL));

        List<OddItem> seItems = new ArrayList<>();
        handler.handleOffer(event, seOffer, "Båda lagen gör mål - 1:a halvlek", SportType.FOOTBALL, seItems);

        assertEquals(2, seItems.size());
        assertEquals(BetScope.HALF_1, ((BinaryMarketBet) seItems.get(0).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) seItems.get(0).getBetType()).outcome());
        assertEquals(BetScope.HALF_1, ((BinaryMarketBet) seItems.get(1).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) seItems.get(1).getBetType()).outcome());

        // Negative check
        assertFalse(handler.supports(offer, "Cards - Both Teams To Score", SportType.FOOTBALL));
    }

    @Test
    public void testDrawNoBetHandler_EnglishAndSwedish() {
        AtgDrawNoBetHandler handler = new AtgDrawNoBetHandler();
        KambiEvent event = new KambiEvent();
        event.setId(3001L);
        event.setHomeName("Liverpool");
        event.setAwayName("Manchester City");

        // 1. English Full Match Draw No Bet
        KambiBetOffer offer = new KambiBetOffer();
        List<KambiOutcome> outcomes = new ArrayList<>();
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(51L);
        o1.setType("OT_ONE");
        o1.setLabel("Liverpool");
        o1.setOdds(1950);
        outcomes.add(o1);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(52L);
        o2.setType("OT_TWO");
        o2.setLabel("Manchester City");
        o2.setOdds(1850);
        outcomes.add(o2);
        offer.setOutcomes(outcomes);

        assertTrue(handler.supports(offer, "Draw No Bet", SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handleOffer(event, offer, "Draw No Bet", SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(0.0, h1.param(), 0.001);
        assertEquals(StatType.MATCH, h1.statType());
        assertEquals(1.95, items.get(0).getValue(), 0.001);

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(0.0, h2.param(), 0.001);
        assertEquals(1.85, items.get(1).getValue(), 0.001);

        // 2. Swedish 1st Half: Oavgjort inget spel - 1:a halvlek
        KambiBetOffer seOffer = new KambiBetOffer();
        List<KambiOutcome> seOutcomes = new ArrayList<>();
        KambiOutcome so1 = new KambiOutcome();
        so1.setId(61L);
        so1.setLabel("Liverpool");
        so1.setOdds(1650);
        seOutcomes.add(so1);

        KambiOutcome so2 = new KambiOutcome();
        so2.setId(62L);
        so2.setLabel("Manchester City");
        so2.setOdds(2200);
        seOutcomes.add(so2);
        seOffer.setOutcomes(seOutcomes);

        assertTrue(handler.supports(seOffer, "Oavgjort inget spel - 1:a halvlek", SportType.FOOTBALL));

        List<OddItem> seItems = new ArrayList<>();
        handler.handleOffer(event, seOffer, "Oavgjort inget spel - 1:a halvlek", SportType.FOOTBALL, seItems);

        assertEquals(2, seItems.size());
        assertEquals(BetScope.HALF_1, ((HandicapBet) seItems.get(0).getBetType()).scope());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) seItems.get(0).getBetType()).outcome());
        assertEquals(0.0, ((HandicapBet) seItems.get(0).getBetType()).param(), 0.001);
        assertEquals(BetScope.HALF_1, ((HandicapBet) seItems.get(1).getBetType()).scope());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) seItems.get(1).getBetType()).outcome());

        // Negative check
        assertFalse(handler.supports(offer, "Corners - Draw No Bet", SportType.FOOTBALL));
    }

    @Test
    public void testOddsMapperIntegration_DoubleChanceBttsDrawNoBet() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId(4001L);
        event.setHomeName("Real Madrid");
        event.setAwayName("Barcelona");
        event.setName("Real Madrid vs Barcelona");
        event.setStart("2026-10-15T19:00:00Z");
        response.setEvents(List.of(event));

        List<KambiBetOffer> betoffers = new ArrayList<>();

        // 1. Double Chance
        KambiBetOffer dc = new KambiBetOffer();
        KambiBetOffer.KambiCriterion dcCrit = new KambiBetOffer.KambiCriterion();
        dcCrit.setLabel("Double Chance");
        dcCrit.setEnglishLabel("Double Chance");
        dc.setCriterion(dcCrit);
        KambiOutcome dc1 = new KambiOutcome();
        dc1.setId(101L);
        dc1.setType("OT_ONE_CROSS");
        dc1.setOdds(1300);
        dc1.setLabel("1X");
        dc.setOutcomes(List.of(dc1));
        betoffers.add(dc);

        // 2. BTTS
        KambiBetOffer btts = new KambiBetOffer();
        KambiBetOffer.KambiCriterion bttsCrit = new KambiBetOffer.KambiCriterion();
        bttsCrit.setLabel("Both Teams To Score");
        bttsCrit.setEnglishLabel("Both Teams To Score");
        btts.setCriterion(bttsCrit);
        KambiOutcome bttsYes = new KambiOutcome();
        bttsYes.setId(102L);
        bttsYes.setType("OT_YES");
        bttsYes.setOdds(1750);
        bttsYes.setLabel("Yes");
        btts.setOutcomes(List.of(bttsYes));
        betoffers.add(btts);

        // 3. DNB
        KambiBetOffer dnb = new KambiBetOffer();
        KambiBetOffer.KambiCriterion dnbCrit = new KambiBetOffer.KambiCriterion();
        dnbCrit.setLabel("Draw No Bet");
        dnbCrit.setEnglishLabel("Draw No Bet");
        dnb.setCriterion(dnbCrit);
        KambiOutcome dnb1 = new KambiOutcome();
        dnb1.setId(103L);
        dnb1.setType("OT_ONE");
        dnb1.setOdds(1600);
        dnb1.setLabel("Real Madrid");
        dnb.setOutcomes(List.of(dnb1));
        betoffers.add(dnb);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "La Liga");
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        assertTrue(request.getOdds().get(0).getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) request.getOdds().get(0).getBetType()).outcome());

        assertTrue(request.getOdds().get(1).getBetType() instanceof BinaryMarketBet);
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) request.getOdds().get(1).getBetType()).outcome());

        assertTrue(request.getOdds().get(2).getBetType() instanceof HandicapBet);
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) request.getOdds().get(2).getBetType()).outcome());
        assertEquals(0.0, ((HandicapBet) request.getOdds().get(2).getBetType()).param(), 0.001);
    }

    @Test
    public void testStatsCornersHandler_Totals1X2HandicapAndHalves() {
        AtgStatsCornersHandler handler = new AtgStatsCornersHandler();
        KambiEvent event = new KambiEvent();
        event.setId(5001L);
        event.setHomeName("Arsenal");
        event.setAwayName("Chelsea");

        // 1. English Full Match Total Corners (Over/Under)
        KambiBetOffer totalOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit1 = new KambiBetOffer.KambiCriterion();
        crit1.setLabel("Total Corners");
        crit1.setEnglishLabel("Total Corners");
        totalOffer.setCriterion(crit1);

        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(501L);
        oOver.setType("OT_OVER");
        oOver.setLabel("Over 9.5");
        oOver.setLine(9.5);
        oOver.setOdds(1850);

        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(502L);
        oUnder.setType("OT_UNDER");
        oUnder.setLabel("Under 9.5");
        oUnder.setLine(9.5);
        oUnder.setOdds(1950);

        totalOffer.setOutcomes(List.of(oOver, oUnder));

        assertTrue(handler.supports(totalOffer, "Total Corners", SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handleOffer(event, totalOffer, "Total Corners", SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet tb1 = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, tb1.scope());
        assertEquals(BetSubject.MATCH, tb1.subject());
        assertEquals(TotalBet.Direction.OVER, tb1.direction());
        assertEquals(9.5, tb1.param(), 0.001);
        assertEquals(StatType.CORNERS, tb1.statType());
        assertEquals(1.85, items.get(0).getValue(), 0.001);

        TotalBet tb2 = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, tb2.scope());
        assertEquals(TotalBet.Direction.UNDER, tb2.direction());
        assertEquals(9.5, tb2.param(), 0.001);
        assertEquals(StatType.CORNERS, tb2.statType());

        // 2. Swedish 1st Half Antal hörnor (Över 4.5 from label)
        KambiBetOffer seHalfOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit2 = new KambiBetOffer.KambiCriterion();
        crit2.setLabel("Antal hörnor - 1:a halvlek");
        seHalfOffer.setCriterion(crit2);

        KambiOutcome seOver = new KambiOutcome();
        seOver.setId(503L);
        seOver.setLabel("Över 4.5");
        seOver.setOdds(1750);

        KambiOutcome seUnder = new KambiOutcome();
        seUnder.setId(504L);
        seUnder.setLabel("Under 4.5");
        seUnder.setOdds(2050);

        seHalfOffer.setOutcomes(List.of(seOver, seUnder));

        assertTrue(handler.supports(seHalfOffer, "Antal hörnor - 1:a halvlek", SportType.FOOTBALL));

        List<OddItem> seItems = new ArrayList<>();
        handler.handleOffer(event, seHalfOffer, "Antal hörnor - 1:a halvlek", SportType.FOOTBALL, seItems);

        assertEquals(2, seItems.size());
        TotalBet seTb1 = (TotalBet) seItems.get(0).getBetType();
        assertEquals(BetScope.HALF_1, seTb1.scope());
        assertEquals(TotalBet.Direction.OVER, seTb1.direction());
        assertEquals(4.5, seTb1.param(), 0.001);
        assertEquals(StatType.CORNERS, seTb1.statType());

        // 3. English Corner Handicap
        KambiBetOffer hdpOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit3 = new KambiBetOffer.KambiCriterion();
        crit3.setLabel("Corner Handicap");
        crit3.setEnglishLabel("Corner Handicap");
        hdpOffer.setCriterion(crit3);

        KambiOutcome h1 = new KambiOutcome();
        h1.setId(505L);
        h1.setType("OT_ONE");
        h1.setLabel("Arsenal");
        h1.setLine(-1.5);
        h1.setOdds(1900);

        KambiOutcome h2 = new KambiOutcome();
        h2.setId(506L);
        h2.setType("OT_TWO");
        h2.setLabel("Chelsea");
        h2.setLine(1.5);
        h2.setOdds(1900);

        hdpOffer.setOutcomes(List.of(h1, h2));

        assertTrue(handler.supports(hdpOffer, "Corner Handicap", SportType.FOOTBALL));

        List<OddItem> hdpItems = new ArrayList<>();
        handler.handleOffer(event, hdpOffer, "Corner Handicap", SportType.FOOTBALL, hdpItems);

        assertEquals(2, hdpItems.size());
        HandicapBet hb1 = (HandicapBet) hdpItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, hb1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param(), 0.001);
        assertEquals(StatType.CORNERS, hb1.statType());

        HandicapBet hb2 = (HandicapBet) hdpItems.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, hb2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param(), 0.001);
        assertEquals(StatType.CORNERS, hb2.statType());

        // 4. Swedish Mest hörnor (1X2 with Draw)
        KambiBetOffer mostOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit4 = new KambiBetOffer.KambiCriterion();
        crit4.setLabel("Mest hörnor");
        mostOffer.setCriterion(crit4);

        KambiOutcome m1 = new KambiOutcome();
        m1.setId(507L);
        m1.setType("OT_ONE");
        m1.setLabel("Arsenal");
        m1.setOdds(1650);

        KambiOutcome mX = new KambiOutcome();
        mX.setId(508L);
        mX.setType("OT_DRAW");
        mX.setLabel("Oavgjort");
        mX.setOdds(7500);

        KambiOutcome m2 = new KambiOutcome();
        m2.setId(509L);
        m2.setType("OT_TWO");
        m2.setLabel("Chelsea");
        m2.setOdds(2400);

        mostOffer.setOutcomes(List.of(m1, mX, m2));

        assertTrue(handler.supports(mostOffer, "Mest hörnor", SportType.FOOTBALL));

        List<OddItem> mostItems = new ArrayList<>();
        handler.handleOffer(event, mostOffer, "Mest hörnor", SportType.FOOTBALL, mostItems);

        assertEquals(3, mostItems.size());
        MatchResultBet mrb1 = (MatchResultBet) mostItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, mrb1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb1.outcome());
        assertEquals(StatType.CORNERS, mrb1.statType());

        MatchResultBet mrbX = (MatchResultBet) mostItems.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, mrbX.outcome());
        assertEquals(StatType.CORNERS, mrbX.statType());

        MatchResultBet mrb2 = (MatchResultBet) mostItems.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, mrb2.outcome());
        assertEquals(StatType.CORNERS, mrb2.statType());

        // 5. Team Corners: "Arsenal - Antal hörnor"
        KambiBetOffer teamCornerOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit5 = new KambiBetOffer.KambiCriterion();
        crit5.setLabel("Arsenal - Antal hörnor");
        teamCornerOffer.setCriterion(crit5);

        KambiOutcome tcOver = new KambiOutcome();
        tcOver.setId(510L);
        tcOver.setLabel("Över 5.5");
        tcOver.setOdds(1800);
        teamCornerOffer.setOutcomes(List.of(tcOver));

        List<OddItem> teamItems = new ArrayList<>();
        handler.handleOffer(event, teamCornerOffer, "Arsenal - Antal hörnor", SportType.FOOTBALL, teamItems);

        assertEquals(1, teamItems.size());
        TotalBet teamTb = (TotalBet) teamItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, teamTb.scope());
        assertEquals(BetSubject.TEAM1, teamTb.subject());
        assertEquals(TotalBet.Direction.OVER, teamTb.direction());
        assertEquals(5.5, teamTb.param(), 0.001);
        assertEquals(StatType.CORNERS, teamTb.statType());
    }

    @Test
    public void testStatsCardsHandler_Totals1X2Handicap() {
        AtgStatsCardsHandler handler = new AtgStatsCardsHandler();
        KambiEvent event = new KambiEvent();
        event.setId(6001L);
        event.setHomeName("Barcelona");
        event.setAwayName("Real Madrid");

        // 1. English Total Cards
        KambiBetOffer totalOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit1 = new KambiBetOffer.KambiCriterion();
        crit1.setLabel("Total Cards");
        crit1.setEnglishLabel("Total Cards");
        totalOffer.setCriterion(crit1);

        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(601L);
        oOver.setType("OT_OVER");
        oOver.setLabel("Over 3.5");
        oOver.setLine(3.5);
        oOver.setOdds(1800);

        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(602L);
        oUnder.setType("OT_UNDER");
        oUnder.setLabel("Under 3.5");
        oUnder.setLine(3.5);
        oUnder.setOdds(2000);

        totalOffer.setOutcomes(List.of(oOver, oUnder));

        assertTrue(handler.supports(totalOffer, "Total Cards", SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handleOffer(event, totalOffer, "Total Cards", SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet tb1 = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, tb1.scope());
        assertEquals(BetSubject.MATCH, tb1.subject());
        assertEquals(TotalBet.Direction.OVER, tb1.direction());
        assertEquals(3.5, tb1.param(), 0.001);
        assertEquals(StatType.YELLOW_CARDS, tb1.statType());

        TotalBet tb2 = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, tb2.scope());
        assertEquals(TotalBet.Direction.UNDER, tb2.direction());
        assertEquals(3.5, tb2.param(), 0.001);
        assertEquals(StatType.YELLOW_CARDS, tb2.statType());

        // 2. Swedish Antal kort - 1:a halvlek
        KambiBetOffer seOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit2 = new KambiBetOffer.KambiCriterion();
        crit2.setLabel("Antal kort - 1:a halvlek");
        seOffer.setCriterion(crit2);

        KambiOutcome seOver = new KambiOutcome();
        seOver.setId(603L);
        seOver.setLabel("Över 1.5");
        seOver.setOdds(1900);
        seOffer.setOutcomes(List.of(seOver));

        assertTrue(handler.supports(seOffer, "Antal kort - 1:a halvlek", SportType.FOOTBALL));

        List<OddItem> seItems = new ArrayList<>();
        handler.handleOffer(event, seOffer, "Antal kort - 1:a halvlek", SportType.FOOTBALL, seItems);

        assertEquals(1, seItems.size());
        TotalBet seTb = (TotalBet) seItems.get(0).getBetType();
        assertEquals(BetScope.HALF_1, seTb.scope());
        assertEquals(TotalBet.Direction.OVER, seTb.direction());
        assertEquals(1.5, seTb.param(), 0.001);
        assertEquals(StatType.YELLOW_CARDS, seTb.statType());

        // 3. Most Cards (1X2)
        KambiBetOffer mostOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion crit3 = new KambiBetOffer.KambiCriterion();
        crit3.setLabel("Most Cards");
        mostOffer.setCriterion(crit3);

        KambiOutcome m1 = new KambiOutcome();
        m1.setId(604L);
        m1.setType("OT_ONE");
        m1.setLabel("Barcelona");
        m1.setOdds(2100);

        KambiOutcome mX = new KambiOutcome();
        mX.setId(605L);
        mX.setType("OT_DRAW");
        mX.setLabel("Draw");
        mX.setOdds(3800);

        KambiOutcome m2 = new KambiOutcome();
        m2.setId(606L);
        m2.setType("OT_TWO");
        m2.setLabel("Real Madrid");
        m2.setOdds(2300);

        mostOffer.setOutcomes(List.of(m1, mX, m2));

        List<OddItem> mostItems = new ArrayList<>();
        handler.handleOffer(event, mostOffer, "Most Cards", SportType.FOOTBALL, mostItems);

        assertEquals(3, mostItems.size());
        MatchResultBet mrb1 = (MatchResultBet) mostItems.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mrb1.outcome());
        assertEquals(StatType.YELLOW_CARDS, mrb1.statType());

        MatchResultBet mrbX = (MatchResultBet) mostItems.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, mrbX.outcome());
        assertEquals(StatType.YELLOW_CARDS, mrbX.statType());

        MatchResultBet mrb2 = (MatchResultBet) mostItems.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, mrb2.outcome());
        assertEquals(StatType.YELLOW_CARDS, mrb2.statType());
    }

    @Test
    public void testOddsMapperIntegration_CornersAndCards() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId(7001L);
        event.setHomeName("Liverpool");
        event.setAwayName("Man City");
        event.setName("Liverpool vs Man City");
        event.setStart("2026-10-20T19:00:00Z");
        response.setEvents(List.of(event));

        List<KambiBetOffer> betoffers = new ArrayList<>();

        // Corner bet offer
        KambiBetOffer cornerOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cornerCrit = new KambiBetOffer.KambiCriterion();
        cornerCrit.setLabel("Total Corners");
        cornerCrit.setEnglishLabel("Total Corners");
        cornerOffer.setCriterion(cornerCrit);

        KambiOutcome co1 = new KambiOutcome();
        co1.setId(701L);
        co1.setType("OT_OVER");
        co1.setLine(10.5);
        co1.setOdds(1900);
        co1.setLabel("Over 10.5");
        cornerOffer.setOutcomes(List.of(co1));
        betoffers.add(cornerOffer);

        // Card bet offer
        KambiBetOffer cardOffer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cardCrit = new KambiBetOffer.KambiCriterion();
        cardCrit.setLabel("Total Cards");
        cardCrit.setEnglishLabel("Total Cards");
        cardOffer.setCriterion(cardCrit);

        KambiOutcome card1 = new KambiOutcome();
        card1.setId(702L);
        card1.setType("OT_UNDER");
        card1.setLine(4.5);
        card1.setOdds(1850);
        card1.setLabel("Under 4.5");
        cardOffer.setOutcomes(List.of(card1));
        betoffers.add(cardOffer);

        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        assertTrue(request.getOdds().get(0).getBetType() instanceof TotalBet);
        TotalBet tbCorners = (TotalBet) request.getOdds().get(0).getBetType();
        assertEquals(StatType.CORNERS, tbCorners.statType());
        assertEquals(10.5, tbCorners.param(), 0.001);
        assertEquals(TotalBet.Direction.OVER, tbCorners.direction());

        assertTrue(request.getOdds().get(1).getBetType() instanceof TotalBet);
        TotalBet tbCards = (TotalBet) request.getOdds().get(1).getBetType();
        assertEquals(StatType.YELLOW_CARDS, tbCards.statType());
        assertEquals(4.5, tbCards.param(), 0.001);
        assertEquals(TotalBet.Direction.UNDER, tbCards.direction());
    }
}