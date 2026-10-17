package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_ai_recommendation", indexes = {
        @Index(name = "idx_ai_rec_match_id", columnList = "match_id"),
        @Index(name = "idx_ai_rec_status", columnList = "result_status"),
        @Index(name = "idx_ai_rec_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id")
    private Long matchId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "team1_name", length = 255)
    private String team1Name;

    @Column(name = "team2_name", length = 255)
    private String team2Name;

    @Column(name = "league_name", length = 255)
    private String leagueName;

    @Column(name = "match_start_time")
    private LocalDateTime matchStartTime;

    @Column(name = "expected_xg", length = 50)
    private String expectedXg;

    @Column(name = "fair_p1", precision = 5, scale = 2)
    private BigDecimal fairP1;

    @Column(name = "fair_px", precision = 5, scale = 2)
    private BigDecimal fairPx;

    @Column(name = "fair_p2", precision = 5, scale = 2)
    private BigDecimal fairP2;

    @Column(name = "fair_over25", precision = 5, scale = 2)
    private BigDecimal fairOver25;

    @Column(name = "fair_under25", precision = 5, scale = 2)
    private BigDecimal fairUnder25;

    @Column(name = "fair_btts_yes", precision = 5, scale = 2)
    private BigDecimal fairBttsYes;

    @Column(name = "fair_btts_no", precision = 5, scale = 2)
    private BigDecimal fairBttsNo;

    @Column(name = "recommended_market", length = 100, nullable = false)
    private String recommendedMarket;

    @Column(name = "recommended_odd", precision = 7, scale = 3)
    private BigDecimal recommendedOdd;

    @Column(name = "best_bookmaker", length = 100)
    private String bestBookmaker;

    @Column(name = "expected_value_pct", precision = 6, scale = 2)
    private BigDecimal expectedValuePct;

    @Column(name = "kelly_stake_pct", precision = 5, scale = 2)
    private BigDecimal kellyStakePct;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "tactical_rationale", columnDefinition = "TEXT")
    private String tacticalRationale;

    @Column(name = "lineup_status", length = 50)
    @Builder.Default
    private String lineupStatus = "ESTIMATED";

    @Column(name = "steam_direction", length = 50)
    @Builder.Default
    private String steamDirection = "NEUTRAL";

    @Column(name = "result_status", length = 50)
    @Builder.Default
    private String resultStatus = "PENDING"; // PENDING, WON, LOST, VOID, CANCELLED

    @Column(name = "final_score", length = 50)
    private String finalScore;

    @Column(name = "actual_pnl", precision = 10, scale = 2)
    private BigDecimal actualPnl;

    @Column(name = "roi_pct", precision = 6, scale = 2)
    private BigDecimal roiPct;
}
