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

import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.mdm.service.MdmLiveScoreConsensusService;

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
    private final MdmLiveScoreConsensusService liveScoreService;

    /**
     * Unified single endpoint to resolve an existing canonical match or create a new one.
     * Also supports legacy /resolve alias.
     */
    @PostMapping(value = {"/find-or-create", "/resolve"})
    public ResponseEntity<CanonicalMatchHeaderDto> findOrCreateMatch(@RequestBody MatchResolutionRequest request) {
        CanonicalMatchHeaderDto header = matchResolutionService.findOrCreateMatch(request);
        return ResponseEntity.ok(header);
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
     * Cache-Aside live score endpoint for frontend/portal.
     * Reads directly from Redis; on cache miss loads from PostgreSQL mdm_match, populates Redis, and returns.
     */
    @GetMapping("/{id}/live-score")
    public ResponseEntity<CanonicalLiveScoreDto> getLiveScore(@PathVariable Long id) {
        CanonicalLiveScoreDto live = liveScoreService.getLiveScore(id);
        if (live != null) {
            return ResponseEntity.ok(live);
        }
        return ResponseEntity.notFound().build();
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

    /**
     * Retrieves a canonical match by ID.
     * Enriched with real-time live score from Redis (with Cache-Aside fallback).
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CanonicalMatchHeaderDto> getMatchById(@PathVariable Long id) {
        return matchRepository.findByIdWithDetails(id)
                .map(match -> {
                    CanonicalMatchHeaderDto header = MdmMatchResolutionService.toHeaderDto(match);
                    enrichWithLiveScore(header);
                    return header;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all active upcoming matches without an upper limit (starts >= NOW() - sinceHours).
     * Enriched in batch with real-time live scores directly from Redis.
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
        enrichWithLiveScores(list);
        return ResponseEntity.ok(list);
    }

    /**
     * Paginated matches endpoint supporting sport, search, and status filters for portal.
     * Enriched in batch with real-time live scores directly from Redis.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<org.springframework.data.domain.Page<CanonicalMatchHeaderDto>> getMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(name = "since_hours", defaultValue = "3") int sinceHours
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "startTime")
        );
        String sportFilter = (sport == null || sport.isBlank() || "All".equalsIgnoreCase(sport)) ? null : sport.trim().toLowerCase();
        String searchFilter = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
        String statusFilter = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) ? null : status.trim().toUpperCase();
        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);

        org.springframework.data.jpa.domain.Specification<pro.datawiki.igaming.mdm.domain.MdmMatch> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromTime));
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
            }
            if (sportFilter != null) {
                predicates.add(cb.equal(cb.lower(root.get("sport").get("slug")), sportFilter));
            }
            if (searchFilter != null) {
                String pattern = "%" + searchFilter + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("homeTeam").get("canonicalName")), pattern),
                        cb.like(cb.lower(root.get("awayTeam").get("canonicalName")), pattern),
                        cb.like(cb.lower(root.get("tournament").get("canonicalName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<CanonicalMatchHeaderDto> pageResult = matchRepository.findAll(spec, pageable)
                .map(MdmMatchResolutionService::toHeaderDto);

        enrichWithLiveScores(pageResult.getContent());
        return ResponseEntity.ok(pageResult);
    }

    private void enrichWithLiveScore(CanonicalMatchHeaderDto header) {
        if (header == null || header.getMatchId() == null) return;
        CanonicalLiveScoreDto live = liveScoreService.getLiveScore(header.getMatchId());
        applyLiveScoreToHeader(header, live);
    }

    private void enrichWithLiveScores(List<CanonicalMatchHeaderDto> headers) {
        if (headers == null || headers.isEmpty()) return;
        List<Long> matchIds = headers.stream().map(CanonicalMatchHeaderDto::getMatchId).filter(java.util.Objects::nonNull).toList();
        java.util.Map<Long, CanonicalLiveScoreDto> liveMap = liveScoreService.getLiveScores(matchIds);
        for (CanonicalMatchHeaderDto header : headers) {
            CanonicalLiveScoreDto live = liveMap.get(header.getMatchId());
            applyLiveScoreToHeader(header, live);
        }
    }

    private void applyLiveScoreToHeader(CanonicalMatchHeaderDto header, CanonicalLiveScoreDto live) {
        if (header == null || live == null) return;
        header.setLiveScore(live);
        if (live.getScore1() != null) header.setScore1(String.valueOf(live.getScore1()));
        if (live.getScore2() != null) header.setScore2(String.valueOf(live.getScore2()));
        if (live.getStatus() != null) header.setStatus(live.getStatus().name());
    }

    @GetMapping("/sports")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getDistinctSports() {
        return ResponseEntity.ok(matchRepository.findDistinctSportSlugs());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("MDM match resolution rejected: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
