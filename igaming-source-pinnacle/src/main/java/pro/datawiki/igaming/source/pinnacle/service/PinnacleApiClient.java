package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.pinnacle.config.PinnacleConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinnacleApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PinnacleConfig pinnacleConfig;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Origin", "https://www.pinnacle.com");
        headers.set("Referer", "https://www.pinnacle.com/");

        String username = pinnacleConfig.getApi().getUsername();
        String password = pinnacleConfig.getApi().getPassword();
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            headers.setBasicAuth(username, password);
        }
        return headers;
    }

    public JsonNode getFixtures(int sportId) {
        String url = UriComponentsBuilder.fromHttpUrl(pinnacleConfig.getApi().getBaseUrl() + "/fixtures")
                .queryParam("sportId", sportId)
                .toUriString();

        try {
            log.debug("Fetching Pinnacle fixtures for sportId: {} from {}", sportId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching Pinnacle fixtures for sportId: {}: {}", sportId, e.getMessage());
            return null;
        }
    }

    public JsonNode getOdds(int sportId) {
        String url = UriComponentsBuilder.fromHttpUrl(pinnacleConfig.getApi().getBaseUrl() + "/odds")
                .queryParam("sportId", sportId)
                .toUriString();

        try {
            log.debug("Fetching Pinnacle odds for sportId: {} from {}", sportId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching Pinnacle odds for sportId: {}: {}", sportId, e.getMessage());
            return null;
        }
    }
}
