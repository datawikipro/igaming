package pro.datawiki.igaming.source.xbet.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class XbetApiErrorTracker extends AbstractApiErrorTracker {

    @Override
    protected String getSourceName() {
        return "1xBet";
    }

    @Scheduled(fixedRateString = "300000") // 5 minutes
    @Override
    public void reportErrors() {
        super.reportErrors();
    }
}
