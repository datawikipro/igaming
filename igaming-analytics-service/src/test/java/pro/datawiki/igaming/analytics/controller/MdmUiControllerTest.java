package pro.datawiki.igaming.analytics.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MdmUiControllerTest {

    private final MdmUiController controller = new MdmUiController();

    @Test
    @DisplayName("mdmHub redirects to entity-resolution-hub.html")
    void testMdmHubRedirect() {
        assertEquals("redirect:/entity-resolution-hub.html", controller.mdmHub());
    }

    @Test
    @DisplayName("crawlerOps redirects to crawler-ops-dashboard.html")
    void testCrawlerOpsRedirect() {
        assertEquals("redirect:/crawler-ops-dashboard.html", controller.crawlerOps());
    }
}
