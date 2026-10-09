package pro.datawiki.igaming.source.bookmaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bookmaker.config.BookmakerConfig;
import pro.datawiki.igaming.source.bookmaker.dto.BookmakerMatchDto;
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
public class BookmakerMatchService extends AbstractBaseBookmakerService {

    private final BookmakerHtmlClient htmlClient;
    private final BookmakerOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final BookmakerConfig bookmakerConfig;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();

    public BookmakerMatchService(MatchCacheRepository matchCacheRepository,
                                 SportCacheRepository sportCacheRepository,
                                 ObjectMapper objectMapper,
                                 SportNormalizationService sportNormalizationService,
                                 MatchPersistenceService persistenceService,
                                 BookmakerHtmlClient htmlClient,
                                 BookmakerOddsMapper oddsMapper,
                                 AggregatorClient aggregatorClient,
                                 BookmakerConfig bookmakerConfig) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        this.htmlClient = htmlClient;
        this.oddsMapper = oddsMapper;
        this.aggregatorClient = aggregatorClient;
        this.bookmakerConfig = bookmakerConfig;
    }

    @Override
    public String getBookmakerFamily() {
        return "bookmaker";
    }

    public int scrapeAllSports() {
        log.info("Starting BookMaker full line scraping across {} sports...", bookmakerConfig.getSports().size());
        int totalPushed = 0;
        int totalUnchanged = 0;

        for (String sportSlug : bookmakerConfig.getSports()) {
            try {
                List<BookmakerMatchDto> matches = htmlClient.fetchSportMatches(sportSlug);
                if (matches == null || matches.isEmpty()) continue;

                SportType sportType = sportNormalizationService.normalize(sportSlug);

                for (BookmakerMatchDto match : matches) {
                    if (match.getId() == null) continue;

                    try {
                        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(match, sportType);
                        if (request == null || request.getOdds() == null || request.getOdds().isEmpty()) {
                            continue;
                        }

                        MatchCache matchCache = new MatchCache();
                        matchCache.setBookmaker("bookmaker");
                        matchCache.setExternalId(match.getId());
                        matchCache.setSportName(match.getSport());
                        matchCache.setLeagueName(match.getLeague());
                        matchCache.setTeam1(request.getTeam1());
                        matchCache.setTeam2(request.getTeam2());
                        matchCache.setIsLive(request.getIsLive());
                        matchCache.setEventUrl(request.getEventUrl());

                        String serializedPayload = objectMapper.writeValueAsString(request);
                        String currentHash = persistenceService.computeHash(serializedPayload);
                        request.setPayloadHash(currentHash);

                        persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);

                        String cachedHash = localStateHashCache.get(match.getId());
                        if (cachedHash == null || !cachedHash.equals(currentHash)) {
                            aggregatorClient.pushOddsUpdate(request);
                            localStateHashCache.put(match.getId(), currentHash);
                            totalPushed++;
                        } else {
                            aggregatorClient.reportUnchangedOdds("bookmaker", match.getId());
                            totalUnchanged++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to map and push BookMaker match {}: {}", match.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scrape BookMaker sport '{}': {}", sportSlug, e.getMessage(), e);
            }
        }

        log.info("BookMaker line scraping completed: {} pushed updates, {} unchanged.", totalPushed, totalUnchanged);
        return totalPushed;
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        if (cache == null || cache.getJsonPayload() == null) {
            return false;
        }

        try {
            BookmakerMatchDto match = objectMapper.readValue(cache.getJsonPayload(), BookmakerMatchDto.class);
            if (match == null) return false;

            SportType sportType = sportNormalizationService.normalize(match.getSport() != null ? match.getSport() : "General");
            OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(match, sportType);

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
                aggregatorClient.reportUnchangedOdds("bookmaker", cache.getExternalId());
            }

            return true;
        } catch (Exception e) {
            log.error("Error loading single match card for BookMaker {}: {}", cache.getExternalId(), e.getMessage());
            return false;
        }
    }
}
