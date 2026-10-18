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

        // Retrieve relevant lessons from persistent AI learning memory
        String team1 = dossierMeta != null ? (String) dossierMeta.get("team1_name") : null;
        String team2 = dossierMeta != null ? (String) dossierMeta.get("team2_name") : null;
        String league = dossierMeta != null ? (String) dossierMeta.get("league_name") : null;

        List<String> teams = new ArrayList<>();
        if (team1 != null) teams.add(team1.toLowerCase());
        if (team2 != null) teams.add(team2.toLowerCase());

        List<AiLearningMemory> memories = teams.isEmpty() ? List.of() : memoryRepository.findRelevantLessons(teams, league != null ? league : "");
        if (!memories.isEmpty()) {
            StringBuilder sb = new StringBuilder("\n\n### ПРЕДЫДУЩИЙ ОПЫТ И УРОКИ САМООБУЧЕНИЯ (AI LEARNING MEMORY):\n");
            for (AiLearningMemory mem : memories) {
                sb.append(String.format("- [%s / %s] %s (Категория: %s, Матч #%d)\n",
                        mem.getTeamName(), mem.getLeagueName(), mem.getLessonText(), mem.getContextCategory(), mem.getSourceMatchId()));
            }
            sb.append("Обязательно учти эти исторические паттерны и тактические уязвимости при формировании базовой гипотезы и запасных теневых вариантов!\n");
            dossierPrompt = dossierPrompt + sb.toString();
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

        String targetMarket = dto.getRecommendedBet();
        if (targetMarket == null || targetMarket.isBlank()) {
            targetMarket = dto.getMarketType();
        }
        if (targetMarket == null || targetMarket.isBlank()) {
            targetMarket = "AH_HOME_0";
        }
        rec.setRecommendedMarket(targetMarket);

        BigDecimal marketOdd = dto.getMarketOdd();
        if (marketOdd == null || marketOdd.compareTo(BigDecimal.ONE) <= 0) {
            marketOdd = BigDecimal.valueOf(1.85);
        }
        rec.setRecommendedOdd(marketOdd);
        rec.setBestBookmaker(dto.getBestBookmaker());
        rec.setExpectedValuePct(dto.getExpectedValuePct());
        rec.setKellyStakePct(dto.getKellyStakePct());
        rec.setConfidenceScore(dto.getConfidenceScore());
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

    /**
     * Periodic scanner for upcoming matches in the T-40m window.
     * Enforces the hard Rolling Slot Queue limit of at most 10 active in-flight matches.
     */
    @Scheduled(cron = "${analyst.scheduling.scan-cron:0 */10 * * * *}")
    public void scanUpcomingMatches() {
        long pendingCount = recommendationRepository.countPending();
        int maxSlots = 10;
        int availableSlots = maxSlots - (int) pendingCount;

        if (availableSlots <= 0) {
            log.info("Queue is FULL ({} active matches in flight, max {}). Throttling evaluation until matches finish.",
                    pendingCount, maxSlots);
            return;
        }

        log.info("Scanning for upcoming matches entering T-40m window (active: {}, available slots: {})...",
                pendingCount, availableSlots);
        List<Long> upcoming = dossierService.findUpcomingMatchesForEvaluation(25, 50);
        log.info("Found {} upcoming matches for AI evaluation.", upcoming.size());

        int evaluated = 0;
        for (Long matchId : upcoming) {
            if (evaluated >= availableSlots) {
                log.info("Reached maximum rolling limit of {} in-flight matches. Remaining matches deferred.", maxSlots);
                break;
            }
            try {
                Optional<MatchAiRecommendation> res = evaluateMatch(matchId);
                if (res.isPresent()) {
                    evaluated++;
                }
            } catch (Exception e) {
                log.error("Failed to evaluate matchId={}: {}", matchId, e.getMessage());
            }
        }
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
