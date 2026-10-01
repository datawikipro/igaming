package pro.datawiki.igaming.source.tenbet.service;

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
import pro.datawiki.igaming.source.tenbet.config.TenBetConfig;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TenBetMatchService extends AbstractBaseBookmakerService {

    private final TenBetApiClient apiClient;
    private final TenBetOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final TenBetConfig tenBetConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public TenBetMatchService(MatchCacheRepository matchCacheRepository,
                              SportCacheRepository sportCacheRepository,
                              ObjectMapper objectMapper,
                              SportNormalizationService sportNormalizationService,
                              MatchPersistenceService persistenceService,
                              TenBetApiClient apiClient,
                              TenBetOddsMapper oddsMapper,
                              AggregatorClient aggregatorClient,
                              TenBetConfig tenBetConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.bookmakerName = "10bet";
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.tenBetConfig = tenBetConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "10bet";
    }

    public int scrapeAllSports() {
        log.info("Starting 10bet line scraping across {} sports...", tenBetConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : tenBetConfig.getSports()) {
            try {
                List<TenBetEventDto> events = apiClient.getEventsBySport(sportSlug);
                if (events == null || events.isEmpty()) continue;

                for (TenBetEventDto event : events) {
                    if (event.getId() == null) continue;

                    try {
                        String sportName = event.getSportName() != null ? event.getSportName() : sportSlug;
                        String leagueName = event.getLeagueName() != null ? event.getLeagueName() : "General";

                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event);
                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                            continue;
                        }

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker("10bet");
                        matchCache.setExternalId(event.getId());
                        matchCache.setSportName(sportName);
                        matchCache.setLeagueName(leagueName);
                        matchCache.setTeam1(request.getTeam1());
                        matchCache.setTeam2(request.getTeam2());
                        matchCache.setIsLive(request.getIsLive());
                        matchCache.setStartTime(request.getStartTime());
                        matchCache.setEventUrl(request.getEventUrl());

                        String serializedPayload = objectMapper.writeValueAsString(request);
                        String currentHash = persistenceService.computeHash(serializedPayload);
                        request.setPayloadHash(currentHash);

                        persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);

                        String cachedHash = localStateHashCache.get(event.getId());
                        if (cachedHash == null || !cachedHash.equals(currentHash)) {
                            aggregatorClient.pushOddsUpdate(request);
                            localStateHashCache.put(event.getId(), currentHash);
                            totalPushed++;
                        } else {
                            aggregatorClient.reportUnchangedOdds("10bet", event.getId());
                            totalUnchanged++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to map and push 10bet event ID {}: {}", event.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape 10bet sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("10bet full line scraping completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getJsonPayload() == null) {
            return false;
        }

        try {
            TenBetEventDto event = objectMapper.readValue(cache.getJsonPayload(), TenBetEventDto.class);
            if (event == null) return false;

            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event);
            if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                return false;
            }

            String serialized = objectMapper.writeValueAsString(request);
            String hash = persistenceService.computeHash(serialized);
            request.setPayloadHash(hash);

            String cachedHash = localStateHashCache.get(cache.getExternalId());
            if (cachedHash == null || !cachedHash.equals(hash)) {
                aggregatorClient.pushOddsUpdate(request);
                localStateHashCache.put(cache.getExternalId(), hash);
            } else {
                aggregatorClient.reportUnchangedOdds("10bet", cache.getExternalId());
            }
            return true;
        } catch (Exception e) {
            log.warn("Warning loading single match card for 10bet {}: {}", cache.getExternalId(), e.getMessage());
            return false;
        }
    }
}
