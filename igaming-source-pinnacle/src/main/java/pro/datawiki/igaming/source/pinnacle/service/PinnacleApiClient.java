package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinnacleApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
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

    public JsonNode getMatchups(int sportId) {
        String baseUrl = resolveBaseUrl();
        String url = baseUrl + "/sports/" + sportId + "/matchups";
        return fetchJsonWithFallback(url, "matchups", sportId);
    }

    public JsonNode getStraightMarkets(int sportId) {
        String baseUrl = resolveBaseUrl();
        String url = baseUrl + "/sports/" + sportId + "/markets/straight";
        return fetchJsonWithFallback(url, "straight markets", sportId);
    }

    public JsonNode getFixtures(int sportId) {
        return getMatchups(sportId);
    }

    public JsonNode getOdds(int sportId) {
        return getStraightMarkets(sportId);
    }

    private String resolveBaseUrl() {
        String url = pinnacleConfig.getApi().getBaseUrl();
        if (url == null || url.isBlank() || url.contains("guest.api.pinnacle.com/v1")) {
            return "https://guest.api.arcadia.pinnacle.com/0.1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private JsonNode fetchJsonWithFallback(String url, String entityName, int sportId) {
        try {
            log.debug("Fetching Pinnacle {} for sportId: {} from {}", entityName, sportId, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("RestTemplate failed fetching Pinnacle {} for sportId {}: {}. Attempting curl fallback...",
                    entityName, sportId, e.getMessage());
        }

        try {
            return fetchWithCurl(url);
        } catch (Exception ex) {
            log.error("Curl fallback failed for Pinnacle {} sportId {}: {}", entityName, sportId, ex.getMessage());
            return null;
        }
    }

    private JsonNode fetchWithCurl(String url) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "curl", "-s", "--max-time", "30",
                "-H", "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "-H", "Accept: application/json",
                "-H", "Origin: https://www.pinnacle.com",
                "-H", "Referer: https://www.pinnacle.com/",
                url
        );
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            JsonNode root = objectMapper.readTree(reader);
            int exitCode = process.waitFor();
            if (exitCode == 0 && root != null) {
                return root;
            }
        }
        return null;
    }
}
