package pro.datawiki.igaming.analytics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller serving the Ingestion Pipeline Dashboard & Thresholds Monitor UI.
 */
@Controller
public class CrawlerOpsUiController {

    @GetMapping({"/crawler-ops", "/crawler-ops/", "/ingestion-pipeline", "/ingestion-pipeline/"})
    public String crawlerOpsDashboard() {
        return "redirect:/crawler-ops-dashboard.html";
    }
}
