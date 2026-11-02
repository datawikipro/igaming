package pro.datawiki.igaming.capture.sofascore.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;
import pro.datawiki.igaming.dto.MatchLineupsDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.lineup.FootballMatchLineupDto;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Executes deep scraping and enrichment ingestion for a single SofaScore event task.
 * Fetches event details, lineups (starters, bench, injuries), and H2H.
 * Persists raw payload to PostgreSQL (sofascore_raw_events) and broadcasts to Kafka:
 * - 'sports.canonical.scores' for live & finished scores
 * - 'sports.football.lineups' for starting XI, bench, and squad news
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
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Value("${app.aggregator.url:http://igaming-aggregator-api.igaming-dev.svc.cluster.local:8080}")
    private String aggregatorUrl;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    @Value("${app.kafka.topics.canonical-scores:sports.canonical.scores}")
    private String canonicalScoresTopic = "sports.canonical.scores";

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
                // Propagate live score / result to MDM Live Score Consensus (Kafka + HTTP fallback)
                propagateToMdmLiveScore(matchId, eventId);
            }

            // 4. Emit Football Lineup event over Kafka bus
            if (dto.getLineups() != null) {
                emitFootballLineup(eventId, matchId, dto.getLineups());
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
                    .build();

            String url = mdmUrl + "/api/v1/mdm/matches/resolve";
            CanonicalMatchHeaderDto header = restTemplate.postForObject(url, req, CanonicalMatchHeaderDto.class);
            if (header != null && header.getMatchId() != null) {
                log.info("[SofaTaskProcessor] Resolved/created canonical MDM match #{} for SofaScore event {}",
                        header.getMatchId(), task.getEventId());
                return header.getMatchId();
            }
        } catch (Exception e) {
            log.warn("[SofaTaskProcessor] Failed to resolve MDM match for event {}: {}", task.getEventId(), e.getMessage());
        }
        return null;
    }

    private void propagateToMdmLiveScore(Long matchId, Long eventId) {
        try {
            String sql = "SELECT home_score, away_score, status FROM sofascore_raw_events WHERE event_id = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, eventId);
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
                        .externalEventId(String.valueOf(eventId))
                        .sport("football")
                        .status(status)
                        .score1(s1)
                        .score2(s2)
                        .regularScore1(s1)
                        .regularScore2(s2)
                        .displayTimer(status == MatchStatus.FINISHED ? "FT" : "")
                        .timestamp(System.currentTimeMillis())
                        .build();

                // 1. Kafka Event Bus publication
                if (kafkaTemplate != null && kafkaTemplate.isPresent()) {
                    try {
                        kafkaTemplate.get().send(canonicalScoresTopic, String.valueOf(matchId), dto);
                        log.info("📡 [Score-Kafka] Emitted SofaScore live score to {}: match #{}, score={}:{}",
                                canonicalScoresTopic, matchId, s1, s2);
                    } catch (Exception kEx) {
                        log.warn("Failed emitting SofaScore score to Kafka: {}", kEx.getMessage());
                    }
                }

                // 2. HTTP Fallback to MDM Live Score endpoint
                try {
                    String url = mdmUrl + "/api/v1/mdm/matches/" + matchId + "/live-score";
                    restTemplate.postForObject(url, dto, CanonicalLiveScoreDto.class);
                    log.info("🎯 [SofaTaskProcessor] Propagated SofaScore score to MDM for match #{}: status={}, score={}:{}",
                            matchId, status, s1, s2);
                } catch (Exception httpEx) {
                    log.debug("HTTP fallback to MDM skipped: {}", httpEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Failed propagating live score to MDM for match #{}: {}", matchId, e.getMessage());
        }
    }

    private void emitFootballLineup(Long eventId, Long matchId, MatchLineupsDto dto) {
        if (kafkaTemplate == null || !kafkaTemplate.isPresent() || dto == null) return;
        try {
            FootballMatchLineupDto lineup = FootballMatchLineupDto.builder()
                    .matchId(matchId)
                    .sourceOrigin("sofascore")
                    .externalEventId(String.valueOf(eventId))
                    .confirmed(dto.isConfirmed())
                    .timestamp(System.currentTimeMillis())
                    .formationHome(dto.getFormationTeam1())
                    .formationAway(dto.getFormationTeam2())
                    .coachNameHome(dto.getCoachNameTeam1())
                    .coachNameAway(dto.getCoachNameTeam2())
                    .startersHome(mapPlayers(dto.getStartersTeam1()))
                    .startersAway(mapPlayers(dto.getStartersTeam2()))
                    .benchHome(mapPlayers(dto.getBenchTeam1()))
                    .benchAway(mapPlayers(dto.getBenchTeam2()))
                    .missingHome(mapMissing(dto.getMissingTeam1()))
                    .missingAway(mapMissing(dto.getMissingTeam2()))
                    .substitutionsHome(mapSubs(dto.getSubstitutionsTeam1()))
                    .substitutionsAway(mapSubs(dto.getSubstitutionsTeam2()))
                    .build();

            String topic = lineup.getKafkaTopic();
            String key = matchId != null ? String.valueOf(matchId) : String.valueOf(eventId);
            kafkaTemplate.get().send(topic, key, lineup);
            log.info("📡 [Lineup-Kafka] Published Football lineup to topic {}: match #{} (ext: {}, confirmed: {})",
                    topic, matchId, eventId, dto.isConfirmed());
        } catch (Exception ex) {
            log.warn("Failed emitting Football lineup to Kafka: {}", ex.getMessage());
        }
    }

    private List<FootballMatchLineupDto.FootballPlayerDto> mapPlayers(List<MatchLineupsDto.PlayerLineupDto> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(p -> FootballMatchLineupDto.FootballPlayerDto.builder()
                .name(p.getName())
                .shirtNumber(p.getShirtNumber())
                .position(p.getPosition())
                .rating(p.getRating())
                .captain(p.isCaptain())
                .substitute(p.isSubstitute())
                .externalPlayerId(p.getSofascorePlayerId())
                .mdmPlayerId(p.getMdmPlayerId())
                .minuteIn(p.getMinuteIn())
                .minuteOut(p.getMinuteOut())
                .minutesPlayed(p.getMinutesPlayed())
                .build()).toList();
    }

    private List<FootballMatchLineupDto.FootballMissingPlayerDto> mapMissing(List<MatchLineupsDto.MissingPlayerDto> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(m -> FootballMatchLineupDto.FootballMissingPlayerDto.builder()
                .name(m.getName())
                .reason(m.getReason())
                .type(m.getType())
                .build()).toList();
    }

    private List<FootballMatchLineupDto.FootballSubstitutionDto> mapSubs(List<MatchLineupsDto.SubstitutionDto> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(s -> FootballMatchLineupDto.FootballSubstitutionDto.builder()
                .playerIn(s.getPlayerIn())
                .playerInNumber(s.getPlayerInNumber())
                .playerOut(s.getPlayerOut())
                .playerOutNumber(s.getPlayerOutNumber())
                .minute(s.getMinute())
                .minuteExtra(s.getMinuteExtra())
                .teamIndex(s.getTeamIndex())
                .build()).toList();
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
