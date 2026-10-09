package pro.datawiki.igaming.source.vbet.service;

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
import pro.datawiki.igaming.source.vbet.dto.VbetEvent;
import pro.datawiki.igaming.source.vbet.dto.VbetStake;
import pro.datawiki.igaming.source.vbet.dto.VbetStakeGroup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class VbetOddsMapperTest {

    private VbetOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new VbetOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    void testMapVbetMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("vbet_9911");
        cache.setTeam1("Arsenal");
        cache.setTeam2("Chelsea");
        cache.setSportName("Football");
        cache.setLeagueName("Premier League");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        VbetEvent event = new VbetEvent();
        event.setId(9911L);
        event.setHomeTeam("Arsenal");
        event.setAwayTeam("Chelsea");
        event.setEnglishSportName("Football");
        event.setEnglishChampionshipName("Premier League");

        // 1X2 Market (Group 1)
        VbetStakeGroup g1X2 = new VbetStakeGroup();
        g1X2.setId(1L);
        g1X2.setName("Match Result");

        VbetStake s1 = new VbetStake();
        s1.setId(101L);
        s1.setName("1");
        s1.setFactor(1.95);

        VbetStake sX = new VbetStake();
        sX.setId(102L);
        sX.setName("X");
        sX.setFactor(3.60);

        VbetStake s2 = new VbetStake();
        s2.setId(103L);
        s2.setName("2");
        s2.setFactor(3.80);

        g1X2.setStakes(List.of(s1, sX, s2));

        // Total Goals (Group 2)
        VbetStakeGroup gTotal = new VbetStakeGroup();
        gTotal.setId(2L);
        gTotal.setName("Total");

        VbetStake sOver = new VbetStake();
        sOver.setId(201L);
        sOver.setName("Over");
        sOver.setArgument(2.5);
        sOver.setFactor(1.80);

        VbetStake sUnder = new VbetStake();
        sUnder.setId(202L);
        sUnder.setName("Under");
        sUnder.setArgument(2.5);
        sUnder.setFactor(2.05);

        gTotal.setStakes(List.of(sOver, sUnder));

        // Handicap (Group 3)
        VbetStakeGroup gHandicap = new VbetStakeGroup();
        gHandicap.setId(3L);
        gHandicap.setName("Handicap");

        VbetStake sH1 = new VbetStake();
        sH1.setId(301L);
        sH1.setName("1");
        sH1.setArgument(-1.0);
        sH1.setFactor(2.65);

        VbetStake sH2 = new VbetStake();
        sH2.setId(302L);
        sH2.setName("2");
        sH2.setArgument(1.0);
        sH2.setFactor(1.50);

        gHandicap.setStakes(List.of(sH1, sH2));

        // BTTS (Group 702)
        VbetStakeGroup gBtts = new VbetStakeGroup();
        gBtts.setId(702L);
        gBtts.setName("Both Teams to Score");

        VbetStake sYes = new VbetStake();
        sYes.setId(401L);
        sYes.setName("Yes");
        sYes.setFactor(1.70);

        VbetStake sNo = new VbetStake();
        sNo.setId(402L);
        sNo.setName("No");
        sNo.setFactor(2.15);

        gBtts.setStakes(List.of(sYes, sNo));

        event.setStakeTypes(List.of(g1X2, gTotal, gHandicap, gBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("vbet", result.getBookmaker());
        assertEquals("9911", result.getExternalEventId());
        assertEquals("Arsenal", result.getTeam1());
        assertEquals("Chelsea", result.getTeam2());
        assertEquals(9, result.getOdds().size());

        // Verify 1X2
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.95));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.60));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.80));

        // Verify Total
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.80));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 2.05));

        // Verify Handicap
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 2.65));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet && o.getValue() == 1.50));

        // Verify BTTS
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.70));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.15));
    }
}
