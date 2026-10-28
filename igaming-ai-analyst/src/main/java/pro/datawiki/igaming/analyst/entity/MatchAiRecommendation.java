package pro.datawiki.igaming.analyst.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Column(name = "sport_name", length = 50)
    private String sportName;

    @Column(name = "league_name", length = 255)
    private String leagueName;

    @Column(name = "match_slug", length = 255)
    private String matchSlug;

    @Column(name = "match_url", length = 512)
    private String matchUrl;

    @Column(name = "tournament_slug", length = 128)
    private String tournamentSlug;

    @Column(name = "sport_slug", length = 64)
    private String sportSlug;

    @Column(name = "match_start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime matchStartTime;

    @Column(name = "expected_xg", length = 50)
    private String expectedXg;

    @Column(name = "fair_p1", precision = 10, scale = 2)
    private BigDecimal fairP1;

    @Column(name = "fair_px", precision = 10, scale = 2)
    private BigDecimal fairPx;

    @Column(name = "fair_p2", precision = 10, scale = 2)
    private BigDecimal fairP2;

    @Column(name = "fair_over25", precision = 10, scale = 2)
    private BigDecimal fairOver25;

    @Column(name = "fair_under25", precision = 10, scale = 2)
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

    @Column(name = "expected_value_pct", precision = 10, scale = 2)
    private BigDecimal expectedValuePct;

    @Column(name = "kelly_stake_pct", precision = 10, scale = 2)
    private BigDecimal kellyStakePct;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "tactical_rationale", columnDefinition = "TEXT")
    private String tacticalRationale;

    @Column(name = "lineup_status", length = 50)
    @Builder.Default
    private String lineupStatus = "ESTIMATED";

    @Column(name = "lineup_resilience_pct", precision = 5, scale = 2)
    private BigDecimal lineupResiliencePct;

    @Column(name = "key_player_dependencies", columnDefinition = "TEXT")
    private String keyPlayerDependencies;

    @Column(name = "projected_lineup_json", columnDefinition = "TEXT")
    private String projectedLineupJson;

    @Column(name = "evaluation_stage", length = 50)
    @Builder.Default
    private String evaluationStage = "PRE_LINEUP"; // PRE_LINEUP (Screening), LINEUP_CONFIRMED (Final)

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

    @Column(name = "conviction_tier", length = 50)
    @Builder.Default
    private String convictionTier = "GOOD_PICK"; // CONFIDENT, GOOD_PICK, PASS

    @Column(name = "core_hypothesis", columnDefinition = "TEXT")
    private String coreHypothesis;

    @Column(name = "identified_risk", columnDefinition = "TEXT")
    private String identifiedRisk;

    @Column(name = "shadow_picks_json", columnDefinition = "TEXT")
    private String shadowPicksJson;

    @Column(name = "post_mortem_report", columnDefinition = "TEXT")
    private String postMortemReport;

    @Column(name = "error_category", length = 50)
    private String errorCategory; // WON_AS_PLANNED, BLACK_SWAN, SHADOW_DEFENSE_PROVEN, TACTICAL_BLIND_SPOT

    @Column(name = "lesson_learned", columnDefinition = "TEXT")
    private String lessonLearned;

    @Column(name = "available_bookmakers", columnDefinition = "TEXT")
    private String availableBookmakers;

    @Column(name = "bookmaker_urls_json", columnDefinition = "TEXT")
    private String bookmakerUrlsJson;
}
