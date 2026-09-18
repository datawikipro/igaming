package pro.datawiki.igaming.source.bovada.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.source.bovada.service.BovadaDiscoveryService;
import pro.datawiki.igaming.source.bovada.service.BovadaMatchService;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bovada")
@RequiredArgsConstructor
public class BovadaController {

    private final BovadaDiscoveryService discoveryService;
    private final BovadaMatchService matchService;
    private final MatchCacheRepository matchCacheRepository;

    @PostMapping("/discover")
    public ResponseEntity<Map<String, Object>> discover() {
        int count = discoveryService.discoverAll();
        return ResponseEntity.ok(Map.of("status", "success", "discovered", count));
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> load() {
        int count = matchService.scrapeAllSports();
        return ResponseEntity.ok(Map.of("status", "success", "pushed", count));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long newCount = matchCacheRepository.countByStatus(MatchCache.Status.NEW);
        long pendingCount = matchCacheRepository.countByStatus(MatchCache.Status.PENDING);
        long processedCount = matchCacheRepository.countByStatus(MatchCache.Status.PROCESSED);
        long failedCount = matchCacheRepository.countByStatus(MatchCache.Status.FAILED);
        long totalCount = matchCacheRepository.count();

        return ResponseEntity.ok(Map.of(
                "total", totalCount,
                "new", newCount,
                "pending", pendingCount,
                "processed", processedCount,
                "failed", failedCount
        ));
    }
}
