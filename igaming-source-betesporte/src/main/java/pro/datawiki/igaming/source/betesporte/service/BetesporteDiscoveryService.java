package pro.datawiki.igaming.source.betesporte.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class BetesporteDiscoveryService extends AbstractAltenarDiscoveryService {

    public BetesporteDiscoveryService(BetesporteApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "betesporte";
    }
}