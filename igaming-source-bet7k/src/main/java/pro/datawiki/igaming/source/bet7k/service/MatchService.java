package pro.datawiki.igaming.source.bet7k.service;

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

    @Value("${app.bookmaker.name:bet7k}")
    private String bookmakerName;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        Bet7kApiClient apiClient,
                        Bet7kOddsMapper oddsMapper,
                        Bet7kDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService,
                aggregatorClient, apiClient, oddsMapper, discoveryService);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isBlank()) ? bookmakerName : "bet7k";
    }
}