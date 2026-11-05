package pro.datawiki.igaming.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.agent.client.AiAnalystClient;
import pro.datawiki.igaming.agent.client.MdmClient;
import pro.datawiki.igaming.agent.dto.EmpiricalVerdictDto;
import pro.datawiki.igaming.agent.dto.EmpiricalVerdictType;
import pro.datawiki.igaming.agent.dto.WorkerGenerateResponse;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.signature.CyberMatchDetector;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpiricalAnomalyAuditorService {

    private final MdmClient mdmClient;
    private final AiAnalystClient aiAnalystClient;
    private final PlaneAiWorkerPoolDispatcher workerPoolDispatcher;
    private final EmpiricalPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    private final Set<String> inFlightAudits = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final ExecutorService sweepExecutor = Executors.newFixedThreadPool(12);

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    private static final Pattern STATS_MARKET_PATTERN = Pattern.compile(
            "(?ui)(?:\\((?:офсайды|удары\\s*от\\s*ворот|видеопросмотры|выход\\s*мед|пен\\.|жк|фолы|угловые|броски\\s*в\\s*створ|штрафное\\s*время|эйсы|двойные\\s*ошибки)\\)|^(?:жк|угл|фол)\\s+)"
    );

    /**
     * Conducts a full empirical audit of a collision between two matches via the Plane AI worker pool.
     * Executes synchronously, verifies worker status == "ok", parses the verdict, and triggers remediation.
     */
    public Optional<EmpiricalVerdictDto> auditCollision(Long matchId1, Long matchId2, String conflictTeam, String details) {
        String auditKey = Math.min(matchId1, matchId2) + ":" + Math.max(matchId1, matchId2);
        if (!inFlightAudits.add(auditKey)) {
            log.info("⏩ [Empirical Auditor] Collision audit for pair {} is already in-flight, skipping duplicate trigger", auditKey);
            return Optional.of(EmpiricalVerdictDto.builder()
                    .verdictType(EmpiricalVerdictType.INCONCLUSIVE)
                    .confidence(0.0)
                    .rationale("Audit already in-flight for pair " + auditKey)
                    .build());
        }

        try {
            log.info("🔍 [Empirical Auditor] Starting empirical investigation: Match #{} vs #{} for team '{}'",
                    matchId1, matchId2, conflictTeam);

            Optional<CanonicalMatchHeaderDto> m1Opt = mdmClient.fetchMatch(matchId1);
            Optional<CanonicalMatchHeaderDto> m2Opt = mdmClient.fetchMatch(matchId2);

            if (m1Opt.isEmpty() || m2Opt.isEmpty()) {
                log.warn("❌ [Empirical Auditor] Cannot proceed: match header missing in MDM (m1={}, m2={})",
                        m1Opt.isPresent(), m2Opt.isPresent());
                return Optional.empty();
            }

            CanonicalMatchHeaderDto m1 = m1Opt.get();
            CanonicalMatchHeaderDto m2 = m2Opt.get();

            // Check if either match is already cancelled in MDM
            if ("CANCELLED".equalsIgnoreCase(m1.getStatus()) || "CANCELLED".equalsIgnoreCase(m2.getStatus())) {
                Long winnerId = "CANCELLED".equalsIgnoreCase(m1.getStatus()) ? matchId2 : matchId1;
                Long loserId = "CANCELLED".equalsIgnoreCase(m1.getStatus()) ? matchId1 : matchId2;
                log.info("ℹ️ [Empirical Auditor] Match #{} is already CANCELLED in MDM, collision with winner #{} is already resolved",
                        loserId, winnerId);
                return Optional.of(EmpiricalVerdictDto.builder()
                        .verdictType(EmpiricalVerdictType.DUPLICATE_MDM_MATCH)
                        .winnerMatchId(winnerId)
                        .loserMatchId(loserId)
                        .confidence(1.0)
                        .rationale("Match #" + loserId + " was already marked CANCELLED in MDM")
                        .build());
            }

            // Fast Heuristic Filter: stats markets, cyber duel, or definitive SofaScore ground-truth duplicate
            Optional<EmpiricalVerdictDto> fastVerdict = evaluateFastHeuristic(m1, m2, matchId1, matchId2);
            if (fastVerdict.isPresent()) {
                EmpiricalVerdictDto verdict = fastVerdict.get();
                applyRemediation(verdict, matchId1, matchId2);
                return Optional.of(verdict);
            }

            String prompt = promptBuilder.buildCollisionAuditPrompt(m1, m2, conflictTeam, details);
            WorkerGenerateResponse workerResp = workerPoolDispatcher.executeEmpiricalPrompt(prompt);

            if (workerResp == null || !"ok".equalsIgnoreCase(workerResp.getStatus()) || workerResp.getResponse() == null) {
                log.warn("⚠️ [Empirical Auditor] Plane AI worker audit failed: {}", workerResp != null ? workerResp.getError() : "null");
                return Optional.empty();
            }

            Optional<EmpiricalVerdictDto> verdictOpt = parseVerdict(workerResp.getResponse());
            if (verdictOpt.isEmpty()) {
                log.warn("⚠️ [Empirical Auditor] Could not parse valid JSON verdict from worker output: {}", workerResp.getResponse());
                return Optional.empty();
            }

            EmpiricalVerdictDto verdict = verdictOpt.get();
            verdict.setWorkerPod(workerResp.getPod());
            verdict.setDurationSeconds(workerResp.getDurationSeconds());

            // Autonomous remediation based on empirical verdict
            applyRemediation(verdict, matchId1, matchId2);

            return Optional.of(verdict);
        } finally {
            inFlightAudits.remove(auditKey);
        }
    }

    private Optional<EmpiricalVerdictDto> parseVerdict(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return Optional.empty();
        }

        String jsonCandidate = rawResponse;
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(rawResponse);
        if (matcher.find()) {
            jsonCandidate = matcher.group(1);
        } else {
            // Find first '{' and last '}'
            int start = rawResponse.indexOf('{');
            int end = rawResponse.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonCandidate = rawResponse.substring(start, end + 1);
            }
        }

        try {
            EmpiricalVerdictDto dto = objectMapper.readValue(jsonCandidate.trim(), EmpiricalVerdictDto.class);
            return Optional.of(dto);
        } catch (Exception e) {
            log.warn("Failed to deserialize EmpiricalVerdictDto JSON: {}. Error: {}", jsonCandidate, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isStatsMarket(CanonicalMatchHeaderDto m) {
        if (m == null) return false;
        Integer sportId = m.getSportId();
        // Dedicated sub-sports (43=corners, 44=hockey shots, 45=yellow cards, 46=fouls, 47=offsides, 48=goal kicks) are legitimate events
        if (sportId != null && (sportId == 43 || sportId == 44 || sportId == 45 || sportId == 46 || sportId == 47 || sportId == 48 || sportId == 49)) {
            return false;
        }
        String h = m.getHomeTeamName() != null ? m.getHomeTeamName() : "";
        String a = m.getAwayTeamName() != null ? m.getAwayTeamName() : "";
        String t = m.getTournamentName() != null ? m.getTournamentName() : "";
        return STATS_MARKET_PATTERN.matcher(h).find()
                || STATS_MARKET_PATTERN.matcher(a).find()
                || STATS_MARKET_PATTERN.matcher(t).find()
                || t.toLowerCase().contains("статистика")
                || t.toLowerCase().contains("statistics");
    }

    private boolean isCyberMatch(CanonicalMatchHeaderDto m) {
        if (m == null) return false;
        return CyberMatchDetector.isCyberMatch(m.getHomeTeamName(), m.getAwayTeamName(), m.getTournamentName());
    }

    private Optional<EmpiricalVerdictDto> evaluateFastHeuristic(CanonicalMatchHeaderDto m1, CanonicalMatchHeaderDto m2, Long matchId1, Long matchId2) {
        // 1. Stats market check
        boolean m1Stats = isStatsMarket(m1);
        boolean m2Stats = isStatsMarket(m2);
        if (m1Stats && !m2Stats) {
            log.info("⚡ [Fast Heuristic] Match #{} is a stats/sub-event market, auto-cancelling in favor of #{}", matchId1, matchId2);
            return Optional.of(EmpiricalVerdictDto.builder()
                    .verdictType(EmpiricalVerdictType.OUTRIGHT_OR_STATS_TRASH)
                    .winnerMatchId(matchId2)
                    .loserMatchId(matchId1)
                    .confidence(0.99)
                    .rationale("Match #" + matchId1 + " is a statistics/sub-event market (" + m1.getHomeTeamName() + " vs " + m1.getAwayTeamName() + ")")
                    .build());
        }
        if (m2Stats && !m1Stats) {
            log.info("⚡ [Fast Heuristic] Match #{} is a stats/sub-event market, auto-cancelling in favor of #{}", matchId2, matchId1);
            return Optional.of(EmpiricalVerdictDto.builder()
                    .verdictType(EmpiricalVerdictType.OUTRIGHT_OR_STATS_TRASH)
                    .winnerMatchId(matchId1)
                    .loserMatchId(matchId2)
                    .confidence(0.99)
                    .rationale("Match #" + matchId2 + " is a statistics/sub-event market (" + m2.getHomeTeamName() + " vs " + m2.getAwayTeamName() + ")")
                    .build());
        }

        // 2. Cyber simulation check
        boolean m1Cyber = isCyberMatch(m1);
        boolean m2Cyber = isCyberMatch(m2);
        if (m1Cyber && !m2Cyber) {
            log.info("⚡ [Fast Heuristic] Match #{} is a cyber simulation, auto-cancelling in favor of #{}", matchId1, matchId2);
            return Optional.of(EmpiricalVerdictDto.builder()
                    .verdictType(EmpiricalVerdictType.SYNTHETIC_DUEL)
                    .winnerMatchId(matchId2)
                    .loserMatchId(matchId1)
                    .confidence(0.98)
                    .rationale("Match #" + matchId1 + " is a cyber simulation (" + m1.getHomeTeamName() + " vs " + m1.getAwayTeamName() + ")")
                    .build());
        }
        if (m2Cyber && !m1Cyber) {
            log.info("⚡ [Fast Heuristic] Match #{} is a cyber simulation, auto-cancelling in favor of #{}", matchId2, matchId1);
            return Optional.of(EmpiricalVerdictDto.builder()
                    .verdictType(EmpiricalVerdictType.SYNTHETIC_DUEL)
                    .winnerMatchId(matchId1)
                    .loserMatchId(matchId2)
                    .confidence(0.98)
                    .rationale("Match #" + matchId2 + " is a cyber simulation (" + m2.getHomeTeamName() + " vs " + m2.getAwayTeamName() + ")")
                    .build());
        }

        // 3. Ground Truth vs Unmapped duplicate check
        if (m1.getStartTimeEpochSec() != null && m2.getStartTimeEpochSec() != null) {
            long timeDiff = Math.abs(m1.getStartTimeEpochSec() - m2.getStartTimeEpochSec());
            if (timeDiff <= 1800) {
                boolean m1HasSofa = m1.getSofascoreId() != null && m1.getSofascoreId() > 0;
                boolean m2HasSofa = m2.getSofascoreId() != null && m2.getSofascoreId() > 0;

                boolean hMatch = SimilarityUtils.areTeamsSimilar(m1.getHomeTeamName(), m2.getHomeTeamName());
                boolean aMatch = SimilarityUtils.areTeamsSimilar(m1.getAwayTeamName(), m2.getAwayTeamName());

                if (m1HasSofa && !m2HasSofa && (hMatch || aMatch)) {
                    log.info("⚡ [Fast Heuristic] Match #{} is verified SofaScore ground truth, merging duplicate #{}", matchId1, matchId2);
                    return Optional.of(EmpiricalVerdictDto.builder()
                            .verdictType(EmpiricalVerdictType.DUPLICATE_MDM_MATCH)
                            .winnerMatchId(matchId1)
                            .loserMatchId(matchId2)
                            .canonicalSofaId(m1.getSofascoreId())
                            .confidence(0.95)
                            .rationale("Match #" + matchId1 + " is verified with SofaScore ID #" + m1.getSofascoreId()
                                    + ", while match #" + matchId2 + " is an unmapped duplicate feed (timeDiff=" + timeDiff + "s)")
                            .build());
                } else if (m2HasSofa && !m1HasSofa && (hMatch || aMatch)) {
                    log.info("⚡ [Fast Heuristic] Match #{} is verified SofaScore ground truth, merging duplicate #{}", matchId2, matchId1);
                    return Optional.of(EmpiricalVerdictDto.builder()
                            .verdictType(EmpiricalVerdictType.DUPLICATE_MDM_MATCH)
                            .winnerMatchId(matchId2)
                            .loserMatchId(matchId1)
                            .canonicalSofaId(m2.getSofascoreId())
                            .confidence(0.95)
                            .rationale("Match #" + matchId2 + " is verified with SofaScore ID #" + m2.getSofascoreId()
                                    + ", while match #" + matchId1 + " is an unmapped duplicate feed (timeDiff=" + timeDiff + "s)")
                            .build());
                } else if (hMatch && aMatch) {
                    Long winner = Math.min(matchId1, matchId2);
                    Long loser = Math.max(matchId1, matchId2);
                    log.info("⚡ [Fast Heuristic] Matches #{} and #{} have identical teams, merging duplicate #{} into #{}",
                            matchId1, matchId2, loser, winner);
                    return Optional.of(EmpiricalVerdictDto.builder()
                            .verdictType(EmpiricalVerdictType.DUPLICATE_MDM_MATCH)
                            .winnerMatchId(winner)
                            .loserMatchId(loser)
                            .confidence(0.92)
                            .rationale("Matches #" + matchId1 + " and #" + matchId2 + " have identical teams scheduled at the same time")
                            .build());
                }
            }
        }

        return Optional.empty();
    }

    private void applyRemediation(EmpiricalVerdictDto verdict, Long originalM1, Long originalM2) {
        EmpiricalVerdictType type = verdict.getVerdictType();
        if (type == null) {
            type = EmpiricalVerdictType.INCONCLUSIVE;
        }

        Long winnerId = verdict.getWinnerMatchId();
        Long loserId = verdict.getLoserMatchId();

        // Default safety fallback if model omitted IDs
        if (winnerId == null || loserId == null) {
            winnerId = originalM1;
            loserId = originalM2;
        }

        log.info("🎯 [Empirical Remediation] Verdict: {}, Winner: #{}, Loser: #{}. Rationale: {}",
                type, winnerId, loserId, verdict.getRationale());

        switch (type) {
            case DUPLICATE_MDM_MATCH:
                // Cancel duplicate match in MDM with clear explanation
                String dupReason = "MERGED_DUPLICATE_MDM: Consolidated into winner match #" + winnerId;
                mdmClient.cancelMatch(loserId, dupReason);
                aiAnalystClient.resolveConflict(loserId, "CANCEL", dupReason, winnerId);

                // Promote and re-evaluate winner
                mdmClient.promoteGroundTruth(winnerId, verdict.getCanonicalSofaId(), "SOFASCORE", "EMPIRICAL_AUDIT_DUPLICATE_MERGE");
                aiAnalystClient.resolveConflict(winnerId, "PROMOTE", "EMPIRICAL_AUDIT_VERIFIED", null);
                aiAnalystClient.triggerEvaluation(winnerId);
                break;

            case OUTRIGHT_OR_STATS_TRASH:
                String trashReason = "CANCELLED_OUTRIGHT_TRASH: Filtered as non-match outright/stats market";
                mdmClient.cancelMatch(loserId, trashReason);
                aiAnalystClient.resolveConflict(loserId, "CANCEL", trashReason, null);
                break;

            case UNMAPPED_TEAM_ALIAS:
                String aliasReason = "MERGED_UNMAPPED_ALIAS: Team alias linked to canonical team #" + verdict.getCanonicalTeamId();
                mdmClient.cancelMatch(loserId, aliasReason);
                if (verdict.getCanonicalTeamId() != null) {
                    Optional<CanonicalMatchHeaderDto> loserHeader = mdmClient.fetchMatch(loserId);
                    if (loserHeader.isPresent()) {
                        Long dupTeamId = loserHeader.get().getHomeTeamId();
                        if (dupTeamId != null && !dupTeamId.equals(verdict.getCanonicalTeamId())) {
                            mdmClient.mergeTeams(verdict.getCanonicalTeamId(), dupTeamId);
                        }
                    }
                }
                aiAnalystClient.resolveConflict(loserId, "CANCEL", aliasReason, winnerId);
                aiAnalystClient.triggerEvaluation(winnerId);
                break;

            case SYNTHETIC_DUEL:
                String synthReason = "CANCELLED_SYNTHETIC_COLLISION: Cyber/synthetic simulation fixture";
                mdmClient.cancelMatch(loserId, synthReason);
                aiAnalystClient.resolveConflict(loserId, "CANCEL", synthReason, winnerId);
                aiAnalystClient.resolveConflict(winnerId, "PROMOTE", "GROUND_TRUTH_REAL_SPORT", null);
                aiAnalystClient.triggerEvaluation(winnerId);
                break;

            case REAL_SCHEDULE_CONFLICT:
                log.warn("⚠️ [Empirical Remediation] Confirmed physical schedule conflict for match #{} vs #{}. Lowering confidence.",
                        winnerId, loserId);
                aiAnalystClient.resolveConflict(loserId, "FLAG_CONFLICT", "CONFIRMED_SCHEDULE_CONFLICT", winnerId);
                break;

            case INCONCLUSIVE:
            default:
                log.warn("ℹ️ [Empirical Remediation] Inconclusive audit for collision #{} vs #{}. Leaving for manual review.",
                        originalM1, originalM2);
                break;
        }
    }

    private static final java.util.regex.Pattern CONFLICT_HYPOTHESIS_PATTERN =
            java.util.regex.Pattern.compile("Команда '([^']+)' задействована в матче #(\\d+) и матче #(\\d+)");

    private record ConflictPair(Long m1, Long m2, String conflictTeam) {}

    public Map<String, Object> sweepAndResolvePendingConflicts(int maxMatchesToAudit) {
        log.info("🧹 [Empirical Auditor] Starting concurrent batch sweep of pending schedule conflicts (limit={})...", maxMatchesToAudit);
        int page = 0;
        int pageSize = 25;
        List<Map<String, Object>> allPending = new ArrayList<>();

        while (allPending.size() < maxMatchesToAudit) {
            List<Map<String, Object>> batch = aiAnalystClient.getPendingConflicts(page, pageSize);
            if (batch == null || batch.isEmpty()) {
                break;
            }
            allPending.addAll(batch);
            page++;
            if (batch.size() < pageSize) {
                break;
            }
        }

        Set<String> processedPairs = new HashSet<>();
        List<ConflictPair> tasks = new ArrayList<>();

        for (Map<String, Object> rec : allPending) {
            String hypothesis = (String) rec.get("coreHypothesis");
            if (hypothesis == null) continue;

            var matcher = CONFLICT_HYPOTHESIS_PATTERN.matcher(hypothesis);
            if (matcher.find()) {
                String conflictTeam = matcher.group(1);
                Long m1 = Long.parseLong(matcher.group(2));
                Long m2 = Long.parseLong(matcher.group(3));

                String pairKey = Math.min(m1, m2) + ":" + Math.max(m1, m2);
                if (processedPairs.add(pairKey)) {
                    tasks.add(new ConflictPair(m1, m2, conflictTeam));
                    if (tasks.size() >= maxMatchesToAudit) break;
                }
            }
        }

        if (tasks.isEmpty()) {
            return Map.of("auditedPairs", 0, "resolvedSuccessfully", 0, "distinctPairsProcessed", 0);
        }

        log.info("🚀 [Conflict Sweep] Running {} conflict audits concurrently across fast heuristics & Plane AI workers...", tasks.size());
        List<CompletableFuture<Boolean>> futures = tasks.stream()
                .map(t -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Optional<EmpiricalVerdictDto> verdictOpt = auditCollision(t.m1(), t.m2(), t.conflictTeam(), null);
                        return verdictOpt.isPresent() && verdictOpt.get().getVerdictType() != null
                                && verdictOpt.get().getVerdictType() != EmpiricalVerdictType.INCONCLUSIVE;
                    } catch (Exception e) {
                        log.warn("Error auditing pair #{} vs #{}: {}", t.m1(), t.m2(), e.getMessage());
                        return false;
                    }
                }, sweepExecutor))
                .toList();

        int successCount = 0;
        for (var f : futures) {
            try {
                if (f.get(120, TimeUnit.SECONDS)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.debug("Timeout or error waiting for audit future: {}", e.getMessage());
            }
        }

        log.info("🏁 [Conflict Sweep] Finished sweep: audited={}, resolvedSuccessfully={}, distinctPairsProcessed={}",
                tasks.size(), successCount, processedPairs.size());

        Map<String, Object> result = new HashMap<>();
        result.put("auditedPairs", tasks.size());
        result.put("resolvedSuccessfully", successCount);
        result.put("distinctPairsProcessed", processedPairs.size());
        return result;
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 45000, initialDelay = 15000)
    public void scheduledPendingConflictsSweep() {
        try {
            sweepAndResolvePendingConflicts(25);
        } catch (Exception e) {
            log.error("Scheduled conflict sweep encountered error: {}", e.getMessage());
        }
    }
}
