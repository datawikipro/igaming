/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.criteria.Expression
 *  jakarta.persistence.criteria.Predicate
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.data.jpa.domain.Specification
 *  org.springframework.http.ResponseEntity
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  pro.datawiki.igaming.dto.CanonicalMatchHeaderDto
 *  pro.datawiki.igaming.dto.MatchResolutionRequest
 *  pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto
 */
package pro.datawiki.igaming.mdm.controller;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.io.Serializable;
import java.time.OffsetDateTime;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.service.MdmLiveScoreConsensusService;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

@RestController
@RequestMapping(value={"/api/v1/mdm/matches"})
public class MdmMatchController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmMatchController.class);
    private final MdmMatchResolutionService matchResolutionService;
    private final MdmMatchRepository matchRepository;
    private final MdmLiveScoreConsensusService liveScoreService;

    @PostMapping(value={"/find-or-create", "/resolve"})
    public ResponseEntity<CanonicalMatchHeaderDto> findOrCreateMatch(@RequestBody MatchResolutionRequest request) {
        CanonicalMatchHeaderDto header = this.matchResolutionService.findOrCreateMatch(request);
        return ResponseEntity.ok(header);
    }

    @PostMapping(value={"/{id}/cancel"})
    public ResponseEntity<CanonicalMatchHeaderDto> cancelMatch(@PathVariable Long id, @RequestParam(defaultValue="ANOMALY_RESOLVER_AGENT") String reason) {
        return ResponseEntity.ok(this.matchResolutionService.cancelMatch(id, reason));
    }

    @PostMapping(value={"/{id}/promote-ground-truth", "/{id}/ground-truth"})
    public ResponseEntity<CanonicalMatchHeaderDto> promoteGroundTruth(@PathVariable Long id, @RequestParam(required=false) Long sofascoreId, @RequestParam(defaultValue="SOFASCORE") String sourceOrigin, @RequestParam(defaultValue="ANOMALY_RESOLVER_AGENT") String reason) {
        return ResponseEntity.ok(this.matchResolutionService.promoteGroundTruth(id, sofascoreId, sourceOrigin, reason));
    }

    @GetMapping(value={"/{id}/live-score"})
    public ResponseEntity<CanonicalLiveScoreDto> getLiveScore(@PathVariable Long id) {
        CanonicalLiveScoreDto live = this.liveScoreService.getLiveScore(id);
        if (live != null) {
            return ResponseEntity.ok(live);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value={"/{id}/live-score"})
    public ResponseEntity<CanonicalLiveScoreDto> ingestLiveScore(@PathVariable Long id, @RequestBody CanonicalLiveScoreDto scoreDto) {
        if (scoreDto != null) {
            scoreDto.setMatchId(id);
        }
        CanonicalLiveScoreDto result = this.liveScoreService.ingestLiveScore(scoreDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value={"/{id}"})
    @Transactional(readOnly=true)
    public ResponseEntity<CanonicalMatchHeaderDto> getMatchById(@PathVariable Long id) {
        return this.matchRepository.findByIdWithDetails(id).map(match -> {
            CanonicalMatchHeaderDto header = MdmMatchResolutionService.toHeaderDto(match);
            this.enrichWithLiveScore(header);
            return header;
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value={"/active"})
    @Transactional(readOnly=true)
    public ResponseEntity<List<CanonicalMatchHeaderDto>> getActiveMatches(@RequestParam(name="since_hours", defaultValue="3") int sinceHours) {
        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);
        List<CanonicalMatchHeaderDto> list = this.matchRepository.findActiveUpcomingMatches(fromTime).stream().map(MdmMatchResolutionService::toHeaderDto).toList();
        this.enrichWithLiveScores(list);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @Transactional(readOnly=true)
    public ResponseEntity<Page<CanonicalMatchHeaderDto>> getMatches(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String sport, @RequestParam(required=false) String search, @RequestParam(required=false) String status, @RequestParam(name="since_hours", defaultValue="3") int sinceHours) {
        PageRequest pageable = PageRequest.of((int)page, (int)size, (Sort)Sort.by((Sort.Direction)Sort.Direction.ASC, (String[])new String[]{"startTime"}));
        String sportFilter = sport == null || sport.isBlank() || "All".equalsIgnoreCase(sport) ? null : sport.trim().toLowerCase();
        String searchFilter = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        String statusFilter = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status) ? null : status.trim().toUpperCase();
        OffsetDateTime fromTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(sinceHours);
        Specification<MdmMatch> spec = (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(cb.greaterThanOrEqualTo((Expression)root.get("startTime"), (Comparable)fromTime));
            if (statusFilter != null) {
                predicates.add(cb.equal((Expression)root.get("status"), (Object)statusFilter));
            }
            if (sportFilter != null) {
                predicates.add(cb.equal(cb.lower((Expression)root.get("sport").get("slug")), (Object)sportFilter));
            }
            if (searchFilter != null) {
                String pattern = "%" + searchFilter + "%";
                predicates.add(cb.or(new Predicate[]{cb.like(cb.lower((Expression)root.get("homeTeam").get("canonicalName")), pattern), cb.like(cb.lower((Expression)root.get("awayTeam").get("canonicalName")), pattern), cb.like(cb.lower((Expression)root.get("tournament").get("canonicalName")), pattern)}));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page pageResult = this.matchRepository.findAll(spec, (Pageable)pageable).map(MdmMatchResolutionService::toHeaderDto);
        this.enrichWithLiveScores(pageResult.getContent());
        return ResponseEntity.ok(pageResult);
    }

    private void enrichWithLiveScore(CanonicalMatchHeaderDto header) {
        if (header == null || header.getMatchId() == null) {
            return;
        }
        CanonicalLiveScoreDto live = this.liveScoreService.getLiveScore(header.getMatchId());
        this.applyLiveScoreToHeader(header, live);
    }

    private void enrichWithLiveScores(List<CanonicalMatchHeaderDto> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        List<Long> matchIds = headers.stream().map(CanonicalMatchHeaderDto::getMatchId).filter(Objects::nonNull).toList();
        Map<Long, CanonicalLiveScoreDto> liveMap = this.liveScoreService.getLiveScores(matchIds);
        for (CanonicalMatchHeaderDto header : headers) {
            CanonicalLiveScoreDto live = liveMap.get(header.getMatchId());
            this.applyLiveScoreToHeader(header, live);
        }
    }

    private void applyLiveScoreToHeader(CanonicalMatchHeaderDto header, CanonicalLiveScoreDto live) {
        if (header == null || live == null) {
            return;
        }
        header.setLiveScore(live);
        if (live.getScore1() != null) {
            header.setScore1(String.valueOf(live.getScore1()));
        }
        if (live.getScore2() != null) {
            header.setScore2(String.valueOf(live.getScore2()));
        }
        if (live.getStatus() != null) {
            header.setStatus(live.getStatus().name());
        }
    }

    @GetMapping(value={"/sports"})
    @Transactional(readOnly=true)
    public ResponseEntity<List<String>> getDistinctSports() {
        return ResponseEntity.ok(this.matchRepository.findDistinctSportSlugs());
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("MDM match resolution rejected: {}", (Object)e.getMessage());
        return ResponseEntity.badRequest().body((Object)e.getMessage());
    }

    @Generated
    public MdmMatchController(MdmMatchResolutionService matchResolutionService, MdmMatchRepository matchRepository, MdmLiveScoreConsensusService liveScoreService) {
        this.matchResolutionService = matchResolutionService;
        this.matchRepository = matchRepository;
        this.liveScoreService = liveScoreService;
    }
}

