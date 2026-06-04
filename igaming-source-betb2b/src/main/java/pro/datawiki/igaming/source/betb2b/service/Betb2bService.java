package pro.datawiki.igaming.source.betb2b.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.SourceRefreshResponse;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.betb2b.dto.Betb2bGame;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Betb2bService extends AbstractBaseBookmakerService {

    private final Betb2bApiClient apiClient;
    private final Betb2bEventDiscoverer eventDiscoverer;
    private final Betb2bOddsProcessor oddsProcessor;
    private final Betb2bApiErrorTracker errorTracker;
    private final AggregatorClient aggregatorClient;

    public Betb2bService(MatchCacheRepository matchCacheRepository,
                         SportCacheRepository sportCacheRepository,
                         ObjectMapper objectMapper,
                         SportNormalizationService sportNormalizationService,
                         MatchPersistenceService persistenceService,
                         Betb2bApiClient apiClient,
                         Betb2bEventDiscoverer eventDiscoverer,
                         Betb2bOddsProcessor oddsProcessor,
                         Betb2bApiErrorTracker errorTracker,
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
        return "betb2b";
    }

    public SourceRefreshResponse discoverEvents() {
        log.info("Starting Betb2b Family event discovery for bookmaker: {}...", bookmakerName);

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

    protected void saveOrUpdateMatchMetadata(String externalId, Betb2bGame game, Map<Integer, String> sportNames, boolean isLive) {
        MatchCache match = new MatchCache();
        match.setExternalId(externalId);

        updateMatchBasicInfo(match, externalId, game, sportNames, isLive);
        String jsonPayload = serializeGame(game);
        List<MatchFactor> factors = oddsProcessor.buildFactors(game);

        match.setBookmaker(bookmakerName);
        persistenceService.saveOrUpdateMatchMetadata(match, jsonPayload, factors);
    }

    private void updateMatchBasicInfo(MatchCache match, String externalId, Betb2bGame game, Map<Integer, String> sportNames, boolean isLive) {
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

    private String serializeGame(Betb2bGame game) {
        try {
            return objectMapper.writeValueAsString(game);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public int loadMatchCards(int batchSize) {
        return super.loadMatchCards(batchSize);
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        // Feed returns all odds directly; individual cards not strictly needed.
        // We build the event URL based on the bookmaker name/domain.
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

    /**
     * Resolves the base URL for a given Betb2b white-label bookmaker.
     */
    private String resolveBaseUrl(String name) {
        if (name == null) return "https://1xbet.com";
        return switch (name.toLowerCase()) {
            case "melbet"     -> "https://melbet.ru";
            case "megapari"   -> "https://megapari.com";
            case "linebet"    -> "https://linebet.com";
            case "betandyou"  -> "https://betandyou.com";
            case "fansport"   -> "https://fan-sport.com";
            case "888starz"   -> "https://888starz.bet";
            default           -> "https://1xbet.com";
        };
    }

    private void pushToAggregator(MatchCache match) {
        if (match.getJsonPayload() == null || match.getJsonPayload().isEmpty()) return;

        try {
            Betb2bGame game = objectMapper.readValue(match.getJsonPayload(), Betb2bGame.class);
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
            log.error("Failed to push Betb2b odds update to aggregator for match {}: {}", match.getExternalId(), e.getMessage());
        }
    }

}
