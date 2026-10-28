package pro.datawiki.igaming.agent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.agent.dto.AgentStatsDto;
import pro.datawiki.igaming.agent.service.AnomalyResolutionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {

    private final AnomalyResolutionService resolutionService;

    @GetMapping("/stats")
    public ResponseEntity<AgentStatsDto> getStats() {
        return ResponseEntity.ok(resolutionService.getStats());
    }

    @PostMapping("/resolve-collision")
    public ResponseEntity<?> resolveCollision(
            @RequestParam Long match1,
            @RequestParam Long match2,
            @RequestParam(required = false, defaultValue = "UNKNOWN") String team
    ) {
        boolean resolved = resolutionService.resolveCollisionBetweenMatches(match1, match2, team, null);
        if (resolved) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "match1", match1, "match2", match2));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "match1", match1, "match2", match2));
    }
}
