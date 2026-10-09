package pro.datawiki.igaming.source.betdaq.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betdaq.config.BetdaqConfig;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqEventDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqMarketDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqPriceDto;
import pro.datawiki.igaming.source.betdaq.dto.BetdaqSelectionDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BetdaqOddsMapperTest {

    private BetdaqOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;
    private BetdaqConfig config;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        config = new BetdaqConfig();
        config.setCommissionRate(0.02);
        config.setDeductCommission(true);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new BetdaqOddsMapper(unmappedBetService, sportNormalizationService, config);
    }

    @Test
    void testNetOddsCommission() {
        // Gross 2.00 at 2% commission -> 1.98
        assertEquals(1.98, mapper.calculateNetBackOdd(2.00, 0.02));
        // Gross 3.00 at 2% commission -> 1 + 2.0 * 0.98 = 2.96
        assertEquals(2.96, mapper.calculateNetBackOdd(3.00, 0.02));
    }

    @Test
    void testMapBetdaqMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("bd_6001");
        cache.setTeam1("Bayern Munich");
        cache.setTeam2("Borussia Dortmund");
        cache.setSportName("Football");
        cache.setLeagueName("Bundesliga");
        cache.setStartTime(1725000000000L);
        cache.setIsLive(false);

        BetdaqEventDto event = new BetdaqEventDto();
        event.setId("bd_6001");
        event.setName("Bayern Munich v Borussia Dortmund");

        // 1X2 Market
        BetdaqMarketDto m1X2 = new BetdaqMarketDto();
        m1X2.setId("m1");
        m1X2.setName("Match Odds");

        BetdaqSelectionDto s1 = new BetdaqSelectionDto();
        s1.setId("s1");
        s1.setName("Bayern Munich");
        BetdaqPriceDto p1 = new BetdaqPriceDto();
        p1.setPrice(1.60);
        s1.setBackPrices(List.of(p1));

        BetdaqSelectionDto sX = new BetdaqSelectionDto();
        sX.setId("sX");
        sX.setName("The Draw");
        BetdaqPriceDto pX = new BetdaqPriceDto();
        pX.setPrice(4.20);
        sX.setBackPrices(List.of(pX));

        BetdaqSelectionDto s2 = new BetdaqSelectionDto();
        s2.setId("s2");
        s2.setName("Borussia Dortmund");
        BetdaqPriceDto p2 = new BetdaqPriceDto();
        p2.setPrice(5.00);
        s2.setBackPrices(List.of(p2));

        m1X2.setSelections(List.of(s1, sX, s2));

        // Over / Under Market
        BetdaqMarketDto mTotal = new BetdaqMarketDto();
        mTotal.setId("m2");
        mTotal.setName("Over/Under 2.5 Goals");

        BetdaqSelectionDto sOver = new BetdaqSelectionDto();
        sOver.setId("sOver");
        sOver.setName("Over 2.5");
        BetdaqPriceDto pOver = new BetdaqPriceDto();
        pOver.setPrice(1.75);
        sOver.setBackPrices(List.of(pOver));

        BetdaqSelectionDto sUnder = new BetdaqSelectionDto();
        sUnder.setId("sUnder");
        sUnder.setName("Under 2.5");
        BetdaqPriceDto pUnder = new BetdaqPriceDto();
        pUnder.setPrice(2.10);
        sUnder.setBackPrices(List.of(pUnder));

        mTotal.setSelections(List.of(sOver, sUnder));

        // Both Teams to Score
        BetdaqMarketDto mBtts = new BetdaqMarketDto();
        mBtts.setId("m3");
        mBtts.setName("Both Teams to Score");

        BetdaqSelectionDto sYes = new BetdaqSelectionDto();
        sYes.setId("sYes");
        sYes.setName("Yes");
        BetdaqPriceDto pYes = new BetdaqPriceDto();
        pYes.setPrice(1.65);
        sYes.setBackPrices(List.of(pYes));

        BetdaqSelectionDto sNo = new BetdaqSelectionDto();
        sNo.setId("sNo");
        sNo.setName("No");
        BetdaqPriceDto pNo = new BetdaqPriceDto();
        pNo.setPrice(2.25);
        sNo.setBackPrices(List.of(pNo));

        mBtts.setSelections(List.of(sYes, sNo));

        event.setMarkets(List.of(m1X2, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("betdaq", result.getBookmaker());
        assertEquals("bd_6001", result.getExternalEventId());
        assertEquals("Bayern Munich", result.getTeam1());
        assertEquals("Borussia Dortmund", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        // 1.60 gross at 2% commission -> 1 + 0.60 * 0.98 = 1.588
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.588));
        // 1.75 gross at 2% commission -> 1 + 0.75 * 0.98 = 1.735
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.735));
        // 1.65 gross at 2% commission -> 1 + 0.65 * 0.98 = 1.637
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.637));
    }
}
