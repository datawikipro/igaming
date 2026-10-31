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
        List<Map<String, Object>> rows = strategyVoteRepository.getMultiStrategyPerformanceSummary();
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
    public void generateStrategyVotes(MatchAiRecommendation rec, Double marketOddVal, String targetMarket) {
        if (rec == null || rec.getId() == null) return;
        List<AiStrategyTemplate> strategies = getAllStrategies();
        if (strategies.isEmpty()) return;

        // If votes already exist, don't duplicate
        List<AiStrategyVote> existing = strategyVoteRepository.findByRecommendationId(rec.getId());
        if (!existing.isEmpty()) return;

        double odd = marketOddVal != null && marketOddVal > 1.01 ? marketOddVal : 1.90;
        String m = targetMarket != null ? targetMarket : "WIN1";
        String sportSlug = rec.getSportSlug() != null ? rec.getSportSlug().toLowerCase() : "football";

        double p1 = rec.getFairP1() != null ? rec.getFairP1().doubleValue() : 0.50;
        double px = rec.getFairPx() != null ? rec.getFairPx().doubleValue() : 0.25;
        double p2 = rec.getFairP2() != null ? rec.getFairP2().doubleValue() : 0.25;
        double pOver = rec.getFairOver25() != null ? rec.getFairOver25().doubleValue() : 0.50;
        double pUnder = rec.getFairUnder25() != null ? rec.getFairUnder25().doubleValue() : 0.50;

        List<MarketOption> ladder = new ArrayList<>();

        boolean isAwayPick = m.contains("2") || m.contains("AWAY") || m.contains("X2");
        boolean isTotalsPick = m.startsWith("TOTAL_");

        if ("football".equals(sportSlug) || "soccer".equals(sportSlug)) {
            // Secondary Blind-Spot 1: Low-Friction Stalemate ("Матч улиток" - Cards Under)
            if (px >= 0.25 || pUnder >= 0.50) {
                ladder.add(new MarketOption("TOTAL_YELLOW_CARDS_UNDER_4.5", "Желтые карточки ТМ 4.5 (Слепая зона: матч улиток)", 1.72, 0.82, true));
                ladder.add(new MarketOption("HT_TOTAL_UNDER_1.0", "1-й тайм ТМ 1.0 (Возврат при 1)", 1.68, 0.74, true));
            }

            // Secondary Blind-Spot 2: First-half Double Chance (Cautious Opening 45')
            if (isAwayPick) {
                ladder.add(new MarketOption("HT_DOUBLE_CHANCE_X2", "1-й тайм X2", 1.34, Math.min(0.92, p2 + px * 0.85), true));
            } else {
                ladder.add(new MarketOption("HT_DOUBLE_CHANCE_1X", "1-й тайм 1X", 1.34, Math.min(0.92, p1 + px * 0.85), true));
            }

            // Public Fan Broken Line ("Фанаты сломали стакан" 1.30..1.45)
            if (odd <= 1.45 && !isTotalsPick) {
                ladder.add(new MarketOption("DRAW", "Ничья (Валуй против сломанного стакана)", 3.45, px, false));
                if (!isAwayPick) {
                    ladder.add(new MarketOption("AH_HOME_-1.0", "Фора 1 (-1.0) / Возврат при минимальной победе", round2(odd * 1.55), p1 * 0.75, false));
                }
            }

            if (isTotalsPick) {
                if (m.contains("UNDER")) {
                    ladder.add(new MarketOption("TOTAL_UNDER_3.5", "Тотал меньше 3.5", 1.42, Math.min(0.96, pUnder + 0.18), true));
                    ladder.add(new MarketOption("TOTAL_UNDER_2.5", "Тотал меньше 2.5", odd, pUnder, false));
                    ladder.add(new MarketOption("TOTAL_UNDER_1.5", "Тотал меньше 1.5", round2(odd * 1.55), Math.max(0.20, pUnder - 0.20), false));
                } else {
                    ladder.add(new MarketOption("TOTAL_OVER_1.5", "Тотал больше 1.5", 1.32, Math.min(0.95, pOver + 0.20), true));
                    ladder.add(new MarketOption("TOTAL_OVER_2.5", "Тотал больше 2.5", odd, pOver, false));
                    ladder.add(new MarketOption("TOTAL_OVER_3.5", "Тотал больше 3.5", round2(odd * 1.65), Math.max(0.18, pOver - 0.22), false));
                }
            } else if (isAwayPick) {
                double p2x = Math.min(0.96, p2 + px);
                double odd2x = Math.max(1.30, Math.min(1.65, round2(1.0 / Math.max(0.01, (1.0 / Math.max(1.01, odd) + 1.0 / 3.3) * 0.95))));
                ladder.add(new MarketOption("DOUBLE_CHANCE_X2", "X2 (Ничья или победа гостей)", odd2x, p2x, true));

                double oddDnb2 = Math.max(1.38, round2(odd * 0.73));
                double pDnb2 = (p1 + p2) > 0 ? (p2 / (p1 + p2)) : 0.65;
                ladder.add(new MarketOption("AH_AWAY_0", "Фора 2 (0) / DNB", oddDnb2, pDnb2, true));

                ladder.add(new MarketOption("WIN2", "Чистая победа 2", odd, p2, false));
            } else {
                double p1x = Math.min(0.96, p1 + px);
                double odd1x = Math.max(1.30, Math.min(1.55, round2(1.0 / Math.max(0.01, (1.0 / Math.max(1.01, odd) + 1.0 / 3.3) * 0.95))));
                ladder.add(new MarketOption("DOUBLE_CHANCE_1X", "1X (Победа хозяев или ничья)", odd1x, p1x, true));

                double oddDnb1 = Math.max(1.38, round2(odd * 0.73));
                double pDnb1 = (p1 + p2) > 0 ? (p1 / (p1 + p2)) : 0.65;
                ladder.add(new MarketOption("AH_HOME_0", "Фора 1 (0) / DNB", oddDnb1, pDnb1, true));

                ladder.add(new MarketOption("WIN1", "Чистая победа 1", odd, p1, false));
            }
        } else if ("tennis".equals(sportSlug)) {
            if (m.contains("PLUS_1.5")) {
                ladder.add(new MarketOption(m, "Фора по сетам (+1.5)", odd, 0.85, true));
                String cleanWin = m.contains("HOME") || m.contains("1") ? "WIN1" : "WIN2";
                ladder.add(new MarketOption(cleanWin, "Чистая победа", round2(odd * 1.55), 0.65, false));
            } else {
                ladder.add(new MarketOption(m, m, odd, Math.max(p1, pOver), false));
            }
        } else {
            ladder.add(new MarketOption(m, m, odd, Math.max(p1, pOver), false));
        }

        List<AiStrategyVote> votes = new ArrayList<>();

        for (AiStrategyTemplate strat : strategies) {
            double minOdd = strat.getMinOdd() != null ? strat.getMinOdd().doubleValue() : 1.30;
            Double maxOdd = strat.getMaxOdd() != null ? strat.getMaxOdd().doubleValue() : null;

            List<MarketOption> eligible = new ArrayList<>();
            for (MarketOption opt : ladder) {
                if (opt.odd >= minOdd && (maxOdd == null || opt.odd <= maxOdd)) {
                    eligible.add(opt);
                }
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
                    chosen = eligible.get(0);
                } else {
                    decision = "PASS";
                }
            } else { // MAX_ODD_ELIGIBLE / High odds
                if (!eligible.isEmpty()) {
                    eligible.sort((a, b) -> Double.compare(b.odd, a.odd));
                    chosen = eligible.get(0);
                } else if (odd >= minOdd && (maxOdd == null || odd <= maxOdd)) {
                    chosen = new MarketOption(m, m, odd, Math.max(p1, pOver), false);
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
                        .build());
            }
        }

        strategyVoteRepository.saveAll(votes);
        log.info("Recorded {} strategy votes for match #{} (rec #{})", votes.size(), rec.getMatchId(), rec.getId());
    }

    /**
     * Settle all strategy votes when final score is confirmed.
     */
    @Transactional
    public void settleVotesForRecommendation(Long recommendationId, String finalScore, int score1, int score2, Sport sport) {
        if (recommendationId == null || finalScore == null || sport == null) return;
        List<AiStrategyVote> votes = strategyVoteRepository.findByRecommendationId(recommendationId);
        if (votes.isEmpty()) return;

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
     * Backfills all recommendations currently missing strategy votes and settles those with final scores.
     */
    @Transactional
    public int backfillAllMissingVotes() {
        List<MatchAiRecommendation> allRecs = recommendationRepository.findAll();
        int backfilled = 0;
        for (MatchAiRecommendation rec : allRecs) {
            List<AiStrategyVote> existing = strategyVoteRepository.findByRecommendationId(rec.getId());
            if (existing.isEmpty()) {
                Double odd = rec.getRecommendedOdd() != null ? rec.getRecommendedOdd().doubleValue() : 1.90;
                String market = rec.getRecommendedMarket() != null ? rec.getRecommendedMarket() : "WIN1";
                generateStrategyVotes(rec, odd, market);
                backfilled++;

                if (rec.getFinalScore() != null && !rec.getFinalScore().isBlank() && !"-:-".equals(rec.getFinalScore().trim())) {
                    String sportCode = rec.getSportSlug() != null ? rec.getSportSlug() : "football";
                    Sport sport = Sports.get(sportCode);
                    int[] parsed = parseScore(rec.getFinalScore());
                    settleVotesForRecommendation(rec.getId(), rec.getFinalScore(), parsed[0], parsed[1], sport);
                }
            }
        }
        log.info("Backfilled strategy votes for {} recommendations", backfilled);
        return backfilled;
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
        String[] parts = s.split("[:\\-]");
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
