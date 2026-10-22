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
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Qualifier;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;

/**
 * Extracts rich match enrichment (Lineups, H2H, Streaks, Storylines)
 * from SofaScore event APIs.
 */
@Component
@Slf4j
public class SofaScoreEnrichmentProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService rawEventService;
    private final pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository rawEventRepository;

    public SofaScoreEnrichmentProvider(
            @Qualifier("sofaScoreRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            pro.datawiki.igaming.capture.sofascore.service.SofaRawEventService rawEventService,
            pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository rawEventRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rawEventService = rawEventService;
        this.rawEventRepository = rawEventRepository;
    }

    private static final String COMPOSITE_EVENT_URL = "https://api.sofascore.com/api/v1/composite/event/{eventId}";
    private static final String EVENT_URL = "https://api.sofascore.com/api/v1/event/{eventId}";
    private static final String LINEUPS_URL = "https://api.sofascore.com/api/v1/event/{eventId}/lineups";
    private static final String H2H_URL = "https://api.sofascore.com/api/v1/event/{eventId}/h2h";
    private static final String STREAKS_URL = "https://api.sofascore.com/api/v1/event/{eventId}/streaks";
    private static final String SCHEDULE_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}";
    private static final String SEARCH_URL = "https://api.sofascore.com/api/v1/search/all?q={query}";

    private HttpHeaders createHeaders() {
        return SofaScoreHeaderUtils.createBrowserHeaders();
    }

    public Optional<MatchEnrichmentDto> fetchEnrichment(long eventId) {
        return fetchEnrichment(eventId, "football");
    }

    /**
     * Fetch complete enrichment for a given SofaScore event ID,
     * persisting the raw event payload (Event, Lineups, H2H, Streaks) to DB and Kafka.
     */
    public Optional<MatchEnrichmentDto> fetchEnrichment(long eventId, String fallbackSport) {
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

        // 0. High-Speed Composite Fetch (event + lineups + h2h + streaks in ~100ms via async bridge)
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> compResp = restTemplate.exchange(
                    COMPOSITE_EVENT_URL, HttpMethod.GET, entity, String.class, eventId);
            if (compResp.getStatusCode().is2xxSuccessful() && compResp.getBody() != null) {
                JsonNode compRoot = objectMapper.readTree(compResp.getBody());
                if (compRoot.has("event") && !compRoot.path("event").isNull()) {
                    rawEventJson = compRoot.path("event").isTextual() ? compRoot.path("event").asText() : compRoot.path("event").toString();
                }
                if (compRoot.has("lineups") && !compRoot.path("lineups").isNull()) {
                    rawLineupsJson = compRoot.path("lineups").isTextual() ? compRoot.path("lineups").asText() : compRoot.path("lineups").toString();
                }
                if (compRoot.has("h2h") && !compRoot.path("h2h").isNull()) {
                    rawH2hJson = compRoot.path("h2h").isTextual() ? compRoot.path("h2h").asText() : compRoot.path("h2h").toString();
                }
                if (compRoot.has("streaks") && !compRoot.path("streaks").isNull()) {
                    rawStreaksJson = compRoot.path("streaks").isTextual() ? compRoot.path("streaks").asText() : compRoot.path("streaks").toString();
                }
            }
        } catch (Exception e) {
            log.debug("[SofaScore Enrichment] Composite fetch not available for {}: {}", eventId, e.getMessage());
        }

        // 1. Fetch Event Details (fallback if composite did not provide event)
        if (rawEventJson != null) {
            try {
                JsonNode eventRoot = objectMapper.readTree(rawEventJson).path("event");
                sport = eventRoot.path("sport").path("slug").asText(null);
                if (sport == null || sport.isBlank()) {
                    sport = eventRoot.path("tournament").path("category").path("sport").path("slug").asText(null);
                }
                if (sport == null || sport.isBlank()) {
                    sport = eventRoot.path("tournament").path("uniqueTournament").path("category").path("sport").path("slug").asText(null);
                }
                if (sport == null || sport.isBlank()) {
                    sport = fallbackSport != null ? fallbackSport : "football";
                }
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
            } catch (Exception e) {
                log.debug("[SofaScore Enrichment] Error parsing event JSON: {}", e.getMessage());
            }
        } else {
            try {
                HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
                ResponseEntity<String> eventResp = restTemplate.exchange(
                        EVENT_URL, HttpMethod.GET, entity, String.class, eventId);
                if (eventResp.getStatusCode().is2xxSuccessful() && eventResp.getBody() != null) {
                    rawEventJson = eventResp.getBody();
                    JsonNode eventRoot = objectMapper.readTree(rawEventJson).path("event");
                    sport = eventRoot.path("sport").path("slug").asText(null);
                    if (sport == null || sport.isBlank()) {
                        sport = eventRoot.path("tournament").path("category").path("sport").path("slug").asText(null);
                    }
                    if (sport == null || sport.isBlank()) {
                        sport = eventRoot.path("tournament").path("uniqueTournament").path("category").path("sport").path("slug").asText(null);
                    }
                    if (sport == null || sport.isBlank()) {
                        sport = fallbackSport != null ? fallbackSport : "football";
                    }
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
        }

        // 2. Lineups
        if (rawLineupsJson != null) {
            lineups = parseLineupsJson(rawLineupsJson);
        } else if (rawEventJson == null) {
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
        }

        // 3. H2H
        if (rawH2hJson != null) {
            h2h = parseH2hJson(rawH2hJson);
        } else if (rawEventJson == null) {
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

            JsonNode eventRoot = null;
            String roundName = null;
            if (rawEventJson != null) {
                eventRoot = objectMapper.readTree(rawEventJson).path("event");
                roundName = eventRoot.path("roundInfo").path("name").asText(null);
            }
            StandardTournamentStage stage = TournamentStageNormalizer.normalize(roundName);

            MatchStorylineDto.VenueDto venue = parseVenue(eventRoot, tournamentName);
            MatchStorylineDto.RefereeDto referee = parseReferee(eventRoot);
            List<String> contextTags = generateContextTags(venue, referee, lineups, stage);

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
            if (venue != null && venue.getVenueContextNotice() != null) {
                keyFacts.add("📍 " + venue.getVenueContextNotice());
            }
            if (referee != null && referee.getAvgYellowCards() != null) {
                String strictText = "STRICT".equals(referee.getStrictness()) ? "строгий арбитр"
                        : ("LENIENT".equals(referee.getStrictness()) ? "лояльный арбитр" : "умеренный арбитр");
                keyFacts.add(String.format(Locale.US, "⚖️ Главный судья: %s (%s, в среднем %.1f ЖК за матч).",
                        referee.getName(), strictText, referee.getAvgYellowCards()));
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
                    .venue(venue)
                    .referee(referee)
                    .contextTags(contextTags)
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

            List<MatchLineupsDto.PlayerLineupDto> allHome = parseAllPlayers(homeNode);
            List<MatchLineupsDto.PlayerLineupDto> starters1 = allHome.stream()
                    .filter(p -> !p.isSubstitute())
                    .collect(Collectors.toList());
            List<MatchLineupsDto.PlayerLineupDto> bench1 = allHome.stream()
                    .filter(MatchLineupsDto.PlayerLineupDto::isSubstitute)
                    .collect(Collectors.toList());

            List<MatchLineupsDto.PlayerLineupDto> allAway = parseAllPlayers(awayNode);
            List<MatchLineupsDto.PlayerLineupDto> starters2 = allAway.stream()
                    .filter(p -> !p.isSubstitute())
                    .collect(Collectors.toList());
            List<MatchLineupsDto.PlayerLineupDto> bench2 = allAway.stream()
                    .filter(MatchLineupsDto.PlayerLineupDto::isSubstitute)
                    .collect(Collectors.toList());

            List<MatchLineupsDto.SubstitutionDto> subs1 = generateSubstitutions(starters1, bench1, 1);
            List<MatchLineupsDto.SubstitutionDto> subs2 = generateSubstitutions(starters2, bench2, 2);

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
                    .substitutionsTeam1(subs1)
                    .substitutionsTeam2(subs2)
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

    private List<MatchLineupsDto.PlayerLineupDto> parseAllPlayers(JsonNode teamNode) {
        List<MatchLineupsDto.PlayerLineupDto> result = new ArrayList<>();
        if (teamNode == null) return result;

        parsePlayersArray(teamNode.path("players"), result, null);

        if (teamNode.has("bench") && teamNode.path("bench").isArray()) {
            parsePlayersArray(teamNode.path("bench"), result, true);
        }

        return result;
    }

    private void parsePlayersArray(JsonNode arrayNode, List<MatchLineupsDto.PlayerLineupDto> target, Boolean forceSubstitute) {
        if (!arrayNode.isArray()) return;

        for (JsonNode p : arrayNode) {
            JsonNode player = p.path("player");
            String name = player.path("name").asText("Игрок");
            int shirtNumber = p.path("jerseyNumber").asInt(p.path("shirtNumber").asInt(player.path("jerseyNumber").asInt(0)));
            String position = p.path("position").asText(player.path("position").asText(""));
            double rating = p.path("statistics").path("rating").asDouble(p.path("avgRating").asDouble(0.0));
            boolean captain = p.path("captain").asBoolean(false);

            boolean isSub = forceSubstitute != null ? forceSubstitute : p.path("substitute").asBoolean(false);

            Integer minutesPlayed = null;
            if (p.path("statistics").has("minutesPlayed")) {
                minutesPlayed = p.path("statistics").path("minutesPlayed").asInt();
            }

            boolean substituted = false;
            Integer minuteIn = null;
            Integer minuteOut = null;

            if (!isSub) {
                // Starter
                if (minutesPlayed != null && minutesPlayed > 0 && minutesPlayed < 90) {
                    substituted = true;
                    minuteOut = minutesPlayed;
                }
            } else {
                // Bench player
                if (minutesPlayed != null && minutesPlayed > 0) {
                    substituted = true;
                    minuteIn = Math.max(1, 90 - minutesPlayed);
                }
            }

            target.add(MatchLineupsDto.PlayerLineupDto.builder()
                    .name(name)
                    .shirtNumber(shirtNumber)
                    .position(position)
                    .rating(rating)
                    .captain(captain)
                    .substitute(isSub)
                    .substituted(substituted)
                    .minuteIn(minuteIn)
                    .minuteOut(minuteOut)
                    .minutesPlayed(minutesPlayed)
                    .build());
        }
    }

    private List<MatchLineupsDto.SubstitutionDto> generateSubstitutions(
            List<MatchLineupsDto.PlayerLineupDto> starters,
            List<MatchLineupsDto.PlayerLineupDto> bench,
            int teamIndex) {
        List<MatchLineupsDto.SubstitutionDto> subs = new ArrayList<>();
        if (starters == null || bench == null) return subs;

        List<MatchLineupsDto.PlayerLineupDto> off = starters.stream()
                .filter(p -> p.isSubstituted() && p.getMinuteOut() != null)
                .sorted(Comparator.comparing(MatchLineupsDto.PlayerLineupDto::getMinuteOut))
                .collect(Collectors.toList());

        List<MatchLineupsDto.PlayerLineupDto> on = bench.stream()
                .filter(p -> p.isSubstituted() && p.getMinuteIn() != null)
                .sorted(Comparator.comparing(MatchLineupsDto.PlayerLineupDto::getMinuteIn))
                .collect(Collectors.toList());

        boolean[] usedOn = new boolean[on.size()];
        for (MatchLineupsDto.PlayerLineupDto outPlayer : off) {
            int bestIdx = -1;
            int minDiff = Integer.MAX_VALUE;
            for (int i = 0; i < on.size(); i++) {
                if (!usedOn[i]) {
                    int diff = Math.abs(outPlayer.getMinuteOut() - on.get(i).getMinuteIn());
                    if (diff < minDiff) {
                        minDiff = diff;
                        bestIdx = i;
                    }
                }
            }

            if (bestIdx != -1 && minDiff <= 12) {
                usedOn[bestIdx] = true;
                MatchLineupsDto.PlayerLineupDto inPlayer = on.get(bestIdx);
                subs.add(MatchLineupsDto.SubstitutionDto.builder()
                        .playerIn(inPlayer.getName())
                        .playerInNumber(inPlayer.getShirtNumber())
                        .playerOut(outPlayer.getName())
                        .playerOutNumber(outPlayer.getShirtNumber())
                        .minute(outPlayer.getMinuteOut())
                        .teamIndex(teamIndex)
                        .build());
            } else {
                subs.add(MatchLineupsDto.SubstitutionDto.builder()
                        .playerOut(outPlayer.getName())
                        .playerOutNumber(outPlayer.getShirtNumber())
                        .minute(outPlayer.getMinuteOut())
                        .teamIndex(teamIndex)
                        .build());
            }
        }

        for (int i = 0; i < on.size(); i++) {
            if (!usedOn[i]) {
                MatchLineupsDto.PlayerLineupDto inPlayer = on.get(i);
                subs.add(MatchLineupsDto.SubstitutionDto.builder()
                        .playerIn(inPlayer.getName())
                        .playerInNumber(inPlayer.getShirtNumber())
                        .minute(inPlayer.getMinuteIn())
                        .teamIndex(teamIndex)
                        .build());
            }
        }

        subs.sort(Comparator.comparing(s -> s.getMinute() != null ? s.getMinute() : 0));
        return subs;
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

    public MatchStorylineDto.RefereeDto parseReferee(JsonNode eventRoot) {
        if (eventRoot == null || !eventRoot.hasNonNull("referee")) {
            return null;
        }
        JsonNode refNode = eventRoot.path("referee");
        String name = refNode.path("name").asText(null);
        if (name == null || name.isBlank()) {
            return null;
        }
        String country = refNode.path("country").path("name").asText(null);
        Integer games = refNode.hasNonNull("games") ? refNode.path("games").asInt() : null;
        Integer yellowCards = refNode.hasNonNull("yellowCards") ? refNode.path("yellowCards").asInt() : null;
        Integer redCards = refNode.hasNonNull("redCards") ? refNode.path("redCards").asInt() : null;

        Double avgYellow = null;
        if (games != null && games > 0 && yellowCards != null) {
            avgYellow = Math.round((yellowCards.doubleValue() / games) * 10.0) / 10.0;
        }

        Double avgRed = null;
        if (games != null && games > 0 && redCards != null) {
            avgRed = Math.round((redCards.doubleValue() / games) * 100.0) / 100.0;
        }

        String strictness = null;
        if (avgYellow != null) {
            if (avgYellow >= 4.5) {
                strictness = "STRICT";
            } else if (avgYellow <= 3.2) {
                strictness = "LENIENT";
            } else {
                strictness = "AVERAGE";
            }
        }

        return MatchStorylineDto.RefereeDto.builder()
                .name(name)
                .country(country)
                .games(games)
                .yellowCards(yellowCards)
                .redCards(redCards)
                .avgYellowCards(avgYellow)
                .avgRedCards(avgRed)
                .strictness(strictness)
                .build();
    }

    public MatchStorylineDto.VenueDto parseVenue(JsonNode eventRoot, String tournamentName) {
        if (eventRoot == null) return null;
        JsonNode venueNode = eventRoot.path("venue");

        String stadiumName = venueNode.path("stadium").path("name").asText(null);
        if (stadiumName == null || stadiumName.isBlank()) {
            stadiumName = venueNode.path("name").asText(null);
        }
        String cityName = venueNode.path("city").path("name").asText(null);
        String countryName = venueNode.path("country").path("name").asText(null);
        Integer capacity = venueNode.path("stadium").hasNonNull("capacity")
                ? venueNode.path("stadium").path("capacity").asInt()
                : (venueNode.hasNonNull("capacity") ? venueNode.path("capacity").asInt() : null);

        boolean neutralGround = venueNode.path("neutralGround").asBoolean(false)
                || eventRoot.path("neutralGround").asBoolean(false);

        String homeCountry = eventRoot.path("homeTeam").path("country").path("name").asText(null);
        if (!neutralGround && homeCountry != null && countryName != null
                && !homeCountry.isBlank() && !countryName.isBlank()
                && !homeCountry.equalsIgnoreCase(countryName)) {
            neutralGround = true;
        }

        String tourName = tournamentName != null ? tournamentName : eventRoot.path("tournament").path("name").asText("");
        String catName = eventRoot.path("tournament").path("category").path("name").asText("");
        boolean isFriendly = tourName.toLowerCase().contains("friendly")
                || tourName.toLowerCase().contains("товарищеск")
                || tourName.toLowerCase().contains("exhibition")
                || catName.toLowerCase().contains("friendly");

        String venueContextNotice = null;
        if (isFriendly && neutralGround) {
            venueContextNotice = "Товарищеский выездной матч на нейтральном поле / за рубежом. Высокий риск экспериментов с составом и непредсказуемой игры.";
        } else if (neutralGround) {
            venueContextNotice = "Матч на нейтральной арене. Фактор домашнего поля отсутствует.";
        } else if (isFriendly) {
            venueContextNotice = "Товарищеский статус матча: возможны широкие ротации и проверка резервистов.";
        }

        if (stadiumName == null && cityName == null && !neutralGround && !isFriendly) {
            return null;
        }

        return MatchStorylineDto.VenueDto.builder()
                .stadiumName(stadiumName)
                .cityName(cityName)
                .countryName(countryName)
                .capacity(capacity)
                .neutralGround(neutralGround)
                .exhibitionOrFriendly(isFriendly)
                .venueContextNotice(venueContextNotice)
                .build();
    }

    public List<String> generateContextTags(MatchStorylineDto.VenueDto venue,
                                           MatchStorylineDto.RefereeDto referee,
                                           MatchLineupsDto lineups,
                                           StandardTournamentStage stage) {
        List<String> tags = new ArrayList<>();

        if (lineups != null) {
            int subs1 = lineups.getSubstitutionsTeam1() != null ? lineups.getSubstitutionsTeam1().size() : 0;
            int subs2 = lineups.getSubstitutionsTeam2() != null ? lineups.getSubstitutionsTeam2().size() : 0;
            int totalSubs = subs1 + subs2;

            if (totalSubs >= 8) {
                tags.add(String.format("🔄 Глубокая ротация: %d замен", totalSubs));
            } else if (totalSubs >= 5) {
                tags.add(String.format("🔄 Активные замены (%d)", totalSubs));
            }

            int missing1 = lineups.getMissingTeam1() != null ? lineups.getMissingTeam1().size() : 0;
            int missing2 = lineups.getMissingTeam2() != null ? lineups.getMissingTeam2().size() : 0;
            int totalMissing = missing1 + missing2;

            if (totalMissing >= 4) {
                tags.add(String.format("🚑 Кадровый дефицит (%d травм/дискв.)", totalMissing));
            } else if (missing1 >= 2 && missing2 >= 2) {
                tags.add(String.format("🚑 Потери с обеих сторон (%d+%d)", missing1, missing2));
            } else if (missing1 >= 2) {
                tags.add(String.format("🚑 Потери у хозяев (%d)", missing1));
            } else if (missing2 >= 2) {
                tags.add(String.format("🚑 Потери у гостей (%d)", missing2));
            }
        }

        if (venue != null) {
            if (Boolean.TRUE.equals(venue.getExhibitionOrFriendly()) && Boolean.TRUE.equals(venue.getNeutralGround())) {
                tags.add("✈️ Выездной спарринг");
                tags.add("⚠️ Риск низкой мотивации");
            } else {
                if (Boolean.TRUE.equals(venue.getNeutralGround())) {
                    tags.add("✈️ Нейтральное поле");
                }
                if (Boolean.TRUE.equals(venue.getExhibitionOrFriendly())) {
                    tags.add("⚠️ Товарищеский матч");
                }
            }
        }

        if (referee != null && referee.getAvgYellowCards() != null) {
            if ("STRICT".equals(referee.getStrictness())) {
                tags.add(String.format(Locale.US, "⚖️ Строгий арбитр (%.1f ЖК)", referee.getAvgYellowCards()));
            } else if ("LENIENT".equals(referee.getStrictness())) {
                tags.add(String.format(Locale.US, "⚖️ Лояльный арбитр (%.1f ЖК)", referee.getAvgYellowCards()));
            }
        }

        if (stage != null) {
            if (stage.isFinal()) {
                tags.add("🏆 Финальный матч");
            } else if (stage.isPlayoff()) {
                tags.add("⚔️ Матч на выбывание");
            }
        }

        return tags;
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

        // Step 0: Check local DB (sofascore_raw_events) first before making remote network calls
        try {
            long windowMs = 12 * 3600 * 1000L;
            List<pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity> localEvents =
                    rawEventRepository.findBySportAndDateRange(sport, match.startTime() - windowMs, match.startTime() + windowMs);
            if (localEvents != null && !localEvents.isEmpty()) {
                for (var ev : localEvents) {
                    String eventTournamentContext = (ev.getTournament() != null ? ev.getTournament() : "") + " " +
                            (ev.getSeason() != null ? ev.getSeason() : "");
                    boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), ev.getHomeTeam(), eventTournamentContext)
                            && SimilarityUtils.areTeamsSimilar(match.team2Name(), ev.getAwayTeam(), eventTournamentContext);
                    boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), ev.getAwayTeam(), eventTournamentContext)
                            && SimilarityUtils.areTeamsSimilar(match.team2Name(), ev.getHomeTeam(), eventTournamentContext);

                    if (directMatch || reversedMatch) {
                        log.info("[SofaScore Resolver] Local DB match found for {} vs {}: event ID {}",
                                match.team1Name(), match.team2Name(), ev.getEventId());
                        return Optional.of(ev.getEventId());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[SofaScore Resolver] Local DB lookup failed for {} vs {}: {}",
                    match.team1Name(), match.team2Name(), e.getMessage());
        }

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
                String eventTournamentContext = event.path("tournament").path("name").asText("") + " " +
                        event.path("tournament").path("category").path("name").asText("");

                boolean directMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), homeTeam, eventTournamentContext)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), awayTeam, eventTournamentContext);
                boolean reversedMatch = SimilarityUtils.areTeamsSimilar(match.team1Name(), awayTeam, eventTournamentContext)
                        && SimilarityUtils.areTeamsSimilar(match.team2Name(), homeTeam, eventTournamentContext);

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
        String team1Raw = match.team1Name() != null ? match.team1Name().trim() : "";
        String team2Raw = match.team2Name() != null ? match.team2Name().trim() : "";

        String core1 = TeamSignatureNormalizer.parse(team1Raw).coreClubName();
        String core2 = TeamSignatureNormalizer.parse(team2Raw).coreClubName();

        String query1 = (!core1.isBlank()) ? core1 : team1Raw;
        Optional<Long> res = executeSearchQuery(query1, match, matchStartSeconds);
        if (res.isPresent()) {
            return res;
        }

        String latinQuery1 = SimilarityUtils.transliterateCyrillic(query1);
        if (!latinQuery1.equalsIgnoreCase(query1)) {
            res = executeSearchQuery(latinQuery1, match, matchStartSeconds);
            if (res.isPresent()) {
                return res;
            }
        }

        String query2 = (!core2.isBlank()) ? core2 : team2Raw;
        res = executeSearchQuery(query2, match, matchStartSeconds);
        if (res.isPresent()) {
            return res;
        }

        String latinQuery2 = SimilarityUtils.transliterateCyrillic(query2);
        if (!latinQuery2.equalsIgnoreCase(query2)) {
            return executeSearchQuery(latinQuery2, match, matchStartSeconds);
        }

        return Optional.empty();
    }

    private Optional<Long> executeSearchQuery(String query, MatchResultProvider.MatchMetadata match, long matchStartSeconds) {
        if (query == null || query.trim().length() < 3) return Optional.empty();
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    SEARCH_URL, HttpMethod.GET, entity, String.class, query.trim());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            List<JsonNode> eventList = new ArrayList<>();
            JsonNode eventsNode = root.path("events");
            if (eventsNode.isArray()) {
                for (JsonNode ev : eventsNode) {
                    eventList.add(ev);
                }
            }
            JsonNode resultsNode = root.path("results");
            if (resultsNode.isArray()) {
                for (JsonNode resItem : resultsNode) {
                    if ("event".equalsIgnoreCase(resItem.path("type").asText())) {
                        JsonNode entityNode = resItem.path("entity");
                        if (!entityNode.isMissingNode() && !entityNode.isNull()) {
                            eventList.add(entityNode);
                        }
                    }
                }
            }

            if (eventList.isEmpty()) {
                return Optional.empty();
            }

            for (JsonNode event : eventList) {
                long eventStart = event.path("startTimestamp").asLong(0);
                if (eventStart != 0 && Math.abs(eventStart - matchStartSeconds) > 43200) {
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
                    long id = event.path("id").asLong(0);
                    if (id > 0) {
                        return Optional.of(id);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[SofaScore Resolver] Search fallback failed for '{}': {}", query, e.getMessage());
        }
        return Optional.empty();
    }

    private String mapSportToSofa(String aggregatorSport) {
        return SportType.fromAny(aggregatorSport).getSlug();
    }
}
