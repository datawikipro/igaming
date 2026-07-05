package pro.datawiki.accounts.llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class GoogleOAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public String idToken;
        public Integer expiresIn;
    }

    /**
     * Exchanges auth code for access/refresh tokens.
     */
    public TokenResponse exchangeCode(String code, String redirectUri) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                TokenResponse res = new TokenResponse();
                res.accessToken = root.path("access_token").asText();
                res.refreshToken = root.has("refresh_token") ? root.path("refresh_token").asText() : null;
                res.idToken = root.path("id_token").asText();
                res.expiresIn = root.path("expires_in").asInt();
                return res;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage(), e);
        }
        throw new RuntimeException("Failed to exchange code: response was not successful");
    }

    /**
     * Refreshes the access token using the refresh token.
     */
    public TokenResponse refreshToken(String refreshToken) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 1. Try official client ID first to preserve the "aicode" scope for native-based tokens
        String officialClientId = "REDACTED";
        String officialClientSecret = "REDACTED";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", officialClientId);
        map.add("client_secret", officialClientSecret);
        map.add("refresh_token", refreshToken);
        map.add("grant_type", "refresh_token");

        try {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                TokenResponse res = new TokenResponse();
                res.accessToken = root.path("access_token").asText();
                res.expiresIn = root.path("expires_in").asInt();
                res.refreshToken = root.has("refresh_token") ? root.path("refresh_token").asText() : null;
                res.idToken = root.has("id_token") ? root.path("id_token").asText() : null;
                log.info("[OAuth] Successfully refreshed token using official client ID.");
                return res;
            }
        } catch (Exception e) {
            log.debug("[OAuth] Refresh with official client ID failed (expected if token was generated via UI): {}", e.getMessage());
        }

        // 2. Fallback to configured client ID
        map.set("client_id", clientId);
        map.set("client_secret", clientSecret);

        try {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                TokenResponse res = new TokenResponse();
                res.accessToken = root.path("access_token").asText();
                res.expiresIn = root.path("expires_in").asInt();
                res.refreshToken = root.has("refresh_token") ? root.path("refresh_token").asText() : null;
                res.idToken = root.has("id_token") ? root.path("id_token").asText() : null;
                log.info("[OAuth] Successfully refreshed token using configured client ID.");
                return res;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
        throw new RuntimeException("Failed to refresh token: response was not successful");
    }

    /**
     * Decodes the user's email from the Google ID token.
     */
    public String extractEmailFromIdToken(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            return null;
        }
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payloadB64 = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
            String jsonPayload = new String(decoded, StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(jsonPayload);
            return root.path("email").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse email from ID token: " + e.getMessage(), e);
        }
    }
}
