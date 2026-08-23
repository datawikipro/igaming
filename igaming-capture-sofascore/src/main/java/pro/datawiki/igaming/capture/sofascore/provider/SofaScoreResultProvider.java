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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SofaScoreResultProvider implements MatchResultProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SOFASCORE_API_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";

    @Override
    public boolean supports(String sportName) {
        return true; // Fallback for all sports
    }

    @Override
    public Optional<MatchResultUpdate> fetchResult(MatchMetadata match) {
        if (match.startTime() == null) {
            return Optional.empty();
        }

        String sport = mapSportToSofa(match.sportName());
        String dateStr = Instant.ofEpochMilli(match.startTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.debug("[SofaScore Provider] Searching for match ID {}: {} vs {} ({}, {})",
                match.id(), match.team1Name(), match.team2Name(), sport, dateStr);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            headers.set("Accept-Language", "ru,en;q=0.9");
            headers.set("Origin", "https://www.sofascore.com");
            headers.set("Referer", "https://www.sofascore.com/");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SOFASCORE_API_URL,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    sport,
                    dateStr
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) {
                return Optional.empty();
            }

            long matchStartSeconds = match.startTime() / 1000;
            long timeWindowSeconds = 7200; // 2 hours window

            for (JsonNode event : events) {
                long eventStartSeconds = event.path("startTimestamp").asLong();
                if (Math.abs(eventStartSeconds - matchStartSeconds) > timeWindowSeconds) {
                    continue;
                }

                String homeTeam = event.path("homeTeam").path("name").asText();
                String awayTeam = event.path("awayTeam").path("name").asText();

                boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), homeTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), awayTeam);

                boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), awayTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), homeTeam);

                if (directMatch || reversedMatch) {
                    JsonNode status = event.path("status");
                    String statusType = status.path("type").asText();

                    if (!"finished".equalsIgnoreCase(statusType)) {
                        log.debug("[SofaScore Provider] Match ID {} found but status is: {}", match.id(), statusType);
                        continue;
                    }

                    int homeScore = event.path("homeScore").path("current").asInt(-1);
                    int awayScore = event.path("awayScore").path("current").asInt(-1);

                    if (homeScore < 0 || awayScore < 0) {
                        continue;
                    }

                    String score1 = directMatch ? String.valueOf(homeScore) : String.valueOf(awayScore);
                    String score2 = directMatch ? String.valueOf(awayScore) : String.valueOf(homeScore);

                    int s1 = Integer.parseInt(score1);
                    int s2 = Integer.parseInt(score2);
                    String matchResult = s1 > s2 ? "WIN1" : (s1 < s2 ? "WIN2" : "DRAW");

                    log.info("[SofaScore Provider] MATCH FOUND: ID {} -> score {}:{} (Result: {})",
                            match.id(), score1, score2, matchResult);

                    return Optional.of(new MatchResultUpdate(score1, score2, matchResult));
                }
            }

        } catch (Exception e) {
            log.debug("[SofaScore Provider] Exception parsing data for match ID {}: {}", match.id(), e.getMessage());
        }

        return Optional.empty();
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
