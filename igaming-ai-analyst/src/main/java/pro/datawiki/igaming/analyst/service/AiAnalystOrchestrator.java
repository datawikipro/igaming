package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.AiAnalysisResultDto;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiLearningMemoryRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalystOrchestrator {

    private final MatchDossierService dossierService;
    private final AgyWorkerClient agyWorkerClient;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiLearningMemoryRepository memoryRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:AGENTS_ANALYST.md")
    private Resource constitutionResource;

    private String analystConstitution = "";

    @Value("${analyst.scheduling.max-slots:150}")
    private int maxSlots;

    @Value("${analyst.scheduling.batch-size:20}")
    private int configuredBatchSize;

    private final ExecutorService evaluationExecutor = Executors.newFixedThreadPool(24);

    @PostConstruct
    public void init() {
        try {
            if (constitutionResource != null && constitutionResource.exists()) {
                try (InputStream is = constitutionResource.getInputStream()) {
                    analystConstitution = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load AGENTS_ANALYST.md from classpath: {}. Using default fallback.", e.getMessage());
            analystConstitution = "You are a quantitative sports betting analyst adhering to Zero-Odds Protocol.";
        }
    }

    /**
     * Evaluates a specific match and stores the recommendation in PostgreSQL.
     */
    @Transactional
    public Optional<MatchAiRecommendation> evaluateMatch(Long matchId) {
        log.info("Evaluating match #{} with AI Quantitative Analyst...", matchId);

        String dossierPrompt = dossierService.getFormattedPrompt(matchId);
        if (dossierPrompt == null || dossierPrompt.startsWith("MATCH_NOT_FOUND")) {
            log.warn("Match dossier not found for matchId={}", matchId);
            return Optional.empty();
        }

        Map<String, Object> dossierMeta = dossierService.getMatchDossier(matchId);

        String sportName = dossierMeta != null ? (String) dossierMeta.get("sport_name") : null;
        String league = dossierMeta != null ? (String) dossierMeta.get("league_name") : null;
        if (league != null) {
            String lLower = league.toLowerCase();
            if (lLower.contains("tt elite") || lLower.contains("setka cup") || lLower.contains("настольный теннис") || lLower.contains("table tennis")) {
                sportName = "TABLE_TENNIS";
            } else if (lLower.contains("cricket") || lLower.contains("крикет") || lLower.contains("t20")) {
                sportName = "CRICKET";
            } else if (lLower.contains("volleyball") || lLower.contains("волейбол") || lLower.contains("воллей лига")) {
                sportName = "VOLLEYBALL";
            }
        }
        Sport sport = Sports.get(sportName != null ? sportName : "FOOTBALL");

        // Retrieve relevant lessons from persistent AI learning memory using sport taxonomy scopes
        String team1 = dossierMeta != null ? (String) dossierMeta.get("team1_name") : null;
        String team2 = dossierMeta != null ? (String) dossierMeta.get("team2_name") : null;

        List<String> teams = new ArrayList<>();
        if (team1 != null) teams.add(team1.toLowerCase());
        if (team2 != null) teams.add(team2.toLowerCase());

        List<String> scopes = (sport != null) ? sport.getMemoryScopes() : List.of("GLOBAL");
        List<AiLearningMemory> memories = teams.isEmpty()
                ? memoryRepository.findByScopes(scopes)
                : memoryRepository.findRelevantLessons(teams, league != null ? league : "", scopes);

        if (!memories.isEmpty()) {
            StringBuilder sb = new StringBuilder("\n\n### AI LEARNING MEMORY (HISTORICAL TACTICAL LESSONS):\n");
            for (AiLearningMemory mem : memories) {
                sb.append(String.format("- [%s / %s / scope=%s] %s (Category: %s, Source Match #%d)\n",
                        mem.getTeamName(), mem.getLeagueName(), mem.getSportScope(), mem.getLessonText(), mem.getContextCategory(), mem.getSourceMatchId()));
            }
            sb.append("You MUST consider these historical patterns and tactical lessons when formulating the core hypothesis and contingency shadow picks!\n");
            dossierPrompt = dossierPrompt + sb.toString();
        }

        // Inject sport-specific quantitative and tactical protocol directly from the Sport domain
        if (sport != null && sport.getTacticalRulesPrompt() != null) {
            dossierPrompt = dossierPrompt + "\n\n### SPORT TACTICAL PROTOCOL (" + sport.getCode() + "):\n" + sport.getTacticalRulesPrompt();
        }

        Optional<AiAnalysisResultDto> resultOpt = agyWorkerClient.evaluateMatch(matchId, dossierPrompt, analystConstitution);
        if (resultOpt.isEmpty()) {
            log.warn("Worker did not produce recommendation for matchId={}", matchId);
            return Optional.empty();
        }

        AiAnalysisResultDto dto = resultOpt.get();

        // Convert or update existing recommendation
        MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                .orElse(new MatchAiRecommendation());

        rec.setMatchId(matchId);
        if (dossierMeta != null) {
            rec.setTeam1Name((String) dossierMeta.get("team1_name"));
            rec.setTeam2Name((String) dossierMeta.get("team2_name"));
            rec.setLeagueName((String) dossierMeta.get("league_name"));
            Long startTs = (Long) dossierMeta.get("start_time");
            if (startTs != null) {
                rec.setMatchStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTs), ZoneOffset.UTC));
            }
            Boolean lineConfirmed = (Boolean) dossierMeta.get("is_lineup_confirmed");
            rec.setLineupStatus(Boolean.TRUE.equals(lineConfirmed) ? "CONFIRMED" : "ESTIMATED");
        }

        rec.setExpectedXg(dto.getExpectedXg());
        rec.setFairP1(dto.getFairP1());
        rec.setFairPx(dto.getFairPx());
        rec.setFairP2(dto.getFairP2());
        rec.setFairOver25(dto.getFairOver25());
        rec.setFairUnder25(dto.getFairUnder25());
        rec.setFairBttsYes(dto.getFairBttsYes());
        rec.setFairBttsNo(dto.getFairBttsNo());

        // Conviction Tier Handling & The Right to PASS (No forced picks)
        String tier = dto.getConvictionTier();
        if (tier == null || tier.isBlank()) {
            tier = "GOOD_PICK";
        } else {
            tier = tier.trim().toUpperCase();
        }

        if ("PASS".equals(tier) || "NO_BET".equals(tier) || "ABSTAIN".equals(tier)
                || "PASS".equalsIgnoreCase(dto.getRecommendedBet()) || "NO_BET".equalsIgnoreCase(dto.getRecommendedBet())) {
            log.info("Analyst decided to PASS match #{} ({}-{}): Reason='{}', Risk='{}'",
                    matchId,
                    dossierMeta != null ? dossierMeta.get("team1_name") : "T1",
                    dossierMeta != null ? dossierMeta.get("team2_name") : "T2",
                    dto.getCoreHypothesis(), dto.getIdentifiedRisk());
            return Optional.empty();
        }

        if (!"CONFIDENT".equals(tier) && !"GOOD_PICK".equals(tier)) {
            tier = "GOOD_PICK";
        }
        rec.setConvictionTier(tier);

        String targetMarket = dto.getRecommendedBet();
        if (targetMarket == null || targetMarket.isBlank()) {
            targetMarket = dto.getMarketType();
        }
        if (sport != null && !sport.isMarketSupported(targetMarket)) {
            log.warn("Market '{}' is invalid for sport '{}'. Re-routing to supported fallback.", targetMarket, sport.getCode());
            if ("ICE_HOCKEY".equals(sport.getCode()) || "HOCKEY".equals(sport.getCode())) {
                targetMarket = (targetMarket.contains("2") || targetMarket.contains("AWAY")) ? "AH_AWAY_PLUS_1.5" : "AH_HOME_PLUS_1.5";
            } else if (!sport.hasDrawOutcome()) {
                targetMarket = (targetMarket.contains("2") || targetMarket.contains("AWAY")) ? "P2" : "P1";
            } else {
                targetMarket = (targetMarket.contains("2") || targetMarket.contains("AWAY")) ? "DOUBLE_CHANCE_X2" : "DOUBLE_CHANCE_1X";
            }
        }
        if (targetMarket == null || targetMarket.isBlank()) {
            targetMarket = (sport != null && !sport.hasDrawOutcome()) ? "P1" : "DOUBLE_CHANCE_1X";
        }

        // In Tier 2 ("GOOD_PICK"), protect against close-match variance:
        // In solo/racket sports (tennis, table tennis), auto-cushion raw P1/P2 to AH +1.5 sets
        if ("GOOD_PICK".equals(tier) && sport != null && !sport.isTeamSport()) {
            if ("P1".equals(targetMarket) || "HOME_WIN".equals(targetMarket)) {
                targetMarket = "AH_HOME_PLUS_1.5";
                log.info("Good Pick Tier: converted raw P1 to cushioned AH_HOME_PLUS_1.5 for match #{}", matchId);
            } else if ("P2".equals(targetMarket) || "AWAY_WIN".equals(targetMarket)) {
                targetMarket = "AH_AWAY_PLUS_1.5";
                log.info("Good Pick Tier: converted raw P2 to cushioned AH_AWAY_PLUS_1.5 for match #{}", matchId);
            }
        }
        rec.setRecommendedMarket(targetMarket);

        BigDecimal marketOdd = dto.getMarketOdd();
        if (marketOdd == null || marketOdd.compareTo(BigDecimal.ONE) <= 0) {
            marketOdd = BigDecimal.valueOf(1.42);
        }
        rec.setRecommendedOdd(marketOdd);
        rec.setBestBookmaker(dto.getBestBookmaker());
        rec.setExpectedValuePct(dto.getExpectedValuePct());
        rec.setKellyStakePct(dto.getKellyStakePct());

        int confidence = dto.getConfidenceScore() != null ? dto.getConfidenceScore() : 80;
        // Stake and conviction calibration
        if ("GOOD_PICK".equals(tier)) {
            // Good pick has identified risks: reduce Kelly stake to 50% for capital defense
            if (rec.getKellyStakePct() != null) {
                rec.setKellyStakePct(rec.getKellyStakePct().multiply(BigDecimal.valueOf(0.5)));
            }
        } else if ("CONFIDENT".equals(tier)) {
            if (confidence < 85) {
                confidence = 85;
            }
        }
        // Solo/racket sport discipline: in tennis/ping pong, odds > 1.58 have elevated upset variance
        if (sport != null && !sport.isTeamSport()) {
            if (marketOdd.compareTo(BigDecimal.valueOf(1.58)) > 0 && confidence < 86) {
                log.info("Solo sport upset risk discipline: match #{} (odds: {}, conf: {}) - halving Kelly stake for bankroll defense",
                        matchId, marketOdd, confidence);
                if (rec.getKellyStakePct() != null) {
                    rec.setKellyStakePct(rec.getKellyStakePct().multiply(BigDecimal.valueOf(0.5)));
                }
            }
        }
        rec.setConfidenceScore(confidence);
        rec.setTacticalRationale(dto.getTacticalRationale());
        rec.setSteamDirection(dto.getSteamDirection() != null ? dto.getSteamDirection() : "NEUTRAL");
        rec.setResultStatus("PENDING");

        // Scenario hierarchy & contingency stress-testing
        rec.setCoreHypothesis(dto.getCoreHypothesis());
        rec.setIdentifiedRisk(dto.getIdentifiedRisk());
        if (dto.getShadowPicks() != null && !dto.getShadowPicks().isEmpty()) {
            try {
                rec.setShadowPicksJson(objectMapper.writeValueAsString(dto.getShadowPicks()));
            } catch (Exception e) {
                log.warn("Failed to serialize shadow picks for matchId={}: {}", matchId, e.getMessage());
            }
        }

        MatchAiRecommendation saved = recommendationRepository.save(rec);
        log.info("Saved AI recommendation for match #{} ({}-{}): Bet='{}' @ {}, Risk='{}'",
                matchId, rec.getTeam1Name(), rec.getTeam2Name(), rec.getRecommendedMarket(), rec.getRecommendedOdd(), rec.getIdentifiedRisk());

        return Optional.of(saved);
    }

    @PreDestroy
    public void destroy() {
        try {
            evaluationExecutor.shutdown();
        } catch (Exception ignored) {
        }
    }

    /**
     * Periodic scanner for upcoming matches across all supported sports.
     * Enforces the Rolling Slot Queue limit of at most 150 active in-flight matches.
     * Dispatches evaluations concurrently across the worker pool for maximum throughput.
     * Scales system throughput to 400+ match evaluations per 24 hours.
     */
    @Scheduled(cron = "${analyst.scheduling.scan-cron:0 */2 * * * *}")
    public void scanUpcomingMatches() {
        long pendingCount = recommendationRepository.countPending();
        int availableSlots = maxSlots - (int) pendingCount;

        if (availableSlots <= 0) {
            log.info("Queue is FULL ({} active matches in flight, max {}). Throttling evaluation until matches finish.",
                    pendingCount, maxSlots);
            return;
        }

        int batchSize = Math.min(availableSlots, configuredBatchSize);
        log.info("Scanning for upcoming multi-sport matches (active: {}, available slots: {}, batchSize: {})...",
                pendingCount, availableSlots, batchSize);
        List<Long> upcoming = dossierService.findUpcomingMultiSportMatchesForEvaluation(batchSize);
        log.info("Found {} upcoming matches for AI evaluation.", upcoming.size());

        if (upcoming.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Long matchId : upcoming) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    evaluateMatch(matchId);
                } catch (Exception e) {
                    log.error("Failed to evaluate matchId={}: {}", matchId, e.getMessage());
                }
            }, evaluationExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed parallel batch evaluation of {} matches.", upcoming.size());
    }

    /**
     * Computes performance statistics across all recommendations.
     */
    public AnalystStatsDto getPerformanceStats() {
        long total = recommendationRepository.countTotalRecommendations();
        long won = recommendationRepository.countWon();
        long lost = recommendationRepository.countLost();
        long voidCount = recommendationRepository.countVoid();
        long pending = recommendationRepository.countPending();
        long settled = won + lost + voidCount;

        double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;
        BigDecimal totalPnl = recommendationRepository.sumActualPnl();
        double roi = settled > 0 ? (totalPnl.doubleValue() / (settled * 1000.0)) * 100.0 : 0.0;

        return AnalystStatsDto.builder()
                .totalEvaluations(total)
                .totalSettled(settled)
                .won(won)
                .lost(lost)
                .voidCount(voidCount)
                .pending(pending)
                .winratePct(Math.round(winrate * 10.0) / 10.0)
                .totalPnl(totalPnl)
                .roiPct(Math.round(roi * 10.0) / 10.0)
                .build();
    }
}
