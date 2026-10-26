package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mdm/matches")
@RequiredArgsConstructor
@Slf4j
public class MdmMatchController {

    private final MdmMatchResolutionService matchResolutionService;
    private final MdmMatchRepository matchRepository;

    /**
     * Unified single endpoint to resolve an existing canonical match or create a new one.
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<CanonicalMatchHeaderDto> findOrCreateMatch(@RequestBody MatchResolutionRequest request) {
        CanonicalMatchHeaderDto header = matchResolutionService.findOrCreateMatch(request);
        return ResponseEntity.ok(header);
    }

    /**
     * Retrieves a canonical match by ID.
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CanonicalMatchHeaderDto> getMatchById(@PathVariable Long id) {
        return matchRepository.findByIdWithDetails(id)
                .map(MdmMatchResolutionService::toHeaderDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all active upcoming matches without an upper limit (starts >= NOW() - sinceHours).
     */
    @GetMapping("/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CanonicalMatchHeaderDto>> getActiveMatches(
            @RequestParam(name = "since_hours", defaultValue = "3") int sinceHours
    ) {
        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);
        List<CanonicalMatchHeaderDto> list = matchRepository.findActiveUpcomingMatches(fromTime).stream()
                .map(MdmMatchResolutionService::toHeaderDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("MDM match resolution rejected: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
