package pro.datawiki.igaming.source.caesars.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.time.LocalDateTime;

@Service
public class CaesarsDummyLoaderService extends AbstractBaseBookmakerService {

    public CaesarsDummyLoaderService(MatchCacheRepository matchCacheRepository,
                                     SportCacheRepository sportCacheRepository,
                                     ObjectMapper objectMapper,
                                     SportNormalizationService sportNormalizationService,
                                     MatchPersistenceService persistenceService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
    }

    @Override
    public String getBookmakerFamily() {
        return "caesars";
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
        return true;
    }
}
