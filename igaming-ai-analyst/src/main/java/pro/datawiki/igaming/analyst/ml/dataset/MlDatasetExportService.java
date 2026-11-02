package pro.datawiki.igaming.analyst.ml.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.analyst.ml.vector.TacticalTensorBuilder;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Institutional ML Dataset Exporter in Pure Java.
 * Completely eliminates Python SQL/feature extraction crutches.
 * Assembles unified 291-dimensional multi-task tactical tensors and ground truth targets,
 * writing high-throughput binary .npy files directly for GPU training.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MlDatasetExportService {

    private final JdbcTemplate jdbcTemplate;
    private final TacticalTensorBuilder tacticalTensorBuilder;
    private final MlSportTargetsEvaluator targetsEvaluator;
    private final ClickHouseDdsClient clickHouse;
    private final ObjectMapper objectMapper;

    @Value("${analyst.dataset.export-dir:/data/cold-storage/sofascore/cache}")
    private String defaultExportDir;

    @Getter
    @Builder
    public static class DatasetExportResult {
        private final String sport;
        private final int totalQueried;
        private final int validSamples;
        private final int featureDimension;
        private final int targetDimension;
        private final String featuresFilePath;
        private final String targetsFilePath;
        private final String metaFilePath;
        private final long durationMs;
        private final String status;
        private final String error;
    }

    private record HistoryMatch(long timestamp, int myScore, int oppScore, String result) {
        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("match_date", timestamp);
            m.put("my_score", myScore);
            m.put("opp_score", oppScore);
            m.put("team_score", myScore);
            m.put("opponent_score", oppScore);
            m.put("teamScore", myScore);
            m.put("opponentScore", oppScore);
            m.put("result", result);
            return m;
        }
    }

    /**
     * Exports full multi-task dataset matrices (.npy) for the specified sport.
     */
    public DatasetExportResult exportDataset(String rawSport, int limit, String customOutputDir) {
        long t0 = System.currentTimeMillis();
        String sport = normalizeSport(rawSport);
        String outDirStr = (customOutputDir != null && !customOutputDir.isBlank()) ? customOutputDir : defaultExportDir;
        File outDir = new File(outDirStr);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        log.info("[MlDatasetExportService] Starting Java dataset export for [{}] -> directory: {}", sport, outDir.getAbsolutePath());

        String sql = "SELECT event_id, mdm_match_id, match_date, home_team, away_team, home_score, away_score, " +
                     "event_json, lineups_json, statistics_json, mdm_home_team_id, mdm_away_team_id, tournament, season " +
                     "FROM sofascore_raw_events " +
                     "WHERE sport = ? AND status = 'finished' AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                     "ORDER BY match_date ASC" + (limit > 0 ? (" LIMIT " + limit) : "");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sport);
        int totalQueried = rows.size();
        log.info("[MlDatasetExportService] Loaded {} raw finished events for {}. Simulating temporal feature tensors...", totalQueried, sport);

        Map<String, Deque<HistoryMatch>> teamHistories = new HashMap<>();
        Map<String, Long> teamLastTs = new HashMap<>();

        List<float[]> xList = new ArrayList<>(Math.min(totalQueried, 10000));
        List<float[]> yList = new ArrayList<>(Math.min(totalQueried, 10000));

        int targetDim = 0;
        int lineupsMatched = 0;

        for (Map<String, Object> row : rows) {
            Long eventId = ((Number) row.get("event_id")).longValue();
            Long mdmMatchId = row.get("mdm_match_id") instanceof Number n ? n.longValue() : null;
            Long matchTs = row.get("match_date") instanceof Number n ? n.longValue() : null;
            String homeTeam = (String) row.get("home_team");
            String awayTeam = (String) row.get("away_team");
            Integer hs = ((Number) row.get("home_score")).intValue();
            Integer as = ((Number) row.get("away_score")).intValue();
            String eventJson = (String) row.get("event_json");
            String lineupsJson = (String) row.get("lineups_json");
            String statisticsJson = (String) row.get("statistics_json");
            Long mdmHomeId = row.get("mdm_home_team_id") instanceof Number n ? n.longValue() : null;
            Long mdmAwayId = row.get("mdm_away_team_id") instanceof Number n ? n.longValue() : null;
            String tournament = (String) row.get("tournament");
            String season = (String) row.get("season");

            if (matchTs == null || homeTeam == null || awayTeam == null) continue;

            String homeKey = mdmHomeId != null && mdmHomeId > 0 ? ("mdm:" + mdmHomeId) : ("str:" + homeTeam.trim().toLowerCase());
            String awayKey = mdmAwayId != null && mdmAwayId > 0 ? ("mdm:" + mdmAwayId) : ("str:" + awayTeam.trim().toLowerCase());

            Deque<HistoryMatch> hHist = teamHistories.get(homeKey);
            Deque<HistoryMatch> aHist = teamHistories.get(awayKey);

            // Require minimum 2 prior matches for both teams to compute momentum & Fourier form vectors
            if (hHist != null && hHist.size() >= 2 && aHist != null && aHist.size() >= 2) {
                // 1. Unpack event_json: nationality/country, canonical tournament, stage, team sofascore IDs, referee
                JsonNode eventRoot = null;
                if (eventJson != null && !eventJson.isBlank()) {
                    try {
                        JsonNode rNode = objectMapper.readTree(eventJson);
                        eventRoot = rNode.has("event") ? rNode.get("event") : rNode;
                    } catch (Exception ignored) {}
                }

                String countryName = null;
                String tournName = tournament;
                String stage = null;
                Long t1SofaId = null;
                Long t2SofaId = null;
                Double refAvgYc = null;

                if (eventRoot != null) {
                    countryName = eventRoot.path("tournament").path("category").path("country").path("name").asText(null);
                    if (countryName == null || countryName.isBlank()) {
                        countryName = eventRoot.path("tournament").path("category").path("name").asText(null);
                    }

                    String uName = eventRoot.path("tournament").path("uniqueTournament").path("name").asText(null);
                    if (uName != null && !uName.isBlank()) {
                        tournName = uName;
                    }

                    stage = eventRoot.path("roundInfo").path("name").asText(null);
                    if (stage == null || stage.isBlank()) {
                        stage = eventRoot.path("roundInfo").path("slug").asText(null);
                    }

                    long hId = eventRoot.path("homeTeam").path("id").asLong(0L);
                    if (hId > 0) t1SofaId = hId;
                    long aId = eventRoot.path("awayTeam").path("id").asLong(0L);
                    if (aId > 0) t2SofaId = aId;

                    if (eventRoot.has("referee")) {
                        JsonNode refNode = eventRoot.get("referee");
                        double yc = refNode.path("yellowCards").asDouble(0.0);
                        int games = refNode.path("games").asInt(0);
                        if (games > 0 && yc > 0) {
                            refAvgYc = yc / games;
                        } else if (yc > 0) {
                            refAvgYc = yc;
                        }
                    }
                }

                // 2. Dynamic Tournament & Seasonal Baseline from ClickHouse DDS
                Map<String, Object> chBaseline = clickHouse.getTournamentScoringBaseline(sport, null, tournName != null ? tournName : tournament);
                Double expTotal = chBaseline.get("regulation_median_total") instanceof Number n ? n.doubleValue() : null;
                Integer regMinutes = chBaseline.get("regulation_minutes") instanceof Number n ? n.intValue() : null;
                Double paceMult = chBaseline.get("pace_multiplier") instanceof Number n ? n.doubleValue() : null;

                Sport sObj = Sports.get(sport);
                if (expTotal == null || expTotal <= 0.0) {
                    if (sObj != null) expTotal = sObj.getDefaultTotalBaseline();
                }
                if (regMinutes == null || regMinutes <= 0) {
                    if (sObj != null) regMinutes = sObj.getDefaultRegulationMinutes();
                    if ("basketball".equals(sport) && tournName != null && tournName.toUpperCase().contains("NBA")) {
                        regMinutes = 48;
                        paceMult = 1.2;
                    }
                }

                // 3. Evaluate Ground Truth Targets
                MlSportTargetsEvaluator.TargetEvaluationResult targetRes = targetsEvaluator.evaluateTargets(
                        sport, eventJson, hs, as, expTotal);

                if (targetRes != null) {
                    targetDim = targetRes.numTargets();

                    // 4. Assemble Rich Match Dossier Map
                    Map<String, Object> dossierMeta = new LinkedHashMap<>();
                    dossierMeta.put("sport_name", sport.toUpperCase());
                    dossierMeta.put("match_id", mdmMatchId != null ? mdmMatchId : eventId);
                    dossierMeta.put("team1_id", mdmHomeId);
                    dossierMeta.put("team2_id", mdmAwayId);
                    dossierMeta.put("team1_sofascore_id", t1SofaId);
                    dossierMeta.put("team2_sofascore_id", t2SofaId);
                    dossierMeta.put("team1_name", homeTeam);
                    dossierMeta.put("team2_name", awayTeam);
                    dossierMeta.put("start_time", matchTs);
                    dossierMeta.put("tournament_name", tournName != null ? tournName : tournament);
                    dossierMeta.put("country_name", countryName != null ? countryName : "");
                    dossierMeta.put("season", season != null ? season : "");
                    dossierMeta.put("stage", stage != null ? stage : "");
                    dossierMeta.put("expected_total_baseline", expTotal);
                    dossierMeta.put("regulation_minutes", regMinutes);
                    dossierMeta.put("pace_multiplier", paceMult);
                    if (refAvgYc != null) {
                        dossierMeta.put("referee_avg_yellow_cards", refAvgYc);
                    }

                    // Formatted recent matches
                    List<Map<String, Object>> t1Recent = new ArrayList<>();
                    for (HistoryMatch m : hHist) t1Recent.add(m.toMap());
                    List<Map<String, Object>> t2Recent = new ArrayList<>();
                    for (HistoryMatch m : aHist) t2Recent.add(m.toMap());
                    dossierMeta.put("team1_recent_matches", t1Recent);
                    dossierMeta.put("team2_recent_matches", t2Recent);

                    // Lineups if available
                    if (lineupsJson != null && !lineupsJson.isBlank()) {
                        parseAndInjectLineups(lineupsJson, dossierMeta);
                        if (dossierMeta.containsKey("team1_starters")) {
                            lineupsMatched++;
                        }
                    }

                    // 5. Construct 291-dim Neural Tactical Tensor
                    TacticalTensorBuilder.TensorResult tensor = tacticalTensorBuilder.buildTensor(
                            mdmMatchId != null ? mdmMatchId : eventId, dossierMeta);

                    xList.add(tensor.getFeatures());
                    yList.add(targetRes.targets());

                    if (xList.size() % 2000 == 0) {
                        log.info("[MlDatasetExportService] [{}] Progress: {} valid samples generated (lineups matched: {})...",
                                sport, xList.size(), lineupsMatched);
                    }
                }
            }

            // 5. Update Rolling History
            String homeRes = hs > as ? "WIN" : (hs < as ? "LOSS" : "DRAW");
            String awayRes = as > hs ? "WIN" : (as < hs ? "LOSS" : "DRAW");

            teamHistories.computeIfAbsent(homeKey, k -> new ArrayDeque<>(10));
            teamHistories.computeIfAbsent(awayKey, k -> new ArrayDeque<>(10));

            Deque<HistoryMatch> hQueue = teamHistories.get(homeKey);
            if (hQueue.size() >= 10) hQueue.pollFirst();
            hQueue.addLast(new HistoryMatch(matchTs, hs, as, homeRes));

            Deque<HistoryMatch> aQueue = teamHistories.get(awayKey);
            if (aQueue.size() >= 10) aQueue.pollFirst();
            aQueue.addLast(new HistoryMatch(matchTs, as, hs, awayRes));

            teamLastTs.put(homeKey, matchTs);
            teamLastTs.put(awayKey, matchTs);
        }

        if (xList.isEmpty()) {
            return DatasetExportResult.builder()
                    .sport(sport)
                    .totalQueried(totalQueried)
                    .validSamples(0)
                    .durationMs(System.currentTimeMillis() - t0)
                    .status("FAILED_EMPTY_DATASET")
                    .error("No valid match samples met the minimum history threshold (>=2 matches)")
                    .build();
        }

        // 6. Write Binary .npy Files
        File xFile = new File(outDir, sport + "_multitask_features_X.npy");
        File yFile = new File(outDir, sport + "_multitask_targets_Y.npy");
        File metaFile = new File(outDir, sport + "_targets_meta.json");

        try {
            NpyWriter.writeFloatMatrix(xFile, xList);
            NpyWriter.writeFloatMatrix(yFile, yList);
            writeTargetsMetaJson(metaFile, sport);
        } catch (IOException e) {
            log.error("[MlDatasetExportService] Failed to write .npy files: {}", e.getMessage(), e);
            return DatasetExportResult.builder()
                    .sport(sport)
                    .totalQueried(totalQueried)
                    .validSamples(xList.size())
                    .durationMs(System.currentTimeMillis() - t0)
                    .status("IO_ERROR")
                    .error(e.getMessage())
                    .build();
        }

        long duration = System.currentTimeMillis() - t0;
        log.info("[MlDatasetExportService] Successfully exported {} dataset: {} samples (291x{}) in {} ms (Lineups matched: {})",
                sport, xList.size(), targetDim, duration, lineupsMatched);

        return DatasetExportResult.builder()
                .sport(sport)
                .totalQueried(totalQueried)
                .validSamples(xList.size())
                .featureDimension(291)
                .targetDimension(targetDim)
                .featuresFilePath(xFile.getAbsolutePath())
                .targetsFilePath(yFile.getAbsolutePath())
                .metaFilePath(metaFile.getAbsolutePath())
                .durationMs(duration)
                .status("SUCCESS")
                .build();
    }

    private void parseAndInjectLineups(String lineupsJson, Map<String, Object> dossierMeta) {
        try {
            JsonNode root = objectMapper.readTree(lineupsJson);
            JsonNode confirmed = root.get("confirmed");
            if (confirmed != null && confirmed.asBoolean()) {
                dossierMeta.put("is_lineup_confirmed", true);
            }
            if (root.has("home") && root.get("home").has("players")) {
                List<Map<String, Object>> starters = new ArrayList<>();
                List<Map<String, Object>> bench = new ArrayList<>();
                for (JsonNode p : root.get("home").get("players")) {
                    Map<String, Object> pl = new HashMap<>();
                    JsonNode playerNode = p.has("player") ? p.get("player") : p;
                    pl.put("name", playerNode.path("name").asText("Player"));
                    pl.put("rating", p.path("rating").asDouble(6.8));
                    pl.put("position", p.path("position").asText("M"));
                    long pid = playerNode.path("id").asLong(0L);
                    if (pid > 0) {
                        pl.put("id", pid);
                        pl.put("sofa_id", pid);
                        pl.put("sofascore_id", pid);
                    }
                    if (p.path("substitute").asBoolean(false)) {
                        bench.add(pl);
                    } else {
                        starters.add(pl);
                    }
                }
                if (!starters.isEmpty()) dossierMeta.put("team1_starters", starters);
                if (!bench.isEmpty()) dossierMeta.put("team1_bench", bench);
            }
            if (root.has("away") && root.get("away").has("players")) {
                List<Map<String, Object>> starters = new ArrayList<>();
                List<Map<String, Object>> bench = new ArrayList<>();
                for (JsonNode p : root.get("away").get("players")) {
                    Map<String, Object> pl = new HashMap<>();
                    JsonNode playerNode = p.has("player") ? p.get("player") : p;
                    pl.put("name", playerNode.path("name").asText("Player"));
                    pl.put("rating", p.path("rating").asDouble(6.8));
                    pl.put("position", p.path("position").asText("M"));
                    long pid = playerNode.path("id").asLong(0L);
                    if (pid > 0) {
                        pl.put("id", pid);
                        pl.put("sofa_id", pid);
                        pl.put("sofascore_id", pid);
                    }
                    if (p.path("substitute").asBoolean(false)) {
                        bench.add(pl);
                    } else {
                        starters.add(pl);
                    }
                }
                if (!starters.isEmpty()) dossierMeta.put("team2_starters", starters);
                if (!bench.isEmpty()) dossierMeta.put("team2_bench", bench);
            }
        } catch (Exception ignored) {}
    }

    private void writeTargetsMetaJson(File targetFile, String sport) throws IOException {
        List<Map<String, Object>> meta = getTargetsMeta(sport);
        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, meta);
        }
    }

    public List<Map<String, Object>> getTargetsMeta(String sport) {
        String s = normalizeSport(sport);
        return switch (s) {
            case "basketball" -> List.of(
                    target(0, "winner_p1", "Moneyline Winner Home (with OT)"),
                    target(1, "spread_minus_5_5", "Home Cover -5.5 Points"),
                    target(2, "spread_plus_5_5", "Away Cover +5.5 Points"),
                    target(3, "spread_minus_9_5", "Home Cover -9.5 Points (Strong Favorite)"),
                    target(4, "total_points_over_baseline", "Total Match Points Over Dynamic Baseline"),
                    target(5, "total_points_cushion_low", "Cushion Points Over Baseline - 10"),
                    target(6, "total_points_cushion_high", "Cushion Points Under Baseline + 10"),
                    target(7, "first_half_winner_p1", "1st Half Winner Home"),
                    target(8, "first_half_over", "1st Half Total Points Over Half Baseline"),
                    target(9, "first_quarter_winner_p1", "1st Quarter Winner Home"),
                    target(10, "first_quarter_over", "1st Quarter Points Over Qtr Baseline"),
                    target(11, "clutch_margin_under_6", "Clutch Game (Final Margin <= 5)"),
                    target(12, "blowout_margin_over_15", "Blowout Game (Final Margin >= 15)"),
                    target(13, "overtime_flag", "Game Went to Overtime (Draw in 4Q)")
            );
            case "ice-hockey" -> List.of(
                    target(0, "winner_reg_p1", "Regulation Time Win Home (P1)"),
                    target(1, "winner_reg_px", "Regulation Draw (PX -> Overtime)"),
                    target(2, "winner_reg_p2", "Regulation Time Win Away (P2)"),
                    target(3, "moneyline_ot_p1", "Moneyline Winner Home (incl. OT/SO)"),
                    target(4, "puckline_minus_1_5", "Puckline Home -1.5 (2+ Goal Win)"),
                    target(5, "puckline_plus_1_5", "Puckline Away +1.5"),
                    target(6, "total_goals_over_5_5", "Total Goals Over Dynamic Baseline (5.5)"),
                    target(7, "total_goals_over_4_5", "Cushion Goals Over Baseline - 1.0"),
                    target(8, "total_goals_under_6_5", "Cushion Goals Under Baseline + 1.0"),
                    target(9, "first_period_p1", "1st Period Winner Home"),
                    target(10, "first_period_over_1_5", "1st Period Goals Over 1.5"),
                    target(11, "both_teams_score_2plus", "Both Teams Score >= 2 Goals"),
                    target(12, "shutout_flag", "Shutout (Zero goals by either team)"),
                    target(13, "high_scoring_over_7_5", "High Scoring Festival Over 7.5")
            );
            case "football" -> List.of(
                    target(0, "winner_1x2_p1", "1X2 Win Home (P1)"),
                    target(1, "winner_1x2_px", "1X2 Draw (PX)"),
                    target(2, "winner_1x2_p2", "1X2 Win Away (P2)"),
                    target(3, "double_chance_1x", "Double Chance 1X"),
                    target(4, "double_chance_x2", "Double Chance X2"),
                    target(5, "handicap_minus_1", "Handicap Home -1.0 (2+ Goals)"),
                    target(6, "handicap_plus_1", "Handicap Away +1.0"),
                    target(7, "total_goals_over_2_5", "Total Goals Over Dynamic Baseline (2.5)"),
                    target(8, "total_goals_over_1_5", "Cushion Goals Over 1.5"),
                    target(9, "total_goals_under_3_5", "Cushion Goals Under 3.5"),
                    target(10, "btts", "Both Teams to Score (BTTS Yes)"),
                    target(11, "btts_and_over_2_5", "Combo: BTTS Yes + Over 2.5"),
                    target(12, "first_half_p1", "1st Half Winner Home"),
                    target(13, "first_half_over_0_5", "1st Half Goals Over 0.5"),
                    target(14, "first_half_over_1_5", "1st Half Goals Over 1.5"),
                    target(15, "clean_sheet_home", "Home Clean Sheet (0 goals conceded)")
            );
            case "tennis" -> List.of(
                    target(0, "winner_p1", "Match Winner Home (P1)"),
                    target(1, "set_handicap_minus_1_5", "Home 2:0 in Sets (Clean Win)"),
                    target(2, "set_handicap_plus_1_5", "Away Wins >= 1 Set (+1.5)"),
                    target(3, "total_sets_over_2_5", "Total Sets Over 2.5 (3-Set Match)"),
                    target(4, "total_games_over_21_5", "Total Games Over Dynamic Baseline (21.5)"),
                    target(5, "total_games_over_19_5", "Cushion Games Over 19.5"),
                    target(6, "total_games_under_23_5", "Cushion Games Under 23.5"),
                    target(7, "first_set_winner_p1", "1st Set Winner Home"),
                    target(8, "first_set_over_9_5", "1st Set Games Over 9.5"),
                    target(9, "first_set_tiebreak", "1st Set Tiebreak (7:6)"),
                    target(10, "both_win_set", "Both Win >= 1 Set"),
                    target(11, "clean_sweep_2_0", "Clean Sweep 2:0"),
                    target(12, "games_margin_over_5_5", "Game Spread >= 6 Games (Blowout)"),
                    target(13, "deciding_set_clutch", "High Volatility Clutch Match")
            );
            case "table-tennis" -> List.of(
                    target(0, "winner_p1", "Match Winner Home (P1)"),
                    target(1, "set_handicap_minus_1_5", "Home -1.5 Sets (3:0 or 3:1)"),
                    target(2, "set_handicap_plus_1_5", "Away +1.5 Sets"),
                    target(3, "total_sets_over_3_5", "Total Sets Over 3.5"),
                    target(4, "total_sets_over_4_5", "Deciding 5th Set (3:2 match)"),
                    target(5, "total_points_over_74_5", "Total Match Points Over Dynamic Baseline (74.5)"),
                    target(6, "total_points_over_68_5", "Cushion Total Points Over 68.5"),
                    target(7, "total_points_under_80_5", "Cushion Total Points Under 80.5"),
                    target(8, "first_set_winner_p1", "1st Set Winner Home (P1)"),
                    target(9, "first_set_total_over_18_5", "1st Set Points Over 18.5"),
                    target(10, "both_win_set", "Both Players Win >= 1 Set"),
                    target(11, "clean_sweep_3_0", "Clean Sweep 3:0"),
                    target(12, "points_margin_over_10", "Total Points Margin >= 10 (Blowout)"),
                    target(13, "high_entropy_clutch", "Clutch Game (Points diff <= 4)")
            );
            case "cs2" -> List.of(
                    target(0, "winner_p1", "Match Winner Home (P1)"),
                    target(1, "map_handicap_minus_1_5", "Home -1.5 Maps (Clean Sweep)"),
                    target(2, "map_handicap_plus_1_5", "Away Wins >= 1 Map (+1.5)"),
                    target(3, "total_maps_over_2_5", "Total Maps Over 2.5 (3-Map Decider)"),
                    target(4, "map1_winner_p1", "Map 1 Winner Home"),
                    target(5, "map1_rounds_over_21_5", "Map 1 Total Rounds Over 21.5"),
                    target(6, "map1_rounds_over_19_5", "Cushion Map 1 Rounds Over 19.5"),
                    target(7, "map1_rounds_under_23_5", "Cushion Map 1 Rounds Under 23.5"),
                    target(8, "map1_overtime_flag", "Map 1 Went to Overtime (12-12)"),
                    target(9, "both_win_map", "Both Teams Win >= 1 Map"),
                    target(10, "map1_handicap_minus_3_5", "Map 1 Rounds P1 -3.5"),
                    target(11, "clean_sweep_2_0", "Clean Sweep 2:0"),
                    target(12, "decider_map_played", "Decider Map 3 Played"),
                    target(13, "high_entropy_clutch", "Clutch Map 1 (Rounds Diff <= 3)")
            );
            case "dota2" -> List.of(
                    target(0, "winner_p1", "Match Winner Home (P1)"),
                    target(1, "map_handicap_minus_1_5", "Home -1.5 Maps (Clean Sweep)"),
                    target(2, "map_handicap_plus_1_5", "Away Wins >= 1 Map (+1.5)"),
                    target(3, "total_maps_over_2_5", "Total Maps Over 2.5 (3-Map Decider)"),
                    target(4, "map1_winner_p1", "Map 1 Winner Home"),
                    target(5, "both_win_map", "Both Teams Win >= 1 Map"),
                    target(6, "clean_sweep_2_0", "Clean Sweep 2:0"),
                    target(7, "decider_map_played", "Decider Map 3 Played"),
                    target(8, "home_wins_at_least_1_map", "Home Takes >= 1 Map"),
                    target(9, "away_wins_at_least_1_map", "Away Takes >= 1 Map"),
                    target(10, "clutch_2_1_home_win", "2:1 Series Win Home"),
                    target(11, "clutch_1_2_away_win", "1:2 Series Win Away"),
                    target(12, "blowout_2_0_home", "2:0 Blowout Home"),
                    target(13, "blowout_0_2_away", "0:2 Blowout Away")
            );
            default -> Collections.emptyList();
        };
    }

    private Map<String, Object> target(int id, String name, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("type", "binary");
        m.put("desc", desc);
        return m;
    }

    private String normalizeSport(String sport) {
        if (sport == null) return "football";
        String s = sport.trim().toLowerCase().replace('_', '-');
        if ("soccer".equals(s)) return "football";
        if ("hockey".equals(s)) return "ice-hockey";
        return s;
    }
}
