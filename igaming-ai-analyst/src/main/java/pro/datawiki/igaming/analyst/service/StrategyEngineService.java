package pro.datawiki.igaming.analyst.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.analyst.dto.StrategyPerformanceDto;
import pro.datawiki.igaming.analyst.entity.AiStrategyTemplate;
import pro.datawiki.igaming.analyst.entity.AiStrategyVote;
import pro.datawiki.igaming.analyst.entity.AiUserStrategySetting;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiStrategyTemplateRepository;
import pro.datawiki.igaming.analyst.repository.AiStrategyVoteRepository;
import pro.datawiki.igaming.analyst.repository.AiUserStrategySettingRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.ml.MarketEdgeCandidateDto;
import pro.datawiki.igaming.dto.sport.SettlementOutcome;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.StrategyLadderContext;
import pro.datawiki.igaming.dto.sport.StrategyMarketOption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyEngineService {

    public static final String DEFAULT_STRATEGY_SLUG = "nerves_shield";

    private final AiStrategyTemplateRepository strategyTemplateRepository;
    private final AiUserStrategySettingRepository userStrategySettingRepository;
    private final AiStrategyVoteRepository strategyVoteRepository;
    private final MatchAiRecommendationRepository recommendationRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final jakarta.persistence.EntityManager entityManager;

    public List<AiStrategyTemplate> getAllStrategies() {
        return strategyTemplateRepository.findAllByOrderBySortOrderAsc();
    }

    public AiStrategyTemplate resolveActiveStrategy(Long userId, String requestedSlug) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            Optional<AiStrategyTemplate> opt = strategyTemplateRepository.findBySlug(requestedSlug.trim().toLowerCase());
            if (opt.isPresent()) return opt.get();
        }

        if (userId != null) {
            Optional<AiUserStrategySetting> userSetting = userStrategySettingRepository.findById(userId);
            if (userSetting.isPresent() && userSetting.get().getActiveStrategySlug() != null) {
                Optional<AiStrategyTemplate> opt = strategyTemplateRepository.findBySlug(userSetting.get().getActiveStrategySlug());
                if (opt.isPresent()) return opt.get();
            }
        }

        return strategyTemplateRepository.findByIsSystemDefaultTrue()
                .orElseGet(() -> strategyTemplateRepository.findBySlug(DEFAULT_STRATEGY_SLUG)
                        .orElse(null));
    }

    @Transactional
    public void setUserActiveStrategy(Long userId, String strategySlug) {
        if (userId == null || strategySlug == null) return;
        AiUserStrategySetting setting = userStrategySettingRepository.findById(userId)
                .orElse(AiUserStrategySetting.builder().userId(userId).build());
        setting.setActiveStrategySlug(strategySlug);
        userStrategySettingRepository.save(setting);
        log.info("Set active strategy '{}' for user #{}", strategySlug, userId);
    }

    public List<StrategyPerformanceDto> getMultiStrategyPerformance() {
        return getMultiStrategyPerformance(null);
    }

    public List<StrategyPerformanceDto> getMultiStrategyPerformance(String sportSlug) {
        return getMultiStrategyPerformance(sportSlug, null);
    }

    public List<StrategyPerformanceDto> getMultiStrategyPerformance(String sportSlug, String version) {
        List<Map<String, Object>> rows;
        boolean hasSport = sportSlug != null && !sportSlug.isBlank() && !"ALL".equalsIgnoreCase(sportSlug);
        boolean hasVersion = version != null && !version.isBlank() && !"ALL".equalsIgnoreCase(version);

        if (hasSport && hasVersion) {
            rows = strategyVoteRepository.getMultiStrategyPerformanceSummaryBySportAndVersion(sportSlug.trim().toLowerCase(), version.trim());
        } else if (hasSport) {
            rows = strategyVoteRepository.getMultiStrategyPerformanceSummaryBySport(sportSlug.trim().toLowerCase());
        } else if (hasVersion) {
            rows = strategyVoteRepository.getMultiStrategyPerformanceSummaryByVersion(version.trim());
        } else {
            rows = strategyVoteRepository.getMultiStrategyPerformanceSummary();
        }
        return mapRowsToStrategyDtos(rows);
    }

    public Map<String, List<StrategyPerformanceDto>> getMultiStrategyPerformanceBySport() {
        List<Map<String, Object>> rows = strategyVoteRepository.getAllSportsPerformanceBreakdown();
        List<AiStrategyTemplate> templates = getAllStrategies();
        Map<String, AiStrategyTemplate> templateMap = new HashMap<>();
        for (AiStrategyTemplate t : templates) {
            templateMap.put(t.getSlug(), t);
        }

        Map<String, List<StrategyPerformanceDto>> result = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String sport = (String) r.get("sportslug");
            if (sport == null) continue;

            String slug = (String) r.get("strategyslug");
            AiStrategyTemplate t = templateMap.get(slug);

            Number picksNum = (Number) r.get("totalpicks");
            Number wonNum = (Number) r.get("woncount");
            Number lostNum = (Number) r.get("lostcount");
            Number voidNum = (Number) r.get("voidcount");
            Number wrNum = (Number) r.get("winratepct");
            Number pnlNum = (Number) r.get("totalpnlrub");
            Number turnoverNum = (Number) r.get("turnoverrub");
            Number roiNum = (Number) r.get("roipct");
            Number oddNum = (Number) r.get("avgodd");

            StrategyPerformanceDto dto = StrategyPerformanceDto.builder()
                    .strategySlug(slug)
                    .icon((String) r.get("icon"))
                    .strategyName((String) r.get("strategyname"))
                    .description(t != null ? t.getDescription() : "")
                    .isDefault(t != null && t.isSystemDefault())
                    .totalPicks(picksNum != null ? picksNum.longValue() : 0L)
                    .wonCount(wonNum != null ? wonNum.longValue() : 0L)
                    .lostCount(lostNum != null ? lostNum.longValue() : 0L)
                    .voidCount(voidNum != null ? voidNum.longValue() : 0L)
                    .winratePct(wrNum != null ? wrNum.doubleValue() : 0.0)
                    .totalPnlRub(pnlNum != null ? BigDecimal.valueOf(pnlNum.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .turnoverRub(turnoverNum != null ? BigDecimal.valueOf(turnoverNum.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .roiPct(roiNum != null ? roiNum.doubleValue() : 0.0)
                    .avgOdd(oddNum != null ? oddNum.doubleValue() : 1.0)
                    .minOdd(t != null ? t.getMinOdd() : BigDecimal.valueOf(1.30))
                    .maxOdd(t != null ? t.getMaxOdd() : null)
                    .minProbabilityPct(t != null ? t.getMinProbabilityPct() : BigDecimal.valueOf(88.0))
                    .build();

            result.computeIfAbsent(sport, k -> new ArrayList<>()).add(dto);
        }
        return result;
    }

    private List<StrategyPerformanceDto> mapRowsToStrategyDtos(List<Map<String, Object>> rows) {
        List<AiStrategyTemplate> templates = getAllStrategies();
        Map<String, AiStrategyTemplate> templateMap = new HashMap<>();
        for (AiStrategyTemplate t : templates) {
            templateMap.put(t.getSlug(), t);
        }

        List<StrategyPerformanceDto> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            String slug = (String) r.get("strategyslug");
            AiStrategyTemplate t = templateMap.get(slug);

            Number picksNum = (Number) r.get("totalpicks");
            Number wonNum = (Number) r.get("woncount");
            Number lostNum = (Number) r.get("lostcount");
            Number voidNum = (Number) r.get("voidcount");
            Number wrNum = (Number) r.get("winratepct");
            Number pnlNum = (Number) r.get("totalpnlrub");
            Number turnoverNum = (Number) r.get("turnoverrub");
            Number roiNum = (Number) r.get("roipct");
            Number oddNum = (Number) r.get("avgodd");

            result.add(StrategyPerformanceDto.builder()
                    .strategySlug(slug)
                    .icon((String) r.get("icon"))
                    .strategyName((String) r.get("strategyname"))
                    .description(t != null ? t.getDescription() : "")
                    .isDefault(t != null && t.isSystemDefault())
                    .totalPicks(picksNum != null ? picksNum.longValue() : 0L)
                    .wonCount(wonNum != null ? wonNum.longValue() : 0L)
                    .lostCount(lostNum != null ? lostNum.longValue() : 0L)
                    .voidCount(voidNum != null ? voidNum.longValue() : 0L)
                    .winratePct(wrNum != null ? wrNum.doubleValue() : 0.0)
                    .totalPnlRub(pnlNum != null ? BigDecimal.valueOf(pnlNum.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .turnoverRub(turnoverNum != null ? BigDecimal.valueOf(turnoverNum.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .roiPct(roiNum != null ? roiNum.doubleValue() : 0.0)
                    .avgOdd(oddNum != null ? oddNum.doubleValue() : 1.0)
                    .minOdd(t != null ? t.getMinOdd() : BigDecimal.valueOf(1.30))
                    .maxOdd(t != null ? t.getMaxOdd() : null)
                    .minProbabilityPct(t != null ? t.getMinProbabilityPct() : BigDecimal.valueOf(88.0))
                    .build());
        }
        return result;
    }

    /**
     * Executes multi-strategy voting across all 9 strategies for a given match recommendation.
     */
    @Transactional
    public List<AiStrategyVote> generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket) {
        return generateStrategyVotes(rec, marketOddVal, targetMarket, null);
    }

        @Transactional
    public List<AiStrategyVote> generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket, Map<String, Object> dossierMeta) {
        return generateStrategyVotes(rec, marketOddVal, targetMarket, null, dossierMeta);
    }

    @Transactional
    public List<AiStrategyVote> generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket, List<MarketEdgeCandidateDto> matrixCandidates, Map<String, Object> dossierMeta) {
        if (rec == null || rec.getId() == null) return Collections.emptyList();

        List<AiStrategyTemplate> allStrategies = getAllStrategies();
        if (allStrategies.isEmpty()) return Collections.emptyList();

        List<AiStrategyVote> existing = strategyVoteRepository.findByRecommendationId(rec.getId());

        // Check if existing votes are all PASS / SKIPPED placeholders while the new recommendation has an active bet
        boolean isPassOrNull = (targetMarket == null || targetMarket.isBlank() || "PASS".equalsIgnoreCase(targetMarket) || "WAIT".equalsIgnoreCase(targetMarket));
        boolean newHasRealBet = !isPassOrNull && !"PASS".equalsIgnoreCase(rec.getRecommendedMarket()) && !"WAIT".equalsIgnoreCase(rec.getRecommendedMarket());

        boolean hasOnlyPassVotes = existing != null && !existing.isEmpty() &&
                existing.stream().allMatch(v -> "PASS".equalsIgnoreCase(v.getMarket()) || "WAIT".equalsIgnoreCase(v.getMarket()) || "SKIPPED".equalsIgnoreCase(v.getResultStatus()));

        if (hasOnlyPassVotes && newHasRealBet) {
            log.info("Match #{} (rec #{}) upgraded from PASS to active bet '{}'. Clearing {} obsolete PASS votes for fresh evaluation.",
                    rec.getMatchId(), rec.getId(), rec.getRecommendedMarket(), existing.size());
            strategyVoteRepository.deleteAll(existing);
            existing = Collections.emptyList();
        }

        Set<String> existingSlugs = (existing != null && !existing.isEmpty())
                ? existing.stream().map(AiStrategyVote::getStrategySlug).filter(Objects::nonNull).collect(Collectors.toSet())
                : Collections.emptySet();

        List<AiStrategyTemplate> missingStrategies = allStrategies.stream()
                .filter(s -> !existingSlugs.contains(s.getSlug()))
                .collect(Collectors.toList());

        if (missingStrategies.isEmpty()) {
            return existing != null ? existing : Collections.emptyList();
        }

        log.info("Match #{} (rec #{}) has {} existing votes, evaluating {} missing strategy invariants: {}",
                rec.getMatchId(), rec.getId(), existing != null ? existing.size() : 0, missingStrategies.size(),
                missingStrategies.stream().map(AiStrategyTemplate::getSlug).toList());

        List<AiStrategyVote> newVotes = buildStrategyVotes(rec, marketOddVal, targetMarket, matrixCandidates, missingStrategies, dossierMeta);
        applySettlementIfScoreAvailable(newVotes, rec);

        List<AiStrategyVote> savedNew = strategyVoteRepository.saveAll(newVotes);
        List<AiStrategyVote> combined = new ArrayList<>();
        if (existing != null) combined.addAll(existing);
        combined.addAll(savedNew);
        log.info("Recorded {} new strategy votes (total {}) for match #{} (rec #{})",
                savedNew.size(), combined.size(), rec.getMatchId(), rec.getId());
        return combined;
    }

    /**
     * Helper to settle generated strategy votes if match already has a terminal score.
     */
    private void applySettlementIfScoreAvailable(List<AiStrategyVote> votes, MatchAiRecommendation rec) {
        if (rec == null || votes == null || votes.isEmpty()) return;
        String score = rec.getFinalScore();
        if (score == null || score.isBlank()) return;

        String sportCode = rec.getSportSlug() != null ? rec.getSportSlug() : "football";
        Sport sport = Sports.get(sportCode);
        if ("-:-".equals(score.trim()) || "VOID".equalsIgnoreCase(rec.getResultStatus())) {
            LocalDateTime now = LocalDateTime.now();
            for (AiStrategyVote vote : votes) {
                vote.setFinalScore(score);
                vote.setSettledAt(now);
                if (!"PASS".equals(vote.getVoteDecision()) && !"SKIPPED".equals(vote.getResultStatus())) {
                    vote.setResultStatus("VOID");
                    vote.setActualPnl(BigDecimal.ZERO);
                    vote.setRoiPct(BigDecimal.ZERO);
                }
            }
        } else {
            java.util.Optional<pro.datawiki.igaming.dto.sport.ParsedScore> parsedOpt = pro.datawiki.igaming.dto.sport.ParsedScore.parse(score);
            if (parsedOpt.isPresent()) {
                pro.datawiki.igaming.dto.sport.ParsedScore parsed = parsedOpt.get();
                settleVotesInMemory(votes, score, parsed.score1(), parsed.score2(), sport);
            } else {
                log.warn("Skipping strategy votes settlement for rec #{} - unparseable score: '{}'", rec.getId(), score);
            }
        }
    }

    /**
     * Targeted recalculation of a specific strategy template across all eligible match recommendations.
     * If forcePurge is true, existing votes for this strategy are deleted before recalculation.
     * Evaluates and persists votes strictly for the specified strategy template.
     */
    private void batchInsertVotes(List<AiStrategyVote> votes) {
        if (votes == null || votes.isEmpty()) return;
        String sql = """
            INSERT INTO ai_strategy_vote (
                recommendation_id, match_id, strategy_slug, strategy_name, market, market_label,
                recommended_odd, fair_probability, expected_value_pct, vote_decision,
                stake_rub, result_status, final_score, actual_pnl, roi_pct, model_version, created_at, settled_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                AiStrategyVote v = votes.get(i);
                ps.setLong(1, v.getRecommendationId());
                ps.setLong(2, v.getMatchId());
                ps.setString(3, v.getStrategySlug());
                ps.setString(4, v.getStrategyName());
                ps.setString(5, v.getMarket());
                ps.setString(6, v.getMarketLabel());
                ps.setBigDecimal(7, v.getRecommendedOdd());
                ps.setBigDecimal(8, v.getFairProbability());
                ps.setBigDecimal(9, v.getExpectedValuePct());
                ps.setString(10, v.getVoteDecision());
                ps.setBigDecimal(11, v.getStakeRub());
                ps.setString(12, v.getResultStatus());
                ps.setString(13, v.getFinalScore());
                ps.setBigDecimal(14, v.getActualPnl());
                ps.setBigDecimal(15, v.getRoiPct());
                ps.setString(16, v.getModelVersion());
                ps.setTimestamp(17, v.getCreatedAt() != null ? java.sql.Timestamp.valueOf(v.getCreatedAt()) : java.sql.Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(18, v.getSettledAt() != null ? java.sql.Timestamp.valueOf(v.getSettledAt()) : null);
            }

            @Override
            public int getBatchSize() {
                return votes.size();
            }
        });
    }

    private void batchUpdateRecScores(List<MatchAiRecommendation> recs) {
        if (recs == null || recs.isEmpty()) return;
        String sql = "UPDATE match_ai_recommendation SET final_score = ? WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                MatchAiRecommendation r = recs.get(i);
                ps.setString(1, r.getFinalScore());
                ps.setLong(2, r.getId());
            }

            @Override
            public int getBatchSize() {
                return recs.size();
            }
        });
    }

    @Transactional
    public Map<String, Object> recalculateSpecificStrategy(String strategySlug, boolean forcePurge) {
        if (strategySlug == null || strategySlug.isBlank()) {
            return Map.of("status", "ERROR", "message", "Strategy slug cannot be blank");
        }
        String slug = strategySlug.trim().toLowerCase();
        Optional<AiStrategyTemplate> templateOpt = strategyTemplateRepository.findBySlug(slug);
        if (templateOpt.isEmpty()) {
            return Map.of("status", "ERROR", "message", "Strategy template not found for slug: " + slug);
        }
        AiStrategyTemplate template = templateOpt.get();

        int purgedCount = 0;
        if (forcePurge) {
            purgedCount = jdbcTemplate.update("DELETE FROM ai_strategy_vote WHERE strategy_slug = ?", slug);
            log.info("Purged {} existing strategy votes for slug '{}'", purgedCount, slug);
        }

        List<Long> missingRecIds = jdbcTemplate.queryForList(
                """
                SELECT r.id FROM match_ai_recommendation r
                WHERE r.evaluation_stage NOT IN ('CANCELLED', 'CANCELLED_SCHEDULE_CONFLICT', 'CANCELLED_SYNTHETIC_COLLISION', 'UNMAPPED_TEAM_PASS', 'UNRESOLVED_PLAYER_PASS')
                  AND NOT EXISTS (
                      SELECT 1 FROM ai_strategy_vote v 
                      WHERE v.recommendation_id = r.id AND v.strategy_slug = ?
                  )
                ORDER BY r.id DESC
                """,
                Long.class,
                slug
        );

        log.info("Found {} recommendations missing strategy invariant '{}'", missingRecIds.size(), slug);
        List<AiStrategyTemplate> singleStrategyList = List.of(template);
        List<AiStrategyVote> batchBuffer = new ArrayList<>(700);
        int generatedCount = 0;
        int settledCount = 0;

        for (int i = 0; i < missingRecIds.size(); i += 500) {
            List<Long> chunk = missingRecIds.subList(i, Math.min(i + 500, missingRecIds.size()));
            List<MatchAiRecommendation> recs = recommendationRepository.findAllById(chunk);

            for (MatchAiRecommendation rec : recs) {
                Double odd = (rec.getRecommendedOdd() != null && rec.getRecommendedOdd().doubleValue() > 1.01) ? rec.getRecommendedOdd().doubleValue() : 1.0;
                String market = rec.getRecommendedMarket();
                List<AiStrategyVote> votes = buildStrategyVotes(rec, odd, market, singleStrategyList, null);
                if (!votes.isEmpty()) {
                    applySettlementIfScoreAvailable(votes, rec);
                    if (rec.getFinalScore() != null && !rec.getFinalScore().isBlank()) {
                        settledCount += votes.size();
                    }
                    batchBuffer.addAll(votes);
                    generatedCount += votes.size();
                }

                if (batchBuffer.size() >= 700) {
                    strategyVoteRepository.saveAll(batchBuffer);
                    batchBuffer.clear();
                }
            }
        }

        if (!batchBuffer.isEmpty()) {
            strategyVoteRepository.saveAll(batchBuffer);
            batchBuffer.clear();
        }

        log.info("Recalculation complete for strategy '{}': purged={}, generated={}, settled={}",
                slug, purgedCount, generatedCount, settledCount);

        return Map.of(
                "status", "SUCCESS",
                "strategySlug", slug,
                "strategyName", template.getName(),
                "purgedCount", purgedCount,
                "generatedCount", generatedCount,
                "settledCount", settledCount,
                "recommendationsEvaluated", missingRecIds.size()
        );
    }

    public List<AiStrategyVote> buildStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket, List<AiStrategyTemplate> strategies, Map<String, Object> dossierMeta) {
        return buildStrategyVotes(rec, marketOddVal, targetMarket, null, strategies, dossierMeta);
    }

    public List<AiStrategyVote> buildStrategyVotes(
            MatchAiRecommendation rec,
            Double marketOddVal,
            String targetMarket,
            List<MarketEdgeCandidateDto> matrixCandidates,
            List<AiStrategyTemplate> strategies,
            Map<String, Object> dossierMeta
    ) {
        if (rec == null || rec.getId() == null || strategies == null || strategies.isEmpty()) return Collections.emptyList();

        double odd = (marketOddVal != null && marketOddVal > 1.01)
                ? marketOddVal
                : (rec.getRecommendedOdd() != null && rec.getRecommendedOdd().doubleValue() > 1.01
                    ? rec.getRecommendedOdd().doubleValue()
                    : 1.0);
        String sportSlug = rec.getSportSlug() != null ? rec.getSportSlug().toLowerCase().trim() : "football";

        double p1 = rec.getFairP1() != null ? rec.getFairP1().doubleValue() : 0.50;
        double px = rec.getFairPx() != null ? rec.getFairPx().doubleValue() : 0.25;
        double p2 = rec.getFairP2() != null ? rec.getFairP2().doubleValue() : 0.25;
        double pOver = rec.getFairOver25() != null ? rec.getFairOver25().doubleValue() : 0.50;
        double pUnder = rec.getFairUnder25() != null ? rec.getFairUnder25().doubleValue() : 0.50;

        boolean isPassOrNull = (targetMarket == null || targetMarket.isBlank() || "PASS".equalsIgnoreCase(targetMarket) || "WAIT".equalsIgnoreCase(targetMarket));
        boolean hasBookmaker = ((rec.getAvailableBookmakers() != null && !rec.getAvailableBookmakers().isBlank() && !"none".equalsIgnoreCase(rec.getAvailableBookmakers()))
                || (rec.getBestBookmaker() != null && !rec.getBestBookmaker().isBlank() && !"NONE".equalsIgnoreCase(rec.getBestBookmaker())))
                && odd > 1.01;
        boolean isNoBookmaker = !hasBookmaker;

        boolean isAnalystPassVeto = isPassOrNull
                || isNoBookmaker
                || "PASS".equalsIgnoreCase(rec.getConvictionTier())
                || "WAIT".equalsIgnoreCase(rec.getConvictionTier())
                || "PASS".equalsIgnoreCase(rec.getRecommendedMarket())
                || "WAIT".equalsIgnoreCase(rec.getRecommendedMarket())
                || "SKIPPED_NO_LINEUP".equalsIgnoreCase(rec.getEvaluationStage())
                || (rec.getEvaluationStage() != null && rec.getEvaluationStage().startsWith("SKIPPED_"))
                || "SKIPPED".equalsIgnoreCase(rec.getEvaluationStage());

        if (isAnalystPassVeto) {
            // When the institutional AI analyst issues a PASS/WAIT veto or no real bookmaker quote exists,
            // all strategies MUST respect the veto! Zero phantom bets!
            String vetoReason = isNoBookmaker ? "Пропуск (нет линии букмекера)" : ("Пропуск (аналитик: " + (rec.getConvictionTier() != null ? rec.getConvictionTier() : "PASS") + ")");
            List<AiStrategyVote> emptyVotes = new ArrayList<>();
            for (AiStrategyTemplate strat : strategies) {
                emptyVotes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market("PASS")
                        .marketLabel(vetoReason)
                        .recommendedOdd(BigDecimal.ONE)
                        .fairProbability(BigDecimal.ZERO)
                        .expectedValuePct(BigDecimal.ZERO)
                        .voteDecision("PASS")
                        .stakeRub(BigDecimal.ZERO)
                        .resultStatus("SKIPPED")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : AiAnalystOrchestrator.CURRENT_MODEL_VERSION)
                        .build());
            }
            return emptyVotes;
        }

        Sport sport = Sports.get(sportSlug);
        String market = (targetMarket != null && !targetMarket.isBlank()) ? targetMarket : rec.getRecommendedMarket();

        // 1. Resolve realistic fair probability of the actual recommended market (Strictly from candidate / model)
        double marketProb = 0.50;
        if (!isPassOrNull && market != null) {
            MarketEdgeCandidateDto directCand = null;
            if (matrixCandidates != null) {
                for (MarketEdgeCandidateDto c : matrixCandidates) {
                    if (c != null && market.equalsIgnoreCase(c.getMarket())) {
                        directCand = c;
                        break;
                    }
                }
            }

            if (directCand != null && directCand.getFairProbabilityPct() > 0.0) {
                marketProb = directCand.getFairProbabilityPct() / 100.0;
            } else if (rec.getExpectedValuePct() != null && rec.getRecommendedOdd() != null
                    && rec.getRecommendedOdd().doubleValue() > 1.01
                    && market.equalsIgnoreCase(rec.getRecommendedMarket())) {
                // Exact inversion: EV = (P * odd - 1) * 100 => P = (EV/100 + 1) / odd
                double ev = rec.getExpectedValuePct().doubleValue();
                double recOdd = rec.getRecommendedOdd().doubleValue();
                marketProb = Math.min(0.99, Math.max(0.01, ((ev / 100.0) + 1.0) / recOdd));
            } else {
                pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(market);
                if (bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb) {
                    marketProb = switch (mrb.outcome()) {
                        case WIN1, WIN1_2WAY -> p1;
                        case WIN2, WIN2_2WAY -> p2;
                        case DRAW -> px;
                        case DC_1X -> Math.min(0.99, p1 + px);
                        case DC_X2 -> Math.min(0.99, p2 + px);
                        case DC_12 -> Math.min(0.99, p1 + p2);
                    };
                } else if (bet instanceof pro.datawiki.igaming.dto.market.TotalBet tb) {
                    if (tb.param() != null && Math.abs(tb.param() - 2.5) < 0.05) {
                        marketProb = (tb.direction() == pro.datawiki.igaming.dto.market.TotalBet.Direction.OVER) ? pOver : pUnder;
                    } else if (rec.getFairUnder25() != null && tb.direction() == pro.datawiki.igaming.dto.market.TotalBet.Direction.UNDER) {
                        marketProb = pUnder;
                    } else if (rec.getFairOver25() != null && tb.direction() == pro.datawiki.igaming.dto.market.TotalBet.Direction.OVER) {
                        marketProb = pOver;
                    }
                } else if (bet instanceof pro.datawiki.igaming.dto.market.BinaryMarketBet bmb && bmb.marketType() == pro.datawiki.igaming.dto.market.BinaryMarketBet.MarketType.BTTS) {
                    if (bmb.outcome() == pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.YES && rec.getFairBttsYes() != null) {
                        marketProb = rec.getFairBttsYes().doubleValue();
                    } else if (bmb.outcome() == pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.NO && rec.getFairBttsNo() != null) {
                        marketProb = rec.getFairBttsNo().doubleValue();
                    }
                }
            }
        }

        // 2. Expected Value (% edge over bookmaker)
        double evPct = ((marketProb * odd) - 1.0) * 100.0;
        if (rec.getExpectedValuePct() != null && rec.getExpectedValuePct().compareTo(BigDecimal.ZERO) != 0) {
            evPct = rec.getExpectedValuePct().doubleValue();
        }

        // 3. Readable localized label for the real market
        String marketLabel = (sport != null && !isPassOrNull && market != null)
                ? sport.formatMarketTitle(market, rec.getTeam1Name(), rec.getTeam2Name(), "ru")
                : (market != null ? market : "PASS");

        // 4. Risk/Reward Odds Quality Grade from External Context Advisor (INSIGHT_WARNING / ELITE_VALUE)
        String oddsQualityGrade = (dossierMeta != null && dossierMeta.get("odds_quality_grade") != null)
                ? String.valueOf(dossierMeta.get("odds_quality_grade"))
                : null;
        boolean isInsightWarning = "INSIGHT_WARNING".equalsIgnoreCase(oddsQualityGrade);

        List<AiStrategyVote> votes = new ArrayList<>();

        for (AiStrategyTemplate strat : strategies) {
            double minOdd = strat.getMinOdd() != null ? strat.getMinOdd().doubleValue() : 1.15;
            Double maxOdd = strat.getMaxOdd() != null ? strat.getMaxOdd().doubleValue() : null;
            double minProb = strat.getMinProbabilityPct() != null ? strat.getMinProbabilityPct().doubleValue() : 50.0;
            boolean pureWinAllowed = strat.isPureWinAllowed();
            boolean isConservativeShield = strat.getSortOrder() <= 4
                    || "ultra_sniper".equals(strat.getSlug())
                    || "strict_golden_edge".equals(strat.getSlug())
                    || "nerves_shield".equals(strat.getSlug())
                    || "iron_bank".equals(strat.getSlug());

            MarketEdgeCandidateDto chosenCandidate = null;
            if (matrixCandidates != null && !matrixCandidates.isEmpty() && !isAnalystPassVeto) {
                chosenCandidate = selectBestCandidateForStrategy(strat, matrixCandidates);
            }

            boolean eligible;
            String chosenMarket;
            String chosenMarketLabel;
            double chosenOdd;
            double chosenProb;
            double chosenEv;
            boolean insightBlocked = false;

            if (chosenCandidate != null) {
                eligible = true;
                chosenMarket = chosenCandidate.getMarket();
                chosenMarketLabel = (sport != null)
                        ? sport.formatMarketTitle(chosenMarket, rec.getTeam1Name(), rec.getTeam2Name(), "ru")
                        : chosenCandidate.getMarketLabel();
                chosenOdd = chosenCandidate.getMarketOdd().doubleValue();
                chosenProb = chosenCandidate.getFairProbabilityPct();
                chosenEv = chosenCandidate.getExpectedValuePct() != null ? chosenCandidate.getExpectedValuePct().doubleValue() : 0.0;
            } else {
                chosenMarket = market;
                chosenMarketLabel = marketLabel;
                chosenOdd = odd;
                chosenProb = marketProb * 100.0;
                chosenEv = evPct;

                eligible = !isAnalystPassVeto && !isPassOrNull && odd > 1.01;
                if (eligible) {
                    if (odd < minOdd) eligible = false;
                    if (maxOdd != null && odd > maxOdd) eligible = false;
                    if (chosenProb < minProb) eligible = false;
                    if (!pureWinAllowed && isPureWin(market)) eligible = false;
                    if (evPct < 0.0) eligible = false;
                    if (isDraw(market) && strat.getSortOrder() <= 5) eligible = false;
                }
            }

            // Conservative Risk/Reward shield: protect low-risk strategies when News Radar confirms injury/rotation insight on drifting line
            if (eligible && isInsightWarning && isConservativeShield) {
                eligible = false;
                insightBlocked = true;
            }

            if (eligible) {
                BigDecimal stake = resolveStrategyStake(strat);
                votes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market(chosenMarket)
                        .marketLabel(chosenMarketLabel)
                        .recommendedOdd(BigDecimal.valueOf(chosenOdd).setScale(3, RoundingMode.HALF_UP))
                        .fairProbability(BigDecimal.valueOf(chosenProb).setScale(1, RoundingMode.HALF_UP))
                        .expectedValuePct(BigDecimal.valueOf(Math.round(chosenEv * 10.0) / 10.0).setScale(2, RoundingMode.HALF_UP))
                        .voteDecision("PICK")
                        .stakeRub(stake)
                        .resultStatus("PENDING")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : AiAnalystOrchestrator.CURRENT_MODEL_VERSION)
                        .build());
            } else {
                String passReason = insightBlocked
                        ? "Пропуск (INSIGHT_WARNING: подтверждённая травма/ротация на дрейфе линии)"
                        : (isAnalystPassVeto
                            ? (isNoBookmaker ? "Пропуск (нет линии букмекера)" : "Пропуск (аналитик: PASS)")
                            : String.format("Пропуск (фильтр стратегии: k=[%.2f..%s], min P=%.1f%%)", minOdd, maxOdd != null ? String.format("%.2f", maxOdd) : "∞", minProb));
                votes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market("PASS")
                        .marketLabel(passReason)
                        .recommendedOdd(BigDecimal.ONE)
                        .fairProbability(BigDecimal.ZERO)
                        .expectedValuePct(BigDecimal.ZERO)
                        .voteDecision("PASS")
                        .stakeRub(BigDecimal.ZERO)
                        .resultStatus("SKIPPED")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : AiAnalystOrchestrator.CURRENT_MODEL_VERSION)
                        .build());
            }
        }
        return votes;
    }

    private MarketEdgeCandidateDto selectBestCandidateForStrategy(
            AiStrategyTemplate strat,
            List<MarketEdgeCandidateDto> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        double minOdd = strat.getMinOdd() != null ? strat.getMinOdd().doubleValue() : 1.15;
        Double maxOdd = strat.getMaxOdd() != null ? strat.getMaxOdd().doubleValue() : null;
        double minProb = strat.getMinProbabilityPct() != null ? strat.getMinProbabilityPct().doubleValue() : 50.0;
        boolean pureWinAllowed = strat.isPureWinAllowed();

        List<MarketEdgeCandidateDto> matching = candidates.stream()
                .filter(c -> {
                    if (c == null || c.getMarketOdd() == null) return false;
                    double odd = c.getMarketOdd().doubleValue();
                    double prob = c.getFairProbabilityPct();
                    double ev = c.getExpectedValuePct() != null ? c.getExpectedValuePct().doubleValue() : 0.0;

                    if (odd < minOdd) return false;
                    if (maxOdd != null && odd > maxOdd) return false;
                    if (prob < minProb) return false;
                    if (ev <= 0.0) return false;
                    if (!pureWinAllowed && isPureWin(c.getMarket())) return false;
                    if (isDraw(c.getMarket()) && strat.getSortOrder() <= 5) return false;

                    return true;
                })
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            return null;
        }

        String rule = strat.getLadderStepRule() != null ? strat.getLadderStepRule() : "SAFEST_ELIGIBLE";
        return switch (rule) {
            case "SAFEST_ELIGIBLE" -> matching.stream()
                    .max(Comparator.comparingDouble(MarketEdgeCandidateDto::getFairProbabilityPct))
                    .orElse(matching.get(0));
            case "MAX_ODD_ELIGIBLE", "MAX_EV_ELIGIBLE" -> matching.stream()
                    .max(Comparator.comparingDouble(c -> c.getExpectedValuePct() != null ? c.getExpectedValuePct().doubleValue() : 0.0))
                    .orElse(matching.get(0));
            case "BALANCED", "FULL_MATCH_MACRO" -> matching.stream()
                    .max(Comparator.comparingDouble(c -> {
                        double ev = c.getExpectedValuePct() != null ? c.getExpectedValuePct().doubleValue() : 0.0;
                        double odd = c.getMarketOdd().doubleValue();
                        double p = c.getFairProbabilityPct() / 100.0;
                        // Universal Sharpe / Kelly Utility: variance-penalized edge
                        // Mathematical ranking: penalizes payout variance of longshots and rewards high-confidence certainty
                        double payoutVariance = p * (1.0 - p) * odd * odd;
                        return ev / Math.sqrt(Math.max(0.01, payoutVariance));
                    }))
                    .orElse(matching.get(0));
            default -> matching.get(0);
        };
    }

    private boolean isPureWin(String market) {
        if (market == null || market.isBlank()) return false;
        pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(market);
        return bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb &&
                (mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN1
                        || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN2
                        || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN1_2WAY
                        || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN2_2WAY);
    }

    private boolean isDraw(String market) {
        if (market == null || market.isBlank()) return false;
        pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(market);
        return bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb &&
                mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DRAW;
    }

    /**
     * Settle all strategy votes in-memory for a given final score.
     */
    public void settleVotesInMemory(List<AiStrategyVote> votes, String finalScore, int score1, int score2, Sport sport) {
        if (votes == null || votes.isEmpty() || finalScore == null || sport == null) return;
        LocalDateTime now = LocalDateTime.now();

        for (AiStrategyVote vote : votes) {
            if ("PASS".equals(vote.getVoteDecision()) || "SKIPPED".equals(vote.getResultStatus())) {
                vote.setFinalScore(finalScore);
                vote.setSettledAt(now);
                if ("PASS".equals(vote.getVoteDecision())) {
                    vote.setResultStatus("PASS");
                }
                continue;
            }

            if ("-:-".equals(finalScore) || "-:-".equals(finalScore.trim())) {
                vote.setFinalScore(finalScore);
                vote.setSettledAt(now);
                vote.setResultStatus("VOID");
                vote.setActualPnl(BigDecimal.ZERO);
                vote.setRoiPct(BigDecimal.ZERO);
                continue;
            }

            SettlementOutcome outcome = sport.evaluateSettlement(vote.getMarket(), score1, score2, finalScore);
            BigDecimal stake = vote.getStakeRub() != null ? vote.getStakeRub() : BigDecimal.valueOf(250.0);
            BigDecimal odd = vote.getRecommendedOdd();
            if (odd == null || odd.compareTo(BigDecimal.valueOf(1.01)) <= 0) {
                vote.setFinalScore(finalScore);
                vote.setSettledAt(now);
                vote.setResultStatus("SKIPPED");
                vote.setActualPnl(BigDecimal.ZERO);
                vote.setRoiPct(BigDecimal.ZERO);
                continue;
            }

            vote.setFinalScore(finalScore);
            vote.setSettledAt(now);

            if (outcome == SettlementOutcome.WON) {
                vote.setResultStatus("WON");
                BigDecimal pnl = odd.subtract(BigDecimal.ONE).multiply(stake).setScale(2, RoundingMode.HALF_UP);
                BigDecimal roi = odd.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100.0)).setScale(2, RoundingMode.HALF_UP);
                vote.setActualPnl(pnl);
                vote.setRoiPct(roi);
            } else if (outcome == SettlementOutcome.LOST) {
                vote.setResultStatus("LOST");
                vote.setActualPnl(stake.negate());
                vote.setRoiPct(BigDecimal.valueOf(-100.0));
            } else if (outcome == SettlementOutcome.VOID) {
                vote.setResultStatus("VOID");
                vote.setActualPnl(BigDecimal.ZERO);
                vote.setRoiPct(BigDecimal.ZERO);
            }
        }
    }

    /**
     * Settle all strategy votes when final score is confirmed.
     */
    @Transactional
    public void settleVotesForRecommendation(Long recommendationId, String finalScore, int score1, int score2, Sport sport) {
        if (recommendationId == null || finalScore == null || sport == null) return;
        List<AiStrategyVote> votes = strategyVoteRepository.findByRecommendationId(recommendationId);
        if (votes.isEmpty()) return;

        settleVotesInMemory(votes, finalScore, score1, score2, sport);
        strategyVoteRepository.saveAll(votes);
        log.info("Settled {} strategy votes for rec #{} with score '{}'", votes.size(), recommendationId, finalScore);
    }

    /**
     * Enriches a batch of recommendations with all 9 strategy votes and flags the active strategy vote.
     */
    public void enrichRecommendationsWithVotes(List<MatchAiRecommendation> recs, String requestedStrategySlug) {
        if (recs == null || recs.isEmpty()) return;

        List<Long> ids = recs.stream().map(MatchAiRecommendation::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) return;

        List<AiStrategyVote> allVotes = strategyVoteRepository.findByRecommendationIdIn(ids);
        Map<Long, List<AiStrategyVote>> votesByRec = allVotes.stream()
                .collect(Collectors.groupingBy(AiStrategyVote::getRecommendationId));

        AiStrategyTemplate activeTemplate = resolveActiveStrategy(null, requestedStrategySlug);
        String targetSlug = activeTemplate != null ? activeTemplate.getSlug() : DEFAULT_STRATEGY_SLUG;

        for (MatchAiRecommendation rec : recs) {
            List<AiStrategyVote> votes = votesByRec.getOrDefault(rec.getId(), Collections.emptyList());
            for (AiStrategyVote v : votes) {
                if (v.getVoteDecision() != null && "PICK".equalsIgnoreCase(v.getVoteDecision())
                        && (v.getExpectedValuePct() == null || BigDecimal.ZERO.compareTo(v.getExpectedValuePct()) == 0)
                        && v.getFairProbability() != null && v.getRecommendedOdd() != null
                        && v.getRecommendedOdd().doubleValue() > 1.0) {
                    double prob = v.getFairProbability().doubleValue();
                    double p = prob > 1.0 ? prob / 100.0 : prob;
                    double odd = v.getRecommendedOdd().doubleValue();
                    double evPct = (p * odd - 1.0) * 100.0;
                    v.setExpectedValuePct(BigDecimal.valueOf(Math.round(evPct * 10.0) / 10.0).setScale(2, RoundingMode.HALF_UP));
                }
            }
            rec.setStrategyVotes(votes);

            for (AiStrategyVote v : votes) {
                if (targetSlug.equals(v.getStrategySlug())) {
                    rec.setActiveStrategyVote(v);
                    break;
                }
            }
        }
    }

    /**
     * Complete recalculation and backfill of all 9 strategies across all recommendations.
     * When forceAll is true, purges existing votes to rebuild fresh votes under updated risk rules.
     * Processes in short batch transactions to avoid DB idle-in-transaction timeouts across FDW.
     */
    public Map<String, Object> recalculateAllStrategyVotes(boolean forceAll) {
        if (forceAll) {
            log.warn("Clearing all existing strategy votes for full recalculation...");
            strategyVoteRepository.deleteAllInBatch();
        }

                // Map each recommendation_id to its set of existing strategy slugs for granular invariant completeness checks
        Map<Long, Set<String>> existingSlugsByRecId = new HashMap<>();
        if (!forceAll) {
            jdbcTemplate.query("SELECT recommendation_id, strategy_slug FROM ai_strategy_vote", rs -> {
                long recId = rs.getLong("recommendation_id");
                String sSlug = rs.getString("strategy_slug");
                existingSlugsByRecId.computeIfAbsent(recId, k -> new HashSet<>()).add(sSlug);
            });
        }

        // 1. Load strategy templates once
        List<AiStrategyTemplate> strategies = getAllStrategies();
        if (strategies.isEmpty()) {
            return Map.of("status", "ERROR", "message", "No strategies found");
        }

        // 2. Prefetch finished match scores in a single query across FDW
        Map<Long, String> scoreByMatchId = new HashMap<>();
        try {
            String sql = "SELECT id, COALESCE(regular_score1, score1) AS s1, COALESCE(regular_score2, score2) AS s2 FROM mdm_match WHERE status IN ('FINISHED', 'SETTLED') AND (regular_score1 IS NOT NULL OR score1 IS NOT NULL)";
            jdbcTemplate.query(sql, rs -> {
                long mId = rs.getLong("id");
                String s1 = rs.getString("s1");
                String s2 = rs.getString("s2");
                if (s1 != null && s2 != null && !s1.isBlank() && !s2.isBlank()) {
                    scoreByMatchId.put(mId, s1.trim() + ":" + s2.trim());
                }
            });
            log.info("Prefetched {} finished match scores for recalculation", scoreByMatchId.size());
        } catch (Exception ex) {
            log.warn("Prefetching match scores failed: {}", ex.getMessage());
        }

        List<MatchAiRecommendation> allRecs = recommendationRepository.findAll();
        int generatedCount = 0;
        int settledCount = 0;
        List<AiStrategyVote> batchBuffer = new ArrayList<>(700);
        List<MatchAiRecommendation> recsToUpdate = new ArrayList<>();

                for (MatchAiRecommendation rec : allRecs) {
            // Look up score: check recommendation first, then pre-fetched map
            String score = rec.getFinalScore();
            if ((score == null || score.isBlank() || "-:-".equals(score.trim())) && rec.getMatchId() != null) {
                String fetched = scoreByMatchId.get(rec.getMatchId());
                if (fetched != null) {
                    score = fetched;
                    rec.setFinalScore(score);
                    recsToUpdate.add(rec);
                }
            }

            Set<String> existingSlugs = existingSlugsByRecId.getOrDefault(rec.getId(), Collections.emptySet());
            List<AiStrategyTemplate> missingStrategies = strategies.stream()
                    .filter(s -> forceAll || !existingSlugs.contains(s.getSlug()))
                    .collect(Collectors.toList());

            if (missingStrategies.isEmpty()) {
                continue;
            }

            Double odd = (rec.getRecommendedOdd() != null && rec.getRecommendedOdd().doubleValue() > 1.01) ? rec.getRecommendedOdd().doubleValue() : 1.0;
            String market = rec.getRecommendedMarket();
            List<AiStrategyVote> votes = buildStrategyVotes(rec, odd, market, missingStrategies, null);
            generatedCount += votes.size();

            if (score != null && !score.isBlank() && !votes.isEmpty()) {
                applySettlementIfScoreAvailable(votes, rec);
                settledCount += votes.size();
            }

            batchBuffer.addAll(votes);
            if (batchBuffer.size() >= 1000) {
                batchInsertVotes(batchBuffer);
                batchBuffer.clear();
                if (!recsToUpdate.isEmpty()) {
                    batchUpdateRecScores(recsToUpdate);
                    recsToUpdate.clear();
                }
                log.info("Recalculation progress: {}/{} recommendations processed", generatedCount, allRecs.size());
            }
        }

        if (!batchBuffer.isEmpty()) {
            batchInsertVotes(batchBuffer);
            batchBuffer.clear();
        }
        if (!recsToUpdate.isEmpty()) {
            batchUpdateRecScores(recsToUpdate);
            recsToUpdate.clear();
        }

        log.info("Recalculation complete: total={}, generated={}, settled={}", allRecs.size(), generatedCount, settledCount);
        return Map.of(
                "status", "SUCCESS",
                "totalRecommendations", allRecs.size(),
                "generatedRecommendations", generatedCount,
                "settledRecommendations", settledCount
        );
    }

    /**
     * Backfills all recommendations currently missing strategy votes and settles those with final scores.
     */
    @Transactional
    public int backfillAllMissingVotes() {
        Map<String, Object> res = recalculateAllStrategyVotes(false);
        return ((Number) res.getOrDefault("generatedRecommendations", 0)).intValue();
    }

    /**
     * Creates a custom strategy template.
     */
    @Transactional
    public AiStrategyTemplate createCustomStrategy(AiStrategyTemplate template) {
        if (template.getName() == null || template.getName().isBlank()) {
            throw new IllegalArgumentException("Strategy name is required");
        }
        String slug = template.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = "custom_" + System.currentTimeMillis();
        }
        template.setSlug(slug.trim().toLowerCase());
        template.setSystemDefault(false);
        if (template.getSortOrder() == null || template.getSortOrder() == 0) {
            template.setSortOrder(100);
        }
        AiStrategyTemplate saved = strategyTemplateRepository.save(template);
        log.info("Created custom strategy '{}' ({})", saved.getName(), saved.getSlug());
        return saved;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private BigDecimal resolveStrategyStake(AiStrategyTemplate strat) {
        if (strat == null || strat.getSlug() == null) return BigDecimal.valueOf(250.0);
        return switch (strat.getSlug()) {
            case "ultra_sniper" -> BigDecimal.valueOf(1000.0);
            case "strict_golden_edge" -> BigDecimal.valueOf(1250.0);
            case "nerves_shield" -> BigDecimal.valueOf(1500.0);
            case "iron_bank" -> BigDecimal.valueOf(2000.0);
            case "macro_edge", "balanced_growth" -> BigDecimal.valueOf(500.0);
            case "tactical_value" -> BigDecimal.valueOf(250.0);
            case "high_odds" -> BigDecimal.valueOf(150.0);
            case "jackpot_outlier" -> BigDecimal.valueOf(100.0);
            default -> BigDecimal.valueOf(250.0);
        };
    }
}
