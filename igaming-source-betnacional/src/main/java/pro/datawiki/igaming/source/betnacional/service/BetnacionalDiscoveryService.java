package pro.datawiki.igaming.source.betnacional.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class BetnacionalDiscoveryService extends AbstractAltenarDiscoveryService {

    public BetnacionalDiscoveryService(BetnacionalApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "betnacional";
    }
}