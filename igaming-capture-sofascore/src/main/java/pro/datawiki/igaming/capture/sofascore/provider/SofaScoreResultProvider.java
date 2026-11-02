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
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.dto.SportType;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;

@Component
@Slf4j
public class SofaScoreResultProvider implements MatchResultProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService rawEventService;

    public SofaScoreResultProvider(
            @Qualifier("sofaScoreRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService rawEventService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rawEventService = rawEventService;
    }

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
            HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
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

                String homeTeam = event.path("homeTeam").path("name").asText("");
                String awayTeam = event.path("awayTeam").path("name").asText("");
                String eventTournamentContext = event.path("tournament").path("name").asText("") + " " +
                        event.path("tournament").path("category").path("name").asText("");

                boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), homeTeam, eventTournamentContext)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), awayTeam, eventTournamentContext);

                boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), awayTeam, eventTournamentContext)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), homeTeam, eventTournamentContext);

                if (directMatch || reversedMatch) {
                    long eventId = event.path("id").asLong(0);
                    JsonNode status = event.path("status");
                    String statusType = status.path("type").asText();

                    if (eventId > 0) {
                        try {
                            pro.datawiki.igaming.dto.sport.Sport spObj = pro.datawiki.igaming.dto.sport.Sports.get(sport);
                            int[] regRaw = spObj.resolveRegulationScoreFromSofa(event.path("homeScore"), event.path("awayScore"), status.path("description").asText(null));
                            int hs = regRaw != null ? regRaw[0] : -1;
                            int as = regRaw != null ? regRaw[1] : -1;
                            pro.datawiki.igaming.dto.raw.SofaRawEventDto rawDto = pro.datawiki.igaming.dto.raw.SofaRawEventDto.builder()
                                    .eventId(eventId)
                                    .sport(event.path("sport").path("slug").asText(sport))
                                    .tournamentName(event.path("tournament").path("name").asText(null))
                                    .season(event.path("season").path("name").asText(null))
                                    .matchDate(event.path("startTimestamp").asLong(0) * 1000L)
                                    .status(statusType)
                                    .homeTeam(homeTeam)
                                    .awayTeam(awayTeam)
                                    .homeScore(hs >= 0 ? hs : null)
                                    .awayScore(as >= 0 ? as : null)
                                    .eventJson(event.toString())
                                    .capturedAt(System.currentTimeMillis())
                                    .build();
                            rawEventService.recordRawEvent(rawDto);
                        } catch (Exception e) {
                            log.debug("[SofaScore Provider] Could not record raw event {}: {}", eventId, e.getMessage());
                        }
                    }

                    if (!"finished".equalsIgnoreCase(statusType)) {
                        log.debug("[SofaScore Provider] Match ID {} found but status is: {}", match.id(), statusType);
                        continue;
                    }

                    pro.datawiki.igaming.dto.sport.Sport sportObj = pro.datawiki.igaming.dto.sport.Sports.get(sport);
                    int[] regScore = sportObj.resolveRegulationScoreFromSofa(event.path("homeScore"), event.path("awayScore"), status.path("description").asText(null));
                    if (regScore == null) {
                        continue;
                    }
                    int homeScore = regScore[0];
                    int awayScore = regScore[1];

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
        return SportType.fromAny(aggregatorSport).getSlug();
    }
}
