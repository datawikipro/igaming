package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.ml.dataset.MlDatasetExportService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller exposing REST API for ML Dataset Matrix Generation and Export.
 * Fully compliant with Institutional Java Architecture (zero Python SQL crutches).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai-analyst/dataset")
@RequiredArgsConstructor
public class MlDatasetExportController {

    private final MlDatasetExportService exportService;

    /**
     * Triggers dataset export for the specified sport.
     * Generates {sport}_multitask_features_X.npy and {sport}_multitask_targets_Y.npy.
     */
    @PostMapping("/export")
    public ResponseEntity<MlDatasetExportService.DatasetExportResult> exportDataset(
            @RequestParam(defaultValue = "basketball") String sport,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(required = false) String outputDir) {

        log.info("[MlDatasetExportController] Received export request for sport={}, limit={}, outputDir={}",
                sport, limit, outputDir);

        MlDatasetExportService.DatasetExportResult result = exportService.exportDataset(sport, limit, outputDir);
        return ResponseEntity.ok(result);
    }

    /**
     * Asynchronously triggers dataset export in background.
     */
    @PostMapping("/export-async")
    public ResponseEntity<Map<String, Object>> exportDatasetAsync(
            @RequestParam(defaultValue = "basketball") String sport,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(required = false) String outputDir) {

        log.info("[MlDatasetExportController] Received async export request for sport={}", sport);
        CompletableFuture.runAsync(() -> exportService.exportDataset(sport, limit, outputDir));

        return ResponseEntity.accepted().body(Map.of(
                "status", "ACCEPTED",
                "message", "Export started in background for sport: " + sport,
                "sport", sport
        ));
    }

    /**
     * Returns target definitions metadata for the specified sport.
     */
    @GetMapping("/targets-meta/{sport}")
    public ResponseEntity<List<Map<String, Object>>> getTargetsMeta(@PathVariable String sport) {
        List<Map<String, Object>> meta = exportService.getTargetsMeta(sport);
        return ResponseEntity.ok(meta);
    }
}
