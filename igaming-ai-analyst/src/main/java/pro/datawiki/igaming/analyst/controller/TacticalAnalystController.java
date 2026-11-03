package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.analyst.model.TacticalMatchDossier;
import pro.datawiki.igaming.analyst.model.TacticalTensor;
import pro.datawiki.igaming.analyst.service.TacticalDossierService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analyst")
@RequiredArgsConstructor
public class TacticalAnalystController {

    private final TacticalDossierService tacticalDossierService;

    @GetMapping("/dossier/{matchId}")
    public ResponseEntity<TacticalMatchDossier> getDossier(@PathVariable String matchId) {
        TacticalMatchDossier dossier = tacticalDossierService.getDossier(matchId);
        if (dossier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dossier);
    }

    @GetMapping("/tensor/{matchId}")
    public ResponseEntity<TacticalTensor> getTensor(@PathVariable String matchId) {
        TacticalTensor tensor = tacticalDossierService.getTensor(matchId);
        if (tensor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tensor);
    }

    @GetMapping("/dossiers")
    public ResponseEntity<Map<String, TacticalMatchDossier>> getAllDossiers() {
        return ResponseEntity.ok(tacticalDossierService.getAllDossiers());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(tacticalDossierService.getMetrics());
    }
}
