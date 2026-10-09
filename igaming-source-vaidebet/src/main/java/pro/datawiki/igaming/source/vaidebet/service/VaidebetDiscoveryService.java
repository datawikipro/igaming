package pro.datawiki.igaming.source.vaidebet.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class VaidebetDiscoveryService extends AbstractAltenarDiscoveryService {

    public VaidebetDiscoveryService(VaidebetApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "vaidebet";
    }
}