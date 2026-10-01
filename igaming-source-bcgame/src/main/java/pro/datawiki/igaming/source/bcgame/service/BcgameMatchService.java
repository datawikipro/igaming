package pro.datawiki.igaming.source.bcgame.service;

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
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BcgameMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final BcgameApiClient apiClient;
    private final BcgameOddsMapper oddsMapper;
    private final BcgameDiscoveryService discoveryService;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private BcgameMatchService self;

    public BcgameMatchService(MatchCacheRepository matchCacheRepository,
                              SportCacheRepository sportCacheRepository,
                              ObjectMapper objectMapper,
                              SportNormalizationService sportNormalizationService,
                              MatchPersistenceService persistenceService,
                              AggregatorClient aggregatorClient,
                              BcgameApiClient apiClient,
                              BcgameOddsMapper oddsMapper,
                              BcgameDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "bcgame";
    }

    @Override
    public String getBookmakerFamily() {
        return "crypto";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    public void fetchOddsForActiveMatches() {
        loadMatchCards(500);
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        try {
            BcgameEventDto eventDetails = apiClient.getEventDetails(cache.getExternalId());
            if (eventDetails != null && eventDetails.getMarkets() != null && !eventDetails.getMarkets().isEmpty()) {
                boolean pushed = self.processAndPush(eventDetails, cache);
                if (!pushed) {
                    aggregatorClient.reportUnchangedOdds(getBookmakerName(), cache.getExternalId());
                }
                return true;
            } else {
                self.markAsFailed(cache);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to load BC.Game match card {}: {}", cache.getExternalId(), e.getMessage());
            try {
                self.markAsFailed(cache);
            } catch (Exception ignored) {}
            return false;
        }
    }

    @Transactional
    public boolean processAndPush(BcgameEventDto eventDetails, MatchCache cached) {
        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(cached, eventDetails);
        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
            return false;
        }

        String currentHash = persistenceService.computeHash(serialize(request));
        String eventId = eventDetails.getId();

        boolean pushed = false;
        if (!currentHash.equals(localStateHashCache.get(eventId))) {
            aggregatorClient.pushOddsUpdate(request);
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
}
