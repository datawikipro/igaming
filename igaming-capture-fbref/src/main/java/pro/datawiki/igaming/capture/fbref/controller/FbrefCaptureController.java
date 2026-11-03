package pro.datawiki.igaming.capture.fbref.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.capture.fbref.entity.FbrefRawReport;
import pro.datawiki.igaming.capture.fbref.entity.UnderstatRawShot;
import pro.datawiki.igaming.capture.fbref.repository.FbrefRawReportRepository;
import pro.datawiki.igaming.capture.fbref.repository.UnderstatRawShotRepository;
import pro.datawiki.igaming.capture.fbref.service.FbrefIngestionService;
import pro.datawiki.igaming.dto.CanonicalMatchXgDto;
import pro.datawiki.igaming.dto.CanonicalShotMapDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/capture/fbref")
@RequiredArgsConstructor
public class FbrefCaptureController {

    private final FbrefIngestionService ingestionService;
    private final FbrefRawReportRepository rawReportRepository;
    private final UnderstatRawShotRepository rawShotRepository;

    @PostMapping("/xg")
    public ResponseEntity<Map<String, Object>> ingestXgReport(@RequestBody CanonicalMatchXgDto dto) {
        FbrefRawReport report = ingestionService.processAndPublishXgReport(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("report_id", report.getId());
        response.put("match_id", report.getMatchId());
        response.put("topic", "sports.canonical.xg");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shots")
    public ResponseEntity<Map<String, Object>> ingestShotMap(@RequestBody CanonicalShotMapDto dto) {
        List<UnderstatRawShot> shots = ingestionService.processAndPublishShotMap(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("match_id", dto.getMatchId());
        response.put("persisted_shots", shots.size());
        response.put("topic", "sports.canonical.shots");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "igaming-capture-fbref");
        status.put("total_raw_reports", rawReportRepository.count());
        status.put("total_raw_shots", rawShotRepository.count());
        status.put("streaming_topics", List.of("sports.canonical.xg", "sports.canonical.shots", "mdm.anomaly.errors"));
        return ResponseEntity.ok(status);
    }

    @GetMapping("/reports/{matchId}")
    public ResponseEntity<List<FbrefRawReport>> getReportsByMatchId(@PathVariable String matchId) {
        return ResponseEntity.ok(rawReportRepository.findByMatchId(matchId));
    }

    @GetMapping("/shots/{matchId}")
    public ResponseEntity<List<UnderstatRawShot>> getShotsByMatchId(@PathVariable String matchId) {
        return ResponseEntity.ok(rawShotRepository.findByMatchIdOrderByMinuteAsc(matchId));
    }
}
