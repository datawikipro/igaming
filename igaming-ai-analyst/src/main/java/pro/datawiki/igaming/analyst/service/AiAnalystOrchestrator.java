package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.dto.ModelVersionDto;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.guard.MatchPreFlightGatekeeper;
import pro.datawiki.igaming.analyst.policy.InstitutionalRiskPolicyManager;
import pro.datawiki.igaming.analyst.quantitative.QuantitativeEdgeHunter;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.scenario.LineupScenarioEnricher;
import pro.datawiki.igaming.dto.Language;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.util.*;

/**
 * High-Level Quantitative AI Analyst Orchestration Facade.
 * Coordinates pre-flight gatekeeping, ML tactical tensor inference,
 * quantitative market edge valuation (+EV), and strategy voting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalystOrchestrator {

    public static final String CURRENT_MODEL_VERSION = "v0.4.1.1.1";
    public static final double MIN_RECOMMENDED_ODD = 1.35;

    private final MatchDossierService dossierService;
    private final MlInferenceClient mlInferenceClient;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final StrategyEngineService strategyEngineService;
    private final AnalystStatisticsService statisticsService;
    private final ObjectMapper objectMapper;

    // Layered architecture components
    private final MatchPreFlightGatekeeper preFlightGatekeeper;
    private final QuantitativeEdgeHunter edgeHunter;
    private final InstitutionalRiskPolicyManager policyManager;
    private final LineupScenarioEnricher lineupScenarioEnricher;
    private final AnalystAnomalyReporter anomalyReporter;
    private final RadarEventPublisher radarEventPublisher;

    public MatchAiRecommendation getOrCreateRecommendation(Long matchId) {
        return recommendationRepository.findByMatchIdAndModelVersion(matchId, CURRENT_MODEL_VERSION)
                .orElseGet(() -> {
                    MatchAiRecommendation r = new MatchAiRecommendation();
                    r.setMatchId(matchId);
                    r.setModelVersion(CURRENT_MODEL_VERSION);
                    return r;
                });
    }

    /**
     * Evaluates a specific match and stores the recommendation in PostgreSQL.
     */
    @Transactional
    public Optional<MatchAiRecommendation> evaluateMatch(Long matchId) {
        return evaluateMatchInternal(matchId, false);
    }

    /**
     * Force-evaluates a match, bypassing the team schedule collision quarantine.
     */
    @Transactional
    public Optional<MatchAiRecommendation> forceEvaluateMatch(Long matchId) {
        log.info("Force-evaluating match #{} bypassing conflict quarantine...", matchId);
        return evaluateMatchInternal(matchId, true);
    }

    private Optional<MatchAiRecommendation> evaluateMatchInternal(Long matchId, boolean bypassConflictCheck) {
        log.info("Evaluating match #{} with AI Quantitative Analyst (bypassConflictCheck={})...", matchId, bypassConflictCheck);
        Optional<MatchAiRecommendation> existingOpt = recommendationRepository.findByMatchIdAndModelVersion(matchId, CURRENT_MODEL_VERSION);

        if (existingOpt.isPresent()) {
            MatchAiRecommendation existing = existingOpt.get();
            String evalStage = existing.getEvaluationStage();

            if ("SETTLED".equalsIgnoreCase(evalStage)) {
                log.info("Match #{} recommendation #{} is already settled (stage={}). Skipping re-evaluation.",
                        matchId, existing.getId(), evalStage);
                return existingOpt;
            }
        }

        String dossierPrompt = dossierService.getFormattedPrompt(matchId);
        if (dossierPrompt == null || dossierPrompt.startsWith("MATCH_NOT_FOUND")) {
            log.warn("Match dossier not found for matchId={}", matchId);
            return Optional.empty();
        }

        Map<String, Object> dossierMeta = dossierService.getMatchDossier(matchId);
        Sport sport = resolveSportFromDossier(dossierMeta);

        MatchAiRecommendation rec = getOrCreateRecommendation(matchId);

        // ── STEP 1: Pre-Flight Gatekeeper & Physical Reality Guards ──
        MatchPreFlightGatekeeper.PreFlightDecision decision = preFlightGatekeeper.checkPreFlightGuards(
                matchId, dossierMeta, sport, rec, bypassConflictCheck
        );
        if (!decision.shouldProceed()) {
            MatchAiRecommendation saved = recommendationRepository.save(decision.getRecommendation());
            return Optional.of(saved);
        }

        Long startMs = dossierMeta != null ? (Long) dossierMeta.get("start_time") : null;
        long minutesToKickoff = (startMs != null)
                ? (startMs - System.currentTimeMillis()) / (60L * 1000L)
                : 9999L;
        boolean isLineupConfirmed = Boolean.TRUE.equals(dossierMeta != null ? dossierMeta.get("is_lineup_confirmed") : false);
        boolean isTeam = (sport == null || sport.isTeamSport());
        int lineupWindow = (sport != null) ? sport.getLineupConfirmationWindowMinutes() : 45;
        boolean allowPreLineupBetting = (sport == null) || sport.isPreLineupBettingAllowed();
        boolean isKickoffWait = !isLineupConfirmed && isTeam && !allowPreLineupBetting && minutesToKickoff <= lineupWindow && minutesToKickoff > 1;

        // ── STEP 2: ML Inference Call (MultiTaskTacticalNet on :8085) ──
        MlPredictionDto ml = mlInferenceClient.predict(matchId, dossierMeta);
        if (ml != null && dossierMeta != null) {
            if (ml.getEffectiveBaseline() != null) dossierMeta.put("expected_total_baseline", ml.getEffectiveBaseline());
            if (ml.getEffectiveCushionLow() != null) dossierMeta.put("tournament_cushion_low", ml.getEffectiveCushionLow());
            if (ml.getEffectiveCushionHigh() != null) dossierMeta.put("tournament_cushion_high", ml.getEffectiveCushionHigh());
            if (ml.getEffectiveProbCushionLow() != null) dossierMeta.put("prob_cushion_low", ml.getEffectiveProbCushionLow());
            if (ml.getEffectiveProbCushionHigh() != null) dossierMeta.put("prob_cushion_high", ml.getEffectiveProbCushionHigh());
        }

        // ── STEP 2.5: Zero-History / Unmapped Team Guard (Zero-Outlier & Kafka Alert Protocol) ──
        List<?> t1Hist = extractRecentMatchesList(dossierMeta != null ? dossierMeta.get("team1_recent_matches") : null);
        List<?> t2Hist = extractRecentMatchesList(dossierMeta != null ? dossierMeta.get("team2_recent_matches") : null);
        boolean t1Empty = t1Hist.isEmpty();
        boolean t2Empty = t2Hist.isEmpty();

        // Asynchronously report unmapped / zero-history teams to MDM for catalog enrichment
        if (t1Empty || t2Empty) {
            String t1 = dossierMeta != null && dossierMeta.get("team1_name") != null ? (String) dossierMeta.get("team1_name") : "Команда 1";
            String t2 = dossierMeta != null && dossierMeta.get("team2_name") != null ? (String) dossierMeta.get("team2_name") : "Команда 2";
            String league = dossierMeta != null && dossierMeta.get("league_name") != null ? (String) dossierMeta.get("league_name") : "Неизвестная лига";
            String sportCode = sport != null ? sport.getCode() : (dossierMeta != null && dossierMeta.get("sport_name") != null ? (String) dossierMeta.get("sport_name") : "SPORT");
            String sportSlug = sport != null ? sport.getSlug() : (sportCode != null ? sportCode.toLowerCase() : "unknown");

            if (t1Empty) {
                anomalyReporter.reportUnmappedTeam(matchId, t1, league, sportCode, sportSlug);
            }
            if (t2Empty) {
                anomalyReporter.reportUnmappedTeam(matchId, t2, league, sportCode, sportSlug);
            }
            if (!t1Empty && !t2Empty) {
                anomalyReporter.reportUnmappedTeam(matchId, t1 + " / " + t2, league, sportCode, sportSlug);
            }
        }

        // Only enforce hard veto if the ML model explicitly rejected the match or failed evaluation
        boolean isZeroHistoryReject = (ml != null && "ZERO_HISTORY_UNMAPPED_TEAM".equals(ml.getDecisionReasonCode()))
                || (ml == null || !ml.isHasMlEvaluation());

        if (isZeroHistoryReject) {
            String t1 = dossierMeta != null && dossierMeta.get("team1_name") != null ? (String) dossierMeta.get("team1_name") : "Команда 1";
            String t2 = dossierMeta != null && dossierMeta.get("team2_name") != null ? (String) dossierMeta.get("team2_name") : "Команда 2";
            String league = dossierMeta != null && dossierMeta.get("league_name") != null ? (String) dossierMeta.get("league_name") : "Неизвестная лига";
            String sportCode = sport != null ? sport.getCode() : (dossierMeta != null && dossierMeta.get("sport_name") != null ? (String) dossierMeta.get("sport_name") : "SPORT");

            List<String> unmappedTeams = new ArrayList<>();
            if (t1Empty) {
                unmappedTeams.add(t1);
            }
            if (t2Empty) {
                unmappedTeams.add(t2);
            }
            if (!t1Empty && !t2Empty) {
                unmappedTeams.add(t1 + " / " + t2);
            }

            String unmappedSummary = String.join(", ", unmappedTeams);
            preFlightGatekeeper.populateMetadata(rec, dossierMeta, sport);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setEvaluationStage("UNMAPPED_TEAM_PASS");
            rec.setErrorCategory("UNMAPPED_TEAM_ZERO_HISTORY");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis(String.format("⚠️ ПРОПУСК МАТЧА: Команда '%s' не имеет матчей в истории (0 матчей). 99%% вероятность ошибки маппинга или новичок. Ставить нельзя!", unmappedSummary));
            rec.setIdentifiedRisk("Ставка на/против команды с 0 матчами в истории запрещена (высокий риск рассинхронизации сущностей в MDM или непредсказуемый новичок).");
            rec.setTacticalRationale(String.format("Аналитический алгоритм заблокировал ставку: команда '%s', лига '%s', спорт '%s' не мапится, требуется срочное исправление маппинга.", unmappedSummary, league, sportCode));

            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // ── STEP 3: Kickoff In-Play Lineup Verification Protocol (WAIT_KICKOFF) ──
        if (isKickoffWait) {
            log.info("Match #{} (starts in {}m): official lineups not yet available. Auto-WAIT until Kickoff.", matchId, minutesToKickoff);
            preFlightGatekeeper.populateMetadata(rec, dossierMeta, sport);
            rec.setEvaluationStage("WAIT_KICKOFF");
            rec.setLineupStatus("ESTIMATED");
            rec.setConvictionTier("WAIT");
            rec.setRecommendedMarket("WAIT");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);

            if (ml != null && ml.isHasMlEvaluation()) {
                rec.setFairP1(ml.getProbHome());
                rec.setFairPx(ml.getProbDraw());
                rec.setFairP2(ml.getProbAway());
                rec.setFairOver25(ml.getProbOver());
                rec.setFairUnder25(ml.getProbUnder());
                lineupScenarioEnricher.enrichRecommendationWithLineupAndScenarios(rec, ml);
            }

            String venueSurface = sport != null ? (sport.isPlayedOnIce() ? "лёд" : (!sport.isTeamSport() ? "корт" : "площадку")) : "площадку";
            String startAction = sport != null ? sport.getStartActionPhrase(Language.RU) : "стартового свистка";

            rec.setCoreHypothesis(String.format("⏳ ОЖИДАНИЕ СТАРТА МАТЧА (In-Play Verification): предматчевый протокол лиги отсутствует. Ставка заблокирована до 1-й минуты для проверки реального состава участников.",
                    sport != null ? sport.getDisplayName(Language.RU) : "СПОРТ"));
            rec.setIdentifiedRisk(String.format("Вторичный турнир без раннего электронного протокола. Риск скрытой ротации. Ожидаем %s (0-2 мин) для проверки вышедших на %s игроков.", startAction, venueSurface));
            rec.setTacticalRationale(String.format("В соответствии с защитным протоколом In-Play Entry, при отсутствии раннего протокола ставка переносится на 1-ю минуту матча (проверка вышедших на %s участников).", venueSurface));
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        if (ml == null || !ml.isHasMlEvaluation()) {
            log.info("Match #{} rejected by Pre-Flight Gate: NO ML EVALUATION ({}). Auto-PASS enforced.",
                    matchId, ml != null ? ml.getReason() : "Service unavailable");
            preFlightGatekeeper.populateMetadata(rec, dossierMeta, sport);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setEvaluationStage((isLineupConfirmed || !isTeam) ? "FINAL_PASS" : "PRE_LINEUP");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ОЖИДАНИЕ ML-ОЦЕНКИ: " + (ml != null && ml.getReason() != null ? ml.getReason() : "Модель формирует прогноз."));
            rec.setIdentifiedRisk("Недостаточная выборка исторических матчей команд для тактического моделирования (Data/Model Void).");
            rec.setTacticalRationale("Аналитический расчет ожидает квантовой оценки от сервиса ML-инференса.");
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // ── STEP 4: Stage 1 (Pre-Lineup Fast Radar Screening) ──
        boolean isPreLineupRadar = !isLineupConfirmed && isTeam && minutesToKickoff > lineupWindow;
        if (isPreLineupRadar) {
            preFlightGatekeeper.populateMetadata(rec, dossierMeta, sport);
            rec.setEvaluationStage("PRE_LINEUP");
            rec.setLineupStatus(Boolean.TRUE.equals(ml.getIsLineupProjected()) ? "PROJECTED" : "ESTIMATED");

            QuantitativeEdgeHunter.ValuationResult radarVal = edgeHunter.evaluatePreLineupRadar(matchId, ml, dossierMeta, sport);
            applyValuationToRecommendation(rec, radarVal);

            lineupScenarioEnricher.enrichRecommendationWithLineupAndScenarios(rec, ml);
            MatchAiRecommendation saved = recommendationRepository.save(rec);

            if (radarVal.getAppliedPolicyCap() != null) {
                policyManager.recordPolicyIntervention(
                        saved,
                        radarVal.getRawTier(),
                        saved.getConvictionTier(),
                        radarVal.getAppliedPolicyCap().getRuleCode(),
                        radarVal.getAppliedPolicyCap().getReason()
                );
            }

            log.info("Saved PRE_LINEUP fast radar recommendation for match #{} ({}-{}): Tier={}, Bet='{}' @ {}",
                    matchId, rec.getTeam1Name(), rec.getTeam2Name(), saved.getConvictionTier(), saved.getRecommendedMarket(), saved.getRecommendedOdd());
            strategyEngineService.generateStrategyVotes(saved, saved.getRecommendedOdd().doubleValue(), saved.getRecommendedMarket(), radarVal.getAllCandidates(), dossierMeta);
            return Optional.of(saved);
        }

        // ── STEP 5: Stage 2 (Lineup Confirmed / Quantitative Institutional Valuation) ──
        QuantitativeEdgeHunter.ValuationResult stage2Val = edgeHunter.evaluateLineupConfirmed(
                matchId, ml, dossierMeta, sport, isLineupConfirmed, minutesToKickoff
        );

        preFlightGatekeeper.populateMetadata(rec, dossierMeta, sport);
        applyValuationToRecommendation(rec, stage2Val);

        String finalStage;
        if (minutesToKickoff < -2) {
            finalStage = "IN_PLAY";
        } else if (minutesToKickoff <= 1) {
            finalStage = "KICKOFF_VERIFIED";
        } else if (isLineupConfirmed) {
            finalStage = "LINEUP_CONFIRMED";
        } else if (Boolean.TRUE.equals(ml.getIsLineupProjected())) {
            finalStage = "PROJECTED";
        } else {
            finalStage = "STAGE_2_PREVIEW";
        }
        rec.setEvaluationStage(finalStage);
        rec.setLineupStatus(isLineupConfirmed ? "CONFIRMED" : (Boolean.TRUE.equals(ml.getIsLineupProjected()) ? "PROJECTED" : "ESTIMATED"));

        lineupScenarioEnricher.enrichRecommendationWithLineupAndScenarios(rec, ml);
        MatchAiRecommendation saved = recommendationRepository.save(rec);

        if (stage2Val.getAppliedPolicyCap() != null) {
            policyManager.recordPolicyIntervention(
                    saved,
                    stage2Val.getRawTier(),
                    saved.getConvictionTier(),
                    stage2Val.getAppliedPolicyCap().getRuleCode(),
                    stage2Val.getAppliedPolicyCap().getReason()
            );
        }

        log.info("Saved AI recommendation for match #{} ({}-{}): Bet='{}' @ {}, Risk='{}'",
                matchId, rec.getTeam1Name(), rec.getTeam2Name(), rec.getRecommendedMarket(), rec.getRecommendedOdd(), rec.getIdentifiedRisk());
        strategyEngineService.generateStrategyVotes(saved, saved.getRecommendedOdd().doubleValue(), saved.getRecommendedMarket(), stage2Val.getAllCandidates(), dossierMeta);
        radarEventPublisher.publishOpportunityIfConfirmed(saved);
        return Optional.of(saved);
    }

    private void applyValuationToRecommendation(MatchAiRecommendation rec, QuantitativeEdgeHunter.ValuationResult val) {
        rec.setConvictionTier(val.getConvictionTier());
        rec.setRecommendedMarket(val.getRecommendedMarket());
        rec.setRecommendedOdd(val.getRecommendedOdd());
        rec.setBestBookmaker(val.getBestBookmaker());
        rec.setExpectedValuePct(val.getExpectedValuePct());
        rec.setKellyStakePct(val.getKellyStakePct());
        rec.setConfidenceScore(val.getConfidenceScore());
        rec.setCoreHypothesis(val.getCoreHypothesis());
        rec.setIdentifiedRisk(val.getIdentifiedRisk());
        rec.setTacticalRationale(val.getTacticalRationale());
        rec.setFairP1(val.getFairP1());
        rec.setFairPx(val.getFairPx());
        rec.setFairP2(val.getFairP2());
        rec.setFairOver25(val.getFairOver25());
        rec.setFairUnder25(val.getFairUnder25());
        if (val.getFairBttsYes() != null) rec.setFairBttsYes(val.getFairBttsYes());
        if (val.getFairBttsNo() != null) rec.setFairBttsNo(val.getFairBttsNo());
        if (val.getExpectedXg() != null) rec.setExpectedXg(val.getExpectedXg());
        if (val.getSteamDirection() != null) rec.setSteamDirection(val.getSteamDirection());
    }

    private Sport resolveSportFromDossier(Map<String, Object> dossierMeta) {
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
            } else if (lLower.contains("short football") || lLower.contains("3x3") || lLower.contains("4x4") || lLower.contains("5x5") || lLower.contains("шорт-футбол") || lLower.contains("шорт футбол")) {
                sportName = "SHORT_FOOTBALL";
            }
        }
        return sportName != null ? Sports.get(sportName) : null;
    }

    // ── Public Facade Delegations ──

    public Optional<MatchAiRecommendation> enrichAndPersistProjectedScenarios(MatchAiRecommendation rec) {
        return lineupScenarioEnricher.enrichAndPersistProjectedScenarios(rec);
    }

    public void recordPolicyIntervention(MatchAiRecommendation rec, String rawTier, String appliedTier, String ruleCode, String reason) {
        policyManager.recordPolicyIntervention(rec, rawTier, appliedTier, ruleCode, reason);
    }

    public List<ModelVersionDto> getModelVersions() {
        return statisticsService.getModelVersions();
    }

    public AnalystStatsDto getPerformanceStats() {
        return statisticsService.getPerformanceStats();
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query) {
        return statisticsService.getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query, String version) {
        return statisticsService.getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, version);
    }

    public AnalystStatsDto getPerformanceStats(
            String status, String sport, String stage, String timeRange,
            String oddRange, String bookmaker, String marketCategory, String query, String version, String strategy) {
        return statisticsService.getPerformanceStats(status, sport, stage, timeRange, oddRange, bookmaker, marketCategory, query, version, strategy);
    }

    public List<Map<String, Object>> getAvailableBookmakersStats() {
        return statisticsService.getAvailableBookmakersStats();
    }

    private List<?> extractRecentMatchesList(Object obj) {
        if (obj instanceof List<?> l) {
            return l;
        }
        return Collections.emptyList();
    }
}
