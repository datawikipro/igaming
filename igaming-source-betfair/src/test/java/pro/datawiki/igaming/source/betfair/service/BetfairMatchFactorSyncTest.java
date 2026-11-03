package pro.datawiki.igaming.source.betfair.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairMarketBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairRunnerBook;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOdd;
import pro.datawiki.igaming.source.betfair.service.BetfairApiClient.BetfairOddsResponse;
import pro.datawiki.igaming.source.betfair.service.exchange.BetfairExchangeCrawler;
import pro.datawiki.igaming.source.betfair.service.exchange.BetfairExchangeScheduler;
import pro.datawiki.igaming.source.betfair.service.exchange.SmartMoneyDetector;
import pro.datawiki.igaming.source.betfair.service.exchange.SmartMoneyKafkaPublisher;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BetfairMatchFactorSyncTest {

    private MatchService matchService;
    private MatchPersistenceService persistenceService;
    private MatchCacheRepository matchCacheRepository;
    private BetfairExchangeScheduler exchangeScheduler;

    @BeforeEach
    void setUp() {
        persistenceService = Mockito.mock(MatchPersistenceService.class);
        matchCacheRepository = Mockito.mock(MatchCacheRepository.class);
        SportCacheRepository sportCacheRepository = Mockito.mock(SportCacheRepository.class);
        SportNormalizationService sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        BetfairApiClient apiClient = Mockito.mock(BetfairApiClient.class);
        BetfairOddsMapper oddsMapper = Mockito.mock(BetfairOddsMapper.class);
        BetfairDiscoveryService discoveryService = Mockito.mock(BetfairDiscoveryService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        matchService = new MatchService(
                matchCacheRepository,
                sportCacheRepository,
                objectMapper,
                sportNormalizationService,
                persistenceService,
                null,
                apiClient,
                oddsMapper,
                discoveryService
        );

        BetfairExchangeCrawler crawler = Mockito.mock(BetfairExchangeCrawler.class);
        SmartMoneyDetector detector = Mockito.mock(SmartMoneyDetector.class);
        SmartMoneyKafkaPublisher publisher = Mockito.mock(SmartMoneyKafkaPublisher.class);
        BetfairConfig config = new BetfairConfig();

        exchangeScheduler = new BetfairExchangeScheduler(
                crawler,
                detector,
                publisher,
                persistenceService,
                matchCacheRepository,
                config,
                objectMapper
        );
    }

    @Test
    void testBuildMatchFactors_WithPriceAndTradedVolume() {
        MatchCache match = new MatchCache();
        match.setExternalId("bf-1001");

        BetfairOdd odd1 = new BetfairOdd("1", "Match Odds", "Real Madrid", 1.85);
        odd1.setTradedVolume(150000.0);

        BetfairOdd oddX = new BetfairOdd("X", "Match Odds", "The Draw", 3.75);
        oddX.setTradedVolume(45000.0);

        BetfairOdd odd2 = new BetfairOdd("2", "Match Odds", "Barcelona", 4.20);
        odd2.setTradedVolume(120000.0);

        BetfairOddsResponse response = new BetfairOddsResponse();
        response.setEventId("bf-1001");
        response.setOdds(List.of(odd1, oddX, odd2));

        List<MatchFactor> factors = matchService.buildMatchFactors(response, match);

        assertNotNull(factors);
        // 3 price factors + 3 volume factors = 6 factors
        assertEquals(6, factors.size());

        assertTrue(factors.stream().anyMatch(f -> "1".equals(f.getFactorId()) && f.getValue() == 1.85));
        assertTrue(factors.stream().anyMatch(f -> "VOL_1".equals(f.getFactorId()) && f.getValue() == 150000.0));
        assertTrue(factors.stream().anyMatch(f -> "X".equals(f.getFactorId()) && f.getValue() == 3.75));
        assertTrue(factors.stream().anyMatch(f -> "VOL_X".equals(f.getFactorId()) && f.getValue() == 45000.0));
    }

    @Test
    void testSyncToMatchCacheAndFactors_SavesMetadataAndFactors() {
        BetfairRunnerBook runner1 = BetfairRunnerBook.builder()
                .selectionId("101")
                .runnerName("Arsenal")
                .bestBackPrice(1.95)
                .totalMatched(75000.0)
                .build();

        BetfairRunnerBook runner2 = BetfairRunnerBook.builder()
                .selectionId("102")
                .runnerName("Chelsea")
                .bestBackPrice(3.90)
                .totalMatched(65000.0)
                .build();

        BetfairMarketBook market = BetfairMarketBook.builder()
                .marketId("1.2001")
                .eventId("bf-ex-001")
                .matchName("Arsenal vs Chelsea")
                .sport("Soccer")
                .league("Premier League")
                .totalMatched(140000.0)
                .runners(List.of(runner1, runner2))
                .build();

        exchangeScheduler.syncToMatchCacheAndFactors(market);

        ArgumentCaptor<MatchCache> matchCaptor = ArgumentCaptor.forClass(MatchCache.class);
        ArgumentCaptor<List<MatchFactor>> factorsCaptor = ArgumentCaptor.forClass(List.class);

        verify(persistenceService, times(1)).saveOrUpdateMatchMetadata(
                matchCaptor.capture(),
                anyString(),
                factorsCaptor.capture()
        );

        MatchCache savedMatch = matchCaptor.getValue();
        assertEquals("betfair", savedMatch.getBookmaker());
        assertEquals("bf-ex-001", savedMatch.getExternalId());
        assertEquals("Arsenal", savedMatch.getTeam1());
        assertEquals("Chelsea", savedMatch.getTeam2());
        assertEquals(MatchCache.Status.PROCESSED, savedMatch.getStatus());

        List<MatchFactor> savedFactors = factorsCaptor.getValue();
        assertEquals(4, savedFactors.size()); // 2 price factors + 2 volume factors
        assertTrue(savedFactors.stream().anyMatch(f -> "101".equals(f.getFactorId()) && f.getValue() == 1.95));
        assertTrue(savedFactors.stream().anyMatch(f -> "VOL_101".equals(f.getFactorId()) && f.getValue() == 75000.0));
    }
}
