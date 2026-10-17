package pro.datawiki.igaming.analyst.service;

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
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

        rec.setRecommendedMarket(dto.getRecommendedBet());
        rec.setRecommendedOdd(dto.getMarketOdd());
        rec.setBestBookmaker(dto.getBestBookmaker());
        rec.setExpectedValuePct(dto.getExpectedValuePct());
        rec.setKellyStakePct(dto.getKellyStakePct());
        rec.setConfidenceScore(dto.getConfidenceScore());
        rec.setTacticalRationale(dto.getTacticalRationale());
        rec.setSteamDirection(dto.getSteamDirection() != null ? dto.getSteamDirection() : "NEUTRAL");
        rec.setResultStatus("PENDING");

        MatchAiRecommendation saved = recommendationRepository.save(rec);
        log.info("Saved AI recommendation for match #{} ({}-{}): Bet='{}' @ {}",
                matchId, rec.getTeam1Name(), rec.getTeam2Name(), rec.getRecommendedMarket(), rec.getRecommendedOdd());

        return Optional.of(saved);
    }

    /**
     * Periodic scanner for upcoming matches in the T-40m window.
     */
    @Scheduled(cron = "${analyst.scheduling.scan-cron:0 */10 * * * *}")
    public void scanUpcomingMatches() {
        log.info("Scanning for upcoming matches entering T-40m window...");
        List<Long> upcoming = dossierService.findUpcomingMatchesForEvaluation(25, 50);
        log.info("Found {} upcoming matches for AI evaluation.", upcoming.size());

        for (Long matchId : upcoming) {
            try {
                evaluateMatch(matchId);
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
