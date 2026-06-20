package pro.datawiki.igaming.source.betsson.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.betsson.dto.kambi.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class BetssonOddsMapperTest {

    private BetssonOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new BetssonOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
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
        assertEquals("betsson", request.getBookmaker());
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
}
