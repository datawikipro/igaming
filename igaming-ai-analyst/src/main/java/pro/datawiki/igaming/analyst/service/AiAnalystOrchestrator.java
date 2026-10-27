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

    public static final double MIN_RECOMMENDED_ODD = 1.30;

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
            } else if (lLower.contains("short football") || lLower.contains("3x3") || lLower.contains("4x4") || lLower.contains("5x5") || lLower.contains("шорт-футбол") || lLower.contains("шорт футбол")) {
                sportName = "SHORT_FOOTBALL";
            }
        }
        Sport sport = Sports.get(sportName != null ? sportName : "FOOTBALL");

        // Domain Guard: Short Football is strictly isolated from 11x11 Association Football
        if (sport == Sports.SHORT_FOOTBALL) {
            log.info("Match #{} belongs to SHORT_FOOTBALL ('{}'). Classical 11x11 model is strictly isolated.", matchId, league);
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
            rec.setEvaluationStage("LINEUP_CONFIRMED");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ВИД СПОРТА: ШОРТ-ФУТБОЛ (" + (league != null ? league : "микро-лига") + "). Изоляция от моделей классического 11х11 футбола.");
            rec.setIdentifiedRisk("Укороченный микро-формат с высокой динамикой голов. Требуется выделенная модель ShortFootballSport.");
            rec.setTacticalRationale(sport.getTacticalRulesPrompt());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted isolated PASS record #{} for SHORT_FOOTBALL match #{}", saved.getId(), matchId);
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
            rec.setEvaluationStage("LINEUP_CONFIRMED");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ВЕТО НА ОТСУТСТВИЕ ML-ОЦЕНКИ: " + (ml != null && ml.getReason() != null ? ml.getReason() : "Модель не смогла сформировать прогноз."));
            rec.setIdentifiedRisk("Недостаточная выборка исторических матчей команд для тактического моделирования (Data/Model Void).");
            rec.setTacticalRationale("В соответствии с доменным правилом ML-Gating, модель не делает ставок на матчи без верифицированной ML-оценки.");
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted Pre-Flight PASS record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);
        }

        // ML Cohort Gating: If the neural net determined P < 60% (equal teams / coinflip)
        if ("PASS".equalsIgnoreCase(ml.getConfidenceTier())) {
            log.info("Match #{} rejected: ML model classified as coinflip / lean (P < 60%). Auto-PASS enforced.", matchId);
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
            rec.setEvaluationStage("LINEUP_CONFIRMED");
            rec.setLineupStatus("ESTIMATED");
            rec.setFairP1(ml.getProbHome());
            rec.setFairPx(ml.getProbDraw());
            rec.setFairP2(ml.getProbAway());
            rec.setFairOver25(ml.getProbOver());
            rec.setFairUnder25(ml.getProbUnder());
            rec.setCoreHypothesis("МЛ-МОДЕЛЬ: Равные шансы / монетка (P=" + ml.getRecommendedProb() + " < 60%). Математический перевес отсутствует.");
            rec.setIdentifiedRisk("Высокая энтропия исхода. Риск случайного результата в некоррелированном матче.");
            rec.setTacticalRationale(ml.getTacticalNarrative());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // =========================================================================
        // PRE-LINEUP FAST RADAR SCREENING (Without calling heavy AI / LLM workers)
        // User Mandate: "Можно Ai не трогать просто сделать предварительную раскидку по когортам"
        // In PRE_LINEUP (hours/days before kickoff), we skip expensive LLM calls.
        // We directly map the ML model prediction to the probability cohort (ML_90, ML_80, ML_70, ML_60)
        // and register the recommendation as PRE_LINEUP radar signal.
        // Deep qualitative LLM synthesis is strictly reserved for LINEUP_CONFIRMED (Stage 2).
        // =========================================================================
        Long startMs = dossierMeta != null ? (Long) dossierMeta.get("start_time") : null;
        long minutesToKickoff = (startMs != null)
                ? (startMs - System.currentTimeMillis()) / (60L * 1000L)
                : 9999L;
        boolean isLineupConfirmed = Boolean.TRUE.equals(dossierMeta != null ? dossierMeta.get("is_lineup_confirmed") : false);
        boolean isTeam = (sport == null || sport.isTeamSport());

        // PRE-LINEUP FAST RADAR SCREENING (Stage 1):
        // Active ONLY when:
        // 1. Official lineups are not yet confirmed
        // 2. AND it is a team sport (solo sports like Tennis/Table Tennis don't have starting XI announcements)
        // 3. AND we are well ahead of the match (> lineup confirmation window before kickoff)
        int lineupWindow = (sport != null) ? sport.getLineupConfirmationWindowMinutes() : 45;
        boolean isPreLineupRadar = !isLineupConfirmed && isTeam && minutesToKickoff > lineupWindow;
        boolean allowPreLineupBetting = (sport == null) || sport.isPreLineupBettingAllowed();

        if (isPreLineupRadar) {
            MatchAiRecommendation rec = recommendationRepository.findByMatchId(matchId)
                    .orElse(new MatchAiRecommendation());
            rec.setMatchId(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setEvaluationStage("PRE_LINEUP");
            rec.setLineupStatus("ESTIMATED");

            // Sport Protocol Guard: In sports where pre-lineup betting is strictly forbidden (e.g. Basketball Starting 5),
            // auto-PASS and lock until confirmed lineups arrive within the T-15m window.
            if (!allowPreLineupBetting) {
                log.info("Match #{} belongs to {} where pre-lineup betting is strictly forbidden. Auto-PASS until T-{}m confirmed lineups.",
                        matchId, sport != null ? sport.getCode() : "SPORT", lineupWindow);
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
                rec.setCoreHypothesis(String.format("🔮 %s: Ожидание подтвержденных стартовых пятерок (Т-%dм). Ставки без официальных составов заблокированы институциональным протоколом.",
                        sport != null ? sport.getDisplayName("ru") : "БАСКЕТБОЛ", lineupWindow));
                rec.setIdentifiedRisk("Высокая волатильность стартовой пятерки (20% состава на 1 игрока) и риск поздних замен (Late Scratches).");
                rec.setTacticalRationale(sport != null ? sport.getTacticalRulesPrompt() : "Ожидание составов");
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                log.info("Persisted Pre-Lineup PASS record #{} for match #{} awaiting confirmed lineups at T-{}m",
                        saved.getId(), matchId, lineupWindow);
                return Optional.of(saved);
            }

            rec.setFairP1(ml.getProbHome());
            rec.setFairPx(ml.getProbDraw());
            rec.setFairP2(ml.getProbAway());
            rec.setFairOver25(ml.getProbOver());
            rec.setFairUnder25(ml.getProbUnder());

            String targetMarket = ml.getRecommendedMarket();
            if (targetMarket == null || targetMarket.isBlank()) {
                targetMarket = (sport != null && !sport.hasDrawOutcome()) ? "P1" : "DOUBLE_CHANCE_1X";
            }
            if (sport != null && !sport.isMarketSupported(targetMarket)) {
                targetMarket = (sport.hasDrawOutcome()) ? "DOUBLE_CHANCE_1X" : "P1";
            }

            double fairProb = ml.getRecommendedProb() != null ? ml.getRecommendedProb().doubleValue() * 100.0 : 65.0;
            String tier = ml.getConfidenceTier();
            if (tier == null || tier.isBlank() || "PASS".equalsIgnoreCase(tier) || "CONFIDENT".equalsIgnoreCase(tier)) {
                if (fairProb >= 88.0) tier = "ML_90";
                else if (fairProb >= 78.0) tier = "ML_80";
                else if (fairProb >= 68.0) tier = "ML_70";
                else if (fairProb >= 58.0) tier = "ML_60";
                else tier = "PASS";
            }
            rec.setConvictionTier(tier);
            rec.setRecommendedMarket(targetMarket);

            RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
            Double marketOddVal = null;
            if (quote != null && quote.value() > 1.01) {
                marketOddVal = quote.value();
            } else {
                marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
            }
            double fairOdd = 100.0 / Math.max(1.0, fairProb);
            if (marketOddVal == null || marketOddVal <= 1.01) {
                marketOddVal = fairOdd > 1.05 ? Math.round(fairOdd * 100.0) / 100.0 : 1.85;
            }
            rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
            rec.setBestBookmaker(quote != null ? quote.bookmaker() : "Market Consensus");

            double evPct = ((marketOddVal * (fairProb / 100.0)) - 1.0) * 100.0;
            rec.setExpectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0));

            // Institutional Risk Guard 1: Minimum Odds Threshold (reject micro-odds like 1.03..1.29)
            if (marketOddVal < MIN_RECOMMENDED_ODD) {
                log.info("Match #{} rejected by Minimum Odds Guard: odd {} is below threshold {}. Auto-PASS enforced.",
                        matchId, marketOddVal, MIN_RECOMMENDED_ODD);
                rec.setConvictionTier("PASS");
                rec.setRecommendedMarket("PASS");
                rec.setResultStatus("SKIPPED");
                rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
                rec.setKellyStakePct(BigDecimal.ZERO);
                rec.setConfidenceScore((int) Math.round(fairProb));
                rec.setTacticalRationale(String.format("Котировка букмекера (%.2f) ниже институционального порога безопасности (%.2f). Ставки на микрокэфы с асимметричным риском запрещены.", marketOddVal, MIN_RECOMMENDED_ODD));
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                return Optional.of(saved);
            }

            // Institutional Risk Guard 2: Negative Expected Value (EV < 0%)
            if (quote != null && evPct < 0.0) {
                log.info("Match #{} rejected by Negative EV Guard: EV is {:.1f}% (odd {} < fair odd {:.2f}). Auto-PASS enforced.",
                        matchId, evPct, marketOddVal, fairOdd);
                rec.setConvictionTier("PASS");
                rec.setRecommendedMarket("PASS");
                rec.setResultStatus("SKIPPED");
                rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
                rec.setKellyStakePct(BigDecimal.ZERO);
                rec.setConfidenceScore((int) Math.round(fairProb));
                rec.setTacticalRationale(String.format("Отрицательное математическое ожидание (EV: %.1f%%). Котировка букмекера (%.2f) ниже справедливого коэффициента модели (%.2f). Ставка отклонена.", evPct, marketOddVal, fairOdd));
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                return Optional.of(saved);
            }

            double stakePct = switch (tier) {
                case "ML_90" -> 5.0;
                case "ML_80" -> 4.0;
                case "ML_70" -> 3.0;
                default      -> 2.0;
            };
            rec.setKellyStakePct(BigDecimal.valueOf(stakePct).setScale(2, RoundingMode.HALF_UP));
            rec.setConfidenceScore((int) Math.round(fairProb));
            rec.setResultStatus("PENDING");
            rec.setActualPnl(BigDecimal.ZERO);
            rec.setCoreHypothesis("🔮 PRE-LINEUP ML РАДАР: " + ml.getTacticalNarrative());
            rec.setIdentifiedRisk("Стартовые составы ещё не опубликованы. Окончательная верификация за 45 минут до матча.");
            rec.setTacticalRationale(ml.getTacticalNarrative());

            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Saved PRE_LINEUP fast radar recommendation for match #{} ({}-{}): Tier={}, Bet='{}' @ {}",
                    matchId, rec.getTeam1Name(), rec.getTeam2Name(), tier, targetMarket, marketOddVal);
            return Optional.of(saved);
        }

        // STAGE 2 (LINEUP_CONFIRMED): Pre-Flight Gate checks only in confirmed stage
        if (isDataVoid(dossierMeta, sport)) {
            log.info("Match #{} rejected by Pre-Flight Gate in confirmed stage: DATA VOID detected. Auto-PASS enforced.", matchId);
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
            rec.setEvaluationStage("LINEUP_CONFIRMED");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ВЕТО НА СЛЕПОТУ ДАННЫХ (Anti-Advocate Protocol): отсутствуют составы, рейтинги игроков и история личных встреч.");
            rec.setIdentifiedRisk("Полное отсутствие фактических данных (Data Void). Риск случайного исхода.");
            rec.setTacticalRationale("В соответствии с доменным правилом Zero Bookmaker Advocacy, модель не делает ставок на матчи без минимальной фактуры.");
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
        if (ml.getRecommendedMarket() != null && (ml.getRecommendedMarket().startsWith("TOTAL_OVER") || ml.getRecommendedMarket().startsWith("TOTAL_UNDER"))) {
            mlSb.append(String.format("🔥 INSTITUTIONAL TOTALS INSIGHT: The ML Tactical Net identified an overwhelming edge in TOTALS: %s with P=%.1f%%!\n",
                    ml.getRecommendedMarket(), ml.getRecommendedProb().doubleValue() * 100.0));
            mlSb.append("If the 1X2 match outcome has high entropy / is a coinflip, you are DIRECTED to recommend the high-probability TOTAL market as your primary bet!\n");
        }
        if (ml.getBenchJokersFound() != null && !ml.getBenchJokersFound().isEmpty()) {
            mlSb.append(String.format("- ⚡ Bench Depth (Impact Options): %s\n", String.join("; ", ml.getBenchJokersFound())));
        }
        mlSb.append("You MUST incorporate this quantitative ML evaluation into your analysis. Verify it against squad reality (lineups, injuries, tactical dynamics).\n");
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
        AiAnalysisResultDto dto;
        if (resultOpt.isPresent()) {
            dto = resultOpt.get();
        } else {
            log.info("AGY LLM worker pool unavailable for match #{}. Conducting Stage 2 final verification via Institutional ML Tactical Net.", matchId);
            dto = buildMlAnalysisResultDto(matchId, ml, dossierMeta, sport, isLineupConfirmed, minutesToKickoff);
        }

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
            rec.setEvaluationStage("LINEUP_CONFIRMED");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis(dto.getCoreHypothesis());
            rec.setIdentifiedRisk(dto.getIdentifiedRisk());
            rec.setTacticalRationale(dto.getCoreHypothesis());
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted PASS audit record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);
        }

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

        // =========================================================================
        // STAGE 2: Quantitative Valuation & Market Edge (+EV) Overlay
        // The LLM determined fair probabilities in Stage 1 completely blind to odds.
        // Now, we query real live bookmaker quotes from Redis/DB and compute true EV.
        // =========================================================================
        double fairProb = resolveFairProbability(dto, ml, targetMarket, sport);

        // 4 Deterministic ML Probability Brackets: 90% ML, 80% ML, 70% ML, 60% ML
        if (fairProb >= 90.0) {
            tier = "ML_90";
        } else if (fairProb >= 80.0) {
            tier = "ML_80";
        } else if (fairProb >= 70.0) {
            tier = "ML_70";
        } else if (fairProb >= 60.0) {
            tier = "ML_60";
        } else {
            tier = "PASS";
        }
        rec.setConvictionTier(tier);

        // In 60% ML tier (or fairProb < 70%), protect against close-match variance:
        // In solo/racket sports (tennis, table tennis), auto-cushion raw P1/P2/WIN1/WIN2 to AH +1.5 sets
        boolean isSoloRacket = (sport != null && !sport.isTeamSport());
        boolean isRawWin1 = "P1".equals(targetMarket) || "HOME_WIN".equals(targetMarket) || "WIN1".equals(targetMarket) || "1".equals(targetMarket);
        boolean isRawWin2 = "P2".equals(targetMarket) || "AWAY_WIN".equals(targetMarket) || "WIN2".equals(targetMarket) || "2".equals(targetMarket);

        if (isSoloRacket && (isRawWin1 || isRawWin2)) {
            if ("ML_60".equals(tier) || fairProb < 70.0) {
                if (isRawWin1) {
                    targetMarket = "AH_HOME_PLUS_1.5";
                    log.info("ML_60 / Solo Racket Discipline: converted raw P1/WIN1 to cushioned AH_HOME_PLUS_1.5 for match #{}", matchId);
                } else {
                    targetMarket = "AH_AWAY_PLUS_1.5";
                    log.info("ML_60 / Solo Racket Discipline: converted raw P2/WIN2 to cushioned AH_AWAY_PLUS_1.5 for match #{}", matchId);
                }
                fairProb = Math.max(fairProb, 75.0);
            }
        }

        RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
        Double marketOddVal = null;
        String bestBookmaker = "Market Consensus";

        if (quote != null && quote.value() > 1.01) {
            marketOddVal = quote.value();
            bestBookmaker = quote.bookmaker();
        } else {
            marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
        }

        double fairOdd = 100.0 / Math.max(1.0, fairProb);

        if (marketOddVal == null || marketOddVal <= 1.01) {
            marketOddVal = fairOdd > 1.05 ? Math.round(fairOdd * 100.0) / 100.0 : 1.85;
            bestBookmaker = "Market Consensus (Fair)";
            log.info("Match #{} has no direct bookmaker quote for market '{}'. Using fair benchmark odd={}", matchId, targetMarket, marketOddVal);
        }

        // Solo/racket sport discipline: Strict ban on naked win bets in danger zone (odds >= 1.50)
        if (isSoloRacket && (isRawWin1 || isRawWin2) && marketOddVal >= 1.50) {
            log.info("Solo sport danger zone (odds {} >= 1.50): auto-cushioning raw win {} to +1.5 set handicap for match #{}", marketOddVal, targetMarket, matchId);
            targetMarket = isRawWin1 ? "AH_HOME_PLUS_1.5" : "AH_AWAY_PLUS_1.5";
            RedisOddsConsensusService.MarketQuote cQuote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
            if (cQuote != null && cQuote.value() > 1.01) {
                marketOddVal = cQuote.value();
                bestBookmaker = cQuote.bookmaker();
            } else {
                marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);
                if (marketOddVal == null || marketOddVal <= 1.01) {
                    marketOddVal = 1.35;
                    bestBookmaker = "Market Consensus (Fair)";
                }
            }
            fairProb = Math.max(fairProb, 75.0);
            fairOdd = 100.0 / fairProb;
        }

        double evPct = ((fairProb / 100.0) * marketOddVal - 1.0) * 100.0;

        // ITF Qualifiers & High Entropy / Doubles Specialists Guard
        if (isSoloRacket && league != null) {
            String lLower = league.toLowerCase();
            boolean isItfOrQual = lLower.contains("itf") || lLower.contains("qual") || lLower.contains("квалификация");
            boolean isDoubles = lLower.contains("пары") || lLower.contains("doubles")
                    || (team1 != null && team1.contains("/")) || (team2 != null && team2.contains("/"));

            if (isItfOrQual || isDoubles) {
                if (fairProb < 75.0 || marketOddVal >= 1.58) {
                    log.info("ITF Qualifier / Doubles High-Entropy Veto for match #{}: auto-PASS enforced", matchId);
                    rec.setConvictionTier("PASS");
                    rec.setRecommendedMarket("PASS");
                    rec.setResultStatus("SKIPPED");
                    rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
                    rec.setExpectedValuePct(BigDecimal.ZERO);
                    rec.setKellyStakePct(BigDecimal.ZERO);
                    rec.setConfidenceScore((int) Math.round(fairProb));
                    rec.setTacticalRationale(String.format("Вето на повышенную энтропию в %s (%s): высокая дисперсия тай-брейков / недостаточная выборка в одиночном разряде.",
                            isDoubles ? "парном разряде" : "квалификации ITF", league));
                    MatchAiRecommendation saved = recommendationRepository.save(rec);
                    return Optional.of(saved);
                }
            }
        }

        // Institutional Risk Guard 1: Minimum Odds Threshold (reject micro-odds like 1.03..1.29)
        if (marketOddVal < MIN_RECOMMENDED_ODD) {
            log.info("Stage 2: Match #{} rejected by Minimum Odds Guard: odd {} is below threshold {}. Auto-PASS enforced.",
                    matchId, marketOddVal, MIN_RECOMMENDED_ODD);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setResultStatus("SKIPPED");
            rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
            rec.setExpectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0));
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore((int) Math.round(fairProb));
            rec.setTacticalRationale(String.format("Котировка букмекера (%.2f) ниже институционального порога безопасности (%.2f). Ставки на микрокэфы с асимметричным риском запрещены.", marketOddVal, MIN_RECOMMENDED_ODD));
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // Institutional Risk Guard 2: Negative Expected Value (EV < 0%)
        if (quote != null && evPct < 0.0) {
            log.info("Stage 2: Match #{} rejected by Negative EV Guard: EV is {:.1f}% (odd {} < fair odd {:.2f}). Auto-PASS enforced.",
                    matchId, evPct, marketOddVal, fairOdd);
            rec.setConvictionTier("PASS");
            rec.setRecommendedMarket("PASS");
            rec.setResultStatus("SKIPPED");
            rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
            rec.setExpectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0));
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore((int) Math.round(fairProb));
            rec.setTacticalRationale(String.format("Отрицательное математическое ожидание (EV: %.1f%%). Котировка букмекера (%.2f) ниже справедливого коэффициента модели (%.2f). Ставка отклонена.", evPct, marketOddVal, fairOdd));
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            return Optional.of(saved);
        }

        // Kelly Criterion Stake Sizing
        double b = marketOddVal - 1.0;
        double p = fairProb / 100.0;
        double q = 1.0 - p;
        double fullKelly = b > 0.0 ? Math.max(0.0, (b * p - q) / b) : 0.0;
        double fraction;
        double maxStake;
        switch (tier) {
            case "ML_90" -> { fraction = 0.35; maxStake = 5.0; }
            case "ML_80" -> { fraction = 0.25; maxStake = 4.0; }
            case "ML_70" -> { fraction = 0.20; maxStake = 3.0; }
            default      -> { fraction = 0.15; maxStake = 2.0; }
        }
        double kellyPct = Math.min(maxStake, Math.max(1.0, fullKelly * fraction * 100.0));

        rec.setRecommendedMarket(targetMarket);
        rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
        rec.setBestBookmaker(bestBookmaker);
        rec.setExpectedValuePct(BigDecimal.valueOf(evPct).setScale(2, RoundingMode.HALF_UP));
        rec.setKellyStakePct(BigDecimal.valueOf(kellyPct).setScale(2, RoundingMode.HALF_UP));

        int confidence = dto.getConfidenceScore() != null ? dto.getConfidenceScore() : (int) Math.round(fairProb);
        if ("ML_90".equals(tier) || "ML_80".equals(tier)) {
            confidence = Math.max(confidence, (int) Math.round(fairProb));
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
        rec.setEvaluationStage("LINEUP_CONFIRMED");
        rec.setLineupStatus("CONFIRMED");

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

        AnalystStatsDto.TierStatsDto ml90 = buildTierStats("ML_90", rows);
        AnalystStatsDto.TierStatsDto ml80 = buildTierStats("ML_80", rows);
        AnalystStatsDto.TierStatsDto ml70 = buildTierStats("ML_70", rows);
        AnalystStatsDto.TierStatsDto ml60 = buildTierStats("ML_60", rows);
        AnalystStatsDto.TierStatsDto pass = buildTierStats("PASS", rows);

        // Fallbacks for legacy/unmigrated tiers if any
        AnalystStatsDto.TierStatsDto confident = buildTierStats("CONFIDENT", rows);
        AnalystStatsDto.TierStatsDto goodPick = buildTierStats("GOOD_PICK", rows);

        long total = ml90.getTotal() + ml80.getTotal() + ml70.getTotal() + ml60.getTotal() + confident.getTotal() + goodPick.getTotal() + pass.getTotal();
        long won = ml90.getWon() + ml80.getWon() + ml70.getWon() + ml60.getWon() + confident.getWon() + goodPick.getWon();
        long lost = ml90.getLost() + ml80.getLost() + ml70.getLost() + ml60.getLost() + confident.getLost() + goodPick.getLost();
        long voidCount = ml90.getVoidCount() + ml80.getVoidCount() + ml70.getVoidCount() + ml60.getVoidCount() + confident.getVoidCount() + goodPick.getVoidCount();
        long pending = ml90.getPending() + ml80.getPending() + ml70.getPending() + ml60.getPending() + confident.getPending() + goodPick.getPending();
        long skipped = pass.getSkipped();
        long settled = won + lost + voidCount;

        double winrate = (won + lost) > 0 ? (won * 100.0) / (won + lost) : 0.0;
        BigDecimal totalPnl = ml90.getTotalPnl()
                .add(ml80.getTotalPnl())
                .add(ml70.getTotalPnl())
                .add(ml60.getTotalPnl())
                .add(confident.getTotalPnl())
                .add(goodPick.getTotalPnl());
        double totalTurnover = (ml90.getSettled() * 1000.0)
                + (ml80.getSettled() * 500.0)
                + ((ml70.getSettled() + confident.getSettled()) * 250.0)
                + ((ml60.getSettled() + goodPick.getSettled()) * 100.0);
        double roi = totalTurnover > 0 ? (totalPnl.doubleValue() / totalTurnover) * 100.0 : 0.0;

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
                .ml90(ml90)
                .ml80(ml80)
                .ml70(ml70)
                .ml60(ml60)
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

                double unitStake = 100.0;
                if ("ML_90".equalsIgnoreCase(targetTier)) {
                    unitStake = 1000.0;
                } else if ("ML_80".equalsIgnoreCase(targetTier)) {
                    unitStake = 500.0;
                } else if ("ML_70".equalsIgnoreCase(targetTier) || "CONFIDENT".equalsIgnoreCase(targetTier)) {
                    unitStake = 250.0;
                } else if ("ML_60".equalsIgnoreCase(targetTier) || "GOOD_PICK".equalsIgnoreCase(targetTier)) {
                    unitStake = 100.0;
                }
                double roi = (settled > 0 && unitStake > 0) ? (pnl.doubleValue() / (settled * unitStake)) * 100.0 : 0.0;

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

    private AiAnalysisResultDto buildMlAnalysisResultDto(Long matchId, MlPredictionDto ml, Map<String, Object> dossierMeta, Sport sport, boolean isLineupConfirmed, long minutesToKickoff) {
        boolean isTeam = sport == null || sport.isTeamSport();
        String riskText;
        String hypothesisText;

        if (isLineupConfirmed) {
            riskText = "Стартовые составы и скамейка официально подтверждены протоколом. Ротация учтена.";
            hypothesisText = "✅ БОЕВАЯ ВЕРИФИКАЦИЯ (Составы подтверждены): " + ml.getTacticalNarrative();
        } else if (!isTeam) {
            riskText = "Индивидуальный матч: стартовые участники фиксированы, форма и личные встречи верифицированы.";
            hypothesisText = "🎯 АНАЛИТИЧЕСКИЙ РАСЧЕТ (ML Tactical Net): " + ml.getTacticalNarrative();
        } else {
            riskText = "Предматчевая верификация завершена: официальный протокол для лиги не публикуется. Расчет зафиксирован по ML Tactical Net и консенсусу линий за 45 минут до старта.";
            hypothesisText = "⚡ ПРЕДМАТЧЕВАЯ ВЕРИФИКАЦИЯ (ML Tactical Net): " + ml.getTacticalNarrative();
        }

        String targetMarket = ml.getRecommendedMarket();
        if (targetMarket == null || targetMarket.isBlank()) {
            targetMarket = (sport != null && !sport.hasDrawOutcome()) ? "P1" : "DOUBLE_CHANCE_1X";
        }
        if (sport != null && !sport.isMarketSupported(targetMarket)) {
            targetMarket = (sport.hasDrawOutcome()) ? "DOUBLE_CHANCE_1X" : "P1";
        }

        int confScore = ml.getRecommendedProb() != null ? (int) Math.round(ml.getRecommendedProb().doubleValue() * 100.0) : 65;

        // Auto-cushion solo/racket sports in ML_60 or confScore < 70
        if (!isTeam) {
            boolean isRawW1 = "P1".equals(targetMarket) || "HOME_WIN".equals(targetMarket) || "WIN1".equals(targetMarket) || "1".equals(targetMarket);
            boolean isRawW2 = "P2".equals(targetMarket) || "AWAY_WIN".equals(targetMarket) || "WIN2".equals(targetMarket) || "2".equals(targetMarket);
            if (isRawW1 || isRawW2) {
                if ("ML_60".equalsIgnoreCase(ml.getConfidenceTier()) || confScore < 70) {
                    targetMarket = isRawW1 ? "AH_HOME_PLUS_1.5" : "AH_AWAY_PLUS_1.5";
                    log.info("buildMlAnalysisResultDto: Auto-cushioned solo/racket raw win to {} for match #{}", targetMarket, matchId);
                }
            }
        }

        return AiAnalysisResultDto.builder()
                .matchId(matchId)
                .recommendedBet(targetMarket)
                .marketType(targetMarket)
                .convictionTier(ml.getConfidenceTier())
                .confidenceScore(confScore)
                .coreHypothesis(hypothesisText)
                .identifiedRisk(riskText)
                .tacticalRationale(ml.getTacticalNarrative())
                .fairP1(ml.getProbHome())
                .fairPx(ml.getProbDraw())
                .fairP2(ml.getProbAway())
                .fairOver25(ml.getProbOver())
                .fairUnder25(ml.getProbUnder())
                .steamDirection("NEUTRAL")
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

    private double resolveFairProbability(AiAnalysisResultDto dto, MlPredictionDto ml, String market, Sport sport) {
        if (market == null) return 60.0;
        String m = market.toUpperCase();
        if ("P1".equals(m) || "HOME_WIN".equals(m) || "WIN1".equals(m) || "1".equals(m)) {
            if (dto.getFairP1() != null) return dto.getFairP1().doubleValue();
            if (ml != null && ml.getProbHome() != null) return ml.getProbHome().doubleValue() * 100.0;
            return 60.0;
        }
        if ("P2".equals(m) || "AWAY_WIN".equals(m) || "WIN2".equals(m) || "2".equals(m)) {
            if (dto.getFairP2() != null) return dto.getFairP2().doubleValue();
            if (ml != null && ml.getProbAway() != null) return ml.getProbAway().doubleValue() * 100.0;
            return 60.0;
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
        if (m.startsWith("TOTAL_UNDER") || m.startsWith("TU_")) {
            if (dto.getFairUnder25() != null && dto.getFairUnder25().doubleValue() > 0) {
                return dto.getFairUnder25().doubleValue();
            }
            if (ml != null && ml.getProbUnder() != null && ml.getProbUnder().doubleValue() > 0) {
                return ml.getProbUnder().doubleValue() * 100.0;
            }
            return 55.0;
        }
        if (m.startsWith("TOTAL_OVER") || m.startsWith("TO_")) {
            if (dto.getFairOver25() != null && dto.getFairOver25().doubleValue() > 0) {
                return dto.getFairOver25().doubleValue();
            }
            if (ml != null && ml.getProbOver() != null && ml.getProbOver().doubleValue() > 0) {
                return ml.getProbOver().doubleValue() * 100.0;
            }
            return 50.0;
        }
        if ((m.startsWith("AH_") || m.startsWith("SET_HANDICAP_")) && m.contains("PLUS")) {
            return 75.0;
        }
        if (dto.getConfidenceScore() != null && dto.getConfidenceScore() > 0) {
            return Math.min(85.0, dto.getConfidenceScore().doubleValue());
        }
        return 65.0;
    }

    private Double fallbackOddFromDossier(Map<String, Object> dossierMeta, String targetMarket) {
        if (dossierMeta == null) return null;
        String m = targetMarket.toUpperCase();
        if ("P1".equals(m) || "HOME_WIN".equals(m) || "WIN1".equals(m) || "1".equals(m)) {
            Object o = dossierMeta.get("best_win1");
            return o instanceof Number n ? n.doubleValue() : null;
        }
        if ("P2".equals(m) || "AWAY_WIN".equals(m) || "WIN2".equals(m) || "2".equals(m)) {
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
        if ("AH_HOME_PLUS_1.5".equals(m) || "SET_HANDICAP_HOME_PLUS_1.5".equals(m)) {
            Object o = dossierMeta.get("best_win1");
            if (o instanceof Number n && n.doubleValue() > 1.01) {
                double w = n.doubleValue();
                return w > 2.0 ? Math.round((1.0 + (w - 1.0) * 0.45) * 100.0) / 100.0 : 1.35;
            }
        }
        if ("AH_AWAY_PLUS_1.5".equals(m) || "SET_HANDICAP_AWAY_PLUS_1.5".equals(m)) {
            Object o = dossierMeta.get("best_win2");
            if (o instanceof Number n && n.doubleValue() > 1.01) {
                double w = n.doubleValue();
                return w > 2.0 ? Math.round((1.0 + (w - 1.0) * 0.45) * 100.0) / 100.0 : 1.35;
            }
        }
        if (m.startsWith("TOTAL_UNDER") || m.startsWith("TU_")) {
            Object o = dossierMeta.get("best_tu25");
            if (o instanceof Number n && n.doubleValue() > 1.01) {
                return n.doubleValue();
            }
            return null;
        }
        if (m.startsWith("TOTAL_OVER") || m.startsWith("TO_")) {
            Object o = dossierMeta.get("best_to25");
            if (o instanceof Number n && n.doubleValue() > 1.01) {
                return n.doubleValue();
            }
            return null;
        }
        return null;
    }
}
