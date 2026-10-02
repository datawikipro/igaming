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
 * Harvester for TheSportsDB API extracting high quality player cutouts (strCutout)
 * and thumbnail portraits (strThumb).
 */
@Slf4j
@Component
public class TheSportsDbHarvester implements PlayerHeadshotHarvester {

    private final RestTemplate restTemplate;

    public TheSportsDbHarvester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public AvatarSourceProvider getProvider() {
        return AvatarSourceProvider.THE_SPORTS_DB;
    }

    @Override
    public int getOrder() {
        return 10; // Top priority in cascade
    }

    @Override
    public Optional<PlayerFaceDto> harvest(String playerName, SportType sport, TourType tour) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }

        try {
            String encodedName = URLEncoder.encode(playerName.trim(), StandardCharsets.UTF_8);
            String url = "https://www.thesportsdb.com/api/v1/json/3/searchplayers.php?p=" + encodedName;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> playerList = (List<Map<String, Object>>) response.getBody().get("player");
                if (playerList != null && !playerList.isEmpty()) {
                    for (Map<String, Object> p : playerList) {
                        String strCutout = (String) p.get("strCutout");
                        String strThumb = (String) p.get("strThumb");
                        String strPlayer = (String) p.get("strPlayer");
                        String strNationality = (String) p.get("strNationality");

                        String chosenAvatar = (strCutout != null && !strCutout.isBlank()) ? strCutout : strThumb;
                        if (chosenAvatar != null && !chosenAvatar.isBlank()) {
                            return Optional.of(PlayerFaceDto.builder()
                                    .playerName(strPlayer != null ? strPlayer : playerName)
                                    .normalizedName(PlayerFace.normalizeName(strPlayer != null ? strPlayer : playerName))
                                    .sport(sport)
                                    .tour(tour)
                                    .country(strNationality)
                                    .avatarUrl(chosenAvatar)
                                    .sourceUrl(chosenAvatar)
                                    .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                                    .webpOptimized(Boolean.TRUE)
                                    .lastHarvestedAt(LocalDateTime.now())
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("TheSportsDB harvest failed for player '{}': {}", playerName, e.getMessage());
        }

        return Optional.empty();
    }
}
