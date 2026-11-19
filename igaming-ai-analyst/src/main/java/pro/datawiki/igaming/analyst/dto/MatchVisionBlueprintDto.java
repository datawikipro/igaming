package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchVisionBlueprintDto implements Serializable {

    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("sport")
    private String sport;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("tournament")
    private String tournament;

    @JsonProperty("prob_home")
    private BigDecimal probHome;

    @JsonProperty("prob_draw")
    private BigDecimal probDraw;

    @JsonProperty("prob_away")
    private BigDecimal probAway;

    @JsonProperty("most_likely_scores")
    private List<String> mostLikelyScores;

    @JsonProperty("home_projected_lineup")
    private ProjectedLineupDto homeProjectedLineup;

    @JsonProperty("away_projected_lineup")
    private ProjectedLineupDto awayProjectedLineup;

    @JsonProperty("tactical_matrix")
    private List<TacticalMatrixPredictionDto> tacticalMatrix;

    @JsonProperty("scenarios")
    private List<Map<String, Object>> scenarios;

    @JsonProperty("tactical_narrative")
    private String tacticalNarrative;

    @JsonProperty("current_version")
    private Integer currentVersion;

    @JsonProperty("version_tag")
    private String versionTag; // "v1", "v2", "v_confirmed"

    @JsonProperty("delta_summary")
    private String deltaSummary;

    @JsonProperty("versions_history")
    private List<Map<String, Object>> versionsHistory;

    @JsonProperty("versions_history_json")
    private String versionsHistoryJson;

    @JsonProperty("google_trends")
    private GoogleTrendsMetricsDto googleTrends;

    @JsonProperty("google_trends_json")
    private String googleTrendsJson;

    @JsonProperty("status")
    private String status; // PROJECTED, CONFIRMED, SETTLED

    @JsonProperty("starter_hit_rate_home")
    private Double starterHitRateHome;

    @JsonProperty("starter_hit_rate_away")
    private Double starterHitRateAway;

    @JsonProperty("matrix_accuracy_pct")
    private Double matrixAccuracyPct;
}
