package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalTournamentDto;
import pro.datawiki.igaming.dto.TournamentResolutionRequest;
import pro.datawiki.igaming.mdm.service.MdmTournamentResolutionService;

@RestController
@RequestMapping("/api/v1/mdm/tournaments")
@RequiredArgsConstructor
@Slf4j
public class MdmTournamentController {

    private final MdmTournamentResolutionService tournamentResolutionService;

    /**
     * Dedicated endpoint to find an existing canonical tournament or create a new one.
     * Takes country/category and international scope into account to eliminate duplicate leagues.
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<CanonicalTournamentDto> findOrCreateTournament(@RequestBody TournamentResolutionRequest request) {
        CanonicalTournamentDto dto = tournamentResolutionService.findOrCreateTournament(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieve canonical tournament by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanonicalTournamentDto> getTournamentById(@PathVariable Long id) {
        return tournamentResolutionService.getTournamentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
