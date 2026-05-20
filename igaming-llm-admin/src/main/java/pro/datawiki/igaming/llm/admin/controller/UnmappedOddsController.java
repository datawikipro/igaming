package pro.datawiki.igaming.llm.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/odds")
@CrossOrigin(origins = "*")
public class UnmappedOddsController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.aggregator-url:http://localhost:3034}")
    private String aggregatorUrl;

    @GetMapping("/unmapped")
    public ResponseEntity<Object[]> getUnmappedOdds() {
        try {
            String url = aggregatorUrl + "/api/diagnostics/unmapped-odds";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch unmapped odds from aggregator: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export-md")
    public ResponseEntity<String> exportUnmappedOdds() {
        try {
            String url = aggregatorUrl + "/api/diagnostics/unmapped-odds/export-md";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"unmapped_odds.md\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/markdown; charset=UTF-8");
            
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export unmapped odds from aggregator: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
