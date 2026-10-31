package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_strategy_template", indexes = {
        @Index(name = "idx_strategy_slug", columnList = "slug", unique = true),
        @Index(name = "idx_strategy_default", columnList = "is_system_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiStrategyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, length = 50, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 20)
    @Builder.Default
    private String icon = "🛡️";

    @Column(name = "is_system_default", nullable = false)
    @Builder.Default
    private boolean isSystemDefault = false;

    @Column(name = "min_odd", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal minOdd = BigDecimal.valueOf(1.30);

    @Column(name = "max_odd", precision = 5, scale = 2)
    private BigDecimal maxOdd;

    @Column(name = "min_probability_pct", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal minProbabilityPct = BigDecimal.valueOf(88.0);

    @Column(name = "ladder_step_rule", length = 50, nullable = false)
    @Builder.Default
    private String ladderStepRule = "SAFEST_ELIGIBLE";

    @Column(name = "pure_win_allowed", nullable = false)
    @Builder.Default
    private boolean pureWinAllowed = false;

    @Column(name = "target_risk_tier", length = 20)
    @Builder.Default
    private String targetRiskTier = "LOW";

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
