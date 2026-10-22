package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchDossierService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisOddsConsensusService redisOddsConsensusService;

    /**
     * Calls PostgreSQL stored function fn_format_match_analyst_prompt(p_match_id)
     * and enriches the dossier with real-time bookmaker consensus from Redis in-memory storage.
     */
    public String getFormattedPrompt(Long matchId) {
        String sql = "SELECT fn_format_match_analyst_prompt(?)";
        String prompt;
        try {
            prompt = jdbcTemplate.queryForObject(sql, String.class, matchId);
        } catch (Exception e) {
            log.error("Failed to execute fn_format_match_analyst_prompt for matchId={}: {}", matchId, e.getMessage());
            return null;
        }

        if (prompt == null || prompt.startsWith("MATCH_NOT_FOUND")) {
            return prompt;
        }

        // Overlay live market odds consensus directly from Redis in-memory storage
        try {
            RedisOddsConsensusService.OddsConsensusDto consensus = redisOddsConsensusService.getConsensus(matchId);
            if (consensus != null && consensus.bookmakersCount() > 0) {
                String marker = "## 📊 Консенсус котировок букмекеров (Market Odds):";
                int idx = prompt.indexOf(marker);
                if (idx != -1) {
                    // Find next section header starting with "## "
                    int nextSectionIdx = prompt.indexOf("\n## ", idx + marker.length());
                    String before = prompt.substring(0, idx);
                    String after = nextSectionIdx != -1 ? prompt.substring(nextSectionIdx) : "";
                    prompt = before + consensus.formattedMarkdown() + "\n" + after;
                } else {
                    prompt = prompt + "\n\n" + consensus.formattedMarkdown();
                }
            }
        } catch (Exception e) {
            log.warn("Could not attach Redis odds consensus for matchId={}: {}", matchId, e.getMessage());
        }

        return prompt;
    }

    /**
     * Retrieves row from v_match_analyst_dossier view and enriches with Redis odds consensus.
     */
    public Map<String, Object> getMatchDossier(Long matchId) {
        String sql = "SELECT * FROM v_match_analyst_dossier WHERE match_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, matchId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> dossier = new HashMap<>(rows.get(0));

        try {
            RedisOddsConsensusService.OddsConsensusDto consensus = redisOddsConsensusService.getConsensus(matchId);
            if (consensus != null && consensus.bookmakersCount() > 0) {
                dossier.put("bookmakers_count", (long) consensus.bookmakersCount());
                if (consensus.bestWin1() != null) dossier.put("best_win1", consensus.bestWin1());
                if (consensus.avgWin1() != null) dossier.put("avg_win1", consensus.avgWin1());
                if (consensus.bestDraw() != null) dossier.put("best_draw", consensus.bestDraw());
                if (consensus.avgDraw() != null) dossier.put("avg_draw", consensus.avgDraw());
                if (consensus.bestWin2() != null) dossier.put("best_win2", consensus.bestWin2());
                if (consensus.avgWin2() != null) dossier.put("avg_win2", consensus.avgWin2());
                if (consensus.bestOver25() != null) dossier.put("best_to25", consensus.bestOver25());
                if (consensus.bestUnder25() != null) dossier.put("best_tu25", consensus.bestUnder25());
            }
        } catch (Exception e) {
            log.debug("Failed to enrich dossier metadata from Redis for matchId={}: {}", matchId, e.getMessage());
        }

        return dossier;
    }

    /**
     * Finds upcoming matches across sports matching their respective lead-time windows.
     * Evaluates matches that do not yet have an AI recommendation in match_ai_recommendation.
     * Filters candidate matches to those that have active lines in Redis.
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

        int candidateLimit = Math.max(limit * 8, 120);
        List<Long> candidates = jdbcTemplate.queryForList(sql, Long.class, soloFrom, soloTo, esportsFrom, esportsTo, teamFrom, teamTo, candidateLimit);

        Set<String> activeOddsMatchIds = redisOddsConsensusService.getActiveMatchIds();
        if (activeOddsMatchIds != null && !activeOddsMatchIds.isEmpty()) {
            List<Long> filtered = candidates.stream()
                    .filter(id -> activeOddsMatchIds.contains(id.toString()))
                    .limit(limit)
                    .toList();
            if (!filtered.isEmpty()) {
                log.info("Multi-sport scanner selected {} candidates with verified Redis odds (from {} DB candidates)",
                        filtered.size(), candidates.size());
                return filtered;
            }
        }

        return candidates.stream().limit(limit).toList();
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
