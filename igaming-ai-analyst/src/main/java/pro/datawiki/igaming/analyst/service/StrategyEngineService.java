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
            if (sport == null) sport = "football";

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
     * Executes multi-strategy voting across all 7 strategies for a given match recommendation.
     */
    @Transactional
    public List<AiStrategyVote> generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket) {
        return generateStrategyVotes(rec, marketOddVal, targetMarket, null);
    }

    @Transactional
    public List<AiStrategyVote> generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket, Map<String, Object> dossierMeta) {
        if (rec == null || rec.getId() == null) return Collections.emptyList();
        // If votes already exist, don't duplicate
        List<AiStrategyVote> existing = strategyVoteRepository.findByRecommendationId(rec.getId());
        if (!existing.isEmpty()) return existing;

        List<AiStrategyTemplate> strategies = getAllStrategies();
        if (strategies.isEmpty()) return Collections.emptyList();

        List<AiStrategyVote> votes = buildStrategyVotes(rec, marketOddVal, targetMarket, strategies, dossierMeta);
        List<AiStrategyVote> saved = strategyVoteRepository.saveAll(votes);
        log.info("Recorded {} strategy votes for match #{} (rec #{})", saved.size(), rec.getMatchId(), rec.getId());
        return saved;
    }

    public List<AiStrategyVote> buildStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket, List<AiStrategyTemplate> strategies, Map<String, Object> dossierMeta) {
        if (rec == null || rec.getId() == null || strategies == null || strategies.isEmpty()) return Collections.emptyList();

        double odd = marketOddVal != null && marketOddVal > 1.01 ? marketOddVal : 1.90;
        String sportSlug = rec.getSportSlug() != null ? rec.getSportSlug().toLowerCase().trim() : "football";

        double p1 = rec.getFairP1() != null ? rec.getFairP1().doubleValue() : 0.50;
        double px = rec.getFairPx() != null ? rec.getFairPx().doubleValue() : 0.25;
        double p2 = rec.getFairP2() != null ? rec.getFairP2().doubleValue() : 0.25;
        double pOver = rec.getFairOver25() != null ? rec.getFairOver25().doubleValue() : 0.50;
        double pUnder = rec.getFairUnder25() != null ? rec.getFairUnder25().doubleValue() : 0.50;

        boolean isPassOrNull = (targetMarket == null || targetMarket.isBlank() || "PASS".equalsIgnoreCase(targetMarket) || "WAIT".equalsIgnoreCase(targetMarket));
        boolean isTotalsPick = !isPassOrNull && targetMarket.toUpperCase().contains("TOTAL");
        boolean isAwayPick = !isPassOrNull && (targetMarket.contains("2") || targetMarket.toUpperCase().contains("AWAY") || targetMarket.toUpperCase().contains("X2"));

        if (rec.getFairP1() == null && isPassOrNull) {
            // No model predictions available and analyst passed - all strategies pass cleanly
            List<AiStrategyVote> emptyVotes = new ArrayList<>();
            for (AiStrategyTemplate strat : strategies) {
                emptyVotes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market("PASS")
                        .marketLabel("Пропуск (нет вероятностной модели)")
                        .recommendedOdd(BigDecimal.ONE)
                        .fairProbability(BigDecimal.ZERO)
                        .expectedValuePct(BigDecimal.ZERO)
                        .voteDecision("PASS")
                        .stakeRub(BigDecimal.ZERO)
                        .resultStatus("SKIPPED")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : "v0.3.3.1.1")
                        .build());
            }
            return strategyVoteRepository.saveAll(emptyVotes);
        }

        if (isPassOrNull) {
            // Deduce natural directional preference from model fair probabilities
            if (pOver >= 0.65) {
                isTotalsPick = true;
            } else if (pUnder >= 0.65) {
                isTotalsPick = true;
            } else if (p2 > p1 && p2 >= 0.38) {
                isAwayPick = true;
            } else {
                isAwayPick = false;
            }
        }

        Sport sport = Sports.get(sportSlug);

        // Disciplinary & regional context for football and card-aware sports
        boolean isDerby = false;
        boolean isHighCardRegion = false;
        boolean isStrictReferee = false;
        Float refAvgYc = null;

        if (dossierMeta != null) {
            isDerby = Boolean.TRUE.equals(dossierMeta.get("is_derby"));
            if (!isDerby) {
                isDerby = pro.datawiki.igaming.analyst.ml.vector.DerbyRivalryDetector.isDerbyMatch(
                        rec.getTeam1Name(), rec.getTeam2Name(), rec.getLeagueName(),
                        (String) dossierMeta.get("country_name"));
            }
            String cLower = ((dossierMeta.get("country_name") != null ? dossierMeta.get("country_name") : "") + " "
                    + (rec.getLeagueName() != null ? rec.getLeagueName() : "")).toLowerCase();
            isHighCardRegion = cLower.contains("paraguay") || cLower.contains("парагвай")
                    || cLower.contains("costa rica") || cLower.contains("коста-рика")
                    || cLower.contains("mexico") || cLower.contains("мексика")
                    || cLower.contains("colombia") || cLower.contains("колумбия")
                    || cLower.contains("argentina") || cLower.contains("аргентина")
                    || cLower.contains("chile") || cLower.contains("чили")
                    || cLower.contains("brazil") || cLower.contains("бразилия")
                    || cLower.contains("uruguay") || cLower.contains("уругвай")
                    || cLower.contains("peru") || cLower.contains("перу")
                    || cLower.contains("ecuador") || cLower.contains("эквадор")
                    || cLower.contains("bolivia") || cLower.contains("боливия")
                    || cLower.contains("greece") || cLower.contains("греция")
                    || cLower.contains("turkey") || cLower.contains("турция")
                    || cLower.contains("serbia") || cLower.contains("сербия")
                    || cLower.contains("spain") || cLower.contains("испания") || cLower.contains("segunda")
                    || cLower.contains("poland") || cLower.contains("польша");

            if (dossierMeta.get("referee_avg_yellow_cards") instanceof Number n) {
                refAvgYc = n.floatValue();
                if (refAvgYc >= 4.2f) {
                    isStrictReferee = true;
                }
            }
            String strictness = (String) dossierMeta.get("referee_strictness");
            if ("STRICT".equalsIgnoreCase(strictness) || "VERY_STRICT".equalsIgnoreCase(strictness)) {
                isStrictReferee = true;
            }
        } else {
            isDerby = pro.datawiki.igaming.analyst.ml.vector.DerbyRivalryDetector.isDerbyMatch(
                    rec.getTeam1Name(), rec.getTeam2Name(), rec.getLeagueName(), "");
        }

        Double expTotal = dossierMeta != null && dossierMeta.get("expected_total_baseline") instanceof Number n ? n.doubleValue() : null;
        Double cushionLow = dossierMeta != null && dossierMeta.get("tournament_cushion_low") instanceof Number n ? n.doubleValue() : null;
        Double cushionHigh = dossierMeta != null && dossierMeta.get("tournament_cushion_high") instanceof Number n ? n.doubleValue() : null;
        Double probCushLow = dossierMeta != null && dossierMeta.get("prob_cushion_low") instanceof Number n ? n.doubleValue() : null;
        Double probCushHigh = dossierMeta != null && dossierMeta.get("prob_cushion_high") instanceof Number n ? n.doubleValue() : null;
        Double handicapDelta = dossierMeta != null && dossierMeta.get("handicap_cushion_delta") instanceof Number n ? n.doubleValue() : null;
        if (handicapDelta == null && dossierMeta != null && dossierMeta.get("tournament_handicap_delta") instanceof Number n) {
            handicapDelta = n.doubleValue();
        }
        if (dossierMeta != null && dossierMeta.get("signals") instanceof Map<?, ?> sig) {
            if (sig.get("prob_cushion_low") instanceof Number n) probCushLow = n.doubleValue();
            if (sig.get("prob_cushion_high") instanceof Number n) probCushHigh = n.doubleValue();
            if (cushionLow == null && sig.get("cushion_low_line") instanceof Number n) cushionLow = n.doubleValue();
            if (cushionHigh == null && sig.get("cushion_high_line") instanceof Number n) cushionHigh = n.doubleValue();
            if (expTotal == null && sig.get("tournament_baseline_total") instanceof Number n) expTotal = n.doubleValue();
            if (handicapDelta == null && sig.get("handicap_cushion_delta") instanceof Number n) handicapDelta = n.doubleValue();
            if (handicapDelta == null && sig.get("tournament_handicap_delta") instanceof Number n) handicapDelta = n.doubleValue();
        }

        StrategyLadderContext ladderCtx = new StrategyLadderContext(
                p1, p2, px, pOver, pUnder, odd, targetMarket,
                isTotalsPick, isAwayPick, isDerby, isHighCardRegion, isStrictReferee, refAvgYc,
                expTotal, cushionLow, cushionHigh, probCushLow, probCushHigh, handicapDelta
        );

        int matchBestOf = 3;
        if (dossierMeta != null) {
            matchBestOf = pro.datawiki.igaming.dto.CanonicalMatchHeaderDto.parseBestOfFromStrings(
                    rec.getLeagueName(),
                    (String) dossierMeta.get("tournament_name"),
                    (String) dossierMeta.get("stage"),
                    (String) dossierMeta.get("round")
            );
        } else {
            matchBestOf = pro.datawiki.igaming.dto.CanonicalMatchHeaderDto.parseBestOfFromStrings(rec.getLeagueName());
        }

        List<StrategyMarketOption> sportLadder = sport != null ? sport.buildStrategyLadder(ladderCtx) : Collections.emptyList();
        List<MarketOption> ladder = new ArrayList<>();
        for (StrategyMarketOption opt : sportLadder) {
            if (sport == null || sport.isMarketSupported(opt.market(), matchBestOf)) {
                ladder.add(new MarketOption(opt.market(), opt.label(), opt.odd(), opt.prob(), opt.isSafeStep()));
            }
        }

        List<AiStrategyVote> votes = new ArrayList<>();

        for (AiStrategyTemplate strat : strategies) {
            double minOdd = strat.getMinOdd() != null ? strat.getMinOdd().doubleValue() : 1.30;
            Double maxOdd = strat.getMaxOdd() != null ? strat.getMaxOdd().doubleValue() : null;
            double minProb = strat.getMinProbabilityPct() != null ? strat.getMinProbabilityPct().doubleValue() : 50.0;
            boolean pureWinAllowed = strat.isPureWinAllowed();

            List<MarketOption> eligible = new ArrayList<>();
            for (MarketOption opt : ladder) {
                // Cut off micro-odds strictly
                if (opt.odd < 1.30) continue;
                if (opt.odd < minOdd) continue;
                if (maxOdd != null && opt.odd > maxOdd) continue;

                // Strict probability threshold
                if ((opt.prob * 100.0) < minProb) continue;

                // Strict veto of pure wins for conservative strategies
                if (!pureWinAllowed && isPureWin(opt.market)) continue;

                eligible.add(opt);
            }

            MarketOption chosen = null;
            String decision = "PICK";

            if ("SAFEST_ELIGIBLE".equals(strat.getLadderStepRule())) {
                if (!eligible.isEmpty()) {
                    eligible.sort((a, b) -> Double.compare(b.prob, a.prob));
                    chosen = eligible.get(0);
                } else {
                    decision = "PASS";
                }
            } else if ("BALANCED".equals(strat.getLadderStepRule())) {
                if (!eligible.isEmpty()) {
                    eligible.sort((a, b) -> Double.compare((b.prob * b.odd), (a.prob * a.odd)));
                    chosen = eligible.get(0);
                } else {
                    decision = "PASS";
                }
            } else { // MAX_ODD_ELIGIBLE / High odds
                if (!eligible.isEmpty()) {
                    // Institutional EV-weighted selection for high odds:
                    // Prioritize Expected Value (prob * odd). When EVs are close (within 0.05), favor higher odd.
                    eligible.sort((a, b) -> {
                        double evB = b.prob * b.odd;
                        double evA = a.prob * a.odd;
                        if (Math.abs(evB - evA) > 0.05) {
                            return Double.compare(evB, evA);
                        }
                        return Double.compare(b.odd, a.odd);
                    });
                    chosen = eligible.get(0);
                } else {
                    decision = "PASS";
                }
            }

            if (chosen != null && "PICK".equals(decision)) {
                votes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market(chosen.market)
                        .marketLabel(chosen.label)
                        .recommendedOdd(BigDecimal.valueOf(chosen.odd).setScale(3, RoundingMode.HALF_UP))
                        .fairProbability(BigDecimal.valueOf(chosen.prob * 100.0).setScale(1, RoundingMode.HALF_UP))
                        .expectedValuePct(BigDecimal.ZERO)
                        .voteDecision("PICK")
                        .stakeRub(BigDecimal.valueOf(250.0))
                        .resultStatus("PENDING")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : "v0.3.3.1.1")
                        .build());
            } else {
                votes.add(AiStrategyVote.builder()
                        .recommendationId(rec.getId())
                        .matchId(rec.getMatchId())
                        .strategySlug(strat.getSlug())
                        .strategyName(strat.getName())
                        .market("PASS")
                        .marketLabel("Пропуск (не удовлетворяет критериям риска)")
                        .recommendedOdd(BigDecimal.ONE)
                        .fairProbability(BigDecimal.ZERO)
                        .expectedValuePct(BigDecimal.ZERO)
                        .voteDecision("PASS")
                        .stakeRub(BigDecimal.ZERO)
                        .resultStatus("SKIPPED")
                        .actualPnl(BigDecimal.ZERO)
                        .roiPct(BigDecimal.ZERO)
                        .modelVersion(rec.getModelVersion() != null ? rec.getModelVersion() : "v0.3.3.1.1")
                        .build());
            }
        }

        return votes;
    }

    private boolean isPureWin(String market) {
        if (market == null) return false;
        String m = market.toUpperCase().trim();
        return m.equals("WIN1") || m.equals("WIN2") || m.equals("P1") || m.equals("P2")
                || m.equals("1") || m.equals("2") || m.equals("HOME") || m.equals("AWAY")
                || m.equals("CLEAN_WIN1") || m.equals("CLEAN_WIN2")
                || m.startsWith("WIN_") || m.endsWith("_WIN");
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
                continue;
            }

            SettlementOutcome outcome = sport.evaluateSettlement(vote.getMarket(), score1, score2, finalScore);
            BigDecimal stake = vote.getStakeRub() != null ? vote.getStakeRub() : BigDecimal.valueOf(250.0);
            BigDecimal odd = vote.getRecommendedOdd() != null ? vote.getRecommendedOdd() : BigDecimal.valueOf(1.90);

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
     * Enriches a batch of recommendations with all 7 strategy votes and flags the active strategy vote.
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
     * Complete recalculation and backfill of all 7 ladder strategies across all recommendations.
     * When forceAll is true, purges existing votes to rebuild fresh votes under updated risk rules.
     * Processes in short batch transactions to avoid DB idle-in-transaction timeouts across FDW.
     */
    public Map<String, Object> recalculateAllStrategyVotes(boolean forceAll) {
        if (forceAll) {
            log.warn("Clearing all existing strategy votes for full recalculation...");
            strategyVoteRepository.deleteAllInBatch();
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
            Double odd = rec.getRecommendedOdd() != null ? rec.getRecommendedOdd().doubleValue() : 1.90;
            String market = rec.getRecommendedMarket();
            List<AiStrategyVote> votes = buildStrategyVotes(rec, odd, market, strategies, null);
            generatedCount++;

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

            if (score != null && !score.isBlank() && !"-:-".equals(score.trim()) && !votes.isEmpty()) {
                String sportCode = rec.getSportSlug() != null ? rec.getSportSlug() : "football";
                Sport sport = Sports.get(sportCode);
                int[] parsed = parseScore(score);
                settleVotesInMemory(votes, score, parsed[0], parsed[1], sport);
                settledCount++;
            }

            batchBuffer.addAll(votes);
            if (batchBuffer.size() >= 700) {
                strategyVoteRepository.saveAll(batchBuffer);
                batchBuffer.clear();
                if (!recsToUpdate.isEmpty()) {
                    recommendationRepository.saveAll(recsToUpdate);
                    recsToUpdate.clear();
                }
                log.info("Recalculation progress: {}/{} recommendations processed", generatedCount, allRecs.size());
            }
        }

        if (!batchBuffer.isEmpty()) {
            strategyVoteRepository.saveAll(batchBuffer);
            batchBuffer.clear();
        }
        if (!recsToUpdate.isEmpty()) {
            recommendationRepository.saveAll(recsToUpdate);
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

    private int[] parseScore(String s) {
        if (s == null) return new int[]{0, 0};
        String cleaned = s.split("[,\\(]")[0].trim();
        String[] parts = cleaned.split("[:\\-]");
        if (parts.length >= 2) {
            try {
                return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
            } catch (Exception ignored) {}
        }
        return new int[]{0, 0};
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static class MarketOption {
        String market;
        String label;
        double odd;
        double prob;
        boolean isSafeStep;

        MarketOption(String market, String label, double odd, double prob, boolean isSafeStep) {
            this.market = market;
            this.label = label;
            this.odd = odd;
            this.prob = prob;
            this.isSafeStep = isSafeStep;
        }
    }
}
