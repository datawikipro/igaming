package pro.datawiki.igaming.analyst.policy;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.dto.ConvictionTier;
import pro.datawiki.igaming.analyst.entity.AiPolicyIntervention;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.repository.AiPolicyInterventionRepository;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;

/**
 * Encapsulates all institutional risk management policies, odds prudence caps,
 * sport-specific volatility caps, and intervention audit recording.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InstitutionalRiskPolicyManager {

    private final AiPolicyInterventionRepository policyInterventionRepository;

    @Getter
    @Builder
    public static class PolicyCapResult {
        private final String rawTier;
        private final String appliedTier;
        private final String ruleCode;
        private final String reason;

        public boolean isCapped() {
            return appliedTier != null && !appliedTier.equals(rawTier);
        }
    }

    /**
     * Applies institutional risk caps based on odds level, market type (e.g. Draw),
     * and sport-specific volatility profile.
     */
    public PolicyCapResult evaluatePrudenceCaps(Sport sport, String market, double marketOddVal, String currentTier) {
        String rawTier = currentTier;
        String appliedTier = currentTier;
        String appliedRuleCode = null;
        String appliedRuleReason = null;

        boolean isDraw = isDrawMarket(market);

        if (isDraw) {
            if ("ML_90".equals(appliedTier) || "ML_80".equals(appliedTier)) {
                appliedTier = "ML_70";
                appliedRuleCode = "DRAW_HIGH_ODDS_PRUDENCE_CAP";
                appliedRuleReason = String.format("Равновесная ничья (коэффициент %.2f): институциональный риск-менеджмент ограничил тир до ML_70 (ставка 250 ₽) для баланса доходности и дисперсии.", marketOddVal);
            }
        } else if ("ML_90".equals(appliedTier)) {
            if (marketOddVal >= 2.00) {
                appliedTier = "ML_70";
                appliedRuleCode = "ODDS_GE_2_00_TIER_CAP";
                appliedRuleReason = String.format("Коэффициент %.2f >= 2.00: институциональный лимит риска снизил тир с ML_90 до ML_70 (ставка 250 ₽ вместо 1000 ₽) во избежание просадки портфеля при высокой дисперсии.", marketOddVal);
            } else if (marketOddVal >= 1.75) {
                appliedTier = "ML_80";
                appliedRuleCode = "ODDS_GE_1_75_TIER_CAP";
                appliedRuleReason = String.format("Коэффициент %.2f >= 1.75: тир ML_90 ограничен до ML_80 (ставка 500 ₽ вместо 1000 ₽) согласно институциональному порогу супер-уверенности.", marketOddVal);
            }
        } else if ("ML_80".equals(appliedTier) && marketOddVal >= 2.50) {
            appliedTier = "ML_70";
            appliedRuleCode = "ODDS_GE_2_50_TIER_CAP";
            appliedRuleReason = String.format("Коэффициент %.2f >= 2.50: тир ML_80 ограничен до ML_70 (ставка 250 ₽ вместо 500 ₽) во избежание овербетинга на аутсайдерах.", marketOddVal);
        }

        // Sport Volatility Cap (polymorphic Sport domain contract)
        if (sport != null && sport.getVolatilityCapTier() != null && !"PASS".equals(appliedTier)) {
            String cap = sport.getVolatilityCapTier();
            if (!cap.equals(appliedTier)) {
                appliedRuleCode = sport.getVolatilityCapRuleCode();
                appliedRuleReason = sport.getVolatilityCapRuleReason(appliedTier);
                appliedTier = cap;
            }
        }

        return PolicyCapResult.builder()
                .rawTier(rawTier)
                .appliedTier(appliedTier)
                .ruleCode(appliedRuleCode)
                .reason(appliedRuleReason)
                .build();
    }

    /**
     * Calculates the Kelly Criterion stake percentage with fractional scaling and maximum tier caps.
     */
    public double calculateKellyStakePct(double marketOddVal, double fairProbPct, String tier) {
        double b = marketOddVal - 1.0;
        double p = fairProbPct / 100.0;
        double q = 1.0 - p;
        double fullKelly = b > 0.0 ? Math.max(0.0, (b * p - q) / b) : 0.0;

        ConvictionTier convTier = ConvictionTier.fromCode(tier);
        double fraction = convTier.getKellyFraction();
        double maxStake = convTier.getMaxKellyStakePct();

        return Math.min(maxStake, Math.max(1.0, fullKelly * fraction * 100.0));
    }

    /**
     * Halves the Kelly stake for solo/racket sports in elevated upset variance zones.
     */
    public BigDecimal applySoloSportUpsetDiscipline(Sport sport, double marketOddVal, int confidence, BigDecimal currentKellyStake) {
        if (sport != null && !sport.isTeamSport() && currentKellyStake != null) {
            if (marketOddVal > 1.58 && confidence < 86) {
                return currentKellyStake.multiply(BigDecimal.valueOf(0.5));
            }
        }
        return currentKellyStake;
    }

    /**
     * Records a formal policy intervention to the audit repository.
     */
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

            AiPolicyIntervention intervention = AiPolicyIntervention.builder()
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
                    .resultStatus("PENDING")
                    .build();

            policyInterventionRepository.save(intervention);
            log.info("🛡️ [Policy Intervention Audit] Recorded {}: match #{} ({}), rawTier={}, appliedTier={}, savedStakeDiff={}",
                    ruleCode, rec.getMatchId(), rec.getSportName(), rawTier, appliedTier, rawStake.subtract(appliedStake));
        } catch (Exception ex) {
            log.warn("Failed recording policy intervention for match #{}: {}", rec.getMatchId(), ex.getMessage());
        }
    }

    public BigDecimal getStakeForTier(String tier) {
        if ("ML_90".equalsIgnoreCase(tier)) return BigDecimal.valueOf(1000.0);
        if ("ML_80".equalsIgnoreCase(tier)) return BigDecimal.valueOf(500.0);
        if ("ML_70".equalsIgnoreCase(tier)) return BigDecimal.valueOf(250.0);
        if ("ML_60".equalsIgnoreCase(tier)) return BigDecimal.valueOf(100.0);
        return BigDecimal.ZERO;
    }

    private boolean isDrawMarket(String market) {
        if (market == null) return false;
        String m = market.toUpperCase();
        return "DRAW".equals(m) || "PX".equals(m) || "X".equals(m);
    }
}
