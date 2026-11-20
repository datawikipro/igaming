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
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsLeague;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final DraftKingsApiClient apiClient;
    private final DraftKingsOddsMapper oddsMapper;
    private final DraftKingsDiscoveryService discoveryService;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private MatchService self;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        DraftKingsApiClient apiClient,
                        DraftKingsOddsMapper oddsMapper,
                        DraftKingsDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerFamily() {
        return "draftkings";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
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
                    if (!pushed) {
                        aggregatorClient.reportUnchangedOdds(getBookmakerName(), cache.getExternalId());
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
        return DraftKingsLeague.resolveEventGroupId(cached.getLeagueName());
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isOptimisticLockException(Throwable e) {
        return pro.datawiki.igaming.source.core.utils.DatabaseErrorChecker.isOptimisticLockException(e);
    }
}
