package pro.datawiki.igaming.analyst.ml.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.ml.tensor.TacticalDnaFeatureBlock;
import pro.datawiki.igaming.dto.sport.Sport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Institutional Qdrant Tactical DNA Client.
 * Fetches and caches 64-dimensional team vector embeddings from Qdrant vector space.
 * Computes exact Cosine Distance and builds strongly-typed {@link TacticalDnaFeatureBlock}.
 */
@Slf4j
@Service
public class QdrantTacticalDnaClient {

    private final String qdrantUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Fast in-memory cache: "collection:key" -> float[64]
    private final Map<String, float[]> vectorCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSyncByCollection = new ConcurrentHashMap<>();

    public QdrantTacticalDnaClient(
            @Value("${analyst.qdrant.url:http://qdrant.igaming-analytics.svc.cluster.local:6333}") String qdrantUrl,
            ObjectMapper objectMapper) {
        this.qdrantUrl = qdrantUrl.endsWith("/") ? qdrantUrl.substring(0, qdrantUrl.length() - 1) : qdrantUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Resolves Home and Away tactical DNA embeddings and builds a verified 129-dim {@link TacticalDnaFeatureBlock}.
     */
    public TacticalDnaFeatureBlock fetchTacticalDna(Sport sport, Long team1Id, String team1Name, Long team2Id, String team2Name) {
        if (sport == null) {
            return TacticalDnaFeatureBlock.neutral();
        }

        String collection = sport.getTacticalDnaCollectionName();
        int expectedDim = sport.getTacticalDnaVectorDimension();

        // Warmup collection cache if not synced in last 6 hours
        ensureCollectionSynced(collection);

        float[] v1 = resolveTeamVector(collection, team1Id, team1Name, expectedDim);
        float[] v2 = resolveTeamVector(collection, team2Id, team2Name, expectedDim);

        // Compute Cosine Distance: 1.0 - Cosine Similarity
        float norm1 = computeNorm(v1);
        float norm2 = computeNorm(v2);
        float cosDist = 1.0f;

        if (norm1 > 1e-6f && norm2 > 1e-6f) {
            float dot = computeDot(v1, v2);
            float cosSim = dot / (norm1 * norm2);
            cosDist = Math.max(0.0f, Math.min(2.0f, 1.0f - cosSim));
        }

        return new TacticalDnaFeatureBlock(v1, v2, cosDist);
    }

    private float[] resolveTeamVector(String collection, Long teamId, String teamName, int expectedDim) {
        if (teamId != null) {
            float[] vec = vectorCache.get(cacheKey(collection, "id:" + teamId));
            if (vec != null && vec.length == expectedDim) return vec;

            vec = vectorCache.get(cacheKey(collection, teamId.toString()));
            if (vec != null && vec.length == expectedDim) return vec;
        }

        if (teamName != null && !teamName.isBlank()) {
            float[] vec = vectorCache.get(cacheKey(collection, teamName.trim().toLowerCase()));
            if (vec != null && vec.length == expectedDim) return vec;
        }

        // Neutral zero vector fallback
        return new float[expectedDim];
    }

    private synchronized void ensureCollectionSynced(String collection) {
        long now = System.currentTimeMillis();
        Long lastSync = lastSyncByCollection.get(collection);
        if (lastSync != null && (now - lastSync) < (6 * 3600 * 1000L)) {
            return;
        }

        try {
            log.info("Prefetching tactical DNA vectors from Qdrant collection '{}'...", collection);
            String url = qdrantUrl + "/collections/" + collection + "/points/scroll";
            String offset = null;
            int loadedCount = 0;

            while (true) {
                Map<String, Object> body = Map.of(
                        "limit", 500,
                        "with_vector", true,
                        "with_payload", true
                );
                if (offset != null) {
                    body = new java.util.HashMap<>(body);
                    body.put("offset", offset);
                }

                String json = objectMapper.writeValueAsString(body);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200 || resp.body() == null) {
                    log.warn("Qdrant collection '{}' returned HTTP {}. Using local cache.", collection, resp.statusCode());
                    break;
                }

                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode result = root.path("result");
                JsonNode points = result.path("points");

                if (!points.isArray() || points.isEmpty()) {
                    break;
                }

                for (JsonNode pt : points) {
                    JsonNode vecNode = pt.path("vector");
                    if (!vecNode.isArray()) continue;

                    float[] vec = new float[vecNode.size()];
                    for (int i = 0; i < vec.length; i++) {
                        vec[i] = (float) vecNode.get(i).asDouble(0.0);
                    }

                    String ptId = pt.path("id").asText(null);
                    if (ptId != null) {
                        vectorCache.put(cacheKey(collection, ptId), vec);
                    }

                    JsonNode payload = pt.path("payload");
                    if (!payload.isMissingNode()) {
                        long tId = payload.path("team_id").asLong(0L);
                        if (tId > 0) {
                            vectorCache.put(cacheKey(collection, "id:" + tId), vec);
                            vectorCache.put(cacheKey(collection, String.valueOf(tId)), vec);
                        }
                        String name = payload.path("team_name").asText(payload.path("name").asText(null));
                        if (name != null && !name.isBlank()) {
                            vectorCache.put(cacheKey(collection, name.trim().toLowerCase()), vec);
                        }
                    }
                    loadedCount++;
                }

                JsonNode nextOffset = result.path("next_page_offset");
                if (nextOffset.isNull() || nextOffset.isMissingNode()) {
                    break;
                }
                offset = nextOffset.asText();
            }

            lastSyncByCollection.put(collection, now);
            log.info("Successfully cached {} tactical DNA vectors from Qdrant collection '{}'.", loadedCount, collection);
        } catch (Exception e) {
            log.warn("Failed to sync Qdrant collection '{}' ({}). Continuing with available vectors.", collection, e.getMessage());
            lastSyncByCollection.put(collection, now); // backoff
        }
    }

    private static String cacheKey(String collection, String key) {
        return collection + ":" + key;
    }

    private static float computeNorm(float[] v) {
        float sum = 0.0f;
        for (float f : v) sum += f * f;
        return (float) Math.sqrt(sum);
    }

    private static float computeDot(float[] v1, float[] v2) {
        float dot = 0.0f;
        int len = Math.min(v1.length, v2.length);
        for (int i = 0; i < len; i++) {
            dot += v1[i] * v2[i];
        }
        return dot;
    }
}
