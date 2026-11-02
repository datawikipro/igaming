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
import pro.datawiki.igaming.analyst.dto.ModelVersionDto;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;
import pro.datawiki.igaming.analyst.entity.AiModelVersion;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiLearningMemoryRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.analyst.state.RecommendationStateMachine;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.Language;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private final MlInferenceClient mlInferenceClient;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final AiLearningMemoryRepository memoryRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final RecommendationStateMachine stateMachine;
    private final pro.datawiki.igaming.analyst.repository.AiPolicyInterventionRepository policyInterventionRepository;
    private final StrategyEngineService strategyEngineService;
    private final AnalystStatisticsService statisticsService;
    public static final String CURRENT_MODEL_VERSION = "v0.3.3.1.1";

    public MatchAiRecommendation getOrCreateRecommendation(Long matchId) {

        return recommendationRepository.findByMatchIdAndModelVersion(matchId, CURRENT_MODEL_VERSION)

                .orElseGet(() -> {

                    MatchAiRecommendation r = new MatchAiRecommendation();
                    r.setMatchId(matchId);
                    r.setModelVersion(CURRENT_MODEL_VERSION);
                    return r;
                });

    }

    @Value("classpath:AGENTS_ANALYST.md")
    private Resource constitutionResource;
    private String analystConstitution = "";

    @Value("${analyst.scheduling.max-slots:150}")
    private int maxSlots;

    @Value("${analyst.scheduling.batch-size:20}")
    private int configuredBatchSize;
    public static final double MIN_RECOMMENDED_ODD = 1.30;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
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

        // Trigger background backfill of available_bookmakers for active records

        new Thread(this::backfillAvailableBookmakers, "bookmaker-backfill-thread").start();

    }

    public void backfillAvailableBookmakers() {

        try {

            Thread.sleep(5000);
            log.info("Starting background backfill of available_bookmakers for active recommendations...");
            List<MatchAiRecommendation> pending = recommendationRepository.findByResultStatus("PENDING");
            int updated = 0;

            for (MatchAiRecommendation rec : pending) {

                if (rec.getMatchId() != null) {

                    boolean changed = false;
                    Set<String> bks = redisOddsConsensusService.getAvailableBookmakers(rec.getMatchId());
                    String bksJoined = (bks != null && !bks.isEmpty()) ? String.join(",", bks) : "";

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

    /**

     * Evaluates a specific match and stores the recommendation in PostgreSQL.

     */

    @Transactional

    public Optional<MatchAiRecommendation> evaluateMatch(Long matchId) {

        return evaluateMatchInternal(matchId, false);

    }

    /**

     * Force-evaluates a match, bypassing the team schedule collision quarantine

     * (e.g. after ground truth resolution by Server-Side Anomaly Resolver Robot).

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
            String resStatus = existing.getResultStatus();
            String evalStage = existing.getEvaluationStage();

            if ("WON".equalsIgnoreCase(resStatus) || "LOST".equalsIgnoreCase(resStatus) || "VOID".equalsIgnoreCase(resStatus) || "SETTLED".equalsIgnoreCase(evalStage)) {

                log.info("Match #{} recommendation #{} is already settled (status={}, stage={}). Skipping re-evaluation.",

                        matchId, existing.getId(), resStatus, evalStage);
                return existingOpt;

            }

        }

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

        // Domain Guard: Unresolved raw placeholder leagues ("League 9661")

        if (league != null) {

            String lLower = league.toLowerCase().trim();

            if (lLower.matches("^league\\s*\\d+$")) {

                log.info("Match #{} belongs to unmapped placeholder league ('{}'). Evaluation deferred until MDM resolution.", matchId, league);
                MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
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
                rec.setEvaluationStage("SKIPPED_UNMAPPED_LEAGUE");
                rec.setLineupStatus("ESTIMATED");
                rec.setCoreHypothesis("НЕРАЗРЕШЕННАЯ ЛИГА-ЗАГЛУШКА (" + league + "). Ожидается маппинг турнира в MDM для получения достоверных стат-метрик.");
                rec.setIdentifiedRisk("Отсутствует профиль турнира в MDM.");
                rec.setTacticalRationale("Аналитический пайплайн требует валидного канонического профиля соревнования для оценки.");
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                return Optional.of(saved);

            }

        }

        Long startMs = dossierMeta != null ? (Long) dossierMeta.get("start_time") : null;

        // Physical Reality Invariant: Team Overlap Check

        // A single team cannot participate in two different matches simultaneously (or within sport-specific minimum interval)

        String t1 = dossierMeta != null ? (String) dossierMeta.get("team1_name") : null;
        String t2 = dossierMeta != null ? (String) dossierMeta.get("team2_name") : null;

        if (!bypassConflictCheck && startMs != null && (t1 != null || t2 != null)) {

            LocalDateTime matchStartLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMs), ZoneOffset.UTC);
            int intervalMinutes = (sport != null) ? sport.getMinimumMatchIntervalMinutes() : 120;
            LocalDateTime windowStart = matchStartLdt.minusMinutes(intervalMinutes);
            LocalDateTime windowEnd = matchStartLdt.plusMinutes(intervalMinutes);
            List<MatchAiRecommendation> activeRecs = recommendationRepository.findByResultStatus("PENDING");

            for (MatchAiRecommendation existing : activeRecs) {

                if (existing.getMatchId() != null && !existing.getMatchId().equals(matchId) && existing.getMatchStartTime() != null) {

                    if (existing.getMatchStartTime().isAfter(windowStart) && existing.getMatchStartTime().isBefore(windowEnd)) {

                        boolean t1Conflict = (t1 != null && (t1.equalsIgnoreCase(existing.getTeam1Name()) || t1.equalsIgnoreCase(existing.getTeam2Name())));
                        boolean t2Conflict = (t2 != null && (t2.equalsIgnoreCase(existing.getTeam1Name()) || t2.equalsIgnoreCase(existing.getTeam2Name())));

                        if (t1Conflict || t2Conflict) {

                            String conflictTeam = t1Conflict ? t1 : t2;

                            log.warn("⚠️ [SCHEDULE_COLLISION] Team '{}' in match #{} ({} vs {}, {}) conflicts with existing match #{} ({} vs {}, {})! Quarantining both and emitting anomaly.",

                                    conflictTeam, matchId, t1, t2, league, existing.getMatchId(), existing.getTeam1Name(), existing.getTeam2Name(), existing.getLeagueName());
                            MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
                            populateMetadata(rec, dossierMeta, sport);
                            rec.setConvictionTier("PASS");
                            rec.setRecommendedMarket("PASS");
                            rec.setRecommendedOdd(BigDecimal.ONE);
                            rec.setBestBookmaker("NONE");
                            rec.setExpectedValuePct(BigDecimal.ZERO);
                            rec.setKellyStakePct(BigDecimal.ZERO);
                            rec.setConfidenceScore(0);
                            rec.setResultStatus("CONFLICT");
                            rec.setActualPnl(BigDecimal.ZERO);
                            rec.setEvaluationStage("SCHEDULE_CONFLICT_PENDING_RESOLUTION");
                            rec.setLineupStatus("ESTIMATED");
                            rec.setCoreHypothesis(String.format("КОНФЛИКТ РАСПИСАНИЯ: Команда '%s' задействована в матче #%d и матче #%d. Находится в очереди ошибок на проверку роботом.", conflictTeam, existing.getMatchId(), matchId));
                            rec.setIdentifiedRisk("Физическая невозможность участия одной команды в двух параллельных матчах (дубликат/кросс-матч).");
                            rec.setTacticalRationale(String.format("В соответствии с инвариантом физической реальности Sport (%s), оба матча отправлены в карантин до автоматического разрешения серверным роботом.", sport.getCode()));
                            MatchAiRecommendation saved = recommendationRepository.save(rec);

                            // Also freeze existing match if active

                            if (!"CANCELLED".equalsIgnoreCase(existing.getResultStatus()) && !"WON".equalsIgnoreCase(existing.getResultStatus()) && !"LOST".equalsIgnoreCase(existing.getResultStatus())) {

                                existing.setEvaluationStage("SCHEDULE_CONFLICT_PENDING_RESOLUTION");
                                existing.setResultStatus("CONFLICT");
                                existing.setConvictionTier("PASS");
                                existing.setRecommendedMarket("PASS");
                                existing.setRecommendedOdd(BigDecimal.ONE);
                                existing.setExpectedValuePct(BigDecimal.ZERO);
                                existing.setKellyStakePct(BigDecimal.ZERO);
                                existing.setCoreHypothesis(String.format("КОНФЛИКТ РАСПИСАНИЯ: Команда '%s' задействована в матче #%d и матче #%d. Находится в очереди ошибок на проверку роботом.", conflictTeam, existing.getMatchId(), matchId));
                                recommendationRepository.save(existing);

                            }

                            // Emit anomaly event to MDM error queue

                            reportScheduleCollisionAnomaly(matchId, existing.getMatchId(), conflictTeam, sport.getSlug(), t1, t2, league, existing);
                            return Optional.of(saved);

                        }

                    }

                }

            }

        }

        long minutesToKickoff = (startMs != null)

                ? (startMs - System.currentTimeMillis()) / (60L * 1000L)

                : 9999L;
        boolean isLineupConfirmed = Boolean.TRUE.equals(dossierMeta != null ? dossierMeta.get("is_lineup_confirmed") : false);
        boolean isTeam = (sport == null || sport.isTeamSport());
        int lineupWindow = (sport != null) ? sport.getLineupConfirmationWindowMinutes() : 45;
        boolean allowPreLineupBetting = (sport == null) || sport.isPreLineupBettingAllowed();
        boolean isKickoffWait = !isLineupConfirmed && isTeam && !allowPreLineupBetting && minutesToKickoff <= lineupWindow && minutesToKickoff > 1;

        // ── Unresolved Player Entity Guard for Individual/Solo Sports (Zero-Garbage Protocol) ──

        // In 1v1 sports (Tennis, Table Tennis, Combat), predicting an outcome when one or both participants

        // have 0 historical matches and no verified DDS Ground Truth identity produces garbage ("Мусор -> ML -> Мусор").

        if (!isTeam) {

            List<?> t1Hist = dossierMeta != null ? (List<?>) dossierMeta.get("team1_recent_matches") : null;
            List<?> t2Hist = dossierMeta != null ? (List<?>) dossierMeta.get("team2_recent_matches") : null;
            Long s1Id = (dossierMeta != null && dossierMeta.get("team1_sofascore_id") instanceof Number n1) ? n1.longValue() : null;
            Long s2Id = (dossierMeta != null && dossierMeta.get("team2_sofascore_id") instanceof Number n2) ? n2.longValue() : null;
            boolean p1Unresolved = (t1Hist == null || t1Hist.isEmpty()) && (s1Id == null || s1Id <= 0);
            boolean p2Unresolved = (t2Hist == null || t2Hist.isEmpty()) && (s2Id == null || s2Id <= 0);

            if (p1Unresolved || p2Unresolved) {

                String unknownName = p1Unresolved ? (t1 != null ? t1 : "Player 1") : (t2 != null ? t2 : "Player 2");

                log.warn("⚠️ [UNRESOLVED_PLAYER_ENTITY] Rejecting bet on match #{}: participant '{}' has 0 verified historical matches and no DDS ground truth. Forcing PASS.",

                        matchId, unknownName);
                MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
                populateMetadata(rec, dossierMeta, sport);
                rec.setConvictionTier("PASS");
                rec.setRecommendedMarket("PASS");
                rec.setRecommendedOdd(BigDecimal.ONE);
                rec.setBestBookmaker("NONE");
                rec.setExpectedValuePct(BigDecimal.ZERO);
                rec.setKellyStakePct(BigDecimal.ZERO);
                rec.setConfidenceScore(0);
                rec.setEvaluationStage("UNRESOLVED_PLAYER_PASS");
                rec.setResultStatus("PASS");
                rec.setActualPnl(BigDecimal.ZERO);
                rec.setErrorCategory("UNRESOLVED_PLAYER_ENTITY");
                rec.setLessonLearned("⚠️ Ошибка сущности игрока: недостаточная история матчей или неразрешенное имя. Задействован Zero-Garbage Gatekeeper.");
                rec.setCoreHypothesis(String.format("⚠️ ПРОПУСК МАТЧА: Участник '%s' не верифицирован в базе DDS (0 подтвержденных матчей). Защита от мусорных данных (Zero-Garbage Invariant).", unknownName));
                rec.setIdentifiedRisk("Отсутствие исторической телеметрии по игроку искажает байесовские априорные вероятности модели.");
                rec.setTacticalRationale("В соответствии с институциональным регламентом, ставки на юниорские/квалификационные матчи с неизвестными участниками строго блокируются.");
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                return Optional.of(saved);

            }

        }

        // STAGE 0: ML TACTICAL NEURAL NET EVALUATION (5 Sports Multi-Task Model)

        // User Mandate: "если нет ML оценки даже сразу пасуем"

        MlPredictionDto ml = mlInferenceClient.predict(matchId, dossierMeta);
        if (ml != null && dossierMeta != null) {
            if (ml.getEffectiveBaseline() != null) dossierMeta.put("expected_total_baseline", ml.getEffectiveBaseline());
            if (ml.getEffectiveCushionLow() != null) dossierMeta.put("tournament_cushion_low", ml.getEffectiveCushionLow());
            if (ml.getEffectiveCushionHigh() != null) dossierMeta.put("tournament_cushion_high", ml.getEffectiveCushionHigh());
            if (ml.getEffectiveProbCushionLow() != null) dossierMeta.put("prob_cushion_low", ml.getEffectiveProbCushionLow());
            if (ml.getEffectiveProbCushionHigh() != null) dossierMeta.put("prob_cushion_high", ml.getEffectiveProbCushionHigh());
        }

        // KICKOFF IN-PLAY LINEUP VERIFICATION PROTOCOL (WAIT_KICKOFF):

        // In sports where pre-lineup betting is forbidden (e.g. Football 11x11, Basketball Starting 5, Hockey, Baseball),

        // if official lineups are still unconfirmed at T-45m..T-2m, do NOT bet in the dark before kickoff.

        // Instead, transition to WAIT_KICKOFF: hold the bet until minute 0-2 when the referee blows the whistle

        // and the actual players on the pitch are registered by ground-truth trackers.

        if (isKickoffWait) {

            log.info("Match #{} (starts in {}m): official lineups not yet available. Auto-WAIT until Kickoff (Minute 1 on-pitch verification).",

                    matchId, minutesToKickoff);
            MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setEvaluationStage("WAIT_KICKOFF");
            rec.setLineupStatus("ESTIMATED");
            rec.setConvictionTier("WAIT");
            rec.setRecommendedMarket("WAIT");
            rec.setRecommendedOdd(BigDecimal.ONE);
            rec.setBestBookmaker("NONE");
            rec.setExpectedValuePct(BigDecimal.ZERO);
            rec.setKellyStakePct(BigDecimal.ZERO);
            rec.setConfidenceScore(0);
            rec.setResultStatus("WAITING");
            rec.setActualPnl(BigDecimal.ZERO);

            if (ml != null && ml.isHasMlEvaluation()) {

                rec.setFairP1(ml.getProbHome());
                rec.setFairPx(ml.getProbDraw());
                rec.setFairP2(ml.getProbAway());
                rec.setFairOver25(ml.getProbOver());
                rec.setFairUnder25(ml.getProbUnder());
                enrichRecommendationWithLineupAndScenarios(rec, ml);

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
            MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
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
            rec.setEvaluationStage((isLineupConfirmed || !isTeam) ? "FINAL_PASS" : "PRE_LINEUP");
            rec.setLineupStatus("ESTIMATED");
            rec.setCoreHypothesis("ОЖИДАНИЕ ML-ОЦЕНКИ: " + (ml != null && ml.getReason() != null ? ml.getReason() : "Модель формирует прогноз."));
            rec.setIdentifiedRisk("Недостаточная выборка исторических матчей команд для тактического моделирования (Data/Model Void).");
            rec.setTacticalRationale("Аналитический расчет ожидает квантовой оценки от сервиса ML-инференса.");
            MatchAiRecommendation saved = recommendationRepository.save(rec);
            log.info("Persisted Pre-Flight PASS record #{} for match #{}", saved.getId(), matchId);
            return Optional.of(saved);

        }

        // Constitution Rule 4: No Artificial Vetoes.

        // If ML confidence tier is PASS, normalize to ML_60 cohort to retain for post-factum forensics.

        // =========================================================================

        // PRE-LINEUP FAST RADAR SCREENING (Without calling heavy AI / LLM workers)

        // User Mandate: "Можно Ai не трогать просто сделать предварительную раскидку по когортам"

        // In PRE_LINEUP (hours/days before kickoff), we skip expensive LLM calls.

        // We directly map the ML model prediction to the probability cohort (ML_90, ML_80, ML_70, ML_60)

        // and register the recommendation as PRE_LINEUP radar signal.

        // Deep qualitative LLM synthesis is strictly reserved for LINEUP_CONFIRMED (Stage 2).

        // =========================================================================

        // PRE-LINEUP FAST RADAR SCREENING (Stage 1):

        // Active ONLY when:

        // 1. Official lineups are not yet confirmed

        // 2. AND it is a team sport (solo sports like Tennis/Table Tennis don't have starting XI announcements)

        // 3. AND we are well ahead of the match (> lineup confirmation window before kickoff)

        boolean isPreLineupRadar = !isLineupConfirmed && isTeam && minutesToKickoff > lineupWindow;

        if (isPreLineupRadar) {

            MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
            populateMetadata(rec, dossierMeta, sport);
            rec.setEvaluationStage("PRE_LINEUP");

            if (Boolean.TRUE.equals(ml.getIsLineupProjected())) {

                rec.setLineupStatus("PROJECTED");

            } else {

                rec.setLineupStatus("ESTIMATED");

            }

            // Stage 1: Pre-Lineup Fast Radar Screening

            // Even before confirmed official lineups, evaluate fair probabilities from ML model,

            // populate expected markets/odds, and evaluate all 7 Strategy Ladder templates.

            // Official starting lineups will update the stage to LINEUP_CONFIRMED at T-15m / T-45m.

            rec.setFairP1(ml.getProbHome());
            rec.setFairPx(ml.getProbDraw());
            rec.setFairP2(ml.getProbAway());
            rec.setFairOver25(ml.getProbOver());
            rec.setFairUnder25(ml.getProbUnder());
            double p1 = ml.getProbHome() != null ? ml.getProbHome().doubleValue() : 0.5;
            double px = ml.getProbDraw() != null ? ml.getProbDraw().doubleValue() : 0.0;
            double p2 = ml.getProbAway() != null ? ml.getProbAway().doubleValue() : 0.5;
            double pOver = ml.getProbOver() != null ? ml.getProbOver().doubleValue() : 0.5;
            double pUnder = ml.getProbUnder() != null ? ml.getProbUnder().doubleValue() : 0.5;
            Double expTotal = (dossierMeta.get("expected_total_baseline") instanceof Number n) ? n.doubleValue() : null;
            int matchBestOf = 3;
            if (dossierMeta != null) {
                matchBestOf = pro.datawiki.igaming.dto.CanonicalMatchHeaderDto.parseBestOfFromStrings(
                        (String) dossierMeta.get("tournament_name"),
                        (String) dossierMeta.get("league_name"),
                        (String) dossierMeta.get("stage"),
                        (String) dossierMeta.get("round")
                );
            }
            pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate recCandidate = (sport != null)
                    ? sport.resolveRecommendedMarket(p1, px, p2, pOver, pUnder, expTotal, matchBestOf)
                    : new pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate("WIN1", p1, "DEFAULT");
            String targetMarket = recCandidate.market();

            if (sport != null && !sport.isMarketSupported(targetMarket, matchBestOf)) {
                targetMarket = "WIN1";
            }

            boolean isDrawRadar = "DRAW".equals(targetMarket);
            double fairProb = ml.getRecommendedProb() != null ? ml.getRecommendedProb().doubleValue() * 100.0 : 65.0;
            String tier = ml.getConfidenceTier();

            if (tier == null || tier.isBlank() || "PASS".equalsIgnoreCase(tier)) {

                if (isDrawRadar) {

                    if (fairProb >= 45.0) tier = "ML_90";
                    else if (fairProb >= 39.0) tier = "ML_80";
                    else if (fairProb >= 34.0) tier = "ML_70";
                    else tier = "ML_60";

                } else {

                    if (fairProb >= 88.0) tier = "ML_90";
                    else if (fairProb >= 78.0) tier = "ML_80";
                    else if (fairProb >= 68.0) tier = "ML_70";
                    else tier = "ML_60";

                }

            }

            RedisOddsConsensusService.MarketQuote quote = redisOddsConsensusService.findBestMarketQuote(matchId, targetMarket);
            Double marketOddVal = null;

            if (quote != null && quote.value() > 1.01) {

                marketOddVal = quote.value();

            } else {

                marketOddVal = fallbackOddFromDossier(dossierMeta, targetMarket);

            }

            double fairOdd = 100.0 / Math.max(1.0, fairProb);

            if (marketOddVal == null || marketOddVal <= 1.01) {

                marketOddVal = fairOdd > 1.05 ? Math.round(fairOdd * 100.0) / 100.0 : (isDrawRadar ? 3.40 : 1.85);

            }

            // Institutional Tier Cap based on Odds and Sport Volatility

            String rawTierFB = tier;
            String appliedRuleCodeFB = null;
            String appliedRuleReasonFB = null;

            if (isDrawRadar) {

                if ("ML_90".equals(tier) || "ML_80".equals(tier)) {

                    tier = "ML_70";
                    appliedRuleCodeFB = "DRAW_HIGH_ODDS_PRUDENCE_CAP";
                    appliedRuleReasonFB = String.format("Равновесная ничья (коэффициент %.2f): институциональный риск-менеджмент ограничил тир до ML_70 (ставка 250 ₽) для баланса доходности и дисперсии.", marketOddVal);

                }

            } else if ("ML_90".equals(tier)) {

                if (marketOddVal >= 2.00) {

                    tier = "ML_70";
                    appliedRuleCodeFB = "ODDS_GE_2_00_TIER_CAP";
                    appliedRuleReasonFB = String.format("Коэффициент %.2f >= 2.00: институциональный лимит риска снизил тир с ML_90 до ML_70 (ставка 250 ₽ вместо 1000 ₽) во избежание просадки портфеля при высокой дисперсии.", marketOddVal);

                } else if (marketOddVal >= 1.75) {

                    tier = "ML_80";
                    appliedRuleCodeFB = "ODDS_GE_1_75_TIER_CAP";
                    appliedRuleReasonFB = String.format("Коэффициент %.2f >= 1.75: тир ML_90 ограничен до ML_80 (ставка 500 ₽ вместо 1000 ₽) согласно институциональному порогу супер-уверенности.", marketOddVal);

                }

            } else if ("ML_80".equals(tier) && marketOddVal >= 2.50) {

                tier = "ML_70";
                appliedRuleCodeFB = "ODDS_GE_2_50_TIER_CAP";
                appliedRuleReasonFB = String.format("Коэффициент %.2f >= 2.50: тир ML_80 ограничен до ML_70 (ставка 250 ₽ вместо 500 ₽) во избежание овербетинга на аутсайдерах.", marketOddVal);

            }

            if (sport != null && sport.getVolatilityCapTier() != null && !"PASS".equals(tier)) {

                String cap = sport.getVolatilityCapTier();

                if (!cap.equals(tier)) {

                    appliedRuleCodeFB = sport.getVolatilityCapRuleCode();
                    appliedRuleReasonFB = sport.getVolatilityCapRuleReason(tier);

                }

                tier = cap;

            }

            rec.setConvictionTier(tier);
            rec.setRecommendedMarket(targetMarket);
            rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
            rec.setBestBookmaker(quote != null ? quote.bookmaker() : "Market Consensus");

            if (appliedRuleCodeFB != null) {

                recordPolicyIntervention(rec, rawTierFB, tier, appliedRuleCodeFB, appliedRuleReasonFB);

            }

            double evPct = ((marketOddVal * (fairProb / 100.0)) - 1.0) * 100.0;
            rec.setExpectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0));

            // Constitution Rule 4: No Artificial Vetoes.

            // In Pre-Lineup fast radar, record actual odd and EV without dropping recommendations.

            double stakePct = switch (tier) {

                case "ML_90" -> 5.0;
                case "ML_80" -> 4.0;
                case "ML_70" -> 3.0;
                default      -> 2.0;
            };

            if (marketOddVal < MIN_RECOMMENDED_ODD || evPct < 0.0) {

                stakePct = 1.0;

            }

            rec.setKellyStakePct(BigDecimal.valueOf(stakePct).setScale(2, RoundingMode.HALF_UP));
            rec.setConfidenceScore((int) Math.round(fairProb));
            rec.setResultStatus("PENDING");
            rec.setActualPnl(BigDecimal.ZERO);
            String reasonCode = ml.getDecisionReasonCode() != null ? ml.getDecisionReasonCode() : "PRE_LINEUP_RADAR";

            rec.setCoreHypothesis(String.format("🔮 ПРЕДВАРИТЕЛЬНЫЙ РАДАР ПОТЕНЦИАЛА: %s (%s | P=%.1f%%, Fair Odds: %.2f)",

                    reasonCode, targetMarket, fairProb, fairOdd));
            int window = sport != null ? sport.getLineupConfirmationWindowMinutes() : 45;
            String risk = "Стартовые составы ещё не опубликованы. Окончательная верификация за " + window + " минут до матча.";

            if (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty()) {

                risk = "⚠️ Риск ротации ключевых игроков: " + String.join("; ", ml.getKeyPlayerDependencies());

            }

            rec.setIdentifiedRisk(risk);
            rec.setTacticalRationale(null);
            enrichRecommendationWithLineupAndScenarios(rec, ml);
            MatchAiRecommendation saved = recommendationRepository.save(rec);

            log.info("Saved PRE_LINEUP fast radar recommendation for match #{} ({}-{}): Tier={}, Bet='{}' @ {}",

                    matchId, rec.getTeam1Name(), rec.getTeam2Name(), tier, targetMarket, marketOddVal);
            strategyEngineService.generateStrategyVotes(saved, marketOddVal, targetMarket, dossierMeta);
            return Optional.of(saved);

        }

        // STAGE 2 (LINEUP_CONFIRMED): Quantitative Institutional Evaluation

        if (isDataVoid(dossierMeta, sport)) {

            log.info("Stage 2: Match #{} has sparse factual context (Data Void detected). Proceeding with ML mathematical distribution.", matchId);

        }

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

        // Stage 2: Quantitative Institutional ML Engine (Pure Math & Tactical Calibration, Zero LLM Noise)

        log.info("Stage 2: Evaluating match #{} via Quantitative Institutional ML Engine (Pure Math, Zero LLM Noise)...", matchId);
        AiAnalysisResultDto dto = buildMlAnalysisResultDto(matchId, ml, dossierMeta, sport, isLineupConfirmed, minutesToKickoff);

        if (!memories.isEmpty() && dto != null) {

            StringBuilder memSb = new StringBuilder();

            for (AiLearningMemory mem : memories) {

                memSb.append(String.format(" 🧠 [Память: %s] %s", mem.getTeamName(), mem.getLessonText()));

            }

            if (dto.getTacticalRationale() != null) {

                dto.setTacticalRationale(dto.getTacticalRationale() + memSb.toString());

            }

        }

        // Convert or update existing recommendation

        MatchAiRecommendation rec = getOrCreateRecommendation(matchId);
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

            tier = "ML_60";

        } else {

            tier = tier.trim().toUpperCase();

        }

        if ("PASS".equals(tier) || "NO_BET".equals(tier) || "ABSTAIN".equals(tier)) {

            tier = "ML_60";

        }

        String targetMarket = dto.getRecommendedBet();

        if (targetMarket == null || targetMarket.isBlank() || "PASS".equalsIgnoreCase(targetMarket) || "NO_BET".equalsIgnoreCase(targetMarket)) {

            targetMarket = (ml != null && ml.getRecommendedMarket() != null && !ml.getRecommendedMarket().isBlank())

                    ? ml.getRecommendedMarket()

                    : ((sport != null && !sport.hasDrawOutcome()) ? "WIN1" : "DOUBLE_CHANCE_1X");

        }

        if (targetMarket == null || targetMarket.isBlank()) {

            targetMarket = dto.getMarketType();

        }

        // Game Theory Equilibrium & Authentic Draw Support

        if ("DRAW".equalsIgnoreCase(targetMarket) || "PX".equalsIgnoreCase(targetMarket) || "X".equalsIgnoreCase(targetMarket)) {

            if (sport != null && !sport.hasDrawOutcome()) {

                double p1 = (ml != null && ml.getProbHome() != null) ? ml.getProbHome().doubleValue() : 0.0;
                double p2 = (ml != null && ml.getProbAway() != null) ? ml.getProbAway().doubleValue() : 0.0;
                targetMarket = (p1 >= p2) ? "WIN1" : "WIN2";
                log.info("Sport '{}' has no draw outcome. Re-routed to {} (P1={}, P2={}) for match #{}", sport.getCode(), targetMarket, p1, p2, matchId);

            } else {

                targetMarket = "DRAW";

            }

        }

        if (sport != null && !sport.isMarketSupported(targetMarket)) {

            log.warn("Market '{}' is invalid for sport '{}'. Re-routing to supported fallback.", targetMarket, sport.getCode());
            targetMarket = sport.getFallbackSupportedMarket(targetMarket);

        }

        if (targetMarket == null || targetMarket.isBlank()) {

            targetMarket = "WIN1";

        }

        // =========================================================================

        // STAGE 2: Quantitative Valuation & Market Edge (+EV) Overlay

        // The LLM determined fair probabilities in Stage 1 completely blind to odds.

        // Now, we query real live bookmaker quotes from Redis/DB and compute true EV.

        // =========================================================================

        double fairProb = resolveFairProbability(dto, ml, targetMarket, sport);
        boolean isDrawMarket = "DRAW".equals(targetMarket) || "PX".equals(targetMarket) || "X".equals(targetMarket);

        // 4 Deterministic ML Probability Brackets: 90% ML, 80% ML, 70% ML, 60% ML

        if (isDrawMarket) {

            if (fairProb >= 45.0) {

                tier = "ML_90";

            } else if (fairProb >= 39.0) {

                tier = "ML_80";

            } else if (fairProb >= 34.0) {

                tier = "ML_70";

            } else {

                tier = "ML_60";

            }

        } else {

            if (fairProb >= 90.0) {

                tier = "ML_90";

            } else if (fairProb >= 80.0) {

                tier = "ML_80";

            } else if (fairProb >= 70.0) {

                tier = "ML_70";

            } else {

                tier = "ML_60";

            }

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

        // Constitution Domain Rule 4: No Artificial Vetoes Policy.

        // Trust the mathematical distributions of the ML model without dropping bets or enforcing arbitrary auto-PASS.

        // Post-factum tracking: record true market odd and EV without skipping.

        if (marketOddVal < MIN_RECOMMENDED_ODD) {

            log.info("Stage 2: Match #{} market odd ({}) is below benchmark ({}), tracking for post-factum forensics.", matchId, marketOddVal, MIN_RECOMMENDED_ODD);

        }

        if (evPct < 0.0) {

            log.info("Stage 2: Match #{} negative EV ({:.1f}%), tracking for post-factum forensics.", matchId, evPct);

        }

        // Institutional Tier Cap based on Odds and Sport Volatility

        String rawTierS2 = tier;
        String appliedRuleCodeS2 = null;
        String appliedRuleReasonS2 = null;

        if (isDrawMarket) {

            if ("ML_90".equals(tier) || "ML_80".equals(tier)) {

                tier = "ML_70";
                appliedRuleCodeS2 = "DRAW_HIGH_ODDS_PRUDENCE_CAP";
                appliedRuleReasonS2 = String.format("Равновесная ничья (коэффициент %.2f): институциональный риск-менеджмент ограничил тир до ML_70 (ставка 250 ₽) во избежание просадки портфеля при высокой дисперсии.", marketOddVal);
                log.info("Institutional Tier Cap: Capped draw recommendation #{} to ML_70 due to odds={}", matchId, marketOddVal);

            }

        } else if ("ML_90".equals(tier)) {

            if (marketOddVal >= 2.00) {

                tier = "ML_70";
                appliedRuleCodeS2 = "ODDS_GE_2_00_TIER_CAP";
                appliedRuleReasonS2 = String.format("Коэффициент %.2f >= 2.00: институциональный лимит риска снизил тир с ML_90 до ML_70 (ставка 250 ₽ вместо 1000 ₽) во избежание просадки портфеля при высокой дисперсии.", marketOddVal);
                log.info("Institutional Tier Cap: Downgraded match #{} from ML_90 to ML_70 due to high odd ({})", matchId, marketOddVal);

            } else if (marketOddVal >= 1.75) {

                tier = "ML_80";
                appliedRuleCodeS2 = "ODDS_GE_1_75_TIER_CAP";
                appliedRuleReasonS2 = String.format("Коэффициент %.2f >= 1.75: тир ML_90 ограничен до ML_80 (ставка 500 ₽ вместо 1000 ₽) согласно институциональному порогу супер-уверенности.", marketOddVal);
                log.info("Institutional Tier Cap: Downgraded match #{} from ML_90 to ML_80 due to odd ({}) >= 1.75", matchId, marketOddVal);

            }

        } else if ("ML_80".equals(tier) && marketOddVal >= 2.50) {

            tier = "ML_70";
            appliedRuleCodeS2 = "ODDS_GE_2_50_TIER_CAP";
            appliedRuleReasonS2 = String.format("Коэффициент %.2f >= 2.50: тир ML_80 ограничен до ML_70 (ставка 250 ₽ вместо 500 ₽) во избежание овербетинга на аутсайдерах.", marketOddVal);
            log.info("Institutional Tier Cap: Downgraded match #{} from ML_80 to ML_70 due to high odd ({})", matchId, marketOddVal);

        }

        // Volatility tier cap: delegated to polymorphic Sport domain contract

        if (sport != null && sport.getVolatilityCapTier() != null && !"PASS".equals(tier)) {

            String cap = sport.getVolatilityCapTier();

            if (!cap.equals(tier)) {

                log.info("{} Volatility Cap: Capping match #{} tier from {} to {}", sport.getCode(), matchId, tier, cap);
                appliedRuleCodeS2 = sport.getVolatilityCapRuleCode();
                appliedRuleReasonS2 = sport.getVolatilityCapRuleReason(tier);
                tier = cap;

            }

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
        rec.setConvictionTier(tier);
        rec.setRecommendedMarket(targetMarket);
        rec.setRecommendedOdd(BigDecimal.valueOf(marketOddVal).setScale(3, RoundingMode.HALF_UP));
        rec.setBestBookmaker(bestBookmaker);
        rec.setExpectedValuePct(BigDecimal.valueOf(evPct).setScale(2, RoundingMode.HALF_UP));
        rec.setKellyStakePct(BigDecimal.valueOf(kellyPct).setScale(2, RoundingMode.HALF_UP));

        // Ensure available bookmakers and direct URLs are saved

        Set<String> bks = redisOddsConsensusService.getAvailableBookmakers(matchId);

        if (bks != null && !bks.isEmpty()) {

            rec.setAvailableBookmakers(String.join(",", bks));

        }

        Map<String, String> urls = redisOddsConsensusService.getBookmakerUrls(matchId, rec.getSportSlug());

        if (urls != null && !urls.isEmpty()) {

            try {

                rec.setBookmakerUrlsJson(objectMapper.writeValueAsString(urls));

            } catch (Exception e) {

                log.error("Failed to serialize bookmaker URLs for matchId={}: {}", matchId, e.getMessage());

            }

        }

        int confidence = dto.getConfidenceScore() != null ? dto.getConfidenceScore() : (int) Math.round(fairProb);

        if ("ML_90".equals(tier) || "ML_80".equals(tier) || "ML_70".equals(tier) || "ML_60".equals(tier)) {

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

        if (isSoloRacket && (targetMarket.equals("AH_HOME_PLUS_1.5") || targetMarket.equals("AH_AWAY_PLUS_1.5"))) {

            String cushionNote = " 🛡️ Дисциплина одиночных ракеточных видов: ставка перекрыта подушечной форой по сетам (+1.5) — взятие хотя бы одного сета гарантирует проход.";
            String currentRat = dto.getTacticalRationale();

            if (currentRat != null && !currentRat.contains("по сетам")) {

                rec.setTacticalRationale(currentRat + cushionNote);

            } else {

                rec.setTacticalRationale(currentRat);

            }

        } else {

            rec.setTacticalRationale(dto.getTacticalRationale());

        }

        rec.setSteamDirection(dto.getSteamDirection() != null ? dto.getSteamDirection() : "NEUTRAL");
        rec.setResultStatus("PENDING");
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

        if (isLineupConfirmed) {

            rec.setLineupStatus("CONFIRMED");

        } else if (Boolean.TRUE.equals(ml.getIsLineupProjected())) {

            rec.setLineupStatus("PROJECTED");

        } else {

            rec.setLineupStatus("ESTIMATED");

        }

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

        enrichRecommendationWithLineupAndScenarios(rec, ml);
        MatchAiRecommendation saved = recommendationRepository.save(rec);

        if (appliedRuleCodeS2 != null) {

            recordPolicyIntervention(saved, rawTierS2, saved.getConvictionTier(), appliedRuleCodeS2, appliedRuleReasonS2);

        }

        log.info("Saved AI recommendation for match #{} ({}-{}): Bet='{}' @ {}, Risk='{}'",

                matchId, rec.getTeam1Name(), rec.getTeam2Name(), rec.getRecommendedMarket(), rec.getRecommendedOdd(), rec.getIdentifiedRisk());
        strategyEngineService.generateStrategyVotes(saved, marketOddVal, targetMarket, dossierMeta);
        return Optional.of(saved);

    }

    private void enrichRecommendationWithLineupAndScenarios(MatchAiRecommendation rec, MlPredictionDto ml) {

        if (rec == null || ml == null) return;

        if (ml.getLineupResiliencePct() != null) {

            rec.setLineupResiliencePct(ml.getLineupResiliencePct());

        }

        if (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty()) {

            rec.setKeyPlayerDependencies(String.join("; ", ml.getKeyPlayerDependencies()));

        }

        try {

            Map<String, Object> data = new HashMap<>();
            if (ml.getProjectedStartersHome() != null) data.put("home_starters", ml.getProjectedStartersHome());
            if (ml.getProjectedStartersAway() != null) data.put("away_starters", ml.getProjectedStartersAway());
            if (ml.getProjectedBenchHome() != null) data.put("home_bench", ml.getProjectedBenchHome());
            if (ml.getProjectedBenchAway() != null) data.put("away_bench", ml.getProjectedBenchAway());
            if (ml.getScenariosDetails() != null) data.put("scenarios", ml.getScenariosDetails());
            if (ml.getWorstCaseFloor() != null) data.put("worst_case_floor", ml.getWorstCaseFloor());

            if (!data.isEmpty()) {

                rec.setProjectedLineupJson(objectMapper.writeValueAsString(data));

            }

        } catch (Exception e) {

            log.warn("Failed to serialize projected lineup data: {}", e.getMessage());

        }

    }

    /**

     * On-demand dynamic generation and persistence of projected lineups & 9-scenario simulation

     * for recommendations where lineups have not been generated yet.

     */

    public Optional<MatchAiRecommendation> enrichAndPersistProjectedScenarios(MatchAiRecommendation rec) {

        if (rec == null) return Optional.empty();

        if (rec.getProjectedLineupJson() != null && !rec.getProjectedLineupJson().isBlank()) {

            return Optional.of(rec);

        }

        try {

            Map<String, Object> meta = new HashMap<>();
            meta.put("sport_name", rec.getSportName() != null ? rec.getSportName() : "FOOTBALL");
            meta.put("team1_name", rec.getTeam1Name());
            meta.put("team2_name", rec.getTeam2Name());
            MlPredictionDto ml = mlInferenceClient.predict(rec.getMatchId() != null ? rec.getMatchId() : 0L, meta);

            if (ml != null && Boolean.TRUE.equals(ml.getIsLineupProjected())) {

                enrichRecommendationWithLineupAndScenarios(rec, ml);
                rec.setLineupStatus("PROJECTED");
                MatchAiRecommendation saved = recommendationRepository.save(rec);
                log.info("Dynamically generated & persisted projected scenarios for rec #{} (Match #{})", rec.getId(), rec.getMatchId());
                return Optional.of(saved);

            }

        } catch (Exception e) {

            log.warn("On-the-fly projected scenario generation failed for rec #{}: {}", rec.getId(), e.getMessage());

        }

        return Optional.of(rec);

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

     * Kickoff In-Play Lineup Verification (Minute 0-2).

     * Runs every 30 seconds to catch matches right as the referee blows the whistle,

     * verifying the actual 11 on the pitch and executing the bet at 0:0 score.

     */

    @Scheduled(fixedDelay = 30000)

    public void scanKickoffInPlayMatches() {

        long nowMs = System.currentTimeMillis();
        List<MatchAiRecommendation> kickoffWaiters = recommendationRepository.findByResultStatusAndEvaluationStage("WAITING", "WAIT_KICKOFF");

        if (kickoffWaiters != null && !kickoffWaiters.isEmpty()) {

            for (MatchAiRecommendation rec : kickoffWaiters) {

                Long matchId = rec.getMatchId();
                if (matchId == null) continue;
                LocalDateTime startLdt = rec.getMatchStartTime();
                if (startLdt == null) continue;
                long startMs = startLdt.toInstant(ZoneOffset.UTC).toEpochMilli();
                long diffMinutes = (startMs - nowMs) / (60L * 1000L);

                // Phase 1: Pre-Kickoff High-Frequency Watchdog (T-45 to T-1)

                // Official protocol can appear at ANY moment (e.g. at T-30, T-15, T-5, T-1).

                // We actively poll for the official match sheet (11x11 + bench + injuries).

                if (diffMinutes > 1) {

                    boolean confirmed = dossierService.checkOrRefreshLineupsOnDemand(matchId);

                    if (confirmed) {

                        log.info("Official pre-match protocol arrived at T-{}m for match #{}! Immediate re-evaluation with full match sheet...", diffMinutes, matchId);
                        evaluateMatch(matchId);

                    }

                    continue;

                }

                // If match is overdue by > 6 minutes and still in WAITING:

                if (diffMinutes < -6) {

                    log.info("Match #{} is 6+ minutes past start time. Triggering evaluation with available parameters.", matchId);
                    evaluateMatch(matchId);
                    continue;

                }

                // Kickoff window [now - 6m ... now + 1m]

                try {

                    // Trigger evaluation now that kickoff has arrived!

                    log.info("Kickoff window reached for match #{} (diffMinutes={}). Triggering on-pitch lineup verification.", matchId, diffMinutes);
                    evaluateMatch(matchId);

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

     * Computes performance statistics across all recommendations, broken down by conviction tier.

     */

    /**

     * Periodically synchronizes the schedule and canonical team names of active recommendations

     * with canonical MDM and SofaScore ground truth to prevent premature IN_PLAY transitions

     * or stale placeholder match start times (e.g. Asian tennis order of play).

     */

    @Scheduled(cron = "${analyst.scheduling.sync-schedule-cron:0 */2 * * * *}")

    public void syncActiveRecommendationsSchedule() {

        try {

            List<MatchAiRecommendation> pendingRecs = recommendationRepository.findByResultStatusInOrderByIdDesc(List.of("PENDING", "WAITING"));
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

                    // If match was prematurely transitioned to IN_PLAY but new start time is in the future

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

                // 2. Synchronize canonical team names and slug if MDM team name changed

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

                // 3. Dynamic Bookmaker Consensus Sync: Refresh available_bookmakers & URLs from Redis

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

                    // Recompute match slug & url if team names changed

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

    public List<Map<String, Object>> getAvailableBookmakersStats() {
        return statisticsService.getAvailableBookmakersStats();
    }

    private AiAnalysisResultDto buildMlAnalysisResultDto(Long matchId, MlPredictionDto ml, Map<String, Object> dossierMeta, Sport sport, boolean isLineupConfirmed, long minutesToKickoff) {

        boolean isTeam = sport == null || sport.isTeamSport();
        String leagueName = (String) dossierMeta.getOrDefault("tournament_name", dossierMeta.get("league_name"));
        double p1 = ml.getProbHome() != null ? ml.getProbHome().doubleValue() : 0.5;
        double px = ml.getProbDraw() != null ? ml.getProbDraw().doubleValue() : 0.0;
        double p2 = ml.getProbAway() != null ? ml.getProbAway().doubleValue() : 0.5;
        double pOver = ml.getProbOver() != null ? ml.getProbOver().doubleValue() : 0.5;
        double pUnder = ml.getProbUnder() != null ? ml.getProbUnder().doubleValue() : 0.5;
        Double expTotal = (dossierMeta.get("expected_total_baseline") instanceof Number n) ? n.doubleValue() : null;
        int matchBestOf = 3;
        if (dossierMeta != null) {
            matchBestOf = pro.datawiki.igaming.dto.CanonicalMatchHeaderDto.parseBestOfFromStrings(
                    (String) dossierMeta.get("tournament_name"),
                    (String) dossierMeta.get("league_name"),
                    (String) dossierMeta.get("stage"),
                    (String) dossierMeta.get("round")
            );
        }
        pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate recCandidate = (sport != null)
                ? sport.resolveRecommendedMarket(p1, px, p2, pOver, pUnder, expTotal, matchBestOf)
                : new pro.datawiki.igaming.dto.sport.RecommendedMarketCandidate("WIN1", p1, "DEFAULT");
        String targetMarket = recCandidate.market();
        if (sport != null && !sport.isMarketSupported(targetMarket, matchBestOf)) {
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

        } else if (sport != null && (sport instanceof pro.datawiki.igaming.dto.sport.football.FootballSport || sport.hasDrawOutcome())) {

            // Nerve Shield Discipline: if match is not an absolute landslide (confScore < 75), prefer safe ladder step DOUBLE_CHANCE_1X / DOUBLE_CHANCE_X2

            boolean isRawW1 = "P1".equals(targetMarket) || "HOME_WIN".equals(targetMarket) || "WIN1".equals(targetMarket) || "1".equals(targetMarket);
            boolean isRawW2 = "P2".equals(targetMarket) || "AWAY_WIN".equals(targetMarket) || "WIN2".equals(targetMarket) || "2".equals(targetMarket);

            if (isRawW1) {

                targetMarket = (confScore >= 75) ? "WIN1" : "DOUBLE_CHANCE_1X";

            } else if (isRawW2) {

                targetMarket = (confScore >= 75) ? "WIN2" : "DOUBLE_CHANCE_X2";

            }

        }

        String reasonCode = ml.getDecisionReasonCode() != null ? ml.getDecisionReasonCode() : "QUANTITATIVE_SIGNAL";
        String riskText;
        String hypothesisText;

        if (minutesToKickoff < -2) {

            riskText = "Отчет зафиксирован к началу матча на основе официальных протоколов команд (не поминутный лайв).";

            hypothesisText = String.format("🟢 СТАРТОВЫЙ РАСЧЕТ (По свистку и протоколам): %s (%s | P=%d%%)",

                    reasonCode, targetMarket, confScore);

        } else if (minutesToKickoff <= 1) {

            String venueSurface = sport != null ? sport.getVenueSurface(Language.RU) : "поле";
            String lineupLabel = sport != null ? sport.getStartingLineupLabel(Language.RU) : "состав";

            if (isLineupConfirmed) {

                riskText = "⚡ Стартовый расчет: Состав на " + venueSurface + " верифицирован на 1-й минуте матча. Ротация учтена.";

                hypothesisText = String.format("⚡ ВЕРИФИКАЦИЯ (1-я минута, реальный %s на %s): %s (%s | P=%d%%)",

                        lineupLabel, venueSurface, reasonCode, targetMarket, confScore);

            } else {

                riskText = "⚡ Стартовый расчет: Игра началась. Ротация и предматчевые параметры зафиксированы.";

                hypothesisText = String.format("⚡ ВЕРИФИКАЦИЯ (Старт игры): %s (%s | P=%d%%)",

                        reasonCode, targetMarket, confScore);

            }

        } else if (isLineupConfirmed) {

            riskText = "Сценарный анализ вероятных составов. " + (ml.getKeyPlayerDependencies() != null && !ml.getKeyPlayerDependencies().isEmpty() ? String.join("; ", ml.getKeyPlayerDependencies()) : "Ротационная устойчивость подтверждена.");
            String lineupBadge = sport != null ? sport.getLineupConfirmedBadge(Language.RU) : "✅ Составы";

            hypothesisText = String.format("📋 АНАЛИЗ ЗАЯВЛЕННЫХ СОСТАВОВ (%s): %s (%s | P=%d%%)",

                    lineupBadge.replace("✅ ", ""), reasonCode, targetMarket, confScore);

        } else if (!isTeam) {

            riskText = "Индивидуальный матч: стартовые участники фиксированы, форма спортсменов и покрытие верифицированы перед матчем.";

            hypothesisText = String.format("📊 ПРЕДМАТЧЕВЫЙ АНАЛИЗ ФОРМЫ СПОРТСМЕНОВ: %s (%s | P=%d%%)",

                    reasonCode, targetMarket, confScore);

        } else {

            int window = sport != null ? sport.getLineupConfirmationWindowMinutes() : 45;
            riskText = String.format("Предматчевая верификация завершена: официальный протокол для лиги не публикуется. Расчет зафиксирован по предматчевой модели и консенсусу линий за %d минут до старта.", window);

            hypothesisText = String.format("⚡ ФИНАЛЬНЫЙ РАСЧЕТ ПЕРЕД СТАРТОМ: %s (%s | P=%d%%)",

                    reasonCode, targetMarket, confScore);

        }

        // Build institutional shadow picks hierarchy (Draw defense & league baseline cushion total)
        List<pro.datawiki.igaming.analyst.dto.ShadowPickDto> shadowPicks = new ArrayList<>();

        if (sport != null && sport.hasDrawOutcome() && ml.getProbDraw() != null && ml.getProbDraw().doubleValue() >= 0.22) {
            String drawMarket = (targetMarket != null && (targetMarket.contains("2") || targetMarket.contains("AWAY")))
                    ? "DOUBLE_CHANCE_X2" : "DOUBLE_CHANCE_1X";
            shadowPicks.add(pro.datawiki.igaming.analyst.dto.ShadowPickDto.builder()
                    .type("DRAW_DEFENSE")
                    .market(drawMarket)
                    .odd(BigDecimal.valueOf(1.35))
                    .hypothesis("Защитная ветка: ничейная страховка (" + drawMarket + ") при повышенной вероятности ничейного исхода (P=" + Math.round(ml.getProbDraw().doubleValue() * 100) + "%)")
                    .resultStatus("PENDING")
                    .build());
        }

        Double cLow = ml.getEffectiveCushionLow();
        Double cHigh = ml.getEffectiveCushionHigh();
        Double pLow = ml.getEffectiveProbCushionLow();
        Double pHigh = ml.getEffectiveProbCushionHigh();
        Double baseTot = ml.getEffectiveBaseline();

        if (targetMarket != null && (targetMarket.contains("TOTAL_OVER") || targetMarket.contains("TO_") || (ml.getProbOver() != null && ml.getProbOver().doubleValue() >= 0.58))) {
            if (cLow != null) {
                shadowPicks.add(pro.datawiki.igaming.analyst.dto.ShadowPickDto.builder()
                        .type("CUSHION_TOTAL")
                        .market("TOTAL_OVER_" + cLow)
                        .odd(BigDecimal.valueOf(1.35))
                        .hypothesis("Защитная подушка тотала: ТБ " + cLow + " (70-75% от базы лиги " + (baseTot != null ? baseTot : "") + ")")
                        .resultStatus("PENDING")
                        .build());
            }
        } else if (targetMarket != null && (targetMarket.contains("TOTAL_UNDER") || targetMarket.contains("TU_") || (ml.getProbUnder() != null && ml.getProbUnder().doubleValue() >= 0.58))) {
            if (cHigh != null) {
                shadowPicks.add(pro.datawiki.igaming.analyst.dto.ShadowPickDto.builder()
                        .type("CUSHION_TOTAL")
                        .market("TOTAL_UNDER_" + cHigh)
                        .odd(BigDecimal.valueOf(1.38))
                        .hypothesis("Защитная подушка тотала: ТМ " + cHigh + " (125-130% от базы лиги " + (baseTot != null ? baseTot : "") + ")")
                        .resultStatus("PENDING")
                        .build());
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

                .tacticalRationale(null)

                .fairP1(ml.getProbHome())

                .fairPx(ml.getProbDraw())

                .fairP2(ml.getProbAway())

                .fairOver25(ml.getProbOver())

                .fairUnder25(ml.getProbUnder())

                .cushionLowLine(cLow != null ? BigDecimal.valueOf(cLow) : null)

                .cushionHighLine(cHigh != null ? BigDecimal.valueOf(cHigh) : null)

                .probCushionLow(pLow != null ? BigDecimal.valueOf(pLow) : null)

                .probCushionHigh(pHigh != null ? BigDecimal.valueOf(pHigh) : null)

                .tournamentBaselineTotal(baseTot != null ? BigDecimal.valueOf(baseTot) : null)

                .shadowPicks(shadowPicks)

                .steamDirection("NEUTRAL")

                .build();

    }

    private void reportScheduleCollisionAnomaly(

            Long matchId1, Long matchId2, String conflictTeam, String sportSlug,

            String t1, String t2, String league1, MatchAiRecommendation rec2

    ) {

        try {

            Map<String, Object> details = new HashMap<>();
            details.put("match1_id", matchId1);
            details.put("match1_teams", t1 + " vs " + t2);
            details.put("match1_league", league1);
            details.put("match2_id", matchId2);
            details.put("match2_teams", rec2.getTeam1Name() + " vs " + rec2.getTeam2Name());
            details.put("match2_league", rec2.getLeagueName());
            details.put("conflict_team", conflictTeam);
            details.put("sport", sportSlug);
            Map<String, Object> anomalyPayload = new HashMap<>();
            anomalyPayload.put("externalSystemCode", "AI_ANALYST");
            anomalyPayload.put("entityType", "SCHEDULE_COLLISION");
            anomalyPayload.put("rawKey", matchId1 + ":" + matchId2);
            anomalyPayload.put("rawName", conflictTeam);
            anomalyPayload.put("rawParentKey", sportSlug);
            anomalyPayload.put("rawCategory", league1 + " vs " + rec2.getLeagueName());
            anomalyPayload.put("details", objectMapper.writeValueAsString(details));
            String url = mdmUrl + "/api/v1/mdm/report-unmapped";

            HttpRequest req = HttpRequest.newBuilder()

                    .uri(URI.create(url))

                    .header("Content-Type", "application/json")

                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(anomalyPayload)))

                    .timeout(Duration.ofSeconds(3))

                    .build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            log.info("📢 Published SCHEDULE_COLLISION anomaly to MDM error queue: #{} vs #{} for team '{}'", matchId1, matchId2, conflictTeam);

        } catch (Exception e) {

            log.warn("Failed to publish SCHEDULE_COLLISION anomaly to MDM: {}", e.getMessage());

        }

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
                rec.setMatchUrl(SlugUtils.buildMatchUrl(rec.getMatchId(), t1, t2, Language.RU));
                Set<String> bks = redisOddsConsensusService.getAvailableBookmakers(rec.getMatchId());

                if (bks != null && !bks.isEmpty()) {

                    rec.setAvailableBookmakers(String.join(",", bks));

                }

                Map<String, String> urls = redisOddsConsensusService.getBookmakerUrls(rec.getMatchId(), rec.getSportSlug());

                if (urls != null && !urls.isEmpty()) {

                    try {

                        rec.setBookmakerUrlsJson(objectMapper.writeValueAsString(urls));

                    } catch (Exception ignored) {}

                }

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

    private double toPct(BigDecimal val, double fallback) {

        if (val == null) return fallback;
        double d = val.doubleValue();
        return (d > 0.0 && d <= 1.0) ? d * 100.0 : d;

    }

    private double resolveFairProbability(AiAnalysisResultDto dto, MlPredictionDto ml, String market, Sport sport) {

        if (market == null) return 60.0;
        String m = market.toUpperCase();

        if ("P1".equals(m) || "HOME_WIN".equals(m) || "WIN1".equals(m) || "1".equals(m)) {

            double p1 = dto != null && dto.getFairP1() != null ? toPct(dto.getFairP1(), 50.0) : (ml != null ? toPct(ml.getProbHome(), 50.0) : 50.0);
            return p1;

        }

        if ("P2".equals(m) || "AWAY_WIN".equals(m) || "WIN2".equals(m) || "2".equals(m)) {

            double p2 = dto != null && dto.getFairP2() != null ? toPct(dto.getFairP2(), 50.0) : (ml != null ? toPct(ml.getProbAway(), 50.0) : 50.0);
            return p2;

        }

        if ("PX".equals(m) || "DRAW".equals(m) || "X".equals(m)) {

            if (dto != null && dto.getFairPx() != null) return toPct(dto.getFairPx(), 25.0);
            if (ml != null && ml.getProbDraw() != null) return toPct(ml.getProbDraw(), 25.0);
            return 25.0;

        }

        if ("DOUBLE_CHANCE_1X".equals(m) || "1X".equals(m)) {

            double p1 = dto != null ? toPct(dto.getFairP1(), ml != null ? toPct(ml.getProbHome(), 45.0) : 45.0) : 45.0;
            double px = dto != null ? toPct(dto.getFairPx(), ml != null ? toPct(ml.getProbDraw(), 28.0) : 28.0) : 28.0;
            return Math.min(95.0, p1 + px);

        }

        if ("DOUBLE_CHANCE_X2".equals(m) || "X2".equals(m)) {

            double p2 = dto != null ? toPct(dto.getFairP2(), ml != null ? toPct(ml.getProbAway(), 40.0) : 40.0) : 40.0;
            double px = dto != null ? toPct(dto.getFairPx(), ml != null ? toPct(ml.getProbDraw(), 28.0) : 28.0) : 28.0;
            return Math.min(95.0, p2 + px);

        }

        if ("AH_HOME_0".equals(m) || "DNB1".equals(m) || "DRAW_NO_BET_1".equals(m) || "AH1_0".equals(m)) {

            double p1 = dto != null ? toPct(dto.getFairP1(), ml != null ? toPct(ml.getProbHome(), 50.0) : 50.0) : 50.0;
            double p2 = dto != null ? toPct(dto.getFairP2(), ml != null ? toPct(ml.getProbAway(), 25.0) : 25.0) : 25.0;
            double sum = p1 + p2;
            return sum > 0 ? (p1 / sum) * 100.0 : 65.0;

        }

        if ("AH_AWAY_0".equals(m) || "DNB2".equals(m) || "DRAW_NO_BET_2".equals(m) || "AH2_0".equals(m)) {

            double p1 = dto != null ? toPct(dto.getFairP1(), ml != null ? toPct(ml.getProbHome(), 25.0) : 25.0) : 25.0;
            double p2 = dto != null ? toPct(dto.getFairP2(), ml != null ? toPct(ml.getProbAway(), 50.0) : 50.0) : 50.0;
            double sum = p1 + p2;
            return sum > 0 ? (p2 / sum) * 100.0 : 65.0;

        }

        if (m.startsWith("TOTAL_UNDER") || m.startsWith("TU_")) {
            Double threshold = Sport.parseSettlementLineNumber(m);
            Double cHigh = ml != null ? ml.getEffectiveCushionHigh() : (dto != null && dto.getCushionHighLine() != null ? dto.getCushionHighLine().doubleValue() : null);
            if (threshold != null && cHigh != null && Math.abs(threshold - cHigh) < 0.1) {
                Double pCush = ml != null ? ml.getEffectiveProbCushionHigh() : (dto != null && dto.getProbCushionHigh() != null ? dto.getProbCushionHigh().doubleValue() : null);
                if (pCush != null) return toPct(BigDecimal.valueOf(pCush), 78.0);
            }

            if (dto != null && dto.getFairUnder25() != null && dto.getFairUnder25().doubleValue() > 0) {

                return toPct(dto.getFairUnder25(), 55.0);

            }

            if (ml != null && ml.getProbUnder() != null && ml.getProbUnder().doubleValue() > 0) {

                return toPct(ml.getProbUnder(), 55.0);

            }

            return 55.0;

        }

        if (m.startsWith("TOTAL_OVER") || m.startsWith("TO_")) {
            Double threshold = Sport.parseSettlementLineNumber(m);
            Double cLow = ml != null ? ml.getEffectiveCushionLow() : (dto != null && dto.getCushionLowLine() != null ? dto.getCushionLowLine().doubleValue() : null);
            if (threshold != null && cLow != null && Math.abs(threshold - cLow) < 0.1) {
                Double pCush = ml != null ? ml.getEffectiveProbCushionLow() : (dto != null && dto.getProbCushionLow() != null ? dto.getProbCushionLow().doubleValue() : null);
                if (pCush != null) return toPct(BigDecimal.valueOf(pCush), 78.0);
            }

            if (dto != null && dto.getFairOver25() != null && dto.getFairOver25().doubleValue() > 0) {

                return toPct(dto.getFairOver25(), 50.0);

            }

            if (ml != null && ml.getProbOver() != null && ml.getProbOver().doubleValue() > 0) {

                return toPct(ml.getProbOver(), 50.0);

            }

            return 50.0;

        }

        if ((m.startsWith("AH_") || m.startsWith("SET_HANDICAP_")) && m.contains("PLUS")) {

            return 75.0;

        }

        if (dto != null && dto.getConfidenceScore() != null && dto.getConfidenceScore() > 0) {

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

    public void recordPolicyIntervention(

            MatchAiRecommendation rec,

            String rawTier,

            String appliedTier,

            String ruleCode,

            String reason

    ) {

        if (policyInterventionRepository == null || rec == null) return;

        try {

            BigDecimal rawStake = getStakeForTier(rawTier);
            BigDecimal appliedStake = getStakeForTier(appliedTier);

            pro.datawiki.igaming.analyst.entity.AiPolicyIntervention intervention = pro.datawiki.igaming.analyst.entity.AiPolicyIntervention.builder()

                    .matchId(rec.getMatchId())

                    .recommendationId(rec.getId())

                    .sportName(rec.getSportName())

                    .team1Name(rec.getTeam1Name())

                    .team2Name(rec.getTeam2Name())

                    .leagueName(rec.getLeagueName())

                    .market(rec.getRecommendedMarket())

                    .recommendedOdd(rec.getRecommendedOdd())

                    .rawConvictionTier(rawTier)

                    .appliedConvictionTier(appliedTier)

                    .rawStakeRub(rawStake)

                    .appliedStakeRub(appliedStake)

                    .ruleCode(ruleCode)

                    .interventionReason(reason)

                    .resultStatus(rec.getResultStatus() != null ? rec.getResultStatus() : "PENDING")

                    .build();
            policyInterventionRepository.save(intervention);

            log.info("🛡️ [Policy Intervention Audit] Recorded {}: match #{} ({}), rawTier={}, appliedTier={}, savedStakeDiff={}",

                    ruleCode, rec.getMatchId(), rec.getSportName(), rawTier, appliedTier, rawStake.subtract(appliedStake));

        } catch (Exception ex) {

            log.warn("Failed recording policy intervention for match #{}: {}", rec.getMatchId(), ex.getMessage());

        }

    }

    private BigDecimal getStakeForTier(String tier) {

        if ("ML_90".equalsIgnoreCase(tier)) return BigDecimal.valueOf(1000.0);
        if ("ML_80".equalsIgnoreCase(tier)) return BigDecimal.valueOf(500.0);
        if ("ML_70".equalsIgnoreCase(tier)) return BigDecimal.valueOf(250.0);
        if ("ML_60".equalsIgnoreCase(tier)) return BigDecimal.valueOf(100.0);
        return BigDecimal.ZERO;

    }

}

