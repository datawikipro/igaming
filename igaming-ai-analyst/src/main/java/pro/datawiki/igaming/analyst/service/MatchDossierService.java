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
     * Finds upcoming matches across sports matching their respective lead-time windows.
     * Evaluates matches that do not yet have an AI recommendation in match_ai_recommendation.
     */
    public List<Long> findUpcomingMultiSportMatchesForEvaluation(int limit) {
        long nowMillis = System.currentTimeMillis();
        // Solo, Racket, Precision, Combat: [now + 15m, now + 720m] (up to 12h ahead to capture line value early)
        long soloFrom = nowMillis + (15L * 60L * 1000L);
        long soloTo   = nowMillis + (720L * 60L * 1000L);

        // Esports (CS2, Dota 2, LoL, etc.): [now + 15m, now + 360m] (up to 6h ahead)
        long esportsFrom = nowMillis + (15L * 60L * 1000L);
        long esportsTo   = nowMillis + (360L * 60L * 1000L);

        // Team ball, puck, goal, field sports: [now + 15m, now + 480m] (up to 8h ahead)
        long teamFrom = nowMillis + (15L * 60L * 1000L);
        long teamTo   = nowMillis + (480L * 60L * 1000L);

        String sql = """
            SELECT m.id
            FROM match_record m
            LEFT JOIN league l ON m.league_id = l.id
            LEFT JOIN team t1 ON m.team1_id = t1.id
            LEFT JOIN team t2 ON m.team2_id = t2.id
            WHERE m.is_live = false
              AND m.sport_name NOT IN ('SYNTHETIC_STATISTICS', 'CYBER', 'VIRTUAL', 'FEATURED', 'ЭКСКЛЮЗИВ', 'CYBER_FOOTBALL', 'CYBER_HOCKEY')
              -- Exclude cyber / simulated FIFA / esports disguised as football
              AND (l.name IS NULL OR (
                  l.name NOT ILIKE '%esoccer%'
                  AND l.name NOT ILIKE '%fifa%'
                  AND l.name NOT ILIKE '%volta%'
                  AND l.name NOT ILIKE '%cyber%'
                  AND l.name NOT ILIKE '%2x4%'
                  AND l.name NOT ILIKE '%2x5%'
                  AND l.name NOT ILIKE '%wplay sports%'
                  AND l.name NOT ILIKE '%h2h gg%'
                  AND l.name NOT ILIKE '%simulated%'
                  AND l.name NOT ILIKE '%e-football%'
                  AND l.name NOT ILIKE '%efootball%'
              ))
              AND (t1.default_display_name IS NULL OR (
                  t1.default_display_name NOT ILIKE '%(cyber)%'
                  AND t1.default_display_name NOT ILIKE '%(esoccer)%'
                  AND t1.default_display_name NOT ILIKE '%(fifa)%'
                  AND t1.default_display_name NOT ILIKE '%2x4%'
                  AND t1.default_display_name NOT ILIKE '%2x5%'
              ))
              AND (t2.default_display_name IS NULL OR (
                  t2.default_display_name NOT ILIKE '%(cyber)%'
                  AND t2.default_display_name NOT ILIKE '%(esoccer)%'
                  AND t2.default_display_name NOT ILIKE '%(fifa)%'
                  AND t2.default_display_name NOT ILIKE '%2x4%'
                  AND t2.default_display_name NOT ILIKE '%2x5%'
              ))
              AND (
                  -- 1. Solo, Racket, Precision, Combat (up to 12h)
                  (m.sport_name IN ('TENNIS', 'TABLE_TENNIS', 'BADMINTON', 'DARTS', 'BOXING', 'MMA', 'SNOOKER', 'GOLF', 'CHESS', 'ARM_WRESTLING', 'SKIING', 'BIATHLON') 
                   AND m.start_time BETWEEN ? AND ?)
                  -- 2. Esports (up to 6h)
               OR (m.sport_name IN ('ESPORTS', 'CS2', 'DOTA2', 'VALORANT', 'LEAGUE_OF_LEGENDS', 'MOBILE_LEGENDS', 'CROSSFIRE', 'STARCRAFT', 'RAINBOW_SIX') 
                   AND m.start_time BETWEEN ? AND ?)
                  -- 3. All Team Ball, Puck, and Goal Sports (up to 8h)
               OR (m.sport_name IN ('FOOTBALL', 'HOCKEY', 'SOCCER', 'ICE_HOCKEY', 'BASKETBALL', 'BASKETBALL_3X3', 'VOLLEYBALL', 'HANDBALL', 'BASEBALL', 'AMERICAN_FOOTBALL', 'RUGBY_UNION', 'RUGBY_LEAGUE', 'CRICKET', 'FUTSAL', 'FLOORBALL', 'WATER_POLO', 'BANDY', 'FIELD_HOCKEY', 'AUSTRALIAN_RULES', 'GAELIC_FOOTBALL', 'GAELIC_HURLING') 
                   AND m.start_time BETWEEN ? AND ?)
              )
              AND NOT EXISTS (
                  SELECT 1 FROM match_ai_recommendation r WHERE r.match_id = m.id
              )
            ORDER BY m.start_time ASC
            LIMIT ?
        """;

        return jdbcTemplate.queryForList(sql, Long.class, soloFrom, soloTo, esportsFrom, esportsTo, teamFrom, teamTo, limit);
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
