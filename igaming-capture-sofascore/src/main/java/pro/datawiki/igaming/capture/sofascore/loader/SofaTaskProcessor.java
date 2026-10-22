package pro.datawiki.igaming.capture.sofascore.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.capture.sofascore.util.SimilarityUtils;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;

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
    private final JdbcTemplate jdbcTemplate;
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

            // 2. Resolve aggregator match ID if not already known
            Long matchId = task.getAggregatorMatchId();
            if (matchId == null) {
                matchId = resolveAggregatorMatchId(task);
                if (matchId != null) {
                    try {
                        jdbcTemplate.update(
                                "UPDATE match_record SET reference_provider = 'SOFASCORE', reference_fixture_id = ?, is_reference_verified = true WHERE id = ?",
                                String.valueOf(eventId), matchId
                        );
                        log.info("[SofaTaskProcessor] Auto-linked SofaScore event ID {} to match_record ID {}", eventId, matchId);
                    } catch (Exception e) {
                        log.debug("[SofaTaskProcessor] Failed to update reference_provider for match {}: {}", matchId, e.getMessage());
                    }
                }
            }

            // 3. If aggregator match ID is resolved, forward directly to aggregator to populate lineups/h2h/storyline
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

    private Long resolveAggregatorMatchId(SofaTaskDto task) {
        if (task == null || task.getSport() == null || task.getMatchDate() == null) {
            return null;
        }
        String sportName = mapSportToAggregator(task.getSport());
        long startMs = task.getMatchDate() - 3 * 3600 * 1000L;
        long endMs = task.getMatchDate() + 3 * 3600 * 1000L;

        String sql = """
            SELECT m.id, t1.default_display_name as team1, t2.default_display_name as team2, l.name as league
            FROM match_record m
            JOIN team t1 ON m.team1_id = t1.id
            JOIN team t2 ON m.team2_id = t2.id
            LEFT JOIN league l ON m.league_id = l.id
            WHERE m.sport_name = ?
              AND m.start_time BETWEEN ? AND ?
        """;

        try {
            List<CandidateMatch> candidates = jdbcTemplate.query(sql, (rs, rowNum) -> new CandidateMatch(
                    rs.getLong("id"),
                    rs.getString("team1"),
                    rs.getString("team2"),
                    rs.getString("league")
            ), sportName, startMs, endMs);

            for (CandidateMatch cand : candidates) {
                boolean direct = SimilarityUtils.areTeamsSimilar(cand.team1(), task.getHomeTeam(), cand.league(), null)
                        && SimilarityUtils.areTeamsSimilar(cand.team2(), task.getAwayTeam(), cand.league(), null);
                boolean reversed = SimilarityUtils.areTeamsSimilar(cand.team1(), task.getAwayTeam(), cand.league(), null)
                        && SimilarityUtils.areTeamsSimilar(cand.team2(), task.getHomeTeam(), cand.league(), null);

                if (direct || reversed) {
                    return cand.id();
                }
            }
        } catch (Exception e) {
            log.debug("[SofaTaskProcessor] Error resolving aggregator match for event {}: {}", task.getEventId(), e.getMessage());
        }
        return null;
    }

    private String mapSportToAggregator(String sofaSport) {
        if (sofaSport == null) return "FOOTBALL";
        return switch (sofaSport.toLowerCase().trim()) {
            case "football", "soccer" -> "FOOTBALL";
            case "basketball" -> "BASKETBALL";
            case "ice-hockey", "hockey" -> "HOCKEY";
            case "tennis" -> "TENNIS";
            case "table-tennis" -> "TABLE_TENNIS";
            case "volleyball" -> "VOLLEYBALL";
            case "esports" -> "ESPORTS";
            default -> sofaSport.toUpperCase().replace('-', '_');
        };
    }

    private record CandidateMatch(Long id, String team1, String team2, String league) {}
}
