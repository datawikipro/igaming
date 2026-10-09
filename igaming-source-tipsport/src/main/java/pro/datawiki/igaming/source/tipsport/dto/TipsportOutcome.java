package pro.datawiki.igaming.source.tipsport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipsportOutcome {
    private Long id;
    private String name;
    @JsonProperty("shortName")
    private String shortName;
    private String code;
    @JsonProperty("rate")
    private Double rate;
    @JsonProperty("odd")
    private Double odd;
    @JsonProperty("param")
    private Double param;

    public Double getResolvedOdds() {
        if (rate != null) return rate;
        return odd;
    }
}
