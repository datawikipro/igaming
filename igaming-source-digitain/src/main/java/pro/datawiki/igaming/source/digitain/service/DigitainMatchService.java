package pro.datawiki.igaming.source.digitain.service;

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
import pro.datawiki.igaming.source.digitain.dto.DigitainMatchOddsData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DigitainMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final DigitainFeedClient feedClient;
    private final DigitainOddsMapper oddsMapper;
    private final DigitainDiscoveryService discoveryService;

    private static final List<Integer> STAKE_TYPES = List.of(1, 2, 3, 46, 702, 992);

    public DigitainMatchService(MatchCacheRepository matchCacheRepository,
                                SportCacheRepository sportCacheRepository,
                                ObjectMapper objectMapper,
                                SportNormalizationService sportNormalizationService,
                                MatchPersistenceService persistenceService,
                                AggregatorClient aggregatorClient,
                                DigitainFeedClient feedClient,
                                DigitainOddsMapper oddsMapper,
                                DigitainDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.feedClient = feedClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "digitain";
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
            List<DigitainMatchOddsData> oddsList = feedClient.fetchMatchesOdds(List.of(matchId), STAKE_TYPES);
            if (oddsList == null || oddsList.isEmpty()) {
                return false;
            }

            DigitainMatchOddsData oddsData = oddsList.get(0);
            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, oddsData);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Digitain event {}: {}", cache.getExternalId(), e.getMessage());
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

        Map<Long, MatchCache> cacheMap = new HashMap<>();
        List<Long> matchIds = new ArrayList<>();
        for (MatchCache mc : batch) {
            try {
                Long id = Long.parseLong(mc.getExternalId());
                matchIds.add(id);
                cacheMap.put(id, mc);
            } catch (Exception ignored) {}
        }

        if (matchIds.isEmpty()) {
            return 0;
        }

        try {
            List<DigitainMatchOddsData> oddsDataList = feedClient.fetchMatchesOdds(matchIds, STAKE_TYPES);
            int successCount = 0;

            for (DigitainMatchOddsData oddsData : oddsDataList) {
                MatchCache cache = cacheMap.get(oddsData.getMatchId());
                if (cache == null) continue;

                OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, oddsData);
                if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                    aggregatorClient.pushOddsUpdate(updateRequest);
                    matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
                    successCount++;
                }
            }

            return successCount;
        } catch (Exception e) {
            log.error("Batch loadMatchCards error: {}", e.getMessage());
            return 0;
        }
    }
}
