package pro.datawiki.igaming.source.vbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VbetStake {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("F")
    private Double factor;

    @JsonProperty("A")
    private Double argument; // line/param for totals and handicaps

    @JsonProperty("IsWinner")
    private Boolean isWinner;
}
