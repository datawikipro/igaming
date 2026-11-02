package pro.datawiki.igaming.analyst.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.analyst.service.MatchMoodDiscoveryService;
import pro.datawiki.igaming.dto.mood.MatchMood;
import pro.datawiki.igaming.dto.mood.MatchMoodCardDto;

import java.util.List;
import java.util.Map;

/**
 * REST controller for match mood discovery ("Watch Tonight / Под пивко").
 * Zero full-text search: purely driven by structured mood presets, time windows, and broadcast availability.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchMoodDiscoveryController {

    private final MatchMoodDiscoveryService discoveryService;

    /**
     * Discovers active and upcoming matches filtered by mood preset.
     *
     * @param mood preset identifier (GOAL_FIESTA, GRUDGE_MATCH, UPSET_WATCH, LATE_CLUTCH)
     * @param hasBroadcast whether only fixtures with playable/embeddable video streams should be returned
     * @param window time window filter (LIVE_NOW, TONIGHT, ALL)
     * @param limit maximum number of matches to return (default 10, max 50)
     */
    @GetMapping("/mood-matches")
    public ResponseEntity<?> getMoodMatches(
            @RequestParam(required = false, defaultValue = "GOAL_FIESTA") String mood,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false, defaultValue = "false") Boolean hasBroadcast,
            @RequestParam(required = false, defaultValue = "TONIGHT") String window,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        log.info("🔍 [Discovery API] Querying mood matches: mood={}, sport={}, hasBroadcast={}, window={}, limit={}",
                mood, sport, hasBroadcast, window, limit);

        // Validate mood parameter if explicitly supplied
        if (mood != null && !mood.isBlank() && !"ALL".equalsIgnoreCase(mood)) {
            try {
                MatchMood.fromString(mood);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_MOOD",
                        "message", "Supported moods are: GOAL_FIESTA, GRUDGE_MATCH, UPSET_WATCH, LATE_CLUTCH, ALL",
                        "supported_moods", List.of("GOAL_FIESTA", "GRUDGE_MATCH", "UPSET_WATCH", "LATE_CLUTCH", "ALL")
                ));
            }
        }

        List<MatchMoodCardDto> matches = discoveryService.discoverMatches(
                mood,
                sport,
                hasBroadcast,
                window,
                limit != null ? limit : 10
        );

        return ResponseEntity.ok(Map.of(
                "selected_mood", mood != null ? mood.toUpperCase() : "ALL",
                "has_broadcast_only", Boolean.TRUE.equals(hasBroadcast),
                "time_window", window != null ? window.toUpperCase() : "TONIGHT",
                "count", matches.size(),
                "matches", matches
        ));
    }

    /**
     * Lists available discovery presets and metadata.
     */
    @GetMapping("/moods")
    public ResponseEntity<?> listAvailableMoods(@RequestParam(required = false, defaultValue = "ru") String lang) {
        List<Map<String, String>> presets = List.of(
                Map.of(
                        "code", MatchMood.GOAL_FIESTA.name(),
                        "emoji", MatchMood.GOAL_FIESTA.getEmoji(),
                        "title", MatchMood.GOAL_FIESTA.getTitle(lang),
                        "description", MatchMood.GOAL_FIESTA.getDescription(lang)
                ),
                Map.of(
                        "code", MatchMood.GRUDGE_MATCH.name(),
                        "emoji", MatchMood.GRUDGE_MATCH.getEmoji(),
                        "title", MatchMood.GRUDGE_MATCH.getTitle(lang),
                        "description", MatchMood.GRUDGE_MATCH.getDescription(lang)
                ),
                Map.of(
                        "code", MatchMood.UPSET_WATCH.name(),
                        "emoji", MatchMood.UPSET_WATCH.getEmoji(),
                        "title", MatchMood.UPSET_WATCH.getTitle(lang),
                        "description", MatchMood.UPSET_WATCH.getDescription(lang)
                ),
                Map.of(
                        "code", MatchMood.LATE_CLUTCH.name(),
                        "emoji", MatchMood.LATE_CLUTCH.getEmoji(),
                        "title", MatchMood.LATE_CLUTCH.getTitle(lang),
                        "description", MatchMood.LATE_CLUTCH.getDescription(lang)
                )
        );
        return ResponseEntity.ok(Map.of("moods", presets));
    }
}
