package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.AiAnalysisResultDto;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiLearningMemoryRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.util.SlugUtils;
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
    private final MlInferenceClient mlInferenceClient;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiLearningMemoryRepository memoryRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

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

        // Pre-Flight Gate: Anti-Advocate & Data-Blindness Protocol
        // In team sports without starters, recent history, or player ratings, betting on favorites
        // turns the agent into a "bookmaker's advocate" with negative mathematical expectation.
        if (isDataVoid(dossierMeta, sport)) {
            log.info("Match #{} rejected by Pre-Flight Gate: DATA VOID detected (starters/history/ratings absent). Auto-PASS enforced.", matchId);
            MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                    .orElse(new MatchAiRecommendation());
            rec.setMatchId(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis("ВЕТО НА СЛЕПОТУ ДАННЫХ (Anti-Advocate Protocol): отсутствуют составы, рейтинги игроков и история личных встреч. Ставка по котировкам без фактуры запрещена.");
            rec.setIdentifiedRisk("Полное отсутствие фактических данных (Data Void). Риск случайного исхода и ложной уверенности по котировкам букмекера.");
            rec.setTacticalRationale("В соответствии с доменным правилом Zero Bookmaker Advocacy, модель не делает ставок на матчи без минимальной фактуры.");
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted Pre-Flight PASS record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);
        }

        // STAGE 0: ML TACTICAL NEURAL NET EVALUATION (5 Sports Multi-Task Model)
        // User Mandate: "если нет ML оценки даже сразу пасуем"
        MlPredictionDto ml = mlInferenceClient.predict(matchId, dossierMeta);
        if (ml == null || !ml.isHasMlEvaluation()) {
            log.info("Match #{} rejected by Pre-Flight Gate: NO ML EVALUATION ({}). Auto-PASS enforced.",
                    matchId, ml != null ? ml.getReason() : "Service unavailable");
            MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                    .orElse(new MatchAiRecommendation());
            rec.setMatchId(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis("ВЕТО НА ОТСУТСТВИЕ ML-ОЦЕНКИ: " + (ml != null && ml.getReason() != null ? ml.getReason() : "Модель не смогла сформировать прогноз."));
            rec.setIdentifiedRisk("Недостаточная выборка исторических матчей команд для тактического моделирования (Data/Model Void).");
            rec.setTacticalRationale("В соответствии с доменным правилом ML-Gating, модель не делает ставок на матчи без верифицированной ML-оценки.");
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted Pre-Flight PASS record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);
        }

        // ML Cohort Gating: If the neural net determined P < 70% (equal teams / coinflip)
        if ("PASS".equalsIgnoreCase(ml.getConfidenceTier())) {
            log.info("Match #{} rejected: ML model classified as coinflip / lean (P < 70%). Auto-PASS enforced.", matchId);
            MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                    .orElse(new MatchAiRecommendation());
            rec.setMatchId(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setFairP1(ml.getProbHome());
            rec.setFairPx(ml.getProbDraw());
            rec.setFairP2(ml.getProbAway());
            rec.setFairOver25(ml.getProbOver());
            rec.setFairUnder25(ml.getProbUnder());
            rec.setCoreHypothesis("МЛ-МОДЕЛЬ: Равные шансы / монетка (P=" + ml.getRecommendedProb() + " < 70%). Математический перевес отсутствует.");
            rec.setIdentifiedRisk("Высокая энтропия исхода. Риск случайного результата в некоррелированном матче.");
            rec.setTacticalRationale(ml.getTacticalNarrative());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // Inject verified quantitative ML Evaluation into dossierPrompt for qualitative LLM synthesis
        StringBuilder mlSb = new StringBuilder("\n\n### 🤖 INSTITUTIONAL ML MODEL EVALUATION (Tactical Neural Net):\n");
        mlSb.append(String.format("- Sport: %s\n", ml.getSport()));
        if (ml.getProbHome() != null) mlSb.append(String.format("- P(Home): %.1f%% (Fair Odds: %s)\n", ml.getProbHome().doubleValue() * 100, ml.getFairOddsHome()));
        if (ml.getProbDraw() != null) mlSb.append(String.format("- P(Draw): %.1f%% (Fair Odds: %s)\n", ml.getProbDraw().doubleValue() * 100, ml.getFairOddsDraw()));
        if (ml.getProbAway() != null) mlSb.append(String.format("- P(Away): %.1f%% (Fair Odds: %s)\n", ml.getProbAway().doubleValue() * 100, ml.getFairOddsAway()));
        if (ml.getProbOver() != null) mlSb.append(String.format("- P(Totals Over): %.1f%% (Fair Odds: %s)\n", ml.getProbOver().doubleValue() * 100, ml.getFairOddsOver()));
        if (ml.getProbUnder() != null) mlSb.append(String.format("- P(Totals Under): %.1f%% (Fair Odds: %s)\n", ml.getProbUnder().doubleValue() * 100, ml.getFairOddsUnder()));
        mlSb.append(String.format("- Model Confidence Tier: %s (P=%s, Cohort Historical Accuracy: %s%%)\n",
                ml.getConfidenceTier(), ml.getRecommendedProb(), ml.getCohortAccuracyPct()));
        mlSb.append(String.format("- Model Baseline Market: %s\n", ml.getRecommendedMarket()));
        mlSb.append(String.format("- Tactical Assessment: %s\n", ml.getTacticalNarrative()));
        if (ml.getBenchJokersFound() != null && !ml.getBenchJokersFound().isEmpty()) {
            mlSb.append(String.format("- ⚡ Bench Jokers Detected: %s\n", String.join("; ", ml.getBenchJokersFound())));
        }
        if (ml.isBenchChaosWarning()) {
            mlSb.append("- ⛔ Bench Chaos Alert: Explosive attackers on bench detected. TOTAL UNDER is vetoed by mathematical chaos!\n");
        }
        mlSb.append("You MUST incorporate this quantitative ML evaluation into your analysis. Verify it against squad reality (lineups, injuries, bench jokers veto).\n");
        dossierPrompt = dossierPrompt + mlSb.toString();

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

        Optional<AiAnalysisResultDto> resultOpt = agyWorkerClient.evaluateMatch(matchId, dossierPrompt, analystConstitution, sport);
        if (resultOpt.isEmpty()) {
            log.warn("Worker did not produce recommendation for matchId={}", matchId);
            return Optional.empty();
        }

        AiAnalysisResultDto dto = resultOpt.get();

        // Convert or update existing recommendation
        MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                .orElse(new MatchAiRecommendation());

        rec.setMatchId(matchId);
        populateMetadata(rec, dossierMeta, sport);

        rec.setExpectedXg(dto.getExpectedXg());
        rec.setFairP1(dto.getFairP1() != null ? dto.getFairP1() : ml.getProbHome());
        rec.setFairPx(dto.getFairPx() != null ? dto.getFairPx() : ml.getProbDraw());
        rec.setFairP2(dto.getFairP2() != null ? dto.getFairP2() : ml.getProbAway());
        rec.setFairOver25(dto.getFairOver25() != null ? dto.getFairOver25() : ml.getProbOver());
        rec.setFairUnder25(dto.getFairUnder25() != null ? dto.getFairUnder25() : ml.getProbUnder());
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

            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker(dto.getBestBookmaker() != null && !dto.getBestBookmaker().isBlank() ? dto.getBestBookmaker() : "NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(dto.getConfidenceScore() != null ? dto.getConfidenceScore() : 0);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis(dto.getCoreHypothesis());
            rec.setIdentifiedRisk(dto.getIdentifiedRisk());
            rec.setTacticalRationale(dto.getCoreHypothesis());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted PASS audit record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);
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
        // =========================================================================
        // STAGE 2: Quantitative Valuation & Market Edge (+EV) Overlay
        // The LLM determined fair probabilities in Stage 1 completely blind to odds.
        // Now, we query real live bookmaker quotes from Redis/DB and compute true EV.
        // =========================================================================
        double fairProb = resolveFairProbability(dto, targetMarket, sport);

        RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
        Double marketOddVal = null;
        String bestBookmaker = "Market Consensus";

        if (quote != null && quote.value() > 1.01) {
            marketOddVal = quote.value();
            bestBookmaker = quote.bookmaker();
        } else {
            marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
        }

        if (marketOddVal == null || marketOddVal <= 1.01) {
            log.info("Match #{} has no live bookmaker odds for market '{}'. Auto-PASS enforced.", matchId, targetMarket);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(dto.getConfidenceScore() != null ? dto.getConfidenceScore() : 0);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis(String.format("Пропуск: в линии букмекеров отсутствуют активные котировки для маркета '%s' (невозможно рассчитать математический перевес).", targetMarket));
            rec.setIdentifiedRisk("Отсутствие ликвидности / котировок в линии букмекеров.");
            rec.setTacticalRationale(dto.getCoreHypothesis());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // Anti-Advocate / Heavy-Favorite Trap Protection:
        // In draw-possible sports, if marketOdd is < 1.45 and targetMarket is raw P1/P2, auto-cushion to Double Chance.
        if (sport != null && sport.hasDrawOutcome() && marketOddVal < 1.45) {
            if ("P1".equals(targetMarket) || "HOME_WIN".equals(targetMarket)) {
                targetMarket = "DOUBLE_CHANCE_1X";
                fairProb = resolveFairProbability(dto, targetMarket, sport);
                RedisOddsConsensusService.MarketQuote dcQuote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
                if (dcQuote != null && dcQuote.value() > 1.01) {
                    marketOddVal = dcQuote.value();
                    bestBookmaker = dcQuote.bookmaker();
                } else {
                    marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
                    if (marketOddVal == null) marketOddVal = 1.25;
                }
                log.info("Anti-Advocate Guard: converted raw P1 to DOUBLE_CHANCE_1X @ {} for match #{}", marketOddVal, matchId);
            } else if ("P2".equals(targetMarket) || "AWAY_WIN".equals(targetMarket)) {
                targetMarket = "DOUBLE_CHANCE_X2";
                fairProb = resolveFairProbability(dto, targetMarket, sport);
                RedisOddsConsensusService.MarketQuote dcQuote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
                if (dcQuote != null && dcQuote.value() > 1.01) {
                    marketOddVal = dcQuote.value();
                    bestBookmaker = dcQuote.bookmaker();
                } else {
                    marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
                    if (marketOddVal == null) marketOddVal = 1.25;
                }
                log.info("Anti-Advocate Guard: converted raw P2 to DOUBLE_CHANCE_X2 @ {} for match #{}", marketOddVal, matchId);
            }
        }

        double fairOdd = 100.0 / fairProb;
        double evPct = ((fairProb / 100.0) * marketOddVal - 1.0) * 100.0;

        // Veto Negative EV Traps (-EV Trap Protection)
        // If the bookmaker is paying less than the model's fair odd, the mathematical expectation is negative.
        if (evPct < 0.0) {
            log.info("Match #{} failed Stage 2 Valuation: Negative EV ({:.1f}%). Model fairOdd={:.2f}, Bookmaker odd={:.2f}. Auto-PASS enforced.",
                    matchId, evPct, fairOdd, marketOddVal);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
            rec.setBestBookmaker(bestBookmaker);
            rec.setExpectedValuePct(BigDecimal.valueOf(evPct).setScale(2, RoundingMode.HALF_UP));
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(dto.getConfidenceScore() != null ? dto.getConfidenceScore() : 70);
            rec.setResultStatus("SKIPPED");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis(String.format("ВЕТО ОТРИЦАТЕЛЬНОГО МАТОЖИДАНИЯ (-EV Trap): модель оценила честную вероятность исхода %s в %.1f%% (справедливый кэф %.2f), но букмекер дает лишь %.2f (EV = %.1f%%). Ставка отклонена.",
                    targetMarket, fairProb, fairOdd, marketOddVal, evPct));
            rec.setIdentifiedRisk("Букмекерская линия переоценена (маржа букмекера съедает прибыль). Ставка на дистанции ведет к гарантированному убытку.");
            rec.setTacticalRationale(dto.getCoreHypothesis());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // Positive EV Bet (+EV Validated!)
        double b = marketOddVal - 1.0;
        double p = fairProb / 100.0;
        double q = 1.0 - p;
        double fullKelly = b > 0.0 ? Math.max(0.0, (b * p - q) / b) : 0.0;
        double fraction = "CONFIDENT".equals(tier) ? 0.25 : 0.15;
        double maxStake = "CONFIDENT".equals(tier) ? 5.0 : 2.5;
        double kellyPct = Math.min(maxStake, Math.max(1.0, fullKelly * fraction * 100.0));

        rec.setRecommendedMarket(targetMarket);
        rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
        rec.setBestBookmaker(bestBookmaker);
        rec.setExpectedValuePct(BigDecimal.valueOf(evPct).setScale(2, RoundingMode.HALF_UP));
        rec.setKellyStakePct(BigDecimal.valueOf(kellyPct).setScale(2, RoundingMode.HALF_UP));

        int confidence = dto.getConfidenceScore() != null ? dto.getConfidenceScore() : 80;
        if ("CONFIDENT".equals(tier)) {
            if (confidence < 85) {
                confidence = 85;
            }
        }
        // Solo/racket sport discipline: in tennis/ping pong, odds > 1.58 have elevated upset variance
        if (sport != null && !sport.isTeamSport()) {
            if (marketOddVal > 1.58 && confidence < 86) {
                log.info("Solo sport upset risk discipline: match #{} (odds: {}, conf: {}) - halving Kelly stake for bankroll defense",
                        matchId, marketOddVal, confidence);
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
     * Computes performance statistics across all recommendations, broken down by conviction tier.
     */
    public AnalystStatsDto getPerformanceStats() {
        String sql = """
            SELECT 
                COALESCE(conviction_tier, 'GOOD_PICK') as tier,
                COUNT(*) as total_count,
                COUNT(*) FILTER (WHERE result_status = 'WON') as won_count,
                COUNT(*) FILTER (WHERE result_status = 'LOST') as lost_count,
                COUNT(*) FILTER (WHERE result_status = 'VOID') as void_count,
                COUNT(*) FILTER (WHERE result_status = 'PENDING') as pending_count,
                COUNT(*) FILTER (WHERE result_status = 'SKIPPED') as skipped_count,
                COALESCE(SUM(actual_pnl), 0) as total_pnl_rub,
                AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS') as avg_odd
            FROM match_ai_recommendation
            GROUP BY COALESCE(conviction_tier, 'GOOD_PICK')
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        AnalystStatsDto.TierStatsDto confident = buildTierStats("CONFIDENT", rows);
        AnalystStatsDto.TierStatsDto goodPick = buildTierStats("GOOD_PICK", rows);
        AnalystStatsDto.TierStatsDto pass = buildTierStats("PASS", rows);

        long total = confident.getTotal() + goodPick.getTotal() + pass.getTotal();
        long won = confident.getWon() + goodPick.getWon();
        long lost = confident.getLost() + goodPick.getLost();
        long voidCount = confident.getVoidCount() + goodPick.getVoidCount();
        long pending = confident.getPending() + goodPick.getPending();
        long skipped = pass.getSkipped();
        long settled = won + lost + voidCount;

        double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;
        BigDecimal totalPnl = confident.getTotalPnl().add(goodPick.getTotalPnl());
        double roi = settled > 0 ? (totalPnl.doubleValue() / (settled * 1000.0)) * 100.0 : 0.0;

        // Sports breakdown query
        String sportSql = """
            SELECT 
                COALESCE(sport_name, 'OTHER') as sport,
                COUNT(*) as total_count,
                COUNT(*) FILTER (WHERE result_status = 'WON') as won_count,
                COUNT(*) FILTER (WHERE result_status = 'LOST') as lost_count,
                COUNT(*) FILTER (WHERE result_status = 'VOID') as void_count,
                COUNT(*) FILTER (WHERE result_status = 'PENDING') as pending_count,
                COUNT(*) FILTER (WHERE result_status = 'SKIPPED') as skipped_count,
                COALESCE(SUM(actual_pnl), 0) as total_pnl_rub,
                AVG(recommended_odd) FILTER (WHERE conviction_tier != 'PASS') as avg_odd
            FROM match_ai_recommendation
            GROUP BY COALESCE(sport_name, 'OTHER')
            ORDER BY total_pnl_rub DESC
        """;
        List<Map<String, Object>> sportRows = jdbcTemplate.queryForList(sportSql);
        Map<String, AnalystStatsDto.SportStatsDto> sportsMap = new java.util.LinkedHashMap<>();
        for (Map<String, Object> r : sportRows) {
            String sp = (String) r.get("sport");
            long spTotal = ((Number) r.getOrDefault("total_count", 0)).longValue();
            long spWon = ((Number) r.getOrDefault("won_count", 0)).longValue();
            long spLost = ((Number) r.getOrDefault("lost_count", 0)).longValue();
            long spVoid = ((Number) r.getOrDefault("void_count", 0)).longValue();
            long spPending = ((Number) r.getOrDefault("pending_count", 0)).longValue();
            long spSkipped = ((Number) r.getOrDefault("skipped_count", 0)).longValue();
            BigDecimal spPnl = BigDecimal.valueOf(((Number) r.getOrDefault("total_pnl_rub", 0.0)).doubleValue())
                    .setScale(2, RoundingMode.HALF_UP);
            double spAvgOdd = r.get("avg_odd") != null ? ((Number) r.get("avg_odd")).doubleValue() : 0.0;
            long spSettled = spWon + spLost + spVoid;
            double spWinrate = (spWon + spLost) > 0 ? (spWon * 100.0) / (spWon + spLost) : 0.0;
            double spRoi = spSettled > 0 ? (spPnl.doubleValue() / (spSettled * 1000.0)) * 100.0 : 0.0;

            sportsMap.put(sp, AnalystStatsDto.SportStatsDto.builder()
                    .sport(sp)
                    .total(spTotal)
                    .settled(spSettled)
                    .won(spWon)
                    .lost(spLost)
                    .voidCount(spVoid)
                    .pending(spPending)
                    .skipped(spSkipped)
                    .winratePct(Math.round(spWinrate * 10.0) / 10.0)
                    .totalPnl(spPnl)
                    .roiPct(Math.round(spRoi * 10.0) / 10.0)
                    .avgOdds(Math.round(spAvgOdd * 100.0) / 100.0)
                    .build());
        }

        // Backtest 151 benchmark dataset
        Map<String, AnalystStatsDto.TierStatsDto> backtest151 = Map.of(
            "CONFIDENT", AnalystStatsDto.TierStatsDto.builder()
                .tier("CONFIDENT").total(18).settled(18).won(15).lost(3).voidCount(0).pending(0).skipped(0)
                .winratePct(83.3).totalPnl(BigDecimal.valueOf(3720.0)).roiPct(20.67).avgOdds(1.38).build(),
            "GOOD_PICK", AnalystStatsDto.TierStatsDto.builder()
                .tier("GOOD_PICK").total(106).settled(104).won(74).lost(30).voidCount(2).pending(0).skipped(0)
                .winratePct(71.2).totalPnl(BigDecimal.valueOf(7010.0)).roiPct(6.74).avgOdds(1.58).build(),
            "PASS", AnalystStatsDto.TierStatsDto.builder()
                .tier("PASS").total(27).settled(26).won(13).lost(13).voidCount(0).pending(0).skipped(27)
                .winratePct(50.0).totalPnl(BigDecimal.valueOf(-6620.0)).roiPct(-25.46).avgOdds(1.72).build()
        );

        return AnalystStatsDto.builder()
                .totalEvaluations(total)
                .totalSettled(settled)
                .won(won)
                .lost(lost)
                .voidCount(voidCount)
                .pending(pending)
                .skipped(skipped)
                .winratePct(Math.round(winrate * 10.0) / 10.0)
                .totalPnl(totalPnl)
                .roiPct(Math.round(roi * 10.0) / 10.0)
                .confident(confident)
                .goodPick(goodPick)
                .pass(pass)
                .sports(sportsMap)
                .backtest151(backtest151)
                .build();
    }

    private AnalystStatsDto.TierStatsDto buildTierStats(String targetTier, List<Map<String, Object>> rows) {
        for (Map<String, Object> r : rows) {
            String tier = (String) r.get("tier");
            if (targetTier.equalsIgnoreCase(tier)) {
                long total = ((Number) r.getOrDefault("total_count", 0)).longValue();
                long won = ((Number) r.getOrDefault("won_count", 0)).longValue();
                long lost = ((Number) r.getOrDefault("lost_count", 0)).longValue();
                long voidCount = ((Number) r.getOrDefault("void_count", 0)).longValue();
                long pending = ((Number) r.getOrDefault("pending_count", 0)).longValue();
                long skipped = ((Number) r.getOrDefault("skipped_count", 0)).longValue();
                BigDecimal pnl = BigDecimal.valueOf(((Number) r.getOrDefault("total_pnl_rub", 0.0)).doubleValue())
                        .setScale(2, RoundingMode.HALF_UP);
                double avgOdd = r.get("avg_odd") != null ? ((Number) r.get("avg_odd")).doubleValue() : 0.0;

                long settled = won + lost + voidCount;
                double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;
                double roi = settled > 0 ? (pnl.doubleValue() / (settled * 1000.0)) * 100.0 : 0.0;

                return AnalystStatsDto.TierStatsDto.builder()
                        .tier(targetTier)
                        .total(total)
                        .settled(settled)
                        .won(won)
                        .lost(lost)
                        .voidCount(voidCount)
                        .pending(pending)
                        .skipped(skipped)
                        .winratePct(Math.round(winrate * 10.0) / 10.0)
                        .totalPnl(pnl)
                        .roiPct(Math.round(roi * 10.0) / 10.0)
                        .avgOdds(Math.round(avgOdd * 100.0) / 100.0)
                        .build();
            }
        }
        return AnalystStatsDto.TierStatsDto.builder()
                .tier(targetTier)
                .total(0).settled(0).won(0).lost(0).voidCount(0).pending(0).skipped(0)
                .winratePct(0.0).totalPnl(BigDecimal.ZERO).roiPct(0.0).avgOdds(0.0)
                .build();
    }

    private void populateMetadata(MatchAiRecommendation rec, Map<String, Object> dossierMeta, Sport sport) {
        if (dossierMeta != null) {
            String t1 = (String) dossierMeta.get("team1_name");
            String t2 = (String) dossierMeta.get("team2_name");
            String league = (String) dossierMeta.get("league_name");
            String sportCode = sport != null ? sport.getCode() : (String) dossierMeta.get("sport_name");

            rec.setTeam1Name(t1);
            rec.setTeam2Name(t2);
            rec.setLeagueName(league);
            rec.setSportName(sportCode);
            rec.setSportSlug(SlugUtils.slugify(sportCode, "football"));
            rec.setTournamentSlug(SlugUtils.slugify(league, "league"));

            if (rec.getMatchId() != null) {
                String matchSlug = SlugUtils.buildMatchSlug(rec.getMatchId(), t1, t2);
                rec.setMatchSlug(matchSlug);
                rec.setMatchUrl(SlugUtils.buildMatchUrl(rec.getMatchId(), t1, t2, "ru"));
            }

            Long startTs = (Long) dossierMeta.get("start_time");
            if (startTs != null) {
                rec.setMatchStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTs), ZoneOffset.UTC));
            }
            Boolean lineConfirmed = (Boolean) dossierMeta.get("is_lineup_confirmed");
            rec.setLineupStatus(Boolean.TRUE.equals(lineConfirmed) ? "CONFIRMED" : "ESTIMATED");
        }
    }

    /**
     * Evaluates whether the match dossier represents a "Data Void".
     * In a Data Void, there are no rosters, no recent matches, no player ratings, and no DNA tags.
     * Betting on favorites under Data Void turns the algorithm into a "bookmaker's advocate",
     * leading to systematic negative expectation in low-tier leagues.
     */
    private boolean isDataVoid(Map<String, Object> dossierMeta, Sport sport) {
        if (dossierMeta == null) {
            return true;
        }

        boolean isTeam = (sport == null || sport.isTeamSport());

        // Check starters
        Object starters1 = dossierMeta.get("team1_starters");
        Object starters2 = dossierMeta.get("team2_starters");
        boolean hasStarters = isNonEmptyJsonArray(starters1) || isNonEmptyJsonArray(starters2);

        // Check recent matches
        Object recent1 = dossierMeta.get("team1_recent_matches");
        Object recent2 = dossierMeta.get("team2_recent_matches");
        boolean hasRecent = isNonEmptyJsonArray(recent1) || isNonEmptyJsonArray(recent2);

        // Check player ratings
        Object rating1 = dossierMeta.get("avg_rating_team1");
        Object rating2 = dossierMeta.get("avg_rating_team2");
        boolean hasRating = rating1 != null || rating2 != null;

        // Check team DNA tags
        Object dna1 = dossierMeta.get("team1_dna_tags");
        Object dna2 = dossierMeta.get("team2_dna_tags");
        boolean hasDna = (dna1 != null && !dna1.toString().isBlank()) || (dna2 != null && !dna2.toString().isBlank());

        // Check head-to-head
        Object h2h = dossierMeta.get("h2h_json");
        boolean hasH2h = h2h != null && !h2h.toString().isBlank() && !"[]".equals(h2h.toString().trim());

        // Check form
        Object form1 = dossierMeta.get("team1_form");
        Object form2 = dossierMeta.get("team2_form");
        boolean hasForm = (form1 != null && !form1.toString().isBlank()) || (form2 != null && !form2.toString().isBlank());

        if (isTeam) {
            // In team sports, without starters, history, ratings, DNA, H2H, and form, there is zero factual basis.
            return !hasStarters && !hasRecent && !hasRating && !hasDna && !hasH2h && !hasForm;
        } else {
            // In solo/racket sports, require either recent matches, ratings, H2H, or form
            return !hasRecent && !hasRating && !hasH2h && !hasForm;
        }
    }

    private boolean isNonEmptyJsonArray(Object obj) {
        if (obj == null) return false;
        String str = obj.toString().trim();
        return !str.isEmpty() && !"[]".equals(str) && !"null".equalsIgnoreCase(str);
    }

    private double resolveFairProbability(AiAnalysisResultDto dto, String market, Sport sport) {
        if (market == null) return 60.0;
        String m = market.toUpperCase();
        if ("P1".equals(m) || "HOME_WIN".equals(m)) {
            return dto.getFairP1() != null ? dto.getFairP1().doubleValue() : 60.0;
        }
        if ("P2".equals(m) || "AWAY_WIN".equals(m)) {
            return dto.getFairP2() != null ? dto.getFairP2().doubleValue() : 60.0;
        }
        if ("PX".equals(m) || "DRAW".equals(m) || "X".equals(m)) {
            return dto.getFairPx() != null ? dto.getFairPx().doubleValue() : 25.0;
        }
        if ("DOUBLE_CHANCE_1X".equals(m) || "1X".equals(m)) {
            double p1 = dto.getFairP1() != null ? dto.getFairP1().doubleValue() : 45.0;
            double px = dto.getFairPx() != null ? dto.getFairPx().doubleValue() : 28.0;
            return Math.min(95.0, p1 + px);
        }
        if ("DOUBLE_CHANCE_X2".equals(m) || "X2".equals(m)) {
            double p2 = dto.getFairP2() != null ? dto.getFairP2().doubleValue() : 40.0;
            double px = dto.getFairPx() != null ? dto.getFairPx().doubleValue() : 28.0;
            return Math.min(95.0, p2 + px);
        }
        if ("TOTAL_UNDER_2.5".equals(m) || "TU_2.5".equals(m)) {
            return dto.getFairUnder25() != null ? dto.getFairUnder25().doubleValue() : 55.0;
        }
        if ("TOTAL_OVER_2.5".equals(m) || "TO_2.5".equals(m)) {
            return dto.getFairOver25() != null ? dto.getFairOver25().doubleValue() : 50.0;
        }
        if (m.startsWith("AH_") && m.contains("PLUS")) {
            return 72.0;
        }
        if (dto.getConfidenceScore() != null && dto.getConfidenceScore() > 0) {
            return Math.min(85.0, dto.getConfidenceScore().doubleValue());
        }
        return 65.0;
    }

    private Double fallbackOddFromDossier(Map<String, Object> dossierMeta, String targetMarket) {
        if (dossierMeta == null) return null;
        String m = targetMarket.toUpperCase();
        if ("P1".equals(m) || "HOME_WIN".equals(m)) {
            Object o = dossierMeta.get("best_win1");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        if ("P2".equals(m) || "AWAY_WIN".equals(m)) {
            Object o = dossierMeta.get("best_win2");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        if ("PX".equals(m) || "DRAW".equals(m) || "X".equals(m)) {
            Object o = dossierMeta.get("best_draw");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        if ("DOUBLE_CHANCE_1X".equals(m) || "1X".equals(m)) {
            Object w1 = dossierMeta.get("best_win1");
            Object x = dossierMeta.get("best_draw");
            if (w1 instanceof Number n1 && x instanceof Number nx) {
                double v1 = n1.doubleValue();
                double vx = nx.doubleValue();
                if (v1 > 1.0 && vx > 1.0) {
                    return Math.round((1.0 / (1.0 / v1 + 1.0 / vx)) * 0.95 * 100.0) / 100.0;
                }
            }
        }
        if ("DOUBLE_CHANCE_X2".equals(m) || "X2".equals(m)) {
            Object w2 = dossierMeta.get("best_win2");
            Object x = dossierMeta.get("best_draw");
            if (w2 instanceof Number n2 && x instanceof Number nx) {
                double v2 = n2.doubleValue();
                double vx = nx.doubleValue();
                if (v2 > 1.0 && vx > 1.0) {
                    return Math.round((1.0 / (1.0 / v2 + 1.0 / vx)) * 0.95 * 100.0) / 100.0;
                }
            }
        }
        if ("TOTAL_UNDER_2.5".equals(m) || "TU_2.5".equals(m)) {
            Object o = dossierMeta.get("best_tu25");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        if ("TOTAL_OVER_2.5".equals(m) || "TO_2.5".equals(m)) {
            Object o = dossierMeta.get("best_to25");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        return null;
    }
}
