package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

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

    @Value("${app.sofascore.ondemand-url:http://igaming-capture-sofascore-on-demand.igaming-enrichment.svc.cluster.local:3041/api/v1/sofascore/on-demand}")
    private String sofaOnDemandUrl;

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

        // Formatted Lineups, Bench and Injuries
        appendLineupsSection(sb, (String) dossier.get("team1_name"), dossier.get("team1_starters"), dossier.get("team1_bench"), dossier.get("team1_missing"));
        appendLineupsSection(sb, (String) dossier.get("team2_name"), dossier.get("team2_starters"), dossier.get("team2_bench"), dossier.get("team2_missing"));

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
    private void appendLineupsSection(StringBuilder sb, String teamName, Object startersObj, Object benchObj, Object missingObj) {
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
                    boolean isImpactSub = (isAttacker && rating >= 7.0) || rating >= 7.3;
                    sb.append(String.format("  * %s (%s, рейтинг: %.1f)%s\n",
                            pMap.get("name"), pos, rating, isImpactSub ? " ⚡ [Опция усиления атаки / ротации]" : ""));
                }
            }
        }
        if (missingObj instanceof List<?> missing && !missing.isEmpty()) {
            sb.append(String.format("\n### 🚨 ТРАВМЫ И ПОТЕРИ: %s (%d игроков):\n", teamName, missing.size()));
            for (Object m : missing) {
                sb.append(String.format("  * %s\n", m));
            }
        }
    }

    /**
     * Retrieves match dossier directly from MDM Ground Truth service or operational match_record and enriches with Redis odds consensus.
     */
    public Map<String, Object> getMatchDossier(Long matchId) {
        if (matchId == null) return null;

        CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
        if (header == null) {
            log.warn("Match #{} not found in MDM Ground Truth Service", matchId);
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


        // Check if raw lineups exist in sofascore_raw_events and resolve canonical SofaScore team names
        String sofaTeam1 = null;
        String sofaTeam2 = null;
        if (header.getSofascoreId() != null) {
            enrichFromSofaRaw(dossier, header.getSofascoreId());
            try {
                List<Map<String, Object>> sofaRows = jdbcTemplate.queryForList(
                        "SELECT home_team, away_team FROM sofascore_raw_events WHERE event_id = ? LIMIT 1",
                        header.getSofascoreId());
                if (!sofaRows.isEmpty()) {
                    sofaTeam1 = (String) sofaRows.get(0).get("home_team");
                    sofaTeam2 = (String) sofaRows.get(0).get("away_team");
                }
            } catch (Exception ignored) {}
        }

        // Enrich recent matches for team1 and team2 using canonical MDM team IDs first, with exact/fuzzy name fallbacks
        String sportSlug = header.getSportSlug() != null ? header.getSportSlug().toLowerCase() : "football";
        String t1Name = sofaTeam1 != null ? sofaTeam1 : header.getHomeTeamName();
        String t2Name = sofaTeam2 != null ? sofaTeam2 : header.getAwayTeamName();
        dossier.put("team1_recent_matches", fetchRecentMatches(sportSlug, header.getHomeTeamId(), t1Name));
        dossier.put("team2_recent_matches", fetchRecentMatches(sportSlug, header.getAwayTeamId(), t2Name));

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
     * Finds upcoming matches across verified bookmaker fixtures with live Redis odds within lead-time window [now - 15m, now + 36h].
     */
    public List<Long> findUpcomingMultiSportMatchesForEvaluation(int limit) {
        long nowMillis = System.currentTimeMillis();
        long windowFrom = nowMillis; // Calculate strictly from current minute onwards
        long windowTo   = nowMillis + (36L * 60L * 60L * 1000L);

        Set<String> activeOddsMatchIds = redisOddsConsensusService.getActiveMatchIds();
        log.info("Scanner found {} active match IDs in Redis", activeOddsMatchIds != null ? activeOddsMatchIds.size() : 0);

        List<Long> candidates = new ArrayList<>();

        // Priority 1: Canonical MDM matches that have active odds in Redis
        try {
            List<CanonicalMatchHeaderDto> activeMatches = fetchMdmActiveMatches();
            for (CanonicalMatchHeaderDto m : activeMatches) {
                if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
                long startMs = m.getStartTimeEpochSec() * 1000L;
                if (startMs < windowFrom || startMs > windowTo) continue;

                String sportSlug = m.getSportSlug() != null ? m.getSportSlug().toLowerCase() : "";
                if (sportSlug.contains("cyber") || sportSlug.contains("virtual") || sportSlug.contains("synthetic")) {
                    continue;
                }
                String tournament = m.getTournamentName() != null ? m.getTournamentName().toLowerCase() : "";
                if (tournament.contains("short") || tournament.contains("3x3") || tournament.contains("4x4")
                        || tournament.contains("5x5") || tournament.contains("2x2") || tournament.contains("люб")) {
                    continue;
                }

                if (!isAlreadyEvaluated(m.getMatchId())) {
                    if (activeOddsMatchIds != null && !activeOddsMatchIds.isEmpty()) {
                        if (activeOddsMatchIds.contains(m.getMatchId().toString())) {
                            candidates.add(m.getMatchId());
                        }
                    } else {
                        candidates.add(m.getMatchId());
                    }
                    if (candidates.size() >= limit) break;
                }
            }
            log.info("Found {} candidate matches with active odds in MDM", candidates.size());
        } catch (Exception e) {
            log.warn("Failed to query upcoming matches from MDM: {}", e.getMessage());
        }


        log.info("Total candidates selected for AI evaluation: {}", candidates.size());
        return candidates;
    }

    public List<Long> findUpcomingMatchesForEvaluation(int minMinutes, int maxMinutes) {
        long nowMillis = System.currentTimeMillis();
        long startFrom = nowMillis + (minMinutes * 60L * 1000L);
        long startTo   = nowMillis + (maxMinutes * 60L * 1000L);

        List<Long> list = new ArrayList<>();
        // Priority 1: MDM canonical matches
        try {
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
        } catch (Exception e) {
            log.warn("Failed to query upcoming matches from MDM: {}", e.getMessage());
        }

        return list;
    }

    private boolean isAlreadyEvaluated(Long matchId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT lineup_status, evaluation_stage, result_status, match_start_time, sport_name FROM match_ai_recommendation WHERE match_id = ?", matchId);
            if (rows.isEmpty()) return false;
            Map<String, Object> r = rows.get(0);
            String resStatus = (String) r.get("result_status");
            // 1. Settled outcomes are permanently immutable
            if ("WON".equals(resStatus) || "LOST".equals(resStatus) || "VOID".equals(resStatus)) {
                return true;
            }
            String stage = (String) r.get("evaluation_stage");
            String lineupStatus = (String) r.get("lineup_status");
            // 2. Final stage with confirmed lineups already performed
            if ("KICKOFF_VERIFIED".equals(stage) || ("LINEUP_CONFIRMED".equals(stage) && "CONFIRMED".equals(lineupStatus))) {
                return true;
            }
            // 3. Pre-Lineup stage (even if previously marked PASS / SKIPPED or PENDING):
            // Check if official lineups have arrived now!
            Boolean isConfirmed = isLineupNowConfirmed(matchId);
            if (Boolean.TRUE.equals(isConfirmed)) {
                log.info("Match #{} has newly confirmed lineups! Triggering Stage 2 re-evaluation.", matchId);
                return false; // Return false to trigger re-evaluation with confirmed lineups
            }

            // 4. Check if match has entered the final pre-match verification window (sport-specific: 45m football, 15m basketball)
            String sportName = (String) r.get("sport_name");
            int finalWindow = 45;
            if (sportName != null) {
                try {
                    Sport sp = Sports.get(sportName);
                    if (sp != null) {
                        finalWindow = sp.getLineupConfirmationWindowMinutes();
                    }
                } catch (Exception ignored) {}
            }

            Object startTimeObj = r.get("match_start_time");
            long nowMs = System.currentTimeMillis();
            if (startTimeObj instanceof java.sql.Timestamp ts) {
                long diffMinutes = (ts.getTime() - nowMs) / (60L * 1000L);
                if (diffMinutes <= finalWindow && diffMinutes >= -15) {
                    if ("WAIT_KICKOFF".equals(stage) && diffMinutes > 1) {
                        // High-Frequency check: if official protocol arrived between T-45 and T-1, re-evaluate immediately!
                        if (Boolean.TRUE.equals(isLineupNowConfirmed(matchId))) {
                            log.info("Match #{} in WAIT_KICKOFF now has confirmed official lineups! Triggering pre-match re-evaluation.", matchId);
                            return false;
                        }
                        return true; // Still waiting for kickoff (1st minute)
                    }
                    log.info("Match #{} is in final pre-match window (starts in {} mins, window {}m). Triggering Stage 2 evaluation.", matchId, diffMinutes, finalWindow);
                    return false;
                }
            } else if (startTimeObj instanceof java.time.LocalDateTime ldt) {
                long startMs = ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                long diffMinutes = (startMs - nowMs) / (60L * 1000L);
                if (diffMinutes <= finalWindow && diffMinutes >= -15) {
                    if ("WAIT_KICKOFF".equals(stage) && diffMinutes > 1) {
                        // High-Frequency check: if official protocol arrived between T-45 and T-1, re-evaluate immediately!
                        if (Boolean.TRUE.equals(isLineupNowConfirmed(matchId))) {
                            log.info("Match #{} in WAIT_KICKOFF now has confirmed official lineups! Triggering pre-match re-evaluation.", matchId);
                            return false;
                        }
                        return true; // Still waiting for kickoff (1st minute)
                    }
                    log.info("Match #{} is in final pre-match window (starts in {} mins, window {}m). Triggering Stage 2 evaluation.", matchId, diffMinutes, finalWindow);
                    return false;
                }
            }

            // 5. Solo / racket sports or esports without starting-XI lineup announcements
            if (sportName != null) {
                String sUpper = sportName.toUpperCase();
                if (sUpper.contains("TENNIS") || sUpper.contains("ESPORTS") || sUpper.contains("CS2") || sUpper.contains("DOTA") || sUpper.contains("DARTS") || sUpper.contains("SNOOKER") || sUpper.contains("BADMINTON")) {
                    log.info("Match #{} is solo/esport sport '{}'. Triggering Stage 2 confirmed evaluation.", matchId, sportName);
                    return false;
                }
            }

            return true; // Lineups not yet confirmed and kickoff is > finalWindow minutes away
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isLineupNowConfirmed(Long matchId) {
        try {
            CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
            if (header != null && header.getSofascoreId() != null) {
                Integer sofaCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sofascore_raw_events WHERE event_id = ? " +
                        "AND lineups_json IS NOT NULL AND lineups_json != '' AND lineups_json != '{}' " +
                        "AND (lineups_json ILIKE '%\"confirmed\":true%' OR lineups_json ILIKE '%\"confirmed\": true%')",
                        Integer.class, header.getSofascoreId());
                return sofaCount != null && sofaCount > 0;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Actively checks if official lineups are confirmed or triggers an on-demand poll
     * to SofaScore capture service for matches in the pre-kickoff window [T-45 ... T-1].
     */
    public boolean checkOrRefreshLineupsOnDemand(Long matchId) {
        if (Boolean.TRUE.equals(isLineupNowConfirmed(matchId))) {
            return true;
        }
        try {
            CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
            if (header != null && header.getSofascoreId() != null) {
                String onDemandUrl = sofaOnDemandUrl + "/match/" + header.getSofascoreId();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(onDemandUrl))
                        .GET()
                        .timeout(Duration.ofSeconds(3))
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null) {
                    JsonNode node = objectMapper.readTree(resp.body());
                    boolean confirmed = node.path("lineups_confirmed").asBoolean(false);
                    if (confirmed) {
                        log.info("On-demand refresh discovered confirmed lineups for match #{} (sofaEvent={})", matchId, header.getSofascoreId());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("On-demand lineup poll skipped or timed out for match #{}: {}", matchId, e.getMessage());
        }
        return false;
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

        if (sideNode.has("missingPlayers")) {
            List<String> missing = new ArrayList<>();
            for (JsonNode mNode : sideNode.path("missingPlayers")) {
                JsonNode p = mNode.path("player");
                String mName = p.path("name").asText(p.path("shortName").asText("Игрок"));
                String reason = mNode.path("reason").asText(mNode.path("type").asText("Травма / Пропуск"));
                missing.add(mName + " (" + reason + ")");
            }
            if (!missing.isEmpty()) {
                String missingKey = startersKey.replace("starters", "missing");
                dossier.put(missingKey, missing);
            }
        }
    }

private String stripAccents(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("(?i)^(FC|CF|AC|AS|FK|SC|Club|Deportivo|Real)\\s+", "")
                .replaceAll("(?i)\\s+(FC|CF|FK|SC)$", "")
                .trim();
    }

    private List<Map<String, Object>> fetchRecentMatches(String sportSlug, Long teamId, String teamName) {
        String sClean = sportSlug != null ? sportSlug.toLowerCase() : "football";
        sClean = sClean.replace('_', '-');
        if ("hockey".equals(sClean)) sClean = "ice-hockey";
        if ("soccer".equals(sClean)) sClean = "football";

        List<Map<String, Object>> list = new ArrayList<>();

        // Priority 1: O(1) indexed lookup by canonical MDM team ID
        if (teamId != null && teamId > 0) {
            list = fetchRecentMatchesByMdmTeamId(sClean, teamId);
        }

        // Priority 2: Fallback to exact string matching if fewer than 2 matches found by ID
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            List<Map<String, Object>> exactMatches = fetchRecentMatchesInternal(sClean, teamName);
            if (exactMatches.size() > list.size()) {
                list = exactMatches;
            }
        }

        // Priority 3: Fallback to normalized, unaccented and transliterated fuzzy matching
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            String cleanTeam = stripAccents(teamName);
            if (!cleanTeam.equalsIgnoreCase(teamName) && cleanTeam.length() >= 3) {
                List<Map<String, Object>> fallback = fetchRecentMatchesInternal(sClean, cleanTeam);
                if (fallback.size() > list.size()) {
                    list = fallback;
                }
            }
            if (list.size() < 2 && cleanTeam.length() >= 4) {
                List<Map<String, Object>> fuzzy = fetchRecentMatchesFuzzy(sClean, cleanTeam);
                if (fuzzy.size() > list.size()) {
                    list = fuzzy;
                }
            }
            if (list.size() < 2) {
                String transliterated = SlugUtils.transliterate(teamName).trim();
                if (!transliterated.equalsIgnoreCase(teamName)) {
                    List<Map<String, Object>> transMatches = fetchRecentMatchesFuzzy(sClean, transliterated);
                    if (transMatches.size() > list.size()) {
                        list = transMatches;
                    }
                    if (list.size() < 2) {
                        String[] parts = transliterated.split("\\s+");
                        for (String part : parts) {
                            if (part.length() >= 3) {
                                List<Map<String, Object>> partMatches = fetchRecentMatchesFuzzy(sClean, part);
                                if (partMatches.size() > list.size()) {
                                    list = partMatches;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    private List<Map<String, Object>> fetchRecentMatchesByMdmTeamId(String sportSlug, Long teamId) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date, mdm_home_team_id, mdm_away_team_id " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (mdm_home_team_id = ? OR mdm_away_team_id = ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportSlug, teamId, teamId);
            for (Map<String, Object> r : rows) {
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                Number hId = (Number) r.get("mdm_home_team_id");
                boolean isHome = hId != null && hId.longValue() == teamId;

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
            log.debug("Failed MDM ID recent matches for teamId {}: {}", teamId, e.getMessage());
        }
        return list;
    }

    private List<Map<String, Object>> fetchRecentMatchesFuzzy(String sportSlug, String cleanTeam) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (home_team ILIKE ? OR away_team ILIKE ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "  AND home_team NOT ILIKE '%Reserve%' AND away_team NOT ILIKE '%Reserve%' " +
                         "  AND home_team NOT ILIKE '% U1%' AND away_team NOT ILIKE '% U1%' " +
                         "  AND home_team NOT ILIKE '% U2%' AND away_team NOT ILIKE '% U2%' " +
                         "ORDER BY match_date DESC LIMIT 10";
            String pattern = "%" + cleanTeam + "%";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportSlug, pattern, pattern);
            String cleanNorm = SimilarityUtils.normalizeTeamName(cleanTeam);
            for (Map<String, Object> r : rows) {
                String ht = (String) r.get("home_team");
                String at = (String) r.get("away_team");
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();

                String htNorm = SimilarityUtils.normalizeTeamName(ht);
                String atNorm = SimilarityUtils.normalizeTeamName(at);
                boolean homeMatch = htNorm.contains(cleanNorm) || (!cleanNorm.isEmpty() && cleanNorm.contains(htNorm));
                boolean awayMatch = atNorm.contains(cleanNorm) || (!cleanNorm.isEmpty() && cleanNorm.contains(atNorm));
                boolean isHome = homeMatch || !awayMatch;

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
            log.debug("Failed fuzzy recent matches for {}: {}", cleanTeam, e.getMessage());
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
                         "  AND home_team NOT ILIKE '%Reserve%' AND away_team NOT ILIKE '%Reserve%' " +
                         "  AND home_team NOT ILIKE '% U1%' AND away_team NOT ILIKE '% U1%' " +
                         "  AND home_team NOT ILIKE '% U2%' AND away_team NOT ILIKE '% U2%' " +
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
