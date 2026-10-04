package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.pinnacle.config.PinnacleConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for Pinnacle Guest API (guest.api.arcadia.pinnacle.com).
 * Routes through PureVPN DE (purevpn-de.proxy.svc.cluster.local:3128)
 * to bypass Cloudflare geo-restrictions on Russian IPs.
 */
@Service
@Slf4j
public class PinnacleApiClient {

    private static final String DEFAULT_API_BASE = "https://guest.api.arcadia.pinnacle.com/0.1";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final PinnacleConfig pinnacleConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.proxy.host:purevpn-de.proxy.svc.cluster.local}")
    private String proxyHost;

    @Value("${app.proxy.port:3128}")
    private int proxyPort;

    @Value("${app.vpn.force:false}")
    private boolean vpnForce;

    public PinnacleApiClient(PinnacleConfig pinnacleConfig) {
        this.pinnacleConfig = pinnacleConfig;
    }

    /**
     * Builds a RestTemplate that routes through the configured HTTP proxy.
     * When proxyHost is set and vpnForce=true, forces all requests through the proxy.
     */
    private RestTemplate buildProxiedRestTemplate() {
        if (proxyHost != null && !proxyHost.isBlank() && vpnForce) {
            try {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                factory.setProxy(proxy);
                factory.setConnectTimeout(10_000);
                factory.setReadTimeout(30_000);
                log.debug("PinnacleApiClient using explicit proxy: {}:{}", proxyHost, proxyPort);
                return new RestTemplate(factory);
            } catch (Exception e) {
                log.warn("Failed to configure proxy {}:{}, falling back to system proxy: {}",
                        proxyHost, proxyPort, e.getMessage());
            }
        }
        // Fall back to system proxy properties (-Dhttps.proxyHost etc.)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return new RestTemplate(factory);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Origin", "https://www.pinnacle.com");
        headers.set("Referer", "https://www.pinnacle.com/en/soccer/matchups/");
        headers.set("sec-ch-ua", "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not-A.Brand\";v=\"99\"");
        headers.set("sec-ch-ua-mobile", "?0");
        headers.set("sec-ch-ua-platform", "\"Windows\"");
        headers.set("sec-fetch-dest", "empty");
        headers.set("sec-fetch-mode", "cors");
        headers.set("sec-fetch-site", "same-site");

        String username = pinnacleConfig.getApi().getUsername();
        String password = pinnacleConfig.getApi().getPassword();
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            headers.setBasicAuth(username, password);
        }
        return headers;
    }

    public JsonNode getMatchups(int sportId) {
        String url = resolveBaseUrl() + "/sports/" + sportId + "/matchups";
        return fetchJsonWithFallback(url, "matchups", sportId);
    }

    public JsonNode getStraightMarkets(int sportId) {
        String url = resolveBaseUrl() + "/sports/" + sportId + "/markets/straight";
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
            return DEFAULT_API_BASE;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private JsonNode fetchJsonWithFallback(String url, String entityName, int sportId) {
        // Primary: RestTemplate with explicit proxy
        try {
            log.debug("Fetching Pinnacle {} for sportId: {} via proxy {}:{}",
                    entityName, sportId, proxyHost, proxyPort);
            RestTemplate rt = buildProxiedRestTemplate();
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<JsonNode> response = rt.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode body = response.getBody();
                // Detect Cloudflare HTML block (403 body contains "DOCTYPE html")
                if (body.isTextual() && body.asText().contains("DOCTYPE html")) {
                    log.warn("Cloudflare block detected in RestTemplate response for sportId {}. Attempting curl fallback...", sportId);
                } else {
                    return body;
                }
            }
        } catch (Exception e) {
            log.warn("RestTemplate failed fetching Pinnacle {} for sportId {}: {}. Attempting curl fallback...",
                    entityName, sportId, e.getMessage());
        }

        // Fallback: curl with explicit proxy
        try {
            return fetchWithCurl(url);
        } catch (Exception ex) {
            log.error("Curl fallback failed for Pinnacle {} sportId {}: {}", entityName, sportId, ex.getMessage());
            return null;
        }
    }

    private JsonNode fetchWithCurl(String url) throws Exception {
        List<String> command = new ArrayList<>(List.of(
                "curl", "-s", "--max-time", "30",
                "-H", "User-Agent: " + USER_AGENT,
                "-H", "Accept: application/json",
                "-H", "Accept-Language: en-US,en;q=0.9",
                "-H", "Origin: https://www.pinnacle.com",
                "-H", "Referer: https://www.pinnacle.com/en/soccer/matchups/",
                "-H", "sec-fetch-dest: empty",
                "-H", "sec-fetch-mode: cors",
                "-H", "sec-fetch-site: same-site"
        ));

        // Add explicit proxy to curl if configured
        if (proxyHost != null && !proxyHost.isBlank() && vpnForce) {
            command.add("--proxy");
            command.add("http://" + proxyHost + ":" + proxyPort);
            log.debug("curl fallback using proxy: {}:{}", proxyHost, proxyPort);
        }

        command.add(url);

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            JsonNode root = objectMapper.readTree(reader);
            int exitCode = process.waitFor();
            if (exitCode == 0 && root != null && !root.isNull()) {
                // Detect Cloudflare HTML block
                if (root.isTextual() && root.asText().contains("DOCTYPE html")) {
                    log.warn("Cloudflare block in curl fallback response for {}", url);
                    return null;
                }
                return root;
            }
        }
        return null;
    }
}
