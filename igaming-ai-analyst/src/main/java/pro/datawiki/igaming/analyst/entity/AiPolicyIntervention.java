package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Institutional Decision Rule Audit & Policy Intervention Record.
 *
 * Persists every risk mitigation, tier cap, or market transformation applied to raw AI/ML predictions.
 * Provides complete auditability of why institutional parameters were capped and measures
 * the exact PnL impact (money saved vs opportunity cost) upon match settlement.
 */
@Entity
@Table(name = "ai_policy_intervention", indexes = {
        @Index(name = "idx_policy_match_id", columnList = "match_id"),
        @Index(name = "idx_policy_rec_id", columnList = "recommendation_id"),
        @Index(name = "idx_policy_rule_code", columnList = "rule_code"),
        @Index(name = "idx_policy_created", columnList = "created_at"),
        @Index(name = "idx_policy_status", columnList = "result_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPolicyIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "recommendation_id")
    private Long recommendationId;

    @Column(name = "sport_name", length = 50)
    private String sportName;

    @Column(name = "team1_name", length = 255)
    private String team1Name;

    @Column(name = "team2_name", length = 255)
    private String team2Name;

    @Column(name = "league_name", length = 255)
    private String leagueName;

    @Column(name = "market", length = 100)
    private String market;

    @Column(name = "recommended_odd", precision = 7, scale = 3)
    private BigDecimal recommendedOdd;

    @Column(name = "raw_conviction_tier", length = 50)
    private String rawConvictionTier;

    @Column(name = "applied_conviction_tier", length = 50)
    private String appliedConvictionTier;

    @Column(name = "raw_stake_rub", precision = 10, scale = 2)
    private BigDecimal rawStakeRub;

    @Column(name = "applied_stake_rub", precision = 10, scale = 2)
    private BigDecimal appliedStakeRub;

    @Column(name = "rule_code", length = 100)
    private String ruleCode;

    @Column(name = "intervention_reason", columnDefinition = "TEXT")
    private String interventionReason;

    @Column(name = "result_status", length = 50)
    @Builder.Default
    private String resultStatus = "PENDING";

    @Column(name = "final_score", length = 50)
    private String finalScore;

    @Column(name = "policy_impact_pnl", precision = 10, scale = 2)
    private BigDecimal policyImpactPnl;
}
