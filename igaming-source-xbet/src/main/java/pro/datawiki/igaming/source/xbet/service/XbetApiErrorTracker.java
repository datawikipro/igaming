package pro.datawiki.igaming.source.xbet.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.service.AbstractApiErrorTracker;

@Component
public class XbetApiErrorTracker extends AbstractApiErrorTracker {

    @Override
    protected String getBookmakerName() {
        return "xbet";
    }
}
