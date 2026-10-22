package pro.datawiki.igaming.source.stoiximan.service;

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
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventsResponse;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.stoiximan.config.StoiximanConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchService extends AbstractBaseBookmakerService {

    private final AggregatorClient aggregatorClient;
    private final StoiximanApiClient apiClient;
    private final StoiximanOddsMapper oddsMapper;
    private final StoiximanDiscoveryService discoveryService;
    private final StoiximanConfig config;

    private final Map<Long, String> localStateHashCache = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private MatchService self;

    @org.springframework.beans.factory.annotation.Value("${app.bookmaker.event-url-prefix:https://www.stoiximan.gr/match/}")
    private String eventUrlPrefix = "https://www.stoiximan.gr/match/";

    public MatchService(MatchCacheRepository matchCacheRepository,
                        SportCacheRepository sportCacheRepository,
                        ObjectMapper objectMapper,
                        SportNormalizationService sportNormalizationService,
                        MatchPersistenceService persistenceService,
                        AggregatorClient aggregatorClient,
                        StoiximanApiClient apiClient,
                        StoiximanOddsMapper oddsMapper,
                        StoiximanDiscoveryService discoveryService,
                        StoiximanConfig config) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.aggregatorClient = aggregatorClient;
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.discoveryService = discoveryService;
        this.config = config;
    }

    @Override
    public String getBookmakerFamily() {
        return "stoiximan";
    }

    public void discoverEvents() {
        discoveryService.discoverEvents();
    }

    public int scrapeAllSports() {
        log.info("Starting Stoiximan/Kambi full line scraping across {} sports for bookmaker '{}'...", config.getSports().size(), getBookmakerName());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : config.getSports()) {
            try {
                Thread.sleep(250);
                KambiEventsResponse response = apiClient.getSportEvents(sportSlug);
                if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
                    continue;
                }

                for (KambiEventsResponse.KambiEventWrapper wrapper : response.getEvents()) {
                    KambiEvent event = wrapper.getEvent();
                    if (event == null || event.getId() == null) continue;

                    try {
                        String sportName = sportSlug.replace("_", " ");
                        String leagueName = event.getGroup() != null ? event.getGroup() : sportName;

                        String team1 = event.getHomeName();
                        String team2 = event.getAwayName();
                        if ((team1 == null || team2 == null) && event.getName() != null) {
                            if (event.getName().contains(" @ ")) {
                                String[] parts = event.getName().split(" @ ", 2);
                                team2 = parts[0].trim();
                                team1 = parts[1].trim();
                            } else {
                                String[] parts = event.getName().split(" - | vs ");
                                if (parts.length >= 2) {
                                    team1 = parts[0].trim();
                                    team2 = parts[1].trim();
                                }
                            }
                        }
                        if (team1 == null || team2 == null) continue;

                        long startMs = System.currentTimeMillis();
                        if (event.getStart() != null) {
                            try {
                                startMs = java.time.Instant.parse(event.getStart()).toEpochMilli();
                            } catch (Exception ignored) {}
                        }

                        String bkm = getBookmakerName() != null && !getBookmakerName().isBlank() ? getBookmakerName() : "stoiximan";
                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker(bkm);
                        matchCache.setExternalId(String.valueOf(event.getId()));
                        matchCache.setSportName(sportName);
                        matchCache.setLeagueName(leagueName);
                        matchCache.setTeam1(team1);
                        matchCache.setTeam2(team2);
                        matchCache.setStartTime(startMs);
                        matchCache.setIsLive("STARTED".equalsIgnoreCase(event.getState()));
                        matchCache.setEventUrl(eventUrlPrefix + event.getId());

                        List<KambiBetOffer> betOffers = wrapper.getBetOffers();
                        if (betOffers == null || betOffers.size() <= 4) {
                            try {
                                KambiEventDetailsResponse details = apiClient.getEventDetails(event.getId());
                                if (details != null && details.getBetoffers() != null && !details.getBetoffers().isEmpty()) {
                                    betOffers = details.getBetoffers();
                                }
                            } catch (Exception e) {
                                log.debug("Kambi event details enrichment failed for {}: {}", event.getId(), e.getMessage());
                            }
                        }
                        if (betOffers == null || betOffers.isEmpty()) {
                            try {
                                persistenceService.saveOrUpdateMatchMetadata(matchCache, "{}");
                            } catch (Exception ignored) {}
                            continue;
                        }

                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(matchCache, betOffers);
                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                            try {
                                persistenceService.saveOrUpdateMatchMetadata(matchCache, "{}");
                            } catch (Exception ignored) {}
                            continue;
                        }

                        String payload = serialize(request);
                        String hash = persistenceService.computeHash(payload);
                        request.setPayloadHash(hash);

                        Long eventId = event.getId();
                        String cachedHash = localStateHashCache.get(eventId);
                        if (cachedHash == null || !cachedHash.equals(hash)) {
                            aggregatorClient.pushOddsUpdate(request);
                            localStateHashCache.put(eventId, hash);
                            totalPushed++;
                        } else {
                            aggregatorClient.reportUnchangedOdds(bkm, String.valueOf(eventId));
                            totalUnchanged++;
                        }

                        try {
                            persistenceService.saveOrUpdateMatchMetadata(matchCache, payload);
                        } catch (Exception ignored) {}
                    } catch (Exception e) {
                        log.error("Failed to map/push Stoiximan event ID {}: {}", event.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape Stoiximan sport '{}': {}", sportSlug, e.getMessage());
            }
        }
        log.info("Stoiximan/Kambi line scraping completed for '{}': {} pushed, {} unchanged.", getBookmakerName(), totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        try {
            KambiEventDetailsResponse detailedEvent = apiClient.getEventDetails(Long.valueOf(cache.getExternalId()));
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
                log.debug("Optimistic locking conflict while processing Stoiximan match card for event {}: {}", cache.getExternalId(), e.getMessage());
            } else {
                log.error("Failed to load Stoiximan match card {}: {}", cache.getExternalId(), e.getMessage());
                try {
                    self.markAsFailed(cache);
                } catch (Exception ex) {
                    if (isOptimisticLockException(ex)) {
                        log.debug("Optimistic locking conflict while marking Stoiximan match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
                    } else {
                        log.error("Failed to mark Stoiximan match card {} as failed: {}", cache.getExternalId(), ex.getMessage());
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
