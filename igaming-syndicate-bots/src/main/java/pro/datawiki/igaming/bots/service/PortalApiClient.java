package pro.datawiki.igaming.bots.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalApiClient {

    private final RestTemplate restTemplate;

    @Value("${portal.api.url:http://igaming-portal:80}")
    private String portalUrl;

    public void topUpWallet(Long userId, String bookmaker, BigDecimal amount, String currency) {
        try {
            String url = portalUrl + "/api/v1/wallet/topup";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bot-User-Id", String.valueOf(userId));

            Map<String, Object> body = new HashMap<>();
            body.put("bookmaker", bookmaker);
            body.put("amount", amount);
            body.put("currency", currency);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, entity, Object.class);
            log.info("Successfully topped up wallet for bot {}: {} {} in {}", userId, amount, currency, bookmaker);
        } catch (Exception e) {
            log.warn("Failed to top up wallet for bot {}: {}", userId, e.getMessage());
        }
    }

    public void placeBet(Long userId, Map<String, Object> betRequest) {
        try {
            String url = portalUrl + "/api/v1/wallet/bet";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bot-User-Id", String.valueOf(userId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(betRequest, headers);
            restTemplate.postForObject(url, entity, Object.class);
            log.info("Placed bet for bot {}: matchId={}", userId, betRequest.get("match_id"));
        } catch (Exception e) {
            log.warn("Failed to place bet for bot {}: {}", userId, e.getMessage());
        }
    }

    public Long createSocialPost(Long userId, Map<String, Object> postRequest) {
        try {
            String url = portalUrl + "/api/v1/social/posts";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bot-User-Id", String.valueOf(userId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(postRequest, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);
            if (resp != null && resp.get("id") != null) {
                return ((Number) resp.get("id")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to create social post for bot {}: {}", userId, e.getMessage());
        }
        return null;
    }

    public void addComment(Long userId, Long postId, Map<String, Object> commentRequest) {
        try {
            String url = portalUrl + "/api/v1/social/posts/" + postId + "/comments";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bot-User-Id", String.valueOf(userId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(commentRequest, headers);
            restTemplate.postForObject(url, entity, Object.class);
            log.info("Added comment to post {} from bot {}", postId, userId);
        } catch (Exception e) {
            log.warn("Failed to add comment to post {} for bot {}: {}", postId, userId, e.getMessage());
        }
    }
}
