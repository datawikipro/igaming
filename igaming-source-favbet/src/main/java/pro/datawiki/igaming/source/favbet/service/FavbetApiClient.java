package pro.datawiki.igaming.source.favbet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.favbet.config.FavbetConfig;
import pro.datawiki.igaming.source.favbet.dto.FavbetEvent;
import pro.datawiki.igaming.source.favbet.dto.FavbetResponse;
import pro.datawiki.igaming.source.favbet.dto.FavbetSport;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavbetApiClient {

    private final RestTemplate favbetRestTemplate;
    private final FavbetConfig favbetConfig;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private BrowserService browserService;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9,uk;q=0.8,ru;q=0.7");
        headers.set("Referer", "https://www.favbet.com/");
        headers.set("Origin", "https://www.favbet.com");
        return headers;
    }

    private String fetchJson(String url) {
        // 1. Try BrowserService first (solves Cloudflare Turnstile with stealth Chromium)
        if (browserService != null) {
            try {
                String body = browserService.navigateAndGetBody(url, 2000, "favbet");
                if (body != null && !body.trim().startsWith("<") && !body.isBlank()) {
                    return body.trim();
                }
            } catch (Exception be) {
                log.debug("Browser fetch for {} failed: {}", url, be.getMessage());
            }
        }

        // 2. Direct HTTP fallback
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = favbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String body = response.getBody().trim();
                if (!body.startsWith("<")) {
                    return body;
                }
            }
        } catch (Exception e) {
            log.debug("RestTemplate fetch for {} failed: {}", url, e.getMessage());
        }
        return null;
    }

    public List<FavbetSport> getSports() {
        String url = favbetConfig.getBaseUrl() + "/sports/";
        try {
            String body = fetchJson(url);
            if (body != null) {
                if (body.startsWith("{")) {
                    FavbetResponse<FavbetSport> resp = objectMapper.readValue(body, new TypeReference<FavbetResponse<FavbetSport>>() {});
                    return resp.getResult() != null ? resp.getResult() : new ArrayList<>();
                } else if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<FavbetSport>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch sports from Favbet: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<FavbetEvent> getEventsBySport(Long sportId) {
        String url = favbetConfig.getBaseUrl() + "/events/?sport_id=" + sportId + "&service_id=0";
        try {
            String body = fetchJson(url);
            if (body != null) {
                if (body.startsWith("{")) {
                    FavbetResponse<FavbetEvent> resp = objectMapper.readValue(body, new TypeReference<FavbetResponse<FavbetEvent>>() {});
                    return resp.getResult() != null ? resp.getResult() : new ArrayList<>();
                } else if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<FavbetEvent>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch events for sport {} from Favbet: {}", sportId, e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<FavbetEvent> getLiveEvents() {
        String url = favbetConfig.getBaseUrl() + "/events/?service_id=1";
        try {
            String body = fetchJson(url);
            if (body != null) {
                if (body.startsWith("{")) {
                    FavbetResponse<FavbetEvent> resp = objectMapper.readValue(body, new TypeReference<FavbetResponse<FavbetEvent>>() {});
                    return resp.getResult() != null ? resp.getResult() : new ArrayList<>();
                } else if (body.startsWith("[")) {
                    return objectMapper.readValue(body, new TypeReference<List<FavbetEvent>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Favbet: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public FavbetEvent getEventDetails(Long eventId) {
        String url = favbetConfig.getBaseUrl() + "/event/" + eventId + "/";
        try {
            String body = fetchJson(url);
            if (body != null && body.startsWith("{")) {
                if (body.contains("\"result\"")) {
                    FavbetResponse<FavbetEvent> resp = objectMapper.readValue(body, new TypeReference<FavbetResponse<FavbetEvent>>() {});
                    if (resp.getResult() != null && !resp.getResult().isEmpty()) {
                        return resp.getResult().get(0);
                    }
                }
                return objectMapper.readValue(body, FavbetEvent.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Favbet: {}", eventId, e.getMessage());
        }
        return null;
    }
}
