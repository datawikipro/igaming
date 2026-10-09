package pro.datawiki.igaming.source.mozzartbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MozzartOutcome {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("value")
    private Double value;
    @JsonProperty("odd")
    private Double odd;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("param")
    private Double param;

    public Double getResolvedOdds() {
        if (value != null) return value;
        if (odd != null) return odd;
        return price;
    }
}
