package pro.datawiki.igaming.source.sbobet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.browser.BrowserContextManager;
import pro.datawiki.igaming.source.core.browser.BrowserExecutor;
import pro.datawiki.igaming.source.core.browser.BrowserLaunchFactory;
import pro.datawiki.igaming.source.core.browser.BrowserProxyManager;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SbobetTest {

    private static BrowserService browserService;

    @BeforeAll
    public static void setup() throws Exception {
        BrowserLaunchFactory launchFactory = new BrowserLaunchFactory();
        BrowserProxyManager proxyManager = mock(BrowserProxyManager.class);
        when(proxyManager.getCurrentProxyUri()).thenReturn(null);
        when(proxyManager.isVpnEnabled()).thenReturn(false);

        BrowserContextManager contextManager = new BrowserContextManager();
        BrowserExecutor executor = mock(BrowserExecutor.class);

        browserService = new BrowserService(launchFactory, proxyManager, contextManager, executor);

        java.lang.reflect.Field profileField = BrowserService.class.getDeclaredField("activeProfile");
        profileField.setAccessible(true);
        profileField.set(browserService, BrowserLaunchFactory.StealthProfile.BASIC);

        browserService.init();
    }

    @AfterAll
    public static void tearDown() {
        if (browserService != null) {
            browserService.shutdown();
        }
    }

    @Test
    public void dumpSbobetNetwork() {
        System.out.println("=== STARTING SBOBET DUMP ===");
        try {
            browserService.dumpNetworkCalls("https://www.sbobet.com/ru-RU/euro/volleyball", 20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=== SBOBET DUMP FINISHED ===");
    }

    @Component
    public static class DummyBookmakerService extends pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService {
        public DummyBookmakerService(pro.datawiki.igaming.source.core.repository.MatchCacheRepository matchCacheRepository,
                                     pro.datawiki.igaming.source.core.repository.SportCacheRepository sportCacheRepository,
                                     com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                                     pro.datawiki.igaming.source.core.service.SportNormalizationService sportNormalizationService,
                                     pro.datawiki.igaming.source.core.service.MatchPersistenceService persistenceService) {
            super(matchCacheRepository, sportCacheRepository, objectMapper, sportNormalizationService, persistenceService);
        }
        @Override
        public String getBookmakerFamily() {
            return "sbobet";
        }
        @Override
        protected boolean loadSingleMatchCard(pro.datawiki.igaming.source.core.domain.MatchCache cache) {
            return false;
        }
    }
}
