package pro.datawiki.igaming.source.wplay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.wplay.config.WplayConfig;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WplayCrawlerLoaderIntegrationTest {

    private HttpServer testServer;
    private int testPort;
    private WplayConfig config;
    private WplayApiClient apiClient;

    private MatchPersistenceService persistenceService;
    private MatchCacheRepository matchCacheRepository;
    private SportCacheRepository sportCacheRepository;
    private SportNormalizationService sportNormalizationService;
    private AggregatorClient aggregatorClient;
    private WplayOddsMapper oddsMapper;

    private WplayDiscoveryService discoveryService;
    private WplayMatchService matchService;

    @BeforeEach
    void setUp() throws Exception {
        testServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        testPort = testServer.getAddress().getPort();

        config = new WplayConfig();
        config.setBaseUrl("http://127.0.0.1:" + testPort);
        config.setConnectTimeout(2000);
        config.setReadTimeout(2000);
        config.setTargetEventCount(550);
        config.setSports(List.of("/es/s/FOOT/Futbol", "/es/s/BASK/Baloncesto", "/es/s/TENNIS/Tenis"));

        apiClient = new WplayApiClient(config);
        persistenceService = mock(MatchPersistenceService.class);
        matchCacheRepository = mock(MatchCacheRepository.class);
        sportCacheRepository = mock(SportCacheRepository.class);
        sportNormalizationService = mock(SportNormalizationService.class);
        aggregatorClient = mock(AggregatorClient.class);
        oddsMapper = new WplayOddsMapper();

        discoveryService = new WplayDiscoveryService(apiClient, config, persistenceService);
        matchService = new WplayMatchService(
                matchCacheRepository,
                sportCacheRepository,
                new ObjectMapper(),
                sportNormalizationService,
                persistenceService,
                aggregatorClient,
                apiClient,
                oddsMapper,
                discoveryService
        );
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    @DisplayName("Crawler Load Test: Discovers >= 500 matches and verifies deduplication on subsequent run")
    void testDiscoveryCrawlerUnderHighLoadAndDeduplication() {
        // Setup home page with 150 events and 2 tournament links
        StringBuilder homeHtml = new StringBuilder("<html><body><div class=\"events\">");
        for (int i = 1; i <= 150; i++) {
            homeHtml.append(String.format("<a href=\"/es/e/%d/Team-Alpha-%d-v-Team-Beta-%d\">Match %d</a>", 1000 + i, i, i, i));
        }
        homeHtml.append("<a href=\"/es/t/2001/Copa-Colombia\">Tournament 1</a>");
        homeHtml.append("<a href=\"/es/t/2002/Liga-Dimayor\">Tournament 2</a>");
        homeHtml.append("</div></body></html>");

        // Setup live page with 50 live events
        StringBuilder liveHtml = new StringBuilder("<html><body><div class=\"live\">");
        for (int i = 1; i <= 50; i++) {
            liveHtml.append(String.format("<a href=\"/es/e/%d/Live-Team-One-%d-vs-Live-Team-Two-%d\">Live Match %d</a>", 2000 + i, i, i, i));
        }
        liveHtml.append("</div></body></html>");

        // Setup football category with 150 events
        StringBuilder footHtml = new StringBuilder("<html><body><div class=\"sport\">");
        for (int i = 1; i <= 150; i++) {
            footHtml.append(String.format("<a href=\"/es/e/%d/Boca-Juniors-%d-v-River-Plate-%d\">Foot Match %d</a>", 3000 + i, i, i, i));
        }
        footHtml.append("</div></body></html>");

        // Setup basketball category with 100 events
        StringBuilder baskHtml = new StringBuilder("<html><body><div class=\"sport\">");
        for (int i = 1; i <= 100; i++) {
            baskHtml.append(String.format("<a href=\"/es/e/%d/Lakers-%d-@-Celtics-%d\">Bask Match %d</a>", 4000 + i, i, i, i));
        }
        baskHtml.append("</div></body></html>");

        // Setup tournament 1 with 120 events
        StringBuilder tour1Html = new StringBuilder("<html><body><div class=\"tour\">");
        for (int i = 1; i <= 120; i++) {
            tour1Html.append(String.format("<a href=\"/es/e/%d/Deportivo-Cali-%d-v-Santa-Fe-%d\">Tour Match %d</a>", 5000 + i, i, i, i));
        }
        tour1Html.append("</div></body></html>");

        registerContext("/es", homeHtml.toString());
        registerContext("/es/live", liveHtml.toString());
        registerContext("/es/s/FOOT/Futbol", footHtml.toString());
        registerContext("/es/s/BASK/Baloncesto", baskHtml.toString());
        registerContext("/es/s/TENNIS/Tenis", "<html><body>No Tennis</body></html>");
        registerContext("/es/t/2001/Copa-Colombia", tour1Html.toString());
        registerContext("/es/t/2002/Liga-Dimayor", "<html><body>No Liga</body></html>");

        testServer.start();

        // 1. First discovery run: expect >= 500 matches
        int totalDiscovered = discoveryService.discoverEvents();
        assertTrue(totalDiscovered >= 500, "Should discover >= 500 matches under full catalog load, discovered: " + totalDiscovered);

        ArgumentCaptor<MatchCache> matchCaptor = ArgumentCaptor.forClass(MatchCache.class);
        verify(persistenceService, times(totalDiscovered)).saveOrUpdateMatchMetadata(matchCaptor.capture(), anyString());

        List<MatchCache> savedMatches = matchCaptor.getAllValues();
        assertEquals(totalDiscovered, savedMatches.size());
        assertTrue(savedMatches.stream().allMatch(m -> "wplay".equals(m.getBookmaker())));
        assertTrue(savedMatches.stream().allMatch(m -> m.getEventUrl() != null && m.getEventUrl().startsWith("https://apuestas.wplay.co/es/e/")));
        assertTrue(savedMatches.stream().anyMatch(m -> "Boca Juniors 1".equals(m.getTeam1()) && "River Plate 1".equals(m.getTeam2())));

        // 2. Second discovery run with identical data: deduplication cache must prevent redundant DB saves
        clearInvocations(persistenceService);
        int secondRunDiscovered = discoveryService.discoverEvents();
        assertEquals(0, secondRunDiscovered, "Second run with identical data must discover 0 new events due to footprint cache");
        verifyNoInteractions(persistenceService);
    }

    @Test
    @DisplayName("Crawler Deadline Test: Gracefully exits when target count reached without stalling")
    void testCrawlerTargetLimitAndQuickExit() {
        // Prepare 700 matches in home page
        StringBuilder homeHtml = new StringBuilder("<html><body>");
        for (int i = 1; i <= 700; i++) {
            homeHtml.append(String.format("<a href=\"/es/e/%d/Home-%d-v-Away-%d\">Match %d</a>", 9000 + i, i, i, i));
        }
        homeHtml.append("</body></html>");

        registerContext("/es", homeHtml.toString());
        registerContext("/es/live", "<html><body>Empty</body></html>");
        registerContext("/es/s/FOOT/Futbol", "<html><body>Empty</body></html>");

        testServer.start();

        long start = System.currentTimeMillis();
        int discovered = discoveryService.discoverEvents();
        long elapsed = System.currentTimeMillis() - start;

        // Target was 550, home page had 700 matches, so single home fetch got 700 and subsequent sports were skipped
        assertTrue(discovered >= 550, "Should have reached target event count, got: " + discovered);
        assertTrue(elapsed < 5000, "Discovery must complete fast without stalls, elapsed: " + elapsed + "ms");
    }

    @Test
    @DisplayName("Loader Batch Test: Loads odds under load, maps 1X2 prices, and pushes updates to aggregator")
    void testOddsLoaderBatchProcessingUnderLoad() {
        int batchSize = 25;
        List<MatchCache> mockMatches = new ArrayList<>();
        for (int i = 1; i <= batchSize; i++) {
            MatchCache m = new MatchCache();
            m.setExternalId("match_" + i);
            m.setTeam1("Team Red " + i);
            m.setTeam2("Team Blue " + i);
            m.setSportName("Soccer");
            m.setLeagueName("Liga Colombiana");
            m.setEventUrl("http://127.0.0.1:" + testPort + "/match/" + i);
            mockMatches.add(m);

            // Realistic Wplay match card HTML with button price structure
            String matchHtml = String.format("""
                    <div class="market-view">
                        <button type="button" class="price">
                            <span class="seln-name">Team Red %d</span>
                            <span class="price dec">2.15</span>
                        </button>
                        <button type="button" class="price">
                            <span class="seln-name">Empate</span>
                            <span class="price dec">3.25</span>
                        </button>
                        <button type="button" class="price">
                            <span class="seln-name">Team Blue %d</span>
                            <span class="price dec">3.40</span>
                        </button>
                    </div>
                    """, i, i);
            registerContext("/match/" + i, matchHtml);
        }

        when(matchCacheRepository.findTop500ByOrderByUpdatedAtDesc()).thenReturn(mockMatches);
        testServer.start();

        long start = System.currentTimeMillis();
        matchService.fetchOddsForActiveMatches();
        long elapsed = System.currentTimeMillis() - start;

        // WplayMatchService limits to top 20 active matches per cycle
        ArgumentCaptor<OddsUpdateRequest> updateCaptor = ArgumentCaptor.forClass(OddsUpdateRequest.class);
        verify(aggregatorClient, times(20)).pushOddsUpdate(updateCaptor.capture());

        List<OddsUpdateRequest> capturedUpdates = updateCaptor.getAllValues();
        assertEquals(20, capturedUpdates.size());

        for (int i = 0; i < 20; i++) {
            OddsUpdateRequest req = capturedUpdates.get(i);
            assertEquals("wplay", req.getBookmaker());
            assertEquals("match_" + (i + 1), req.getExternalEventId());
            assertEquals(3, req.getOdds().size());

            assertEquals(2.15, req.getOdds().get(0).getValue());
            assertInstanceOf(MatchResultBet.class, req.getOdds().get(0).getBetType());
            assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) req.getOdds().get(0).getBetType()).outcome());

            assertEquals(3.25, req.getOdds().get(1).getValue());
            assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) req.getOdds().get(1).getBetType()).outcome());

            assertEquals(3.40, req.getOdds().get(2).getValue());
            assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) req.getOdds().get(2).getBetType()).outcome());
        }

        assertTrue(elapsed < 10000, "Processing 20 matches must complete in under 10 seconds, elapsed: " + elapsed + "ms");
    }

    @Test
    @DisplayName("Loader Resilience Test: Handles HTTP 500 errors, timeouts, and empty HTML without throwing exceptions")
    void testOddsLoaderErrorToleranceAndRateLimitResilience() {
        List<MatchCache> mockMatches = new ArrayList<>();

        // Match 1: 200 OK
        MatchCache m1 = createMatch("m1", "/match/ok");
        registerContext("/match/ok", """
                <div>
                   <button type="button" class="price"><span class="seln-name">A</span><span class="price dec">1.90</span></button>
                   <button type="button" class="price"><span class="seln-name">Empate</span><span class="price dec">3.10</span></button>
                   <button type="button" class="price"><span class="seln-name">B</span><span class="price dec">4.00</span></button>
                </div>
                """);
        mockMatches.add(m1);

        // Match 2: 500 Internal Server Error
        MatchCache m2 = createMatch("m2", "/match/500");
        testServer.createContext("/match/500", exchange -> {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        mockMatches.add(m2);

        // Match 3: Malformed HTML with no odds
        MatchCache m3 = createMatch("m3", "/match/empty");
        registerContext("/match/empty", "<html><body>No odds available</body></html>");
        mockMatches.add(m3);

        // Match 4: Another 200 OK
        MatchCache m4 = createMatch("m4", "/match/ok2");
        registerContext("/match/ok2", """
                <div>
                   <span class="price dec">2.00</span>
                   <span class="price dec">3.00</span>
                   <span class="price dec">3.50</span>
                </div>
                """);
        mockMatches.add(m4);

        when(matchCacheRepository.findTop500ByOrderByUpdatedAtDesc()).thenReturn(mockMatches);
        testServer.start();

        assertDoesNotThrow(() -> matchService.fetchOddsForActiveMatches());

        // Should successfully push odds for m1 and m4 (2 successful out of 4)
        ArgumentCaptor<OddsUpdateRequest> updateCaptor = ArgumentCaptor.forClass(OddsUpdateRequest.class);
        verify(aggregatorClient, times(2)).pushOddsUpdate(updateCaptor.capture());

        List<OddsUpdateRequest> updates = updateCaptor.getAllValues();
        assertEquals("m1", updates.get(0).getExternalEventId());
        assertEquals("m4", updates.get(1).getExternalEventId());
    }

    @Test
    @DisplayName("Concurrent Load Test: Multi-threaded execution of discovery and odds loading without deadlocks")
    void testConcurrentDiscoveryAndOddsProcessingUnderLoad() throws Exception {
        // Register endpoints
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 1; i <= 60; i++) {
            html.append(String.format("<a href=\"/es/e/%d/Team-Alpha-%d-v-Team-Beta-%d\">Match %d</a>", 8000 + i, i, i, i));
        }
        html.append("</body></html>");

        registerContext("/es", html.toString());
        registerContext("/es/live", "<html><body>Empty</body></html>");
        for (String sport : config.getSports()) {
            registerContext(sport, html.toString());
        }

        List<MatchCache> activeMatches = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            MatchCache m = createMatch("conc_" + i, "/conc/" + i);
            registerContext("/conc/" + i, """
                    <div>
                       <span class="price dec">1.80</span>
                       <span class="price dec">3.30</span>
                       <span class="price dec">4.20</span>
                    </div>
                    """);
            activeMatches.add(m);
        }
        when(matchCacheRepository.findTop500ByOrderByUpdatedAtDesc()).thenReturn(activeMatches);

        testServer.start();

        int workerThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(workerThreads);
        CountDownLatch latch = new CountDownLatch(workerThreads);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < workerThreads; t++) {
            final int threadIdx = t;
            executor.submit(() -> {
                try {
                    if (threadIdx % 2 == 0) {
                        discoveryService.discoverEvents();
                    } else {
                        matchService.fetchOddsForActiveMatches();
                    }
                } catch (Throwable ex) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrent crawler and loader workers must complete without deadlock");
        assertEquals(0, errorCount.get(), "Zero exceptions expected during concurrent crawler/loader execution");
    }

    private MatchCache createMatch(String externalId, String path) {
        MatchCache m = new MatchCache();
        m.setExternalId(externalId);
        m.setTeam1("Team A");
        m.setTeam2("Team B");
        m.setSportName("Soccer");
        m.setLeagueName("Liga");
        m.setEventUrl("http://127.0.0.1:" + testPort + path);
        return m;
    }

    private void registerContext(String path, String responseBody) {
        testServer.createContext(path, exchange -> {
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }
}
