package pro.datawiki.igaming.source.favbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavbetOutcome {
    @JsonProperty("outcome_id")
    private Long outcomeId;

    @JsonProperty("outcome_name")
    private String outcomeName;

    @JsonProperty("outcome_param")
    private Double outcomeParam;

    @JsonProperty("outcome_coef")
    private Double outcomeCoef;

    @JsonProperty("outcome_visible")
    private Boolean outcomeVisible;

    @JsonProperty("outcome_type_id")
    private Integer outcomeTypeId;

    @JsonProperty("outcome_short_name")
    private String outcomeShortName;
}
