package pro.datawiki.igaming.source.betrivers.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.source.betrivers.service.BetriversDiscoveryService;
import pro.datawiki.igaming.source.betrivers.service.BetriversMatchService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/source/betrivers")
@RequiredArgsConstructor
@Slf4j
public class BetriversController {

    private final BetriversDiscoveryService discoveryService;
    private final BetriversMatchService matchService;
    private final MatchCacheRepository matchCacheRepository;

    @PostMapping("/discover")
    public ResponseEntity<Map<String, Object>> triggerDiscovery() {
        log.info("Manual trigger for BetRivers discovery");
        int count = discoveryService.discoverAll();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "matchesDiscovered", count
        ));
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> triggerLoad() {
        log.info("Manual trigger for BetRivers lines update");
        int pushed = matchService.scrapeAllSports();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "updatesPushed", pushed
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long newCount = matchCacheRepository.countByStatus(pro.datawiki.igaming.source.core.domain.MatchCache.Status.NEW);
        long pendingCount = matchCacheRepository.countByStatus(pro.datawiki.igaming.source.core.domain.MatchCache.Status.PENDING);
        long processedCount = matchCacheRepository.countByStatus(pro.datawiki.igaming.source.core.domain.MatchCache.Status.PROCESSED);
        long failedCount = matchCacheRepository.countByStatus(pro.datawiki.igaming.source.core.domain.MatchCache.Status.FAILED);
        long totalCount = matchCacheRepository.count();

        return ResponseEntity.ok(Map.of(
                "bookmaker", "betrivers",
                "total", totalCount,
                "new", newCount,
                "pending", pendingCount,
                "processed", processedCount,
                "failed", failedCount
        ));
    }
}
