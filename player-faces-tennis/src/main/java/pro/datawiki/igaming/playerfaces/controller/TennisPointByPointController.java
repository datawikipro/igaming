package pro.datawiki.igaming.playerfaces.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.TennisPointByPointEvent;
import pro.datawiki.igaming.playerfaces.service.TennisPointByPointService;

import java.util.Map;

/**
 * REST controller exposing deep tennis point-by-point statistics,
 * on-demand harvesting to Kafka, and match surface edge calculations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tennis/stats")
@RequiredArgsConstructor
public class TennisPointByPointController {

    private final TennisPointByPointService pointByPointService;

    /**
     * Retrieves deep point-by-point statistics for a player across court surfaces.
     */
    @GetMapping("/point-by-point/{playerName}")
    public ResponseEntity<TennisPointByPointEvent> getPointByPointStats(
            @PathVariable String playerName,
            @RequestParam(required = false, defaultValue = "ATP") String tour
    ) {
        TourType tourType = TourType.fromString(tour);
        TennisPointByPointEvent stats = pointByPointService.getStats(playerName, tourType);
        return ResponseEntity.ok(stats);
    }

    /**
     * Triggers on-demand point-by-point harvesting from Tennis Abstract and Tennis Explorer,
     * resolves MDM player mapping, and streams the event to Kafka topic 'sports.tennis.point-by-point'.
     */
    @PostMapping("/harvest")
    public ResponseEntity<TennisPointByPointEvent> triggerHarvest(
            @RequestParam String playerName,
            @RequestParam(required = false, defaultValue = "ATP") String tour
    ) {
        TourType tourType = TourType.fromString(tour);
        TennisPointByPointEvent event = pointByPointService.harvestAndPublish(playerName, tourType);
        return ResponseEntity.ok(event);
    }

    /**
     * Calculates court surface advantage and hold probabilities between two players.
     */
    @PostMapping("/match-edge")
    public ResponseEntity<Map<String, Object>> evaluateMatchEdge(
            @RequestBody MatchEdgeRequest request
    ) {
        TourType tour = TourType.fromString(request.tour());
        Map<String, Object> result = pointByPointService.evaluateMatchEdge(
                request.player1(),
                request.player2(),
                request.surface(),
                tour
        );
        return ResponseEntity.ok(result);
    }

    public record MatchEdgeRequest(
            String player1,
            String player2,
            String surface,
            String tour
    ) {}
}
