package pro.datawiki.igaming.source.matchbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchbookPrice {
    private String side;

    @JsonProperty("decimal-odds")
    private Double decimalOdds;

    @JsonProperty("available-amount")
    private Double availableAmount;

    @JsonProperty("odds-type")
    private String oddsType;
}
