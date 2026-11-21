package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for historical match querying and recent performance extraction
 * from ClickHouse DDS and relational storage (PostgreSQL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchDossierHistoryService {

    private final JdbcTemplate jdbcTemplate;
    @Autowired(required = false)
    private final ClickHouseDdsClient clickHouse;

    private final Map<Long, Optional<Long>> sofaIdCache = new ConcurrentHashMap<>();

    public List<DossierRecentMatchDto> fetchRecentMatches(Sport sport, Long teamId, String teamName) {
        String sClean = sport != null ? sport.getSlug() : "football";
        List<DossierRecentMatchDto> list = new ArrayList<>();

        // Priority 1: O(1) indexed lookup by canonical MDM team ID in sofascore_raw_events
        if (teamId != null && teamId > 0) {
            list = fetchRecentMatchesByMdmTeamId(sport, teamId);
        }

        // Priority 1.2: Fallback to mdm_match (Unified MDM Catalog across all bookmakers & feeds, 714k matches)
        if (list.size() < 2 && teamId != null && teamId > 0) {
            List<DossierRecentMatchDto> mdmMatches = fetchRecentMatchesFromMdmMatch(sport, teamId);
            if (mdmMatches.size() > list.size()) {
                list = mdmMatches;
            }
        }

        // Priority 1.5: If fewer than 2 matches by ID, check mdm_team canonical name if available
        if (list.size() < 2 && teamId != null && teamId > 0) {
            try {
                String mdmCanonical = jdbcTemplate.queryForObject(
                        "SELECT canonical_name FROM mdm_team WHERE id = ?", String.class, teamId);
                if (mdmCanonical != null && !mdmCanonical.isBlank() && !mdmCanonical.equalsIgnoreCase(teamName)) {
                    List<DossierRecentMatchDto> mdmMatches = fetchRecentMatchesInternal(sport, mdmCanonical);
                    if (mdmMatches.size() > list.size()) {
                        list = mdmMatches;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Priority 2: Resolve canonical teamId from mdm_team_translation (556k aliases) and query MDM
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            Long resolvedId = resolveTeamIdFromTranslation(teamName);
            if (resolvedId != null && resolvedId > 0 && !resolvedId.equals(teamId)) {
                List<DossierRecentMatchDto> transMdm = fetchRecentMatchesFromMdmMatch(sport, resolvedId);
                if (transMdm.size() > list.size()) {
                    list = transMdm;
                }
                if (list.size() < 2) {
                    List<DossierRecentMatchDto> transSofa = fetchRecentMatchesByMdmTeamId(sport, resolvedId);
                    if (transSofa.size() > list.size()) {
                        list = transSofa;
                    }
                }
            }
        }

        // Priority 3: Query ClickHouse DDS by resolved SofaScore ID (Strict ID-Only)
        if (list.size() < 2) {
            Long sofaId = resolveTeamSofaScoreId(teamId, teamName);
            if (sofaId != null && sofaId > 0) {
                List<DossierRecentMatchDto> chMatches = fetchRecentMatchesFromClickHouse(sport, sofaId);
                if (!chMatches.isEmpty()) {
                    Set<Long> existingDates = list.stream()
                            .map(DossierRecentMatchDto::getMatchDate)
                            .filter(Objects::nonNull)
                            .map(val -> val > 0 ? (val < 10_000_000_000L ? val * 1000L : val) : null)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    for (DossierRecentMatchDto chMatch : chMatches) {
                        Long dateMs = chMatch.getMatchDate();
                        if (dateMs == null || !existingDates.contains(dateMs)) {
                            list.add(chMatch);
                            if (dateMs != null) existingDates.add(dateMs);
                        }
                        if (list.size() >= 10) break;
                    }
                    list.sort((a, b) -> {
                        long da = a.getMatchDate() != null ? a.getMatchDate() : 0L;
                        long db = b.getMatchDate() != null ? b.getMatchDate() : 0L;
                        return Long.compare(db, da);
                    });
                }
            }
        }

        return list;
    }

    public Long resolveTeamSofaScoreId(Long teamId, String teamName) {
        if (teamId == null && (teamName == null || teamName.isBlank())) return null;
        Long cacheKey = teamId != null ? teamId : (long) teamName.hashCode();
        return sofaIdCache.computeIfAbsent(cacheKey, k -> {
            try {
                // 1. Direct indexed lookup on mdm_team by primary key (0.1 ms)
                if (teamId != null && teamId > 0) {
                    List<Long> sofaIds = jdbcTemplate.query(
                            "SELECT sofascore_id FROM mdm_team WHERE id = ?",
                            (rs, rowNum) -> rs.getLong("sofascore_id"), teamId);
                    if (!sofaIds.isEmpty() && sofaIds.get(0) != null && sofaIds.get(0) > 0) {
                        return Optional.of(sofaIds.get(0));
                    }
                }

                // 2. Fast B-tree index lookup via mdm_team_translation (0.6 ms)
                String queryName = teamName;
                if (queryName == null || queryName.isBlank()) {
                    if (teamId != null && teamId > 0) {
                        List<String> names = jdbcTemplate.query(
                                "SELECT canonical_name FROM mdm_team WHERE id = ?",
                                (rs, rowNum) -> rs.getString("canonical_name"), teamId);
                        if (!names.isEmpty()) queryName = names.get(0);
                    }
                }

                if (queryName != null && !queryName.isBlank()) {
                    Long transTeamId = resolveTeamIdFromTranslation(queryName);
                    if (transTeamId != null && transTeamId > 0 && !transTeamId.equals(teamId)) {
                        List<Long> sofaIds = jdbcTemplate.query(
                                "SELECT sofascore_id FROM mdm_team WHERE id = ?",
                                (rs, rowNum) -> rs.getLong("sofascore_id"), transTeamId);
                        if (!sofaIds.isEmpty() && sofaIds.get(0) != null && sofaIds.get(0) > 0) {
                            log.info("Resolved entity #{} ('{}') via translation to canonical SofaScore ID #{}",
                                    teamId, queryName, sofaIds.get(0));
                            return Optional.of(sofaIds.get(0));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to resolve sofascore_id for team {} / {}: {}", teamId, teamName, e.getMessage());
            }
            return Optional.empty();
        }).orElse(null);
    }

    public List<DossierRecentMatchDto> fetchRecentMatchesFromClickHouse(Sport sport, Long sofaId) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        if (sofaId == null || sofaId <= 0 || clickHouse == null || sport == null || !sport.isDdsEnabled()) {
            return list;
        }
        try {
            String table = sport.getDdsTableName();
            String p1Col;
            String p2Col;
            String s1Col;
            String s2Col;
            if ("cs2_match_fact".equalsIgnoreCase(table) || "cs2".equalsIgnoreCase(sport.getSlug()) || "counter-strike-2".equalsIgnoreCase(sport.getSlug())) {
                p1Col = "team1_id";
                p2Col = "team2_id";
                s1Col = "team1_score";
                s2Col = "team2_score";
            } else if ("dota2_match_fact".equalsIgnoreCase(table) || "dota2".equalsIgnoreCase(sport.getSlug())) {
                p1Col = "radiant_team_id";
                p2Col = "dire_team_id";
                s1Col = "radiant_score";
                s2Col = "dire_score";
            } else if (sport.isTeamSport()) {
                p1Col = "home_team_id";
                p2Col = "away_team_id";
                s1Col = "home_score";
                s2Col = "away_score";
            } else {
                p1Col = "player1_id";
                p2Col = "player2_id";
                s1Col = "score1";
                s2Col = "score2";
            }
            String tCol = "match_time";

            String sql = "SELECT " +
                    "if(" + p1Col + " = ?, " + s1Col + ", " + s2Col + ") as team_score, " +
                    "if(" + p1Col + " = ?, " + s2Col + ", " + s1Col + ") as opponent_score, " +
                    "if(if(" + p1Col + " = ?, " + s1Col + ", " + s2Col + ") > if(" + p1Col + " = ?, " + s2Col + ", " + s1Col + "), 'WIN', 'LOSS') as result, " +
                    "toUnixTimestamp(" + tCol + ") * 1000 as match_date " +
                    "FROM " + table + " " +
                    "WHERE " + p1Col + " = ? OR " + p2Col + " = ? " +
                    "ORDER BY " + tCol + " DESC LIMIT 10";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("recent_ch:" + table + ":" + sofaId, sql,
                    sofaId, sofaId, sofaId, sofaId, sofaId, sofaId);
            for (Map<String, Object> r : rows) {
                int ts = toIntSafe(r.get("team_score"), 0);
                int os = toIntSafe(r.get("opponent_score"), 0);
                long dateMs = toLongSafe(r.get("match_date"), 0L);
                String res = String.valueOf(r.getOrDefault("result", ts > os ? "WIN" : "LOSS"));

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(ts)
                        .opponentScore(os)
                        .matchDate(dateMs)
                        .cleanSheet(os == 0)
                        .failedToScore(ts == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed ClickHouse DDS recent matches for {} sofaId {}: {}", sport.getSlug(), sofaId, e.getMessage());
        }
        return list;
    }

    public List<DossierRecentMatchDto> fetchRecentMatchesByMdmTeamId(Sport sport, Long teamId) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date, mdm_home_team_id, mdm_away_team_id " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (mdm_home_team_id = ? OR mdm_away_team_id = ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sport.getSlug(), teamId, teamId);
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

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed MDM ID recent matches for teamId {}: {}", teamId, e.getMessage());
        }
        return list;
    }

    public List<DossierRecentMatchDto> fetchRecentMatchesFromMdmMatch(Sport sport, Long teamId) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        if (teamId == null || teamId <= 0) return list;
        try {
            Integer sportId = resolveMdmSportId(sport);
            String sql;
            List<Object> params = new ArrayList<>();
            if (sportId != null && sportId > 0) {
                sql = "SELECT id, start_time, COALESCE(regular_score1, score1) as score1, COALESCE(regular_score2, score2) as score2, home_team_id, away_team_id " +
                      "FROM mdm_match " +
                      "WHERE sport_id = ? AND (home_team_id = ? OR away_team_id = ?) " +
                      "  AND status IN ('FINISHED', 'SETTLED') AND (score1 IS NOT NULL OR regular_score1 IS NOT NULL) " +
                      "ORDER BY start_time DESC LIMIT 10";
                params.add(sportId);
                params.add(teamId);
                params.add(teamId);
            } else {
                sql = "SELECT id, start_time, COALESCE(regular_score1, score1) as score1, COALESCE(regular_score2, score2) as score2, home_team_id, away_team_id " +
                      "FROM mdm_match " +
                      "WHERE (home_team_id = ? OR away_team_id = ?) " +
                      "  AND status IN ('FINISHED', 'SETTLED') AND (score1 IS NOT NULL OR regular_score1 IS NOT NULL) " +
                      "ORDER BY start_time DESC LIMIT 10";
                params.add(teamId);
                params.add(teamId);
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
            for (Map<String, Object> r : rows) {
                String s1 = String.valueOf(r.get("score1")).trim();
                String s2 = String.valueOf(r.get("score2")).trim();
                if (s1.isEmpty() || s2.isEmpty() || "null".equalsIgnoreCase(s1) || "null".equalsIgnoreCase(s2)) continue;
                int hs, as;
                try {
                    hs = Integer.parseInt(s1);
                    as = Integer.parseInt(s2);
                } catch (NumberFormatException ignored) {
                    continue;
                }

                Number hId = (Number) r.get("home_team_id");
                boolean isHome = hId != null && hId.longValue() == teamId;

                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                Object stObj = r.get("start_time");
                long dateMs = 0L;
                if (stObj instanceof java.sql.Timestamp ts) {
                    dateMs = ts.getTime();
                } else if (stObj instanceof java.util.Date d) {
                    dateMs = d.getTime();
                }

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(dateMs)
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed mdm_match recent matches for teamId {}: {}", teamId, e.getMessage());
        }
        return list;
    }

    public Long resolveTeamIdFromTranslation(String teamName) {
        if (teamName == null || teamName.isBlank()) return null;
        try {
            String raw = teamName.trim();
            String lower = raw.toLowerCase(Locale.ROOT);
            String unaccented = stripAccents(lower);
            String translit = SimilarityUtils.hasCyrillic(raw) ? SimilarityUtils.transliterateCyrillic(raw).trim().toLowerCase(Locale.ROOT) : null;

            Set<String> candidates = new LinkedHashSet<>();
            candidates.add(lower);
            if (!unaccented.equals(lower)) candidates.add(unaccented);
            if (translit != null && !translit.equals(lower)) candidates.add(translit);

            for (String cand : candidates) {
                if (cand.length() < 3) continue;
                List<Long> ids = jdbcTemplate.query(
                        "SELECT team_id FROM mdm_team_translation WHERE LOWER(name) = ? OR LOWER(short_name) = ? ORDER BY id ASC LIMIT 1",
                        (rs, rowNum) -> rs.getLong("team_id"),
                        cand, cand);
                if (!ids.isEmpty() && ids.get(0) != null && ids.get(0) > 0) {
                    return ids.get(0);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<DossierRecentMatchDto> fetchRecentMatchesInternal(Sport sport, String teamName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        if (teamName == null || teamName.isBlank()) return list;
        try {
            String cleanLower = teamName.trim().toLowerCase(Locale.ROOT);
            String sportSlug = sport != null ? sport.getSlug().toLowerCase(Locale.ROOT) : "football";
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (LOWER(home_team) = ? OR LOWER(away_team) = ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 15";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportSlug, cleanLower, cleanLower);
            for (Map<String, Object> r : rows) {
                String ht = (String) r.get("home_team");
                String at = (String) r.get("away_team");
                if (isReserveOrYouth(ht) || isReserveOrYouth(at)) continue;

                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                boolean isHome = cleanLower.equalsIgnoreCase(ht);
                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
                if (list.size() >= 10) break;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch recent matches for team {}: {}", teamName, e.getMessage());
        }
        return list;
    }

    private boolean isReserveOrYouth(String name) {
        if (name == null || name.isBlank()) return false;
        return TeamSignatureNormalizer.parse(name).isYouth();
    }

    private Integer resolveMdmSportId(Sport sport) {
        return sport != null ? sport.getId() : null;
    }

    public String stripAccents(String s) {
        if (s == null || s.isBlank()) return "";
        String unaccented = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return TeamSignatureNormalizer.parse(unaccented).coreClubName().trim();
    }

    private static int toIntSafe(Object obj, int def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return def;
        }
    }

    private static long toLongSafe(Object obj, long def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return def;
        }
    }
}
