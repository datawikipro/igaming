package pro.datawiki.igaming.source.betfair.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOddsResponse;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOdd;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BetfairOddsMapperTest {

    private BetfairOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;
    private BetfairConfig config;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        config = new BetfairConfig();
        config.getExchange().setCommissionRate(0.02);
        config.getExchange().setDeductCommission(true);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new BetfairOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver, config);
    }

    @Test
    void testCommissionCalculation() {
        // gross 2.00 at 2% commission -> 1.98
        assertEquals(1.98, mapper.calculateNetBackOdd(2.00, 0.02));
        // gross 3.50 at 2% commission -> 1 + 2.5 * 0.98 = 3.45
        assertEquals(3.45, mapper.calculateNetBackOdd(3.50, 0.02));
        // gross 1.05 at 5% commission -> 1 + 0.05 * 0.95 = 1.048
        assertEquals(1.048, mapper.calculateNetBackOdd(1.05, 0.05));
    }

    @Test
    void testMapBetfairMarketsWithCommission() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("bf_5001");
        cache.setTeam1("Manchester United");
        cache.setTeam2("Chelsea");
        cache.setSportName("Soccer");
        cache.setLeagueName("English Premier League");
        cache.setStartTime(1720000000000L);
        cache.setIsLive(false);

        BetfairOddsResponse response = new BetfairOddsResponse();
        response.setEventId("bf_5001");

        BetfairOdd o1 = new BetfairOdd("1", "Match Odds", "Manchester United", 2.00);
        BetfairOdd oX = new BetfairOdd("X", "Match Odds", "Draw", 3.40);
        BetfairOdd o2 = new BetfairOdd("2", "Match Odds", "Chelsea", 3.80);

        BetfairOdd oOver = new BetfairOdd("over", "Over/Under 2.5 Goals", "Over 2.5", 1.90);
        BetfairOdd oUnder = new BetfairOdd("under", "Over/Under 2.5 Goals", "Under 2.5", 1.95);

        BetfairOdd bttsYes = new BetfairOdd("yes", "Both Teams to Score", "Yes", 1.70);
        BetfairOdd bttsNo = new BetfairOdd("no", "Both Teams to Score", "No", 2.10);

        response.setOdds(List.of(o1, oX, o2, oOver, oUnder, bttsYes, bttsNo));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(response, cache, "Soccer", "Premier League");

        assertNotNull(result);
        assertEquals("betfair", result.getBookmaker());
        assertEquals("bf_5001", result.getExternalEventId());
        assertEquals("Manchester United", result.getTeam1());
        assertEquals("Chelsea", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        // Net odd for 2.00 at 2% commission is 1.98
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.98));
        // Net odd for 1.90 at 2% commission is 1 + 0.90 * 0.98 = 1.882
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.882));
        // Net odd for 1.70 at 2% commission is 1 + 0.70 * 0.98 = 1.686
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.686));
    }
}
