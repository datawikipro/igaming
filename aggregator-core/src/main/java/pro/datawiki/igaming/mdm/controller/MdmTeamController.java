/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.http.ResponseEntity
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  pro.datawiki.igaming.dto.CanonicalTeamDto
 *  pro.datawiki.igaming.dto.TeamResolutionRequest
 */
package pro.datawiki.igaming.mdm.controller;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

@RestController
@RequestMapping(value={"/api/v1/mdm/teams"})
public class MdmTeamController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmTeamController.class);
    private final MdmMatchResolutionService resolutionService;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping(value={"/find-or-create"})
    public ResponseEntity<CanonicalTeamDto> findOrCreateTeam(@RequestBody TeamResolutionRequest request) {
        CanonicalTeamDto dto = this.resolutionService.findOrCreateTeam(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<CanonicalTeamDto> getTeamById(@PathVariable Long id) {
        return this.resolutionService.getTeamById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value={"/search"})
    public ResponseEntity<List<Map<String, Object>>> searchTeams(@RequestParam(value="query") String query, @RequestParam(value="sportId", required=false) Integer sportId, @RequestParam(value="limit", defaultValue="20") int limit) {
        String cleanQuery;
        String string = cleanQuery = query != null ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        StringBuilder sql = new StringBuilder("    SELECT\n        t.id, t.canonical_name, t.sport_id, t.sofascore_id, t.country_name, t.country_code,\n        s.slug as sport_name,\n        (SELECT count(*) FROM mdm_match m WHERE m.home_team_id = t.id OR m.away_team_id = t.id) as match_count\n    FROM mdm_team t\n    JOIN mdm_sport s ON t.sport_id = s.id\n    WHERE t.canonical_name ILIKE ?\n");
        ArrayList<Object> params = new ArrayList<Object>();
        params.add("%" + cleanQuery + "%");
        if (sportId != null) {
            sql.append(" AND t.sport_id = ?");
            params.add(sportId);
        }
        sql.append(" ORDER BY match_count DESC, t.id ASC LIMIT ?");
        params.add(Math.min(limit, 100));
        List rows = this.jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(rows);
    }

    @GetMapping(value={"/duplicates"})
    public ResponseEntity<List<Map<String, Object>>> findDuplicates(@RequestParam(value="sportId", required=false) Integer sportId, @RequestParam(value="limit", defaultValue="30") int limit) {
        StringBuilder sql = new StringBuilder("    SELECT\n        t1.id as id1, t1.canonical_name as name1, t1.sport_id, t1.sofascore_id as sofa1, t1.country_name as country1,\n        t2.id as id2, t2.canonical_name as name2, t2.sofascore_id as sofa2, t2.country_name as country2,\n        s.slug as sport_name,\n        COALESCE(m1.match_count, 0) as matches1,\n        COALESCE(m2.match_count, 0) as matches2\n    FROM mdm_team t1\n    JOIN mdm_team t2 ON t1.sport_id = t2.sport_id\n        AND t1.id < t2.id\n        AND t1.age_category = t2.age_category\n        AND t1.gender = t2.gender\n        AND (\n            lower(t1.canonical_name) = lower(t2.canonical_name)\n            OR (t1.sofascore_id IS NOT NULL AND t1.sofascore_id = t2.sofascore_id)\n        )\n    JOIN mdm_sport s ON t1.sport_id = s.id\n    LEFT JOIN LATERAL (\n        SELECT count(*)::bigint as match_count\n        FROM mdm_match m\n        WHERE m.home_team_id = t1.id OR m.away_team_id = t1.id\n    ) m1 ON true\n    LEFT JOIN LATERAL (\n        SELECT count(*)::bigint as match_count\n        FROM mdm_match m\n        WHERE m.home_team_id = t2.id OR m.away_team_id = t2.id\n    ) m2 ON true\n    WHERE 1=1\n");
        ArrayList<Integer> params = new ArrayList<Integer>();
        if (sportId != null) {
            sql.append(" AND t1.sport_id = ?");
            params.add(sportId);
        }
        sql.append("    ORDER BY (COALESCE(m1.match_count, 0) + COALESCE(m2.match_count, 0)) DESC, t1.id ASC\n    LIMIT ?\n");
        params.add(Math.min(limit, 100));
        List rows = this.jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(rows);
    }

    @PostMapping(value={"/merge"})
    public ResponseEntity<Map<String, Object>> mergeTeams(@RequestBody MergeRequest request) {
        if (request.getSurvivingTeamId() == null || request.getDuplicateTeamId() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Both survivingTeamId and duplicateTeamId must be provided"));
        }
        if (request.getSurvivingTeamId().equals(request.getDuplicateTeamId())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Cannot merge a team into itself"));
        }
        try {
            log.info("Executing mdm_merge_team: surviving={}, duplicate={}", (Object)request.getSurvivingTeamId(), (Object)request.getDuplicateTeamId());
            this.jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
                try (PreparedStatement ps = conn.prepareStatement("SELECT mdm_merge_team(?, ?)");){
                    ps.setLong(1, request.getSurvivingTeamId());
                    ps.setLong(2, request.getDuplicateTeamId());
                    ps.execute();
                }
                return null;
            });
            return ResponseEntity.ok(Map.of("success", true, "survivingTeamId", request.getSurvivingTeamId(), "duplicateTeamId", request.getDuplicateTeamId(), "message", "Successfully merged duplicate team #" + request.getDuplicateTeamId() + " into surviving canonical team #" + request.getSurvivingTeamId()));
        }
        catch (Exception ex) {
            log.error("Failed to merge teams: surviving={}, duplicate={}, error={}", new Object[]{request.getSurvivingTeamId(), request.getDuplicateTeamId(), ex.getMessage(), ex});
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    @Generated
    public MdmTeamController(MdmMatchResolutionService resolutionService, JdbcTemplate jdbcTemplate) {
        this.resolutionService = resolutionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class MergeRequest {
        private Long survivingTeamId;
        private Long duplicateTeamId;

        @Generated
        public MergeRequest() {
        }

        @Generated
        public Long getSurvivingTeamId() {
            return this.survivingTeamId;
        }

        @Generated
        public Long getDuplicateTeamId() {
            return this.duplicateTeamId;
        }

        @Generated
        public void setSurvivingTeamId(Long survivingTeamId) {
            this.survivingTeamId = survivingTeamId;
        }

        @Generated
        public void setDuplicateTeamId(Long duplicateTeamId) {
            this.duplicateTeamId = duplicateTeamId;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof MergeRequest)) {
                return false;
            }
            MergeRequest other = (MergeRequest)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$survivingTeamId = this.getSurvivingTeamId();
            Long other$survivingTeamId = other.getSurvivingTeamId();
            if (this$survivingTeamId == null ? other$survivingTeamId != null : !((Object)this$survivingTeamId).equals(other$survivingTeamId)) {
                return false;
            }
            Long this$duplicateTeamId = this.getDuplicateTeamId();
            Long other$duplicateTeamId = other.getDuplicateTeamId();
            return !(this$duplicateTeamId == null ? other$duplicateTeamId != null : !((Object)this$duplicateTeamId).equals(other$duplicateTeamId));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof MergeRequest;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $survivingTeamId = this.getSurvivingTeamId();
            result = result * 59 + ($survivingTeamId == null ? 43 : ((Object)$survivingTeamId).hashCode());
            Long $duplicateTeamId = this.getDuplicateTeamId();
            result = result * 59 + ($duplicateTeamId == null ? 43 : ((Object)$duplicateTeamId).hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "MdmTeamController.MergeRequest(survivingTeamId=" + this.getSurvivingTeamId() + ", duplicateTeamId=" + this.getDuplicateTeamId() + ")";
        }
    }
}

