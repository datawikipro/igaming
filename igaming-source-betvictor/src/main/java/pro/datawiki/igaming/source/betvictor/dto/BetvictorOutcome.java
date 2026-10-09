package pro.datawiki.igaming.source.betvictor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetvictorOutcome {
    private Long id;
    private String name;
    private String description;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("price_decimal")
    private Double priceDecimal;

    @JsonProperty("odds")
    private Double odds;

    private Double handicap;
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private String code;

    public Double getResolvedPrice() {
        if (price != null) return price;
        if (priceDecimal != null) return priceDecimal;
        if (odds != null) return odds;
        return null;
    }

    public String getResolvedName() {
        if (description != null && !description.isBlank()) return description;
        if (name != null && !name.isBlank()) return name;
        if (type != null && !type.isBlank()) return type;
        return "";
    }
}
