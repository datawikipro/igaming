package pro.datawiki.igaming.source.apuestatotal.service;

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
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalMatchOddsData;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ApuestatotalMatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final ApuestatotalFeedClient feedClient;
    private final ApuestatotalOddsMapper oddsMapper;
    private final ApuestatotalDiscoveryService discoveryService;

    public static final List<Integer> STAKE_TYPES = List.of(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 46,
            166, 167, 168, 187, 188, 189,
            702, 703, 704, 705, 740, 741, 742,
            992, 993
    );

    public ApuestatotalMatchService(MatchCacheRepository matchCacheRepository,
                                   SportCacheRepository sportCacheRepository,
                                   ObjectMapper objectMapper,
                                   SportNormalizationService sportNormalizationService,
                                   MatchPersistenceService persistenceService,
                                   AggregatorClient aggregatorClient,
                                   ApuestatotalFeedClient feedClient,
                                   ApuestatotalOddsMapper oddsMapper,
                                   ApuestatotalDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.feedClient = feedClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerName() {
        return "apuestatotal";
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
            List<ApuestatotalMatchOddsData> oddsList = feedClient.fetchMatchesOdds(List.of(matchId), STAKE_TYPES);
            if (oddsList == null || oddsList.isEmpty()) {
                return false;
            }

            ApuestatotalMatchOddsData oddsData = oddsList.get(0);
            OddsUpdateRequest updateRequest = oddsMapper.mapToOddsUpdateRequest(cache, oddsData);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Apuesta Total event {}: {}", cache.getExternalId(), e.getMessage());
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
            List<ApuestatotalMatchOddsData> oddsDataList = feedClient.fetchMatchesOdds(matchIds, STAKE_TYPES);
            int successCount = 0;

            for (ApuestatotalMatchOddsData oddsData : oddsDataList) {
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
            log.error("Failed to load match cards for Apuesta Total: {}", e.getMessage());
            return 0;
        }
    }
}
