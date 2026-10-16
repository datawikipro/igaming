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
import pro.datawiki.igaming.dto.MatchEnrichmentDto;
import pro.datawiki.igaming.dto.MatchH2hDto;
import pro.datawiki.igaming.dto.MatchLineupsDto;
import pro.datawiki.igaming.dto.MatchStorylineDto;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.tournament.StandardTournamentStage;
import pro.datawiki.igaming.dto.tournament.TournamentStageNormalizer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extracts rich match enrichment (Lineups, H2H, Streaks, Storylines)
 * from SofaScore event APIs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SofaScoreEnrichmentProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService rawEventService;

    private static final String EVENT_URL = "https://api.sofascore.com/api/v1/event/{eventId}";
    private static final String LINEUPS_URL = "https://api.sofascore.com/api/v1/event/{eventId}/lineups";
    private static final String H2H_URL = "https://api.sofascore.com/api/v1/event/{eventId}/h2h";
    private static final String STREAKS_URL = "https://api.sofascore.com/api/v1/event/{eventId}/streaks";
    private static final String SCHEDULE_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";
    private static final String SEARCH_URL = "https://api.sofascore.com/api/v1/search/all?q={query}";

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "*/*");
        headers.set("Accept-Language", "ru,en;q=0.9");
        headers.set("Origin", "https://www.sofascore.com");
        headers.set("Referer", "https://www.sofascore.com/");
        return headers;
    }

    /**
     * Fetch complete enrichment for a given SofaScore event ID,
     * persisting the raw event payload (Event, Lineups, H2H, Streaks) to DB and Kafka.
     */
    public Optional<MatchEnrichmentDto> fetchEnrichment(long eventId) {
        log.info("[SofaScore Enrichment] Extracting lineups, H2H and storylines for event ID {}", eventId);

        String rawEventJson = null;
        String rawLineupsJson = null;
        String rawH2hJson = null;
        String rawStreaksJson = null;

        String sport = null;
        String tournamentName = null;
        String season = null;
        Long matchDate = null;
        String status = null;
        String homeTeam = null;
        String awayTeam = null;
        Integer homeScore = null;
        Integer awayScore = null;

        MatchH2hDto h2h = null;
        MatchLineupsDto lineups = null;
        MatchStorylineDto storyline = null;

        // 1. Fetch Event Details
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> eventResp = restTemplate.exchange(
                    EVENT_URL, HttpMethod.GET, entity, String.class, eventId);
            if (eventResp.getStatusCode().is2xxSuccessful() && eventResp.getBody() != null) {
                rawEventJson = eventResp.getBody();
                JsonNode eventRoot = objectMapper.readTree(rawEventJson).path("event");
                sport = eventRoot.path("sport").path("slug").asText(null);
                tournamentName = eventRoot.path("tournament").path("name").asText(null);
                season = eventRoot.path("season").path("name").asText(null);
                long startSec = eventRoot.path("startTimestamp").asLong(0);
                if (startSec > 0) matchDate = startSec * 1000L;
                status = eventRoot.path("status").path("type").asText(null);
                homeTeam = eventRoot.path("homeTeam").path("name").asText(null);
                awayTeam = eventRoot.path("awayTeam").path("name").asText(null);
                int hs = eventRoot.path("homeScore").path("current").asInt(-1);
                int as = eventRoot.path("awayScore").path("current").asInt(-1);
                if (hs >= 0) homeScore = hs;
                if (as >= 0) awayScore = as;
            }
        } catch (Exception e) {
            log.debug("[SofaScore Enrichment] Event fetch failed for {}: {}", eventId, e.getMessage());
        }

        // 2. Fetch Lineups
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> lineupsResp = restTemplate.exchange(
                    LINEUPS_URL, HttpMethod.GET, entity, String.class, eventId);
            if (lineupsResp.getStatusCode().is2xxSuccessful() && lineupsResp.getBody() != null) {
                rawLineupsJson = lineupsResp.getBody();
                lineups = parseLineupsJson(rawLineupsJson);
            }
        } catch (Exception e) {
            log.debug("[SofaScore Lineups] No lineups available for event {}: {}", eventId, e.getMessage());
        }

        // 3. Fetch H2H
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> h2hResp = restTemplate.exchange(
                    H2H_URL, HttpMethod.GET, entity, String.class, eventId);
            if (h2hResp.getStatusCode().is2xxSuccessful() && h2hResp.getBody() != null) {
                rawH2hJson = h2hResp.getBody();
                h2h = parseH2hJson(rawH2hJson);
            }
        } catch (Exception e) {
            log.debug("[SofaScore H2H] Failed to fetch H2H for event {}: {}", eventId, e.getMessage());
        }

        // 4. Fetch Streaks & build Storyline
        try {
            List<String> streaks = new ArrayList<>();
            try {
                HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
                ResponseEntity<String> streaksResp = restTemplate.exchange(
                        STREAKS_URL, HttpMethod.GET, entity, String.class, eventId);
                if (streaksResp.getStatusCode().is2xxSuccessful() && streaksResp.getBody() != null) {
                    rawStreaksJson = streaksResp.getBody();
                    JsonNode streaksNode = objectMapper.readTree(rawStreaksJson).path("general");
                    if (streaksNode.isArray()) {
                        for (JsonNode s : streaksNode) {
                            String name = s.path("name").asText();
                            if (name != null && !name.isBlank()) {
                                streaks.add(name);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            String roundName = null;
            if (rawEventJson != null) {
                JsonNode eventRoot = objectMapper.readTree(rawEventJson).path("event");
                roundName = eventRoot.path("roundInfo").path("name").asText(null);
            }
            StandardTournamentStage stage = TournamentStageNormalizer.normalize(roundName);

            List<String> keyFacts = new ArrayList<>();
            String h2hSummary = null;
            if (h2h != null && h2h.getTotalMatches() > 0) {
                h2hSummary = String.format("Команды провели %d очных встреч: %d побед у первой команды, %d у второй, %d ничьих.",
                        h2h.getTotalMatches(), h2h.getTeam1Wins(), h2h.getTeam2Wins(), h2h.getDraws());
                keyFacts.add(h2hSummary);
                if (h2h.getBttsPercentage() != null && h2h.getBttsPercentage() >= 60.0) {
                    keyFacts.add(String.format("В %.0f%% последних очных встреч забивали обе команды.", h2h.getBttsPercentage()));
                }
                if (h2h.getOver25Percentage() != null && h2h.getOver25Percentage() >= 60.0) {
                    keyFacts.add(String.format("В %.0f%% матчей пробивался тотал больше 2.5 голов.", h2h.getOver25Percentage()));
                }
            }
            if (stage.isFinal()) {
                keyFacts.add("🏆 Финальный решающий матч за титул турнира!");
            }

            storyline = MatchStorylineDto.builder()
                    .stage(stage)
                    .tournamentStage(roundName != null ? roundName : stage.getRussianName())
                    .headToHeadSummary(h2hSummary)
                    .streaks(streaks)
                    .keyFacts(keyFacts)
                    .build();
        } catch (Exception e) {
            log.debug("[SofaScore Storyline] Failed for event {}: {}", eventId, e.getMessage());
        }

        // 5. Dual Ingestion: Record 100% raw payload into PostgreSQL & emit to Kafka
        if (rawEventJson != null || rawLineupsJson != null || rawH2hJson != null) {
            pro.datawiki.igaming.dto.raw.SofaRawEventDto rawDto = pro.datawiki.igaming.dto.raw.SofaRawEventDto.builder()
                    .eventId(eventId)
                    .sport(sport)
                    .tournamentName(tournamentName)
                    .season(season)
                    .matchDate(matchDate)
                    .status(status)
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .homeScore(homeScore)
                    .awayScore(awayScore)
                    .eventJson(rawEventJson)
                    .lineupsJson(rawLineupsJson)
                    .h2hJson(rawH2hJson)
                    .streaksJson(rawStreaksJson)
                    .capturedAt(System.currentTimeMillis())
                    .build();
            rawEventService.recordRawEvent(rawDto);
        }

        if (lineups == null && h2h == null && storyline == null) {
            return Optional.empty();
        }

        return Optional.of(MatchEnrichmentDto.builder()
                .lineups(lineups)
                .h2h(h2h)
                .storyline(storyline)
                .updatedAt(System.currentTimeMillis())
                .build());
    }

    public Optional<MatchLineupsDto> fetchLineups(long eventId) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    LINEUPS_URL, HttpMethod.GET, entity, String.class, eventId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(parseLineupsJson(response.getBody()));
        } catch (Exception e) {
            log.debug("[SofaScore Lineups] No lineups available for event {}: {}", eventId, e.getMessage());
            return Optional.empty();
        }
    }

    public MatchLineupsDto parseLineupsJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            boolean confirmed = root.path("confirmed").asBoolean(false);

            JsonNode homeNode = root.path("home");
            JsonNode awayNode = root.path("away");

            String formation1 = homeNode.path("formation").asText(null);
            String formation2 = awayNode.path("formation").asText(null);

            List<MatchLineupsDto.PlayerLineupDto> starters1 = parsePlayers(homeNode.path("players"));
            List<MatchLineupsDto.PlayerLineupDto> starters2 = parsePlayers(awayNode.path("players"));

            List<MatchLineupsDto.PlayerLineupDto> bench1 = parsePlayers(homeNode.path("bench"));
            List<MatchLineupsDto.PlayerLineupDto> bench2 = parsePlayers(awayNode.path("bench"));

            List<MatchLineupsDto.MissingPlayerDto> missing1 = parseMissing(homeNode.path("missingPlayers"));
            List<MatchLineupsDto.MissingPlayerDto> missing2 = parseMissing(awayNode.path("missingPlayers"));

            return MatchLineupsDto.builder()
                    .confirmed(confirmed)
                    .formationTeam1(formation1)
                    .formationTeam2(formation2)
                    .startersTeam1(starters1)
                    .startersTeam2(starters2)
                    .benchTeam1(bench1)
                    .benchTeam2(bench2)
                    .missingTeam1(missing1)
                    .missingTeam2(missing2)
                    .build();
        } catch (Exception e) {
            log.debug("[SofaScore Lineups] Error parsing lineups JSON: {}", e.getMessage());
            return null;
        }
    }

    public Optional<MatchH2hDto> fetchH2h(long eventId) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    H2H_URL, HttpMethod.GET, entity, String.class, eventId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(parseH2hJson(response.getBody()));
        } catch (Exception e) {
            log.debug("[SofaScore H2H] Failed to fetch H2H for event {}: {}", eventId, e.getMessage());
            return Optional.empty();
        }
    }

    public MatchH2hDto parseH2hJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode team1WinsNode = root.path("team1Wins");
            JsonNode team2WinsNode = root.path("team2Wins");
            JsonNode drawsNode = root.path("draws");

            int team1Wins = team1WinsNode.asInt(0);
            int team2Wins = team2WinsNode.asInt(0);
            int draws = drawsNode.asInt(0);
            int total = team1Wins + team2Wins + draws;

            List<MatchH2hDto.H2hEncounterDto> encounters = new ArrayList<>();
            JsonNode events = root.path("events");
            int totalGoals = 0;
            int bttsCount = 0;
            int over25Count = 0;

            if (events.isArray()) {
                for (JsonNode ev : events) {
                    long dateEpoch = ev.path("startTimestamp").asLong(0);
                    String tournament = ev.path("tournament").path("name").asText("Турнир");
                    String homeTeam = ev.path("homeTeam").path("name").asText("");
                    String awayTeam = ev.path("awayTeam").path("name").asText("");
                    int homeScore = ev.path("homeScore").path("current").asInt(0);
                    int awayScore = ev.path("awayScore").path("current").asInt(0);

                    String winner = "DRAW";
                    if (homeScore > awayScore) winner = "HOME";
                    else if (awayScore > homeScore) winner = "AWAY";

                    encounters.add(MatchH2hDto.H2hEncounterDto.builder()
                            .dateEpochSeconds(dateEpoch)
                            .tournamentName(tournament)
                            .homeTeam(homeTeam)
                            .awayTeam(awayTeam)
                            .homeScore(homeScore)
                            .awayScore(awayScore)
                            .winner(winner)
                            .build());

                    int matchGoals = homeScore + awayScore;
                    totalGoals += matchGoals;
                    if (homeScore > 0 && awayScore > 0) bttsCount++;
                    if (matchGoals > 2) over25Count++;
                }
            }

            int count = encounters.isEmpty() ? 1 : encounters.size();
            Double avgGoals = (double) totalGoals / count;
            Double bttsPct = ((double) bttsCount / count) * 100.0;
            Double over25Pct = ((double) over25Count / count) * 100.0;

            return MatchH2hDto.builder()
                    .totalMatches(total > 0 ? total : encounters.size())
                    .team1Wins(team1Wins)
                    .team2Wins(team2Wins)
                    .draws(draws)
                    .avgGoalsPerMatch(avgGoals)
                    .bttsPercentage(bttsPct)
                    .over25Percentage(over25Pct)
                    .recentEncounters(encounters)
                    .build();
        } catch (Exception e) {
            log.debug("[SofaScore H2H] Error parsing H2H JSON: {}", e.getMessage());
            return null;
        }
    }

    public Optional<MatchStorylineDto> fetchStoryline(long eventId, MatchH2hDto h2h) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    EVENT_URL, HttpMethod.GET, entity, String.class, eventId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody()).path("event");
            String roundName = root.path("roundInfo").path("name").asText(null);
            StandardTournamentStage stage = TournamentStageNormalizer.normalize(roundName);

            // Fetch streaks if available
            List<String> streaks = new ArrayList<>();
            try {
                ResponseEntity<String> streaksResp = restTemplate.exchange(
                        STREAKS_URL, HttpMethod.GET, entity, String.class, eventId);
                if (streaksResp.getStatusCode().is2xxSuccessful() && streaksResp.getBody() != null) {
                    JsonNode streaksNode = objectMapper.readTree(streaksResp.getBody()).path("general");
                    if (streaksNode.isArray()) {
                        for (JsonNode s : streaksNode) {
                            String name = s.path("name").asText();
                            if (name != null && !name.isBlank()) {
                                streaks.add(name);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            // Build narrative context
            List<String> keyFacts = new ArrayList<>();
            String h2hSummary = null;

            if (h2h != null && h2h.getTotalMatches() > 0) {
                h2hSummary = String.format("Команды провели %d очных встреч: %d побед у первой команды, %d у второй, %d ничьих.",
                        h2h.getTotalMatches(), h2h.getTeam1Wins(), h2h.getTeam2Wins(), h2h.getDraws());
                keyFacts.add(h2hSummary);

                if (h2h.getBttsPercentage() != null && h2h.getBttsPercentage() >= 60.0) {
                    keyFacts.add(String.format("В %.0f%% последних очных встреч забивали обе команды.", h2h.getBttsPercentage()));
                }
                if (h2h.getOver25Percentage() != null && h2h.getOver25Percentage() >= 60.0) {
                    keyFacts.add(String.format("В %.0f%% матчей пробивался тотал больше 2.5 голов.", h2h.getOver25Percentage()));
                }
            }

            if (stage.isFinal()) {
                keyFacts.add("🏆 Финальный решающий матч за титул турнира!");
            }

            return Optional.of(MatchStorylineDto.builder()
                    .stage(stage)
                    .tournamentStage(roundName != null ? roundName : stage.getRussianName())
                    .headToHeadSummary(h2hSummary)
                    .streaks(streaks)
                    .keyFacts(keyFacts)
                    .build());
        } catch (Exception e) {
            log.debug("[SofaScore Storyline] Failed for event {}: {}", eventId, e.getMessage());
            return Optional.empty();
        }
    }

    private List<MatchLineupsDto.PlayerLineupDto> parsePlayers(JsonNode playersNode) {
        List<MatchLineupsDto.PlayerLineupDto> result = new ArrayList<>();
        if (!playersNode.isArray()) return result;

        for (JsonNode p : playersNode) {
            JsonNode player = p.path("player");
            result.add(MatchLineupsDto.PlayerLineupDto.builder()
                    .name(player.path("name").asText("Игрок"))
                    .shirtNumber(p.path("jerseyNumber").asInt(0))
                    .position(p.path("position").asText(""))
                    .rating(p.path("avgRating").asDouble(0.0))
                    .captain(p.path("captain").asBoolean(false))
                    .build());
        }
        return result;
    }

    private List<MatchLineupsDto.MissingPlayerDto> parseMissing(JsonNode missingNode) {
        List<MatchLineupsDto.MissingPlayerDto> result = new ArrayList<>();
        if (!missingNode.isArray()) return result;

        for (JsonNode p : missingNode) {
            JsonNode player = p.path("player");
            result.add(MatchLineupsDto.MissingPlayerDto.builder()
                    .name(player.path("name").asText("Игрок"))
                    .reason(p.path("reason").asText("Недоступен"))
                    .type(p.path("type").asText("INJURY"))
                    .build());
        }
        return result;
    }

    public Optional<MatchEnrichmentDto> fetchEnrichment(MatchResultProvider.MatchMetadata match) {
        if (match == null) return Optional.empty();

        Optional<Long> sofaEventIdOpt = resolveSofaScoreEventId(match);
        if (sofaEventIdOpt.isEmpty()) {
            log.debug("[SofaScore Enrichment] Could not resolve SofaScore event ID for match ID {}: {} vs {}",
                    match.id(), match.team1Name(), match.team2Name());
            return Optional.empty();
        }

        long sofaEventId = sofaEventIdOpt.get();
        log.info("[SofaScore Enrichment] Resolved SofaScore event ID {} for match ID {}: {} vs {}",
                sofaEventId, match.id(), match.team1Name(), match.team2Name());

        Optional<MatchEnrichmentDto> enrichmentOpt = fetchEnrichment(sofaEventId);
        enrichmentOpt.ifPresent(dto -> dto.setMatchId(match.id()));
        return enrichmentOpt;
    }

    public Optional<Long> resolveSofaScoreEventId(MatchResultProvider.MatchMetadata match) {
        if (match == null || match.team1Name() == null || match.team2Name() == null || match.startTime() == null) {
            return Optional.empty();
        }

        String sport = mapSportToSofa(match.sportName());
        LocalDate matchDate = Instant.ofEpochMilli(match.startTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        long matchStartSeconds = match.startTime() / 1000;

        List<LocalDate> datesToCheck = new ArrayList<>();
        datesToCheck.add(matchDate);
        long hourOfDay = Instant.ofEpochMilli(match.startTime()).atZone(ZoneOffset.UTC).getHour();
        if (hourOfDay <= 3) {
            datesToCheck.add(matchDate.minusDays(1));
        } else if (hourOfDay >= 21) {
            datesToCheck.add(matchDate.plusDays(1));
        }

        for (LocalDate date : datesToCheck) {
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Optional<Long> resolved = findInScheduledEvents(sport, dateStr, match, matchStartSeconds);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        return searchByTeamName(match, matchStartSeconds);
    }

    private Optional<Long> findInScheduledEvents(String sport, String dateStr, MatchResultProvider.MatchMetadata match, long matchStartSeconds) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    SCHEDULE_URL, HttpMethod.GET, entity, String.class, sport, dateStr);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) {
                return Optional.empty();
            }

            for (JsonNode event : events) {
                long eventStart = event.path("startTimestamp").asLong(0);
                if (eventStart == 0 || Math.abs(eventStart - matchStartSeconds) > 10800) {
                    continue;
                }

                String homeTeam = event.path("homeTeam").path("name").asText("");
                String awayTeam = event.path("awayTeam").path("name").asText("");

                boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), homeTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), awayTeam);
                boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), awayTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), homeTeam);

                if (directMatch || reversedMatch) {
                    long id = event.path("id").asLong(0);
                    if (id > 0) {
                        return Optional.of(id);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[SofaScore Resolver] Failed scheduled events lookup for {} on {}: {}", sport, dateStr, e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<Long> searchByTeamName(MatchResultProvider.MatchMetadata match, long matchStartSeconds) {
        try {
            String query = match.team1Name().trim();
            if (query.length() < 3) return Optional.empty();

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    SEARCH_URL, HttpMethod.GET, entity, String.class, query);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray()) {
                return Optional.empty();
            }

            for (JsonNode event : events) {
                long eventStart = event.path("startTimestamp").asLong(0);
                if (eventStart != 0 && Math.abs(eventStart - matchStartSeconds) > 43200) {
                    continue;
                }

                String homeTeam = event.path("homeTeam").path("name").asText("");
                String awayTeam = event.path("awayTeam").path("name").asText("");

                boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), homeTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), awayTeam);
                boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), awayTeam)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), homeTeam);

                if (directMatch || reversedMatch) {
                    long id = event.path("id").asLong(0);
                    if (id > 0) {
                        return Optional.of(id);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[SofaScore Resolver] Search fallback failed for '{}': {}", match.team1Name(), e.getMessage());
        }
        return Optional.empty();
    }

    private String mapSportToSofa(String aggregatorSport) {
        return SportType.fromAny(aggregatorSport).getSlug();
    }
}
