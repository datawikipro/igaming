package pro.datawiki.igaming.source.betbra.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class BetbraDiscoveryService extends AbstractAltenarDiscoveryService {

    public BetbraDiscoveryService(BetbraApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "betbra";
    }
}