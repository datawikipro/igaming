package pro.datawiki.igaming.source.bet7k.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.bet7k.config.Bet7kConfig;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class Bet7kMatchService extends AbstractBaseBookmakerService {

    private final Bet7kApiClient apiClient;
    private final Bet7kOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final Bet7kConfig bet7kConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public Bet7kMatchService(MatchCacheRepository matchCacheRepository,
                             SportCacheRepository sportCacheRepository,
                             ObjectMapper objectMapper,
                             SportNormalizationService sportNormalizationService,
                             MatchPersistenceService persistenceService,
                             Bet7kApiClient apiClient,
                             Bet7kOddsMapper oddsMapper,
                             AggregatorClient aggregatorClient,
                             Bet7kConfig bet7kConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.bookmakerName = "bet7k";
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.bet7kConfig = bet7kConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "bet7k";
    }

    public int scrapeAllSports() {
        log.info("Starting Bet7k line scraping across {} sports...", bet7kConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : bet7kConfig.getSports()) {
            try {
                List<Bet7kEventDto> events = apiClient.getEventsBySport(sportSlug);
                if (events == null || events.isEmpty()) continue;

                for (Bet7kEventDto event : events) {
                    if (event.getId() == null) continue;

                    try {
                        String sportName = event.getSportName() != null ? event.getSportName() : sportSlug;
                        String leagueName = event.getLeagueName() != null ? event.getLeagueName() : "General";

                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event);
                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                            continue;
                        }

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker("bet7k");
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
                            aggregatorClient.reportUnchangedOdds("bet7k", event.getId());
                            totalUnchanged++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to map and push Bet7k event ID {}: {}", event.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape Bet7k sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("Bet7k full line scraping completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getJsonPayload() == null) {
            return false;
        }

        try {
            Bet7kEventDto event = objectMapper.readValue(cache.getJsonPayload(), Bet7kEventDto.class);
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
                aggregatorClient.reportUnchangedOdds("bet7k", cache.getExternalId());
            }
            return true;
        } catch (Exception e) {
            log.error("Error loading single match card for Bet7k {}: {}", cache.getExternalId(), e.getMessage());
            return false;
        }
    }
}
