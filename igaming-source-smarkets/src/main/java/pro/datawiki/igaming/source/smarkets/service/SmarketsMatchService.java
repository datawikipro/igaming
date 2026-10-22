package pro.datawiki.igaming.source.smarkets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.smarkets.config.SmarketsConfig;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SmarketsMatchService extends AbstractBaseBookmakerService {

    private final SmarketsApiClient apiClient;
    private final SmarketsOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final SmarketsConfig smarketsConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger enrichmentOffset = new java.util.concurrent.atomic.AtomicInteger(0);

    public SmarketsMatchService(MatchCacheRepository matchCacheRepository,
                               SportCacheRepository sportCacheRepository,
                               ObjectMapper objectMapper,
                               SportNormalizationService sportNormalizationService,
                               MatchPersistenceService persistenceService,
                               SmarketsApiClient apiClient,
                               SmarketsOddsMapper oddsMapper,
                               AggregatorClient aggregatorClient,
                               SmarketsConfig smarketsConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.smarketsConfig = smarketsConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "smarkets";
    }

    @Scheduled(fixedDelayString = "${smarkets.scheduler.poll-interval-ms:30000}", initialDelay = 5000)
    public void scheduledScrape() {
        try {
            scrapeAllSports();
        } catch (Exception e) {
            log.error("Error during scheduled Smarkets scrape: {}", e.getMessage(), e);
        }
    }

    public int scrapeAllSports() {
        log.info("Starting Smarkets Betting Exchange orderbook ingestion across {} sports...", smarketsConfig.getSports().size());
        int totalSaved = 0;
        int totalPushed = 0;
        int totalUnchanged = 0;

        List<SmarketsEvent> allEvents = new ArrayList<>();
        Map<String, String> eventToSport = new HashMap<>();

        // Phase 1: Fast metadata ingestion across all sports (Golden Rule #8: >= 500 threshold)
        for (String sportSlug : smarketsConfig.getSports()) {
            try {
                int limit = smarketsConfig.getApi().getEventsPerSportLimit();
                List<SmarketsEvent> sportEvents = new ArrayList<>();
                List<SmarketsEvent> events = apiClient.getUpcomingEvents(sportSlug, limit);
                if (events != null && !events.isEmpty()) {
                    log.info("Fetched {} upcoming events for Smarkets sport '{}'", events.size(), sportSlug);
                    sportEvents.addAll(events);
                }
                List<SmarketsEvent> liveEvents = apiClient.getLiveEvents(sportSlug, limit);
                if (liveEvents != null && !liveEvents.isEmpty()) {
                    log.info("Fetched {} live events for Smarkets sport '{}'", liveEvents.size(), sportSlug);
                    sportEvents.addAll(liveEvents);
                }
                if (sportEvents.isEmpty()) {
                    continue;
                }
                allEvents.addAll(sportEvents);

                for (SmarketsEvent event : sportEvents) {
                    if (event == null || event.getId() == null) {
                        continue;
                    }
                    eventToSport.put(event.getId(), sportSlug);

                    try {
                        SportType sportType = oddsMapper.resolveSportType(event.getType() != null ? event.getType() : sportSlug);
                        String[] teams = parseTeams(event.getName());
                        long startTime = parseStartTime(event.getStartDatetime());

                        OddsUpdateRequest placeholder = new OddsUpdateRequest();
                        placeholder.setBookmaker("smarkets");
                        placeholder.setRegions(List.of(pro.datawiki.igaming.dto.BookmakerRegion.EU, pro.datawiki.igaming.dto.BookmakerRegion.GLOBAL));
                        placeholder.setExternalEventId(event.getId());
                        placeholder.setSportType(sportType);
                        placeholder.setSportName(sportType.name());
                        placeholder.setLeagueName(event.getSlug() != null ? event.getSlug() : "smarkets");
                        placeholder.setTeam1(teams[0]);
                        placeholder.setTeam2(teams[1]);
                        placeholder.setIsLive("live".equalsIgnoreCase(event.getState()));
                        placeholder.setStartTime(startTime);
                        placeholder.setOdds(Collections.emptyList());

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker("smarkets");
                        matchCache.setExternalId(event.getId());
                        matchCache.setSportName(sportType.name());
                        matchCache.setLeagueName(placeholder.getLeagueName());
                        matchCache.setTeam1(teams[0]);
                        matchCache.setTeam2(teams[1]);
                        matchCache.setIsLive(placeholder.getIsLive());
                        matchCache.setStartTime(startTime);
                        matchCache.setEventUrl("https://smarkets.com/event/" + event.getId());
                        matchCache.setStatus(MatchCache.Status.NEW);

                        String serializedPayload = objectMapper.writeValueAsString(placeholder);
                        String currentHash = persistenceService.computeHash(serializedPayload);
                        placeholder.setPayloadHash(currentHash);

                        persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);
                        totalSaved++;
                    } catch (Exception e) {
                        log.debug("Failed to pre-save Smarkets event {}: {}", event.getId(), e.getMessage());
                    }
                }
                Thread.sleep(150); // Small pause between sports
            } catch (Exception e) {
                log.error("Failed to scrape Smarkets sport '{}': {}", sportSlug, e.getMessage());
            }
        }

        // Phase 2: Enrich active events with market quotes using rotating window across all discovered events
        int enrichedCount = 0;
        if (!allEvents.isEmpty()) {
            int totalEvents = allEvents.size();
            int batchSize = Math.min(25, totalEvents);
            int startIdx = enrichmentOffset.getAndAdd(batchSize) % totalEvents;

            for (int i = 0; i < batchSize; i++) {
                int idx = (startIdx + i) % totalEvents;
                SmarketsEvent event = allEvents.get(idx);
                if (event == null || event.getId() == null) {
                    continue;
                }

            try {
                Thread.sleep(500); // 500ms delay between quote lookups
                List<SmarketsMarket> markets = apiClient.getMarketsForEvent(event.getId());
                if (markets == null || markets.isEmpty()) {
                    continue;
                }

                List<String> marketIds = new ArrayList<>();
                for (SmarketsMarket market : markets) {
                    String mName = market.getName() != null ? market.getName().toLowerCase().trim() : "";
                    String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                            market.getMarketType().getName().toLowerCase().trim() : "";

                    boolean isExcludedAuxiliary = mName.contains("corner") || mName.contains("card") || mName.contains("half")
                            || mName.contains("period") || mName.contains("quarter") || mName.contains("time bracket")
                            || mName.contains("minute") || mName.contains("booking") || mName.contains("shot")
                            || mtName.contains("bracket") || mtName.contains("corner") || mtName.contains("card") || mtName.contains("half");
                    if (!isExcludedAuxiliary) {
                        if (mtName.equals("winner_3_way") || mtName.equals("winner_2_way") || mtName.equals("moneyline")
                                || mtName.equals("over_under") || mtName.equals("both_teams_to_score") || mtName.equals("double_chance")
                                || mName.equals("full-time result") || mName.equals("match winner") || mName.equals("both teams to score")
                                || mName.startsWith("over/under ") || mName.equals("double chance")) {
                            marketIds.add(market.getId());
                        }
                    }
                }

                if (marketIds.isEmpty()) {
                    continue;
                }

                List<SmarketsContract> contracts = apiClient.getContractsForMarkets(marketIds);
                Map<String, List<SmarketsContract>> contractsByMarketId = new HashMap<>();
                if (contracts != null) {
                    for (SmarketsContract c : contracts) {
                        if (c.getMarketId() != null) {
                            contractsByMarketId.computeIfAbsent(c.getMarketId(), k -> new ArrayList<>()).add(c);
                        }
                    }
                }

                Map<String, SmarketsContractQuotes> quotesByContractId = apiClient.getQuotesForMarkets(marketIds);
                if (quotesByContractId == null || quotesByContractId.isEmpty()) {
                    continue;
                }

                OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, markets, contractsByMarketId, quotesByContractId);
                if (request != null && request.getOdds() != null && !request.getOdds().isEmpty()) {
                    String sportSlug = eventToSport.getOrDefault(event.getId(), "football_match");
                    SportType sportType = oddsMapper.resolveSportType(event.getType() != null ? event.getType() : sportSlug);
                    String[] teams = parseTeams(event.getName());
                    long startTime = parseStartTime(event.getStartDatetime());

                    MatchCache matchCache = new MatchCache();
                    matchCache.setBookmaker("smarkets");
                    matchCache.setExternalId(event.getId());
                    matchCache.setSportName(sportType.name());
                    matchCache.setLeagueName(request.getLeagueName());
                    matchCache.setTeam1(teams[0]);
                    matchCache.setTeam2(teams[1]);
                    matchCache.setIsLive(request.getIsLive());
                    matchCache.setStartTime(startTime);
                    matchCache.setEventUrl("https://smarkets.com/event/" + event.getId());
                    matchCache.setStatus(MatchCache.Status.NEW);

                    String serializedPayload = objectMapper.writeValueAsString(request);
                    String currentHash = persistenceService.computeHash(serializedPayload);
                    request.setPayloadHash(currentHash);

                    List<pro.datawiki.igaming.source.core.domain.MatchFactor> factorEntities = new ArrayList<>();
                    for (OddItem item : request.getOdds()) {
                        pro.datawiki.igaming.source.core.domain.MatchFactor mf = new pro.datawiki.igaming.source.core.domain.MatchFactor();
                        mf.setMatch(matchCache);
                        mf.setFactorId(item.getFactorId());
                        mf.setName(item.getName());
                        mf.setValue(item.getValue());
                        factorEntities.add(mf);
                    }

                    persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload, factorEntities);

                    String externalId = event.getId();
                    String cachedHash = localStateHashCache.get(externalId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(externalId, currentHash);
                        totalPushed++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("smarkets", externalId);
                        totalUnchanged++;
                    }
                    enrichedCount++;
                }
            } catch (Exception ex) {
                log.debug("Quotes enrichment skipped for event {}: {}", event.getId(), ex.getMessage());
            }
        }
        }

        log.info("Smarkets ingestion completed: {} saved matches, {} pushed updates, {} unchanged.",
                totalSaved, totalPushed, totalUnchanged);
        return totalSaved;
    }

    private String[] parseTeams(String name) {
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (name != null) {
            if (name.contains(" vs. ")) {
                String[] parts = name.split(" vs\\. ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" vs ")) {
                String[] parts = name.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" - ")) {
                String[] parts = name.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" @ ")) {
                String[] parts = name.split(" @ ", 2);
                team2 = parts[0].trim();
                team1 = parts[1].trim();
            } else if (name.contains(" v ")) {
                String[] parts = name.split(" v ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        return new String[]{team1, team2};
    }

    private long parseStartTime(String startDatetime) {
        if (startDatetime != null) {
            try {
                return java.time.Instant.parse(startDatetime).toEpochMilli();
            } catch (Exception ignored) {}
        }
        return System.currentTimeMillis();
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getExternalId() == null) {
            return false;
        }
        return true;
    }
}
