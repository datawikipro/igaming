package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.forensic.ForensicAuditService;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;

import java.util.Map;

/**
 * REST API for executing and querying post-mortem forensic audits.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai-analyst/forensic")
@RequiredArgsConstructor
public class ForensicAuditController {

    private final ForensicAuditService forensicAuditService;

    /**
     * Returns the structured ForensicMatchContext DTO for a recommendation.
     */
    @GetMapping("/context/{recommendationId}")
    public ResponseEntity<ForensicMatchContext> getContext(@PathVariable Long recommendationId) {
        ForensicMatchContext ctx = forensicAuditService.buildContext(recommendationId);
        return ResponseEntity.ok(ctx);
    }

    /**
     * Generates the polymorphic class-dependent institutional prompt.
     */
    @GetMapping(value = "/prompt/{recommendationId}", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> getPrompt(@PathVariable Long recommendationId) {
        String prompt = forensicAuditService.generateForensicPrompt(recommendationId);
        return ResponseEntity.ok(prompt);
    }

    /**
     * Ingests the LLM JSON evaluation and updates learning memory.
     */
    @PostMapping("/report/{recommendationId}")
    public ResponseEntity<ForensicAuditReport> recordReport(
            @PathVariable Long recommendationId,
            @RequestBody Map<String, String> payload) {
        String jsonResponse = payload.getOrDefault("response", "");
        ForensicAuditReport report = forensicAuditService.recordAuditReport(recommendationId, jsonResponse);
        return ResponseEntity.ok(report);
    }
}
