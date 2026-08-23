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
public class OpenDotaResultProvider implements MatchResultProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OPENDOTA_PRO_MATCHES_URL = "https://api.opendota.com/api/proMatches";

    @Override
    public boolean supports(String sportName) {
        if (sportName == null) return false;
        String lower = sportName.toLowerCase();
        return lower.contains("dota") || lower.contains("esports") || lower.contains("киберспорт");
    }

    @Override
    public Optional<MatchResultUpdate> fetchResult(MatchMetadata match) {
        log.info("[OpenDota Provider] Polling match result for: {} vs {} (Sport: {})",
                match.team1Name(), match.team2Name(), match.sportName());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SmartBet-Result-Engine/1.0");
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENDOTA_PRO_MATCHES_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[OpenDota Provider] Non-2xx response from OpenDota API: {}", response.getStatusCode());
                return Optional.empty();
            }

            JsonNode matches = objectMapper.readTree(response.getBody());
            if (!matches.isArray()) {
                return Optional.empty();
            }

            int team1Wins = 0;
            int team2Wins = 0;
            boolean matchFound = false;

            for (JsonNode m : matches) {
                String radiant = m.path("radiant_name").asText("");
                String dire = m.path("dire_name").asText("");
                boolean radiantWin = m.path("radiant_win").asBoolean();

                boolean direct = SimilarityUtils.areTeamsSimilar(match.team1Name(), radiant)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), dire);

                boolean reversed = SimilarityUtils.areTeamsSimilar(match.team1Name(), dire)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), radiant);

                if (direct) {
                    matchFound = true;
                    if (radiantWin) team1Wins++; else team2Wins++;
                } else if (reversed) {
                    matchFound = true;
                    if (radiantWin) team2Wins++; else team1Wins++;
                }
            }

            if (matchFound && (team1Wins > 0 || team2Wins > 0)) {
                String score1 = String.valueOf(team1Wins);
                String score2 = String.valueOf(team2Wins);
                String result = team1Wins > team2Wins ? "WIN1" : (team2Wins > team1Wins ? "WIN2" : "DRAW");

                log.info("[OpenDota Provider] SUCCESS: Resolved Dota 2 result for ID {}: {}:{} (Result: {})",
                        match.id(), score1, score2, result);

                return Optional.of(new MatchResultUpdate(score1, score2, result));
            }

        } catch (Exception e) {
            log.error("[OpenDota Provider] Failed to fetch pro matches: {}", e.getMessage());
        }

        return Optional.empty();
    }
}
