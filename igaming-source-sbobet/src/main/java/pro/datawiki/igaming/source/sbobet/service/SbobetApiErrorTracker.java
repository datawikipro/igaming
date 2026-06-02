package pro.datawiki.igaming.source.sbobet.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class SbobetApiErrorTracker extends AbstractApiErrorTracker {

    public SbobetApiErrorTracker(pro.datawiki.igaming.source.core.repository.MatchCacheRepository matchCacheRepository) {
        setMatchCacheRepository(matchCacheRepository);
    }

    @Override
    protected String getSourceName() {
        return "SBOBET";
    }

    @Scheduled(fixedRateString = "60000") // 1 minute
    @Override
    public void reportErrors() {
        super.reportErrors();
    }
}
