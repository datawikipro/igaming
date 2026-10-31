package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_strategy_vote", indexes = {
        @Index(name = "idx_vote_rec_id", columnList = "recommendation_id"),
        @Index(name = "idx_vote_match_id", columnList = "match_id"),
        @Index(name = "idx_vote_strategy", columnList = "strategy_slug"),
        @Index(name = "idx_vote_status", columnList = "result_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiStrategyVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recommendation_id", nullable = false)
    private Long recommendationId;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "strategy_slug", nullable = false, length = 50)
    private String strategySlug;

    @Column(name = "strategy_name", nullable = false, length = 100)
    private String strategyName;

    @Column(name = "market", nullable = false, length = 50)
    private String market;

    @Column(name = "market_label", length = 100)
    private String marketLabel;

    @Column(name = "recommended_odd", precision = 6, scale = 3)
    private BigDecimal recommendedOdd;

    @Column(name = "fair_probability", precision = 5, scale = 2)
    private BigDecimal fairProbability;

    @Column(name = "expected_value_pct", precision = 6, scale = 2)
    private BigDecimal expectedValuePct;

    @Column(name = "vote_decision", length = 20)
    @Builder.Default
    private String voteDecision = "PICK"; // PICK, PASS

    @Column(name = "stake_rub", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stakeRub = BigDecimal.valueOf(250.0);

    @Column(name = "result_status", length = 20)
    @Builder.Default
    private String resultStatus = "PENDING"; // PENDING, WON, LOST, VOID, SKIPPED

    @Column(name = "final_score", length = 50)
    private String finalScore;

    @Column(name = "actual_pnl", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal actualPnl = BigDecimal.ZERO;

    @Column(name = "roi_pct", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal roiPct = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;
}
