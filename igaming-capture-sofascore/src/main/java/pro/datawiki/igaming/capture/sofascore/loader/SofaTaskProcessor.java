package pro.datawiki.igaming.capture.sofascore.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;

/**
 * Executes deep scraping and enrichment ingestion for a single SofaScore event task.
 * Fetches event details, lineups (starters, bench, injuries), and H2H.
 * Persists raw payload to PostgreSQL (sofascore_raw_events) and Kafka.
 * If matched to an aggregator match_record, pushes to aggregator REST endpoint.
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

            // 2. If aggregator match ID is known, forward directly to aggregator to populate match_lineup_player
            Long matchId = task.getAggregatorMatchId();
            if (matchId != null) {
                dto.setMatchId(matchId);
                String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/enrichment";
                restTemplate.postForLocation(updateUrl, dto);
                log.info("[SofaTaskProcessor] Successfully enriched aggregator match ID {} from event ID {}", matchId, eventId);
            }

            log.info("[SofaTaskProcessor] Ingested {} event ID {}: {} vs {} (Lineups confirmed: {})",
                    task.getType(), eventId, task.getHomeTeam(), task.getAwayTeam(),
                    dto.getLineups() != null && dto.getLineups().isConfirmed());
            return true;
        } catch (Exception e) {
            log.error("[SofaTaskProcessor] Error processing event ID {}: {}", eventId, e.getMessage());
            return false;
        }
    }
}
