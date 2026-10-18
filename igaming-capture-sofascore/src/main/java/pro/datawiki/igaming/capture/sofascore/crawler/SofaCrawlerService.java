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
    public static final String SCHEDULED_EVENTS_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}";
    public static final String INVERSE_SCHEDULED_EVENTS_URL = "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SofaQueueService queueService;

    @Value("${sofascore.crawler.sports:football,basketball,ice-hockey,tennis,volleyball,table-tennis,esports}")
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
        if (date == null) return 0;
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        int totalQueued = 0;
        HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (String sport : sports) {
            String sportSlug = sport.trim().toLowerCase();
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        SCHEDULED_EVENTS_URL,
                        HttpMethod.GET,
                        entity,
                        String.class,
                        sportSlug,
                        dateStr
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<SofaTaskDto> tasks = extractTasksFromJson(response.getBody(), sportSlug, SofaTaskDto.TaskType.PREMATCH, 1);
                    int queued = queueService.pushBatch(tasks);
                    totalQueued += queued;
                    log.info("[SofaCrawler] Discovered {} prematch events for sport '{}' on {}", queued, sportSlug, dateStr);
                }
            } catch (Exception e) {
                log.debug("[SofaCrawler] Schedule check for {} on {} returned: {}", sportSlug, dateStr, e.getMessage());
            }
        }
        return totalQueued;
    }

    /**
     * Discovers historical events for a past date and pushes to Redis P2 queue.
     */
    public int crawlHistoricalDate(LocalDate date) {
        if (date == null) return 0;
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        int totalQueued = 0;
        HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (String sport : sports) {
            String sportSlug = sport.trim().toLowerCase();
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        INVERSE_SCHEDULED_EVENTS_URL,
                        HttpMethod.GET,
                        entity,
                        String.class,
                        sportSlug,
                        dateStr
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<SofaTaskDto> tasks = extractTasksFromJson(response.getBody(), sportSlug, SofaTaskDto.TaskType.HISTORY, 2);
                    int queued = queueService.pushBatch(tasks);
                    totalQueued += queued;
                }
            } catch (Exception e) {
                log.debug("[SofaCrawler] Historical check for {} on {} returned: {}", sportSlug, dateStr, e.getMessage());
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

                    String tournamentName = event.path("tournament").path("name").asText(null);
                    String categoryName = event.path("tournament").path("category").path("name").asText(null);
                    String rawHome = event.path("homeTeam").path("name").asText(null);
                    String rawAway = event.path("awayTeam").path("name").asText(null);

                    String canonicalHome = TeamSignatureNormalizer.toCanonicalDisplayName(rawHome, tournamentName, categoryName);
                    String canonicalAway = TeamSignatureNormalizer.toCanonicalDisplayName(rawAway, tournamentName, categoryName);

                    String homeTeam = (canonicalHome != null && !canonicalHome.isBlank()) ? canonicalHome : rawHome;
                    String awayTeam = (canonicalAway != null && !canonicalAway.isBlank()) ? canonicalAway : rawAway;
                    long startTimestamp = event.path("startTimestamp").asLong(0) * 1000L;

                    list.add(SofaTaskDto.builder()
                            .eventId(eventId)
                            .sport(sport)
                            .type(type)
                            .homeTeam(homeTeam)
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
}
