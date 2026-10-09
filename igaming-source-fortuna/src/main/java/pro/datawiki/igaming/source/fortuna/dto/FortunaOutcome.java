package pro.datawiki.igaming.source.fortuna.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FortunaOutcome {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("odds")
    private Double odds;
    @JsonProperty("rate")
    private Double rate;
    @JsonProperty("param")
    private Double param;

    public Double getResolvedOdds() {
        if (odds != null) return odds;
        return rate;
    }
}
