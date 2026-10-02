package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.util.IndividualSportDetector;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for fetching and in-memory caching of player face avatar metadata from SofaScore.
 *
 * <p>For individual sports (tennis, MMA, boxing, badminton, squash, padel, etc.),
 * SofaScore event entries use {@code homeTeam}/{@code awayTeam} nodes that actually
 * represent individual players (not teams). This service resolves the face portrait URL
 * and a basic profile for a given SofaScore player ID and sport.</p>
 *
 * <p>Results are cached in-memory (TTL not enforced — the service is periodically
 * restarted via Kubernetes rolling-update or the JVM lifecycle caps retention naturally).
 * For production, an external Redis cache could be added, but the current volume
 * (hundreds of active players per sport) fits comfortably in a 512 Mi heap.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerAvatarFetchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SOFASCORE_PLAYER_URL =
            "https://api.sofascore.com/api/v1/player/{playerId}";

    /** In-memory cache: sofaPlayerId → PlayerFaceProfile */
    private final ConcurrentHashMap<String, PlayerFaceProfile> cache = new ConcurrentHashMap<>();

    /**
     * Returns a {@link PlayerFaceProfile} for the given SofaScore player ID.
     *
     * <p>The result contains the portrait avatar URL and a basic player name / country,
     * which can be forwarded to the portal API or embedded in match card metadata.</p>
     *
     * @param sofaPlayerId SofaScore internal player ID (numeric string)
     * @param sportName    aggregator sport name, e.g. {@code "TENNIS"} (used only for logging)
     * @return player profile if successfully fetched; empty if the player is unknown or the API fails
     */
    public Optional<PlayerFaceProfile> fetchPlayerFace(String sofaPlayerId, String sportName) {
        if (sofaPlayerId == null || sofaPlayerId.isBlank()) {
            return Optional.empty();
        }

        // Fast path: check in-memory cache
        PlayerFaceProfile cached = cache.get(sofaPlayerId);
        if (cached != null) {
            log.trace("[PlayerAvatarFetch] Cache hit for player {} ({})", sofaPlayerId, sportName);
            return Optional.of(cached);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            headers.set("Accept-Language", "ru,en;q=0.9");
            headers.set("Origin", "https://www.sofascore.com");
            headers.set("Referer", "https://www.sofascore.com/");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SOFASCORE_PLAYER_URL,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    sofaPlayerId
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.debug("[PlayerAvatarFetch] Non-2xx response for player {} ({}): {}",
                        sofaPlayerId, sportName, response.getStatusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode player = root.path("player");
            if (player.isMissingNode()) {
                return Optional.empty();
            }

            String name = player.path("name").asText(null);
            String shortName = player.path("shortName").asText(name);
            String country = player.path("country").path("name").asText(null);
            String countryCode = player.path("country").path("alpha2").asText(null);
            String faceUrl = IndividualSportDetector.buildPlayerAvatarUrl(sofaPlayerId);

            PlayerFaceProfile profile = new PlayerFaceProfile(
                    sofaPlayerId, name, shortName, country, countryCode, faceUrl, sportName
            );

            cache.put(sofaPlayerId, profile);
            log.info("[PlayerAvatarFetch] Fetched player face for {} ({}) → {}", name, sportName, faceUrl);
            return Optional.of(profile);

        } catch (Exception e) {
            log.debug("[PlayerAvatarFetch] Failed to fetch player {} ({}): {}", sofaPlayerId, sportName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Returns the number of player profiles currently held in the in-memory cache.
     */
    public int cacheSize() {
        return cache.size();
    }

    /**
     * Immutable value object representing a player's face profile fetched from SofaScore.
     *
     * @param sofaPlayerId  SofaScore internal player ID
     * @param fullName      player's full name in English
     * @param shortName     short/abbreviated display name
     * @param country       country name in English
     * @param countryCode   ISO 3166-1 alpha-2 country code (nullable)
     * @param faceAvatarUrl absolute URL to the player portrait image
     * @param sport         sport name as used by the aggregator
     */
    public record PlayerFaceProfile(
            String sofaPlayerId,
            String fullName,
            String shortName,
            String country,
            String countryCode,
            String faceAvatarUrl,
            String sport
    ) {}
}
