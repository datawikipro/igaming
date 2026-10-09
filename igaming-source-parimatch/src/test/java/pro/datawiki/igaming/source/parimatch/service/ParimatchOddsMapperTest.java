package pro.datawiki.igaming.source.parimatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchEvent;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchMarket;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchOutcome;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ParimatchOddsMapperTest {

    private ParimatchOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new ParimatchOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    void testMapParimatchMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("pm_1001");
        cache.setTeam1("Real Madrid");
        cache.setTeam2("Barcelona");
        cache.setSportName("Football");
        cache.setLeagueName("La Liga");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        ParimatchEvent event = new ParimatchEvent();
        event.setId("pm_1001");
        event.setName("Real Madrid - Barcelona");
        event.setSportName("Football");
        event.setTournamentName("La Liga");

        // 1X2 Market
        ParimatchMarket m1X2 = new ParimatchMarket();
        m1X2.setId("m_1");
        m1X2.setName("1X2");

        ParimatchOutcome o1 = new ParimatchOutcome();
        o1.setId("o_1");
        o1.setName("1");
        o1.setValue(2.10);

        ParimatchOutcome oX = new ParimatchOutcome();
        oX.setId("o_2");
        oX.setName("X");
        oX.setValue(3.50);

        ParimatchOutcome o2 = new ParimatchOutcome();
        o2.setId("o_3");
        o2.setName("2");
        o2.setValue(3.20);

        m1X2.setOutcomes(List.of(o1, oX, o2));

        // Total Market
        ParimatchMarket mTotal = new ParimatchMarket();
        mTotal.setId("m_2");
        mTotal.setName("Total Goals");

        ParimatchOutcome oOver = new ParimatchOutcome();
        oOver.setId("o_4");
        oOver.setName("Over");
        oOver.setParam(2.5);
        oOver.setValue(1.75);

        ParimatchOutcome oUnder = new ParimatchOutcome();
        oUnder.setId("o_5");
        oUnder.setName("Under");
        oUnder.setParam(2.5);
        oUnder.setValue(2.10);

        mTotal.setOutcomes(List.of(oOver, oUnder));

        // Handicap Market
        ParimatchMarket mHandicap = new ParimatchMarket();
        mHandicap.setId("m_3");
        mHandicap.setName("Handicap");

        ParimatchOutcome oH1 = new ParimatchOutcome();
        oH1.setId("o_6");
        oH1.setName("1");
        oH1.setParam(-1.5);
        oH1.setValue(3.80);

        ParimatchOutcome oH2 = new ParimatchOutcome();
        oH2.setId("o_7");
        oH2.setName("2");
        oH2.setParam(1.5);
        oH2.setValue(1.25);

        mHandicap.setOutcomes(List.of(oH1, oH2));

        // BTTS Market
        ParimatchMarket mBtts = new ParimatchMarket();
        mBtts.setId("m_4");
        mBtts.setName("Both Teams to Score");

        ParimatchOutcome oYes = new ParimatchOutcome();
        oYes.setId("o_8");
        oYes.setName("Yes");
        oYes.setValue(1.65);

        ParimatchOutcome oNo = new ParimatchOutcome();
        oNo.setId("o_9");
        oNo.setName("No");
        oNo.setValue(2.20);

        mBtts.setOutcomes(List.of(oYes, oNo));

        event.setMarkets(List.of(m1X2, mTotal, mHandicap, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("parimatch", result.getBookmaker());
        assertEquals("pm_1001", result.getExternalEventId());
        assertEquals("Real Madrid", result.getTeam1());
        assertEquals("Barcelona", result.getTeam2());
        assertEquals(9, result.getOdds().size());

        // Verify 1X2
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.10));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.50));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.20));

        // Verify Total
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.75));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 2.10));

        // Verify Handicap
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 3.80));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 1.25));

        // Verify BTTS
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.65));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.20));
    }
}
