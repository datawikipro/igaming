package pro.datawiki.igaming.source.unibet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final UnibetApiClient unibetApiClient;
    private final UnibetOddsMapper oddsMapper;
    private final UnibetDiscoveryService discoveryService;

    private final Map<Long, String> localStateHashCache = new ConcurrentHashMap<>();

    private final pro.datawiki.igaming.source.unibet.config.UnibetConfig unibetConfig;

    @Autowired
    @Lazy
    private MatchService self;

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        UnibetApiClient unibetApiClient,
                        UnibetOddsMapper oddsMapper,
                        UnibetDiscoveryService discoveryService,
                        pro.datawiki.igaming.source.unibet.config.UnibetConfig unibetConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.unibetApiClient = unibetApiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
        this.unibetConfig = unibetConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "unibet";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    public int scrapeAllSports() {
        log.info("Starting Unibet full line scraping across {} sports...", unibetConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : unibetConfig.getSports()) {
            try {
                KambiEventsResponse response = unibetApiClient.getSportEvents(sportSlug);
                if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
                    continue;
                }

                for (KambiEventsResponse.KambiEventWrapper wrapper : response.getEvents()) {
                    KambiEvent event = wrapper.getEvent();
                    if (event == null || event.getId() == null) continue;

                    try {
                        List<KambiBetOffer> betOffers = wrapper.getBetOffers();
                        if (betOffers == null || betOffers.isEmpty()) {
                            KambiEventDetailsResponse details = unibetApiClient.getEventDetails(event.getId());
                            if (details != null && details.getBetoffers() != null) {
                                betOffers = details.getBetoffers();
                            }
                        }

                        if (betOffers == null || betOffers.isEmpty()) continue;

                        String sportName = event.getPath() != null && !event.getPath().isEmpty() ? event.getPath().get(0).getName() : sportSlug;
                        String leagueName = event.getGroup() != null ? event.getGroup() : sportName;

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker(getBookmakerName() != null ? getBookmakerName() : "unibet");
                        matchCache.setExternalId(String.valueOf(event.getId()));
                        matchCache.setSportName(sportName);
                        matchCache.setLeagueName(leagueName);
                        matchCache.setTeam1(event.getHomeName());
                        matchCache.setTeam2(event.getAwayName());
                        matchCache.setIsLive("STARTED".equalsIgnoreCase(event.getState()));
                        matchCache.setEventUrl("https://www.unibet.com/betting/sports/event/" + event.getId());

                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(matchCache, betOffers);
                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) continue;

                        String payload = serialize(request);
                        String hash = persistenceService.computeHash(payload);
                        request.setPayloadHash(hash);

                        persistenceService.saveOrUpdateMatchMetadata(matchCache, payload);

                        Long eventId = event.getId();
                        String cachedHash = localStateHashCache.get(eventId);
                        if (cachedHash == null || !cachedHash.equals(hash)) {
                            aggregatorClient.pushOddsUpdate(request);
                            localStateHashCache.put(eventId, hash);
                            totalPushed++;
                        } else {
                            aggregatorClient.reportUnchangedOdds(getBookmakerName() != null ? getBookmakerName() : "unibet", String.valueOf(eventId));
                            totalUnchanged++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to map/push Unibet event ID {}: {}", event.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape Unibet sport '{}': {}", sportSlug, e.getMessage());
            }
        }
        log.info("Unibet line scraping completed: {} pushed, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        try {
            KambiEventDetailsResponse detailedEvent = unibetApiClient.getEventDetails(Long.valueOf(cache.getExternalId()));
            if (detailedEvent != null && detailedEvent.getBetoffers() != null && !detailedEvent.getBetoffers().isEmpty()) {
                boolean pushed = self.processAndPush(detailedEvent, cache);
                if (!pushed) {
                    aggregatorClient.reportUnchangedOdds(getBookmakerName(), cache.getExternalId());
                }
                return true;
            } else {
                self.markAsFailed(cache);
                return false;
            }
        } catch (Exception e) {
            if (isOptimisticLockException(e)) {
                log.debug("Optimistic locking conflict while processing Unibet match card for event {}: {}", cache.getExternalId(), e.getMessage());
            } else {
                log.error("Failed to load Unibet match card {}: {}", cache.getExternalId(), e.getMessage());
                try {
                    self.markAsFailed(cache);
                } catch (Exception ex) {
                    if (isOptimisticLockException(ex)) {
                        log.debug("Optimistic locking conflict while marking Unibet match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    } else {
                        log.error("Failed to mark Unibet match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    }
                }
            }
            return false;
        }
    }

    @Transactional
    public boolean processAndPush(KambiEventDetailsResponse eventDetails, MatchCache cached) {
        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(cached, eventDetails.getBetoffers());
        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) return false;

        String currentHash = persistenceService.computeHash(serialize(request));
        Long eventId = Long.valueOf(cached.getExternalId());

        boolean pushed = false;
        if (!currentHash.equals(localStateHashCache.get(eventId))) {
            if (request.getTeam1() == null && request.getTeam2() == null) {
                aggregatorClient.pushOutrightUpdate(request);
            } else {
                aggregatorClient.pushOddsUpdate(request);
            }
            localStateHashCache.put(eventId, currentHash);
            pushed = true;
        }

        matchCacheRepository.findById(cached.getId()).ifPresent(freshCache -> {
            freshCache.setUpdatedAt(LocalDateTime.now());
            freshCache.setStatus(MatchCache.Status.PROCESSED);
            matchCacheRepository.save(freshCache);
        });
        return pushed;
    }

    @Transactional
    public void markAsFailed(MatchCache cached) {
        matchCacheRepository.findById(cached.getId()).ifPresent(freshCache -> {
            freshCache.setStatus(MatchCache.Status.FAILED);
            freshCache.setUpdatedAt(LocalDateTime.now());
            matchCacheRepository.save(freshCache);
        });
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isOptimisticLockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.contains("OptimisticLockingFailureException") 
                    || name.contains("OptimisticLockException") 
                    || name.contains("StaleObjectStateException") 
                    || (cause.getMessage() != null && cause.getMessage().contains("Row was updated or deleted by another transaction"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
