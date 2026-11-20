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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mdm/matches")
@RequiredArgsConstructor
@Slf4j
public class MdmMatchController {

    private final MdmMatchResolutionService matchResolutionService;
    private final MdmMatchRepository matchRepository;
    private final MdmLiveScoreConsensusService liveScoreService;
    private final pro.datawiki.igaming.mdm.repository.MdmTeamMembershipRepository membershipRepository;
    private final pro.datawiki.igaming.mdm.service.TeamNameLocalizationService localizationService;

    private final com.github.benmanes.caffeine.cache.Cache<String, java.util.Optional<CanonicalMatchHeaderDto>> matchHeaderByIdCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofSeconds(30))
                    .maximumSize(50_000)
                    .build();

    private final com.github.benmanes.caffeine.cache.Cache<String, List<CanonicalMatchHeaderDto>> activeMatchesCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(java.time.Duration.ofSeconds(60))
                    .maximumSize(100)
                    .build();

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
        matchHeaderByIdCache.asMap().keySet().removeIf(k -> k.endsWith(":" + id));
        activeMatchesCache.invalidateAll();
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
        matchHeaderByIdCache.asMap().keySet().removeIf(k -> k.endsWith(":" + id));
        activeMatchesCache.invalidateAll();
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
    public ResponseEntity<CanonicalMatchHeaderDto> getMatchById(
            @PathVariable Long id,
            @RequestParam(required = false) String lang
    ) {
        String cacheKey = (lang == null ? "default" : lang) + ":" + id;
        java.util.Optional<CanonicalMatchHeaderDto> cached = matchHeaderByIdCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }

        java.util.Optional<CanonicalMatchHeaderDto> loaded = matchRepository.findByIdWithDetails(id)
                .map(match -> {
                    CanonicalMatchHeaderDto header = localizationService.toLocalizedHeaderDto(match, lang, null, null);
                    enrichWithLiveScore(header);
                    enrichWithPlayers(header);
                    return header;
                });
        matchHeaderByIdCache.put(cacheKey, loaded);
        return loaded.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all active upcoming matches without an upper limit (starts >= NOW() - sinceHours).
     * Enriched in batch with real-time live scores directly from Redis.
     */
    @GetMapping("/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CanonicalMatchHeaderDto>> getActiveMatches(
            @RequestParam(name = "since_hours", defaultValue = "3") int sinceHours,
            @RequestParam(required = false) String lang
    ) {
        String cacheKey = (lang == null ? "default" : lang) + ":" + sinceHours;
        List<CanonicalMatchHeaderDto> cached = activeMatchesCache.getIfPresent(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);
        List<pro.datawiki.igaming.mdm.domain.MdmMatch> matches = matchRepository.findActiveUpcomingMatches(fromTime);
        Map<Long, Map<String, String>> teamTrans = localizationService.loadTeamTranslations(matches);
        Map<Long, Map<String, String>> tournTrans = localizationService.loadTournamentTranslations(matches);
        List<CanonicalMatchHeaderDto> list = matches.stream()
                .map(m -> localizationService.toLocalizedHeaderDto(m, lang, teamTrans, tournTrans))
                .toList();
        enrichWithLiveScores(list);
        activeMatchesCache.put(cacheKey, list);
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
            @RequestParam(name = "since_hours", defaultValue = "3") int sinceHours,
            @RequestParam(required = false) String lang,
            @RequestParam(name = "real_sports_only", required = false) Boolean realSportsOnly
    ) {
        String sportFilter = (sport == null || sport.isBlank() || "All".equalsIgnoreCase(sport)) ? null : sport.trim().toLowerCase();
        String searchFilter = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
        String statusFilter = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) ? null : status.trim().toUpperCase();
        boolean isLive = "LIVE".equalsIgnoreCase(statusFilter) || "IN_PLAY".equalsIgnoreCase(statusFilter);

        org.springframework.data.domain.Sort sort = isLive
                ? org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "startTime")
                : org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "startTime");

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, sort
        );
        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);

        org.springframework.data.jpa.domain.Specification<pro.datawiki.igaming.mdm.domain.MdmMatch> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromTime));
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
                if (isLive) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(20)));
                }
            }
            if (sportFilter != null) {
                predicates.add(cb.equal(cb.lower(root.get("sport").get("slug")), sportFilter));
            } else if (Boolean.TRUE.equals(realSportsOnly)) {
                // SportVibe (Clean Mode): Strictly Real Physical Sports only (exclude corners, yellow cards, fouls, cyber, specials)
                predicates.add(root.get("sport").get("id").in(
                    java.util.List.of(1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 30, 31, 32)
                ));
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

        org.springframework.data.domain.Page<pro.datawiki.igaming.mdm.domain.MdmMatch> rawPage = matchRepository.findAll(spec, pageable);
        List<pro.datawiki.igaming.mdm.domain.MdmMatch> matches = rawPage.getContent();
        Map<Long, Map<String, String>> teamTrans = localizationService.loadTeamTranslations(matches);
        Map<Long, Map<String, String>> tournTrans = localizationService.loadTournamentTranslations(matches);

        List<CanonicalMatchHeaderDto> dtos = matches.stream()
                .map(m -> localizationService.toLocalizedHeaderDto(m, lang, teamTrans, tournTrans))
                .toList();

        enrichWithLiveScores(dtos);
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(dtos, pageable, rawPage.getTotalElements()));
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
        if (live.getStatus() != null) {
            if (!"CANCELLED".equalsIgnoreCase(header.getStatus()) && !"POSTPONED".equalsIgnoreCase(header.getStatus())) {
                header.setStatus(live.getStatus().name());
            }
        }
    }

    private void enrichWithPlayers(CanonicalMatchHeaderDto header) {
        if (header == null) return;
        if (header.getHomeTeamId() != null) {
            try {
                List<pro.datawiki.igaming.mdm.domain.MdmTeamMembership> mems = membershipRepository.findActiveMembershipsWithPlayer(header.getHomeTeamId());
                if (mems != null && !mems.isEmpty()) {
                    header.setHomePlayers(mems.stream().map(this::toPlayerDto).filter(java.util.Objects::nonNull).toList());
                }
            } catch (Exception e) {
                log.warn("Failed to enrich home players for team {}: {}", header.getHomeTeamId(), e.getMessage());
            }
        }
        if (header.getAwayTeamId() != null) {
            try {
                List<pro.datawiki.igaming.mdm.domain.MdmTeamMembership> mems = membershipRepository.findActiveMembershipsWithPlayer(header.getAwayTeamId());
                if (mems != null && !mems.isEmpty()) {
                    header.setAwayPlayers(mems.stream().map(this::toPlayerDto).filter(java.util.Objects::nonNull).toList());
                }
            } catch (Exception e) {
                log.warn("Failed to enrich away players for team {}: {}", header.getAwayTeamId(), e.getMessage());
            }
        }
    }

    private pro.datawiki.igaming.dto.PlayerDto toPlayerDto(pro.datawiki.igaming.mdm.domain.MdmTeamMembership m) {
        if (m == null) return null;
        pro.datawiki.igaming.mdm.domain.MdmPlayer p = m.getPlayer();
        if (p == null) return null;
        String countryName = p.getNationality() != null ? p.getNationality().getCanonicalName() : null;
        String countryCode = p.getNationality() != null ? p.getNationality().getIsoCode() : null;
        return pro.datawiki.igaming.dto.PlayerDto.builder()
                .id(p.getId())
                .name(p.getCommonName())
                .slug(p.getSlug())
                .sofascoreId(p.getSofascoreId())
                .gender(p.getGender())
                .country(countryName)
                .countryCode(countryCode)
                .roleName(m.getSquadRole())
                .squadRole(m.getSquadRole())
                .jerseyNumber(m.getJerseyNumber())
                .photoUrl(p.getPhotoUrl())
                .build();
    }

    @GetMapping("/by-team/{teamId}/upcoming")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CanonicalMatchHeaderDto>> getUpcomingMatchesByTeam(@PathVariable Long teamId) {
        List<pro.datawiki.igaming.mdm.domain.MdmMatch> matches = matchRepository.findUpcomingMatchesForTeam(teamId);
        List<CanonicalMatchHeaderDto> dtos = matches.stream()
                .map(MdmMatchResolutionService::toHeaderDto)
                .peek(this::enrichWithPlayers)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-team/{teamId}/past")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CanonicalMatchHeaderDto>> getPastMatchesByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "10") int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 50));
        List<pro.datawiki.igaming.mdm.domain.MdmMatch> matches = matchRepository.findPastMatchesForTeam(teamId, pageable);
        List<CanonicalMatchHeaderDto> dtos = matches.stream()
                .map(MdmMatchResolutionService::toHeaderDto)
                .peek(this::enrichWithPlayers)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
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
