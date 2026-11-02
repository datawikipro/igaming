package pro.datawiki.igaming.analyst.ml.dataset;

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
 * Strictly adheres to Rule 9 via polymorphic Sport domain model.
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
     * Exports full multi-task dataset matrices (.npy) for the specified sport by name/slug.
     */
    public DatasetExportResult exportDataset(String rawSport, int limit, String customOutputDir) {
        Sport sport = Sports.get(rawSport);
        return exportDataset(sport, limit, customOutputDir);
    }

    /**
     * Exports full multi-task dataset matrices (.npy) for the specified polymorphic Sport instance.
     */
    public DatasetExportResult exportDataset(Sport sportObj, int limit, String customOutputDir) {
        long t0 = System.currentTimeMillis();
        Sport sport = sportObj != null ? sportObj : Sports.SOCCER;
        String sportSlug = sport.getSlug();
        String outDirStr = (customOutputDir != null && !customOutputDir.isBlank()) ? customOutputDir : defaultExportDir;
        File outDir = new File(outDirStr);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        log.info("[MlDatasetExportService] Starting Java dataset export for [{}] -> directory: {}", sportSlug, outDir.getAbsolutePath());

        String sql = "SELECT event_id, mdm_match_id, match_date, home_team, away_team, home_score, away_score, " +
                     "event_json, lineups_json, statistics_json, mdm_home_team_id, mdm_away_team_id, tournament, season " +
                     "FROM sofascore_raw_events " +
                     "WHERE sport = ? AND status = 'finished' AND home_score IS NOT NULL AND away_score IS NOT NULL " +
                     "ORDER BY match_date ASC" + (limit > 0 ? (" LIMIT " + limit) : "");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sportSlug);
        int totalQueried = rows.size();
        log.info("[MlDatasetExportService] Loaded {} raw finished events for {}. Simulating temporal feature tensors...", totalQueried, sportSlug);

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

            String homeKey = mdmHomeId != null && mdmHomeId > 0 ? ("mdm:" + mdmHomeId) : ("str:" + homeTeam.trim().toLowerCase(Locale.ROOT));
            String awayKey = mdmAwayId != null && mdmAwayId > 0 ? ("mdm:" + mdmAwayId) : ("str:" + awayTeam.trim().toLowerCase(Locale.ROOT));

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

                String tournName = tournament;
                String stage = null;
                String country = null;
                Long t1SofaId = null;
                Long t2SofaId = null;
                String refName = null;
                Double refAvgYc = null;

                if (eventRoot != null) {
                    if (eventRoot.has("tournament") && eventRoot.get("tournament").has("name")) {
                        tournName = eventRoot.get("tournament").get("name").asText(tournName);
                    }
                    if (eventRoot.has("roundInfo") && eventRoot.get("roundInfo").has("round")) {
                        stage = "Round " + eventRoot.get("roundInfo").get("round").asText();
                    }
                    if (eventRoot.has("tournament") && eventRoot.get("tournament").has("category")
                            && eventRoot.get("tournament").get("category").has("name")) {
                        country = eventRoot.get("tournament").get("category").get("name").asText();
                    }
                    if (eventRoot.has("homeTeam") && eventRoot.get("homeTeam").has("id")) {
                        t1SofaId = eventRoot.get("homeTeam").get("id").asLong();
                    }
                    if (eventRoot.has("awayTeam") && eventRoot.get("awayTeam").has("id")) {
                        t2SofaId = eventRoot.get("awayTeam").get("id").asLong();
                    }
                    if (eventRoot.has("referee") && eventRoot.get("referee").has("name")) {
                        refName = eventRoot.get("referee").get("name").asText();
                        int games = eventRoot.get("referee").path("games").asInt(0);
                        int yc = eventRoot.get("referee").path("yellowCards").asInt(0);
                        if (games > 0 && yc > 0) {
                            refAvgYc = (double) yc / games;
                        } else if (yc > 0) {
                            refAvgYc = (double) yc;
                        }
                    }
                }

                // 2. Dynamic Tournament & Seasonal Baseline from ClickHouse DDS
                Map<String, Object> chBaseline = clickHouse.getTournamentScoringBaseline(sportSlug, null, tournName != null ? tournName : tournament);
                Double expTotal = chBaseline.get("regulation_median_total") instanceof Number n ? n.doubleValue() : null;
                Integer regMinutes = chBaseline.get("regulation_minutes") instanceof Number n ? n.intValue() : null;
                Double paceMult = chBaseline.get("pace_multiplier") instanceof Number n ? n.doubleValue() : null;

                if (expTotal == null || expTotal <= 0.0) {
                    expTotal = sport.getDefaultTotalBaseline();
                }
                if (regMinutes == null || regMinutes <= 0) {
                    regMinutes = sport.getDefaultRegulationMinutes();
                    if ("basketball".equals(sportSlug) && tournName != null && tournName.toUpperCase(Locale.ROOT).contains("NBA")) {
                        regMinutes = 48;
                        paceMult = 1.2;
                    }
                }

                // 3. Evaluate Ground Truth Targets via polymorphic evaluator
                MlSportTargetsEvaluator.TargetEvaluationResult targetRes = targetsEvaluator.evaluateTargets(
                        sport, eventJson, hs, as, expTotal);

                if (targetRes != null) {
                    targetDim = targetRes.numTargets();

                    // 4. Assemble Rich Match Dossier Map
                    Map<String, Object> dossierMeta = new LinkedHashMap<>();
                    dossierMeta.put("sport_name", sport.getCode());
                    dossierMeta.put("match_id", mdmMatchId != null ? mdmMatchId : eventId);
                    dossierMeta.put("team1_id", mdmHomeId);
                    dossierMeta.put("team2_id", mdmAwayId);
                    dossierMeta.put("team1_sofascore_id", t1SofaId);
                    dossierMeta.put("team2_sofascore_id", t2SofaId);
                    dossierMeta.put("team1_name", homeTeam);
                    dossierMeta.put("team2_name", awayTeam);
                    dossierMeta.put("tournament_name", tournName != null ? tournName : "Championship");
                    dossierMeta.put("country", country);
                    dossierMeta.put("season", season);
                    dossierMeta.put("stage", stage);
                    dossierMeta.put("kickoff_timestamp", matchTs);
                    dossierMeta.put("referee_name", refName);
                    dossierMeta.put("referee_avg_yellow_cards", refAvgYc);

                    // Dynamic Baselines
                    if (expTotal != null) dossierMeta.put("expected_total_baseline", expTotal);
                    if (regMinutes != null) dossierMeta.put("regulation_minutes", regMinutes);
                    if (paceMult != null) dossierMeta.put("pace_multiplier", paceMult);

                    // Rest days computation
                    Long hLast = teamLastTs.get(homeKey);
                    Long aLast = teamLastTs.get(awayKey);
                    double hRest = hLast != null ? Math.max(0.5, (matchTs - hLast) / 86400000.0) : 4.0;
                    double aRest = aLast != null ? Math.max(0.5, (matchTs - aLast) / 86400000.0) : 4.0;
                    dossierMeta.put("team1_rest_days", hRest);
                    dossierMeta.put("team2_rest_days", aRest);

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
                                sportSlug, xList.size(), lineupsMatched);
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

        int nSamples = xList.size();
        if (nSamples == 0) {
            log.warn("[MlDatasetExportService] No valid training samples found for [{}]", sportSlug);
            return DatasetExportResult.builder()
                    .sport(sportSlug)
                    .totalQueried(totalQueried)
                    .validSamples(0)
                    .status("EMPTY")
                    .error("Zero matches satisfied minimum 2 prior matches history and score validity.")
                    .build();
        }

        log.info("[MlDatasetExportService] Assembled {} valid samples for [{}]. Writing binary .npy files to disk...", nSamples, sportSlug);

        int featureDim = 291;
        File xFile = new File(outDir, String.format("%s_multitask_features_X.npy", sportSlug));
        File yFile = new File(outDir, String.format("%s_multitask_targets_Y.npy", sportSlug));
        File metaFile = new File(outDir, String.format("%s_targets_meta.json", sportSlug));

        try {
            NpyWriter.writeFloatMatrix(xFile, xList);
            NpyWriter.writeFloatMatrix(yFile, yList);
            writeTargetsMetaJson(metaFile, sport);
        } catch (IOException e) {
            log.error("[MlDatasetExportService] Failed to write dataset files: {}", e.getMessage(), e);
            return DatasetExportResult.builder()
                    .sport(sportSlug)
                    .totalQueried(totalQueried)
                    .validSamples(nSamples)
                    .status("ERROR")
                    .error(e.getMessage())
                    .build();
        }

        long duration = System.currentTimeMillis() - t0;
        log.info("[MlDatasetExportService] Successfully exported [{}] dataset: {} samples (featDim={}, targetDim={}) in {} ms. Files: X={}, Y={}",
                sportSlug, nSamples, featureDim, targetDim, duration, xFile.getName(), yFile.getName());

        return DatasetExportResult.builder()
                .sport(sportSlug)
                .totalQueried(totalQueried)
                .validSamples(nSamples)
                .featureDimension(featureDim)
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

    private void writeTargetsMetaJson(File targetFile, Sport sport) throws IOException {
        List<Map<String, Object>> meta = getTargetsMeta(sport);
        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, meta);
        }
    }

    public List<Map<String, Object>> getTargetsMeta(String sport) {
        return targetsEvaluator.getTargetSchemas(sport);
    }

    public List<Map<String, Object>> getTargetsMeta(Sport sport) {
        return targetsEvaluator.getTargetSchemas(sport);
    }
}
