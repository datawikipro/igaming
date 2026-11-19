package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.service.LiveMatchSimulator;
import pro.datawiki.igaming.dto.ml.LiveInPlayPredictionDto;
import pro.datawiki.igaming.dto.ml.LiveMatchSnapshotDto;

import java.util.Map;
import java.util.Optional;

/**
 * Mobile Live In-Play Radar REST Controller ("Bar Scenario").
 * Delivers point-in-time rest-of-match forecasts and +EV entries in < 250ms.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai-analyst")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LiveAnalystController {

    private final LiveMatchSimulator liveMatchSimulator;

    /**
     * 1-Click Mobile In-Play Radar Query for Live Matches.
     */
    @GetMapping("/live-radar/{matchId}")
    public ResponseEntity<?> getLiveMatchRadar(
            @PathVariable Long matchId,
            @RequestParam(required = false) Integer minute,
            @RequestParam(required = false) Integer scoreHome,
            @RequestParam(required = false) Integer scoreAway,
            @RequestParam(required = false) Integer redCardsHome,
            @RequestParam(required = false) Integer redCardsAway,
            @RequestParam(required = false) Double liveXgHome,
            @RequestParam(required = false) Double liveXgAway
    ) {
        log.info("Received Live Radar query for match #{} (minute={}, score={}:{})",
                matchId, minute, scoreHome, scoreAway);

        LiveMatchSnapshotDto snapshot = null;
        if (minute != null && scoreHome != null && scoreAway != null) {
            snapshot = LiveMatchSnapshotDto.builder()
                    .matchId(matchId)
                    .minute(minute)
                    .scoreHome(scoreHome)
                    .scoreAway(scoreAway)
                    .redCardsHome(redCardsHome != null ? redCardsHome : 0)
                    .redCardsAway(redCardsAway != null ? redCardsAway : 0)
                    .liveXgHome(liveXgHome != null ? liveXgHome : 0.0)
                    .liveXgAway(liveXgAway != null ? liveXgAway : 0.0)
                    .matchPeriod(minute > 45 ? "2ND_HALF" : "1ST_HALF")
                    .isLive(true)
                    .build();
        }

        Optional<LiveInPlayPredictionDto> predictionOpt = liveMatchSimulator.simulateLiveMatch(matchId, snapshot);
        if (predictionOpt.isPresent()) {
            return ResponseEntity.ok(predictionOpt.get());
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "LIVE_SIMULATION_FAILED",
                "message", "Could not generate In-Play prediction for match #" + matchId
        ));
    }
}
