package pro.datawiki.igaming.mdm.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.TeamResolutionRequest;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mdm/teams")
@RequiredArgsConstructor
@Slf4j
public class MdmTeamController {

    private final MdmMatchResolutionService resolutionService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Dedicated endpoint to find an existing canonical team or create a new one.
     * Takes country/category, age category, gender, and tournament roster context into account.
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<CanonicalTeamDto> findOrCreateTeam(@RequestBody TeamResolutionRequest request) {
        CanonicalTeamDto dto = resolutionService.findOrCreateTeam(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieve canonical team by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanonicalTeamDto> getTeamById(@PathVariable Long id) {
        return resolutionService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search teams by name and sport for deduplication workbench.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTeams(
            @RequestParam("query") String query,
            @RequestParam(value = "sportId", required = false) Integer sportId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        String cleanQuery = query != null ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        StringBuilder sql = new StringBuilder("""
            SELECT 
                t.id, t.canonical_name, t.sport_id, t.sofascore_id, t.country_name, t.country_code,
                s.slug as sport_name,
                (SELECT count(*) FROM mdm_match m WHERE m.home_team_id = t.id OR m.away_team_id = t.id) as match_count
            FROM mdm_team t
            JOIN mdm_sport s ON t.sport_id = s.id
            WHERE t.canonical_name ILIKE ?
        """);
        List<Object> params = new java.util.ArrayList<>();
        params.add("%" + cleanQuery + "%");
        if (sportId != null) {
            sql.append(" AND t.sport_id = ?");
            params.add(sportId);
        }
        sql.append(" ORDER BY match_count DESC, t.id ASC LIMIT ?");
        params.add(Math.min(limit, 100));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(rows);
    }

    /**
     * Finds candidate duplicate clusters across MDM teams.
     */
    @GetMapping("/duplicates")
    public ResponseEntity<List<Map<String, Object>>> findDuplicates(
            @RequestParam(value = "sportId", required = false) Integer sportId,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                t1.id as id1, t1.canonical_name as name1, t1.sport_id, t1.sofascore_id as sofa1, t1.country_name as country1,
                t2.id as id2, t2.canonical_name as name2, t2.sofascore_id as sofa2, t2.country_name as country2,
                s.slug as sport_name,
                COALESCE(m1.match_count, 0) as matches1,
                COALESCE(m2.match_count, 0) as matches2
            FROM mdm_team t1
            JOIN mdm_team t2 ON t1.sport_id = t2.sport_id 
                AND t1.id < t2.id 
                AND t1.age_category = t2.age_category
                AND t1.gender = t2.gender
                AND (
                    lower(t1.canonical_name) = lower(t2.canonical_name)
                    OR (t1.sofascore_id IS NOT NULL AND t1.sofascore_id = t2.sofascore_id)
                )
            JOIN mdm_sport s ON t1.sport_id = s.id
            LEFT JOIN LATERAL (
                SELECT count(*)::bigint as match_count 
                FROM mdm_match m 
                WHERE m.home_team_id = t1.id OR m.away_team_id = t1.id
            ) m1 ON true
            LEFT JOIN LATERAL (
                SELECT count(*)::bigint as match_count 
                FROM mdm_match m 
                WHERE m.home_team_id = t2.id OR m.away_team_id = t2.id
            ) m2 ON true
            WHERE 1=1
        """);
        List<Object> params = new java.util.ArrayList<>();
        if (sportId != null) {
            sql.append(" AND t1.sport_id = ?");
            params.add(sportId);
        }
        sql.append("""
            ORDER BY (COALESCE(m1.match_count, 0) + COALESCE(m2.match_count, 0)) DESC, t1.id ASC
            LIMIT ?
        """);
        params.add(Math.min(limit, 100));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(rows);
    }

    @Data
    public static class MergeRequest {
        private Long survivingTeamId;
        private Long duplicateTeamId;
    }

    /**
     * Merges a duplicate team into a surviving canonical team using authoritative stored procedure mdm_merge_team.
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeTeams(@RequestBody MergeRequest request) {
        if (request.getSurvivingTeamId() == null || request.getDuplicateTeamId() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Both survivingTeamId and duplicateTeamId must be provided"
            ));
        }
        if (request.getSurvivingTeamId().equals(request.getDuplicateTeamId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Cannot merge a team into itself"
            ));
        }
        try {
            log.info("Executing mdm_merge_team: surviving={}, duplicate={}", 
                    request.getSurvivingTeamId(), request.getDuplicateTeamId());
            jdbcTemplate.execute((java.sql.Connection conn) -> {
                try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT mdm_merge_team(?, ?)")) {
                    ps.setLong(1, request.getSurvivingTeamId());
                    ps.setLong(2, request.getDuplicateTeamId());
                    ps.execute();
                }
                return null;
            });
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "survivingTeamId", request.getSurvivingTeamId(),
                    "duplicateTeamId", request.getDuplicateTeamId(),
                    "message", "Successfully merged duplicate team #" + request.getDuplicateTeamId() + 
                               " into surviving canonical team #" + request.getSurvivingTeamId()
            ));
        } catch (Exception ex) {
            log.error("Failed to merge teams: surviving={}, duplicate={}, error={}", 
                    request.getSurvivingTeamId(), request.getDuplicateTeamId(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        }
    }
}
