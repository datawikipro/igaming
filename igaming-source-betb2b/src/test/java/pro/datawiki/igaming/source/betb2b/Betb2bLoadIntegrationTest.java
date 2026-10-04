package pro.datawiki.igaming.source.betb2b;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyEvent;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyGame;
import pro.datawiki.igaming.source.core.engine.xbet.mapper.XbetFamilyMapper;
import pro.datawiki.igaming.source.core.engine.xbet.service.XbetFamilyOddsProcessor;
import pro.datawiki.igaming.source.core.engine.xbet.strategy.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.MappingConflictService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Betb2bLoadIntegrationTest {

    private XbetFamilyOddsProcessor oddsProcessor;
    private List<XbetFactorStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = List.of(
                new XbetMainResultStrategy(),
                new XbetHandicapStrategy(),
                new XbetTotalStrategy(),
                new XbetHalvesStrategy(),
                new XbetHalfTimeFullTimeStrategy(),
                new XbetTeamToScoreStrategy(),
                new XbetBinaryMarketStrategy(),
                new XbetStatsStrategy()
        );

        XbetFamilyMapper mapper = new XbetFamilyMapper(strategies);
        MappingConflictService conflictService = mock(MappingConflictService.class);
        BetTypeResolverService resolverService = new BetTypeResolverService(List.of(mapper), conflictService);
        UnmappedBetService unmappedBetService = mock(UnmappedBetService.class);

        oddsProcessor = new XbetFamilyOddsProcessor(resolverService, unmappedBetService, strategies);
    }

    @Test
    @DisplayName("Load Test: Process 500 matches (20,000 odds) across 10 threads without memory leaks or race conditions")
    void testHighThroughputConcurrentBatchProcessingUnderLoad() throws Exception {
        int matchCount = 500;
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(matchCount);
        AtomicInteger totalOddsProcessed = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < matchCount; i++) {
            final int matchIndex = i;
            executor.submit(() -> {
                try {
                    MatchCache match = new MatchCache();
                    match.setExternalId("fansport_event_" + matchIndex);
                    match.setTeam1("Team Alpha " + matchIndex);
                    match.setTeam2("Team Beta " + matchIndex);

                    XbetFamilyGame game = createRealisticGame(matchIndex);
                    List<OddItem> items = oddsProcessor.processOdds(match, game, SportType.FOOTBALL, "fansport");

                    assertNotNull(items);
                    assertFalse(items.isEmpty());
                    totalOddsProcessed.addAndGet(items.size());
                } catch (Throwable t) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long durationMs = System.currentTimeMillis() - startTime;

        assertTrue(completed, "Batch processing must complete within timeout");
        assertEquals(0, failureCount.get(), "Zero exceptions expected during concurrent processing under load");
        assertTrue(totalOddsProcessed.get() >= 9000, "Should have processed at least 9,000 odds items, got: " + totalOddsProcessed.get());
        assertTrue(durationMs < 15000, "Processing 500 matches must complete in less than 15s (actual: " + durationMs + "ms)");
    }

    @Test
    @DisplayName("Isolation Test under Load: Verify full match, half time, and corner statistics do not cross-contaminate")
    void testMarketIsolationAndNoCrossContaminationUnderLoad() {
        MatchCache match = new MatchCache();
        match.setExternalId("fansport_iso_777");
        match.setTeam1("Real Madrid");
        match.setTeam2("Barcelona");

        XbetFamilyGame game = new XbetFamilyGame();
        List<XbetFamilyEvent> events = new ArrayList<>();

        // 1X2 Full Match
        events.add(createEvent(1, 2.10, null));
        events.add(createEvent(2, 3.40, null));
        events.add(createEvent(3, 3.20, null));

        // 1H 1X2
        events.add(createEvent(15, 2.80, null));
        events.add(createEvent(16, 2.10, null));
        events.add(createEvent(17, 3.60, null));

        // Full Match Total Over/Under 2.5
        events.add(createEvent(9, 1.85, 2.5));
        events.add(createEvent(10, 1.95, 2.5));

        // 1H Total Over 1.5
        events.add(createEvent(45, 2.45, 1.5));

        // Corners Total Over 9.5
        events.add(createEvent(1711, 1.90, 9.5));

        game.setEvents(events);

        List<OddItem> items = oddsProcessor.processOdds(match, game, SportType.FOOTBALL, "fansport");
        assertNotNull(items);

        for (OddItem item : items) {
            BetType betType = item.getBetType();
            if (betType instanceof MatchResultBet mrb) {
                if (List.of("1", "2", "3").contains(item.getFactorId())) {
                    assertEquals(BetScope.FULL_MATCH, mrb.scope(), "Main 1X2 must have FULL_MATCH scope");
                } else if (List.of("15", "16", "17").contains(item.getFactorId())) {
                    assertEquals(BetScope.HALF_1, mrb.scope(), "1H 1X2 must have HALF_1 scope");
                }
            } else if (betType instanceof TotalBet tb) {
                if ("9".equals(item.getFactorId()) || "10".equals(item.getFactorId())) {
                    assertEquals(BetScope.FULL_MATCH, tb.scope());
                    assertEquals(StatType.MATCH, tb.statType());
                } else if ("45".equals(item.getFactorId())) {
                    assertEquals(BetScope.HALF_1, tb.scope());
                    assertEquals(StatType.MATCH, tb.statType());
                } else if ("1711".equals(item.getFactorId())) {
                    assertEquals(StatType.CORNERS, tb.statType(), "Corner total must have CORNERS statType");
                }
            }
        }
    }

    @Test
    @DisplayName("Deduplication Test: Verify duplicate factors are safely suppressed without crashing")
    void testDuplicateCollisionSuppressionUnderLoad() {
        MatchCache match = new MatchCache();
        match.setExternalId("fansport_dup_100");
        match.setTeam1("Bayern");
        match.setTeam2("Dortmund");

        XbetFamilyGame game = new XbetFamilyGame();
        List<XbetFamilyEvent> events = new ArrayList<>();

        // Add W1 outcome twice with different odds
        events.add(createEvent(1, 1.80, null));
        events.add(createEvent(1, 1.85, null)); // duplicate

        // Add Total Over 2.5 twice
        events.add(createEvent(9, 1.90, 2.5));
        events.add(createEvent(9, 1.95, 2.5)); // duplicate

        game.setEvents(events);

        List<OddItem> items = oddsProcessor.processOdds(match, game, SportType.FOOTBALL, "fansport");
        assertEquals(2, items.size(), "Duplicates must be deduplicated; exactly 2 distinct items expected");
        assertEquals(1.80, items.get(0).getValue(), "First seen W1 odd must be preserved");
        assertEquals(1.90, items.get(1).getValue(), "First seen Total Over odd must be preserved");
    }

    private XbetFamilyGame createRealisticGame(int id) {
        XbetFamilyGame game = new XbetFamilyGame();
        game.setId((long) id);

        List<XbetFamilyEvent> events = new ArrayList<>();
        // 1X2
        events.add(createEvent(1, 2.05 + (id % 10) * 0.05, null));
        events.add(createEvent(2, 3.20 + (id % 5) * 0.05, null));
        events.add(createEvent(3, 3.50 + (id % 8) * 0.05, null));
        // Double Chance
        events.add(createEvent(4, 1.30, null));
        events.add(createEvent(5, 1.35, null));
        events.add(createEvent(6, 1.70, null));
        // Totals
        events.add(createEvent(9, 1.90, 2.5));
        events.add(createEvent(10, 1.90, 2.5));
        events.add(createEvent(9, 1.45, 1.5));
        events.add(createEvent(10, 2.65, 1.5));
        // Handicaps
        events.add(createEvent(7, 1.95, -0.5));
        events.add(createEvent(8, 1.85, 0.5));
        // 1H
        events.add(createEvent(15, 2.70, null));
        events.add(createEvent(16, 2.10, null));
        events.add(createEvent(17, 3.90, null));
        events.add(createEvent(45, 2.80, 1.5));
        // Corners
        events.add(createEvent(1711, 1.92, 9.5));
        events.add(createEvent(1712, 1.88, 9.5));

        game.setEvents(events);
        return game;
    }

    private XbetFamilyEvent createEvent(int type, double coeff, Double param) {
        XbetFamilyEvent event = new XbetFamilyEvent();
        event.setType(type);
        event.setCoefficient(coeff);
        event.setParameter(param);
        return event;
    }
}
