package pro.datawiki.igaming.source.bcgame.service;

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
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BcgameOddsMapperTest {

    private BcgameOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.BASKETBALL);

        mapper = new BcgameOddsMapper(unmappedBetService, sportNormalizationService);
    }

    @Test
    void testMapBcgameMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("bc_2001");
        cache.setTeam1("Los Angeles Lakers");
        cache.setTeam2("Boston Celtics");
        cache.setSportName("Basketball");
        cache.setLeagueName("NBA");
        cache.setStartTime(1710000000000L);
        cache.setIsLive(false);

        BcgameEventDto event = new BcgameEventDto();
        event.setId("bc_2001");
        event.setHomeTeam("Los Angeles Lakers");
        event.setAwayTeam("Boston Celtics");
        event.setSportName("Basketball");
        event.setTournamentName("NBA");

        // Moneyline
        BcgameMarketDto mWinner = new BcgameMarketDto();
        mWinner.setId("m1");
        mWinner.setName("Winner (incl. overtime)");

        BcgameOutcomeDto o1 = new BcgameOutcomeDto();
        o1.setId("o1");
        o1.setName("Los Angeles Lakers");
        o1.setOdds(2.05);

        BcgameOutcomeDto o2 = new BcgameOutcomeDto();
        o2.setId("o2");
        o2.setName("Boston Celtics");
        o2.setOdds(1.80);

        mWinner.setOutcomes(List.of(o1, o2));

        // Total
        BcgameMarketDto mTotal = new BcgameMarketDto();
        mTotal.setId("m2");
        mTotal.setName("Total Points (Over/Under)");

        BcgameOutcomeDto oOver = new BcgameOutcomeDto();
        oOver.setId("oOver");
        oOver.setName("Over 224.5");
        oOver.setOdds(1.90);
        oOver.setTotal(224.5);

        BcgameOutcomeDto oUnder = new BcgameOutcomeDto();
        oUnder.setId("oUnder");
        oUnder.setName("Under 224.5");
        oUnder.setOdds(1.90);
        oUnder.setTotal(224.5);

        mTotal.setOutcomes(List.of(oOver, oUnder));

        // BTTS
        BcgameMarketDto mBtts = new BcgameMarketDto();
        mBtts.setId("m3");
        mBtts.setName("Both Teams to Score 100+ points");

        BcgameOutcomeDto oYes = new BcgameOutcomeDto();
        oYes.setId("oYes");
        oYes.setName("Yes");
        oYes.setOdds(1.45);

        BcgameOutcomeDto oNo = new BcgameOutcomeDto();
        oNo.setId("oNo");
        oNo.setName("No");
        oNo.setOdds(2.65);

        mBtts.setOutcomes(List.of(oYes, oNo));

        event.setMarkets(List.of(mWinner, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("bcgame", result.getBookmaker());
        assertEquals("bc_2001", result.getExternalEventId());
        assertEquals("Los Angeles Lakers", result.getTeam1());
        assertEquals("Boston Celtics", result.getTeam2());
        assertEquals(6, result.getOdds().size());

        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.05));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.90));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.45));
    }
}
