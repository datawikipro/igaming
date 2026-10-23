package pro.datawiki.igaming.source.apuestatotal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarMatchService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

@Service
public class MatchService extends AbstractAltenarMatchService {

    @Value("${app.bookmaker.name:apuestatotal}")
    private String bookmakerName;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        ApuestaTotalApiClient apiClient,
                        ApuestaTotalOddsMapper oddsMapper,
                        ApuestaTotalDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService,
                aggregatorClient, apiClient, oddsMapper, discoveryService);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isEmpty()) ? bookmakerName : "apuestatotal";
    }
}