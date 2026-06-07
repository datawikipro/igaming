package pro.datawiki.igaming.source.bet365.service;

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
import pro.datawiki.igaming.source.bet365.service.Bet365ApiClient.Bet365OddsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final Bet365ApiClient bet365ApiClient;
    private final Bet365OddsMapper oddsMapper;
    private final Bet365DiscoveryService discoveryService;

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
                        Bet365ApiClient bet365ApiClient,
                        Bet365OddsMapper oddsMapper,
                        Bet365DiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.bet365ApiClient = bet365ApiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    public String getBookmakerFamily() {
        return "bet365";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        try {
            Bet365OddsResponse detailedOdds = bet365ApiClient.getEventOdds(cache.getExternalId());
            if (detailedOdds == null || detailedOdds.getOdds() == null || detailedOdds.getOdds().isEmpty()) {
                String jsonPayload = cache.getJsonPayload();
                if (jsonPayload != null && !jsonPayload.isEmpty() && !jsonPayload.contains("|")) {
                    try {
                        List<Bet365ApiClient.Bet365Odd> odds = objectMapper.readValue(
                                jsonPayload,
                                new com.fasterxml.jackson.core.type.TypeReference<List<Bet365ApiClient.Bet365Odd>>() {}
                        );
                        if (odds != null && !odds.isEmpty()) {
                            detailedOdds = new Bet365OddsResponse();
                            detailedOdds.setEventId(cache.getExternalId());
                            detailedOdds.setOdds(odds);
                        }
                    } catch (Exception ex) {
                        log.warn("Failed to deserialize odds from jsonPayload for match {}: {}", cache.getExternalId(), ex.getMessage());
                    }
                }
            }

            if (detailedOdds != null && detailedOdds.getOdds() != null && !detailedOdds.getOdds().isEmpty()) {
                boolean pushed = self.processAndPush(detailedOdds, cache);
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
                log.debug("Optimistic locking conflict while processing Bet365 match card for event {}: {}", cache.getExternalId(), e.getMessage());
            } else {
                log.error("Failed to load Bet365 match card {}: {}", cache.getExternalId(), e.getMessage());
                try {
                    self.markAsFailed(cache);
                } catch (Exception ex) {
                    if (isOptimisticLockException(ex)) {
                        log.debug("Optimistic locking conflict while marking Bet365 match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    } else {
                        log.error("Failed to mark Bet365 match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    }
                }
            }
            return false;
        }
    }

    @Transactional
    public boolean processAndPush(Bet365OddsResponse oddsResponse, MatchCache cached) {
        String sportName = cached.getSportName() != null ? cached.getSportName() : "Unknown";
        String leagueName = cached.getLeagueName() != null ? cached.getLeagueName() : "Unknown";
        
        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(oddsResponse, cached, sportName, leagueName);
        if (request == null) return false;

        String currentHash = persistenceService.computeHash(serialize(request));
        String eventId = oddsResponse.getEventId();

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
