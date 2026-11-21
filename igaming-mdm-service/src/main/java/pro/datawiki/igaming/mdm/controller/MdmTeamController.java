package pro.datawiki.igaming.mdm.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;
import pro.datawiki.igaming.mdm.service.TeamNameLocalizationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mdm/teams")
@RequiredArgsConstructor
@Slf4j
public class MdmTeamController {

    private final MdmMatchResolutionService resolutionService;
    private final MdmTeamRepository teamRepository;
    private final TeamNameLocalizationService localizationService;

    /**
     * Dedicated endpoint to find an existing canonical team or create a new one.
     * Takes country/category, age category, gender, and tournament roster context into account.
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<CanonicalTeamDto> findOrCreateTeam(@RequestBody TeamResolutionRequest request) {
        CanonicalTeamDto dto = resolutionService.findOrCreateTeam(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieve canonical team by ID with optional language localization.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanonicalTeamDto> getTeamById(
            @PathVariable Long id,
            @RequestParam(required = false) String lang
    ) {
        return teamRepository.findById(id)
                .map(t -> localizationService.toLocalizedTeamDto(t, lang))
                .or(() -> resolutionService.getTeamById(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search teams by name and sport for deduplication workbench.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTeams(
            @RequestParam("query") String query,
            @RequestParam(value = "sportId", required = false) Integer sportId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        List<Map<String, Object>> rows = teamRepository.searchTeams(query, sportId, limit);
        return ResponseEntity.ok(rows);
    }

    /**
     * Finds candidate duplicate clusters across MDM teams.
     */
    @GetMapping("/duplicates")
    public ResponseEntity<List<Map<String, Object>>> findDuplicates(
            @RequestParam(value = "sportId", required = false) Integer sportId,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        List<Map<String, Object>> rows = teamRepository.findDuplicates(sportId, limit);
        return ResponseEntity.ok(rows);
    }

    @Data
    public static class MergeRequest {
        private Long survivingTeamId;
        private Long duplicateTeamId;
    }

    /**
     * Merges a duplicate team into a surviving canonical team using authoritative stored procedure mdm_merge_team.
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeTeams(@RequestBody MergeRequest request) {
        if (request.getSurvivingTeamId() == null || request.getDuplicateTeamId() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Both survivingTeamId and duplicateTeamId must be provided"
            ));
        }
        if (request.getSurvivingTeamId().equals(request.getDuplicateTeamId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Cannot merge a team into itself"
            ));
        }
        try {
            teamRepository.mergeTeams(request.getSurvivingTeamId(), request.getDuplicateTeamId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "survivingTeamId", request.getSurvivingTeamId(),
                    "duplicateTeamId", request.getDuplicateTeamId(),
                    "message", "Successfully merged duplicate team #" + request.getDuplicateTeamId() + 
                               " into surviving canonical team #" + request.getSurvivingTeamId()
            ));
        } catch (Exception ex) {
            log.error("Failed to merge teams: surviving={}, duplicate={}, error={}", 
                    request.getSurvivingTeamId(), request.getDuplicateTeamId(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        }
    }
}
