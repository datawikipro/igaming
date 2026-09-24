package pro.datawiki.igaming.source.wplay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.List;

@Slf4j
@Service
public class WplayMatchService extends AbstractBaseBookmakerService {

    @Value("${app.bookmaker.name:wplay}")
    private String bookmakerName;

    private final AggregatorClient aggregatorClient;
    private final WplayApiClient apiClient;
    private final WplayOddsMapper oddsMapper;
    private final WplayDiscoveryService discoveryService;

    public WplayMatchService(MatchCacheRepository matchCacheRepository,
                             SportCacheRepository sportCacheRepository,
                             ObjectMapper objectMapper,
                             SportNormalizationService sportNormalizationService,
                             MatchPersistenceService persistenceService,
                             AggregatorClient aggregatorClient,
                             WplayApiClient apiClient,
                             WplayOddsMapper oddsMapper,
                             WplayDiscoveryService discoveryService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
    }

    @Override
    public String getBookmakerFamily() {
        return "playtech";
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isBlank()) ? bookmakerName : "wplay";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getEventUrl() == null) {
            return false;
        }

        try {
            String html = apiClient.fetchHtml(cache.getEventUrl());
            if (html == null) {
                return false;
            }

            OddsUpdateRequest updateRequest = oddsMapper.mapHtmlToOddsUpdateRequest(cache, html);
            if (updateRequest != null && updateRequest.getOdds() != null && !updateRequest.getOdds().isEmpty()) {
                aggregatorClient.pushOddsUpdate(updateRequest);
                return true;
            }
        } catch (Exception e) {
            log.debug("Error loading match card for Wplay event {}: {}", cache.getExternalId(), e.getMessage());
        }
        return false;
    }

    public void fetchOddsForActiveMatches() {
        List<MatchCache> activeMatches = matchCacheRepository.findTop500ByOrderByUpdatedAtDesc();
        if (activeMatches == null || activeMatches.isEmpty()) {
            return;
        }

        int successCount = 0;
        // Fetch odds for top 20 active matches to avoid saturating wplay rate limits
        int limit = Math.min(20, activeMatches.size());
        for (int i = 0; i < limit; i++) {
            MatchCache match = activeMatches.get(i);
            if (loadSingleMatchCard(match)) {
                successCount++;
            }
        }
        log.info("Wplay odds cycle finished. Successfully updated {}/{} matches", successCount, limit);
    }
}
