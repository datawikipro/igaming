package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillProgressDto;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillResult;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.raw.SofaRawEventDto;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Historical backfill service for SofaScore match events.
 * Fetches batches of past scheduled events from api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse,
 * persists raw JSON payloads into PostgreSQL (sofascore_raw_events) via SofaRawEventService,
 * and broadcasts events to Kafka topic igaming.sofascore.raw-events.
 * 
 * Supports safe rate-limiting (200-300 ms delays) and multi-sport batch runs over date ranges.
 */
@Service
@Slf4j
public class SofaHistoryBackfillService {

    public static final String SOFASCORE_SCHEDULE_URL =
            "https://api.sofascore.com/api/v1/sport/{sport}/scheduled-events/{date}/inverse";

    public static final List<String> DEFAULT_SPORTS = List.of(
            "football", "basketball", "ice-hockey", "tennis", "volleyball", "table-tennis",
            "handball", "baseball", "american-football", "rugby", "aussie-rules", "cricket",
            "darts", "snooker", "badminton", "futsal", "waterpolo", "floorball", "mma",
            "motorsport", "cycling", "esports"
    );

    private final RestTemplate sofaScoreRestTemplate;
    private final ObjectMapper objectMapper;
    private final SofaRawEventService rawEventService;

    private final AtomicBoolean isBackfillRunning = new AtomicBoolean(false);
    private final AtomicReference<BackfillProgressDto> progressReference = new AtomicReference<>();

    public SofaHistoryBackfillService(
            @Qualifier("sofaScoreRestTemplate") RestTemplate sofaScoreRestTemplate,
            ObjectMapper objectMapper,
            SofaRawEventService rawEventService) {
        this.sofaScoreRestTemplate = sofaScoreRestTemplate;
        this.objectMapper = objectMapper;
        this.rawEventService = rawEventService;
    }

