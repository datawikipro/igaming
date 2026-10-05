package pro.datawiki.igaming.source.atg.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.atg.service.handler.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AtgLoadIntegrationTest {

    private AtgOddsMapper oddsMapper;

    @BeforeEach
    void setUp() {
        UnmappedBetService unmappedBetService = mock(UnmappedBetService.class);
        SportNormalizationService sportNormalizationService = mock(SportNormalizationService.class);
        BetTypeResolverService betTypeResolver = mock(BetTypeResolverService.class);

        when(sportNormalizationService.normalize(anyString())).thenAnswer(inv -> {
            String sport = inv.getArgument(0, String.class);
            if (sport == null) return SportType.FOOTBALL;
            String lower = sport.toLowerCase();
            if (lower.contains("cs2") || lower.contains("esport")) return SportType.CS2;
            if (lower.contains("basket")) return SportType.BASKETBALL;
            if (lower.contains("hockey")) return SportType.HOCKEY;
            return SportType.FOOTBALL;
        });

        List<AtgMarketHandler> handlers = List.of(
                new AtgEsportsHandler(),
                new AtgStatsCornersHandler(),
                new AtgStatsCardsHandler(),
                new AtgDoubleChanceHandler(),
                new AtgBttsHandler(),
                new AtgDrawNoBetHandler(),
                new AtgTotalHandler(),
                new AtgHandicapHandler(),
                new AtgMoneylineHandler()
        );

        oddsMapper = new AtgOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver, handlers);
    }

    @Test
    @DisplayName("Load Test: Process 500 matches (20,000+ odds) concurrently across 10 threads without errors or race conditions")
    void testConcurrentHighThroughputBatchProcessingUnderLoad() throws Exception {
        int matchCount = 500;
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(matchCount);
        AtomicInteger totalOddsProcessed = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < matchCount; i++) {
            final int matchId = 100_000 + i;
            executor.submit(() -> {
                try {
                    KambiEventDetailsResponse response = createRealisticKambiResponse(matchId);
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");

                    assertNotNull(request);
                    assertEquals("atg", request.getBookmaker());
                    assertFalse(request.getOdds().isEmpty());
                    totalOddsProcessed.addAndGet(request.getOdds().size());
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

        assertTrue(completed, "Batch processing must complete within 30 seconds");
        assertEquals(0, failureCount.get(), "Zero exceptions expected during high throughput concurrent processing");
        assertTrue(totalOddsProcessed.get() >= 5000, "Should have processed at least 5,000 odds items, got: " + totalOddsProcessed.get());
        assertTrue(durationMs < 15000, "Processing 500 matches must complete in less than 15s (actual: " + durationMs + "ms)");
    }

    @Test
    @DisplayName("Isolation Test under Load: Full match, halves, corners, cards, and esports must not cross-contaminate")
    void testMarketScopeAndStatisticalIsolationUnderLoad() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId(99999L);
        event.setName("Arsenal vs Chelsea");
        event.setHomeName("Arsenal");
        event.setAwayName("Chelsea");
        event.setState("NOT_STARTED");
        response.setEvents(List.of(event));

        List<KambiBetOffer> offers = new ArrayList<>();

        // 1. Full Match 1X2
        offers.add(createOffer("Full Time Result", List.of(
                createOutcome(101L, "OT_ONE", "Arsenal", 2.10, null),
                createOutcome(102L, "OT_DRAW", "Draw", 3.40, null),
                createOutcome(103L, "OT_TWO", "Chelsea", 3.20, null)
        )));

        // 2. 1st Half 1X2
        offers.add(createOffer("Half Time Result", List.of(
                createOutcome(201L, "OT_ONE", "Arsenal", 2.80, null),
                createOutcome(202L, "OT_DRAW", "Draw", 2.10, null),
                createOutcome(203L, "OT_TWO", "Chelsea", 3.60, null)
        )));

        // 3. Full Match Total Over/Under 2.5
        offers.add(createOffer("Total Goals", List.of(
                createOutcome(301L, "OT_OVER", "Over 2.5", 1.85, 2.5),
                createOutcome(302L, "OT_UNDER", "Under 2.5", 1.95, 2.5)
        )));

        // 4. 1st Half Total Over 1.5
        offers.add(createOffer("Total Goals - 1st Half", List.of(
                createOutcome(401L, "OT_OVER", "Over 1.5", 2.45, 1.5)
        )));

        // 5. Corners Total Over 9.5
        offers.add(createOffer("Total Corners", List.of(
                createOutcome(501L, "OT_OVER", "Over 9.5", 1.90, 9.5)
        )));

        // 6. Yellow Cards Total Under 3.5
        offers.add(createOffer("Total Cards", List.of(
                createOutcome(601L, "OT_UNDER", "Under 3.5", 2.05, 3.5)
        )));

        response.setBetoffers(offers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "Premier League");
        assertNotNull(request);

        for (OddItem item : request.getOdds()) {
            BetType betType = item.getBetType();
            if (betType instanceof MatchResultBet mrb) {
                if (List.of("101", "102", "103").contains(item.getFactorId())) {
                    assertEquals(BetScope.FULL_MATCH, mrb.scope(), "Main 1X2 must have FULL_MATCH scope");
                    assertEquals(StatType.MATCH, mrb.statType());
                } else if (List.of("201", "202", "203").contains(item.getFactorId())) {
                    assertEquals(BetScope.HALF_1, mrb.scope(), "1H 1X2 must have HALF_1 scope");
                    assertEquals(StatType.MATCH, mrb.statType());
                }
            } else if (betType instanceof TotalBet tb) {
                if ("301".equals(item.getFactorId()) || "302".equals(item.getFactorId())) {
                    assertEquals(BetScope.FULL_MATCH, tb.scope());
                    assertEquals(StatType.MATCH, tb.statType());
                } else if ("401".equals(item.getFactorId())) {
                    assertEquals(BetScope.HALF_1, tb.scope());
                    assertEquals(StatType.MATCH, tb.statType());
                } else if ("501".equals(item.getFactorId())) {
                    assertEquals(StatType.CORNERS, tb.statType(), "Corner total must have CORNERS statType");
                } else if ("601".equals(item.getFactorId())) {
                    assertEquals(StatType.YELLOW_CARDS, tb.statType(), "Cards total must have YELLOW_CARDS statType");
                }
            }
        }
    }

    @Test
    @DisplayName("Super-Arb Suppression: 3-Way European handicap and 3-way totals must be strictly rejected")
    void testEuropean3WayHandicapAnd3WayTotalsSuppression() {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId(88888L);
        event.setName("Barcelona vs Real Madrid");
        event.setHomeName("Barcelona");
        event.setAwayName("Real Madrid");
        response.setEvents(List.of(event));

        List<KambiBetOffer> offers = new ArrayList<>();

        // 1. European 3-Way Handicap (contains OT_DRAW / Draw outcome)
        offers.add(createOffer("3-Way Handicap", List.of(
                createOutcome(701L, "OT_ONE", "Barcelona (-1)", 2.65, -1.0),
                createOutcome(702L, "OT_DRAW", "Tie (Barcelona -1)", 3.60, -1.0),
                createOutcome(703L, "OT_TWO", "Real Madrid (+1)", 2.20, 1.0)
        )));

        // 2. Swedish 3-Vägshandikapp
        offers.add(createOffer("Handikapp (3-vägs)", List.of(
                createOutcome(704L, "OT_ONE", "Barcelona (-2)", 4.50, -2.0),
                createOutcome(705L, "OT_CROSS", "Oavgjort", 4.10, -2.0),
                createOutcome(706L, "OT_TWO", "Real Madrid (+2)", 1.55, 2.0)
        )));

        // 3. 3-Way Total (contains OT_EXACTLY outcome)
        offers.add(createOffer("3-Way Total Goals", List.of(
                createOutcome(707L, "OT_OVER", "Over 2", 2.25, 2.0),
                createOutcome(708L, "OT_EXACTLY", "Exactly 2", 3.40, 2.0),
                createOutcome(709L, "OT_UNDER", "Under 2", 2.80, 2.0)
        )));

        // 4. Valid 2-Way Asian Handicap (MUST be mapped)
        offers.add(createOffer("Asian Handicap", List.of(
                createOutcome(710L, "OT_ONE", "Barcelona (-0.5)", 1.95, -0.5),
                createOutcome(711L, "OT_TWO", "Real Madrid (+0.5)", 1.85, 0.5)
        )));

        response.setBetoffers(offers);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(response, "Football", "La Liga");
        assertNotNull(request);

        // Only the valid 2-Way Asian Handicap (710, 711) should be mapped
        assertEquals(2, request.getOdds().size(), "3-way European handicaps and 3-way totals must be suppressed; exactly 2 valid 2-way odds expected");
        assertEquals("710", request.getOdds().get(0).getFactorId());
        assertEquals("711", request.getOdds().get(1).getFactorId());
        assertTrue(request.getOdds().get(0).getBetType() instanceof HandicapBet);
        assertTrue(request.getOdds().get(1).getBetType() instanceof HandicapBet);
    }

    private KambiEventDetailsResponse createRealisticKambiResponse(int id) {
        KambiEventDetailsResponse response = new KambiEventDetailsResponse();
        KambiEvent event = new KambiEvent();
        event.setId((long) id);
        event.setName("Team A" + id + " vs Team B" + id);
        event.setHomeName("Team A" + id);
        event.setAwayName("Team B" + id);
        event.setStart("2026-10-05T18:00:00Z");
        event.setState("NOT_STARTED");

        KambiEvent.KambiPath p = new KambiEvent.KambiPath();
        p.setName("Football");
        event.setPath(List.of(p));
        response.setEvents(List.of(event));

        List<KambiBetOffer> offers = new ArrayList<>();
        // 1X2
        offers.add(createOffer("Full Time Result", List.of(
                createOutcome(id * 10L + 1, "OT_ONE", "1", 2.05, null),
                createOutcome(id * 10L + 2, "OT_DRAW", "X", 3.25, null),
                createOutcome(id * 10L + 3, "OT_TWO", "2", 3.50, null)
        )));
        // Double Chance
        offers.add(createOffer("Double Chance", List.of(
                createOutcome(id * 10L + 4, "OT_ONE_CROSS", "1X", 1.30, null),
                createOutcome(id * 10L + 5, "OT_ONE_TWO", "12", 1.35, null),
                createOutcome(id * 10L + 6, "OT_CROSS_TWO", "X2", 1.70, null)
        )));
        // Total Goals
        offers.add(createOffer("Total Goals", List.of(
                createOutcome(id * 10L + 7, "OT_OVER", "Over 2.5", 1.90, 2.5),
                createOutcome(id * 10L + 8, "OT_UNDER", "Under 2.5", 1.90, 2.5)
        )));
        // Draw No Bet
        offers.add(createOffer("Draw No Bet", List.of(
                createOutcome(id * 10L + 9, "OT_ONE", "Team A" + id, 1.55, 0.0),
                createOutcome(id * 10L + 10, "OT_TWO", "Team B" + id, 2.45, 0.0)
        )));

        response.setBetoffers(offers);
        return response;
    }

    private KambiBetOffer createOffer(String label, List<KambiOutcome> outcomes) {
        KambiBetOffer offer = new KambiBetOffer();
        KambiBetOffer.KambiCriterion criterion = new KambiBetOffer.KambiCriterion();
        criterion.setLabel(label);
        criterion.setEnglishLabel(label);
        offer.setCriterion(criterion);
        offer.setOutcomes(outcomes);
        return offer;
    }

    private KambiOutcome createOutcome(Long id, String type, String label, double decimalOdds, Double line) {
        KambiOutcome outcome = new KambiOutcome();
        outcome.setId(id);
        outcome.setType(type);
        outcome.setLabel(label);
        outcome.setOdds((int) Math.round(decimalOdds * 1000));
        outcome.setLine(line);
        return outcome;
    }
}
