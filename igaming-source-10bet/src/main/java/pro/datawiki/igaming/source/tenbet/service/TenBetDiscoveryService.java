package pro.datawiki.igaming.source.tenbet.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class TenBetDiscoveryService extends AbstractSBTechDiscoveryService {

    public TenBetDiscoveryService(TenBetApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "10bet";
    }
}
