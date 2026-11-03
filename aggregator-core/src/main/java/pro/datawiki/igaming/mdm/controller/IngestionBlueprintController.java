/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  pro.datawiki.igaming.dto.UnmappedAnomalyEvent
 */
package pro.datawiki.igaming.mdm.controller;

import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto;
import pro.datawiki.igaming.mdm.service.AnomalyReportingService;
import pro.datawiki.igaming.mdm.service.IngestionBlueprintService;

@RestController
@RequestMapping(value={"/api/v1/mdm"})
public class IngestionBlueprintController {
    private final IngestionBlueprintService blueprintService;
    private final AnomalyReportingService anomalyReportingService;

    @GetMapping(value={"/blueprint/{systemCode}"})
    public ResponseEntity<IngestionBlueprintDto> getBlueprint(@PathVariable String systemCode) {
        return ResponseEntity.ok(this.blueprintService.getBlueprint(systemCode));
    }

    @PostMapping(value={"/blueprint/{systemCode}/invalidate"})
    public ResponseEntity<Void> invalidateBlueprint(@PathVariable String systemCode) {
        this.blueprintService.invalidate(systemCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value={"/blueprint/invalidate-all"})
    public ResponseEntity<Void> invalidateAllBlueprints() {
        this.blueprintService.invalidateAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value={"/report-unmapped"})
    public ResponseEntity<Void> reportUnmapped(@RequestBody UnmappedAnomalyEvent event) {
        this.anomalyReportingService.reportAnomaly(event);
        return ResponseEntity.accepted().build();
    }

    @Generated
    public IngestionBlueprintController(IngestionBlueprintService blueprintService, AnomalyReportingService anomalyReportingService) {
        this.blueprintService = blueprintService;
        this.anomalyReportingService = anomalyReportingService;
    }
}

