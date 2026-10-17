package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchDossierService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Calls PostgreSQL stored function fn_format_match_analyst_prompt(p_match_id)
     * which pre-assembles a comprehensive analytical briefing dossier.
     */
    public String getFormattedPrompt(Long matchId) {
        String sql = "SELECT fn_format_match_analyst_prompt(?)";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, matchId);
        } catch (Exception e) {
            log.error("Failed to execute fn_format_match_analyst_prompt for matchId={}: {}", matchId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves row from v_match_analyst_dossier view.
     */
    public Map<String, Object> getMatchDossier(Long matchId) {
        String sql = "SELECT * FROM v_match_analyst_dossier WHERE match_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, matchId);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    /**
     * Finds upcoming matches starting in window [now + minMinutes, now + maxMinutes]
     * that do not yet have an AI recommendation in match_ai_recommendation.
     */
    public List<Long> findUpcomingMatchesForEvaluation(int minMinutes, int maxMinutes) {
        long nowMillis = System.currentTimeMillis();
        long startFrom = nowMillis + (minMinutes * 60L * 1000L);
        long startTo = nowMillis + (maxMinutes * 60L * 1000L);

        String sql = """
            SELECT m.id
            FROM match_record m
            WHERE m.start_time BETWEEN ? AND ?
              AND m.is_live = false
              AND NOT EXISTS (
                  SELECT 1 FROM match_ai_recommendation r WHERE r.match_id = m.id
              )
            ORDER BY m.start_time ASC
            LIMIT 15
        """;

        return jdbcTemplate.queryForList(sql, Long.class, startFrom, startTo);
    }
}
