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
import pro.datawiki.igaming.capture.sofascore.util.SimilarityUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EspnResultProvider implements MatchResultProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String ESPN_SOCCER_URL = "https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard";
    private static final String ESPN_BASKETBALL_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard";

    @Override
    public boolean supports(String sportName) {
        if (sportName == null) return false;
        String s = sportName.toUpperCase();
        return s.contains("FOOTBALL") || s.contains("BASKETBALL") || s.contains("HOCKEY");
    }

    @Override
    public Optional<MatchResultUpdate> fetchResult(MatchMetadata match) {
        String url = match.sportName().toUpperCase().contains("BASKETBALL") ? ESPN_BASKETBALL_URL : ESPN_SOCCER_URL;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SmartBet-Result-Engine/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) return Optional.empty();

            for (JsonNode event : events) {
                JsonNode competitions = event.path("competitions");
                if (!competitions.isArray() || competitions.isEmpty()) continue;

                JsonNode comp = competitions.get(0);
                JsonNode competitors = comp.path("competitors");
                if (!competitors.isArray() || competitors.size() < 2) continue;

                String teamA = competitors.get(0).path("team").path("displayName").asText();
                String teamB = competitors.get(1).path("team").path("displayName").asText();

                boolean direct = SimilarityUtils.areTeamsSimilar(match.team1Name(), teamA)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), teamB);
                boolean reversed = SimilarityUtils.areTeamsSimilar(match.team1Name(), teamB)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), teamA);

                if (direct || reversed) {
                    boolean completed = comp.path("status").path("type").path("completed").asBoolean(false);
                    if (!completed) continue;

                    String scoreA = competitors.get(0).path("score").asText();
                    String scoreB = competitors.get(1).path("score").asText();

                    String score1 = direct ? scoreA : scoreB;
                    String score2 = direct ? scoreB : scoreA;

                    int s1 = Integer.parseInt(score1);
                    int s2 = Integer.parseInt(score2);
                    String result = s1 > s2 ? "WIN1" : (s1 < s2 ? "WIN2" : "DRAW");

                    log.info("[ESPN Provider] SUCCESS: Resolved match ID {} -> {}:{} ({})",
                            match.id(), score1, score2, result);

                    return Optional.of(new MatchResultUpdate(score1, score2, result));
                }
            }

        } catch (Exception e) {
            log.debug("[ESPN Provider] Scoreboard lookup error: {}", e.getMessage());
        }

        return Optional.empty();
    }
}
