package pro.datawiki.igaming.source.betway.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class BetwayDiscoveryService extends AbstractSBTechDiscoveryService {

    public BetwayDiscoveryService(BetwayApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return "betway";
    }
}
