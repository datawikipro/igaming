package pro.datawiki.igaming.analyst.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.service.AiAnalystOrchestrator;
import pro.datawiki.igaming.analyst.service.MatchDossierService;
import pro.datawiki.igaming.analyst.service.RedisOddsConsensusService;
import pro.datawiki.igaming.analyst.state.RecommendationStateMachine;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.Language;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dedicated scheduling & background coordination layer:
 * - Rolling 150 slot queue scanner
 * - Kickoff in-play watchdog (minute 0-2 verification)
 * - MDM / Redis live schedule synchronization
 * - Available bookmakers cache backfill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalystScheduler {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final MatchDossierService dossierService;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final RecommendationStateMachine stateMachine;
    private final AiAnalystOrchestrator orchestrator;
    private final pro.datawiki.igaming.analyst.service.MatchSettlementService settlementService;
    private final ObjectMapper objectMapper;

    @Value("${analyst.scheduling.backfill-enabled:true}")
    private boolean backfillEnabled;

    @Value("${analyst.scheduling.backfill-batch-size:50}")
    private int backfillBatchSize;

    private final java.util.concurrent.atomic.AtomicBoolean isBackfillRunning = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static final List<String> NON_PENDING_OR_SETTLEMENT_STAGES = List.of(
            "SETTLED", "AWAITING_SETTLEMENT", "CANCELLED",
            "CANCELLED_SCHEDULE_CONFLICT", "CANCELLED_SYNTHETIC_COLLISION",
            "SCHEDULE_CONFLICT_PENDING_RESOLUTION", "SKIPPED", "SKIPPED_NO_LINEUP", "FINAL_PASS"
    );


    @Value("${analyst.scheduling.batch-size:50}")
    private int configuredBatchSize;

    private final ExecutorService evaluationExecutor = Executors.newFixedThreadPool(24);

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(this::backfillAvailableBookmakers, evaluationExecutor);
    }

    @PreDestroy
    public void destroy() {
        try {
            evaluationExecutor.shutdown();
        } catch (Exception ignored) {}
    }

    /**
     * Periodic scanner for upcoming matches across all supported sports.
     * Enforces the Rolling Slot Queue limit of at most 150 active in-flight matches.
     * Dispatches evaluations concurrently across the worker pool for maximum throughput.
     */
    @Scheduled(cron = "${analyst.scheduling.scan-cron:0 */2 * * * *}")
    public void scanUpcomingMatches() {
        int batchSize = configuredBatchSize;
        log.info("Scanning for upcoming multi-sport matches (batchSize: {})...", batchSize);
        List<Long> upcoming = dossierService.findUpcomingMultiSportMatchesForEvaluation(batchSize);
        log.info("Found {} upcoming matches for AI evaluation.", upcoming.size());

        if (upcoming.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Long matchId : upcoming) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    orchestrator.evaluateMatch(matchId);
                } catch (Exception e) {
                    log.error("Failed to evaluate matchId={}: {}", matchId, e.getMessage());
                }
            }, evaluationExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed parallel batch evaluation of {} matches.", upcoming.size());
    }

    /**
     * Kickoff In-Play Lineup Verification (Minute 0-2).
     * Runs every 30 seconds to catch matches right as the referee blows the whistle.
     */
    @Scheduled(fixedDelay = 30000)
    public void scanKickoffInPlayMatches() {
        long nowMs = System.currentTimeMillis();
        List<MatchAiRecommendation> kickoffWaiters = recommendationRepository.findByEvaluationStage("WAIT_KICKOFF");

        if (kickoffWaiters != null && !kickoffWaiters.isEmpty()) {
            for (MatchAiRecommendation rec : kickoffWaiters) {
                Long matchId = rec.getMatchId();
                if (matchId == null) continue;
                LocalDateTime startLdt = rec.getMatchStartTime();
                if (startLdt == null) continue;
                long startMs = startLdt.toInstant(ZoneOffset.UTC).toEpochMilli();
                long diffMinutes = (startMs - nowMs) / (60L * 1000L);

                // Phase 1: Pre-Kickoff High-Frequency Watchdog (T-45 to T-1)
                if (diffMinutes > 1) {
                    boolean confirmed = dossierService.checkOrRefreshLineupsOnDemand(matchId);
                    if (confirmed) {
                        log.info("Official pre-match protocol arrived at T-{}m for match #{}! Immediate re-evaluation with full match sheet...", diffMinutes, matchId);
                        orchestrator.evaluateMatch(matchId);
                    }
                    continue;
                }

                // If match is overdue by > 6 minutes and still in WAITING:
                if (diffMinutes < -6) {
                    log.info("Match #{} is 6+ minutes past start time. Triggering evaluation with available parameters.", matchId);
                    orchestrator.evaluateMatch(matchId);
                    continue;
                }

                // Kickoff window [now - 6m ... now + 1m]
                try {
                    log.info("Kickoff window reached for match #{} (diffMinutes={}). Triggering on-pitch lineup verification.", matchId, diffMinutes);
                    orchestrator.evaluateMatch(matchId);
                } catch (Exception e) {
                    log.warn("Error during kickoff evaluation of match #{}: {}", matchId, e.getMessage());
                }
            }
        }

        // Phase 2: Live In-Play Transition Watchdog via State Machine
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        List<MatchAiRecommendation> liveTransitionPending = recommendationRepository.findPendingNeedingLiveTransition(nowUtc);

        if (liveTransitionPending != null && !liveTransitionPending.isEmpty()) {
            Instant nowInstant = Instant.ofEpochMilli(nowMs);
            for (MatchAiRecommendation rec : liveTransitionPending) {
                try {
                    boolean changed = stateMachine.advanceLifecycle(rec, nowInstant);
                    if (changed) {
                        recommendationRepository.save(rec);
                        log.info("Transitioned match #{} ({}-{}) to {}", rec.getMatchId(), rec.getTeam1Name(), rec.getTeam2Name(), rec.getEvaluationStage());
                    }
                } catch (Exception e) {
                    log.warn("Error transitioning match #{} to live: {}", rec.getMatchId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Synchronizes schedule and canonical team names of active recommendations with MDM ground truth.
     */
    @Scheduled(cron = "${analyst.scheduling.sync-schedule-cron:0 */2 * * * *}")
    public void syncActiveRecommendationsSchedule() {
        try {
            List<MatchAiRecommendation> pendingRecs = recommendationRepository.findByEvaluationStageNotInOrderByIdDesc(NON_PENDING_OR_SETTLEMENT_STAGES);
            if (pendingRecs == null || pendingRecs.isEmpty()) return;
            long nowEpochSec = Instant.now().getEpochSecond();

            for (MatchAiRecommendation rec : pendingRecs) {
                Long matchId = rec.getMatchId();
                if (matchId == null) continue;
                CanonicalMatchHeaderDto header = dossierService.fetchMdmMatchHeader(matchId);
                if (header == null || header.getStartTimeEpochSec() == null) continue;
                long mdmStartEpochSec = header.getStartTimeEpochSec();
                LocalDateTime mdmStartTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(mdmStartEpochSec), ZoneOffset.UTC);
                boolean updated = false;

                // 1. Synchronize match start time if drifted by > 300 seconds
                if (rec.getMatchStartTime() == null || Math.abs(rec.getMatchStartTime().toEpochSecond(ZoneOffset.UTC) - mdmStartEpochSec) > 300) {
                    log.info("⏰ [AI-Analyst] Schedule synchronization for match #{} ({}-{}): updated start time from {} to {}",
                            matchId, rec.getTeam1Name(), rec.getTeam2Name(), rec.getMatchStartTime(), mdmStartTime);
                    rec.setMatchStartTime(mdmStartTime);
                    updated = true;

                    if ("IN_PLAY".equals(rec.getEvaluationStage()) && mdmStartEpochSec > nowEpochSec) {
                        log.info("⏪ [AI-Analyst] Reverting premature IN_PLAY stage for match #{} back to PRE_LINEUP (kickoff is in future at {})",
                                matchId, mdmStartTime);
                        rec.setEvaluationStage("PRE_LINEUP");
                        if (rec.getCoreHypothesis() != null) {
                            rec.setCoreHypothesis(rec.getCoreHypothesis()
                                    .replace("🟢 СТАРТОВЫЙ РАСЧЕТ (По свистку и протоколам): ", "")
                                    .replace("🔴 LIVE / IN-PLAY (В игре): ", ""));
                        }
                        if (rec.getIdentifiedRisk() != null && rec.getIdentifiedRisk().contains("In-Play")) {
                            rec.setIdentifiedRisk("Волатильность коэффициента на отрезке до матча и возможные тактические паузы.");
                        }
                    }
                }

                // 2. Synchronize canonical team names
                if (header.getHomeTeamName() != null && !header.getHomeTeamName().isBlank()
                        && !header.getHomeTeamName().equals(rec.getTeam1Name())) {
                    log.info("🏷️ [AI-Analyst] Team name synchronization for match #{}: home team updated from '{}' to '{}'",
                            matchId, rec.getTeam1Name(), header.getHomeTeamName());
                    rec.setTeam1Name(header.getHomeTeamName());
                    updated = true;
                }

                if (header.getAwayTeamName() != null && !header.getAwayTeamName().isBlank()
                        && !header.getAwayTeamName().equals(rec.getTeam2Name())) {
                    log.info("🏷️ [AI-Analyst] Team name synchronization for match #{}: away team updated from '{}' to '{}'",
                            matchId, rec.getTeam2Name(), header.getAwayTeamName());
                    rec.setTeam2Name(header.getAwayTeamName());
                    updated = true;
                }

                // 3. Dynamic Bookmaker Consensus Sync
                Set<String> currentBks = redisOddsConsensusService.getAvailableBookmakers(matchId);
                String bksStr = (currentBks != null && !currentBks.isEmpty()) ? String.join(",", currentBks) : "";
                if (!Objects.equals(bksStr, rec.getAvailableBookmakers())) {
                    rec.setAvailableBookmakers(bksStr);
                    updated = true;
                }

                Map<String, String> currentUrls = redisOddsConsensusService.getBookmakerUrls(matchId, rec.getSportSlug());
                String urlsJson = (currentUrls != null && !currentUrls.isEmpty()) ? objectMapper.writeValueAsString(currentUrls) : null;
                if (!Objects.equals(urlsJson, rec.getBookmakerUrlsJson())) {
                    rec.setBookmakerUrlsJson(urlsJson);
                    updated = true;
                }

                if (updated) {
                    String slug = SlugUtils.buildMatchSlug(matchId, rec.getTeam1Name(), rec.getTeam2Name());
                    rec.setMatchSlug(slug);
                    rec.setMatchUrl(SlugUtils.buildMatchUrl(matchId, rec.getTeam1Name(), rec.getTeam2Name(), Language.RU));
                    rec.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    recommendationRepository.save(rec);
                }
            }
        } catch (Exception e) {
            log.warn("Error during active recommendations schedule synchronization: {}", e.getMessage());
        }
    }

    /**
     * Continuous historical backfill worker:
     * Re-evaluates matches from earlier model versions under CURRENT_MODEL_VERSION
     * so we get a continuous side-by-side A/B delta ("было / стало") without losing history.
     */
    @Scheduled(fixedDelayString = "${analyst.scheduling.backfill-delay-ms:5000}")
    public void backfillHistoricalMatchesUnderCurrentVersion() {
        if (!backfillEnabled) {
            return;
        }
        if (!isBackfillRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            List<Long> candidateMatchIds = recommendationRepository.findMatchIdsNeedingVersionBackfill(
                    AiAnalystOrchestrator.CURRENT_MODEL_VERSION, backfillBatchSize
            );
            if (candidateMatchIds == null || candidateMatchIds.isEmpty()) {
                return;
            }
            log.info("Historical Backfill: Re-evaluating {} matches under {}...", candidateMatchIds.size(), AiAnalystOrchestrator.CURRENT_MODEL_VERSION);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Long matchId : candidateMatchIds) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Optional<MatchAiRecommendation> evalOpt = orchestrator.forceEvaluateMatch(matchId);
                        if (evalOpt.isPresent()) {
                            MatchAiRecommendation saved = evalOpt.get();
                            settlementService.settleSingleRecommendation(saved);
                        }
                    } catch (Exception e) {
                        log.debug("Backfill failed for match #{}: {}", matchId, e.getMessage());
                    }
                }, evaluationExecutor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("Historical Backfill: Successfully re-evaluated and settled batch of {} matches.", candidateMatchIds.size());
        } catch (Exception e) {
            log.error("Historical Backfill tick failed: {}", e.getMessage());
        } finally {
            isBackfillRunning.set(false);
        }
    }

    public void backfillAvailableBookmakers() {
        try {
            List<MatchAiRecommendation> pending = recommendationRepository.findByEvaluationStageNotIn(NON_PENDING_OR_SETTLEMENT_STAGES);
            int updated = 0;
            for (MatchAiRecommendation rec : pending) {
                if (rec.getMatchId() != null) {
                    boolean changed = false;
                    Set<String> bks = redisOddsConsensusService.getAvailableBookmakers(rec.getMatchId());
                    String bksJoined = (bks != null && !bks.isEmpty()) ? String.join(",", bks) : null;
                    if (!Objects.equals(bksJoined, rec.getAvailableBookmakers())) {
                        rec.setAvailableBookmakers(bksJoined);
                        changed = true;
                    }

                    Map<String, String> urls = redisOddsConsensusService.getBookmakerUrls(rec.getMatchId(), rec.getSportSlug());
                    String urlsJson = (urls != null && !urls.isEmpty()) ? objectMapper.writeValueAsString(urls) : null;
                    if (!Objects.equals(urlsJson, rec.getBookmakerUrlsJson())) {
                        rec.setBookmakerUrlsJson(urlsJson);
                        changed = true;
                    }

                    if (changed) {
                        recommendationRepository.save(rec);
                        updated++;
                    }
                }
            }
            log.info("Backfilled available bookmakers for {} / {} active recommendations", updated, pending.size());
        } catch (Exception e) {
            log.warn("Error in backfillAvailableBookmakers: {}", e.getMessage());
        }
    }
}
