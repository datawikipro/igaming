package pro.datawiki.igaming.source.apuestatotal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class ApuestaTotalDiscoveryService extends AbstractAltenarDiscoveryService {

    @Value("${app.bookmaker.name:apuestatotal}")
    private String bookmakerName;

    public ApuestaTotalDiscoveryService(ApuestaTotalApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isEmpty()) ? bookmakerName : "apuestatotal";
    }
}