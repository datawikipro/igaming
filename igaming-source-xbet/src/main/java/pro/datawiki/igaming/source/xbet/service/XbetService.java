package pro.datawiki.igaming.source.xbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.engine.xbet.service.AbstractXbetFamilyService;
import pro.datawiki.igaming.source.core.engine.xbet.service.XbetFamilyApiErrorTracker;
import pro.datawiki.igaming.source.core.engine.xbet.service.XbetFamilyEventDiscoverer;
import pro.datawiki.igaming.source.core.engine.xbet.service.XbetFamilyOddsProcessor;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

@Service
@Slf4j
public class XbetService extends AbstractXbetFamilyService {

    public XbetService(MatchCacheRepository matchCacheRepository,
                         SportCacheRepository sportCacheRepository,
                         ObjectMapper objectMapper,
                         SportNormalizationService sportNormalizationService,
                         MatchPersistenceService persistenceService,
                         XbetApiClient apiClient,
                         XbetFamilyEventDiscoverer eventDiscoverer,
                         XbetFamilyOddsProcessor oddsProcessor,
                         XbetFamilyApiErrorTracker errorTracker,
                         AggregatorClient aggregatorClient) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService,
                apiClient, eventDiscoverer, oddsProcessor, errorTracker, aggregatorClient);
    }

    @Override
    protected String resolveBaseUrl(String name) {
        if (name == null) return "https://1xbet.com";
        return switch (name.toLowerCase()) {
            case "megapari"   -> "https://megapari.com";
            case "linebet"    -> "https://linebet.com";
            case "888starz"   -> "https://888starz.bet";
            default           -> "https://1xbet.com";
        };
    }
}
