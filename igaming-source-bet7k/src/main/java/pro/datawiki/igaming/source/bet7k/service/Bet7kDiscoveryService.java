package pro.datawiki.igaming.source.bet7k.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarDiscoveryService;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

@Service
public class Bet7kDiscoveryService extends AbstractAltenarDiscoveryService {

    @Value("${app.bookmaker.name:bet7k}")
    private String bookmakerName;

    public Bet7kDiscoveryService(Bet7kApiClient apiClient, MatchPersistenceService persistenceService) {
        super(apiClient, persistenceService);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isBlank()) ? bookmakerName : "bet7k";
    }
}