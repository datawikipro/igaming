package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.mdm.config.ConditionalOnMdmResolution;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.service.AnomalyReportingService;
import pro.datawiki.igaming.mdm.service.IngestionBlueprintService;

@RestController
@RequestMapping("/api/v1/mdm")
@RequiredArgsConstructor
@ConditionalOnMdmResolution
public class IngestionBlueprintController {

    private final IngestionBlueprintService blueprintService;
    private final AnomalyReportingService anomalyReportingService;

    @GetMapping("/blueprint/{systemCode}")
    public ResponseEntity<IngestionBlueprintDto> getBlueprint(@PathVariable String systemCode) {
        return ResponseEntity.ok(blueprintService.getBlueprint(systemCode));
    }

    @PostMapping("/blueprint/{systemCode}/invalidate")
    public ResponseEntity<Void> invalidateBlueprint(@PathVariable String systemCode) {
        blueprintService.invalidate(systemCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/blueprint/invalidate-all")
    public ResponseEntity<Void> invalidateAllBlueprints() {
        blueprintService.invalidateAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/report-unmapped")
    public ResponseEntity<Void> reportUnmapped(@RequestBody UnmappedAnomalyEvent event) {
        anomalyReportingService.reportAnomaly(event);
        return ResponseEntity.accepted().build();
    }
}
