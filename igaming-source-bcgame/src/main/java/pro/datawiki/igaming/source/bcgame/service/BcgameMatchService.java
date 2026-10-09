package pro.datawiki.igaming.source.bcgame.service;

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
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;

import java.util.List;

@Service
@Slf4j
public class BcgameMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final BcgameApiClient apiClient;
    private final BcgameOddsMapper oddsMapper;
    private final BcgameDiscoveryService discoveryService;

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
        return "bcgame";
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
            BcgameEventDto event = apiClient.getEventDetails(cache.getExternalId());
            if (event == null) {
                return false;
            }

            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, event);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for BC.Game event {}: {}", cache.getExternalId(), e.getMessage());
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
        log.info("BC.Game odds cycle finished. Successfully updated {}/{} matches", successCount, activeMatches.size());
    }
}
