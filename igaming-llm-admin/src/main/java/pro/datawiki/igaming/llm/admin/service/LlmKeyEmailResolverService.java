package pro.datawiki.igaming.llm.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;
import pro.datawiki.igaming.llm.admin.repository.LlmProviderKeyRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmKeyEmailResolverService {

    private final LlmProviderKeyRepository keyRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    /**
     * Resolves emails for all OAuth keys on application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🚀 Application ready. Triggering initial Google Account email resolution for OAuth keys...");
        resolveAllKeys();
    }

    /**
     * Periodically resolves/verifies emails for active OAuth keys every 4 hours.
     */
    @Scheduled(fixedDelay = 4 * 3600 * 1000, initialDelay = 4 * 3600 * 1000)
    public void scheduledResolution() {
        log.info("🔄 Running scheduled Google Account email resolution...");
        resolveAllKeys();
    }

    @Transactional
    public void resolveAllKeys() {
        try {
            List<LlmProviderKey> keys = keyRepository.findAll();
            for (LlmProviderKey key : keys) {
                if (key.getApiKey() != null && key.getApiKey().trim().startsWith("{")) {
                    // This is an OAuth credentials JSON string
                    try {
                        String resolvedEmail = resolveEmail(key.getApiKey());
                        if (resolvedEmail != null && !resolvedEmail.isBlank()) {
                            // If current label does not already match the email, update it
                            if (!resolvedEmail.equalsIgnoreCase(key.getLabel())) {
                                String oldLabel = key.getLabel();
                                key.setLabel(resolvedEmail);
                                keyRepository.saveAndFlush(key);
                                log.info("🔑 Resolved key ID {} email: {} (was: '{}')", key.getId(), resolvedEmail, oldLabel);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to resolve email for key ID {} (label: '{}'): {}", key.getId(), key.getLabel(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to resolve OAuth keys: {}", e.getMessage());
        }
    }

    private String resolveEmail(String apiKeyJson) throws Exception {
        JsonNode root = objectMapper.readTree(apiKeyJson);

        // 1. Try resolving locally from id_token (JWT) if present
        if (root.has("id_token")) {
            String idToken = root.get("id_token").asText();
            String email = extractEmailFromJwt(idToken);
            if (email != null && !email.isBlank()) {
                log.debug("Found email in id_token locally");
                return email;
            }
        }

        // 2. If id_token not present or didn't contain email, fetch via Google OAuth + UserInfo API
        if (root.has("refresh_token") && root.has("client_id") && root.has("client_secret")) {
            String refreshToken = root.get("refresh_token").asText();
            String clientId = root.get("client_id").asText();
            String clientSecret = root.get("client_secret").asText();

            log.debug("Exchanging refresh_token for access_token with Google API...");
            String accessToken = fetchAccessToken(clientId, clientSecret, refreshToken);
            if (accessToken != null) {
                return fetchEmailFromUserInfo(accessToken);
            }
        }

        return null;
    }

    private String extractEmailFromJwt(String jwtToken) {
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length >= 2) {
                String payloadBase64 = parts[1];
                byte[] decodedBytes = Base64.getUrlDecoder().decode(payloadBase64);
                String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
                JsonNode payloadNode = objectMapper.readTree(payloadJson);
                if (payloadNode.has("email")) {
                    return payloadNode.get("email").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse local id_token: {}", e.getMessage());
        }
        return null;
    }

    private String fetchAccessToken(String clientId, String clientSecret, String refreshToken) throws Exception {
        String requestBody = "client_id=" + java.net.URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&client_secret=" + java.net.URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                "&refresh_token=" + java.net.URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            if (responseJson.has("access_token")) {
                return responseJson.get("access_token").asText();
            }
        } else {
            log.warn("Google token endpoint returned status {}: {}", response.statusCode(), response.body());
        }
        return null;
    }

    private String fetchEmailFromUserInfo(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            if (responseJson.has("email")) {
                return responseJson.get("email").asText();
            }
        } else {
            log.warn("Google userinfo endpoint returned status {}: {}", response.statusCode(), response.body());
        }
        return null;
    }
}
