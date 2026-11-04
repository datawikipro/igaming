package pro.datawiki.igaming.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.agent.config.PlaneAiProperties;
import pro.datawiki.igaming.agent.dto.WorkerGenerateRequest;
import pro.datawiki.igaming.agent.dto.WorkerGenerateResponse;
import pro.datawiki.igaming.agent.dto.WorkerStatusDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaneAiWorkerClient {

    private final PlaneAiProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public String buildWorkerUrl(int podIndex) {
        return String.format(properties.getBaseUrlPattern(), podIndex);
    }

    public Optional<WorkerStatusDto> queryStatus(int podIndex) {
        String url = buildWorkerUrl(podIndex) + "/status";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                WorkerStatusDto dto = objectMapper.readValue(response.body(), WorkerStatusDto.class);
                return Optional.ofNullable(dto);
            }
        } catch (Exception e) {
            log.debug("Worker #{} status probe failed ({}): {}", podIndex, url, e.getMessage());
        }
        return Optional.empty();
    }

    public void triggerRefreshQuota(int podIndex) {
        String url = buildWorkerUrl(podIndex) + "/refresh-quota";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            log.info("🔄 [Plane AI Client] Triggered async quota/auth refresh on worker #{}", podIndex);
        } catch (Exception e) {
            log.debug("Worker #{} triggerRefreshQuota failed: {}", podIndex, e.getMessage());
        }
    }

    public WorkerGenerateResponse executeSynchronously(int podIndex, WorkerGenerateRequest req) {
        String url = buildWorkerUrl(podIndex) + "/generate";
        int timeoutSec = (req.getTimeout() != null && req.getTimeout() > 0)
                ? req.getTimeout()
                : properties.getTimeoutSeconds();

        try {
            String jsonPayload = objectMapper.writeValueAsString(req);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSec + 10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                WorkerGenerateResponse genResp = objectMapper.readValue(response.body(), WorkerGenerateResponse.class);
                return genResp;
            } else if (response.statusCode() == 429) {
                log.info("Worker #{} is busy (HTTP 429), failing over immediately to next worker...", podIndex);
                return WorkerGenerateResponse.builder()
                        .status("busy")
                        .error("WORKER_BUSY: " + response.body())
                        .pod("plane-ai-worker-" + podIndex)
                        .build();
            } else {
                log.warn("Worker #{} returned HTTP {}: {}", podIndex, response.statusCode(), response.body());
                return WorkerGenerateResponse.builder()
                        .status("error")
                        .error("HTTP " + response.statusCode() + ": " + response.body())
                        .pod("plane-ai-worker-" + podIndex)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Worker #{} synchronous call failed: {}", podIndex, e.getMessage());
            return WorkerGenerateResponse.builder()
                    .status("error")
                    .error(e.getMessage())
                    .pod("plane-ai-worker-" + podIndex)
                    .build();
        }
    }
}
