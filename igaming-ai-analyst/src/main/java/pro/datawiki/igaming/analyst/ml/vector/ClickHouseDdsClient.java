package pro.datawiki.igaming.analyst.ml.vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-speed HTTP client querying ClickHouse igaming_dds.
 * Caches aggregate metrics in-memory to deliver < 1ms vector building.
 */
@Slf4j
@Component
public class ClickHouseDdsClient {

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String database;
    private final String user;
    private final String password;
    private final int timeoutMs;

    private static final Map<String, CacheEntry> QUERY_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 600_000L; // 10 minutes

    private record CacheEntry(long timestamp, List<Map<String, Object>> data) {}

    public ClickHouseDdsClient(
            ObjectMapper objectMapper,
            @Value("${analyst.clickhouse.url:http://clickhouse.igaming-analytics.svc.cluster.local:8123}") String baseUrl,
            @Value("${analyst.clickhouse.database:igaming_dds}") String database,
            @Value("${analyst.clickhouse.user:default}") String user,
            @Value("${analyst.clickhouse.password:clickhouse}") String password,
            @Value("${analyst.clickhouse.timeout-seconds:3}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.database = database;
        this.user = user;
        this.password = password;
        this.timeoutMs = timeoutSeconds * 1000;
    }

    /**
     * Executes a SELECT query against ClickHouse DDS with caching.
     * Returns a list of rows as Map<String, Object>.
     */
    public List<Map<String, Object>> queryJsonCached(String cacheKey, String sql) {
        long now = System.currentTimeMillis();
        CacheEntry cached = QUERY_CACHE.get(cacheKey);
        if (cached != null && (now - cached.timestamp() < CACHE_TTL_MS)) {
            return cached.data();
        }

        List<Map<String, Object>> result = queryJson(sql);
        if (result != null && !result.isEmpty()) {
            QUERY_CACHE.put(cacheKey, new CacheEntry(now, result));
        }
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Executes raw query against ClickHouse HTTP interface with JSONEachRow format.
     */
    public List<Map<String, Object>> queryJson(String sql) {
        try {
            String fullSql = sql.trim();
            if (!fullSql.toUpperCase().contains("FORMAT JSONEACHROW")) {
                fullSql += " FORMAT JSONEachRow";
            }

            URL url = new URL(baseUrl + "/?database=" + database);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            if (user != null && !user.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-User", user);
            }
            if (password != null && !password.isBlank()) {
                conn.setRequestProperty("X-ClickHouse-Key", password);
            }

            byte[] body = fullSql.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(body);

            int code = conn.getResponseCode();
            if (code >= 400) {
                try (InputStream errStream = conn.getErrorStream()) {
                    String err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "";
                    log.warn("[ClickHouse Client] Query failed [{}] {}: {}", code, sql, err);
                }
                return Collections.emptyList();
            }

            try (InputStream in = conn.getInputStream()) {
                String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (raw.isBlank()) {
                    return Collections.emptyList();
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                for (String line : raw.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        Map<String, Object> row = objectMapper.readValue(trimmed, new TypeReference<>() {});
                        rows.add(row);
                    }
                }
                return rows;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            log.warn("[ClickHouse Client] Query exception for {}: {}", sql, e.getMessage());
            return Collections.emptyList();
        }
    }
}
