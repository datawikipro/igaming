package pro.datawiki.igaming.capture.sofascore.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.capture.sofascore.stats.AdvancedMetricsKafkaPublisher;
import pro.datawiki.igaming.capture.sofascore.stats.AdvancedStatsCaptureService;
import pro.datawiki.igaming.capture.sofascore.stats.CanonicalAdvancedMetricsEvent;

import java.util.List;
import java.util.Map;

/**
 * Controller providing REST management and ingestion endpoints for institutional
 * metrics across Ice Hockey (KHL / NHL) and Basketball (NBA / EuroLeague).
 */
@RestController
@RequestMapping("/api/capture/advanced-stats")
@RequiredArgsConstructor
@Slf4j
public class AdvancedStatsCaptureController {

    private final AdvancedStatsCaptureService captureService;
    private final AdvancedMetricsKafkaPublisher kafkaPublisher;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "igaming-capture-sofascore",
                "kafka_topic", kafkaPublisher.getTopicName(),
                "published_count", kafkaPublisher.getPublishedCount(),
                "supported_sports", List.of("ICE_HOCKEY", "BASKETBALL"),
                "hockey_metrics", List.of("Corsi", "Fenwick", "High Danger Scoring Chances (HDCF)", "Goalie GSAx"),
                "basketball_metrics", List.of("Pace", "Offensive Rating (ORtg)", "Defensive Rating (DRtg)", "True Shooting % (TS%)", "Bench Rotation Depth")
        ));
    }

    @PostMapping("/hockey")
    public ResponseEntity<CanonicalAdvancedMetricsEvent> ingestHockeyStats(@RequestBody HockeyStatsIngestRequest req) {
        log.info("[AdvancedStatsController] Received Ice Hockey ingestion for {}/{}", req.getLeagueName(), req.getExternalMatchId());

        CanonicalAdvancedMetricsEvent event = captureService.captureAndPublishHockeyStats(
                req.getProvider(),
                req.getLeagueName(),
                req.getExternalMatchId(),
                req.getMatchStartTimeEpochMs(),
                req.getHomeTeam(),
                req.getAwayTeam(),
                req.getHomeShotsOnGoal(),
                req.getAwayShotsOnGoal(),
                req.getHomeMissedShots(),
                req.getAwayMissedShots(),
                req.getHomeBlockedShots(),
                req.getAwayBlockedShots(),
                req.getHomeSlotShots(),
                req.getAwaySlotShots(),
                req.getHomeGoals(),
                req.getAwayGoals(),
                req.getHomeExpectedGoalsAgainst(),
                req.getAwayExpectedGoalsAgainst(),
                req.getHomeGoalieName(),
                req.getAwayGoalieName()
        );

        return ResponseEntity.ok(event);
    }

    @PostMapping("/basketball")
    public ResponseEntity<CanonicalAdvancedMetricsEvent> ingestBasketballStats(@RequestBody BasketballStatsIngestRequest req) {
        log.info("[AdvancedStatsController] Received Basketball ingestion for {}/{}", req.getLeagueName(), req.getExternalMatchId());

        CanonicalAdvancedMetricsEvent event = captureService.captureAndPublishBasketballStats(
                req.getProvider(),
                req.getLeagueName(),
                req.getExternalMatchId(),
                req.getMatchStartTimeEpochMs(),
                req.getHomeTeam(),
                req.getAwayTeam(),
                req.getHomePoints(),
                req.getAwayPoints(),
                req.getHomeFga(),
                req.getAwayFga(),
                req.getHomeFta(),
                req.getAwayFta(),
                req.getHomeOrb(),
                req.getAwayOrb(),
                req.getHomeTov(),
                req.getAwayTov(),
                req.getTotalTeamMinutes() > 0 ? req.getTotalTeamMinutes() : 240.0,
                req.getHomeBenchMinutes(),
                req.getAwayBenchMinutes(),
                req.getHomeBenchPoints(),
                req.getAwayBenchPoints()
        );

        return ResponseEntity.ok(event);
    }

    @Data
    public static class HockeyStatsIngestRequest {
        private String provider = "SOFASCORE";
        private String leagueName = "NHL";
        private String externalMatchId;
        private Long matchStartTimeEpochMs;
        private String homeTeam;
        private String awayTeam;
        private int homeShotsOnGoal;
        private int awayShotsOnGoal;
        private int homeMissedShots;
        private int awayMissedShots;
        private int homeBlockedShots;
        private int awayBlockedShots;
        private int homeSlotShots;
        private int awaySlotShots;
        private int homeGoals;
        private int awayGoals;
        private double homeExpectedGoalsAgainst;
        private double awayExpectedGoalsAgainst;
        private String homeGoalieName;
        private String awayGoalieName;
    }

    @Data
    public static class BasketballStatsIngestRequest {
        private String provider = "SOFASCORE";
        private String leagueName = "NBA";
        private String externalMatchId;
        private Long matchStartTimeEpochMs;
        private String homeTeam;
        private String awayTeam;
        private int homePoints;
        private int awayPoints;
        private int homeFga;
        private int awayFga;
        private int homeFta;
        private int awayFta;
        private int homeOrb;
        private int awayOrb;
        private int homeTov;
        private int awayTov;
        private double totalTeamMinutes = 240.0;
        private List<Double> homeBenchMinutes;
        private List<Double> awayBenchMinutes;
        private int homeBenchPoints;
        private int awayBenchPoints;
    }
}
