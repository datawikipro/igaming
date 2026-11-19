package pro.datawiki.igaming.analyst.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dedicated persistence entity for Match Vision Blueprint & Tactical Matrices.
 * Stores comprehensive multi-parameter forecasts, projected lineups, and post-match accuracy.
 */
@Entity
@Table(name = "match_vision_blueprint", indexes = {
        @Index(name = "idx_mv_match_id", columnList = "match_id"),
        @Index(name = "idx_mv_status", columnList = "status"),
        @Index(name = "idx_mv_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchVisionBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "sport_name", length = 50)
    private String sportName;

    @Column(name = "home_team", length = 255)
    private String homeTeam;

    @Column(name = "away_team", length = 255)
    private String awayTeam;

    @Column(name = "tournament_name", length = 255)
    private String tournamentName;

    @Column(name = "match_start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime matchStartTime;

    @Column(name = "prob_home", precision = 6, scale = 4)
    private BigDecimal probHome;

    @Column(name = "prob_draw", precision = 6, scale = 4)
    private BigDecimal probDraw;

    @Column(name = "prob_away", precision = 6, scale = 4)
    private BigDecimal probAway;

    @Column(name = "projected_lineup_home_json", columnDefinition = "TEXT")
    private String projectedLineupHomeJson;

    @Column(name = "projected_lineup_away_json", columnDefinition = "TEXT")
    private String projectedLineupAwayJson;

    @Column(name = "tactical_matrix_json", columnDefinition = "TEXT")
    private String tacticalMatrixJson;

    @Column(name = "scenarios_json", columnDefinition = "TEXT")
    private String scenariosJson;

    @Column(name = "tactical_narrative", columnDefinition = "TEXT")
    private String tacticalNarrative;

    @Column(name = "current_version")
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "versions_history_json", columnDefinition = "TEXT")
    @Builder.Default
    private String versionsHistoryJson = "[]";

    @Column(name = "google_trends_json", columnDefinition = "TEXT")
    @Builder.Default
    private String googleTrendsJson = "{}";

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "PROJECTED"; // PROJECTED, CONFIRMED, SETTLED

    @Column(name = "starter_hit_rate_home", precision = 5, scale = 2)
    private BigDecimal starterHitRateHome;

    @Column(name = "starter_hit_rate_away", precision = 5, scale = 2)
    private BigDecimal starterHitRateAway;

    @Column(name = "matrix_accuracy_pct", precision = 5, scale = 2)
    private BigDecimal matrixAccuracyPct;

    @Column(name = "final_score", length = 50)
    private String finalScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
