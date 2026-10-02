package pro.datawiki.igaming.capture.sofascore.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.dto.ReferenceFixtureDto;
import pro.datawiki.igaming.dto.TeamProfileDto;
import pro.datawiki.igaming.capture.sofascore.util.NationalTeamDetector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class SofaScoreFixtureProvider implements MatchFixtureProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SOFASCORE_SCHEDULE_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";
    private static final String SOFASCORE_TEAM_URL = "https://api.sofascore.com/api/v1/team/{teamId}";
    private static final String SOFASCORE_PLAYER_URL = "https://api.sofascore.app/api/v1/player/{playerId}/image";

    @Override
    public String getProviderName() {
        return "SOFASCORE";
    }

    @Override
    public boolean supportsSport(String sportName) {
        if (sportName == null) return false;
        String s = sportName.toUpperCase();
        return s.contains("FOOTBALL") || s.contains("BASKETBALL") || s.contains("HOCKEY")
                || s.contains("TENNIS") || s.contains("VOLLEYBALL") || s.contains("DOTA")
                || s.contains("CS2") || s.contains("ESPORTS")
                || s.contains("MMA") || s.contains("BOXING") || s.contains("BOX")
                || s.contains("BADMINTON") || s.contains("WRESTLING") || s.contains("DARTS");
    }

    @Override
    public List<ReferenceFixtureDto> fetchScheduledFixtures(String sportName, LocalDate date) {
        String sport = mapSportToSofa(sportName);
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.debug("[SofaScore Fixtures] Fetching scheduled events for sport '{}' on {}", sport, dateStr);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            headers.set("Accept-Language", "ru,en;q=0.9");
            headers.set("Origin", "https://www.sofascore.com");
            headers.set("Referer", "https://www.sofascore.com/");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SOFASCORE_SCHEDULE_URL,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    sport,
                    dateStr
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) {
                return Collections.emptyList();
            }

            List<ReferenceFixtureDto> result = new ArrayList<>();
            for (JsonNode event : events) {
                String eventId = event.path("id").asText(null);
                long startTimestamp = event.path("startTimestamp").asLong(0);
                if (eventId == null || startTimestamp == 0) continue;

                JsonNode home = event.path("homeTeam");
                JsonNode away = event.path("awayTeam");
                JsonNode tournament = event.path("tournament");
                JsonNode category = tournament.path("category");

                String homeId = home.path("id").asText(null);
                String awayId = away.path("id").asText(null);
                String homeName = home.path("name").asText(null);
                String awayName = away.path("name").asText(null);

                // For team sports: use team logo URL. For individual/single-player sports (tennis,
                // MMA, boxing, etc.): prefer the player photo portrait endpoint.
                boolean isSinglePlayer = isSinglePlayerSport(sportName)
                        || "player".equalsIgnoreCase(home.path("type").asText(null))
                        || "player".equalsIgnoreCase(away.path("type").asText(null));

                String homeLogo;
                String awayLogo;
                String homePlayerId = null;
                String awayPlayerId = null;

                if (isSinglePlayer) {
                    // In SofaScore single-player matches the homeTeam node may contain
                    // a nested "player" object with the actual player ID.
                    JsonNode homePlayer = home.path("player");
                    JsonNode awayPlayer = away.path("player");

                    homePlayerId = homePlayer.isMissingNode() ? homeId : homePlayer.path("id").asText(homeId);
                    awayPlayerId = awayPlayer.isMissingNode() ? awayId : awayPlayer.path("id").asText(awayId);

                    // Player portrait (face avatar): /api/v1/player/{id}/image
                    homeLogo = homePlayerId != null
                            ? "https://api.sofascore.app/api/v1/player/" + homePlayerId + "/image"
                            : null;
                    awayLogo = awayPlayerId != null
                            ? "https://api.sofascore.app/api/v1/player/" + awayPlayerId + "/image"
                            : null;
                } else {
                    homeLogo = homeId != null ? "https://api.sofascore.app/api/v1/team/" + homeId + "/image" : null;
                    awayLogo = awayId != null ? "https://api.sofascore.app/api/v1/team/" + awayId + "/image" : null;
                }

                // Populate metadata for consumer services (aggregator, portal, frontend)
                Map<String, Object> metadata = null;
                if (isSinglePlayer) {
                    metadata = new HashMap<>();
                    metadata.put("isIndividualSport", true);
                    if (homeLogo != null) metadata.put("team1FaceUrl", homeLogo);
                    if (awayLogo != null) metadata.put("team2FaceUrl", awayLogo);
                    if (homePlayerId != null) metadata.put("team1PlayerId", homePlayerId);
                    if (awayPlayerId != null) metadata.put("team2PlayerId", awayPlayerId);
                    log.debug("[SofaScore Fixtures] Individual sport match detected ({}): {} vs {}, " +
                                    "homePlayerId={}, awayPlayerId={}",
                            sportName,
                            home.path("name").asText("?"),
                            away.path("name").asText("?"),
                            homePlayerId, awayPlayerId);
                }

                String homePlayerFace = null;
                String awayPlayerFace = null;
                if (isSinglePlayerSport(sportName)) {
                    homePlayerFace = homeId != null ? "https://api.sofascore.app/api/v1/player/" + homeId + "/image" : null;
                    awayPlayerFace = awayId != null ? "https://api.sofascore.app/api/v1/player/" + awayId + "/image" : null;
                    log.debug("[SofaScore Fixtures] Player face URLs populated for single-sport {}: {} vs {}",
                            sportName, homeName, awayName);
                }

                // ── National team detection ───────────────────────────────────
                boolean homeIsNational = NationalTeamDetector.isNationalTeamFromApi(home)
                        || NationalTeamDetector.isNationalTeamByName(homeName);
                boolean awayIsNational = NationalTeamDetector.isNationalTeamFromApi(away)
                        || NationalTeamDetector.isNationalTeamByName(awayName);

                String homeCountryCode = homeIsNational
                        ? NationalTeamDetector.resolveCountryCode(home, homeName, category)
                        : null;
                String awayCountryCode = awayIsNational
                        ? NationalTeamDetector.resolveCountryCode(away, awayName, category)
                        : null;

                String homeFlagUrl = homeIsNational ? NationalTeamDetector.buildFlagUrl(homeId) : null;
                String awayFlagUrl = awayIsNational ? NationalTeamDetector.buildFlagUrl(awayId) : null;

                if (homeIsNational || awayIsNational) {
                    log.debug("[SofaScore Fixtures] National team detected in event {}: home={} ({}), away={} ({})",
                            eventId, homeName, homeCountryCode, awayName, awayCountryCode);
                }
                // ─────────────────────────────────────────────────────────────

                ReferenceFixtureDto dto = ReferenceFixtureDto.builder()
                        .provider(getProviderName())
                        .externalFixtureId(eventId)
                        .sport(sportName)
                        .leagueName(tournament.path("name").asText(null))
                        .countryName(category.path("name").asText(null))
                        .team1Name(homeName)
                        .team1NameLocal(home.path("shortName").asText(null))
                        .team1ExternalId(homeId)
                        .team1LogoUrl(homeLogo)
                        .team1PlayerFaceUrl(homePlayerFace)
                        .team1IsNational(homeIsNational)
                        .team1CountryCode(homeCountryCode)
                        .team1FlagUrl(homeFlagUrl)
                        .team2Name(awayName)
                        .team2NameLocal(away.path("shortName").asText(null))
                        .team2ExternalId(awayId)
                        .team2LogoUrl(awayLogo)
                        .team2PlayerFaceUrl(awayPlayerFace)
                        .team2IsNational(awayIsNational)
                        .team2CountryCode(awayCountryCode)
                        .team2FlagUrl(awayFlagUrl)
                        .startTimeEpochMs(startTimestamp * 1000)
                        .status(event.path("status").path("type").asText("notstarted"))
                        .metadata(metadata)
                        .build();

                result.add(dto);
            }

            log.info("[SofaScore Fixtures] Successfully ingested {} fixtures for sport '{}' ({})",
                    result.size(), sportName, dateStr);
            return result;

        } catch (Exception e) {
            log.warn("[SofaScore Fixtures] Error fetching scheduled events for sport '{}': {}", sportName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<TeamProfileDto> fetchTeamProfile(String externalTeamId, String sportName) {
        if (externalTeamId == null || externalTeamId.isBlank()) {
            return Optional.empty();
        }

        // For individual sports, attempt to use the player endpoint first, which returns
        // the player's portrait photo (face avatar) rather than a team logo.
        if (isSinglePlayerSport(sportName)) {
            return fetchPlayerProfile(externalTeamId, sportName);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            headers.set("Accept-Language", "ru,en;q=0.9");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SOFASCORE_TEAM_URL,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    externalTeamId
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode team = root.path("team");
            if (team.isMissingNode()) return Optional.empty();

            String name = team.path("name").asText(null);
            String shortName = team.path("shortName").asText(null);
            String country = team.path("country").path("name").asText(null);
            String logoUrl = "https://api.sofascore.app/api/v1/team/" + externalTeamId + "/image";

            TeamProfileDto profile = TeamProfileDto.builder()
                    .provider(getProviderName())
                    .externalId(externalTeamId)
                    .sport(sportName)
                    .nameEnglish(name)
                    .nameLocal(shortName)
                    .nameShort(shortName)
                    .country(country)
                    .logoUrl(logoUrl)
                    .build();

            return Optional.of(profile);

        } catch (Exception e) {
            log.debug("[SofaScore Fixtures] Failed to fetch team profile for ID {}: {}", externalTeamId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetches a player profile (for individual sports like Tennis, MMA, Boxing) using the
     * SofaScore player API endpoint. The player's portrait image URL is stored as the logoUrl
     * and also in metadata["faceUrl"] for explicit consumer access.
     */
    private Optional<TeamProfileDto> fetchPlayerProfile(String playerId, String sportName) {
        String playerApiUrl = "https://api.sofascore.com/api/v1/player/" + playerId;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            headers.set("Accept-Language", "ru,en;q=0.9");
            headers.set("Origin", "https://www.sofascore.com");
            headers.set("Referer", "https://www.sofascore.com/");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    playerApiUrl, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.debug("[SofaScore Fixtures] Player API returned non-2xx for playerId={}", playerId);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode player = root.path("player");
            if (player.isMissingNode()) return Optional.empty();

            String name = player.path("name").asText(null);
            String shortName = player.path("shortName").asText(null);
            String country = player.path("country").path("name").asText(null);
            String countryCode = player.path("country").path("alpha2").asText(null);

            // Face avatar URL for player portrait
            String faceUrl = "https://api.sofascore.app/api/v1/player/" + playerId + "/image";

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("faceUrl", faceUrl);
            metadata.put("isPlayer", true);

            TeamProfileDto profile = TeamProfileDto.builder()
                    .provider(getProviderName())
                    .externalId(playerId)
                    .sport(sportName)
                    .nameEnglish(name)
                    .nameLocal(shortName)
                    .nameShort(shortName)
                    .country(country)
                    .countryCode(countryCode)
                    .logoUrl(faceUrl)   // logoUrl = face avatar for individual athletes
                    .metadata(metadata)
                    .build();

            log.debug("[SofaScore Fixtures] Fetched player profile: name='{}', sport={}, faceUrl={}",
                    name, sportName, faceUrl);
            return Optional.of(profile);

        } catch (Exception e) {
            log.debug("[SofaScore Fixtures] Failed to fetch player profile for playerId={}: {}", playerId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Returns true for sports where participants are individual athletes rather than teams.
     * In these sports SofaScore uses /player/{id}/image for face avatars instead of /team/{id}/image.
     */
    private boolean isSinglePlayerSport(String sportName) {
        if (sportName == null) return false;
        return switch (sportName.toUpperCase()) {
            case "TENNIS", "TABLE_TENNIS", "TABLE-TENNIS", "BADMINTON", "SQUASH",
                 "MMA", "BOXING", "BOX", "WRESTLING", "DARTS", "SNOOKER" -> true;
            default -> false;
        };
    }

    private String mapSportToSofa(String aggregatorSport) {
        if (aggregatorSport == null) return "football";
        return switch (aggregatorSport.toUpperCase()) {
            case "FOOTBALL" -> "football";
            case "TENNIS" -> "tennis";
            case "BASKETBALL" -> "basketball";
            case "HOCKEY", "ICE_HOCKEY", "ICE-HOCKEY" -> "hockey";
            case "VOLLEYBALL" -> "volleyball";
            case "ESPORTS", "DOTA2", "CS2" -> "esports";
            case "TABLE_TENNIS", "TABLE-TENNIS" -> "table-tennis";
            case "BADMINTON" -> "badminton";
            case "MMA" -> "mma";
            case "BOXING", "BOX" -> "boxing";
            default -> aggregatorSport.toLowerCase();
        };
    }

    /**
     * Returns true for individual (single-player) sports where homeTeam/awayTeam in SofaScore
     * actually represent individual players. For these sports, player face portrait URLs
     * should be populated using the SofaScore player image API.
     *
     * @param sportName aggregator sport name (case-insensitive)
     * @return true if sport is played by individual athletes
     */
    private boolean isSinglePlayerSport(String sportName) {
        if (sportName == null) return false;
        return switch (sportName.toUpperCase()) {
            case "TENNIS", "MMA", "BOXING", "TABLE_TENNIS", "BADMINTON", "SQUASH", "DARTS" -> true;
            default -> false;
        };
    }
}
