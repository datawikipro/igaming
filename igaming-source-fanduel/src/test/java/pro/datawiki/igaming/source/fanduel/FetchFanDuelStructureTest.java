package pro.datawiki.igaming.source.fanduel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.datawiki.igaming.source.core.browser.BrowserService;

@SpringBootTest
public class FetchFanDuelStructureTest {

    @Autowired
    private BrowserService browserService;

    @Test
    public void dumpFanDuelNetwork() {
        System.out.println("Starting network dump for FanDuel...");
        browserService.dumpNetworkCalls("https://sportsbook.fanduel.com/", 20000);
        System.out.println("Finished network dump.");
    }
}
