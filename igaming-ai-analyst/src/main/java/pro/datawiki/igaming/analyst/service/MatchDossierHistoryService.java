package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
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
                    if (list.size() < 2) {
                        List<DossierRecentMatchDto> aggCanonical = fetchRecentMatchesFromAggregatorHistory(sport, mdmCanonical);
                        if (aggCanonical.size() > list.size()) {
                            list = aggCanonical;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Priority 2: Fallback to exact string matching in sofascore_raw_events
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            List<DossierRecentMatchDto> exactMatches = fetchRecentMatchesInternal(sport, teamName);
            if (exactMatches.size() > list.size()) {
                list = exactMatches;
            }
        }

        // Priority 2.2: Fallback to team_match_history (Aggregator History Engine across 19 sports, 76k precomputed matches)
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            List<DossierRecentMatchDto> aggMatches = fetchRecentMatchesFromAggregatorHistory(sport, teamName);
            if (aggMatches.size() > list.size()) {
                list = aggMatches;
            }
        }

        // Priority 2.5: Fallback to stripped gender/age markers
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            String base = teamName.replaceAll("(?i)\\s*\\([жw]\\)", "")
                    .replaceAll("(?i)\\s*\\(жен\\)", "")
                    .replaceAll("(?i)\\s*\\(women\\)", "")
                    .replaceAll("(?i)\\s*\\(d\\)", "")
                    .replaceAll("(?i)\\s*\\(games\\)", "")
                    .trim();
            if (!base.equalsIgnoreCase(teamName) && base.length() >= 3) {
                List<DossierRecentMatchDto> baseMatches = fetchRecentMatchesInternal(sport, base);
                if (baseMatches.size() > list.size()) {
                    list = baseMatches;
                }
                if (list.size() < 2) {
                    List<DossierRecentMatchDto> aggBase = fetchRecentMatchesFromAggregatorHistory(sport, base);
                    if (aggBase.size() > list.size()) {
                        list = aggBase;
                    }
                }
            }
        }

        // Priority 3: Cross-language Cyrillic -> Latin transliteration using SimilarityUtils
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            if (SimilarityUtils.hasCyrillic(teamName)) {
                String cyrTranslit = SimilarityUtils.transliterateCyrillic(teamName).trim();
                if (!cyrTranslit.equalsIgnoreCase(teamName) && cyrTranslit.length() >= 3) {
                    List<DossierRecentMatchDto> cyrMatches = fetchRecentMatchesInternal(sport, cyrTranslit);
                    if (cyrMatches.size() > list.size()) {
                        list = cyrMatches;
                    }
                    if (list.size() < 2) {
                        List<DossierRecentMatchDto> aggTranslit = fetchRecentMatchesFromAggregatorHistory(sport, cyrTranslit);
                        if (aggTranslit.size() > list.size()) {
                            list = aggTranslit;
                        }
                    }
                }
            }
        }

        // Priority 3.2: Resolve canonical teamId from mdm_team_translation (556k aliases)
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

        // Priority 3.5: Fallback to normalized, unaccented and transliterated exact matching (Strict Zero-Wildcard Policy)
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            String cleanTeam = stripAccents(teamName);
            if (!cleanTeam.equalsIgnoreCase(teamName) && cleanTeam.length() >= 3) {
                List<DossierRecentMatchDto> fallback = fetchRecentMatchesInternal(sport, cleanTeam);
                if (fallback.size() > list.size()) {
                    list = fallback;
                }
            }
            if (list.size() < 2) {
                String transliterated = SlugUtils.transliterate(teamName).trim();
                if (!transliterated.equalsIgnoreCase(teamName) && transliterated.length() >= 3) {
                    List<DossierRecentMatchDto> transMatches = fetchRecentMatchesInternal(sport, transliterated);
                    if (transMatches.size() > list.size()) {
                        list = transMatches;
                    }
                }
            }
        }

        // Priority 4: For solo/racket sports (table-tennis, tennis), match by player surname prefix
        if (list.size() < 2 && teamName != null && !teamName.isBlank() && sport != null && !sport.isTeamSport()) {
            List<DossierRecentMatchDto> surnameMatches = fetchRecentMatchesBySurname(sport, teamName);
            if (surnameMatches.size() > list.size()) {
                list = surnameMatches;
            }
        }

        // Priority 5: If fewer than 2 matches, query ClickHouse DDS by resolved SofaScore ID
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
        if (teamId == null || teamId <= 0) return null;
        return sofaIdCache.computeIfAbsent(teamId, id -> {
            try {
                // 1. Direct indexed lookup on mdm_team
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT sofascore_id, canonical_name, normalized_signature FROM mdm_team WHERE id = ?", id);
                if (rows.isEmpty()) return Optional.empty();
                Map<String, Object> r = rows.get(0);
                Number sofaNum = (Number) r.get("sofascore_id");
                if (sofaNum != null && sofaNum.longValue() > 0) {
                    return Optional.of(sofaNum.longValue());
                }

                // 2. Transliteration & inverted token matching for Cyrillic / bookmaker name order [Фамилия Имя] vs [First Last]
                String cName = (String) r.get("canonical_name");
                String queryName = (cName != null && !cName.isBlank()) ? cName : teamName;
                if (queryName != null && !queryName.isBlank()) {
                    String clean = queryName.replaceAll("\\s*\\([^)]*\\)", "").trim();
                    String latin = SimilarityUtils.transliterateCyrillic(clean).trim();
                    String[] tokens = latin.split("\\s+");

                    // Check individual surname / name tokens (length >= 4 to avoid noise)
                    for (String tok : tokens) {
                        String tClean = tok.toLowerCase().replaceAll("[^a-z]", "");
                        if (tClean.length() < 4) continue;
                        String tAlt = tClean.replace("yrou", "jirou").replace("yro", "jiro");

                        List<Long> matched = jdbcTemplate.query(
                                "SELECT sofascore_id FROM mdm_team WHERE sofascore_id IS NOT NULL AND " +
                                "(canonical_name ILIKE ? OR canonical_name ILIKE ? OR normalized_signature ILIKE ? OR normalized_signature ILIKE ?) " +
                                "ORDER BY id ASC LIMIT 1",
                                (rs, rowNum) -> rs.getLong("sofascore_id"),
                                "%" + tClean + "%", "%" + tAlt + "%", "%" + tClean + "%", "%" + tAlt + "%");

                        if (!matched.isEmpty() && matched.get(0) != null && matched.get(0) > 0) {
                            log.info("Resolved entity #{} ('{}', token '{}'/'{}') to canonical SofaScore ID #{}",
                                    id, queryName, tClean, tAlt, matched.get(0));
                            return Optional.of(matched.get(0));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to resolve sofascore_id for team {}: {}", id, e.getMessage());
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
            String p1Col = sport.isTeamSport() ? "home_team_id" : "player1_id";
            String p2Col = sport.isTeamSport() ? "away_team_id" : "player2_id";
            String s1Col = sport.isTeamSport() ? "home_score" : "score1";
            String s2Col = sport.isTeamSport() ? "away_score" : "score2";
            String tCol = "match_time";

            String sql = "SELECT " +
                    "if(" + p1Col + " = " + sofaId + ", " + s1Col + ", " + s2Col + ") as team_score, " +
                    "if(" + p1Col + " = " + sofaId + ", " + s2Col + ", " + s1Col + ") as opponent_score, " +
                    "if(if(" + p1Col + " = " + sofaId + ", " + s1Col + ", " + s2Col + ") > if(" + p1Col + " = " + sofaId + ", " + s2Col + ", " + s1Col + "), 'WIN', 'LOSS') as result, " +
                    "toUnixTimestamp(" + tCol + ") * 1000 as match_date " +
                    "FROM " + table + " " +
                    "WHERE " + p1Col + " = " + sofaId + " OR " + p2Col + " = " + sofaId + " " +
                    "ORDER BY " + tCol + " DESC LIMIT 10";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("recent_ch:" + table + ":" + sofaId, sql);
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

    public List<DossierRecentMatchDto> fetchRecentMatchesBySurname(Sport sport, String rawName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            // Strip any parenthesized annotations: (Чех), (Рос), (Cze), (W), (D), etc.
            String clean = rawName.replaceAll("\\s*\\([^)]*\\)", "").trim();
            if (SimilarityUtils.hasCyrillic(clean)) {
                clean = SimilarityUtils.transliterateCyrillic(clean).trim();
            }
            String[] parts = clean.split("\\s+");
            if (parts.length == 0) return list;

            // Collect candidate surname words: both parts[0] (e.g. Nokun) and parts[last] (e.g. Balcar)
            Set<String> candidates = new LinkedHashSet<>();
            if (parts[0].length() >= 3) candidates.add(parts[0]);
            if (parts[parts.length - 1].length() >= 3) candidates.add(parts[parts.length - 1]);

            // Also add phonetic variants for Polish / Czech transliterations:
            // 'k' <-> 'c' (Nokun <-> Nocun), 'w' <-> 'v' (Nowak <-> Novak)
            Set<String> allVariants = new LinkedHashSet<>(candidates);
            for (String cand : candidates) {
                if (cand.toLowerCase().contains("k")) {
                    allVariants.add(cand.replaceAll("(?i)k", "c"));
                }
                if (cand.toLowerCase().contains("c")) {
                    allVariants.add(cand.replaceAll("(?i)c", "k"));
                }
                if (cand.toLowerCase().contains("w")) {
                    allVariants.add(cand.replaceAll("(?i)w", "v"));
                }
                if (cand.toLowerCase().contains("v")) {
                    allVariants.add(cand.replaceAll("(?i)v", "w"));
                }
            }

            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (home_team ILIKE ? OR away_team ILIKE ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";

            List<Map<String, Object>> rows = Collections.emptyList();
            String matchedVariant = null;

            for (String variant : allVariants) {
                if (variant.length() < 3) continue;
                // Pattern 1: "Surname %" (e.g. "Nocun Z.", "Heuman I.")
                rows = jdbcTemplate.queryForList(sql, sport.getSlug(), variant + " %", variant + " %");
                if (!rows.isEmpty()) {
                    matchedVariant = variant;
                    break;
                }
                // Pattern 2: "% Surname" (e.g. "Zbigniew Nocun", "Tomas Balcar")
                rows = jdbcTemplate.queryForList(sql, sport.getSlug(), "% " + variant, "% " + variant);
                if (!rows.isEmpty()) {
                    matchedVariant = variant;
                    break;
                }
                // Pattern 3: substring if length >= 4
                if (variant.length() >= 4) {
                    rows = jdbcTemplate.queryForList(sql, sport.getSlug(), "%" + variant + "%", "%" + variant + "%");
                    if (!rows.isEmpty()) {
                        matchedVariant = variant;
                        break;
                    }
                }
            }

            if (matchedVariant == null || rows.isEmpty()) {
                return list;
            }

            String targetLower = matchedVariant.toLowerCase();
            for (Map<String, Object> r : rows) {
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                String ht = (String) r.get("home_team");
                boolean isHome = ht != null && ht.toLowerCase().contains(targetLower);
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
            log.debug("Surname match fallback failed for {}: {}", rawName, e.getMessage());
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
            String sql = "SELECT id, start_time, score1, score2, home_team_id, away_team_id " +
                         "FROM mdm_match " +
                         "WHERE (home_team_id = ? OR away_team_id = ?) " +
                         "  AND status = 'FINISHED' AND score1 IS NOT NULL AND score2 IS NOT NULL " +
                         "ORDER BY start_time DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, teamId, teamId);
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

    public List<DossierRecentMatchDto> fetchRecentMatchesFromAggregatorHistory(Sport sport, String teamName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        if (teamName == null || teamName.isBlank()) return list;
        try {
            String sportCode = sport != null ? sport.getCode() : "FOOTBALL";
            if ("ICE_HOCKEY".equals(sportCode)) sportCode = "HOCKEY";

            String latin = SimilarityUtils.hasCyrillic(teamName) ? SimilarityUtils.transliterateCyrillic(teamName).trim() : teamName;
            String clean = teamName.replaceAll("\\s*\\([^)]*\\)", "").trim();

            String sql = "SELECT opponent_name, team_score, opponent_score, match_result, match_date, clean_sheet, failed_to_score " +
                         "FROM team_match_history " +
                         "WHERE (sport = ? OR sport = ?) AND (LOWER(team_name) = LOWER(?) OR LOWER(team_name) = LOWER(?) OR LOWER(team_name) = LOWER(?)) " +
                         "ORDER BY match_date DESC LIMIT 10";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportCode, sport != null ? sport.getSlug().toUpperCase() : sportCode, teamName, latin, clean);
            for (Map<String, Object> r : rows) {
                int ts = toIntSafe(r.get("team_score"), 0);
                int os = toIntSafe(r.get("opponent_score"), 0);
                String res = (String) r.get("match_result");
                if (res == null) res = ts > os ? "WIN" : (ts < os ? "LOSS" : "DRAW");

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(ts)
                        .opponentScore(os)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(Boolean.TRUE.equals(r.get("clean_sheet")) || os == 0)
                        .failedToScore(Boolean.TRUE.equals(r.get("failed_to_score")) || ts == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed team_match_history for {}: {}", teamName, e.getMessage());
        }
        return list;
    }

    public Long resolveTeamIdFromTranslation(String teamName) {
        if (teamName == null || teamName.isBlank()) return null;
        try {
            List<Long> ids = jdbcTemplate.query(
                    "SELECT team_id FROM mdm_team_translation WHERE LOWER(name) = LOWER(?) OR LOWER(short_name) = LOWER(?) ORDER BY id ASC LIMIT 1",
                    (rs, rowNum) -> rs.getLong("team_id"),
                    teamName, teamName);
            return !ids.isEmpty() ? ids.get(0) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<DossierRecentMatchDto> fetchRecentMatchesInternal(Sport sport, String teamName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (LOWER(home_team) = LOWER(?) OR LOWER(away_team) = LOWER(?)) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "  AND home_team NOT ILIKE '%Reserve%' AND away_team NOT ILIKE '%Reserve%' " +
                         "  AND home_team NOT ILIKE '% U1%' AND away_team NOT ILIKE '% U1%' " +
                         "  AND home_team NOT ILIKE '% U2%' AND away_team NOT ILIKE '% U2%' " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sport.getSlug(), teamName, teamName);
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
            log.debug("Failed to fetch recent matches for team {}: {}", teamName, e.getMessage());
        }
        return list;
    }

    public String stripAccents(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("(?i)^(FC|CF|AC|AS|FK|SC|Club|Deportivo|Real|KK|BC|BK|CSKA|SK|HC|HK|EHC|EC)\\s+", "")
                .replaceAll("(?i)\\s+(FC|CF|FK|SC|BC|KK)$", "")
                .trim();
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
