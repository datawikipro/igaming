package pro.datawiki.igaming.source.core.engine.xbet.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class XbetFamilyApiErrorTracker extends AbstractApiErrorTracker {
    @Override
    protected String getSourceName() {
        return "1xbet-family";
    }
}
