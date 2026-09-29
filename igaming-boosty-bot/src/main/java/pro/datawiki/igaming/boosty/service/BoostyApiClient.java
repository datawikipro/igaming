package pro.datawiki.igaming.boosty.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.boosty.boosty.BoostyComment;
import pro.datawiki.igaming.boosty.boosty.BoostyPost;
import pro.datawiki.igaming.boosty.boosty.BoostySubscriber;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HTTP client for the unofficial Boosty API (api.boosty.to).
 *
 * <p><b>Authentication:</b> Boosty uses a Bearer access token extracted from
 * browser cookies / localStorage. There is no official OAuth for third-party apps.
 * The token is configured via environment variable {@code BOOSTY_ACCESS_TOKEN}.
 *
 * <p><b>Endpoints used:</b>
 * <ul>
 *   <li>GET /v1/blog/{blog}/subscribers — list of paying subscribers</li>
 *   <li>GET /v1/blog/{blog}/post — list of posts (paginated)</li>
 *   <li>GET /v1/blog/{blog}/post/{postId}/comment — comments on a post</li>
 * </ul>
 *
 * <p><b>Rate limiting:</b> Requests are spaced by the polling interval configured
 * via {@code BOOSTY_POLLING_INTERVAL_MS}. The client includes a 10-second timeout
 * per request to prevent hanging.
 *
 * <p>Reference: community libraries akovardin/boosty (Go), PyBoostyApi (Python).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoostyApiClient {

    private static final String API_BASE = "https://api.boosty.to";
    private static final int REQUEST_TIMEOUT_SEC = 10;
    private static final int MAX_SUBSCRIBERS_PER_PAGE = 200;
    private static final int MAX_POSTS_PER_PAGE = 50;

    @Value("${boosty.account.blog-name}")
    private String blogName;

    @Value("${boosty.account.access-token}")
    private String accessToken;

    @Value("${boosty.account.device-id:}")
    private String deviceId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // ─────────────────────────────────────────────────────────
    // Public API methods
    // ─────────────────────────────────────────────────────────

    /**
     * Fetch all active paying subscribers for the blog.
     *
     * <p>Handles pagination by issuing multiple requests if needed.
     * Returns an empty list on auth/network errors (does not throw).
     *
     * @return list of active subscribers (may be empty)
     */
    public List<BoostySubscriber> fetchSubscribers() {
        List<BoostySubscriber> result = new ArrayList<>();
        int offset = 0;
        int total = Integer.MAX_VALUE;

        while (offset < total) {
            String url = String.format(
                    "%s/v1/blog/%s/subscribers?limit=%d&offset=%d&sort_by=on_time&order=gt",
                    API_BASE, blogName, MAX_SUBSCRIBERS_PER_PAGE, offset);
            try {
                String body = get(url);
                JsonNode root = objectMapper.readTree(body);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<BoostySubscriber> page = objectMapper.convertValue(
                            data, new TypeReference<>() {});
                    result.addAll(page);
                    if (page.isEmpty()) break;
                }
                JsonNode extra = root.path("extra");
                if (!extra.isMissingNode()) {
                    total = extra.path("total").asInt(0);
                }
                offset += MAX_SUBSCRIBERS_PER_PAGE;
            } catch (Exception e) {
                log.warn("BoostyApiClient: error fetching subscribers at offset={}: {}", offset, e.getMessage());
                break;
            }
        }

        log.debug("BoostyApiClient: fetched {} subscribers", result.size());
        return result;
    }

    /**
     * Fetch recent posts for the blog (paginated, newest first).
     *
     * @param maxPosts maximum number of posts to return
     * @return list of posts (may be empty)
     */
    public List<BoostyPost> fetchRecentPosts(int maxPosts) {
        List<BoostyPost> result = new ArrayList<>();
        int offset = 0;

        while (result.size() < maxPosts) {
            int limit = Math.min(MAX_POSTS_PER_PAGE, maxPosts - result.size());
            String url = String.format(
                    "%s/v1/blog/%s/post?limit=%d&offset=%d&sort_by=publish_time&order=gt",
                    API_BASE, blogName, limit, offset);
            try {
                String body = get(url);
                JsonNode root = objectMapper.readTree(body);
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<BoostyPost> page = objectMapper.convertValue(
                            data, new TypeReference<>() {});
                    result.addAll(page);
                    if (page.isEmpty()) break;
                } else {
                    break;
                }
                offset += limit;
            } catch (Exception e) {
                log.warn("BoostyApiClient: error fetching posts at offset={}: {}", offset, e.getMessage());
                break;
            }
        }

        log.debug("BoostyApiClient: fetched {} posts", result.size());
        return result;
    }

    /**
     * Fetch comments for a specific post.
     *
     * @param postId Boosty post ID (UUID string)
     * @return list of comments (may be empty)
     */
    public List<BoostyComment> fetchCommentsForPost(String postId) {
        String url = String.format(
                "%s/v1/blog/%s/post/%s/comment?limit=100&offset=0",
                API_BASE, blogName, postId);
        try {
            String body = get(url);
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");
            if (data.isArray()) {
                return objectMapper.convertValue(data, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("BoostyApiClient: error fetching comments for postId={}: {}", postId, e.getMessage());
        }
        return Collections.emptyList();
    }

    // ─────────────────────────────────────────────────────────
    // Internal HTTP helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Perform a GET request to the Boosty API with authentication headers.
     *
     * @param url full URL to request
     * @return response body as string
     * @throws IOException          on network errors
     * @throws InterruptedException if the request is interrupted
     */
    private String get(String url) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "SmartBet-BoostyBot/1.0");

        if (deviceId != null && !deviceId.isBlank()) {
            requestBuilder.header("X-Device-Id", deviceId);
        }

        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            log.error("BoostyApiClient: 401 Unauthorized — BOOSTY_ACCESS_TOKEN is expired or invalid!");
            throw new IOException("Boosty API: 401 Unauthorized");
        }
        if (response.statusCode() != 200) {
            log.warn("BoostyApiClient: HTTP {} for URL: {}", response.statusCode(), url);
            throw new IOException("Boosty API: HTTP " + response.statusCode());
        }

        return response.body();
    }
}
