package pro.datawiki.igaming.capture.sofascore.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SofaScore Crawler Service:
 * Traverses event schedules and live feeds across sports,
 * extracts match metadata and enqueues tasks into Redis.
 * Decoupled completely from detailed lineup and statistic extraction.
 */
@Service
@Slf4j
public class SofaCrawlerService {

    public static final String LIVE_EVENTS_URL = "https://api.sofascore.com/api/v1/sport/{sport}/events/live";
    public static final String CATEGORIES_URL = "https://api.sofascore.com/api/v1/sport/{sport}/{date}/10800/categories";
    public static final String CATEGORY_SCHEDULED_EVENTS_URL = "https://api.sofascore.com/api/v1/category/{categoryId}/scheduled-events/{date}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SofaQueueService queueService;

    @Value("${sofascore.crawler.sports:football,basketball,ice-hockey,tennis,table-tennis,volleyball,handball,baseball,american-football,rugby,aussie-rules,cricket,darts,snooker,badminton,futsal,waterpolo,floorball,mma,motorsport,cycling,esports}")
    private String[] sports;

    public SofaCrawlerService(
            @Qualifier("sofaScoreRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            SofaQueueService queueService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.queueService = queueService;
    }

    /**
     * Discovers all currently live matches across sports and pushes to Redis P0 queue.
     */
    public int crawlLiveEvents() {
        int totalQueued = 0;
        HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (String sport : sports) {
            String sportSlug = sport.trim().toLowerCase();
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        LIVE_EVENTS_URL,
                        HttpMethod.GET,
                        entity,
                        String.class,
                        sportSlug
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<SofaTaskDto> tasks = extractTasksFromJson(response.getBody(), sportSlug, SofaTaskDto.TaskType.LIVE, 0);
                    int queued = queueService.pushBatch(tasks);
                    totalQueued += queued;
                    if (queued > 0) {
                        log.info("[SofaCrawler] Discovered {} live matches for sport '{}'", queued, sportSlug);
                    }
                }
            } catch (Exception e) {
                log.debug("[SofaCrawler] Live check for {} returned: {}", sportSlug, e.getMessage());
            }
        }
        return totalQueued;
    }

    /**
     * Discovers scheduled events for given date and pushes to Redis P1 queue.
     */
    public int crawlScheduledEvents(LocalDate date) {
        return crawlDateEvents(date, SofaTaskDto.TaskType.PREMATCH, 1);
    }

    /**
     * Discovers historical events for a past date and pushes to Redis P2 queue.
     */
    public int crawlHistoricalDate(LocalDate date) {
        return crawlDateEvents(date, SofaTaskDto.TaskType.HISTORY, 2);
    }

    private int crawlDateEvents(LocalDate date, SofaTaskDto.TaskType taskType, int priority) {
        if (date == null) return 0;
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        int totalQueued = 0;
        HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (String sport : sports) {
            String sportSlug = sport.trim().toLowerCase();
            try {
                ResponseEntity<String> catResp = restTemplate.exchange(
                        CATEGORIES_URL,
                        HttpMethod.GET,
                        entity,
                        String.class,
                        sportSlug,
                        dateStr
                );

                if (catResp.getStatusCode().is2xxSuccessful() && catResp.getBody() != null) {
                    JsonNode catRoot = objectMapper.readTree(catResp.getBody());
                    JsonNode categories = catRoot.path("categories");
                    if (categories.isArray()) {
                        int sportTotal = 0;
                        for (JsonNode catItem : categories) {
                            int totalEvents = catItem.path("totalEvents").asInt(0);
                            if (totalEvents <= 0) continue;
                            long categoryId = catItem.path("category").path("id").asLong(0);
                            if (categoryId <= 0) continue;

                            try {
                                ResponseEntity<String> evResp = restTemplate.exchange(
                                        CATEGORY_SCHEDULED_EVENTS_URL,
                                        HttpMethod.GET,
                                        entity,
                                        String.class,
                                        categoryId,
                                        dateStr
                                );
                                if (evResp.getStatusCode().is2xxSuccessful() && evResp.getBody() != null) {
                                    List<SofaTaskDto> tasks = extractTasksFromJson(evResp.getBody(), sportSlug, taskType, priority);
                                    int queued = queueService.pushBatch(tasks);
                                    sportTotal += queued;
                                    totalQueued += queued;
                                }
                            } catch (Exception ex) {
                                log.debug("[SofaCrawler] Failed events for category {} ({}): {}", categoryId, sportSlug, ex.getMessage());
                            }
                        }
                        log.info("[SofaCrawler] Discovered {} {} events for sport '{}' on {}", sportTotal, taskType, sportSlug, dateStr);
                    }
                }
            } catch (Exception e) {
                log.debug("[SofaCrawler] Categories check for {} on {} returned: {}", sportSlug, dateStr, e.getMessage());
            }
        }
        return totalQueued;
    }

    private List<SofaTaskDto> extractTasksFromJson(String json, String sport, SofaTaskDto.TaskType type, int priority) {
        List<SofaTaskDto> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode events = root.path("events");
            if (events.isArray()) {
                for (JsonNode event : events) {
                    long eventId = event.path("id").asLong(0);
                    if (eventId <= 0) continue;

                    long tournamentId = event.path("tournament").path("id").asLong(0);
                    String tournamentName = event.path("tournament").path("name").asText(null);
                    String categoryName = event.path("tournament").path("category").path("name").asText(null);
                    long homeTeamId = event.path("homeTeam").path("id").asLong(0);
                    String rawHome = event.path("homeTeam").path("name").asText(null);
                    long awayTeamId = event.path("awayTeam").path("id").asLong(0);
                    String rawAway = event.path("awayTeam").path("name").asText(null);

                    String canonicalHome = TeamSignatureNormalizer.toCanonicalDisplayName(rawHome, tournamentName, categoryName);
                    String canonicalAway = TeamSignatureNormalizer.toCanonicalDisplayName(rawAway, tournamentName, categoryName);

                    String homeTeam = (canonicalHome != null && !canonicalHome.isBlank()) ? canonicalHome : rawHome;
                    String awayTeam = (canonicalAway != null && !canonicalAway.isBlank()) ? canonicalAway : rawAway;
                    long startTimestamp = event.path("startTimestamp").asLong(0) * 1000L;

                    String resolvedSport = sport;
                    if ("esports".equalsIgnoreCase(sport)) {
                        resolvedSport = resolveEsportsDiscipline(categoryName, tournamentName);
                    }

                    list.add(SofaTaskDto.builder()
                            .eventId(eventId)
                            .sport(resolvedSport)
                            .type(type)
                            .tournamentId(tournamentId > 0 ? tournamentId : null)
                            .tournamentName(tournamentName)
                            .categoryName(categoryName)
                            .homeTeamId(homeTeamId > 0 ? homeTeamId : null)
                            .homeTeam(homeTeam)
                            .awayTeamId(awayTeamId > 0 ? awayTeamId : null)
                            .awayTeam(awayTeam)
                            .matchDate(startTimestamp > 0 ? startTimestamp : System.currentTimeMillis())
                            .priority(priority)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("[SofaCrawler] Failed to parse events JSON for {}: {}", sport, e.getMessage());
        }
        return list;
    }

    public static String resolveEsportsDiscipline(String categoryName, String tournamentName) {
        String cat = categoryName != null ? categoryName.toLowerCase() : "";
        String tourn = tournamentName != null ? tournamentName.toLowerCase() : "";

        if (cat.contains("counter-strike") || cat.contains("cs:go") || cat.contains("cs2") || tourn.contains("cs2") || tourn.contains("counter-strike")) {
            return "cs2";
        }
        if (cat.contains("dota") || tourn.contains("dota")) {
            return "dota2";
        }
        if (cat.contains("league of legends") || cat.contains("lol") || tourn.contains("league of legends")) {
            return "league-of-legends";
        }
        if (cat.contains("valorant") || tourn.contains("valorant")) {
            return "valorant";
        }
        if (cat.contains("rainbow six") || cat.contains("r6") || tourn.contains("rainbow six")) {
            return "rainbow-six";
        }
        if (cat.contains("starcraft") || tourn.contains("starcraft")) {
            return "starcraft";
        }
        if (cat.contains("mobile legends") || cat.contains("mlbb") || tourn.contains("mobile legends")) {
            return "mobile-legends";
        }
        if (cat.contains("cyber football") || cat.contains("ea sports") || cat.contains("fifa")
                || tourn.contains("ea sports fc") || tourn.contains("esports battle (2x")
                || tourn.contains("киберфутбол") || tourn.contains("h2h лига")) {
            return "cyber-football";
        }
        return "esports";
    }
}
