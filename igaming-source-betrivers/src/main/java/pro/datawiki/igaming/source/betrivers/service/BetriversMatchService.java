package pro.datawiki.igaming.source.betrivers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betrivers.config.BetriversConfig;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiBetOfferDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiBetOfferResponse;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiEventDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewItemDto;
import pro.datawiki.igaming.source.betrivers.dto.kambi.KambiListViewResponse;
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
public class BetriversMatchService extends AbstractBaseBookmakerService {

    private final BetriversApiClient apiClient;
    private final BetriversOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final BetriversConfig betriversConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${app.bookmaker.name:betrivers}")
    private String bookmakerName = "betrivers";

    public BetriversMatchService(MatchCacheRepository matchCacheRepository,
                                 SportCacheRepository sportCacheRepository,
                                 ObjectMapper objectMapper,
                                 SportNormalizationService sportNormalizationService,
                                 MatchPersistenceService persistenceService,
                                 BetriversApiClient apiClient,
                                 BetriversOddsMapper oddsMapper,
                                 AggregatorClient aggregatorClient,
                                 BetriversConfig betriversConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.betriversConfig = betriversConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "kambi";
    }

    public int scrapeAllSports() {
        log.info("Starting BetRivers/Kambi full line scraping across {} sports...", betriversConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : betriversConfig.getSports()) {
            try {
                KambiListViewResponse response = apiClient.getListView(sportSlug);
                if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
                    continue;
                }

                for (KambiListViewItemDto item : response.getEvents()) {
                    KambiEventDto event = item.getEvent();
                    if (event == null || event.getId() == null) {
                        continue;
                    }

                    try {
                        String sportName = event.getSport() != null ? event.getSport() : sportSlug;
                        String leagueName = event.getGroup() != null ? event.getGroup() : sportName;
                        SportType sportType = sportNormalizationService.normalize(sportName);

                        List<KambiBetOfferDto> betOffers = item.getBetOffers();

                        // If listView has no bet offers, try direct lookup for event
                        if (betOffers == null || betOffers.isEmpty()) {
                            KambiBetOfferResponse boResp = apiClient.getBetOffers(event.getId());
                            if (boResp != null) {
                                betOffers = boResp.getBetOffers();
                            }
                        }

                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                                event, betOffers, sportType, sportName, leagueName);

                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                            continue;
                        }
                        request.setBookmaker(bookmakerName);

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker(bookmakerName);
                        matchCache.setExternalId(String.valueOf(event.getId()));
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

                        String externalId = String.valueOf(event.getId());
                        String cachedHash = localStateHashCache.get(externalId);
                        if (cachedHash == null || !cachedHash.equals(currentHash)) {
                            aggregatorClient.pushOddsUpdate(request);
                            localStateHashCache.put(externalId, currentHash);
                            totalPushed++;
                        } else {
                            aggregatorClient.reportUnchangedOdds(bookmakerName, externalId);
                            totalUnchanged++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to map and push BetRivers event ID {}: {}", event.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape BetRivers sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("BetRivers line scraping completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getExternalId() == null) {
            return false;
        }

        try {
            Long eventId = Long.parseLong(cache.getExternalId());
            KambiBetOfferResponse boResp = apiClient.getBetOffers(eventId);
            if (boResp == null || boResp.getBetOffers() == null || boResp.getBetOffers().isEmpty()) {
                return false;
            }

            KambiEventDto event = new KambiEventDto();
            event.setId(eventId);
            event.setName(cache.getTeam1() + " vs " + cache.getTeam2());
            event.setHomeName(cache.getTeam1());
            event.setAwayName(cache.getTeam2());
            event.setSport(cache.getSportName());
            event.setGroup(cache.getLeagueName());
            event.setState(Boolean.TRUE.equals(cache.getIsLive()) ? "STARTED" : "NOT_STARTED");

            String sportName = cache.getSportName() != null ? cache.getSportName() : "General";
            String leagueName = cache.getLeagueName() != null ? cache.getLeagueName() : "League";
            SportType sportType = sportNormalizationService.normalize(sportName);

            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                    event, boResp.getBetOffers(), sportType, sportName, leagueName);

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
                aggregatorClient.reportUnchangedOdds("betrivers", cache.getExternalId());
            }

            return true;
        } catch (Exception e) {
            log.error("Error loading single match card for BetRivers {}: {}", cache.getExternalId(), e.getMessage());
            return false;
        }
    }
}
