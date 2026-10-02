package pro.datawiki.igaming.playerfaces.harvester;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wikimedia Commons API harvester for finding athlete headshot photos
 * under Creative Commons and public domain licenses.
 */
@Slf4j
@Component
public class WikimediaCommonsHarvester implements PlayerHeadshotHarvester {

    private final RestTemplate restTemplate;

    public WikimediaCommonsHarvester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public AvatarSourceProvider getProvider() {
        return AvatarSourceProvider.WIKIMEDIA;
    }

    @Override
    public int getOrder() {
        return 30; // 3rd in cascade
    }

    @Override
    public Optional<PlayerFaceDto> harvest(String playerName, SportType sport, TourType tour) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }

        try {
            String searchQuery = playerName.trim() + " " + (sport != null ? sport.name().toLowerCase() : "");
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = "https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch="
                    + encodedQuery + "&srnamespace=6&format=json&srlimit=3";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> queryObj = (Map<String, Object>) response.getBody().get("query");
                if (queryObj != null) {
                    List<Map<String, Object>> searchResults = (List<Map<String, Object>>) queryObj.get("search");
                    if (searchResults != null && !searchResults.isEmpty()) {
                        for (Map<String, Object> item : searchResults) {
                            String title = (String) item.get("title");
                            if (title != null && isSuitableImage(title)) {
                                String cleanTitle = title.replace("File:", "").replace("file:", "");
                                String imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/"
                                        + URLEncoder.encode(cleanTitle.replace(" ", "_"), StandardCharsets.UTF_8)
                                        + "?width=500";

                                return Optional.of(PlayerFaceDto.builder()
                                        .playerName(playerName)
                                        .normalizedName(PlayerFace.normalizeName(playerName))
                                        .sport(sport)
                                        .tour(tour)
                                        .avatarUrl(imageUrl)
                                        .sourceUrl("https://commons.wikimedia.org/wiki/" + URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8))
                                        .sourceProvider(AvatarSourceProvider.WIKIMEDIA)
                                        .webpOptimized(Boolean.TRUE)
                                        .lastHarvestedAt(LocalDateTime.now())
                                        .build());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Wikimedia harvest failed for player '{}': {}", playerName, e.getMessage());
        }

        return Optional.empty();
    }

    private boolean isSuitableImage(String title) {
        String lower = title.toLowerCase();
        return (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))
                && !lower.contains("logo")
                && !lower.contains("stadium")
                && !lower.contains("court")
                && !lower.contains("flag")
                && !lower.contains("map");
    }
}
