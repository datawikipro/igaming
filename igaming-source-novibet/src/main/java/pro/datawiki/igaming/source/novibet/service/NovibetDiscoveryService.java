package pro.datawiki.igaming.source.novibet.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class NovibetDiscoveryService extends AbstractAltenarDiscoveryService {

    public NovibetDiscoveryService(NovibetApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "novibet";
    }
}