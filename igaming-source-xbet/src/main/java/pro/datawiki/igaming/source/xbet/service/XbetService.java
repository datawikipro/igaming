package pro.datawiki.igaming.source.xbet.service;

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
import pro.datawiki.igaming.source.xbet.dto.XbetGame;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class XbetService extends AbstractBaseBookmakerService {

    private final XbetApiClient apiClient;
    private final XbetEventDiscoverer eventDiscoverer;
    private final XbetOddsProcessor oddsProcessor;
    private final XbetApiErrorTracker errorTracker;

    public XbetService(MatchCacheRepository matchCacheRepository,
                       SportCacheRepository sportCacheRepository,
                       ObjectMapper objectMapper,
                       SportNormalizationService sportNormalizationService,
                       MatchPersistenceService persistenceService,
                       XbetApiClient apiClient,
                       XbetEventDiscoverer eventDiscoverer,
                       XbetOddsProcessor oddsProcessor,
                       XbetApiErrorTracker errorTracker) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.apiClient = apiClient;
        this.eventDiscoverer = eventDiscoverer;
        this.oddsProcessor = oddsProcessor;
        this.errorTracker = errorTracker;
    }

    @Override
    public String getBookmakerFamily() {
        return "1xbet";
    }

    public SourceRefreshResponse discoverEvents() {
        log.info("Starting 1xBet Family event discovery for bookmaker: {}...", bookmakerName);

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

    protected void saveOrUpdateMatchMetadata(String externalId, XbetGame game, Map<Integer, String> sportNames, boolean isLive) {
        MatchCache match = new MatchCache();
        match.setExternalId(externalId);

        updateMatchBasicInfo(match, externalId, game, sportNames, isLive);
        String jsonPayload = serializeGame(game);
        List<MatchFactor> factors = oddsProcessor.buildFactors(game);

        match.setBookmaker(bookmakerName);
        persistenceService.saveOrUpdateMatchMetadata(match, jsonPayload, factors);
    }

    private void updateMatchBasicInfo(MatchCache match, String externalId, XbetGame game, Map<Integer, String> sportNames, boolean isLive) {
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

    private void parseScore(String scoreStr, MatchCache match) {
        if (scoreStr != null && scoreStr.contains(":")) {
            String[] scores = scoreStr.split(" ")[0].split(":");
            if (scores.length >= 2) {
                match.setScore1(scores[0]);
                match.setScore2(scores[1]);
            }
        }
    }

    private String serializeGame(XbetGame game) {
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
        // We will mark PROCESSED right away and assign event target URL based on configured domain/name.
        String baseUrl = "https://1xbet.com";
        if ("megapari".equalsIgnoreCase(bookmakerName)) {
            baseUrl = "https://megapari.com";
        } else if ("linebet".equalsIgnoreCase(bookmakerName)) {
            baseUrl = "https://linebet.com";
        } else if ("888starz".equalsIgnoreCase(bookmakerName)) {
            baseUrl = "https://888starz.bet";
        }

        String url = String.format("%s/line/sport/league/game-%s", baseUrl, cache.getExternalId());

        try {
            cache.setEventUrl(url);
            matchCacheRepository.save(cache);
            matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.error("Failed to update {} match card status for {}: {}", bookmakerName, cache.getExternalId(), e.getMessage());
            matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.FAILED, LocalDateTime.now());
            return false;
        }
    }
}
