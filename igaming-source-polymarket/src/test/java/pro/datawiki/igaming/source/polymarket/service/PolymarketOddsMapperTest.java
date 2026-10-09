package pro.datawiki.igaming.source.polymarket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketEventDto;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketMarketDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PolymarketOddsMapperTest {

    private PolymarketOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);
        objectMapper = new ObjectMapper();

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new PolymarketOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver, objectMapper);
    }

    @Test
    void testDecimalOddsCalculation() {
        // p = 0.50 -> 2.00
        assertEquals(2.00, mapper.calculateDecimalOdds(0.50));
        // p = 0.25 -> 4.00
        assertEquals(4.00, mapper.calculateDecimalOdds(0.25));
        // p = 0.65 -> 1.538
        assertEquals(1.538, mapper.calculateDecimalOdds(0.65));
        // edge bounds
        assertNull(mapper.calculateDecimalOdds(0.0001));
        assertNull(mapper.calculateDecimalOdds(0.9999));
    }

    @Test
    void testMapPolymarketMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("poly_4001");
        cache.setTeam1("Real Madrid");
        cache.setTeam2("Barcelona");
        cache.setSportName("Soccer");
        cache.setLeagueName("La Liga");
        cache.setStartTime(1720000000000L);
        cache.setIsLive(false);

        PolymarketEventDto event = new PolymarketEventDto();
        event.setId("poly_4001");
        event.setTitle("Real Madrid vs Barcelona");

        // 3-way 1X2 Market
        PolymarketMarketDto m1X2 = new PolymarketMarketDto();
        m1X2.setId("m1");
        m1X2.setQuestion("Real Madrid vs Barcelona: Who will win?");
        m1X2.setOutcomes(List.of("Real Madrid", "Draw", "Barcelona"));
        m1X2.setOutcomePrices(List.of(0.48, 0.26, 0.26)); // ~2.083, 3.846, 3.846

        // Over 2.5 Market
        PolymarketMarketDto mTotal = new PolymarketMarketDto();
        mTotal.setId("m2");
        mTotal.setQuestion("Will there be over 2.5 goals in Real Madrid vs Barcelona?");
        mTotal.setOutcomes(List.of("Yes", "No"));
        mTotal.setOutcomePrices(List.of(0.55, 0.45)); // ~1.818, 2.222

        // BTTS Market
        PolymarketMarketDto mBtts = new PolymarketMarketDto();
        mBtts.setId("m3");
        mBtts.setQuestion("Both teams to score in Real Madrid vs Barcelona?");
        mBtts.setOutcomes(List.of("Yes", "No"));
        mBtts.setOutcomePrices(List.of(0.60, 0.40)); // ~1.667, 2.500

        event.setMarkets(List.of(m1X2, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("polymarket", result.getBookmaker());
        assertEquals("poly_4001", result.getExternalEventId());
        assertEquals("Real Madrid", result.getTeam1());
        assertEquals("Barcelona", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.083));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.818));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.667));
    }
}
