package pro.datawiki.igaming.source.betfair.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairMarketBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairRunnerBook;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetfairExchangeCrawlerTest {

    private BetfairExchangeCrawler crawler;
    private BetfairConfig config;

    @BeforeEach
    void setUp() {
        config = new BetfairConfig();
        config.getExchange().setMinMatchesThreshold(500);

        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        BrowserService browserService = Mockito.mock(BrowserService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        crawler = new BetfairExchangeCrawler(config, restTemplate, browserService, objectMapper);
    }

    @Test
    void testCrawlExchangeMarkets_MeetsMinimumThreshold() {
        int targetCount = 500;
        List<BetfairMarketBook> marketBooks = crawler.crawlExchangeMarkets(targetCount);

        assertNotNull(marketBooks);
        assertTrue(marketBooks.size() >= targetCount,
                "Crawled exchange markets must meet or exceed threshold of " + targetCount + " (Golden Rule #8)");

        BetfairMarketBook sample = marketBooks.get(0);
        assertNotNull(sample.getMarketId());
        assertNotNull(sample.getEventId());
        assertNotNull(sample.getMatchName());
        assertTrue(sample.getTotalMatched() > 0.0, "Total matched volume must be positive");
        assertNotNull(sample.getRunners());
        assertTrue(sample.getRunners().size() >= 2, "Market must have at least 2 runners");

        for (BetfairRunnerBook runner : sample.getRunners()) {
            assertNotNull(runner.getSelectionId());
            assertNotNull(runner.getRunnerName());
            assertTrue(runner.getTotalMatched() > 0.0, "Runner traded volume must be positive");
            assertTrue(runner.getBestBackPrice() > 1.0, "Best back price must be valid decimal odds");
            assertTrue(runner.getBestLayPrice() >= runner.getBestBackPrice(), "Best lay price must be >= back price");
            assertFalse(runner.getAvailableToBack().isEmpty(), "Runner must have availableToBack ladder");
            assertFalse(runner.getAvailableToLay().isEmpty(), "Runner must have availableToLay ladder");
            assertFalse(runner.getTradedVolumeLadder().isEmpty(), "Runner must have tradedVolumeLadder");
        }
    }
}
