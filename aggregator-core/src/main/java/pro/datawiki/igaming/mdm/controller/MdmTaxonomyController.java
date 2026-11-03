/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package pro.datawiki.igaming.mdm.controller;

import java.util.List;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto;
import pro.datawiki.igaming.mdm.service.MdmTaxonomyService;

@RestController
@RequestMapping(value={"/api/v1/mdm/taxonomy"})
public class MdmTaxonomyController {
    private final MdmTaxonomyService taxonomyService;

    @GetMapping(value={"/sports"})
    public ResponseEntity<List<MdmSport>> getSports() {
        return ResponseEntity.ok(this.taxonomyService.getAllSports());
    }

    @GetMapping(value={"/tournaments"})
    public ResponseEntity<List<MdmTournament>> getTournaments(@RequestParam String sportSlug) {
        return ResponseEntity.ok(this.taxonomyService.getTournamentsBySport(sportSlug));
    }

    @GetMapping(value={"/teams/{id}/profile"})
    public ResponseEntity<TeamProfileDto> getTeamProfile(@PathVariable Long id) {
        return this.taxonomyService.getTeamProfile(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value={"/players/{id}/profile"})
    public ResponseEntity<PlayerProfileDto> getPlayerProfile(@PathVariable Long id) {
        return this.taxonomyService.getPlayerProfile(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Generated
    public MdmTaxonomyController(MdmTaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }
}

