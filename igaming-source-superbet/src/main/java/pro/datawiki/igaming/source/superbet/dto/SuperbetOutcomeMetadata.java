package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetOutcomeMetadata {
    @JsonProperty("outcome_id")
    private Long outcomeId;
    private String name;
    private String code;
    private String info;
    @JsonProperty("special_bet_value")
    private String specialBetValue;
}
