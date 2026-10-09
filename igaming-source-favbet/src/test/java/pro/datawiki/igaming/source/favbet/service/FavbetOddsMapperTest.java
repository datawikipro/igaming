package pro.datawiki.igaming.source.favbet.service;

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
import pro.datawiki.igaming.source.favbet.dto.FavbetEvent;
import pro.datawiki.igaming.source.favbet.dto.FavbetMarket;
import pro.datawiki.igaming.source.favbet.dto.FavbetOutcome;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class FavbetOddsMapperTest {

    private FavbetOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new FavbetOddsMapper(unmappedBetService, sportNormalizationService);
    }

    @Test
    void testMapFavbetMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("1001");
        cache.setTeam1("Dynamo Kyiv");
        cache.setTeam2("Shakhtar Donetsk");
        cache.setSportName("Football");
        cache.setLeagueName("Ukrainian Premier League");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        FavbetEvent event = new FavbetEvent();
        event.setEventId(1001L);
        event.setEventName("Dynamo Kyiv - Shakhtar Donetsk");
        event.setSportName("Football");
        event.setTournamentName("Ukrainian Premier League");

        // 1X2 Market
        FavbetMarket m1X2 = new FavbetMarket();
        m1X2.setMarketId(101L);
        m1X2.setMarketName("Match Result (1X2)");

        FavbetOutcome o1 = new FavbetOutcome();
        o1.setOutcomeId(11L);
        o1.setOutcomeName("1");
        o1.setOutcomeCoef(2.35);

        FavbetOutcome oX = new FavbetOutcome();
        oX.setOutcomeId(12L);
        oX.setOutcomeName("X");
        oX.setOutcomeCoef(3.40);

        FavbetOutcome o2 = new FavbetOutcome();
        o2.setOutcomeId(13L);
        o2.setOutcomeName("2");
        o2.setOutcomeCoef(2.95);

        m1X2.setOutcomes(List.of(o1, oX, o2));

        // Total Market
        FavbetMarket mTotal = new FavbetMarket();
        mTotal.setMarketId(102L);
        mTotal.setMarketName("Total Goals");

        FavbetOutcome oOver = new FavbetOutcome();
        oOver.setOutcomeId(21L);
        oOver.setOutcomeName("Over 2.5");
        oOver.setOutcomeParam(2.5);
        oOver.setOutcomeCoef(1.85);

        FavbetOutcome oUnder = new FavbetOutcome();
        oUnder.setOutcomeId(22L);
        oUnder.setOutcomeName("Under 2.5");
        oUnder.setOutcomeParam(2.5);
        oUnder.setOutcomeCoef(1.95);

        mTotal.setOutcomes(List.of(oOver, oUnder));

        // Both to score
        FavbetMarket mBtts = new FavbetMarket();
        mBtts.setMarketId(103L);
        mBtts.setMarketName("Both Teams to Score");

        FavbetOutcome oYes = new FavbetOutcome();
        oYes.setOutcomeId(31L);
        oYes.setOutcomeName("Yes");
        oYes.setOutcomeCoef(1.72);

        FavbetOutcome oNo = new FavbetOutcome();
        oNo.setOutcomeId(32L);
        oNo.setOutcomeName("No");
        oNo.setOutcomeCoef(2.05);

        mBtts.setOutcomes(List.of(oYes, oNo));

        event.setMarkets(List.of(m1X2, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, event);

        assertNotNull(result);
        assertEquals("favbet", result.getBookmaker());
        assertEquals("1001", result.getExternalEventId());
        assertEquals("Dynamo Kyiv", result.getTeam1());
        assertEquals("Shakhtar Donetsk", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        // Verify 1X2 odds
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.35));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 3.40));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 2.95));

        // Verify Total odds
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.85));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.95));

        // Verify BTTS odds
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.72));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 2.05));
    }
}
