package pro.datawiki.igaming.analyst.ml.vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * High-speed HTTP client querying ClickHouse igaming_dds.
 * Caches aggregate metrics in-memory to deliver < 1ms vector building.
 */
@Slf4j
@Component
public class ClickHouseDdsClient {

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String database;
    private final String user;
    private final String password;
    private final int timeoutMs;

    private static final Map<String, CacheEntry> QUERY_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 600_000L; // 10 minutes

    private record CacheEntry(long timestamp, List<Map<String, Object>> data) {}

    public record TennisRanking(
            long playerId,
            String playerName,
            int currentRank,
            int peakRank,
            double quarticStature
    ) {}

    public record TennisMmr(
            long playerId,
            String playerName,
            String surface,
            double mmr,
            double rd,
            int matchesPlayed,
            int streak,
            double peakMmr
    ) {}

    /**
     * Resolves Universal Dynamic MMR from ClickHouse sport_mmr_rating across any sport and surface.
     * Checks requested surface first, falling back to overall 'ALL' surface.
     */
    public Optional<TennisMmr> getSportMmr(Long playerId, String sportSlug, String surface) {
        if (playerId == null || playerId <= 0) return Optional.empty();
        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required and cannot be null or blank");
        }
        String cleanSport = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');
        String cleanSurf = (surface != null && !surface.isBlank()) ? surface.toUpperCase(Locale.ROOT) : null;
        String cacheKey = "sport_mmr:" + cleanSport + ":" + playerId + ":" + (cleanSurf != null ? cleanSurf : "ALL");
        String sql;
        List<Map<String, Object>> rows;
        if (cleanSurf != null && !"ALL".equals(cleanSurf)) {
            sql = "SELECT player_id, player_name, surface, mmr, rating_deviation, matches_played, " +
                    "current_streak, peak_mmr FROM igaming_dds.sport_mmr_rating " +
                    "WHERE player_id = ? AND sport_slug = ? AND surface IN (?, 'ALL') " +
                    "ORDER BY if(surface = ?, 1, 2) LIMIT 1";
            rows = queryJsonCached(cacheKey, sql, playerId, cleanSport, cleanSurf, cleanSurf);
        } else {
            sql = "SELECT player_id, player_name, surface, mmr, rating_deviation, matches_played, " +
                    "current_streak, peak_mmr FROM igaming_dds.sport_mmr_rating " +
                    "WHERE player_id = ? AND sport_slug = ? AND surface = 'ALL' LIMIT 1";
            rows = queryJsonCached(cacheKey, sql, playerId, cleanSport);
        }
        if (rows.isEmpty()) return Optional.empty();

