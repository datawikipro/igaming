package pro.datawiki.igaming.source.dafabet.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class DafabetApiErrorTracker extends AbstractApiErrorTracker {

    public DafabetApiErrorTracker(MatchCacheRepository matchCacheRepository) {
        setMatchCacheRepository(matchCacheRepository);
    }

    @Override
    protected String getSourceName() {
        return "Dafabet";
    }

    @Scheduled(fixedRateString = "60000") // 1 minute
    @Override
    public void reportErrors() {
        super.reportErrors();
    }
}
