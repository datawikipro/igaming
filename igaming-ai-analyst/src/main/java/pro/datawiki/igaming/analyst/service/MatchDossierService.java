package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Match Dossier Service fully decoupled from legacy match_record and stored procedures.
 * Fetches canonical Ground Truth match metadata directly from MDM Service (/api/v1/mdm/matches).
 * Formats prompt in pure Java and strictly enforces the Zero-Odds Protocol.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchDossierService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final ObjectMapper objectMapper;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Builds structured match analyst prompt in pure Java directly from MDM Ground Truth.
     * Strictly enforces the Zero-Odds Protocol (Zero odds in prompt).
     */
    public String getFormattedPrompt(Long matchId) {
        Map<String, Object> dossier = getMatchDossier(matchId);
        if (dossier == null) {
            return "MATCH_NOT_FOUND";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("## ⚽ МАТЧ: %s vs %s\n", dossier.get("team1_name"), dossier.get("team2_name")));
        sb.append(String.format("- **Вид спорта**: %s\n", dossier.get("sport_name")));
        sb.append(String.format("- **Турнир / Лига**: %s\n", dossier.get("league_name")));
        sb.append(String.format("- **Время начала (UTC)**: %s\n", dossier.get("start_time_utc")));
        sb.append(String.format("- **Match ID**: %s\n", dossier.get("match_id")));
        if (dossier.get("sofascore_id") != null) {
            sb.append(String.format("- **Ground Truth SofaScore ID**: %s\n", dossier.get("sofascore_id")));
        }

        // Lineup context if available
        if (Boolean.TRUE.equals(dossier.get("is_lineup_confirmed"))) {
            sb.append("\n### 👥 ПОДТВЕРЖДЁННЫЕ СОСТАВЫ:\n");
            sb.append("- Стартовые составы и скамейка официально подтверждены.\n");
        }

        // Formatted recent match history
        appendRecentMatchesSection(sb, (String) dossier.get("team1_name"), dossier.get("team1_recent_matches"));
        appendRecentMatchesSection(sb, (String) dossier.get("team2_name"), dossier.get("team2_recent_matches"));

        // Formatted Lineups and Bench
        appendLineupsSection(sb, (String) dossier.get("team1_name"), dossier.get("team1_starters"), dossier.get("team1_bench"));
        appendLineupsSection(sb, (String) dossier.get("team2_name"), dossier.get("team2_starters"), dossier.get("team2_bench"));

        // Storyline narrative if present
        if (dossier.get("storyline_json") != null) {
            sb.append(String.format("\n### 📖 КОНТЕКСТ МАТЧА:\n%s\n", dossier.get("storyline_json")));
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendRecentMatchesSection(StringBuilder sb, String teamName, Object recentObj) {
        if (!(recentObj instanceof List<?> list) || list.isEmpty()) return;
        sb.append(String.format("\n### 📊 ПОСЛЕДНИЕ МАТЧИ: %s (Всего: %d):\n", teamName, list.size()));
        int over25Count = 0;
        int totalGoals = 0;
        int cleanSheets = 0;

        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                String res = String.valueOf(m.get("result"));
                int tScore = m.get("team_score") instanceof Number n ? n.intValue() : 0;
                int oScore = m.get("opponent_score") instanceof Number n ? n.intValue() : 0;
                boolean cs = Boolean.TRUE.equals(m.get("clean_sheet"));
                int sumGoals = tScore + oScore;
                if (sumGoals > 2) over25Count++;
                if (cs) cleanSheets++;
                totalGoals += sumGoals;
                sb.append(String.format("  * %s %d:%d (Тотал: %d, Сухой матч: %s)\n", res, tScore, oScore, sumGoals, cs ? "Да" : "Нет"));
            }
        }
        double avgGoals = (double) totalGoals / list.size();
        sb.append(String.format("  -> Итог по тоталам: Средний тотал = %.2f гола/матч, Матчей с ТБ 2.5 = %d/%d (%.0f%%), Сухих матчей = %d/%d\n",
                avgGoals, over25Count, list.size(), (over25Count * 100.0) / list.size(), cleanSheets, list.size()));
    }

    @SuppressWarnings("unchecked")
    private void appendLineupsSection(StringBuilder sb, String teamName, Object startersObj, Object benchObj) {
        if (startersObj instanceof List<?> starters && !starters.isEmpty()) {
            sb.append(String.format("\n### 🟢 СТАРТОВЫЙ СОСТАВ: %s (%d игроков):\n", teamName, starters.size()));
            for (Object p : starters) {
                if (p instanceof Map<?, ?> pMap) {
                    sb.append(String.format("  * %s (%s, рейтинг: %s)\n", pMap.get("name"), pMap.get("position"), pMap.get("rating")));
                }
            }
        }
        if (benchObj instanceof List<?> bench && !bench.isEmpty()) {
            sb.append(String.format("\n### 🟡 СКАМЕЙКА ЗАПАСНЫХ: %s (%d игроков):\n", teamName, bench.size()));
            for (Object p : bench) {
                if (p instanceof Map<?, ?> pMap) {
                    String pos = String.valueOf(pMap.get("position")).toUpperCase();
                    double rating = pMap.get("rating") instanceof Number n ? n.doubleValue() : 0.0;
                    boolean isAttacker = pos.contains("F") || pos.contains("FW") || pos.contains("W") || pos.contains("ST") || pos.contains("ATTACK");
                    boolean isJoker = (isAttacker && rating >= 7.0) || rating >= 7.3;
                    sb.append(String.format("  * %s (%s, рейтинг: %.1f)%s\n",
                            pMap.get("name"), pos, rating, isJoker ? " ⚡ [ДЖОКЕР АТАКИ - ВЕТО НА ТМ]" : ""));
                }
            }
        }
    }

    /**
     * Retrieves match dossier directly from MDM Ground Truth service and enriches with Redis odds consensus.
     */
    public Map<String, Object> getMatchDossier(Long matchId) {
        if (matchId == null) return null;

        CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
        if (header == null) {
            log.warn("Match #{} not found in MDM Service", matchId);
            return null;
        }

        Map<String, Object> dossier = new HashMap<>();
        dossier.put("match_id", header.getMatchId());
        dossier.put("sport_name", header.getSportSlug() != null ? header.getSportSlug().toUpperCase() : "FOOTBALL");
        dossier.put("league_name", header.getTournamentName() != null ? header.getTournamentName() : "Tournament");
        dossier.put("team1_name", header.getHomeTeamName());
        dossier.put("team2_name", header.getAwayTeamName());
        dossier.put("team1_id", header.getHomeTeamId());
        dossier.put("team2_id", header.getAwayTeamId());

        long startMs = header.getStartTimeEpochSec() != null ? header.getStartTimeEpochSec() * 1000L : System.currentTimeMillis();
        dossier.put("start_time", startMs);
        dossier.put("start_time_utc", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(startMs)));
        dossier.put("is_live", false);
        dossier.put("sofascore_id", header.getSofascoreId());

        // Check if raw lineups exist in sofascore_raw_events
        if (header.getSofascoreId() != null) {
            enrichFromSofaRaw(dossier, header.getSofascoreId());
        }

        // Enrich recent matches for team1 and team2
        String sportSlug = header.getSportSlug() != null ? header.getSportSlug().toLowerCase() : "football";
        if (header.getHomeTeamName() != null) {
            dossier.put("team1_recent_matches", fetchRecentMatches(sportSlug, header.getHomeTeamName()));
        }
        if (header.getAwayTeamName() != null) {
            dossier.put("team2_recent_matches", fetchRecentMatches(sportSlug, header.getAwayTeamName()));
        }

        // Enrich with Redis odds consensus (checks MDM match_id or sofascore_id)
        try {
            RedisOddsConsensusService.OddsConsensusDto consensus = redisOddsConsensusService.getConsensus(matchId);
            if (consensus == null && header.getSofascoreId() != null) {
                consensus = redisOddsConsensusService.getConsensus(header.getSofascoreId());
            }
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
     * Finds upcoming matches directly from MDM Service within lead-time window [now - 15m, now + 36h].
     */
    public List<Long> findUpcomingMultiSportMatchesForEvaluation(int limit) {
        long nowMillis = System.currentTimeMillis();
        long windowFrom = nowMillis - (15L * 60L * 1000L);
        long windowTo   = nowMillis + (36L * 60L * 60L * 1000L);

        List<Long> candidates = new ArrayList<>();
        List<CanonicalMatchHeaderDto> activeMatches = fetchMdmActiveMatches();
        log.info("Scanner retrieved {} active matches from MDM Service", activeMatches.size());

        for (CanonicalMatchHeaderDto m : activeMatches) {
            if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
            long startMs = m.getStartTimeEpochSec() * 1000L;
            if (startMs < windowFrom || startMs > windowTo) continue;

            String sportSlug = m.getSportSlug() != null ? m.getSportSlug().toLowerCase() : "";
            if (sportSlug.contains("cyber") || sportSlug.contains("virtual") || sportSlug.contains("synthetic")) {
                continue;
            }

            if (!isAlreadyEvaluated(m.getMatchId())) {
                candidates.add(m.getMatchId());
                if (candidates.size() >= limit) break;
            }
        }

        log.info("Found {} candidate matches from MDM for AI evaluation", candidates.size());
        return candidates;
    }

    public List<Long> findUpcomingMatchesForEvaluation(int minMinutes, int maxMinutes) {
        long nowMillis = System.currentTimeMillis();
        long startFrom = nowMillis + (minMinutes * 60L * 1000L);
        long startTo   = nowMillis + (maxMinutes * 60L * 1000L);

        List<Long> list = new ArrayList<>();
        List<CanonicalMatchHeaderDto> activeMatches = fetchMdmActiveMatches();
        for (CanonicalMatchHeaderDto m : activeMatches) {
            if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
            long startMs = m.getStartTimeEpochSec() * 1000L;
            if (startMs >= startFrom && startMs <= startTo && !isAlreadyEvaluated(m.getMatchId())) {
                String sportSlug = m.getSportSlug() != null ? m.getSportSlug().toLowerCase() : "";
                if (sportSlug.contains("cyber") || sportSlug.contains("virtual") || sportSlug.contains("synthetic")) {
                    continue;
                }
                list.add(m.getMatchId());
                if (list.size() >= 15) break;
            }
        }
        return list;
    }

    private boolean isAlreadyEvaluated(Long matchId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM match_ai_recommendation WHERE match_id = ?", Integer.class, matchId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public CanonicalMatchHeaderDto fetchMdmMatchHeader(Long matchId) {
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/" + matchId;
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(3)).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                return objectMapper.readValue(resp.body(), CanonicalMatchHeaderDto.class);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch MDM match #{}: {}", matchId, e.getMessage());
        }
        return null;
    }

    private List<CanonicalMatchHeaderDto> fetchMdmActiveMatches() {
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/active?since_hours=0";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                return objectMapper.readValue(resp.body(), new TypeReference<List<CanonicalMatchHeaderDto>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to fetch active matches from MDM: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private void enrichFromSofaRaw(Map<String, Object> dossier, Long sofaEventId) {
        try {
            String sql = "SELECT event_json, lineups_json FROM sofascore_raw_events WHERE event_id = ? ORDER BY captured_at DESC LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sofaEventId);
            if (!rows.isEmpty()) {
                String lineups = (String) rows.get(0).get("lineups_json");
                if (lineups != null && !lineups.isBlank()) {
                    JsonNode root = objectMapper.readTree(lineups);
                    if (root.has("confirmed")) {
                        dossier.put("is_lineup_confirmed", root.path("confirmed").asBoolean(false));
                    }
                    parseLineupSide(root.path("home"), dossier, "team1_bench", "team1_starters");
                    parseLineupSide(root.path("away"), dossier, "team2_bench", "team2_starters");
                }
            }
        } catch (Exception e) {
            log.debug("No raw lineups payload for sofaEventId {}: {}", sofaEventId, e.getMessage());
        }
    }

    private void parseLineupSide(JsonNode sideNode, Map<String, Object> dossier, String benchKey, String startersKey) {
        if (sideNode == null || !sideNode.has("players")) return;
        List<Map<String, Object>> bench = new ArrayList<>();
        List<Map<String, Object>> starters = new ArrayList<>();
        for (JsonNode pNode : sideNode.path("players")) {
            JsonNode player = pNode.path("player");
            String name = player.path("name").asText(player.path("shortName").asText("Player"));
            String pos = pNode.path("position").asText(player.path("position").asText(""));
            double rating = pNode.path("rating").asDouble(player.path("rating").asDouble(0.0));
            boolean isSub = pNode.path("substitute").asBoolean(false);

            Map<String, Object> pMap = new HashMap<>();
            pMap.put("name", name);
            pMap.put("position", pos);
            pMap.put("rating", rating);
            if (isSub) {
                bench.add(pMap);
            } else {
                starters.add(pMap);
            }
        }
        if (!bench.isEmpty()) dossier.put(benchKey, bench);
        if (!starters.isEmpty()) dossier.put(startersKey, starters);
    }

    private List<Map<String, Object>> fetchRecentMatches(String sportSlug, String teamName) {
        List<Map<String, Object>> list = fetchRecentMatchesInternal(sportSlug, teamName);
        if (list.size() < 2) {
            // Try cleaning common prefix/suffixes e.g. "FC Barcelona" -> "Barcelona"
            String cleaned = teamName.replaceAll("(?i)^(FC|CF|AC|AS|FK|SC)\\s+", "")
                                     .replaceAll("(?i)\\s+(FC|CF|FK|SC)$", "").trim();
            if (!cleaned.equalsIgnoreCase(teamName) && cleaned.length() >= 3) {
                List<Map<String, Object>> fallback = fetchRecentMatchesInternal(sportSlug, cleaned);
                if (fallback.size() > list.size()) {
                    return fallback;
                }
            }
        }
        return list;
    }

    private List<Map<String, Object>> fetchRecentMatchesInternal(String sportSlug, String teamName) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (home_team = ? OR away_team = ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportSlug, teamName, teamName);
            for (Map<String, Object> r : rows) {
                String ht = (String) r.get("home_team");
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                boolean isHome = teamName.equalsIgnoreCase(ht);
                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                Map<String, Object> m = new HashMap<>();
                m.put("result", res);
                m.put("team_score", myScore);
                m.put("opponent_score", oppScore);
                m.put("match_date", r.get("match_date"));
                m.put("clean_sheet", oppScore == 0);
                m.put("failed_to_score", myScore == 0);
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch recent matches for team {}: {}", teamName, e.getMessage());
        }
        return list;
    }
}
