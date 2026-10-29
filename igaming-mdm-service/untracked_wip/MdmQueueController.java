package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.mdm.dto.*;
import pro.datawiki.igaming.mdm.service.MdmUnmappedQueueService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mdm/queue")
@RequiredArgsConstructor
public class MdmQueueController {

    private final MdmUnmappedQueueService queueService;

    @GetMapping("/unmapped")
    public ResponseEntity<List<UnmappedQueueItemDto>> getPendingUnmapped(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String systemCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(queueService.getPendingItems(entityType, systemCode, page, limit));
    }

    @PostMapping("/ignore")
    public ResponseEntity<Map<String, Object>> ignoreItem(@RequestBody Map<String, Object> body) {
        Long queueItemId = body.get("queueItemId") != null ? ((Number) body.get("queueItemId")).longValue() : null;
        String reason = (String) body.getOrDefault("reason", "Ignored by LLM Worker");
        boolean ok = queueService.ignoreItem(queueItemId, reason);
        return ResponseEntity.ok(Map.of("status", ok ? "ok" : "failed", "id", String.valueOf(queueItemId)));
    }

    @GetMapping("/candidates/tournaments")
    public ResponseEntity<List<TournamentCandidateDto>> getTournamentCandidates(
            @RequestParam(required = false) Integer sportId,
            @RequestParam(required = false) String sportSlug,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(queueService.getTournamentCandidates(sportId, sportSlug, country, search));
    }

    @GetMapping("/candidates/teams")
    public ResponseEntity<List<TeamCandidateDto>> getTeamCandidates(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) Integer sportId,
            @RequestParam(required = false) Integer countryId,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(queueService.getTeamCandidates(tournamentId, sportId, countryId, search));
    }

    @PostMapping("/resolve-tournament")
    public ResponseEntity<Map<String, Object>> resolveTournament(@RequestBody ResolveTournamentMappingRequest request) {
        boolean success = queueService.resolveTournament(request);
        return ResponseEntity.ok(Map.of("status", success ? "ok" : "failed", "type", "TOURNAMENT", "rawKey", request.getRawKey()));
    }

    @PostMapping("/resolve-team")
    public ResponseEntity<Map<String, Object>> resolveTeam(@RequestBody ResolveTeamMappingRequest request) {
        boolean success = queueService.resolveTeam(request);
        return ResponseEntity.ok(Map.of("status", success ? "ok" : "failed", "type", "TEAM", "rawKey", request.getRawKey()));
    }

    @PostMapping("/sync-unmapped")
    public ResponseEntity<Map<String, Object>> syncUnmapped() {
        int count = queueService.syncUnmappedFromBookmakers();
        return ResponseEntity.ok(Map.of("status", "ok", "enqueued", count));
    }
}
