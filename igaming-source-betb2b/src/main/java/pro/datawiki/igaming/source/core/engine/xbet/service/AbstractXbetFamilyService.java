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
        List<String> jsonLines = apiClient.fetchLines(isLive);
        if (jsonLines == null || jsonLines.isEmpty()) return 0;

        int totalCount = 0;
        String regionStr = getRegions().stream().findFirst().map(Enum::name).orElse("INT");

        for (String json : jsonLines) {
            if (json == null || json.isEmpty()) continue;
            try {
                Map<Integer, String> sportNames = eventDiscoverer.extractSportNames(json);
                totalCount += eventDiscoverer.discover(json, bookmakerName, regionStr, isLive, errorTracker,
                        (externalId, game) -> saveOrUpdateMatchMetadata(externalId, game, sportNames, isLive));
            } catch (Exception e) {
                log.warn("Error discovering events in {} feed batch: {}", isLive ? "LIVE" : "PREMATCH", e.getMessage());
            }
        }
        return totalCount;
    }

    protected void saveOrUpdateMatchMetadata(String externalId, XbetFamilyGame game, Map<Integer, String> sportNames, boolean isLive) {
        MatchCache match = new MatchCache();
        match.setExternalId(externalId);

        updateMatchBasicInfo(match, externalId, game, sportNames, isLive);

        String baseUrl = resolveBaseUrl(bookmakerName);
        String section = isLive ? "live" : "line";
        String sportSlug = normalizeSportSlug(match.getSportName());
        String leagueId = game.getLeagueId() != null ? String.valueOf(game.getLeagueId()) : "0";
        match.setEventUrl(String.format("%s/%s/%s/%s/%s", baseUrl, section, sportSlug, leagueId, externalId));

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
        match.setLeagueName(game.getLeagueName());

        if (game.getStartTime() != null) {
            long ts = game.getStartTime();
            if (ts < 10000000000L) ts *= 1000;
            match.setStartTime(ts);
        }

        match.setIsLive(isLive);
        parseScore(game.getScore(), match);
    }

    void parseScore(JsonNode scoreNode, MatchCache match) {
        if (scoreNode == null) return;
        if (scoreNode.isTextual()) {
            String scoreStr = scoreNode.asText();
            if (scoreStr.contains(":")) {
                String[] scores = scoreStr.split(" ")[0].split(":");
                if (scores.length >= 2) {
                    match.setScore1(scores[0].replaceAll("[^0-9]", ""));
                    match.setScore2(scores[1].replaceAll("[^0-9]", ""));
                }
            }
        } else if (scoreNode.isObject()) {
            boolean isBasketball = match.getSportName() != null &&
                    (match.getSportName().toLowerCase().contains("баскет") || match.getSportName().toLowerCase().contains("basket"));

            if (isBasketball) {
                // In BetB2B/1xBet, "PS" contains the actual quarter scores
                JsonNode psNode = scoreNode.get("PS");
                if (psNode != null && (psNode.isArray() || psNode.isObject())) {
                    int total1 = 0;
                    int total2 = 0;
                    boolean hasPeriods = false;

                    if (psNode.isArray()) {
                        for (JsonNode p : psNode) {
                            JsonNode val = p.has("Value") ? p.get("Value") : p;
                            if (val != null && val.has("S1") && val.has("S2")) {
                                total1 += val.get("S1").asInt(0);
                                total2 += val.get("S2").asInt(0);
                                hasPeriods = true;
                            }
                        }
                    } else if (psNode.isObject()) {
                        var fields = psNode.fields();
                        while (fields.hasNext()) {
                            var entry = fields.next();
                            JsonNode val = entry.getValue();
                            if (val != null && val.has("S1") && val.has("S2")) {
                                total1 += val.get("S1").asInt(0);
                                total2 += val.get("S2").asInt(0);
                                hasPeriods = true;
                            }
                        }
                    }

                    if (hasPeriods && (total1 > 0 || total2 > 0)) {
                        match.setScore1(String.valueOf(total1));
                        match.setScore2(String.valueOf(total2));
                        return;
                    }
                }

                // If direct cumulative points S1/S2 are provided
                if (scoreNode.has("S1") && scoreNode.has("S2")) {
                    int s1 = scoreNode.get("S1").asInt(0);
                    int s2 = scoreNode.get("S2").asInt(0);
                    if (s1 + s2 > 10) {
                        match.setScore1(String.valueOf(s1));
                        match.setScore2(String.valueOf(s2));
                        return;
                    }
                }
            }

            // Standard fallback to FS (Full Score / sets / goals)
            JsonNode fsNode = scoreNode.get("FS");
            if (fsNode != null && fsNode.isObject()) {
                JsonNode s1 = fsNode.get("S1");
                JsonNode s2 = fsNode.get("S2");
                if (s1 != null && s2 != null) {
                    match.setScore1(s1.asText().replaceAll("[^0-9]", ""));
                    match.setScore2(s2.asText().replaceAll("[^0-9]", ""));
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
        String section = Boolean.TRUE.equals(cache.getIsLive()) ? "live" : "line";
        String sportSlug = normalizeSportSlug(cache.getSportName());
        String leagueId = "0";
        try {
            if (cache.getJsonPayload() != null && !cache.getJsonPayload().isEmpty()) {
                XbetFamilyGame game = objectMapper.readValue(cache.getJsonPayload(), XbetFamilyGame.class);
                if (game != null && game.getLeagueId() != null) {
                    leagueId = String.valueOf(game.getLeagueId());
                }
            }
        } catch (Exception ignored) {}

        String url = String.format("%s/%s/%s/%s/%s", baseUrl, section, sportSlug, leagueId, cache.getExternalId());

        try {
            cache.setEventUrl(url);

            // Push odds update to the aggregator
            pushToAggregator(cache);

            matchCacheRepository.updateEventUrlAndStatus(cache.getId(), url, MatchCache.Status.PROCESSED, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.error("Failed to update {} match card status for {}: {}", bookmakerName, cache.getExternalId(), e.getMessage());
            try {
                matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.FAILED, LocalDateTime.now());
            } catch (Exception ignored) {}
            return false;
        }
    }

    private String normalizeSportSlug(String sportName) {
        if (sportName == null) return "football";
        String lower = sportName.toLowerCase();
        if (lower.contains("футбол") || lower.contains("foot")) return "football";
        if (lower.contains("теннис") || lower.contains("tennis")) return "tennis";
        if (lower.contains("баскетбол") || lower.contains("basket")) return "basketball";
        if (lower.contains("хоккей") || lower.contains("hockey")) return "ice-hockey";
        if (lower.contains("волейбол") || lower.contains("volley")) return "volleyball";
        if (lower.contains("киберспорт") || lower.contains("esport")) return "esports";
        if (lower.contains("настольный") || lower.contains("table")) return "table-tennis";
        if (lower.contains("гандбол") || lower.contains("handball")) return "handball";
        return "football";
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

