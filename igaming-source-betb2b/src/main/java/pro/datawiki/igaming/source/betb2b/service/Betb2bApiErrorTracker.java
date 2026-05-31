package pro.datawiki.igaming.source.betb2b.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class Betb2bApiErrorTracker extends AbstractApiErrorTracker {

    @Override
    protected String getSourceName() {
        return "Betb2b";
    }

    @Scheduled(fixedRateString = "300000") // 5 minutes
    @Override
    public void reportErrors() {
        super.reportErrors();
    }
}
