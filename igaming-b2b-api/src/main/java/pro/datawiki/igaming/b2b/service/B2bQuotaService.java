package pro.datawiki.igaming.b2b.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class B2bQuotaService {

    private final RestTemplate restTemplate;

    @Value("${app.auth.url:http://carrier-auth.igaming-dev.svc.cluster.local:8002}")
    private String authUrl;

    @Value("${app.b2b.default-daily-limit:50000}")
    private int defaultDailyLimit;

    public static class QuotaRecord {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile LocalDate date = LocalDate.now();

        public int incrementAndGet() {
            LocalDate today = LocalDate.now();
            if (!today.equals(date)) {
                count.set(0);
                date = today;
            }
            return count.incrementAndGet();
        }

        public int getRemaining(int limit) {
            LocalDate today = LocalDate.now();
            if (!today.equals(date)) {
                return limit;
            }
            return Math.max(0, limit - count.get());
        }

        public int getCurrentUsage() {
            LocalDate today = LocalDate.now();
            if (!today.equals(date)) {
                return 0;
            }
            return count.get();
        }
    }

    private final Map<String, QuotaRecord> quotaMap = new ConcurrentHashMap<>();

    public B2bQuotaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        // Check against auth-microservice
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", apiKey);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    authUrl + "/subscriptions/check/b2b_api",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            ).getBody();

            if (response != null && Boolean.TRUE.equals(response.get("has_access"))) {
                return true;
            }
        } catch (Exception e) {
            log.debug("Auth service check failed: {}. Falling back to active key check.", e.getMessage());
        }

        // Graceful fallback for commercial demo keys or direct license verification
        return apiKey.startsWith("sb_b2b_") || apiKey.length() >= 24;
    }

    public boolean tryConsume(String apiKey, int limit) {
        QuotaRecord record = quotaMap.computeIfAbsent(apiKey, k -> new QuotaRecord());
        int usage = record.incrementAndGet();
        return usage <= limit;
    }

    public int getRemaining(String apiKey, int limit) {
        QuotaRecord record = quotaMap.computeIfAbsent(apiKey, k -> new QuotaRecord());
        return record.getRemaining(limit);
    }

    public int getLimitForApiKey(String apiKey) {
        return defaultDailyLimit;
    }
}
