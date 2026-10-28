package pro.datawiki.igaming.capture.sofascore.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import java.util.Map;
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
    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<SofaOnDemandService> onDemandServiceProvider;

    @Value("${app.aggregator.url:http://igaming-aggregator-api.igaming-dev.svc.cluster.local:8080}")
    private String aggregatorUrl;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
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
                try {
                    String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/enrichment";
                    restTemplate.postForLocation(updateUrl, dto);
                    log.info("[SofaTaskProcessor] Successfully enriched aggregator match ID {} from event ID {}", matchId, eventId);
                } catch (Exception e) {
                    log.debug("[SofaTaskProcessor] Legacy aggregator enrichment forward skipped/unavailable: {}", e.getMessage());
                }
                // Propagate live score / result to MDM Live Score Consensus
                propagateToMdmLiveScore(matchId, eventId);
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

    public void propagateToMdmLiveScore(Long matchId, Long eventId) {
        if (matchId == null || eventId == null) return;
        try {
            String sofaSql = "SELECT home_score, away_score, status FROM sofascore_raw_events WHERE event_id = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sofaSql, eventId);
            if (rows.isEmpty()) return;
            Map<String, Object> r = rows.get(0);
            Object hs = r.get("home_score");
            Object as = r.get("away_score");
            String st = (String) r.get("status");

            MatchStatus status = mapSofaStatus(st);
            Integer s1 = hs != null ? ((Number) hs).intValue() : null;
            Integer s2 = as != null ? ((Number) as).intValue() : null;

            if (status != null && (s1 != null || s2 != null || status == MatchStatus.FINISHED)) {
                CanonicalLiveScoreDto dto = CanonicalLiveScoreDto.builder()
                        .matchId(matchId)
                        .sourceCode("SOFASCORE")
                        .status(status)
                        .score1(s1)
                        .score2(s2)
                        .displayTimer(status == MatchStatus.FINISHED ? "FT" : "")
                        .timestamp(System.currentTimeMillis())
                        .build();

                String url = mdmUrl + "/api/v1/mdm/matches/" + matchId + "/live-score";
                restTemplate.postForObject(url, dto, CanonicalLiveScoreDto.class);
                log.info("🎯 [SofaTaskProcessor] Propagated SofaScore score to MDM for match #{}: status={}, score={}:{}",
                        matchId, status, s1, s2);
            }
        } catch (Exception e) {
            log.debug("Failed propagating live score to MDM for match #{}: {}", matchId, e.getMessage());
        }
    }

    public static MatchStatus mapSofaStatus(String status) {
        if (status == null || status.isBlank()) return MatchStatus.SCHEDULED;
        String s = status.toLowerCase().trim();
        if (s.equals("finished") || s.equals("ended") || s.equals("closed") || s.equals("ft") || s.equals("aet") || s.equals("ap")) {
            return MatchStatus.FINISHED;
        }
        if (s.equals("inprogress") || s.equals("live") || s.contains("half") || s.equals("ht") || s.contains("set") || s.contains("period") || s.contains("quarter") || s.equals("extra_time") || s.equals("penalties")) {
            return MatchStatus.LIVE;
        }
        if (s.equals("postponed")) {
            return MatchStatus.POSTPONED;
        }
        if (s.equals("cancelled") || s.equals("canceled") || s.equals("abandoned") || s.equals("interrupted")) {
            return MatchStatus.CANCELLED;
        }
        return MatchStatus.SCHEDULED;
    }
}
