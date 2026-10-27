package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MlPredictionDto {

    @JsonProperty("has_ml_evaluation")
    private boolean hasMlEvaluation;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("sport")
    private String sport;

    @JsonProperty("prob_home")
    private BigDecimal probHome;

    @JsonProperty("prob_draw")
    private BigDecimal probDraw;

    @JsonProperty("prob_away")
    private BigDecimal probAway;

    @JsonProperty("prob_over")
    private BigDecimal probOver;

    @JsonProperty("prob_under")
    private BigDecimal probUnder;

    @JsonProperty("fair_odds_home")
    private BigDecimal fairOddsHome;

    @JsonProperty("fair_odds_draw")
    private BigDecimal fairOddsDraw;

    @JsonProperty("fair_odds_away")
    private BigDecimal fairOddsAway;

    @JsonProperty("fair_odds_over")
    private BigDecimal fairOddsOver;

    @JsonProperty("fair_odds_under")
    private BigDecimal fairOddsUnder;

    @JsonProperty("confidence_tier")
    private String confidenceTier;

    @JsonProperty("recommended_market")
    private String recommendedMarket;

    @JsonProperty("recommended_prob")
    private BigDecimal recommendedProb;

    @JsonProperty("cohort_accuracy_pct")
    private BigDecimal cohortAccuracyPct;

    @JsonProperty("tactical_narrative")
    private String tacticalNarrative;

    @JsonProperty("bench_jokers_found")
    private java.util.List<String> benchJokersFound;

    @JsonProperty("bench_chaos_warning")
    private boolean benchChaosWarning;

    @JsonProperty("is_lineup_projected")
    private Boolean isLineupProjected;

    @JsonProperty("lineup_resilience_pct")
    private BigDecimal lineupResiliencePct;

    @JsonProperty("key_player_dependencies")
    private java.util.List<String> keyPlayerDependencies;

    @JsonProperty("projected_starters_home")
    private java.util.List<java.util.Map<String, Object>> projectedStartersHome;

    @JsonProperty("projected_starters_away")
    private java.util.List<java.util.Map<String, Object>> projectedStartersAway;

    @JsonProperty("projected_bench_home")
    private java.util.List<java.util.Map<String, Object>> projectedBenchHome;

    @JsonProperty("projected_bench_away")
    private java.util.List<java.util.Map<String, Object>> projectedBenchAway;

    @JsonProperty("scenarios_details")
    private java.util.List<java.util.Map<String, Object>> scenariosDetails;

    @JsonProperty("worst_case_floor")
    private java.util.Map<String, Object> worstCaseFloor;
}
