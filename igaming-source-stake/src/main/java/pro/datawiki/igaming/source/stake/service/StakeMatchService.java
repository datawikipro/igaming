package pro.datawiki.igaming.source.stake.service;

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
import pro.datawiki.igaming.source.stake.dto.StakeFixtureDto;

import java.util.List;

@Service
@Slf4j
public class StakeMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final StakeApiClient apiClient;
    private final StakeOddsMapper oddsMapper;
    private final StakeDiscoveryService discoveryService;

    public StakeMatchService(MatchCacheRepository matchCacheRepository,
                             SportCacheRepository sportCacheRepository,
                             ObjectMapper objectMapper,
                             SportNormalizationService sportNormalizationService,
                             MatchPersistenceService persistenceService,
                             AggregatorClient aggregatorClient,
                             StakeApiClient apiClient,
                             StakeOddsMapper oddsMapper,
                             StakeDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "stake";
    }

    @Override
    public String getBookmakerFamily() {
        return "stake";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        return false;
    }

    public void fetchAndPushOdds() {
        try {
            List<StakeFixtureDto> fixtures = apiClient.fetchAllFixtures();
            if (fixtures == null || fixtures.isEmpty()) {
                return;
            }

            int successCount = 0;
            for (StakeFixtureDto fixture : fixtures) {
                if (fixture.getMarkets() != null && !fixture.getMarkets().isEmpty()) {
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(fixture);
                    if (request != null && request.getOdds() != null && !request.getOdds().isEmpty()) {
                        aggregatorClient.pushOddsUpdate(request);
                        successCount++;
                    }
                }
            }
            log.info("Stake odds cycle finished. Successfully pushed odds for {}/{} fixtures", successCount, fixtures.size());
        } catch (Exception e) {
            log.error("Error in Stake odds cycle: {}", e.getMessage());
        }
    }
}