        Map<String, Object> r = rows.get(0);
        return Optional.of(new TennisMmr(
                parseLong(r.get("player_id"), playerId),
                String.valueOf(r.getOrDefault("player_name", "")),
                String.valueOf(r.getOrDefault("surface", cleanSurf != null ? cleanSurf : "ALL")),
                toDouble(r.get("mmr"), 1500.0),
                toDouble(r.get("rating_deviation"), 350.0),
                toInt(r.get("matches_played"), 0),
                toInt(r.get("current_streak"), 0),
                toDouble(r.get("peak_mmr"), 1500.0)
        ));
    }

    /**
     * Resolves Universal Dynamic MMR from ClickHouse sport_mmr_rating for Tennis.
     */
    public Optional<TennisMmr> getTennisMmr(Long playerId, String surface) {
        return getSportMmr(playerId, "tennis", surface);
    }

    /**
     * Upserts an active sport MMR rating into ClickHouse igaming_dds.sport_mmr_rating.
     * ReplacingMergeTree engine automatically deduplicates on (player_id, sport_slug, surface).
     */
    public void upsertSportMmrRating(pro.datawiki.igaming.analyst.rating.SportMmrRatingEntity rating) {
        if (rating == null || rating.getPlayerId() == null) return;
        if (rating.getSportSlug() == null || rating.getSportSlug().isBlank()) {
            throw new IllegalArgumentException("rating.sportSlug is required and cannot be null or blank");
        }
        if (rating.getSurface() == null || rating.getSurface().isBlank()) {
            throw new IllegalArgumentException("rating.surface is required and cannot be null or blank");
        }
        try {
            String pName = rating.getPlayerName() != null ? rating.getPlayerName().replace("'", "\\'") : "";
            String cCode = rating.getCountry() != null ? rating.getCountry().replace("'", "\\'") : "";
            String sql = String.format(Locale.ROOT,
                    "INSERT INTO igaming_dds.sport_mmr_rating " +
                    "(player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, last_match_at, updated_at) " +
                    "VALUES (%d, '%s', '%s', %.2f, %.2f, %.6f, %d, %d, %d, %d, %.2f, '%s', '%s', now(), now())",
                    rating.getPlayerId(),
                    rating.getSportSlug(),
                    rating.getSurface(),
                    rating.getMmr() != null ? rating.getMmr() : 1500.0,
                    rating.getRatingDeviation() != null ? rating.getRatingDeviation() : 350.0,
                    rating.getVolatility() != null ? rating.getVolatility() : 0.06,
                    rating.getMatchesPlayed() != null ? rating.getMatchesPlayed() : 0,
                    rating.getWinsCount() != null ? rating.getWinsCount() : 0,
                    rating.getLossesCount() != null ? rating.getLossesCount() : 0,
                    rating.getCurrentStreak() != null ? rating.getCurrentStreak() : 0,
                    rating.getPeakMmr() != null ? rating.getPeakMmr() : 1500.0,
                    pName, cCode
            );
            execute(sql);
        } catch (Exception e) {
            log.warn("Failed to upsert sport_mmr_rating into ClickHouse for player #{}: {}", rating.getPlayerId(), e.getMessage());
        }
    }

    private final AtomicReference<Map<String, TennisRanking>> tennisRankingsCache = new AtomicReference<>(null);
    private final AtomicLong tennisRankingsCacheTimestamp = new AtomicLong(0);

    public ClickHouseDdsClient(
            ObjectMapper objectMapper,
            @Value("${analyst.clickhouse.url:http://clickhouse.igaming-analytics.svc.cluster.local:8123}") String baseUrl,
            @Value("${analyst.clickhouse.database:igaming_dds}") String database,
            @Value("${analyst.clickhouse.user:default}") String user,
            @Value("${analyst.clickhouse.password:clickhouse}") String password,
            @Value("${analyst.clickhouse.timeout-seconds:3}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.database = database;
        this.user = user;
        this.password = password;
        this.timeoutMs = timeoutSeconds * 1000;
    }

    /**
     * Executes a SELECT query against ClickHouse DDS with caching.
     * Returns a list of rows as Map<String, Object>.
     */
    public List<Map<String, Object>> queryJsonCached(String cacheKey, String sql) {
        long now = System.currentTimeMillis();
        CacheEntry cached = QUERY_CACHE.get(cacheKey);
        if (cached != null && (now - cached.timestamp() < CACHE_TTL_MS)) {
            return cached.data();
        }

        List<Map<String, Object>> result = queryJson(sql);
        List<Map<String, Object>> safeResult = result != null ? result : Collections.emptyList();
        QUERY_CACHE.put(cacheKey, new CacheEntry(now, safeResult));
        return safeResult;
    }

    public List<Map<String, Object>> queryJsonCached(String cacheKey, String sqlTemplate, Object... params) {
        return queryJsonCached(cacheKey, bindParams(sqlTemplate, params));
    }

    public List<Map<String, Object>> queryJson(String sqlTemplate, Object... params) {
        return queryJson(bindParams(sqlTemplate, params));
    }

    public static String bindParams(String sqlTemplate, Object... params) {
        if (params == null || params.length == 0) return sqlTemplate;
        StringBuilder sb = new StringBuilder();
        int paramIdx = 0;
        for (int i = 0; i < sqlTemplate.length(); i++) {
            char c = sqlTemplate.charAt(i);
            if (c == '?' && paramIdx < params.length) {
                Object p = params[paramIdx++];
                sb.append(formatClickHouseLiteral(p));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String formatClickHouseLiteral(Object p) {
        if (p == null) return "NULL";
        if (p instanceof Number) return p.toString();
        if (p instanceof Boolean b) return b ? "1" : "0";
        if (p instanceof Collection<?> coll) {
            StringJoiner sj = new StringJoiner(", ", "(", ")");
            for (Object item : coll) {
                sj.add(formatClickHouseLiteral(item));
            }
            return sj.toString();
        }
        return "'" + p.toString().replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    /**
     * Retrieves Point-in-Time tournament standings and precalculated motivation gradients
     * captured immediately before match kickoff.
     */
    public Map<String, Object> getTournamentPointInTime(Long mdmMatchId) {
        if (mdmMatchId == null || mdmMatchId <= 0) {
            return Collections.emptyMap();
        }
        String sql = "SELECT mdm_match_id, home_blowout_gradient, away_blowout_gradient, " +
                "home_win_leverage, away_win_leverage, home_elimination_hazard, away_elimination_hazard, " +
                "home_prize_wage_ratio, away_prize_wage_ratio, group_entropy, is_group_of_death, is_knockout_stage " +
                "FROM tournament_point_in_time_fact WHERE mdm_match_id = ? LIMIT 1";
        List<Map<String, Object>> rows = queryJsonCached("tourn_pit:" + mdmMatchId, sql, mdmMatchId);
        return (rows != null && !rows.isEmpty()) ? rows.get(0) : Collections.emptyMap();
    }

    /**
     * Resolves tennis player ranking and quartic stature from in-memory multi-key index.
     * Supports exact name, stripped accents (Cilic <-> Čilić), Cyrillic transliterations,
     * token inversions (First Last <-> Last First), surname+initials ("Djokovic N."), and unique surnames.
     * Operates in 0ms with zero SQL network roundtrips.
     */
    public Optional<TennisRanking> resolveTennisPlayerRanking(String rawName) {
        if (rawName == null || rawName.isBlank()) return Optional.empty();

        long now = System.currentTimeMillis();
        Map<String, TennisRanking> cache = tennisRankingsCache.get();
        if (cache == null || (now - tennisRankingsCacheTimestamp.get() > 3600_000L)) {
            cache = loadAllTennisRankings();
            if (cache != null && !cache.isEmpty()) {
                tennisRankingsCache.set(cache);
                tennisRankingsCacheTimestamp.set(now);
            }
        }
        if (cache == null || cache.isEmpty()) return Optional.empty();

        String clean = rawName.replaceAll("\\s*\\([^)]*\\)", "").trim();
        if (clean.isBlank()) return Optional.empty();

        String latin = SimilarityUtils.hasCyrillic(clean) ? SimilarityUtils.transliterateCyrillic(clean).trim() : clean;
        String normalized = stripAccents(latin).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        TennisRanking found = cache.get(normalized);
        if (found != null) return Optional.of(found);

        String[] tokens = normalized.split(" ");
        if (tokens.length >= 2) {
            String inverted = tokens[tokens.length - 1] + " " + tokens[0];
            found = cache.get(inverted);
            if (found != null) return Optional.of(found);

            String initialKey = tokens[0] + " " + tokens[1].charAt(0);
            found = cache.get(initialKey);
            if (found != null) return Optional.of(found);

            String initialKeyInverted = tokens[tokens.length - 1] + " " + tokens[0].charAt(0);
            found = cache.get(initialKeyInverted);
            if (found != null) return Optional.of(found);
        }

        for (String tok : tokens) {
            if (tok.length() >= 4) {
                found = cache.get("surname:" + tok);
                if (found != null) return Optional.of(found);
            }
        }

        return Optional.empty();
    }

    private Map<String, TennisRanking> loadAllTennisRankings() {
        try {
            String sql = "SELECT player_id, player_name, current_rank, peak_rank, quartic_stature FROM igaming_dds.tennis_player_rankings";
            List<Map<String, Object>> rows = queryJson(sql);
            if (rows.isEmpty()) return Collections.emptyMap();

            Map<String, TennisRanking> map = new HashMap<>();
            Map<String, List<TennisRanking>> surnameGroups = new HashMap<>();

            for (Map<String, Object> r : rows) {
                long pid = parseLong(r.get("player_id"), 0L);
                String pName = String.valueOf(r.getOrDefault("player_name", ""));
                int curRank = toInt(r.get("current_rank"), 1500);
                int peakRank = toInt(r.get("peak_rank"), curRank);
                double qs = toDouble(r.get("quartic_stature"), 0.0);
                if (qs <= 0.0 && curRank > 0 && curRank <= 2000) {
                    qs = Math.pow(curRank, -0.25);
                }
                TennisRanking ranking = new TennisRanking(pid, pName, curRank, peakRank, qs);

                String clean = stripAccents(pName).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
                map.put(clean, ranking);

                String[] parts = clean.split(" ");
                if (parts.length >= 2) {
                    map.put(parts[parts.length - 1] + " " + parts[0], ranking);
                    map.put(parts[parts.length - 1] + " " + parts[0].charAt(0), ranking);
                    map.put(parts[0].charAt(0) + " " + parts[parts.length - 1], ranking);

                    String surname = parts[parts.length - 1];
                    if (surname.length() >= 4) {
                        surnameGroups.computeIfAbsent(surname, k -> new ArrayList<>()).add(ranking);
                    }
                }
            }

            for (Map.Entry<String, List<TennisRanking>> e : surnameGroups.entrySet()) {
                List<TennisRanking> list = e.getValue();
                if (list.size() == 1) {
                    map.put("surname:" + e.getKey(), list.get(0));
                } else {
                    list.sort(Comparator.comparingInt(TennisRanking::currentRank));
                    if (list.get(0).currentRank() <= 100 && list.get(1).currentRank() > 300) {
                        map.put("surname:" + e.getKey(), list.get(0));
                    }
                }
            }

            log.info("Loaded {} tennis player rankings into fast in-memory multi-key index ({} lookup keys)",
                    rows.size(), map.size());
            return map;
        } catch (Exception e) {
            log.warn("Failed to load tennis rankings from ClickHouse: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private double toDouble(Object obj, double fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Retrieves dynamic tournament scoring baseline and league pace/scoring category from ClickHouse DDS.
     * Computes regulation-time statistics (excluding overtime and penalty shootouts).
     */
    public Map<String, Object> getTournamentScoringBaseline(String sport, Long tournamentId, String tournamentName) {
        if (sport == null) return Collections.emptyMap();
        String cleanSport = sport.toLowerCase().replace('_', '-');
        if ("soccer".equals(cleanSport)) cleanSport = "football";
        if ("hockey".equals(cleanSport)) cleanSport = "ice-hockey";

        String cacheKey = "tourn_base:" + cleanSport + ":" + (tournamentId != null ? tournamentId : (tournamentName != null ? tournamentName : "all"));

        String sql;
        List<Map<String, Object>> rows;
        if (tournamentId != null && tournamentId > 0) {
            sql = "SELECT regulation_median_total, cushion_low_total, cushion_high_total, handicap_cushion_delta, scoring_category " +
                    "FROM igaming_dds.tournament_scoring_baseline FINAL WHERE sport = ? AND tournament_id = ? LIMIT 1";
            rows = queryJsonCached(cacheKey, sql, cleanSport, tournamentId);
        } else if (tournamentName != null && !tournamentName.isBlank()) {
            sql = "SELECT regulation_median_total, cushion_low_total, cushion_high_total, handicap_cushion_delta, scoring_category " +
                    "FROM igaming_dds.tournament_scoring_baseline FINAL WHERE sport = ? AND tournament_name = ? LIMIT 1";
            rows = queryJsonCached(cacheKey, sql, cleanSport, tournamentName.trim());
        } else {
            return Collections.emptyMap();
        }

        if (rows != null && !rows.isEmpty()) {
            return rows.get(0);
        }
        return Collections.emptyMap();
    }

    /**
     * Retrieves dynamic country/region scoring baseline from ClickHouse DDS.
     * Used when a tournament has no seasonal statistics or is an international/friendly cross-match.
     */
    public Map<String, Object> getCountryScoringBaseline(String sport, Integer countryId, String gender, String ageCategory) {
        if (sport == null || countryId == null || countryId <= 0) return Collections.emptyMap();
        String cleanSport = sport.toLowerCase().replace('_', '-');
        if ("soccer".equals(cleanSport)) cleanSport = "football";
        if ("hockey".equals(cleanSport)) cleanSport = "ice-hockey";

        String g = (gender != null && !gender.isBlank()) ? gender.toUpperCase().trim() : "MALE";
        String a = (ageCategory != null && !ageCategory.isBlank()) ? ageCategory.toUpperCase().trim() : "SENIOR";

        String cacheKey = "country_base:" + cleanSport + ":" + countryId + ":" + g + ":" + a;
        String sql = "SELECT regulation_median_total, cushion_low_total, cushion_high_total, scoring_category " +
                "FROM igaming_dds.country_scoring_baseline FINAL WHERE sport = ? AND country_id = ? AND gender = ? AND age_category = ? LIMIT 1";

        List<Map<String, Object>> rows = queryJsonCached(cacheKey, sql, cleanSport, countryId, g, a);
        if (rows != null && !rows.isEmpty()) {
            return rows.get(0);
        }

        // Demographic fallback 1: If youth tier not found, try SENIOR
        if (!"SENIOR".equals(a)) {
            String fbKey = "country_base:" + cleanSport + ":" + countryId + ":" + g + ":SENIOR";
            String fbSql = "SELECT regulation_median_total, cushion_low_total, cushion_high_total, scoring_category " +
                    "FROM igaming_dds.country_scoring_baseline FINAL WHERE sport = ? AND country_id = ? AND gender = ? LIMIT 1";
            List<Map<String, Object>> fbRows = queryJsonCached(fbKey, fbSql, cleanSport, countryId, g);
            if (fbRows != null && !fbRows.isEmpty()) {
                return fbRows.get(0);
            }
        }

        // Demographic fallback 2: Any gender in this country
        String fbAnyKey = "country_base:" + cleanSport + ":" + countryId + ":any";
        String fbAnySql = "SELECT regulation_median_total, cushion_low_total, cushion_high_total, scoring_category " +
                "FROM igaming_dds.country_scoring_baseline FINAL WHERE sport = ? AND country_id = ? LIMIT 1";
        List<Map<String, Object>> fbAnyRows = queryJsonCached(fbAnyKey, fbAnySql, cleanSport, countryId);
        if (fbAnyRows != null && !fbAnyRows.isEmpty()) {
            return fbAnyRows.get(0);
        }

        return Collections.emptyMap();
    }

    /**
     * Retrieves empirical referee discipline statistics from match_discipline_benchmark_v.
     * Contains average yellow cards, total fouls, under-card win rates, and low-friction stalemate frequencies.
     */
    public Map<String, Object> getRefereeDiscipline(String sport, String refereeName) {
        if (refereeName == null || refereeName.isBlank()) return Collections.emptyMap();
        String cleanSport = sport != null ? sport.toLowerCase().replace('_', '-') : "football";
        if ("soccer".equals(cleanSport)) cleanSport = "football";
        String cacheKey = "ref_disc:" + cleanSport + ":" + refereeName.trim();
        String sql = "SELECT avg_yellow_cards, total_fouls, under_card_win_rate, low_friction_stalemate_freq " +
                "FROM igaming_dds.match_discipline_benchmark_v WHERE sport = ? AND referee_name = ? LIMIT 1";
        List<Map<String, Object>> rows = queryJsonCached(cacheKey, sql, cleanSport, refereeName.trim());
        return (rows != null && !rows.isEmpty()) ? rows.get(0) : Collections.emptyMap();
    }

    /**
     * Retrieves physiological injury degradation percentages for players in their return match
     * or suffering from active tissue elasticity decline.
     */
    public Map<Long, Float> getPlayerInjuryDegradation(Collection<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyMap();
        List<Long> valid = playerIds.stream().filter(id -> id != null && id > 0).toList();
        if (valid.isEmpty()) return Collections.emptyMap();

        String cacheKey = "inj_deg:" + valid.hashCode();
        String sql = "SELECT player_id, max(rating_degradation_pct) as degradation " +
                "FROM igaming_dds.player_injury_absence_fact " +
                "WHERE player_id IN ? " +
                "GROUP BY player_id";

        List<Map<String, Object>> rows = queryJsonCached(cacheKey, sql, valid);
        if (rows == null || rows.isEmpty()) return Collections.emptyMap();

        Map<Long, Float> map = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object pidObj = r.get("player_id");
            Object degObj = r.get("degradation");
            if (pidObj != null && degObj instanceof Number n) {
                try {
                    map.put(Long.parseLong(pidObj.toString()), n.floatValue());
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    public record PlayerMembershipFact(
            long sofascoreId,
            long mdmPlayerId,
            long mdmTeamId,
            String membershipType,
            boolean isActive,
            long firstMatchTs,
            long lastMatchTs,
            int totalMatches
    ) {}

    /**
     * Retrieves player membership timeline from igaming_dds.player_membership_timeline_v.
     * Contains club tenure, debut/recent match dates, match counts, and national team appearances.
     */
    public List<PlayerMembershipFact> getPlayerMemberships(Collection<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyList();
        List<Long> valid = playerIds.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (valid.isEmpty()) return Collections.emptyList();

        String cacheKey = "pl_memberships:" + valid.hashCode();
        String sql = "SELECT player_sofascore_id, mdm_player_id, mdm_team_id, membership_type, is_active, " +
                "first_match_ts, last_match_ts, total_matches " +
                "FROM igaming_dds.player_membership_timeline_v " +
                "WHERE player_sofascore_id IN ? OR mdm_player_id IN ?";

        List<Map<String, Object>> rows = queryJsonCached(cacheKey, sql, valid, valid);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<PlayerMembershipFact> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            long sofaId = parseLong(r.get("player_sofascore_id"), 0L);
            long mdmId = parseLong(r.get("mdm_player_id"), 0L);
            long teamId = parseLong(r.get("mdm_team_id"), 0L);
            String mtype = String.valueOf(r.getOrDefault("membership_type", "CLUB_PRIMARY"));
            boolean active = parseLong(r.get("is_active"), 1L) == 1L;
            long firstTs = parseLong(r.get("first_match_ts"), 0L);
            long lastTs = parseLong(r.get("last_match_ts"), 0L);
            int matches = toInt(r.get("total_matches"), 0);

            result.add(new PlayerMembershipFact(sofaId, mdmId, teamId, mtype, active, firstTs, lastTs, matches));
        }
        return result;
    }

    private long parseLong(Object obj, long fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

    private int toInt(Object obj, int fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Executes non-query DDL or DML statement (e.g. INSERT) against ClickHouse HTTP interface.
     */
    public void execute(String sql) {
        try {
            URL url = new URL(baseUrl + "/?database=" + database);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            if (user != null && !user.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-User", user);
            }
            if (password != null && !password.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-Key", password);
            }

            conn.getOutputStream().write(sql.trim().getBytes(StandardCharsets.UTF_8));

            int code = conn.getResponseCode();
            if (code >= 400) {
                try (InputStream errStream = conn.getErrorStream()) {
                    String err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "";
                    log.warn("[ClickHouse Client] Execute failed [{}] {}: {}", code, sql, err);
                }
            }
        } catch (Exception e) {
            log.warn("[ClickHouse Client] Execute error for {}: {}", sql, e.getMessage());
        }
    }

    /**
     * Executes raw query against ClickHouse HTTP interface with JSONEachRow format.
     */
    public List<Map<String, Object>> queryJson(String sql) {
        try {
            String fullSql = sql.trim();
            if (!fullSql.toUpperCase().contains("FORMAT JSONEACHROW")) {
                fullSql += " FORMAT JSONEachRow";
            }

            URL url = new URL(baseUrl + "/?database=" + database);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            if (user != null && !user.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-User", user);
            }
            if (password != null && !password.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-Key", password);
            }

            byte[] body = fullSql.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(body);

            int code = conn.getResponseCode();
            if (code >= 400) {
                try (InputStream errStream = conn.getErrorStream()) {
                    String err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "";
                    log.warn("[ClickHouse Client] Query failed [{}] {}: {}", code, sql, err);
                }
                return Collections.emptyList();
            }

            try (InputStream in = conn.getInputStream()) {
                String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (raw.isBlank()) {
                    return Collections.emptyList();
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                for (String line : raw.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        Map<String, Object> row = objectMapper.readValue(trimmed, new TypeReference<>() {});
                        rows.add(row);
                    }
                }
                return rows;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            log.warn("[ClickHouse Client] Query exception for {}: {}", sql, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Streams TabSeparated results line-by-line via HTTP streaming to prevent heap exhaustion.
     */
    public long streamTsv(String sql, Consumer<String[]> rowConsumer) {
        long count = 0;
        try {
            String fullSql = sql.trim();
            if (!fullSql.toUpperCase().contains("FORMAT TABSEPARATED")) {
                fullSql += " FORMAT TabSeparated";
            }

            URL url = new URL(baseUrl + "/?database=" + database);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(180000);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            if (user != null && !user.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-User", user);
            }
            if (password != null && !password.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-Key", password);
            }

            conn.getOutputStream().write(fullSql.getBytes(StandardCharsets.UTF_8));

            int code = conn.getResponseCode();
            if (code >= 400) {
                try (InputStream errStream = conn.getErrorStream()) {
                    String err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "";
                    log.error("[ClickHouse Client] Streaming failed [{}] {}: {}", code, sql, err);
                }
                return 0;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8), 65536)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    String[] tokens = line.split("\t", -1);
                    rowConsumer.accept(tokens);
                    count++;
                }
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            log.error("[ClickHouse Client] Stream exception for {}: {}", sql, e.getMessage(), e);
        }
        return count;
    }

    /**
     * High-speed bulk TSV insert directly into ClickHouse HTTP endpoint.
     */
    public boolean insertTsv(String tableWithColumns, byte[] tsvBytes) {
        if (tsvBytes == null || tsvBytes.length == 0) return true;
        try {
            String queryParam = URLEncoder.encode("INSERT INTO " + tableWithColumns + " FORMAT TabSeparated", StandardCharsets.UTF_8);
            URL url = new URL(baseUrl + "/?database=" + database + "&query=" + queryParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(180000);
            conn.setRequestProperty("Content-Type", "text/tab-separated-values; charset=UTF-8");

            if (user != null && !user.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-User", user);
            }
            if (password != null && !password.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-Key", password);
            }

            conn.getOutputStream().write(tsvBytes);

            int code = conn.getResponseCode();
            if (code >= 400) {
                try (InputStream errStream = conn.getErrorStream()) {
                    String err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "";
                    log.error("[ClickHouse Client] Bulk insert failed [{}] into {}: {}", code, tableWithColumns, err);
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[ClickHouse Client] Bulk insert error into {}: {}", tableWithColumns, e.getMessage(), e);
            return false;
        }
    }
}
