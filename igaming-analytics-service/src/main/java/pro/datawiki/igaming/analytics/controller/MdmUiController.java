package pro.datawiki.igaming.analytics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the Entity Resolution Hub & UnknownBet Radar UI page.
 * Available at /mdm (redirects to static HTML).
 */
@Controller
public class MdmUiController {

    /** Перенаправить с /mdm на статическую страницу Entity Resolution Hub. */
    @GetMapping({"/", "/mdm", "/mdm/"})
    public String mdmHub() {
        return "redirect:/entity-resolution-hub.html";
    }

    /** Перенаправить с /crawler-ops на статическую страницу Ingestion Pipeline Dashboard. */
    @GetMapping({"/crawler-ops", "/crawler-ops/", "/pipeline", "/pipeline/"})
    public String crawlerOps() {
        return "redirect:/crawler-ops-dashboard.html";
    }
}
