package pro.datawiki.igaming.source.tonybet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.core.engine.kambi.dto.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class TonyBetOddsMapperTest {

    private TonyBetOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new TonyBetOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    public void testMapToOddsUpdateRequest() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        List<KambiEvent> events = new ArrayList<>();
        KambiEvent event = new KambiEvent();
        event.setId(99223344L);
        event.setName("Arsenal vs Chelsea");
        event.setHomeName("Arsenal");
        event.setAwayName("Chelsea");
        event.setStart("2026-09-14T18:00:00Z");
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
        o1.setId(101L);
        o1.setLabel("Arsenal");
        o1.setType("OT_ONE");
        o1.setOdds(1850);
        outcomes1.add(o1);

        KambiOutcome o2 = new KambiOutcome();
        o2.setId(102L);
        o2.setLabel("Draw");
        o2.setType("OT_DRAW");
        o2.setOdds(3500);
        outcomes1.add(o2);

        KambiOutcome o3 = new KambiOutcome();
        o3.setId(103L);
        o3.setLabel("Chelsea");
        o3.setType("OT_TWO");
        o3.setOdds(4200);
        outcomes1.add(o3);

        matchResultOffer.setOutcomes(outcomes1);
        betoffers.add(matchResultOffer);
        response.setBetoffers(betoffers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");

        assertNotNull(request);
        assertEquals("tonybet", request.getBookmaker());
        assertEquals("99223344", request.getExternalEventId());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertEquals(3, request.getOdds().size());

        OddItem item1 = request.getOdds().get(0);
        assertEquals("101", item1.getFactorId());
        assertEquals(1.85, item1.getValue(), 0.001);
        assertTrue(item1.getBetType() instanceof MatchResultBet);
    }
}
