package pro.datawiki.igaming.source.tenbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechMatchService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

@Service
public class MatchService extends AbstractSBTechMatchService {

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        TenBetApiClient apiClient,
                        TenBetOddsMapper oddsMapper,
                        TenBetDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService,
                aggregatorClient, apiClient, oddsMapper, discoveryService);
    }

    @Override
    public String getBookmakerName() {
        return "10bet";
    }
}
