package pro.datawiki.igaming.source.favbet.service;

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
import pro.datawiki.igaming.source.favbet.dto.FavbetEvent;

import java.util.List;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final FavbetApiClient apiClient;
    private final FavbetOddsMapper oddsMapper;
    private final FavbetDiscoveryService discoveryService;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        FavbetApiClient apiClient,
                        FavbetOddsMapper oddsMapper,
                        FavbetDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "favbet";
    }

    public String getBookmakerFamily() {
        return "favbet";
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
            Long eventId = Long.parseLong(cache.getExternalId());
            FavbetEvent event = apiClient.getEventDetails(eventId);
            if (event == null) {
                return false;
            }

            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, event);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Favbet event {}: {}", cache.getExternalId(), e.getMessage());
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
        log.info("Favbet odds cycle finished. Successfully updated {}/{} matches", successCount, activeMatches.size());
    }
}
