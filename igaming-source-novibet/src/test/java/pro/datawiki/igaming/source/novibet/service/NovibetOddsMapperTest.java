package pro.datawiki.igaming.source.novibet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.altenar.dto.AltenarEvent;
import pro.datawiki.igaming.source.core.engine.altenar.dto.AltenarMarket;
import pro.datawiki.igaming.source.core.engine.altenar.dto.AltenarOutcome;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class NovibetOddsMapperTest {

    private NovibetOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new NovibetOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    void testMapAltenarMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("alt_1001");
        cache.setTeam1("Flamengo");
        cache.setTeam2("Palmeiras");
        cache.setSportName("Soccer");
        cache.setLeagueName("Brasileirao");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        AltenarEvent event = new AltenarEvent();
        event.setId(1001L);
        event.setName("Flamengo vs Palmeiras");
        event.setSportName("Soccer");
        event.setChampionshipName("Brasileirao");

        // 1X2 Market
        AltenarMarket m1X2 = new AltenarMarket();
        m1X2.setId(1L);
        m1X2.setName("1X2");

        AltenarOutcome o1 = new AltenarOutcome();
        o1.setId(101L);
        o1.setName("1");
        o1.setDecimalPrice(2.10);

        AltenarOutcome oX = new AltenarOutcome();
        oX.setId(102L);
        oX.setName("X");
        oX.setDecimalPrice(3.20);

        AltenarOutcome o2 = new AltenarOutcome();
        o2.setId(103L);
        o2.setName("2");
        o2.setDecimalPrice(3.40);

        m1X2.setLines(List.of(o1, oX, o2));

        // Total Market
        AltenarMarket mTotal = new AltenarMarket();
        mTotal.setId(2L);
        mTotal.setName("Total Mais/Menos");

        AltenarOutcome oOver = new AltenarOutcome();
        oOver.setId(201L);
        oOver.setName("Mais");
        oOver.setLine(2.5);
        oOver.setDecimalPrice(1.85);

        AltenarOutcome oUnder = new AltenarOutcome();
        oUnder.setId(202L);
        oUnder.setName("Menos");
        oUnder.setLine(2.5);
        oUnder.setDecimalPrice(1.95);

        mTotal.setLines(List.of(oOver, oUnder));

        // Both to score
        AltenarMarket mBtts = new AltenarMarket();
        mBtts.setId(3L);
        mBtts.setName("Ambas Marcam");

        AltenarOutcome oYes = new AltenarOutcome();
        oYes.setId(301L);
        oYes.setName("Sim");
        oYes.setDecimalPrice(1.75);

        AltenarOutcome oNo = new AltenarOutcome();
        oNo.setId(302L);
        oNo.setName("Não");
        oNo.setDecimalPrice(2.05);

        mBtts.setLines(List.of(oYes, oNo));

        event.setMarkets(List.of(m1X2, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("novibet", result.getBookmaker());
        assertEquals("1001", result.getExternalEventId());
        assertEquals("Flamengo", result.getTeam1());
        assertEquals("Palmeiras", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.10));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.85));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.75));
    }
}