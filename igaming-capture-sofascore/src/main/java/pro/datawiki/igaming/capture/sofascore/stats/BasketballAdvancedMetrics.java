package pro.datawiki.igaming.capture.sofascore.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Institutional advanced metrics model for Basketball (NBA / EuroLeague).
 * Encompasses Pace (possessions / 48m), Offensive Rating (ORtg),
 * Defensive Rating (DRtg), Net Rating, True Shooting % (TS%),
 * and Bench Rotation Depth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasketballAdvancedMetrics implements Serializable {

    // ── Pace & Possessions ────────────────────────────────────────────────────
    /**
     * Estimated possessions normalized to 48 minutes of play.
     */
    @JsonProperty("pace")
    private Double pace;

    @JsonProperty("home_possessions")
    private Double homePossessions;

    @JsonProperty("away_possessions")
    private Double awayPossessions;

    // ── Four Factors: Offensive & Defensive Rating (per 100 possessions) ───────
    /**
     * Home ORtg: points scored per 100 possessions.
     */
    @JsonProperty("home_offensive_rating")
    private Double homeOffensiveRating;

    /**
     * Away ORtg: points scored per 100 possessions.
     */
    @JsonProperty("away_offensive_rating")
    private Double awayOffensiveRating;

    /**
     * Home DRtg: points allowed per 100 opponent possessions.
     */
    @JsonProperty("home_defensive_rating")
    private Double homeDefensiveRating;

    /**
     * Away DRtg: points allowed per 100 opponent possessions.
     */
    @JsonProperty("away_defensive_rating")
    private Double awayDefensiveRating;

    /**
     * Home Net Rating: home_offensive_rating - home_defensive_rating.
     */
    @JsonProperty("home_net_rating")
    private Double homeNetRating;

    /**
     * Away Net Rating: away_offensive_rating - away_defensive_rating.
     */
    @JsonProperty("away_net_rating")
    private Double awayNetRating;

    // ── True Shooting Efficiency (TS%) ────────────────────────────────────────
    /**
     * Home True Shooting %: Points / (2 * (FGA + 0.44 * FTA)) * 100.
     */
    @JsonProperty("home_true_shooting_pct")
    private Double homeTrueShootingPct;

    /**
     * Away True Shooting %: Points / (2 * (FGA + 0.44 * FTA)) * 100.
     */
    @JsonProperty("away_true_shooting_pct")
    private Double awayTrueShootingPct;

    @JsonProperty("home_effective_field_goal_pct")
    private Double homeEffectiveFieldGoalPct;

    @JsonProperty("away_effective_field_goal_pct")
    private Double awayEffectiveFieldGoalPct;

    // ── Bench Rotation Depth & Second Unit Metrics ────────────────────────────
    /**
     * Number of bench players receiving meaningful rotation minutes (>= 10.0 minutes).
     */
    @JsonProperty("home_bench_rotation_depth")
    private Integer homeBenchRotationDepth;

    @JsonProperty("away_bench_rotation_depth")
    private Integer awayBenchRotationDepth;

    /**
     * Total points scored by bench players.
     */
    @JsonProperty("home_bench_points")
    private Integer homeBenchPoints;

    @JsonProperty("away_bench_points")
    private Integer awayBenchPoints;

    /**
     * Share of team total points scored by the bench (0.00 to 1.00).
     */
    @JsonProperty("home_bench_scoring_share")
    private Double homeBenchScoringShare;

    @JsonProperty("away_bench_scoring_share")
    private Double awayBenchScoringShare;

    /**
     * Total minutes logged by the bench.
     */
    @JsonProperty("home_bench_minutes")
    private Double homeBenchMinutes;

    @JsonProperty("away_bench_minutes")
    private Double awayBenchMinutes;
}
