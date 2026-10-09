package pro.datawiki.igaming.source.sts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StsOutcome {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("rate")
    private Double rate;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("param")
    private Double param;

    public Double getResolvedOdds() {
        if (rate != null) return rate;
        return price;
    }
}
