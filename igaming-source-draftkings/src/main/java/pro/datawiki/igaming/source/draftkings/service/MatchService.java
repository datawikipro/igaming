package pro.datawiki.igaming.source.draftkings.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService {

    private final MatchCacheRepository matchCacheRepository;
    private final MatchPersistenceService persistenceService;
    private final AggregatorClient aggregatorClient;
    private final ObjectMapper objectMapper;

    private final DraftKingsApiClient apiClient;
    private final DraftKingsOddsMapper oddsMapper;
    private final DraftKingsDiscoveryService discoveryService;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger accumulatedStaleCount = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.atomic.AtomicLong lastStaleLogTime = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());

    @Autowired
    @Lazy
    private MatchService self;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        ObjectMapper objectMapper,
                        DraftKingsApiClient apiClient,
                        DraftKingsOddsMapper oddsMapper,
                        DraftKingsDiscoveryService discoveryService) {
        this.matchCacheRepository = matchCacheRepository;
        this.persistenceService = persistenceService;
        this.aggregatorClient = aggregatorClient;
        this.objectMapper = objectMapper;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    public String getBookmakerFamily() {
        return "draftkings";
    }

    public String getBookmakerName() {
        return "draftkings";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    public int loadMatchCards(int batchSize) {
        List<MatchCache> batch = self.fetchBatchToProcess(batchSize);
        if (batch.isEmpty()) return 0;

        java.util.concurrent.atomic.AtomicInteger pushedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger unchangedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger staleCount = new java.util.concurrent.atomic.AtomicInteger(0);

        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            List<java.util.concurrent.Future<Boolean>> futures = batch.stream()
                    .map(cache -> executor.submit(() -> {
                        boolean isStale = false;
                        if (cache.getPotentialEndTime() != null) {
                            if (System.currentTimeMillis() > cache.getPotentialEndTime()) {
                                isStale = true;
                            }
                        } else if (cache.getStartTime() != null) {
                            if (System.currentTimeMillis() - cache.getStartTime() > 5 * 60 * 60 * 1000) { // 5 hours
                                isStale = true;
                            }
                        }
                        if (isStale) {
                            log.debug("DraftKings: Skipping stale/ended match {} ({}) past its end window. Marking as PROCESSED.",
                                    cache.getExternalId(), cache.getTeam1() + " - " + cache.getTeam2());
                            matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
                            staleCount.incrementAndGet();
                            return false;
                        }

                        try {
                            int eventGroupId = resolveEventGroupId(cache);
                            DraftKingsEventGroupResponse detailedGroup = apiClient.getEventGroup(eventGroupId);
                            if (detailedGroup != null && detailedGroup.getEventGroup() != null && detailedGroup.getEventGroup().getEvents() != null) {
                                // Find our specific event in the group
                                DraftKingsEventGroupResponse.DraftKingsEvent matchingEvent = detailedGroup.getEventGroup().getEvents().stream()
                                        .filter(e -> cache.getExternalId().equals(e.getEventId()))
                                        .findFirst()
                                        .orElse(null);

                                if (matchingEvent != null) {
                                    boolean pushed = self.processAndPush(matchingEvent, detailedGroup, cache);
                                    if (pushed) {
                                        pushedCount.incrementAndGet();
                                    } else {
                                        unchangedCount.incrementAndGet();
                                    }
                                    return true;
                                }
                            }
                            self.markAsFailed(cache);
                            return false;
                        } catch (Exception e) {
                            if (isOptimisticLockException(e)) {
                                log.debug("Optimistic locking conflict while processing DraftKings match card for event {}: {}", cache.getExternalId(), e.getMessage());
                            } else {
                                log.error("Failed to load DraftKings match card {}: {}", cache.getExternalId(), e.getMessage());
                                try {
                                    self.markAsFailed(cache);
                                } catch (Exception ex) {
                                    if (isOptimisticLockException(ex)) {
                                        log.debug("Optimistic locking conflict while marking DraftKings match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                                    } else {
                                        log.error("Failed to mark DraftKings match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                                    }
                                }
                            }
                            return false;
                        }
                    }))
                    .collect(Collectors.toList());

            int processed = 0;
            for (var future : futures) {
                try {
                    if (future.get()) {
                        processed++;
                    }
                } catch (Exception e) {
                    log.error("Error processing DraftKings match card in parallel: {}", e.getMessage(), e);
                }
            }

            int ucCount = unchangedCount.get();
            if (ucCount > 0) {
                aggregatorClient.reportUnchangedOdds(getBookmakerName(), ucCount);
            }

            if (staleCount.get() > 0) {
                accumulatedStaleCount.addAndGet(staleCount.get());
            }

            long now = System.currentTimeMillis();
            long lastLog = lastStaleLogTime.get();
            if (now - lastLog >= 60000) {
                if (lastStaleLogTime.compareAndSet(lastLog, now)) {
                    int countToLog = accumulatedStaleCount.getAndSet(0);
                    if (countToLog > 0) {
                        log.info("Skipped {} stale/ended matches for {}", countToLog, getBookmakerName());
                    }
                }
            }

            return processed;
        }
    }

    @Transactional
    public List<MatchCache> fetchBatchToProcess(int batchSize) {
        List<MatchCache> batch = matchCacheRepository.findAndLockOnlyNewPendingMatchesByBookmaker("draftkings", org.springframework.data.domain.PageRequest.of(0, batchSize));
        for (MatchCache match : batch) {
            match.setStatus(MatchCache.Status.PENDING);
            match.setUpdatedAt(LocalDateTime.now());
        }
        return matchCacheRepository.saveAll(batch);
    }

    @Transactional
    public boolean processAndPush(DraftKingsEventGroupResponse.DraftKingsEvent event,
                                  DraftKingsEventGroupResponse eventGroupDetails,
                                  MatchCache cached) {
        String sportName = cached.getSportName() != null ? cached.getSportName() : "Unknown";
        String leagueName = cached.getLeagueName() != null ? cached.getLeagueName() : "Unknown";

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, eventGroupDetails, sportName, leagueName);
        if (request == null) return false;

        String currentHash = persistenceService.computeHash(serialize(request));
        String eventId = event.getEventId();

        boolean pushed = false;
        if (!currentHash.equals(localStateHashCache.get(eventId))) {
            if (request.getTeam1() == null && request.getTeam2() == null) {
                aggregatorClient.pushOutrightUpdate(request);
            } else {
                aggregatorClient.pushOddsUpdate(request);
            }
            localStateHashCache.put(eventId, currentHash);
            pushed = true;
        }

        matchCacheRepository.findById(cached.getId()).ifPresent(freshCache -> {
            freshCache.setUpdatedAt(LocalDateTime.now());
            freshCache.setStatus(MatchCache.Status.PROCESSED);
            matchCacheRepository.save(freshCache);
        });
        return pushed;
    }

    @Transactional
    public void markAsFailed(MatchCache cached) {
        matchCacheRepository.findById(cached.getId()).ifPresent(freshCache -> {
            freshCache.setStatus(MatchCache.Status.FAILED);
            freshCache.setUpdatedAt(LocalDateTime.now());
            matchCacheRepository.save(freshCache);
        });
    }

    private int resolveEventGroupId(MatchCache cached) {
        String league = cached.getLeagueName();
        if (league == null) return 88670846; // fallback to NBA
        return switch (league) {
            case "NBA" -> 88670846;
            case "NFL" -> 88670847;
            case "MLB" -> 88670848;
            case "NHL" -> 88670849;
            case "UFC" -> 88671587;
            case "Premier League" -> 88671238;
            default -> {
                try {
                    String numeric = league.replaceAll("[^0-9]", "");
                    yield numeric.isEmpty() ? 88670846 : Integer.parseInt(numeric);
                } catch (Exception e) {
                    yield 88670846;
                }
            }
        };
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isOptimisticLockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.contains("OptimisticLockingFailureException")
                    || name.contains("OptimisticLockException")
                    || name.contains("StaleObjectStateException")
                    || (cause.getMessage() != null && cause.getMessage().contains("Row was updated or deleted by another transaction"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
