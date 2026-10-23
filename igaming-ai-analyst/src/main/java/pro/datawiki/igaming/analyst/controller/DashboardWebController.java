package pro.datawiki.igaming.analyst.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class DashboardWebController {

    @GetMapping(value = {"/", "/index.html", "/dashboard"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> dashboard() {
        try {
            ClassPathResource res = new ClassPathResource("static/index.html");
            try (InputStream is = res.getInputStream()) {
                String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore().mustRevalidate())
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(html);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error loading dashboard: " + e.getMessage());
        }
    }
}
