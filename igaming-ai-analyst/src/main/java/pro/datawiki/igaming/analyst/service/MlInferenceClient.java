package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.ml.vector.TacticalTensorBuilder;
import pro.datawiki.igaming.dto.ml.LiveInPlayPredictionDto;
import pro.datawiki.igaming.dto.ml.LiveMatchSnapshotDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * High-speed HTTP client communicating with the Institutional Tactical ML Inference Engine
 * (igaming-ml-inference on port 8085).
 * Evaluates match tactical probabilities across 5 sports in < 5ms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MlInferenceClient {

    private final ObjectMapper objectMapper;
    private final TacticalTensorBuilder tensorBuilder;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${analyst.ml.url:http://igaming-ml-inference.igaming-analytics.svc.cluster.local:8085}")
    private String mlServiceUrl;

    public MlPredictionDto predict(Long matchId, Map<String, Object> dossierMeta) {
        if (dossierMeta == null) {
            return MlPredictionDto.builder()
                    .hasMlEvaluation(false)
                    .reason("Dossier metadata is null")
                    .build();
        }

        try {
            // Build 100% Java Polymorphic Tactical Tensor (Zero Owls, Zero Sines)
            TacticalTensorBuilder.TensorResult tensorResult = tensorBuilder.buildTensor(matchId, dossierMeta);

            // Hard Contract Assertion: Feature length MUST strictly match expected model dimension
            int expectedDim = tensorResult.getSport().getExpectedModelDimension();
            if (tensorResult.getFeatures().length != expectedDim) {
                throw new pro.datawiki.igaming.dto.ml.tensor.TacticalContractViolationException(
                        tensorResult.getSport().getCode(), expectedDim, tensorResult.getFeatures().length,
                        "Tensor dimension does not match sport contract!");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("expected_dimension", expectedDim);
            payload.put("match_id", matchId);
            payload.put("sport_name", dossierMeta.get("sport_name"));
            payload.put("league_name", dossierMeta.get("league_name"));
            payload.put("tournament_name", dossierMeta.get("league_name"));
            payload.put("features", tensorResult.getFeatures());
            payload.put("team1_id", dossierMeta.get("team1_id"));
            payload.put("team2_id", dossierMeta.get("team2_id"));
            payload.put("team1_sofascore_id", dossierMeta.get("team1_sofascore_id"));
            payload.put("team2_sofascore_id", dossierMeta.get("team2_sofascore_id"));
            payload.put("team1_name", dossierMeta.get("team1_name"));
            payload.put("team2_name", dossierMeta.get("team2_name"));
            payload.put("start_time", dossierMeta.get("start_time"));
            payload.put("team1_recent_matches", dossierMeta.get("team1_recent_matches"));
            payload.put("team2_recent_matches", dossierMeta.get("team2_recent_matches"));
            payload.put("team1_bench", dossierMeta.get("team1_bench"));
            payload.put("team2_bench", dossierMeta.get("team2_bench"));
            payload.put("team1_starters", dossierMeta.get("team1_starters"));
            payload.put("team2_starters", dossierMeta.get("team2_starters"));
            payload.put("team1_dna_tags", dossierMeta.get("team1_dna_tags"));
            payload.put("team2_dna_tags", dossierMeta.get("team2_dna_tags"));
            payload.put("is_lineup_confirmed", dossierMeta.get("is_lineup_confirmed"));
            payload.put("expected_total_baseline", dossierMeta.get("expected_total_baseline"));
            payload.put("tournament_cushion_low", dossierMeta.get("tournament_cushion_low"));
            payload.put("tournament_cushion_high", dossierMeta.get("tournament_cushion_high"));
            payload.put("tournament_scoring_category", dossierMeta.get("tournament_scoring_category"));

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlServiceUrl + "/predict"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null) {
                MlPredictionDto dto = objectMapper.readValue(response.body(), MlPredictionDto.class);
                return dto;
            } else {
                log.warn("ML inference service returned HTTP {} for matchId={}: {}",
                        response.statusCode(), matchId, response.body());
                return MlPredictionDto.builder()
                        .hasMlEvaluation(false)
                        .reason("ML Service HTTP error: " + response.statusCode())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to call ML inference service for matchId={}: {}", matchId, e.getMessage());
            return MlPredictionDto.builder()
                    .hasMlEvaluation(false)
                    .reason("ML Service unavailable: " + e.getMessage())
                    .build();
        }
    }

    public LiveInPlayPredictionDto livePredict(
            LiveMatchSnapshotDto snapshot,
            String sportName, String team1Name, String team2Name
    ) {
        if (snapshot == null) return null;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("match_id", snapshot.getMatchId());
            payload.put("sport_name", sportName != null ? sportName : "football");
            payload.put("minute", snapshot.getMinute());
            payload.put("score_home", snapshot.getScoreHome());
            payload.put("score_away", snapshot.getScoreAway());
            payload.put("red_cards_home", snapshot.getRedCardsHome());
            payload.put("red_cards_away", snapshot.getRedCardsAway());
            payload.put("live_xg_home", snapshot.getLiveXgHome());
            payload.put("live_xg_away", snapshot.getLiveXgAway());
            payload.put("shots_on_target_home", snapshot.getShotsOnTargetHome());
            payload.put("shots_on_target_away", snapshot.getShotsOnTargetAway());
            payload.put("team1_name", team1Name != null ? team1Name : "Хозяева");
            payload.put("team2_name", team2Name != null ? team2Name : "Гости");

            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlServiceUrl + "/live-predict"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                return objectMapper.readValue(response.body(), LiveInPlayPredictionDto.class);
            } else {
                log.warn("ML /live-predict returned HTTP {}: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.warn("Failed to call ML /live-predict for matchId={}: {}", snapshot.getMatchId(), e.getMessage());
            return null;
        }
    }

    public Map<String, Object> simulateCustomLineup(Map<String, Object> payload) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlServiceUrl + "/simulate_custom_lineup"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                return objectMapper.readValue(response.body(), Map.class);
            } else {
                return Map.of("success", false, "error", "HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            log.warn("Failed to simulate custom lineup: {}", e.getMessage());
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> getTeamProfile(String teamName, String sport) {
        if (teamName == null || teamName.isBlank() || sport == null || sport.isBlank()) {
            return Map.of("found", false);
        }
        try {
            String encodedTeam = java.net.URLEncoder.encode(teamName, java.nio.charset.StandardCharsets.UTF_8);
            String encodedSport = java.net.URLEncoder.encode(sport, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlServiceUrl + "/team_lineup_profile?team=" + encodedTeam + "&sport=" + encodedSport))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                return objectMapper.readValue(response.body(), Map.class);
            }
            return Map.of("found", false, "team", teamName);
        } catch (Exception e) {
            log.warn("Failed to get team profile for {}: {}", teamName, e.getMessage());
            return Map.of("found", false, "team", teamName, "error", e.getMessage());
        }
    }
}
