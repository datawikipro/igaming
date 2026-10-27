package pro.datawiki.igaming.capture.sofascore.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;

/**
 * Executes deep scraping and enrichment ingestion for a single SofaScore event task.
 * Fetches event details, lineups (starters, bench, injuries), and H2H.
 * Persists raw payload to PostgreSQL (sofascore_raw_events) and Kafka.
 * Resolves match_record link if unmapped, and pushes enrichment to aggregator REST endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SofaTaskProcessor {

    private final SofaScoreEnrichmentProvider enrichmentProvider;
    private final RestTemplate restTemplate;
    private final ObjectProvider<SofaOnDemandService> onDemandServiceProvider;

    @Value("${app.aggregator.url:http://igaming-aggregator-api.igaming-dev.svc.cluster.local:8080}")
    private String aggregatorUrl;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-dev.svc.cluster.local:8095}")
    private String mdmUrl;

    public boolean processTask(SofaTaskDto task) {
        if (task == null || task.getEventId() == null) {
            return false;
        }

        if (task.getType() == SofaTaskDto.TaskType.ON_DEMAND) {
            SofaOnDemandService onDemandService = onDemandServiceProvider.getIfAvailable();
            if (onDemandService != null) {
                onDemandService.processTask(task);
                return true;
            }
        }

        long eventId = task.getEventId();
        log.debug("[SofaTaskProcessor] Processing {} task for event ID {} ({} vs {}, sport: {})",
                task.getType(), eventId, task.getHomeTeam(), task.getAwayTeam(), task.getSport());

        try {
            // 1. Fetch complete enrichment (Event + Lineups + H2H + Streaks)
            // This internally saves raw JSON to sofascore_raw_events and Kafka!
            Optional<MatchEnrichmentDto> enrichmentOpt = enrichmentProvider.fetchEnrichment(eventId, task.getSport());

            if (enrichmentOpt.isEmpty()) {
                log.debug("[SofaTaskProcessor] No enrichment returned for event ID {}", eventId);
                return false;
            }

            MatchEnrichmentDto dto = enrichmentOpt.get();

            // 2. Resolve or create canonical match in Ground Truth MDM
            Long matchId = task.getAggregatorMatchId();
            if (matchId == null) {
                matchId = resolveOrCreateMdmMatch(task);
            }

            // 3. If match ID is resolved, forward directly to aggregator to populate lineups/h2h/storyline
            if (matchId != null) {
                dto.setMatchId(matchId);
                String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/enrichment";
                restTemplate.postForLocation(updateUrl, dto);
                log.info("[SofaTaskProcessor] Successfully enriched aggregator match ID {} from event ID {}", matchId, eventId);
            }

            log.info("[SofaTaskProcessor] Ingested {} event ID {}: {} vs {} (Lineups confirmed: {}, AggregatorMatchId: {})",
                    task.getType(), eventId, task.getHomeTeam(), task.getAwayTeam(),
                    dto.getLineups() != null && dto.getLineups().isConfirmed(), matchId);
            return true;
        } catch (Exception e) {
            log.error("[SofaTaskProcessor] Error processing event ID {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    private Long resolveOrCreateMdmMatch(SofaTaskDto task) {
        if (task == null || task.getSport() == null || task.getEventId() == null) {
            return null;
        }
        try {
            Long startTimeEpoch = task.getMatchDate() != null ? task.getMatchDate() / 1000L : null;
            MatchResolutionRequest req = MatchResolutionRequest.builder()
                    .sportSlug(task.getSport().trim().toLowerCase())
                    .sourceOrigin("SOFASCORE")
                    .createdBySource("sofascore")
                    .sofascoreId(task.getEventId())
                    .sofascoreTournamentId(task.getTournamentId())
                    .tournamentName(task.getTournamentName())
                    .categoryName(task.getCategoryName())
                    .sofascoreHomeTeamId(task.getHomeTeamId())
                    .homeTeamName(task.getHomeTeam())
                    .sofascoreAwayTeamId(task.getAwayTeamId())
                    .awayTeamName(task.getAwayTeam())
                    .startTimeEpochSec(startTimeEpoch)
                    .eventUrl("https://www.sofascore.com/event/" + task.getEventId())
                    .build();

            String url = mdmUrl + "/api/v1/mdm/matches/find-or-create";
            CanonicalMatchHeaderDto header = restTemplate.postForObject(url, req, CanonicalMatchHeaderDto.class);
            if (header != null && header.getMatchId() != null) {
                log.info("🎯 [SofaTaskProcessor] Ground Truth MDM match resolved: #{} (sofaId={}, {} vs {})",
                        header.getMatchId(), task.getEventId(), header.getHomeTeamName(), header.getAwayTeamName());
                return header.getMatchId();
            }
        } catch (Exception e) {
            log.warn("⚠️ [SofaTaskProcessor] Failed to findOrCreateMatch in MDM for sofa event #{}: {}",
                    task.getEventId(), e.getMessage());
        }
        return null;
    }
}
