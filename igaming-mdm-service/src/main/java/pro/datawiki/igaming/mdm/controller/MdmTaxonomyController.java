package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto;
import pro.datawiki.igaming.mdm.service.MdmTaxonomyService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mdm/taxonomy")
@RequiredArgsConstructor
public class MdmTaxonomyController {

    private final MdmTaxonomyService taxonomyService;

    @GetMapping("/sports")
    public ResponseEntity<List<MdmSport>> getSports() {
        return ResponseEntity.ok(taxonomyService.getAllSports());
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<MdmTournament>> getTournaments(@RequestParam String sportSlug) {
        return ResponseEntity.ok(taxonomyService.getTournamentsBySport(sportSlug));
    }

    @GetMapping("/teams/{id}/profile")
    public ResponseEntity<TeamProfileDto> getTeamProfile(@PathVariable Long id) {
        return taxonomyService.getTeamProfile(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/players/{id}/profile")
    public ResponseEntity<PlayerProfileDto> getPlayerProfile(@PathVariable Long id) {
        return taxonomyService.getPlayerProfile(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
