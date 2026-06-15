package pro.datawiki.igaming.source.sport888.service;

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
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEventDetailsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final Sport888ApiClient sport888ApiClient;
    private final Sport888OddsMapper oddsMapper;
    private final Sport888DiscoveryService discoveryService;

    private final Map<Long, String> localStateHashCache = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private MatchService self;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        Sport888ApiClient sport888ApiClient,
                        Sport888OddsMapper oddsMapper,
                        Sport888DiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.sport888ApiClient = sport888ApiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerFamily() {
        return "888sport";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        try {
            KambiEventDetailsResponse detailedEvent = sport888ApiClient.getEventDetails(Long.valueOf(cache.getExternalId()));
            if (detailedEvent != null && detailedEvent.getEvents() != null && !detailedEvent.getEvents().isEmpty()) {
                boolean pushed = self.processAndPush(detailedEvent, cache);
                if (!pushed) {
                    aggregatorClient.reportUnchangedOdds(getBookmakerName(), 1);
                }
                return true;
            } else {
                self.markAsFailed(cache);
                return false;
            }
        } catch (Exception e) {
            if (isOptimisticLockException(e)) {
                log.debug("Optimistic locking conflict while processing Sport888 match card for event {}: {}", cache.getExternalId(), e.getMessage());
            } else {
                log.error("Failed to load Sport888 match card {}: {}", cache.getExternalId(), e.getMessage());
                try {
                    self.markAsFailed(cache);
                } catch (Exception ex) {
                    if (isOptimisticLockException(ex)) {
                        log.debug("Optimistic locking conflict while marking Sport888 match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    } else {
                        log.error("Failed to mark Sport888 match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    }
                }
            }
            return false;
        }
    }

    @Transactional
    public boolean processAndPush(KambiEventDetailsResponse eventDetails, MatchCache cached) {
        String sportName = cached.getSportName() != null ? cached.getSportName() : "Unknown";
        String leagueName = cached.getLeagueName() != null ? cached.getLeagueName() : "Unknown";
        
        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(eventDetails, sportName, leagueName);
        if (request == null) return false;

        String currentHash = persistenceService.computeHash(serialize(request));
        Long eventId = eventDetails.getEvents().get(0).getId();

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
