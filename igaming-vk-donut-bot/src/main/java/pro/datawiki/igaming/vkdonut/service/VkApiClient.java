package pro.datawiki.igaming.vkdonut.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Client for the VK Community Messages API.
 *
 * Sends messages on behalf of the SmartBet.guru VK community using
 * the community access token. VK requires `messages.send` permission
 * and peer_id for direct messages to subscribers.
 *
 * Reference: https://dev.vk.com/ru/method/messages.send
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VkApiClient {

    private static final String VK_API_BASE = "https://vk.com/dev/messages.send";
    private static final String VK_API_URL   = "https://api.vk.com/method/";
    private static final String VK_API_VERSION = "5.199";

    private final RestTemplate restTemplate;

    @Value("${vk.community.token}")
    private String communityToken;

    @Value("${vk.community.id}")
    private Long communityId;

    public Long getCommunityId() {
        return communityId;
    }

    // -------------------------------------------------------
    // Public API Methods
    // -------------------------------------------------------

    /**
     * Publishes a wall post to the VK community, optionally restricted to VK Donut supporters.
     *
     * @param message            Message text (plain text)
     * @param donutPaidDuration  Duration in seconds for Donut exclusivity, or -1 for permanently exclusive to dons
     * @return Map containing post_id on success or error info
     */
    public Map<String, Object> postWall(String message, int donutPaidDuration) {
        long ownerId = communityId != null ? -Math.abs(communityId) : 0L;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(VK_API_URL + "wall.post")
                .queryParam("owner_id", ownerId)
                .queryParam("from_group", 1)
                .queryParam("message", message)
                .queryParam("access_token", communityToken)
                .queryParam("v", VK_API_VERSION);

        if (donutPaidDuration != 0) {
            builder.queryParam("donut_paid_duration", donutPaidDuration);
        }

        String url = builder.toUriString();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("VK API wall.post returned HTTP {}: {}", response.getStatusCode(), response.getBody());
                return Map.of("error", "HTTP " + response.getStatusCode());
            }
            Map<?, ?> body = response.getBody();
            if (body != null && body.containsKey("error")) {
                log.warn("VK API wall.post error: {}", body.get("error"));
                return Map.of("error", body.get("error"));
            }
            if (body != null && body.containsKey("response")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> respMap = (Map<String, Object>) body.get("response");
                log.info("VK wall post published successfully: post_id={}", respMap.get("post_id"));
                return respMap;
            }
            return Map.of("status", "ok");
        } catch (Exception e) {
            log.error("VK API wall.post failed: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Send a plain-text message from the community to a VK user.
     *
     * @param vkUserId  Target VK user ID (positive integer)
     * @param message   Message text (plain text, max 4096 chars)
     */
    public void sendMessage(Long vkUserId, String message) {
        String randomId = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()));
        String url = UriComponentsBuilder.fromHttpUrl(VK_API_URL + "messages.send")
                .queryParam("user_id", vkUserId)
                .queryParam("message", message)
                .queryParam("random_id", randomId)
                .queryParam("access_token", communityToken)
                .queryParam("v", VK_API_VERSION)
                .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("VK API messages.send to userId={} returned HTTP {}: {}",
                        vkUserId, response.getStatusCode(), response.getBody());
            } else {
                Map<?, ?> body = response.getBody();
                if (body != null && body.containsKey("error")) {
                    log.warn("VK API messages.send error for userId={}: {}", vkUserId, body.get("error"));
                } else {
                    log.debug("VK message sent to userId={}", vkUserId);
                }
            }
        } catch (Exception e) {
            log.error("VK API messages.send failed for userId={}: {}", vkUserId, e.getMessage());
        }
    }

    /**
     * Resolve VK user display name for logging/welcome messages.
     *
     * @param vkUserId VK user ID
     * @return "FirstName LastName" or "Донор" on failure
     */
    public String resolveUserName(Long vkUserId) {
        String url = UriComponentsBuilder.fromHttpUrl(VK_API_URL + "users.get")
                .queryParam("user_ids", vkUserId)
                .queryParam("access_token", communityToken)
                .queryParam("v", VK_API_VERSION)
                .toUriString();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var body = response.getBody();
                if (body.containsKey("response")) {
                    @SuppressWarnings("unchecked")
                    var list = (java.util.List<Map<String, Object>>) body.get("response");
                    if (!list.isEmpty()) {
                        Map<String, Object> user = list.get(0);
                        String fn = String.valueOf(user.getOrDefault("first_name", ""));
                        String ln = String.valueOf(user.getOrDefault("last_name", ""));
                        return (fn + " " + ln).trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("VK users.get failed for userId={}: {}", vkUserId, e.getMessage());
        }
        return "Донор";
    }
}
