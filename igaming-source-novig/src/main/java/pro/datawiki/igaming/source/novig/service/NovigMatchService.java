package pro.datawiki.igaming.source.novig.service;

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
import pro.datawiki.igaming.source.novig.config.NovigConfig;
import pro.datawiki.igaming.source.novig.dto.NovigEventDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NovigMatchService extends AbstractBaseBookmakerService {

    private final NovigApiClient apiClient;
    private final NovigOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final NovigConfig novigConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public NovigMatchService(MatchCacheRepository matchCacheRepository,
                            SportCacheRepository sportCacheRepository,
                            ObjectMapper objectMapper,
                            SportNormalizationService sportNormalizationService,
                            MatchPersistenceService persistenceService,
                            NovigApiClient apiClient,
                            NovigOddsMapper oddsMapper,
                            AggregatorClient aggregatorClient,
                            NovigConfig novigConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.novigConfig = novigConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "novig";
    }

    @Scheduled(fixedDelayString = "${novig.scheduler.poll-interval-ms:30000}", initialDelay = 5000)
    public void scheduledScrape() {
        try {
            scrapeUpcomingLines();
        } catch (Exception e) {
            log.error("Error during scheduled Novig scrape: {}", e.getMessage(), e);
        }
    }

    public int scrapeUpcomingLines() {
        log.info("Starting Novig Zero-Commission Exchange line ingestion...");
        int totalPushed = 0;
        int totalUnchanged = 0;

        try {
            List<NovigEventDto> events = apiClient.fetchUpcomingEvents(Instant.now(), novigConfig.getApi().getBatchSize());
            if (events == null || events.isEmpty()) {
                log.info("No active events returned from Novig GraphQL API");
                return 0;
            }

            for (NovigEventDto event : events) {
                if (event == null || event.getId() == null) {
                    continue;
                }

                try {
                    OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event);
                    if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                        continue;
                    }

                    MatchCache matchCache = new MatchCache();
                    matchCache.setBookmaker("novig");
                    matchCache.setExternalId(event.getId());
                    matchCache.setSportName(request.getSportName());
                    matchCache.setLeagueName(request.getLeagueName());
                    matchCache.setTeam1(request.getTeam1());
                    matchCache.setTeam2(request.getTeam2());
                    matchCache.setIsLive(request.getIsLive());
                    matchCache.setStartTime(request.getStartTime());
                    matchCache.setEventUrl("https://app.novig.us/event/" + event.getId());

                    String serializedPayload = objectMapper.writeValueAsString(request);
                    String currentHash = persistenceService.computeHash(serializedPayload);
                    request.setPayloadHash(currentHash);

                    persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);

                    String externalId = event.getId();
                    String cachedHash = localStateHashCache.get(externalId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(externalId, currentHash);
                        totalPushed++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("novig", externalId);
                        totalUnchanged++;
                    }
                } catch (Exception e) {
                    log.debug("Failed to ingest Novig event {}: {}", event.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to scrape Novig: {}", e.getMessage(), e);
        }

        log.info("Novig line ingestion completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
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
