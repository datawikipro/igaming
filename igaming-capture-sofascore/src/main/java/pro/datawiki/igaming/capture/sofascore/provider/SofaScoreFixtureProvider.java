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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SofaScoreFixtureProvider implements MatchFixtureProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SOFASCORE_SCHEDULE_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";
    private static final String SOFASCORE_TEAM_URL = "https://api.sofascore.com/api/v1/team/{teamId}";

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
                || s.contains("CS2") || s.contains("ESPORTS");
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

                String homeLogo = homeId != null ? "https://api.sofascore.app/api/v1/team/" + homeId + "/image" : null;
                String awayLogo = awayId != null ? "https://api.sofascore.app/api/v1/team/" + awayId + "/image" : null;

                ReferenceFixtureDto dto = ReferenceFixtureDto.builder()
                        .provider(getProviderName())
                        .externalFixtureId(eventId)
                        .sport(sportName)
                        .leagueName(tournament.path("name").asText(null))
                        .countryName(category.path("name").asText(null))
                        .team1Name(home.path("name").asText(null))
                        .team1NameLocal(home.path("shortName").asText(null))
                        .team1ExternalId(homeId)
                        .team1LogoUrl(homeLogo)
                        .team2Name(away.path("name").asText(null))
                        .team2NameLocal(away.path("shortName").asText(null))
                        .team2ExternalId(awayId)
                        .team2LogoUrl(awayLogo)
                        .startTimeEpochMs(startTimestamp * 1000)
                        .status(event.path("status").path("type").asText("notstarted"))
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

    private String mapSportToSofa(String aggregatorSport) {
        if (aggregatorSport == null) return "football";
        return switch (aggregatorSport.toUpperCase()) {
            case "FOOTBALL" -> "football";
            case "TENNIS" -> "tennis";
            case "BASKETBALL" -> "basketball";
            case "HOCKEY", "ICE_HOCKEY", "ICE-HOCKEY" -> "hockey";
            case "VOLLEYBALL" -> "volleyball";
            case "ESPORTS", "DOTA2", "CS2" -> "esports";
            default -> aggregatorSport.toLowerCase();
        };
    }
}
