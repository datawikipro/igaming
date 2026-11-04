package pro.datawiki.igaming.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.agent.client.AiAnalystClient;
import pro.datawiki.igaming.agent.client.MdmClient;
import pro.datawiki.igaming.agent.client.SofaScoreClient;
import pro.datawiki.igaming.agent.dto.AgentStatsDto;
import pro.datawiki.igaming.agent.dto.AnomalyEventDto;
import pro.datawiki.igaming.agent.dto.EmpiricalVerdictDto;
import pro.datawiki.igaming.agent.dto.EmpiricalVerdictType;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyResolutionService {

    private final MdmClient mdmClient;
    private final AiAnalystClient aiAnalystClient;
    private final SofaScoreClient sofaScoreClient;
    private final EmpiricalAnomalyAuditorService empiricalAuditorService;
    private final PlaneAiWorkerPoolDispatcher workerPoolDispatcher;
    private final ObjectMapper objectMapper;

    private static final Pattern SYNTHETIC_LEAGUE_PATTERN = Pattern.compile(
            "^(?:league\\s*\\d+|.*(?:fantasy|synthetic|мифическ|e-league|esoccer|2x2|3x3|4x4).*)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MAJOR_LEAGUE_PATTERN = Pattern.compile(
            ".*(?:nba|euroleague|vtb|premier league|la liga|serie a|bundesliga|ligue 1|nhl|khl|uefa).*$",
            Pattern.CASE_INSENSITIVE
    );

    private final AtomicLong totalEventsReceived = new AtomicLong(0);
    private final AtomicLong scheduleCollisionsProcessed = new AtomicLong(0);
    private final AtomicLong empiricalAuditsCount = new AtomicLong(0);
    private final AtomicLong duplicateMergesCount = new AtomicLong(0);
    private final AtomicLong trashOutrightsCancelledCount = new AtomicLong(0);
    private final AtomicLong unmappedAliasesResolvedCount = new AtomicLong(0);
    private final AtomicLong syntheticMatchesCancelled = new AtomicLong(0);
    private final AtomicLong groundTruthMatchesPromoted = new AtomicLong(0);
    private final AtomicLong confirmedCollisionsCount = new AtomicLong(0);
    private final AtomicLong unmappedTournamentsDetected = new AtomicLong(0);
    private final AtomicLong errorsCount = new AtomicLong(0);
    private volatile Instant lastActiveTimestamp = Instant.now();

    public void processAnomaly(AnomalyEventDto event) {
        totalEventsReceived.incrementAndGet();
        lastActiveTimestamp = Instant.now();
        log.info("📥 [Anomaly Agent] Received anomaly: system={}, type={}, key='{}', name='{}'",
                event.getExternalSystemCode(), event.getEntityType(), event.getRawKey(), event.getRawName());

        if ("SCHEDULE_COLLISION".equalsIgnoreCase(event.getEntityType())) {
            resolveScheduleCollision(event);
        } else if ("TOURNAMENT".equalsIgnoreCase(event.getEntityType()) || "UNMAPPED_TOURNAMENT".equalsIgnoreCase(event.getEntityType())) {
            unmappedTournamentsDetected.incrementAndGet();
            log.info("ℹ️ [Anomaly Agent] Unmapped tournament logged: system={}, rawKey='{}', rawName='{}'",
                    event.getExternalSystemCode(), event.getRawKey(), event.getRawName());
        } else if ("UNMAPPED_TEAM".equalsIgnoreCase(event.getEntityType()) || "TEAM".equalsIgnoreCase(event.getEntityType())) {
            log.warn("🚨 [Anomaly Agent] Unmapped team detected: team='{}', league='{}', sport='{}', details='{}'",
                    event.getRawName(), event.getRawCategory(), event.getRawParentKey(), event.getDetails());
        }
    }

    public boolean resolveScheduleCollision(AnomalyEventDto event) {
        scheduleCollisionsProcessed.incrementAndGet();
        String rawKey = event.getRawKey();
        if (rawKey == null || !rawKey.contains(":")) {
            log.warn("Invalid rawKey for SCHEDULE_COLLISION anomaly: {}", rawKey);
            errorsCount.incrementAndGet();
            return false;
        }

        String[] parts = rawKey.split(":");
        try {
            Long matchId1 = Long.parseLong(parts[0].trim());
            Long matchId2 = Long.parseLong(parts[1].trim());
            String conflictTeam = event.getRawName();

            return resolveCollisionBetweenMatches(matchId1, matchId2, conflictTeam, event.getDetails());
        } catch (Exception e) {
            log.error("Failed to parse collision match IDs from rawKey '{}': {}", rawKey, e.getMessage(), e);
            errorsCount.incrementAndGet();
            return false;
        }
    }

    public boolean resolveCollisionBetweenMatches(Long matchId1, Long matchId2, String conflictTeam, String detailsJson) {
        // 1. Primary Empirical Audit: Invoke Plane AI Worker Pool Synchronously
        Optional<EmpiricalVerdictDto> verdictOpt = empiricalAuditorService.auditCollision(
                matchId1, matchId2, conflictTeam, detailsJson);

        if (verdictOpt.isPresent()) {
            EmpiricalVerdictDto v = verdictOpt.get();
            if (v.getVerdictType() == EmpiricalVerdictType.INCONCLUSIVE) {
                // In-flight or intentionally skipped - DO NOT engage fallback!
                return true;
            }
            empiricalAuditsCount.incrementAndGet();
            switch (v.getVerdictType()) {
                case DUPLICATE_MDM_MATCH -> duplicateMergesCount.incrementAndGet();
                case OUTRIGHT_OR_STATS_TRASH -> trashOutrightsCancelledCount.incrementAndGet();
                case UNMAPPED_TEAM_ALIAS -> unmappedAliasesResolvedCount.incrementAndGet();
                case SYNTHETIC_DUEL -> syntheticMatchesCancelled.incrementAndGet();
                case REAL_SCHEDULE_CONFLICT -> confirmedCollisionsCount.incrementAndGet();
                default -> {}
            }
            log.info("🏆 [Anomaly Agent] Plane AI Empirical Verdict applied successfully: type={}, pod={}, duration={}s",
                    v.getVerdictType(), v.getWorkerPod(), v.getDurationSeconds());
            return true;
        }

        // 2. Fallback: Rule-based heuristic resolution if worker pool was unreachable
        log.warn("⚠️ [Anomaly Agent] Empirical Plane AI audit failed, engaging fallback heuristic arbitration...");
        return resolveCollisionBetweenMatchesFallback(matchId1, matchId2, conflictTeam, detailsJson);
    }

    public boolean resolveCollisionBetweenMatchesFallback(Long matchId1, Long matchId2, String conflictTeam, String detailsJson) {
        log.info("🤖 [Anomaly Agent Fallback] Investigating collision between match #{} and #{} for team '{}'...",
                matchId1, matchId2, conflictTeam);

        Optional<CanonicalMatchHeaderDto> m1Opt = mdmClient.fetchMatch(matchId1);
        Optional<CanonicalMatchHeaderDto> m2Opt = mdmClient.fetchMatch(matchId2);

        if (m1Opt.isEmpty() || m2Opt.isEmpty()) {
            log.warn("Could not retrieve both match headers from MDM: match1_present={}, match2_present={}",
                    m1Opt.isPresent(), m2Opt.isPresent());
            errorsCount.incrementAndGet();
            return false;
        }

        CanonicalMatchHeaderDto m1 = m1Opt.get();
        CanonicalMatchHeaderDto m2 = m2Opt.get();

        if ("CANCELLED".equalsIgnoreCase(m1.getStatus()) || "CANCELLED".equalsIgnoreCase(m2.getStatus())) {
            log.info("ℹ️ [Anomaly Agent Fallback] One of the matches is already CANCELLED in MDM (m1={}, m2={}), collision resolved.",
                    m1.getStatus(), m2.getStatus());
            return true;
        }

        Long winnerId;
        Long loserId;
        String reason;
        Long winnerSofaId = null;

        // Arbitration Rule 1: SofaScore Ground Truth ID
        if (m1.getSofascoreId() != null && m2.getSofascoreId() == null) {
            winnerId = matchId1;
            loserId = matchId2;
            winnerSofaId = m1.getSofascoreId();
            reason = "MATCH_1_HAS_SOFASCORE_GROUND_TRUTH_ID";
        } else if (m2.getSofascoreId() != null && m1.getSofascoreId() == null) {
            winnerId = matchId2;
            loserId = matchId1;
            winnerSofaId = m2.getSofascoreId();
            reason = "MATCH_2_HAS_SOFASCORE_GROUND_TRUTH_ID";
        } else {
            // Arbitration Rule 2: Synthetic / Fantasy League Name
            String league1 = m1.getTournamentName() != null ? m1.getTournamentName() : "";
            String league2 = m2.getTournamentName() != null ? m2.getTournamentName() : "";

            boolean l1Synthetic = SYNTHETIC_LEAGUE_PATTERN.matcher(league1).matches();
            boolean l2Synthetic = SYNTHETIC_LEAGUE_PATTERN.matcher(league2).matches();

            if (l1Synthetic && !l2Synthetic) {
                winnerId = matchId2;
                loserId = matchId1;
                reason = "MATCH_1_SYNTHETIC_LEAGUE_DUEL (" + league1 + ")";
            } else if (l2Synthetic && !l1Synthetic) {
                winnerId = matchId1;
                loserId = matchId2;
                reason = "MATCH_2_SYNTHETIC_LEAGUE_DUEL (" + league2 + ")";
            } else {
                // Arbitration Rule 3: Major Canonical League Prominence
                boolean l1Major = MAJOR_LEAGUE_PATTERN.matcher(league1).matches();
                boolean l2Major = MAJOR_LEAGUE_PATTERN.matcher(league2).matches();

                if (l1Major && !l2Major) {
                    winnerId = matchId1;
                    loserId = matchId2;
                    reason = "MATCH_1_MAJOR_TIER_CANONICAL_LEAGUE (" + league1 + ")";
                } else if (l2Major && !l1Major) {
                    winnerId = matchId2;
                    loserId = matchId1;
                    reason = "MATCH_2_MAJOR_TIER_CANONICAL_LEAGUE (" + league2 + ")";
                } else {
                    // Arbitration Rule 4: Bookmaker Breadth & Chronology
                    int b1 = m1.getBookmakerUrls() != null ? m1.getBookmakerUrls().size() : 0;
                    int b2 = m2.getBookmakerUrls() != null ? m2.getBookmakerUrls().size() : 0;
                    if (b1 > b2) {
                        winnerId = matchId1;
                        loserId = matchId2;
                        reason = "MATCH_1_BOOKMAKER_BREADTH (" + b1 + " vs " + b2 + ")";
                    } else if (b2 > b1) {
                        winnerId = matchId2;
                        loserId = matchId1;
                        reason = "MATCH_2_BOOKMAKER_BREADTH (" + b2 + " vs " + b1 + ")";
                    } else {
                        // Chronological canonical fallback: earlier match ID is canonical!
                        winnerId = Math.min(matchId1, matchId2);
                        loserId = Math.max(matchId1, matchId2);
                        reason = "MATCH_CANONICAL_CHRONOLOGY (earlier match #" + winnerId + " retained)";
                    }
                }
            }
        }

        log.warn("🎯 [Anomaly Agent Verdict Fallback] Winner: Match #{}, Loser: Match #{}. Rationale: {}",
                winnerId, loserId, reason);

        // Remediation 1: Cancel the fake/synthetic match in MDM and Analyst
        boolean mdmCancelOk = mdmClient.cancelMatch(loserId, reason);
        boolean analystCancelOk = aiAnalystClient.resolveConflict(loserId, "CANCEL", reason, winnerId);
        if (mdmCancelOk || analystCancelOk) {
            syntheticMatchesCancelled.incrementAndGet();
        }

        // Remediation 2: Promote and re-evaluate the genuine match in MDM and Analyst
        boolean mdmPromoteOk = mdmClient.promoteGroundTruth(winnerId, winnerSofaId, "SOFASCORE", "GROUND_TRUTH_VERIFIED");
        boolean analystPromoteOk = aiAnalystClient.resolveConflict(winnerId, "PROMOTE", "GROUND_TRUTH_VERIFIED", null);
        if (mdmPromoteOk || analystPromoteOk) {
            groundTruthMatchesPromoted.incrementAndGet();
        }

        return true;
    }

    public AgentStatsDto getStats() {
        var poolSummary = workerPoolDispatcher.getPoolStatusSummary();
        int totalWorkers = (int) poolSummary.getOrDefault("totalConfigured", 18);
        int onlineWorkers = ((Long) poolSummary.getOrDefault("onlineWorkers", 0L)).intValue();

        return AgentStatsDto.builder()
                .totalEventsReceived(totalEventsReceived.get())
                .scheduleCollisionsProcessed(scheduleCollisionsProcessed.get())
                .empiricalAuditsCount(empiricalAuditsCount.get())
                .duplicateMergesCount(duplicateMergesCount.get())
                .trashOutrightsCancelledCount(trashOutrightsCancelledCount.get())
                .unmappedAliasesResolvedCount(unmappedAliasesResolvedCount.get())
                .syntheticMatchesCancelled(syntheticMatchesCancelled.get())
                .groundTruthMatchesPromoted(groundTruthMatchesPromoted.get())
                .confirmedCollisionsCount(confirmedCollisionsCount.get())
                .unmappedTournamentsDetected(unmappedTournamentsDetected.get())
                .errorsCount(errorsCount.get())
                .planeWorkerPoolTotal(totalWorkers)
                .planeWorkerPoolHealthy(onlineWorkers)
                .lastActiveTimestamp(lastActiveTimestamp)
                .build();
    }
}
