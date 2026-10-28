package pro.datawiki.igaming.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalystClient {

    private final ObjectMapper objectMapper;

    @Value("${app.analyst.url:http://igaming-ai-analyst.igaming-analytics.svc.cluster.local:8080}")
    private String analystUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public boolean resolveConflict(Long matchId, String action, String reason, Long winnerMatchId) {
        if (matchId == null) return false;
        try {
            String encodedReason = URLEncoder.encode(reason != null ? reason : "ROBOT_RESOLVER", StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder(analystUrl)
                    .append("/api/v1/ai-analyst/recommendations/")
                    .append(matchId)
                    .append("/resolve-conflict?action=")
                    .append(action != null ? action : "PROMOTE")
                    .append("&reason=")
                    .append(encodedReason);
            if (winnerMatchId != null) {
                sb.append("&winnerMatchId=").append(winnerMatchId);
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(sb.toString()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("🎯 [Analyst Client] Successfully resolved conflict for match #{} (action={}, reason={})", matchId, action, reason);
                return true;
            }
            log.warn("Analyst returned status {} when resolving conflict for match #{}: {}", resp.statusCode(), matchId, resp.body());
        } catch (Exception e) {
            log.error("Failed to resolve conflict for match #{} in AI Analyst: {}", matchId, e.getMessage());
        }
        return false;
    }

    public boolean triggerEvaluation(Long matchId) {
        if (matchId == null) return false;
        try {
            String url = analystUrl + "/api/v1/ai-analyst/evaluate/" + matchId;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("🎯 [Analyst Client] Triggered AI evaluation for match #{}", matchId);
                return true;
            }
            log.warn("Analyst returned status {} when triggering evaluation for match #{}: {}", resp.statusCode(), matchId, resp.body());
        } catch (Exception e) {
            log.error("Failed to trigger evaluation for match #{} in AI Analyst: {}", matchId, e.getMessage());
        }
        return false;
    }
}
