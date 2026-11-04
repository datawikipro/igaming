package pro.datawiki.igaming.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MdmClient {

    private final ObjectMapper objectMapper;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public Optional<CanonicalMatchHeaderDto> fetchMatch(Long id) {
        if (id == null) return Optional.empty();
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/" + id;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(4))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                CanonicalMatchHeaderDto header = objectMapper.readValue(resp.body(), CanonicalMatchHeaderDto.class);
                return Optional.ofNullable(header);
            }
            log.warn("MDM returned status {} for match #{}", resp.statusCode(), id);
        } catch (Exception e) {
            log.error("Failed to fetch match #{} from MDM: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    public boolean cancelMatch(Long id, String reason) {
        if (id == null) return false;
        try {
            String encodedReason = URLEncoder.encode(reason != null ? reason : "ANOMALY_RESOLVER", StandardCharsets.UTF_8);
            String url = mdmUrl + "/api/v1/mdm/matches/" + id + "/cancel?reason=" + encodedReason;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(4))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("🎯 [MDM Client] Successfully cancelled match #{} in MDM (reason: {})", id, reason);
                return true;
            }
            log.warn("MDM returned status {} when cancelling match #{}", resp.statusCode(), id);
        } catch (Exception e) {
            log.error("Failed to cancel match #{} in MDM: {}", id, e.getMessage());
        }
        return false;
    }

    public boolean promoteGroundTruth(Long id, Long sofascoreId, String sourceOrigin, String reason) {
        if (id == null) return false;
        try {
            StringBuilder sb = new StringBuilder(mdmUrl).append("/api/v1/mdm/matches/").append(id).append("/promote-ground-truth?");
            if (sofascoreId != null) {
                sb.append("sofascoreId=").append(sofascoreId).append("&");
            }
            if (sourceOrigin != null) {
                sb.append("sourceOrigin=").append(sourceOrigin).append("&");
            }
            String encodedReason = URLEncoder.encode(reason != null ? reason : "ANOMALY_RESOLVER", StandardCharsets.UTF_8);
            sb.append("reason=").append(encodedReason);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(sb.toString()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(4))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("🎯 [MDM Client] Successfully promoted match #{} to ground truth in MDM (sofascoreId={})", id, sofascoreId);
                return true;
            }
            log.warn("MDM returned status {} when promoting match #{}", resp.statusCode(), id);
        } catch (Exception e) {
            log.error("Failed to promote match #{} in MDM: {}", id, e.getMessage());
        }
        return false;
    }

    public boolean mergeTeams(Long survivingTeamId, Long duplicateTeamId) {
        if (survivingTeamId == null || duplicateTeamId == null || survivingTeamId.equals(duplicateTeamId)) {
            return false;
        }
        try {
            String url = mdmUrl + "/api/v1/mdm/teams/merge";
            String body = objectMapper.writeValueAsString(java.util.Map.of(
                    "survivingTeamId", survivingTeamId,
                    "duplicateTeamId", duplicateTeamId
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(6))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("🎯 [MDM Client] Successfully merged duplicate team #{} into canonical #{}", duplicateTeamId, survivingTeamId);
                return true;
            }
            log.warn("MDM returned status {} when merging team #{} into #{}", resp.statusCode(), duplicateTeamId, survivingTeamId);
        } catch (Exception e) {
            log.error("Failed to merge teams in MDM (surviving={}, duplicate={}): {}", survivingTeamId, duplicateTeamId, e.getMessage());
        }
        return false;
    }
}
