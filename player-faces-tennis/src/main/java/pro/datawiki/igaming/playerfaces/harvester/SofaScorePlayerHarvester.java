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
 * Harvester integrating with SofaScore player media API and search endpoints.
 */
@Slf4j
@Component
public class SofaScorePlayerHarvester implements PlayerHeadshotHarvester {

    private final RestTemplate restTemplate;

    public SofaScorePlayerHarvester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public AvatarSourceProvider getProvider() {
        return AvatarSourceProvider.SOFASCORE;
    }

    @Override
    public int getOrder() {
        return 40; // 4th in cascade
    }

    @Override
    public Optional<PlayerFaceDto> harvest(String playerName, SportType sport, TourType tour) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }

        try {
            String encodedName = URLEncoder.encode(playerName.trim(), StandardCharsets.UTF_8);
            String url = "https://api.sofascore.app/api/v1/search/all?q=" + encodedName;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                if (results != null) {
                    for (Map<String, Object> item : results) {
                        String type = (String) item.get("type");
                        if ("player".equalsIgnoreCase(type)) {
                            Map<String, Object> entity = (Map<String, Object>) item.get("entity");
                            if (entity != null) {
                                Object idObj = entity.get("id");
                                String name = (String) entity.get("name");
                                Map<String, Object> countryObj = (Map<String, Object>) entity.get("country");
                                String countryName = countryObj != null ? (String) countryObj.get("name") : null;
                                String countryAlpha2 = countryObj != null ? (String) countryObj.get("alpha2") : null;

                                if (idObj != null) {
                                    String avatarUrl = "https://api.sofascore.app/api/v1/player/" + idObj + "/image";
                                    return Optional.of(PlayerFaceDto.builder()
                                            .playerName(name != null ? name : playerName)
                                            .normalizedName(PlayerFace.normalizeName(name != null ? name : playerName))
                                            .sport(sport)
                                            .tour(tour)
                                            .country(countryName)
                                            .countryCode(countryAlpha2)
                                            .avatarUrl(avatarUrl)
                                            .sourceUrl(avatarUrl)
                                            .sourceProvider(AvatarSourceProvider.SOFASCORE)
                                            .webpOptimized(Boolean.TRUE)
                                            .lastHarvestedAt(LocalDateTime.now())
                                            .build());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("SofaScore harvest failed for player '{}': {}", playerName, e.getMessage());
        }

        return Optional.empty();
    }
}
