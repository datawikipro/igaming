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
 * Wikidata SPARQL / MediaWiki API harvester for extracting open-license athlete portraits,
 * country of citizenship (P27) and Wikidata P18 image claims.
 */
@Slf4j
@Component
public class WikidataHeadshotHarvester implements PlayerHeadshotHarvester {

    private final RestTemplate restTemplate;

    public WikidataHeadshotHarvester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public AvatarSourceProvider getProvider() {
        return AvatarSourceProvider.WIKIDATA;
    }

    @Override
    public int getOrder() {
        return 20; // 2nd in cascade
    }

    @Override
    public Optional<PlayerFaceDto> harvest(String playerName, SportType sport, TourType tour) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }

        try {
            String encodedName = URLEncoder.encode(playerName.trim(), StandardCharsets.UTF_8);
            String url = "https://www.wikidata.org/w/api.php?action=wbsearchentities&search="
                    + encodedName + "&language=en&format=json&limit=3";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> searchResults = (List<Map<String, Object>>) response.getBody().get("search");
                if (searchResults != null && !searchResults.isEmpty()) {
                    for (Map<String, Object> result : searchResults) {
                        String description = (String) result.get("description");
                        String label = (String) result.get("label");
                        String entityId = (String) result.get("id");

                        // Check if entity matches sport keywords
                        if (isMatchingSportDescription(description, sport)) {
                            return fetchEntityDetails(entityId, label != null ? label : playerName, sport, tour);
                        }
                    }

                    // Fallback to first entity if description is generic athlete
                    Map<String, Object> first = searchResults.get(0);
                    String entityId = (String) first.get("id");
                    String label = (String) first.get("label");
                    return fetchEntityDetails(entityId, label != null ? label : playerName, sport, tour);
                }
            }
        } catch (Exception e) {
            log.debug("Wikidata harvest failed for player '{}': {}", playerName, e.getMessage());
        }

        return Optional.empty();
    }

    private boolean isMatchingSportDescription(String description, SportType sport) {
        if (description == null) {
            return false;
        }
        String desc = description.toLowerCase();
        if (sport == SportType.TENNIS) {
            return desc.contains("tennis") || desc.contains("player");
        } else if (sport == SportType.MMA) {
            return desc.contains("mixed martial") || desc.contains("mma") || desc.contains("fighter") || desc.contains("ufc");
        } else if (sport == SportType.BOXING) {
            return desc.contains("boxer") || desc.contains("boxing");
        } else if (sport == SportType.TABLE_TENNIS) {
            return desc.contains("table tennis") || desc.contains("ping-pong");
        }
        return desc.contains("athlete") || desc.contains("player") || desc.contains("sports");
    }

    private Optional<PlayerFaceDto> fetchEntityDetails(String entityId, String playerName, SportType sport, TourType tour) {
        try {
            String url = "https://www.wikidata.org/wiki/Special:EntityData/" + entityId + ".json";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            Map<String, Object> entities = (Map<String, Object>) response.getBody().get("entities");
            if (entities == null || !entities.containsKey(entityId)) {
                return Optional.empty();
            }

            Map<String, Object> entityData = (Map<String, Object>) entities.get(entityId);
            Map<String, Object> claims = (Map<String, Object>) entityData.get("claims");
            if (claims == null) {
                return Optional.empty();
            }

            // P18 is the Wikidata claim for Image
            List<Map<String, Object>> imageClaims = (List<Map<String, Object>>) claims.get("P18");
            if (imageClaims != null && !imageClaims.isEmpty()) {
                Map<String, Object> mainSnak = (Map<String, Object>) imageClaims.get(0).get("mainsnak");
                if (mainSnak != null && mainSnak.containsKey("datavalue")) {
                    Map<String, Object> dataValue = (Map<String, Object>) mainSnak.get("datavalue");
                    String imageName = (String) dataValue.get("value");
                    if (imageName != null && !imageName.isBlank()) {
                        String imageFileUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/"
                                + URLEncoder.encode(imageName.replace(" ", "_"), StandardCharsets.UTF_8)
                                + "?width=500";

                        return Optional.of(PlayerFaceDto.builder()
                                .playerName(playerName)
                                .normalizedName(PlayerFace.normalizeName(playerName))
                                .sport(sport)
                                .tour(tour)
                                .avatarUrl(imageFileUrl)
                                .sourceUrl("https://www.wikidata.org/wiki/" + entityId)
                                .sourceProvider(AvatarSourceProvider.WIKIDATA)
                                .webpOptimized(Boolean.TRUE)
                                .lastHarvestedAt(LocalDateTime.now())
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch entity details for {}: {}", entityId, e.getMessage());
        }

        return Optional.empty();
    }
}
