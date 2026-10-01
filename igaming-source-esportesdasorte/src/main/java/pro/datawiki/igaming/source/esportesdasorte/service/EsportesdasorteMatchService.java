package pro.datawiki.igaming.source.esportesdasorte.service;

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
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteMatchOddsData;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class EsportesdasorteMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final EsportesdasorteFeedClient feedClient;
    private final EsportesdasorteOddsMapper oddsMapper;
    private final EsportesdasorteDiscoveryService discoveryService;

    public static final List<Integer> STAKE_TYPES = List.of(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 46,
            166, 167, 168, 187, 188, 189,
            702, 703, 704, 705, 740, 741, 742,
            992, 993
    );

    public EsportesdasorteMatchService(MatchCacheRepository matchCacheRepository,
                                       SportCacheRepository sportCacheRepository,
                                       ObjectMapper objectMapper,
                                       SportNormalizationService sportNormalizationService,
                                       MatchPersistenceService persistenceService,
                                       AggregatorClient aggregatorClient,
                                       EsportesdasorteFeedClient feedClient,
                                       EsportesdasorteOddsMapper oddsMapper,
                                       EsportesdasorteDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.feedClient = feedClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "esportesdasorte";
    }

    @Override
    public String getBookmakerFamily() {
        return "digitain";
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
            Long matchId = Long.parseLong(cache.getExternalId());
            List<EsportesdasorteMatchOddsData> oddsList = feedClient.fetchMatchesOdds(List.of(matchId), STAKE_TYPES);
            if (oddsList == null || oddsList.isEmpty()) {
                return false;
            }

            EsportesdasorteMatchOddsData oddsData = oddsList.get(0);
            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, oddsData);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Esportes da Sorte event {}: {}", cache.getExternalId(), e.getMessage());
        }

        return false;
    }

    @Override
    public int loadMatchCards(int batchSize) {
        List<MatchCache> matchesToLoad = matchCacheRepository.findTop500ByOrderByUpdatedAtDesc();
        if (matchesToLoad == null || matchesToLoad.isEmpty()) {
            return 0;
        }

        int limit = Math.min(batchSize, matchesToLoad.size());
        List<MatchCache> batch = matchesToLoad.subList(0, limit);

        int loadedCount = 0;
        for (MatchCache match : batch) {
            try {
                if (loadSingleMatchCard(match)) {
                    loadedCount++;
                }
            } catch (Exception e) {
                log.debug("Failed to load match card for {}: {}", match.getExternalId(), e.getMessage());
            }
        }

        return loadedCount;
    }
}
