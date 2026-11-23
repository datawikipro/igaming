package pro.datawiki.igaming.mdm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.mdm.config.ConditionalOnMdmResolution;
import pro.datawiki.igaming.mdm.service.MdmLiveScoreConsensusService;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

@RestController
@RequestMapping("/api/v1/mdm/matches")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnMdmResolution
public class MdmMatchResolutionController {

    private final MdmMatchResolutionService matchResolutionService;
    private final MdmLiveScoreConsensusService liveScoreService;

    /**
     * Unified single endpoint to resolve an existing canonical match or create a new one.
     * Also supports legacy /resolve alias.
     */
    @PostMapping(value = {"/find-or-create", "/resolve"})
    public ResponseEntity<CanonicalMatchHeaderDto> findOrCreateMatch(@RequestBody MatchResolutionRequest request) {
        try {
            CanonicalMatchHeaderDto header = matchResolutionService.findOrCreateMatch(request);
            return ResponseEntity.ok(header);
        } catch (IllegalArgumentException e) {
            log.warn("MDM match resolution rejected: {} (source={}, sportId={}, sportSlug={}, tourn={}, home={}, away={})",
                    e.getMessage(),
                    request != null ? request.getCreatedBySource() : "null",
                    request != null ? request.getSportId() : "null",
                    request != null ? request.getSportSlug() : "null",
                    request != null ? request.getTournamentName() : "null",
                    request != null ? request.getHomeTeamName() : "null",
                    request != null ? request.getAwayTeamName() : "null");
            throw e;
        }
    }

    /**
     * Cancels a match (e.g. synthetic cross-match or cancelled fixture).
     * Invoked by Server-Side Anomaly Resolver Robot.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<CanonicalMatchHeaderDto> cancelMatch(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ANOMALY_RESOLVER_AGENT") String reason
    ) {
        return ResponseEntity.ok(matchResolutionService.cancelMatch(id, reason));
    }

    /**
     * Promotes a match to ground truth (e.g. SofaScore verification after collision).
     * Invoked by Server-Side Anomaly Resolver Robot or SofaReconciliationService.
     */
    @PostMapping(value = {"/{id}/promote-ground-truth", "/{id}/ground-truth"})
    public ResponseEntity<CanonicalMatchHeaderDto> promoteGroundTruth(
            @PathVariable Long id,
            @RequestParam(required = false) Long sofascoreId,
            @RequestParam(defaultValue = "SOFASCORE") String sourceOrigin,
            @RequestParam(defaultValue = "ANOMALY_RESOLVER_AGENT") String reason
    ) {
        return ResponseEntity.ok(matchResolutionService.promoteGroundTruth(id, sofascoreId, sourceOrigin, reason));
    }

    /**
     * Ingestion endpoint for crawlers emitting canonical live scores.
     * Applies Monotonic Clock Consensus, writes directly to Redis, and flags match dirty for 1-minute batch flusher.
     */
    @PostMapping("/{id}/live-score")
    public ResponseEntity<CanonicalLiveScoreDto> ingestLiveScore(
            @PathVariable Long id,
            @RequestBody CanonicalLiveScoreDto scoreDto) {
        if (scoreDto != null) {
            scoreDto.setMatchId(id);
        }
        CanonicalLiveScoreDto result = liveScoreService.ingestLiveScore(scoreDto);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("MDM match resolution rejected: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
