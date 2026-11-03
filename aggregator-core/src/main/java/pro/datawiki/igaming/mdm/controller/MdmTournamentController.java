/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  pro.datawiki.igaming.dto.CanonicalTournamentDto
 *  pro.datawiki.igaming.dto.TournamentResolutionRequest
 */
package pro.datawiki.igaming.mdm.controller;

import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.dto.CanonicalTournamentDto;
import pro.datawiki.igaming.dto.TournamentResolutionRequest;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

@RestController
@RequestMapping(value={"/api/v1/mdm/tournaments"})
public class MdmTournamentController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmTournamentController.class);
    private final MdmMatchResolutionService resolutionService;

    @PostMapping(value={"/find-or-create"})
    public ResponseEntity<CanonicalTournamentDto> findOrCreateTournament(@RequestBody TournamentResolutionRequest request) {
        CanonicalTournamentDto dto = this.resolutionService.findOrCreateTournament(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<CanonicalTournamentDto> getTournamentById(@PathVariable Long id) {
        return this.resolutionService.getTournamentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Generated
    public MdmTournamentController(MdmMatchResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }
}