    /**
     * Backfills match events for a single date and sport.
     *
     * @param sport sport name or slug (e.g. "football", "HOCKEY", "теннис")
     * @param date  target date
     * @return number of successfully ingested events
     */
    public int backfillDate(String sport, LocalDate date) {
        if (date == null) return 0;
        String sportSlug = mapSportToSlug(sport);
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String apiSportSlug = isEsportsDiscipline(sportSlug) ? "esports" : sportSlug;

        log.debug("[SofaBackfill] Requesting scheduled events for sport '{}' (api: '{}') on {}", sportSlug, apiSportSlug, dateStr);

        try {
            HttpHeaders headers = SofaScoreHeaderUtils.createBrowserHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = sofaScoreRestTemplate.exchange(
                    SOFASCORE_SCHEDULE_URL,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    apiSportSlug,
                    dateStr
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.debug("[SofaBackfill] Non-2xx response for {} on {}: {}", sportSlug, dateStr, response.getStatusCode());
                return 0;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode events = root.path("events");
            if (!events.isArray() || events.isEmpty()) {
                log.debug("[SofaBackfill] No events returned for {} on {}", sportSlug, dateStr);
                return 0;
            }

            int count = 0;
            for (JsonNode event : events) {
                long eventId = event.path("id").asLong(0);
                if (eventId <= 0) continue;

                long startTimestamp = event.path("startTimestamp").asLong(0);
                JsonNode homeNode = event.path("homeTeam");
                JsonNode awayNode = event.path("awayTeam");
                JsonNode tournamentNode = event.path("tournament");
                JsonNode seasonNode = event.path("season");
                JsonNode statusNode = event.path("status");

                String statusType = statusNode.path("type").asText("notstarted");
                int hs = event.path("homeScore").path("current").asInt(-1);
                int as = event.path("awayScore").path("current").asInt(-1);

                String eventSport = event.path("sport").path("slug").asText(sportSlug);
                if ("esports".equalsIgnoreCase(eventSport) || isEsportsDiscipline(sportSlug)) {
                    String categoryName = tournamentNode.path("category").path("name").asText(null);
                    String tournName = tournamentNode.path("name").asText(null);
                    eventSport = pro.datawiki.igaming.capture.sofascore.crawler.SofaCrawlerService.resolveEsportsDiscipline(categoryName, tournName);
                    // If caller specifically requested a specific discipline like cs2 or dota2, skip non-matching events
                    if (isEsportsDiscipline(sportSlug) && !sportSlug.equalsIgnoreCase("esports") && !sportSlug.equalsIgnoreCase(eventSport)) {
                        continue;
                    }
                }

                SofaRawEventDto rawDto = SofaRawEventDto.builder()
                        .eventId(eventId)
                        .sport(eventSport)
                        .tournamentName(tournamentNode.path("name").asText(null))
                        .season(seasonNode.path("name").asText(null))
                        .matchDate(startTimestamp > 0 ? startTimestamp * 1000L : null)
                        .status(statusType)
                        .homeTeam(homeNode.path("name").asText(null))
                        .awayTeam(awayNode.path("name").asText(null))
                        .homeScore(hs >= 0 ? hs : null)
                        .awayScore(as >= 0 ? as : null)
                        .eventJson(event.toString())
                        .capturedAt(System.currentTimeMillis())
                        .build();

                rawEventService.recordRawEvent(rawDto);
                count++;
            }

            log.info("[SofaBackfill] Ingested {} matches for sport '{}' ({})", count, sportSlug, dateStr);
            return count;

        } catch (HttpStatusCodeException e) {
            log.warn("[SofaBackfill] HTTP error {} fetching {} on {}: {}",
                    e.getStatusCode(), sportSlug, dateStr, e.getStatusText());
            return 0;
        } catch (Exception e) {
            log.warn("[SofaBackfill] Failed fetching {} on {}: {}", sportSlug, dateStr, e.getMessage());
            return 0;
        }
    }

    /**
     * Backfills match events for a single date across multiple sports.
     */
    public int backfillDateAllSports(LocalDate date, List<String> sports) {
        if (date == null) return 0;
        List<String> targetSports = (sports != null && !sports.isEmpty()) ? sports : DEFAULT_SPORTS;
        int total = 0;
        for (String sport : targetSports) {
            total += backfillDate(sport, date);
        }
        return total;
    }

    /**
     * Backfills a date range for a specific sport with rate-limiting between days.
     *
     * @param sport     sport name or slug
     * @param startDate start date (e.g. 2024-10-16)
     * @param endDate   end date (e.g. 2026-10-16)
     * @param delayMs   rate-limiting pause in milliseconds (e.g. 250ms)
     * @return BackfillResult with summary statistics
     */
    public BackfillResult backfillRange(String sport, LocalDate startDate, LocalDate endDate, long delayMs) {
        return backfillRangeAllSports(Collections.singletonList(sport), startDate, endDate, delayMs);
    }

    /**
     * Backfills a date range for multiple sports with rate-limiting.
     *
     * @param sports    list of sports
     * @param startDate start date
     * @param endDate   end date
     * @param delayMs   rate-limiting pause in ms
     * @return BackfillResult summary
     */
    public BackfillResult backfillRangeAllSports(List<String> sports, LocalDate startDate, LocalDate endDate, long delayMs) {
        if (startDate == null) startDate = LocalDate.now().minusYears(2);
        if (endDate == null) endDate = LocalDate.now();

        if (startDate.isAfter(endDate)) {
            return BackfillResult.builder()
                    .status("FAILED")
                    .startDate(startDate)
                    .endDate(endDate)
                    .sports(sports)
                    .message("startDate cannot be after endDate")
                    .build();
        }

        List<String> targetSports = (sports != null && !sports.isEmpty()) ? sports : DEFAULT_SPORTS;
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long startTime = System.currentTimeMillis();

        log.info("[SofaBackfill] Starting backfill range: {} to {} ({} days) for sports: {}, delay: {}ms",
                startDate, endDate, totalDays, targetSports, delayMs);

        isBackfillRunning.set(true);
        int totalIngested = 0;
        int datesProcessed = 0;
        int errorsCount = 0;
        String status = "COMPLETED";
        String message = "Backfill completed successfully";

        try {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                datesProcessed++;

                for (String sport : targetSports) {
                    updateProgress(true, sport, current, startDate, endDate, targetSports,
                            (int) totalDays, datesProcessed, totalIngested, errorsCount,
                            Instant.ofEpochMilli(startTime), "Processing " + sport + " for " + current);

                    try {
                        int ingested = backfillDate(sport, current);
                        totalIngested += ingested;
                    } catch (Exception e) {
                        errorsCount++;
                        log.error("[SofaBackfill] Unexpected error on date {} sport {}: {}", current, sport, e.getMessage());
                    }
                }

                // Safe rate limiting between days
                if (delayMs > 0 && !current.isEqual(endDate)) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("[SofaBackfill] Backfill loop interrupted at date {}", current);
                        status = "INTERRUPTED";
                        message = "Backfill interrupted at " + current;
                        break;
                    }
                }

                current = current.plusDays(1);
            }
        } finally {
            isBackfillRunning.set(false);
            long duration = System.currentTimeMillis() - startTime;
            updateProgress(false, null, endDate, startDate, endDate, targetSports,
                    (int) totalDays, datesProcessed, totalIngested, errorsCount,
                    Instant.ofEpochMilli(startTime), message);

            log.info("[SofaBackfill] {} in {} ms. Days processed: {}/{}, Total events: {}, Errors: {}",
                    status, duration, datesProcessed, totalDays, totalIngested, errorsCount);
        }

        long durationMs = System.currentTimeMillis() - startTime;
        return BackfillResult.builder()
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .sports(targetSports)
                .totalDatesProcessed(datesProcessed)
                .totalEventsIngested(totalIngested)
                .errorsCount(errorsCount)
                .durationMs(durationMs)
                .message(message)
                .build();
    }

    /**
     * Executes backfill asynchronously in background thread.
     */
    public CompletableFuture<BackfillResult> backfillRangeAsync(
            List<String> sports, LocalDate startDate, LocalDate endDate, long delayMs) {
        return CompletableFuture.supplyAsync(() -> backfillRangeAllSports(sports, startDate, endDate, delayMs));
    }

    public boolean isRunning() {
        return isBackfillRunning.get();
    }

    public BackfillProgressDto getCurrentProgress() {
        BackfillProgressDto progress = progressReference.get();
        if (progress == null) {
            return BackfillProgressDto.builder()
                    .running(false)
                    .lastMessage("No backfill operation has been executed yet")
                    .build();
        }
        if (progress.isRunning() && progress.getStartedAt() != null) {
            progress.setDurationSeconds(Duration.between(progress.getStartedAt(), Instant.now()).toSeconds());
        }
        return progress;
    }

    private void updateProgress(boolean running, String sport, LocalDate currentDate,
                                LocalDate startDate, LocalDate endDate, List<String> sports,
                                int totalDays, int daysProcessed, int eventsIngested, int errorsCount,
                                Instant startedAt, String message) {
        progressReference.set(BackfillProgressDto.builder()
                .running(running)
                .currentSport(sport)
                .currentDate(currentDate)
                .startDate(startDate)
                .endDate(endDate)
                .sports(sports)
                .totalDays(totalDays)
                .daysProcessed(daysProcessed)
                .eventsIngested(eventsIngested)
                .errorsCount(errorsCount)
                .startedAt(startedAt)
                .durationSeconds(Duration.between(startedAt, Instant.now()).toSeconds())
                .lastMessage(message)
                .build());
    }

    public String mapSportToSlug(String sportName) {
        if (sportName == null || sportName.isBlank()) return "football";
        try {
            return SportType.fromAny(sportName).getSlug();
        } catch (Exception e) {
            String lower = sportName.trim().toLowerCase();
            if (lower.contains("foot") || lower.contains("фут")) return "football";
            if (lower.contains("basket") || lower.contains("баск")) return "basketball";
            if (lower.contains("hock") || lower.contains("хок")) return "ice-hockey";
            if (lower.contains("tenn") || lower.contains("тенн")) return "tennis";
            if (lower.contains("volley") || lower.contains("вол")) return "volleyball";
            if (lower.contains("cs") || lower.contains("counter")) return "cs2";
            if (lower.contains("dota")) return "dota2";
            if (lower.contains("valorant")) return "valorant";
            if (lower.contains("league of legends") || lower.equals("lol")) return "league-of-legends";
            if (lower.contains("cyber") || lower.contains("киберфут")) return "cyber-football";
            if (lower.contains("esport")) return "esports";
            return lower;
        }
    }

    private boolean isEsportsDiscipline(String slug) {
        if (slug == null) return false;
        String s = slug.toLowerCase();
        return s.equals("esports") || s.equals("cs2") || s.equals("dota2") || s.equals("league-of-legends")
                || s.equals("valorant") || s.equals("cyber-football") || s.equals("cyber-basketball")
                || s.equals("cyber-hockey") || s.equals("rainbow-six") || s.equals("starcraft")
                || s.equals("mobile-legends");
    }
}
