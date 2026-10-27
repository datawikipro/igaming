package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;

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
            Map<String, Object> payload = new HashMap<>();
            payload.put("match_id", matchId);
            payload.put("sport_name", dossierMeta.get("sport_name"));
            payload.put("team1_id", dossierMeta.get("team1_id"));
            payload.put("team2_id", dossierMeta.get("team2_id"));
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
        try {
            String encodedTeam = java.net.URLEncoder.encode(teamName != null ? teamName : "", java.nio.charset.StandardCharsets.UTF_8);
            String encodedSport = java.net.URLEncoder.encode(sport != null ? sport : "football", java.nio.charset.StandardCharsets.UTF_8);
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
