package pro.datawiki.igaming.source.sbobet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.browser.BrowserService;

@SpringBootTest(classes = SbobetApplication.class)
public class SbobetTest {

    @Autowired
    private BrowserService browserService;

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
