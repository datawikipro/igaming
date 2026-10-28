package pro.datawiki.igaming.source.betb2b.service;

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
public class Betb2bService extends AbstractXbetFamilyService {

    public Betb2bService(MatchCacheRepository matchCacheRepository,
                         SportCacheRepository sportCacheRepository,
                         ObjectMapper objectMapper,
                         SportNormalizationService sportNormalizationService,
                         MatchPersistenceService persistenceService,
                         Betb2bApiClient apiClient,
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
            case "melbet", "melbet-ru", "melbet.ru" -> "https://melbet.ru";
            case "melbet-com", "melbet.com"          -> "https://melbet.com";
            case "megapari", "megapari.com"          -> "https://megapari.com";
            case "linebet", "linebet.com"            -> "https://linebet.com";
            case "betandyou", "betandyou.com"        -> "https://betandyou.com";
            case "fansport", "fan-sport.com", "fansport.com" -> "https://fan-sport.com";
            case "888starz", "888starz.bet"          -> "https://888starz.bet";
            case "betwinner", "betwinner.com"        -> "https://betwinner.com";
            case "1xbit", "1xbit.com"                -> "https://1xbit.com";
            case "22bet", "22bet.com"                -> "https://22bet.com";
            case "betlabel", "betlabel.com"          -> "https://betlabel.com";
            case "betobet", "betobet.com"            -> "https://betobet.com";
            case "mostbet", "mostbet.com"            -> "https://mostbet.com";
            case "sapphirebet", "sapphirebet.com"    -> "https://sapphirebet.com";
            case "spinbetter", "spinbetter.com"      -> "https://spinbetter.com";
            case "jvspin", "jvspin.com"              -> "https://jvspin.com";
            case "helabet", "helabet.com"            -> "https://helabet.com";
            case "paripesa", "paripesa.com"          -> "https://paripesa.com";
            case "1xbet-kz", "1xbet.kz"              -> "https://1xbet.kz";
            case "1xbet-ru", "1xbet.ru"              -> "https://melbet.ru";
            case "1xbet-com", "1xbet.com"            -> "https://1xbet.com";
            default                                  -> "https://1xbet.com";
        };
    }
}
