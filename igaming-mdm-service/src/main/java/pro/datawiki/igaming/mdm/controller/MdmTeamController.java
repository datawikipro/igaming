package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

@RestController
@RequestMapping("/api/v1/mdm/teams")
@RequiredArgsConstructor
@Slf4j
public class MdmTeamController {

    private final MdmMatchResolutionService resolutionService;

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
     * Retrieve canonical team by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanonicalTeamDto> getTeamById(@PathVariable Long id) {
        return resolutionService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
