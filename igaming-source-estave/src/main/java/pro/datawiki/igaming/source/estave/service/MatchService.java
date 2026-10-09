package pro.datawiki.igaming.source.estave.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.estave.dto.EstaveEventDto;

import java.util.List;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final EstaveApiClient apiClient;
    private final EstaveOddsMapper oddsMapper;
    private final EstaveDiscoveryService discoveryService;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        EstaveApiClient apiClient,
                        EstaveOddsMapper oddsMapper,
                        EstaveDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "estave";
    }

    public String getBookmakerFamily() {
        return "estave";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getExternalId() == null) {
            return false;
        }

        try {
            EstaveEventDto event = new EstaveEventDto();
            event.setId(cache.getExternalId());
            event.setHomeTeam(cache.getTeam1());
            event.setAwayTeam(cache.getTeam2());
            event.setIsLive(cache.getIsLive());

            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, event);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Estave event {}: {}", cache.getExternalId(), e.getMessage());
        }
        return false;
    }

    public void fetchOddsForActiveMatches() {
        List<MatchCache> activeMatches = matchCacheRepository.findTop500ByOrderByUpdatedAtDesc();
        if (activeMatches == null || activeMatches.isEmpty()) {
            return;
        }

        int successCount = 0;
        for (MatchCache match : activeMatches) {
            if (loadSingleMatchCard(match)) {
                successCount++;
            }
        }
        log.info("Estave odds cycle finished. Successfully updated {}/{} matches", successCount, activeMatches.size());
    }
}
