package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.MatchResultProvider;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;
import pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;
import pro.datawiki.igaming.dto.MatchLineupsDto;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandRequest;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandResponse;
import pro.datawiki.igaming.dto.raw.SofaRawEventDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service orchestrating synchronous and asynchronous on-demand match refreshes.
 * Pulls fresh match data (score, status, confirmed lineups, missing players/injuries)
 * from SofaScore, persists raw event data, emits to Kafka, and notifies the aggregator.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SofaOnDemandService {

    private final SofaScoreEnrichmentProvider enrichmentProvider;
    private final SofaQueueService queueService;
    private final SofaRawEventRepository rawEventRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.aggregator.url:http://igaming-aggregator-api.igaming-dev.svc.cluster.local:8080}")
    private String aggregatorUrl;

    /**
     * Synchronous on-demand refresh. Blocks until fresh data is fetched and returns complete response.
     */
    public SofaOnDemandResponse refreshMatchSync(SofaOnDemandRequest request) {
        long startTime = System.currentTimeMillis();
        String taskId = "sod-sync-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[SofaOnDemand] Executing synchronous refresh for matchId: {}, eventId: {}, {} vs {} (sport: {})",
                request.getMatchId(), request.getEventId(), request.getHomeTeam(), request.getAwayTeam(), request.getSport());

        try {
            Long eventId = resolveEventId(request);
            if (eventId == null) {
                log.warn("[SofaOnDemand] Could not resolve SofaScore event ID for request: {}", request);
                return SofaOnDemandResponse.builder()
                        .taskId(taskId)
                        .status("NOT_FOUND")
                        .matchId(request.getMatchId())
                        .sport(request.getSport())
                        .homeTeam(request.getHomeTeam())
                        .awayTeam(request.getAwayTeam())
                        .timestamp(System.currentTimeMillis())
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .message("Could not resolve SofaScore event ID for match")
                        .build();
            }

            // 1. Fetch complete enrichment (Event + Lineups + H2H + Streaks)
            Optional<MatchEnrichmentDto> enrichmentOpt = enrichmentProvider.fetchEnrichment(eventId);

            // 2. Fetch raw event from DB (saved by fetchEnrichment)
            Optional<SofaRawEventEntity> rawEntityOpt = rawEventRepository.findById(eventId);

            SofaOnDemandResponse response = buildResponse(taskId, request, eventId, enrichmentOpt.orElse(null), rawEntityOpt.orElse(null), startTime);

            // 3. Forward to aggregator if matchId present
            if (request.getMatchId() != null && enrichmentOpt.isPresent()) {
                enrichmentOpt.get().setMatchId(request.getMatchId());
                forwardEnrichmentToAggregator(request.getMatchId(), enrichmentOpt.get());
            }

            // Cache result in Redis
            queueService.saveTaskResult(taskId, response);
            return response;

        } catch (Exception e) {
            log.error("[SofaOnDemand] Error executing sync refresh: {}", e.getMessage(), e);
            return SofaOnDemandResponse.builder()
                    .taskId(taskId)
                    .status("FAILED")
                    .matchId(request.getMatchId())
                    .eventId(request.getEventId())
                    .sport(request.getSport())
                    .homeTeam(request.getHomeTeam())
                    .awayTeam(request.getAwayTeam())
                    .timestamp(System.currentTimeMillis())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .message("Failed to fetch fresh match data: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Asynchronous on-demand refresh. Pushes to P00 Redis queue and returns immediate 202 Accepted.
     */
    public SofaOnDemandResponse submitMatchRefreshAsync(SofaOnDemandRequest request) {
        String taskId = "sod-async-" + UUID.randomUUID().toString();
        log.info("[SofaOnDemand] Enqueuing asynchronous task {} for matchId: {}, eventId: {}",
                taskId, request.getMatchId(), request.getEventId());

        SofaTaskDto task = SofaTaskDto.builder()
                .taskId(taskId)
                .eventId(request.getEventId())
                .sport(request.getSport())
                .homeTeam(request.getHomeTeam())
                .awayTeam(request.getAwayTeam())
                .aggregatorMatchId(request.getMatchId())
                .matchDate(request.getMatchDate())
                .type(SofaTaskDto.TaskType.ON_DEMAND)
                .priority(0)
                .build();

        queueService.pushOnDemandTask(task);

        return SofaOnDemandResponse.builder()
                .taskId(taskId)
                .status("QUEUED")
                .matchId(request.getMatchId())
                .eventId(request.getEventId())
                .sport(request.getSport())
                .homeTeam(request.getHomeTeam())
                .awayTeam(request.getAwayTeam())
                .timestamp(System.currentTimeMillis())
                .message("Task queued for high-priority on-demand execution")
                .build();
    }

    /**
     * Retrieves status or cached result of an on-demand task.
     */
    public SofaOnDemandResponse getTaskStatus(String taskId) {
        if (taskId == null) {
            return SofaOnDemandResponse.builder().status("NOT_FOUND").message("Task ID is null").build();
        }

        SofaOnDemandResponse cached = queueService.getTaskResult(taskId);
        if (cached != null) {
            return cached;
        }

        String status = queueService.getTaskStatus(taskId);
        if (status != null) {
            return SofaOnDemandResponse.builder()
                    .taskId(taskId)
                    .status(status)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        return SofaOnDemandResponse.builder()
                .taskId(taskId)
                .status("NOT_FOUND")
                .message("No task found with ID " + taskId)
                .build();
    }

    /**
     * Processes an on-demand task popped from the queue.
     */
    public SofaOnDemandResponse processTask(SofaTaskDto task) {
        long startTime = System.currentTimeMillis();
        String taskId = task.getTaskId() != null ? task.getTaskId() : "sod-" + UUID.randomUUID().toString();
        queueService.setTaskStatus(taskId, "PROCESSING");
        log.info("[SofaOnDemand] Processing queued task {} for eventId {}", taskId, task.getEventId());

        SofaOnDemandRequest req = SofaOnDemandRequest.builder()
                .matchId(task.getAggregatorMatchId())
                .eventId(task.getEventId())
                .sport(task.getSport())
                .homeTeam(task.getHomeTeam())
                .awayTeam(task.getAwayTeam())
                .matchDate(task.getMatchDate())
                .build();

        try {
            Long eventId = resolveEventId(req);
            if (eventId == null) {
                SofaOnDemandResponse resp = SofaOnDemandResponse.builder()
                        .taskId(taskId)
                        .status("FAILED")
                        .matchId(task.getAggregatorMatchId())
                        .eventId(task.getEventId())
                        .sport(task.getSport())
                        .message("Could not resolve event ID")
                        .timestamp(System.currentTimeMillis())
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
                queueService.saveTaskResult(taskId, resp);
                return resp;
            }

            Optional<MatchEnrichmentDto> enrichmentOpt = enrichmentProvider.fetchEnrichment(eventId);
            Optional<SofaRawEventEntity> rawEntityOpt = rawEventRepository.findById(eventId);

            SofaOnDemandResponse response = buildResponse(taskId, req, eventId, enrichmentOpt.orElse(null), rawEntityOpt.orElse(null), startTime);

            if (task.getAggregatorMatchId() != null && enrichmentOpt.isPresent()) {
                enrichmentOpt.get().setMatchId(task.getAggregatorMatchId());
                forwardEnrichmentToAggregator(task.getAggregatorMatchId(), enrichmentOpt.get());
            }

            queueService.saveTaskResult(taskId, response);
            return response;

        } catch (Exception e) {
            log.error("[SofaOnDemand] Processing task {} failed: {}", taskId, e.getMessage(), e);
            SofaOnDemandResponse failed = SofaOnDemandResponse.builder()
                    .taskId(taskId)
                    .status("FAILED")
                    .matchId(task.getAggregatorMatchId())
                    .eventId(task.getEventId())
                    .message("Processing error: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            queueService.saveTaskResult(taskId, failed);
            return failed;
        }
    }

    private Long resolveEventId(SofaOnDemandRequest request) {
        if (request.getEventId() != null && request.getEventId() > 0) {
            return request.getEventId();
        }

        // Try lookup in DB first if teams are provided
        if (request.getSport() != null && request.getHomeTeam() != null && request.getAwayTeam() != null) {
            List<SofaRawEventEntity> matches = rawEventRepository.findBySportAndHomeTeamOrAwayTeamOrderByMatchDateDesc(
                    request.getSport(), request.getHomeTeam(), request.getAwayTeam());
            for (SofaRawEventEntity entity : matches) {
                if (entity.getEventId() != null) {
                    return entity.getEventId();
                }
            }
        }

        // Try resolution via SofaScoreEnrichmentProvider
        if (request.getHomeTeam() != null && request.getAwayTeam() != null) {
            long startTime = request.getMatchDate() != null ? request.getMatchDate() : System.currentTimeMillis();
            MatchResultProvider.MatchMetadata meta = new MatchResultProvider.MatchMetadata(
                    request.getMatchId() != null ? request.getMatchId() : 0L,
                    request.getSport() != null ? request.getSport() : "football",
                    request.getHomeTeam(),
                    request.getAwayTeam(),
                    startTime
            );
            Optional<Long> resolved = enrichmentProvider.resolveSofaScoreEventId(meta);
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }

        return null;
    }

    private SofaOnDemandResponse buildResponse(String taskId, SofaOnDemandRequest req, Long eventId,
                                              MatchEnrichmentDto enrichment, SofaRawEventEntity rawEntity, long startTime) {
        String homeTeam = req.getHomeTeam();
        String awayTeam = req.getAwayTeam();
        String sport = req.getSport();
        String statusType = "UNKNOWN";
        Integer homeScore = null;
        Integer awayScore = null;
        Integer incidentsCount = null;

        if (rawEntity != null) {
            if (rawEntity.getHomeTeam() != null) homeTeam = rawEntity.getHomeTeam();
            if (rawEntity.getAwayTeam() != null) awayTeam = rawEntity.getAwayTeam();
            if (rawEntity.getSport() != null) sport = rawEntity.getSport();
            if (rawEntity.getStatus() != null) statusType = rawEntity.getStatus();
            homeScore = rawEntity.getHomeScore();
            awayScore = rawEntity.getAwayScore();

            if (rawEntity.getEventJson() != null) {
                try {
                    JsonNode root = objectMapper.readTree(rawEntity.getEventJson());
                    JsonNode ev = root.path("event");
                    if (statusType.equals("UNKNOWN")) {
                        statusType = ev.path("status").path("type").asText("UNKNOWN");
                    }
                } catch (Exception ignored) {}
            }
        }

        boolean lineupsConfirmed = false;
        List<String> startersHome = Collections.emptyList();
        List<String> startersAway = Collections.emptyList();
        List<String> missingHome = Collections.emptyList();
        List<String> missingAway = Collections.emptyList();

        if (enrichment != null && enrichment.getLineups() != null) {
            MatchLineupsDto l = enrichment.getLineups();
            lineupsConfirmed = l.isConfirmed();

            if (l.getStartersTeam1() != null) {
                startersHome = l.getStartersTeam1().stream()
                        .map(MatchLineupsDto.PlayerLineupDto::getName)
                        .collect(Collectors.toList());
            }
            if (l.getStartersTeam2() != null) {
                startersAway = l.getStartersTeam2().stream()
                        .map(MatchLineupsDto.PlayerLineupDto::getName)
                        .collect(Collectors.toList());
            }
            if (l.getMissingTeam1() != null) {
                missingHome = l.getMissingTeam1().stream()
                        .map(m -> m.getName() + (m.getReason() != null ? " (" + m.getReason() + ")" : ""))
                        .collect(Collectors.toList());
            }
            if (l.getMissingTeam2() != null) {
                missingAway = l.getMissingTeam2().stream()
                        .map(m -> m.getName() + (m.getReason() != null ? " (" + m.getReason() + ")" : ""))
                        .collect(Collectors.toList());
            }
        }

        SofaRawEventDto rawDto = null;
        if (rawEntity != null) {
            rawDto = SofaRawEventDto.builder()
                    .eventId(rawEntity.getEventId())
                    .sport(rawEntity.getSport())
                    .tournamentName(rawEntity.getTournament())
                    .season(rawEntity.getSeason())
                    .matchDate(rawEntity.getMatchDate())
                    .status(rawEntity.getStatus())
                    .homeTeam(rawEntity.getHomeTeam())
                    .awayTeam(rawEntity.getAwayTeam())
                    .homeScore(rawEntity.getHomeScore())
                    .awayScore(rawEntity.getAwayScore())
                    .eventJson(rawEntity.getEventJson())
                    .lineupsJson(rawEntity.getLineupsJson())
                    .h2hJson(rawEntity.getH2hJson())
                    .streaksJson(rawEntity.getStreaksJson())
                    .capturedAt(rawEntity.getCapturedAt() != null ? rawEntity.getCapturedAt().toEpochMilli() : System.currentTimeMillis())
                    .build();
        }

        return SofaOnDemandResponse.builder()
                .taskId(taskId)
                .status("COMPLETED")
                .matchId(req.getMatchId())
                .eventId(eventId)
                .sport(sport)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .statusType(statusType)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .lineupsConfirmed(lineupsConfirmed)
                .startersHome(startersHome)
                .startersAway(startersAway)
                .missingHome(missingHome)
                .missingAway(missingAway)
                .incidentsCount(incidentsCount)
                .enrichment(enrichment)
                .rawEvent(rawDto)
                .timestamp(System.currentTimeMillis())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .message("Successfully refreshed match data")
                .build();
    }

    private void forwardEnrichmentToAggregator(Long matchId, MatchEnrichmentDto enrichment) {
        try {
            String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/enrichment";
            restTemplate.postForLocation(updateUrl, enrichment);
            log.info("[SofaOnDemand] Forwarded fresh enrichment to aggregator for match ID {}", matchId);
        } catch (Exception e) {
            log.warn("[SofaOnDemand] Could not forward enrichment to aggregator at {}: {}", aggregatorUrl, e.getMessage());
        }
    }
}
