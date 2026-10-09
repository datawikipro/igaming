package pro.datawiki.igaming.source.cloudbet.service;

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
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetFixtureDto;

import java.util.List;

@Service
@Slf4j
public class CloudbetMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final CloudbetApiClient apiClient;
    private final CloudbetOddsMapper oddsMapper;
    private final CloudbetDiscoveryService discoveryService;

    public CloudbetMatchService(MatchCacheRepository matchCacheRepository,
                                SportCacheRepository sportCacheRepository,
                                ObjectMapper objectMapper,
                                SportNormalizationService sportNormalizationService,
                                MatchPersistenceService persistenceService,
                                AggregatorClient aggregatorClient,
                                CloudbetApiClient apiClient,
                                CloudbetOddsMapper oddsMapper,
                                CloudbetDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "cloudbet";
    }

    @Override
    public String getBookmakerFamily() {
        return "cloudbet";
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
            CloudbetFixtureDto fixture = apiClient.getFixtureDetails(cache.getExternalId());
            if (fixture == null) {
                return false;
            }

            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, fixture);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Cloudbet event {}: {}", cache.getExternalId(), e.getMessage());
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
        log.info("Cloudbet odds cycle finished. Successfully updated {}/{} matches", successCount, activeMatches.size());
    }
}
