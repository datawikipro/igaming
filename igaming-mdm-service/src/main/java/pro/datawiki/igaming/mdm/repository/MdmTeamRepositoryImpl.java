package pro.datawiki.igaming.mdm.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MdmTeamRepositoryImpl implements MdmTeamRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> searchTeams(String query, Integer sportId, int limit) {
        String cleanQuery = query != null ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return List.of();
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
        List<Object> params = new ArrayList<>();
        params.add("%" + cleanQuery + "%");
        if (sportId != null) {
            sql.append(" AND t.sport_id = ?");
            params.add(sportId);
        }
        sql.append(" ORDER BY match_count DESC, t.id ASC LIMIT ?");
        params.add(Math.min(limit, 100));
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> findDuplicates(Integer sportId, int limit) {
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
        List<Object> params = new ArrayList<>();
        if (sportId != null) {
            sql.append(" AND t1.sport_id = ?");
            params.add(sportId);
        }
        sql.append("""
            ORDER BY (COALESCE(m1.match_count, 0) + COALESCE(m2.match_count, 0)) DESC, t1.id ASC
            LIMIT ?
        """);
        params.add(Math.min(limit, 100));
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public void mergeTeams(Long survivingTeamId, Long duplicateTeamId) {
        log.info("Executing mdm_merge_team: surviving={}, duplicate={}", survivingTeamId, duplicateTeamId);
        jdbcTemplate.execute((java.sql.Connection conn) -> {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT mdm_merge_team(?, ?)")) {
                ps.setLong(1, survivingTeamId);
                ps.setLong(2, duplicateTeamId);
                ps.execute();
            }
            return null;
        });
    }
}
