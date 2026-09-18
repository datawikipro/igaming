package pro.datawiki.igaming.source.bovada.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bovada.config.BovadaConfig;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventGroupDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaPathDto;
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
public class BovadaMatchService extends AbstractBaseBookmakerService {

    private final BovadaApiClient apiClient;
    private final BovadaOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final BovadaConfig bovadaConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public BovadaMatchService(MatchCacheRepository matchCacheRepository,
                              SportCacheRepository sportCacheRepository,
                              ObjectMapper objectMapper,
                              SportNormalizationService sportNormalizationService,
                              MatchPersistenceService persistenceService,
                              BovadaApiClient apiClient,
                              BovadaOddsMapper oddsMapper,
                              AggregatorClient aggregatorClient,
                              BovadaConfig bovadaConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.bovadaConfig = bovadaConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "bovada";
    }

    public int scrapeAllSports() {
        log.info("Starting Bovada full line scraping across {} sports...", bovadaConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : bovadaConfig.getSports()) {
            try {
                List<BovadaEventGroupDto> groups = apiClient.getSportEvents(sportSlug);
                if (groups == null || groups.isEmpty()) continue;

                for (BovadaEventGroupDto group : groups) {
                    if (group.getEvents() == null || group.getEvents().isEmpty()) continue;

                    String sportName = extractSportName(group.getPath(), sportSlug);
                    String leagueName = extractLeagueName(group.getPath(), sportSlug);
                    SportType sportType = sportNormalizationService.normalize(sportName);

                    for (BovadaEventDto event : group.getEvents()) {
                        if (event.getId() == null) continue;

                        try {
                            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                                    event, group.getPath(), sportType, sportName, leagueName);

                            if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                                continue;
                            }

                            // Save or update match cache
                            MatchCache matchCache = new MatchCache();
                            matchCache.setBookmaker("bovada");
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

                            // Verify if odds have changed
                            String cachedHash = localStateHashCache.get(event.getId());
                            if (cachedHash == null || !cachedHash.equals(currentHash)) {
                                aggregatorClient.pushOddsUpdate(request);
                                localStateHashCache.put(event.getId(), currentHash);
                                totalPushed++;
                            } else {
                                aggregatorClient.reportUnchangedOdds("bovada", event.getId());
                                totalUnchanged++;
                            }
                        } catch (Exception e) {
                            log.error("Failed to map and push Bovada event ID {}: {}", event.getId(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape Bovada sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("Bovada full line scraping completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getJsonPayload() == null) {
            return false;
        }

        try {
            BovadaEventDto event = objectMapper.readValue(cache.getJsonPayload(), BovadaEventDto.class);
            if (event == null) return false;

            String sportName = cache.getSportName() != null ? cache.getSportName() : "General";
            String leagueName = cache.getLeagueName() != null ? cache.getLeagueName() : "League";
            SportType sportType = sportNormalizationService.normalize(sportName);

            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, null, sportType, sportName, leagueName);
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
                aggregatorClient.reportUnchangedOdds("bovada", cache.getExternalId());
            }
            return true;
        } catch (Exception e) {
            log.error("Error loading single match card for Bovada {}: {}", cache.getExternalId(), e.getMessage());
            return false;
        }
    }

    private String extractSportName(List<BovadaPathDto> paths, String fallback) {
        if (paths != null) {
            for (BovadaPathDto p : paths) {
                if ("SPORT".equalsIgnoreCase(p.getType()) && p.getDescription() != null) {
                    return p.getDescription();
                }
            }
        }
        return Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
    }

    private String extractLeagueName(List<BovadaPathDto> paths, String fallback) {
        if (paths != null) {
            for (BovadaPathDto p : paths) {
                if ("LEAGUE".equalsIgnoreCase(p.getType()) && p.getDescription() != null) {
                    return p.getDescription();
                }
            }
        }
        return "General " + extractSportName(paths, fallback);
    }
}
