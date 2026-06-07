package pro.datawiki.igaming.source.dafabet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.repository.SportCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DafabetDummyLoaderService extends AbstractBaseBookmakerService {

    public DafabetDummyLoaderService(MatchCacheRepository matchCacheRepository,
                                    SportCacheRepository sportCacheRepository,
                                    ObjectMapper objectMapper,
                                    SportNormalizationService sportNormalizationService,
                                    MatchPersistenceService persistenceService) {
        super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
    }

    @Override
    public String getBookmakerFamily() {
        return "dafabet";
    }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        log.debug("Loader: Dafabet uses direct crawler-pushed model. Marking match {} as PROCESSED.", cache.getExternalId());
        matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
        return true;
    }
}
