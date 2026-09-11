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

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EspnFixtureProvider implements MatchFixtureProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String ESPN_SOCCER_URL = "https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard?dates={date}";
    private static final String ESPN_BASKETBALL_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates={date}";
    private static final String ESPN_HOCKEY_URL = "https://site.api.espn.com/apis/site/v2/sports/hockey/nhl/scoreboard?dates={date}";

    @Override
    public String getProviderName() {
        return "ESPN";
    }

    @Override
    public boolean supportsSport(String sportName) {
        if (sportName == null) return false;
        String s = sportName.toUpperCase();
        return s.contains("FOOTBALL") || s.contains("BASKETBALL") || s.contains("HOCKEY");
    }

    @Override
    public List<ReferenceFixtureDto> fetchScheduledFixtures(String sportName, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String url;
        if (sportName.toUpperCase().contains("BASKETBALL")) {
            url = ESPN_BASKETBALL_URL;
        } else if (sportName.toUpperCase().contains("HOCKEY")) {
            url = ESPN_HOCKEY_URL;
        } else {
            url = ESPN_SOCCER_URL;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SmartBet-Result-Engine/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    dateStr
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) return Collections.emptyList();

            List<ReferenceFixtureDto> result = new ArrayList<>();
            for (JsonNode event : events) {
                String eventId = event.path("id").asText(null);
                String dateIso = event.path("date").asText(null);
                long startEpochMs = 0;
                if (dateIso != null) {
                    try {
                        startEpochMs = Instant.parse(dateIso).toEpochMilli();
                    } catch (Exception ignored) {}
                }

                JsonNode competitions = event.path("competitions");
                if (!competitions.isArray() || competitions.isEmpty()) continue;
                JsonNode comp = competitions.get(0);
                JsonNode competitors = comp.path("competitors");
                if (!competitors.isArray() || competitors.size() < 2) continue;

                JsonNode teamHome = null;
                JsonNode teamAway = null;
                for (JsonNode c : competitors) {
                    if ("home".equalsIgnoreCase(c.path("homeAway").asText())) {
                        teamHome = c;
                    } else {
                        teamAway = c;
                    }
                }
                if (teamHome == null) teamHome = competitors.get(0);
                if (teamAway == null) teamAway = competitors.get(1);

                String homeName = teamHome.path("team").path("displayName").asText(null);
                String homeShort = teamHome.path("team").path("shortDisplayName").asText(null);
                String homeId = teamHome.path("team").path("id").asText(null);
                String homeLogo = teamHome.path("team").path("logo").asText(null);

                String awayName = teamAway.path("team").path("displayName").asText(null);
                String awayShort = teamAway.path("team").path("shortDisplayName").asText(null);
                String awayId = teamAway.path("team").path("id").asText(null);
                String awayLogo = teamAway.path("team").path("logo").asText(null);

                ReferenceFixtureDto dto = ReferenceFixtureDto.builder()
                        .provider(getProviderName())
                        .externalFixtureId(eventId)
                        .sport(sportName)
                        .leagueName(event.path("season").path("name").asText("ESPN League"))
                        .team1Name(homeName)
                        .team1NameLocal(homeShort)
                        .team1ExternalId(homeId)
                        .team1LogoUrl(homeLogo)
                        .team2Name(awayName)
                        .team2NameLocal(awayShort)
                        .team2ExternalId(awayId)
                        .team2LogoUrl(awayLogo)
                        .startTimeEpochMs(startEpochMs)
                        .status(comp.path("status").path("type").path("name").asText("STATUS_SCHEDULED"))
                        .build();

                result.add(dto);
            }

            log.info("[ESPN Fixtures] Successfully ingested {} fixtures for sport '{}'", result.size(), sportName);
            return result;

        } catch (Exception e) {
            log.warn("[ESPN Fixtures] Error fetching events for sport '{}': {}", sportName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<TeamProfileDto> fetchTeamProfile(String externalTeamId, String sportName) {
        return Optional.empty();
    }
}
