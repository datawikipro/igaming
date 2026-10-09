package pro.datawiki.igaming.source.matchbook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.matchbook.config.MatchbookConfig;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchbookMatchService extends AbstractBaseBookmakerService {

    private final MatchbookApiClient apiClient;
    private final MatchbookOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final MatchbookConfig matchbookConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public MatchbookMatchService(MatchCacheRepository matchCacheRepository,
                                 SportCacheRepository sportCacheRepository,
                                 ObjectMapper objectMapper,
                                 SportNormalizationService sportNormalizationService,
                                 MatchPersistenceService persistenceService,
                                 MatchbookApiClient apiClient,
                                 MatchbookOddsMapper oddsMapper,
                                 AggregatorClient aggregatorClient,
                                 MatchbookConfig matchbookConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.matchbookConfig = matchbookConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "matchbook";
    }

    @Scheduled(fixedDelayString = "${matchbook.scheduler.poll-interval-ms:30000}", initialDelay = 5000)
    public void scheduledScrape() {
        try {
            scrapeAllEvents();
        } catch (Exception e) {
            log.error("Error during scheduled Matchbook scrape: {}", e.getMessage(), e);
        }
    }

    public int scrapeAllEvents() {
        log.info("Starting Matchbook Betting Exchange line ingestion...");
        int totalPushed = 0;
        int totalUnchanged = 0;
        int offset = 0;
        int perPage = matchbookConfig.getApi().getBatchSize() > 0 ? matchbookConfig.getApi().getBatchSize() : 50;
        int maxEvents = 1000;
        int fetchedCount = 0;

        while (fetchedCount < maxEvents) {
            List<MatchbookEvent> events = apiClient.fetchEvents(offset, perPage);
            if (events == null || events.isEmpty()) {
                break;
            }

            for (MatchbookEvent event : events) {
                if (event == null || event.getId() == null) {
                    continue;
                }

                try {
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event);
                    if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                        continue;
                    }

                    MatchCache matchCache = new MatchCache();
                    matchCache.setBookmaker("matchbook");
                    matchCache.setExternalId(String.valueOf(event.getId()));
                    matchCache.setSportName(request.getSportName());
                    matchCache.setLeagueName(request.getLeagueName());
                    matchCache.setTeam1(request.getTeam1());
                    matchCache.setTeam2(request.getTeam2());
                    matchCache.setIsLive(request.getIsLive());
                    matchCache.setStartTime(request.getStartTime());
                    matchCache.setEventUrl("https://www.matchbook.com/events/" + event.getId());

                    String serializedPayload = objectMapper.writeValueAsString(request);
                    String currentHash = persistenceService.computeHash(serializedPayload);
                    request.setPayloadHash(currentHash);

                    persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);

                    String externalId = String.valueOf(event.getId());
                    String cachedHash = localStateHashCache.get(externalId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(externalId, currentHash);
                        totalPushed++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("matchbook", externalId);
                        totalUnchanged++;
                    }
                } catch (Exception e) {
                    log.debug("Failed to ingest Matchbook event {}: {}", event.getId(), e.getMessage());
                }
            }

            fetchedCount += events.size();
            offset += events.size();

            if (events.size() < perPage) {
                break;
            }
        }

        log.info("Matchbook ingestion completed: {} pushed updates, {} unchanged (total events scanned: {}).",
                totalPushed, totalUnchanged, fetchedCount);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getExternalId() == null) {
            return false;
        }
        return true;
    }
}
