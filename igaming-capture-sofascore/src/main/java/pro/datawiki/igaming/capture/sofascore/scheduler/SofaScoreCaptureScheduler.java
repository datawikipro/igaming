package pro.datawiki.igaming.capture.sofascore.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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
public class SofaScoreCaptureScheduler {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.capture.enabled:true}")
    private boolean enabled;

    private static final String SOFASCORE_API_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";

    public record MatchMetadata(
            Long id,
            String sportName,
            String team1Name,
            String team2Name,
            Long startTime
    ) {}

    public record MatchResultUpdate(
            String score1,
            String score2,
            String matchResult
    ) {}

    @Scheduled(fixedDelayString = "${app.capture.fixed-delay:300000}", initialDelay = 15000)
    public void runCapture() {
        if (!enabled) {
            log.trace("SofaScore result capture is disabled.");
            return;
        }

        log.info("[SofaScore Capture] Starting scheduled polling of aggregator...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing-results";
            ResponseEntity<MatchMetadata[]> response = restTemplate.getForEntity(pendingUrl, MatchMetadata[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[SofaScore Capture] Aggregator returned non-2xx status: {}", response.getStatusCode());
                return;
            }

            MatchMetadata[] matches = response.getBody();
            if (matches.length == 0) {
                log.info("[SofaScore Capture] No matches found needing results.");
                return;
            }

            log.info("[SofaScore Capture] Found {} matches needing results.", matches.length);
            int updatedCount = 0;

            for (MatchMetadata match : matches) {
                try {
                    Optional<MatchResultUpdate> captured = captureResult(match);
                    if (captured.isPresent()) {
                        sendResultToAggregator(match.id(), captured.get());
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.error("[SofaScore Capture] Failed to process match ID {}: {}", match.id(), e.getMessage());
                }
            }

            log.info("[SofaScore Capture] Done. Updated {}/{} matches.", updatedCount, matches.length);
        } catch (Exception e) {
            log.error("[SofaScore Capture] General error in capture scheduling loop: {}", e.getMessage(), e);
        }
    }

    private Optional<MatchResultUpdate> captureResult(MatchMetadata match) {
        if (match.startTime() == null) {
            return Optional.empty();
        }

        String sport = mapSportToSofa(match.sportName());
        String dateStr = Instant.ofEpochMilli(match.startTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.debug("[SofaScore Capture] Searching for match ID {}: {} vs {} on SofaScore ({}, {})",
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
            long timeWindowSeconds = 5400; // 1.5 hours window

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
                        log.debug("[SofaScore Capture] Match ID {} found but not finished. Status: {}", match.id(), statusType);
                        continue;
                    }

                    int homeScore = event.path("homeScore").path("current").asInt(-1);
                    int awayScore = event.path("awayScore").path("current").asInt(-1);

                    if (homeScore < 0 || awayScore < 0) {
                        continue;
                    }

                    String score1 = directMatch ? String.valueOf(homeScore) : String.valueOf(awayScore);
                    String score2 = directMatch ? String.valueOf(awayScore) : String.valueOf(homeScore);

                    String matchResult = determineMatchResult(score1, score2);
                    if (matchResult == null) {
                        continue;
                    }

                    log.info("[SofaScore Capture] MATCH FOUND: Match ID {} -> score {}:{} (Result: {})",
                            match.id(), score1, score2, matchResult);

                    return Optional.of(new MatchResultUpdate(score1, score2, matchResult));
                }
            }

        } catch (Exception e) {
            log.error("[SofaScore Capture] Exception parsing SofaScore data for match ID {}: {}", match.id(), e.getMessage());
        }

        return Optional.empty();
    }

    private void sendResultToAggregator(Long matchId, MatchResultUpdate update) {
        String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/result";
        log.info("[SofaScore Capture] Sending score update for match ID {} to {}", matchId, updateUrl);
        try {
            restTemplate.postForLocation(updateUrl, update);
        } catch (Exception e) {
            log.error("[SofaScore Capture] Failed to send update for match ID {}: {}", matchId, e.getMessage());
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
            default -> aggregatorSport.toLowerCase();
        };
    }

    private String determineMatchResult(String score1Str, String score2Str) {
        try {
            int s1 = Integer.parseInt(score1Str.trim());
            int s2 = Integer.parseInt(score2Str.trim());
            if (s1 > s2) return "WIN1";
            if (s1 < s2) return "WIN2";
            return "DRAW";
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
