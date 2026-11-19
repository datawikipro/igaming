package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectedPlayerDto implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("position")
    private String position; // G, D, M, F

    @JsonProperty("jersey_number")
    private Integer jerseyNumber;

    @JsonProperty("rating")
    private Double rating;

    @JsonProperty("start_probability")
    private Double startProbability;

    @JsonProperty("starts_count")
    private int startsCount;

    @JsonProperty("bench_count")
    private int benchCount;

    @JsonProperty("is_contested")
    private boolean isContested;

    @JsonProperty("contender_name")
    private String contenderName;

    @JsonProperty("is_joker")
    private boolean isJoker;

    @JsonProperty("fatigue_risk_pct")
    private Double fatigueRiskPct;
}
