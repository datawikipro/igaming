package pro.datawiki.igaming.source.bwin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.bwin.dto.entain.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class BwinOddsMapperTest {

    private BwinOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new BwinOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    private EntainValueObject val(String s) {
        EntainValueObject v = new EntainValueObject();
        v.setValue(s);
        return v;
    }

    @Test
    public void testMapToOddsUpdateRequest() {
        EntainEventDetailsResponse response = new EntainEventDetailsResponse();
        EntainFixture fixture = new EntainFixture();
        fixture.setSourceId(99223344L);
        fixture.setName(val("Real Madrid - Barcelona"));
        fixture.setStartDate("2026-06-02T18:00:00Z");

        List<EntainOptionMarket> markets = new ArrayList<>();

        // 1. Match Result (1X2)
        EntainOptionMarket matchResultMarket = new EntainOptionMarket();
        matchResultMarket.setName(val("Match Result"));
        List<EntainOption> options1 = new ArrayList<>();

        EntainOption o1 = new EntainOption();
        o1.setId(101L);
        o1.setName(val("1"));
        EntainPrice p1 = new EntainPrice();
        p1.setOdds(1.95);
        o1.setPrice(p1);
        options1.add(o1);

        EntainOption o2 = new EntainOption();
        o2.setId(102L);
        o2.setName(val("X"));
        EntainPrice p2 = new EntainPrice();
        p2.setOdds(3.40);
        o2.setPrice(p2);
        options1.add(o2);

        EntainOption o3 = new EntainOption();
        o3.setId(103L);
        o3.setName(val("2"));
        EntainPrice p3 = new EntainPrice();
        p3.setOdds(3.60);
        o3.setPrice(p3);
        options1.add(o3);

        matchResultMarket.setOptions(options1);
        markets.add(matchResultMarket);

        // 2. Total Goals
        EntainOptionMarket totalMarket = new EntainOptionMarket();
        totalMarket.setName(val("Total Goals"));
        List<EntainOption> options2 = new ArrayList<>();

        EntainOption oOver = new EntainOption();
        oOver.setId(201L);
        oOver.setName(val("Over 2.5"));
        EntainPrice pOver = new EntainPrice();
        pOver.setOdds(1.90);
        oOver.setPrice(pOver);
        options2.add(oOver);

        EntainOption oUnder = new EntainOption();
        oUnder.setId(202L);
        oUnder.setName(val("Under 2.5"));
        EntainPrice pUnder = new EntainPrice();
        pUnder.setOdds(1.92);
        oUnder.setPrice(pUnder);
        options2.add(oUnder);

        totalMarket.setOptions(options2);
        markets.add(totalMarket);

        fixture.setOptionMarkets(markets);
        response.setFixture(fixture);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "La Liga");

        assertNotNull(request);
        assertEquals("bwin", request.getBookmaker());
        assertEquals("99223344", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("La Liga", request.getLeagueName());
        assertEquals("Real Madrid", request.getTeam1());
        assertEquals("Barcelona", request.getTeam2());
        assertTrue(request.getIsLive());
        assertEquals(1780423200000L, request.getStartTime()); // 2026-06-02T18:00:00Z in epoch millis

        List<OddItem> odds = request.getOdds();
        assertNotNull(odds);
        assertEquals(5, odds.size());

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
    }
}
