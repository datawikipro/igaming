package pro.datawiki.igaming.source.core.engine.xbet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.SourceRefreshResponse;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyGame;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractXbetFamilyService extends AbstractBaseBookmakerService {

    private final AbstractBetB2BFamilyApiClient apiClient;
    private final XbetFamilyEventDiscoverer eventDiscoverer;
    private final XbetFamilyOddsProcessor oddsProcessor;
    private final XbetFamilyApiErrorTracker errorTracker;
    private final AggregatorClient aggregatorClient;

    protected AbstractXbetFamilyService(MatchCacheRepository matchCacheRepository,
                                        SportCacheRepository sportCacheRepository,
                                        ObjectMapper objectMapper,
                                        SportNormalizationService sportNormalizationService,
                                        MatchPersistenceService persistenceService,
                                        AbstractBetB2BFamilyApiClient apiClient,
                                        XbetFamilyEventDiscoverer eventDiscoverer,
                                        XbetFamilyOddsProcessor oddsProcessor,
                                        XbetFamilyApiErrorTracker errorTracker,
                                        AggregatorClient aggregatorClient) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.eventDiscoverer = eventDiscoverer;
        this.oddsProcessor = oddsProcessor;
        this.errorTracker = errorTracker;
        this.aggregatorClient = aggregatorClient;
    }

    @Override
    public String getBookmakerFamily() {
        return "xbet-family";
    }

    public SourceRefreshResponse discoverEvents() {
        log.info("Starting Xbet Family event discovery for bookmaker: {}...", bookmakerName);

        int discoveredLive = processFeed(true);
        int discoveredPrematch = processFeed(false);
        int total = discoveredLive + discoveredPrematch;

        return SourceRefreshResponse.builder()
                .status("ok")
                .message("Discovered " + total + " events")
                .eventsProcessed(total)
                .build();
    }

    private int processFeed(boolean isLive) {
        String json = apiClient.fetchLine(isLive);
        if (json == null || json.isEmpty()) return 0;

        Map<Integer, String> sportNames = eventDiscoverer.extractSportNames(json);

        String regionStr = getRegions().stream().findFirst().map(Enum::name).orElse("INT");
        return eventDiscoverer.discover(json, bookmakerName, regionStr, isLive, errorTracker,
                (externalId, game) -> saveOrUpdateMatchMetadata(externalId, game, sportNames, isLive));
    }

    protected void saveOrUpdateMatchMetadata(String externalId, XbetFamilyGame game, Map<Integer, String> sportNames, boolean isLive) {
        MatchCache match = new MatchCache();
        match.setExternalId(externalId);

        updateMatchBasicInfo(match, externalId, game, sportNames, isLive);
        String jsonPayload = serializeGame(game);
        List<MatchFactor> factors = oddsProcessor.buildFactors(game);

        match.setBookmaker(bookmakerName);
        persistenceService.saveOrUpdateMatchMetadata(match, jsonPayload, factors);
    }

    private void updateMatchBasicInfo(MatchCache match, String externalId, XbetFamilyGame game, Map<Integer, String> sportNames, boolean isLive) {
        if (game.getSportId() != null) {
            match.setSportId(game.getSportId());
            match.setSportName(sportNames.getOrDefault(game.getSportId(), game.getSportName()));
        }

        match.setTeam1(game.getTeam1() != null ? game.getTeam1() : "Team1_" + externalId);
        match.setTeam2(game.getTeam2() != null ? game.getTeam2() : "Team2_" + externalId);

        if (game.getStartTime() != null) {
            long ts = game.getStartTime();
            if (ts < 10000000000L) ts *= 1000;
            match.setStartTime(ts);
        }

        match.setIsLive(isLive);
        parseScore(game.getScore(), match);
    }

    private void parseScore(JsonNode scoreNode, MatchCache match) {
        if (scoreNode != null) {
            if (scoreNode.isTextual()) {
                String scoreStr = scoreNode.asText();
                if (scoreStr.contains(":")) {
                    String[] scores = scoreStr.split(" ")[0].split(":");
                    if (scores.length >= 2) {
                        match.setScore1(scores[0]);
                        match.setScore2(scores[1]);
                    }
                }
            } else if (scoreNode.isObject()) {
                JsonNode fsNode = scoreNode.get("FS");
                if (fsNode != null && fsNode.isObject()) {
                    JsonNode s1 = fsNode.get("S1");
                    JsonNode s2 = fsNode.get("S2");
                    if (s1 != null && s2 != null) {
                        match.setScore1(s1.asText());
                        match.setScore2(s2.asText());
                    }
                }
            }
        }
    }

    private String serializeGame(XbetFamilyGame game) {
        try {
            return objectMapper.writeValueAsString(game);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        String baseUrl = resolveBaseUrl(bookmakerName);
        String url = String.format("%s/line/sport/league/game-%s", baseUrl, cache.getExternalId());

        try {
            cache.setEventUrl(url);
            matchCacheRepository.save(cache);

            // Push odds update to the aggregator
            pushToAggregator(cache);

            matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.error("Failed to update {} match card status for {}: {}", bookmakerName, cache.getExternalId(), e.getMessage());
            matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.FAILED, LocalDateTime.now());
            return false;
        }
    }

    protected abstract String resolveBaseUrl(String bookmakerName);

    private void pushToAggregator(MatchCache match) {
        if (match.getJsonPayload() == null || match.getJsonPayload().isEmpty()) return;

        try {
            XbetFamilyGame game = objectMapper.readValue(match.getJsonPayload(), XbetFamilyGame.class);
            if (game == null) return;

            pro.datawiki.igaming.dto.SportType sportType = sportNormalizationService.normalize(match.getSportName());
            List<pro.datawiki.igaming.dto.OddItem> odds = oddsProcessor.processOdds(match, game, sportType, bookmakerName);

            if (odds != null && !odds.isEmpty()) {
                pro.datawiki.igaming.dto.OddsUpdateRequest request = new pro.datawiki.igaming.dto.OddsUpdateRequest();
                request.setBookmaker(bookmakerName);
                request.setExternalEventId(match.getExternalId());
                request.setTeam1(match.getTeam1());
                request.setTeam2(match.getTeam2());
                request.setSportName(match.getSportName());
                request.setSportType(sportType);
                request.setLeagueName(match.getLeagueName());
                request.setStartTime(match.getStartTime());
                request.setIsLive(match.getIsLive());
                request.setEventUrl(match.getEventUrl());
                request.setOdds(odds);

                aggregatorClient.pushOddsUpdate(request);
            }
        } catch (Exception e) {
            log.error("Failed to push {} odds update to aggregator for match {}: {}", bookmakerName, match.getExternalId(), e.getMessage());
        }
    }
}
