package pro.datawiki.igaming.capture.injuries.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.capture.injuries.scheduler.InjuriesCaptureScheduler;
import pro.datawiki.igaming.source.core.injury.model.MatchInjuryReport;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/injuries")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InjuriesCaptureController {

    private final InjuriesCaptureScheduler scheduler;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(scheduler.getDiagnostics());
    }

    @GetMapping("/matches/{externalId}")
    public ResponseEntity<?> getMatchInjuries(@PathVariable String externalId) {
        return scheduler.getReportForMatch(externalId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerManualCrawl() {
        log.info("[Injuries Controller] Manual crawl cycle triggered via REST API");
        CompletableFuture.runAsync(scheduler::runCaptureCycle);
        return ResponseEntity.ok(Map.of("status", "triggered", "message", "Multi-source crawl cycle started asynchronously"));
    }
}
