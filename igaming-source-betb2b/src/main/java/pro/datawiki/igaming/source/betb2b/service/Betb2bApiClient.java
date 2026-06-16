package pro.datawiki.igaming.source.betb2b.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.engine.xbet.service.AbstractXbetFamilyApiClient;
import pro.datawiki.igaming.source.core.engine.xbet.service.XbetFamilyApiErrorTracker;

@Service
@Slf4j
public class Betb2bApiClient extends AbstractXbetFamilyApiClient {

    public Betb2bApiClient(XbetFamilyApiErrorTracker errorTracker,
                           BrowserService browserService,
                           @Value("${app.betb2b.live-url:https://1xbet.com/LiveFeed/Get1xMatchByLeague}") String liveUrl,
                           @Value("${app.betb2b.prematch-url:https://1xbet.com/LineFeed/Get1xMatchByLeague}") String prematchUrl,
                           @Value("${app.betb2b.partner-id:}") String partnerId) {
        super(errorTracker, browserService, liveUrl, prematchUrl, partnerId);
    }
}
