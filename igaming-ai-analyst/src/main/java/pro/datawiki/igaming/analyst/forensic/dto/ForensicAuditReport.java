package pro.datawiki.igaming.analyst.forensic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized forensic audit report parsed from LLM JSON response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForensicAuditReport {
    @JsonProperty("match_id")
    private Long matchId;

    @JsonProperty("recommendation_id")
    private Long recommendationId;

    @JsonProperty("sport_code")
    private String sportCode;

    @JsonProperty("primary_failure_mechanism")
    private String primaryFailureMechanism;

    @JsonProperty("inflection_minute")
    private Integer inflectionMinute;

    @JsonProperty("inflection_description")
    private String inflectionDescription;

    @JsonProperty("missing_feature_name")
    private String missingFeatureName;

    @JsonProperty("missing_feature_formula")
    private String missingFeatureFormula;

    @JsonProperty("weight_adjustment_recommendation")
    private String weightAdjustmentRecommendation;

    @JsonProperty("confidence_score")
    private Integer confidenceScore;
}
