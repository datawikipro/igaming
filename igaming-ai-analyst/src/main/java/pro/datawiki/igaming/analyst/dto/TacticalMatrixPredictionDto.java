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
public class TacticalMatrixPredictionDto implements Serializable {

    @JsonProperty("metric_key")
    private String metricKey;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("category")
    private String category;

    @JsonProperty("projected_condition")
    private String projectedCondition;

    @JsonProperty("confidence_prob")
    private Double confidenceProb;

    @JsonProperty("actual_value")
    private String actualValue;

    @JsonProperty("is_hit")
    private Boolean isHit; // null before settlement, true/false after
}
