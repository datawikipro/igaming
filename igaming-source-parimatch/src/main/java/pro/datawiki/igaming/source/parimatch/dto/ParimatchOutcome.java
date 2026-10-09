package pro.datawiki.igaming.source.parimatch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParimatchOutcome {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("param")
    private Double param;

    @JsonProperty("type")
    private String type;

    public Double getEffectiveOdd() {
        if (value != null && value > 1.0) return value;
        if (price != null && price > 1.0) return price;
        return null;
    }
}
