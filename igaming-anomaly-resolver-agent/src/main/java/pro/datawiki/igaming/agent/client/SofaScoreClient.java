package pro.datawiki.igaming.agent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SofaScoreClient {

    private final ObjectMapper objectMapper;

    @Value("${app.sofascore.ondemand-url:http://igaming-capture-sofascore-on-demand.igaming-enrichment.svc.cluster.local:3041/api/v1/sofascore/on-demand}")
    private String sofaOnDemandUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public Optional<JsonNode> fetchByEventId(Long eventId) {
        if (eventId == null) return Optional.empty();
        try {
            String url = sofaOnDemandUrl + "/match/" + eventId;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                JsonNode node = objectMapper.readTree(resp.body());
                return Optional.ofNullable(node);
            }
        } catch (Exception e) {
            log.warn("SofaScore on-demand lookup failed for event #{}: {}", eventId, e.getMessage());
        }
        return Optional.empty();
    }
}
